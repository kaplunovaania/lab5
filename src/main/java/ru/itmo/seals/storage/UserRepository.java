package ru.itmo.seals.storage;

import ru.itmo.seals.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class UserRepository {
    private final TreeMap<Long, User> users = new TreeMap<>();
    private final JsonMapper jsonMapper;
    private final String filePath;

    public UserRepository(String filePath) {
        this.filePath = filePath;
        this.jsonMapper = new JsonMapper();
        loadFromFile();
    }

    public long getNextId() {
        return users.isEmpty() ? 1 : users.lastKey() + 1;
    }

    public Optional<User> findByLogin(String login) {
        return users.values().stream()
                .filter(u -> u.getLogin().equals(login))
                .findFirst();
    }

    public boolean existsByLogin(String login) {
        return findByLogin(login).isPresent();
    }

    public User add(User user) {
        if (existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("User with login '" + user.getLogin() + "' already exists");
        }
        users.put(user.getId(), user);
        saveToFile();
        return user;
    }

    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public List<User> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(users.values()));
    }

    public void saveToFile() {
        try {
            UserDto[] userDtos = users.values().stream()
                    .map(u -> new UserDto(u.getId(), u.getLogin(), u.getPasswordHash()))
                    .toArray(UserDto[]::new);
            jsonMapper.save(userDtos, filePath);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }

            try (Reader reader = new InputStreamReader(
                    new FileInputStream(file),
                    StandardCharsets.UTF_8)) {

                com.google.gson.reflect.TypeToken<UserDto[]> typeToken =
                        new com.google.gson.reflect.TypeToken<UserDto[]>() {};

                UserDto[] userDtos = jsonMapper.getGson().fromJson(reader, typeToken.getType());

                if (userDtos != null) {
                    for (UserDto dto : userDtos) {
                        User user = new User(dto.id, dto.login, dto.passwordHash, true);
                        users.put(user.getId(), user);
                    }
                    System.out.println("Loaded " + users.size() + " users");
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private static class UserDto {
        public long id;
        public String login;
        public String passwordHash;

        public UserDto(long id, String login, String passwordHash) {
            this.id = id;
            this.login = login;
            this.passwordHash = passwordHash;
        }
    }
}