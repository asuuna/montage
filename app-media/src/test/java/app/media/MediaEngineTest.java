package app.media;

import app.domain.Project;
import app.media.cache.FrameCache;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MediaEngineTest {
    @Test
    void initializeDoesNotThrow() throws Exception {
        FrameCache cache = new FrameCache(Path.of(System.getProperty("java.io.tmpdir"), "media-engine-cache"), 10);
        MediaEngine engine = new MediaEngine(cache);
        var project = new Project("Test", Path.of("/tmp"));
        assertDoesNotThrow(() -> engine.initialize(project));
    }
}
