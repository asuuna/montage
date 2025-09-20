package app.timeline;

import app.domain.TrackType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TimelineTrack {
    private final UUID id;
    private final TrackType type;
    private final List<TimelineClip> clips = new ArrayList<>();

    public TimelineTrack(TrackType type) {
        this(UUID.randomUUID(), type);
    }

    public TimelineTrack(UUID id, TrackType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
    }

    public UUID getId() {
        return id;
    }

    public TrackType getType() {
        return type;
    }

    public List<TimelineClip> getClips() {
        clips.sort(Comparator.comparing(TimelineClip::getStart));
        return Collections.unmodifiableList(clips);
    }

    public void addClip(TimelineClip clip) {
        clips.add(Objects.requireNonNull(clip, "clip"));
        clips.sort(Comparator.comparing(TimelineClip::getStart));
    }

    public void removeClip(TimelineClip clip) {
        clips.remove(clip);
    }

    public Duration getDuration() {
        return clips.stream().map(TimelineClip::getEnd).max(Duration::compareTo).orElse(Duration.ZERO);
    }

    public TimelineClip clipAt(Duration position) {
        Objects.requireNonNull(position, "position");
        return clips.stream()
                .filter(clip -> clip.getStart().compareTo(position) <= 0 && clip.getEnd().compareTo(position) > 0)
                .findFirst()
                .orElse(null);
    }
}
