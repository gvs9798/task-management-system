package com.comp713.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public static List<String[]> getAllTasks() {

        List<String[]> tasks = new ArrayList<>();

        String sql = """
                SELECT id, title, description, status, user_id
                FROM tasks
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                String[] task = {
                    String.valueOf(resultSet.getInt("id")),
                    resultSet.getString("title"),
                    resultSet.getString("description"),
                    resultSet.getString("status"),
                    String.valueOf(resultSet.getInt("user_id"))
                };

                tasks.add(task);
            }

        } catch (Exception e) {
            System.out.println("Error getting tasks: " + e.getMessage());
        }

        return tasks;
    }


    public static boolean createTask(
            String title,
            String description,
            int userId) {

        String sql = """
                INSERT INTO tasks (title, description, status, user_id)
                VALUES (?, ?, 'TODO', ?)
                """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, title);
            statement.setString(2, description);
            statement.setInt(3, userId);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            System.out.println("Error creating task: " + e.getMessage());
            return false;
        }
    }
    public static boolean updateTask(
        int taskId,
        String title,
        String description,
        String status) {

    String sql = """
            UPDATE tasks
            SET title = ?, description = ?, status = ?
            WHERE id = ?
            """;

    try (Connection connection = Database.connect();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, title);
        statement.setString(2, description);
        statement.setString(3, status);
        statement.setInt(4, taskId);

        int rowsUpdated = statement.executeUpdate();

        return rowsUpdated > 0;

    } catch (Exception e) {
        System.out.println("Error updating task: " + e.getMessage());
        return false;
        }
    }
    public static boolean deleteTask(int taskId) {

    String sql = "DELETE FROM tasks WHERE id = ?";

    try (Connection connection = Database.connect();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, taskId);

        int rowsDeleted = statement.executeUpdate();

        return rowsDeleted > 0;

    } catch (Exception e) {
        System.out.println("Error deleting task: " + e.getMessage());
        return false;
    }
}
}