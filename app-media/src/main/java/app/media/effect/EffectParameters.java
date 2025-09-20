package app.media.effect;

import java.nio.file.Path;
import java.util.Objects;

public record EffectParameters(double brightness,
                               double contrast,
                               double saturation,
                               Path lutPath,
                               String titleText,
                               double titleOpacity,
                               double audioDuckingDb) {
    public EffectParameters {
        if (brightness < -1.0 || brightness > 1.0) {
            throw new IllegalArgumentException("brightness must be between -1 and 1");
        }
        if (contrast < -1.0 || contrast > 1.0) {
            throw new IllegalArgumentException("contrast must be between -1 and 1");
        }
        if (saturation < -1.0 || saturation > 1.0) {
            throw new IllegalArgumentException("saturation must be between -1 and 1");
        }
        if (titleOpacity < 0.0 || titleOpacity > 1.0) {
            throw new IllegalArgumentException("titleOpacity must be between 0 and 1");
        }
        if (AudioUtils.isNaN(audioDuckingDb)) {
            throw new IllegalArgumentException("audioDuckingDb must be a valid number");
        }
    }

    public static EffectParameters identity() {
        return new EffectParameters(0.0, 0.0, 0.0, null, null, 0.0, 0.0);
    }

    private static final class AudioUtils {
        private static boolean isNaN(double value) {
            return Double.isNaN(value) || Double.isInfinite(value);
        }
    }
}
