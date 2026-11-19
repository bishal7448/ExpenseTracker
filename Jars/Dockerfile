#FROM amazoncorretto:21-alpine-jdk
#
#LABEL maintainer="bishal7448"
#
#WORKDIR /app
#
#COPY UserService-0.0.1-SNAPSHOT.jar user-service.jar
#
#ENTRYPOINT ["java", "-jar", "user-service.jar"]

# docker build -t user-servive .

FROM amazoncorretto:21-alpine-jdk

LABEL org.opencontainers.image.authors="bishal7448"

WORKDIR /app

COPY app.jar app-service.jar

ENTRYPOINT ["java", "-jar", "app-service.jar"]

# docker build -t auth-servive .