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
- PostgreSQL en ejecución; H2 en memoria para las pruebas.
- Maven.

## Ejecución con Docker

Lo único que hay que instalar es [Docker](https://www.docker.com/). Con un comando se levanta todo (PostgreSQL 16.4, la aplicación con Java 21 y el OCR para los PDF escaneados), con las mismas versiones para todo el equipo:

```
docker compose up --build
```

La aplicación queda en http://localhost:8080. La primera vez crea las tablas, ejecuta los scripts de `src/main/resources/schema` (índices y disparadores) y carga la semilla de demostración (usuarios de prueba con clave `123456`: ver `SemillaService`).

| Comando | Qué hace |
|---|---|
| `docker compose up --build` | Compila y levanta la base y la aplicación |
| `docker compose down` | Apaga todo; la base y los archivos subidos se conservan |
| `docker compose down -v` | Apaga todo y borra la base y los archivos subidos (arranque desde cero) |

Notas:
- La base queda también en `localhost:5434` (usuario y clave `postgres`) para conectarse con un cliente; usa 5434 para no chocar con un PostgreSQL local.
- Por defecto el compose recrea las tablas y la semilla en cada arranque (`SPRING_JPA_HIBERNATE_DDL_AUTO=create`). Para conservar los datos entre arranques, cambiar a `none` e `IDONEOS_SEMILLA` a `"false"` en `docker-compose.yml`.
- Las versiones se fijan en `pom.xml` (Java y Spring Boot), `Dockerfile` (Maven, JDK y sistema del OCR) y `docker-compose.yml` (PostgreSQL y, más adelante, Ollama).

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

Requisitos: JDK 21, Maven 3.9 o superior y PostgreSQL.

1. Crear la base de datos `idoneos.online` en PostgreSQL. Por defecto la aplicación usa el usuario y la contraseña `postgres` en `localhost:5432`; se pueden cambiar en `src/main/resources/application.properties` o en un perfil `local`.
2. Ejecutar:

```bash
mvn spring-boot:run
```

3. Abrir http://localhost:8080.

Con `idoneos.semilla=true` (valor por defecto) se cargan datos de demostración al iniciar. En un entorno real debe ser `false`.

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
