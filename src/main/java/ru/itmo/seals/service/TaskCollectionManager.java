package ru.itmo.seals.service;

import ru.itmo.seals.model.Task;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class TaskCollectionManager {
    private final TreeMap<Long, Task> taskCollection = new TreeMap<>();
    private final ZonedDateTime initializationDate = ZonedDateTime.now();
    private long nextId = 1;


    public void addTask(Task task) {
        for (Task t : taskCollection) {
            if (t.getId() == task.getId()) {
                throw new IllegalArgumentException("Task with id" + task.getId() + "already exists");

            }
        }
    }
    public List<Task> getTask() {
        return Collection.unmodifiableList(taskCollection);
    }
    public long getTaskNextId() {
        return System.currentTimeMillis() + taskCollection.size();
    }
        task.id = nextId++;
        taskCollection.put(task.id, task);
    }
