# Stage 1: Build the application
FROM egovio/alpine-maven-builder-jdk-8:1-master-NA-6036091e AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file and install dependencies
COPY business-services/billing-service/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

# Copy the source code
COPY business-services/billing-service/src ./src

# Package the application
RUN mvn -B -f /app/pom.xml package

# Stage 2: Create the final image
FROM egovio/8-openjdk-alpine:latest

# Set the working directory for the final image
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/billing-service.jar ./billing-service.jar

# Set the entrypoint for the application
ENTRYPOINT ["java", "-jar", "billing-service.jar"]
