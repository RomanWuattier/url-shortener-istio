#!/bin/sh

(cd url-service; ./gradlew clean build)
(cd key-generator-service; ./gradlew clean build)
