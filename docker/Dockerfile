# Shared image for every boot jar. Build with:
#   docker build -f docker/Dockerfile --build-arg JAR=apis/public-api/build/libs/public-api-0.0.1-SNAPSHOT.jar -t market-public-api .
FROM eclipse-temurin:21-jre-alpine

ARG JAR
WORKDIR /app

RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Ulaanbaatar /etc/localtime \
    && echo "Asia/Ulaanbaatar" > /etc/timezone

COPY ${JAR} app.jar

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
