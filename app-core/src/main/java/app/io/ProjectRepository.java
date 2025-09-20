package app.io;

import app.domain.Project;
import java.io.IOException;
import java.util.Optional;

public interface ProjectRepository {
    void save(Project project) throws IOException;

    Optional<Project> load(String projectId) throws IOException;
}
