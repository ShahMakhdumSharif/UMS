# University Management System (Spring Boot RBAC & Docker)
**CSE 3230 Home Task - 4th Lab**

A full-featured University Management System built with **Java Spring Boot**, implementing **Role-Based Access Control (RBAC)** across three distinct user roles (`Admin`, `Teacher`, `Student`), comprehensive **REST API CRUD** operations, and containerization via **Dockerfile** and **Docker Compose**.

---

## 📌 Requirements Matrix & Features

### 1. Three User Roles & Exact RBAC Matrix
- 👑 **Admin (`ROLE_ADMIN`)**: Can perform CRUD on **all three resources** (`/api/admins/**`, `/api/teachers/**`, `/api/students/**`).
- 👨‍🏫 **Teacher (`ROLE_TEACHER`)**: Can perform CRUD on **Teachers** (`/api/teachers/**`) and **Students** (`/api/students/**`). Requests to `/api/admins/**` return `403 Forbidden`.
- 🎓 **Student (`ROLE_STUDENT`)**: Can perform CRUD **only on Students** (`/api/students/**`). Requests to `/api/admins/**` or `/api/teachers/**` return `403 Forbidden`.

### 2. Spring Security & Password Hashing
- **BCrypt Password Hashing**: Passwords stored encrypted in the database; plain passwords are never returned in JSON responses.
- **JWT Authentication**: Stateless authentication via `Authorization: Bearer <token>`.
- **Public Endpoints**: `/api/auth/login`, `/api/auth/logout`, static UI files (`/`, `/index.html`, `/css/**`, `/js/**`).

### 3. REST API CRUD Operations & Status Codes
- `GET /api/{admins,teachers,students}` &rarr; `200 OK` (list)
- `GET /api/{admins,teachers,students}/{id}` &rarr; `200 OK` or `404 Not Found`
- `POST /api/{admins,teachers,students}` &rarr; `201 Created` with JSON payload or `400 Bad Request` (validation/duplicate)
- `PUT /api/{admins,teachers,students}/{id}` &rarr; `200 OK` or `404 Not Found`
- `DELETE /api/{admins,teachers,students}/{id}` &rarr; `200 OK` or `404 Not Found`
- Unauthorized requests &rarr; `401 Unauthorized`
- Insufficient permissions &rarr; `403 Forbidden`

### 4. Basic Responsive Web UI
- Served directly at `http://localhost:8080/`.
- 1-Click quick test login buttons for Admin, Teacher, and Student.
- Resource tab navigation displaying real-time data tables with **Add**, **Edit**, and **Delete** modals.
- **Live REST API & RBAC Console**: Shows real-time HTTP Method, Endpoint, Status Code (200, 201, 403, etc.), and JSON response traces.

### 5. Dockerization & Compose
- **`Dockerfile`**: Multi-stage build producing an Eclipse Temurin 21 JRE container.
- **`compose.yaml`**: Multi-container stack orchestration with PostgreSQL 16 database and application service.

---

## 🔑 Pre-Seeded Test Credentials

| Role | Username | Password | Full Name | Email |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Dr. Sarah Jenkins | `admin@university.edu` |
| **Teacher** | `teacher` | `teacher123` | Prof. Robert Langdon | `robert.langdon@university.edu` |
| **Student** | `student` | `student123` | Alice Walker | `alice.walker@university.edu` |

*(Additional sample teachers `prof_elena` and students `stu_bob` are also pre-seeded in the database).*

---

## 🚀 Running the Project

### Option A: Local Execution (H2 In-Memory)
1. Ensure Java 17+ or OpenJDK 21+ is available:
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk ./mvnw clean package -DskipTests
   ```
2. Run the application:
   ```bash
   JAVA_HOME=/opt/homebrew/opt/openjdk java -jar target/myapp-0.0.1-SNAPSHOT.jar
   ```
3. Open your browser at:
   ```
   http://localhost:8080
   ```

### Option B: Running with Docker Compose (PostgreSQL 16)
1. Make sure Docker is running on your machine.
2. Run Docker Compose:
   ```bash
   docker compose up --build
   ```
3. Open your browser at `http://localhost:8080`.
4. To stop the containers:
   ```bash
   docker compose down
   ```

---

## 🧪 Automated Integration Tests

Run the test suite verifying all 7 test cases covering the complete RBAC matrix:
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk ./mvnw clean test
```
All tests verify status codes `200`, `201`, `401`, `403`, and `404`.
