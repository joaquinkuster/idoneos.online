# Diseño: documentos, índice, oraciones y fuentes

Estado: **propuesta para aprobación, sin código escrito**. Cierra la conversación del 16/11/2026 sobre el modelo (diagrama `Diagrama de Base de Datos.DM1`).

## 1. Decisiones cerradas

**Vocabulario**
- **Material bibliográfico** (tabla `material_bibliografico`): libros, manuales y publicaciones formales. Tiene identificador (ISBN, ISSN o DOI). Es siempre público: es la biblioteca compartida.
- **Material didáctico** (tabla `material_didactico`): lo que el docente elabora para su curso. Sin identificador. Puede ser privado.
- Categorías del material didáctico: **Apunte de Cátedra** (resúmenes, transcripciones de clases, artículos o compilaciones teóricas del equipo docente), **Guía de Estudio** (guías de lectura, de ejercicios prácticos o integradores que orientan el estudio de un tema) y **Presentación**. No hay categoría «Resumen» ni «Documento»: el resumen generado con IA se guarda como Apunte de Cátedra.
- **Documento** es una entidad real: lo que tiene archivo y texto. Cuelgan de él el índice, las oraciones y las fuentes. Enlace, grabación y video con clon no son documentos.
- **IntegranteEquipo** reemplaza a `DocenteCurso`: una fila por docente en un curso (`esTitular` marca el rol). Es el término de los casos de uso («equipo docente», «titular o ayudante»).
- **Fuente** no es un tipo: es un rol que cumple cualquier documento al generar contenido.

**Reglas**
- El índice se arma **una sola vez, en el alta**. El sistema lo sugiere, el docente lo revisa, y recién al confirmar se guarda. Hay un único índice por documento y no tiene estados.
- El índice es **obligatorio en los datos**: al menos una entrada. Si el PDF no tiene títulos, la entrada por defecto es el título del documento (nivel 1, página 1).
- Las oraciones se guardan en el alta y **cuelgan de una entrada del índice**. Las anteriores al primer título van a la primera entrada.
- Las oraciones de un documento subido **no se editan**. Solo se editan las de contenido generado con IA, y solo si ese contenido no es fuente de otro.
- La generación **lee oraciones e índice de la base**. No usa PDFBox ni relee el PDF. OpenNLP sigue cargado: lematiza, etiqueta y quita sujetos sobre las oraciones guardadas.
- El docente elige las **fuentes** al generar, entre los documentos del tema. Por defecto quedan marcadas las escritas por personas; las generadas con IA aparecen sin marcar.
- Un documento que es **fuente o evidencia de contenido generado vigente** no se puede modificar (todo, incluido el título) ni dar de baja. Los modales se abren igual y el error responde al guardar. «Vigente» = sin baja y no fallido.
- Si el docente borra una entrada del índice con oraciones, estas pasan a la entrada anterior en la misma transacción. El índice mínimo no se puede dejar vacío.
- Reemplazar el PDF vuelve a correr el alta (solo si el documento no está en uso).
- La rotación de oraciones entre pools del mismo tema se hace **por referencia** (`pregunta.idOracionEvidencia`): primero las no usadas, después las menos usadas.

## 2. Modelo

Tipos y largos son los de la base actual. Los ids son `integer` con identity.

