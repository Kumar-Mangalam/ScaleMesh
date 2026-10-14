# ScaleMesh: Environment and Toolchain Diagnostics

**Document Version:** 1.0.0  
**Timestamp:** 2026-10-12  
**Platform Host:** Windows 11 Enterprise / Pro (amd64)  
**Shell Environment:** PowerShell 5.1 / PowerShell 7  

---

## 1. Toolchain Inventory & Baseline State

| Component / Tool | Version Detected | Availability Status | Notes & Capabilities |
| :--- | :--- | :--- | :--- |
| **Java JDK** | Eclipse Temurin 25.0.4.1 LTS | **Available** | Java 25 LTS active; Spring Boot 3.5.16 supports Java 25. |
| **Maven Wrapper** | Apache Maven 3.10.0 (`mvnw.cmd`) | **Available** | Project-embedded wrapper configured and operational. |
| **Git CLI** | 2.53.0.windows.2 | **Available** | Local and remote git repository management verified. |
| **Docker Engine** | Docker 29.3.0 | **Installed / Initializing** | Docker Desktop 29.3.0 installed; engine process initiated. |
| **Docker Compose** | v5.1.0 | **Available** | Compose CLI available for multi-service stack orchestration. |
| **Kubectl CLI** | v1.35.3 (Client) | **Available** | K8s client installed with Kustomize v5.7.1 built-in. |
| **Minikube** | v1.38.1 | **Available** | Minikube local cluster orchestrator installed. |
| **Helm** | N/A | **Not on PATH** | Standard manifests & Kustomize preferred for cluster management. |
| **Terraform CLI** | v1.15.2 (windows_amd64) | **Available** | HashiCorp Terraform ready for AWS IaC synthesis & validation. |
| **AWS CLI** | 2.36.38 (Python 3.14.6) | **Available** | Configured for AWS account and region queries. |
| **Jenkins** | N/A (Local CLI not in PATH) | **Configured via CI Pipeline** | CI/CD definitions managed via `Jenkinsfile` for agent execution. |

---

## 2. Existing Workspace Inspection

- **Legacy Core Application:** `ScalemeshApplication` (single-service Spring Boot app).
- **Existing Persistence:** MySQL (`jdbc:mysql://localhost:3307/scalemesh_db`), Hibernate JPA 7.4.5, HikariCP.
- **Legacy Entities:** `ComputeNode` (fields: `id`, `name`, `status`, `cpuCores`, `memoryGb`).
- **Status Lifecycle:** `STOPPED` <-> `RUNNING` status transitions on individual compute nodes.
- **REST Endpoints:**
  - `POST /api/nodes` (Create node)
  - `GET /api/nodes` (List nodes)
  - `GET /api/nodes/{id}` (Get node)
  - `PUT /api/nodes/{id}` (Update node)
  - `PUT /api/nodes/{id}/start` (Set status to RUNNING)
  - `PUT /api/nodes/{id}/stop` (Set status to STOPPED)
  - `DELETE /api/nodes/{id}` (Delete node)
  - `GET /api/health` (Health probe string)
- **Validation & Errors:** `CreateNodeRequest` with Bean Validation (`@NotBlank`, `@Min`, `@Max`, `@Size`), handled by `GlobalExceptionHandler`.
- **Target Architectural Evolution:** Transition from monolithic node status toggle to the full **Elastic Fleet Coordination Microservices Architecture** consisting of 6 dedicated microservices:
  1. API Gateway
  2. Fleet Telemetry Service
  3. Trip Dispatch Service
  4. Route Engine Service
  5. Demand Analysis Service (Elastic HPA workload generator target)
  6. Incident Manager Service

---

## 3. Execution Safety & Credential Boundary

- In accordance with authoritative requirements:
  - Local operations (Java builds, unit tests, integration tests, containerization, local cluster manifests) will be executed autonomously and verified.
  - Terraform configurations will be linted (`terraform fmt`), validated (`terraform validate`), and planned (`terraform plan`).
  - No remote billable AWS resources will be applied without human review.
  - No secrets, tokens, or plaintext credentials are to be stored in git repository artifacts.
