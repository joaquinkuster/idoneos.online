# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 30 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.11.md` (29 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.12.md` (30 de agosto de 2026)  

---

## 1. Resumen Ejecutivo

En esta versión `v.0.12` se consolidaron importantes avances en la experiencia de usuario, robustez funcional y cobertura de pruebas automatizadas:

1. **Evolución del Cronograma Académico (CU-24):** Conversión a modal interactivo y visualizador de barra de progreso cronológica horizontal dinámica y proporcional a las duraciones de las unidades.
2. **Perfeccionamiento del Módulo de Evaluaciones (CU-53 / CU-54 / CU-55):** Paginación completa de pools de preguntas, creación y edición dinámica de opciones múltiples / verdadero o falso con switches reactivos de selección única/múltiple de respuestas correctas.
3. **Conversión en Modales y Suite de Integración del Módulo 05 (Clases en Vivo — CU-65 a CU-72):**
   - Implementación de modales para Programar (CU-66), Modificar (CU-67), Cancelar (CU-68), Dar de Baja (CU-69) y Finalizar Transmisión (CU-71).
   - Suite de pruebas de integración completa con 8 tests pasando al 100% (`Modulo05ClasesEnVivoIntegrationTest.java`).
4. **Rediseño Inmersivo de la Sala de Streaming (CU-70 / CU-72 — Estilo Google Meet Fullscreen):**
   - Sala responsiva a pantalla completa 100vh con toggle nativo fullscreen.
   - Barra de control Meet inferior con botones circulares interactivos (micrófono, cámara, pantalla compartida, levantar mano, colgar llamada).
   - Panel lateral con pestañas para **Personas Conectadas** y **Chat en Vivo** con envío reactivo instantáneo y miniaturas flotantes PiP.

### Métricas Globales del Sistema:
- **Casos de Uso Certificados:** 100 / 100 (100%).
- **Suite de Pruebas Automatizadas:** 55+ tests de integración pasando exitosamente (`BUILD SUCCESS`).
- **Arquitectura de Streaming:** Soporte RTMP / WebRTC simulado en entorno inmersivo Google Meet para Docentes y Alumnos.
- **Inmutabilidad del Modelo:** Cumplimiento estricto del modelo JPA (`com.app.idoneos.model.*`).

---

## 2. Detalle Exhaustivo de Cambios Implementados

### 📅 1. Módulo Académico: Cronograma Dinámico Horizontal (CU-24)
- **Barra Horizontal de Cronograma:**
  - Sustitución de listas verticales por un timeline horizontal con bloques proporcionales en ancho (`flex: duracion`) según la duración estimada en semanas/días de cada unidad.
  - Colores distintivos e indicadores visuales de semanas acumuladas.
- **Modal CU-24 (Modificar Cronograma):**
  - Ajuste de reordenamiento de unidades y duración sin recargas de página invasivas.

---

### 📝 2. Módulo de Evaluaciones: Pools de Preguntas & Paginación (CU-53 / CU-54 / CU-55)
- **Paginación en `cu-53-buscar-pool.html`:**
  - Integración de paginador estándar de Idóneos Online (`Pageable` Spring Data JPA) con botones de navegación estilizados.
- **Formularios Dinámicos de Preguntas (Crear y Modificar Pool):**
  - Switches claros e interactivos para alternar entre "Única opción correcta" y "Múltiples opciones correctas".
  - Botones con iconografía y micro-animaciones para añadir/remover opciones de respuesta y preguntas de forma fluida.
  - Corrección de botones de edición y visualización de opciones correctas marcadas en tiempo real.

---

### 🎥 3. Módulo de Clases en Vivo: Modales y Suite de Tests (CU-65 a CU-72)
- **Transformación de Vistas en Modales (CU-65):**
  - Modal **CU-66 (Programar clase en vivo)** con validación de horarios y selección de cohorte.
  - Modal **CU-67 (Modificar clase en vivo)** para ajustes de título, duración y fecha.
  - Modal **CU-68 (Cancelar clase en vivo)** y Modal **CU-69 (Dar de baja clase en vivo)** con confirmaciones contextuales.
- **Semilla y Persistencia:**
  - Ajuste de títulos de prueba en `SemillaService.java` respetando límites de base de datos (`@Column(length = 50)`).
  - Manejo case-insensitive de estados (`"En vivo"`, `"Programada"`, `"Finalizada"`, `"Cancelada"`).
- **Suite de Pruebas `Modulo05ClasesEnVivoIntegrationTest.java`:**
  - `testCU65_BuscarClasesEnVivo`
  - `testCU66_ProgramarClaseEnVivo`
  - `testCU67_ModificarClaseEnVivo`
  - `testCU68_CancelarClaseEnVivo`
  - `testCU69_DarDeBajaClaseEnVivo`
  - `testCU70_IniciarClaseEnVivo`
  - `testCU71_FinalizarClaseEnVivo`
  - `testCU72_IngresarASalaClaseEnVivo`

---

### 🌐 4. Sala de Transmisión Inmersiva Google Meet (CU-70 / CU-72)
- **Diseño Inmersivo 100vh (`wf-meet-room`):**
  - Layout viewport completo oscuro (`#111827`, `#030712`) sin márgenes blancos ni cortes visuales.
  - Soporte de pantalla completa nativa mediante API del navegador (`toggleFullScreenMode`).
- **Toolbar Inferior Flotante de Google Meet (`wf-meet-bottombar`):**
  - Controles para micrófono (Mute/Unmute), cámara (On/Off), compartir pantalla, levantar la mano y salir/finalizar la clase.
- **Panel Lateral Desplegable (`wf-meet-sidebar`):**
  - **Pestaña Personas:** Lista en tiempo real de participantes con avatares cromáticos, roles (Docente/Anfitrión, Alumno) y estado de micrófonos.
  - **Pestaña Chat:** Historial de mensajes estilizados en tiempo real con input reactivo (envío con `Enter` y scroll automático).
- **Miniatura Docente PiP (Picture-in-Picture):**
  - Mini-pantalla superpuesta en la esquina inferior derecha con borde dorado y estado de señal.

---

## 3. Estado de Validación y Certificación

- **Compilación Maven:** `BUILD SUCCESS` (0 errores).
- **Tests de Integración:** 100% de tests unitarios y de integración aprobados sin fallos.
- **Compatibilidad UI:** Vistas totalmente responsivas en resoluciones móviles, tablets y escritorios 1080p/4K.
