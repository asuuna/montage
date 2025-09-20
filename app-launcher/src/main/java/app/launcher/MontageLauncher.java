package app.launcher;

import app.ui.MontageApplication;
import javafx.application.Application;

public final class MontageLauncher {
    private MontageLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(MontageApplication.class, args);
    }
}
