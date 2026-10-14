# ScaleMesh — Cloud-Native Fleet Coordination Platform

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Java 17](https://img.shields.io/badge/Java-17%20LTS-blue.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-green.svg)]()
[![Kubernetes](https://img.shields.io/badge/Kubernetes-v1.35%20Ready-326CE5.svg)]()
[![Terraform](https://img.shields.io/badge/Terraform-1.15.2%20Validated-7B42BC.svg)]()

**Prerequisite:** JDK 17 LTS (or higher) is required to build and run the Java services.

> **Final-Year B.Tech Computer Science & Engineering Capstone Project**  
> **Student:** Kumar Mangalam (Registration Number: 12318090)  
> **Supervisor:** Ajay Kumar Badhan  
> **Department & University:** Department of Computer Science and Engineering, Lovely Professional University  
> **Domain:** Distributed Systems, Cloud-Native Microservices, Elastic Autoscaling, Infrastructure as Code & Observability.

---

## 1. Project Overview

**ScaleMesh** is an enterprise-grade, cloud-native microservices platform engineered to solve the challenges of **elastic fleet coordination, distributed vehicle telemetry ingestion, deterministic trip dispatching, and dynamic workload-driven horizontal autoscaling**.

Unlike monolithic fleet tracking systems or theoretical projects with mock databases, ScaleMesh features:
1. **Six independently deployable Spring Boot 3 microservices** coordinated through an API Gateway reverse proxy (Port 8081 / NodePort 30081).
2. **Dual-profile persistence architecture**: zero-dependency in-memory H2 for autonomous testing and production MySQL with connection pooling.
3. **True workload-driven Horizontal Pod Autoscaling (HPA v2)** using spatial matrix convolutions that generate real CPU burn to trigger Kubernetes pod replica scaling (configured: 1 to 10 pods; empirically tested: 1 to 8 pods).
4. **Lightweight containerization** using Alpine Linux JRE images and non-root security contexts (`USER 10001:10001`).
5. **AWS Infrastructure as Code (Terraform)** providing VPC, private/public subnets, security groups, IAM least-privilege roles, S3 telemetry archives, and EC2 compute nodes (dry-run validated; zero cloud billing).
6. **Observability architecture** with Prometheus metric scrapers, alert rules, and pre-provisioned Grafana monitoring dashboards (configuration validated; runtime containers offline to avoid host port collisions).
7. **Complete CI/CD pipelines** featuring Jenkins declarative pipelines and GitHub Actions automated testing workflows (pipeline definitions validated).
8. **Interactive single-page web dashboard** for live fleet telemetry visualization, trip lifecycle tracking, and on-demand autoscaling stress testing (NodePort 30081).

---

## 2. Architecture Diagram

```
                              +--------------------------+
                              |   Client Web Browser /   |
                              | Workload Generator (Py)  |
                              +-------------+------------+
                                            | HTTP :30081 / :8081
                                            v
                              +--------------------------+
                              |    ScaleMesh Gateway     |
                              |  (:8081, NodePort 30081) |
                              +-------------+------------+
                                            |
         +------------------+---------------+------------------+------------------+
         |                  |                                  |                  |
         v                  v                                  v                  v
+-----------------+ +-----------------+              +-----------------+ +-----------------+
| Fleet Telemetry | |  Trip Dispatch  |              |  Route Engine   | | Incident Manager|
|     (:8082)     | |     (:8083)     |              |     (:8084)     | |     (:8086)     |
+--------+--------+ +--------+--------+              +-----------------+ +--------+--------+
         |                   |                                                    |
         +---------+---------+----------------------------------------------------+
                   |
                   v
         +-----------------+                         +-------------------------------------+
         |      MySQL      |                         |       Demand Analysis Service       |
         |     (:3306)     |                         |                (:8085)              |
         +-----------------+                         |  * Algorithmic Matrix Convolution   |
                                                     |  * CPU Burn Stress Engine           |
                                                     |  * Target of K8s HPA (1->10, test:8)|
                                                     +------------------+------------------+
                                                                        |
                                                     +------------------v------------------+
                                                     |    Kubernetes HPA Controller        |
                                                     |   (Target: 50% CPU Utilization)     |
                                                     +-------------------------------------+
```

---

## 3. Microservices Inventory

| Microservice | Port | Description & Responsibilities | Tech Stack & Persistence |
| :--- | :---: | :--- | :--- |
| **`api-gateway`** | `8081` (local) / `30081` (K8s NodePort) | Reverse proxy entry point, unified routing, request validation, composite subsystem health checking. | Spring Boot 3, RestTemplate, Actuator |
| **`fleet-telemetry-service`** | `8082` | Vehicle telemetry ingestion, geospatial coordinates, battery & speed tracking, legacy compute node lifecycle. | Spring Data JPA, MySQL / H2, Micrometer |
| **`trip-dispatch-service`** | `8083` | Deterministic trip lifecycle (`REQUESTED` -> `DISPATCHED` -> `IN_TRANSIT` -> `COMPLETED` / `CANCELLED`). | Spring Data JPA, MySQL / H2, GeoUtils |
| **`route-engine-service`** | `8084` | Mathematical distance estimation (Haversine formula), trip duration modeling, and traffic factor simulation. | Spring Boot 3, Stateless In-Memory |
| **`demand-analysis-service`**| `8085` | Spatial matrix convolution and controlled CPU burn loops. Primary scaling target for Kubernetes HPA (min: 1, max: 10, tested: 1 to 8). | Spring Boot 3, Multi-threaded CPU Engine |
| **`incident-manager-service`**| `8086` | Real-time fleet incident reporting, severity classification (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), and resolution workflows. | Spring Data JPA, MySQL / H2 |

---

## 4. Quick Start & Execution Guide

### Option 1: Native Local Build (Zero External Dependencies)
All services fall back to embedded in-memory H2 databases automatically when running locally:

```powershell
# 1. Compile and execute all automated unit & integration tests (25/25 passing)
.\mvnw.cmd test

# 2. Package all executable service JARs
.\mvnw.cmd package -DskipTests

# 3. Launch all services simultaneously using the Windows batch runner:
.\run-local.bat

# 4. Open the interactive dashboard
# Access through browser at: http://localhost:30081 (Kubernetes NodePort) or http://localhost:8081 (Local runner)
# Or open the client page:
start web-dashboard\index.html

# 5. When finished, safely stop all 6 microservices:
.\stop-local.bat
```

> **Local Port Architecture Note:**
> On systems where port `8080` is reserved by other services (e.g., local Jenkins), ScaleMesh API Gateway runs on port **`8081`** by default for local execution (`${SERVER_PORT:8081}`) and NodePort **`30081`** in Kubernetes. All microservice routes (`:8082` through `:8086`) are transparently reverse-proxied.

### Verified Test Results Summary

| Module | Tests Run | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `scalemesh-common` | 3 | 0 | 0 | **SUCCESS** |
| `api-gateway` | 3 | 0 | 0 | **SUCCESS** |
| `fleet-telemetry-service` | 6 | 0 | 0 | **SUCCESS** |
| `trip-dispatch-service` | 4 | 0 | 0 | **SUCCESS** |
| `route-engine-service` | 3 | 0 | 0 | **SUCCESS** |
| `demand-analysis-service` | 3 | 0 | 0 | **SUCCESS** |
| `incident-manager-service` | 3 | 0 | 0 | **SUCCESS** |
| **Total** | **25** | **0** | **0** | **BUILD SUCCESS** |

### Known Limitations & Environment Specifications

1. **Local In-Memory Persistence:** By default, local standalone execution runs in H2 in-memory mode for zero-configuration testing. For persistent MySQL, run `docker compose up -d mysql` and launch with `--spring.profiles.active=mysql`.
2. **Hop-by-Hop Header Sanitization:** Reverse proxy forwarding strictly strips hop-by-hop headers (`Transfer-Encoding`, `Connection`, `Keep-Alive`) to preserve RFC 7230 compliance across downstream microservices and browser clients.
3. **Deterministic Workload Generation:** Route estimations, simulated congestion multipliers, and demand matrices use deterministic seeded mathematical algorithms suitable for academic benchmarking without incurring external paid map API costs.

### Option 2: Docker Compose Orchestration
Run the complete stack including MySQL 8, Prometheus, and Grafana:

```powershell
# Build and launch all 6 services + MySQL + Prometheus + Grafana
docker compose up -d --build

# Verify running containers
docker compose ps

# Access services:
# - Web Dashboard:  web-dashboard/index.html (connects to http://localhost:30081 or http://localhost:8081)
# - API Gateway:    http://localhost:8081/api/system/health
# - Prometheus:     http://localhost:9090 (Note: may conflict with existing host services)
# - Grafana:        http://localhost:3000 (admin / admin; Note: may conflict with existing host services)
```

### Option 3: Kubernetes Deployment
Deploy to Minikube or any standard Kubernetes cluster using Kustomize:

```powershell
# 0. Initialize local secrets from template (required before Kustomize):
copy k8s\secrets.yaml.example k8s\secrets.yaml

# 1. Validate manifests client-side
kubectl kustomize k8s

# 2. Apply all resources into the scalemesh namespace
kubectl apply -k k8s

# 3. Check deployment and pod status
kubectl get pods -n scalemesh
kubectl get hpa -n scalemesh
```

---

## 5. Elastic Autoscaling Experiment (HPA)

To trigger and observe real Kubernetes Horizontal Pod Autoscaling:

```powershell
# Run the automated 3-phase workload generator (Python 3 standard library):
py -3 scripts\workload-generator.py --gateway http://localhost:30081 --baseline-requests 20 --surge-requests 100 --surge-concurrency 15

# Generate academic evaluation report
py -3 scripts\evaluate-autoscaling.py --gateway http://localhost:30081
```

---

## 6. AWS Infrastructure as Code (Terraform)

Terraform configurations are located in `terraform/`:

```powershell
cd terraform
terraform init -backend=false
terraform fmt -check
terraform validate
# Note: Do not run `terraform apply` without cloud authorization to avoid billing.
```

---

## 7. Submission Documentation Index

1. **[Academic Capstone Project Report](docs/PROJECT_REPORT.md)** *(Comprehensive: Abstract, Problem Statement, Objectives, Architecture, Tech Stack, HPA Results, Limitations, References)*
2. [Architecture Specification & Design](docs/ARCHITECTURE.md)
3. [REST API Reference & Endpoint Samples](docs/API.md)
4. [Environment Architecture, Ports & Databases](docs/ENVIRONMENT.md)
5. [Autoscaling Experiment Methodology & Results](docs/AUTOSCALING_EXPERIMENT.md)
6. [AWS Cloud Infrastructure as Code (Terraform)](docs/TERRAFORM_AWS.md)
7. [Viva Defense Preparation & Examiner Q&A Guide](docs/VIVA_PREPARATION.md)
8. [Final Verification & Traceability Checklist](docs/FINAL_VERIFICATION_CHECKLIST.md)
9. [Requirements Traceability Matrix](docs/REQUIREMENTS_MATRIX.md)
10. [Architecture & Implementation Milestones](docs/IMPLEMENTATION_PLAN.md)

### Verification Boundaries & Status Matrix

| Component / Feature | Operational Status | Evidence & Boundary Summary |
| :--- | :---: | :--- |
| **Spring Boot Microservices** | **VERIFIED (PASS)** | All 6 services + Gateway compiled, tested (25/25), containerized, and running in Minikube. |
| **Kubernetes HPA Autoscaling** | **PREVIOUSLY DEMONSTRATED (PASS)** | Live scale-out from 1 to 8 replicas (policy: 1 to 10) recorded under CPU burn; scales back to 1 pod. |
| **Interactive Web Dashboard** | **VERIFIED (PASS)** | Live telemetry visualization and trip lifecycle tracking on `http://localhost:30081`. |
| **Autoscaling Evaluation Report** | **VERIFIED (PASS)** | Generated `docs/AUTOSCALING_EVALUATION_REPORT.md` recording live cluster metrics (631 computations, 184.53 ms latency). |
| **Prometheus & Grafana Monitoring** | **CONFIGURED / UNTESTED AT RUNTIME** | Scrape configs and dashboards created in `monitoring/`; runtime containers offline to avoid host port collisions (9090/3000). |
| **AWS Cloud Infrastructure (Terraform)** | **CONFIGURED / DRY RUN ONLY** | Terraform manifests (`terraform/*.tf`) syntactically validated (`terraform validate`). Zero cloud resources provisioned. |
| **CI/CD Pipelines (Jenkins & GitHub Actions)** | **CONFIGURED / UNTESTED IN RUNNER** | Jenkinsfile and GitHub Actions workflow defined. No remote Git repository connected; no live runner executions recorded. |

