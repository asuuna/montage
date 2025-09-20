package app.media.probe;

public record MediaMetadata(
        long durationMillis,
        double frameRate,
        int width,
        int height,
        long videoBitrate,
        long audioBitrate,
        int audioChannels,
        String videoCodec,
        String audioCodec,
        long fileSizeBytes
) {
    public double durationSeconds() {
        return durationMillis / 1000.0;
    }
}
