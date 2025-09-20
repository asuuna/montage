package app.ai.highlight;

import java.time.Duration;

public record HighlightSegment(Duration start, Duration end, double score) {
    public HighlightSegment {
        if (start.isNegative() || end.isNegative() || end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("Invalid highlight segment");
        }
    }
}
