# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 11 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.2.md` (11 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.3` se concretó la **fidelidad estricta y trazabilidad 1:1 del modelo de datos orientada a objetos**, comentando exhaustivamente mediante Javadoc en español las **43 clases `@Entity`**, coincidiendo punto por punto con las 43 tablas definidas en la fuente de verdad oficial [`base_datos.sql`](file:///C:/Users/Lazaro/Desktop/TF/idoneos.online/docs/analisis/base_datos.sql).

El sistema se encuentra **100% operativo, compila limpiamente (`BUILD SUCCESS`) y arranca en PostgreSQL 17**, poblando la semilla de datos iniciales sin excepciones ni advertencias de instanciación JPA.

```text
Started IdoneosApplication in 5.47 seconds
Idóneos Online: La semilla ya se encuentra insertada.
```

---

## 2. Reestructuración y Documentación Javadoc Completa (43 Entidades)

### 2.1 Trazabilidad 1 a 1 con `base_datos.sql`
Se revisó y documentó la totalidad de los 43 archivos del paquete `com.app.idoneos.model`, incorporando comentarios en español descriptivos a nivel de clase y a nivel de cada atributo/columna:

| # | Entidad JPA | Tabla SQL Mapeada | Descripción del Dominio |
|:---|:---|:---|:---|
| 1 | `Administrador` | `"administrador"` | Subtipo de Usuario con rol de administración del sistema. |
| 2 | `Alumno` | `"Alumno"` | Subtipo de Usuario con rol de estudiante que cursa y rinde. |
| 3 | `Auditoria` | `"Auditoria"` | Registro AOP de acciones y modificaciones sobre datos. |
| 4 | `Autoevaluacion` | `"Autoevaluacion"` | Exámenes o pruebas rrendibles por unidad temática. |
| 5 | `Categoria` | `"Categoria"` | Clasificación temática de los cursos del catálogo. |
| 6 | `ClaseClonIA` | `"ClaseClonIA"` | Clases en video generadas por avatar IA a partir de guiones. |
| 7 | `ClaseEnVivo` | `"ClaseEnVivo"` | Transmisiones en directo vía RTMP/OBS. |
| 8 | `Configuracion` | `"Configuracion"` | Parámetros del sistema en clave-valor. |
| 9 | `ConsultaForo` | `"ConsultaForo"` | Preguntas formuladas por alumnos en el foro por unidad. |
| 10 | `Curso` | `"Curso"` | Catálogo de oferta académica comercial. |
| 11 | `Descuento` | `"Descuento"` | Cupones o promociones sobre inscripciones. |
| 12 | `Dictado` | `"Dictado"` | Cronograma y período de cursada de un programa. |
| 13 | `DictadoDocente` | `"Dictado Docente"` | Tabla intermedia asociativa de asignación docente a dictado. |
| 14 | `Docente` | `"Docente"` | Subtipo de Usuario con perfil de profesor/autor. |
| 15 | `EstadoClaseClonIA` | `"EstadoClaseClonIA"` | Estados del proceso de generación de video con IA. |
| 16 | `EstadoClaseEnVivo` | `"EstadoClaseEnVivo"` | Estados de la transmisión en directo. |
| 17 | `EstadoPago` | `"EstadoPago"` | Catálogo de estados de transacciones de pago. |
| 18 | `Inscripcion` | `"Inscripcion"` | Vínculo alumno-dictado. Incluye datos de certificado. |
| 19 | `IntentoAutoevaluacion` | `"IntentoAutoevaluacion"` | Examen rendido por un alumno con su calificación. |
| 20 | `Material` | `"Material"` | Archivo o contenido de lectura/video por unidad. |
| 21 | `MetodoPago` | `"MetodoPago"` | Medios de pago aceptados. |
| 22 | `Modalidad` | `"Modalidad"` | Modalidad de dictado (En vivo, Grabada, Clon IA). |
| 23 | `ModalidadCurso` | `"Modalidad Curso"` | Tabla intermedia entre modalidades y cursos. |
| 24 | `OpcionRespuesta` | `"OpcionRespuesta"` | Opciones de respuesta para preguntas de autoevaluaciones. |
| 25 | `Pago` | `"Pago"` | Transacciones procesadas. Incluye datos de comprobante. |
| 26 | `Pool` | `"Pool"` | Banco de preguntas por unidad. |
| 27 | `PoolAutoevaluacion` | `"pool_autoevaluacion"` | Tabla intermedia asociativa entre Pools y Autoevaluaciones. |
| 28 | `Pregunta` | `"Pregunta"` | Preguntas de evaluación pertenencientes a un pool. |
| 29 | `Programa` | `"Programa"` | Versión del plan de estudios de un curso. |
| 30 | `Progreso` | `"Progreso"` | Seguimiento de avance del alumno por unidad. |
| 31 | `Reporte` | `"Reporte"` | Informes generados por la administración. |
| 32 | `RespuestaForo` | `"RespuestaForo"` | Respuestas enviadas por docentes a consultas de alumnos. |
| 33 | `RespuestaIntento` | `"RespuestaIntento"` | Opción seleccionada en un intento de autoevaluación. |
| 34 | `Rol` | `"Rol"` | Catálogo de roles de seguridad. |
| 35 | `Sesion` | `"Sesion"` | Sesiones de usuario con IP y dispositivo. |
| 36 | `TerminoGlosario` | `"TerminoGlosario"` | Diccionario de términos técnicos por unidad. |
| 37 | `TipoAccionAuditoria` | `"TipoAccionAuditoria"` | Acciones registradas en auditoría. |
| 38 | `TipoMaterial` | `"TipoMaterial"` | Clasificación de materiales (Grabación, Bibliografía, etc.). |
| 39 | `TipoReporte` | `"TipoReporte"` | Tipos de informes administrativos. |
| 40 | `TituloDocente` | `"TituloDocente"` | Títulos académicos del equipo docente. |
| 41 | `Unidad` | `"Unidad"` | Unidades temáticas componentes del programa. |
| 42 | `Usuario` | `"Usuario"` | Entidad base de autenticación e identidad. |
| 43 | `UsuarioRol` | `"Usuario Rol"` | Tabla intermedia asociativa entre usuarios y roles. |

---

## 3. Resumen de Verificación y Calidad

- ✅ **43/43 Entidades Comentadas en Español**: Trazabilidad absoluta con `base_datos.sql`.
- ✅ **Sin Errores de Instanciación JPA**: Todos los modelos disponen de constructor explícito `public Entity()`.
- ✅ **Compilación Exitosa**: `.\mvnw.cmd clean compile` ejecutado limpiamente (`BUILD SUCCESS`).
- ✅ **Versionado Git**: Commit y push sincronizado con el repositorio oficial en GitHub.
