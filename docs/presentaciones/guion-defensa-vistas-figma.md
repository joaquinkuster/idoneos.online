# IDÓNEOS ONLINE — Trabajo Final Küster & Martínez
## Guion de Defensa — 100 Wireframes de Casos de Uso

> **Cómo usar este documento:** Por cada pantalla en Figma, encontrás el CU explicado: qué hace, qué tablas usa, las reglas clave y un **Speech** listo para decirle al profesor.

---

# 📁 MOD-F-01: Módulo de Cursos

---

### CU-01 · Buscar Curso
**Actor:** Docente / Administrador

**¿Qué hace?** El actor ingresa filtros (nombre, categoría, nivel, docente, modalidad) y el sistema devuelve los cursos que coinciden. Si es Docente, solo ve los cursos donde participa.

**Tablas:** `Curso`, `Categoria`, `Nivel`, `Docente`, `Ayudante`, `Modalidad`, `ModalidadCurso`

**Contrato buscarCursos:** Todos los filtros son opcionales. Si el actor es Docente, se agrega un WHERE que lo restringe a sus cursos como titular o ayudante.

**Speech:** *"CU-01 es el punto de entrada al módulo de cursos. El DSS itera sobre la colección cursos y evalúa cumpleFiltros() en cada instancia. Si el actor es Docente, el sistema agrega automáticamente un criterio extra: el docente debe figurar como titular en Curso.idDocente o como ayudante en la tabla Ayudante."*

---

### CU-02 · Ver Mis Cursos
**Actor:** Alumno

**¿Qué hace?** El alumno ve sus propias inscripciones activas con el progreso de cada una.

**Tablas:** `Inscripcion`, `Cohorte`, `Programa`, `Curso`, `Progreso`

**Contrato buscarInscripcionesAlumno:** Filtra por idAlumno = sesión actual.

**Speech:** *"CU-02 es la vista personal del alumno. Consulta Inscripcion WHERE idAlumno = el alumno logueado, y para cada inscripción calcula el progreso leyendo cuántas filas de Progreso tienen completada = true versus el total de unidades del Cronograma."*

---

### CU-03 · Registrar Curso
**Actor:** Administrador

**¿Qué hace?** Crea un nuevo curso con toda su información comercial y académica.

**Tablas:** `Curso`, `Categoria`, `Nivel`, `Docente`, `Ayudante`, `Modalidad`, `ModalidadCurso`

**Reglas clave del contrato:**
- Campos obligatorios: nombre, descripción, precio >= 0, categoría, nivel, al menos una modalidad, docente titular.
- La categoría debe estar activa (baja = false).
- El docente titular no puede aparecer también como ayudante.

**Al confirmar:** INSERT en Curso con baja = false y fechaCreacion = hoy. También INSERTs en Ayudante y ModalidadCurso.

**Speech:** *"En CU-03 el formulario se carga con 4 consultas al abrir: buscarCategorias, buscarNiveles, buscarDocentes, buscarModalidades. Lo más interesante es que valida que el docente titular no sea también ayudante: si aparece en ambos campos, el sistema rechaza el guardado antes de tocar la base de datos."*

---

### CU-04 · Modificar Curso
**Actor:** Administrador

**¿Qué hace?** Edita los datos de un curso existente. Tiene dos caminos según si hay alumnos inscriptos activos.

**Tablas:** `Curso`, `Categoria`, `Nivel`, `Docente`, `Ayudante`, `Modalidad`, `ModalidadCurso`, `Inscripcion`

**La regla clave — bloque alternativo del DSS:**
- Sin inscripciones activas: se habilita el formulario completo.
- Con inscripciones activas: solo precio e imagen son editables.

**Speech:** *"CU-04 es un caso con bloque alternativo en el DSS. Antes de mostrar el formulario, el sistema consulta Inscripcion WHERE idCurso = :id AND baja = false. Si hay resultados, deshabilita los campos de docente, nivel, categoría y modalidades. Solo precio e imagen permanecen editables."*

---

### CU-05 · Dar de Baja Curso
**Actor:** Administrador

**¿Qué hace?** Elimina lógicamente un curso (baja = true, NO borra de la BD).

**Tablas:** `Curso`, `Programa`

**Regla bloqueante:** Si COUNT(Programa WHERE idCurso = :id AND baja = false) > 0, el botón "Confirmar Baja" queda deshabilitado y se listan los programas dependientes.

**Visualmente:** el curso queda con fondo gris y badge "Dado de baja".

**Speech:** *"CU-05 implementa borrado lógico puro: no hay ningún DELETE. El contrato verifica primero que no existan programas activos en la tabla Programa. Si los hay, bloquea la operación preventivamente mostrando la lista de dependencias antes de que el admin pueda confirmar."*

---

### CU-06 · Explorar Catálogo de Cursos
**Actor:** Alumno / visitante

**¿Qué hace?** Muestra el catálogo público de cursos con cohortes abiertas. Cualquier visitante puede verlo sin sesión.

**Tablas:** `Curso`, `Cohorte`, `Categoria`, `Nivel`, `Modalidad`, `Docente`, `Usuario`

**Contrato buscarCursosAbiertos:** No requiere sesión. Devuelve cursos con al menos una cohorte donde la fecha actual está dentro del período de inscripción y baja = false.

**Speech:** *"CU-06 es el escaparate público. El contrato consulta Cohorte buscando registros donde fechaActual BETWEEN fechaInicioInscripcion AND fechaFinInscripcion. También verifica cupo disponible comparando COUNT(Inscripcion WHERE idCohorte) contra Cohorte.cupoMaximo."*

---

### CU-07 · Buscar Categoría
**Actor:** Administrador

**¿Qué hace?** Lista y filtra las categorías temáticas existentes.

**Tablas:** `Categoria` — columnas: nombre, descripcion, baja, fechaCreacion

**Speech:** *"CU-07 es la pantalla de gestión de categorías. Es el punto de entrada para CU-08, CU-09 y CU-10. Las categorías dadas de baja siguen apareciendo pero con badge gris para preservar el historial."*

---

### CU-08 · Registrar Categoría
**Actor:** Administrador

**Tablas:** `Categoria`

**Reglas:** nombre no puede estar vacío; no puede existir ya una categoría activa con el mismo nombre.

**Speech:** *"CU-08 es un INSERT simple en Categoria. La validación de nombre único busca si ya existe otra categoría con baja = false y el mismo nombre para evitar duplicados."*

---

### CU-09 · Modificar Categoría
**Actor:** Administrador

