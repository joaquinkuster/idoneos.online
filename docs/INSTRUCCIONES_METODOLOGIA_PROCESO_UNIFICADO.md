# Guía Metodológica de Documentación Encadenada (Proceso Unificado)

> **Propósito**: Esta guía define el pipeline de trabajo secuencial para construir la documentación completa de cualquier sistema de software de forma progresiva, trazable y adaptativa, desde la concepción del proyecto hasta la defensa final.

---

## 🏛️ Regla de Oro: El Modelo de Datos (SQL) como Ancla Canónica

El archivo de base de datos (`.sql` o esquema físico/relacional) constituye la **Fuente de Verdad Central (Single Source of Truth)** para todo el sistema:
1. **Consistencia de Entidades**: Toda entidad o concepto de negocio debe mapear a una tabla del SQL.
2. **Consistencia de Atributos**: Cada campo citado en formularios, contratos o mensajes debe existir como columna en el SQL con su tipo correspondiente (`VARCHAR`, `INT`, `BOOLEAN`, etc.).
3. **Reglas de Integridad**: Las restricciones de base de datos (`NOT NULL`, claves foráneas `FK`, unicidad `UNIQUE` y flags de borrado lógico como `baja`) dictan las precondiciones, postcondiciones y validaciones en todas las fases.

---

## 🔄 Flujo de Trabajo Secuencial Encadenado

```
[Fase 0: Nota de Proyecto]
         │ (Objetivos de negocio, alcance y módulos)
         ▼
[Fase 1: Requisitos] 
         │ (Casos de Uso Extendidos: CU-xx, Entradas/Salidas lógicas)
         ▼
[Fase 2: Análisis] ◄────── [Modelo SQL Canónico]
         │ (Aspectos Técnicos, DSS y Contratos de Operación)
         ▼
[Fase 3: Diseño]
         │ (Casos de Uso Reales con UI/UX, DSD con objetos y capas)
         ▼
[Fase 4: Pendiente & Auditoría]
         │ (Gaps, discrepancias, deuda técnica y pendientes)
         ▼
[Fase 5: Estado del Sistema]
         │ (Fotografía viva de completitud y versionado: v0.1 -> v1.0)
         ▼
[Fase 6: Presentaciones & Defensa]
           (Guiones para evaluadores, demos de consultas y soporte visual)
```

---

## 📌 Fase 0: Nota de Presentación del Proyecto (`/docs/nota_presentacion/`)

* **Objetivo**: Establecer el acta fundacional, justificación, alcance, objetivos estratégicos y arquitectura modular de alto nivel antes de detallar el comportamiento del sistema.
* **Entrada**: Necesidad de negocio o requerimiento inicial del cliente.
* **Salida**: `Nota de presentación.md`

### Instrucciones para el Agente:
1. Redactar la introducción describiendo el problema que el sistema resuelve y su valor diferencial.
2. Definir los **Objetivos Generales** y **Objetivos Específicos** con identificadores persistentes (`OBJ-01`, `OBJ-02`, ...).
3. Establecer los **Requisitos de Información** (`RI-01`, `RI-02`, ...).
4. Dividir el sistema en **Módulos Funcionales** (`MOD-F-xx`) y **Módulos No Funcionales / Transversales** (`MOD-NF-xx`).
5. Listar los **Actores** y roles del sistema junto con una matriz preliminar de alcance.

---

## 📌 Fase 1: Requisitos (`/docs/requisitos/`)

* **Objetivo**: Especificar el comportamiento funcional desde la perspectiva del usuario (caja negra), sin detalles tecnológicos ni interfaces gráficas.
* **Entrada**: `Nota de presentación.md` y estructura de módulos.
* **Salida**: `Casos de Uso Extendidos.md`

