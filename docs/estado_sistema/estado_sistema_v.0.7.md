# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 21 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.6.md` (21 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.7.md` (21 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.7` se alcanzó el **100% de cobertura, validación y sincronización de los 99 Diagramas de Secuencia UML (DSS)** y sus correspondientes **Casos de Uso Extendidos (CU-01 a CU-99)** contra el backend Spring Boot.

Se implementaron todos los flujos principales, caminos alternativos (`alt`), bucles (`loop`), bloques condicionales (`opt`) y referencias cruzadas (`ref`), complementando los controladores, servicios y repositorios con trazabilidad en código Javadoc.

### Métricas Globales del Sistema:
- **Casos de Uso Validados:** 99 / 99 (100%).
- **Diagramas de Secuencia UML (DSS) Sincronizados:** 99 / 99 (100%).
- **Entidades JPA & Tablas SQL:** 43 entidades estrictamente consistentes con `base_datos.sql`.
- **Clases Java Compiladas:** 158 clases fuente.
- **Estado de Build:** `BUILD SUCCESS` (0 errores) verificado con Maven Wrapper y JDK 21.

---

## 2. Cobertura Detallada por Módulo y Bloque de DSS

### 2.1 MOD-F-01: Módulo de Cursos (CU-01 a CU-14)
- **Controladores:** `AdminController.java`, `CursoController.java`, `DocenteController.java`.
- **Flujos DSS Implementados:**
  - `CU-01`: Búsqueda de cursos por vistas diferenciadas (Alumno, Docente titular/supervisor, Admin).
  - `CU-02`: Aula virtual del alumno (`mis-cursos`) con porcentaje de progreso por unidad.
  - `CU-03`: Registro de curso con docente titular y supervisor opcional.
  - `CU-04 / CU-05`: Modificación, publicación y baja lógica de cursos con validación previa de programas activos (`EX-CU05-01`).
  - `CU-06`: Catálogo público con visualización de descripción, nivel, modalidad y cohortes abiertas.
  - `CU-07 a CU-10`: Búsqueda, registro, modificación y baja lógica de Categorías (`POST /admin/categorias/{id}/modificar`).
  - `CU-11 a CU-14`: Ciclo de vida completo de Cohortes (`GET /admin/programas/{id}/cohortes`, `POST .../guardar`, `POST .../modificar`, `POST .../baja` con validación de alumnos inscriptos activos `EX-CU14-01`).

### 2.2 MOD-F-02: Módulo de Gestión Académica (CU-15 a CU-42)
- **Controladores:** `DocenteController.java`, `ForoController.java`, `CursoController.java`.
- **Flujos DSS Implementados:**
  - `CU-15 a CU-18`: Gestión de Programas (obtener vigente, clonar cronograma de programa base, modificar y baja lógica).
  - `CU-19 a CU-22`: Búsqueda, agregado (creación vs vinculación reutilizable), modificación y desvinculación de Unidades temáticas.
  - `CU-23 a CU-25`: Consulta y edición masiva de Cronogramas (orden de unidades y semanas de duración), y listado unificado de Participantes.
  - `CU-26`: Acceso y navegación del alumno al contenido publicado del curso.
  - `CU-27 a CU-30`: Gestión de Materiales didácticos (grabaciones, bibliografía, resúmenes, presentaciones) con visibilidad y autoría.
  - `CU-31 a CU-34`: CRUD y listado de Términos de Glosario técnico por unidad temática.
  - `CU-35 a CU-38`: Publicación, edición y moderación/baja de Consultas de Foro con notificación automática por email.
  - `CU-39 a CU-42`: Consulta, respuesta, edición y baja de Respuestas de Foro con alerta por email al alumno.

### 2.3 MOD-F-03: Módulo de Inscripciones y Pagos (CU-43 a CU-52)
- **Controladores:** `CursoController.java`, `PagoController.java`, `AdminController.java`.
- **Flujos DSS Implementados:**
  - `CU-43`: Búsqueda de inscripciones activas y emisión de certificados digitales acreditados (`CertificadoController`).
  - `CU-44`: Inscripción con selección de cohorte en dictado, validación de cupo máximo, semanas de acceso y derivación a pasarela de pago para cursos arancelados.
  - `CU-45`: Baja/cancelación de inscripción con motivo (`POST /cursos/inscripciones/{id}/baja`).
  - `CU-46`: Consulta de transacciones y emisión de comprobantes de pago digitales.
  - `CU-47`: Checkout, cálculo automático de bonificaciones y procesamiento de pago con tarjeta.
  - `CU-48`: Seguimiento de progreso por unidad y porcentaje general de la cursada.
  - `CU-49 a CU-52`: Ciclo de vida de Descuentos/Promociones (`GET /admin/descuentos`, `POST /admin/descuentos/guardar`, `POST .../modificar`, `POST .../baja`) con control de vigencia, límite de usos y porcentajes ($1\% \le p \le 100\%$).

