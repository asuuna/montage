package app.media.frame;

import app.media.cache.FrameCache;
import app.media.cache.FrameLoader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class FrameExtractor {
    private final FrameCache frameCache;

    public FrameExtractor(FrameCache frameCache) {
        this.frameCache = frameCache;
    }

    public BufferedImage extract(Path mediaPath, double seconds) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds must be >= 0");
        }
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }
        String key = mediaPath.toAbsolutePath() + "#" + String.format("%.3f", seconds);
        if (frameCache == null) {
            return loadFrame(mediaPath, seconds);
        }
        return frameCache.get(key, new CacheLoader(mediaPath, seconds));
    }

    private static BufferedImage loadFrame(Path mediaPath, double seconds) throws IOException {
        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile());
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();
            long timestampMicros = (long) (seconds * 1_000_000);
            grabber.setTimestamp(Math.max(0, timestampMicros));
            Frame frame = grabber.grabImage();
            if (frame == null) {
                frame = grabber.grab();
            }
            if (frame == null) {
                throw new IOException("No frame available at " + seconds + "s for " + mediaPath);
            }
            BufferedImage image = converter.getBufferedImage(frame);
            grabber.stop();
            return image;
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new IOException("Failed to extract frame from " + mediaPath, e);
        }
    }

    private static final class CacheLoader implements FrameLoader {
        private final Path mediaPath;
        private final double seconds;

        private CacheLoader(Path mediaPath, double seconds) {
            this.mediaPath = mediaPath;
            this.seconds = seconds;
        }

        @Override
        public BufferedImage load() throws IOException {
            return loadFrame(mediaPath, seconds);
        }
    }
}
