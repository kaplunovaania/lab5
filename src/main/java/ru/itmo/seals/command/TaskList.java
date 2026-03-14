package ru.itmo.seals.command;

import ru.itmo.seals.model.Task;

import java.util.List;

public class TaskList {
    public void taskList() {
        List<Task> tasks = taskManager.getAll();
        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
            return;
        }
        System.out.println("ID Priority  Status      Deadline     Assignee    Text");
        for (Task task : tasks) {
            String deadlineStr = formatDeadline(task.getDeadlineAt());
            String assignee = task.getAssigneeUsername() != null ? task.getAssigneeUsername() : "-";
            System.out.printf("%-2d %-9s %-11s %-12s %-11s %s%n", task.getId(), task.getPriority(), task.getStatus(), deadlineStr, assignee, task.getText());
        }
    }
}
