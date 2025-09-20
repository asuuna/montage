package app.ai;

import app.ai.subtitles.SubtitleFormatter;
import app.ai.subtitles.SubtitleLine;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtitleFormatterTest {
    @Test
    void formatsSrtAndVtt() {
        List<SubtitleLine> lines = List.of(
                new SubtitleLine(1, Duration.ofMillis(0), Duration.ofSeconds(2), "Hello world"),
                new SubtitleLine(2, Duration.ofSeconds(3), Duration.ofSeconds(5), "Montage AI")
        );
        String srt = SubtitleFormatter.toSrt(lines);
        String vtt = SubtitleFormatter.toVtt(lines);
        assertTrue(srt.contains("00:00:00,000 --> 00:00:02,000"));
        assertTrue(vtt.contains("WEBVTT"));
    }
}
