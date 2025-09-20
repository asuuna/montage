package app.ui.timeline;

import app.ui.i18n.I18n;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TimelineView extends BorderPane {

    private final TimelineController controller;
    private final TimelineCanvas canvas;
    private final Label playheadStatus = new Label();
    private final Label playbackStatus = new Label();
    private boolean playing;

    public TimelineView() {
        controller = new TimelineController();
        getStyleClass().add("timeline-root");
        canvas = new TimelineCanvas(controller);
        controller.addListener(() -> {
            canvas.render();
            updateStatus();
        });
        canvas.render();
        setTop(createToolbar());
        setCenter(createTimelinePane());
        setBottom(createStatusBar());
        setPadding(new Insets(8, 0, 8, 0));
        setFocusTraversable(true);
        setupKeyBindings();
        setupDragAndDrop();
        I18n.localeProperty().addListener((obs, oldLocale, newLocale) -> {
            updateStatus();
            refreshPlaybackStatus();
        });
        refreshPlaybackStatus();
        updateStatus();
    }

    private ToolBar createToolbar() {
        Button undo = button("timeline.toolbar.undo", () -> controller.undo());
        undo.setTooltip(tooltip("timeline.tooltip.undo"));

        Button redo = button("timeline.toolbar.redo", () -> controller.redo());
        redo.setTooltip(tooltip("timeline.tooltip.redo"));

        Button splitButton = button("timeline.toolbar.split",
                () -> controller.splitAtPlayhead(controller.getVideoTrack().getId()));
        splitButton.setTooltip(tooltip("timeline.tooltip.split"));

        Button rippleButton = button("timeline.toolbar.ripple",
                () -> controller.rippleDelete(controller.getTimeline().getPlayhead(),
                        controller.getTimeline().getPlayhead().plusSeconds(1)));
        rippleButton.setTooltip(tooltip("timeline.tooltip.ripple"));

        Button zoomIn = button("timeline.toolbar.zoomIn", () -> controller.zoomByFactor(1.2));
        zoomIn.setTooltip(tooltip("timeline.tooltip.zoomIn"));

        Button zoomOut = button("timeline.toolbar.zoomOut", () -> controller.zoomByFactor(1 / 1.2));
        zoomOut.setTooltip(tooltip("timeline.tooltip.zoomOut"));

        Slider zoomSlider = new Slider(20, 400, controller.getPixelsPerSecond());
        Tooltip zoomTooltip = new Tooltip();
        zoomTooltip.textProperty().bind(I18n.bind("timeline.toolbar.zoom.tooltip"));
        zoomSlider.setTooltip(zoomTooltip);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> controller.setPixelsPerSecond(newVal.doubleValue()));
        zoomSlider.setPrefWidth(180);

        CheckBox snapToggle = new CheckBox();
        snapToggle.textProperty().bind(I18n.bind("timeline.toolbar.snap"));
        snapToggle.setTooltip(tooltip("timeline.tooltip.snap"));
        snapToggle.setSelected(true);
        snapToggle.selectedProperty().addListener((obs, oldVal, selected) -> controller.toggleSnapping());

        Label shortcuts = new Label();
        shortcuts.textProperty().bind(I18n.bind("timeline.shortcuts.label"));
        shortcuts.setStyle("-fx-text-fill: #b6bad1;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolbar = new ToolBar(undo, redo, splitButton, rippleButton, zoomOut, zoomIn, zoomSlider, snapToggle, shortcuts);
        toolbar.setStyle("-fx-background-color: rgba(42,46,58,0.9);");
        return toolbar;
    }

    private ScrollPane createTimelinePane() {
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.accessibleTextProperty().bind(I18n.bind("timeline.accessible"));
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        canvas.setOnMouseClicked(event -> {
            Duration clickedPosition = Duration.ofMillis((long) (event.getX() / controller.getPixelsPerSecond() * 1000));
            controller.movePlayhead(clickedPosition);
        });
        return scrollPane;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(16, playheadStatus, playbackStatus);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #20232d; -fx-text-fill: #e1e4f2;");
        return statusBar;
    }

    private Button button(String key, Runnable action) {
        Button button = new Button();
        button.textProperty().bind(I18n.bind(key));
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }

    private Tooltip tooltip(String key) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(I18n.bind(key));
        return tooltip;
    }

    private void setupKeyBindings() {
        setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                controller.undo();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                controller.redo();
                event.consume();
            } else if (event.getCode() == KeyCode.S) {
                controller.splitAtPlayhead(controller.getVideoTrack().getId());
                event.consume();
            } else if (event.getCode() == KeyCode.DELETE) {
                controller.rippleDelete(controller.getTimeline().getPlayhead(),
                        controller.getTimeline().getPlayhead().plusSeconds(1));
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                togglePlayPause();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                controller.movePlayheadBySeconds(event.isShiftDown() ? -1 : -0.1);
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                controller.movePlayheadBySeconds(event.isShiftDown() ? 1 : 0.1);
                event.consume();
            }
        });
    }

    private void togglePlayPause() {
        playing = !playing;
        refreshPlaybackStatus();
    }

    private void setupDragAndDrop() {
        setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            }
        });
        setOnDragDropped(event -> {
            var dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                List<Path> files = dragboard.getFiles().stream().map(java.io.File::toPath).toList();
                controller.importMedia(files);
                event.setDropCompleted(true);
                requestFocus();
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private void updateStatus() {
        Duration playhead = controller.getTimeline().getPlayhead();
        double playheadSeconds = playhead.toMillis() / 1000.0;
        double zoom = controller.getPixelsPerSecond();
        playheadStatus.setText(I18n.t("timeline.status.playhead", playheadSeconds, zoom));
    }

    private void refreshPlaybackStatus() {
        playbackStatus.setText(I18n.t(playing ? "timeline.playback.playing" : "timeline.playback.stopped"));
    }

    public TimelineController getController() {
        return controller;
    }

    public TimelineCanvas getCanvas() {
        return canvas;
    }
}
