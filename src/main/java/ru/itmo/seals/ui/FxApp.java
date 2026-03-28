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

    // Передаём менеджеры из main
    public static void setManagers(TaskCollectionManager tm,
                                   ChecklistCollectionManager cm,
                                   FileStorage s) {
        taskManager = tm;
        checklistManager = cm;
        storage = s;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загружаем FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

        // Получаем контроллер и инициализируем его
        MasterController controller = loader.getController();
        controller.init(taskManager, checklistManager, storage);

        // Настраиваем окно
        primaryStage.setTitle("Task & Checklist Manager (Master-Detail)");
        primaryStage.setScene(new Scene(root, 1100, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Инициализация менеджеров
        taskManager = new TaskCollectionManager();
        checklistManager = new ChecklistCollectionManager();
        storage = new FileStorage();

        // Автозагрузка если есть аргумент командной строки
        if (args.length > 0) {
            System.out.println("Auto-loading from: " + args[0]);
            storage.load(args[0], taskManager, checklistManager);
        }

        // Запуск JavaFX
        launch(args);
    }
}