**Tablas:** `Categoria`, `Inscripcion`

**Regla:** Si la categoría tiene inscripciones activas (vía los cursos que la usan) → no se puede modificar.

---

### CU-10 · Dar de Baja Categoría
**Actor:** Administrador

**Tablas:** `Categoria`, `Curso`

**Regla bloqueante:** Si Curso WHERE idCategoria = :id AND baja = false tiene resultados, el botón "Confirmar Eliminación" queda deshabilitado.

**Speech:** *"CU-10 demuestra el patrón de integridad de negocio. El sistema lista los cursos activos que usan esa categoría y bloquea el botón. No puede haber cursos activos huérfanos sin categoría."*

---

### CU-11 · Buscar Cohorte
**Actor:** Docente / Administrador

**¿Qué hace?** Lista las ediciones de cursada (cohortes) de un programa dado.

**Tablas:** `Cohorte` — columnas: fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, baja. También: `Programa`

**Speech:** *"Una cohorte es la apertura de un programa para un período específico. La tabla Cohorte tiene sus propias fechas de inscripción y de dictado, independientes del programa al que pertenece."*

---

### CU-12 · Registrar Cohorte
**Actor:** Administrador

**¿Qué hace?** Crea una nueva edición de cursada con fechas, cupo y semanas de acceso.

**Tablas:** `Cohorte`, `Programa`, `Cronograma`

**6 validaciones del contrato:**
1. fechaFinInscripcion > fechaInicioInscripcion
2. fechaFinDictado > fechaInicioDictado (si aplica)
3. fechaInicioDictado >= fechaFinInscripcion
4. cupoMaximo > 0 (si se definió)
5. semanasAcceso >= suma de semanasUnidad del Cronograma
6. El programa debe tener el mínimo de unidades con material publicado

**Speech:** *"CU-12 es el que hace público el curso en el catálogo. La validación número 5 es la más académica: las semanas de acceso tienen que ser suficientes para cubrir toda la duración del cronograma. El sistema suma todos los semanasUnidad del Cronograma para obtener ese mínimo."*

---

### CU-13 · Modificar Cohorte
**Actor:** Administrador

**Tablas:** `Cohorte`, `Inscripcion`

**Regla:** Con inscripciones activas no se pueden cambiar las fechas ni el cupo.

---

### CU-14 · Dar de Baja Cohorte
**Actor:** Administrador

**Tablas:** `Cohorte`, `Inscripcion`

**Regla bloqueante:** Si hay inscripciones activas, se muestran los alumnos afectados y el botón "Confirmar Cancelación" queda inhabilitado.

**Visualmente:** La cohorte sigue en la tabla con badge "Cohorte cancelada" y fondo gris.

**Speech:** *"CU-14 cierra el ciclo del módulo de cursos. Si existe una sola inscripción activa, el contrato bloquea la operación mostrando quiénes son los alumnos. El registro queda visible con borrado lógico para preservar el historial de auditoría."*

---

# 📁 MOD-F-02: Módulo de Gestión Académica

---

### CU-15 · Buscar Programa
**Actor:** Docente / Administrador

**¿Qué hace?** Lista las versiones del plan de estudios de un curso. El sistema resuelve automáticamente cuál es el programa vigente (el más reciente).

**Tablas:** `Programa` — columnas: nombre, objetivos, cargaHorariaTotal, baja, fechaCreacion. También: `Cronograma`

**Contrato obtenerProgramaVigente:** Devuelve el programa con fechaCreacion más reciente y baja = false.

**Speech:** *"CU-15 implementa versionado. Un curso puede tener múltiples programas en el tiempo. El sistema elige el vigente con ORDER BY fechaCreacion DESC LIMIT 1. Los programas anteriores quedan intactos para los alumnos que cursaron bajo ellos."*

---

### CU-16 · Registrar Programa
**Actor:** Docente

**¿Qué hace?** Crea una nueva versión del plan de estudios. Puede partir de cero o copiar el cronograma de un programa anterior.

**Tablas:** `Programa`, `Cronograma`, `Curso`

**La funcionalidad clave — copiar cronograma:** El sistema clona todas las filas de Cronograma del programa anterior cambiando solo idPrograma. El docente edita libremente sin afectar el programa original.

**Speech:** *"CU-16 es el versionado en acción. La copia del cronograma es un INSERT SELECT WHERE idPrograma = :anterior, asignando el nuevo idPrograma. Esto permite trabajar sobre un punto de partida sin pisar el historial de cohortes ya abiertas."*

---

### CU-17 · Modificar Programa
**Actor:** Docente

**Tablas:** `Programa`, `Inscripcion`, `Cohorte`

**Regla:** Si el programa tiene cohortes con inscripciones activas, no se puede modificar.

---

### CU-18 · Dar de Baja Programa
**Actor:** Docente / Administrador

**Tablas:** `Programa`, `Cohorte`

**Regla:** Si el programa tiene o tuvo cohortes (sin importar si están dadas de baja), no se permite la baja. Se preserva el historial académico.

**Speech:** *"CU-18 es el más restrictivo. Bloquea la baja si existió alguna vez una Cohorte WHERE idPrograma = :id, sin importar si esa cohorte ya está dada de baja o no. Protege el historial de alumnos que cursaron."*

---

### CU-19 · Buscar Unidad
**Actor:** Docente / Administrador

**¿Qué hace?** Lista las unidades del cronograma de un programa con contenido expandible (acordeón estilo Moodle).

**Tablas:** `Unidad` — titulo, descripcion, contenido. `Cronograma` — orden, semanasUnidad. Al expandir: `Material`, `TerminoGlosario`, `Pool`, `Autoevaluacion`, `ClaseEnVivo`

**Contrato buscarUnidades(programa):** Retorna unidades ordenadas por Cronograma.orden.

**Speech:** *"CU-19 es el panel de control del docente. La tabla Cronograma es la tabla asociativa entre Programa y Unidad: tiene idPrograma, idUnidad, orden y semanasUnidad. Al expandir el acordeón de una unidad, el sistema ejecuta 5 consultas en cascada: materiales, glosario, pools, autoevaluaciones y clases."*

---

### CU-20 · Agregar Unidad
**Actor:** Docente

**¿Qué hace?** Agrega una unidad al cronograma. Puede crear una nueva O reutilizar una de otro programa del mismo curso.

**Tablas:** `Unidad`, `Cronograma`

