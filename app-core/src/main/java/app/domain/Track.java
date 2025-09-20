package app.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Track {
    private final UUID id;
    private final TrackType type;
    private final List<Clip> clips = new ArrayList<>();

    public Track(TrackType type) {
        this(UUID.randomUUID(), type);
    }

    public Track(UUID id, TrackType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
    }

    public UUID getId() {
        return id;
    }

    public TrackType getType() {
        return type;
    }

    public List<Clip> getClips() {
        return Collections.unmodifiableList(clips);
    }

    public Duration getDuration() {
        return clips.stream().map(Clip::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    public void addClip(Clip clip) {
        clips.add(Objects.requireNonNull(clip, "clip"));
    }
}
