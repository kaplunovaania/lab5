package ru.itmo.seals.storage;

import ru.itmo.seals.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager implements AutoCloseable {
    private Connection connection;

    public boolean connect() {
        try {
            Class.forName(DatabaseConfig.getDriver());
            connection = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
            );
            System.out.println("Connected to PostgreSQL!");
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Выполняет инициализацию схемы
    public boolean initializeSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Читаем schema.sql из ресурсов
            String schema = new String(getClass()
                    .getClassLoader()
                    .getResourceAsStream("schema.sql")
                    .readAllBytes());

            // Разделяем на отдельные запросы по ";"
            String[] queries = schema.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }
            System.out.println("Schema initialized!");
            return true;
        } catch (Exception e) {
            System.err.println("Schema initialization failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    public long insertAndGetId(PreparedStatement stmt) throws SQLException {
        stmt.executeUpdate();
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return -1;
    }

    public boolean handleSqlError(SQLException e, String operation) {
        String sqlState = e.getSQLState();
        switch (sqlState) {
            case "23505": // unique_violation
                System.err.println(operation + ": duplicate entry");
                break;
            case "23503": // foreign_key_violation
                System.err.println(operation + ": foreign key violation");
                break;
            case "08001": // connection_failure
                System.err.println(operation + ": cannot connect to database");
                break;
            default:
                System.err.println(operation + ": " + e.getMessage());
        }
        return false;
    }
}
