package ru.itmo.seals.model;
import java.time.Instant;
import java.util.Objects;

public final class Checklist {
    private final long id;
    // К какой задаче относится (id задачи).
// Должен ссылаться на реально существующий Task.
    private long taskId;
// Текст пункта (что проверить/сделать). Нельзя пустое. До 256 символов.
    private String text;
    // Выполнен ли пункт. true = выполнен.
    private boolean done;
    // Когда пункт создан. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда пункт обновляли (например, переключали done). Программа обновляет автоматически.
    private Instant updatedAt;

    public Checklist(long id, long taskId, String text, boolean done, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.taskId = taskId;
        validateText(text);
        this.text = text;
        this.done = false;
        if (createdAt != null) {
            this.createdAt = createdAt;
        } else {
            this.createdAt = Instant.now();
        }
        if (updatedAt != null) {
            this.updatedAt = createdAt;
        } else {
            this.updatedAt = Instant.now();
        }
    }
    private void validateText(String text) {
        if (text == null || text.isEmpty() || text.length() > 25) {
            throw new IllegalArgumentException("Invalid text: " + text + "текст не должен превышать 25 символов");
        }
    }

    public long getId() { return id; }
    public long getTaskId() { return taskId; }
    public String getText() { return text; }
    public boolean isDone() { return done; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setText(String text) {
        validateText(text);
            this.text = text;
            this.updatedAt = Instant.now();  // ← Только это добавить!
        }

    public void setDone(boolean done) {
        this.done = done;
        this.updatedAt = Instant.now();
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checklist that = (Checklist) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Checklist{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", text='" + text + '\'' +
                ", done=" + done +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
