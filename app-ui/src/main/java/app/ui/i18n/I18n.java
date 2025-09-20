package app.ui.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class I18n {
    private static final String BUNDLE_NAME = "app.ui.i18n.Messages";
    private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(detectLocale());

    private I18n() {
    }

    public static String t(String key, Object... args) {
        ResourceBundle bundle = getBundle(locale.get());
        if (!bundle.containsKey(key)) {
            return '!' + key + '!';
        }
        String pattern = bundle.getString(key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        MessageFormat format = new MessageFormat(pattern, locale.get());
        return format.format(args);
    }

    public static StringBinding bind(String key, Object... args) {
        return Bindings.createStringBinding(() -> t(key, args), locale);
    }

    public static void setLocale(Locale newLocale) {
        Locale effective = Objects.requireNonNullElseGet(newLocale, I18n::detectLocale);
        locale.set(effective);
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static ReadOnlyObjectProperty<Locale> localeProperty() {
        return locale;
    }

    private static Locale detectLocale() {
        String override = System.getProperty("montage.locale");
        if (override != null && !override.isBlank()) {
            return Locale.forLanguageTag(override);
        }
        Locale systemLocale = Locale.getDefault();
        if ("fr".equalsIgnoreCase(systemLocale.getLanguage())) {
            return systemLocale;
        }
        return Locale.FRANCE;
    }

    private static ResourceBundle getBundle(Locale targetLocale) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = I18n.class.getClassLoader();
        }
        ResourceBundle.Control control = new Utf8Control();
        return ResourceBundle.getBundle(BUNDLE_NAME, targetLocale, loader, control);
    }

    private static final class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                                         boolean reload) throws IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            InputStream stream;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url == null) {
                    return null;
                }
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                } else {
                    stream = null;
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            }
        }
    }
}
