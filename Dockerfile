FROM openjdk:21

EXPOSE 8090

ADD out/artifacts/cloud_jar/cloud.jar backend.jar

CMD ["java", "-jar", "backend.jar"]
