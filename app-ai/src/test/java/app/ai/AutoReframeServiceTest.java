package app.ai;

import app.ai.config.ReframeConfig;
import app.ai.reframe.AutoReframeService;
import app.ai.reframe.ReframeResult;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoReframeServiceTest {
    @Test
    void producesReframedOutput() throws Exception {
        Path input = Files.createTempFile("reframe-input", ".mp4");
        TestClipFactory.createSceneChangeClip(input);
        Path output = Files.createTempFile("reframe-output", ".mp4");
        AutoReframeService service = new AutoReframeService(ReframeConfig.vertical9x16(false));
        ReframeResult result = service.reframe(input, output);
        assertTrue(Files.size(result.output()) > 0);
        assertTrue(result.processedDuration().toMillis() > 0);
    }
}
