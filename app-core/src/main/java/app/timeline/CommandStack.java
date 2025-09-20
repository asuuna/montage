package app.timeline;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandStack {
    private final Deque<TimelineCommand> undoStack = new ArrayDeque<>();
    private final Deque<TimelineCommand> redoStack = new ArrayDeque<>();

    public void push(TimelineCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }
        TimelineCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }
        TimelineCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
