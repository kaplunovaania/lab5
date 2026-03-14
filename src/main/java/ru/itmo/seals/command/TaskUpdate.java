package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;

public class TaskUpdate {
    public void taskUpdate(long taskId, String field, String value) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.err.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        try {
            switch (field.toLowerCase()) {
                case "text" -> task.setText(value);
                case "priority" -> task.setPriority(TaskPriority.valueOf(value.toUpperCase()));
                case "status" -> task.setStatus(TaskStatus.valueOf(value.toUpperCase()));
                case "deadline" -> task.setDeadlineAt(parseDeadline(value));
                case "assignee" -> task.setAssigneeUsername(value);
                default -> {
                    System.err.println("Ошибка: неизвестное поле '" + field + "'");
                    return;
                }
            }
            System.out.println("OK");
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
