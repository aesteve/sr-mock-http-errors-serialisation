FROM eclipse-temurin:17-jre

COPY ./sr-throttler/target/scala-3.2.1/mock-sr-throttled.jar /usr/app/

WORKDIR /usr/app

ENTRYPOINT [ "java", "-jar", "mock-sr-throttled.jar" ]