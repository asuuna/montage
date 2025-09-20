package app.media;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

public final class TestMediaFactory {
    private TestMediaFactory() {
    }

    public static Path createSampleVideo(Path output, int width, int height, int frames, double fps) throws Exception {
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int sampleRate = 44_100;
        int audioChannels = 1;
        int samplesPerFrame = (int) Math.round(sampleRate / fps);
        short[] audioSamples = new short[samplesPerFrame * audioChannels];
        ShortBuffer audioBuffer = ShortBuffer.wrap(audioSamples);

        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output.toFile(), width, height, audioChannels)) {
            recorder.setFormat("mp4");
            recorder.setFrameRate(fps);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(2_000_000);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(128_000);
            recorder.setSampleRate(sampleRate);
            recorder.start();

            for (int i = 0; i < frames; i++) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(new Color((i * 40) % 255, (i * 80) % 255, (i * 120) % 255));
                g2d.fillRect(0, 0, width, height);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Frame " + i, 10, 20);
                g2d.dispose();

                recorder.record(converter.convert(image));
                audioBuffer.rewind();
                recorder.recordSamples(sampleRate, audioChannels, audioBuffer);
            }
            recorder.stop();
        }
        return output;
    }
}
