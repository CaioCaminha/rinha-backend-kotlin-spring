FROM arm64v8/ubuntu:22.04
WORKDIR /app
COPY build/native/nativeCompile/rinha-backend-kotlin-spring-native .
RUN chmod +x /app/rinha-backend-kotlin-spring-native
USER 1000
EXPOSE 8080
ENTRYPOINT ["/app/rinha-backend-kotlin-spring-native"]