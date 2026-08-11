# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 11 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.3.md` (11 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.4` se resolvió la depuración integral de **mapeos JPA/JPQL**, la **refactorización y regeneración limpia del modelo `Administrador.java`**, y la **migración y despliegue del esquema de base de datos desde cero en la nueva base de datos PostgreSQL `idoneos.online`**.

El sistema se encuentra **100% operativo, compila limpiamente (`BUILD SUCCESS`) y se ejecuta sin errores en Tomcat (puerto 8080)**, poblando exitosamente la semilla de datos iniciales en la base de datos `idoneos.online`.

```text
Tomcat started on port 8080 (http) with context path '/'
Started IdoneosApplication in 45.327 seconds
Idóneos Online: Datos iniciales insertados correctamente.
```

---

## 2. Correcciones y Mejoras Desarrolladas en la v0.4

### 2.1 Reconstrucción y Limpieza del Modelo `Administrador.java`
- Se refactorizó por completo el archivo `com.app.idoneos.model.Administrador`, asegurando un balance estricto de sintaxis Java.
- Se incorporaron anotaciones Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) y constructores explícitos compatibles con JPA Hibernate.

### 2.2 Corrección de Mapeos JPA y Consultas JPQL
1. **`Docente.java`**:
   - Se removió la anotación `@Transient` del atributo `fechaConsentimientoClon` y se reemplazó por `@Column(name = "fecha_consentimiento_clon")`.
   - Se resolvió la excepción `UnknownPathException` que afectaba a las consultas de docentes habilitados para Clon IA en `DocenteRepository`.
2. **`AutoevaluacionRepository.java`**:
   - Se corrigió la consulta JPQL en `findByPoolsContainingAndBajaFalse`, cambiando el alias `a.poolsAutoevaluaciones` por la propiedad real mapeada `a.pools`.

### 2.3 Despliegue y Migración a la Nueva Base de Datos `idoneos.online`
- **Cadena de Conexión**: Se actualizó `application.properties` para apuntar a `jdbc:postgresql://localhost:5432/idoneos.online`.
- **Creación de Esquema desde Cero**: Se ejecutó el arranque con `spring.jpa.hibernate.ddl-auto=create`, generando las 43 tablas de base de datos en la nueva instancia `idoneos.online`.
- **Preservación**: Posterior al semillado automático, se retornó a `spring.jpa.hibernate.ddl-auto=update` para garantizar la persistencia de datos producidos.

---

## 3. Resumen de Verificación y Calidad

- ✅ **Modelo Administrador Reparado**: Sintaxis y constructores corregidos sin errores sintácticos de cierre de llaves.
- ✅ **Consultas JPQL Validadas**: Corrección de mapeos JPA en `Docente` y `AutoevaluacionRepository`.
- ✅ **Nueva Base de Datos Operativa**: Instancia `idoneos.online` creada, con tablas pobladas y datos de prueba verificados.
- ✅ **Compilación y Ejecución**: Servidor Web Tomcat operando en puerto 8080 con respuesta limpia.
- ✅ **Versionado Git**: Cambios preparados y committeados al repositorio oficial en GitHub.
