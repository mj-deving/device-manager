FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/device-manager-server-1.0.0.jar app.jar

# Activate the prod Spring profile by default inside the container.
# Override at runtime: docker run -e SPRING_PROFILES_ACTIVE=dev ...
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# ENTRYPOINT (not CMD) so the process cannot be accidentally replaced by
# passing a command to docker run. Additional JVM flags can still be
# injected via JAVA_OPTS if needed.
ENTRYPOINT ["java", "-jar", "app.jar"]
