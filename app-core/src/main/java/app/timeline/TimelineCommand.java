package app.timeline;

public interface TimelineCommand {
    void execute();

    void undo();

    String description();
}
