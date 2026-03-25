package ru.itmo.seals.storage;

import ru.itmo.seals.model.*;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private final JsonMapper jsonMapper;
    private final FileValidator validator;

    // Временное хранилище для атомарной загрузки
    private List<Task> pendingTasks;
    private List<Checklist> pendingChecklists;

    public FileStorage() {
        this.jsonMapper = new JsonMapper();
        this.validator = new FileValidator();
    }

    public void save(String path, TaskCollectionManager taskManager,
                     ChecklistCollectionManager checklistManager) throws IOException {

        DataDto data = new DataDto();
        data.tasks = new ArrayList<>();
        data.checklists = new ArrayList<>();

        // Конвертируем Task → TaskDto
        for (Task task : taskManager.getAll()) {
            TaskDto dto = new TaskDto();
            dto.id = task.getId();
            dto.text = task.getText();
            dto.priority = task.getPriority().name();
            dto.status = task.getStatus().name();
            dto.deadlineAt = task.getDeadlineAt() != null ? task.getDeadlineAt().toString() : null;
            dto.assigneeUsername = task.getAssigneeUsername();
            dto.ownerUsername = task.getOwnerUsername();
            dto.createdAt = task.getCreatedAt().toString();
            dto.updatedAt = task.getUpdatedAt().toString();
            data.tasks.add(dto);
        }

        // Конвертируем Checklist → ChecklistDto
        for (Checklist checklist : checklistManager.getAll()) {
            ChecklistDto dto = new ChecklistDto();
            dto.id = checklist.getId();
            dto.taskId = checklist.getTaskId();
            dto.text = checklist.getText();
            dto.done = checklist.isDone();
            dto.createdAt = checklist.getCreatedAt().toString();
            dto.updatedAt = checklist.getUpdatedAt().toString();
            data.checklists.add(dto);
        }

        jsonMapper.save(data, path);
    }

    public boolean load(String path, TaskCollectionManager taskManager,
                        ChecklistCollectionManager checklistManager) {

        try {
            // 1. Читаем файл
            DataDto data = jsonMapper.load(path);

            // 2. Валидируем
            FileValidator.ValidationResult validation = validator.validate(data);
            if (!validation.isValid) {
                System.out.println("Ошибка валидации файла:");
                for (String error : validation.errors) {
                    System.out.println("  - " + error);
                }
                return false;
            }

            // 3. Сохраняем во временное хранилище
            pendingTasks = new ArrayList<>();
            pendingChecklists = new ArrayList<>();

            for (TaskDto dto : data.tasks) {
                Task task = dtoToTask(dto);
                pendingTasks.add(task);
            }

            for (ChecklistDto dto : data.checklists) {
                Checklist checklist = dtoToChecklist(dto);
                pendingChecklists.add(checklist);
            }

            // 4. Очищаем текущие коллекции и загружаем новые
            // (здесь нужно добавить методы clear() в менеджеры)
            taskManager.clear();
            checklistManager.clear();

            for (Task task : pendingTasks) {
                taskManager.addTask(task);
            }

            for (Checklist checklist : pendingChecklists) {
                checklistManager.addChecklist(checklist);
            }

            pendingTasks = null;
            pendingChecklists = null;

            System.out.println("Загружено задач: " + data.tasks.size());
            System.out.println("Загружено чеклистов: " + data.checklists.size());
            return true;

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
            return false;
        }
    }

    private Task dtoToTask(TaskDto dto) {
        return new Task(
                dto.id,
                dto.text,
                TaskPriority.valueOf(dto.priority),
                TaskStatus.valueOf(dto.status),
                dto.deadlineAt != null ? Instant.parse(dto.deadlineAt) : null,
                dto.assigneeUsername,
                dto.ownerUsername,
                Instant.parse(dto.createdAt),
                Instant.parse(dto.updatedAt)
        );
    }

    private Checklist dtoToChecklist(ChecklistDto dto) {
        Checklist checklist = new Checklist(dto.id, dto.taskId, dto.text);
        checklist.setDone(dto.done);
        // Нужно добавить сеттеры для createdAt/updatedAt или изменить конструктор
        return checklist;
    }
}
