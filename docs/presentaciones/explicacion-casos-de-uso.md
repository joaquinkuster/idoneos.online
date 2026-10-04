# 📘 Explicación Clara de Casos de Uso Reales — Idóneos Online
> **¿Qué es este archivo?**  
> Una explicación simple de los Casos de Uso más interesantes del sistema. Por cada CU te explico en lenguaje simple **qué pasa**, **qué datos de la base de datos se usan**, **cómo interactúan los objetos (DSS)** y **qué condiciones se verifican (Contratos)**.

---

# 📑 Índice de Casos de Uso Seleccionados

| # | Caso de Uso | Módulo | Actor |
| :--- | :--- | :--- | :--- |
| 1 | CU-03: Registrar Curso | MOD-F-01 | Administrador |
| 2 | CU-04: Modificar Curso | MOD-F-01 | Administrador |
| 3 | CU-05: Dar de Baja Curso | MOD-F-01 | Administrador |
| 4 | CU-10: Dar de Baja Categoría | MOD-F-01 | Administrador |
| 5 | CU-12: Registrar Cohorte | MOD-F-01 | Administrador |
| 6 | CU-14: Dar de Baja Cohorte | MOD-F-01 | Administrador |
| 7 | CU-16: Registrar Programa | MOD-F-02 | Docente |
| 8 | CU-20: Agregar Unidad al Cronograma | MOD-F-02 | Docente |
| 9 | CU-26: Acceder al Curso (Aula Virtual) | MOD-F-02 | Alumno |
| 10 | CU-44: Inscribir Curso | MOD-F-03 | Alumno |
| 11 | CU-47: Realizar Pago | MOD-F-03 | Alumno |
| 12 | CU-54: Crear Pool (Banco de Preguntas) | MOD-F-04 | Docente |
| 13 | CU-58: Crear Autoevaluación | MOD-F-04 | Docente |
| 14 | CU-63: Realizar Intento de Examen | MOD-F-04 | Alumno |
| 15 | CU-66: Programar Clase en Vivo | MOD-F-05 | Docente |
| 16 | CU-78: Generar Clase con Clon de IA | MOD-F-06 | Docente |
| 17 | CU-83: Registrar Usuario (con rol Docente) | MOD-F-07 | Administrador |
| 18 | CU-95: Consultar Auditoría | MOD-F-07 | Administrador |

---

# MÓDULO 1 — Catálogo y Gestión de Cursos

---

## 🟡 CU-03: Registrar Curso

**¿Qué hace?**  
El Administrador crea un nuevo curso en el sistema eligiendo su nombre, precio, categoría, nivel, docente titular, docentes ayudantes y modalidades de cursada (presencial, en vivo, grabado).

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Curso` | Se crea el nuevo registro con todos los datos del curso. |
| `Categoria` | Se elige de qué tema trata (ej: "Fintech"). |
| `Nivel` | Se selecciona la dificultad (Inicial, Intermedio, Avanzado). |
| `Docente` + `Usuario` | Se asigna el titular y se cargan los ayudantes. |
| `Ayudante` | Se guarda la lista de profesores colaboradores del curso. |
| `Modalidad` + `Modalidad Curso` | Se vincula el curso con sus formas de cursada. |

**¿Cómo se relacionan?**
- `Categoria` → `Curso`: **1 a N** (muchos cursos en una categoría).
- `Nivel` → `Curso`: **1 a N** (un nivel para muchos cursos).
- `Docente` → `Curso` (como titular): **1 a N**.
- `Docente` ↔ `Curso` (como ayudante): **N a M** via `Ayudante`.
- `Modalidad` ↔ `Curso`: **N a M** via `Modalidad Curso`.

**Qué verifica el Contrato (`registrarCurso`):**
- Que el nombre, descripción, precio, categoría, nivel, modalidad y docente titular no estén vacíos.
- Que la categoría elegida esté activa (`Categoria.baja = false`).
- Que el docente titular **no sea también ayudante** del mismo curso.
- Que el precio no sea negativo.

**¿Qué pasa en la BD cuando se confirma?**
- Se crea una fila en `Curso` con `baja = false` y `fechaCreacion = hoy`.
- Se vinculan las filas en `Ayudante` y en `Modalidad Curso`.

**Cómo funciona el DSS (qué consultas hace el sistema al abrir el formulario):**
1. Llama a `buscarCategorias()` → trae todas las categorías activas de la tabla `Categoria`.
2. Llama a `buscarNiveles()` → trae todos los niveles de la tabla `Nivel`.
3. Llama a `buscarDocentes()` → trae todos los docentes habilitados de `Docente` + `Usuario`.
4. Llama a `buscarModalidades()` → trae las opciones de `Modalidad`.
5. Al guardar, llama a `registrarCurso(...)` que crea el curso y todas sus relaciones.

**🗣️ Speech para el profesor:**
> *"En CU-03, cuando el admin abre el formulario, el sistema ejecuta en cascada 4 consultas: categorías, niveles, docentes y modalidades. Esto popular los selectores del formulario. Al confirmar, el contrato `registrarCurso` verifica las reglas de negocio más importantes: que el docente titular no aparezca también en la lista de ayudantes, y que la categoría no esté dada de baja. Recién ahí el sistema inserta en `Curso`, `Ayudante` y `Modalidad Curso`."*

---

## 🟡 CU-04: Modificar Curso

**¿Qué hace?**  
Permite editar un curso existente. Pero hay una regla importante: **si el curso ya tiene alumnos anotados, el sistema bloquea los cambios estructurales y solo deja cambiar el precio y la imagen.**

**Tablas de la BD que se usan:** Igual que CU-03, más:

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Inscripcion` | Se consulta si hay alumnos activos para decidir qué se puede editar. |

