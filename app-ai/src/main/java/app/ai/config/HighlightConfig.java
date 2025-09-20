package app.ai.config;

public record HighlightConfig(double motionWeight,
                              double audioWeight,
                              double faceWeight,
                              double keywordWeight,
                              double targetDurationSeconds) {
    public HighlightConfig {
        if (motionWeight < 0 || audioWeight < 0 || faceWeight < 0 || keywordWeight < 0) {
            throw new IllegalArgumentException("weights must be >= 0");
        }
        if (targetDurationSeconds <= 0) {
            throw new IllegalArgumentException("targetDurationSeconds must be > 0");
        }
    }

    public static HighlightConfig defaultConfig() {
        return new HighlightConfig(0.35, 0.25, 0.25, 0.15, 60.0);
    }
}
