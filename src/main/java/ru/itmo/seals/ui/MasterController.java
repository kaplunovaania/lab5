package ru.itmo.seals.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.service.UserService;
import ru.itmo.seals.storage.DatabaseStorage;
import ru.itmo.seals.storage.FileStorage;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;      // ← ← ← ДОБАВЬ ЭТО!
import java.util.List;
import java.util.Optional;

public class MasterController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Long> idColumn;
    @FXML private TableColumn<Task, String> textColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> deadlineColumn;
    @FXML private TableColumn<Task, String> assigneeColumn;
    @FXML private TableColumn<Task, String> ownerColumn;

    @FXML private Label detailId;
    @FXML private Label detailText;
    @FXML private Label detailStatus;
    @FXML private Label detailPriority;
    @FXML private Label detailDeadline;
    @FXML private Label detailAssignee;
    @FXML private Label detailOwner;
    @FXML private Label detailCreated;
    @FXML private Label detailUpdated;

    @FXML private ListView<Checklist> checklistView;

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> filterStatus;

    @FXML private Label statusBarText;
    @FXML private Label taskCount;
    @FXML private Label lastRefresh;
    @FXML private Label currentUserLabel;

    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private TaskCollectionManager taskManager;
    private ChecklistCollectionManager checklistManager;
    private DatabaseStorage storage;
    private UserService userService;

    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private Task selectedTask;

    @FXML
    private void initialize() {
        if (filterStatus != null) {
            filterStatus.getItems().add("All");
            filterStatus.getItems().add("NEW");
            filterStatus.getItems().add("IN_PROGRESS");
            filterStatus.getItems().add("DONE");
            filterStatus.setValue("All");
            filterStatus.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> filterTasks());
        }
    }

    public void init(TaskCollectionManager taskManager,
                     ChecklistCollectionManager checklistManager,
                     DatabaseStorage storage,
                     UserService userService) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.storage = storage;
        this.userService = userService;

        setupTable();
        setupSelectionListener();
        refreshData();
        updateCurrentUserLabel();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        textColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (!empty && item != null && item.length() > 25) {
                    setTooltip(new Tooltip(item));
                }
            }
        });

        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        deadlineColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task.getDeadlineAt() != null) {
                LocalDate date = LocalDate.ofInstant(task.getDeadlineAt(), ZoneOffset.UTC);
                return new javafx.beans.property.SimpleStringProperty(date.toString());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        assigneeColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            String assignee = task.getAssigneeUsername();
            return new javafx.beans.property.SimpleStringProperty(assignee != null ? assignee : "-");
        });

        ownerColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("ID:" + task.getOwnerId());
        });

        taskTable.setItems(taskList);
    }

    private void setupSelectionListener() {
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                selectedTask = sel;
                showTaskDetails(sel);
            }
        });
    }

    private void showTaskDetails(Task task) {
        detailId.setText(String.valueOf(task.getId()));
        detailText.setText(task.getText());
        detailStatus.setText(task.getStatus().name());
        detailPriority.setText(task.getPriority().name());

        if (task.getDeadlineAt() != null) {
            detailDeadline.setText(LocalDate.ofInstant(task.getDeadlineAt(), ZoneOffset.UTC).toString());
        } else {
            detailDeadline.setText("-");
        }

        detailAssignee.setText(task.getAssigneeUsername() != null ? task.getAssigneeUsername() : "-");
        detailOwner.setText("ID: " + task.getOwnerId());

        if (task.getCreatedAt() != null) {
            detailCreated.setText(task.getCreatedAt().atZone(ZoneOffset.UTC).format(DATE_FORMAT));
        } else {
            detailCreated.setText("-");
        }

        if (task.getUpdatedAt() != null) {
            detailUpdated.setText(task.getUpdatedAt().atZone(ZoneOffset.UTC).format(DATE_FORMAT));
        } else {
            detailUpdated.setText("-");
        }

        loadChecklist(task);

        updateActionButtons();
    }

    private void loadChecklist(Task task) {
        List<Checklist> items = checklistManager.getByTaskId(task.getId());
        ObservableList<Checklist> data = FXCollections.observableArrayList(items);
        checklistView.setItems(data);

        checklistView.setCellFactory(listView -> new ListCell<Checklist>() {
            @Override
            protected void updateItem(Checklist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String status = item.isDone() ? "[x]" : "[ ]";
                    setText(status + "  " + item.getText());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println("[Refresh] Starting full refresh...");

        // Перезагрузка задач из БД
        taskManager.loadFromDatabase();

        // ← ← ← ДОБАВЬ ЭТО: Перезагрузка чеклистов из БД
        checklistManager.loadFromDatabase();

        // Обновление таблицы задач
        refreshData();

        // ← ← ← ДОБАВЬ ЭТО: Обновление чеклиста для выбранной задачи
        if (selectedTask != null) {
            loadChecklist(selectedTask);  // Перечитать чеклист из менеджера
            showTaskDetails(selectedTask);  // Обновить детали
        }

        statusBarText.setText("Reloaded from database");
        System.out.println("[Refresh] Full refresh completed");
    }

    private void refreshData() {
        // Получаем текущего пользователя
        long myUserId = userService.getCurrentUserId();
        String myLogin = userService.getCurrentUserLogin();

        System.out.println("[Filter] Current user: " + myLogin + " (id=" + myUserId + ")");

        // 1. Очищаем таблицу
        taskList.clear();
        System.out.println("[Filter] taskList cleared");

        // 2. Получаем все задачи из памяти
        List<Task> allTasks = taskManager.getAll();
        System.out.println("[Filter] Total tasks in memory: " + allTasks.size());

        // 3. Фильтруем
        List<Task> visibleTasks = new ArrayList<>();
        for (Task task : allTasks) {
            boolean isOwner = (task.getOwnerId() == myUserId);
            boolean isAssignee = (task.getAssigneeUsername() != null &&
                    task.getAssigneeUsername().equals(myLogin));

            if (isOwner || isAssignee) {
                visibleTasks.add(task);
            }
        }

        System.out.println("[Filter] Visible tasks: " + visibleTasks.size());

        // 4. ← ← ← ВАЖНО: Добавляем в таблицу
        taskList.addAll(visibleTasks);
        System.out.println("[Filter] taskList updated with " + visibleTasks.size() + " tasks");

        // 5. Применяем фильтры (поиск, статус)
        filterTasks();

        // 6. Обновляем счётчики
        taskCount.setText(String.valueOf(visibleTasks.size()));
        lastRefresh.setText("Last: " + java.time.LocalTime.now().toString().substring(0, 8));
        statusBarText.setText("Showing " + visibleTasks.size() + " tasks");

        System.out.println("[Filter] refreshData() completed");
    }

    @FXML
    private void handleSearch(javafx.scene.input.KeyEvent event) {
        filterTasks();
    }

    private void filterTasks() {
        String search = searchField != null && searchField.getText() != null
                ? searchField.getText().toLowerCase() : "";
        String status = filterStatus != null && filterStatus.getValue() != null
                ? filterStatus.getValue() : "All";

        // ← ← ← ВАЖНО: Фильтруем ТОЛЬКО то что уже в taskList (не все задачи!)
        List<Task> filtered = taskList.stream()  // ← ← ← taskList, НЕ taskManager.getAll()!
                .filter(t -> t.getText().toLowerCase().contains(search))
                .filter(t -> "All".equals(status) || t.getStatus().name().equals(status))
                .toList();

        taskList.setAll(filtered);

        System.out.println("[FilterTasks] After search/status filter: " + filtered.size() + " tasks");
    }

    @FXML
    private void handleAddTask() {
        Dialog<ru.itmo.seals.model.Task> dialog = new Dialog<>();
        dialog.setTitle("Add Task");
        dialog.setHeaderText("Create new task");

        TextField text = new TextField();
        text.setPromptText("Task text (max 25 chars)");

        ChoiceBox<TaskPriority> priority = new ChoiceBox<>();
        priority.getItems().addAll(TaskPriority.values());
        priority.setValue(TaskPriority.MEDIUM);

        DatePicker date = new DatePicker();

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Text:"), 0, 0);
        grid.add(text, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priority, 1, 1);
        grid.add(new Label("Deadline:"), 0, 2);
        grid.add(date, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (text.getText() == null || text.getText().trim().isEmpty()) {
                    showError("Validation error", "Task text cannot be empty");
                    return null;
                }
                if (text.getText().length() > 25) {
                    showError("Validation error", "Task text must be max 25 characters");
                    return null;
                }

                if (!userService.isLoggedIn()) {
                    showError("Not logged in", "Please login first");
                    return null;
                }

                return new ru.itmo.seals.model.Task(
                        0,
                        text.getText().trim(),
                        priority.getValue(),
                        TaskStatus.NEW,
                        date.getValue() != null
                                ? date.getValue().atStartOfDay(ZoneOffset.UTC).toInstant()
                                : null,
                        null,
                        userService.getCurrentUserId(),
                        Instant.now(),
                        Instant.now()
                );
            }
            return null;
        });

        Optional<ru.itmo.seals.model.Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            if (task != null) {
                try {
                    taskManager.addTask(task);
                    refreshData();
                    statusBarText.setText("Task added: " + task.getId());
                } catch (Exception e) {
                    showError("Error adding task", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showError("Select task", "Please select a task first");
            return;
        }

        if (!userService.hasTaskAccess(selectedTask)) {
            showError("Access denied", "You don't have permission to edit this task");
            return;
        }

        Dialog<ru.itmo.seals.model.Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Update task #" + selectedTask.getId());

        TextField text = new TextField(selectedTask.getText());
        text.setPromptText("Task text (max 25 chars)");

        ChoiceBox<TaskPriority> priority = new ChoiceBox<>();
        priority.getItems().addAll(TaskPriority.values());
        priority.setValue(selectedTask.getPriority());

        ChoiceBox<TaskStatus> status = new ChoiceBox<>();
        status.getItems().addAll(TaskStatus.values());
        status.setValue(selectedTask.getStatus());

        DatePicker date = new DatePicker();
        if (selectedTask.getDeadlineAt() != null) {
            date.setValue(LocalDate.ofInstant(selectedTask.getDeadlineAt(), ZoneOffset.UTC));
        }

        TextField assignee = new TextField(selectedTask.getAssigneeUsername() != null
                ? selectedTask.getAssigneeUsername() : "");
        assignee.setPromptText("Username (optional)");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Text:"), 0, 0);
        grid.add(text, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priority, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(status, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(date, 1, 3);
        grid.add(new Label("Assignee:"), 0, 4);
        grid.add(assignee, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (text.getText() == null || text.getText().trim().isEmpty()) {
                    showError("Validation error", "Task text cannot be empty");
                    return null;
                }
                if (text.getText().length() > 25) {
                    showError("Validation error", "Task text must be max 25 characters");
                    return null;
                }

                selectedTask.setText(text.getText().trim());
                selectedTask.setPriority(priority.getValue());
                selectedTask.setStatus(status.getValue());
                selectedTask.setAssigneeUsername(assignee.getText().trim().isEmpty()
                        ? null : assignee.getText().trim());

                if (date.getValue() != null) {
                    selectedTask.setDeadlineAt(date.getValue().atStartOfDay(ZoneOffset.UTC).toInstant());
                } else {
                    selectedTask.setDeadlineAt(null);
                }

                return selectedTask;
            }
            return null;
        });

        Optional<ru.itmo.seals.model.Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            if (task != null) {
                try {
                    taskManager.updateTask(task, userService.getCurrentUserId());
                    refreshData();
                    showTaskDetails(selectedTask);
                    statusBarText.setText("Task updated");
                } catch (Exception e) {
                    showError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Save");
        alert.setHeaderText(null);
        alert.setContentText("All changes are automatically saved to database");
        alert.showAndWait();

        statusBarText.setText("Data auto-saved to database");
    }

    @FXML
    private void handleLoad() {
        statusBarText.setText("Data loaded from database at startup");
        refreshData();

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Load");
        alert.setHeaderText(null);
        alert.setContentText("Data was loaded from database when application started");
        alert.showAndWait();
    }

    @FXML
    private void handleToggleChecklist() {
        if (selectedTask == null) {
            showError("Select task", "Please select a task first");
            return;
        }

        Checklist selectedItem = checklistView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("Select item", "Please select a checklist item first");
            return;
        }

        // Переключить статус
        selectedItem.setDone(!selectedItem.isDone());

        // ← ← ← ДОБАВЬ ЭТО: Сохранить в БД
        checklistManager.updateChecklist(selectedItem);

        // Обновить отображение
        loadChecklist(selectedTask);
        statusBarText.setText("Item updated");
    }

    @FXML
    private void handleAddChecklist() {
        if (selectedTask == null) {
            showError("Select task", "Please select a task first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Checklist Item");
        dialog.setHeaderText("Item text (max 256 chars):");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            try {
                if (text == null || text.trim().isEmpty()) {
                    showError("Validation error", "Item text cannot be empty");
                    return;
                }
                if (text.length() > 256) {
                    showError("Validation error", "Item text must be max 256 characters");
                    return;
                }

                Checklist item = new Checklist(
                        0,
                        selectedTask.getId(),
                        text.trim()
                );
                checklistManager.addChecklist(item);
                loadChecklist(selectedTask);
                statusBarText.setText("Item added");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showError("Select task", "Please select a task first");
            return;
        }

        if (!userService.canDeleteTask(selectedTask)) {
            showError("Access denied", "Only the owner can delete this task");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete task");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Task #" + selectedTask.getId() +
                "\n\n" + selectedTask.getText() +
                "\n\nThis cannot be undone!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskManager.remove(selectedTask.getId(), userService.getCurrentUserId());
            refreshData();
            clearDetails();
            statusBarText.setText("Task deleted");
        }
    }

    private void clearDetails() {
        detailId.setText("-");
        detailText.setText("-");
        detailStatus.setText("-");
        detailPriority.setText("-");
        detailDeadline.setText("-");
        detailAssignee.setText("-");
        detailOwner.setText("-");
        detailCreated.setText("-");
        detailUpdated.setText("-");
        checklistView.setItems(FXCollections.observableArrayList());
        selectedTask = null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleLogin() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Enter your credentials");  // ← ВАЖНО: заголовок!
        dialog.initOwner(taskTable.getScene().getWindow());  // ← ВАЖНО: привязка к окну

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        TextField loginField = new TextField();
        loginField.setPromptText("Login");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        loginField.textProperty().addListener((obs, old, newVal) ->
                loginButton.setDisable(newVal.trim().isEmpty() || passwordField.getText().trim().isEmpty()));
        passwordField.textProperty().addListener((obs, old, newVal) ->
                loginButton.setDisable(newVal.trim().isEmpty() || loginField.getText().trim().isEmpty()));

        dialog.setResultConverter(param -> {
            if (param == loginButtonType) {
                return userService.login(loginField.getText(), passwordField.getText());
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(success -> {
            if (success) {
                updateCurrentUserLabel();
                statusBarText.setText("Logged in successfully");
                updateActionButtons();
            } else {
                statusBarText.setText("Login failed");
                showError("Login Error", "Invalid login or password");
            }
        });
    }

    private void updateCurrentUserLabel() {
        if (currentUserLabel == null || userService == null) {
            return;
        }

        if (userService.isLoggedIn()) {
            currentUserLabel.setText(userService.getCurrentUserLogin());
        } else {
            currentUserLabel.setText("Not logged in");
        }
    }

    @FXML
    private void handleRegister() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Create new account");
        dialog.initOwner(taskTable.getScene().getWindow());

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        TextField loginField = new TextField();
        loginField.setPromptText("Login (min 4 chars)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 4 chars)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.setDisable(true);

        loginField.textProperty().addListener((obs, old, newVal) ->
                registerButton.setDisable(newVal.trim().length() < 4 || passwordField.getText().trim().length() < 4));
        passwordField.textProperty().addListener((obs, old, newVal) ->
                registerButton.setDisable(newVal.trim().length() < 4 || loginField.getText().trim().length() < 4));

        dialog.setResultConverter(param -> {
            if (param == registerButtonType) {
                return userService.register(loginField.getText(), passwordField.getText());
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(success -> {
            if (success) {
                statusBarText.setText("Registration successful");
            } else {
                statusBarText.setText("Registration failed");
            }
        });
    }

    @FXML
    private void handleLogout() {
        if (userService != null) {
            userService.logout();
            updateCurrentUserLabel();
            updateActionButtons();
            statusBarText.setText("Logged out");
        }
    }

    private void updateActionButtons() {
        if (selectedTask == null || userService == null) {
            if (editButton != null) editButton.setDisable(true);
            if (deleteButton != null) deleteButton.setDisable(true);
            return;
        }

        // Проверяем права
        boolean hasAccess = userService.hasTaskAccess(selectedTask);
        boolean canDelete = userService.canDeleteTask(selectedTask);

        // Edit: владелец ИЛИ назначенный
        if (editButton != null) {
            editButton.setDisable(!hasAccess);
        }

        // Delete: только владелец
        if (deleteButton != null) {
            deleteButton.setDisable(!canDelete);
        }
    }
}