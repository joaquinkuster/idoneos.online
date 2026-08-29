# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 29 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.10.md` (27 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.11.md` (29 de agosto de 2026)  

---

## 1. Resumen Ejecutivo

En esta versión `v.0.11` se consolidó la **Arquitectura de Navegación Contextual Unificada (Moodle-Style)** y la **persistencia transversal del contexto del curso activo (`cursoId`)** a través de todas las pantallas académicas, foros, evaluaciones y clases en vivo.

Asimismo, se perfeccionó la documentación formal del proyecto sincronizando los **100 Casos de Uso Reales (`Casos de Uso Reales.md` y `.docx`)**, integrando el **Esquema de Navegación del Sistema (Sección 4.8.0)**, unificando el menú de usuario global sin etiquetas técnicas y eliminando discrepancias en el Modo Edición y renderizado de evaluaciones.

### Métricas Globales del Sistema:
- **Casos de Uso Certificados y Documentados:** 100 / 100 (100%).
- **Arquitectura de Navegación Contextual:** Pestañas fijas Moodle (`curso_header.html`) operativas con persistencia dinámica de `cursoId`.
- **Estandarización de Menú de Usuario:** Dropdown global (`wf_navbar.html`) con nombres formales y limpios de Casos de Uso.
- **Fidelidad Wireframe Prototipo Oficial:** 100% alineado a `Pantallas_CU_Reales.html`.
- **Estado de Transmisiones en Vivo:** Detección y renderizado reactivo de clases activas y programadas en Aula Virtual y Sala.
- **Estado de Compilación y Backend:** Compilación limpia (`BUILD SUCCESS`), 0 errores de inicialización de contexto Spring.

---

## 2. Detalle Exhaustivo de Cambios Implementados

### 🧭 1. Esquema de Navegación Contextual del Curso (Moodle-Style)
- **Fragmento Unificado (`curso_header.html`):**
  - Implementación de cabecera con breadcrumbs interactivos (`Mis cursos > [Nombre del Curso]`), docente titular dinámico y las **10 pestañas contextuales del curso**:
    1. *Curso & Unidades* (`/academico/curso/{id}`)
    2. *Cronograma* (`/academico/cronograma?cursoId={id}`)
    3. *Materiales* (`/academico/materiales?cursoId={id}`)
    4. *Glosario* (`/academico/glosario?cursoId={id}`)
    5. *Autoevaluaciones* (`/evaluaciones/autoevaluaciones?cursoId={id}`)
    6. *Pools* (`/evaluaciones/pools?cursoId={id}`)
    7. *Foros* (`/academico/consultas?cursoId={id}`)
    8. *Clases en Vivo* (`/clases-vivo?cursoId={id}`)
    9. *Participantes* (`/academico/participantes?cursoId={id}`)
    10. *Calificaciones* (`/evaluaciones/calificaciones?cursoId={id}`)
  - Estilo de pestaña activa con fondo tenue (`#F8FAFC`), tipografía oscurecida en negrita y borde inferior dorado (`border-bottom: 3px solid #D4A03D`).
  - Botón de alternancia rápida entre **Activar Modo Edición** y **Ver Modo Alumno** según el rol del usuario logueado.

---

### 🔄 2. Persistencia y Resolución Dinámica de `cursoId` en Backend
- **`AcademicoController.java`:**
  - Soporte de parámetro opcional `@RequestParam(value = "cursoId", required = false) Integer cursoId` en `/academico/materiales`, `/academico/glosario`, `/academico/consultas`, `/academico/cronograma` y `/academico/participantes`.
  - Inyección del objeto `curso` y `cursoSeleccionado` en el modelo, filtrando unidades, materiales y consultas correspondientes al curso activo.
  - Resolución dinámica de transmisiones activas asociadas al curso para alimentar el banner en vivo en CU-26.
- **`EvaluacionController.java`:**
  - Incorporación de `cursoId` en `/evaluaciones/pools`, `/evaluaciones/autoevaluaciones` y `/evaluaciones/calificaciones`.
  - Corrección de expresiones Thymeleaf en `cu-57-buscar-autoevaluacion.html` (`idAutoevaluacion`, `tiempoLimite`, `intentosPermitidos`) eliminando errores `500` / `ERR_INCOMPLETE_CHUNKED_ENCODING`.
  - Eliminación de encabezados manuales duplicados y textos estáticos antiguos ("Especialización en Idoneidad Bursátil").
- **`ClaseEnVivoController.java`:**
  - Recepción de `cursoId`, vinculación contextual con cohortes y programas del curso y filtrado de clases en vivo.
  - Corrección estricta de variables en expresiones lambda para compilación robusta.

---

### 📺 3. Clases en Vivo: Banner Condicional y Datos Semilla
- **Banner Dinámico en Aula Virtual (CU-26):**
  - Configuración con directiva `th:if="${claseEnVivo != null}"` para que el banner rojo *"● EN DIRECTO AHORA"* solo se muestre cuando exista una transmisión en vivo real para dicho curso.
- **Población en `SemillaService.java`:**
  - Creación de instancias de prueba de `ClaseEnVivo` activas (estado *"En vivo"*) y programadas (estado *"Programada"*), asignadas a las cohortes de los cursos principales (*Mercado de Capitales Argentino* y *Análisis Integral de Bonos y Renta Fija*).
- **Alineación de Rutas en `cu-65-buscar-clase-en-vivo.html`:**
  - Estandarización de columnas (`Título`, `Docente`, `Fecha y Hora`, `Duración`, `Estado`, `Transmisión / Acceso`) y enlaces hacia `/clases-vivo/{id}/sala` e `/clases-vivo/{id}/iniciar`.

---

### 📝 4. Actualización de Documentación Oficial de Casos de Uso
- **`docs/diseño/Casos de Uso Reales.md`:**
  - Incorporación de la sección formal **4.8.0. Esquema de Navegación del Sistema** definiendo la jerarquía entre Pestañas Horizontales Contextuales del Curso y el Menú de Usuario Global.
  - Actualización del Paso 1 en los 100 Casos de Uso Reales para reflejar la ruta de interacción Moodle-Style exacta.
- **Regeneración de Documentos:**
  - Regeneración de `Casos de Uso Reales.docx` mediante script de compilación oficial.
  - Sincronización de `docs/diseño/Pantallas_CU_Reales.html` para reflejar todas las cabeceras unificadas.

---

## 3. Estado de Validación y Certificación

- **Compilación Maven:** `BUILD SUCCESS` (0 errores).
- **Controladores y Mapeos:** 100% verificados y libres de colisiones de enrutamiento.
- **Navegación Contextual:** Validación completa del flujo `Mis Cursos -> /academico/curso/8 -> Materiales -> Glosario -> Autoevaluaciones -> Modo Edición`, manteniendo inalterado el contexto del curso seleccionado.
