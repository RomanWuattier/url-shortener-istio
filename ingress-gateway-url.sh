#!/bin/sh

IP=$(minikube ip)
PORT_SERVICE=$(kubectl -n istio-system get service istio-ingressgateway -o \
	jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
PORT_GRAFANA=$(kubectl -n istio-system get service istio-ingressgateway -o \
	jsonpath='{.spec.ports[?(@.name=="grafana")].nodePort}')
echo Service gateway: http://$IP:$PORT_SERVICE/
echo Grafana gateway: http://$IP:$PORT_GRAFANA/
