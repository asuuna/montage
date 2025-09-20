package app.ui;

import app.ui.i18n.I18n;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MontageApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();
        Scene scene = new Scene(mainView, 960, 600);
        scene.getStylesheets().add(getClass().getResource("/app/ui/styles.css").toExternalForm());
        primaryStage.titleProperty().bind(I18n.bind("app.title"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