**Bloque alternativo del DSS:**
- Nueva: crearUnidad() → INSERT en Unidad + INSERT en Cronograma
- Existente: buscarUnidadesReutilizables() → solo INSERT en Cronograma, la Unidad no se duplica

**Speech:** *"CU-20 demuestra el diseño N:M entre Programa y Unidad. Una unidad 'Introducción a Renta Fija' puede estar en dos programas sin duplicar contenido. Si el docente la reutiliza, el sistema solo inserta una fila en Cronograma apuntando a la Unidad existente."*

---

### CU-21 · Modificar Unidad
**Actor:** Docente

**Tablas:** `Unidad`, `Inscripcion`

**Regla:** Con inscripciones activas en programas que la incluyan → no se puede modificar.

---

### CU-22 · Quitar Unidad
**Actor:** Docente / Administrador

**¿Qué hace?** Quita una unidad del cronograma. Si deja de pertenecer a cualquier cronograma, el sistema la da de baja automáticamente.

**Tablas:** `Cronograma`, `Unidad`

**Postcondición del contrato:** Si COUNT(Cronograma WHERE idUnidad = :id) = 0 → Unidad.baja = true.

**Speech:** *"CU-22 tiene una postcondición inteligente: la baja de la Unidad es automática y condicional. El sistema verifica si la unidad todavía aparece en algún otro cronograma antes de marcarla. Evita dejar unidades huérfanas sin eliminar contenido que puede estar en uso."*

---

### CU-23 · Buscar Cronograma
**Actor:** Docente / Administrador

**¿Qué hace?** Muestra el cronograma completo: unidades en orden con su duración en semanas.

**Tablas:** `Cronograma` — orden, semanasUnidad. `Unidad` — titulo

**Contrato buscarCronograma:** Retorna unidades ordenadas por Cronograma.orden ASC.

---

### CU-24 · Modificar Cronograma
**Actor:** Docente

**¿Qué hace?** Reordena las unidades (drag & drop) y cambia la duración en semanas de cada una.

**Tablas:** `Cronograma`, `Inscripcion`

**Contrato modificarCronograma(unPrograma, ordenUnidades, duraciones):** UPDATE masivo sobre Cronograma con los nuevos valores de orden y duración.

**Speech:** *"CU-24 es la operación más visual del módulo. El docente arrastra unidades en el frontend y el sistema envía un array [{idUnidad, nuevoOrden, nuevaDuracion}] al backend. El contrato hace un UPDATE masivo sobre Cronograma."*

---

### CU-25 · Ver Participantes
**Actor:** Docente / Administrador

**Tablas:** `Usuario`, `Docente`, `Ayudante`, `Alumno`, `Inscripcion`

**Contrato buscarParticipantes:** Retorna lista unificada con rol diferenciado: Titular / Ayudante / Alumno.

---

### CU-26 · Acceder Curso (Alumno — Aula Virtual)
**Actor:** Alumno

**¿Qué hace?** El alumno entra al aula virtual con 4 pestañas: Curso, Cronograma, Participantes y Calificaciones.

**Tablas:** `Inscripcion` (valida acceso vigente), `Cronograma`+`Unidad` (menú lateral), `Progreso` (avance por unidad), `Material` (videos/PDFs), `TerminoGlosario`, `ConsultaForo`+`RespuestaForo`, `Autoevaluacion`, `ClaseEnVivo`

**Regla de avance secuencial:** La siguiente unidad se habilita cuando Progreso.completada = true para la unidad actual.

**Contrato verContenidoUnidad:** Retorna materiales, glosario, foro, autoevaluaciones con intentos del alumno y clases de la unidad seleccionada.

**Speech:** *"CU-26 es el corazón pedagógico. La barra lateral se construye con Cronograma ORDER BY orden, y cada ítem verifica Progreso WHERE idInscripcion = :mine AND idUnidad = :this. Al abrir una unidad, trae en cascada: materiales publicados, glosario, foro con respuestas, autoevaluaciones filtradas por fecha de apertura/cierre, y los intentos ya rendidos por ese alumno."*

---

### CU-26b · Acceder Curso — Modo Edición (Docente / Administrador)

**¿Qué hace?** Habilita edición inline estilo Moodle: aparecen botones de agregar, editar y eliminar sobre cada unidad sin salir de la vista principal.

**Tablas:** Las mismas que CU-26, más formularios de CU-20, CU-28, CU-32, etc.

**Speech:** *"CU-26b es la interfaz dual del aula. Con el toggle Modo Edición activado, la misma vista del aula muestra los controles de gestión inline. Es el patrón de interfaz contextual de Moodle aplicado al sistema."*

---

### CU-27 · Buscar Material
**Actor:** Docente / Administrador

**Tablas:** `Material` — titulo, tipo, oculto, generadoPorIa, baja. `TipoMaterial` — Grabación / Bibliografía / Presentación

**Contrato buscarMateriales:** Incluye materiales ocultos (a diferencia de la vista del alumno que solo muestra oculto = false).

---

### CU-28 · Subir Material
**Actor:** Docente

**¿Qué hace?** Carga manualmente un archivo a una unidad. Siempre se crea oculto (oculto = true).

**Tablas:** `Material`, `TipoMaterial`, `Unidad`

**3 contratos según tipo:**
- subirGrabacion: título + archivo de video
- subirBibliografia: título + archivo + autor
- subirPresentacion: título + archivo de presentación

**Speech:** *"CU-28 siempre crea el material en estado oculto. Esto permite al docente subir contenido antes de que empiece la cohorte sin que los alumnos lo vean. El atributo oculto en Material actúa como un interruptor: false = visible para alumnos con la unidad habilitada."*

---

### CU-29 · Modificar Material
**Actor:** Docente

**Tablas:** `Material` — titulo, rutaArchivo, oculto

**Contrato modificarMaterial:** La postcondición más importante es Material.oculto = estadoPublicacion (el interruptor de visibilidad).

---

### CU-30 · Dar de Baja Material

**Tablas:** `Material` → baja = true

---

### CU-31 · Buscar Término de Glosario

**Tablas:** `TerminoGlosario` — termino, definicion, baja. `Unidad` (FK)

---

### CU-32 · Registrar Término de Glosario

**Tablas:** `TerminoGlosario`

**Regla:** No puede existir ya un término con el mismo nombre en la misma unidad.

---

### CU-33 · Modificar Término de Glosario

**Tablas:** `TerminoGlosario`

**Regla:** Si se cambia el término, no puede coincidir con otro ya existente en la misma unidad.

---

### CU-34 · Dar de Baja Término de Glosario

