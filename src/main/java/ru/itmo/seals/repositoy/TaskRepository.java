package ru.itmo.seals.repositoy;

import ru.itmo.seals.model.Task;
import java.time.ZonedDateTime;
import java.util.TreeMap;

public class TaskRepository {
    private final TreeMap<Long, Task> collection = new TreeMap<>();
    private final ZonedDateTime initializationDate = ZonedDateTime.now();
    private long nextId = 1;

    public void add(Task task) {
        task.id = nextId++;
        collection.put(task.id, task);
    }

    public TreeMap<Long, Task> getCollection() {
        return collection;
    }

    public Task getById(long id) {
        return collection.get(id);
    }

    public boolean remove(long id) {
        return collection.remove(id) != null;
    }

    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    public int size() {
        return collection.size();
    }
}
