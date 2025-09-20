package app.service;

import app.io.InMemoryProjectRepository;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectServiceTest {
    @Test
    void createProjectPersistsProject() throws IOException {
        ProjectService service = new ProjectService(new InMemoryProjectRepository());
        var project = service.createProject("Demo", Path.of("/tmp/demo"));
        assertNotNull(project.getId());
        assertEquals("Demo", project.getName());
    }
}
