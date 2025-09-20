package app.render;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RenderJob {
    private final UUID id;
    private final Path projectFile;
    private final Path output;
    private final RenderPreset preset;
    private final Duration start;
    private final Duration end;
    private final Map<String, Object> metadata;

    public RenderJob(Path projectFile, Path output, RenderPreset preset, Duration start, Duration end, Map<String, Object> metadata) {
        this(UUID.randomUUID(), projectFile, output, preset, start, end, metadata);
    }

    public RenderJob(UUID id, Path projectFile, Path output, RenderPreset preset, Duration start, Duration end, Map<String, Object> metadata) {
        this.id = Objects.requireNonNull(id, "id");
        this.projectFile = Objects.requireNonNull(projectFile, "projectFile");
        this.output = Objects.requireNonNull(output, "output");
        this.preset = Objects.requireNonNull(preset, "preset");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        if (end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("end must be after start");
        }
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public UUID getId() {
        return id;
    }

    public Path getProjectFile() {
        return projectFile;
    }

    public Path getOutput() {
        return output;
    }

    public RenderPreset getPreset() {
        return preset;
    }

    public Duration getStart() {
        return start;
    }

    public Duration getEnd() {
        return end;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
