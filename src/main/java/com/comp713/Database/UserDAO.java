package com.comp713.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static List<String[]> getAllUsers() {

        List<String[]> users = new ArrayList<>();

        String sql = "SELECT id, username, email FROM users";

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                String[] user = {
                    String.valueOf(resultSet.getInt("id")),
                    resultSet.getString("username"),
                    resultSet.getString("email")
                };

                users.add(user);
            }

        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
        }

        return users;
    }

    public static boolean createUser(String username, String email) {

        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, email);

            statement.executeUpdate();

            return true;

        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
}