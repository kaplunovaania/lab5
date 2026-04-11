package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskStatus;
import ru.itmo.seals.service.TaskCollectionManager;
import java.util.Scanner;
import ru.itmo.seals.service.UserService;

public class TaskDone extends Command {
    private final TaskCollectionManager taskManager;
    private final UserService userService;

    public TaskDone(TaskCollectionManager taskManager, UserService userService) {
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
            Task task = taskManager.getById(taskId);
            if (task == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }
            if (task.getOwnerId() != userService.getCurrentUserId()) {
                System.out.println("Ошибка: у вас нет прав на изменение этого объекта");
                return;
            }
            task.setStatus(TaskStatus.DONE);
            System.out.println("OK task " + taskId + " DONE");
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
