package ru.itmo.seals.command;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.storage.FileStorage;
import java.util.Scanner;

public class Save extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;
    private final FileStorage storage;

    public Save(TaskCollectionManager taskManager,
                ChecklistCollectionManager checklistManager) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.storage = new FileStorage();
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 1) {
            System.out.println("Ошибка: укажите путь к файлу");
            System.out.println("Пример: save data.json");
            return;
        }

        try {
            storage.save(args[0], taskManager, checklistManager);
            System.out.println("OK данные сохранены в " + args[0]);
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}
