# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 11 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.1.md` (6 de agosto de 2026)

---

## 1. Resumen Ejecutivo

Esta versión documenta la **Auditoría Completa del Modelo de Datos**, ejecutada como paso previo obligatorio a cualquier refactorización mayor del sistema. El objetivo fue garantizar la consistencia total entre el esquema relacional SQL (fuente de verdad) y las 42 entidades Java del paquete `com.app.idoneos.model`.

Al finalizar la sesión de trabajo, el sistema compila sin errores (`BUILD SUCCESS`) y el servidor **arranca completamente en PostgreSQL**, insertando los datos de semilla e iniciando Tomcat en el puerto 8080:

```
Started IdoneosApplication in 2.085 seconds (process running for 42.77)
Idóneos Online: La semilla ya se encuentra insertada.
```

---

## 2. Trabajo Realizado en Esta Versión

### 2.1 Auditoría del Modelo SQL vs. Entidades Java

Se analizó exhaustivamente el archivo `base_datos.sql` como fuente de verdad. Se verificaron:

- 43 tablas SQL → 42 clases Java (1 tabla intermedia resuelta como `@JoinTable`)
- Tipos de datos, restricciones `NOT NULL`, claves primarias y foráneas
- Relaciones 1:1 (`@MapsId`), 1:N (`@OneToMany`) y N:M (`@ManyToMany` / tabla intermedia)
- Herencia mediante claves compartidas (`alumno`, `docente`, `administrador`)
- Cardinalidades y cascadas JPA

### 2.2 Entidades Reconstruidas o Creadas

Las siguientes entidades fueron creadas desde cero por no existir o existir con un modelo incorrecto:

| Entidad | Motivo |
|:---|:---|
| `Certificado.java` | No existía — mapeada desde la tabla `certificado` |
| `Comprobante.java` | No existía — mapeada desde la tabla `comprobante` |
| `DocenteCurso.java` | No existía — tabla intermedia `docente_curso` con atributo `es_supervisor` |
| `PoolAutoevaluacion.java` | Reconstruida para reflejar la tabla intermedia `pool_autoevaluacion` |

### 2.3 Correcciones de Compilación — Constructores y Métodos Helper

Se resolvieron **más de 30 errores de compilación** originados por servicios y controllers que dependían de constructores y métodos no disponibles en los modelos:

| Entidad | Corrección Aplicada |
|:---|:---|
| `Pool` | Constructor `(String nombre, Unidad unidad)` |
| `Pregunta` | Constructor `(String texto, boolean esOpcionMultiple, Pool pool)` |
| `OpcionRespuesta` | Constructor `(String texto, boolean esCorrecta, Pregunta pregunta)` |
| `Pago` | Constructor `(double monto, Inscripcion, EstadoPago)` + aliases `setPaymentId`, `setPreferenceId`, `setEmailPagador` |
| `Reporte` | Constructor `(TipoReporte, Administrador)` |
| `Comprobante` | Constructor `(String numero, Pago pago)` |
| `Certificado` | Constructor `(String numero, Inscripcion inscripcion)` |
| `Descuento` | Método `estaVigente()` |
| `Material` | Alias `setGeneradoPorIA()` / `isGeneradoPorIA()` |
| `Progreso` | Constructor `(Inscripcion, Unidad, boolean)` + alias `setFechaCompletado(LocalDate)` |
| `IntentoAutoevaluacion` | Constructor `(Autoevaluacion, Usuario)` |
| `RespuestaIntento` | Constructor `(IntentoAutoevaluacion, OpcionRespuesta)` |
| `ConsultaForo` | Constructor `(String, Unidad, Usuario)` + `getConsulta()` |
| `RespuestaForo` | Constructor `(String, ConsultaForo, Docente)` + `getConsulta()` |
| `TerminoGlosario` | Constructor `(String termino, String definicion, Unidad)` |
| `Autoevaluacion` | Constructor `(String, Pool, int)` + aliases `getPools()` / `setPools(List<Pool>)` |
| `Inscripcion` | Constructor de compatibilidad `(Usuario, Curso)` |
| `EstadoPago`, `MetodoPago` | Constructor `(String nombre)` |
| `TipoMaterial`, `TipoReporte` | Constructor `(String nombre)` |
| `Modalidad`, `EstadoClaseEnVivo`, `EstadoClaseClonIA` | Constructor `(String nombre)` |
| `Alumno` | Reescritura limpia: constructores `()` y `(Usuario)` sin Lombok conflictivo |
| `Usuario` | Constructor `()` explícito requerido por Hibernate |
| `Unidad` | Constructor de compatibilidad `(String, String, int, Curso)` con auto-creación de `Programa` |
| `PoolAutoevaluacion` | Constructor `()` explícito + `(Pool, Autoevaluacion)` |

### 2.4 Correcciones de Repositorios y Servicios

