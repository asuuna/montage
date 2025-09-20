package app.io;

import app.domain.Project;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProjectRepository implements ProjectRepository {
    private final Map<String, Project> projects = new ConcurrentHashMap<>();

    @Override
    public void save(Project project) throws IOException {
        projects.put(project.getId().toString(), project);
    }

    @Override
    public Optional<Project> load(String projectId) throws IOException {
        return Optional.ofNullable(projects.get(projectId));
    }
}