**Tablas:** `TerminoGlosario` → baja = true

---

### CU-35 · Buscar Consulta de Foro

**Tablas:** `ConsultaForo` — texto, fecha, baja. `RespuestaForo` (anidadas). `Alumno`+`Usuario` (autor)

---

### CU-36 · Registrar Consulta de Foro
**Actor:** Alumno

**Tablas:** `ConsultaForo`, `Inscripcion` (valida acceso vigente), `Progreso` (valida que la unidad esté desbloqueada)

**Postcondición extra:** El sistema notifica al docente titular y ayudantes de la nueva consulta.

---

### CU-37 · Modificar Consulta de Foro
**Actor:** Alumno

**Tablas:** `ConsultaForo`, `Parametro` (el plazo límite de edición es configurable)

**Regla:** Si NOW() - ConsultaForo.fecha > plazoLimite → bloqueado.

---

### CU-38 · Dar de Baja Consulta de Foro
**Actor:** Administrador

**Tablas:** `ConsultaForo` + `RespuestaForo` (ambas baja = true en cascada)

---

### CU-39 · Buscar Respuesta de Foro

**Tablas:** `RespuestaForo` — texto, fecha, baja. `Docente`+`Usuario` (autor)

---

### CU-40 · Registrar Respuesta de Foro
**Actor:** Docente

**Tablas:** `RespuestaForo`, `ConsultaForo` (FK)

**Postcondición:** Notifica al alumno autor de la consulta.

---

### CU-41 · Modificar Respuesta de Foro

**Tablas:** `RespuestaForo` — actualiza texto dentro del plazo límite

---

### CU-42 · Dar de Baja Respuesta de Foro

**Tablas:** `RespuestaForo` → baja = true

---

# 📁 MOD-F-03: Módulo de Inscripciones

---

### CU-43 · Buscar Inscripción
**Actor:** Alumno / Administrador

**¿Qué hace?** Lista inscripciones con filtros. El alumno solo ve las propias; el admin ve todas. Permite descargar el certificado si fue emitido.

**Tablas:** `Inscripcion` — fecha, fechaVencimientoAcceso, baja, numeroCertificado. `Cohorte`→`Programa`→`Curso`, `Alumno`+`Usuario`

**Contrato generarCertificado:** Solo disponible si Inscripcion.numeroCertificado IS NOT NULL.

---

### CU-44 · Inscribir Curso
**Actor:** Alumno

**¿Qué hace?** El alumno elige una cohorte abierta del catálogo y se anota.

**Tablas:** `Cohorte`, `Inscripcion`, `Progreso`, `Pago`

**3 validaciones del contrato registrarInscripcion:**
1. NOW() BETWEEN Cohorte.fechaInicioInscripcion AND Cohorte.fechaFinInscripcion
2. COUNT(Inscripcion WHERE idCohorte) < Cohorte.cupoMaximo
3. No existe ya una inscripción activa del mismo alumno a esa cohorte

**Cálculo de fechaVencimientoAcceso:**
- Sin fechas de dictado: fechaActual + semanasAcceso
- Con fechas de dictado: fechaInicioDictado + semanasAcceso

**Speech:** *"CU-44 integra el catálogo con las inscripciones. La postcondición más interesante es la creación automática del primer Progreso: el sistema inserta una fila con idInscripcion, idUnidad (la primera del cronograma) y completada = false. Esto inicializa el avance del alumno."*

---

### CU-45 · Dar de Baja Inscripción
**Actor:** Alumno / Administrador

**¿Qué hace?** El alumno se da de baja voluntariamente o el admin la cancela. No hay reembolso.

**Tablas:** `Inscripcion` — baja = true, observaciones. `IntentoAutoevaluacion` — también baja = true en cascada

---

### CU-46 · Buscar Pago
**Actor:** Alumno / Administrador

**Tablas:** `Pago` — monto, fecha, estado, numeroComprobante, nombrePagador. `EstadoPago`, `MetodoPago`, `Inscripcion`

**Contrato generarComprobantePago:** Solo disponible si Pago.estado = Acreditado.

---

### CU-47 · Realizar Pago
**Actor:** Alumno (encadenado desde CU-44)

**¿Qué hace?** El alumno paga el arancel con MODO. El sistema aplica descuentos automáticamente y guarda IDs externos para idempotencia.

**Tablas:** `Pago` — se crea en Pendiente; se actualiza al confirmar MODO. `EstadoPago`, `Descuento`, `MetodoPago`, `Inscripcion`

**Campos clave de Pago:**
- paymentRequestId + externalIntentionId: IDs de MODO para idempotencia
- numeroPagador, ultimosDigitosTarjeta: datos del pagador post-acreditación
- numeroComprobante, fechaEmisionComprobante: generados al acreditarse

**Contrato calcularMonto:** Si existe un Descuento activo (vigenciaDesde <= NOW() <= vigenciaHasta AND cantidadUsada < cantidadLimite) aplica precio * (1 - porcentaje/100).

**Contrato realizarPago:** Crea el Pago en Pendiente, envía a MODO, cuando MODO confirma (callback asincrónico) actualiza estado y habilita acceso en Inscripcion.

**Speech:** *"CU-47 es el flujo de pago con pasarela externa. Los campos paymentRequestId y externalIntentionId de la tabla Pago son los identificadores de MODO: si el alumno cierra el navegador y vuelve, el sistema puede consultar a MODO el estado sin volver a cobrar. Al acreditarse, genera el numeroComprobante y lo envía por email."*

---

### CU-48 · Buscar Progreso
**Actor:** Docente / Administrador

**Tablas:** `Progreso` — completada, fechaCompletado. `Inscripcion`, `Cronograma`+`Unidad`

---

### CU-49 · Buscar Descuento

**Tablas:** `Descuento` — nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite, cantidadUsada, cursosRequeridos

---

### CU-50 · Registrar Descuento

**Tablas:** `Descuento` — se crea con cantidadUsada = 0, baja = false

**Validaciones:** porcentaje 1-100, vigenciaHasta > vigenciaDesde, cantidadLimite > 0, cursosRequeridos >= 0

---

### CU-51 · Modificar Descuento

**Tablas:** `Descuento`

**Nota:** El sistema desactiva automáticamente el descuento cuando cantidadUsada >= cantidadLimite o cuando NOW() > vigenciaHasta.

---

### CU-52 · Dar de Baja Descuento

**Tablas:** `Descuento` → baja = true

**Regla bloqueante:** Si Descuento.cantidadUsada > 0 → no se puede dar de baja.

