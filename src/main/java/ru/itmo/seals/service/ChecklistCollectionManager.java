package ru.itmo.seals.service;

import ru.itmo.seals.model.Checklist;
import ru.itmo.seals.storage.DatabaseStorage;
import java.util.*;

public class ChecklistCollectionManager {
    private final TreeMap<Long, Checklist> checklistCollection = new TreeMap<>();

    private DatabaseStorage dbStorage;

    public void setDatabaseStorage(DatabaseStorage dbStorage) {
        this.dbStorage = dbStorage;
    }

    public void addChecklist(Checklist checklist) {
        for (Checklist t : checklistCollection.values()) {
            if (t.getId() == checklist.getId()) {
                throw new IllegalArgumentException("Чеклист с id " + checklist.getId() + " уже существует");
            }
        }

        if (dbStorage != null) {
            long dbId = dbStorage.saveChecklist(checklist);
            if (dbId > 0) {
                checklistCollection.put(dbId, checklist);
                return;
            }
        }

        checklistCollection.put(checklist.getId(), checklist);
    }

    public boolean updateChecklist(Checklist checklist) {
        if (dbStorage != null) {
            if (dbStorage.updateChecklist(checklist)) {
                checklistCollection.put(checklist.getId(), checklist);
                return true;
            }
            return false;
        }
        if (checklistCollection.containsKey(checklist.getId())) {
            checklistCollection.put(checklist.getId(), checklist);
            return true;
        }
        return false;
    }


    public List<Checklist> getChecklist() {
        return Collections.unmodifiableList(new ArrayList<>(checklistCollection.values()));
    }

    public Checklist getById(long id) {
        return checklistCollection.get(id);
    }

    public List<Checklist> getAll() {
        return new ArrayList<>(checklistCollection.values());
    }

    public long getChecklistNextId() {
        // Для БД: возвращаем 0, база сгенерирует реальный ID
        if (dbStorage != null) {
            return 0;
        }
        // Для памяти: генерируем следующий ID
        return checklistCollection.isEmpty() ? 1 : checklistCollection.lastKey() + 1;
    }

    public boolean remove(long id) {
        return checklistCollection.remove(id) != null;
    }

    public void update(long id, String newChecklist) {
        Checklist checklist = getById(id);
        if (checklist == null) throw new NoSuchElementException("Чеклист с таким id не найден");

        checklist.setText(newChecklist);
    }
    public void clear() {
        checklistCollection.clear();
    }

    public List<Checklist> getByTaskId(long taskId) {
        List<Checklist> result = new ArrayList<>();
        for (Checklist item : checklistCollection.values()) {
            if (item.getTaskId() == taskId) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }


    // Загрузка из БД
    public void loadFromDatabase() {
        if (dbStorage == null) {
            System.out.println("[ChecklistDB] dbStorage is null!");
            return;
        }

        System.out.println("[ChecklistDB] Loading checklists from database...");
        List<Checklist> checklists = dbStorage.loadAllChecklists();

        checklistCollection.clear();  // ← Очистить перед загрузкой!

        for (Checklist checklist : checklists) {
            checklistCollection.put(checklist.getId(), checklist);
        }

        System.out.println("[ChecklistDB] Loaded " + checklistCollection.size() + " checklists");
    }
}