**La clave de este CU: el Bloque Alternativo del DSS**

El sistema tiene **dos caminos distintos** según la situación:

- **Camino 1** — Si `COUNT(Inscripcion WHERE idCurso = :id AND baja = false) = 0`:  
  → Se habilita el formulario completo: cambiar docente, nivel, categoría, modalidades, etc.  
  → Se llama a `modificarCurso(unCurso, nombre, descripcion, precio, imagen, ...)`.

- **Camino 2** — Si el curso tiene inscripciones activas:  
  → Solo se habilitan los campos de precio e imagen.  
  → Se llama a `modificarCursoPrecioImagen(unCurso, precio, imagen)`.

**¿Por qué esta regla?**  
Si un alumno se inscribió y pagó para cursar con un docente o en cierta modalidad, no se puede cambiar eso "a mitad del partido" sin su consentimiento.

**🗣️ Speech para el profesor:**
> *"CU-04 es un caso de uso muy interesante porque el DSS tiene un bloque alternativo condicional. El sistema primero consulta la tabla `Inscripcion` para ver si hay alumnos matriculados activos. Si los hay, invoca `modificarCursoPrecioImagen`, un contrato más restringido. Si no hay, invoca `modificarCurso` con todos los parámetros. Esto protege los derechos de los alumnos que ya se inscribieron."*

---

## 🟡 CU-05: Dar de Baja Curso

**¿Qué hace?**  
El Administrador elimina lógicamente un curso (NO lo borra de la BD).

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Curso` | Se actualiza el campo `baja = true`. |
| `Programa` | Se consulta si el curso tiene programas activos. |

**Regla de negocio del Contrato (`darDeBajaCurso`):**
- ❌ Si `COUNT(Programa WHERE idCurso = :id AND baja = false) > 0` → **No se puede dar de baja**. El modal muestra el error y el botón "Confirmar Baja" queda bloqueado.
- ✅ Si no tiene programas activos → Se pone `Curso.baja = true`.

**¿Qué pasa visualmente en el sistema?**  
El curso NO desaparece de la lista. Aparece con fondo gris apagado, badge "Dado de baja" y las acciones de editar/borrar bloqueadas con candado.

**🗣️ Speech para el profesor:**
> *"En CU-05 vemos el patrón de borrado lógico aplicado. El sistema no ejecuta ningún DELETE en la base de datos: solo pone `baja = true`. La excepción del paso 2 del caso de uso real bloquea la operación si el curso tiene programas vigentes asociados en la tabla `Programa`. Esto garantiza la integridad referencial a nivel de negocio, antes de llegar a la restricción de clave foránea de la base de datos."*

---

## 🟡 CU-10: Dar de Baja Categoría

**¿Qué hace?**  
Permite eliminar lógicamente una categoría, pero solo si ningún curso activo la está usando.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Categoria` | Se pone `baja = true` si se permite. |
| `Curso` | Se consulta cuántos cursos activos usan esa categoría. |

**Regla del Contrato (`darDeBajaCategoria`):**
- ❌ Si `Curso.idCategoria = :id AND Curso.baja = false` tiene resultados → **Bloqueado**: el modal muestra la lista de cursos que dependen y deshabilita el botón "Confirmar Eliminación".
- ✅ Si no hay cursos activos → `Categoria.baja = true`.

**DSS (flujo simple):**
1. Desde CU-07 (Buscar Categoría) se selecciona la categoría.
2. Se llama a `darDeBajaCategoria(unaCategoria)`.
3. El sistema verifica cursos dependientes → aprueba o bloquea.

**🗣️ Speech para el profesor:**
> *"CU-10 demuestra cómo el sistema protege la integridad de los datos a nivel de negocio. Antes de permitir la baja, consulta la tabla `Curso` para ver si la categoría está en uso. Si está en uso, informa exactamente cuáles son y bloquea preventivamente la acción, mostrando la lista de cursos dependientes en el modal de confirmación."*

