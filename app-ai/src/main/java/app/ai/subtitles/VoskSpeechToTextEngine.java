package app.ai.subtitles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.vosk.Model;
import org.vosk.Recognizer;

public class VoskSpeechToTextEngine implements SpeechToTextEngine {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final Path modelPath;
    private final float targetSampleRate;

    public VoskSpeechToTextEngine(Path modelPath, float targetSampleRate) {
        this.modelPath = Objects.requireNonNull(modelPath, "modelPath");
        if (targetSampleRate <= 0) {
            throw new IllegalArgumentException("targetSampleRate must be > 0");
        }
        this.targetSampleRate = targetSampleRate;
    }

    @Override
    public List<SubtitleLine> transcribe(Path mediaPath) throws IOException {
        if (!Files.exists(modelPath)) {
            throw new IOException("Vosk model not found at " + modelPath);
        }
        if (!Files.exists(mediaPath)) {
            throw new IOException("Media file does not exist: " + mediaPath);
        }

        try (Model model = new Model(modelPath.toString());
             FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(mediaPath.toFile());
             Recognizer recognizer = new Recognizer(model, targetSampleRate)) {
            grabber.setSampleRate((int) targetSampleRate);
            grabber.setAudioChannels(1);
            grabber.start();
            List<SubtitleLine> subtitles = new ArrayList<>();
            int index = 1;
            Frame frame;
            while ((frame = grabber.grabSamples()) != null) {
                if (frame.samples == null || frame.samples.length == 0 || !(frame.samples[0] instanceof java.nio.ShortBuffer)) {
                    continue;
                }
                java.nio.ShortBuffer buffer = ((java.nio.ShortBuffer) frame.samples[0]).duplicate();
                buffer.rewind();
                short[] samples = new short[buffer.remaining()];
                buffer.get(samples);
                byte[] bytes = shortsToBytes(samples);
                boolean accepted = recognizer.acceptWaveForm(bytes, bytes.length);
                if (accepted) {
                    String resultJson = recognizer.getResult();
                    index = appendFromResult(subtitles, resultJson, index);
                }
            }
            String finalJson = recognizer.getFinalResult();
            appendFromResult(subtitles, finalJson, subtitles.size() + 1);
            grabber.stop();
            return subtitles;
        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
            }
            throw new IOException("Failed to transcribe media", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static int appendFromResult(List<SubtitleLine> subtitles, String json, int nextIndex) throws IOException {
        if (json == null || json.isBlank()) {
            return nextIndex;
        }
        Map<String, Object> map = OBJECT_MAPPER.readValue(json, MAP_TYPE);
        Object textObj = map.get("text");
        if (!(textObj instanceof String text) || text.isBlank()) {
            return nextIndex;
        }
        double start = ((List<Map<String, Object>>) map.getOrDefault("result", List.of()))
                .stream().map(entry -> (Number) entry.getOrDefault("start", 0.0))
                .mapToDouble(Number::doubleValue)
                .min().orElse(0);
        double end = ((List<Map<String, Object>>) map.getOrDefault("result", List.of()))
                .stream().map(entry -> (Number) entry.getOrDefault("end", start + 2.0))
                .mapToDouble(Number::doubleValue)
                .max().orElse(start + 2.0);
        subtitles.add(new SubtitleLine(nextIndex,
                Duration.ofMillis((long) (start * 1000)),
                Duration.ofMillis((long) (end * 1000)),
                text.trim()));
        return nextIndex + 1;
    }

    private static byte[] shortsToBytes(short[] samples) {
        ByteBuffer buffer = ByteBuffer.allocate(samples.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (short sample : samples) {
            buffer.putShort(sample);
        }
        return buffer.array();
    }
}
