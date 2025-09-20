package app.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Timeline {
    private final UUID id;
    private final List<TimelineTrack> tracks = new ArrayList<>();
    private Duration playhead = Duration.ZERO;
    private double zoom = 1.0;

    public Timeline() {
        this(UUID.randomUUID());
    }

    public Timeline(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public UUID getId() {
        return id;
    }

    public List<TimelineTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public void addTrack(TimelineTrack track) {
        tracks.add(Objects.requireNonNull(track, "track"));
    }

    public Optional<TimelineTrack> findTrack(UUID trackId) {
        return tracks.stream().filter(track -> track.getId().equals(trackId)).findFirst();
    }

    public Duration getPlayhead() {
        return playhead;
    }

    public void setPlayhead(Duration playhead) {
        if (playhead.isNegative()) {
            throw new IllegalArgumentException("Playhead cannot be negative");
        }
        this.playhead = playhead;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        if (zoom <= 0.05 || zoom > 10.0) {
            throw new IllegalArgumentException("Zoom must be between 0.05 and 10.0");
        }
        this.zoom = zoom;
    }
}
