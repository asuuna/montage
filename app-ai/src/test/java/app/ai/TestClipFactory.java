package app.ai;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

final class TestClipFactory {
    private TestClipFactory() {
    }

    static Path createSceneChangeClip(Path path) throws Exception {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(path.toFile(), 320, 240, 1)) {
            recorder.setFormat("mp4");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(2_000_000);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFrameRate(24);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(96_000);
            recorder.setSampleRate(16_000);
            recorder.start();

            int frames = 120;
            int samplesPerFrame = 16000 / 24;
            short[] audio = new short[samplesPerFrame];
            ShortBuffer buffer = ShortBuffer.wrap(audio);
            for (int i = 0; i < frames; i++) {
                BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g2d = image.createGraphics();
                Color color = (i < 40) ? Color.BLUE : (i < 80 ? Color.GREEN : Color.RED);
                g2d.setColor(color);
                g2d.fillRect(0, 0, 320, 240);
                g2d.setColor(Color.WHITE);
                g2d.drawString("Frame " + i, 10, 20);
                g2d.dispose();
                recorder.record(converter.convert(image));

                for (int j = 0; j < audio.length; j++) {
                    audio[j] = (short) ((i < 60) ? 0 : (Math.sin(2 * Math.PI * j / audio.length) * 10_000));
                }
                buffer.rewind();
                recorder.recordSamples(16_000, 1, buffer);
            }
            recorder.stop();
        }
        return path;
    }
}
