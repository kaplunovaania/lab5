package ru.itmo.seals.command;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.storage.FileStorage;
import java.util.Scanner;

public class Load extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;
    private final FileStorage storage;

    public Load(TaskCollectionManager taskManager,
                ChecklistCollectionManager checklistManager) {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
        this.storage = new FileStorage();
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 1) {
            System.out.println("Ошибка: укажите путь к файлу");
            System.out.println("Пример: load data.json");
            return;
        }

        boolean success = storage.load(args[0], taskManager, checklistManager);
        if (success) {
            System.out.println("OK данные загружены из " + args[0]);
        } else {
            System.out.println("Ошибка: данные не загружены");
        }
    }
}
