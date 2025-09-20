package app.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RippleDeleteCommand implements TimelineCommand {
    private final Timeline timeline;
    private final UUID trackId;
    private final Duration start;
    private final Duration end;
    private final List<ClipState> movedClips = new ArrayList<>();

    public RippleDeleteCommand(Timeline timeline, UUID trackId, Duration start, Duration end) {
        if (end.compareTo(start) <= 0) {
            throw new IllegalArgumentException("End must be after start");
        }
        this.timeline = Objects.requireNonNull(timeline, "timeline");
        this.trackId = Objects.requireNonNull(trackId, "trackId");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
    }

    @Override
    public void execute() {
        Duration delta = end.minus(start);
        timeline.findTrack(trackId).ifPresent(track -> {
            for (TimelineClip clip : track.getClips()) {
                if (clip.getStart().compareTo(end) >= 0) {
                    movedClips.add(new ClipState(clip, clip.getStart()));
                    clip.setStart(clip.getStart().minus(delta));
                }
            }
        });
    }

    @Override
    public void undo() {
        movedClips.forEach(state -> state.clip().setStart(state.originalStart()));
        movedClips.clear();
    }

    @Override
    public String description() {
        return "Ripple delete";
    }

    private record ClipState(TimelineClip clip, Duration originalStart) { }
}
