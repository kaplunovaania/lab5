package ru.itmo.seals.model;
import java.time.Instant;
import java.util.Objects;

public final class Task {
    private final long id;
// Текст задачи (что сделать). Нельзя пустое. До 25 символов.
    private String text;
    private TaskPriority priority;
    private TaskStatus status;
// Дедлайн (до какого дня сделать). Можно null, если незадан.
    private Instant deadlineAt;
// На кого назначена (логин). Можно null, если не назначено.
    private String assigneeUsername;
// Кто создал задачу (логин). На ранних этапах можно "SYSTEM".
    private String ownerUsername;
    // Когда создано. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда обновляли. Программа обновляет автоматически.
    private Instant updatedAt;

    public Task(long id, Instant createdAt, String text, TaskPriority priority, TaskStatus status, Instant deadlineAt, String assigneeUsername, String ownerUsername, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.setText(text);
        this.priority = priority;
        this.status = status;
        this.deadlineAt = deadlineAt;
        this.assigneeUsername = assigneeUsername;
        this.ownerUsername = ownerUsername;
        this.updatedAt = updatedAt;
    }

    public Task(long id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
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

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setText(String text) {
        if (!text.isEmpty() && text.length() <= 25 && text != null) {
            this.text = text;
        } else {
            throw new IllegalArgumentException("Invalid text: " + text);
        }

    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDeadlineAt(Instant deadlineAt) {
        this.deadlineAt = deadlineAt;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(text, task.text) && priority == task.priority && status == task.status && Objects.equals(deadlineAt, task.deadlineAt) && Objects.equals(assigneeUsername, task.assigneeUsername) && Objects.equals(ownerUsername, task.ownerUsername) && Objects.equals(createdAt, task.createdAt) && Objects.equals(updatedAt, task.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, priority, status, deadlineAt, assigneeUsername, ownerUsername, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", deadlineAt=" + deadlineAt +
                ", assigneeUsername='" + assigneeUsername + '\'' +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}