package app.ai.config;

public record ReframeConfig(double outputAspectWidth,
                             double outputAspectHeight,
                             boolean enableGpu) {
    public ReframeConfig {
        if (outputAspectWidth <= 0 || outputAspectHeight <= 0) {
            throw new IllegalArgumentException("Aspect ratio components must be > 0");
        }
    }

    public static ReframeConfig vertical9x16(boolean enableGpu) {
        return new ReframeConfig(9, 16, enableGpu);
    }

    public static ReframeConfig square(boolean enableGpu) {
        return new ReframeConfig(1, 1, enableGpu);
    }
}
