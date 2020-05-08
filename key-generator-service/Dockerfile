FROM openjdk:11.0.3-jre-slim
COPY build/libs/key-generator-service-0.0.1-SNAPSHOT.jar .
CMD /usr/bin/java -XX:InitialRAMPercentage=60 -XX:MaxRAMPercentage=90 -XX:TieredStopAtLevel=1 -noverify -jar key-generator-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=kube
EXPOSE 8080
