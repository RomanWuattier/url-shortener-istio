#!/bin/sh

echo '========================================================'
echo 'Stopping minikube'
echo '========================================================'
minikube stop
echo '\n'

echo '========================================================'
echo 'Deleting minikube'
echo '========================================================'
minikube delete