FROM openjdk:8
MAINTAINER vctek
WORKDIR /
ADD /build/libs/orderservice.jar /
CMD [ "java", "-jar", "-Dspring.profiles.active=dev", "/orderservice.jar"]
EXPOSE 2228
