package app.ui;

import app.ui.ai.AISettingsView;
import app.ui.i18n.I18n;
import app.ui.timeline.TimelineView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class MainView extends BorderPane {
    private final TimelineView timelineView = new TimelineView();
    private final BooleanProperty darkTheme = new SimpleBooleanProperty(true);

    public MainView() {
        setTop(createMenuBar());
        setCenter(createContent());

        darkTheme.addListener((obs, oldVal, newVal) -> applyTheme(getScene()));
        sceneProperty().addListener((obs, oldScene, newScene) -> applyTheme(newScene));
    }

    private MenuBar createMenuBar() {
        MenuItem newProject = menuItem("menu.file.new", null);
        MenuItem openProject = menuItem("menu.file.open", null);
        MenuItem export = menuItem("menu.file.export", null);
        Menu fileMenu = menu("menu.file", newProject, openProject, export);

        MenuItem undo = menuItem("menu.edit.undo", () -> timelineView.getController().undo());
        MenuItem redo = menuItem("menu.edit.redo", () -> timelineView.getController().redo());
        Menu editMenu = menu("menu.edit", undo, redo);

        MenuItem toggleTheme = createToggleThemeItem();
        Menu viewMenu = menu("menu.view", toggleTheme);

        MenuItem shortcuts = menuItem("menu.help.shortcuts", this::showShortcuts);
        Menu helpMenu = menu("menu.help", shortcuts);

        return new MenuBar(fileMenu, editMenu, viewMenu, helpMenu);
    }

    private MenuItem createToggleThemeItem() {
        MenuItem toggleTheme = new MenuItem();
        toggleTheme.textProperty().bind(Bindings.createStringBinding(
                () -> darkTheme.get() ? I18n.t("menu.view.theme.light") : I18n.t("menu.view.theme.dark"),
                darkTheme, I18n.localeProperty()));
        toggleTheme.setOnAction(e -> toggleTheme());
        return toggleTheme;
    }

    private Menu menu(String key, MenuItem... items) {
        Menu menu = new Menu();
        menu.textProperty().bind(I18n.bind(key));
        if (items != null) {
            menu.getItems().addAll(items);
        }
        return menu;
    }

    private MenuItem menuItem(String key, Runnable action) {
        MenuItem item = new MenuItem();
        item.textProperty().bind(I18n.bind(key));
        if (action != null) {
            item.setOnAction(e -> action.run());
        }
        return item;
    }

    private SplitPane createContent() {
        BorderPane previewPane = createPreviewPane();
        SplitPane vertical = new SplitPane(previewPane, timelineView);
        vertical.setOrientation(Orientation.VERTICAL);
        vertical.setDividerPositions(0.38);

        AISettingsView aiSettingsView = new AISettingsView();
        aiSettingsView.setMinWidth(280);
        aiSettingsView.setMaxWidth(360);

        SplitPane horizontal = new SplitPane(vertical, aiSettingsView);
        horizontal.setDividerPositions(0.72);
        return horizontal;
    }

    private BorderPane createPreviewPane() {
        BorderPane preview = new BorderPane();
        preview.getStyleClass().add("preview-pane");
        preview.setPadding(new Insets(16));

        Label title = createLabel("preview.title");
        title.getStyleClass().add("preview-title");
        preview.setTop(title);

        Label placeholder = createLabel("preview.placeholder");
        StackPane monitor = new StackPane(placeholder);
        monitor.getStyleClass().add("preview-monitor");
        preview.setCenter(monitor);

        Button playButton = createButton("preview.play");
        Button stopButton = createButton("preview.stop");
        playButton.setFocusTraversable(false);
        stopButton.setFocusTraversable(false);
        playButton.setOnAction(e -> timelineView.requestFocus());
        stopButton.setOnAction(e -> timelineView.requestFocus());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox transport = new HBox(10, playButton, stopButton, spacer);
        transport.setPadding(new Insets(12, 0, 0, 0));
        preview.setBottom(transport);

        preview.setOnMouseClicked(e -> timelineView.requestFocus());
        return preview;
    }

    private void applyTheme(Scene scene) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();
        String stylesheet = darkTheme.get() ? "/app/ui/styles.css" : "/app/ui/styles-light.css";
        scene.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
    }

    private void toggleTheme() {
        darkTheme.set(!darkTheme.get());
    }

    private Button createButton(String key) {
        Button button = new Button();
        button.textProperty().bind(I18n.bind(key));
        return button;
    }

    private Label createLabel(String key) {
        Label label = new Label();
        label.textProperty().bind(I18n.bind(key));
        return label;
    }

    private void showShortcuts() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.t("shortcuts.dialog.title"));
        alert.setHeaderText(I18n.t("shortcuts.dialog.header"));
        alert.setContentText(I18n.t("shortcuts.dialog.content"));
        alert.showAndWait();
    }
}
