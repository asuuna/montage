package app.ui.timeline;

import app.domain.TrackType;
import app.timeline.CommandStack;
import app.timeline.Timeline;
import app.timeline.TimelineClip;
import app.timeline.TimelineService;
import app.timeline.TimelineTrack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class TimelineController {
    private static final Duration DEFAULT_SNAP_THRESHOLD = Duration.ofMillis(100);

    private final Timeline timeline;
    private final TimelineService service;
    private final TimelineTrack videoTrack;
    private final TimelineTrack audioTrack;
    private final List<Runnable> listeners = new ArrayList<>();

    private boolean snappingEnabled = true;
    private double pixelsPerSecond = 120.0;

    public TimelineController() {
        timeline = new Timeline();
        service = new TimelineService(timeline, DEFAULT_SNAP_THRESHOLD);
        videoTrack = service.addTrack(TrackType.VIDEO);
        audioTrack = service.addTrack(TrackType.AUDIO);
        seedSampleClips();
    }

    private void seedSampleClips() {
        addSampleClip(videoTrack.getId(), "intro.mp4", Duration.ZERO, Duration.ofSeconds(5));
        addSampleClip(videoTrack.getId(), "broll.mp4", Duration.ofSeconds(6), Duration.ofSeconds(4));
        addSampleClip(audioTrack.getId(), "music.wav", Duration.ZERO, Duration.ofSeconds(12));
        notifyListeners();
    }

    private boolean addSampleClip(UUID trackId, String fileName, Duration start, Duration duration) {
        Path mediaPath = Path.of(fileName);
        if (!Files.exists(mediaPath)) {
            return false;
        }
        service.addClip(trackId, mediaPath, start, duration);
        return true;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public TimelineTrack getVideoTrack() {
        return videoTrack;
    }

    public TimelineTrack getAudioTrack() {
        return audioTrack;
    }

    public double getPixelsPerSecond() {
        return pixelsPerSecond;
    }

    public void setPixelsPerSecond(double pixelsPerSecond) {
        if (pixelsPerSecond < 20.0) {
            pixelsPerSecond = 20.0;
        }
        if (pixelsPerSecond > 400.0) {
            pixelsPerSecond = 400.0;
        }
        this.pixelsPerSecond = pixelsPerSecond;
        notifyListeners();
    }

    public void zoomByFactor(double factor) {
        setPixelsPerSecond(getPixelsPerSecond() * factor);
    }

    public void toggleSnapping() {
        snappingEnabled = !snappingEnabled;
    }

    public boolean isSnappingEnabled() {
        return snappingEnabled;
    }

    public Duration snap(Duration target) {
        if (!snappingEnabled) {
            return target;
        }
        return service.snap(target);
    }

    public void movePlayhead(Duration position) {
        timeline.setPlayhead(position.isNegative() ? Duration.ZERO : position);
        notifyListeners();
    }

    public void movePlayheadBySeconds(double seconds) {
        Duration newPos = timeline.getPlayhead().plusMillis((long) (seconds * 1000));
        if (newPos.isNegative()) {
            newPos = Duration.ZERO;
        }
        movePlayhead(newPos);
    }

    public void splitAtPlayhead(UUID trackId) {
        TimelineClip clip = findClipAt(trackId, timeline.getPlayhead());
        if (clip == null) {
            return;
        }
        service.splitClip(trackId, clip.getId(), timeline.getPlayhead());
        notifyListeners();
    }

    public void adjustPlaybackRate(UUID trackId, UUID clipId, double rate) {
        withClip(trackId, clipId, clip -> clip.setPlaybackRate(rate));
    }

    public void adjustAudioGain(UUID trackId, UUID clipId, double gainDb) {
        withClip(trackId, clipId, clip -> clip.setAudioGainDb(gainDb));
    }

    public void setTransitions(UUID trackId, UUID clipId, app.timeline.Transition in, app.timeline.Transition out) {
        withClip(trackId, clipId, clip -> {
            clip.setTransitionIn(in);
            clip.setTransitionOut(out);
        });
    }

    public void rippleDelete(Duration start, Duration end) {
        service.rippleDelete(videoTrack.getId(), start, end);
        notifyListeners();
    }

    public void importMedia(List<Path> mediaFiles) {
        Duration insertPoint = timeline.getPlayhead();
        for (Path path : mediaFiles) {
            service.addClip(videoTrack.getId(), path, insertPoint, Duration.ofSeconds(5));
            insertPoint = insertPoint.plusSeconds(5);
        }
        notifyListeners();
    }

    public CommandStack commandStack() {
        return service.getCommandStack();
    }

    public void undo() {
        if (commandStack().canUndo()) {
            commandStack().undo();
            notifyListeners();
        }
    }

    public void redo() {
        if (commandStack().canRedo()) {
            commandStack().redo();
            notifyListeners();
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }

    private TimelineClip findClipAt(UUID trackId, Duration position) {
        return timeline.findTrack(trackId)
                .map(track -> track.clipAt(position))
                .orElse(null);
    }

    private void withClip(UUID trackId, UUID clipId, Consumer<TimelineClip> consumer) {
        timeline.findTrack(trackId).ifPresent(track -> track.getClips().stream()
                .filter(clip -> clip.getId().equals(clipId))
                .findFirst()
                .ifPresent(clip -> {
                    consumer.accept(clip);
                    notifyListeners();
                }));
    }
}
