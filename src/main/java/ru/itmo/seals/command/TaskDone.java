package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskStatus;
import ru.itmo.seals.service.TaskCollectionManager;
import java.util.Scanner;

public class TaskDone extends Command {
    private final TaskCollectionManager taskManager;

    public TaskDone(TaskCollectionManager taskManager) {
        super();
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
            Task task = taskManager.getById(taskId);
            if (task == null) {
                System.out.println("Ошибка: задача не найдена");
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
