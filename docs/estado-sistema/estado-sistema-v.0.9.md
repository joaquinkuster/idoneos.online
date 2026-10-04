# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 21 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.8.md` (21 de agosto de 2026)  
**Versión actual:** `estado_sistema_v.0.9.md` (21 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.9` se completó la **renovación, sincronización y construcción integral de la capa Frontend (Vistas Thymeleaf & Sistema de Diseño CSS)**, conectándola de manera exhaustiva con los 18 controladores Spring Boot 3 del backend y la totalidad de los 99 Casos de Uso (`CU-01` a `CU-99`) distribuidos en los módulos funcionales (`MOD-F-01` a `MOD-F-06`) y no funcionales (`MOD-NF-01` a `MOD-NF-04`).

Se diseñó e implementó un **Design System Financiero de Alta Gama** (*Navy & Gold*), con microinteracciones, widgets KPI, terminales de examen cronometradas con auto-entrega, salas de streaming RTMP con chat interactivo en vivo, reproductores de video-lecciones sintéticas HeyGen y tableros analíticos interactivos con Chart.js.

### Métricas Globales del Sistema:
- **Casos de Uso Validados y Mapeados:** 99 / 99 (100%).
- **Diagramas de Secuencia UML (DSS) Sincronizados:** 99 / 99 (100%).
- **Controladores Spring Boot 3:** 18 controladores activos organizados en paquetes modulares.
- **Plantillas HTML / Thymeleaf:** 32 vistas y fragmentos sincronizados con los getters y entidades del modelo (`base_datos.sql`).
- **Sistema de Estilos:** `styles.css` con variables CSS HSL, diseño responsive, alertas personalizadas y tipografía Google Fonts.
- **Estado de Build:** `BUILD SUCCESS` (0 errores) verificado con Maven Wrapper y JDK 21.

---

## 2. Mapa de Vistas Frontend y Cobertura por Módulo

| Módulo | Casos de Uso | Vistas HTML / Thymeleaf | Descripción y Capacidades UI |
| :--- | :--- | :--- | :--- |
| **MOD-F-01: Cursos & Catálogo** | CU-01 a CU-14 | `pages/cursos/catalogo.html`<br>`pages/cursos/detalle.html`<br>`fragments/cartaCurso.html`<br>`pages/admin/cohortes.html` | Catálogo con filtros por categoría y búsqueda en tiempo real. Ficha de curso con cuerpo docente CNV, arancel oficial y acordeón curricular. Gestión de cohortes y cupos. |
| **MOD-F-02: Gestión Académica** | CU-15 a CU-42 | `pages/docente/programas.html`<br>`pages/docente/unidades.html`<br>`pages/docente/cronograma.html`<br>`pages/docente/materiales.html`<br>`pages/docente/glosario.html`<br>`pages/docente/participantes.html`<br>`pages/foro/foro-unidad.html` | Planificación de programas, carga horaria, secuenciación cronológica semanal, biblioteca de materiales didácticos, diccionario financiero CNV, roster de participantes y foros de consulta por unidad. |
| **MOD-F-03: Inscripciones & Pagos** | CU-43 a CU-52 | `pages/cursos/mis-cursos.html`<br>`pages/cursos/mi-cursada.html`<br>`pages/alumno/checkout.html`<br>`pages/alumno/pago-resultado.html`<br>`pages/admin/descuentos.html` | Panel del alumno, aula virtual interactiva con porcentaje de avance, checkout con aplicación automática de descuentos por fidelidad, emisión de comprobantes oficiales y administración de políticas comerciales. |
| **MOD-F-04: Evaluaciones** | CU-53 a CU-64 | `pages/alumno/rendir-examen.html`<br>`pages/alumno/resultado-examen.html`<br>`pages/docente/gestionar-pool.html`<br>`pages/docente/autoevaluaciones.html`<br>`pages/docente/intentos-autoevaluacion.html`<br>`pages/docente/calificaciones.html` | Terminal de examen cronometrado con temporizador JavaScript y auto-entrega, informe de calificación con dictamen, banco de preguntas con selector MCQ/VF, configuración de autoevaluaciones, supervisión de intentos y planilla de notas. |
| **MOD-F-05: Clases en Vivo (RTMP)** | CU-65 a CU-72 | `pages/docente/clases-en-vivo.html`<br>`pages/alumno/ver-clase-vivo.html` | Programación de transmisiones en directo, emisión de claves y URLs de stream para OBS Studio, sala de visualización con chat interactivo e incorporación de grabaciones archivadas. |
| **MOD-F-06: IA & Clones HeyGen** | CU-73 a CU-80 | `pages/docente/clon-ia.html`<br>`pages/docente/gestionar-pool.html` (modal Ollama)<br>`pages/docente/gestionar-curso.html` | Aceptación de términos y consentimiento biométrico de avatar/voz HeyGen, renderizado de video-lecciones sintéticas y generación automática de bancos de preguntas/resúmenes con Ollama (Llama 3.1 8B). |
| **MOD-NF-01: Usuarios & Seguridad** | CU-81 a CU-94 | `pages/login.html`<br>`pages/registro.html`<br>`pages/recuperar-contrasena.html`<br>`pages/resetear-contrasena.html`<br>`pages/perfil/verPerfil.html`<br>`pages/perfil/cambiarContrasena.html`<br>`pages/perfil/sesiones.html`<br>`pages/admin/usuarios.html`<br>`pages/admin/nuevo-usuario.html` | Autenticación con credenciales y OAuth2 Google, restablecimiento seguro de clave por token, modificación de datos personales, revocación remota de sesiones activas y administración del padrón de usuarios y roles. |
| **MOD-NF-02: Auditoría** | CU-95 | `pages/admin/auditoria.html` | Panel de consulta del log inmutable de operaciones interceptadas por AOP (altas, modificaciones y bajas lógicas) con filtro por entidad. |
| **MOD-NF-03: Reportes & Métricas** | CU-96 a CU-98 | `pages/admin/reportes.html`<br>`pages/admin/panel.html`<br>`pages/perfil/certificados.html`<br>`pages/alumno/certificado-vista.html` | Tablero ejecutivo en tiempo real, gráficos de líneas y barras horizontales con Chart.js, exportación de balances a PDF y emisión de diplomas digitales con código de verificación criptográfica. |
| **MOD-NF-04: Configuración** | CU-99 | `pages/admin/configuracion.html` | Administración en caliente de parámetros operativos clave-valor del sistema. |

---

## 3. Principales Mejoras Técnicas Implementadas

1. **Resolución Segura de Propiedades SpEL en Thymeleaf:**
   - Estandarización de expresiones de navegación en entidades para evitar `PropertyNotFoundException` o `SpelEvaluationException` en accesos nulos (e.g. `docente.usuario.nombre`, `cohorte.programa.curso.categoria`, `estadoClaseEnVivo.nombre`).
2. **Componentes Reutilizables y Accesibilidad:**
   - Actualización de fragmentos globales (`fragments/head`, `fragments/navbar`, `fragments/footer`, `fragments/scripts`, `fragments/cartaCurso`).
   - Integración de badges de roles dinámicos en el Navbar según el contexto de seguridad de Spring Security (`ROLE_ALUMNO`, `ROLE_DOCENTE`, `ROLE_ADMINISTRADOR`).
3. **Flujos de Usuario Pulidos y Reactivos:**
   - Mensajes flash de confirmación/error integrados en todas las vistas con botones de descarte interactivos.
   - Modales de Bootstrap 5 para creación rápida de preguntas, unidades, categorías y parámetros sin recarga innecesaria de pantalla.
   - Script de temporizador de cuenta regresiva con auto-envío de formulario en `rendir-examen.html`.
   - Soporte para impresión nativa y guardado en PDF de certificados mediante media queries `@media print`.

---

## 4. Estado de Validación y Compilación

- **Compilación de Código Fuente y Recursos:**
  ```powershell
  .\mvnw.cmd test-compile
  # Output: BUILD SUCCESS (0 errores)
  ```
- **Integridad:** 100% de las rutas mapeadas en controladores retornan plantillas existentes y operativas.
