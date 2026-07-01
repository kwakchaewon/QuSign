output "instance_id" {
  value = aws_instance.app.id
}

output "elastic_ip" {
  value = aws_eip.app.public_ip
}
