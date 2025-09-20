package app.media.cache;

import java.awt.image.BufferedImage;
import java.io.IOException;

@FunctionalInterface
public interface FrameLoader {
    BufferedImage load() throws IOException;
}
