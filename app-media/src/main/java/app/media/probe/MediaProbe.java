package app.media.probe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class MediaProbe {
    public MediaMetadata probe(Path mediaPath) throws IOException {
        Objects.requireNonNull(mediaPath, "mediaPath");
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile())) {
            grabber.start();
            long durationMicros = grabber.getLengthInTime();
            double frameRate = grabber.getVideoFrameRate();
            if (frameRate <= 0.0) {
                frameRate = grabber.getFrameRate();
            }

            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            long videoBitrate = grabber.getVideoBitrate();
            long audioBitrate = grabber.getAudioBitrate();
            int audioChannels = grabber.getAudioChannels();

            String videoCodec = grabber.getVideoCodecName();
            String audioCodec = grabber.getAudioCodecName();
            long fileSize = Files.size(mediaPath);

            grabber.stop();

            return new MediaMetadata(
                    durationMicros / 1000,
                    frameRate,
                    width,
                    height,
                    videoBitrate,
                    audioBitrate,
                    audioChannels,
                    videoCodec,
                    audioCodec,
                    fileSize
            );
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new IOException("Failed to probe media: " + mediaPath, e);
        }
    }
}
