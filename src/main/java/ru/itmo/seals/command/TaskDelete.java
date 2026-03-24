package ru.itmo.seals.command;

import ru.itmo.seals.service.TaskCollectionManager;
import java.util.Scanner;

public class TaskDelete extends Command {
    private final TaskCollectionManager taskManager;

    public TaskDelete(TaskCollectionManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 1) {
            System.out.println("Ошибка: укажите ID задачи");
            return;
        }
        try {
            long taskId = Long.parseLong(args[0]);
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