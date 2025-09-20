package app.ai.audio;

import app.ai.config.SilenceDetectionConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

public class SilenceDetectionService {
    private final SilenceDetectionConfig config;

    public SilenceDetectionService(SilenceDetectionConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public List<SilenceRange> detectSilence(Path mediaPath) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile())) {
            grabber.start();
            if (grabber.getAudioChannels() <= 0) {
                grabber.stop();
                return List.of();
            }

            List<SilenceRange> silences = new ArrayList<>();
            boolean silenceActive = false;
            Duration silenceStart = Duration.ZERO;
            long sampleRate = grabber.getSampleRate();
            if (sampleRate <= 0) {
                sampleRate = 48_000;
            }
            int windowSize = (int) Math.max(1024, sampleRate / 20);
            long samplesProcessed = 0;

            Frame frame;
            while ((frame = grabber.grabSamples()) != null) {
                if (!(frame.samples != null && frame.samples.length > 0 && frame.samples[0] instanceof java.nio.ShortBuffer)) {
                    continue;
                }
                java.nio.ShortBuffer buffer = ((java.nio.ShortBuffer) frame.samples[0]).duplicate();
                buffer.rewind();
                while (buffer.hasRemaining()) {
                    int chunk = Math.min(windowSize, buffer.remaining());
                    short[] temp = new short[chunk];
                    buffer.get(temp);
                    double rms = rms(temp, 0, chunk);
                    Duration currentTime = Duration.ofMillis((long) ((samplesProcessed / (double) sampleRate) * 1000));
                    samplesProcessed += chunk;

                    if (rms < config.rmsThreshold()) {
                        if (!silenceActive) {
                            silenceActive = true;
                            silenceStart = currentTime;
                        }
                    } else if (silenceActive) {
                        Duration silenceEnd = currentTime;
                        if (silenceEnd.minus(silenceStart).compareTo(config.minimumSilence()) >= 0) {
                            silences.add(new SilenceRange(silenceStart, silenceEnd));
                        }
                        silenceActive = false;
                    }
                }
            }

            if (silenceActive) {
                Duration end = Duration.ofMillis((long) ((samplesProcessed / (double) sampleRate) * 1000));
                if (end.minus(silenceStart).compareTo(config.minimumSilence()) >= 0) {
                    silences.add(new SilenceRange(silenceStart, end));
                }
            }

            grabber.stop();
            return silences;
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new IOException("Failed to detect silence", e);
        }
    }

    private static double rms(short[] samples, int offset, int length) {
        double sum = 0.0;
        for (int i = offset; i < offset + length; i++) {
            double normalized = samples[i] / 32768.0;
            sum += normalized * normalized;
        }
        return Math.sqrt(sum / length);
    }
}