### Instrucciones para el Agente:
1. Asignar a cada caso de uso un código correlativo único (`CU-01`, `CU-02`, ...), agrupados por módulo.
2. Mapear cada caso de uso con sus respectivos `OBJ-xx` y `RI-xx` de la Fase 0.
3. Describir el flujo de eventos en una tabla de pasos secuenciales (`Paso` vs `Acción`) distinguiendo claramente la acción del actor y la respuesta del sistema.
4. Identificar datos de entrada y salida respetando los atributos del negocio que luego se persistirán en el SQL.

#### Estructura Estándar por Caso de Uso Extendido:
```markdown
### CU-[Número]: [Nombre en infinitivo]
- **Objetivo(s) asociado(s)**: [OBJ-xx: Descripción corta]
- **Requisito(s) de información asociado(s)**: [RI-xx: Descripción corta]
- **Módulo**: [MOD-F-xx: Nombre del módulo]
- **Actor(es)**: [Actor Principal], [Actor Secundario opcional]
- **Descripción**: [Resumen conciso del propósito].
- **Precondición(es)**: [Estado previo requerido del sistema y rol autenticado].
- **Flujo de eventos**:

| Paso  | Acción                                                                     |
| :---: | :------------------------------------------------------------------------- |
|   1   | El caso de uso inicia cuando el actor solicita [acción inicial].           |
|   2   | El sistema solicita los datos / criterios de búsqueda [campos de entrada]. |
|   3   | El actor ingresa o selecciona los datos requeridos.                        |
|   4   | El sistema valida las restricciones y recupera/procesa la información.     |
|   5   | El sistema lista / confirma el resultado de la operación.                  |
|   6   | Fin del caso de uso.                                                       |

- **Flujos Alternativos / Excepciones**:
  - **Paso X.a** [Condición de fallo / validación]:
    - X.a.1. El sistema informa el motivo de error.
    - X.a.2. Fin del caso de uso / Vuelve al paso Y.
- **Salida**: [Datos resultantes que se presentan o estado modificado].
- **Frecuencia**: [Alta | Media | Baja]
- **Estabilidad**: [Alta | Media | Baja]
- **Comentarios**: [Observaciones adicionales o reglas de negocio].
```

---

## 📌 Fase 2: Análisis (`/docs/analisis/`)

* **Objetivo**: Construir el modelo conceptual, identificar los eventos del sistema y formalizar los cambios de estado internos contra el modelo de datos.
* **Entrada**: `Casos de Uso Extendidos.md` y el archivo de base de datos canónico (`.sql`).
* **Salida**:
  1. `Aspectos técnicos modelo dominio.md`
  2. `DSS.md` (Diagramas de Secuencia del Sistema)
  3. `Contratos.md`

### Instrucciones para el Agente:

#### 1. En `Aspectos técnicos modelo dominio.md`:
* Traducir el esquema SQL a conceptos del dominio.
* Documentar los criterios transversales de diseño:
  - Manejo de atributos derivados (ej. no almacenar redundancias calculables).
  - Políticas de borrado lógico (`baja BOOLEAN`) vs borrado físico.
  - Tablas catálogo vs flags booleanos.
* Detallar cada tabla/entidad: nombre de atributos, tipos de datos, nulabilidad y explicación funcional de cada relación foránea (`FK`).

#### 2. En `DSS.md`:
* Por cada `CU-xx`, modelar la secuencia temporal donde el sistema es una caja negra unificada (`:Sistema`).
* Traducir las acciones del actor a mensajes/operaciones con firmas precisas: `nombreOperacion(param1, param2)`.
* Usar bloques estructurados estándar:
  - `alt` (Alternativo/Condicional) para validaciones exitosas o fallidas.
  - `opt` (Opcional) para pasos facultativos del usuario.
  - `loop` (Bucle) para procesamiento de múltiples elementos.

#### 3. En `Contratos.md`:
* Crear un contrato por cada operación identificada en los DSS.
* Redactar las **Postcondiciones** estrictamente en términos del modelo SQL:
  - *Creación de tuplas*: `Se creó una instancia de Entidad con atributos...`
  - *Modificación*: `Se actualizó el campo X de la Entidad identificada por Y...`
  - *Asociación*: `Se asoció la Entidad A con la Entidad B a través de su clave foránea...`

