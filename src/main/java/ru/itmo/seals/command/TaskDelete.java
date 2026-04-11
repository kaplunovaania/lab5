package ru.itmo.seals.command;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.UserService;
import java.util.Scanner;

public class TaskDelete extends Command {
    private final TaskCollectionManager taskManager;
    private final UserService userService;

    public TaskDelete(TaskCollectionManager taskManager, UserService userService) {
        this.taskManager = taskManager;
        this.userService = userService;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (!userService.isLoggedIn()) {
            System.out.println("Ошибка: необходимо войти в систему");
            return;
        }

        if (args.length < 1) {
            System.out.println("Ошибка: укажите ID задачи");
            return;
        }
        try {
            long taskId = Long.parseLong(args[0]);

            var task = taskManager.getById(taskId);
            if (task == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }

            if (task.getOwnerId() != userService.getCurrentUserId()) {
                System.out.println("Ошибка: у вас нет прав на удаление этого объекта");
                return;
            }

            boolean removed = taskManager.remove(taskId);
            if (removed) {
                System.out.println("OK deleted");
            } else {
                System.out.println("Ошибка: задача не найдена");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}