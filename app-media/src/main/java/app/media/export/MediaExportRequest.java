package app.media.export;

import java.nio.file.Path;
import java.util.Objects;

public record MediaExportRequest(
        Path input,
        Path output,
        ExportPreset preset,
        Integer targetWidth,
        Integer targetHeight,
        Double targetFrameRate
) {
    public MediaExportRequest {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(preset, "preset");
    }

    public int widthOrDefault(int value) {
        return targetWidth != null ? targetWidth : value;
    }

    public int heightOrDefault(int value) {
        return targetHeight != null ? targetHeight : value;
    }

    public double frameRateOrDefault(double value) {
        return targetFrameRate != null && targetFrameRate > 0 ? targetFrameRate : value;
    }
}
