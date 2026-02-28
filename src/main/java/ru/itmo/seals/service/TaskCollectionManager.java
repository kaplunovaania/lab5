package ru.itmo.seals.service;

import ru.itmo.seals.model.Task;

import java.time.Instant;
import java.util.*;

public class TaskCollectionManager {
    private final TreeMap<Long, Task> taskCollection = new TreeMap<>();
    public void addTask(Task task) {
        for (Task t : taskCollection.values()) {
            if (t.getId() == task.getId()) {
                throw new IllegalArgumentException("Task with id" + task.getId() + "already exists");
            }
        }
        taskCollection.put(task.getId(), task);
    }
    public List<Task> getTask() {
        return Collections.unmodifiableList(new ArrayList<>(taskCollection.values()));
    }
    public long getTaskNextId() {
        return System.currentTimeMillis() + taskCollection.size();
    }
    public Task getById(long id) {
        return taskCollection.get(id);
    }
    public List<Task> getAll() {
        return new ArrayList<>(taskCollection.values());
    }

    public boolean remove(long id) {
        return taskCollection.remove(id) != null;
    }
    public void update(long id, String newText) {
        Task task = getById(id);
        if (task == null) throw new NoSuchElementException("Task with this id doesn't find");

        task.setText(newText);
        task.setUpdatedAt(Instant.now());
    }
}
