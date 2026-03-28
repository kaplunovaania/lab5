package ru.itmo.seals.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.storage.FileStorage;

public class FxApp extends Application {

    private static TaskCollectionManager taskManager;
    private static ChecklistCollectionManager checklistManager;
    private static FileStorage storage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itmo/seals/ui/main-view.fxml"));
        Parent root = loader.load();

        MasterController controller = loader.getController();
        controller.init(taskManager, checklistManager, storage);

        primaryStage.setTitle("Task Manager");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        taskManager = new TaskCollectionManager();
        checklistManager = new ChecklistCollectionManager();
        storage = new FileStorage();

        if (args.length > 0) {
            storage.load(args[0], taskManager, checklistManager);
        }

        launch(args);
    }
}