package ru.itmo.seals.service;

import ru.itmo.seals.model.Checklist; // Или Checklist, если не переименовали

import java.time.Instant;
import java.util.*;

public class ChecklistCollectionManager {
    private final TreeMap<Long, Checklist> checklistCollection = new TreeMap<>();

    // Ваш метод addChecklist (оставляем как есть)
    public void addChecklist(Checklist checklist) {
        for (Checklist t : checklistCollection.values()) {
            if (t.getId() == checklist.getId()) {
                throw new IllegalArgumentException("Checklist with id " + checklist.getId() + " already exists");
            }
        }
        checklistCollection.put(checklist.getId(), checklist);
    }

    // Ваш метод генерации ID (оставляем)
    public long getChecklistNextId() {
        return System.currentTimeMillis() + checklistCollection.size();
    }

    // Ваши геттеры (оставляем)
    public List<Checklist> getChecklist() {
        return Collections.unmodifiableList(new ArrayList<>(checklistCollection.values()));
    }

    public Checklist getById(long id) {
        return checklistCollection.get(id);
    }

    public List<Checklist> getAll() {
        return new ArrayList<>(checklistCollection.values());
    }

    // Ваш метод remove (оставляем)
    public boolean remove(long id) {
        return checklistCollection.remove(id) != null;
    }

    // Ваш метод update (оставляем)
    public void update(long id, String newChecklist) {
        Checklist checklist = getById(id);
        if (checklist == null) throw new NoSuchElementException("Checklist with this id doesn't find");

        checklist.setText(newChecklist);
    }
}
