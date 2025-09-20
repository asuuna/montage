package app.ai.reframe;

import java.nio.file.Path;
import java.time.Duration;

public record ReframeResult(Path output, Duration processedDuration) {
}