---

## 🟡 CU-12: Registrar Cohorte

**¿Qué hace?**  
Crea una nueva "edición" de cursada para un programa (ej: "Cohorte Marzo 2026"). Define cuándo empieza la inscripción, cuándo empiezan las clases, el cupo y cuánto tiempo tendrán acceso los alumnos.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Cohorte` | Se crea el nuevo período con todas las fechas. |
| `Programa` | Se selecciona el plan de estudios al que pertenece. |
| `Cronograma` | Se consulta para validar que las semanas de acceso sean suficientes. |

**Validaciones clave del Contrato (`registrarCohorte`):**
1. Las fechas de inscripción deben ser coherentes: fin > inicio.
2. Las fechas de dictado también: fin > inicio.
3. El dictado no puede empezar antes de que cierren las inscripciones.
4. El cupo máximo tiene que ser un número entero mayor a 0.
5. Las semanas de acceso tienen que ser ≥ a la duración total de las unidades del cronograma.
6. El programa tiene que tener al menos las unidades mínimas requeridas con material publicado.

**DSS (lo que pasa al abrir el formulario):**
1. El admin primero busca el programa (CU-15 → `obtenerProgramaVigente`).
2. Se abre el formulario de cohorte.
3. Al guardar → `registrarCohorte(unPrograma, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)`.

**🗣️ Speech para el profesor:**
> *"CU-12 es rico en validaciones cruzadas de fechas. El contrato `registrarCohorte` verifica 6 condiciones distintas antes de permitir el alta. La más interesante desde el punto de vista académico es la última: las semanas de acceso otorgadas al alumno deben ser suficientes para cubrir la duración total del cronograma del programa. Esto lo verifica consultando la tabla `Cronograma`."*

---

## 🟡 CU-14: Dar de Baja Cohorte

**¿Qué hace?**  
Cancela una cohorte (edición de cursada). Solo se permite si **ningún alumno está actualmente inscripto** en esa cohorte.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Cohorte` | Se pone `baja = true` si se permite. |
| `Inscripcion` | Se consulta si hay alumnos matriculados activos. |

**Regla del Contrato (`darDeBajaCohorte`):**
- ❌ Si `Inscripcion WHERE idCohorte = :id AND baja = false` tiene resultados → **Bloqueado**: se muestran los alumnos afectados y el botón "Confirmar Cancelación" queda inhabilitado.
- ✅ Si no hay inscripciones activas → `Cohorte.baja = true`.

**¿Qué se ve en la pantalla después?**  
La cohorte no desaparece de la tabla. Aparece con fondo gris, badge "Cohorte cancelado" y el botón de cancelar bloqueado.

**🗣️ Speech para el profesor:**
> *"CU-14 protege los derechos de los alumnos inscriptos. El contrato `darDeBajaCohorte` consulta la tabla `Inscripcion` en busca de registros activos. Si los encuentra, muestra al administrador exactamente quiénes son los alumnos afectados y bloquea el botón de confirmar. El registro queda con borrado lógico visible en la tabla para que el historial quede intacto."*

---

# MÓDULO 2 — Gestión Académica, Programas y Unidades

---

## 🟢 CU-16: Registrar Programa

**¿Qué hace?**  
El Docente crea un nuevo plan de estudios para un curso. Puede partir de cero o copiar el cronograma de un programa anterior.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Programa` | Se crea la nueva versión del plan de estudios. |
| `Curso` | El curso al que pertenece el programa. |
| `Cronograma` | Si se eligió un programa anterior, se copian sus unidades y duraciones. |

**Lo más interesante: la función de "Copiar programa base"**
- Si el docente decide partir de un programa anterior, el sistema hace `registrarPrograma(..., unProgramaAnterior)`.
- El contrato dice: *"Si se indicó un programa anterior, se copia su cronograma (unidades, orden y duración) al nuevo programa como punto de partida editable."*
- Esto significa que las filas de `Cronograma` del programa viejo se duplican apuntando al nuevo `Programa.idPrograma`.

**DSS:**
1. El docente busca el curso (`buscarCursos`).
2. Opcionalmente elige un programa anterior como base (`buscarProgramas`).
3. Completa los campos y llama a `registrarPrograma(unCurso, nombre, objetivos, cargaHorariaTotal, bibliografia, unProgramaAnterior)`.

**🗣️ Speech para el profesor:**
> *"CU-16 implementa el patrón de versionado de planes de estudio. Un curso puede tener múltiples programas a lo largo del tiempo sin perder el historial. La funcionalidad de 'copiar de un programa anterior' resuelve la tabla `Cronograma`: el sistema clona las filas existentes cambiando solo la FK `idPrograma` hacia el nuevo plan, permitiendo al docente trabajar sobre un punto de partida sin modificar el programa anterior que puede tener cohortes activas."*

---

## 🟢 CU-20: Agregar Unidad al Cronograma

**¿Qué hace?**  
El Docente agrega una unidad temática al cronograma del programa. Puede crear una unidad nueva o reutilizar una unidad que ya existe en otro programa.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Unidad` | Se crea la nueva unidad o se selecciona una existente. |
| `Cronograma` | Se crea la fila que vincula la unidad con el programa. |
| `Programa` | El programa al que se agrega la unidad. |

