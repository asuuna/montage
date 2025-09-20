package app.ai.subtitles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface SpeechToTextEngine {
    List<SubtitleLine> transcribe(Path mediaPath) throws IOException;
}
