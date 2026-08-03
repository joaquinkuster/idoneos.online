# Sistema de Gestión Idóneos Online

**Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales**  
*Idóneos Online S.A.S. · Argentina*

---

## 🏛️ Información Académica

- **Universidad**: Universidad Nacional de Misiones (UNaM)
- **Facultad**: Facultad de Ciencias Exactas, Químicas y Naturales (FCEQyN)
- **Carreras**:
  - Licenciatura en Sistemas de Información (Plan 2013) — Cátedra: *Proyecto Software*
  - Analista en Sistemas de Computación (Plan 2010) — Cátedra: *Trabajo Final*
- **Profesor Adjunto**: Lic. Sergio Daniel Caballero
- **Autores**:
  - **Küster Joaquín** (Matrícula: `906560` — DNI: `44.652.101`) · GitHub: [@joaquinkuster](https://github.com/joaquinkuster)
  - **Martinez Lazaro Ezequiel** (Matrícula: `906047` — DNI: `42.086.981`) · GitHub: [@lazamartinez](https://github.com/lazamartinez)
- **Fecha de Presentación**: Apóstoles (Misiones), 2026

---

## 📋 Descripción del Proyecto

**Idóneos Online** surge como respuesta a las limitaciones detectadas en la plataforma estática preliminar desarrollada en Wix. El sistema proporciona una solución tecnológica dinámica, escalable y propia para dar soporte integral a la operación académica, comercial y administrativa de la empresa.

### Diferenciales de Valor
1. **Cuerpo Docente de Élite**: Directores de maestría universitaria (UADE, UTDT, UCA) y economistas de referencia (Fausto Spotorno, Sebastián Bordato).
2. **Tres Modalidades de Dictado**:
   - **En vivo**: Transmisión sincrónica vía OBS Studio y streaming RTMP con chat en vivo.
   - **Grabada**: Cursada asincrónica con avance secuencial obligado por autoevaluaciones.
   - **Clon con IA**: Clones sintéticos que reproducen la imagen y voz reales del docente (vía HeyGen API).
3. **Generación de Contenido con Inteligencia Artificial**: Integración con **Google Gemini 3.1 Pro** y **Whisper STT** para la generación automática de bancos de preguntas, resúmenes de clases y estructuras de presentaciones.

---

## 💻 Entorno Tecnológico y Arquitectura

- **Lenguaje**: Java 21
- **Framework Back-End**: Spring Boot 3.4.0 (Spring Security 6, Spring Data JPA, Spring AOP)
- **Plantillas Front-End**: Thymeleaf, HTML5, Vanilla CSS3 (Diseño institucional Navy/Gold), Bootstrap 5.3
- **Base de Datos**: PostgreSQL / H2 Database en memoria (despliegue local de prueba)
- **Tipografías**: Google Fonts (*Fraunces* Serif Editorial & *Inter* Sans-serif)
- **Iconografía**: FontAwesome 6 Pro

---

## 🧩 Especificación de Módulos (10 Módulos)

| Módulo | Descripción | Responsable |
|---|---|---|
| **Módulo de Cursos** | Catálogo temático, estructura secuencial en 10+ unidades, gestión de materiales (grabaciones, bibliografía, resúmenes, presentaciones) y modalidades de dictado. | Martinez Lazaro Ezequiel |
| **Módulo de Inscripción y Pagos** | Checkout con tarjeta de crédito/débito directamente en la plataforma vía Mercado Pago API, promociones/descuentos automáticos y comprobantes digitales. | Martinez Lazaro Ezequiel |
| **Módulo de Evaluación y Certificación** | Pools de preguntas, autoevaluaciones con temporizador en tiempo real, corrección automática e historial de intentos. Generación automática de certificados `CERT-YYYY-000XXX`. | Küster Joaquín |
| **Módulo Clon con IA** | Verificación de consentimiento (*Avatar Consent*), prompt input de guion y generación asincrónica de video con avatar HeyGen. | Küster Joaquín |
| **Módulo Clases en Vivo** | Programación, inicio/finalización de clases, generación de credenciales RTMP para OBS Studio y chat en directo. | Martinez Lazaro Ezequiel |
| **Módulo Generación de Contenido con IA** | Generación asistida por Gemini 3.1 Pro y Whisper STT de pools de examen, resúmenes estructurados y diapositivas. | Küster Joaquín |
| **Módulo de Usuarios y Notificaciones** | Perfiles de Alumno, Docente y Administrador con autenticación Spring Security, control de sesiones concurrentes e historial de notificaciones. | Küster Joaquín |
| **Módulo de Auditoría** | Trazabilidad automática con Spring AOP que registra usuario, fecha, hora, tipo de acción (Crear, Modificar, Eliminar) y entidad afectada. | Martinez Lazaro Ezequiel |
| **Módulo de Reportes y Estadísticas** | Dashboard KPI con indicadores en tiempo real de alumnos inscriptos, cursos cargados, inscripciones e ingresos acumulados en ARS. | Küster Joaquín |
| **Módulo de Configuración** | Administración dinámica de parámetros operativos clave-valor (`plataforma.nombre`, `evaluacion.umbral_aprobacion`, etc.). | Martinez Lazaro Ezequiel |

---

## ⚡ Procesos Automatizados del Sistema (PA-1 a PA-9)

- **PA-1: Login y Autenticación de Usuarios**: Alta automática de cuenta con asignación de subtipo referencial.
- **PA-2: Pago con tarjeta integrado (Mercado Pago)**: Validación de tarjeta, acreditación en línea y habilitación inmediata de la cursada.
- **PA-3: Aplicación automática de descuentos**: Evaluación de condiciones según historial de compras del alumno y límite ofertado.
- **PA-4: Clases en Vivo**: Ciclo RTMP/OBS, streaming simultáneo y conversión a grabación temporal.
- **PA-5: Generación de videos Clon IA**: Envío de prompt guion a HeyGen y recepción de material oculto para revisión docente.
- **PA-6: Emisión automática de certificados**: Verificación de aprobación del 100% de las unidades y generación de constancia digital.
- **PA-7: Generación de presentaciones con IA**: Estructuración de diapositivas mediante Gemini API.
- **PA-8: Generación de resúmenes de clases**: Transcripción de audio con Whisper y resumen estructurado con Gemini.
- **PA-9: Generación de banco de preguntas**: Creación automática de pools de opción múltiple y verdadero/falso.

---

## 🚀 Instrucciones de Ejecución Local

### 1. Clonar el repositorio
```bash
git clone https://github.com/joaquinkuster/idoneos.online.git
cd idoneos.online
```

### 2. Ejecutar la aplicación
El proyecto está configurado con H2 en memoria para una ejecución local inmediata sin necesidad de configurar una base de datos externa:

```bash
.\mvnw.cmd spring-boot:run
```

### 3. Acceder al sistema
Abre tu navegador en: [http://localhost:8080](http://localhost:8080)

---

## 🔑 Credenciales de Prueba (Semilla de Datos)

| Rol | Email | Contraseña |
|---|---|---|
| **Administrador** | `admin@idoneos.online` | `123456` |
| **Docente Titular** | `fausto.spotorno@idoneos.online` | `123456` |
| **Docente Supervisor** | `sebastian.bordato@idoneos.online` | `123456` |
| **Alumno** | `alumno@correo.com` | `123456` |

---

## 📄 Licencia y Derechos

© 2026 **Idóneos Online S.A.S.** — Desarrollado por Küster Joaquín y Martinez Lazaro Ezequiel para FCEQyN - UNaM.
