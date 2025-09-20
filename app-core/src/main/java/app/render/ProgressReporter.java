package app.render;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ProgressReporter {
    private final Consumer<ProgressSnapshot> listener;
    private final AtomicLong completed = new AtomicLong(0);
    private volatile long total = 1;

    public ProgressReporter(Consumer<ProgressSnapshot> listener) {
        this.listener = listener != null ? listener : snapshot -> { };
    }

    public void setTotal(long total) {
        this.total = Math.max(1, total);
    }

    public void increment(long value) {
        long done = completed.addAndGet(value);
        double ratio = Math.min(1.0, done / (double) total);
        listener.accept(new ProgressSnapshot(ratio, done, total, Instant.now()));
    }

    public record ProgressSnapshot(double progress, long completedUnits, long totalUnits, Instant timestamp) { }
}
