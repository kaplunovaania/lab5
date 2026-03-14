package ru.itmo.seals.command;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.model.Task;

import java.util.List;

public class TaskShow {
    public void taskShow(long taskId) {
        Task task = taskManager.getById(taskId);
        if (task == null) {
            System.err.println("Ошибка: задача с id=" + taskId + " не найдена");
            return;
        }
        List<Checklist> items = checklistManager.getByTaskId(taskId);

        System.out.println("Task #" + task.getId());
        System.out.println("status: " + task.getStatus());
        System.out.println("priority: " + task.getPriority());
        System.out.println("deadline: " + formatDeadline(task.getDeadlineAt()));
        System.out.println("checklist: " + items.size() + " items");
    }
}
