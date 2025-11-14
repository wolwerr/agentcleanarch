# Dockerfile
# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY pom.xml .
# COPY .mvn/ .mvn
# COPY mvnw .
RUN mvn -B -f pom.xml -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/target/quarkus-app/lib/ /app/lib/
COPY --from=builder /workspace/target/quarkus-app/*.jar /app/
COPY --from=builder /workspace/target/quarkus-app/app/ /app/app/
COPY --from=builder /workspace/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080

ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV JAVA_OPTS="-Dquarkus.http.host=${QUARKUS_HTTP_HOST} -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Nada de credenciais aqui. Passe-as em runtime.
ENTRYPOINT ["/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/quarkus-run.jar"]
