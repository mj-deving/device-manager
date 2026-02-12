# Device Manager

Multi-module project: Spring Boot REST API + JavaFX Desktop Client for device management.

## Project 2 - Learning Goals

Build enterprise skills:
- Spring Boot REST API development
- Spring Data JPA and ORM concepts
- JavaFX GUI development
- Multi-module Maven projects
- Client-Server architecture
- Async/concurrent programming

## Tech Stack

**Server:**
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI

**Client:**
- JavaFX 21
- HTTP Client
- GSON (JSON processing)

## Modules

### device-manager-server

REST API providing CRUD operations for devices.

**Endpoints:**
- `GET /api/v1/devices` - List devices with pagination
- `GET /api/v1/devices/{id}` - Get device details
- `POST /api/v1/devices` - Create device
- `PUT /api/v1/devices/{id}` - Update device
- `DELETE /api/v1/devices/{id}` - Delete device
- `GET /api/v1/devices/{id}/logs` - Device event logs
- `GET /api/v1/stats` - Statistics and aggregations

**Build & Run:**
```bash
cd device-manager-server
mvn clean package
java -jar target/device-manager-server-1.0.0.jar
```

### device-manager-client

JavaFX desktop application consuming the REST API.

**Features:**
- Device list with TableView
- Create/Edit dialogs
- Status filtering
- Real-time status updates
- Error handling and offline mode

**Build & Run:**
```bash
cd device-manager-client
mvn clean javafx:run
```

Or package:
```bash
mvn clean package
java -jar target/device-manager-client-1.0.0.jar
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 16

### Build All Modules

```bash
mvn clean package
```

### Run Locally

Terminal 1 (Server):
```bash
cd device-manager-server
mvn spring-boot:run
```

Terminal 2 (Client):
```bash
cd device-manager-client
mvn javafx:run
```

## Project Structure

```
device-manager/
├── device-manager-server/
│   ├── src/main/java/com/mj/portfolio/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── config/
│   └── src/main/resources/
│       └── application.yml
├── device-manager-client/
│   ├── src/main/java/com/mj/portfolio/
│   │   ├── client/
│   │   ├── service/
│   │   ├── ui/
│   │   └── model/
│   └── src/main/resources/
│       └── fxml/
├── pom.xml (parent)
└── README.md
```

## CI/CD

GitLab CI/CD pipeline:
1. Build both modules
2. Run unit tests
3. Generate test reports
4. Deploy server to VPS
5. Package client

## Next Steps

After this project:
- ✓ REST API design and Spring Boot mastery
- ✓ JavaFX GUI programming
- ✓ Client-Server communication
- ✓ Multi-module Maven projects
- ✓ Foundation for Project 3 (Web dashboard for same API)