```markdown
### [nombreOperacion](parametro1, parametro2, ...)
- **Responsabilidades**: [Qué calcula, valida o persiste la operación].
- **Tipo**: Sistema.
- **Referencias cruzadas**: [CU-xx: Nombre del Caso de Uso].
- **Notas**: [Criterios opcionales o valores por defecto].
- **Excepciones**: [Restricciones de unicidad o validaciones de negocio fallidas].
- **Salida**: [Instancia, lista o booleano de retorno].
- **Precondiciones**: [Registros previos que deben existir en las tablas SQL].
- **Postcondiciones**: [Instancias creadas, modificadas o asociadas en BD].
```

---

## 📌 Fase 3: Diseño (`/docs/diseño/`)

* **Objetivo**: Transformar el análisis en la solución técnica concreta: arquitectura de software, experiencia de usuario (UI/UX), navegación y realización de operaciones mediante objetos y capas.
* **Entrada**: Documentos de Requisitos y Análisis, archivo `.sql` y lineamientos de UI.
* **Salida**:
  1. `Casos de Uso Reales.md`
  2. `DSD.md` (Diagramas de Secuencia del Diseño)
  3. Prototipos visuales / Mockups (`.html` o vínculos a Figma).

### Instrucciones para el Agente:

#### 1. En `Casos de Uso Reales.md`:
* Incluir en la cabecera el **Esquema de Navegación Global**: rutas/URLs del sistema (`/modulo/recurso`) y menús de acceso por cada rol.
* Refinar cada `CU-xx` asociándolo a vistas concretas, componentes de UI, campos de formularios (mapeados a las columnas SQL) y botones de acción (`[btnGuardar]`, `[inputFiltro]`, etc.).

#### 2. En `DSD.md`:
* Realizar internamente cada contrato de la Fase 2 aplicando patrones de asignación de responsabilidades (Controlador, Creador, Experto en Información).
* Modelar la interacción interna entre:
  - El Controlador (`:ControladorSistema`).
  - Las Colecciones / Repositorios (`coleccion : Coleccion`).
  - Las Instancias de las Entidades (`instancia : Entidad`).
* Detallar variables locales, bucles de búsqueda y llamadas a métodos internos.

```markdown
### DSD [nombreOperacion(params)] | CU-[Número]: [Nombre Caso de Uso]
* **Actores:** [Actor]
* **Objetos / Instancias participantes:**
  * `:ControladorSistema` (Controlador)
  * `coleccionInstancias : Coleccion` (Estructura de persistencia / Repositorio SQL)
  * `instancia : Entidad` (Instancia que mapea a la fila de la tabla SQL)
* **Variables locales / Notas:**
  * `Lista resultados = nueva Lista()`
* **Flujo de interacción:**
  1. El actor invoca `resultado = nombreOperacion(params)` en `:ControladorSistema`.
  2. `:ControladorSistema` inicializa variables locales.
  3. **Bloque Bucle (`loop`)** [Mientras haya registros en la colección]:
     * `:ControladorSistema` solicita `instancia = obtenerSiguiente()`.
     * `:ControladorSistema` consulta `cumple = verificarCondicion(params)`.
     * **Bloque Alternativo (`alt`)** [cumple == true]:
       * `:ControladorSistema` ejecuta `resultados.agregar(instancia)`.
  4. `:ControladorSistema` retorna `resultados` al actor.
```

---

## 📌 Fase 4: Pendiente & Auditoría (`/docs/pendiente/`)

* **Objetivo**: Control de calidad continuo, detección de discrepancias entre fases, gaps de trazabilidad y gestión de la deuda técnica.
* **Entrada**: Todos los documentos generados de las Fases 0 a 3 y el archivo SQL.
* **Salida**: `AUDITORIA_SISTEMA_COMPLETA.md`

