resource "aws_iam_role" "node_role" {
  name = "scalemesh-node-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_policy" "s3_archive_policy" {
  name        = "scalemesh-s3-archive-policy"
  description = "Allows writing telemetry archives to the dedicated S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.telemetry_archive.arn,
          "${aws_s3_bucket.telemetry_archive.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_s3" {
  role       = aws_iam_role.node_role.name
  policy_arn = aws_iam_policy.s3_archive_policy.arn
}

resource "aws_iam_instance_profile" "node_profile" {
  name = "scalemesh-node-instance-profile"
  role = aws_iam_role.node_role.name
}
