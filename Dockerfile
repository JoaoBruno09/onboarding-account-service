FROM maven:3.9.6-amazoncorretto-21
WORKDIR /boot/target
COPY /boot/target/account-service-0.0.1-SNAPSHOT.jar account-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "account-service-0.0.1-SNAPSHOT.jar"]