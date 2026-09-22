package com.comp713;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import com.comp713.Database.UserDAO;
import com.comp713.Database.TaskDAO;

public class Server {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0);


        // GET /api/users
        server.createContext("/api/users", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<String[]> users = UserDAO.getAllUsers();

            StringBuilder response = new StringBuilder("[");

            for (int i = 0; i < users.size(); i++) {

                String[] user = users.get(i);

                response.append("""
                        {
                            "id": %s,
                            "username": "%s",
                            "email": "%s"
                        }
                        """.formatted(
                            user[0],
                            user[1],
                            user[2]
                        ));

                if (i < users.size() - 1) {
                    response.append(",");
                }
            }

            response.append("]");

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");

            exchange.sendResponseHeaders(
                    200,
                    response.toString().getBytes().length
            );

            exchange.getResponseBody().write(
                    response.toString().getBytes()
            );

            exchange.getResponseBody().close();
        });


        // POST /api/users/create
        server.createContext("/api/users/create", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(
                    exchange.getRequestBody().readAllBytes()
            );

            String username = body
                    .split("\"username\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            String email = body
                    .split("\"email\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            if (username.isBlank() || email.isBlank()) {

                String response = """
                        {"error":"Username and email are required"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);

                exchange.getResponseBody().write(
                        response.getBytes());

                exchange.getResponseBody().close();

                return;
            }

            boolean created = UserDAO.createUser(username, email);

            String response;

            if (created) {

                response = """
                        {"message":"User created successfully"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        201,
                        response.getBytes().length);

            } else {

                response = """
                        {"error":"Could not create user"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);
            }

            exchange.getResponseBody().write(
                    response.getBytes());

            exchange.getResponseBody().close();
        });


        // PUT /api/tasks/update
        server.createContext("/api/tasks/update", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(
                    exchange.getRequestBody().readAllBytes()
            );

            String taskIdText = body
                    .split("\"id\"\\s*:\\s*")[1]
                    .split("[,}]")[0]
                    .trim();

            String title = body
                    .split("\"title\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            String description = body
                    .split("\"description\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            String status = body
                    .split("\"status\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            int taskId = Integer.parseInt(taskIdText);

            if (title.isBlank()
                    || description.isBlank()
                    || status.isBlank()) {

                String response = """
                        {"error":"Title, description and status are required"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);

                exchange.getResponseBody().write(
                        response.getBytes());

                exchange.getResponseBody().close();

                return;
            }

            // Validate task status
            if (!status.equals("TODO")
                    && !status.equals("IN_PROGRESS")
                    && !status.equals("DONE")) {

                String response = """
                        {"error":"Status must be TODO, IN_PROGRESS, or DONE"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);

                exchange.getResponseBody().write(
                        response.getBytes());

                exchange.getResponseBody().close();

                return;
            }

            boolean updated = TaskDAO.updateTask(
                    taskId,
                    title,
                    description,
                    status
            );

            String response;

            if (updated) {

                response = """
                        {"message":"Task updated successfully"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        200,
                        response.getBytes().length);

            } else {

                response = """
                        {"error":"Task not found or could not be updated"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        404,
                        response.getBytes().length);
            }

            exchange.getResponseBody().write(
                    response.getBytes());

            exchange.getResponseBody().close();
        });


        // POST /api/tasks/create
        server.createContext("/api/tasks/create", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(
                    exchange.getRequestBody().readAllBytes()
            );

            String title = body
                    .split("\"title\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            String description = body
                    .split("\"description\"\\s*:\\s*\"")[1]
                    .split("\"")[0];

            String userIdText = body
                    .split("\"user_id\"\\s*:\\s*")[1]
                    .split("[,}]")[0]
                    .trim();

            int userId = Integer.parseInt(userIdText);

            if (title.isBlank() || description.isBlank()) {

                String response = """
                        {"error":"Title and description are required"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);

                exchange.getResponseBody().write(
                        response.getBytes());

                exchange.getResponseBody().close();

                return;
            }

            boolean created = TaskDAO.createTask(
                    title,
                    description,
                    userId
            );

            String response;

            if (created) {

                response = """
                        {"message":"Task created successfully"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        201,
                        response.getBytes().length);

            } else {

                response = """
                        {"error":"Could not create task"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        400,
                        response.getBytes().length);
            }

            exchange.getResponseBody().write(
                    response.getBytes());

            exchange.getResponseBody().close();
        });


        // DELETE /api/tasks/delete
        server.createContext("/api/tasks/delete", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(
                    exchange.getRequestBody().readAllBytes()
            );

            String taskIdText = body
                    .split("\"id\"\\s*:\\s*")[1]
                    .split("[,}]")[0]
                    .trim();

            int taskId = Integer.parseInt(taskIdText);

            boolean deleted = TaskDAO.deleteTask(taskId);

            String response;

            if (deleted) {

                response = """
                        {"message":"Task deleted successfully"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        200,
                        response.getBytes().length);

            } else {

                response = """
                        {"error":"Task not found or could not be deleted"}
                        """;

                exchange.getResponseHeaders().set(
                        "Content-Type", "application/json");

                exchange.sendResponseHeaders(
                        404,
                        response.getBytes().length);
            }

            exchange.getResponseBody().write(
                    response.getBytes());

            exchange.getResponseBody().close();
        });


        // GET /api/tasks
        server.createContext("/api/tasks", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<String[]> tasks = TaskDAO.getAllTasks();

            StringBuilder response = new StringBuilder("[");

            for (int i = 0; i < tasks.size(); i++) {

                String[] task = tasks.get(i);

                response.append("""
                        {
                            "id": %s,
                            "title": "%s",
                            "description": "%s",
                            "status": "%s",
                            "user_id": %s
                        }
                        """.formatted(
                            task[0],
                            task[1],
                            task[2],
                            task[3],
                            task[4]
                        ));

                if (i < tasks.size() - 1) {
                    response.append(",");
                }
            }

            response.append("]");

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");

            exchange.sendResponseHeaders(
                    200,
                    response.toString().getBytes().length
            );

            exchange.getResponseBody().write(
                    response.toString().getBytes()
            );

            exchange.getResponseBody().close();
        });


        // Serve the frontend
        server.createContext("/", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {

                var inputStream = Server.class
                        .getResourceAsStream("/index.html");

                if (inputStream == null) {

                    String response = "index.html not found";

                    exchange.sendResponseHeaders(
                            404,
                            response.getBytes().length);

                    exchange.getResponseBody().write(
                            response.getBytes());

                    exchange.getResponseBody().close();

                    return;
                }

                byte[] response = inputStream.readAllBytes();

                exchange.getResponseHeaders()
                        .set("Content-Type", "text/html");

                exchange.sendResponseHeaders(
                        200,
                        response.length);

                exchange.getResponseBody().write(response);

                exchange.getResponseBody().close();

            } catch (Exception e) {

                String response = "Error loading frontend";

                exchange.sendResponseHeaders(
                        500,
                        response.getBytes().length);

                exchange.getResponseBody().write(
                        response.getBytes());

                exchange.getResponseBody().close();
            }
        });


        server.start();

        System.out.println(
                "Server started on http://localhost:8080");
    }
}