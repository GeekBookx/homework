# Build stage
# 使用基于 Eclipse Temurin 的 Maven 镜像，更稳定
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
# 将 openjdk:17-jdk-slim 替换为 eclipse-temurin:17-jdk-alpine
# Alpine 版本体积小，且 eclipse-temurin 是目前推荐的官方替代品
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]