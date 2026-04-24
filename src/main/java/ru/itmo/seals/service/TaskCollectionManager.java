package ru.itmo.seals.service;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.storage.DatabaseStorage;

import java.util.*;
import java.util.stream.Collectors;

public class TaskCollectionManager {
    private final Map<Long, Task> taskCollection = new HashMap<>();
    private DatabaseStorage dbStorage;

    public void setDatabaseStorage(DatabaseStorage dbStorage) {
        this.dbStorage = dbStorage;
    }

    // Загрузка из БД при старте
    public void loadFromDatabase() {
        if (dbStorage == null) return;
        List<Task> tasks = dbStorage.loadAllTasks();
        taskCollection.clear();
        for (Task task : tasks) {
            taskCollection.put(task.getId(), task);
        }
        System.out.println("Loaded " + taskCollection.size() + " tasks from database");
    }

    public long addTask(Task task) {
        if (dbStorage != null) {
            long dbId = dbStorage.saveTask(task);
            if (dbId > 0) {
                taskCollection.put(dbId, task);
                return dbId;
            }
        }
        // Fallback to in-memory only
        long id = taskCollection.isEmpty() ? 1 : taskCollection.keySet().stream().max(Long::compare).get() + 1;
        taskCollection.put(id, task);
        return id;
    }

    public boolean updateTask(Task task) {
        if (dbStorage != null) {
            if (dbStorage.updateTask(task)) {
                taskCollection.put(task.getId(), task);
                return true;
            }
            return false;
        }
        if (taskCollection.containsKey(task.getId())) {
            taskCollection.put(task.getId(), task);
            return true;
        }
        return false;
    }

    public boolean remove(long id, long ownerId) {
        if (dbStorage != null) {
            if (dbStorage.deleteTask(id, ownerId)) {
                taskCollection.remove(id);
                return true;
            }
            return false;
        }
        Task task = taskCollection.get(id);
        if (task != null && task.getOwnerId() == ownerId) {
            taskCollection.remove(id);
            return true;
        }
        return false;
    }

    // === GETTERS ===
    public Task getById(long id) { return taskCollection.get(id); }
    public List<Task> getAll() { return new ArrayList<>(taskCollection.values()); }
    public List<Task> getByOwnerId(long ownerId) {
        return taskCollection.values().stream()
                .filter(t -> t.getOwnerId() == ownerId)
                .collect(Collectors.toList());
    }
    public void clear() { taskCollection.clear(); }
}