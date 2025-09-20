package app.ai;

import app.ai.audio.SilenceRange;
import app.ai.config.HighlightConfig;
import app.ai.highlight.HighlightScorer;
import app.ai.highlight.HighlightSegment;
import app.ai.scene.SceneSegment;
import app.ai.subtitles.SubtitleLine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighlightScorerTest {
    @Test
    void scoresHighlightsWithKeywords() throws Exception {
        Path tempVideo = Files.createTempFile("highlight-test", ".mp4");
        TestClipFactory.createSceneChangeClip(tempVideo);
        HighlightScorer scorer = new HighlightScorer(HighlightConfig.defaultConfig());
        List<SceneSegment> scenes = List.of(
                new SceneSegment(Duration.ZERO, Duration.ofSeconds(2)),
                new SceneSegment(Duration.ofSeconds(2), Duration.ofSeconds(5))
        );
        List<SilenceRange> silences = List.of(new SilenceRange(Duration.ZERO, Duration.ofSeconds(1)));
        List<SubtitleLine> subtitles = List.of(new SubtitleLine(1, Duration.ofSeconds(2), Duration.ofSeconds(4), "Great goal!"));
        List<HighlightSegment> highlights = scorer.score(tempVideo, scenes, silences, subtitles, List.of("goal"));
        assertFalse(highlights.isEmpty());
        assertTrue(highlights.stream().anyMatch(seg -> seg.score() > 0));
    }
}
