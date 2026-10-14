variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Target deployment environment"
  type        = string
  default     = "production"
}

variable "vpc_cidr" {
  description = "CIDR block for ScaleMesh VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block for private backend subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "instance_type" {
  description = "EC2 instance type for cluster worker nodes"
  type        = string
  default     = "t3.medium"
}

variable "telemetry_archive_bucket_name" {
  description = "Globally unique S3 bucket name for telemetry event archiving"
  type        = string
  default     = "scalemesh-fleet-telemetry-archive-2026"
}
