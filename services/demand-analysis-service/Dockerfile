FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -g 10001 -S scalemesh && adduser -u 10001 -S scalemesh -G scalemesh
COPY target/demand-analysis-service-1.0.0.jar app.jar
USER 10001:10001
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8085
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health/readiness || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
