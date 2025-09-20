package app.ai.highlight;

import app.ai.config.HighlightConfig;
import app.ai.scene.SceneSegment;
import app.ai.audio.SilenceRange;
import app.ai.subtitles.SubtitleLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

public class HighlightScorer {
    private final HighlightConfig config;

    public HighlightScorer(HighlightConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public List<HighlightSegment> score(Path mediaPath,
                                         List<SceneSegment> scenes,
                                         List<SilenceRange> silences,
                                         List<SubtitleLine> subtitles,
                                         List<String> keywords) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }
        if (scenes == null || scenes.isEmpty()) {
            return List.of();
        }
        Set<String> keywordSet = keywords == null ? Set.of() : keywords.stream()
                .map(k -> k.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<HighlightSegment> segments = new ArrayList<>();

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile())) {
            grabber.start();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            for (SceneSegment scene : scenes) {
                double motionScore = sampleMotion(grabber, converter, scene);
                double audioScore = 1.0 - silenceCoverage(scene, silences);
                double keywordScore = keywordDensity(scene, subtitles, keywordSet);
                double faceScore = motionScore * 0.5 + 0.5; // placeholder until face detection added

                double score = config.motionWeight() * motionScore
                        + config.audioWeight() * audioScore
                        + config.keywordWeight() * keywordScore
                        + config.faceWeight() * faceScore;

                segments.add(new HighlightSegment(scene.start(), scene.end(), Math.min(score, 1.0)));
            }
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new IOException("Failed to score highlights", e);
        }
        return segments.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .collect(Collectors.toList());
    }

    private static double sampleMotion(FFmpegFrameGrabber grabber, OpenCVFrameConverter.ToMat converter, SceneSegment scene) throws FFmpegFrameGrabber.Exception {
        long startMicros = scene.start().toMillis() * 1000;
        long endMicros = scene.end().toMillis() * 1000;
        long stepMicros = Math.max(200_000, (endMicros - startMicros) / 5);
        long current = startMicros;
        Mat previous = null;
        double totalDiff = 0.0;
        int comparisons = 0;
        while (current < endMicros) {
            grabber.setTimestamp(current);
            Frame frame = grabber.grabImage();
            if (frame == null) {
                break;
            }
            Mat mat = converter.convert(frame);
            Mat gray = new Mat();
            opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
            opencv_imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
            if (previous != null) {
                Mat diff = new Mat();
                opencv_core.absdiff(previous, gray, diff);
                double sum = opencv_core.sumElems(diff).get(0);
                totalDiff += sum / (gray.rows() * gray.cols() * 255.0);
                diff.release();
                comparisons++;
            }
            if (previous != null) {
                previous.release();
            }
            previous = gray;
            current += stepMicros;
        }
        if (previous != null) {
            previous.release();
        }
        if (comparisons == 0) {
            return 0.0;
        }
        return Math.min(1.0, totalDiff / comparisons);
    }

    private static double silenceCoverage(SceneSegment scene, List<SilenceRange> silences) {
        if (silences == null || silences.isEmpty()) {
            return 0.0;
        }
        Duration overlap = Duration.ZERO;
        for (SilenceRange silence : silences) {
            Duration start = silence.start().compareTo(scene.start()) > 0 ? silence.start() : scene.start();
            Duration end = silence.end().compareTo(scene.end()) < 0 ? silence.end() : scene.end();
            if (end.compareTo(start) > 0) {
                overlap = overlap.plus(end.minus(start));
            }
        }
        return overlap.toMillis() / (double) scene.duration().toMillis();
    }

    private static double keywordDensity(SceneSegment scene, List<SubtitleLine> subtitles, Set<String> keywords) {
        if (subtitles == null || subtitles.isEmpty() || keywords.isEmpty()) {
            return 0.0;
        }
        long matches = subtitles.stream()
                .filter(line -> overlaps(line, scene))
                .map(line -> line.text().toLowerCase(Locale.ROOT))
                .filter(text -> keywords.stream().anyMatch(text::contains))
                .count();
        return Math.min(1.0, matches / 5.0);
    }

    private static boolean overlaps(SubtitleLine line, SceneSegment scene) {
        return line.end().compareTo(scene.start()) > 0 && line.start().compareTo(scene.end()) < 0;
    }
}
