FROM arm64v8/alpine:3.19
WORKDIR /app
COPY /build/native/nativeCompile/rinha-backend-kotlin-spring-native .
RUN adduser -D appuser && chown appuser /app/rinha-backend-kotlin-spring-native
USER appuser
EXPOSE 8080
ENTRYPOINT ["/app/rinha-backend-kotlin-spring-native"]