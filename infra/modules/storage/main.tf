resource "aws_s3_bucket" "documents" {
  bucket = "qusign-documents-prod"
  # 실제 버킷엔 태그가 없음 — 일치시키기 위해 생략 (태그 추가는 별도 의도적 변경으로 취급)
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

  image_scanning_configuration { scan_on_push = true } # 실제 콘솔 값 확인됨
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.ap-southeast-1.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [var.private_route_table_id]
  tags              = { Name = "qusign_vpc-vpce-s3" }
}
