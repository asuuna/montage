package app.media.proxy;

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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyManagerTest {
    @Test
    void createsProxyFile() throws Exception {
        Path media = Files.createTempFile("proxy-source", ".mp4");
        createSampleVideo(media);
        Path proxyDir = Files.createTempDirectory("proxy-cache");
        ProxyManager manager = new ProxyManager(proxyDir);
        Path proxy = manager.ensureProxy(media);
        assertTrue(Files.exists(proxy));
    }

    private static void createSampleVideo(Path output) throws Exception {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output.toFile(), 320, 240, 1)) {
            recorder.setFormat("mp4");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(2_000_000);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFrameRate(24);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(96_000);
            recorder.setSampleRate(16_000);
            recorder.start();
            int frames = 48;
            int samplesPerFrame = 16_000 / 24;
            short[] audio = new short[samplesPerFrame];
            ShortBuffer audioBuffer = ShortBuffer.wrap(audio);
            for (int i = 0; i < frames; i++) {
                BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g = image.createGraphics();
                g.setColor(i < 24 ? Color.BLUE : Color.GREEN);
                g.fillRect(0, 0, 320, 240);
                g.setColor(Color.WHITE);
                g.drawString("Frame " + i, 10, 20);
                g.dispose();
                recorder.record(converter.convert(image));
                for (int j = 0; j < audio.length; j++) {
                    audio[j] = (short) (Math.sin(2 * Math.PI * j / audio.length) * 8_000);
                }
                audioBuffer.rewind();
                recorder.recordSamples(16_000, 1, audioBuffer);
            }
            recorder.stop();
        }
    }
}