**El bloque alternativo del DSS tiene 2 caminos:**

- **Camino 1** — Crear unidad nueva:  
  → `crearUnidad(unPrograma, titulo, descripcion, contenido)`.  
  → Se crea una fila en `Unidad` y otra en `Cronograma`.

- **Camino 2** — Reutilizar una unidad de otro programa:  
  → `buscarUnidadesReutilizables(unPrograma)` trae unidades de otros programas del mismo docente.  
  → `agregarUnidadExistente(unPrograma, unaUnidad)`.  
  → Solo se crea la fila en `Cronograma` (la `Unidad` ya existe, no se duplica).

**¿Por qué esto es importante?**  
Una misma unidad ("Introducción a la Renta Fija") puede estar en múltiples programas de distintos cursos sin duplicar el contenido en la base de datos.

**🗣️ Speech para el profesor:**
> *"CU-20 demuestra el diseño inteligente del modelo relacional. La tabla `Cronograma` actúa como tabla asociativa N a M entre `Programa` y `Unidad`. Cuando un docente reutiliza una unidad existente, el sistema solo inserta una fila nueva en `Cronograma` sin tocar la tabla `Unidad`. Esto evita la duplicación de contenido y permite que los cambios en una unidad se reflejen en todos los programas que la comparten."*

---

## 🟢 CU-26: Acceder al Curso (Aula Virtual)

**¿Qué hace?**  
El Alumno entra al Aula Virtual del curso en el que está inscripto. Desde ahí puede ver las unidades, materiales, glosario, foro y autoevaluaciones.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Inscripcion` | Valida que el alumno tiene acceso y que no venció su fecha de acceso. |
| `Cronograma` + `Unidad` | Lista las unidades en el orden correcto del menú lateral. |
| `Progreso` | Muestra qué unidades ya completó y cuáles están bloqueadas. |
| `Material` | Videos, PDFs y presentaciones de cada unidad. |
| `TerminoGlosario` | Términos y definiciones del glosario interactivo. |
| `ConsultaForo` + `RespuestaForo` | Preguntas del alumno y respuestas del docente. |
| `Autoevaluacion` | Lista los exámenes disponibles de cada unidad. |
| `ClaseEnVivo` | Clases programadas o en directo de la cohorte. |

**El DSS de este CU tiene 4 caminos alternativos:**
1. El alumno abre una unidad: trae `materiales, glosario, foro, autoevaluaciones, clases`.
2. El alumno ve los participantes de su cohorte.
3. El alumno consulta sus calificaciones.
4. El alumno ve el cronograma completo del programa.

**Regla de avance secuencial:**
La tabla `Progreso` tiene una fila por cada combinación de `Inscripcion` + `Unidad`. El campo `completada` determina si la siguiente unidad está desbloqueada o no.

**🗣️ Speech para el profesor:**
> *"CU-26 es el corazón pedagógico del sistema. Cada vez que el alumno entra al aula, el sistema consulta la tabla `Cronograma` para armar la barra lateral de unidades en orden, y la tabla `Progreso` para marcar con un tilde verde cuáles ya terminó y cuáles siguen bloqueadas. Al abrir una unidad se hace una sola consulta que trae materiales, glosario, foro, autoevaluaciones y clases en vivo de esa unidad."*

---

# MÓDULO 3 — Inscripciones, Pagos y Descuentos

---

## 🔵 CU-44: Inscribir Curso

**¿Qué hace?**  
El Alumno elige un curso del catálogo, selecciona la fecha de cursada disponible (cohorte) y se anota.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Curso` | El curso elegido por el alumno. |
| `Cohorte` | Se muestran las ediciones abiertas con cupo disponible. |
| `Inscripcion` | Se crea la matrícula del alumno. |
| `Pago` | Si el curso es arancelado, se inicia el flujo de pago (CU-47). |

**Reglas para que una cohorte aparezca disponible:**
- `Cohorte.baja = false` (no cancelada).
- Fecha actual está entre `fechaInicioInscripcion` y `fechaFinInscripcion`.
- `COUNT(Inscripcion WHERE idCohorte = :id) < Cohorte.cupoMaximo` (hay lugar).

