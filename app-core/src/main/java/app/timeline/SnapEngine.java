package app.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SnapEngine {
    private final Duration threshold;

    public SnapEngine(Duration threshold) {
        if (threshold.isNegative()) {
            throw new IllegalArgumentException("Threshold must be positive or zero");
        }
        this.threshold = threshold;
    }

    public Duration snap(Duration target, Collection<Duration> anchors) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(anchors, "anchors");
        List<Duration> candidates = new ArrayList<>(anchors);
        Duration closest = target;
        Duration minDelta = threshold.plus(Duration.ofMillis(1));
        for (Duration anchor : candidates) {
            Duration delta = target.minus(anchor).abs();
            if (delta.compareTo(minDelta) < 0) {
                minDelta = delta;
                closest = anchor;
            }
        }
        return closest;
    }
}