```
recurso                    (sin cambios: superclase de documento, enlace, grabacion, video_clon)

documento                  -- lo que hoy está repetido en bibliografia y material_lectura
  id_documento             PK
  id_recurso               NOT NULL  AK  FK → recurso
  id_estadoocr             NULL      FK → estadoocr
  autor                    varchar(100) NOT NULL
  paginas                  integer NULL   CHECK (paginas IS NULL OR paginas > 0)
  tamanio_bytes            bigint  NULL   CHECK (tamanio_bytes IS NULL OR tamanio_bytes >= 0)
  extension                varchar(10)  NULL
  ruta_archivo             varchar(500) NULL   -- obligatoria en el bibliográfico; nula mientras se genera el didáctico

material_bibliografico     (antes bibliografia)
  id_material_bibliografico  PK
  id_documento             NOT NULL  AK  FK → documento
  identificador            varchar(150) NOT NULL   -- ISBN, ISSN o DOI; el tipo se deduce del formato
  publicado_en             varchar(150) NULL       -- editorial, revista o congreso (antes editorial)
  fecha_publicacion        date NULL

material_didactico         (antes material_lectura)
  id_material_didactico    PK
  id_documento             NOT NULL  AK  FK → documento
  id_categoria_material_didactico  NOT NULL  FK
  id_estado_generacionia   NULL  FK      -- no nulo = generado con IA
  detalle_error            varchar(500) NULL

categoria_material_didactico   (antes categoria_material_lectura)
  id_categoria_material_didactico  PK
  nombre                   varchar(50) NOT NULL UNIQUE   -- Apunte de Cátedra, Guía de Estudio, Presentación

indice_documento           (antes indice_recurso)
  id_indice_documento      PK
  id_documento             NOT NULL  FK → documento
  nivel                    smallint NOT NULL
  numero_orden             integer NOT NULL
  titulo                   varchar(150) NOT NULL
  pagina                   integer NOT NULL
  fecha_creacion, ultima_modificacion
  AK (id_documento, numero_orden)

oracion
  id_oracion               PK
  id_indice_documento      NOT NULL  FK → indice_documento
  numero_orden             integer NOT NULL
  texto                    text NOT NULL  CHECK (trim(texto) <> '')

documento_fuente           -- «este contenido generado salió de estos documentos»
  id_documento, id_documento_fuente   PK compuesta, CHECK (distintos)

pool_fuente                -- «este banco de preguntas salió de estos documentos»
  id_pool, id_documento               PK compuesta

pregunta                   (columna nueva)
  id_oracion_evidencia     integer NULL  FK → oracion   -- NULL en las del glosario

integrante_equipo          (antes docente_curso)
  id_integrante_equipo     PK
  id_curso, id_docente     NOT NULL  FK, AK (id_curso, id_docente)
  es_titular, id_cohorte_por_defecto (NULL), baja, fechas
  -- tema_recurso, clase_en_vivo y respuesta_foro la referencian con una sola columna
```

Otros cambios del diagrama: `tema_recurso` con clave `(id_tema, id_recurso)`; `pregunta_intento` con `(id_pregunta, id_intento_autoevaluacion)` y `respuesta_intento` con esas dos más `id_opcion_respuesta`; `resaltado_alumno` con `(id_documento, id_alumno, pagina)` y apuntando a documento; clave alternativa `(id_intento_autoevaluacion, orden)` en `pregunta_intento`; 11 claves alternativas en total.

## 3. Reglas de integridad (triggers y servicio)

| Regla | Dónde |
|---|---|
| Un documento tiene exactamente un subtipo, bibliográfico o didáctico | Trigger |
| Un documento tiene al menos una entrada de índice | Trigger diferido (como `pool_con_preguntas`) |
| `ruta_archivo` obligatoria en el bibliográfico | Trigger o CHECK |
| `identificador`: formato, único si es ISBN o DOI, sin chequeo de repetición si es ISSN | Trigger y servicio |
| Un documento en uso no se modifica ni se da de baja (incluye su recurso, índice y oraciones) | Trigger `fn_documento_en_uso` |
| Una oración de documento subido no se modifica; una de contenido generado sí, si no es fuente | Trigger |
| Borrar una entrada con oraciones las pasa a la anterior | Servicio, misma transacción |
| El material bibliográfico es siempre público | Servicio (regla actual) |

«En uso» cuenta: fuente de un pool o contenido generado **vigente**, o con oraciones en `pregunta.id_oracion_evidencia` de un pool vigente. Mensaje con la convención `fn_msg_accion`: «No se puede modificar el documento «X» porque es fuente o evidencia de contenido generado con inteligencia artificial.» y lo mismo para «dar de baja». Reemplaza y unifica a `trg_bibliografia_en_uso`.

## 4. Flujos

**Alta de documento.** Subir archivo → OCR (el alta no se completa sin OCR) → extracción (PDFBox y OpenNLP, solo acá) → propuesta de índice en el formulario → el docente la revisa y confirma → se guardan documento, subtipo, entradas y oraciones en una transacción. Límites: se reutilizan los del OCR (`idoneos.ocr.max-paginas`, megas).

**Edición del índice.** El docente puede agregar, renombrar, reordenar y quitar entradas. Las oraciones se reacomodan al índice y no se editan. Todo se bloquea si el documento está en uso.

**Generación.** El docente elige fuentes entre los documentos del tema (`tema_recurso`) → se guardan en `pool_fuente` o `documento_fuente` → el sistema lee las oraciones de la base, las prioriza (BM25 y MMR) y arma las preguntas con el camino por rondas → guarda `id_oracion_evidencia`. Rotación por referencia entre los pools del tema.

