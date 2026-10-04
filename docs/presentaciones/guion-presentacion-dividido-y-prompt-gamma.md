# Guión de Exposición y Prompt Gamma: Sistema Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online  
**Cátedras:** Trabajo Final (ASC) | Proyecto Software (LSI) - FCEQyN - UNaM  
**Disertantes:** Joaquín Küster | Lázaro Martinez  
**Cliente:** Idóneos Online S.A.S. (Fausto Spotorno & Sebastián Bordato)

---

# Parte 1: Guión de Exposición por Diapositiva (Dividido por Disertante)

### Diapositiva 1: Portada y Presentación Institucional
**Orador (Lázaro Martinez):**
"Buenos días profesores del tribunal. Junto a mi compañero Joaquín Küster, presentamos la propuesta de desarrollo del **Sistema de Gestión Idóneos Online**, correspondiente al Trabajo Final de Analista en Sistemas y Proyecto Software de la Licenciatura en Sistemas de Información de la Universidad Nacional de Misiones. Este desarrollo responde a los requerimientos de la empresa Idóneos Online S.A.S., encabezada por el economista Fausto Spotorno y Sebastián Bordato."

---

### Diapositiva 2: 1. Problemática y Desafíos Operativos Reales
**Orador (Joaquín Küster):**
"En Argentina, la formación dictada por directores de maestrías universitarias de élite históricamente estuvo restringida a graduados de carreras de grado. Quienes deseaban formarse en finanzas sin una carrera previa encontraban cursos hipersegmentados en el mercado bursátil o de baja calidad académica y pedagógica.

Frente a esto, Idóneos Online diseñó una propuesta integral (abarcando mercado de capitales, impuestos, contabilidad y flujo de caja) con docentes de primer nivel y una metodología secuencial tipo 'Netflix'. Sin embargo, la operación enfrenta serias dificultades reales:
1. **Dependencia técnica constante:** Cada curso nuevo requiere intervención manual de sistemas para maquetar accesos y simular áreas privadas, impidiendo que docentes y administradores publiquen con autonomía.
2. **Herramientas fragmentadas y desconectadas:** Creación de contenido audiovisual de forma manual mediante métodos tradicionales y correos manuales.
3. **Falta de persistencia y reportes:** Ausencia de base de datos relacional para registrar navegación, notas y trazabilidad de alumnos, impidiendo generar estadísticas de ingresos y métricas comerciales.
4. **Inseguridad operativa:** Inexistencia de una pasarela de pagos integrada y validada, y falta de control de sesiones concurrentes que prevenga el uso compartido de credenciales."

---

### Diapositiva 3: 2. Estudio de Mercado y Propuesta de Valor
**Orador (Lázaro Martinez):**
"Nuestra propuesta de valor resuelve estas barreras mediante cuatro pilares clave:
1. **Docentes de Élite:** Acceso directo y abierto a directores de posgrado para un público objetivo proyectado en más de 2.000 alumnos anuales.
2. **Metodología 'Netflix' Estructurada:** Programas modulares con al menos 10 unidades secuenciales, exigiendo completar la autoevaluación de cada unidad para desbloquear la siguiente.
3. **Inteligencia Artificial Nativa:** Integración de un modelo local de lenguaje (Ollama) para crear resúmenes, presentaciones y bancos de preguntas sin costo por token, junto a clases dictadas por Clones de IA (HeyGen).
4. **Tres Modalidades Integradas:** Cursos Grabados tradicionales, En Vivo mediante streaming sincrónico y con Clones de IA, incorporando además la variante 'Supervisada' con tutoría docente."

---

### Diapositiva 4: 3. Ciclo de Funcionamiento Actual y Cuellos de Botella
**Orador (Joaquín Küster):**
"Actualmente, el flujo de trabajo depende de procesos manuales críticos:
* **Maquetación Manual:** La administración coordina con sistemas la carga artesanal de cada programa y unidad didáctica.
* **Herramientas Desconectadas:** Las clases sincrónicas y los videos de avatares se generan en entornos externos desconectados, sin publicación automatizada.
* **Validación Manual de Pagos:** La validación de pagos se realiza manualmente por correo electrónico, generando demoras en la matriculación.
* **Sin Trazabilidad Académica:** No existe registro del avance de los estudiantes, lo que impide un seguimiento pedagógico real y auditoría financiera.

El nuevo sistema unifica estas áreas en una plataforma integral, relacional, escalable y autogestionable."

---

