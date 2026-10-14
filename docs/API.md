# ScaleMesh: API Reference Specification

All endpoints are accessible via the **ScaleMesh API Gateway** (`http://localhost:8080`) or directly on the individual microservice ports.

---

## 1. System Health & Gateway
- `GET /api/system/health`: Aggregated platform health status across all downstream microservices.
- `GET /actuator/health/readiness`: Kubernetes readiness probe.
- `GET /actuator/health/liveness`: Kubernetes liveness probe.
- `GET /actuator/prometheus`: Prometheus scrape endpoint.

---

## 2. Compute Nodes Lifecycle (Core Requirement A)
- `POST /api/nodes`
  ```json
  { "name": "worker-node-1", "cpuCores": 4, "memoryGb": 8 }
  ```
- `GET /api/nodes`: List all registered compute nodes.
- `GET /api/nodes/{id}`: Retrieve a specific compute node by UUID.
- `PUT /api/nodes/{id}`: Update compute node configuration.
- `PUT /api/nodes/{id}/start`: Transition status to `RUNNING`.
- `PUT /api/nodes/{id}/stop`: Transition status to `STOPPED`.
- `DELETE /api/nodes/{id}`: Remove compute node registration.

---

## 3. Fleet Telemetry Ingestion
- `POST /api/telemetry`
  ```json
  {
    "vehicleId": "FLEET-VEH-101",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "speed": 45.5,
    "heading": 180.0,
    "batteryLevel": 88.0,
    "status": "ACTIVE"
  }
  ```
- `GET /api/telemetry/stats`: Overview of total active vehicles, average speed, and battery health.
- `GET /api/telemetry/vehicle/{vehicleId}`: Historical telemetry events for a vehicle.

---

## 4. Trip Dispatch Service
- `POST /api/trips`
  ```json
  {
    "passengerId": "PASS-88",
    "pickupLat": 37.7749,
    "pickupLng": -122.4194,
    "dropoffLat": 37.7891,
    "dropoffLng": -122.4014,
    "priority": "HIGH",
    "passengerCount": 2
  }
  ```
- `POST /api/trips/{tripId}/dispatch`: Assigns fleet vehicle and transitions status to `DISPATCHED`.
- `PUT /api/trips/{tripId}/status?status=IN_TRANSIT`: Advances trip to transit.
- `PUT /api/trips/{tripId}/status?status=COMPLETED`: Completes trip.
- `GET /api/trips`: List paginated trips.
- `GET /api/trips/stats`: Summary counts by status.

---

## 5. Route Engine Service
- `POST /api/routes/calculate`
  ```json
  {
    "originLat": 37.7749,
    "originLng": -122.4194,
    "destLat": 37.7891,
    "destLng": -122.4014
  }
  ```
  Returns distance in km, duration in minutes, and generated route waypoints.

---

## 6. Demand Analysis Service (HPA Scaling Target)
- `POST /api/demand/calculate`
  ```json
  {
    "regionId": "downtown-core",
    "gridSize": 40,
    "iterationCount": 30000,
    "cpuBurnMs": 100,
    "intensityLevel": 3
  }
  ```
- `GET /api/demand/latest`: Returns the most recently computed spatial demand index.
- `GET /api/demand/metrics`: Returns concurrency and latency telemetry.

---

## 7. Incident Manager Service
- `POST /api/incidents`
  ```json
  {
    "vehicleId": "FLEET-VEH-101",
    "type": "BATTERY_DEPLETED",
    "severity": "HIGH",
    "description": "Battery dropped below 5% in transit",
    "latitude": 37.7749,
    "longitude": -122.4194
  }
  ```
- `PUT /api/incidents/{id}/resolve`: Resolves the incident.
- `GET /api/incidents`: List incidents (filterable by status/severity).
