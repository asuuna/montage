package app.ai.scene;

import app.ai.config.SceneDetectionConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.javacpp.indexer.DoubleIndexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class SceneDetectionService {
    private final SceneDetectionConfig config;

    public SceneDetectionService(SceneDetectionConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public List<SceneSegment> detect(Path mediaPath) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }

        List<SceneSegment> segments = new ArrayList<>();
        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile())) {
            grabber.start();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            double[] previousFeature = null;
            Duration segmentStart = Duration.ZERO;
            long frameIndex = 0;
            int frameStep = Math.max(1, (int) Math.round(Math.max(1.0, grabber.getFrameRate()) / 2.0));

            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                if (frameIndex % frameStep != 0) {
                    frameIndex++;
                    continue;
                }
                Duration timestamp = Duration.ofMillis((long) (grabber.getTimestamp() / 1000.0));
                Mat mat = converter.convert(frame);
                double[] feature = computeFeature(mat);
                mat.release();
                if (previousFeature != null) {
                    double diff = euclidean(previousFeature, feature) * 100;
                    if (diff > config.threshold() && timestamp.minus(segmentStart).compareTo(config.minimumSceneLength()) >= 0) {
                        segments.add(new SceneSegment(segmentStart, timestamp));
                        segmentStart = timestamp;
                    }
                }
                previousFeature = feature;
                frameIndex++;
            }

            Duration total = Duration.ofMillis((long) (grabber.getLengthInTime() / 1000.0));
            if (total.isZero()) {
                total = Duration.ofMillis((long) (grabber.getTimestamp() / 1000.0));
            }
            if (total.isZero()) {
                total = segmentStart.plus(config.minimumSceneLength());
            }
            segments.add(new SceneSegment(segmentStart, total));
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new IOException("Failed to detect scenes", e);
        }
        return mergeShortSegments(segments, config.minimumSceneLength());
    }

    private static double[] computeFeature(Mat mat) {
        Mat resized = new Mat();
        opencv_imgproc.resize(mat, resized, new Size(64, 36));
        Mat hsv = new Mat();
        opencv_imgproc.cvtColor(resized, hsv, opencv_imgproc.COLOR_BGR2HSV);
        Mat meanMat = new Mat();
        Mat stdMat = new Mat();
        opencv_core.meanStdDev(hsv, meanMat, stdMat);
        DoubleIndexer meanIdx = meanMat.createIndexer();
        DoubleIndexer stdIdx = stdMat.createIndexer();
        double[] feature = new double[]{
                meanIdx.get(0, 0),
                meanIdx.get(Math.min(1, meanMat.rows() - 1), 0),
                meanIdx.get(Math.min(2, meanMat.rows() - 1), 0),
                stdIdx.get(0, 0),
                stdIdx.get(Math.min(1, stdMat.rows() - 1), 0),
                stdIdx.get(Math.min(2, stdMat.rows() - 1), 0)
        };
        meanMat.release();
        stdMat.release();
        hsv.release();
        resized.release();
        return feature;
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private static List<SceneSegment> mergeShortSegments(List<SceneSegment> segments, Duration minimumLength) {
        if (segments.isEmpty()) {
            return segments;
        }
        List<SceneSegment> merged = new ArrayList<>();
        SceneSegment current = segments.get(0);
        for (int i = 1; i < segments.size(); i++) {
            SceneSegment next = segments.get(i);
            if (current.duration().compareTo(minimumLength) < 0) {
                current = new SceneSegment(current.start(), next.end());
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}





