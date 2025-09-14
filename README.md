# 📂 Paste Sharing Service

A **Spring Boot** based service that lets users securely upload, manage, and share files (like Pastebin but extended).  
It supports **user management, folder organization, secure/expiring file links, once-view pastes, and caching** for performance.

---

## 🚀 Features

### 🔑 User Management
- Create and update users.
- Username availability check with **Bloom Filter**.
- Password hashing with **BCrypt**.
- User profile retrieval with all folders/files.

### 📁 Folder Management
- Create, update, and delete folders.
- Associate files with folders.
- Retrieve folder data and mappings.

### 📄 File Management
- Upload and update files.
- Generate **unique sharable link** (`/file/get/{endpoint}`).
- Support for:
  - 🔒 **Password-protected files**
  - ⏳ **Expiring files** (auto-deletion via scheduler)
  - 👁 **Once-view files** (deleted after first access)
- Delete files anytime.

### 🗑 Expired File Cleanup
- **Scheduler runs every midnight** (`@Scheduled(cron = "0 0 0 * * *")`)
- Deletes expired files from DB.

### ⚡ Performance & Security
- **Hazelcast cache** for quick access to users, files, and folders.
- **Bloom Filter** for fast username uniqueness checks.
- **DTO mapping** for clean responses.
- Centralized **utility classes** for string, time, and password handling.

---

## 🛠 Tech Stack

- **Spring Boot 3+**
- **Spring Data JPA (Hibernate)**
- **Hazelcast** (caching)
- **Bloom Filter** (Guava / custom impl)
- **Spring Scheduler** (cleanup jobs)
- **Spring Security (BCrypt)** for passwords
- **SLF4J / Logback** for logging
- **H2 / MySQL / PostgreSQL** (pluggable persistence)

---

## 📂 Project Structure
src/main/java/com/example/learning
│
├── entity/ # Entities (User, File, Folder)
├── dto/ # DTOs (UserDTO, FileResponse, FolderDTO, etc.)
├── repository/ # Repositories (JpaRepository interfaces)
├── service/ # Business services
│ ├── FileService.java
│ ├── FolderService.java
│ ├── UserService.java
│ └── CacheService.java
├── scheduler/ # Scheduled cleanup tasks
│ ├── PasteCleanupScheduler.java
│ └── SchedulerService.java
├── utility/ # Helper utilities
│ ├── StringUtils.java
│ ├── TimeUtils.java
│ └── GenericUtil.java
└── config/ # Hazelcast, Cache, Security configs

---

## ⚡ How It Works

1. **File Upload**
   - User uploads file → gets unique endpoint link.
   - If secured, password is hashed before saving.
   - Expiration time (if provided) is stored.

2. **File Retrieval**
   - Normal file → returns data.
   - Password-protected → asks for password before serving.
   - Once-view → deletes immediately after retrieval.

3. **Scheduler**
   - Every midnight → expired files auto-deleted.

4. **Usernames**
   - Bloom Filter ensures no duplicate usernames.
   - Cached in Hazelcast for fast lookup.

---

## 🔗 API Overview

### File APIs
- `POST /file` → Upload file
- `PUT /file/{id}` → Update file
- `GET /file/{endpoint}` → Retrieve file (normal or secured)
- `DELETE /file/{id}` → Delete file

### Folder APIs
- `POST /folder` → Create folder
- `PUT /folder/{id}` → Update folder
- `DELETE /folder/{id}` → Delete folder
- `GET /folder/{id}` → Get folder data

### User APIs
- `POST /user` → Create user
- `PUT /user/{id}` → Update user
- `GET /user/{id}` → Get user details
- `GET /user/{id}/folders` → Get user’s folders

---

## ⚙️ Setup & Run

### Prerequisites
- JDK 17+
- Maven/Gradle
- (Optional) Docker for DB

### Steps
```bash
# Clone the repository
git clone https://github.com/your-username/paste-sharing-service.git
cd paste-sharing-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

---

### **🧹 Scheduled Tasks**

A daily scheduled task runs at **midnight (00:00)** to maintain data hygiene. This task is responsible for deleting all expired pastes from the system.

```java
@Scheduled(cron = "0 0 0 * * *")
public void deleteExpiredPastes() {
    schdulerService.deleteExpiredFiles();
}
```

---

## 🛡️ Security Notes

Security is a top priority, and the following measures have been implemented:

- **Password Storage**: All passwords are hashed with BCrypt to ensure they are not stored in a readable format.
- **Secure Endpoints**: Links to shared pastes are generated using UUID endpoints, making them unpredictable and difficult to guess.
- **Data Leak Prevention**: Sensitive data leaks are prevented by using expiring and once-view pastes.
- **Logging Policy**: The application does not log sensitive data such as passwords.

---

## 🧩 Future Improvements

Several enhancements are planned to improve the service:

- Implement JWT-based authentication for secure user sessions.
- Add pagination to file and folder listings to improve performance with large datasets.
- Integrate cloud storage solutions (e.g., S3, GCP) for scalable and reliable file storage.
- Support file previews for common file types.

---

## 📜 License

This project is released under the MIT License.

© 2025
