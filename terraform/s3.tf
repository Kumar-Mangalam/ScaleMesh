resource "aws_s3_bucket" "telemetry_archive" {
  bucket        = var.telemetry_archive_bucket_name
  force_destroy = false

  tags = {
    Name        = "scalemesh-telemetry-archive"
    Environment = var.environment
  }
}

resource "aws_s3_bucket_versioning" "archive_versioning" {
  bucket = aws_s3_bucket.telemetry_archive.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "archive_encryption" {
  bucket = aws_s3_bucket.telemetry_archive.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "archive_public_block" {
  bucket = aws_s3_bucket.telemetry_archive.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "archive_lifecycle" {
  bucket = aws_s3_bucket.telemetry_archive.id

  rule {
    id     = "archive-transition"
    status = "Enabled"

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 90
      storage_class = "GLACIER"
    }
  }
}
