package ru.itmo.seals.command;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import java.util.List;
import java.util.Scanner;

public class CheckList extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;

    public CheckList(TaskCollectionManager taskManager, ChecklistCollectionManager checklistManager) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 1) {
            System.out.println("Ошибка: укажите ID задачи");
            return;
        }
        try {
            long taskId = Long.parseLong(args[0]);
            if (taskManager.getById(taskId) == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }

            List<Checklist> items = checklistManager.getByTaskId(taskId);
            System.out.println("ID  Done  Text");
            for (Checklist item : items) {
                System.out.printf("%-2d %-5s %s%n",
                        item.getId(), item.isDone() ? "YES" : "NO", item.getText());
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}