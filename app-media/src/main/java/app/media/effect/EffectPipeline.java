package app.media.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectPipeline.class);

    private final EffectParameters parameters;
    private final List<EffectPlugin> plugins = new ArrayList<>();

    public EffectPipeline(EffectParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    public void addPlugin(EffectPlugin plugin) {
        plugins.add(Objects.requireNonNull(plugin, "plugin"));
    }

    public void process(Path input, Path output) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        if (!Files.exists(input)) {
            throw new IOException("Input media not found: " + input);
        }
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(input.toFile())) {
            grabber.start();
            int width = Math.max(1, grabber.getImageWidth());
            int height = Math.max(1, grabber.getImageHeight());
            double frameRate = grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 30.0;

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output.toFile(), width, height, grabber.getAudioChannels())) {
                recorder.setFormat("mp4");
                recorder.setFrameRate(frameRate);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setVideoBitrate(5_000_000);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioBitrate(192_000);
                recorder.setSampleRate(grabber.getSampleRate());
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.start();

                Java2DFrameConverter converter = new Java2DFrameConverter();
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    if (frame.image != null) {
                        BufferedImage image = converter.getBufferedImage(frame);
                        BufferedImage processed = applyEffects(image);
                        recorder.record(converter.convert(processed));
                    } else if (frame.samples != null) {
                        applyDucking(frame);
                        recorder.record(frame);
                    }
                }
                recorder.stop();
            }
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception | FFmpegFrameRecorder.Exception e) {
            throw new IOException("Failed to process effects", e);
        }
    }

    private BufferedImage applyEffects(BufferedImage source) {
        BufferedImage working = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = working.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();

        applyBrightnessContrast(working, parameters.brightness(), parameters.contrast());
        applySaturation(working, parameters.saturation());
        if (parameters.titleText() != null && !parameters.titleText().isBlank()) {
            overlayTitle(working, parameters.titleText(), parameters.titleOpacity());
        }
        if (parameters.lutPath() != null) {
            LOGGER.warn("3D LUT application not implemented yet: {}", parameters.lutPath());
        }

        for (EffectPlugin plugin : plugins) {
            try {
                working = plugin.apply(working);
            } catch (Exception e) {
                LOGGER.warn("Effect plugin {} failed: {}", plugin.getClass().getSimpleName(), e.getMessage());
            }
        }
        return working;
    }

    private static void applyBrightnessContrast(BufferedImage image, double brightness, double contrast) {
        if (brightness == 0.0 && contrast == 0.0) {
            return;
        }
        float scale = (float) (1.0 + contrast);
        float offset = (float) (brightness * 255);
        RescaleOp op = new RescaleOp(new float[]{scale, scale, scale, 1.0f}, new float[]{offset, offset, offset, 0f}, null);
        op.filter(image, image);
    }

    private static void applySaturation(BufferedImage image, double saturation) {
        if (saturation == 0.0) {
            return;
        }
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                float[] hsb = Color.RGBtoHSB(red, green, blue, null);
                hsb[1] = clamp((float) (hsb[1] * (1.0 + saturation)), 0f, 1f);
                int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                image.setRGB(x, y, (alpha << 24) | (rgb & 0xFFFFFF));
            }
        }
    }

    private static void overlayTitle(BufferedImage image, String text, double opacity) {
        opacity = clamp((float) opacity, 0f, 1f);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
        g2d.setColor(new Color(0, 0, 0, (int) (opacity * 128)));
        g2d.fillRect(0, image.getHeight() - 120, image.getWidth(), 120);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
        g2d.drawString(text, 60, image.getHeight() - 50);
        g2d.dispose();
    }

    private static void applyDucking(Frame frame) {
        // Placeholder: real ducking would adjust audio samples based on metadata.
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

