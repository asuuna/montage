package app.timeline;

import java.time.Duration;
import java.util.Objects;

public class TrimClipCommand implements TimelineCommand {
    private final TimelineClip clip;
    private final Duration newStart;
    private final Duration newDuration;
    private Duration previousStart;
    private Duration previousDuration;

    public TrimClipCommand(TimelineClip clip, Duration newStart, Duration newDuration) {
        this.clip = Objects.requireNonNull(clip, "clip");
        this.newStart = Objects.requireNonNull(newStart, "newStart");
        this.newDuration = Objects.requireNonNull(newDuration, "newDuration");
    }

    @Override
    public void execute() {
        previousStart = clip.getStart();
        previousDuration = clip.getDuration();
        clip.setStart(newStart);
        clip.setDuration(newDuration);
    }

    @Override
    public void undo() {
        clip.setStart(previousStart);
        clip.setDuration(previousDuration);
    }

    @Override
    public String description() {
        return "Trim clip";
    }
}
