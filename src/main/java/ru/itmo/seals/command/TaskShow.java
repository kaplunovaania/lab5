package ru.itmo.seals.command;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.model.Task;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import java.util.List;
import java.util.Scanner;

public class TaskShow extends Command {
    private final TaskCollectionManager taskManager;
    private final ChecklistCollectionManager checklistManager;

    public TaskShow(TaskCollectionManager taskManager, ChecklistCollectionManager checklistManager) {
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
            Task task = taskManager.getById(taskId);

            if (task == null) {
                System.out.println("Ошибка: задача с id=" + taskId + " не найдена");
                return;
            }

            List<Checklist> items = checklistManager.getByTaskId(taskId);
            String deadlineStr = (task.getDeadlineAt() == null) ? "null" :
                    java.time.LocalDate.ofInstant(task.getDeadlineAt(), java.time.ZoneOffset.UTC).toString();

            System.out.println("Task #" + task.getId());
            System.out.println("status: " + task.getStatus());
            System.out.println("priority: " + task.getPriority());
            System.out.println("deadline: " + deadlineStr);
            System.out.println("assignee: " + (task.getAssigneeUsername() != null ? task.getAssigneeUsername() : "-"));
            System.out.println("checklist: " + items.size() + " items");

            if (!items.isEmpty()) {
                System.out.println("Checklist");
                for (Checklist item : items) {
                    System.out.println((item.isDone() ? "[x]" : "[ ]") + " " + item.getText());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}
