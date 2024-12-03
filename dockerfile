# Usar una imagen base de Java 17
FROM eclipse-temurin:17-jdk-alpine

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR generado al contenedor
COPY target/api-no-jutsu-1.0.0-SNAPSHOT.jar app.jar

# Exponer el puerto en el que escucha la API (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]