package app.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Path preferenceFile;

    public PreferencesService(Path preferenceFile) {
        this.preferenceFile = Objects.requireNonNull(preferenceFile, "preferenceFile");
    }

    public UserPreferences load() {
        try {
            if (Files.exists(preferenceFile)) {
                return OBJECT_MAPPER.readValue(preferenceFile.toFile(), UserPreferences.class);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load preferences, using defaults", e);
        }
        return UserPreferences.defaults();
    }

    public void save(UserPreferences preferences) {
        Objects.requireNonNull(preferences, "preferences");
        try {
            if (preferenceFile.getParent() != null) {
                Files.createDirectories(preferenceFile.getParent());
            }
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(preferenceFile.toFile(), preferences);
        } catch (IOException e) {
            LOGGER.error("Failed to persist preferences", e);
        }
    }

    public UserPreferences update(java.util.function.UnaryOperator<UserPreferences> updater) {
        UserPreferences current = load();
        UserPreferences updated = Objects.requireNonNull(updater.apply(current));
        save(updated);
        return updated;
    }
}
