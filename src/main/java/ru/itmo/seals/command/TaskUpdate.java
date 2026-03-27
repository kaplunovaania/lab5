package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;
import ru.itmo.seals.service.TaskCollectionManager;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Scanner;

public class TaskUpdate extends Command {
    private final TaskCollectionManager taskManager;

    public TaskUpdate(TaskCollectionManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 2) {
            System.out.println("Ошибка: формат task_update <id> <поле (priority, text, deadline, status, assignee)>=<значение>");
            return;
        }

        try {
            long taskId = Long.parseLong(args[0]);
            Task task = taskManager.getById(taskId);

            if (task == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }

            String[] parts = args[1].split("=", 2);
            if (parts.length != 2) {
                System.out.println("Ошибка: неверный формат поля. Используйте field=value");
                return;
            }

            String field = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            switch (field) {
                case "text" -> task.setText(value);
                case "priority" -> task.setPriority(TaskPriority.valueOf(value.toUpperCase()));
                case "status" -> task.setStatus(TaskStatus.valueOf(value.toUpperCase()));
                case "deadline" -> {
                    if (value.isEmpty()) {
                        task.setDeadlineAt(null);
                    } else {
                        LocalDate date = LocalDate.parse(value);
                        task.setDeadlineAt(date.atStartOfDay(ZoneOffset.UTC).toInstant());
                    }
                }
                case "assignee" -> task.setAssigneeUsername(value);
                default -> {
                    System.out.println("Ошибка: неизвестное поле '" + field + "'");
                    return;
                }
            }
            System.out.println("OK");
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка обновления: " + e.getMessage());
        }
    }
}
