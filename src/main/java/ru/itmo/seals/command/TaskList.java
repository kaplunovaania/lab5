package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.service.TaskCollectionManager;
import java.util.List;
import java.util.Scanner;

public class TaskList extends Command {
    private final TaskCollectionManager taskManager;

    public TaskList(TaskCollectionManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        List<Task> tasks = taskManager.getAll();
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
            return;
        }

        System.out.println("ID  Priority  Status      Deadline     Assignee    Text");
        for (Task task : tasks) {
            String deadlineStr = (task.getDeadlineAt() == null) ? "null" :
                    java.time.LocalDate.ofInstant(task.getDeadlineAt(), java.time.ZoneOffset.UTC).toString();
            String assignee = (task.getAssigneeUsername() == null) ? "-" : task.getAssigneeUsername();

            System.out.printf("%-2d %-9s %-11s %-12s %-11s %s%n",
                    task.getId(), task.getPriority(), task.getStatus(), deadlineStr, assignee, task.getText());
        }
    }
}