**DSS:**
1. Desde el catálogo el alumno selecciona un curso (`buscarCursosAbiertos`).
2. El sistema trae las cohortes con cupo disponible (`buscarCohortesAbiertas`).
3. El alumno elige una cohorte → `registrarInscripcion(unaCohorte)`.
4. Si el curso tiene costo → automáticamente se encadena con CU-47.

**🗣️ Speech para el profesor:**
> *"CU-44 integra el módulo de catálogo con el de inscripciones. El contrato `buscarCohortesAbiertas` filtra la tabla `Cohorte` revisando la fecha actual y el cupo restante comparando el count de `Inscripcion`. Al confirmar la inscripción, si el curso tiene precio mayor a cero, el DSS encadena directamente al caso de uso CU-47 de pago."*

---

## 🔵 CU-47: Realizar Pago

**¿Qué hace?**  
El Alumno paga el arancel del curso usando la pasarela de pagos (MODO). El sistema calcula si hay descuentos y registra todo el proceso.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Pago` | Se guarda el registro del cobro con todos los datos fiscales. |
| `EstadoPago` | Estado actual: Pendiente, Acreditado o Rechazado. |
| `MetodoPago` | MODO, Tarjeta, Transferencia. |
| `Descuento` | Si el alumno califica, se le aplica un descuento automático. |
| `Inscripcion` | Al acreditarse el pago, se actualiza con la fecha de vencimiento de acceso. |

**DSS paso a paso:**
1. `calcularMonto(unCurso)` → El sistema revisa si hay algún `Descuento` activo y vigente que aplique. Devuelve el monto final y el descuento usado.
2. `buscarMediosPago()` → Trae las opciones de la tabla `MetodoPago`.
3. El alumno elige → `seleccionarMedioPago(...)`.
4. `realizarPago(unaInscripcion, unMedioPago)` → Se crea en `Pago` con `idEstadoPago = Pendiente`, se abre el modal de MODO y se guardan los IDs externos (`paymentRequestId`, `externalIntentionId`).
5. Cuando MODO confirma → El sistema cambia `idEstadoPago = Acreditado`, genera el `numeroComprobante` y activa el acceso al alumno.

**🗣️ Speech para el profesor:**
> *"CU-47 implementa el flujo de cobro con una pasarela de pago externa. Lo más interesante es que se guardan los identificadores externos de MODO en la tabla `Pago` para garantizar idempotencia: si el alumno cierra el navegador y vuelve, el sistema puede verificar el estado del pago sin volver a cobrar. Al acreditarse, el sistema calcula automáticamente `fechaVencimientoAcceso` en `Inscripcion`."*

---

# MÓDULO 4 — Evaluaciones y Exámenes

---

## 🟣 CU-54: Crear Pool (Banco de Preguntas)

**¿Qué hace?**  
El Docente crea un banco de preguntas asociado a una unidad. Cada pregunta puede ser opción múltiple o verdadero/falso, con sus opciones de respuesta y cuál es la correcta.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Pool` | Nombre del banco y la unidad a la que pertenece. |
| `Pregunta` | Cada pregunta (enunciado y tipo). |
| `OpcionRespuesta` | Las alternativas de cada pregunta. Una de ellas tiene `esCorrecta = true`. |
| `Unidad` | La unidad temática del banco. |

**Relaciones:**
- `Unidad` → `Pool`: **1 a N**.
- `Pool` → `Pregunta`: **1 a N**.
- `Pregunta` → `OpcionRespuesta`: **1 a N**.

**DSS: tiene un bucle anidado**

```
LOOP (mientras haya preguntas para agregar):
    → agregarPreguntaUnPool(tipo, enunciado) → crea fila en Pregunta
    LOOP (mientras haya opciones para esa pregunta):
        → agregarOpcion(unaPregunta, texto, esCorrecta) → crea fila en OpcionRespuesta
```

**Reglas del Contrato:**
- Una pregunta debe tener mínimo 2 opciones y al menos 1 correcta.
- No se puede modificar ni borrar un pool si ya tiene intentos de examen asociados.

**🗣️ Speech para el profesor:**
> *"CU-54 muestra la jerarquía del motor de evaluación: Pool → Pregunta → OpcionRespuesta. El DSS implementa bucles anidados para ir construyendo el banco pregunta por pregunta y opción por opción. El campo `esCorrecta` en `OpcionRespuesta` es el que el sistema usará luego en CU-63 para corregir el examen automáticamente."*

---

## 🟣 CU-58: Crear Autoevaluación

