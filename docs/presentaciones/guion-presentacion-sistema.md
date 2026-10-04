# Guion de Presentación: Sistema de Gestión Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online | **Cátedras:** Trabajo Final (ASC) / Proyecto Software (LSI) - UNaM  
**Integrantes:** Küster Joaquín - Martinez Lazaro Ezequiel | **Cliente Final:** Idóneos Online S.A.S. (Fausto Spotorno - Sebastián Bordato)

---

### 1. Problemática
Idóneos Online es un proyecto impulsado por el economista Fausto Spotorno (director académico) y Sebastián Bordato (director ejecutivo y docente), constituido como S.A.S. con CUIT. Apunta a capacitar a más de 2.000 alumnos anuales en finanzas, economía e impuestos con foco en la normativa argentina. Históricamente, el acceso a directores de maestría estaba restringido a graduados universitarios, mientras que la oferta de mercado (como BYMA o cursos comerciales) es hipersegmentada o de baja calidad académica y audiovisual.
Actualmente operan con una landing page en Wix que no escala: cada curso nuevo exige maquetación manual por un técnico, carece de panel de gestión, no persiste datos en base de datos, las herramientas (OBS y HeyGen) funcionan de forma aislada, no hay pasarela de pago validada ni control de sesiones concurrentes. Por ello, se propone el Sistema Idóneos Online para centralizar y automatizar toda la operación.

### 2. Estudio de Mercado
El proyecto es factible e innovador al cubrir un vacío formativo integrando:
1. **Docentes de élite:** Directores de maestría universitaria de primer nivel accesibles a todo público.
2. **Metodología secuencial ("Netflix"):** Cursos de al menos 10 unidades con avance condicionado a autoevaluaciones obligatorias y alta calidad visual.
3. **Integración nativa de IA:** Generación de resúmenes, presentaciones y exámenes con Ollama local, y clases con Clones de IA (HeyGen).
4. **Tres modalidades en una plataforma:** Grabadas, en vivo sincrónicas vía OBS (grabación con 4 meses de disponibilidad) y Clones de IA, con opción de supervisión de cátedra.

### 3. Ciclo de Funcionamiento Actual
El relevamiento con Sebastián Bordato evidenció los siguientes cuellos de botella:
- **Maquetación artesanal:** Creación manual de páginas por curso en Wix sin autonomía docente/administrativa.
- **Herramientas desconectadas:** Uso fragmentado de HeyGen, OBS y Gmail sin trazabilidad centralizada.
- **Sin persistencia ni seguridad:** Ausencia de base de datos para usuarios/progreso y falta de control de sesiones concurrentes.
- **Comercialización manual:** Sin pasarela de pagos integrada ni reportes centralizados de alumnos e ingresos.

### 4. Objetivos y Alcance
- **Objetivos:** Gestionar el catálogo dinámico, estructurar contenidos (videos locales, glosarios, foros), procesar pagos online con MODO, evaluar secuencialmente y certificar, soportar streaming OBS, asistir al docente con IA (Ollama y HeyGen), controlar usuarios con Google OAuth y auditar operaciones críticas.
- **Alcance por perfil (Web Responsive):**
  - **Alumno:** Registro, compra con MODO, cursada secuencial, resolución de autoevaluaciones, foros y certificados.
  - **Docente:** Carga de contenidos, clases en vivo vía OBS, redacción de prompts para IA y clases con Clon.
  - **Administración:** Gestión de usuarios, verificación de títulos docentes, control de dictados, auditoría, estadísticas y configuración.

### 5. Limitaciones
- **Sin reembolsos:** Política estándar universitaria confirmada por el cliente.
- **100% Virtual:** No contempla clases presenciales ni gestión de aulas físicas.
- **Facturación fiscal externa:** Comprobantes automáticos internos; facturas A/B con AFIP se tramitan manualmente por email.
- **Certificados institucionales:** Verificación con código único institucional sin firma digital PKI.
- **Sin contabilidad/RRHH:** Sin libros societarios ni liquidación de haberes. Dependencia operativa de APIs externas (MODO, HeyGen, Google).

### 6. Módulos del Sistema (10 Módulos - Distribución 48% Küster / 52% Martinez)
- **Módulos Funcionales (6):** Catálogo de Cursos (14% - Martinez), Contenido de Unidades (12% - Martinez), Inscripciones y Pagos (14% - Martinez), Evaluaciones (11% - Küster), Clases en Vivo OBS (9% - Martinez) y Generación de Contenido con IA (11% - Küster).
- **Módulos de Soporte (4):** Usuarios y Notificaciones (17% - Küster), Auditoría (3% - Martinez), Reportes y Estadísticas (6% - Küster) y Configuración (3% - Küster).

### 7. Procesos Automatizados (PA-01 a PA-09)
- **PA-1: Login con Google (Küster):** OAuth2 crea automáticamente la cuenta Alumno e inicia sesión.
- **PA-2: Pago online con MODO (Martinez):** Cobro vía QR/Deep-link; al confirmarse habilita el acceso y emite comprobante.
- **PA-3: Aplicación automática de descuentos (Küster):** Evalúa compras previas, aplica descuento y descuenta cupo.
- **PA-4: Clases en Vivo (Martinez):** Genera RTMP para OBS, transmite, guarda grabación y programa baja a los 4 meses.
- **PA-5: Generación de videos Clon IA (Küster):** Envía guión a HeyGen, descarga video y lo asocia en estado oculto.
- **PA-6: Emisión automática de certificados (Küster):** Al aprobar la última unidad al 100%, emite PDF con código único.
- **PA-7: Generación de presentaciones con IA (Martinez):** Ollama estructura diapositivas a partir de la bibliografía.
- **PA-8: Generación de resúmenes con IA (Küster):** Ollama genera síntesis de unidad desde la bibliografía.
- **PA-9: Generación de bancos de preguntas (Martinez):** Ollama crea preguntas cerradas y las añade al pool de evaluación.

### 8. Stack Tecnológico
- **Back-End:** Java con Spring Boot, Spring Security (OAuth2), Spring AI (Ollama) y JavaMailSender (SMTP Gmail).
- **Front-End:** Thymeleaf, Bootstrap, HTML5 y JavaScript responsivo.
- **Base de Datos y Almacenamiento:** PostgreSQL relacional y sistema de archivos local para multimedia.
- **Integraciones:** Ollama local (Llama 3.1 8B), HeyGen API, OBS Studio (RTMP) y MODO API.
- **Metodología:** Proceso Unificado (UP) estructurado en 82 días para desarrollo con Cliente Final.
