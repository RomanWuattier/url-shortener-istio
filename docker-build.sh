#!/bin/sh

docker build --tag=url-service:1 url-service
docker build --tag=key-generator-service:1 key-generator-service
