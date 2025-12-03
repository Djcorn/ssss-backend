FROM eclipse-temurin:21
RUN apt-get update && apt-get install -y netcat-openbsd
COPY app/build/libs/app.jar s4-backend-1.0.0.jar
ENTRYPOINT ["java","-jar","/s4-backend-1.0.0.jar"]