# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 21 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.7.md` (21 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.8.md` (21 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.8` se consolidó una **reorganización arquitectónica integral y modular** de la base del backend Spring Boot, alineando la estructura física de paquetes de Controladores, Servicios y Repositorios con la especificación de módulos funcionales y no funcionales del sistema (`MOD-F-01` a `MOD-F-06`, `MOD-NF-01` a `MOD-NF-04`).

Esta refactorización desacopla los componentes por límites de contexto (Bounded Contexts), facilitando el mantenimiento, la escalabilidad y la trazabilidad directa con la documentación de Casos de Uso (CU-01 a CU-99) y Diagramas de Secuencia (DSS-01 a DSS-99).

### Métricas Globales del Sistema:
- **Casos de Uso Validados y Mapeados:** 99 / 99 (100%).
- **Diagramas de Secuencia UML (DSS) Sincronizados:** 99 / 99 (100%).
- **Entidades JPA & Tablas SQL:** 43 entidades estrictamente consistentes con `base_datos.sql`.
- **Estructura Modular:** Paquetización temática organizada en submódulos para `controller`, `service` y `repository`.
- **Estado de Build:** `BUILD SUCCESS` (0 errores) verificado con Maven Wrapper y JDK 21.

---

## 2. Reorganización Arquitectónica Modular

Se agrupó la lógica de negocio, persistencia y exposición HTTP en los siguientes módulos:

| Módulo | Paquetes (`controller`, `service`, `repository`) | Casos de Uso Abarcados |
| :--- | :--- | :--- |
| **Módulo Cursos (`MOD-F-01`)** | `com.app.idoneos.*.modulo_cursos` | CU-01 al CU-14 |
| **Gestión Académica (`MOD-F-02`)** | `com.app.idoneos.*.modulo_gestion_academica` | CU-15 al CU-42 |
| **Inscripciones y Pagos (`MOD-F-03`)** | `com.app.idoneos.*.modulo_inscripciones` | CU-43 al CU-52 |
| **Evaluaciones (`MOD-F-04`)** | `com.app.idoneos.*.modulo_evaluaciones` | CU-53 al CU-64 |
| **Clases en Vivo (`MOD-F-05`)** | `com.app.idoneos.*.modulo_clases_vivo` | CU-65 al CU-72 |
| **IA & Clones Pedagógicos (`MOD-F-06`)** | `com.app.idoneos.*.modulo_ia` | CU-73 al CU-80 |
| **Usuarios y Seguridad (`MOD-NF-01`)** | `com.app.idoneos.*.modulo_usuarios` | CU-81 al CU-94 |
| **Auditoría (`MOD-NF-02`)** | `com.app.idoneos.*.modulo_auditoria` | CU-95 |
| **Reportes y Estadísticas (`MOD-NF-03`)** | `com.app.idoneos.*.modulo_reportes` | CU-96 al CU-98 |
| **Configuración del Sistema (`MOD-NF-04`)** | `com.app.idoneos.*.modulo_configuracion` | CU-99 |

---

## 3. Cobertura Detallada por Módulo y Bloque de DSS

### 3.1 MOD-F-01: Módulo de Cursos (CU-01 a CU-14)
- **Controladores:** `modulo_cursos.AdminCursoController`, `modulo_cursos.CursoController`, `modulo_cursos.DocenteCursoController`.
- **Servicios:** `modulo_cursos.CursoService`, `modulo_cursos.CategoriaService`.
- **Repositorios:** `modulo_cursos.CursoRepository`, `modulo_cursos.CategoriaRepository`, `modulo_cursos.CohorteRepository`, etc.
- **Flujos DSS:** Búsqueda, catálogo público, aula virtual del alumno, CRUD de cursos, categorías y cohortes con validaciones de programas activos (`EX-CU05-01`, `EX-CU14-01`).

### 3.2 MOD-F-02: Módulo de Gestión Académica (CU-15 a CU-42)
- **Controladores:** `modulo_gestion_academica.DocenteAcademicoController`, `modulo_gestion_academica.ForoController`, `modulo_gestion_academica.ContenidoCursoController`.
- **Servicios:** `modulo_gestion_academica.ProgramaService`, `modulo_gestion_academica.UnidadService`, `modulo_gestion_academica.MaterialService`, `modulo_gestion_academica.GlosarioService`, `modulo_gestion_academica.ForoService`.
- **Repositorios:** `modulo_gestion_academica.ProgramaRepository`, `modulo_gestion_academica.UnidadRepository`, `modulo_gestion_academica.CronogramaRepository`, `modulo_gestion_academica.MaterialRepository`, `modulo_gestion_academica.TerminoGlosarioRepository`, `modulo_gestion_academica.ConsultaForoRepository`, `modulo_gestion_academica.RespuestaForoRepository`.
- **Flujos DSS:** Gestión de programas vigentes, clonación de cronogramas, unidades didácticas, materiales multimediales, glosarios y foros de consulta con alertas por email.