### Diapositiva 5: 4. Objetivos y Alcance del Sistema
**Orador (Lázaro Martinez):**
"El sistema implementa 10 objetivos específicos alineados a sus módulos, cubriendo el ciclo académico-comercial completo:
* **Catálogo Dinámico y Contenidos:** Administración de cursos, categorías, cohortes, programas, unidades secuenciales, videos locales, glosarios y foros de consulta.
* **Cobro Online y Evaluaciones:** Integración con la pasarela MODO (QR/Deep-link), cupones automáticos, autoevaluaciones con pools de preguntas y emisión automática de certificados en PDF.
* **Streaming e Inteligencia Artificial:** Transmisión en vivo vía OBS con grabación y retención configurable de 4 meses, y generación automática de contenido didáctico con Ollama y HeyGen.
* **Seguridad y Auditoría:** Autenticación por Google OAuth, control estricto de sesiones concurrentes, registro inmutable de auditoría transaccional, reportes gerenciales y configuración clave-valor sin tocar código.

El alcance abarca tres perfiles: **Alumno** (compra, cursada secuencial, foros, exámenes y certificación), **Docente** (gestión académica, streaming, redacción de prompts y validación de clones) y **Administrador** (gestión integral, auditoría, estadísticas y parámetros)."

---

### Diapositiva 6: 5. Limitaciones del Sistema
**Orador (Joaquín Küster):**
"Para delimitar con precisión el proyecto, se establecieron las siguientes limitaciones basadas en la entrevista con el cliente:
* **Política de No Reembolso:** Los pagos no son reembolsables, siguiendo el estándar de formación académica universitaria.
* **Entorno 100% Virtual:** No se gestionan aulas físicas ni cursada presencial.
* **Facturación Fiscal Externa:** Se emiten comprobantes internos automáticos; las facturas A o B se tramitan manualmente por correo electrónico sin conexión directa a AFIP.
* **Certificación Institucional:** Las constancias cuentan con código de verificación único institucional, sin firma digital PKI.
* **Sin Contabilidad ni RRHH:** No se gestionan libros contables societarios ni liquidación de sueldos o haberes de socios.
* **Validación Oficial Externa:** Trámites de homologación ante organismos como la CNV son gestiones institucionales ajenas al sistema.
* **Dependencia de Servicios Externos:** Disponibilidad operativa sujeta a las APIs de MODO, Google OAuth y HeyGen."

---

### Diapositiva 7: 6. Especificación de Módulos (100 Casos de Uso Reales)
**Orador (Lázaro Martinez):**
"El sistema se estructura formalmente en 6 Módulos Funcionales (MOD-F) y 4 Módulos No Funcionales (MOD-NF), abarcando los 100 Casos de Uso Reales:

**Módulos Funcionales (6):**
1. **MOD-F-01: Módulo de Cursos (CU-01 a CU-14):** Catálogo público, búsqueda, cursos, categorías temáticas y cohortes con cupos y fechas.
2. **MOD-F-02: Módulo de Gestión Académica (CU-15 a CU-42):** Programas vigentes, unidades, cronogramas, participantes, acceso Moodle-style (CU-26/26b en Modo Edición), materiales locales, glosarios y foros de consulta/respuesta.
3. **MOD-F-03: Módulo de Inscripciones (CU-43 a CU-52):** Inscripciones a dictados, pagos online con MODO, progreso por unidad, cupones y descuentos automáticos.
4. **MOD-F-04: Módulo de Evaluaciones (CU-53 a CU-64):** Pools de preguntas (opción múltiple y V/F), autoevaluaciones con 10 preguntas (100% de exigencia), historial de intentos, calificaciones y certificados.
5. **MOD-F-05: Módulo de Clases en Vivo (CU-65 a CU-72):** Programación, transmisión RTMP vía OBS, grabación y ciclo de retención de 4 meses con baja automática.
6. **MOD-F-06: Módulo de Generación de Contenido con IA (CU-73 a CU-80):** Generación con LLM local Ollama (preguntas, resúmenes, presentaciones) y clases con Clon de IA vía HeyGen."

