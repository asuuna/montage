package app.util;

import java.nio.file.Path;

public final class ConfigPaths {
    private static final String DEFAULT_PROJECT_DIR = "MontageProjects";
    private static final String DEFAULT_MODEL_DIR = "MontageModels";

    private ConfigPaths() {
    }

    public static Path defaultProjectRoot() {
        return Path.of(System.getProperty("user.home"), DEFAULT_PROJECT_DIR);
    }

    public static Path defaultModelRoot() {
        return Path.of(System.getProperty("user.home"), DEFAULT_MODEL_DIR);
    }
}
