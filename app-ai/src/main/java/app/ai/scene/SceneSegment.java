package app.ai.scene;

import java.time.Duration;

public record SceneSegment(Duration start, Duration end) {
    public SceneSegment {
        if (start.isNegative() || end.isNegative() || end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("Invalid scene boundaries");
        }
    }

    public Duration duration() {
        return end.minus(start);
    }
}
