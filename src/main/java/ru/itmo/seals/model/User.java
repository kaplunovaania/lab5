package ru.itmo.seals.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class User {
    private final long id;
    private final String login;
    private final String passwordHash;

    public User(long id, String login, String password) {
        this.id = id;
        this.login = login;
        this.passwordHash = hashPassword(password);
    }

    // Для десериализации из JSON
    public User(long id, String login, String passwordHash, boolean isHashed) {
        this.id = id;
        this.login = login;
        this.passwordHash = isHashed ? passwordHash : hashPassword(passwordHash);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public long getId() { return id; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                '}';
    }
}