package app.render;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderQueueTest {
    private RenderQueue queue;
    private Path storePath;

    @BeforeEach
    void setUp() throws Exception {
        storePath = Files.createTempFile("render-jobs", ".json");
        queue = new RenderQueue(storePath, 2);
    }

    @AfterEach
    void tearDown() {
        if (queue != null) {
            queue.shutdown();
        }
        try {
            Files.deleteIfExists(storePath);
        } catch (Exception ignored) {
        }
    }

    @Test
    void enqueueCompletesJob() throws Exception {
        RenderJob job = new RenderJob(Path.of("demo.project"), Path.of("demo.mp4"), RenderPreset.YOUTUBE_1080P,
                Duration.ZERO, Duration.ofMinutes(3), null);
        var future = queue.enqueue(job, 10);
        assertTrue(future.get(10, TimeUnit.SECONDS) == null);
    }

    @Test
    void cancelStopsRunningJob() throws Exception {
        RenderJob job = new RenderJob(Path.of("demo.project"), Path.of("demo.mp4"), RenderPreset.YOUTUBE_1080P,
                Duration.ZERO, Duration.ofMinutes(3), null);
        var future = queue.enqueue(job, 10);
        queue.cancel(job.getId());
        queue.shutdown();
        boolean cancelled = false;
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (CancellationException | ExecutionException expected) {
            cancelled = true;
        }
        assertTrue(cancelled);
    }
}
