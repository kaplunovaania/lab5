package ru.itmo.seals.service;

import ru.itmo.seals.model.Checklist;

import java.time.Instant;
import java.util.*;

public class ChecklistCollectionManager {
    private final TreeMap<Long, Checklist> checklistCollection = new TreeMap<>();
    public void addChecklist(Checklist checklist) {
        for (Checklist t : checklistCollection.values()) {
            if (t.getId() == checklist.getId()) {
                throw new IllegalArgumentException("Checklist with id" + checklist.getId() + "already exists");
            }
        }
        checklistCollection.put(checklist.getId(), checklist);
    }
    public List<Checklist> getChecklist() {
        return Collections.unmodifiableList(new ArrayList<>(checklistCollection.values()));
    }
    public long getChecklistNextId() {
        return System.currentTimeMillis() + checklistCollection.size();
    }
    public Checklist getById(long id) {
        return checklistCollection.get(id);
    }
    public List<Checklist> getAll() {
        return new ArrayList<>(checklistCollection.values());
    }

    public boolean remove(long id) {
        return checklistCollection.remove(id) != null;
    }

    public void update(long id, String newChecklist) {
        Checklist checklist = getById(id);
        if (checklist == null) throw new NoSuchElementException("Checklist with this id doesn't find");

        checklist.setText(newChecklist);
        checklist.setUpdatedAt(Instant.now());
    }
}
