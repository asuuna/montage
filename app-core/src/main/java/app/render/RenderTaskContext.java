package app.render;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;

public class RenderTaskContext {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final DoubleConsumer progressConsumer;

    public RenderTaskContext(DoubleConsumer progressConsumer) {
        this.progressConsumer = progressConsumer != null ? progressConsumer : d -> { };
    }

    public void reportProgress(double progress) {
        progressConsumer.accept(progress);
    }

    public void cancel() {
        cancelled.set(true);
    }

    public void checkCancelled() {
        if (cancelled.get()) {
            throw new CancellationException("Render task cancelled");
        }
    }
}
