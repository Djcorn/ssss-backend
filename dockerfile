FROM eclipse-temurin:21
COPY app/build/libs/app.jar s4-backend-1.0.0.jar
ENTRYPOINT ["java","-jar","/s4-backend-1.0.0.jar"]