**Orador (Joaquín Küster):**
"**Módulos No Funcionales (Soporte, Control y Calidad - 4):**
7. **MOD-NF-01: Módulo de Usuarios y Notificaciones (CU-81 a CU-94):** Registro, gestión de usuarios, validación de docentes (títulos universitarios y matrícula CNV), autenticación con Google OAuth, límite de sesiones concurrentes y correos con JavaMailSender.
8. **MOD-NF-02: Módulo de Auditoría (CU-95):** Registro inmutable de transacciones críticas (pagos, altas, modificaciones) con usuario, IP, fecha/hora y detalle valor anterior/nuevo.
9. **MOD-NF-03: Módulo de Reportes y Estadísticas (CU-96 a CU-98):** Informes consolidados de alumnos e ingresos por curso, y consulta de estadísticas generales en pantalla.
10. **MOD-NF-04: Módulo de Configuración (CU-99):** Administración de parámetros clave-valor (plazos, sesiones, credenciales de API y datos institucionales) sin alterar el esquema del sistema."

---

### Diapositiva 8: 7. Procesos Automatizados del Sistema (PA-1 a PA-9)
**Orador (Joaquín Küster):**
"El sistema implementa 9 Procesos Automatizados como módulos inteligentes encadenados:
* **PA-1: Login con Google** *(Küster - MOD-NF-01 + MOD-NF-04)*: Redirige a Google OAuth, valida token, busca usuario y, si no existe, crea cuenta de Alumno, inicia sesión y notifica el alta.
* **PA-2: Pago online con billetera virtual** *(Martinez - MOD-F-03 + MOD-NF-01 + MOD-NF-04)*: Genera solicitud en MODO, presenta QR en PC o Deep-link en móvil, recibe webhook de acreditación, registra pago, genera comprobante, habilita curso y notifica.
* **PA-3: Aplicación automática de descuentos** *(Küster - MOD-F-03 + MOD-NF-01)*: Evalúa cursos previos comprados, aplica porcentaje al arancel, descuenta cupo, desactiva por fin de vigencia/cupo y notifica.
* **PA-4: Clases en Vivo** *(Martinez - MOD-F-05 + MOD-NF-01 + MOD-NF-04)*: Genera URL RTMP para OBS, transmite a alumnos, graba en servidor, publica grabación por 4 meses (aviso y baja programados) y notifica.
* **PA-5: Generación de videos Clon IA** *(Küster - MOD-F-06 + MOD-F-02 + MOD-NF-01)*: Toma prompt/guión del docente, valida Avatar Consent en HeyGen, sintetiza video, descarga al servidor como grabación oculta y notifica para revisión.
* **PA-6: Emisión automática de certificados** *(Küster - MOD-F-04 + MOD-F-02 + MOD-NF-01)*: Corrige autoevaluación, valida 100% de unidades aprobadas, genera código único institucional, maqueta PDF, envía por correo y publica en perfil.
* **PA-7: Generación de presentaciones de unidad** *(Martinez - MOD-F-02 + MOD-F-06 + MOD-NF-01)*: Envía bibliografía a Ollama local, extrae puntos clave, formatea presentación descargable en estado oculto y notifica.
* **PA-8: Generación de resúmenes de unidad** *(Küster - MOD-F-02 + MOD-F-06 + MOD-NF-01)*: Envía bibliografía a Ollama local, genera síntesis estructurada, carga como material Resumen oculto y notifica.
* **PA-9: Generación de banco de preguntas** *(Martinez - MOD-F-06 + MOD-F-04 + MOD-NF-01)*: Procesa bibliografía y glosario en Ollama local, crea preguntas cerradas (opción múltiple y V/F con proporción configurada), valida estructura y carga al pool de la unidad."

---

### Diapositiva 9: 8. Stack Tecnológico, Decisiones y Metodología
**Orador (Lázaro Martinez):**
"El stack tecnológico fue seleccionado para garantizar máxima robustez y costo cero de infraestructura:
* **Back-End:** Java con Spring Boot, Spring Security (OAuth2), Spring AI (ChatClient Ollama) y JavaMailSender (SMTP Gmail).
* **Front-End:** Thymeleaf, Bootstrap, HTML5 y JavaScript responsivo, con navegación pedagógica tipo Moodle (pestañas contextuales y switch de Modo Edición).
* **Base de Datos y Multimedia:** PostgreSQL relacional para datos transaccionales y sistema de archivos del servidor local para grabaciones y presentaciones.
* **Inteligencia Artificial:** Ollama local (Llama 3.1 8B sin costos por token) + HeyGen API para Clones de voz y video.
* **Streaming y Pagos:** OBS Studio (protocolo RTMP) + MODO API (QR/Deep-link con entorno Testing).
* **Metodología:** Proceso Unificado (UP) estructurado en 82 días de desarrollo iterativo junto al Cliente Final.

