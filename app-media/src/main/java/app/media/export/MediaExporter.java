package app.media.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class MediaExporter {
    public void export(MediaExportRequest request, ExportProgressListener listener) throws IOException {
        Objects.requireNonNull(request, "request");
        ExportProgressListener progressListener = listener != null ? listener : ExportProgressListener.NO_OP;

        Path parent = request.output().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(request.input().toFile())) {
            grabber.start();

            int sourceWidth = Math.max(1, grabber.getImageWidth());
            int sourceHeight = Math.max(1, grabber.getImageHeight());
            double frameRate = grabber.getVideoFrameRate();
            if (frameRate <= 0.0) {
                frameRate = grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 30.0;
            }

            int width = request.widthOrDefault(sourceWidth);
            int height = request.heightOrDefault(sourceHeight);
            double targetFrameRate = request.frameRateOrDefault(frameRate);
            long totalFrames = grabber.getLengthInFrames();
            if (totalFrames <= 0) {
                long durationMicros = grabber.getLengthInTime();
                if (durationMicros > 0) {
                    totalFrames = Math.max(1, Math.round((durationMicros / 1_000_000.0) * targetFrameRate));
                }
            }

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(request.output().toFile(), width, height, grabber.getAudioChannels())) {
                recorder.setFormat("mp4");
                recorder.setFrameRate(targetFrameRate);
                recorder.setGopSize((int) Math.max(1, Math.round(targetFrameRate * 2)));
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoBitrate(request.preset().videoBitrate());
                recorder.setOption("preset", request.preset().ffmpegPreset());
                recorder.setOption("crf", Integer.toString(request.preset().crf()));
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioBitrate(request.preset().audioBitrate());
                int audioChannels = Math.max(1, grabber.getAudioChannels());
                recorder.setAudioChannels(audioChannels);
                recorder.setSampleRate(grabber.getSampleRate() > 0 ? grabber.getSampleRate() : 48_000);

                recorder.start();
                long processedFrames = 0;
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                    if (frame.image != null && totalFrames > 0) {
                        processedFrames++;
                        double progress = Math.min(1.0, processedFrames / (double) totalFrames);
                        progressListener.onProgress(progress);
                    }
                }
                recorder.stop();
            }

            grabber.stop();
            progressListener.onProgress(1.0);
        } catch (FFmpegFrameGrabber.Exception | FFmpegFrameRecorder.Exception e) {
            throw new IOException("Failed to export media", e);
        }
    }
}