---

# 📁 MOD-F-04: Módulo de Evaluaciones

---

### CU-53 · Buscar Pool

**Tablas:** `Pool` — nombre, descripcion, baja. `Pregunta` (COUNT por pool)

---

### CU-54 · Crear Pool
**Actor:** Docente

**¿Qué hace?** Crea un banco de preguntas con sus opciones de respuesta.

**Tablas:** `Pool`, `Pregunta` — enunciado, tipo (múltiple / V-F). `OpcionRespuesta` — texto, esCorrecta

**Jerarquía:** Pool → Pregunta (1:N) → OpcionRespuesta (1:N)

**DSS — bucle anidado:**
```
LOOP (agregar preguntas):
  agregarPreguntaUnPool(tipo, enunciado) → INSERT en Pregunta
  LOOP (agregar opciones):
    agregarOpcion(unaPregunta, texto, esCorrecta) → INSERT en OpcionRespuesta
```
**Reglas:** mínimo 2 opciones por pregunta, exactamente 1 marcada como correcta.

**Speech:** *"CU-54 construye la jerarquía del motor de evaluaciones. El DSS tiene bucles anidados. El campo esCorrecta en OpcionRespuesta es el que luego en CU-63 el sistema compara contra la respuesta del alumno para corregir automáticamente."*

---

### CU-55 · Modificar Pool

**Tablas:** `Pool`, `Pregunta`, `OpcionRespuesta`, `IntentoAutoevaluacion`

**Regla:** Con intentos registrados → no se puede modificar el contenido (sería inconsistente con lo ya rendido).

---

### CU-56 · Dar de Baja Pool

**Tablas:** `Pool` → baja = true. `PoolAutoevaluacion` (se valida que no esté vinculado a una autoevaluación activa)

---

### CU-57 · Buscar Autoevaluación

**Tablas:** `Autoevaluacion` — nombre, tiempoLimite, cantidadPreguntas, intentosPermitidos, fechaApertura, fechaCierre. `PoolAutoevaluacion`

---

### CU-58 · Crear Autoevaluación
**Actor:** Docente

**¿Qué hace?** Configura un examen vinculando uno o más pools de preguntas.

**Tablas:** `Autoevaluacion`, `PoolAutoevaluacion` (N:M entre Autoevaluacion y Pool), `Pool`

**Validaciones del contrato:**
- tiempoLimite, cantidadPreguntas, intentosPermitidos > 0
- Si se definió fechaCierre: debe ser posterior a fechaApertura
- Los pools en conjunto deben tener >= cantidadPreguntas preguntas

**Speech:** *"CU-58 configura el examen usando la relación N:M entre Autoevaluacion y Pool a través de PoolAutoevaluacion. Combinar múltiples pools hace los exámenes más variados. La validación de suficientes preguntas garantiza que el sistema siempre pueda sortear la cantidad configurada en cada intento."*

---

### CU-59 · Modificar Autoevaluación

**Tablas:** `Autoevaluacion`, `IntentoAutoevaluacion`

**Bloque alternativo del DSS:**
- Sin intentos: se puede editar todo (nombre, tiempo, pools, fechas)
- Con intentos: solo fechaCierre (extender), intentosPermitidos (ampliar) y visibilidad

---

### CU-60 · Dar de Baja Autoevaluación

**Tablas:** `Autoevaluacion` → baja = true. `IntentoAutoevaluacion` (se valida que no haya intentos activos)

---

### CU-61 · Buscar Intento de Autoevaluación

**Tablas:** `IntentoAutoevaluacion` — fecha, nota, resultado. `RespuestaIntento`, `Alumno`+`Usuario`

---

### CU-62 · Ver Calificaciones

**Tablas:** `IntentoAutoevaluacion` — nota, resultado. `Autoevaluacion`, `Inscripcion`

**Contrato buscarCalificacionesAlumno(inscripcion):** Retorna nota y resultado por autoevaluación rendida en el programa de la cohorte del alumno.

---

### CU-63 · Realizar Intento de Autoevaluación
**Actor:** Alumno

**¿Qué hace?** El alumno rinde el examen: el sistema sortea preguntas, el alumno responde con tiempo límite y el sistema corrige automáticamente.

**Tablas:** `Autoevaluacion`, `PoolAutoevaluacion`, `Pregunta`+`OpcionRespuesta`, `IntentoAutoevaluacion`, `RespuestaIntento`, `Progreso`, `Inscripcion`

**DSS — el flujo del examen:**
```
iniciarIntento(unaAutoevaluacion)
  → Sistema sortea N preguntas de los pools en PoolAutoevaluacion
  → Crea IntentoAutoevaluacion (estado: en curso)

LOOP (por cada pregunta):
  siguientePregunta() → buscarOpciones()
  El alumno selecciona → agregarRespuesta() → INSERT en RespuestaIntento

entregarIntento(unIntento)
  → Compara RespuestaIntento.idOpcion con OpcionRespuesta.esCorrecta
  → nota = (aciertos / total) x 10
  → Si todos correctos → aprobado
  → Si aprobado → Progreso.completada = true
  → Si evaluación final y aprobado → genera numeroCertificado en Inscripcion
```

**Speech:** *"CU-63 es el más complejo del módulo. La trazabilidad es total: cada respuesta del alumno queda en RespuestaIntento vinculando el intento con la opción elegida. Al entregar, el sistema compara cada RespuestaIntento.idOpcion con OpcionRespuesta.esCorrecta. Si aprueba, actualiza Progreso.completada = true y desbloquea la siguiente unidad."*

---

### CU-64 · Dar de Baja Intento de Autoevaluación
**Actor:** Administrador

**¿Qué hace?** Anula un intento ante fraude. Revierte el progreso y el certificado si los hubiera.

**Tablas:** `IntentoAutoevaluacion` → baja = true. `Progreso` → completada = false (si el intento aprobó). `Inscripcion` → numeroCertificado = NULL (si se había generado)

**Speech:** *"CU-64 es el mecanismo anti-fraude. Si se detecta suplantación, el admin anula el intento y el sistema revierte en cascada: primero Progreso.completada = false, luego borra el numeroCertificado de la Inscripcion. El alumno queda como si nunca hubiera rendido ese examen."*

---

# 📁 MOD-F-05: Módulo de Clases en Vivo

---

### CU-65 · Buscar Clase en Vivo

