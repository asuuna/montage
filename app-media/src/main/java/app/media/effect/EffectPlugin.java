package app.media.effect;

import java.awt.image.BufferedImage;

public interface EffectPlugin {
    BufferedImage apply(BufferedImage input) throws Exception;
}
