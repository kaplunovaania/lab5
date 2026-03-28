package ru.itmo.seals.ui;

// === JavaFX Imports ===
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// === Project Imports ===
import ru.itmo.seals.model.*;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.storage.FileStorage;

// === Java Standard Imports ===
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class MasterController {

    // === ТАБЛИЦА (MASTER) ===
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Long> idColumn;
    @FXML private TableColumn<Task, String> textColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    // === ДЕТАЛИ (DETAIL) ===
    @FXML private Label detailId;
    @FXML private Label detailText;
    @FXML private Label detailStatus;
    @FXML private Label detailPriority;
    @FXML private Label detailDeadline;
    @FXML private Label detailAssignee;
    @FXML private Label detailOwner;

    // === ЧЕКЛИСТ ===
    @FXML private ListView<String> checklistView;

    // === СТАТУС ===
    @FXML private Label statusLabel;

    // === МЕНЕДЖЕРЫ ===
    private TaskCollectionManager taskManager;
    private ChecklistCollectionManager checklistManager;
    private FileStorage storage;

    private ObservableList<Task> taskList = FXCollections.observableArrayList();
    private Task selectedTask;

    // === ИНИЦИАЛИЗАЦИЯ ===
    public void init(TaskCollectionManager taskManager,
                     ChecklistCollectionManager checklistManager,
                     FileStorage storage) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.storage = storage;

        setupTable();
        setupSelectionListener();
        refreshData();
    }

    // === НАСТРОЙКА ТАБЛИЦЫ ===
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        taskTable.setItems(taskList);
    }

    // === ВЫБОР ЗАДАЧИ ===
    private void setupSelectionListener() {
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedTask = newSelection;
                        showTaskDetails(newSelection);
                    }
                }
        );
    }

    // === ПОКАЗ ДЕТАЛЕЙ ===
    private void showTaskDetails(Task task) {
        detailId.setText(String.valueOf(task.getId()));
        detailText.setText(task.getText());
        detailStatus.setText(task.getStatus().name());
        detailPriority.setText(task.getPriority().name());

        if (task.getDeadlineAt() != null) {
            LocalDate date = LocalDate.ofInstant(task.getDeadlineAt(), ZoneOffset.UTC);
            detailDeadline.setText(date.toString());
        } else {
            detailDeadline.setText("—");
        }

        detailAssignee.setText(task.getAssigneeUsername() != null
                ? task.getAssigneeUsername() : "—");

        detailOwner.setText(task.getOwnerUsername() != null
                ? task.getOwnerUsername() : "—");

        loadChecklist(task);
    }

    // === ЗАГРУЗКА ЧЕКЛИСТА ===
    private void loadChecklist(Task task) {
        List<Checklist> items = checklistManager.getByTaskId(task.getId());
        ObservableList<String> checklistData = FXCollections.observableArrayList();

        for (Checklist item : items) {
            String status = item.isDone() ? "✓" : "○";
            checklistData.add(status + " " + item.getText());
        }

        checklistView.setItems(checklistData);
    }

    // === КНОПКА REFRESH (обязательное требование!) ===
    @FXML
    private void handleRefresh() {
        refreshData();
        statusLabel.setText("Данные обновлены: " + java.time.LocalTime.now());
    }

    // === ОБНОВЛЕНИЕ ДАННЫХ ===
    private void refreshData() {
        taskList.clear();
        taskList.addAll(taskManager.getAll());
        taskTable.refresh();

        // Если задача выбрана, обновляем детали
        if (selectedTask != null) {
            Task refreshed = taskManager.getById(selectedTask.getId());
            if (refreshed != null) {
                showTaskDetails(refreshed);
            }
        }
    }

    // === ДОБАВИТЬ ЗАДАЧУ ===
    @FXML
    private void handleAddTask() {
        Dialog<Task> dialog = createAddTaskDialog();

        dialog.showAndWait().ifPresent(task -> {
            try {
                taskManager.addTask(task);
                refreshData();
                statusLabel.setText("Задача добавлена: " + task.getId());
            } catch (Exception e) {
                showError("Ошибка добавления задачи", e.getMessage());
            }
        });
    }

    // === ДИАЛОГ ДОБАВЛЕНИЯ ===
    private Dialog<Task> createAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Добавить задачу");
        dialog.setHeaderText("Создание новой задачи");

        TextField textField = new TextField();
        textField.setPromptText("Текст задачи (до 25 символов)");

        ChoiceBox<TaskPriority> priorityBox = new ChoiceBox<>();
        priorityBox.getItems().addAll(TaskPriority.values());
        priorityBox.setValue(TaskPriority.MEDIUM);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("YYYY-MM-DD");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Текст:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("Приоритет:"), 0, 1);
        grid.add(priorityBox, 1, 1);
        grid.add(new Label("Дедлайн:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                long id = taskManager.getTaskNextId();
                return new Task(
                        id,
                        textField.getText(),
                        priorityBox.getValue(),
                        TaskStatus.NEW,
                        datePicker.getValue() != null
                                ? datePicker.getValue().atStartOfDay(ZoneOffset.UTC).toInstant()
                                : null,
                        null,
                        "SYSTEM",
                        Instant.now(),
                        Instant.now()
                );
            }
            return null;
        });

        return dialog;
    }

    // === СОХРАНИТЬ ===
    @FXML
    private void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("data.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
        if (file != null) {
            try {
                storage.save(file.getAbsolutePath(), taskManager, checklistManager);
                statusLabel.setText("Сохранено: " + file.getName());
            } catch (IOException e) {
                showError("Ошибка сохранения", e.getMessage());
            }
        }
    }

    // === ЗАГРУЗИТЬ ===
    @FXML
    private void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File file = fileChooser.showOpenDialog(taskTable.getScene().getWindow());
        if (file != null) {
            boolean success = storage.load(file.getAbsolutePath(), taskManager, checklistManager);
            if (success) {
                refreshData();
                statusLabel.setText("Загружено: " + file.getName());
            } else {
                showError("Ошибка загрузки", "Не удалось загрузить файл. Данные не изменены.");
            }
        }
    }

    // === ПЕРЕКЛЮЧИТЬ ЧЕКЛИСТ ===
    @FXML
    private void handleToggleChecklist() {
        if (selectedTask == null) {
            showError("Выбор задачи", "Сначала выберите задачу в списке");
            return;
        }

        List<Checklist> items = checklistManager.getByTaskId(selectedTask.getId());
        if (items.isEmpty()) {
            showError("Чеклист пуст", "Добавьте пункты чеклиста сначала");
            return;
        }

        ChoiceDialog<Checklist> dialog = new ChoiceDialog<>(items.get(0), items);
        dialog.setTitle("Переключить пункт");
        dialog.setHeaderText("Выберите пункт чеклиста:");

        Optional<Checklist> result = dialog.showAndWait();
        result.ifPresent(item -> {
            item.setDone(!item.isDone());
            showTaskDetails(selectedTask);
            statusLabel.setText("Пункт обновлён");
        });
    }

    // === ДОБАВИТЬ ПУНКТ ЧЕКЛИСТА ===
    @FXML
    private void handleAddChecklist() {
        if (selectedTask == null) {
            showError("Выбор задачи", "Сначала выберите задачу в списке");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить пункт чеклиста");
        dialog.setHeaderText("Текст пункта (до 256 символов):");
        dialog.setContentText("Текст:");

        dialog.showAndWait().ifPresent(text -> {
            try {
                long id = checklistManager.getChecklistNextId();
                Checklist item = new Checklist(id, selectedTask.getId(), text);
                checklistManager.addChecklist(item);
                showTaskDetails(selectedTask);
                statusLabel.setText("Пункт добавлен");
            } catch (Exception e) {
                showError("Ошибка", e.getMessage());
            }
        });
    }

    // === РЕДАКТИРОВАТЬ ЗАДАЧУ ===
    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showError("Выбор задачи", "Сначала выберите задачу в списке");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Редактировать задачу");
        dialog.setHeaderText("Новый текст задачи:");

        TextField textField = new TextField(selectedTask.getText());
        dialog.getDialogPane().setContent(textField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return textField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newText -> {
            try {
                selectedTask.setText(newText);
                refreshData();
                statusLabel.setText("Задача обновлена");
            } catch (Exception e) {
                showError("Ошибка", e.getMessage());
            }
        });
    }

    // === УДАЛИТЬ ЗАДАЧУ ===
    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showError("Выбор задачи", "Сначала выберите задачу в списке");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Удаление задачи");
        alert.setHeaderText("Вы уверены?");
        alert.setContentText("Задача #" + selectedTask.getId() +
                " будет удалена безвозвратно вместе с чеклистом.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskManager.remove(selectedTask.getId());
            refreshData();
            clearDetails();
            statusLabel.setText("Задача удалена");
        }
    }

    // === ОЧИСТКА ДЕТАЛЕЙ ===
    private void clearDetails() {
        detailId.setText("—");
        detailText.setText("—");
        detailStatus.setText("—");
        detailPriority.setText("—");
        detailDeadline.setText("—");
        detailAssignee.setText("—");
        detailOwner.setText("—");
        checklistView.setItems(FXCollections.observableArrayList());
        selectedTask = null;
    }

    // === ПОКАЗАТЬ ОШИБКУ (Alert) ===
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
