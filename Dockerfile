FROM maven:3.8.5-openjdk-11-slim AS builder
WORKDIR /app
COPY . .
RUN mvn clean package
FROM tomcat:10-jdk11
WORKDIR /app
COPY --from=builder /app/target/fabflix.com.war /usr/local/tomcat/webapps/fabflix.com.war
EXPOSE 8080
CMD ["catalina.sh", "run"]