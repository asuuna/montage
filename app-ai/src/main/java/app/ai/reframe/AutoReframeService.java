package app.ai.reframe;

import app.ai.config.ReframeConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;

public class AutoReframeService {
    private final ReframeConfig config;

    public AutoReframeService(ReframeConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public ReframeResult reframe(Path input, Path output) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        if (!Files.exists(input)) {
            throw new IOException("Input media does not exist: " + input);
        }
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(input.toFile())) {
            grabber.start();
            double frameRate = grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 30.0;
            int sourceWidth = grabber.getImageWidth();
            int sourceHeight = grabber.getImageHeight();

            int targetWidth;
            int targetHeight;
            double aspectRatio = config.outputAspectWidth() / config.outputAspectHeight();
            if (sourceWidth / (double) sourceHeight > aspectRatio) {
                targetHeight = sourceHeight;
                targetWidth = (int) Math.round(targetHeight * aspectRatio);
            } else {
                targetWidth = sourceWidth;
                targetHeight = (int) Math.round(targetWidth / aspectRatio);
            }

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output.toFile(), targetWidth, targetHeight, grabber.getAudioChannels())) {
                recorder.setFormat("mp4");
                recorder.setFrameRate(frameRate);
                recorder.setVideoCodec(grabber.getVideoCodec());
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setSampleRate(grabber.getSampleRate());
                recorder.start();

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Rect roi = null;
                Frame frame;
                long processedFrames = 0;
                while ((frame = grabber.grab()) != null) {
                    if (frame.image == null) {
                        recorder.record(frame);
                        continue;
                    }
                    Mat mat = converter.convert(frame);
                    if (roi == null) {
                        roi = initialRoi(mat, aspectRatio);
                    } else {
                        roi = blendRoi(roi, detectSubject(mat, roi));
                    }
                    Mat cropped = cropToAspect(mat, roi, targetWidth, targetHeight);
                    recorder.record(converter.convert(cropped));
                    cropped.release();
                    mat.release();
                    processedFrames++;
                }
                recorder.stop();
                grabber.stop();
                double durationSeconds = processedFrames / frameRate;
                return new ReframeResult(output, java.time.Duration.ofMillis((long) (durationSeconds * 1000)));
            }
        } catch (FFmpegFrameGrabber.Exception | FFmpegFrameRecorder.Exception e) {
            throw new IOException("Failed to auto-reframe video", e);
        }
    }

    private Rect initialRoi(Mat mat, double aspectRatio) {
        Mat edges = detectEdges(mat);
        Rect bounding = opencv_imgproc.boundingRect(edges);
        edges.release();
        if (bounding.width() == 0 || bounding.height() == 0) {
            int centerX = mat.cols() / 2;
            int centerY = mat.rows() / 2;
            int width = (int) (mat.cols() * 0.6);
            int height = (int) (width / aspectRatio);
            bounding = new Rect(Math.max(0, centerX - width / 2), Math.max(0, centerY - height / 2), Math.min(width, mat.cols()), Math.min(height, mat.rows()));
        }
        return bounding;
    }

    private Rect detectSubject(Mat mat, Rect fallback) {
        Mat edges = detectEdges(mat);
        Rect bounding = opencv_imgproc.boundingRect(edges);
        edges.release();
        if (bounding.width() == 0 || bounding.height() == 0) {
            return fallback;
        }
        return bounding;
    }

    private Rect blendRoi(Rect previous, Rect detected) {
        int x = (int) Math.round(previous.x() * 0.7 + detected.x() * 0.3);
        int y = (int) Math.round(previous.y() * 0.7 + detected.y() * 0.3);
        int width = (int) Math.round(previous.width() * 0.7 + detected.width() * 0.3);
        int height = (int) Math.round(previous.height() * 0.7 + detected.height() * 0.3);
        return new Rect(x, y, width, height);
    }

    private Mat detectEdges(Mat mat) {
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Mat edges = new Mat();
        opencv_imgproc.Canny(gray, edges, 50, 150);
        gray.release();
        return edges;
    }

    private Mat cropToAspect(Mat source, Rect roi, int targetWidth, int targetHeight) {
        double scaleY = targetHeight / (double) roi.height();
        double scaleX = targetWidth / (double) roi.width();
        double scale = Math.min(scaleX, scaleY);
        int cropWidth = (int) Math.round(targetWidth / scale);
        int cropHeight = (int) Math.round(targetHeight / scale);
        int x = (int) Math.round(roi.x() + roi.width() / 2.0 - cropWidth / 2.0);
        int y = (int) Math.round(roi.y() + roi.height() / 2.0 - cropHeight / 2.0);
        x = Math.max(0, Math.min(x, source.cols() - cropWidth));
        y = Math.max(0, Math.min(y, source.rows() - cropHeight));
        Rect cropRect = new Rect(x, y, Math.min(cropWidth, source.cols() - x), Math.min(cropHeight, source.rows() - y));
        Mat cropped = new Mat(source, cropRect).clone();
        Mat resized = new Mat();
        opencv_imgproc.resize(cropped, resized, new Size(targetWidth, targetHeight));
        cropped.release();
        return resized;
    }
}
