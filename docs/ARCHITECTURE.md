# ScaleMesh: System Architecture & Technical Specification

## 1. Architectural Philosophy
ScaleMesh is structured around distributed cloud-native microservices principles:
- **Single Responsibility Principle (SRP):** Each microservice has an isolated boundary and its own lifecycle.
- **Stateless Core Services:** The Route Engine and Demand Analysis services are completely stateless, enabling horizontal scaling without cache synchronization bottlenecks.
- **Database-per-Service Pattern:** Fleet Telemetry, Trip Dispatch, and Incident Manager maintain dedicated database schemas (`scalemesh_telemetry`, `scalemesh_trips`, `scalemesh_incidents`).
- **Resilient Reverse Proxy:** The API Gateway decouples external consumer clients from internal cluster networking, providing single-point routing, validation, and composite health checking.

## 2. Microservices Breakdown

### 2.1 API Gateway (`:8080`)
- **Technology:** Spring Boot 3 Web, RestTemplate, Actuator.
- **Responsibilities:**
  - Routes external requests (`/api/telemetry/**`, `/api/nodes/**`, `/api/trips/**`, `/api/routes/**`, `/api/demand/**`, `/api/incidents/**`).
  - Aggregates health status from downstream microservices under `/api/system/health`.
  - Normalizes and sanitizes HTTP error responses via `@RestControllerAdvice`.

### 2.2 Fleet Telemetry Service (`:8082`)
- **Technology:** Spring Boot 3 Data JPA, MySQL 8 / H2, Micrometer Prometheus.
- **Responsibilities:**
  - High-throughput ingestion of vehicle telemetry data (`vehicleId`, `latitude`, `longitude`, `speed`, `heading`, `batteryLevel`, `status`).
  - Historical telemetry queries and aggregated fleet statistics.
  - Legacy `ComputeNode` registration, status modification (`RUNNING`/`STOPPED`), and lifecycle tracking.

### 2.3 Trip Dispatch Service (`:8083`)
- **Technology:** Spring Boot 3 Data JPA, MySQL 8 / H2, Micrometer.
- **Responsibilities:**
  - Manages the finite state machine for trip lifecycle:
    `REQUESTED` -> `DISPATCHED` -> `IN_TRANSIT` -> `COMPLETED` / `CANCELLED`.
  - Deterministic vehicle assignment algorithm.
  - Haversine distance and duration calculation via `GeoUtils`.

### 2.4 Route Engine Service (`:8084`)
- **Technology:** Spring Boot 3 Web, Stateless Math Engine.
- **Responsibilities:**
  - Computes optimal point-to-point routes, calculating distance (km), estimated time (min), and waypoints based on traffic multipliers.
  - Eliminates reliance on brittle external third-party mapping APIs.

### 2.5 Demand Analysis Service (`:8085`)
- **Technology:** Spring Boot 3 Web, Multi-threaded CPU Convolution Engine.
- **Responsibilities:**
  - Simulates spatial heatmaps and demand indices across urban clusters.
  - Houses a configurable CPU burn loop designed specifically to stress CPU utilization to trigger the Kubernetes Horizontal Pod Autoscaler (HPA).

### 2.6 Incident Manager Service (`:8086`)
- **Technology:** Spring Boot 3 Data JPA, MySQL 8 / H2.
- **Responsibilities:**
  - Fleet anomaly logging and incident triage (`MECHANICAL_FAILURE`, `BATTERY_DEPLETED`, `COLLISION`, `SENSOR_MALFUNCTION`).
  - Severity level tracking (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
  - Workflow transitions from `REPORTED` to `INVESTIGATING` to `RESOLVED`.
