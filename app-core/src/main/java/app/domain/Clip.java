package app.domain;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public final class Clip {
    private final UUID id;
    private final Path source;
    private final Duration startOffset;
    private final Duration duration;

    public Clip(Path source, Duration startOffset, Duration duration) {
        this(UUID.randomUUID(), source, startOffset, duration);
    }

    public Clip(UUID id, Path source, Duration startOffset, Duration duration) {
        this.id = Objects.requireNonNull(id, "id");
        this.source = Objects.requireNonNull(source, "source");
        this.startOffset = Objects.requireNonNull(startOffset, "startOffset");
        this.duration = Objects.requireNonNull(duration, "duration");
    }

    public UUID getId() {
        return id;
    }

    public Path getSource() {
        return source;
    }

    public Duration getStartOffset() {
        return startOffset;
    }

    public Duration getDuration() {
        return duration;
    }
}
