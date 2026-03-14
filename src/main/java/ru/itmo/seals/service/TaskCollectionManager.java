package ru.itmo.seals.service;

import ru.itmo.seals.model.Task;
import java.util.*;

public class TaskCollectionManager {
    private final TreeMap<Long, Task> taskCollection = new TreeMap<>();

    public void addTask(Task task) {
        for (Task t : taskCollection.values()) {
            if (t.getId() == task.getId()) {
                throw new IllegalArgumentException("Задание с id " + task.getId() + " уже существует");
            }
        }
        taskCollection.put(task.getId(), task);
    }

    public long getTaskNextId() {
        return taskCollection.size();
    }

    public List<Task> getTask() {
        return Collections.unmodifiableList(new ArrayList<>(taskCollection.values()));
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
        if (task == null) throw new NoSuchElementException("Нет задания с таким id");
        task.setText(newText);
    }
}
