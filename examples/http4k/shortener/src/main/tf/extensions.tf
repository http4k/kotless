resource "aws_dynamodb_table" "http4k_short_url_table" {
  name = "http4k-short-url-table"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20
  hash_key = "URLHash"

  attribute {
    name = "URLHash"
    type = "S"
  }
}
