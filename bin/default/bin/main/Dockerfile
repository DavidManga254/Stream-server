# Stage 1 - Build
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# copy gradle files first (layer caching - only re-downloads dependencies if these change)
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .

# download dependencies
RUN ./gradlew dependencies --no-daemon

# copy source code and build
COPY . .
RUN ./gradlew jar --no-daemon

# Stage 2 - Run
FROM eclipse-temurin:21-jre

# install FFmpeg
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# copy JAR from build stage
COPY --from=builder /app/build/libs/stream-server.jar .

# create video storage directory
RUN mkdir -p storage/videos

# expose your port
EXPOSE 4221

CMD ["java", "-jar", "stream-server.jar"]