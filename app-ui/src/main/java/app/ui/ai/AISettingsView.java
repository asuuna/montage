package app.ui.ai;

import app.ai.config.SceneDetectionConfig;
import app.ai.config.SilenceDetectionConfig;
import app.ai.system.GpuCapabilityDetector;
import app.ui.i18n.I18n;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class AISettingsView extends GridPane {
    private final DoubleProperty sceneSensitivity = new SimpleDoubleProperty(SceneDetectionConfig.defaultConfig().threshold());
    private final ObjectProperty<Duration> minimumSceneLength = new SimpleObjectProperty<>(SceneDetectionConfig.defaultConfig().minimumSceneLength());
    private final DoubleProperty silenceThreshold = new SimpleDoubleProperty(SilenceDetectionConfig.defaultConfig().rmsThreshold());
    private final BooleanProperty gpuEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty telemetryEnabled = new SimpleBooleanProperty(false);
    private final ObjectProperty<Path> modelDirectory = new SimpleObjectProperty<>();

    public AISettingsView() {
        getStyleClass().add("ai-settings-panel");
        setHgap(12);
        setVgap(10);
        setPadding(new Insets(16));

        int row = 0;
        add(label("ai.settings.sceneSensitivity"), 0, row);
        Slider sensitivitySlider = new Slider(5, 80, sceneSensitivity.get());
        sensitivitySlider.valueProperty().bindBidirectional(sceneSensitivity);
        sensitivitySlider.setShowTickMarks(true);
        sensitivitySlider.setShowTickLabels(true);
        add(sensitivitySlider, 1, row++);

        add(label("ai.settings.minimumSceneLength"), 0, row);
        Slider sceneLengthSlider = new Slider(0.5, 5, minimumSceneLength.get().toMillis() / 1000.0);
        sceneLengthSlider.setMajorTickUnit(0.5);
        sceneLengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> minimumSceneLength.set(Duration.ofMillis((long) (newVal.doubleValue() * 1000))));
        add(sceneLengthSlider, 1, row++);

        add(label("ai.settings.silenceThreshold"), 0, row);
        Slider silenceSlider = new Slider(0.005, 0.2, silenceThreshold.get());
        silenceSlider.valueProperty().bindBidirectional(silenceThreshold);
        add(silenceSlider, 1, row++);

        add(label("ai.settings.gpu"), 0, row);
        CheckBox gpuToggle = new CheckBox();
        gpuToggle.textProperty().bind(I18n.bind("ai.settings.gpu.toggle"));
        gpuToggle.selectedProperty().bindBidirectional(gpuEnabled);
        Map<String, Boolean> capabilities = GpuCapabilityDetector.detect();
        boolean hasAcceleration = capabilities.values().stream().anyMatch(Boolean.TRUE::equals);
        gpuToggle.setDisable(!hasAcceleration);
        if (!hasAcceleration) {
            gpuEnabled.set(false);
        }
        Label capabilityLabel = new Label();
        capabilityLabel.getStyleClass().add(hasAcceleration ? "ai-capability-info" : "ai-capability-warning");
        capabilityLabel.textProperty().bind(capabilitySummary(capabilities));
        add(gpuToggle, 1, row);
        add(capabilityLabel, 1, row++, 2, 1);

        add(label("ai.settings.telemetry"), 0, row);
        CheckBox telemetryToggle = new CheckBox();
        telemetryToggle.textProperty().bind(I18n.bind("ai.settings.telemetry.toggle"));
        telemetryToggle.selectedProperty().bindBidirectional(telemetryEnabled);
        add(telemetryToggle, 1, row++);

        add(label("ai.settings.modelDirectory"), 0, row);
        TextField modelPathField = new TextField();
        modelPathField.promptTextProperty().bind(I18n.bind("ai.settings.modelDirectory.prompt"));
        modelDirectory.addListener((obs, oldVal, newVal) -> modelPathField.setText(newVal == null ? "" : newVal.toString()));
        Button browse = new Button();
        browse.textProperty().bind(I18n.bind("ai.settings.modelDirectory.button"));
        browse.setOnAction(e -> browseForModelDirectory());
        add(modelPathField, 1, row);
        add(browse, 2, row++);

        add(label("ai.settings.modelType"), 0, row);
        ChoiceBox<String> modelType = new ChoiceBox<>();
        populateModelOptions(modelType);
        modelType.getSelectionModel().selectFirst();
        I18n.localeProperty().addListener((obs, oldLocale, newLocale) -> {
            int selectedIndex = modelType.getSelectionModel().getSelectedIndex();
            populateModelOptions(modelType);
            if (selectedIndex >= 0) {
                modelType.getSelectionModel().select(Math.min(selectedIndex, modelType.getItems().size() - 1));
            }
        });
        add(modelType, 1, row++);
    }

    private void populateModelOptions(ChoiceBox<String> choiceBox) {
        choiceBox.getItems().setAll(
                I18n.t("ai.settings.modelType.vosk"),
                I18n.t("ai.settings.modelType.whisperTiny"),
                I18n.t("ai.settings.modelType.whisperBase"));
    }

    private Label label(String key) {
        Label label = new Label();
        label.textProperty().bind(I18n.bind(key));
        return label;
    }

    private StringBinding capabilitySummary(Map<String, Boolean> capabilities) {
        return Bindings.createStringBinding(() -> I18n.t("ai.settings.gpu.capabilities",
                toYesNo(capabilities.get("cuda")),
                toYesNo(capabilities.get("directml")),
                toYesNo(capabilities.get("metal"))),
                I18n.localeProperty());
    }

    private String toYesNo(Boolean value) {
        return I18n.t(Boolean.TRUE.equals(value) ? "boolean.yes" : "boolean.no");
    }

    private void browseForModelDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(I18n.t("ai.settings.modelDirectory.dialog"));
        Path current = modelDirectory.get();
        if (current != null) {
            File initial = current.toFile();
            if (initial.isDirectory()) {
                chooser.setInitialDirectory(initial);
            }
        }
        Window window = getScene() != null ? getScene().getWindow() : null;
        File selected = chooser.showDialog(window);
        if (selected != null) {
            modelDirectory.set(selected.toPath());
        }
    }

    public double getSceneSensitivity() {
        return sceneSensitivity.get();
    }

    public Duration getMinimumSceneLength() {
        return minimumSceneLength.get();
    }

    public double getSilenceThreshold() {
        return silenceThreshold.get();
    }

    public boolean isGpuEnabled() {
        return gpuEnabled.get();
    }

    public boolean isTelemetryEnabled() {
        return telemetryEnabled.get();
    }

    public Path getModelDirectory() {
        return modelDirectory.get();
    }
}
