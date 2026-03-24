package ru.itmo.seals.command;

import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.service.TaskCollectionManager;

import java.util.Scanner;


public abstract class Command {
    private TaskCollectionManager taskManager = null;
    private ChecklistCollectionManager checklistManager = null;

    public Command() {
    }

    public abstract void execute(String[] args, Scanner scanner);

    public TaskCollectionManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskCollectionManager taskManager) {
        this.taskManager = taskManager;
    }

    public ChecklistCollectionManager getChecklistManager() {
        return checklistManager;
    }

    public void setChecklistManager(ChecklistCollectionManager checklistManager) {
        this.checklistManager = checklistManager;
    }
}
