package app.media.frame;

import app.media.TestMediaFactory;
import app.media.cache.FrameCache;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FrameExtractorTest {
    @Test
    void extractReturnsFrameAndUsesCache() throws Exception {
        Path mediaDir = Files.createTempDirectory("frame-extractor");
        Path sample = mediaDir.resolve("sample.mp4");
        TestMediaFactory.createSampleVideo(sample, 320, 240, 30, 30.0);

        Path cacheDir = Files.createTempDirectory("frame-cache");
        FrameCache cache = new FrameCache(cacheDir, 2);
        FrameExtractor extractor = new FrameExtractor(cache);

        BufferedImage first = extractor.extract(sample, 0.5);
        assertNotNull(first);
        assertEquals(320, first.getWidth());

        FrameCache newCacheInstance = new FrameCache(cacheDir, 2);
        FrameExtractor extractorWithDisk = new FrameExtractor(newCacheInstance);
        BufferedImage cached = extractorWithDisk.extract(sample, 0.5);
        assertNotNull(cached);
        assertEquals(320, cached.getWidth());
    }
}
