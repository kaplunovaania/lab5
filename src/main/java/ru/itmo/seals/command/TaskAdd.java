package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;
import ru.itmo.seals.service.TaskCollectionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Scanner;

public class TaskAdd extends Command {
    private final TaskCollectionManager taskManager;
    public TaskAdd(TaskCollectionManager taskManager) {
        super();
        this.taskManager = taskManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        String ownerUsername = "SYSTEM";

        if (args.length == 0) {
            runInteractiveMode(scanner, ownerUsername);
        } else {
            runArgsMode(args, ownerUsername);
        }
    }

    private void runInteractiveMode(Scanner scanner, String ownerUsername) {
        System.out.println("Создание новой задачи");

        String text = null;
        while (text == null || text.isEmpty()) {
            System.out.print("Текст задачи: ");
            text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("Ошибка: текст не может быть пустым.");
            }
        }

        TaskPriority priority = null;
        while (priority == null) {
            System.out.print("Приоритет (LOW|MEDIUM|HIGH): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                priority = TaskPriority.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неверный приоритет. Доступны: LOW, MEDIUM, HIGH");
            }
        }

        Instant deadline = null;
        boolean deadlineSet = false;
        while (!deadlineSet) {
            System.out.print("Дедлайн (YYYY-MM-DD, оставьте пустым если нет): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                deadlineSet = true;
            } else {
                try {
                    LocalDate date = LocalDate.parse(input);
                    deadline = date.atStartOfDay(ZoneOffset.UTC).toInstant();
                    deadlineSet = true;
                } catch (Exception e) {
                    System.out.println("Ошибка: неверный формат даты. Используйте YYYY-MM-DD");
                }
            }
        }

        createAndSaveTask(text, priority, deadline, ownerUsername);
    }

    private void runArgsMode(String[] args, String ownerUsername) {
        String priorityStr = "MEDIUM";
        String deadlineStr = "";
        int textEndIndex = args.length;
        if (args.length >= 1) {
            String last = args[args.length - 1];
            if (last.matches("\\d{4}-\\d{2}-\\d{2}")) {
                deadlineStr = last;
                textEndIndex--;
            }
        }

        for (int i = textEndIndex - 1; i >= 0; i--) {
            String p = args[i].toUpperCase();
            if (p.equals("LOW") || p.equals("MEDIUM") || p.equals("HIGH")) {
                priorityStr = p;
                textEndIndex = i;
                break;
            }
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < textEndIndex; i++) {
            if (i > 0) textBuilder.append(" ");
            textBuilder.append(args[i]);
        }
        String text = textBuilder.toString();

        if (text.isEmpty()) {
            System.out.println("Ошибка: текст задачи не может быть пустым.");
            return;
        }

        TaskPriority priority;
        try {
            priority = TaskPriority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: неверный приоритет '" + priorityStr + "'");
            return;
        }

        Instant deadline = null;
        if (!deadlineStr.isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(deadlineStr);
                deadline = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (Exception e) {
                System.out.println("Ошибка: неверный формат даты '" + deadlineStr + "'");
                return;
            }
        }

        createAndSaveTask(text, priority, deadline, ownerUsername);
    }

    private void createAndSaveTask(String text, TaskPriority priority, Instant deadline, String ownerUsername) {
        try {
            long id = taskManager.getTaskNextId();
            Task task = new Task(
                    id,
                    text,
                    priority,
                    TaskStatus.NEW,
                    deadline,
                    null, // assignee
                    ownerUsername,
                    Instant.now(),
                    Instant.now()
            );
            taskManager.addTask(task);
            System.out.println("OK task_id=" + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Неизвестная ошибка: " + e.getMessage());
        }
    }
}
