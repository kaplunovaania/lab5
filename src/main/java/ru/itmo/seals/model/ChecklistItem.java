package ru.itmo.seals.model;
import java.time.Instant;
import java.util.Objects;

public final class ChecklistItem {
    private final long id;
    // К какой задаче относится (id задачи).
// Должен ссылаться на реально существующий Task.
    private long taskId;
// Текст пункта (что проверить/сделать). Нельзя пустое. До 256 символов.
    private String text;
    // Выполнен ли пункт. true = выполнен.
    private boolean done;
    // Когда пункт создан. Программа ставит автоматически.
    private Instant createdAt;
    // Когда пункт обновляли (например, переключали done). Программа обновляет автоматически.
    private Instant updatedAt;

    public ChecklistItem(long id, long taskId, String text, boolean done, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.taskId = taskId;
        this.text = text;
        this.done = done;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text != null && !text.isEmpty() && text.length() <= 256) {
            this.text = text;
        } else {
            throw new IllegalArgumentException("Invalid text: " + text);
        }
        this.text = text;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
