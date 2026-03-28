package ru.itmo.seals.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import ru.itmo.seals.storage.FileStorage;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    private TaskCollectionManager taskManager;
    private ChecklistCollectionManager checklistManager;
    private FileStorage storage;

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
                     FileStorage storage) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.storage = storage;

        setupTable();
        setupSelectionListener();
        refreshData();
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
        detailOwner.setText(task.getOwnerUsername() != null ? task.getOwnerUsername() : "-");

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
        refreshData();
    }

    private void refreshData() {
        taskList.clear();
        taskList.addAll(taskManager.getAll());
        filterTasks();

        taskCount.setText(String.valueOf(taskManager.getAll().size()));
        lastRefresh.setText("Last: " + java.time.LocalTime.now().toString().substring(0, 8));
        statusBarText.setText("Updated");
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

        List<Task> filtered = taskManager.getAll().stream()
                .filter(t -> t.getText().toLowerCase().contains(search))
                .filter(t -> "All".equals(status) || t.getStatus().name().equals(status))
                .toList();

        taskList.setAll(filtered);
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

                return new ru.itmo.seals.model.Task(
                        taskManager.getTaskNextId(),
                        text.getText().trim(),
                        priority.getValue(),
                        TaskStatus.NEW,
                        date.getValue() != null
                                ? date.getValue().atStartOfDay(ZoneOffset.UTC).toInstant()
                                : null,
                        null,
                        "SYSTEM",
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
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("data.json");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File f = fc.showSaveDialog(taskTable.getScene().getWindow());
        if (f != null) {
            try {
                storage.save(f.getAbsolutePath(), taskManager, checklistManager);
                statusBarText.setText("Saved: " + f.getName());
            } catch (IOException e) {
                showError("Save error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoad() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File f = fc.showOpenDialog(taskTable.getScene().getWindow());
        if (f != null) {
            progressBar.setVisible(true);
            progressBar.setManaged(true);
            progressLabel.setVisible(true);
            progressLabel.setManaged(true);
            progressLabel.setText("Loading " + f.getName() + "...");

            javafx.concurrent.Task<Void> loadTask = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() {
                    storage.load(f.getAbsolutePath(), taskManager, checklistManager);
                    return null;
                }
            };

            loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
                progressBar.setVisible(false);
                progressBar.setManaged(false);
                progressLabel.setVisible(false);
                progressLabel.setManaged(false);
                refreshData();
                statusBarText.setText("Loaded: " + f.getName());
            }));

            loadTask.setOnFailed(e -> Platform.runLater(() -> {
                progressBar.setVisible(false);
                progressBar.setManaged(false);
                progressLabel.setVisible(false);
                progressLabel.setManaged(false);
                showError("Load error", "Failed to load file");
            }));

            new Thread(loadTask).start();
        }
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

        selectedItem.setDone(!selectedItem.isDone());
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
                        checklistManager.getChecklistNextId(),
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete task");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Task #" + selectedTask.getId() +
                "\n\n" + selectedTask.getText() +
                "\n\nThis cannot be undone!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskManager.remove(selectedTask.getId());
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
}