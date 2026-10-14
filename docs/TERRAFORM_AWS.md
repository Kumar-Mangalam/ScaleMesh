# ScaleMesh: AWS Cloud Infrastructure via Terraform

## 1. Overview
The ScaleMesh cloud deployment is fully codified in HashiCorp Terraform (`terraform/`), targeting Amazon Web Services (AWS) using the official `hashicorp/aws` provider (~> 5.40).

## 2. Infrastructure Resources Codified

1. **Custom VPC (`aws_vpc.scalemesh_vpc`):**
   - CIDR: `10.0.0.0/16`
   - DNS hostnames and resolution enabled.
2. **Subnet Segmentation:**
   - Public Subnet: `10.0.1.0/24` with Internet Gateway routing for public API Gateway ingress.
   - Private Subnet: `10.0.2.0/24` for internal microservice pods and MySQL storage.
3. **Security Groups:**
   - `scalemesh-alb-sg`: Permits external HTTP (80) and API Gateway (8080) ingress.
   - `scalemesh-node-sg`: Permits traffic from ALB and allows unrestricted inter-pod mesh communication while blocking unauthorized external exposure.
4. **S3 Telemetry Event Archive (`aws_s3_bucket.telemetry_archive`):**
   - Versioning enabled.
   - Server-Side Encryption (SSE-S3 AES-256).
   - Public access block (all 4 block rules enabled).
   - Lifecycle rules: Transition to `STANDARD_IA` after 30 days, transition to `GLACIER` after 90 days.
5. **IAM Least-Privilege Execution Role:**
   - Role `scalemesh-node-execution-role` with EC2 trust policy.
   - Scoped policy permitting only `s3:PutObject`, `s3:GetObject`, `s3:ListBucket` against the specific telemetry archive bucket.
6. **EC2 Compute Node (`aws_instance.scalemesh_host`):**
   - Amazon Linux 2023 AMI (`x86_64`).
   - Instance type: `t3.medium`.
   - 30GB encrypted gp3 root volume.
   - User Data script automatically bootstrapping Docker and cluster agents.

## 3. Validation & Safety Boundary

All Terraform configurations were verified with:
```powershell
cd terraform
terraform init -backend=false
terraform fmt -check
terraform validate
```
**FinOps Safety Notice:** In compliance with project guidelines, billable cloud provisioning (`terraform apply`) is restricted to user-approved test cycles.
