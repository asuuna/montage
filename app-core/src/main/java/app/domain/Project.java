package app.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Project {
    private final UUID id;
    private final String name;
    private final Path location;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Track> tracks = new ArrayList<>();

    public Project(String name, Path location) {
        this(UUID.randomUUID(), name, location, Instant.now(), Instant.now());
    }

    public Project(UUID id, String name, Path location, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.location = Objects.requireNonNull(location, "location");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Path getLocation() {
        return location;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Track> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(Track track) {
        tracks.add(Objects.requireNonNull(track, "track"));
        updatedAt = Instant.now();
    }
}
