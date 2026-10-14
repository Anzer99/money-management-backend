# Money Management System Backend

Spring Boot REST API for the Money Management System project. It supports account signup and login, and user-specific income and expense transactions. Passwords are stored as BCrypt hashes.

## Requirements

- Java 25 or newer
- MySQL 8

## Run locally on Windows

1. In MySQL, create the database: `CREATE DATABASE money_management;`
2. Open Command Prompt in this project folder.
3. Set the MySQL account details in that window:
   - `set DB_USERNAME=root`
   - `set DB_PASSWORD=your-MySQL-password`
4. Start the server with `mvnw.cmd spring-boot:run`.

The API runs on `http://localhost:8080`. Hibernate creates or updates the tables in `money_management` when the app starts.

Do not commit database passwords. Set them as environment variables when running or deploying the backend.
