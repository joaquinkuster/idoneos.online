# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 21 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.5.md` (11 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.6.md` (21 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.6` se llevó a cabo una **auditoría y alineación integral de los 18 controladores MVC/REST** (`src/main/java/com/app/idoneos/controller`) y la sincronización con la capa de servicios y entidades JPA, contrastándolos rigurosamente contra los **99 Casos de Uso Extendidos (CU-01 a CU-99)** organizados en los 10 módulos del sistema (6 funcionales y 4 no funcionales).

Se resolvieron discrepancias de constructores y compatibilidad de tipos (sobrecarga de constructores con enum `RolUsuario`, helpers de navegación y getters de conveniencia en entidades JPA), garantizando:
1. **Compilación 100% Limpia**: `BUILD SUCCESS` con Maven Wrapper en Java 21 sobre las 157 clases del sistema.
2. **Inmutabilidad del Modelo de Datos**: Preservación estricta de las 43 tablas y relaciones mapeadas desde `docs/analisis/base_datos.sql`.
3. **Trazabilidad Funcional y Reglas de Negocio**: Cobertura exhaustiva de reglas críticas (`RN-07` mínimo un Administrador activo, `RN-11` protección de baja a docente con cursos publicados, consentimiento firmado de Clon IA con HeyGen, validación de descuentos y control de intentos en evaluaciones).

---

## 2. Auditoría y Cobertura de Controladores por Módulo

### 2.1 MOD-F-01: Módulo de Cursos (CU-01 a CU-14)
- **Controladores:** `AdminController.java`, `CursoController.java`, `DocenteController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-01 (Buscar curso):** Vistas y filtros para Administrador, Alumno y Docente restringido a cursos donde participa como titular o supervisor.
  - **CU-02 (Ver mis cursos):** Aula del alumno con cursos inscriptos y avance porcentual.
  - **CU-03 (Registrar curso):** Validación de precio $\ge 0$, asignación de titular y supervisor opcional.
  - **CU-04 / CU-05 (Modificar / Dar de baja curso):** Verificación de programas y cohortes activas antes de permitir la baja lógica.
  - **CU-06 (Explorar catálogo):** Ficha pública del curso con contenidos y unidades temáticas.
  - **CU-07 a CU-10 (Gestión de Categorías):** Búsqueda, alta con unicidad de nombre, edición y baja validando cursos activos.
  - **CU-11 a CU-14 (Gestión de Cohortes):** Relación de dictados y períodos de acceso modelados en `Cohorte` y `Cronograma`.

### 2.2 MOD-F-02: Módulo de Gestión Académica (CU-15 a CU-42)
- **Controladores:** `DocenteController.java`, `ForoController.java`, `CursoController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-15 a CU-25 (Programas, Unidades y Cronogramas):** Gestión de contenido por unidad, orden secuencial y panel de control docente.
  - **CU-26 (Acceder curso):** Aula virtual del alumno con cálculo de avance y contenido publicado.
  - **CU-27 a CU-30 (Materiales):** Gestión de grabaciones, bibliografía, resúmenes y presentaciones con control de publicación (`oculto`/`publicado`).
  - **CU-31 a CU-34 (Glosario Técnico):** Alta y consulta de términos asociados por unidad temática.
  - **CU-35 a CU-42 (Foro por Unidad):** Publicación de consultas de alumnos, respuestas de docentes titulares/supervisores y notificaciones automáticas por correo vía `EmailService`.

### 2.3 MOD-F-03: Módulo de Inscripciones (CU-43 a CU-52)
- **Controladores:** `CursoController.java`, `PagoController.java`, `DescuentoController.java`, `ProgresoController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-43 / CU-44 (Buscar / Inscribir curso):** Alta de inscripción y derivación al checkout según arancel.
  - **CU-46 / CU-47 (Buscar / Realizar pago):** Integración del flujo de pago simulado con Mercado Pago, cálculo automático de descuentos y emisión de comprobantes.
  - **CU-48 (Buscar progreso):** Seguimiento del progreso del alumno por unidad.
  - **CU-49 a CU-52 (Descuentos y Cupones):** Búsqueda, registro con rango (1% a 100%), vigencia cronológica, límites de uso y baja lógica.

### 2.4 MOD-F-04: Módulo de Evaluaciones (CU-53 a CU-64)
- **Controladores:** `EvaluacionController.java`, `CertificadoController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-53 a CU-56 (Pools de Preguntas):** Creación de pools por unidad, carga de preguntas de opción múltiple / V-F con validación de opción correcta.
  - **CU-57 a CU-60 (Autoevaluaciones):** Apertura, límite de intentos y configuración por unidad.
  - **CU-61 a CU-63 (Intentos y Calificaciones):** Sorteo aleatorio de 10 preguntas, corrección automática, cálculo de nota, registro de unidad completada y emisión automática de certificados.