Agradecemos su atención y quedamos a disposición del tribunal para las preguntas."

---

# Parte 2: Prompt Copiable para Gamma App

```text
Crea una presentación ejecutiva y académica de 9 diapositivas para la defensa de Trabajo Final universitario titulada: "Sistema de Gestión Idóneos Online - Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales".

ESTILO VISUAL Y DISEÑO:
- Estética: Minimalista, moderna, limpia y estructurada (espacio en blanco, lectura ágil, tarjetas ejecutivas).
- Paleta de Colores:
  * Fondo: Azul marino institucional profundo (#0A192F) o Blanco puro (#FFFFFF).
  * Acentos principales: Dorado / Oro suave (#D4AF37 / #E5C07B) para resaltar conceptos clave, iconos y métricas.
  * Texto primario: Azul marino intenso (#0F172A) o Blanco puro en dark mode.
  * Texto secundario: Gris pizarra neutro (#475569 / #94A3B8).
- Tipografía: Segoe UI / Inter / Plus Jakarta Sans con jerarquía limpia.
- Formato visual: Cards de contenido, grids y bullets concisos sin sobrecarga de texto.

---

Slide 1: Portada y Presentación
- Título Principal: Sistema de Gestión Idóneos Online
- Subtítulo: Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales
- Autores: Joaquín Küster & Lázaro Martinez
- Marco Académico: Trabajo Final (ASC) | Proyecto Software (LSI) - FCEQyN - UNaM
- Cliente Final: Idóneos Online S.A.S. (Fausto Spotorno & Sebastián Bordato)

---

Slide 2: 1. Problemática y Desafíos Operativos Reales
- Contexto Institucional: Idóneos Online S.A.S. impulsada por Fausto Spotorno (director académico) y Sebastián Bordato (ideólogo y docente), con público de 22 a 45 años en Argentina y proyección de +2.000 alumnos anuales.
- Vacío de Mercado: Formación con directores de maestría antes restringida a graduados de grado; oferta existente hipersegmentada en mercado de capitales o con deficiencias pedagógicas.
- Propuesta Integral: Temario amplio (mercado bursátil, contabilidad, impuestos, flujo de caja), docentes de élite y avance secuencial tipo "Netflix".
- Desafíos Operativos Reales:
  * Dependencia Técnica: Carga manual de cursos por personal de sistemas, sin autonomía para docentes ni administradores.
  * Herramientas Fragmentadas: Creación de contenido audiovisual de forma manual mediante métodos tradicionales y correos manuales.
  * Sin Persistencia ni Reportes: Ausencia de base de datos relacional para registrar navegación, calificaciones ni estadísticas de ingresos.
  * Inseguridad Operativa: Inexistencia de pasarela de pagos integrada y falta de control de sesiones concurrentes contra el uso compartido de credenciales.

---

Slide 3: 2. Estudio de Mercado y Propuesta de Valor
- Factibilidad e Innovación en Formación Financiera:
  1. Docentes de Élite: Directores de maestría universitaria de primer nivel accesibles a todo público.
  2. Metodología "Netflix" Estructurada: Avance secuencial por unidades (+10 unidades por curso con grabaciones, glosario, bibliografía, resumen y autoevaluación obligatoria).
  3. Integración Nativa de IA: Generación de resúmenes, presentaciones y pools de preguntas con LLM local Ollama, y clases dictadas por Clones de IA (HeyGen).
  4. 3 Modalidades Unificadas: Grabadas, En Vivo (OBS con 4 meses de disponibilidad) y Clones IA, con variante de dictado Supervisado.

---

Slide 4: 3. Ciclo de Funcionamiento Actual y Cuellos de Botella
- Diagnóstico del Escenario Operativo Previo:
  * Maquetación Manual: Creación artesanal de accesos y páginas por curso sin autonomía docente.
  * Herramientas Desconectadas: Transmisiones y avatares generados en entornos externos sin publicación automática.
  * Sin Trazabilidad: Falta de registro de progreso de alumnos, historial de autoevaluaciones ni analítica académica.
  * Gestión Manual de Pagos: Validación de pagos y matrículas por correo electrónico, generando demoras.
  * Ausencia de Métricas: Imposibilidad de generar reportes consolidados de recaudación e inscripciones.

---

Slide 5: 4. Objetivos y Alcance del Sistema
- 10 Objetivos Principales (Alineados a los 10 Módulos):
  * Catálogo Dinámico: Cursos, categorías, cohortes, programas vigentes (mínimo 10 unidades) y dictados con cupos y fechas.
  * Contenido Didáctico: Materiales en servidor local (videos, lecturas, presentaciones), glosarios y foros de consulta.
  * Cobro Online y Descuentos: Integración con pasarela MODO (QR/Deep-link), cupones automáticos y comprobantes.
  * Evaluación y Certificación: Pools de preguntas, autoevaluaciones con 100% de exigencia y certificados en PDF.
  * Streaming Sincrónico: Transmisión vía OBS RTMP con grabación y retención automática de 4 meses.
  * Asistente con IA: Generación de contenido con Ollama local y clases con Avatar Clon vía HeyGen.
  * Control de Usuarios: Autenticación con Google OAuth, control de sesiones concurrentes y correos con JavaMailSender.
  * Auditoría y Reportes: Registro inmutable de transacciones críticas, informes gerenciales y configuración clave-valor.
- Alcance por Perfil (Web Responsive):
  * Alumno: Registro, compra online, cursada secuencial por unidades, foros, autoevaluaciones y certificados.
  * Docente: Carga de material didáctico, streaming con OBS, redacción de prompts para IA y clases con Clon.
  * Administración: Gestión de usuarios y títulos docentes, control de cohortes, auditoría, estadísticas y parámetros.

---

Slide 6: 5. Limitaciones del Sistema
- Delimitación Oficial del Sistema (Relevada con el Cliente):
  * Política de No Reembolso: Pagos finales no reembolsables según estándar de formación universitaria.
  * Entorno 100% Virtual: Sin gestión de aulas físicas ni clases presenciales.
  * Facturación Fiscal Externa: Comprobantes internos automáticos; facturas A/B con AFIP se tramitan manualmente por correo.
  * Certificación Institucional: Código de verificación único institucional sin firma digital certificada PKI.
  * Sin Contabilidad ni RRHH: Sin libros contables societarios ni liquidación de sueldos o haberes de socios.
  * Validación Oficial Externa: Homologación ante CNV u organismos gubernamentales son gestiones institucionales ajenas al sistema.
  * Dependencia de Servicios Externos: Disponibilidad operativa sujeta a APIs de MODO, Google OAuth y HeyGen.

---

Slide 7: 6. Especificación de Módulos (100 Casos de Uso Reales)
• Módulos Funcionales (MOD-F):
  1. MOD-F-01: Módulo de Cursos (CU-01 a CU-14): Catálogo público, búsqueda, cursos, categorías temáticas y cohortes con cupos y ventanas de inscripción.
  2. MOD-F-02: Módulo de Gestión Académica (CU-15 a CU-42): Programas vigentes, unidades, cronogramas, participantes, acceso al curso Moodle-Style (CU-26/26b en Modo Edición), materiales locales, glosarios y foros de consulta/respuesta.
  3. MOD-F-03: Módulo de Inscripciones (CU-43 a CU-52): Inscripciones a dictados, pasarela de cobro MODO, comprobantes descargables, progreso de unidades y cupones de descuento.
  4. MOD-F-04: Módulo de Evaluaciones (CU-53 a CU-64): Pools de preguntas (opción múltiple y V/F), autoevaluaciones con 10 preguntas aleatorias (100% de exigencia), historial de intentos, calificaciones y certificados.
  5. MOD-F-05: Módulo de Clases en Vivo (CU-65 a CU-72): Programación, transmisión RTMP vía OBS, grabación y ciclo de retención de 4 meses con baja automática.
  6. MOD-F-06: Módulo de Generación de Contenido con IA (CU-73 a CU-80): Prompts y LLM local Ollama (preguntas, resúmenes, presentaciones) y clases con Avatar Clon vía HeyGen.
• Módulos No Funcionales (MOD-NF):
  7. MOD-NF-01: Módulo de Usuarios y Notificaciones (CU-81 a CU-94): Alumnos, docentes (validación de títulos y matrícula CNV), administradores, Google OAuth, límite de sesiones y emails por JavaMailSender.
  8. MOD-NF-02: Módulo de Auditoría (CU-95): Registro inmutable de transacciones críticas (pagos, altas, modificaciones) con usuario, IP, fecha/hora y detalle valor anterior/nuevo.
  9. MOD-NF-03: Módulo de Reportes y Estadísticas (CU-96 a CU-98): Informes consolidados de alumnos e ingresos por curso, y KPIs generales en pantalla.
  10. MOD-NF-04: Módulo de Configuración (CU-99): Parámetros operativos clave-valor (plazos, sesiones, credenciales API y datos institucionales).

---

Slide 8: 7. Procesos Automatizados (PA-1 a PA-9: Desglose y Módulos)
- PA-1: Login con Google | Resp: Küster
  * Módulos: MOD-NF-01 (Usuarios y Notificaciones) + MOD-NF-04 (Configuración).
  * Flujo: (1) Redirección a Google OAuth; (2) Recepción de token; (3) Búsqueda por email; (4) Si no existe, crea cuenta Alumno, inicia sesión y notifica alta.
- PA-2: Pago online con billetera virtual | Resp: Martinez
  * Módulos: MOD-F-03 (Inscripciones) + MOD-NF-01 (Usuarios y Notificaciones) + MOD-NF-04 (Configuración).
  * Flujo: (1) Solicitud a MODO (QR/Deep-link); (2) Presentación de QR/modal; (3) Pago por el alumno; (4) Webhook de confirmación; (5) Registro de pago, comprobante, habilitación de curso y notificación.
- PA-3: Aplicación automática de descuentos | Resp: Küster
  * Módulos: MOD-F-03 (Inscripciones) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Evaluación de cursos previos; (2) Aplicación del % al arancel; (3) Descuento de cupo; (4) Desactivación por fin de vigencia/cupo y notificación.
- PA-4: Clases en Vivo | Resp: Martinez
  * Módulos: MOD-F-05 (Clases en Vivo) + MOD-NF-01 (Usuarios y Notificaciones) + MOD-NF-04 (Configuración).
  * Flujo: (1) Genera RTMP para OBS; (2) Transmite a alumnos y graba; (3) Publica grabación por 4 meses (aviso y baja programados); (4) Notifica a inscriptos.
- PA-5: Generación de videos Clon IA | Resp: Küster
  * Módulos: MOD-F-06 (Generación IA) + MOD-F-02 (Gestión Académica) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Docente ingresa prompt/guión y envía a HeyGen con Avatar Consent; (2) HeyGen genera video; (3) Descarga local y carga como Grabación oculta; (4) Notifica al docente.
- PA-6: Emisión automática de certificados | Resp: Küster
  * Módulos: MOD-F-04 (Evaluaciones) + MOD-F-02 (Gestión Académica) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Corrección y verificación de 100% de unidades aprobadas; (2) Generación de ID único; (3) Maquetación y envío de PDF por email; (4) Disponible en perfil.
- PA-7: Generación de presentaciones de unidad | Resp: Martinez
  * Módulos: MOD-F-02 (Gestión Académica) + MOD-F-06 (Generación IA) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Envío de bibliografía a Ollama local; (2) Ollama estructura puntos clave; (3) Formateo como presentación descargable oculta; (4) Notificación docente.
- PA-8: Generación de resúmenes de unidad | Resp: Küster
  * Módulos: MOD-F-02 (Gestión Académica) + MOD-F-06 (Generación IA) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Envío de bibliografía a Ollama local; (2) Ollama genera resumen en texto; (3) Formateo como material Resumen oculto; (4) Notificación docente.
- PA-9: Generación de banco de preguntas | Resp: Martinez
  * Módulos: MOD-F-06 (Generación IA) + MOD-F-04 (Evaluaciones) + MOD-NF-01 (Usuarios y Notificaciones).
  * Flujo: (1) Envío de bibliografía y glosario a Ollama local; (2) Ollama crea preguntas de opción múltiple y V/F con proporción configurada; (3) Carga al pool de preguntas; (4) Notificación docente.

---

Slide 9: 8. Stack Tecnológico, Decisiones y Metodología
- Arquitectura y Entorno Técnico:
  * Back-End: Java con Spring Boot, Spring Security (OAuth2), Spring AI (ChatClient Ollama) y JavaMailSender (SMTP Gmail).
  * Front-End: Thymeleaf, Bootstrap, HTML5 y JavaScript responsivo, interfaz pedagógica estilo Moodle con pestañas contextuales y Modo Edición inline.
  * Base de Datos y Almacenamiento: PostgreSQL relacional y sistema de archivos local para grabaciones/multimedia.
  * Inteligencia Artificial: Ollama local (Llama 3.1 8B sin costos por token) + HeyGen API (Clones).
  * Streaming & Cobros: OBS Studio (protocolo RTMP) + MODO API (QR/Deep-link con APK Testing).
  * Metodología: Proceso Unificado (UP) estructurado en 82 días para desarrollo con Cliente Final.
```
