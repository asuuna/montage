package app.render;

import app.render.storage.JobStore;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderQueue.class);

    private final PriorityBlockingQueue<QueuedJob> queue = new PriorityBlockingQueue<>(11, Comparator.comparingInt(QueuedJob::priority).reversed());
    private final ExecutorService executor;
    private final JobStore jobStore;
    private final ConcurrentHashMap<UUID, Future<?>> running = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RenderTaskContext> contexts = new ConcurrentHashMap<>();

    public RenderQueue(Path jobStorePath, int maxWorkers) throws IOException {
        this.jobStore = new JobStore(jobStorePath);
        this.executor = Executors.newFixedThreadPool(Math.max(1, maxWorkers));
        jobStore.pending().forEach(job -> submit(job, 0));
    }

    public CompletableFuture<Void> enqueue(RenderJob job, int priority) throws IOException {
        jobStore.save(job);
        return submit(job, priority);
    }

    private CompletableFuture<Void> submit(RenderJob job, int priority) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        queue.offer(new QueuedJob(job, priority, future));
        schedule();
        return future;
    }

    private void schedule() {
        while (!queue.isEmpty() && running.size() < ((java.util.concurrent.ThreadPoolExecutor) executor).getMaximumPoolSize()) {
            QueuedJob qj = queue.poll();
            if (qj == null) {
                break;
            }
            Runnable runnable = () -> executeJob(qj.job(), qj.future());
            Future<?> future = executor.submit(runnable);
            running.put(qj.job().getId(), future);
        }
    }

    private void executeJob(RenderJob job, CompletableFuture<Void> future) {
        LOGGER.info("Starting render job {} -> {}", job.getId(), job.getOutput());
        RenderTaskContext context = new RenderTaskContext(progress -> LOGGER.debug("Job {} progress {}", job.getId(), progress));
        contexts.put(job.getId(), context);
        ProgressReporter reporter = new ProgressReporter(snapshot -> LOGGER.debug("Job {} {}%", job.getId(), snapshot.progress() * 100));
        try {
            RenderDag dag = buildDag(job);
            List<RenderTask> order = dag.topologicalOrder();
            reporter.setTotal(order.size());
            for (RenderTask task : order) {
                context.checkCancelled();
                Instant start = Instant.now();
                task.execute(context);
                reporter.increment(1);
                Duration duration = Duration.between(start, Instant.now());
                LOGGER.info("Task {} completed in {} ms", task.getId(), duration.toMillis());
            }
            jobStore.remove(job.getId());
            future.complete(null);
            LOGGER.info("Render job {} completed", job.getId());
        } catch (CancellationException cancel) {
            LOGGER.warn("Render job {} cancelled", job.getId());
            future.completeExceptionally(cancel);
        } catch (Exception ex) {
            LOGGER.error("Render job {} failed", job.getId(), ex);
            future.completeExceptionally(ex);
        } finally {
            running.remove(job.getId());
            contexts.remove(job.getId());
            schedule();
        }
    }

    private RenderDag buildDag(RenderJob job) {
        RenderDag dag = new RenderDag();
        RenderTask decode = new SimpleTask("decode", Duration.ofSeconds(1));
        RenderTask analyze = new SimpleTask("analyze", Duration.ofSeconds(2), decode);
        RenderTask effects = new SimpleTask("effects", Duration.ofSeconds(2), analyze);
        RenderTask encode = new SimpleTask("encode", Duration.ofSeconds(3), effects);
        dag.addTask(decode);
        dag.addTask(analyze);
        dag.addTask(effects);
        dag.addTask(encode);
        return dag;
    }

    public void cancel(UUID jobId) {
        contexts.computeIfPresent(jobId, (id, ctx) -> {
            ctx.cancel();
            return ctx;
        });
        Future<?> future = running.remove(jobId);
        if (future != null) {
            future.cancel(true);
        } else {
            queue.removeIf(queuedJob -> queuedJob.job().getId().equals(jobId));
            try {
                jobStore.remove(jobId);
            } catch (IOException e) {
                LOGGER.warn("Failed to remove cancelled job {} from store", jobId, e);
            }
        }
    }

    public void shutdown() {
        queue.clear();
        executor.shutdownNow();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private record QueuedJob(RenderJob job, int priority, CompletableFuture<Void> future) { }

    private static class SimpleTask implements RenderTask {
        private final UUID id = UUID.randomUUID();
        private final String name;
        private final Duration duration;
        private final List<RenderTask> dependencies = new ArrayList<>();

        SimpleTask(String name, Duration duration, RenderTask... deps) {
            this.name = name;
            this.duration = duration;
            if (deps != null) {
                dependencies.addAll(List.of(deps));
            }
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public List<RenderTask> dependencies() {
            return dependencies;
        }

        @Override
        public void execute(RenderTaskContext context) throws Exception {
            context.checkCancelled();
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new CancellationException("Render task interrupted");
            }
        }

        @Override
        public Duration estimatedDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

