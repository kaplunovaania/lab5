package ru.itmo.seals.service;

import ru.itmo.seals.model.User;
import ru.itmo.seals.storage.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private User currentUser;

    public UserService(String usersFilePath) {
        this.userRepository = new UserRepository(usersFilePath);
    }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            System.out.println("Ошибка: логин не может быть пустым");
            return false;
        }
        if (password == null || password.length() < 4) {
            System.out.println("Ошибка: пароль должен быть не менее 4 символов");
            return false;
        }
        if (userRepository.existsByLogin(login)) {
            System.out.println("Ошибка: пользователь с таким логином уже существует");
            return false;
        }

        try {
            long id = userRepository.getNextId();
            User user = new User(id, login, password);
            userRepository.add(user);
            System.out.println("Пользователь " + login + " зарегистрирован (id=" + id + ")");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
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

    public void logout() {
        if (currentUser != null) {
            System.out.println("Выход из системы: " + currentUser.getLogin());
            currentUser = null;
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public String getCurrentUserLogin() {
        return currentUser != null ? currentUser.getLogin() : "anonym";
    }
}