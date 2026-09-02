# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 02 de septiembre de 2026  
**Versión anterior:** `estado_sistema_v.0.13.md` (02 de septiembre de 2026)  
**Versión actual:** `estado_sistema_v.0.14.md` (02 de septiembre de 2026)  

---

## 1. Resumen Ejecutivo

En esta versión `v.0.14` se completaron las mejoras de consistencia gráfica, responsividad, fragmentos de scripts globales y generación de reportes reales del sistema:

1. **Resolución de Renderizado y Fragmentos Thymeleaf (`scripts.html` & `wf-global-scripts`):**
   - Corrección del error `ERR_INCOMPLETE_CHUNKED_ENCODING` en `/auditoria`, `/seguridad/usuarios` y reportes al sincronizar e integrar los fragmentos `th:fragment="scripts"` y `th:fragment="wf-global-scripts"` en un único archivo maestro resiliente `src/main/resources/templates/fragments/scripts.html`.
   - Limpieza y centralización del motor de paginación JavaScript cliente `renderWfPagination` con soporte para selección dinámica de páginas, elipsis inteligentes y conteo exacto de registros.

2. **Módulo de Reportes y Estadísticas Reales (CU-96, CU-97 y CU-98):**
   - **CU-96 (Informe de Alumnos):** Vinculación con datos de alumnos inscriptos reales, cálculo dinámico de promedios, tasas de completitud y exportación a PDF oficial mediante OpenPDF con la identidad institucional (paleta Navy `#081426` y Oro `#E4BE6C`).
   - **CU-97 (Informe de Ingresos):** Cálculo de ingresos brutos por curso y cohorte a partir de inscripciones reales, métricas de ticket promedio y descarga de informe contable en PDF.
   - **CU-98 (Dashboard de Estadísticas):** Corrección y carga de gráficos interactivos Chart.js con datos en tiempo real de alumnos activos, recaudación y distribución temática.

3. **Perfil de Usuario Modal y Limpieza de Identificadores (CU-86 / CU-87):**
   - Conversión de la visualización y edición del perfil de usuario a un **Modal Global interactivo** accesible de forma instantánea desde la barra de navegación superior (`wf_navbar.html`) y desde la tabla de usuarios (`cu-82-buscar-usuario.html`).
   - Eliminación de etiquetas técnicas como "CU-86" y "CU-87" de la vista para una experiencia de usuario limpia y profesional.

4. **Interactividad Completa en Participantes del Curso (CU-25):**
   - Conexión de la barra de búsqueda en tiempo real (por nombre, apellido y correo institucional).
   - Implementación de filtros por selector de rol y cohorte.
   - Filtrado interactivo por iniciales A-Z de Nombre y Apellido con reseteo rápido de filtros.
   - Integración fluida con el sistema de paginación `renderWfPagination`.

5. **Responsividad en Clases con Clon de IA (CU-77):**
   - Rediseño del layout de la pantalla de gestión de clases con clon IA mediante un sistema de grilla Bootstrap `row g-4` y columnas adaptativas (`col-12 col-lg-6`), garantizando visualización óptima en pantallas medianas, notebooks y monitores sin desbordamientos ni columnas colapsadas.

6. **Redirección de Calificaciones desde Cards de Cursos:**
   - Vinculación del botón *Calificaciones* en cada card del catálogo (`cu-01-buscar-curso.html`) para redirigir dinámicamente al curso correspondiente (`/evaluaciones/calificaciones?cursoId={id}`).

---

## 2. Detalle Exhaustivo de Cambios Implementados

### 📄 1. Reportes y Exportación Contable/Académica (MOD-NF-02 / MOD-NF-03)
- **Controlador `ReportesController.java`:**
  - Métodos `@GetMapping("/alumnos")`, `@GetMapping("/alumnos/descargar")`, `@GetMapping("/ingresos")`, `@GetMapping("/ingresos/descargar")` y `@GetMapping("/estadisticas")` conectados a los repositorios de datos reales.
  - Generación de PDF estilizado con cabeceras formales de Idóneos Online S.A.S. y tablas de detalle.
- **Vistas `cu-96`, `cu-97` y `cu-98`:**
  - Alineación exacta con los wireframes oficiales de tesis.

---

### 👤 2. Perfil de Usuario y Barra de Navegación (`wf_navbar.html` y `cu-82`)
- **Modal Global de Perfil:** Se añadió el modal `#modalGlobalMiPerfil` en `wf_navbar.html` para permitir a cualquier usuario autenticado consultar y editar su información personal desde cualquier pantalla.
- **Ficha Rápida en Gestión de Usuarios:** En `cu-82-buscar-usuario.html`, el botón *Ver perfil* abre de forma reactiva `#modalVerPerfilUsuario` con los datos cargados mediante data-attributes sin recarga de página.

---

### 👥 3. Gestión de Participantes (CU-25)
- **Filtros en tiempo real:** Event listeners sobre input de texto, select de cohorte y botones A-Z.
- **Paginación sincronizada:** Actualización en vivo del total de filas visibles y re-renderizado del paginador sin llamadas innecesarias al servidor.

---

### 🤖 4. Clases con Clon Digital (CU-77)
- **Grid fluido:** Reestructuración de la vista para visualización en dos columnas balanceadas (`col-12 col-lg-6`) con previsualizador de video HeyGen Studio y panel de clases.

---

## 3. Estado de Validación y Calidad

- **Compilación Maven:** `mvn clean compile` exitoso sin inconsistencias.
- **Renderizado Web:** Chunked encoding y plantillas Thymeleaf 100% estables.
- **Git:** Commit y push consolidados en la rama `main`.
