package ru.itmo.seals.service;

import ru.itmo.seals.model.User;
import ru.itmo.seals.storage.DatabaseManager;

import java.sql.*;
import java.util.Optional;

import ru.itmo.seals.model.Task;

public class UserService {
    private final DatabaseManager db;
    private User currentUser;

    public UserService(DatabaseManager db) {
        this.db = db;
    }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty() || login.length() < 4) {
            System.out.println("Ошибка: логин должен содержать минимум 4 символа");
            return false;
        }
        if (password == null || password.length() < 4) {
            System.out.println("Ошибка: пароль должен содержать минимум 4 символа");
            return false;
        }

        // Проверка на существование
        if (findByLogin(login).isPresent()) {
            System.out.println("Ошибка: пользователь с таким логином уже существует");
            return false;
        }

        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            User user = new User(0, login, password);  // id=0, будет сгенерирован

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());

            long id = db.insertAndGetId(stmt);
            if (id > 0) {
                System.out.println("Пользователь " + login + " зарегистрирован (id=" + id + ")");
                return true;
            }
            return false;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.out.println("Ошибка: пользователь с таким логином уже существует");
            } else {
                System.out.println("Ошибка регистрации: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean login(String login, String password) {
        Optional<User> userOpt = findByLogin(login);
        if (userOpt.isEmpty()) {
            System.out.println("Ошибка: пользователь не найден");
            return false;
        }

        User user = userOpt.get();
        if (!user.checkPassword(password)) {
            System.out.println("Ошибка: неверный пароль");
            return false;
        }

        currentUser = user;
        System.out.println("Вход выполнен: " + user.getLogin() + " (id=" + user.getId() + ")");
        return true;
    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getLong("id"),
                            rs.getString("login"),
                            rs.getString("password_hash"),
                            true  // already hashed
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Выход из системы: " + currentUser.getLogin());
            currentUser = null;
        }
    }

    public boolean isLoggedIn() { return currentUser != null; }
    public User getCurrentUser() { return currentUser; }
    public long getCurrentUserId() { return currentUser != null ? currentUser.getId() : -1; }
    public String getCurrentUserLogin() { return currentUser != null ? currentUser.getLogin() : "anonym"; }

    // Проверка: текущий пользователь имеет доступ к задаче
    public boolean hasTaskAccess(Task task) {
        if (currentUser == null) {
            return false;
        }

        // Владелец ИЛИ назначенный
        boolean isOwner = task.getOwnerId() == currentUser.getId();
        boolean isAssignee = task.getAssigneeUsername() != null &&
                task.getAssigneeUsername().equals(currentUser.getLogin());

        return isOwner || isAssignee;
    }

    // Проверка: текущий пользователь может удалить задачу
    public boolean canDeleteTask(Task task) {
        if (currentUser == null) {
            return false;
        }
        // Только владелец может удалять
        return task.getOwnerId() == currentUser.getId();
    }
}