### 3.3 MOD-F-03: Módulo de Inscripciones y Pagos (CU-43 a CU-52)
- **Controladores:** `modulo_inscripciones.InscripcionController`, `modulo_inscripciones.PagoController`, `modulo_inscripciones.CertificadoController`.
- **Servicios:** `modulo_inscripciones.InscripcionService`, `modulo_inscripciones.PagoService`, `modulo_inscripciones.DescuentoService`, `modulo_inscripciones.ProgresoService`.
- **Repositorios:** `modulo_inscripciones.InscripcionRepository`, `modulo_inscripciones.PagoRepository`, `modulo_inscripciones.DescuentoRepository`, `modulo_inscripciones.ProgresoRepository`, `modulo_inscripciones.MetodoPagoRepository`, `modulo_inscripciones.EstadoPagoRepository`.
- **Flujos DSS:** Inscripción a cohortes, control de cupos y semanas de acceso, checkout con validación de promociones, pasarela de pago, tracking de progreso y emisión de certificados digitales.

### 3.4 MOD-F-04: Módulo de Evaluaciones y Rendición (CU-53 a CU-64)
- **Controladores:** `modulo_evaluaciones.EvaluacionController`.
- **Servicios:** `modulo_evaluaciones.EvaluacionService`, `modulo_evaluaciones.IntentoService`.
- **Repositorios:** `modulo_evaluaciones.PoolRepository`, `modulo_evaluaciones.PreguntaRepository`, `modulo_evaluaciones.OpcionRespuestaRepository`, `modulo_evaluaciones.PoolAutoevaluacionRepository`, `modulo_evaluaciones.IntentoAutoevaluacionRepository`, `modulo_evaluaciones.RespuestaIntentoRepository`.
- **Flujos DSS:** CRUD de bancos de preguntas, autoevaluaciones con tiempo límite, sorteo aleatorio de preguntas, entrega y corrección automática con registro de notas y anulación administrativa.

### 3.5 MOD-F-05: Módulo de Clases en Vivo (CU-65 a CU-72)
- **Controladores:** `modulo_clases_vivo.ClaseEnVivoController`.
- **Repositorios:** `modulo_clases_vivo.ClaseEnVivoRepository`, `modulo_clases_vivo.EstadoClaseEnVivoRepository`.
- **Flujos DSS:** Programación, modificación, cancelación y baja de transmisiones; emisión de credenciales RTMP/Stream Key para OBS; conexión en vivo de alumnos y generación automática de grabación como material didáctico.

### 3.6 MOD-F-06: Módulo de Generación Pedagógica con IA y Clones (CU-73 a CU-80)
- **Controladores:** `modulo_ia.GeneracionContenidoIAController`, `modulo_ia.ClaseClonIAController`.
- **Servicios:** `modulo_ia.ClaseClonIAService`, `modulo_ia.OllamaService`.
- **Repositorios:** `modulo_ia.ClaseClonIARepository`, `modulo_ia.EstadoClaseClonIARepository`.
- **Flujos DSS:** Generación automática de bancos de preguntas, resúmenes y presentaciones con Llama 3.1 (Ollama); gestión de Clones IA con TyC biométrico y renderizado de video-lecciones con HeyGen API.

### 3.7 Módulos No Funcionales (MOD-NF-01 a MOD-NF-04: CU-81 a CU-99)
- **Usuarios & Seguridad (`MOD-NF-01`):** `modulo_usuarios.UsuarioController`, `modulo_usuarios.LoginController`, `modulo_usuarios.UsuarioService`, `modulo_usuarios.UsuarioRepository`, `modulo_usuarios.RolRepository`, `modulo_usuarios.SesionRepository`, `modulo_usuarios.DocenteRepository`, `modulo_usuarios.TituloDocenteRepository`.
- **Auditoría (`MOD-NF-02`):** `modulo_auditoria.AuditoriaController`, `modulo_auditoria.AuditoriaRepository`, `modulo_auditoria.TipoAccionAuditoriaRepository`.
- **Reportes (`MOD-NF-03`):** `modulo_reportes.ReportesController`, `modulo_reportes.ReportesService`, `modulo_reportes.ReporteRepository`, `modulo_reportes.TipoReporteRepository`.
- **Configuración (`MOD-NF-04`):** `modulo_configuracion.ConfiguracionController`, `modulo_configuracion.ConfiguracionRepository`.

---

## 4. Estado de la Persistencia, Validaciones y Compilación

- **Estructura JPA / SQL:** Todas las entidades respetan la integridad referencial y nombres de tabla/columna exactos definidos en `base_datos.sql`.
- **Validación de Compilación:**
  ```powershell
  .\mvnw.cmd test-compile
  # Output: BUILD SUCCESS (0 errores)
  ```
- **Pruebas de Integración y Carga de Contexto:**
  ```powershell
  .\mvnw.cmd test
  # Output: BUILD SUCCESS (0 fallos, 0 errores)
  ```
