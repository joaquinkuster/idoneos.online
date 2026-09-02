# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 02 de septiembre de 2026  
**Versión anterior:** `estado_sistema_v.0.12.md` (30 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.13.md` (02 de septiembre de 2026)  

---

## 1. Resumen Ejecutivo

En esta versión `v.0.13` se completó la modernización, conversión a modales interactivos y estandarización visual y funcional de los módulos no funcionales y de soporte de la plataforma:

1. **Gestión Integral de Usuarios (CU-82 a CU-85 y CU-88):**
   - Rediseño estético y funcional en `cu-82-buscar-usuario.html` con botones dorados unificados (`wf-btn-gold`) para **Nuevo Usuario** y **Nuevo Docente**.
   - Incorporación de avatares con gradiente navy/oro y sombras suaves para usuarios activos, y tonos grises apagados (`#94A3B8`) para usuarios con desactivación lógica (`baja = true`).
   - Botones de acción cromáticos: **Ver perfil** (celeste institucional), **Modificar** (teal) y **Desactivar cuenta** (rojo carmesí) con confirmación mediante modal.
   - Paginación cliente-servidor estandarizada con `wf-pagination` (`window.renderWfPagination`) a 8 usuarios por página.
2. **Reestructuración del Menú de Navegación y Cuenta (`wf_navbar.html`):**
   - Remoción de identificadores técnicos `(CU-XX)` de la interfaz pública y de administración.
   - Reorganización del dropdown administrativo con accesos explícitos: *Gestión de usuarios*, *Consultar auditoria*, *Informe de alumnos*, *Informe de ingresos*, *Consultar estadisticas* y *Configurar parametros*.
   - Integración de confirmación de cierre de sesión mediante **SweetAlert2**.
3. **Módulo de Configuración Responsive (CU-99):**
   - Contenedor fluido con `table-responsive` y scroll táctil para tablas densas de parámetros.
   - Formularios flexbox adaptables a dispositivos móviles y tablets.
4. **Paginación y Modales en Sesiones Activas (CU-93 / CU-94) y Auditoría (CU-95):**
   - Integración de `wf-pagination` en las vistas de auditoría y sesiones.
   - Cierre forzoso de sesiones con confirmación contextual por modal emergente (IP, dispositivo, usuario).
5. **Modales de Clon Digital con IA (CU-79 / CU-80):**
   - Edición de guiones para HeyGen y confirmación de baja de clases con clon sin abandonar el visor interactivo.

### Métricas Globales del Sistema:
- **Casos de Uso Implementados y Sincronizados:** 100 / 100 (100%).
- **Compilación Maven:** `BUILD SUCCESS` (0 errores).
- **Control de Calidad UI/UX:** Alineación total con el sistema de diseño oficial de la tesis.

---

## 2. Detalle Exhaustivo de Cambios Implementados

### 👥 1. Módulo de Seguridad y Usuarios (MOD-NF-01)
- **Controlador `SeguridadController.java`:**
  - Consulta general de usuarios mediante `usuarioRepository.findAll()` para permitir visualizar y gestionar cuentas activas y desactivadas con búsqueda en tiempo real.
- **Vista `cu-82-buscar-usuario.html`:**
  - Botones dorados `wf-btn-gold` para creación rápida.
  - Formato visual condicional para cuentas desactivadas (`opacity: 0.65`, fondo `#F8FAFC`, badge `Desactivado`).
  - Botones con iconografía y texto explícito (*Ver perfil*, *Modificar*, *Desactivar cuenta*).
  - Paginador dinámico `#paginacionUsuarios`.

---

### 🧭 2. Barra de Navegación Superior (`wf_navbar.html`)
- Depuración de sintaxis JavaScript en listeners y modales.
- Eliminación de códigos de caso de uso visibles para el usuario.
- Enrutamiento exacto para el módulo gerencial y de reportes.
- Diálogo de confirmación SweetAlert2 para logout con redirección inmediata.

---

### ⚙️ 3. Configuración del Sistema (MOD-NF-04 - CU-99)
- Adaptación a resoluciones móviles y pantallas pequeñas de la tabla de parámetros y edición de variables clave.

---

### 🛡️ 4. Auditoría y Sesiones (CU-93 a CU-95)
- Inclusión del componente unificado `wf-pagination` en las tablas de auditoría y sesiones activas.
- Modal interactivo CU-94 para revocación remota de credenciales.

---

## 3. Estado de Validación y Calidad

- **Build:** `mvnw test-compile` exitoso sin advertencias críticas.
- **Git:** Versionado completo y trazabilidad consolidada en el repositorio.
