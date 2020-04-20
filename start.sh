#!/bin/sh

echo '========================================================'
echo 'Start minikube: cpu=4, memory=4096, disk=20000'
echo '========================================================'
minikube start --cpus=4 --memory=4096
minikube docker-env
eval $(minikube -p minikube docker-env)
echo '\n'

echo '========================================================'
echo 'Configure istio: profile=demo'
echo '========================================================'
istioctl manifest apply --set profile=demo
kubectl label namespace default istio-injection=enabled
echo '\n'

echo '========================================================'
echo 'List images'
echo '========================================================'
docker images
echo '\n'

echo '========================================================'
echo 'Build microservices'
echo '========================================================'
./build.sh
echo '\n'

echo '========================================================'
echo 'Build docker image'
echo '========================================================'
./docker-build.sh
echo '\n'

echo '========================================================'
echo 'List images'
echo '========================================================'
docker images
echo '\n'

echo '========================================================'
echo 'Kubernetes apply deployment'
echo '========================================================'
kubectl apply -f url-service/infra
kubectl apply -f key-generator-service/infra
kubectl apply -f infra
echo '\n'

echo '========================================================'
echo 'Ingress gateway URL'
echo '========================================================'
./ingress-gateway-url.sh
echo '\n'
