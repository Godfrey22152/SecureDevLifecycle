# Stage 1: Build the application with Maven
FROM maven:3.9.7-eclipse-temurin-17 AS builder

LABEL maintainer="Odo Godfrey <godfreyifeanyi50@gmail.com>"
LABEL org.opencontainers.image.authors="Odo Godfrey <godfreyifeanyi50@gmail.com>"
LABEL org.opencontainers.image.description="Train-Booking App Tomcat deployment for Java web application"

WORKDIR /app

# Leverage Docker cache by copying pom.xml first
COPY pom.xml ./
RUN mvn -B dependency:go-offline -Dmaven.main.skip=true

# Now add the full source and Build the application
COPY src ./src/
COPY WebContent ./WebContent/
RUN mvn clean package -DskipTests

# Stage 2: Create a custom minimal JRE using jlink
FROM eclipse-temurin:17-jdk-alpine AS jre-builder

RUN jlink \
  --add-modules java.base,java.logging,java.naming,java.xml,java.sql,java.management,java.instrument,java.security.jgss,java.desktop \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --compress=2 \
  --output /custom-jre

# Stage 3: Prepare Slim Tomcat base image
FROM alpine:3.21 AS tomcat-base

SHELL ["/bin/sh", "-c"]

ENV TOMCAT_VERSION=9.0.105
ENV TOMCAT_SHA512=904f10378ee2c7c68529edfefcba50c77eb677aa4586cfac0603e44703b0278f71f683b0295774f3cdcb027229d146490ef2c8868d8c2b5a631cf3db61ff9956

RUN apk add --no-cache curl=8.13.0-r0 tar=1.35-r2 && \
    curl -fsSL https://dlcdn.apache.org/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz -o tomcat.tar.gz && \
    echo "$TOMCAT_SHA512  tomcat.tar.gz" | sha512sum -c - && \
    tar -xzf tomcat.tar.gz -C /opt && \
    mv /opt/apache-tomcat-${TOMCAT_VERSION} /opt/tomcat && \
    rm -rf tomcat.tar.gz \
           /opt/tomcat/webapps/* \
           /opt/tomcat/webapps.dist \
           /opt/tomcat/bin/*.bat \
           /opt/tomcat/logs/* \
           /opt/tomcat/temp/* \
           /opt/tomcat/work/* && \
    apk del curl

# Stage 4: Final runtime image
FROM alpine:3.21

# Add a non-root user for security
RUN addgroup -S tomcat && \
    adduser -S -G tomcat -s /sbin/nologin tomcat

# Copy custom JRE, Tomcat, and WAR
COPY --from=jre-builder /custom-jre /opt/java
COPY --from=tomcat-base /opt/tomcat /opt/tomcat
COPY --from=builder /app/target/*.war /opt/tomcat/webapps/ROOT.war

# Environment setup
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"
ENV LD_LIBRARY_PATH="${JAVA_HOME}/lib/server:${LD_LIBRARY_PATH}"

# Set permissions
RUN chown -R tomcat:tomcat /opt/tomcat && \
    chmod -R go-w /opt/tomcat

USER tomcat
WORKDIR /opt/tomcat

EXPOSE 8080

CMD ["bin/catalina.sh", "run"]
