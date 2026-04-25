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

    public boolean updateTask(Task task, long userId) {
        if (dbStorage != null) {
            // Для БД просто сохраняем (проверка прав будет в SQL)
            if (dbStorage.updateTask(task, userId)) {
                taskCollection.put(task.getId(), task);
                return true;
            }
            return false;
        }

        // Проверка прав в памяти (без userService!)
        Task existing = taskCollection.get(task.getId());
        if (existing != null &&
                (existing.getOwnerId() == userId ||
                        (existing.getAssigneeUsername() != null &&
                                existing.getAssigneeUsername().equals(getUserLoginById(userId))))) {  // ← Новый метод
            taskCollection.put(task.getId(), task);
            return true;
        }
        return false;
    }

    // Вспомогательный метод для получения логина (заглушка)
    private String getUserLoginById(long userId) {
        // Для простоты возвращаем пустую строку
        // В реальной системе тут был бы запрос к UserService
        return "";
    }

    public boolean remove(long id, long ownerId) {
        if (dbStorage != null) {
            if (dbStorage.deleteTask(id, ownerId)) {  // ← Только владелец
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