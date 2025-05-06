# Stage 1: Build the application with Maven
FROM maven:3.9.7-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Layer caching for dependencies
COPY pom.xml /app/
RUN mvn -B dependency:go-offline -Dmaven.main.skip=true

# Copy source code and build
COPY src /app/src/
COPY WebContent /app/WebContent/
RUN mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true


# Stage 2: Create a new image with Tomcat
FROM tomcat:9.0.104-jdk17

# Clean up default Tomcat files to reduce image size
RUN find /usr/local/tomcat -mindepth 1 -maxdepth 1 ! -name 'bin' ! -name 'lib' ! -name 'conf' -exec rm -rf {} +

# Copy the WAR file from the previous stage
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Create a non-root user and group
RUN groupadd -r tomcat && \
    useradd -r -g tomcat --no-log-init --shell /bin/false tomcat && \
    chown -R tomcat:tomcat /usr/local/tomcat

# Switch to non-root user
USER tomcat

# Expose the Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
