# ScaleMesh: Final-Year B.Tech Viva Voce Preparation Guide

**Project Title:** Cloud-Native Microservices Platform for Elastic Fleet Coordination  
**Student:** Kumar Mangalam (Registration Number: 12318090)  
**Supervisor:** Ajay Kumar Badhan  
**Department & University:** Department of Computer Science and Engineering, Lovely Professional University  

This guide equips you to defend and explain the ScaleMesh project during your final-year academic project evaluation and viva examination.

---

## 1. Project Pitch (Elevator Statement)
> *"ScaleMesh is a cloud-native, distributed microservices platform engineered for elastic fleet coordination. It decouples high-throughput vehicle telemetry ingestion, deterministic trip dispatch, and route estimation into six independently deployable services behind an API Gateway reverse proxy. To demonstrate true cloud elasticity, we engineered a spatial matrix convolution service that creates realistic, controlled CPU stress, triggering Kubernetes Horizontal Pod Autoscaling (HPA) configured for 1 to 10 pods, with empirically demonstrated scaling from 1 to 8 pods under peak surge load. The platform is codified using Terraform for AWS infrastructure, defined with Jenkins and GitHub Actions CI/CD pipelines, and monitored through Prometheus and Grafana configurations."*

---

## 2. Key Architectural Questions & Answers

### Q1: Why did you choose a microservices architecture instead of a monolith?
- **Answer:** Fleet management has drastically asymmetrical workload characteristics. Telemetry ingestion is write-heavy and high-frequency (thousands of pings per second), trip dispatch is stateful and transactional, while demand computation is CPU-bound. In a monolith, a spike in demand calculations would starve CPU cycles for telemetry ingestion. Microservices allow independent scaling and fault isolation.

### Q2: How does Kubernetes know when to scale your pods?
- **Answer:** We configured an HPA v2 controller targeting **50% CPU utilization** on the `demand-analysis-service` with `minReplicas: 1` and `maxReplicas: 10`. The Kubernetes Metrics Server polls the kubelet summary API, computes the average utilization across active pods, and if average utilization exceeds 50%, it issues an API request to the Deployment controller to increase the replica count. In our stress test experiment, the replica count climbed from 1 to 8 pods under peak surge load, and scaled back down to 1 pod during cooldown.

### Q3: What is the purpose of the API Gateway?
- **Answer:** The API Gateway acts as the single entry point (reverse proxy on port 8081 / NodePort 30081) for external clients. It abstracts internal network topology, aggregates subsystem health probes (`/api/system/health`), strips hop-by-hop headers for RFC 7230 compliance, and performs centralized validation and error payload sanitization.

### Q4: How is data stored, and how did you prevent database bottlenecks?
- **Answer:** We followed the database-per-service pattern. `fleet-telemetry-service`, `trip-dispatch-service`, and `incident-manager-service` maintain separate databases (`scalemesh_telemetry`, `scalemesh_trips`, `scalemesh_incidents`). For testing and development, we enabled Spring profile-driven persistence: H2 in-memory mode for zero-dependency test suites and MySQL 8 with HikariCP connection pooling for production containers.

### Q5: How do you handle failure in a downstream microservice?
- **Answer:** The API Gateway intercepts downstream HTTP timeouts and errors using `@RestControllerAdvice`, returning structured `ErrorResponse` JSON with standard HTTP status codes (503 Service Unavailable or 502 Bad Gateway) rather than unhandled stack traces. Downstream state is protected with transactional rollback (`@Transactional`).

### Q6: What did you codify in Terraform?
- **Answer:** We codified an entire AWS production VPC topology: custom VPC, public subnets for ingress, private subnets for backend workloads, Internet Gateway, route tables, least-privilege security groups, an encrypted S3 bucket with lifecycle rules for telemetry cold archiving, IAM execution roles, and an EC2 instance with user data bootstrap scripts. Manifests were validated with `terraform validate` as a dry run to avoid student cloud billing charges.

---

## 3. Demonstration Workflow for Evaluators
1. **Show Source Code & Tests:** Run `.\mvnw.cmd test` — demonstrate that all 7 reactor modules compile and all 25 unit/integration tests pass cleanly.
2. **Launch Dashboard:** Open `web-dashboard\index.html` connected to `http://localhost:30081` on the active Kubernetes cluster.
3. **Show Core Requirements:**
   - Demonstrate platform health showing all 6 microservices `HEALTHY` / `UP`.
   - Register a compute node via UI, start it, stop it, and delete it.
   - Ingest simulated vehicle telemetry events and view stats (`FLEET-VEH-101`).
   - Create a trip, dispatch it, and mark it complete.
4. **Trigger Autoscaling:** Run `py -3 scripts\workload-generator.py --gateway http://localhost:30081` and show active concurrency rising and response latencies being recorded. Review the exported evaluation report at `docs/AUTOSCALING_EVALUATION_REPORT.md`.
5. **Show Kubernetes & Terraform Manifests:** Point to `k8s/` (Deployment, HPA, NetworkPolicy) and `terraform/` (VPC, EC2, S3, IAM) and demonstrate validation outputs (`kubectl kustomize k8s` and `terraform validate`).
6. **Explain Verification Boundaries:** Be transparent that microservices, HPA, unit tests, and dashboard are live in Kubernetes, while Prometheus/Grafana and Terraform AWS are configured and dry-run validated.
