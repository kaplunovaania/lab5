package ru.itmo.seals.storage;

import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;
import java.util.*;

public class FileValidator {

    public static class ValidationResult {
        public boolean isValid;
        public List<String> errors = new ArrayList<>();

        public ValidationResult(boolean isValid) {
            this.isValid = isValid;
        }

        public void addError(String error) {
            errors.add(error);
            this.isValid = false;
        }
    }

    public ValidationResult validate(DataDto data) {
        ValidationResult result = new ValidationResult(true);

        if (data == null) {
            result.addError("Файл пуст или имеет неверный формат");
            return result;
        }

        if (data.tasks == null) data.tasks = new ArrayList<>();
        if (data.checklists == null) data.checklists = new ArrayList<>();

        Set<Long> taskIds = new HashSet<>();
        for (TaskDto task : data.tasks) {
            if (task == null) {
                result.addError("Найдена null задача");
                continue;
            }
            if (!taskIds.add(task.id)) {
                result.addError("Дубликат ID задачи: " + task.id);
            }
            validateTask(task, result);
        }

        Set<Long> checklistIds = new HashSet<>();
        for (ChecklistDto checklist : data.checklists) {
            if (checklist == null) {
                result.addError("Найден null чеклист");
                continue;
            }
            if (!checklistIds.add(checklist.id)) {
                result.addError("Дубликат ID чеклиста: " + checklist.id);
            }
            validateChecklist(checklist, result, taskIds);
        }

        return result;
    }

    private void validateTask(TaskDto task, ValidationResult result) {
        if (task.text == null || task.text.isEmpty()) {
            result.addError("Задача id=" + task.id + ": пустое поле text");
        }
        if (task.text != null && task.text.length() > 25) {
            result.addError("Задача id=" + task.id + ": text длиннее 25 символов");
        }
        try {
            TaskPriority.valueOf(task.priority);
        } catch (Exception e) {
            result.addError("Задача id=" + task.id + ": неверный priority=" + task.priority);
        }
        try {
            TaskStatus.valueOf(task.status);
        } catch (Exception e) {
            result.addError("Задача id=" + task.id + ": неверный status=" + task.status);
        }
    }

    private void validateChecklist(ChecklistDto checklist, ValidationResult result, Set<Long> taskIds) {
        if (checklist.text == null || checklist.text.isEmpty()) {
            result.addError("Чеклист id=" + checklist.id + ": пустое поле text");
        }
        if (!taskIds.contains(checklist.taskId)) {
            result.addError("Чеклист id=" + checklist.id + ": ссылается на несуществующую задачу taskId=" + checklist.taskId);
        }
    }
}