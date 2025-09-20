package app.ai;

import app.ai.audio.SilenceDetectionService;
import app.ai.audio.SilenceRange;
import app.ai.config.SilenceDetectionConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SilenceDetectionServiceTest {
    @Test
    void findsSilenceSections() throws Exception {
        Path tempVideo = Files.createTempFile("silence-test", ".mp4");
        TestClipFactory.createSceneChangeClip(tempVideo);
        SilenceDetectionService service = new SilenceDetectionService(new SilenceDetectionConfig(0.05, Duration.ofMillis(300)));
        List<SilenceRange> silences = service.detectSilence(tempVideo);
        assertFalse(silences.isEmpty(), "Expected at least one silence segment");
        assertTrue(silences.stream().anyMatch(range -> range.duration().compareTo(Duration.ofMillis(300)) >= 0));
    }
}
