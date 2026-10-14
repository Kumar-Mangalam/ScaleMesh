output "vpc_id" {
  description = "ID of the created ScaleMesh VPC"
  value       = aws_vpc.scalemesh_vpc.id
}

output "public_subnet_id" {
  description = "ID of the public subnet"
  value       = aws_subnet.public_subnet.id
}

output "private_subnet_id" {
  description = "ID of the private subnet"
  value       = aws_subnet.private_subnet.id
}

output "ec2_instance_id" {
  description = "ID of the provisioned EC2 instance"
  value       = aws_instance.scalemesh_host.id
}

output "ec2_public_ip" {
  description = "Public IP address of the EC2 instance"
  value       = aws_instance.scalemesh_host.public_ip
}

output "s3_archive_bucket" {
  description = "Name of the S3 telemetry archive bucket"
  value       = aws_s3_bucket.telemetry_archive.bucket
}

output "s3_archive_bucket_arn" {
  description = "ARN of the S3 telemetry archive bucket"
  value       = aws_s3_bucket.telemetry_archive.arn
}
