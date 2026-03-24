package ru.itmo.seals.command;
import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.service.ChecklistCollectionManager;

import java.util.Scanner;

public class CheckToggle extends Command {
    private final ChecklistCollectionManager checklistManager;

    public CheckToggle(ChecklistCollectionManager checklistManager) {
        this.checklistManager = checklistManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
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

            item.setDone(!item.isDone());
            System.out.println("OK item " + itemId + " DONE=" + (item.isDone() ? "YES" : "NO"));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}