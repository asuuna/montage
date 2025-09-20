package app.timeline;

import app.domain.TrackType;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TimelineService {
    private final Timeline timeline;
    private final CommandStack commandStack;
    private final SnapEngine snapEngine;

    public TimelineService(Timeline timeline, Duration snapThreshold) {
        this.timeline = Objects.requireNonNull(timeline, "timeline");
        this.commandStack = new CommandStack();
        this.snapEngine = new SnapEngine(snapThreshold);
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public CommandStack getCommandStack() {
        return commandStack;
    }

    public TimelineTrack addTrack(TrackType type) {
        TimelineTrack track = new TimelineTrack(type);
        timeline.addTrack(track);
        return track;
    }

    public void addClip(UUID trackId, Path source, Duration start, Duration duration) {
        Duration snapped = snap(start);
        TimelineClip clip = new TimelineClip(source, snapped, duration);
        commandStack.push(new AddClipCommand(timeline, trackId, clip));
    }

    public void splitClip(UUID trackId, UUID clipId, Duration splitPoint) {
        timeline.findTrack(trackId).ifPresent(track -> track.getClips().stream()
                .filter(clip -> clip.getId().equals(clipId))
                .findFirst()
                .ifPresent(clip -> commandStack.push(new SplitClipCommand(timeline, trackId, clip, splitPoint))));
    }

    public void trimClip(UUID trackId, UUID clipId, Duration newStart, Duration newDuration) {
        timeline.findTrack(trackId).ifPresent(track -> track.getClips().stream()
                .filter(clip -> clip.getId().equals(clipId))
                .findFirst()
                .ifPresent(clip -> commandStack.push(new TrimClipCommand(clip, newStart, newDuration))));
    }

    public void rippleDelete(UUID trackId, Duration start, Duration end) {
        commandStack.push(new RippleDeleteCommand(timeline, trackId, start, end));
    }

    public Duration snap(Duration target) {
        List<Duration> anchors = new ArrayList<>();
        for (TimelineTrack track : timeline.getTracks()) {
            for (TimelineClip clip : track.getClips()) {
                anchors.add(clip.getStart());
                anchors.add(clip.getEnd());
            }
        }
        return snapEngine.snap(target, anchors);
    }
}
