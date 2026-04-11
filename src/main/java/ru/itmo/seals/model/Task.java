package ru.itmo.seals.model;
import java.time.Instant;
import java.util.Objects;

public final class Task {
    private final long id;
// Текст задачи (что сделать). Нельзя пустое. До 25 символов.
    private String text;
    private TaskPriority priority;
    private TaskStatus status;
// Дедлайн (до какого дня сделать). Можно null, если не задан.
    private Instant deadlineAt;
// На кого назначена (логин). Можно null, если не назначено.
    private String assigneeUsername;
// Кто создал задачу (логин). На ранних этапах можно "SYSTEM".
    private long ownerId;
    // Когда создано. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда обновляли. Программа обновляет автоматически.
    private Instant updatedAt;

    public Task(long id, String text, TaskPriority priority, TaskStatus status,
                Instant deadlineAt, String assigneeUsername, long ownerId,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        validateText(text);
        this.text = text;
        validatePriority(priority);
        this.priority = priority;
        validateStatus(status);
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.assigneeUsername = assigneeUsername;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Task(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
    private void validateText(String text) {
        if (text == null || text.isEmpty() || text.length() > 25) {
            throw new IllegalArgumentException("Текст задачи не может быть пустым и должен быть до 25 символов");
        }
    }

    private void validatePriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Приоритет не может быть пустым");
        }
    }

    private void validateStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Статус не может быть пустым");
        }
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getDeadlineAt() {
        return deadlineAt;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getOwnerId() {
        return ownerId;
    }


    public void setText(String text) {
        validateText(text);
        this.text = text;
        this.updatedAt = Instant.now();
    }

    public void setPriority(TaskPriority priority) {
        validatePriority(priority);
        this.priority = priority;
        this.updatedAt = Instant.now();
    }

    public void setStatus(TaskStatus status) {
        validateStatus(status);
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setDeadlineAt(Instant deadlineAt) {
        this.deadlineAt = deadlineAt;
        this.updatedAt = Instant.now();
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
        this.updatedAt = Instant.now();
    }


    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(text, task.text) && priority == task.priority && status == task.status && Objects.equals(deadlineAt, task.deadlineAt) && Objects.equals(assigneeUsername, task.assigneeUsername) && Objects.equals(ownerId, task.ownerId) && Objects.equals(createdAt, task.createdAt) && Objects.equals(updatedAt, task.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, priority, status, deadlineAt, assigneeUsername, ownerId, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Task {" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", deadlineAt=" + deadlineAt +
                ", assigneeUsername='" + assigneeUsername + '\'' +
                ", ownerId=" + ownerId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}