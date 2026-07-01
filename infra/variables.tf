variable "aws_region" {
  default = "ap-southeast-1"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "ami_id" {
  description = "EC2 AMI ID (콘솔 확인)"
  type        = string
}

variable "key_pair_name" {
  description = "EC2 Key Pair 이름"
  type        = string
}
