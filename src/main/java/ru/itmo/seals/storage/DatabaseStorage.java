package ru.itmo.seals.storage;

import ru.itmo.seals.model.*;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStorage {
    private final DatabaseManager db;

    public DatabaseStorage(DatabaseManager db) {
        this.db = db;
    }

    // === ЗАГРУЗКА ДАННЫХ ИЗ БД ===

    public List<Task> loadAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY id";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = mapTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            db.handleSqlError(e, "Load tasks");
        }
        return tasks;
    }

    public List<Checklist> loadAllChecklists() {
        List<Checklist> checklists = new ArrayList<>();
        String sql = "SELECT * FROM checklists ORDER BY id";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Checklist checklist = mapChecklist(rs);
                checklists.add(checklist);
            }
        } catch (SQLException e) {
            db.handleSqlError(e, "Load checklists");
        }
        return checklists;
    }

    // === СОХРАНЕНИЕ В БД ===

    public long saveTask(Task task) {
        String sql = """
            INSERT INTO tasks 
            (text, priority, status, deadline_at, assignee_username, owner_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, task.getText());
            stmt.setString(2, task.getPriority().name());
            stmt.setString(3, task.getStatus().name());

            if (task.getDeadlineAt() != null) {
                stmt.setTimestamp(4, Timestamp.from(task.getDeadlineAt()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setString(5, task.getAssigneeUsername());
            stmt.setLong(6, task.getOwnerId());
            stmt.setTimestamp(7, Timestamp.from(task.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.from(task.getUpdatedAt()));

            long generatedId = db.insertAndGetId(stmt);
            if (generatedId > 0) {
                // Обновляем ID в объекте (если был 0)
                if (task.getId() == 0) {
                    // Нужен сеттер или рефакторинг конструктора
                }
            }
            return generatedId;

        } catch (SQLException e) {
            db.handleSqlError(e, "Save task");
            return -1;
        }
    }

    public boolean updateTask(Task task) {
        String sql = """
            UPDATE tasks SET 
            text = ?, priority = ?, status = ?, deadline_at = ?, 
            assignee_username = ?, updated_at = ?
            WHERE id = ? AND owner_id = ?
            """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, task.getText());
            stmt.setString(2, task.getPriority().name());
            stmt.setString(3, task.getStatus().name());

            if (task.getDeadlineAt() != null) {
                stmt.setTimestamp(4, Timestamp.from(task.getDeadlineAt()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }

            stmt.setString(5, task.getAssigneeUsername());
            stmt.setTimestamp(6, Timestamp.from(task.getUpdatedAt()));
            stmt.setLong(7, task.getId());
            stmt.setLong(8, task.getOwnerId());  // Проверка прав на уровне БД

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            db.handleSqlError(e, "Update task");
            return false;
        }
    }

    public boolean deleteTask(long taskId, long ownerId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND owner_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, taskId);
            stmt.setLong(2, ownerId);  // Проверка прав

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            db.handleSqlError(e, "Delete task");
            return false;
        }
    }

    // === ЧЕКЛИСТЫ ===

    public long saveChecklist(Checklist checklist) {
        String sql = """
            INSERT INTO checklists (task_id, text, done, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, checklist.getTaskId());
            stmt.setString(2, checklist.getText());
            stmt.setBoolean(3, checklist.isDone());
            stmt.setTimestamp(4, Timestamp.from(checklist.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.from(checklist.getUpdatedAt()));

            return db.insertAndGetId(stmt);

        } catch (SQLException e) {
            db.handleSqlError(e, "Save checklist");
            return -1;
        }
    }

    public boolean updateChecklist(Checklist checklist) {
        String sql = """
            UPDATE checklists SET text = ?, done = ?, updated_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, checklist.getText());
            stmt.setBoolean(2, checklist.isDone());
            stmt.setTimestamp(3, Timestamp.from(checklist.getUpdatedAt()));
            stmt.setLong(4, checklist.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            db.handleSqlError(e, "Update checklist");
            return false;
        }
    }

    public boolean deleteChecklist(long checklistId) {
        String sql = "DELETE FROM checklists WHERE id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, checklistId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            db.handleSqlError(e, "Delete checklist");
            return false;
        }
    }

    public List<Checklist> getChecklistsByTaskId(long taskId) {
        List<Checklist> result = new ArrayList<>();
        String sql = "SELECT * FROM checklists WHERE task_id = ? ORDER BY id";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapChecklist(rs));
                }
            }
        } catch (SQLException e) {
            db.handleSqlError(e, "Get checklists by task");
        }
        return result;
    }

    // === МАППИНГ ===

    private Task mapTask(ResultSet rs) throws SQLException {
        return new Task(
                rs.getLong("id"),
                rs.getString("text"),
                TaskPriority.valueOf(rs.getString("priority")),
                TaskStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("deadline_at") != null
                        ? rs.getTimestamp("deadline_at").toInstant() : null,
                rs.getString("assignee_username"),
                rs.getLong("owner_id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private Checklist mapChecklist(ResultSet rs) throws SQLException {
        Checklist c = new Checklist(
                rs.getLong("id"),
                rs.getLong("task_id"),
                rs.getString("text")
        );
        c.setDone(rs.getBoolean("done"));
        // createdAt/updatedAt уже установлены в конструкторе
        return c;
    }
}
