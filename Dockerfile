FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . .
EXPOSE 8081
RUN mvn clean install -DskipTests
CMD ["mvn", "test"]
