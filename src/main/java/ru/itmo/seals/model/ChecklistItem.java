package ru.itmo.seals.model;

import java.time.Instant;

public final class ChecklistItem {
    private final long id;
    private long taskId;
    private String text;
    private boolean done;
    private final Instant createdAt;
    private Instant updatedAt;

    @Override
    public String toString() {
        return String.format("  [%s] ID=%d: %s", (done ? "X" : " "), id, text);
    }
}
