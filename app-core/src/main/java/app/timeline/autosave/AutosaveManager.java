package app.timeline.autosave;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutosaveManager {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "montage-autosave");
        thread.setDaemon(true);
        return thread;
    });
    private final Runnable autosaveTask;
    private ScheduledFuture<?> scheduledFuture;

    public AutosaveManager(Runnable autosaveTask) {
        this.autosaveTask = Objects.requireNonNull(autosaveTask, "autosaveTask");
    }

    public void start(Duration interval) {
        Objects.requireNonNull(interval, "interval");
        stop();
        scheduledFuture = executor.scheduleAtFixedRate(autosaveTask,
                interval.toMillis(),
                interval.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void triggerNow() {
        executor.execute(autosaveTask);
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    public void shutdown() {
        stop();
        executor.shutdownNow();
    }
}
