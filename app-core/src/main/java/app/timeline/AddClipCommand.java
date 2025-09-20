package app.timeline;

import java.util.Objects;
import java.util.UUID;

public class AddClipCommand implements TimelineCommand {
    private final Timeline timeline;
    private final UUID trackId;
    private final TimelineClip clip;

    public AddClipCommand(Timeline timeline, UUID trackId, TimelineClip clip) {
        this.timeline = Objects.requireNonNull(timeline, "timeline");
        this.trackId = Objects.requireNonNull(trackId, "trackId");
        this.clip = Objects.requireNonNull(clip, "clip");
    }

    @Override
    public void execute() {
        timeline.findTrack(trackId).ifPresent(track -> track.addClip(clip));
    }

    @Override
    public void undo() {
        timeline.findTrack(trackId).ifPresent(track -> track.removeClip(clip));
    }

    @Override
    public String description() {
        return "Add clip";
    }
}
