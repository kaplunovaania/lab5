package ru.itmo.seals.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.itmo.seals.service.UserService;
import ru.itmo.seals.storage.*;
import javafx.application.Platform;

import ru.itmo.seals.service.*;
import ru.itmo.seals.storage.*;

public class FxApp extends Application {

    private TaskCollectionManager taskManager;
    private ChecklistCollectionManager checklistManager;
    private UserService userService;
    private DatabaseManager dbManager;
    private DatabaseStorage dbStorage;

    @Override
    public void start(Stage primaryStage) {
        // 1. Подключаемся к БД
        dbManager = new DatabaseManager();
        if (!dbManager.connect()) {
            showErrorAndExit("Не удалось подключиться к базе данных. Проверьте настройки и запустите PostgreSQL.");
            return;
        }

        // 2. Инициализируем схему
        if (!dbManager.initializeSchema()) {
            showErrorAndExit("Не удалось инициализировать схему базы данных.");
            return;
        }

        // 3. Создаём сервисы
        dbStorage = new DatabaseStorage(dbManager);
        taskManager = new TaskCollectionManager();
        checklistManager = new ChecklistCollectionManager();
        userService = new UserService(dbManager);

        taskManager.setDatabaseStorage(dbStorage);
        // checklistManager.setDatabaseStorage(dbStorage); // аналогично

        // 4. Загружаем данные в память
        taskManager.loadFromDatabase();
        // checklistManager.loadFromDatabase();

        // 5. Показываем окно авторизации
        if (!showLoginWindow(primaryStage)) {
            dbManager.close();
            return;
        }

        // 6. Показываем основное окно
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itmo/seals/ui/main-view.fxml"));
            Parent root = loader.load();

            MasterController controller = loader.getController();
            controller.init(taskManager, checklistManager, dbStorage, userService);

            primaryStage.setTitle("Task Manager - " + userService.getCurrentUserLogin());
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.setOnCloseRequest(e -> {
                dbManager.close();
            });
            primaryStage.show();

        } catch (Exception e) {
            showErrorAndExit("Ошибка загрузки интерфейса: " + e.getMessage());
        }
    }

    private boolean showLoginWindow(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/itmo/seals/ui/login-view.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setUserService(userService);

            Stage loginStage = new Stage();
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.initOwner(owner);
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root, 400, 300));
            loginStage.setResizable(false);

            controller.setStage(loginStage);
            loginStage.showAndWait();

            return userService.isLoggedIn();

        } catch (Exception e) {
            System.err.println("Error showing login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}