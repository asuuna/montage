package app.ai;

import app.ai.config.SceneDetectionConfig;
import app.ai.scene.SceneDetectionService;
import app.ai.scene.SceneSegment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SceneDetectionServiceTest {
    @Test
    void detectsSceneChanges() throws Exception {
        Path tempVideo = Files.createTempFile("scene-test", ".mp4");
        TestClipFactory.createSceneChangeClip(tempVideo);
        SceneDetectionService service = new SceneDetectionService(SceneDetectionConfig.defaultConfig());
        List<SceneSegment> segments = service.detect(tempVideo);
        assertTrue(segments.size() >= 2, "Expected at least two scenes");
        double totalSeconds = segments.stream().mapToDouble(s -> s.duration().toMillis() / 1000.0).sum();
        assertTrue(totalSeconds >= 4.0);
    }
}