**Tablas:** `ClaseEnVivo` — titulo, fechaHora, duracionEstimada, urlRtmp, claveStream. `EstadoClaseEnVivo` — Programada / En Vivo / Finalizada / Cancelada. `Docente`+`Usuario`, `Cohorte`

---

### CU-66 · Programar Clase en Vivo
**Actor:** Docente

**¿Qué hace?** Programa una sesión de streaming para una cohorte activa.

**Tablas:** `ClaseEnVivo` — se crea con idEstado = Programada. `Unidad`, `Cohorte`, `Docente`

**Validaciones del contrato:**
- La fecha debe ser futura
- Debe caer dentro de las fechas de dictado de la cohorte
- No debe superponerse con otra clase del mismo docente

**Speech:** *"CU-66 programa la sesión de streaming. La validación de superposición horaria protege al docente de programar dos clases al mismo tiempo. Más adelante en CU-70, al iniciar la transmisión, se generarán los parámetros RTMP."*

---

### CU-67 · Modificar Clase en Vivo

**Tablas:** `ClaseEnVivo` — actualiza titulo, fechaHora, duracionEstimada

**Regla:** Solo mientras idEstado = Programada.

---

### CU-68 · Cancelar Clase en Vivo
**Actor:** Docente

**Tablas:** `ClaseEnVivo` → baja = true

**Postcondición:** Se notifica a los alumnos inscriptos de la cancelación.

---

### CU-69 · Dar de Baja Clase en Vivo
**Actor:** Administrador

**¿Qué hace?** Modera y elimina lógicamente una clase ya finalizada (ej: contenido inapropiado transmitido).

**Tablas:** `ClaseEnVivo` → baja = true. `Material` — si generó grabación, también baja = true

**Regla:** Solo clases en estado Finalizada.

---

### CU-70 · Iniciar Clase en Vivo
**Actor:** Docente

**¿Qué hace?** El docente inicia la transmisión. El sistema genera las credenciales RTMP para OBS Studio.

**Tablas:** `ClaseEnVivo` — se actualizan urlRtmp, claveStream e idEstado = En Vivo

**DSS:**
1. Docente presiona "Transmitir en Vivo"
2. Sistema genera credenciales (urlRtmp + claveStream) y las guarda en ClaseEnVivo
3. Estado cambia a En Vivo
4. El docente copia la clave a OBS y empieza a transmitir
5. El sistema recibe la señal RTMP y la redistribuye a los alumnos en tiempo real

**Speech:** *"CU-70 muestra la integración con OBS Studio. La tabla ClaseEnVivo almacena urlRtmp y claveStream generados por el sistema. La clave es privada por docente: garantiza que solo él pueda transmitir en esa sala."*

---

### CU-71 · Finalizar Clase en Vivo
**Actor:** Docente

**¿Qué hace?** El docente termina la transmisión. El sistema genera la grabación y la agrega como material de la unidad.

**Tablas:** `ClaseEnVivo` — idEstado = Finalizada. `Material` — se crea fila de tipo Grabación con oculto = false (publicada automáticamente)

**Postcondición:** Los alumnos reciben notificación de que la grabación está disponible.

---

### CU-72 · Ingresar a Clase en Vivo
**Actor:** Alumno

**Tablas:** `ClaseEnVivo` (valida idEstado = En Vivo), `Inscripcion` (valida acceso vigente)

---

# 📁 MOD-F-06: Módulo de Generación de Contenido con IA

---

### CU-73 · Generar Banco de Preguntas (IA — Ollama)
**Actor:** Docente

**¿Qué hace?** El sistema genera automáticamente un pool de preguntas a partir de la bibliografía y glosario de la unidad usando IA local (Ollama).

**Tablas:** `Pool`, `Pregunta`+`OpcionRespuesta` (generadas), `Material` (bibliografía como contexto), `TerminoGlosario` (glosario como contexto)

**DSS:**
1. Sistema recopila Material de tipo Bibliografía y todos los TerminoGlosario de la unidad
2. Los envía al modelo Ollama local junto con el guion del docente
3. Ollama devuelve el banco en formato JSON
4. Sistema valida que cada pregunta tenga >= 2 opciones y 1 correcta
5. INSERT en Pool, Pregunta y OpcionRespuesta

**Speech:** *"CU-73 usa IA local con Ollama. El procesamiento es completamente privado: no sale nada a Internet. El sistema recolecta los materiales bibliográficos y el glosario de la unidad, y los pasa como contexto al modelo para generar preguntas relevantes al contenido del curso."*

---

### CU-74 · Generar Resumen de Unidad (IA)

**Tablas:** `Material` — la bibliografía como input; se crea un nuevo Material de tipo Resumen con oculto = true (para revisión del docente antes de publicar)

---

### CU-75 · Generar Presentación de Unidad (IA)

**Tablas:** `Material` — la bibliografía como input; se crea un nuevo Material de tipo Presentación con oculto = true

---

### CU-76 · Crear Clon (Avatar + Voz IA)
**Actor:** Docente

**¿Qué hace?** El docente registra su avatar visual y voz clonada usando HeyGen. Requisito previo para CU-78.

**Tablas:** `Docente` — se guardan avatarId, voiceId (IDs de HeyGen) y fechaAceptacionTycClon

**DSS:**
1. Docente sube foto facial y graba muestra de voz
2. Sistema muestra Términos y Condiciones de uso del clon
3. Docente los acepta
4. Sistema envía foto + audio a la API de HeyGen
5. HeyGen genera avatar + voz clonada
6. HeyGen devuelve avatar_id y voice_id
7. Sistema los guarda en Docente.avatarId y Docente.voiceId

**Regla:** El docente no puede usar CU-78 sin Docente.fechaAceptacionTycClon IS NOT NULL.

**Speech:** *"CU-76 es el setup del módulo de IA generativa. La tabla Docente solo guarda los identificadores de HeyGen, no el avatar ni la voz en sí. Estos IDs son los que luego en CU-78 se envían a HeyGen para que anime el avatar existente con el nuevo guion."*

---

### CU-77 · Buscar Clase con Clon

**Tablas:** `ClaseClon` — titulo, guion, idEstado. `EstadoClaseClon` — Pendiente / Generada / Error

---

### CU-78 · Generar Clase con Clon de IA (HeyGen)
**Actor:** Docente

**¿Qué hace?** El docente escribe un guion y el sistema genera un video con su avatar y voz clonados.

**Tablas:** `ClaseClon` — se crea con idEstado = Pendiente; se actualiza a Generada al terminar. `Docente` — se leen avatarId y voiceId. `Material` — el video queda como Grabación con generadoPorIa = true y oculto = true. `Unidad`

