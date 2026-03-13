package ru.itmo.seals.service;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;

import java.time.Instant;
import java.util.*;

public class TaskCollectionManager {
    private final TreeMap<Long, Task> taskCollection = new TreeMap<>();

    // Ваш метод addTask (оставляем как есть)
    public void addTask(Task task) {
        for (Task t : taskCollection.values()) {
            if (t.getId() == task.getId()) {
                throw new IllegalArgumentException("Task with id " + task.getId() + " already exists");
            }
        }
        taskCollection.put(task.getId(), task);
    }

    // Ваш метод генерации ID (оставляем)
    public long getTaskNextId() {
        return System.currentTimeMillis() + taskCollection.size();
    }

    // Ваши геттеры (оставляем)
    public List<Task> getTask() {
        return Collections.unmodifiableList(new ArrayList<>(taskCollection.values()));
    }

    public Task getById(long id) {
        return taskCollection.get(id);
    }

    public List<Task> getAll() {
        return new ArrayList<>(taskCollection.values());
    }

    // Ваш метод remove (оставляем)
    public boolean remove(long id) {
        return taskCollection.remove(id) != null;
    }

    // Ваш метод update (оставляем)
    public void update(long id, String newText) {
        Task task = getById(id);
        if (task == null) throw new NoSuchElementException("Task with this id doesn't find");
        task.setText(newText);
    }
}
