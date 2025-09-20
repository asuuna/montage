package app.media.env;

import app.media.MediaEnvironment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaEnvironmentTest {
    @Test
    void detectReturnsAvailabilityInfo() {
        MediaEnvironment environment = MediaEnvironment.detect();
        assertNotNull(environment.diagnosticsEntries().get("installHelp"));
        // FFmpeg should be available because JavaCV bundles it when tests run locally
        assertTrue(environment.isFfmpegAvailable());
    }
}
