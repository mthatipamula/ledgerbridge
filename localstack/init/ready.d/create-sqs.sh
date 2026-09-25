#!/bin/bash

set -e

QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name settlement-dlq \
  --query QueueUrl \
  --output text)

DLQ_ARN="arn:aws:sqs:us-east-1:000000000000:settlement-dlq"

awslocal sqs create-queue \
  --queue-name settlement-requests \
  --attributes "{
    \"RedrivePolicy\": \"{\\\"deadLetterTargetArn\\\":\\\"${DLQ_ARN}\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"
  }"

echo "Created settlement queues with DLQ redrive policy"
echo "DLQ: ${QUEUE_URL}"