### 2.4 MOD-F-04: Módulo de Evaluaciones y Rendición (CU-53 a CU-64)
- **Controladores:** `EvaluacionController.java`.
- **Flujos DSS Implementados:**
  - `CU-53 a CU-56`: CRUD completo de Pools de Preguntas, preguntas de opción múltiple / V-F y opciones de respuesta.
  - `CU-57 a CU-60`: Configuración y ciclo de vida de Autoevaluaciones (tiempo límite, fechas de apertura/cierre, intentos permitidos y visibilidad).
  - `CU-61`: Búsqueda y consulta de historial de intentos de autoevaluación (vistas docente/admin y alumno).
  - `CU-62`: Cuadro de calificaciones consolidado por unidad y curso.
  - `CU-63`: Ejecución de examen, sorteo aleatorio de 10 preguntas, entrega y corrección automática con registro de aprobación ($\ge 7$).
  - `CU-64`: Anulación y baja administrativa de intentos de examen (`POST /evaluacion/intento/{id}/baja`).

### 2.5 MOD-F-05: Módulo de Clases en Vivo (CU-65 a CU-72)
- **Controlador:** `ClaseEnVivoController.java`.
- **Flujos DSS Implementados:**
  - `CU-65`: Listado de sesiones en vivo para docentes y administradores.
  - `CU-66`: Programación de clase asociada a cohorte en dictado con duración estimada.
  - `CU-67`: Modificación de datos informativos y temporales de la clase.
  - `CU-68`: Cancelación de sesión programada con notificación.
  - `CU-69`: Baja lógica de la clase en vivo del sistema.
  - `CU-70`: Apertura de sala y generación de datos de conexión (URL RTMP y clave de transmisión para OBS).
  - `CU-71`: Finalización de clase y generación automática de entidad `Material` de tipo "Grabación" (en estado revisión).
  - `CU-72`: Ingreso y conexión del alumno a la transmisión de la sala virtual.

### 2.6 MOD-F-06: Módulo de Generación Pedagógica con IA y Clones (CU-73 a CU-80)
- **Controladores:** `GeneracionContenidoIAController.java`, `ClaseClonIAController.java`.
- **Flujos DSS Implementados:**
  - `CU-73`: Generación asistida de bancos de preguntas cerradas mediante Ollama (Llama 3.1 8B).
  - `CU-74`: Generación asistida de resúmenes de unidad con persistencia como material didáctico.
  - `CU-75`: Compilación asistida de presentaciones/diapositivas de unidad.
  - `CU-76`: Creación y modelado de Clon IA con validación de consentimiento biométrico (`fechaAceptacionTycClon`).
  - `CU-77`: Consulta de clases generadas con Clon IA.
  - `CU-78`: Renderizado de video-lección mediante avatar y voz sintetizada con HeyGen API v2.
  - `CU-79 / CU-80`: Modificación de guion/título y baja lógica de clases con clon.

### 2.7 Módulos No Funcionales (MOD-NF-01 a MOD-NF-04: CU-81 a CU-99)
- **Controladores:** `LoginController.java`, `UsuarioController.java`, `AdminController.java`, `ReportesController.java`.
- **Flujos DSS Implementados:**
  - `CU-81`: Auto-registro de alumnos con encriptación BCrypt.
  - `CU-82 a CU-85`: Búsqueda, alta con asignación de rol, edición de datos personales (`CU-84`) y baja lógica con reglas de negocio `RN-07` y `RN-11`.
  - `CU-86 / CU-87`: Consulta y edición del perfil del usuario en sesión con refresco de `SecurityContext`.
  - `CU-88 / CU-89`: Registro y edición del perfil profesional docente y CRUD de Títulos Académicos (`TituloDocenteRepository`).
  - `CU-90 / CU-91`: Inicio de sesión (estándar y Google SSO) y cierre seguro de sesión.
  - `CU-92`: Recuperación de contraseña mediante token UUID temporal con expiración de 2 horas.
  - `CU-93 / CU-94`: Historial de sesiones y revocación/cierre remoto de sesiones activas (`POST /usuario/sesiones/{id}/eliminar`).
  - `CU-95`: Consulta del log de auditoría con filtros por entidad afectada y fecha.
  - `CU-96`: Generación y descarga de Informe de Alumnos en PDF (3 vistas: listado, aprobados, notas).
  - `CU-97`: Generación y descarga de Informe de Ingresos en PDF (recaudación por curso y período).
  - `CU-98`: Panel de estadísticas y KPIs ejecutivos en tiempo real.
  - `CU-99`: Consulta y configuración dinámica de variables del sistema (`POST /admin/configuracion/{id}/modificar`).

---

## 3. Estado de la Base de Datos y Repositorios

Se incorporó el repositorio complementario:
- `TituloDocenteRepository.java`: Consultas de certificaciones universitarias vinculadas a la entidad `Docente`.

Todas las 43 entidades JPA permanecen en correspondencia 1:1 con el esquema DDL `base_datos.sql`.

---

## 4. Validación y Compilación

- **Comando:** `.\mvnw.cmd test-compile`
- **Resultado:** **`BUILD SUCCESS`**
- **Total Time:** 5.082 s
- **Errores:** 0
- **Warnings:** 0 críticos (solo avisos informativos sobre Java 21 / Jansi).
