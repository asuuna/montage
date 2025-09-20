package app.ai;

import app.domain.Project;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiTaskService.class);

    public CompletableFuture<Void> warmUpAsync(Project project) {
        Objects.requireNonNull(project, "project");
        return CompletableFuture.runAsync(() -> LOGGER.info("AI warm-up queued for {}", project.getName()));
    }
}
