# Idóneos Online

Plataforma web de cursos de finanzas, economía y mercado de capitales para Idóneos Online S.A.S. (Argentina).

## Datos

- Universidad Nacional de Misiones, Facultad de Ciencias Exactas, Químicas y Naturales.
- Licenciatura en Sistemas de Información (Plan 2013), cátedra Proyecto Software.
- Analista en Sistemas de Computación (Plan 2010), cátedra Trabajo Final.
- Profesor adjunto: Lic. Sergio Daniel Caballero.
- Autores: Küster Joaquín (matrícula 906560, [@joaquinkuster](https://github.com/joaquinkuster)) y Martinez Lazaro Ezequiel (matrícula 906047, [@lazamartinez](https://github.com/lazamartinez)).
- Cliente final: Sebastián Bordato y Fausto Spotorno.
- Apóstoles, Misiones, 2026.

## Alcance

1. Cursos
2. Inscripción y Pagos
3. Evaluación y Certificación
4. Clon con IA
5. Clases en Vivo
6. Generación de Contenido con IA
7. Usuarios y Notificaciones
8. Auditoría
9. Reportes y Estadísticas
10. Configuración

## Tecnologías

- Java 21 y Spring Boot 3.4 (Spring Web, Security, Data JPA, Validation).
- Thymeleaf, Bootstrap 5.3 y JavaScript sin frameworks.
- PostgreSQL 16; H2 en memoria para las pruebas.
- Maven 3.9.
- Docker con Compose v2.

## Estructura

```
src/main/java/com/app/idoneos
  config/       seguridad y configuración
  controller/   controladores web, uno por entidad
  service/      servicios (interfaz e implementación)
  repository/   repositorios Spring Data
  model/        entidades del dominio
  util/         utilidades estáticas
src/main/resources
  templates/    vistas Thymeleaf (layout, components y pages)
  static/       css y js organizados por las mismas capas
  schema/       scripts SQL (índices y disparadores)
```

## Ejecución local

Requisito: Docker.

1. Ejecutar:

```bash
docker compose up --build
```

2. Abrir http://localhost:8080.

Con `IDONEOS_SEMILLA: "true"` (valor por defecto) se cargan datos de demostración al iniciar. En un entorno real debe ser `false`.

`docker compose down` apaga todo y `docker compose down -v` además borra la base y los archivos subidos. La base queda en `localhost:5433`.

Si la red intercepta el HTTPS (por ejemplo un antivirus) y la construcción falla con «unable to get local issuer certificate», copiar el certificado raíz como `.crt` en la carpeta `certs/` y volver a construir.

## Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@idoneos.online | 123456 |
| Docente | fausto.spotorno@idoneos.online | 123456 |
| Alumno | valentina.ruiz@correo.com | 123456 |

## Pruebas

```bash
mvn test
```

## Licencia

© 2026 Idóneos Online S.A.S. Desarrollado por Küster Joaquín y Martinez Lazaro Ezequiel para la FCEQyN, UNaM.
