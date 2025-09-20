package app.pipeline;

import app.domain.Project;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PipelineOrchestrator {
    public CompletableFuture<Void> processAsync(Project project) {
        Objects.requireNonNull(project, "project");
        return CompletableFuture.completedFuture(null);
    }
}
