package app.ai;

import app.ai.system.GpuCapabilityDetector;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GpuCapabilityDetectorTest {
    @Test
    void reportsCapabilities() {
        Map<String, Boolean> capabilities = GpuCapabilityDetector.detect();
        assertNotNull(capabilities.get("cuda"));
        assertNotNull(capabilities.get("directml"));
        assertNotNull(capabilities.get("metal"));
    }
}
