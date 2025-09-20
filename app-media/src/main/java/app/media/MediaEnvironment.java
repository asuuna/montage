package app.media;

import java.util.Map;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.BytePointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MediaEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaEnvironment.class);
    private static final String INSTALL_HELP_URL = "https://github.com/bytedeco/javacv#platforms";

    private final boolean ffmpegAvailable;
    private final String ffmpegVersion;

    private MediaEnvironment(boolean ffmpegAvailable, String ffmpegVersion) {
        this.ffmpegAvailable = ffmpegAvailable;
        this.ffmpegVersion = ffmpegVersion;
    }

    public static MediaEnvironment detect() {
        boolean available = false;
        String version = "unknown";
        try {
            Loader.load(avutil.class);
            available = true;
            BytePointer info = avutil.av_version_info();
            if (info != null) {
                String value = info.getString();
                if (value != null && !value.isBlank()) {
                    version = value;
                }
            }
        } catch (UnsatisfiedLinkError error) {
            LOGGER.warn("FFmpeg libraries are not available: {}", error.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Unexpected error while detecting FFmpeg availability", exception);
        }
        return new MediaEnvironment(available, version);
    }

    public boolean isFfmpegAvailable() {
        return ffmpegAvailable;
    }

    public String getFfmpegVersion() {
        return ffmpegVersion;
    }

    public Map<String, String> diagnosticsEntries() {
        return Map.of(
                "ffmpegAvailable", Boolean.toString(ffmpegAvailable),
                "ffmpegVersion", ffmpegVersion,
                "installHelp", INSTALL_HELP_URL
        );
    }

    public String installationHelpUrl() {
        return INSTALL_HELP_URL;
    }
}
