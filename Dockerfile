FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /workspace

# Copy full project for reproducible CI/local container builds.
COPY . .

# Build library, run unit tests, skip integration tests and signing.
CMD ["mvn", "-B", "-ntp", "clean", "package", "-DskipITs=true", "-Dgpg.skip=true"]

