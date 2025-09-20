package app.ai.audio;

import java.time.Duration;

public record SilenceRange(Duration start, Duration end) {
    public SilenceRange {
        if (start.isNegative() || end.isNegative() || end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("Invalid silence boundaries");
        }
    }

    public Duration duration() {
        return end.minus(start);
    }
}
