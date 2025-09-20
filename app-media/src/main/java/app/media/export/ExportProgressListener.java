package app.media.export;

public interface ExportProgressListener {
    void onProgress(double progress);

    ExportProgressListener NO_OP = progress -> { };
}
