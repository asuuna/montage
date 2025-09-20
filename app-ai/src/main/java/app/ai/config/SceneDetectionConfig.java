package app.ai.config;

import java.time.Duration;
import java.util.Objects;

public final class SceneDetectionConfig {
    private final double threshold;
    private final Duration minimumSceneLength;

    public SceneDetectionConfig(double threshold, Duration minimumSceneLength) {
        if (threshold <= 0.0) {
            throw new IllegalArgumentException("threshold must be > 0");
        }
        this.threshold = threshold;
        this.minimumSceneLength = Objects.requireNonNull(minimumSceneLength, "minimumSceneLength");
    }

    public static SceneDetectionConfig defaultConfig() {
        return new SceneDetectionConfig(35.0, Duration.ofSeconds(1));
    }

    public double threshold() {
        return threshold;
    }

    public Duration minimumSceneLength() {
        return minimumSceneLength;
    }
}
