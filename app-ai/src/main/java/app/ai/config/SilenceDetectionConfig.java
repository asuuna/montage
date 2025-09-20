package app.ai.config;

import java.time.Duration;
import java.util.Objects;

public final class SilenceDetectionConfig {
    private final double rmsThreshold;
    private final Duration minimumSilence;

    public SilenceDetectionConfig(double rmsThreshold, Duration minimumSilence) {
        if (rmsThreshold <= 0.0) {
            throw new IllegalArgumentException("rmsThreshold must be > 0");
        }
        this.rmsThreshold = rmsThreshold;
        this.minimumSilence = Objects.requireNonNull(minimumSilence, "minimumSilence");
    }

    public static SilenceDetectionConfig defaultConfig() {
        return new SilenceDetectionConfig(0.02, Duration.ofMillis(500));
    }

    public double rmsThreshold() {
        return rmsThreshold;
    }

    public Duration minimumSilence() {
        return minimumSilence;
    }
}
