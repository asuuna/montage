package app.timeline;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public class SplitClipCommand implements TimelineCommand {
    private final Timeline timeline;
    private final UUID trackId;
    private final TimelineClip originalClip;
    private final Duration splitPoint;
    private TimelineClip rightClip;

    public SplitClipCommand(Timeline timeline, UUID trackId, TimelineClip originalClip, Duration splitPoint) {
        this.timeline = Objects.requireNonNull(timeline, "timeline");
        this.trackId = Objects.requireNonNull(trackId, "trackId");
        this.originalClip = Objects.requireNonNull(originalClip, "originalClip");
        this.splitPoint = Objects.requireNonNull(splitPoint, "splitPoint");
    }

    @Override
    public void execute() {
        Duration relativeSplit = splitPoint.minus(originalClip.getStart());
        if (relativeSplit.isNegative() || relativeSplit.compareTo(originalClip.getDuration()) >= 0) {
            return;
        }

        Duration leftDuration = relativeSplit;
        Duration rightDuration = originalClip.getDuration().minus(relativeSplit);
        originalClip.setDuration(leftDuration);

        rightClip = new TimelineClip(originalClip.getSource(), splitPoint, rightDuration);
        rightClip.setPlaybackRate(originalClip.getPlaybackRate());
        rightClip.setAudioGainDb(originalClip.getAudioGainDb());
        rightClip.setTransitionOut(originalClip.getTransitionOut());
        rightClip.setTransitionIn(originalClip.getTransitionOut());

        timeline.findTrack(trackId).ifPresent(track -> track.addClip(rightClip));
    }

    @Override
    public void undo() {
        if (rightClip == null) {
            return;
        }
        Duration mergedDuration = originalClip.getDuration().plus(rightClip.getDuration());
        originalClip.setDuration(mergedDuration);
        timeline.findTrack(trackId).ifPresent(track -> track.removeClip(rightClip));
        rightClip = null;
    }

    @Override
    public String description() {
        return "Split clip";
    }
}
