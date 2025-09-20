package app.media.probe;

import app.media.TestMediaFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaProbeTest {
    @Test
    void probeReturnsMetadataForSampleClip() throws Exception {
        Path tempDir = Files.createTempDirectory("media-probe-test");
        Path sample = tempDir.resolve("sample.mp4");
        TestMediaFactory.createSampleVideo(sample, 320, 180, 24, 24.0);

        MediaProbe probe = new MediaProbe();
        MediaMetadata metadata = probe.probe(sample);

        assertEquals(320, metadata.width());
        assertEquals(180, metadata.height());
        assertTrue(metadata.durationSeconds() > 0.5);
        assertTrue(metadata.frameRate() >= 23.0 && metadata.frameRate() <= 25.0);
        assertNotNull(metadata.videoCodec());
        assertNotNull(metadata.audioCodec());
    }
}