**¿Qué hace?**  
El Docente configura un examen para una unidad: cuántas preguntas toma, cuánto tiempo tiene el alumno, cuántos intentos permite y de cuáles bancos de preguntas se sortean.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Autoevaluacion` | La configuración del examen. |
| `Pool Autoevaluacion` | Qué bancos se usan para armar el examen (N a M). |
| `Pool` | Los bancos disponibles de la unidad. |
| `Unidad` | La unidad a la que pertenece el examen. |

**Atributos importantes de `Autoevaluacion`:**
- `tiempoLimite` → Minutos para responder.
- `cantidadPreguntas` → Cuántas preguntas se sortean de los pools.
- `intentosPermitidos` → Cuántas veces puede rendir el alumno.
- `fechaApertura` y `fechaCierre` → Ventana de tiempo para rendir.

**DSS:**
1. El docente busca la unidad (`buscarUnidades`).
2. El sistema trae los pools de esa unidad (`buscarPools`).
3. El docente elige cuáles pools participan → `seleccionarPools(...)`.
4. Llama a `crearAutoevaluacion(unaUnidad, nombre, tiempoLimite, cantidadPreguntas, fechaApertura, fechaCierre, intentosPermitidos, poolsSeleccionados)`.

**Si ya tiene intentos (CU-59 Modificar):** Solo se pueden cambiar `fechaCierre`, `visible` e `intentosPermitidos`. No se pueden cambiar las preguntas ni los pools para no invalidar intentos ya realizados.

**🗣️ Speech para el profesor:**
> *"CU-58 configura el examen usando una relación N a M entre `Autoevaluacion` y `Pool` a través de la tabla `Pool Autoevaluacion`. El docente puede combinar múltiples bancos para que el examen sea más variado. Una vez que hay intentos rendidos, el contrato `modificarAutoevaluacion` bloquea los cambios estructurales para preservar la validez académica de los resultados ya registrados."*

---

## 🟣 CU-63: Realizar Intento de Examen

**¿Qué hace?**  
El Alumno rinde el examen. El sistema sortea preguntas al azar de los pools, el alumno responde con tiempo límite, y al entregar el sistema califica automáticamente.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Autoevaluacion` | Trae la configuración del examen. |
| `Pool Autoevaluacion` | Determina de cuáles pools se toman las preguntas. |
| `Pregunta` | Las preguntas sorteadas. |
| `OpcionRespuesta` | Las opciones mostradas al alumno. |
| `IntentoAutoevaluacion` | El examen rendido: fecha, nota final. |
| `RespuestaIntento` | Qué opción eligió el alumno en cada pregunta. |
| `Progreso` | Si aprobó, se marca la unidad como completada. |

**DSS: el flujo del examen**

```
→ iniciarIntento(unaAutoevaluacion)
   → Sistema sortea cantidadPreguntas de los pools en Pool Autoevaluacion
   → Crea fila en IntentoAutoevaluacion (estado: en curso)
   
LOOP (mientras hay preguntas):
   → siguientePregunta(preguntas)
   → buscarOpciones(unaPregunta)
   → El alumno elige → seleccionarOpcion(idOpcion, opciones)
   → agregarRespuesta(unIntento, unaOpcion)  ← inserta en RespuestaIntento
   
→ entregarIntento(unIntento)
   → Sistema lee RespuestaIntento y contrasta con OpcionRespuesta.esCorrecta
   → Calcula la nota (aciertos / total * 10)
   → Guarda la nota en IntentoAutoevaluacion.nota
   → Si nota >= 7 → Progreso.completada = true para esa unidad
```

**🗣️ Speech para el profesor:**
> *"CU-63 es el caso de uso más complejo del módulo de evaluaciones. El DSS tiene un bucle que recorre cada pregunta sorteada. Lo más destacable es la trazabilidad: cada respuesta del alumno se guarda en la tabla `RespuestaIntento` vinculando el intento con la opción elegida. Al entregar, el sistema contrasta esas opciones con `OpcionRespuesta.esCorrecta` para calificar automáticamente. Si el alumno aprueba, `Progreso.completada` se actualiza y desbloquea la siguiente unidad."*

---

# MÓDULO 5 — Clases en Vivo

---

## 🟠 CU-66: Programar Clase en Vivo

**¿Qué hace?**  
El Docente programa una sesión de streaming para una cohorte activa de una unidad específica.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `ClaseEnVivo` | Guarda los datos de la sesión. |
| `EstadoClaseEnVivo` | Estado: Programada, En Directo, Finalizada o Cancelada. |
| `Unidad` | La unidad a la que pertenece la clase. |
| `Cohorte` | La cohorte que puede acceder. |
| `Docente` | El profesor que da la clase. |

**Atributos de `ClaseEnVivo`:**
- `titulo`, `fechaHora`, `duracionEstimada`.
- `urlRtmp` y `claveStream` → Credenciales privadas de streaming para OBS Studio.

