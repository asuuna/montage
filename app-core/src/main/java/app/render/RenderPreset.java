package app.render;

public enum RenderPreset {
    YOUTUBE_1080P("mp4", "h264", 8_000_000, 192_000, 1920, 1080, 30),
    TIKTOK_VERTICAL("mp4", "h264", 6_000_000, 160_000, 1080, 1920, 30),
    INSTAGRAM_SQUARE("mp4", "h264", 5_000_000, 160_000, 1080, 1080, 30),
    PRORES_422("mov", "prores_ks", 200_000_000, 384_000, 3840, 2160, 30);

    private final String container;
    private final String codec;
    private final int videoBitrate;
    private final int audioBitrate;
    private final int width;
    private final int height;
    private final int frameRate;

    RenderPreset(String container, String codec, int videoBitrate, int audioBitrate, int width, int height, int frameRate) {
        this.container = container;
        this.codec = codec;
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
    }

    public String container() {
        return container;
    }

    public String codec() {
        return codec;
    }

    public int videoBitrate() {
        return videoBitrate;
    }

    public int audioBitrate() {
        return audioBitrate;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int frameRate() {
        return frameRate;
    }
}
