resource "aws_s3_bucket" "documents" {
  bucket = "qusign-documents-prod"
  tags   = { Name = "qusign-documents" }
}

resource "aws_s3_bucket_versioning" "documents" {
  bucket = aws_s3_bucket.documents.id
  versioning_configuration { status = "Disabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_ecr_repository" "backend" {
  name                 = "qusign_backend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration { scan_on_push = false }
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.ap-southeast-1.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [var.private_route_table_id]
  tags = { Name = "qusign_vpc-vpce-s3" }
}
