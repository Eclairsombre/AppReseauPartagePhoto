# Build Stage
FROM maven AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

# Run Stage
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar Fotoshare.jar
# Create uploads directory for photo storage
RUN mkdir -p /app/uploads/photos
EXPOSE 8080
ENTRYPOINT ["java","-jar","Fotoshare.jar"]
