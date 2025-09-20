package app.timeline;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public final class TimelineClip {
    private final UUID id;
    private final Path source;
    private Duration start;
    private Duration duration;
    private double playbackRate;
    private double audioGainDb;
    private Transition transitionIn;
    private Transition transitionOut;

    public TimelineClip(Path source, Duration start, Duration duration) {
        this(UUID.randomUUID(), source, start, duration);
    }

    public TimelineClip(UUID id, Path source, Duration start, Duration duration) {
        this.id = Objects.requireNonNull(id, "id");
        this.source = Objects.requireNonNull(source, "source");
        setStart(start);
        setDuration(duration);
        this.playbackRate = 1.0;
        this.audioGainDb = 0.0;
        this.transitionIn = Transition.NONE;
        this.transitionOut = Transition.NONE;
    }

    public UUID getId() {
        return id;
    }

    public Path getSource() {
        return source;
    }

    public Duration getStart() {
        return start;
    }

    public void setStart(Duration start) {
        this.start = Objects.requireNonNull(start, "start");
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Clip duration must be positive");
        }
        this.duration = duration;
    }

    public Duration getEnd() {
        return start.plus(duration);
    }

    public double getPlaybackRate() {
        return playbackRate;
    }

    public void setPlaybackRate(double playbackRate) {
        if (playbackRate <= 0.0) {
            throw new IllegalArgumentException("Playback rate must be positive");
        }
        this.playbackRate = playbackRate;
    }

    public double getAudioGainDb() {
        return audioGainDb;
    }

    public void setAudioGainDb(double audioGainDb) {
        this.audioGainDb = audioGainDb;
    }

    public Transition getTransitionIn() {
        return transitionIn;
    }

    public void setTransitionIn(Transition transitionIn) {
        this.transitionIn = Objects.requireNonNull(transitionIn, "transitionIn");
    }

    public Transition getTransitionOut() {
        return transitionOut;
    }

    public void setTransitionOut(Transition transitionOut) {
        this.transitionOut = Objects.requireNonNull(transitionOut, "transitionOut");
    }
}
