
package com.comp713;

import com.comp713.Database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TaskEventDAO {

    public static void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS task_events (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    task_id INT NOT NULL,
                    task_title VARCHAR(255) NOT NULL,
                    old_status VARCHAR(30) NOT NULL,
                    new_status VARCHAR(30) NOT NULL,
                    user_id INT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.executeUpdate();
            System.out.println("Task events table is ready.");

        } catch (Exception e) {
            System.out.println(
                    "Could not create task events table: "
                    + e.getMessage()
            );
        }
    }

    public static boolean saveEvent(
            int taskId,
            String taskTitle,
            String oldStatus,
            String newStatus,
            int userId) {

        String sql = """
                INSERT INTO task_events
                    (task_id, task_title, old_status, new_status, user_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, taskId);
            statement.setString(2, taskTitle);
            statement.setString(3, oldStatus);
            statement.setString(4, newStatus);
            statement.setInt(5, userId);

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println(
                    "Could not save task event: " + e.getMessage()
            );
            return false;
        }
    }

    
    public static void showSavedEvents() {

        String sql = """
                SELECT id, task_id, task_title, old_status,
                       new_status, user_id, created_at
                FROM task_events
                ORDER BY id DESC
                LIMIT 10
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Recent saved task events:");

            while (resultSet.next()) {
                System.out.println(
                        "Event ID: " + resultSet.getInt("id")
                        + ", Task ID: " + resultSet.getInt("task_id")
                        + ", Title: " + resultSet.getString("task_title")
                        + ", Old status: " + resultSet.getString("old_status")
                        + ", New status: " + resultSet.getString("new_status")
                        + ", User ID: " + resultSet.getInt("user_id")
                        + ", Created: " + resultSet.getTimestamp("created_at")
                );
            }

        } catch (Exception e) {
            System.out.println(
                    "Could not read saved task events: " + e.getMessage()
            );
        }
    }


}
