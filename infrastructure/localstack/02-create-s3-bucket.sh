#!/bin/bash

echo "Creating S3 bucket..."

# Create S3 bucket
awslocal s3 mb s3://cooking-app-images-local --region ap-northeast-1

# Enable versioning
awslocal s3api put-bucket-versioning \
  --bucket cooking-app-images-local \
  --versioning-configuration Status=Enabled \
  --region ap-northeast-1

# Set CORS configuration
awslocal s3api put-bucket-cors \
  --bucket cooking-app-images-local \
  --cors-configuration '{
    "CORSRules": [
      {
        "AllowedOrigins": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST"],
        "AllowedHeaders": ["*"],
        "MaxAgeSeconds": 3000
      }
    ]
  }' \
  --region ap-northeast-1

echo "S3 bucket created successfully!"
