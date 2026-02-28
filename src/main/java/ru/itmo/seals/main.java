package ru.itmo.seals;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.service.TaskCollectionManager;

import java.time.Instant;
import java.util.List;

public class main {
    public static void main(String[] args) {
    TaskCollectionManager taskCollection = new TaskCollectionManager();
    long taskNextId = taskCollection.getTaskNextId();
    Task task = new Task(0L, Instant.now());

    taskCollection.addTask(task);
    List<Task> t = taskCollection.getTask();
    t.forEach(System.out::println);
    }
}
