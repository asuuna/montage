package app.preferences;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public record UserPreferences(Locale locale,
                              Theme theme,
                              boolean telemetryEnabled,
                              Path mediaLibrary,
                              Path proxyDirectory,
                              Map<String, String> customShortcuts) {
    public static UserPreferences defaults() {
        return new UserPreferences(Locale.ENGLISH, Theme.DARK, false,
                Path.of(System.getProperty("user.home"), "MontageProjects"),
                Path.of(System.getProperty("user.home"), "MontageCache"),
                Map.of());
    }

    public enum Theme {
        DARK,
        LIGHT
    }
}
