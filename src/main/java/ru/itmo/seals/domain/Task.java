package ru.itmo.seals.domain;

import java.time.Instant;
import java.util.Objects;

public final class Task implements Comparable<Task> {
    public long id; // Автогенерация
    public String text;
    public TaskPriority priority;
    public TaskStatus status;
    public Instant deadlineAt;
    public String assigneeUsername;
    public String ownerUsername;
    public Instant createdAt;
    public Instant updatedAt;

    @Override
    public int compareTo(Task o) {
        return Long.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return String.format("Task[ID=%d, Status=%s, Priority=%s, Text='%s', Deadline=%s]",
                id, status, priority, text, deadlineAt != null ? deadlineAt : "none");
    }

    // Переопределяем equals/hashCode для корректной работы в коллекциях (как в варианте 5, но полезно везде)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (ru.itmo.seals.domain.Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}