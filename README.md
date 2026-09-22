# Task Management System

COMP713 Mobile Systems – Individual Project

## Overview

This project is a distributed task management web application developed using Java, Maven, JavaScript, HTML, CSS, and MySQL.

The application allows users to create, view, update, and delete tasks. Tasks are associated with users through a foreign-key relationship in the MySQL database.

## Technologies Used

- Java 17
- Maven 3.9.16
- MySQL 8.4
- HTML
- CSS
- JavaScript
- JDBC
- Java HTTP Server
- JUnit 5
- Visual Studio Code

## Architecture

The application uses a simple client-server architecture:

Client (HTML/CSS/JavaScript)
        |
        | HTTP requests
        v
Java HTTP Server
        |
        | JDBC / SQL
        v
MySQL Database

The client communicates with the Java server using HTTP requests. The server processes API requests and uses JDBC to communicate with the MySQL database.

## Database

The application uses two related tables:

### Users

- `id`
- `username`
- `email`

### Tasks

- `id`
- `title`
- `description`
- `status`
- `user_id`

Each task belongs to a user through the `user_id` foreign key.

## API Endpoints

### Users

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | Retrieve all users |
| POST | `/api/users/create` | Create a new user |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks` | Retrieve all tasks |
| POST | `/api/tasks/create` | Create a task |
| PUT | `/api/tasks/update` | Update a task |
| DELETE | `/api/tasks/delete` | Delete a task |

## Validation and Error Handling

The server performs basic input validation.

Examples include:

- Empty task title or description returns HTTP 400.
- Invalid task status returns HTTP 400.
- Updating or deleting a task that does not exist returns HTTP 404.

Valid task statuses are:

- `TODO`
- `IN_PROGRESS`
- `DONE`

## Database Configuration

The application uses environment variables for database configuration:

- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_URL`

The database URL uses MySQL SSL mode:

`sslMode=REQUIRED`

The database password is not stored in the source code.

## Running the Application

### 1. Configure database environment variables

In PowerShell:

```powershell
$env:DB_USERNAME="YOUR_DATABASE_USERNAME"
$env:DB_URL="YOUR_DATABASE_URL"
$env:DB_PASSWORD="YOUR_DATABASE_PASSWORD"