### Instrucciones para el Agente:
1. **Auditoría Cruzada**:
   - Detectar si existen casos de uso sin contrato asociado o contratos sin caso de uso.
   - Verificar si algún atributo mencionado en los casos de uso no existe en el archivo SQL.
   - Detectar discrepancias en nombres de operaciones entre `DSS.md`, `Contratos.md` y `DSD.md`.
2. **Estructura del Informe**:
   - **Métricas de Cobertura**: Porcentaje de trazabilidad Requisitos $\rightarrow$ Análisis $\rightarrow$ Diseño.
   - **Gaps Críticos**: Inconsistencias de nombres, parámetros o atributos faltantes.
   - **Deuda Técnica y Mejoras Futuras**: Tareas pendientes clasificadas por prioridad (Alta, Media, Baja).

---

## 📌 Fase 5: Estado del Sistema (`/docs/estado_sistema/`)

* **Objetivo**: Registrar la evolución incremental del software a través de versiones formales (`estado_sistema_v.0.X.md`), sirviendo como bitácora de sincronización para el equipo.
* **Entrada**: Avances de implementación, auditorías resueltas y cambios de alcance.
* **Salida**: `estado_sistema_v.0.X.md` (con versionado correlativo: v.0.1, v.0.2, etc.).

### Instrucciones para el Agente:
1. Registrar un **Resumen Ejecutivo de la Versión**: qué se integró, qué se corrigió y fecha de corte.
2. Mantener una **Matriz General de Casos de Uso** indicando el estado de avance de cada uno:
   - `Documentado (Extendido)`
   - `Analizado (DSS + Contrato)`
   - `Diseñado (Real + DSD)`
   - `Implementado / En Pruebas`
3. Detallar las correcciones arquitectónicas introducidas respecto a la versión anterior.

---

## 📌 Fase 6: Presentaciones & Defensa (`/docs/presentaciones/`)

* **Objetivo**: Sintetizar y comunicar los resultados a evaluadores, clientes o jurados académicos, transformando la documentación técnica en material didáctico y de defensa.
* **Entrada**: Documentación consolidada de todas las fases anteriores.
* **Salida**:
  - `guion_presentacion_sistema.md`: Guion oratorio paso a paso para la exposición.
  - `guion_defensa_vistas_figma.md`: Explicación guiada de los prototipos visuales y decisiones de UX.
  - `demo_consultas_profesor.sql`: Scripts SQL preparados para demostrar en vivo la integridad del modelo relacional ante preguntas técnicas.
  - Prompts estructurados para herramientas de diapositivas automáticas (ej. Gamma / Marp).

### Instrucciones para el Agente:
1. Redactar el guion de defensa con tono profesional, destacando el problema de negocio, la solidez metodológica y la arquitectura elegida.
2. Armar consultas SQL de demostración comentadas para exhibir en vivo cómo responde la base de datos a los casos de uso principales.
3. Crear las pautas de presentación visual divididas por bloques de tiempo (Introducción, Demo Funcional, Arquitectura/Datos y Cierre).

---

## 📋 Checklist Final de Consistencia para el Agente

Antes de dar por finalizada la generación de documentación en cualquier fase, el agente debe validar:
- [ ] **Trazabilidad 100%**: ¿Todo `CU-xx` presente en Requisitos tiene su reflejo en DSS, Contratos, Casos Reales y DSD?
- [ ] **Exactitud contra el SQL**: ¿Cada campo, tipo de dato y clave foránea coincide exactamente con el script SQL de la base de datos?
- [ ] **Coincidencia de Firmas**: ¿Las firmas de los métodos `nombreOperacion(params)` son idénticas carácter por carácter en los DSS, Contratos y DSD?
- [ ] **Control de Cambios**: ¿Cualquier ajuste técnico en el diseño fue reflejado en la última versión de `estado_sistema`?