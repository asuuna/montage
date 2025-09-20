package app.ai.subtitles;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class SubtitleFormatter {
    private SubtitleFormatter() {
    }

    public static String toSrt(List<SubtitleLine> lines) {
        Objects.requireNonNull(lines, "lines");
        StringBuilder sb = new StringBuilder();
        for (SubtitleLine line : lines) {
            sb.append(line.index()).append("\r\n");
            sb.append(formatTimestamp(line.start())).append(" --> ").append(formatTimestamp(line.end())).append("\r\n");
            sb.append(line.text()).append("\r\n\r\n");
        }
        return sb.toString();
    }

    public static String toVtt(List<SubtitleLine> lines) {
        Objects.requireNonNull(lines, "lines");
        StringBuilder sb = new StringBuilder("WEBVTT\n\n");
        for (SubtitleLine line : lines) {
            sb.append(formatTimestamp(line.start()).replace(',', '.'))
              .append(" --> ")
              .append(formatTimestamp(line.end()).replace(',', '.'))
              .append("\n")
              .append(line.text())
              .append("\n\n");
        }
        return sb.toString();
    }

    private static String formatTimestamp(Duration duration) {
        long millis = duration.toMillis();
        long hours = millis / 3_600_000;
        long minutes = (millis % 3_600_000) / 60_000;
        long seconds = (millis % 60_000) / 1000;
        long remainder = millis % 1000;
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, remainder);
    }
}
