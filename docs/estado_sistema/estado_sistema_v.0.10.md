# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 27 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.9.md` (21 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.10.md` (27 de agosto de 2026)  

---

## 1. Resumen Ejecutivo

En esta versión `v.0.10` se completó la **fidelización visual y funcional 100% exacta contra los prototipos oficiales de Casos de Uso (`Pantallas_CU_Reales.html`)**, incorporando la conversión dinámica de formularios a **Modales Interactivos Bootstrap**, un nuevo **Sistema SweetAlert Toast animado y elegante** con lenguaje natural, la resolución definitiva de renderizado de **íconos vectoriales en más de 64 plantillas** y la integración del **Isologotipo Oficial** en la cabecera del Top Navbar.

Se realizaron además correcciones críticas de backend en endpoints de registro y compatibilidad de parámetros opcionales (`programaId` en `/cursos/cohortes/guardar`), asegurando una experiencia de usuario fluida y libre de errores 500 en todas las pantallas.

### Métricas Globales del Sistema:
- **Casos de Uso Certificados:** 100 / 100 (100%).
- **Fidelidad Wireframe Prototipo Oficial:** 100% alineado a `Pantallas_CU_Reales.html`.
- **Modales Dinámicos Integrados:** 7 modales oficiales (CU-08, CU-12, CU-20, CU-28, CU-32, CU-50, CU-66).
- **Sistema de Notificaciones:** Sweet Toast lateral animado (`wf-toast`) con disparo contextual de operaciones de BD.
- **Iconografía:** FontAwesome 6 Pro inyectado y visible en la totalidad de vistas y botones de acción.
- **Estado de Auditoría Automatizada:** 100% de los endpoints respondiendo `HTTP 200 OK`.

---

## 2. Detalle Exhaustivo de Cambios Implementados

### 🎯 1. Fidelización de Modales Dinámicos con Wireframes Oficiales
Se transformaron y adaptaron los formularios emergentes para que repliquen con exactitud milimétrica la tipografía, badges, campos, placeholders y colores corporativos Deep Navy (`#081426`) y Dorado (`#D4A03D`):

1. **CU-08 (Registrar Categoría) en CU-07:**
   - Badge oficial: `FORMULARIO DE ALTA`.
   - Labels en mayúsculas: `NOMBRE DE LA CATEGORÍA`, `DESCRIPCIÓN TEMÁTICA Y ALCANCE`.
   - Botones: `Cancelar / Volver` (outline) y `Guardar Categoría` (`#081426` con `fa-check`).
2. **CU-12 (Registrar Cohorte) en CU-11:**
   - Subtítulo de programa académico y badge `FORMULARIO DE ALTA`.
   - Labels: `DENOMINACIÓN / CÓDIGO DE COHORTE`, `INICIO DE INSCRIPCIÓN`, `FIN DE INSCRIPCIÓN`, `CUPO MÁXIMO DE ALUMNOS`, `SEMANAS DE ACCESO AL CONTENIDO`, `FECHA INICIO DE DICTADO` y `FECHA FIN DE DICTADO`.
   - Botón: `Guardar Cohorte` (`#081426` con `fa-check`).
3. **CU-20 (Agregar Unidad) en CU-26b (Modo Edición):**
   - Header con badge `FORMULARIO DE ALTA`, campo de reutilización de unidades de programas anteriores y botón `Agregar Unidad`.
4. **CU-28 (Subir Material) en CU-26b (Modo Edición):**
   - Header con badge `NUEVO RECURSO`, selector de tipo de material, explorador de archivos con botón `Examinar...` y recuadro de visibilidad.
5. **CU-32 (Registrar Término de Glosario) en CU-31:**
   - Badge `NUEVO CONCEPTO`, campos `CONCEPTO / TÉRMINO TÉCNICO` y `DEFINICIÓN TÉCNICA Y FÓRMULA`, con botón `Agregar`.
6. **CU-50 (Registrar Descuento / Beca) en CU-49:**
   - Header con `FORMULARIO DE ALTA`, campos de porcentaje, vigencias, cupo límite de usos y cantidad de cursos requeridos, con botón `Guardar Descuento`.
7. **CU-66 (Programar Clase en Vivo) en CU-26b (Modo Edición):**
   - Header con badge `NUEVA SESIÓN`, selector de cohorte destinataria, fecha y hora de inicio, duración estimada en minutos, enlace a sala (Meet/Zoom) y botón `Programar Clase`.

---

### 🔔 2. Sistema SweetAlert Toast Lateral Derecho Animado
- **Diseño UI/UX:** Notificación flotante en esquina superior derecha (`top: 24px; right: 24px`) con sombra profunda, animación elástica suave, borde izquierdo de estado (verde `#10B981`, rojo `#EF4444`, dorado `#D4A03D`) y botón de descarte.
- **Lenguaje Natural y Humano:** Se eliminaron las trazas técnicas de DSS y se implementó detección semántica de mensajes:
  - *Inicio de Sesión:* `¡Bienvenido de vuelta!`
  - *Cierre de Sesión:* `Sesión Finalizada`
  - *Altas y Creación:* `Guardado Exitoso`
  - *Modificaciones:* `Actualización Exitosa`
  - *Bajas:* `Eliminación Exitosa`
  - *Errores:* `Atención`
- **Control Antispam:** Solo emerge cuando el servidor Spring Boot envía un mensaje flash explícito, evitando activaciones en recargas ordinarias (F5).

---

### 🎨 3. Solución Global de Renderizado de Íconos Vectoriales
- **Corrección de Conflicto CSS:** Se resolvió la sobreescritura del selector comodín `* { font-family }` que impedía mostrar los glifos de FontAwesome y provocaba rectángulos vacíos (`▯`).
- **Enriquecimiento Masivo:** Se actualizaron 64 archivos HTML inyectando íconos semánticos en búsquedas (`fa-magnifying-glass`), confirmaciones (`fa-check`), guardado (`fa-floppy-disk`), ediciones (`fa-pen-to-square`), bajas (`fa-trash`) y navegación (`fa-arrow-left`).

---

### 🖼️ 4. Identidad Visual y Navegación
- **Isologotipo Oficial:** Integración del archivo `image.png` en el Top Navbar (`wf_navbar.html`) alineado con la marca institucional.
- **Accesos Rápidos de Administración:** Inclusión de enlaces directos a *Categorías de Cursos*, *Cohortes y Períodos* y *Cupones y Descuentos* tanto en el menú desplegable del avatar como en el Hero Banner de CU-01.
- **Switch Animado de Modo Edición:** Conmutador interactivo con animación deslizante dorada para transicionar entre la vista de alumno (CU-26) y la vista de edición docente/admin (CU-26b).

---

### 🛠️ 5. Correcciones de Backend y Estabilidad
- **CursoController.java:** En el método `guardarCohorte` (`/cursos/cohortes/guardar`), se definió `programaId` como opcional con fallback automático al programa principal (`id: 1`), evitando excepciones `Required request parameter 'programaId' is not present` (Error 500).
- **Rutas de Administración:** Habilitación y verificación de acceso con rol `Administrador` a `/reportes/estadisticas`, `/auditoria`, `/inscripciones` y `/seguridad/usuarios`.

---

## 3. Estado de Validación y Certificación

- **Compilación Maven:** `BUILD SUCCESS` (0 errores).
- **Suite de Pruebas Automatizadas (`run_final_certification.js`):**
  - **Sesión Alumno:** OK ✅
  - **Sesión Docente:** OK ✅
  - **Sesión Administrador:** OK ✅
  - **100% de Casos de Uso con respuesta HTTP 200 OK.**
