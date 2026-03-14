package ru.itmo.seals.service;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Scanner;

public class Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;

    public Command(TaskCollectionManager taskManager,
                          ChecklistCollectionManager checklistManager) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
    }


    public void taskAdd(String text, String priorityStr, String deadlineStr, String ownerUsername) {
        try {
            TaskPriority priority = TaskPriority.valueOf(priorityStr.toUpperCase());
            Instant deadline = parseDeadline(deadlineStr);

            long id = taskManager.getTaskNextId();
            Task task = new Task(
                    id, text, priority, TaskStatus.NEW, deadline,
                    null, ownerUsername != null ? ownerUsername : "SYSTEM",
                    Instant.now(), Instant.now()
            );
            taskManager.addTask(task);

            System.out.println("OK task_id=" + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public void taskList() {
        List<Task> tasks = taskManager.getAll();
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
            return;
        }
        System.out.println("ID Priority  Status      Deadline     Assignee    Text");
        for (Task task : tasks) {
            String deadlineStr = formatDeadline(task.getDeadlineAt());
            String assignee = task.getAssigneeUsername() != null ? task.getAssigneeUsername() : "-";
            System.out.printf("%-2d %-9s %-11s %-12s %-11s %s%n",
                    task.getId(), task.getPriority(), task.getStatus(), deadlineStr, assignee, task.getText());
        }
    }

    public void taskShow(long taskId) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        List<Checklist> items = checklistManager.getByTaskId(taskId);

        System.out.println("Task #" + task.getId());
        System.out.println("status: " + task.getStatus());
        System.out.println("priority: " + task.getPriority());
        System.out.println("deadline: " + formatDeadline(task.getDeadlineAt()));
        System.out.println("checklist: " + items.size() + " items");
    }

    public void taskUpdate(long taskId, String field, String value) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
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
                    System.out.println("Ошибка: неизвестное поле '" + field + "'");
                    return;
                }
            }
            System.out.println("OK");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public void taskDone(long taskId) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        task.setStatus(TaskStatus.DONE);
        System.out.println("OK task " + taskId + " DONE");
    }

    public void taskAssign(long taskId, String username) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        if (username == null || username.isEmpty()) {
            System.out.println("Ошибка: пустой username");
            return;
        }
        task.setAssigneeUsername(username);
        System.out.println("OK assigned");
    }

    public void taskDelete(long taskId) {
        boolean removed = taskManager.remove(taskId);
        if (removed) {
            System.out.println("OK deleted");
        } else {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
        }
    }


    public void checkAdd(long taskId, String text) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        try {
            long id = checklistManager.getChecklistNextId();
            Checklist item = new Checklist(id, taskId, text, false, Instant.now(), Instant.now());
            checklistManager.addChecklist(item);
            System.out.println("OK item_id=" + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public void checkList(long taskId) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        List<Checklist> items = checklistManager.getByTaskId(taskId);
        if (items.isEmpty()) {
            System.out.println("Чеклист пуст");
            return;
        }
        System.out.println("ID Done  Text");
        for (Checklist item : items) {
            System.out.printf("%-2d %-5s %s%n",
                    item.getId(), item.isDone() ? "YES" : "NO", item.getText());
        }
    }

    public void checkToggle(long itemId) {
        Checklist item = checklistManager.getById(itemId);
        if (item == null) {
            System.out.println("Ошибка: item с id=" + itemId + " не найден");
            return;
        }
        item.setDone(!item.isDone());
        System.out.println("OK item " + itemId + " DONE=" + (item.isDone() ? "YES" : "NO"));
    }


    private Instant parseDeadline(String deadlineStr) {
        if (deadlineStr == null || deadlineStr.isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(deadlineStr);
            return date.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный формат даты (ожидается YYYY-MM-DD)");
        }
    }

    private String formatDeadline(Instant deadline) {
        if (deadline == null) return "null";
        return LocalDate.ofInstant(deadline, ZoneOffset.UTC).toString();
    }
    public void taskAddInteractive(Scanner scanner, String ownerUsername) {
        System.out.println("Создание новой задачи:");

        String text = null;
        while (text == null || text.isEmpty()) {
            System.out.print("Текст задачи: ");
            text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("Ошибка: текст не может быть пустым");
            }
        }

        TaskPriority priority = null;
        while (priority == null) {
            System.out.print("Приоритет (LOW|MEDIUM|HIGH): ");
            String priorityStr = scanner.nextLine().trim().toUpperCase();
            try {
                priority = TaskPriority.valueOf(priorityStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неверный приоритет. Допустимые значения: LOW, MEDIUM, HIGH");
            }
        }

        Instant deadline = null;
        boolean deadlineSet = false;
        while (!deadlineSet) {
            System.out.print("Дедлайн (YYYY-MM-DD, оставьте пустым если нет): ");
            String deadlineStr = scanner.nextLine().trim();
            if (deadlineStr.isEmpty()) {
                deadline = null;
                deadlineSet = true;
            } else {
                try {
                    LocalDate date = LocalDate.parse(deadlineStr);
                    deadline = date.atStartOfDay(ZoneOffset.UTC).toInstant();
                    deadlineSet = true;
                } catch (Exception e) {
                    System.out.println("Ошибка: неверный формат даты. Используйте YYYY-MM-DD");
                }
            }
        }

        try {
            long id = taskManager.getTaskNextId();
            Task task = new Task(
                    id, text, priority, TaskStatus.NEW, deadline,
                    null, ownerUsername != null ? ownerUsername : "SYSTEM",
                    Instant.now(), Instant.now()
            );
            taskManager.addTask(task);
            System.out.println("OK task_id=" + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
