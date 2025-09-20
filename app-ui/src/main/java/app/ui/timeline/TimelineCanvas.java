package app.ui.timeline;

import app.ui.i18n.I18n;
import app.timeline.Timeline;
import app.timeline.TimelineClip;
import app.timeline.TimelineTrack;
import java.text.NumberFormat;
import java.time.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TimelineCanvas extends Canvas {
    private static final double TRACK_HEIGHT = 80.0;
    private static final double TRACK_GAP = 16.0;
    private static final double RULER_HEIGHT = 26.0;

    private final TimelineController controller;

    public TimelineCanvas(TimelineController controller) {
        this.controller = controller;
        setWidth(1000);
        setHeight(280);
    }

    public void render() {
        Timeline timeline = controller.getTimeline();
        double pixelsPerSecond = controller.getPixelsPerSecond();
        double totalDurationSeconds = timeline.getTracks().stream()
                .map(TimelineTrack::getDuration)
                .mapToDouble(duration -> duration.toMillis() / 1000.0)
                .max()
                .orElse(10.0);
        double contentWidth = Math.max(800, totalDurationSeconds * pixelsPerSecond + 200);
        double contentHeight = RULER_HEIGHT + timeline.getTracks().size() * (TRACK_HEIGHT + TRACK_GAP) + TRACK_GAP;
        setWidth(contentWidth);
        setHeight(contentHeight);

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#1f222d"));
        gc.fillRect(0, 0, contentWidth, contentHeight);

        drawRuler(gc, contentWidth, pixelsPerSecond);

        double trackY = RULER_HEIGHT + TRACK_GAP;
        for (TimelineTrack track : timeline.getTracks()) {
            drawTrack(gc, track, trackY, pixelsPerSecond);
            trackY += TRACK_HEIGHT + TRACK_GAP;
        }

        drawPlayhead(gc, timeline.getPlayhead(), pixelsPerSecond, contentHeight);
    }

    private void drawRuler(GraphicsContext gc, double width, double pixelsPerSecond) {
        gc.setFill(Color.web("#2c3140"));
        gc.fillRect(0, 0, width, RULER_HEIGHT);
        gc.setStroke(Color.web("#3c4255"));
        gc.strokeLine(0, RULER_HEIGHT - 0.5, width, RULER_HEIGHT - 0.5);

        gc.setFill(Color.web("#d0d3dc"));
        gc.setFont(Font.font(11));
        double secondsStep = chooseRulerStep(pixelsPerSecond);
        double maxSeconds = width / pixelsPerSecond;
        for (double second = 0; second <= maxSeconds; second += secondsStep) {
            double x = second * pixelsPerSecond;
            gc.setStroke(Color.web("#363c4f"));
            gc.strokeLine(x, RULER_HEIGHT, x, RULER_HEIGHT - 8);
            gc.fillText(formatSeconds(second), x + 4, RULER_HEIGHT - 10);
        }
    }

    private void drawTrack(GraphicsContext gc, TimelineTrack track, double y, double pixelsPerSecond) {
        gc.setFill(Color.web("#252a38"));
        gc.fillRoundRect(0, y, getWidth(), TRACK_HEIGHT, 8, 8);
        gc.setFill(Color.web("#9aa0b4"));
        gc.setFont(Font.font(13));
        gc.fillText(I18n.t("timeline.track.type." + track.getType().name()), 12, y + 18);

        for (TimelineClip clip : track.getClips()) {
            drawClip(gc, clip, y + 24, pixelsPerSecond, track.getType());
        }
    }

    private void drawClip(GraphicsContext gc, TimelineClip clip, double y, double pixelsPerSecond, app.domain.TrackType trackType) {
        double startX = clip.getStart().toMillis() / 1000.0 * pixelsPerSecond;
        double width = clip.getDuration().toMillis() / 1000.0 * pixelsPerSecond;
        double height = TRACK_HEIGHT - 36;

        Color baseColor = trackType == app.domain.TrackType.VIDEO ? Color.web("#5791ff") : Color.web("#5bc487");
        gc.setFill(baseColor.deriveColor(0, 1, 1, 0.8));
        gc.fillRoundRect(startX, y, width, height, 8, 8);
        gc.setStroke(baseColor.brighter());
        gc.strokeRoundRect(startX, y, width, height, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        String text = clip.getSource().getFileName().toString();
        gc.fillText(text, startX + 8, y + 18);
        gc.setFont(Font.font(10));
        gc.fillText(I18n.t("timeline.clip.parameters", clip.getPlaybackRate(), clip.getAudioGainDb()), startX + 8, y + 34);
    }

    private void drawPlayhead(GraphicsContext gc, Duration playhead, double pixelsPerSecond, double height) {
        double x = playhead.toMillis() / 1000.0 * pixelsPerSecond;
        gc.setStroke(Color.web("#ff6868"));
        gc.setLineWidth(2);
        gc.strokeLine(x, 0, x, height);
    }

    private static double chooseRulerStep(double pixelsPerSecond) {
        if (pixelsPerSecond > 200) {
            return 0.5;
        }
        if (pixelsPerSecond > 120) {
            return 1.0;
        }
        if (pixelsPerSecond > 80) {
            return 2.0;
        }
        return 5.0;
    }

    private static String formatSeconds(double seconds) {
        int totalSeconds = (int) Math.floor(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        double fractional = seconds - totalSeconds;
        if (minutes > 0) {
            return String.format(I18n.getLocale(), "%d:%02d", minutes, secs);
        }
        if (fractional > 0.0) {
            NumberFormat format = NumberFormat.getNumberInstance(I18n.getLocale());
            format.setMinimumFractionDigits(1);
            format.setMaximumFractionDigits(1);
            return format.format(seconds);
        }
        return Integer.toString(secs);
    }
}
