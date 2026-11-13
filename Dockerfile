FROM maven:3.9.9-eclipse-temurin-21 AS compilacion
COPY certs/ /certs/
RUN for c in /certs/*.crt; do [ -e "$c" ] && keytool -importcert -noprompt -trustcacerts -cacerts -storepass changeit -alias "$(basename "$c")" -file "$c"; done; true
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY certs/ /certs/
RUN for c in /certs/*.crt; do [ -e "$c" ] && keytool -importcert -noprompt -trustcacerts -cacerts -storepass changeit -alias "$(basename "$c")" -file "$c"; done; true
RUN cp /certs/*.crt /usr/local/share/ca-certificates/ 2>/dev/null; update-ca-certificates
RUN sed -i 's|http://|https://|g' /etc/apt/sources.list \
    && apt-get -o Acquire::Retries=5 update \
    && apt-get -o Acquire::Retries=5 install -y --no-install-recommends ocrmypdf tesseract-ocr-spa tesseract-ocr-eng ghostscript ffmpeg \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=compilacion /app/target/*.jar app.jar
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
