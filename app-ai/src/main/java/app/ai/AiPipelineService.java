package app.ai;

import app.ai.audio.SilenceDetectionService;
import app.ai.audio.SilenceRange;
import app.ai.config.HighlightConfig;
import app.ai.config.ReframeConfig;
import app.ai.config.SceneDetectionConfig;
import app.ai.config.SilenceDetectionConfig;
import app.ai.highlight.HighlightScorer;
import app.ai.highlight.HighlightSegment;
import app.ai.reframe.AutoReframeService;
import app.ai.reframe.ReframeResult;
import app.ai.scene.SceneDetectionService;
import app.ai.scene.SceneSegment;
import app.ai.subtitles.SpeechToTextEngine;
import app.ai.subtitles.SubtitleFormatter;
import app.ai.subtitles.SubtitleLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class AiPipelineService {
    private final SceneDetectionService sceneDetectionService;
    private final SilenceDetectionService silenceDetectionService;
    private final SpeechToTextEngine speechToTextEngine;
    private final HighlightScorer highlightScorer;

    public AiPipelineService(SceneDetectionConfig sceneConfig,
                             SilenceDetectionConfig silenceConfig,
                             SpeechToTextEngine sttEngine,
                             HighlightConfig highlightConfig) {
        this.sceneDetectionService = new SceneDetectionService(sceneConfig);
        this.silenceDetectionService = new SilenceDetectionService(silenceConfig);
        this.speechToTextEngine = Objects.requireNonNull(sttEngine, "speechToTextEngine");
        this.highlightScorer = new HighlightScorer(highlightConfig);
    }

    public List<SceneSegment> detectScenes(Path mediaPath) throws IOException {
        return sceneDetectionService.detect(mediaPath);
    }

    public List<SilenceRange> detectSilences(Path mediaPath) throws IOException {
        return silenceDetectionService.detectSilence(mediaPath);
    }

    public List<SubtitleLine> generateSubtitles(Path mediaPath) throws IOException {
        return speechToTextEngine.transcribe(mediaPath);
    }

    public String writeSrt(Path mediaPath, Path outputSrt) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        Objects.requireNonNull(outputSrt, "outputSrt");
        List<SubtitleLine> lines = generateSubtitles(mediaPath);
        String content = SubtitleFormatter.toSrt(lines);
        Path parent = outputSrt.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputSrt, content);
        return content;
    }

    public List<HighlightSegment> generateHighlights(Path mediaPath,
                                                      List<SceneSegment> scenes,
                                                      List<SilenceRange> silences,
                                                      List<SubtitleLine> subtitles,
                                                      List<String> keywords) throws IOException {
        return highlightScorer.score(mediaPath, scenes, silences, subtitles, keywords);
    }

    public ReframeResult reframe(Path mediaPath, Path output, ReframeConfig config) throws IOException {
        AutoReframeService service = new AutoReframeService(config);
        return service.reframe(mediaPath, output);
    }
}

