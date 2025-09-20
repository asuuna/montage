package app.timeline;

import app.timeline.autosave.AutosaveManager;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutosaveManagerTest {
    @Test
    void autosaveTriggeredOnSchedule() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AutosaveManager manager = new AutosaveManager(latch::countDown);
        manager.start(Duration.ofMillis(50));
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
        manager.shutdown();
    }
}
