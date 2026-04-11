package ru.itmo.seals.command;
import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.UserService;

import java.util.Scanner;

public class CheckToggle extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;
    private final UserService userService;

    public CheckToggle(TaskCollectionManager taskManager, ChecklistCollectionManager checklistManager, UserService userService) {
        this.checklistManager = checklistManager;
        this.userService = userService;
        this.taskManager = taskManager;
    }


    @Override
    public void execute(String[] args, Scanner scanner) {
        if (!userService.isLoggedIn()) {
            System.out.println("Ошибка: необходимо войти в систему");
            return;
        }
        if (args.length < 1) {
            System.out.println("Ошибка: укажите ID пункта");
            return;
        }
        try {
            long itemId = Long.parseLong(args[0]);
            Checklist item = checklistManager.getById(itemId);

            if (item == null) {
                System.out.println("Ошибка: пункт не найден");
                return;
            }
            var task = taskManager.getById(item.getTaskId());
            if (task != null && task.getOwnerId() != userService.getCurrentUserId()) {
                System.out.println("Ошибка: у вас нет прав на изменение этого объекта");
                return;
            }
            item.setDone(!item.isDone());
            System.out.println("OK item " + itemId + " DONE=" + (item.isDone() ? "YES" : "NO"));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}