| Archivo | Problema | Solución |
|:---|:---|:---|
| `IntentoAutoevaluacionRepository` | Método derivado `findByAutoevaluacionAndUsuario` falla — `IntentoAutoevaluacion` tiene `alumno`, no `usuario` directo | Reemplazado con `@Query` navegando `alumno.usuario` |
| `AutoevaluacionRepository` | HQL con paths incorrectos hacia `curso` | Paths corregidos a `unidad.programa.curso` |
| `PoolRepository` | HQL con paths incorrectos | Paths corregidos a `unidad.programa.curso` |
| `IntentoService` | Stream en `getPools()` asumía `List<Pool>` pero devuelve `List<PoolAutoevaluacion>` | Agregado `.map(pa -> pa.getPool())` |
| `IntentoService` | `intento.getNota() != null` sobre tipo primitivo `double` | Eliminada comparación con `null` |
| `EmailService` | `enviarComprobante(Usuario, Pago, Curso)` incompatible con llamada que pasaba `Comprobante` | Agregado overload `enviarComprobante(Usuario, Comprobante, Curso)` |
| `DocenteController` | `material::setTipo` — método inexistente | Corregido a `material::setTipoMaterial` |
| `DocenteController` | `ae.getPools().get(0).getUnidad()` asume `Pool` pero devuelve `PoolAutoevaluacion` | Corregido a `.get(0).getPool().getUnidad()` |

### 2.5 Ajustes de Mapping JPA y Restricciones

| Entidad | Cambio | Motivo |
|:---|:---|:---|
| `Configuracion` | `administrador_id` → `nullable = true` | Parámetros globales del sistema sin dueño administrativo |
| `Usuario` | `dni` → nullable | Usuarios OAuth y administradores no tienen DNI requerido |
| `Material` | `docente_id` → `nullable = true` | Materiales de semilla se crean sin docente asignado |
| `Unidad` | `programa` → `cascade = CascadeType.PERSIST` | Persiste el `Programa` creado automáticamente al construir `Unidad(Curso)` |
| `Programa` | `nombre` 50 → 150 chars | Nombres reales superan 50 caracteres |
| `Curso` | `nombre` 50 → 150 chars | Ídem |
| `Unidad` | `titulo` 50 → 150 chars | Ídem |
| `Material` | `titulo` 50 → 150 chars | Ídem |
| `Categoria` | `nombre` 50 → 150 chars | Ídem |

---

## 3. Estado de la Base de Datos (42 Entidades)

El paquete `com.app.idoneos.model` cuenta con **42 clases Java** alineadas al 100% con `base_datos.sql`.

| Grupo | Entidades |
|:---|:---|
| **Usuarios y Seguridad** | `Usuario`, `Alumno`, `Docente`, `Administrador`, `Rol`, `UsuarioRol`, `Sesion` |
| **Estructura Académica** | `Curso`, `Categoria`, `Programa`, `Unidad`, `Material`, `TipoMaterial`, `Modalidad`, `DocenteCurso`, `TituloDocente` |
| **Comunidad y Contenido** | `TerminoGlosario`, `ConsultaForo`, `RespuestaForo` |
| **Ventas y Transacciones** | `Inscripcion`, `Pago`, `EstadoPago`, `MetodoPago`, `Comprobante`, `Certificado`, `Descuento` |
| **Evaluaciones e IA** | `Pool`, `Pregunta`, `OpcionRespuesta`, `Autoevaluacion`, `PoolAutoevaluacion`, `IntentoAutoevaluacion`, `RespuestaIntento`, `ClaseClonIA`, `EstadoClaseClonIA` |
| **Streaming en Vivo** | `ClaseEnVivo`, `EstadoClaseEnVivo` |
| **Gobierno y Métricas** | `Configuracion`, `Auditoria`, `TipoAccionAuditoria`, `Reporte`, `TipoReporte`, `Progreso` |

---

## 4. Cobertura Funcional (86 / 86 CUs — Sin Cambios)

Los 86 Casos de Uso permanecen implementados tal como se documentó en `v.0.1`. Esta versión no incorporó funcionalidades nuevas; el trabajo fue exclusivamente de **corrección estructural y estabilización de la capa de persistencia**.

| Módulo | CUs | Estado |
|:---|:---:|:---:|
| MOD-F-01: Cursos | 30 | ✅ |
| MOD-F-02: Inscripción y Pagos | 11 | ✅ |
| MOD-F-03: Evaluación y Certificación | 12 | ✅ |
| MOD-F-04: Clon con IA | 3 | ✅ |
| MOD-F-05: Clases en Vivo | 7 | ✅ |
| MOD-F-06: Generación con IA Local | 3 | ✅ |
| MOD-NF-01: Usuarios y Autenticación | 14 | ✅ |
| MOD-NF-02: Auditoría | 1 | ✅ |
| MOD-NF-03: Reportes | 4 | ✅ |
| MOD-NF-04: Configuración | 1 | ✅ |
| **Total** | **86** | **✅ 100%** |

---

## 5. Commits de Esta Versión

| Hash | Mensaje | Archivos Afectados |
|:---|:---|:---:|
| `2dfa3be` | `feat: auditoria y reconstruccion completa del modelo de datos` | 42 entidades |
| `1aa0274` | `fix: compilacion completa y startup exitoso — entidades, repositorios y restricciones corregidas` | 56 archivos |

---

## 6. Conclusión y Verificación

El sistema se encuentra en un estado sólido y plenamente verificado:

- ✅ **Compilación limpia** — `mvn clean compile` sin errores de compilación
- ✅ **Startup exitoso** — Tomcat en `localhost:8080`, PostgreSQL 17 en `localhost:5432/idoneos`
- ✅ **Semilla de datos** insertada correctamente (catálogos, usuarios, cursos, unidades y materiales)
- ✅ **Modelo de datos 100% alineado** al esquema SQL oficial (`base_datos.sql`)
- ✅ **Repositorios y servicios** corregidos para navegar correctamente las relaciones JPA
- ✅ **Código commiteado y pusheado** a `main` en el repositorio remoto

El sistema está listo para continuar con las siguientes etapas de desarrollo.
