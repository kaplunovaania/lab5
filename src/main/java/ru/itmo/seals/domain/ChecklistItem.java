package ru.itmo.seals.domain;

import java.time.Instant;

public final class ChecklistItem {
    public long id;
    public long taskId;
    public String text;
    public boolean done;
    public Instant createdAt;
    public Instant updatedAt;

    @Override
    public String toString() {
        return String.format("  [%s] ID=%d: %s", (done ? "X" : " "), id, text);
    }
}
