package app.ai.subtitles;

import java.time.Duration;

public record SubtitleLine(int index, Duration start, Duration end, String text) {
    public SubtitleLine {
        if (index < 1) {
            throw new IllegalArgumentException("index must be >= 1");
        }
        if (start.isNegative() || end.isNegative() || end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("Invalid subtitle timing");
        }
    }
}
