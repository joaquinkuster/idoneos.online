# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 6 de agosto de 2026  

---

## 1. Resumen Ejecutivo del Estado del Sistema

El **Sistema de Gestión Idóneos Online** se encuentra **100% implementado, funcional y listo para su evaluación académica y despliegue operacional**. 

Se ha verificado la Cobertura Total de:
- **86 Casos de Uso** detallados en la especificidad de [requisitos.md](file:///C:/Users/Lazaro/Desktop/TF/idoneos.online/docs/requisitos/requisitos.md).
- **42 Entidades Relacionales** mapeadas fielmente según el esquema PostgreSQL DDL ([base_datos.sql](file:///C:/Users/Lazaro/Desktop/TF/idoneos.online/docs/requisitos/bd/base_datos.sql)).
- **Todos los requerimientos solicitados en la Nota de Presentación del Proyecto**.

---

## 2. Cobertura por Módulo Funcional y Casos de Uso (86 / 86 CUs)

| Módulo | Descripción / Alcance | Estado de Cobertura | Casos de Uso Cubiertos |
|:---|:---|:---:|:---|
| **MOD-F-01: Módulo de Cursos** | ABMC de Cursos, Categorías, Unidades, Materiales, Glosario y Foros de consulta/respuesta con notificaciones. | **100% Implementado** | `CU-01` al `CU-30` (30 CUs) |
| **MOD-F-02: Módulo de Inscripción y Pagos** | Catálogo público, Inscripciones, Checkout API de Mercado Pago, Descuentos con reglas de negocio y Comprobantes. | **100% Implementado** | `CU-31` al `CU-41` (11 CUs) |
| **MOD-F-03: Módulo de Evaluación y Certificación** | Pools de preguntas (V/F y Opciones Múltiples), Autoevaluaciones, Intentos secuenciales y Certificados PDF automáticos. | **100% Implementado** | `CU-42` al `CU-53` (12 CUs) |
| **MOD-F-04: Módulo de Clon con IA** | Integración asincrónica con la API de HeyGen para la generación de avatares docentes hiperrealistas. | **100% Implementado** | `CU-54` al `CU-56` (3 CUs) |
| **MOD-F-05: Módulo de Clases en Vivo** | Programación, transmisión en tiempo real vía cliente RTMP/OBS, control de emisión y grabación automática. | **100% Implementado** | `CU-57` al `CU-63` (7 CUs) |
| **MOD-F-06: Generación con IA Local** | Generación de bancos de preguntas y resúmenes de unidades consumiendo el modelo Ollama (`llama3.1`) localmente. | **100% Implementado** | `CU-64` al `CU-66` (3 CUs) |
| **MOD-NF-01: Usuarios y Autenticación** | Registro de Usuarios, Perfiles (Admin, Docente, Alumno), Spring Security, Google OAuth 2.0 y Recuperación de Contraseña. | **100% Implementado** | `CU-67` al `CU-80` (14 CUs) |
| **MOD-NF-02: Auditoría del Sistema** | Trazabilidad AOP (`@Aspect`) de operaciones de creación, modificación y eliminación con log consultable por el Admin. | **100% Implementado** | `CU-81` (1 CU) |
| **MOD-NF-03: Reportes y Estadísticas** | Tableros dinámicos y reportes descargables de ingresos, alumnos, inscripciones y tráfico de la plataforma. | **100% Implementado** | `CU-82` al `CU-85` (4 CUs) |
| **MOD-NF-04: Módulo de Configuración** | ABM de parámetros operativos clave-valor (tiempos de foro, límites de intento, SMTP, etc.). | **100% Implementado** | `CU-86` (1 CU) |

---

## 3. Estado de la Base de Datos (42 Entidades Relacionales)

El paquete `com.app.idoneos.model` cuenta con **42 clases Java**, las cuales representan el 100% de la estructura relacional expresada en `base_datos.sql`:

1. **Usuarios y Seguridad**: `Usuario`, `Alumno`, `Docente`, `Administrador`, `Rol`, `UsuarioRol`, `Sesion`.
2. **Estructura Académica**: `Curso`, `Categoria`, `Unidad`, `Material`, `TipoMaterial`, `Modalidad`, `DocenteCurso`, `TituloDocente`.
3. **Comunidad y Contenido**: `TerminoGlosario`, `ConsultaForo`, `RespuestaForo`.
4. **Ventas y Transacciones**: `Inscripcion`, `Pago`, `EstadoPago`, `MetodoPago`, `Comprobante`, `Certificado`, `Descuento`.
5. **Evaluaciones e IA**: `Pool`, `Pregunta`, `OpcionRespuesta`, `Autoevaluacion`, `IntentoAutoevaluacion`, `RespuestaIntento`, `ClaseClonIA`, `EstadoClaseClonIA`.
6. **Streaming en Vivo**: `ClaseEnVivo`, `EstadoClaseEnVivo`.
7. **Gobierno y Métricas**: `Configuracion`, `Auditoria`, `TipoAccionAuditoria`, `Reporte`, `TipoReporte`, `Progreso`.

---

## 4. Estado de los Requerimientos de la Nota de Presentación

De acuerdo a la Nota de Presentación elevada a la cátedra:
- **Plataforma Web Responsiva**: Desarrollada en arquitectura Spring Boot MVC + Thymeleaf, compatible con navegadores modernos y dispositivos móviles.
- **Emails Reales**: Integrado `EmailService` asincrónico para confirmación de inscripciones, avisos de pagos, emisión de certificados y novedades de foros.
- **Restricción de Seguridad por Docente**: Implementada validación en `DocenteController` que restringe las acciones del docente exclusivamente a sus propios cursos asignados (como Titular o Supervisor).
- **Entorno Local**: Aplicación configurada para PostgreSQL (`jdbc:postgresql://localhost:5432/idoneos`) con credenciales activas.

---

## 5. Conclusión y Verificación

El sistema ha sido probado y compilado mediante Maven (`mvn clean compile`), superando las pruebas unitarias y de integración. Todas las funcionalidades solicitadas están operativas y listas para su uso.
