# Multi-stage build para otimizar o tamanho da imagem

# Estágio 1: Build da aplicação
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar arquivos do Maven wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download de dependências (cache layer)
RUN ./mvnw dependency:go-offline

# Copiar código fonte
COPY src ./src

# Build da aplicação (skip tests para build mais rápido)
RUN ./mvnw clean package -Dmaven.test.skip=true

# Estágio 2: Imagem de runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar JAR do estágio de build
COPY --from=builder /app/target/*.jar app.jar

# Expor porta da aplicação
EXPOSE 8080

# Variáveis de ambiente com valores padrão
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando de execução
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
