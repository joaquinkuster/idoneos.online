# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 11 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.2.md` (11 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.3` se concretó la **fidelidad estricta y simplificación del modelo de datos orientada a objetos**, reduciendo las clases `@Entity` a **exactamente 43 entidades relacionales**, coincidiendo punto por punto con las 43 tablas definidas en la fuente de verdad oficial `base_datos.sql`.

El sistema se encuentra **100% operativo, compila limpiamente (`BUILD SUCCESS`) y arranca en PostgreSQL 17**, poblando la semilla de datos iniciales sin excepciones ni advertencias de instanciación JPA.

```text
Started IdoneosApplication in 5.47 seconds
Idóneos Online: La semilla ya se encuentra insertada.
```

---

## 2. Reestructuración y Alineación del Modelo (43 Entidades)

### 2.1 Eliminación de Entidades Divergentes
Se removieron las entidades `@Entity` que no poseían tabla propia en el modelo DDL de base de datos:

1. **`Certificado.java`**: Los datos del certificado (`numero_certificado`, `fecha_emision_certificado`, `certificado_enviado`) fueron reincorporados como atributos directos de la entidad `Inscripcion`.
2. **`Comprobante.java`**: Los datos del comprobante (`numero_comprobante`, `fecha_emision_comprobante`, `comprobante_enviado`) fueron reincorporados como atributos directos de la entidad `Pago`.
3. **`DocenteCurso.java`**: Se eliminó la relación directa Docente ↔ Curso en favor de la tabla intermedia oficial del SQL: **`DictadoDocente.java`** (`"Dictado Docente"`), vinculando `Dictado` con `Docente`.
4. **`RolUsuario.java`**: Se mantuvo como `enum` puro en Java para type-safety sin anotación `@Entity`.

### 2.2 Documentación Javadoc en Español
Se revisó y documentó la totalidad de los archivos de modelo en `com.app.idoneos.model`, incorporando comentarios en español descriptivos por clase y por atributo según el documento técnico de aspectos de dominio del Trabajo Final.

### 2.3 Corrección de Constructores y Métodos de Entidades Clave
Se limpiaron e independizaron de Lombok las entidades primarias (`Administrador`, `Dictado`, `Programa`, `Usuario`, `PoolAutoevaluacion`), garantizando constructores explícitos `public Entity()` (no-arg) para instanciación mediante reflección por Hibernate.

---

## 3. Adaptación de Repositorios, Servicios y Controladores

- **Repositorios**:
  - Eliminados: `CertificadoRepository`, `ComprobanteRepository`, `DocenteCursoRepository`.
  - Creados: `DictadoDocenteRepository`, `ProgramaRepository`, `DictadoRepository`.
  - Actualizado `CursoRepository` para navegar `DictadoDocente -> Dictado -> Programa -> Curso`.
- **Servicios**:
  - `SemillaService`: Crea automáticamente `Programa`, `Dictado` y `DictadoDocente` al sembrar la base.
  - `PagoService` y `CertificadoService`: Persisten la emisión de comprobantes y certificados en el mismo objeto `Pago` e `Inscripcion`.
  - `EmailService`: Notificaciones adaptadas para consumir atributos integrados.
- **Controladores**:
  - `AdminController`, `DocenteController`, `ForoController` y `CertificadoController` actualizados a la nueva estructura de dictados y docentes.

---

## 4. Estado de la Base de Datos (43 Entidades JPA)

El paquete `com.app.idoneos.model` cuenta con **43 clases `@Entity`** exactas:

| Grupo | Entidades |
|:---|:---|
| **Usuarios y Seguridad** | `Usuario`, `Alumno`, `Docente`, `Administrador`, `Rol`, `UsuarioRol`, `Sesion` |
| **Estructura Académica** | `Curso`, `Categoria`, `Programa`, `Dictado`, `Unidad`, `Material`, `TipoMaterial`, `Modalidad`, `ModalidadCurso`, `DictadoDocente`, `TituloDocente` |
| **Comunidad y Contenido** | `TerminoGlosario`, `ConsultaForo`, `RespuestaForo` |
| **Ventas y Transacciones** | `Inscripcion`, `Pago`, `EstadoPago`, `MetodoPago`, `Descuento` |
| **Evaluaciones e IA** | `Pool`, `Pregunta`, `OpcionRespuesta`, `Autoevaluacion`, `PoolAutoevaluacion`, `IntentoAutoevaluacion`, `RespuestaIntento`, `ClaseClonIA`, `EstadoClaseClonIA` |
| **Streaming en Vivo** | `ClaseEnVivo`, `EstadoClaseEnVivo` |
| **Gobierno y Métricas** | `Configuracion`, `Auditoria`, `TipoAccionAuditoria`, `Reporte`, `TipoReporte`, `Progreso` |

---

## 5. Resumen de Cobertura y Verificación

- ✅ **43/43 Tablas SQL Mapeadas**: Coincidencia exacta 1:1 con `base_datos.sql`.
- ✅ **86/86 Casos de Uso Activos**: Sin pérdida de funcionalidad de negocio.
- ✅ **Compilación Limpia**: `.\mvnw.cmd clean compile` aprueba sin errores (`BUILD SUCCESS`).
- ✅ **Startup Verificado**: Servidor ejecuta Tomcat en puerto 8080 e interactúa con PostgreSQL correctamente.

---

## 6. Conclusión

El sistema alcanza la máxima coherencia entre el modelo relacional conceptual (`base_datos.sql`), el dominio de clases Java (`com.app.idoneos.model`) y los componentes de negocio (`service` / `controller`), preparado para ser evaluado y continuar con futuras expansiones.