**DSS:**
1. Docente ingresa titulo + guion
2. Sistema verifica Docente.avatarId IS NOT NULL AND Docente.fechaAceptacionTycClon IS NOT NULL
3. Crea ClaseClon con idEstado = Pendiente
4. Envía {guion, avatarId, voiceId} a la API de HeyGen
5. HeyGen anima el avatar sincronizando boca y gestos con la voz clonada
6. Cuando termina → sistema descarga el video y actualiza idEstado = Generada
7. Crea en Material con generadoPorIa = true y oculto = true

**Speech:** *"CU-78 es la joya tecnológica del sistema. La tabla ClaseClon actúa como registro de estado del proceso asincrónico: el sistema crea la fila con Pendiente, envía el request a HeyGen y vuelve. Cuando HeyGen termina (puede tardar minutos), notifica al sistema que actualiza el estado a Generada. El campo generadoPorIa = true en Material permite que los alumnos sepan que el video fue creado con IA."*

---

### CU-79 · Modificar Clase con Clon

**Tablas:** `ClaseClon` — actualiza titulo y/o guion. Si cambia el guion → idEstado vuelve a Pendiente. `Material` — se reemplaza la grabación si se regeneró

**Regla:** Si solo se cambia el título → no se regenera el video en HeyGen. Si cambia el guion → sí.

---

### CU-80 · Dar de Baja Clase con Clon

**Tablas:** `ClaseClon` → baja = true. `Material` (la grabación asociada también baja = true)

---

# 📁 MOD-NF-01: Módulo de Usuarios y Notificaciones

---

### CU-81 · Registrarse
**Actor:** Alumno (auto-registro)

**¿Qué hace?** El visitante crea su propia cuenta. El sistema envía un email de confirmación.

**Tablas:** `Usuario` — se crea con activo = false hasta confirmar. `Alumno` — perfil del alumno (1:1 con Usuario). `Rol`

**DSS:**
1. registrarse(nombre, apellido, correo, dni, contraseña) → crea Usuario + Alumno
2. Sistema envía email con token de confirmación
3. validarCuenta(token) → Usuario.activo = true

**Speech:** *"CU-81 implementa el patrón de especialización: la tabla Usuario centraliza la autenticación, y Alumno guarda el perfil específico con FK idUsuario en relación 1:1. El doble paso (registro + validación de email) garantiza que la dirección de correo sea real."*

---

### CU-82 · Buscar Usuario

**Tablas:** `Usuario` — nombre, apellido, correo, dni, activo. `Rol` (filtro por rol)

---

### CU-83 · Registrar Usuario (con rol Docente)
**Actor:** Administrador

**¿Qué hace?** El admin crea un usuario con cualquier rol. Si el rol es Docente, se completa el perfil profesional con títulos académicos.

**Tablas:** `Usuario`, `Rol`, `Docente` — biografia, aniosExperiencia, matriculaCnv. `TituloDocente` — titulo, matriculaColegio (1:N con Docente). `Administrador`, `Alumno`

**DSS si el rol es Docente:**
1. buscarRoles() → admin elige Docente
2. registrarUsuario() → INSERT en Usuario
3. registrarDocente(unUsuario, bio, anios, matricula) → INSERT en Docente
4. LOOP: agregarTitulo(unDocente, titulo, matriculaColegio) → INSERT en TituloDocente

**Speech:** *"CU-83 demuestra el patrón de especialización del modelo relacional. La tabla Usuario es la base con las credenciales. Docente, Alumno y Administrador son especializaciones con FK idUsuario y relación 1:1. Si el rol es Docente, el DSS encadena automáticamente con CU-88 para completar el perfil profesional con sus títulos en TituloDocente."*

---

### CU-84 · Modificar Usuario

**Tablas:** `Usuario` — actualiza nombre, apellido, email, teléfono, imagenPerfil

---

### CU-85 · Dar de Baja Usuario

**Tablas:** `Usuario` — activo = false

---

### CU-86 · Ver Perfil

**Tablas:** `Usuario`, `Docente` o `Alumno` o `Administrador` (según rol), `TituloDocente` (si es Docente)

**Contrato verPerfil():** Lee la sesión activa y retorna los datos del usuario y su perfil de rol.

---

### CU-87 · Editar Perfil

**Tablas:** `Usuario` — actualiza nombre, apellido, teléfono, imagenPerfil

---

### CU-88 · Registrar Docente
**Actor:** Administrador

**¿Qué hace?** Completa el perfil profesional de un usuario con rol Docente. Normalmente encadenado desde CU-83.

**Tablas:** `Docente` — biografia, aniosExperiencia, matriculaCnv, habilitado. `TituloDocente` — titulo, matriculaColegio

**DSS — bucle de títulos:**
```
registrarDocente(unUsuario, bio, anios, matricula) → INSERT en Docente
LOOP (mientras haya títulos para agregar):
  agregarTitulo(unDocente, titulo, matriculaColegio) → INSERT en TituloDocente
```

---

### CU-89 · Modificar Docente

**Tablas:** `Docente` — actualiza biografia, aniosExperiencia, matriculaCnv, habilitado. `TituloDocente` — se insertan, actualizan o eliminan filas de títulos

**DSS — bloque alternativo por título:**
- Agregar: INSERT en TituloDocente
- Editar: UPDATE en TituloDocente
- Eliminar: DELETE en TituloDocente (este es el único caso donde se hace DELETE en el sistema)

---

### CU-90 · Iniciar Sesión

**Tablas:** `Usuario` (valida correo + contraseña o token Google), `Sesion` — fechaHora, ip, dispositivo

**DSS — bloque alternativo:**
- Rama 1: iniciarSesion(correo, contraseña)
- Rama 2: iniciarSesionConGoogle(tokenGoogle)

---

### CU-91 · Cerrar Sesión

**Tablas:** `Sesion` — se registra fechaFin = NOW()

---

### CU-92 · Recuperar Contraseña

**Tablas:** `Usuario` (se actualiza la contraseña), `TokenRecuperacion` — token temporal con fecha de expiración

**DSS:**
1. solicitarRecuperacionContrasena(correo) → genera token + envía email
2. restablecerContrasena(token, nuevaContrasena) → valida token y actualiza Usuario.contraseña

---

### CU-93 · Buscar Sesión

**Tablas:** `Sesion` — fechaHora, ip, dispositivo, idUsuario

**¿Qué hace?** Muestra el historial de sesiones iniciadas por el propio usuario.

