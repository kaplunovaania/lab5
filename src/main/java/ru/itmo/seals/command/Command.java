package ru.itmo.seals.command;

import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.service.TaskCollectionManager;

import java.util.Scanner;


abstract class Command {
    private TaskCollectionManager taskManager = null;
    private ChecklistCollectionManager checklistManager = null;

    public Command() {
        this.taskManager = taskManager;
        this.checklistManager = checklistManager;
    }

    public abstract void execute(String[] args, Scanner scanner);
}
