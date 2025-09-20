package app.media.proxy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class ProxyManager {
    private final Path proxyDir;
    private final ConcurrentMap<Path, Path> cache = new ConcurrentHashMap<>();

    public ProxyManager(Path proxyDir) throws IOException {
        this.proxyDir = Objects.requireNonNull(proxyDir, "proxyDir");
        Files.createDirectories(proxyDir);
    }

    public Path ensureProxy(Path mediaPath) throws IOException {
        return cache.computeIfAbsent(mediaPath.toAbsolutePath(), this::generateProxyUnchecked);
    }

    private Path generateProxyUnchecked(Path mediaPath) {
        try {
            Path proxy = proxyDir.resolve(mediaPath.getFileName().toString() + ".proxy.mp4");
            if (Files.exists(proxy)) {
                return proxy;
            }
            try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile())) {
                grabber.start();
                int width = Math.max(320, grabber.getImageWidth() / 2);
                int height = Math.max(180, grabber.getImageHeight() / 2);
                double frameRate = grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 30.0;
                try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(proxy.toFile(), width, height, grabber.getAudioChannels())) {
                    recorder.setFormat("mp4");
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    recorder.setVideoBitrate(1_500_000);
                    recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                    recorder.setFrameRate(frameRate);
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                    recorder.setAudioBitrate(96_000);
                    recorder.setSampleRate(grabber.getSampleRate());
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.start();
                    Frame frame;
                    while ((frame = grabber.grab()) != null) {
                        recorder.record(frame);
                    }
                    recorder.stop();
                }
                grabber.stop();
            }
            return proxy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build proxy for " + mediaPath, e);
        }
    }
}
