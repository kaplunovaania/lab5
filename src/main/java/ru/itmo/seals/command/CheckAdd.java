package ru.itmo.seals.command;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import java.util.Scanner;
import ru.itmo.seals.service.UserService;

public class CheckAdd extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;
    private final UserService userService;

    public CheckAdd(TaskCollectionManager taskManager, ChecklistCollectionManager checklistManager, UserService userService) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.userService = userService;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (!userService.isLoggedIn()) {
            System.out.println("Ошибка: необходимо войти в систему");
            return;
        }
        if (args.length < 2) {
            System.out.println("Ошибка: формат check_add <task_id> <text>");
            return;
        }
        try {
            long taskId = Long.parseLong(args[0]);
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) textBuilder.append(" ");
                textBuilder.append(args[i]);
            }
            String text = textBuilder.toString();

            if (taskManager.getById(taskId) == null) {
                System.out.println("Ошибка: задача не найдена");
                return;
            }

            long id = checklistManager.getChecklistNextId();
            Checklist item = new Checklist(id, taskId, text);
            checklistManager.addChecklist(item);
            System.out.println("OK item_id=" + id);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
        }
    }
}
