# Run Stage - Uses pre-built jar from local target directory
FROM amazoncorretto:17-alpine
WORKDIR /app
# Copy the pre-built jar directly from local target
COPY target/FotoShare-0.0.1-SNAPSHOT.jar Fotoshare.jar
# Create uploads directory for photo storage
RUN mkdir -p /app/uploads/photos
EXPOSE 8081
ENTRYPOINT ["java","-jar","Fotoshare.jar"]
