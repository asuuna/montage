package app.media.export;

import app.media.TestMediaFactory;
import app.media.probe.MediaMetadata;
import app.media.probe.MediaProbe;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaExporterTest {
    @Test
    void exportTranscodesClipWithPreset() throws Exception {
        Path workDir = Files.createTempDirectory("media-export");
        Path input = workDir.resolve("input.mp4");
        Path output = workDir.resolve("output.mp4");
        TestMediaFactory.createSampleVideo(input, 320, 240, 30, 30.0);

        MediaExportRequest request = new MediaExportRequest(
                input,
                output,
                ExportPreset.BALANCED,
                null,
                null,
                30.0
        );

        AtomicBoolean progressReached = new AtomicBoolean(false);
        MediaExporter exporter = new MediaExporter();
        exporter.export(request, progress -> {
            if (progress >= 1.0) {
                progressReached.set(true);
            }
        });

        assertTrue(Files.exists(output));
        MediaProbe probe = new MediaProbe();
        MediaMetadata metadata = probe.probe(output);
        assertEquals(320, metadata.width());
        assertEquals(240, metadata.height());
        assertTrue(progressReached.get());
    }
}
