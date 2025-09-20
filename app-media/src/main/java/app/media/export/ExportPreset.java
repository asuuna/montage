package app.media.export;

public enum ExportPreset {
    FAST_PREVIEW(2_000_000, 128_000, "veryfast", 23),
    BALANCED(5_000_000, 160_000, "faster", 21),
    HIGH_QUALITY(12_000_000, 192_000, "slow", 18);

    private final int videoBitrate;
    private final int audioBitrate;
    private final String ffmpegPreset;
    private final int crf;

    ExportPreset(int videoBitrate, int audioBitrate, String ffmpegPreset, int crf) {
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.ffmpegPreset = ffmpegPreset;
        this.crf = crf;
    }

    public int videoBitrate() {
        return videoBitrate;
    }

    public int audioBitrate() {
        return audioBitrate;
    }

    public String ffmpegPreset() {
        return ffmpegPreset;
    }

    public int crf() {
        return crf;
    }
}