**DSS:**
1. El docente busca la unidad → selecciona la cohorte en dictado (`buscarCohortesEnDictado`).
2. Llama a `programarClaseEnVivo(unaUnidad, unaCohorte, titulo, fechaHora, duracionEstimada)`.
3. El sistema crea la fila en `ClaseEnVivo` con `idEstado = Programada`.

**Lo que pasa cuando se inicia la clase (CU-70):**
- Se llama a `iniciarClaseEnVivo(unaClase)` → devuelve `datosConexion` con `urlRtmp` y `claveStream`.
- El docente los copia a OBS Studio y comienza la transmisión.
- El estado cambia a `En Directo`.

**🗣️ Speech para el profesor:**
> *"En CU-66, el docente programa la sesión de streaming vinculando la clase a una unidad y una cohorte específica. La tabla `ClaseEnVivo` guarda los parámetros RTMP para que el docente pueda transmitir con OBS. Cuando inicia la clase (CU-70), el sistema genera las credenciales de conexión y actualiza el badge a 'EN VIVO'. Al finalizar (CU-71), el estado cambia a 'Finalizada' y la grabación puede adjuntarse como un Material de la unidad."*

---

# MÓDULO 6 — Clones con IA

---

## 🔴 CU-78: Generar Clase con Clon de IA

**¿Qué hace?**  
El Docente ingresa un guion de texto y el sistema genera automáticamente un video de la clase usando el avatar y la voz del docente clonados con IA.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `ClaseClon` | El video a generar: título, guion, estado de generación. |
| `EstadoClaseClon` | Generando, Lista o Error. |
| `Docente` | Tiene `avatarId` y `voiceId` (IDs de la API de IA). |
| `Material` | Cuando termina de generarse, el video queda como material de la unidad. |
| `Unidad` | La unidad a la que pertenece el video generado. |

**Requisito previo:**  
`Docente.fechaAceptacionTycClon IS NOT NULL` → El docente debe haber aceptado los términos y condiciones de uso del clon antes de poder usarlo.

**DSS:**
1. El docente busca la unidad.
2. Llama a `generarClaseConClon(unaUnidad, titulo, guion)`.
3. El sistema crea la fila en `ClaseClon` con `idEstado = Generando`.
4. En paralelo, la API de IA genera el video usando `Docente.avatarId` y `Docente.voiceId`.
5. Al terminar → `idEstado = Lista` y se crea una fila en `Material` con `generadoPorIa = true`.

**🗣️ Speech para el profesor:**
> *"CU-78 integra la IA Generativa de la plataforma. La tabla `Docente` almacena los identificadores del avatar y la voz del docente (`avatarId`, `voiceId`). Al solicitar la generación, se crea la clase en estado 'Generando' y el proceso es asincrónico: cuando la API de IA termina, actualiza el estado a 'Lista' y registra el video como un `Material` con la bandera `generadoPorIa = true` para que los alumnos sepan que fue creado con IA."*

---

# MÓDULO 7 — Administración, Usuarios y Auditoría

---

## ⚫ CU-83: Registrar Usuario con Rol Docente

