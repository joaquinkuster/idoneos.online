# Informe de Estado del Sistema — Idóneos Online

**Proyecto:** Sistema de Gestión Idóneos Online — Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales  
**Empresa:** Idóneos Online S.A.S. · Argentina  
**Autores:** Küster Joaquín (Leg. 906560) · Martinez Lazaro Ezequiel (Leg. 906047)  
**Cátedras:** Proyecto Software (LSI) | Trabajo Final (ASC) — FCEQyN · UNaM  
**Profesor:** Lic. Sergio Daniel Caballero  
**Fecha de informe:** 11 de agosto de 2026  
**Versión anterior:** `estado_sistema_v.0.4.md` (11 de agosto de 2026)

---

## 1. Resumen Ejecutivo

En esta versión `v.0.5` se completó la construcción de la **Capa de Servicios y Dominio Backend integral** cubriendo la totalidad de los **93 Casos de Uso (CU-01 a CU-93)** especificados en la documentación funcional (`docs/requisitos/requisitos.md`).

La implementación fue desarrollada bajo dos directivas fundamentales e innegociables:
1. **Inmutabilidad Absoluta del Modelo de Datos**: Se respetó al 100% la estructura de las 43 tablas persistentes y los atributos de la base de datos `docs/analisis/base_datos.sql` y las entidades `@Entity` mapeadas en Java.
2. **Español Estricto y Trazabilidad**: Todo el código de servicios, excepciones personalizadas, mensajes de error y comentarios Javadoc de trazabilidad funcional se escribieron íntegramente en idioma español.

El proyecto compila exitosamente (`BUILD SUCCESS`) sobre los 148 archivos del sistema.

---

## 2. Novedades y Módulos Desarrollados en la v0.5

### 2.1 Infraestructura Global de Excepciones en Español
Se creó la jerarquía de excepciones personalizadas en `com.app.idoneos.exception` con su correspondiente `@RestControllerAdvice`:
- **`ExcepcionNegocio.java`**: Excepción base con soporte para código de estado HTTP y mensaje detallado.
- **`ExcepcionRecursoNoEncontrado.java`**: Manejo de respuestas HTTP 404 NOT FOUND.
- **`ExcepcionConflicto.java`**: Manejo de conflictos de estado o datos duplicados (HTTP 409 CONFLICT).
- **`ExcepcionValidacion.java`**: Manejo de errores de validación de reglas de negocio en los casos de uso (HTTP 400 BAD REQUEST).
- **`ManejadorExcepcionesGlobales.java`**: Interceptor global que transforma excepciones no capturadas en respuestas JSON formateadas.

### 2.2 Implementación Completa de Servicios Backend (CU-01 a CU-93)

1. **Gestión de Catálogo de Cursos y Categorías (CU-01 a CU-09)**:
   - `CursoServiceImpl.java`: Búsqueda multicriterio, precio no negativo, regla compleja de publicación ($\ge 1$ programa con $\ge 10$ unidades con material publicado), edición y baja lógica validando inscriptos.
   - `CategoriaServiceImpl.java`: Validación de unicidad de nombre de categoría e impedimento de baja lógica si posee cursos activos.
2. **Programas, Dictados y Unidades Temáticas (CU-10 a CU-24)**:
   - `ProgramaServiceImpl.java`: Control de carga horaria, validación de estado de dictados activos para bajas.
   - `DictadoServiceImpl.java`: Validación de coherencia de fechas (inicio $\le$ fin) y control de cupo máximo.
   - `UnidadServiceImpl.java`: Secuenciamiento por número de orden y gestión de contenido.
3. **Materiales de Estudio, Glosario y Foro (CU-25 a CU-40)**:
   - `MaterialServiceImpl.java`: Registro por tipo de material, visibilidad de materiales libres vs. de pago.
   - `GlosarioServiceImpl.java`: CRUD de términos del glosario técnico vinculados a unidades.
   - `ForoServiceImpl.java`: Consultas de alumnos, respuestas docentes y control de autoría en edición/baja.
4. **Inscripciones, Pagos, Comprobantes y Descuentos (CU-41 a CU-50)**:
   - `InscripcionServiceImpl.java`: Verificación de inscripción previa y cupo del dictado.
   - `PagoService.java`: Integración con la Checkout API de Mercado Pago y emisión de comprobantes en PDF.
   - `DescuentoServiceImpl.java`: Gestión de cupones, límites de uso y rango de porcentaje (1% a 100%).
5. **Evaluaciones, Clon IA y Gestión de Usuarios (CU-51 a CU-88)**:
   - `IntentoService.java`: Sorteo aleatorio de 10 preguntas por intento, corrección automática y nota.
   - `ClaseClonIAServiceImpl.java`: Validación de consentimiento firmado por docente para generación por Clon IA.
   - `UsuarioServiceImpl.java`: Registro de usuarios, perfiles, cumplimiento de `RN-07` (mínimo 1 admin activo) y `RN-11` (protección de baja a docente titular con cursos publicados).

---

## 3. Resumen de Verificación y Calidad

- ✅ **Inmutabilidad del Modelo Preservada**: Ninguna tabla, columna o relación de `base_datos.sql` fue alterada.
- ✅ **100% en Español**: Nombres de clases de excepción, mensajes, interfaces de servicio y comentarios Javadoc en español.
- ✅ **Compilación Exitosa**: Se ejecutó `./mvnw clean compile` con resultado `BUILD SUCCESS` (148 clases procesadas).
- ✅ **Trazabilidad Funcional**: Cada método de servicio documenta el Caso de Uso (CU), paso y excepciones contempladas.
- ✅ **Control de Versionado**: Repositorio actualizado con commit y push hacia `origin/main`.