---

### CU-94 · Eliminar Sesión

**Tablas:** `Sesion` — se invalida la sesión seleccionada

**¿Qué hace?** Cierra una sesión remota activa (ej: el usuario ve que alguien inició sesión desde un dispositivo desconocido y la revoca).

---

# 📁 MOD-NF-02: Módulo de Auditoría

---

### CU-95 · Consultar Auditoría
**Actor:** Administrador

**¿Qué hace?** Muestra el historial completo e inmutable de todas las operaciones sensibles del sistema.

**Tablas:**
- `Auditoria` — entidadAfectada, idAfectado, ipUsuario, fechaHora
- `DetalleAuditoria` — campo, valorAnterior, valorNuevo
- `TipoAccionAuditoria` — Alta / Modificación / Baja Lógica / Login
- `Usuario` — el responsable de la acción

**Relaciones:** TipoAccionAuditoria → Auditoria (1:N). Usuario → Auditoria (1:N). Auditoria → DetalleAuditoria (1:N)

**Ejemplos de registros:**
- Dar de baja un curso → 1 fila en Auditoria (Baja Lógica) + 1 fila en DetalleAuditoria (campo: "baja", valorAnterior: "false", valorNuevo: "true")
- Cambiar precio → 1 fila en Auditoria (Modificación) + 1 fila en DetalleAuditoria (campo: "precio", valorAnterior: "100000", valorNuevo: "150000")
- Login exitoso → 1 fila en Auditoria (Login), sin DetalleAuditoria

**Contrato buscarRegistrosAuditoria(usuario, tipoAccion, entidad, rangoFechas):** Filtra la tabla Auditoria con los criterios indicados.

**Speech:** *"CU-95 implementa una capa de auditoría con el patrón Append-Only. Las tablas Auditoria y DetalleAuditoria solo permiten INSERT, nunca UPDATE ni DELETE. Por cada operación sensible el sistema registra en dos niveles: la cabecera (quién, cuándo, desde qué IP, qué entidad y qué registro) y el detalle (qué campo cambió, de qué valor a qué valor). Esto cumple los requisitos de trazabilidad para una plataforma regulada por la CNV."*

---

# 📁 MOD-NF-03: Módulo de Reportes y Estadísticas

---

### CU-96 · Generar Informe de Alumnos de un Curso

**Tablas:** `Inscripcion`, `Alumno`+`Usuario`, `Cohorte`+`Programa`+`Curso`, `Progreso`

**DSS:**
1. buscarCursos() → admin elige un curso
2. seleccionarCurso()
3. generarInformeAlumnos(unCurso, rangoFechas) → retorna el informe

---

### CU-97 · Generar Informe de Ingresos de un Curso

**Tablas:** `Pago` — monto, fecha, estado. `Descuento`, `Inscripcion`+`Curso`

**DSS:**
1. buscarCursos() → admin elige un curso
2. seleccionarCurso()
3. generarInformeIngresos(unCurso, rangoFechas) → retorna el informe financiero

---

### CU-98 · Consultar Estadísticas

**¿Qué hace?** Muestra el dashboard con métricas globales del sistema.

**Tablas:** `Inscripcion`, `Curso`, `Cohorte`, `Pago`, `IntentoAutoevaluacion`

**Contrato consultarEstadisticas():** Ejecuta consultas agregadas (COUNT, SUM, AVG) sobre las tablas clave y devuelve los indicadores en un solo objeto.

**Speech:** *"CU-98 es el panel de control ejecutivo. El contrato ejecuta múltiples agregaciones en paralelo: cursos activos (COUNT sobre Curso WHERE baja = false), inscripciones vigentes (COUNT sobre Inscripcion WHERE baja = false), ingresos del mes (SUM Pago.monto WHERE fechaAprobacion >= inicio_mes), y tasa de aprobación (AVG de IntentoAutoevaluacion.resultado)."*

---

# 📁 MOD-NF-04: Módulo de Configuración

---

### CU-99 · Configurar Parámetros
**Actor:** Administrador

**¿Qué hace?** Gestiona los parámetros configurables del sistema (plazo límite de edición del foro, duración de grabaciones, porcentaje mínimo de aprobación, etc.).

**Tablas:** `Parametro` — nombre, valor, descripcion, ultimaModificacion

**DSS:**
1. buscarParametros() → lista todos los parámetros
2. seleccionarParametro(idParametro, parametros)
3. modificarParametro(unParametro, valorNuevo) → UPDATE en Parametro.valor

**Speech:** *"CU-99 es la tabla de configuración global. En lugar de hardcodear valores como '15 minutos para editar el foro', estos valores se guardan en la tabla Parametro y el admin puede cambiarlos desde la interfaz sin tocar código. El sistema los lee en tiempo de ejecución para aplicarlos en los contratos correspondientes."*

---

# Resumen: Patrones de Diseño que Aparecen en Múltiples CU

| Patrón | Dónde aparece | Descripción |
|---|---|---|
| Borrado Lógico | CU-05, CU-10, CU-14, CU-18, CU-22, CU-30, CU-34, CU-38, CU-42, CU-45, CU-52, CU-56, CU-60, CU-64, CU-68, CU-69, CU-80 | Se pone baja = true en lugar de DELETE |
| Validación Bloqueante | CU-05, CU-10, CU-14, CU-18, CU-52, CU-56, CU-60 | El botón de confirmar se deshabilita si hay dependencias activas |
| Especialización 1:1 | CU-81, CU-83, CU-88 | Usuario base con Alumno, Docente, Administrador como extensiones |
| Tabla Asociativa N:M | CU-03, CU-20, CU-58 | Ayudante, Cronograma, PoolAutoevaluacion como tablas puente |
| Bloque Alternativo DSS | CU-04, CU-20, CU-59, CU-90 | El sistema elige un camino según el estado actual de los datos |
| Bucle Anidado DSS | CU-54, CU-63, CU-88 | Bucles dentro de bucles para crear jerarquías de datos |
| Append-Only | CU-95 | La tabla de Auditoría solo permite INSERT |
| Calculo Automatico | CU-44, CU-47 | Fecha de vencimiento de acceso, descuentos en tiempo real |
| Idempotencia | CU-47 | IDs de MODO guardados para evitar doble cobro |
| Estado del Proceso | CU-78, CU-73 | ClaseClon.idEstado como máquina de estados del proceso asincrónico |

---

*Documento preparado para la defensa del Trabajo Final — IDÓNEOS ONLINE — Küster & Martínez — 2026*