### 2.5 MOD-F-05: Módulo de Clases en Vivo (CU-65 a CU-72)
- **Controlador:** `ClaseEnVivoController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-65 a CU-68 (Gestión y Programación):** Búsqueda, programación con fecha/hora futura, modificación y cancelación.
  - **CU-70 / CU-71 (Iniciar / Finalizar Transmisión):** Generación de URL RTMP y clave de transmisión para OBS, y conversión automática de la transmisión finalizada en Material de tipo Grabación para revisión.
  - **CU-72 (Ingresar a clase en vivo):** Conexión del alumno a la transmisión en curso.

### 2.6 MOD-F-06: Módulo de Generación de Contenido con IA (CU-73 a CU-80)
- **Controladores:** `GeneracionContenidoIAController.java`, `ClaseClonIAController.java`.
- **Casos de Uso Cubiertos:**
  - **CU-73 a CU-75 (Generación con Ollama / Llama 3.1):** Generación automática de bancos de preguntas, resúmenes estructurados y presentaciones de clase a partir de la bibliografía cargada.
  - **CU-76 a CU-80 (Clon IA con HeyGen API v2):** Verificación obligatoria de consentimiento firmado (`fechaConsentimientoClon`), generación de video y registro como Material sin publicar para revisión docente.

### 2.7 Módulos No Funcionales (MOD-NF-01 a MOD-NF-04)
- **MOD-NF-01 (Usuarios y Notificaciones - CU-81 a CU-94):** Auto-registro de alumnos (`LoginController`), login/logout con Spring Security, recuperación de contraseña con token UUID de 2 horas de vigencia (`LoginController` y `UsuarioController`), y administración central de cuentas con reglas de negocio `RN-07` y `RN-11` (`AdminController`).
- **MOD-NF-02 (Auditoría - CU-95):** Panel de consulta con filtros por entidad afectada y ordenamiento cronológico (`AuditoriaController`).
- **MOD-NF-03 (Reportes y Estadísticas - CU-96 a CU-98):** Generación de PDFs de Alumnos (3 vistas), Ingresos (4 vistas), KPIs en tiempo real y persistencia del historial de reportes (`ReportesController`).
- **MOD-NF-04 (Configuración - CU-99):** Esquema clave-valor para parámetros operativos del sistema (`ConfiguracionController`).

---

## 3. Correcciones Técnicas Aplicadas

1. **Sobrecarga de Constructores y Helpers en Entidades:**
   - `Usuario.java`: Agregados constructores y setters sobrecargados que aceptan el enum `RolUsuario` junto con la entidad `Rol`.
   - `Docente.java`: Agregado constructor `Docente(Usuario)` para altas directas.
   - `Auditoria.java`: Agregado constructor de conveniencia `Auditoria(String, int, Usuario, TipoAccionAuditoria)`.
   - `Reporte.java`: Agregado constructor `Reporte(TipoReporte, Administrador)` para generación de reportes globales sin curso obligatorio.
   - `Categoria.java`, `Descuento.java`, `ClaseEnVivo.java`, `Unidad.java`, `Material.java`, `Autoevaluacion.java`: Incorporación de helpers de estado (`esInactivo()`, `estaVigente()`, `getUnidad()`, `getCurso()`, `getNumeroOrden()`, `getPools()`, `getPublicado()`, `setGeneradoPorIA()`).
2. **Sincronización de Servicios:**
   - `CursoServiceImpl.java`: Actualizada la validación de dependencias para operar sobre `Cohorte` en lugar de la entidad anterior `Dictado`.
   - `ForoServiceImpl.java`: Corregida la comparación de roles mediante `usuario.getRolUsuario()`.
   - `ReportesService.java`: Alineada la lectura de cursos e inscripciones a través de `Inscripcion.getCurso()`.
   - `OllamaService.java`: Corregido el orden de parámetros en la instanciación de `Material`.

---

## 4. Estado de Verificación y Compilación

- **Compilador:** Java JDK 21 / Maven 3.9.9 (vía `mvnw`).
- **Resultado del Build:** `BUILD SUCCESS` (157 archivos compilados sin errores).
- **Control de Versiones:** Commit y push de todos los cambios aplicados en la rama principal (`main`).