**¿Qué hace?**  
El Administrador crea un nuevo usuario en el sistema y si su rol es Docente, completa también su perfil profesional con experiencia, matrícula CNV y títulos académicos.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Usuario` | Datos generales: nombre, apellido, DNI, email, contraseña. |
| `Rol` | Determina si es Alumno, Docente o Administrador. |
| `Docente` | Perfil profesional del docente (vinculado 1 a 1 con `Usuario`). |
| `TituloDocente` | Lista de títulos universitarios y matrículas del docente. |

**Relaciones clave:**
- `Rol` → `Usuario`: **1 a N** (un rol tiene muchos usuarios).
- `Usuario` ↔ `Docente`: **1 a 1** (FK: `Docente.idUsuario`).
- `Docente` → `TituloDocente`: **1 a N** (un docente tiene varios títulos).

**DSS:**
1. `buscarRoles()` → trae las opciones de la tabla `Rol`.
2. Admin elige el rol → `seleccionarRol(...)`.
3. `registrarUsuario(nombre, apellido, correo, dni, telefono, unRol)` → crea fila en `Usuario`.
4. **Si el rol es Docente** → Se encadena con CU-88:
   - `registrarDocente(unUsuario, biografia, aniosExperiencia, matriculaCnv)` → crea fila en `Docente`.
   - Bucle: `agregarTitulo(unDocente, titulo, matriculaColegio)` → crea filas en `TituloDocente`.

**🗣️ Speech para el profesor:**
> *"CU-83 demuestra el patrón de especialización del modelo relacional. La tabla `Usuario` centraliza los datos de seguridad y login para todos los roles. Si el usuario es Docente, el DSS encadena automáticamente con CU-88 que completa la tabla `Docente` (vinculada 1 a 1 con `Usuario`) y agrega tantos títulos académicos como sean necesarios en `TituloDocente`. Este diseño evita columnas nulas en la tabla base."*

---

## ⚫ CU-95: Consultar Auditoría

**¿Qué hace?**  
El Administrador consulta el historial completo e inmutable de todas las operaciones sensibles del sistema: quién cambió qué, cuándo y desde qué IP.

**Tablas de la BD que se usan:**

| Tabla | ¿Para qué? |
| :--- | :--- |
| `Auditoria` | Cabecera del evento: entidad afectada, ID del registro, usuario, IP y fecha. |
| `DetalleAuditoria` | El cambio campo por campo: nombre del campo, valor viejo y valor nuevo. |
| `TipoAccionAuditoria` | Categoría de la acción: Alta, Modificación, Baja Lógica, Login. |
| `Usuario` | El responsable que realizó la acción. |

**Relaciones:**
- `TipoAccionAuditoria` → `Auditoria`: **1 a N**.
- `Usuario` → `Auditoria`: **1 a N** (un usuario genera muchos registros).
- `Auditoria` → `DetalleAuditoria`: **1 a N** (una acción puede tocar varios campos).

**Atributos de `Auditoria`:**
- `entidadAfectada` (ej: "Curso", "Pago", "Cohorte").
- `idAfectado` (el ID del registro modificado).
- `ipUsuario` (la dirección IP desde donde se hizo la operación).
- `fechaHora` (timestamp exacto).

**Atributos de `DetalleAuditoria`:**
- `campo` (ej: "precio").
- `valorAnterior` (ej: "100000.0").
- `valorNuevo` (ej: "150000.0").

**Ejemplos de lo que queda registrado:**
- Dar de baja un curso → 1 fila en `Auditoria` (tipo: "Baja Lógica") + 1 fila en `DetalleAuditoria` (campo: "baja", viejo: "false", nuevo: "true").
- Cambiar el precio → 1 fila en `Auditoria` + 1 fila en `DetalleAuditoria` (campo: "precio").
- Iniciar sesión desde una IP desconocida → 1 fila en `Auditoria` (tipo: "Login").

**DSS (simple):**
1. `buscarRegistrosAuditoria(usuario, tipoAccion, entidad, rangoFechas)` → filtra la tabla y devuelve la lista.

**🗣️ Speech para el profesor:**
> *"CU-95 implementa una capa de auditoría inmutable. Las tablas `Auditoria` y `DetalleAuditoria` son de solo inserción: nadie puede modificar ni borrar sus registros, ni siquiera el administrador. Por cada operación sensible el sistema graba en dos niveles: la cabecera (quién, cuándo, desde qué IP, qué entidad) y el detalle (qué campo cambió, de qué valor a qué valor). Esto cumple con los requisitos normativos de trazabilidad para plataformas de certificación financiera reguladas por la CNV."*

---

# 🧠 Resumen de las Reglas de Negocio más Importantes

| Caso de Uso | Regla de Negocio Clave |
| :--- | :--- |
| CU-03: Registrar Curso | El docente titular no puede ser también ayudante del mismo curso. |
| CU-04: Modificar Curso | Si hay alumnos inscriptos → solo se puede cambiar precio e imagen. |
| CU-05: Dar de Baja Curso | Solo se puede dar de baja si no tiene programas activos. |
| CU-10: Dar de Baja Categoría | Solo se puede si no hay cursos activos que la usen. |
| CU-12: Registrar Cohorte | Las semanas de acceso deben cubrir la duración del cronograma. |
| CU-14: Dar de Baja Cohorte | Solo si no hay alumnos inscriptos activos. |
| CU-16: Registrar Programa | Al copiar un programa anterior, se clonan las filas de `Cronograma`. |
| CU-20: Agregar Unidad | Se puede reutilizar una unidad sin duplicarla en la BD. |
| CU-44: Inscribir Curso | La cohorte debe estar abierta y con cupo disponible. |
| CU-47: Realizar Pago | Se guardan los IDs externos de MODO para garantizar idempotencia. |
| CU-54: Crear Pool | Una pregunta necesita mínimo 2 opciones y 1 correcta. |
| CU-58: Crear Autoevaluación | Con intentos activos → solo se puede cambiar fecha de cierre e intentos. |
| CU-63: Realizar Intento | Cada respuesta del alumno queda guardada en `RespuestaIntento` (auditable). |
| CU-83: Registrar Docente | El DSS encadena automáticamente el alta de docente y sus títulos. |
| CU-95: Auditoría | La tabla `Auditoria` es inmutable: solo se permite insertar, nunca borrar. |

---
*Documento consolidado para la defensa técnica del Trabajo Final — Idóneos Online — 2026.*
