# ScaleMesh: Implementation & Execution Plan

**Project:** ScaleMesh: Cloud-Native Microservices Platform for Elastic Fleet Coordination  
**Architecture Paradigm:** Distributed Cloud-Native Microservices, Polyglot Observability & Infrastructure as Code  
**Target Completion:** Comprehensive Engineering Implementation  

---

## 1. Executive Summary & Strategy

ScaleMesh addresses the problem of elastic fleet coordination through horizontally scalable microservices.
Rather than simulating scale through boolean database toggles, ScaleMesh provides:
1. Six independently deployable microservices orchestrated via an API Gateway.
2. A high-throughput telemetry ingestion pipeline and deterministic trip dispatch engine.
3. A CPU/memory-intensive Demand Analysis service engineered specifically to trigger real Kubernetes Horizontal Pod Autoscaler (HPA) dynamics under fluctuating synthetic load.
4. Production-grade Infrastructure as Code (Terraform for AWS), enterprise CI/CD (Jenkins + GitHub Actions), multi-stage containerization, and Prometheus/Grafana observability.

---

## 2. Phase-by-Phase Roadmap

### Phase 0: Baseline Diagnostics & Environment Discovery [COMPLETED]
- Inventory local tools: Java 17, Maven wrapper 3.10.0, Git 2.53, Docker CLI 29.3, Kubectl 1.35, Minikube 1.38, Terraform 1.15.2, AWS CLI 2.36 (authenticated to account `271598834954`).
- Document existing monolithic Spring Boot application, entity models (`ComputeNode`), MySQL dependency, and initial test failure due to missing database connection.
- Initialize Git repository and establish verification baseline.

### Phase 1: Microservice Modularization & Build Structure
- Establish Maven multi-module root `pom.xml`:
  - `scalemesh-common`: Shared DTOs, domain events, utilities, and exception contracts.
  - `services/api-gateway`: Spring Boot HTTP Reverse Proxy / Router, routing to downstream services with contract validation and aggregate health checks.
  - `services/fleet-telemetry-service`: Ingestion and query service for vehicle telemetry, location coordinates, battery/speed metrics, and historical queries.
  - `services/trip-dispatch-service`: Trip lifecycle manager (REQUESTED, DISPATCHED, IN_TRANSIT, COMPLETED, CANCELLED) with deterministic fleet assignment.
  - `services/route-engine-service`: Route estimation engine calculating distances, durations, waypoints, and traffic multipliers without mock external APIs.
  - `services/demand-analysis-service`: Workload aggregation and heavy analytical computation engine (matrix convolutions / spatial clustering) serving as the primary HPA autoscaling target.
  - `services/incident-manager-service`: Fleet incident tracking, severity levels, automated alerts, and resolution workflows.
- Configure Spring Boot Actuator, Micrometer Prometheus registry, and profile-driven database configuration (H2 for zero-dependency tests/local mode, MySQL for production/Docker).

### Phase 2: Domain Implementation, Persistence & Unit/Integration Tests
- Implement models, repositories, services, controllers, DTOs, and `@RestControllerAdvice` global exception handling in each service.
- Implement structured JSON error responses with standard HTTP status codes (400, 404, 409, 422, 500).
- Preserve existing `ComputeNode` capabilities within fleet telemetry / infrastructure inventory tracking.
- Add comprehensive JUnit 5 and Spring Boot slice tests across all services.
- Execute full `mvn test` build verifying that all tests pass cleanly.

### Phase 3: Multi-Stage Containerization & Docker Compose
- Author production-grade multi-stage `Dockerfile` for each microservice:
  - Stage 1: Build & package JAR with cached Maven layers.
  - Stage 2: Minimal Distroless / Eclipse Temurin JRE runtime with non-root security context (`USER 10001:10001`).
- Create `docker-compose.yml` linking all 6 services, MySQL database, Prometheus, and Grafana.
- Configure healthchecks (`HEALTHCHECK`), container resource limits, and environment variable parameterization.

