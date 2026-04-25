package ru.itmo.seals.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.cfg")) {
            if (input == null) {
                System.err.println("Config file 'database.cfg' not found!");
            }
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
    }

    public static String getUrl() {
        return props.getProperty("db.url",
                System.getenv("DB_URL"));  // Fallback to env var
    }

    public static String getUsername() {
        return props.getProperty("db.username",
                System.getenv("DB_USERNAME"));
    }

    public static String getPassword() {
        return props.getProperty("db.password",
                System.getenv("DB_PASSWORD"));
    }

    public static String getDriver() {
        return props.getProperty("db.driver", "org.postgresql.Driver");
    }
}