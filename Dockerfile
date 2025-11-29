# --------- Stage 1: Build JAR bằng Gradle ----------
FROM gradle:8.8-jdk21 AS builder

WORKDIR /workspace

# Copy toàn bộ project vào container
COPY . .

# Build JAR (bỏ test để nhanh & tránh lỗi)
RUN gradle clean bootJar -x test

# --------- Stage 2: Runtime (siêu nhẹ) ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR từ stage build
COPY --from=builder /workspace/build/libs/*.jar gateway.jar

# Expose port Gateway (mặc định 8080)
EXPOSE 8080

# Tối ưu RAM cho container
ENTRYPOINT ["java", "-XX:+UseG1GC", "-Xms256m", "-Xmx512m", "-jar", "gateway.jar"]
