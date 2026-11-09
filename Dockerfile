# ── Etapa 1: compilar ────────────────────────────────────────────────────────
# Maven 3.9 con JDK 21: las mismas versiones para todo el equipo. Spring Boot (3.4.0) y las librerías salen del pom.xml.
FROM maven:3.9.9-eclipse-temurin-21 AS compilacion
WORKDIR /app

# Primero solo el pom: las dependencias se descargan una vez y quedan en la caché de Docker hasta que el pom cambie
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ── Etapa 2: ejecutar ────────────────────────────────────────────────────────
# Java 21 sobre Ubuntu 22.04 (jammy). Se instala el OCR (OCRmyPDF + Tesseract en español + Ghostscript) para los PDF escaneados.
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends ocrmypdf tesseract-ocr-spa ghostscript \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=compilacion /app/target/*.jar app.jar

# Archivos subidos (imágenes de cursos, materiales, etc.): se montan como volumen para que no se pierdan al recrear el contenedor
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
