package app.service;

import app.domain.Project;
import app.io.ProjectRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public Project createProject(String name, Path location) throws IOException {
        Project project = new Project(name, location);
        repository.save(project);
        return project;
    }

    public Project loadProject(String projectId) throws IOException {
        return repository.load(projectId).orElseThrow(() -> new IOException("Project not found: " + projectId));
    }
}
