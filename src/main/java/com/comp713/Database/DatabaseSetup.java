package com.comp713.Database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void main(String[] args) {

        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE
                )
                """;

        String createTasks = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    description TEXT,
                    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
                    user_id INT NOT NULL,

                    CONSTRAINT fk_tasks_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                )
                """;

        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createUsers);
            System.out.println("Users table created!");

            statement.executeUpdate(createTasks);
            System.out.println("Tasks table created!");

            System.out.println("Database setup complete!");

        } catch (Exception e) {
            System.out.println("Database setup failed: " + e.getMessage());
        }
    }
}