### Phase 4: Kubernetes Manifests & Orchestration
- Create standard Kubernetes manifests:
  - `Namespace`, `ConfigMap`, and `Secret` definitions.
  - `Deployment` specifications with explicit `resources.requests` and `resources.limits`.
  - HTTP `livenessProbe` and `readinessProbe` targeting `/actuator/health/liveness` and `/actuator/health/readiness`.
  - `Service` definitions (ClusterIP for internal microservices, NodePort / LoadBalancer for API Gateway).
  - `HorizontalPodAutoscaler` (HPA v2) for `demand-analysis-service` targeting 50% CPU utilization.
  - RBAC service accounts and `NetworkPolicy` restricting inter-pod communications.

### Phase 5: Autoscaling Experimentation & Workload Generator
- Develop a Python workload generator (`scripts/workload-generator.py`) capable of:
  - Phase 1: Baseline load (low QPS) — record 1-2 replicas.
  - Phase 2: Stepped surge (high concurrency CPU demand) — observe HPA scale-out to maximum replicas.
  - Phase 3: Cool-down (zero traffic) — observe scale-in after stabilization window.
- Develop `scripts/evaluate-autoscaling.py` to collect replica counts, CPU usage, request latency, and export structured JSON/Markdown reports.

### Phase 6: AWS Infrastructure as Code (Terraform)
- Structure Terraform code:
  - `terraform/vpc.tf`: Custom VPC, public/private subnets, Internet Gateway, NAT Gateway.
  - `terraform/security_groups.tf`: Principle of least privilege ingress/egress rules.
  - `terraform/iam.tf`: IAM roles and policies for EC2/EKS and S3 access.
  - `terraform/s3.tf`: S3 bucket for fleet telemetry archive with server-side encryption and lifecycle rules.
  - `terraform/ec2.tf`: EC2 instances / launch template with user data script to bootstrap Docker/Kubernetes.
- Execute `terraform fmt`, `terraform validate`, and `terraform plan`.
- Present plan outputs, resource count, and FinOps cost risk assessment without executing billable cloud creations.

### Phase 7: CI/CD Pipeline (Jenkins & GitHub Actions)
- Author declarative `Jenkinsfile` with 9 mandatory pipeline stages.
- Author `.github/workflows/ci.yml` providing automated linting, test execution, and image validation on GitHub.
- Document webhook triggers, secret handling, and credential management.

### Phase 8: Full-Stack Observability
- Configure Prometheus (`monitoring/prometheus/prometheus.yml`) with scrape jobs for all 6 microservices.
- Define Prometheus alerts (`monitoring/prometheus/alerts.yml`) for HighErrorRate, ServiceDown, and AutoscalingTriggered.
- Design Grafana dashboard definitions (`monitoring/grafana/dashboards/scalemesh-fleet.json`) with metrics:
  - Request rate, error percentage, p95/p99 latency.
  - Telemetry event volume and active vehicles.
  - Demand calculation intensity and HPA replica scaling curve.

### Phase 9: Unified Web Demonstration Dashboard
- Build an interactive, responsive HTML5/CSS3/JavaScript single-page management dashboard.
- Real-time interaction with the API Gateway endpoints:
  - Live Telemetry ingestion & simulator feed.
  - Trip creation and dispatch board.
  - Route calculation simulator.
  - Real-time incident logger and resolver.
  - Demand load burst generator trigger with live replica count and CPU telemetry chart.

### Phase 10: Complete Technical Documentation Suite
- Deliver all required markdown documents:
  - `README.md`
  - `docs/ARCHITECTURE.md`
  - `docs/SETUP.md`
  - `docs/API.md`
  - `docs/DATABASE.md`
  - `docs/DOCKER.md`
  - `docs/KUBERNETES.md`
  - `docs/AUTOSCALING_EXPERIMENT.md`
  - `docs/TERRAFORM_AWS.md`
  - `docs/CI_CD.md`
  - `docs/OBSERVABILITY.md`
  - `docs/SECURITY_AND_COST.md`
  - `docs/TESTING.md`
  - `docs/KNOWN_LIMITATIONS.md`
  - `docs/VIVA_PREPARATION.md`

### Phase 11: Final Validation, Audit & Verification
- Execute end-to-end tests across the entire codebase.
- Audit all files against the requirements matrix.
- Generate final completion artifact.
