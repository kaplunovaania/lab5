package ru.itmo.seals.command;

import ru.itmo.seals.service.TaskCollectionManager;
import java.util.Scanner;

public class TaskAssign extends Command {
    private final TaskCollectionManager taskManager;

    public TaskAssign(TaskCollectionManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 2) {
            System.out.println("Ошибка: формат task_assign <id> <username>");
            return;
        }
        try {
            long taskId = Long.parseLong(args[0]);
            String username = args[1];

            if (username.isEmpty()) {
                System.out.println("Ошибка: имя пользователя не может быть пустым");
                return;
            }

            var task = taskManager.getById(taskId);
            if (task == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }

            task.setAssigneeUsername(username);
            System.out.println("OK assigned");
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}