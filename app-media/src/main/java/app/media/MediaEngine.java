package app.media;

import app.domain.Project;
import app.media.cache.FrameCache;
import app.media.export.ExportProgressListener;
import app.media.export.MediaExportRequest;
import app.media.export.MediaExporter;
import app.media.frame.FrameExtractor;
import app.media.probe.MediaMetadata;
import app.media.probe.MediaProbe;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaEngine.class);

    private final MediaEnvironment environment;
    private final MediaProbe mediaProbe;
    private final FrameExtractor frameExtractor;
    private final MediaExporter mediaExporter;

    public MediaEngine(FrameCache frameCache) {
        this(MediaEnvironment.detect(), frameCache);
    }

    public MediaEngine(MediaEnvironment environment, FrameCache frameCache) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.mediaProbe = new MediaProbe();
        this.frameExtractor = new FrameExtractor(frameCache);
        this.mediaExporter = new MediaExporter();
    }

    public MediaEnvironment getEnvironment() {
        return environment;
    }

    public void initialize(Project project) {
        Objects.requireNonNull(project, "project");
        LOGGER.info("Media engine initialized for project {}", project.getName());
        if (!environment.isFfmpegAvailable()) {
            LOGGER.warn("FFmpeg libraries are unavailable. Guidance: {}", environment.installationHelpUrl());
        }
    }

    public MediaMetadata probe(Path mediaPath) throws IOException {
        LOGGER.debug("Probing media {}", mediaPath);
        ensureFfmpeg();
        return mediaProbe.probe(mediaPath);
    }

    public BufferedImage extractFrame(Path mediaPath, double seconds) throws IOException {
        LOGGER.debug("Extracting frame at {}s from {}", seconds, mediaPath);
        ensureFfmpeg();
        return frameExtractor.extract(mediaPath, seconds);
    }

    public void export(MediaExportRequest request, ExportProgressListener listener) throws IOException {
        LOGGER.info("Exporting {} to {} with preset {}", request.input(), request.output(), request.preset());
        ensureFfmpeg();
        mediaExporter.export(request, listener);
    }

    private void ensureFfmpeg() throws IOException {
        if (!environment.isFfmpegAvailable()) {
            throw new IOException("FFmpeg libraries are not available. Install instructions: " + environment.installationHelpUrl());
        }
    }
}
