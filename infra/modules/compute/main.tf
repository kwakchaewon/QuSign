resource "aws_instance" "app" {
  ami                    = var.ami_id
  instance_type          = "t3.small"
  subnet_id              = var.public_subnet_id
  vpc_security_group_ids = [var.sg_id]
  key_name               = var.key_pair_name
  iam_instance_profile   = aws_iam_instance_profile.ec2.name

  tags = { Name = "qusign_app" }

  lifecycle {
    # AMI 업데이트나 user_data 변경이 EC2 재생성 트리거하지 않도록
    ignore_changes = [ami, user_data]
  }
}

resource "aws_eip" "app" {
  domain = "vpc"
  tags   = { Name = "qusign_app_elasticip" }
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

resource "aws_iam_role" "ec2_role" {
  name = "qusign_ec2_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "qusign_ec2_role"
  role = aws_iam_role.ec2_role.name
}
