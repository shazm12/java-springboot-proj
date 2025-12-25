#!/bin/bash

set -e

aws --endpoint-url=http://localhost:5566 cloudformation deploy \
    --stack-name patient-managment \
    --template-file ./cdk.out/localstack.template.json \

aws --endpoint-url=http://localhost:5566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text