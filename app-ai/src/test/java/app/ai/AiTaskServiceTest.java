package app.ai;

import app.domain.Project;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AiTaskServiceTest {
    @Test
    void warmUpCompletes() {
        var service = new AiTaskService();
        var project = new Project("AI", Path.of("/tmp"));
        assertDoesNotThrow(() -> service.warmUpAsync(project).get());
    }
}
