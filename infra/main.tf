terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

module "networking" {
  source   = "./modules/networking"
  vpc_cidr = var.vpc_cidr
}

module "compute" {
  source           = "./modules/compute"
  ami_id           = var.ami_id
  key_pair_name    = var.key_pair_name
  public_subnet_id = module.networking.public_subnet_id
  sg_id            = module.networking.sg_id
}

module "iam" {
  source = "./modules/iam"
}

module "storage" {
  source                  = "./modules/storage"
  vpc_id                  = module.networking.vpc_id
  private_route_table_id  = module.networking.private_route_table_id
}

module "secrets" {
  source = "./modules/secrets"
}

module "scheduler" {
  source          = "./modules/scheduler"
  ec2_instance_id = module.compute.instance_id
}

module "dns" {
  source     = "./modules/dns"
  elastic_ip = module.compute.elastic_ip
}
