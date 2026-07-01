# main 라우팅 테이블(rtb-031428bb52dc8216c)은 서브넷 연결 없이 방치 상태라
# Terraform 리소스로 관리하지 않는다 (VPC 생성 시 자동으로 딸려오는 기본 RT).

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = { Name = "qusign_vpc-vpc" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.0.0/20"
  availability_zone       = "ap-southeast-1a"
  map_public_ip_on_launch = false
  tags = { Name = "qusign_vpc-subnet-public1-ap-southeast-1a" }
}

resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.128.0/20"
  availability_zone = "ap-southeast-1a"
  tags = { Name = "qusign_vpc-subnet-private1-ap-southeast-1a" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "qusign_vpc-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = { Name = "qusign_vpc-rtb-public" }
}

# S3 게이트웨이 엔드포인트 라우트는 storage 모듈의 aws_vpc_endpoint.s3에서
# route_table_ids로 이 RT를 지정하는 방식으로 부여된다.
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  tags = { Name = "qusign_vpc-rtb-private1-ap-southeast-1a" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private.id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "main" {
  name   = "qusign_ec2_sg"
  vpc_id = aws_vpc.main.id

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTP (Nginx redirect)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # GitHub Actions 러너 IP가 동적 → 현재 0.0.0.0/0 유지
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "qusign_ec2_sg" }
}
