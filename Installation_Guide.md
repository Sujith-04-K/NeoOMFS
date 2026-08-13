# NeoOMFS Installation & Setup Guide

This document details the configuration instructions to execute the NeoOMFS Android and Spring Boot modules on a local developer machine.

---

## Technical Prerequisites
Ensure you have the following frameworks installed:

- **Android Studio** (Koala / Ladybug or newer)
- **JDK 21** (Required for Spring Boot 3.x backend build execution)
- **Maven 3.8+** (Java package manager)
- **MySQL Server 8.x** (Relational database engine)

---

## 1. Database Setup (MySQL Server)
1. Launch the MySQL CLI or a UI administrator tool (e.g. MySQL Workbench).
2. Execute the schema script located at `database/schema.sql` to initialize databases and configure relation tables:
   ```bash
   mysql -u root -p < database/schema.sql
   ```
3. Load the default clinical triage dataset by executing the seed script:
   ```bash
   mysql -u root -p < database/seed.sql
   ```
4. Verify that tables (`users`, `patients`, `patient_vitals`, etc.) are created in the `neoomfs` database.

---

## 2. Backend Setup (Spring Boot)
1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```
2. Open `src/main/resources/application.properties` and verify your MySQL connection string, username, and password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/neoomfs?useSSL=false
   spring.datasource.username=root
   spring.datasource.password=root1234
   ```
3. Compile and build the Spring Boot executable JAR file using Maven:
   ```bash
   mvn clean package
   ```
4. Run the Spring Boot API service:
   ```bash
   mvn spring-boot:run
   ```
5. Confirm the server launches successfully on `http://localhost:8080/api/v1`.

---

## 3. Frontend Setup (Android Studio)
1. Open **Android Studio**.
2. Select **Open File or Project** and choose the `frontend/` directory (ensure you select `frontend/` and not the workspace root).
3. Wait for the Gradle project sync process to compile.
4. Verify your local Java Compiler configurations in Android Studio:
   - Navigate to **Settings / Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle**.
   - Verify that **Gradle JDK** is configured to point to a valid JDK installation matching Java 17+.
5. Connect an Android Emulator or physical device (with USB debugging enabled).
6. Press the **Run** button (or compile via CLI using `.\gradlew assembleDebug`) to install the application.
