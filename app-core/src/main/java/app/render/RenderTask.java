package app.render;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface RenderTask {
    UUID getId();

    List<RenderTask> dependencies();

    void execute(RenderTaskContext context) throws Exception;

    Duration estimatedDuration();
}
