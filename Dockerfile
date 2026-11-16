FROM eclipse-temurin:17-jre-jammy

RUN groupadd --gid 10001 hotel \
    && useradd --uid 10001 --gid hotel --no-create-home hotel

WORKDIR /app

COPY --chown=hotel:hotel presentation/target/presentation-0.0.1-SNAPSHOT.jar app.jar

USER hotel

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]