**Contenido generado editable (fase D).** El resumen o la presentación generados se guardan como documento con índice y oraciones. Se editan las oraciones y el PDF se regenera a partir de ellas.

## 5. Fases

Cada fase deja la suite verde. El commit lo hace el usuario.

**Fase 0 — Vocabulario y modelo base** (sin funcionalidad nueva)
- **0a. Renombres de identificadores** (clases, variables, tablas, columnas, `schema/*.sql`, plantillas y JS; **sin cambiar ningún texto que ve el usuario**): `Bibliografia` a `MaterialBibliografico` (unos 33 archivos), `MaterialLectura` y `CategoriaMaterialLectura` a `MaterialDidactico` y `CategoriaMaterialDidactico` (unos 20), `DocenteCurso` a `IntegranteEquipo` (unos 43). `IndiceRecurso` a `IndiceDocumento` se renombra en 0c, cuando cambia de padre.
- **0b. Claves compuestas y claves alternativas**, de a una entidad y en este orden, con la suite verde y un commit del usuario en cada paso:
  1. `RolUsuario`, `CursoModalidad`, `InteresAlumno`, `AutoevaluacionPool`.
  2. `RecordatorioCohorte`, `Progreso`, `TemaCronograma`.
  3. `PreguntaIntento` y `RespuestaIntento` (módulo de evaluaciones).
  4. `TemaRecurso` y `ResaltadoAlumno` (con `pagina` en la clave).
  5. Las 11 claves alternativas del diagrama.
- **0c. Tabla `documento`** (composición, con getters delegados en los subtipos para no tocar el código existente), `IndiceRecurso` pasa a `IndiceDocumento` colgando de `documento`.
- **0d. Identificador y publicado en:** `identificador` (ISBN, ISSN o DOI) y `publicado_en`, con validación por formato.
- **0e. Vocabulario visible:** categorías nuevas (Apunte de Cátedra, Guía de Estudio, Presentación; fuera «Resumen»), tipos de subida, textos de pantalla (Biblioteca, Material bibliográfico, Material didáctico), tooltips, aviso de que lo bibliográfico es público, íconos y color turquesa para Guía de Estudio.
- Riesgo: el diff es muy grande. Se hace antes de cualquier tabla nueva.

**Fase A — Estructura en el alta**
- Tablas `indice_documento` y `oracion`; servicio de estructura (reutiliza `EstructuraPdfUtil`, `OracionesUtil` y los marcadores de PDF); paso de revisión del índice en el alta; trigger diferido; semilla (índice de `Requisitos.pdf` desde sus marcadores y extracción de los demás PDF).
- Riesgos: tiempo de extracción en el alta, PDF escaneados sin títulos (queda la entrada por defecto), duración de la semilla.

**Fase B — Fuentes y generación desde la base**
- Tablas `documento_fuente` y `pool_fuente`; `pregunta.id_oracion_evidencia`; selector de fuentes; `GeneracionIAServiceImpl` deja de leer PDF (`leerOraciones`, `empezarBanco`); rotación por referencia.
- Un índice de una sola entrada se trata como «sin índice» para resumen y presentación.

**Fase C — Bloqueo por uso**
- `fn_documento_en_uso`, mensajes, y que los modales se abran pero el guardado responda el error, como en el ABM de cursos.

**Fase D — Contenido generado editable** (diseño propio antes de empezar)
- Guardar lo generado como documento con oraciones, edición de oraciones, regenerar el PDF.

## 6. Decisiones que estaban abiertas (cerradas el 16/11)

1. **Claves compuestas: se implementan**, de pie a cabeza con el diagrama. Afecta a 11 entidades JPA: `TemaRecurso` (17 archivos en `main` y 8 en `test`), `Progreso` (19 y 5), `TemaCronograma` (26 y 7), `RecordatorioCohorte`, `RolUsuario`, `CursoModalidad`, `InteresAlumno`, `AutoevaluacionPool`, `PreguntaIntento` (11 y 3), `RespuestaIntento` y `ResaltadoAlumno`. Se hace en la fase 0b.
2. **Subtipos de documento: composición** (id propio más clave alternativa, `@OneToOne`), igual que `Alumno`, `Docente` y `Administrador` con `Usuario` y que `Enlace`, `Grabacion` y `VideoClon` con `Recurso`. Se mitiga con getters delegados.

## 7. Fuera de alcance

- Guardar lemas o categorías gramaticales en la base.
- Editar oraciones de documentos subidos.
- Sacar OpenNLP de la generación.
- Más categorías o tipos de identificador.
