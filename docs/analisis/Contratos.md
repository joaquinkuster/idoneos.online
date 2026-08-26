# Contratos

## MOD-F-01: Módulo del Catálogo de Cursos

### buscarCursos(nombre, categoria, nivel, docentes, modalidades)
- **Responsabilidades**: Retorna las instancias de unCurso cuyos datos coincidan con los criterios especificados. Si el actor es Docente, restringe el resultado a sus cursos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-01: Buscar curso.
- **Notas**: Todos los criterios son opcionales; si no se especifica ninguno, se retornan todos los cursos visibles para el actor.
- **Excepciones**: –
- **Salida**: cursos.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. Debe existir al menos un curso activo.
- **Postcondiciones**: –

---

### seleccionarCurso(idCurso, cursos)
- **Responsabilidades**: Retorna la instancia de unCurso cuyo identificador coincide con idCurso dentro de cursos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-01: Buscar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unCurso.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarInscripcionesAlumno(nombreCurso, estadoInscripcion)
- **Responsabilidades**: Retorna las instancias de unaInscripcion del alumno cuyo curso y estado coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-02: Ver mis cursos.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: inscripciones.
- **Precondiciones**: El alumno debe tener al menos una inscripción.
- **Postcondiciones**: –

---

### seleccionarInscripcion(idInscripcion, inscripciones)
- **Responsabilidades**: Retorna la instancia de unaInscripcion cuyo identificador coincide con idInscripcion dentro de inscripciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-02: Ver mis cursos. CU-43: Buscar inscripción.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaInscripcion.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarNiveles()
- **Responsabilidades**: Retorna todas las instancias de unNivel registradas en el sistema.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: niveles.
- **Precondiciones**: Debe existir al menos un nivel registrado.
- **Postcondiciones**: –

---

### seleccionarNivel(idNivel, niveles)
- **Responsabilidades**: Retorna la instancia de unNivel cuyo identificador coincide con idNivel dentro de niveles.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unNivel.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarModalidades()
- **Responsabilidades**: Retorna todas las instancias de unaModalidad registradas en el sistema.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: modalidades.
- **Precondiciones**: Debe existir al menos una modalidad registrada.
- **Postcondiciones**: –

---

### seleccionarModalidades(idsModalidades, modalidades)
- **Responsabilidades**: Retorna las instancias de unaModalidad cuyos identificadores coinciden con idsModalidades dentro de modalidades.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: modalidades.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarDocentes()
- **Responsabilidades**: Retorna todas las instancias de unDocente habilitadas en el sistema.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: docentes.
- **Precondiciones**: Debe existir al menos un docente activo y habilitado.
- **Postcondiciones**: –

---

### seleccionarDocente(idDocente, docentes)
- **Responsabilidades**: Retorna la instancia de unDocente cuyo identificador coincide con idDocente dentro de docentes.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unDocenteTitular.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarAyudantes(idsDocentes, docentes)
- **Responsabilidades**: Retorna las instancias de unDocente cuyos identificadores coinciden con idsDocentes dentro de docentes.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: Selección múltiple; idsDocentes puede ser una lista vacía (curso sin ayudantes).
- **Excepciones**: –
- **Salida**: ayudantes.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarCurso(nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)
- **Responsabilidades**: Registra un nuevo curso con los datos especificados, sin cohortes abiertas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-03: Registrar curso.
- **Notas**: –
- **Excepciones**:
  - No se completó alguno de los campos obligatorios (nombre, descripción, precio, categoría, nivel, al menos una modalidad, docente titular).
  - unaCategoria no se encuentra activa.
  - Uno o más docentes no se encuentra activo o habilitado.
  - unDocenteTitular no puede ser también ayudante del curso.
  - El precio es menor a cero.
- **Salida**: unCurso.
- **Precondiciones**: Debe existir al menos una categoría activa. Debe existir al menos un docente activo y habilitado.
- **Postcondiciones**:
  - Se creó una instancia de unCurso.
  - Se asignó a unCurso.Nombre el valor nombre.
  - Se asignó a unCurso.Descripcion el valor descripcion.
  - Se asignó a unCurso.Precio el valor precio.
  - Se asignó a unCurso.Imagen el valor imagen.
  - Se asignó unCurso.EmiteCertificado el valor emiteCertificado.
  - Se asignó a unCurso.FechaCreacion la fecha actual.
  - Se asignó unCurso.Baja el valor falso.
  - Se asoció unaCategoria a unCurso.
  - Se asoció unDocenteTitular a unCurso.
  - Se asoció ayudantes a unCurso.
  - Se asoció unNivel a unCurso.
  - Se asoció modalidades a unCurso.

---

### modificarCurso(unCurso, nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)
- **Responsabilidades**: Actualiza los datos de unCurso con los valores especificados, dado que no existen inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**:
  - unCurso no se encuentra activo.
  - unCurso tiene inscripciones activas asociadas.
  - Algún campo obligatorio queda vacío.
  - unaCategoria no se encuentra activa.
  - Uno o más docentes no se encuentra activo o habilitado.
  - unDocenteTitular no puede ser también ayudante de unCurso.
  - El precio es menor a cero.
- **Salida**: –
- **Precondiciones**: unCurso debe estar activo. unCurso no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó a unCurso.Nombre el valor nombre.
  - Se asignó a unCurso.Descripcion el valor descripcion.
  - Se asignó a unCurso.Precio el valor precio.
  - Se asignó a unCurso.Imagen el valor imagen.
  - Se asignó unCurso.EmiteCertificado el valor emiteCertificado.
  - Se asignó a unCurso.UltimaModificacion la fecha actual.
  - Se asoció unaCategoria a unCurso.
  - Se asoció unDocenteTitular a unCurso.
  - Se asoció ayudantes a unCurso.
  - Se asoció unNivel a unCurso.
  - Se asoció modalidades a unCurso.

---

### modificarCursoPrecioImagen(unCurso, precio, imagen)
- **Responsabilidades**: Actualiza únicamente el precio y la imagen de unCurso, dado que existen inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**:
  - unCurso no se encuentra activo.
  - El precio es menor a cero.
- **Salida**: –
- **Precondiciones**: unCurso debe estar activo.
- **Postcondiciones**:
  - Se asignó a unCurso.Precio el valor precio.
  - Se asignó a unCurso.Imagen el valor imagen.
  - Se asignó unCurso.UltimaModificacion el valor de la fecha actual.

---

### darDeBajaCurso(unCurso)
- **Responsabilidades**: Da de baja a unCurso, siempre que no existan programas activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-05: Dar de baja curso.
- **Notas**: –
- **Excepciones**:
  - unCurso no se encuentra activo.
  - unCurso tiene programas activos asociados.
- **Salida**: –
- **Precondiciones**: unCurso debe estar activo.
- **Postcondiciones**: Se asignó a unCurso.Baja el valor verdadero.

---

### buscarCursosAbiertos(nombre, categoria, nivel, docente, modalidad)
- **Responsabilidades**: Retorna las instancias de unCurso con alguna cohorte con inscripción abierta que coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-06: Explorar catálogo de cursos.
- **Notas**: No requiere sesión iniciada. Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: cursos.
- **Precondiciones**: Debe existir al menos un curso con alguna cohorte con inscripción abierta.
- **Postcondiciones**: –

---

### buscarCategorias(nombre)
- **Responsabilidades**: Retorna las instancias de unaCategoria cuyo nombre coincida con el criterio especificado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-07: Buscar categoría.
- **Notas**: El criterio es opcional.
- **Excepciones**: –
- **Salida**: categorias.
- **Precondiciones**: Debe existir al menos una categoría activa.
- **Postcondiciones**: –

---

### seleccionarCategoria(idCategoria, categorias)
- **Responsabilidades**: Retorna la instancia de unaCategoria cuyo identificador coincide con idCategoria dentro de categorias.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-07: Buscar categoría. CU-03: Registrar curso. CU-04: Modificar curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaCategoria.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarCategoria(nombre, descripcion)
- **Responsabilidades**: Registra una nueva categoría en estado activo con los datos especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-08: Registrar categoría.
- **Notas**: descripcion es un dato opcional.
- **Excepciones**:
  - El nombre no fue completado.
  - Ya existe una categoría activa con el mismo nombre.
- **Salida**: unaCategoria.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unaCategoria.
  - Se asignó unaCategoria.Nombre el valor nombre.
  - Se asignó unaCategoria.Descripcion el valor descripcion.
  - Se asignó unaCategoria.Baja el valor falso.
  - Se asignó unaCategoria.FechaCreacion la fecha actual.

---

### modificarCategoria(unaCategoria, nombre, descripcion)
- **Responsabilidades**: Actualiza el nombre y la descripción de unaCategoria, siempre que no existan inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-09: Modificar categoría.
- **Notas**: –
- **Excepciones**:
  - unaCategoria no se encuentra activa.
  - unaCategoria tiene inscripciones activas asociadas.
  - El nombre queda vacío.
  - El nombre coincide con el de otra categoría activa.
- **Salida**: –
- **Precondiciones**: unaCategoria debe estar activa. unaCategoria no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó a unaCategoria.Nombre el valor nombre.
  - Se asignó a unaCategoria.Descripcion el valor descripcion.
  - Se asignó unaCategoria.UltimaModificacion la fecha actual.

---

### darDeBajaCategoria(unaCategoria)
- **Responsabilidades**: Da de baja a unaCategoria, siempre que no existan cursos activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-10: Dar de baja categoría.
- **Notas**: –
- **Excepciones**:
  - unaCategoria no se encuentra activa.
  - unaCategoria tiene cursos activos asociados.
- **Salida**: –
- **Precondiciones**: unaCategoria debe estar activa.
- **Postcondiciones**: Se asignó a unaCategoria.Baja el valor verdadero.

---

### buscarCohortes(programa, estado, fechaInicioInscripcion, fechaFinInscripcion)
- **Responsabilidades**: Retorna las instancias de unaCohorte de un programa que coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-11: Buscar cohorte.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: cohortes.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. Debe existir al menos una cohorte activa.
- **Postcondiciones**: –

---

### seleccionarCohorte(idCohorte, cohortes)
- **Responsabilidades**: Retorna la instancia de unaCohorte cuyo identificador coincide con idCohorte dentro de cohortes.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-11: Buscar cohorte.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaCohorte.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarCohorte(unPrograma, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)
- **Responsabilidades**: Registra una nueva cohorte asociada a unPrograma, con los datos especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-12: Registrar cohorte.
- **Notas**: –
- **Excepciones**:
  - unPrograma no se encuentra activo.
  - unPrograma no tiene el mínimo de unidades establecido con material publicado en su cronograma.
  - Algún campo obligatorio no fue completado.
  - Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado.
  - La fechaFinInscripcion no es posterior a fechaInicioInscripcion, o fechaFinDictado no es posterior a fechaInicioDictado.
  - La fechaInicioDictado es anterior a fechaFinInscripcion.
  - El cupoMaximo no es un entero mayor a cero.
  - Las semanasAcceso es menor a la duración total del cronograma de unPrograma.
- **Salida**: unaCohorte.
- **Precondiciones**: unPrograma debe estar activo. unPrograma debe tener el mínimo de unidades establecido con material publicado en su cronograma.
- **Postcondiciones**:
  - Se creó una instancia de unaCohorte.
  - Se asignó a unaCohorte.FechaInicioInscripcion el valor fechaInicioInscripcion.
  - Se asignó a unaCohorte.FechaFinInscripcion el valor fechaFinInscripcion.
  - Se asignó a unaCohorte.CupoMaximo el valor cupoMaximo.
  - Se asignó a unaCohorte.SemanasAcceso el valor semanasAcceso.
  - Se asignó a unaCohorte.FechaInicioDictado el valor fechaInicioDictado.
  - Se asignó a unaCohorte.FechaFinDictado el valor fechaFinDictado.
  - Se asignó unaCohorte.Baja el valor falso.
  - Se asignó a unaCohorte.FechaCreacion la fecha actual.
  - Se asoció unPrograma a unaCohorte.

---

### modificarCohorte(unaCohorte, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)
- **Responsabilidades**: Actualiza los datos especificados de unaCohorte, siempre que no existan inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-13: Modificar cohorte.
- **Notas**: No es posible modificar una cohorte finalizada.
- **Excepciones**:
  - unaCohorte no se encuentra activa.
  - unaCohorte no pertenece al programa vigente.
  - unaCohorte tiene inscripciones activas asociadas.
  - Algún campo obligatorio queda vacío.
  - Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado.
  - La fechaFinInscripcion no es posterior a fechaInicioInscripcion, o fechaFinDictado no es posterior a fechaInicioDictado.
  - La fechaInicioDictado es anterior a fechaFinInscripcion.
  - El cupoMaximo no es un entero mayor a cero.
  - Las semanasAcceso es menor a la duración total del cronograma del programa.
- **Salida**: –
- **Precondiciones**: unaCohorte debe estar activa. unaCohorte debe pertenecer al programa vigente. unaCohorte no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó a unaCohorte.FechaInicioInscripcion el valor fechaInicioInscripcion.
  - Se asignó a unaCohorte.FechaFinInscripcion el valor fechaFinInscripcion.
  - Se asignó a unaCohorte.CupoMaximo el valor cupoMaximo.
  - Se asignó a unaCohorte.SemanasAcceso el valor semanasAcceso.
  - Se asignó a unaCohorte.FechaInicioDictado el valor fechaInicioDictado.
  - Se asignó a unaCohorte.FechaFinDictado el valor fechaFinDictado.
  - Se asignó unaCohorte.UltimaModificacion el valor de la fecha actual.

---

### darDeBajaCohorte(unaCohorte)
- **Responsabilidades**: Da de baja a unaCohorte, siempre que no existan inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-14: Dar de baja cohorte.
- **Notas**: –
- **Excepciones**:
  - unaCohorte no se encuentra activa.
  - unaCohorte tiene inscripciones activas asociadas.
- **Salida**: –
- **Precondiciones**: unaCohorte debe estar activa.
- **Postcondiciones**: Se asignó a unaCohorte.Baja el valor verdadero.

---

## MOD-F-02: Módulo de Gestión Académica

### obtenerProgramaVigente(curso)
- **Responsabilidades**: Retorna el programa vigente de un curso, o el último programa al que el actor lo haya cambiado previamente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-15: Buscar programa.
- **Notas**: Se invoca por defecto al iniciar CU-15 y en toda operación que requiera resolver el programa de trabajo de un curso.
- **Excepciones**: –
- **Salida**: unPrograma.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. El curso debe estar activo. El curso debe tener al menos un programa activo.
- **Postcondiciones**: –

---

### buscarProgramas(curso, nombre, estado)
- **Responsabilidades**: Retorna las instancias de unPrograma de un curso cuyos datos coincidan con los criterios especificados, indicando cuál se encuentra vigente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-15: Buscar programa. CU-16: Registrar programa.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: programas.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. El curso debe estar activo. El curso debe tener al menos un programa activo.
- **Postcondiciones**: –

---

### seleccionarPrograma(idPrograma, programas)
- **Responsabilidades**: Retorna la instancia de unPrograma cuyo identificador coincide con idPrograma dentro de programas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-15: Buscar programa. CU-16: Registrar programa.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unPrograma.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarPrograma(unCurso, nombre, descripcion, objetivos, cargaHorariaTotal, bibliografia, unProgramaAnterior)
- **Responsabilidades**: Registra un nuevo programa para unCurso, que pasa a ser el vigente por ser el de fecha de creación más reciente. Si se indicó unProgramaAnterior, copia su cronograma al nuevo programa como punto de partida editable.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-16: Registrar programa.
- **Notas**: unProgramaAnterior es opcional; descripcion y cargaHorariaTotal son opcionales.
- **Excepciones**:
  - unCurso no se encuentra activo.
  - El docente no participa en él como titular o ayudante.
  - No se completó alguno de los campos obligatorios (nombre, objetivos o bibliografía).
- **Salida**: unPrograma.
- **Precondiciones**: El docente participa en él como titular o ayudante. unCurso debe estar activo.
- **Postcondiciones**:
  - Se creó una instancia de unPrograma.
  - Se asignó a unPrograma.Nombre el valor nombre.
  - Se asignó a unPrograma.Descripcion el valor descripcion.
  - Se asignó a unPrograma.Objetivos el valor objetivos.
  - Se asignó a unPrograma.CargaHorariaTotal el valor cargaHorariaTotal.
  - Se asignó a unPrograma.Bibliografia el valor bibliografia.
  - Se asignó a unPrograma.FechaCreacion la fecha actual.
  - Se asignó a unPrograma.Baja el valor falso.
  - Se asoció unPrograma a unCurso.
  - Si se indicó unProgramaAnterior, se copió su cronograma (unidades, orden y duración) a unPrograma.

---

### modificarPrograma(unPrograma, nombre, descripcion, objetivos, cargaHorariaTotal, bibliografia)
- **Responsabilidades**: Actualiza los datos especificados de unPrograma, siempre que no existan inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-17: Modificar programa.
- **Notas**: –
- **Excepciones**:
  - unPrograma no se encuentra activo.
  - unPrograma tiene inscripciones activas asociadas.
  - El docente no participa en el curso como titular o ayudante.
  - Algún campo obligatorio quedó vacío.
- **Salida**: –
- **Precondiciones**: El docente participa en él como titular o ayudante. unPrograma debe estar activo. unPrograma no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó a unPrograma.Nombre el valor nombre.
  - Se asignó a unPrograma.Descripcion el valor descripcion.
  - Se asignó a unPrograma.Objetivos el valor objetivos.
  - Se asignó a unPrograma.CargaHorariaTotal el valor cargaHorariaTotal.
  - Se asignó a unPrograma.Bibliografia el valor bibliografia.
  - Se asignó a unPrograma.UltimaModificacion la fecha actual.

---

### darDeBajaPrograma(unPrograma)
- **Responsabilidades**: Da de baja a unPrograma, siempre que no existan cohortes activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-18: Dar de baja programa.
- **Notas**: –
- **Excepciones**:
  - unPrograma no se encuentra activo.
  - unPrograma tiene cohortes activas asociadas.
  - El docente no participa en el curso como titular o ayudante.
- **Salida**: –
- **Precondiciones**: Si el actor es Docente, participa en él como titular o ayudante. unPrograma debe estar activo. unPrograma no debe tener cohortes activas asociadas.
- **Postcondiciones**: Se asignó a unPrograma.Baja el valor verdadero.

---

### buscarUnidades(programa)
- **Responsabilidades**: Retorna las instancias de unaUnidad incluidas en el cronograma del programa.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-19: Buscar unidad. CU-26: Acceder curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unidades.
- **Precondiciones**: Si el actor es Docente, participa en él como titular o ayudante. El programa debe estar activo. El programa debe tener al menos una unidad incluida en su cronograma.
- **Postcondiciones**: –

---

### seleccionarUnidad(idUnidad, unidades)
- **Responsabilidades**: Retorna la instancia de unaUnidad cuyo identificador coincide con idUnidad dentro de unidades.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-19: Buscar unidad. CU-20: Agregar unidad. CU-26: Acceder curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaUnidad.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### crearUnidad(unPrograma, titulo, descripcion, contenido)
- **Responsabilidades**: Registra una unidad y la incorpora al final del cronograma de unPrograma, con una semana de duración por defecto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-20: Agregar unidad.
- **Notas**: descripcion es opcional.
- **Excepciones**:
  - unPrograma no se encuentra activo.
  - El dicente no participa en él como titular o ayudante.
  - No se completó alguno de los campos obligatorios (título o contenido).
- **Salida**: unaUnidad.
- **Precondiciones**: El docente participa en él como titular o ayudante. unPrograma debe estar activo.
- **Postcondiciones**:
  - Se creó una instancia de unaUnidad.
  - Se asignó a unaUnidad.Titulo el valor titulo.
  - Se asignó a unaUnidad.Descripcion el valor descripcion.
  - Se asignó a unaUnidad.Contenido el valor contenido.
  - Se asignó a unaUnidad.FechaCreacion la fecha actual.
  - Se asignó a unaUnidad.Baja el valor falso.
  - Se incorporó la unidad al cronograma de unPrograma en la última posición, con una semana de duración.

---

### buscarUnidadesReutilizables(unPrograma)
- **Responsabilidades**: Retorna las instancias de unaUnidad de otros programas del mismo curso de unPrograma que todavía no formen parte del cronograma de unPrograma.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-20: Agregar unidad.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unidadesReutilizables.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### agregarUnidadExistente(unPrograma, unaUnidad)
- **Responsabilidades**: Incorpora unaUnidad, ya existente en otro programa del mismo curso, al final del cronograma de unPrograma, con una semana de duración por defecto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-20: Agregar unidad.
- **Notas**: –
- **Excepciones**:
  - unPrograma no se encuentra activo.
  - unaUnidad no se encuentra activa.
  - El docente no participa en él como titular o ayudante.
- **Salida**: –
- **Precondiciones**: El docente participa en él como titular o ayudante. unPrograma debe estar activo. unaUnidad debe estar activa.
- **Postcondiciones**: Se incorporó unaUnidad al cronograma de unPrograma en la última posición, con una semana de duración.

---

### modificarUnidad(unaUnidad, titulo, descripcion, contenido)
- **Responsabilidades**: Actualiza los datos especificados de unaUnidad, siempre que no existan inscripciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-21: Modificar unidad.
- **Notas**: –
- **Excepciones**:
  - unaUnidad no se encuentra activa.
  - unaUnidad tiene inscripciones activas asociadas.
  - El docente no participa en el curso como titular o ayudante.
  - Algún campo obligatorio queda vacío.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe estar activa. unaUnidad no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó a unaUnidad.Titulo el valor titulo.
  - Se asignó a unaUnidad.Descripcion el valor descripcion.
  - Se asignó a unaUnidad.Contenido el valor contenido.
  - Se asignó a unaUnidad.UltimaModificacion la fecha actual.

---

### quitarUnidad(unPrograma, unaUnidad)
- **Responsabilidades**: Quita a unaUnidad del cronograma de unPrograma. Si al quitarla deja de formar parte del cronograma de cualquier otro programa, la da de baja automáticamente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-22: Quitar unidad.
- **Notas**: –
- **Excepciones**:
  - Si el actor es Docente y no participa en el curso como titular o ayudante.
  - unPrograma no se encuentra activo.
  - unaUnidad no se encuentra activa.
  - unaUnidad no pertenece al cronograma de unPrograma.
  - unPrograma tiene inscripciones activas asociadas.
- **Salida**: –
- **Precondiciones**: Si el actor es Docente, participa como titular o ayudante en el curso de ese programa. unPrograma debe estar activo. unaUnidad debe estar activa. unaUnidad debe pertenecer al cronograma de unPrograma. unPrograma no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se quitó unaUnidad del cronograma de unPrograma.
  - Si ya no pertenece a ningún otro cronograma, se asignó a unaUnidad.Baja el valor verdadero.

---

### buscarCronograma(programa)
- **Responsabilidades**: Retorna las unidades del cronograma del programa, ordenadas por su número de orden, con su duración en semanas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-23: Buscar cronograma.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unCronograma.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. El programa debe estar activo.
- **Postcondiciones**: –

---

### modificarCronograma(unPrograma, ordenUnidades, duraciones)
- **Responsabilidades**: Reordena las unidades del cronograma de unPrograma según ordenUnidades y actualiza la duración en semanas de cada una según duraciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-24: Modificar cronograma.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unPrograma no se encuentra activo.
  - unPrograma tiene inscripciones activas asociadas.
  - Alguna duración no es un número entero de semanas mayor a cero.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unPrograma debe estar activo. unPrograma no debe tener inscripciones activas asociadas.
- **Postcondiciones**:
  - Se asignó en las unidades del cronograma el orden indicado por ordenUnidades, con la duración indicada por duraciones.
  - Se asignó a unPrograma.UltimaModificacion la fecha actual.

---

### buscarParticipantes(curso, nombreCompleto, rol)
- **Responsabilidades**: Retorna el equipo docente y los alumnos de todas las cohortes de un curso cuyo nombre y rol coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-25: Ver participantes.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: Ningún participante coincide con los criterios.
- **Salida**: participantes.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante.
- **Postcondiciones**: –

---

### verContenidoUnidad(unaUnidad)
- **Responsabilidades**: Retorna el contenido publicado de unaUnidad: su material, sus términos de glosario, el acceso al foro, sus autoevaluaciones (con los intentos que el alumno ya haya registrado sobre cada una) y sus clases en vivo programadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso.
- **Notas**: –
- **Excepciones**:
  - El alumno no tiene una inscripción vigente al curso.
  - unaUnidad todavía no está habilitada para el alumno según su avance secuencial.
- **Salida**: materiales, glosario, foro, autoevaluaciones, clases.
- **Precondiciones**: El alumno debe tener una inscripción vigente al curso. Si la cohorte del alumno posee fechas de dictado, la fecha actual no debe ser anterior a la fecha de inicio de dictado.
- **Postcondiciones**: –

---

### buscarIntentos(autoevaluacion, alumno, rangoFechas, resultado)
- **Responsabilidades**: Retorna las instancias de unIntento de autoevaluacion que coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso. CU-61: Buscar intento de autoevaluación.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: intentos.
- **Precondiciones**: Debe existir al menos un intento activo para la autoevaluación.
- **Postcondiciones**: –

---

### seleccionarClaseEnVivo(idClase, clases)
- **Responsabilidades**: Retorna la instancia de unaClase cuyo identificador coincide con idClase dentro de clases.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso. CU-65: Buscar clase en vivo.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaClase.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarParticipantesPorCohorte(cohorte, nombreCompleto, rol)
- **Responsabilidades**: Retorna el equipo docente completo del curso y los alumnos de la propia cohorte del alumno, cuyo nombre y rol coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: participantes.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarCalificacionesAlumno(inscripcion)
- **Responsabilidades**: Retorna, para cada autoevaluación rendida por el alumno en el programa de su cohorte según inscripcion, la nota y el resultado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso.
- **Notas**: –
- **Excepciones**: El alumno no rindió ninguna autoevaluación del curso.
- **Salida**: calificaciones.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarCronogramaAlumno(inscripcion)
- **Responsabilidades**: Retorna las unidades del cronograma del programa de la cohorte del alumno según inscripcion. Calcula la semana esperada de avance y compara contra la última unidad con progreso completado, indicando si el alumno se encuentra atrasado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-26: Acceder curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: cronograma.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarMateriales(unidad, tipo, titulo, generadoIA)
- **Responsabilidades**: Retorna las instancias de unMaterial de la unidad, incluyendo los ocultos, cuyos datos coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-27: Buscar material.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: materiales.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos un material cargado.
- **Postcondiciones**: –

---

### seleccionarMaterial(idMaterial, materiales)
- **Responsabilidades**: Retorna la instancia de unMaterial cuyo identificador coincide con idMaterial dentro de materiales.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-27: Buscar material.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unMaterial.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarTiposMateriales()
- **Responsabilidades**: Retorna todos los tipos de material admitidos: Grabación, Bibliografía y Presentación.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-28: Subir material.
- **Notas**: –
- **Excepciones**: –
- **Salida**: tiposMateriales.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarTipoMaterial(idTipoMaterial, tiposMateriales)
- **Responsabilidades**: Retorna el tipo de material cuyo identificador coincide con idTipoMaterial dentro de tiposMateriales.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-28: Subir material.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unTipoMaterial.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### subirGrabacion(unaUnidad, titulo, archivoVideo, unTipoMaterial)
- **Responsabilidades**: Registra un material de tipo Grabación en unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-28: Subir material.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - No se completó alguno de los campos obligatorios (título o el archivo de video).
  - unaUnidad no se encuentra activa.
- **Salida**: unMaterial.
- **Precondiciones**: El docente participa como titular o ayudante. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unMaterial.
  - Se asoció unMaterial a unaUnidad.
  - Se asoció unMaterial a unTipoMaterial.
  - Se asignó a unMaterial.Titulo el valor titulo.
  - Se asignó a unMaterial.RutaArchivo la ruta de archivoVideo.
  - Se asignó a unMaterial.Oculto el valor verdadero.
  - Se asignó a unMaterial.FechaCreacion la fecha actual.
  - Se asignó a unMaterial.Baja el valor falso.

---

### subirBibliografia(unaUnidad, titulo, archivo, autor, unTipoMaterial)
- **Responsabilidades**: Registra un material de tipo Bibliografía en unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-28: Subir material.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - No se completó alguno de los campos obligatorios (título, archivo o autor).
  - unaUnidad no se encuentra activa.
- **Salida**: unMaterial.
- **Precondiciones**: El docente participa como titular o ayudante. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unMaterial.
  - Se asoció unMaterial a unaUnidad.
  - Se asoció unMaterial a unTipoMaterial.
  - Se asignó a unMaterial.Titulo el valor titulo.
  - Se asignó a unMaterial.RutaArchivo la ruta de archivo.
  - Se asignó a unMaterial.Autor el valor autor.
  - Se asignó a unMaterial.Oculto el valor verdadero.
  - Se asignó a unMaterial.FechaCreacion la fecha actual.
  - Se asignó a unMaterial.Baja el valor falso.

---

### subirPresentacion(unaUnidad, titulo, archivoPresentacion, unTipoMaterial)
- **Responsabilidades**: Registra un material de tipo Presentación en unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-28: Subir material.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - No se completó alguno de los campos obligatorios (título o el archivo de presentación).
  - unaUnidad no se encuentra activa.
- **Salida**: unMaterial.
- **Precondiciones**: El docente participa como titular o ayudante. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unMaterial.
  - Se asoció unMaterial a unaUnidad.
  - Se asoció unMaterial a unTipoMaterial.
  - Se asignó a unMaterial.Titulo el valor titulo.
  - Se asignó a unMaterial.RutaArchivo la ruta de archivoPresentacion.
  - Se asignó a unMaterial.Oculto el valor verdadero.
  - Se asignó a unMaterial.FechaCreacion la fecha actual.
  - Se asignó a unMaterial.Baja el valor falso.

---

### modificarMaterial(unMaterial, titulo, archivo, autor, estadoPublicacion)
- **Responsabilidades**: Actualiza el título, el archivo y el estado de publicación de unMaterial. Publicarlo lo hace visible para los alumnos con acceso habilitado a la unidad.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-29: Modificar material.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unMaterial no se encuentra activo.
  - Alguno de los campos obligatorios queda vacío.
  - El nuevo archivo no cumple los datos obligatorios del tipo de material.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unMaterial debe estar activo.
- **Postcondiciones**:
  - Se asignó a unMaterial.Titulo el valor titulo.
  - Se asignó a unMaterial.RutaArchivo la ruta de archivo.
  - Se asignó a unMaterial.Autor el valor autor.
  - Se asignó a unMaterial.Oculto el valor estadoPublicacion.
  - Se asignó a unMaterial.UltimaModificacion la fecha actual.

---

### darDeBajaMaterial(unMaterial)
- **Responsabilidades**: Da de baja a unMaterial y deja de mostrarlo a los alumnos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-30: Dar de baja material.
- **Notas**: –
- **Excepciones**:
  - El actor es Docente y no participa en el curso como titular o ayudante.
  - unMaterial no se encuentra activo.
- **Salida**: –
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. unMaterial debe estar activo.
- **Postcondiciones**: Se asignó a unMaterial.Baja el valor verdadero.

---

### buscarTerminosGlosario(unidad, termino, definicion)
- **Responsabilidades**: Retorna los términos de glosario de la unidad cuyo término o definición coincide con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-31: Buscar término de glosario.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: terminos.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos un término de glosario cargado.
- **Postcondiciones**: –

---

### seleccionarTerminoGlosario(idTermino, terminos)
- **Responsabilidades**: Retorna la instancia de unTermino cuyo identificador coincide con idTermino dentro de terminos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-31: Buscar término de glosario. CU-33: Modificar término de glosario. CU-34: Dar de baja término de glosario.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unTermino.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarTerminoGlosario(unaUnidad, termino, definicion)
- **Responsabilidades**: Registra un nuevo término de glosario en unaUnidad.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-32: Registrar término de glosario.
- **Notas**: –
- **Excepciones**:
  - unaUnidad no se encuentra activa.
  - El docente no participa en el curso como titular o ayudante.
  - No se completó alguno de los campos obligatorios (término o definición).
  - El término ya está registrado en el glosario de unaUnidad.
- **Salida**: unTermino.
- **Precondiciones**: El docente participa como titular o ayudante. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unTermino.
  - Se asoció unTermino a unaUnidad.
  - Se asignó a unTermino.Termino el valor termino.
  - Se asignó a unTermino.Definicion el valor definicion.
  - Se asignó a unTermino.Baja el valor falso.

---

### modificarTerminoGlosario(unTermino, termino, definicion)
- **Responsabilidades**: Actualiza el término y la definición de unTermino.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-33: Modificar término de glosario.
- **Notas**: –
- **Excepciones**:
  - unTermino no se encuentra activo.
  - El docente no participa en el curso como titular o ayudante.
  - Alguno de los campos obligatorios queda vacío.
  - El término modificado ya está registrado en el glosario de esa unidad.
- **Salida**: –
- **Precondiciones**: El docente participa como titular o ayudante. unTermino debe estar activo.
- **Postcondiciones**:
  - Se asignó a unTermino.Termino el valor termino.
  - Se asignó a unTermino.Definicion el valor definicion.

---

### darDeBajaTerminoGlosario(unTermino)
- **Responsabilidades**: Da de baja a unTermino del glosario.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-34: Dar de baja término de glosario.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unTermino no se encuentra activo.
- **Salida**: –
- **Precondiciones**: El docente participa como titular o ayudante. unTermino debe estar activo.
- **Postcondiciones**: Se asignó a unTermino.Baja el valor verdadero.

---

### buscarConsultasForo(unidad, texto, rangoFechas)
- **Responsabilidades**: Retorna las consultas del foro de la unidad, junto con sus respuestas asociadas, cuyo texto y fecha coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-35: Buscar consulta de foro.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: consultas.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos una consulta de foro registrada.
- **Postcondiciones**: –

---

### seleccionarConsultaForo(idConsulta, consultas)
- **Responsabilidades**: Retorna la instancia de unaConsulta cuyo identificador coincide con idConsulta dentro de consultas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-35: Buscar consulta de foro. CU-37: Modificar consulta de foro. CU-38: Dar de baja consulta de foro.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaConsulta.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarConsultaForo(unaUnidad, texto)
- **Responsabilidades**: Registra una nueva consulta de foro en unaUnidad, asociada al alumno.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-36: Registrar consulta de foro.
- **Notas**: –
- **Excepciones**:
  - El alumno no tiene una inscripción vigente al curso.
  - unaUnidad no se encuentra activa.
  - unaUnidad no se encuentra habilitada según el avance secuencial del alumno.
  - No se completó alguno de los campos obligatorios (texto).
- **Salida**: unaConsulta.
- **Precondiciones**: El alumno debe tener una inscripción vigente al curso. unaUnidad debe estar activa. unaUnidad debe estar habilitada según el avance secuencial del alumno.
- **Postcondiciones**:
  - Se creó una instancia de unaConsulta.
  - Se asoció unaConsulta a unaUnidad.
  - Se asoció unaConsulta al alumno.
  - Se asignó a unaConsulta.Texto el valor texto.
  - Se asignó a unaConsulta.Fecha la fecha actual.
  - Se asignó a unaConsulta.Baja el valor falso.

---

### modificarConsultaForo(unaConsulta, texto)
- **Responsabilidades**: Actualiza el texto de unaConsulta, siempre que no se haya superado el plazo límite de edición configurado desde su registro.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-37: Modificar consulta de foro.
- **Notas**: –
- **Excepciones**:
  - unaConsulta no se encuentra activa.
  - unaConsulta no fue registrada por el alumno.
  - Se superó el plazo límite de edición.
  - Alguno de los campos obligatorios queda vacío.
- **Salida**: –
- **Precondiciones**: unaConsulta debe estar activa. unaConsulta debe haber sido registrada por el alumno.
- **Postcondiciones**: Se asignó a unaConsulta.Texto el valor texto.

---

### darDeBajaConsultaForo(unaConsulta)
- **Responsabilidades**: Da de baja a unaConsulta, junto con las respuestas asociadas si existen.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-38: Dar de baja consulta de foro.
- **Notas**: –
- **Excepciones**: unaConsulta no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaConsulta debe estar activa.
- **Postcondiciones**: Se asignó a unaConsulta.Baja el valor verdadero, junto con el de sus respuestas asociadas.

---

### buscarRespuestasForo(consulta)
- **Responsabilidades**: Retorna las respuestas asociadas a la consulta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-39: Buscar respuesta de foro.
- **Notas**: –
- **Excepciones**: –
- **Salida**: respuestas.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La consulta debe estar activa. La consulta debe tener al menos una respuesta registrada.
- **Postcondiciones**: –

---

### seleccionarRespuestaForo(idRespuesta, respuestas)
- **Responsabilidades**: Retorna la instancia de unaRespuesta cuyo identificador coincide con idRespuesta dentro de respuestas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-39: Buscar respuesta de foro. CU-41: Modificar respuesta de foro. CU-42: Dar de baja respuesta de foro.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaRespuesta.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarRespuestaForo(unaConsulta, texto)
- **Responsabilidades**: Registra una nueva respuesta a unaConsulta, asociada al docente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-40: Registrar respuesta de foro.
- **Notas**: –
- **Excepciones**:
  - El docente no participa como titular o ayudante.
  - unaConsulta no se encuentra activa.
  - No se completó alguno de los campos obligatorios (texto).
- **Salida**: unaRespuesta.
- **Precondiciones**: El docente participa como titular o ayudante. unaConsulta debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unaRespuesta.
  - Se asoció unaRespuesta a unaConsulta.
  - Se asoció unaRespuesta al docente.
  - Se asignó a unaRespuesta.Texto el valor texto.
  - Se asignó a unaRespuesta.Fecha la fecha actual.
  - Se asignó a unaRespuesta.Baja el valor falso.

---

### modificarRespuestaForo(unaRespuesta, texto)
- **Responsabilidades**: Actualiza el texto de unaRespuesta, siempre que no se haya superado el plazo límite de edición configurado desde su registro.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-41: Modificar respuesta de foro.
- **Notas**: –
- **Excepciones**:
  - El docente no participa como titular o ayudante.
  - unaRespuesta no se encuentra activa.
  - unaRespuesta no fue registrada por el docente.
  - Se superó el plazo límite de edición.
  - El texto queda vacío.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaRespuesta debe estar activa. unaRespuesta debe haber sido registrada por el docente.
- **Postcondiciones**: Se asignó a unaRespuesta.Texto el valor texto.

---

### darDeBajaRespuestaForo(unaRespuesta)
- **Responsabilidades**: Da de baja a unaRespuesta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-42: Dar de baja respuesta de foro.
- **Notas**: –
- **Excepciones**: unaRespuesta no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaRespuesta debe estar activa.
- **Postcondiciones**: Se asignó a unaRespuesta.Baja el valor verdadero.

---

## MOD-F-03: Módulo de Inscripciones

### buscarInscripciones(curso, alumno, estado)
- **Responsabilidades**: Retorna las instancias de unaInscripcion que coincidan con los criterios especificados, restringidas a las propias si el actor es Alumno.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-43: Buscar inscripción.
- **Notas**: Todos los criterios son opcionales; alumno solo aplica para el Administrador.
- **Excepciones**: –
- **Salida**: inscripciones.
- **Precondiciones**: Debe existir al menos una inscripción registrada.
- **Postcondiciones**: –

---

### generarCertificado(unaInscripcion)
- **Responsabilidades**: Genera el archivo descargable del certificado de finalización de unaInscripcion, a partir de sus datos de emisión ya registrados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-43: Buscar inscripción.
- **Notas**: –
- **Excepciones**: El certificado de unaInscripcion todavía no fue emitido.
- **Salida**: certificado.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarCohortesAbiertas(unCurso)
- **Responsabilidades**: Retorna las instancias de unaCohorte de unCurso que poseen inscripción abierta a la fecha actual.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-44: Inscribir curso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: cohortes.
- **Precondiciones**: unCurso debe tener al menos una cohorte con inscripción abierta.
- **Postcondiciones**: –

---

### registrarInscripcion(unaCohorte)
- **Responsabilidades**: Registra una nueva inscripción del alumno a unaCohorte, calculando la fecha de vencimiento de acceso según las fechas de dictado de unaCohorte si las posee, o según sus semanas de acceso desde la fecha actual en caso contrario. Registra además el progreso inicial del alumno sobre la primera unidad del cronograma del programa de unaCohorte, sin completar.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-44: Inscribir curso.
- **Notas**: –
- **Excepciones**:
  - La fecha actual está fuera de la ventana de inscripción de unaCohorte.
  - unaCohorte ya alcanzó su cupo máximo.
  - El alumno ya posee una inscripción vigente a esa cohorte.
- **Salida**: unaInscripcion.
- **Precondiciones**: La fecha actual debe encontrarse dentro de la ventana de inscripción de unaCohorte. unaCohorte no debe haber alcanzado su cupo máximo, si tiene uno definido. El alumno no debe tener una inscripción vigente a unaCohorte.
- **Postcondiciones**:
  - Se creó una instancia de unaInscripcion.
  - Se asoció unaInscripcion al alumno.
  - Se asoció unaInscripcion a unaCohorte.
  - Se asignó a unaInscripcion.Fecha la fecha actual.
  - Se asignó a unaInscripcion.Baja el valor falso.
  - Se asignó a unaInscripcion.FechaVencimientoAcceso el valor calculado según corresponda.
  - Se creó un progreso del alumno sobre la primera unidad del cronograma del programa de unaCohorte, con completado falso.

---

### darDeBajaInscripcion(unaInscripcion, motivo)
- **Responsabilidades**: Da de baja a unaInscripcion y a los intentos de autoevaluación asociados, registrando el motivo u observación si fue ingresado. No genera reembolso del pago realizado, si corresponde.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-45: Dar de baja inscripción.
- **Notas**: motivo es opcional.
- **Excepciones**:
  - unaInscripcion no se encuentra activa.
  - El actor es Alumno y unaInscripcion no le pertenece.
- **Salida**: –
- **Precondiciones**: unaInscripcion debe estar activa. Si el actor es Alumno, unaInscripcion le debe pertenecer.
- **Postcondiciones**:
  - Se asignó a unaInscripcion.Baja el valor verdadero.
  - Se asignó a unaInscripcion.Observaciones el valor motivo.
  - Se dió de baja los intentos de autoevaluación asociados a unaInscripcion.
  - Por cada intento, se asignó a unIntento.Baja el valor verdadero.

---

### buscarPagos(curso, alumno, estado, rangoFechas)
- **Responsabilidades**: Retorna las instancias de unPago que coincidan con los criterios especificados, restringidas a los propios si el actor es Alumno.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-46: Buscar pago.
- **Notas**: Todos los criterios son opcionales; alumno solo aplica para el Administrador.
- **Excepciones**: –
- **Salida**: pagos.
- **Precondiciones**: Debe existir al menos un pago registrado.
- **Postcondiciones**: –

---

### seleccionarPago(idPago, pagos)
- **Responsabilidades**: Retorna la instancia de unPago cuyo identificador coincide con idPago dentro de pagos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-46: Buscar pago.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unPago.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### generarComprobantePago(unPago)
- **Responsabilidades**: Genera el archivo descargable del comprobante de unPago, a partir de sus datos de emisión ya registrados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-46: Buscar pago.
- **Notas**: –
- **Excepciones**: unPago no fue acreditado.
- **Salida**: comprobante.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### calcularMonto(unCurso)
- **Responsabilidades**: Calcula el monto a pagar por unCurso, evaluando si el alumno autenticado cumple alguna condición de descuento vigente y, de ser así, aplicándolo sobre el precio de unCurso.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-47: Realizar pago.
- **Notas**: –
- **Excepciones**: –
- **Salida**: monto, unDescuento.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarMediosPago()
- **Responsabilidades**: Retorna los medios de pago disponibles para pagar un curso.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-47: Realizar pago.
- **Notas**: –
- **Excepciones**: –
- **Salida**: mediosPago.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarMedioPago(idMedioPago, mediosPago)
- **Responsabilidades**: Retorna el medio de pago cuyo identificador coincide con idMedioPago dentro de mediosPago.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-47: Realizar pago.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unMedioPago.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### realizarPago(unaInscripcion, unMedioPago)
- **Responsabilidades**: Arma y envía a MODO la solicitud de pago de unaInscripcion por unMedioPago, recalculando internamente el monto (con el descuento vigente si corresponde) al momento de generar la solicitud, y registra el pago como pendiente con los datos que devuelve la plataforma de pagos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-47: Realizar pago.
- **Notas**: –
- **Excepciones**:
  - El alumno no tiene ninguna inscripción registrada para el curso, sin el pago realizado.
  - El alumno cancela el pago desde el modal de MODO.
  - MODO rechaza el pago.
  - No se recibe la confirmación de MODO dentro del plazo configurado; el pago se registra como rechazado.
- **Salida**: unPago.
- **Precondiciones**: El alumno debe tener al menos una inscripción registrada para el curso, sin el pago realizado.
- **Postcondiciones**:
  - Se creó una instancia de unPago.
  - Se asoció unPago a unaInscripcion.
  - Se asoció unPago a unMedioPago.
  - Si corresponde, se asoció unPago a unDescuento.
  - Se asignó a unPago.Monto el valor recalculado.
  - Se asignó a unPago.Fecha la fecha actual.
  - Se asignó a unPago.Estado el valor pendiente.
  - Se asignó a unPago.PaymentRequestId el valor devuelto por MODO.
  - Se asignó a unPago.ExternalIntentionId el valor devuelto por MODO.
  - Cuando MODO informa el resultado, se asignó a unPago.Estado el valor acreditado o rechazado.
  - Cuando MODO informa el resultado, se asignó a unPago.ReferenceCode el valor devuelto.
  - Cuando MODO informa el resultado, se asignó a unPago.DetalleEstado el valor devuelto.
  - Si el pago fue con tarjeta, se asignó a unPago.UltimosDigitosTarjeta el valor devuelto por MODO.
  - Si el pago fue acreditado, se asignó a unPago.FechaAprobacion la fecha actual.
  - Si el pago fue acreditado, se habilitó el acceso al curso de unaInscripcion.
  - Si el pago fue acreditado, se asignaron a unPago.NombrePagador y unPago.DniPagador.
  - Si el pago fue acreditado, se asignó a unPago.NumeroComprobante el valor generado.
  - Si el pago fue acreditado, se asignó a unPago.FechaEmisionComprobante la fecha actual.
  - Si el pago fue acreditado, se asignó a unPago.ComprobanteEnviado el valor verdadero.

---

### buscarProgreso(curso, alumno)
- **Responsabilidades**: Retorna el progreso de los alumnos inscriptos en curso en las unidades del programa de su cohorte, filtrado opcionalmente por alumno.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-48: Buscar progreso.
- **Notas**: alumno es opcional.
- **Excepciones**: –
- **Salida**: progresos.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante.
- **Postcondiciones**: –

---

### seleccionarProgreso(idProgreso, progresos)
- **Responsabilidades**: Retorna la instancia de unProgreso cuyo identificador coincide con idProgreso dentro de progresos, con el detalle de cada unidad del programa de la cohorte del alumno, si fue completada y, en caso afirmativo, la fecha en que se completó.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-48: Buscar progreso.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unProgreso.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarDescuentos(nombre, vigencia)
- **Responsabilidades**: Retorna las instancias de unDescuento cuyos datos coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-49: Buscar descuento.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: descuentos.
- **Precondiciones**: Debe existir al menos un descuento activo.
- **Postcondiciones**: –

---

### seleccionarDescuento(idDescuento, descuentos)
- **Responsabilidades**: Retorna la instancia de unDescuento cuyo identificador coincide con idDescuento dentro de descuentos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-49: Buscar descuento. CU-51: Modificar descuento. CU-52: Dar de baja descuento.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unDescuento.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarDescuento(nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite, cantidadCursosRequeridos)
- **Responsabilidades**: Registra un nuevo descuento activo, con cantidad usada en cero, a aplicar automáticamente a los alumnos que cumplan la condición configurada.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-50: Registrar descuento.
- **Notas**: cantidadCursosRequeridos es opcional.
- **Excepciones**:
  - No se completó alguno de los campos obligatorios (nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite).
  - El porcentaje ingresado no está entre 1 y 100.
  - La vigenciaHasta no es posterior a la vigenciaDesde.
  - La cantidadLimite ingresada no es un número entero mayor a cero.
  - La cantidadCursosRequeridos ingresada no es un número entero mayor o igual a cero.
- **Salida**: unDescuento.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unDescuento.
  - Se asignó a unDescuento.Nombre el valor nombre.
  - Se asignó a unDescuento.Porcentaje el valor porcentaje.
  - Se asignó a unDescuento.VigenciaDesde el valor vigenciaDesde.
  - Se asignó a unDescuento.VigenciaHasta el valor vigenciaHasta.
  - Se asignó a unDescuento.CantidadLimite el valor cantidadLimite.
  - Se asignó a unDescuento.CursosRequeridos el valor cantidadCursosRequeridos.
  - Se asignó a unDescuento.CantidadUsada el valor cero.
  - Se asignó a unDescuento.Baja el valor falso.
  - Se asignó a unDescuento.FechaCreacion la fecha actual.

---

### modificarDescuento(unDescuento, nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite, cantidadCursosRequeridos)
- **Responsabilidades**: Actualiza los datos de unDescuento.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-51: Modificar descuento.
- **Notas**: El sistema desactiva automáticamente el descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero.
- **Excepciones**:
  - unDescuento no se encuentra activo.
  - Algún campo obligatorio queda vacío.
  - El porcentaje ingresado no está entre 1 y 100.
  - La vigenciaHasta no es posterior a la vigenciaDesde.
  - La cantidadLimite ingresada no es un número entero mayor a cero.
  - La cantidadCursosRequeridos ingresada no es un número entero mayor o igual a cero.
- **Salida**: –
- **Precondiciones**: unDescuento debe estar activo.
- **Postcondiciones**:
  - Se asignó a unDescuento.Nombre el valor nombre.
  - Se asignó a unDescuento.Porcentaje el valor porcentaje.
  - Se asignó a unDescuento.VigenciaDesde el valor vigenciaDesde.
  - Se asignó a unDescuento.VigenciaHasta el valor vigenciaHasta.
  - Se asignó a unDescuento.CantidadLimite el valor cantidadLimite.
  - Se asignó a unDescuento.CursosRequeridos el valor cantidadCursosRequeridos.
  - Se asignó a unDescuento.UltimaModificacion la fecha actual.

---

### darDeBajaDescuento(unDescuento)
- **Responsabilidades**: Da de baja a unDescuento, siempre que su cantidad usada sea cero.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-52: Dar de baja descuento.
- **Notas**: –
- **Excepciones**:
  - unDescuento no se encuentra activo.
  - unDescuento ya fue aplicado a alguna inscripción (cantidad usada mayor a cero).
- **Salida**: –
- **Precondiciones**: unDescuento debe estar activo. unDescuento no debe haber sido usado.
- **Postcondiciones**: Se asignó a unDescuento.Baja el valor verdadero.

---

## MOD-F-04: Módulo de Evaluaciones

### buscarPools(unidad, nombre)
- **Responsabilidades**: Retorna las instancias de unPool de la unidad cuyo nombre coincida con el criterio especificado, con su cantidad de preguntas cargadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-53: Buscar pool. CU-58: Crear autoevaluación. CU-59: Modificar autoevaluación.
- **Notas**: nombre es opcional.
- **Excepciones**: –
- **Salida**: pools.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos un pool activo.
- **Postcondiciones**: –

---

### seleccionarPool(idPool, pools)
- **Responsabilidades**: Retorna la instancia de unPool cuyo identificador coincide con idPool dentro de pools.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-53: Buscar pool. CU-55: Modificar pool. CU-56: Dar de baja pool.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unPool.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### crearPool(unaUnidad, nombre)
- **Responsabilidades**: Registra un nuevo pool vacío en unaUnidad, con el nombre especificado, como punto de partida para cargar sus preguntas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-54: Crear pool.
- **Notas**: –
- **Excepciones**:
  - unaUnidad no se encuentra activa.
  - El docente no participa en el curso como titular o ayudante.
  - El nombre no fue completado.
- **Salida**: unPool.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unPool.
  - Se asoció unPool a unaUnidad.
  - Se asignó a unPool.Nombre el valor nombre.
  - Se asignó a unPool.Baja el valor falso.
  - Se asignó a unPool.FechaCreacion la fecha actual.

---

### agregarPregunta(unPool, esOpcionMultiple, enunciado)
- **Responsabilidades**: Agrega una nueva pregunta de opción múltiple o verdadero/falso, según esOpcionMultiple, a unPool, con su enunciado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-54: Crear pool. CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: unPool no se encuentra activo.
- **Salida**: unaPregunta.
- **Precondiciones**: unPool debe estar activo.
- **Postcondiciones**:
  - Se creó una instancia de unaPregunta.
  - Se asoció unaPregunta a unPool.
  - Se asignó a unaPregunta.EsOpcionMultiple el valor esOpcionMultiple.
  - Se asignó a unaPregunta.Baja el valor falso.
  - Se asignó a unaPregunta.Texto el valor enunciado.

---

### agregarOpcion(unaPregunta, texto, esCorrecta)
- **Responsabilidades**: Agrega una nueva opción de respuesta a unaPregunta, con su texto e indicación de si es la correcta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-54: Crear pool. CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: –
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unaOpcion.
  - Se asoció unaOpcion a unaPregunta.
  - Se asignó a unaOpcion.Texto el valor texto.
  - Se asignó a unaOpcion.Baja el valor falso.
  - Se asignó a unaOpcion.EsCorrecta el valor esCorrecta.

---

### modificarPool(unPool, nombre)
- **Responsabilidades**: Actualiza el nombre de unPool.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**:
  - unPool no se encuentra activo.
  - El docente no participa en el curso como titular o ayudante.
  - El nombre no fue completado.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unPool debe estar activo.
- **Postcondiciones**: Se asignó a unPool.Nombre el valor nombre.

---

### buscarPreguntas(unPool)
- **Responsabilidades**: Retorna las instancias de unaPregunta de unPool, con sus opciones de respuesta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: –
- **Salida**: preguntas.
- **Precondiciones**: unPool debe estar activo.
- **Postcondiciones**: –

---

### seleccionarPregunta(idPregunta, preguntas)
- **Responsabilidades**: Retorna la instancia de unaPregunta cuyo identificador coincide con idPregunta dentro de preguntas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaPregunta.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### modificarPregunta(unaPregunta, esOpcionMultiple, enunciado)
- **Responsabilidades**: Actualiza si es de opción múltiple o verdadero/falso, y el enunciado de unaPregunta, siempre que no existan intento de autoevaluación activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: El pool tiene intentos de autoevaluación activos asociados.
- **Salida**: –
- **Precondiciones**: El pool que contiene a unaPregunta no debe tener intentos de autoevaluación registrados.
- **Postcondiciones**:
  - Se asignó a unaPregunta.EsOpcionMultiple el valor esOpcionMultiple.
  - Se asignó a unaPregunta.Texto el valor enunciado.

---

### modificarOpcion(unaOpcion, texto, esCorrecta)
- **Responsabilidades**: Actualiza el texto y la corrección de unaOpcion.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: –
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**:
  - Se asignó a unaOpcion.Texto el valor texto.
  - Se asignó a unaOpcion.EsCorrecta el valor esCorrecta.

---

### eliminarOpcion(unaOpcion)
- **Responsabilidades**: Elimina a unaOpcion de su pregunta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: La pregunta quedaría con menos de dos opciones tras la eliminación.
- **Salida**: –
- **Precondiciones**: La pregunta de unaOpcion debe conservar al menos dos opciones después de eliminarla.
- **Postcondiciones**: unaOpcion dejó de existir.

---

### eliminarPregunta(unaPregunta)
- **Responsabilidades**: Elimina a unaPregunta de su pool, junto con sus opciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-55: Modificar pool.
- **Notas**: –
- **Excepciones**: El pool quedaría sin preguntas tras la eliminación.
- **Salida**: –
- **Precondiciones**: El pool de unaPregunta debe conservar al menos una pregunta después de eliminarla.
- **Postcondiciones**: unaPregunta dejó de existir, junto con sus opciones.

---

### darDeBajaPool(unPool)
- **Responsabilidades**: Da de baja a unPool, siempre que no existan autoevaluaciones activas asociadas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-56: Dar de baja pool.
- **Notas**: –
- **Excepciones**:
  - El actor es Docente y no participa en el curso como titular o ayudante.
  - unPool no se encuentra activo.
  - unPool tiene autoevaluaciones activas asociadas.
- **Salida**: –
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. unPool debe estar activo.
- **Postcondiciones**: Se asignó a unPool.Baja el valor verdadero.

---

### buscarAutoevaluaciones(unidad, nombre)
- **Responsabilidades**: Retorna las instancias de unaAutoevaluacion de la unidad cuyo nombre coincida con el criterio especificado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-57: Buscar autoevaluación.
- **Notas**: nombre es opcional.
- **Excepciones**: –
- **Salida**: autoevaluaciones.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. Debe existir al menos una autoevaluación activa.
- **Postcondiciones**: –

---

### seleccionarAutoevaluacion(idAutoevaluacion, autoevaluaciones)
- **Responsabilidades**: Retorna la instancia de unaAutoevaluacion cuyo identificador coincide con idAutoevaluacion dentro de autoevaluaciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-57: Buscar autoevaluación. CU-59: Modificar autoevaluación. CU-60: Dar de baja autoevaluación. CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaAutoevaluacion.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarPools(idsPools, pools)
- **Responsabilidades**: Retorna las instancias de unPool cuyos identificadores coinciden con idsPools dentro de pools, para asociarlos a la autoevaluación.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-58: Crear autoevaluación.
- **Notas**: Selección múltiple; reutilizada en CU-59.
- **Excepciones**: –
- **Salida**: poolsSeleccionados.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### crearAutoevaluacion(unaUnidad, nombre, tiempoLimite, cantidadPreguntas, fechaApertura, fechaCierre, cantidadIntentosPermitidos, poolsSeleccionados)
- **Responsabilidades**: Registra una nueva autoevaluación en unaUnidad, asociada a poolsSeleccionados (de la propia unidad y, si es la última del programa, opcionalmente de otras, conformando la evaluación final).
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-58: Crear autoevaluación.
- **Notas**: fechaCierre y cantidadIntentosPermitidos son opcionales; sin cantidadIntentosPermitidos no hay límite de intentos.
- **Excepciones**:
  - unaUnidad no se encuentra activa.
  - El docente no participa en el curso como titular o ayudante.
  - No se completaron los campos obligatorios o no se seleccionó ningún pool.
  - El tiempoLimite, la cantidadPreguntas, o la cantidadIntentosPermitidos cuando se indicó, no son valores enteros mayores a cero.
  - La fechaCierre especificada no es posterior a la fechaApertura.
  - Los pools seleccionados no reúnen como mínimo cantidadPreguntas activas en conjunto.
- **Salida**: unaAutoevaluacion.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe estar activa. unaUnidad debe tener al menos un pool activo.
- **Postcondiciones**:
  - Se creó una instancia de unaAutoevaluacion.
  - Se asoció unaAutoevaluacion a unaUnidad.
  - Se asoció poolsSeleccionados a unaAutoevaluacion.
  - Se asignó a unaAutoevaluacion.Nombre el valor nombre.
  - Se asignó a unaAutoevaluacion.TiempoLimite el valor tiempoLimite.
  - Se asignó a unaAutoevaluacion.CantidadPreguntas el valor cantidadPreguntas.
  - Se asignó a unaAutoevaluacion.FechaApertura el valor fechaApertura.
  - Se asignó a unaAutoevaluacion.FechaCierre el valor fechaCierre.
  - Se asignó a unaAutoevaluacion.IntentosPermitidos el valor cantidadIntentosPermitidos.
  - Se asignó a unaAutoevaluacion.Baja el valor falso.
  - Se asignó a unaAutoevaluacion.FechaCreacion la fecha actual.

---

### modificarAutoevaluacion(unaAutoevaluacion, nombre, tiempoLimite, cantidadPreguntas, fechaApertura, fechaCierre, cantidadIntentosPermitidos, poolsSeleccionados)
- **Responsabilidades**: Actualiza todos los datos de unaAutoevaluacion, siempre que no existan intentos activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-59: Modificar autoevaluación.
- **Notas**: –
- **Excepciones**:
  - unaAutoevaluacion no se encuentra activa.
  - El docente no participa en el curso como titular o ayudante.
  - El tiempoLimite, la cantidadPreguntas, o la cantidadIntentosPermitidos cuando se indicó, no son valores enteros mayores a cero.
  - La fechaCierre especificada no es posterior a la fechaApertura.
  - Los pools seleccionados no reúnen como mínimo cantidadPreguntas activas en conjunto.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaAutoevaluacion debe estar activa.
- **Postcondiciones**:
  - Se asignó a unaAutoevaluacion.Nombre el valor nombre.
  - Se asignó a unaAutoevaluacion.TiempoLimite el valor tiempoLimite.
  - Se asignó a unaAutoevaluacion.CantidadPreguntas el valor cantidadPreguntas.
  - Se asignó a unaAutoevaluacion.FechaApertura el valor fechaApertura.
  - Se asignó a unaAutoevaluacion.FechaCierre el valor fechaCierre.
  - Se asignó a unaAutoevaluacion.IntentosPermitidos el valor cantidadIntentosPermitidos.
  - Se asoció los pools asociados a unaAutoevaluacion según lo ingresado.
  - Se asignó a unaAutoevaluacion.UltimaModificacion la fecha actual.

---

### modificarAutoevaluacionVigencia(unaAutoevaluacion, fechaCierre, visible, cantidadIntentosPermitidos)
- **Responsabilidades**: Actualiza únicamente la fecha de cierre, la visibilidad y la cantidad de intentos permitidos de unaAutoevaluacion, dado que ya registra intentos activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-59: Modificar autoevaluación.
- **Notas**: –
- **Excepciones**:
  - unaAutoevaluacion no se encuentra activa.
  - El docente no participa en el curso como titular o ayudante.
  - La fechaCierre especificada no es posterior a la fechaApertura ni a la fechaCierre actual.
  - La cantidadIntentosPermitidos ingresada no es mayor a la actual.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaAutoevaluacion debe estar activa.
- **Postcondiciones**:
  - Se asignó a unaAutoevaluacion.FechaCierre el valor fechaCierre.
  - Se asignó a unaAutoevaluacion.Oculto el valor negado de visible.
  - Se asignó a unaAutoevaluacion.IntentosPermitidos el valor cantidadIntentosPermitidos.
  - Se asignó a unaAutoevaluacion.UltimaModificacion la fecha actual.

---

### darDeBajaAutoevaluacion(unaAutoevaluacion)
- **Responsabilidades**: Da de baja a unaAutoevaluacion, siempre que no existan intentos activos asociados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-60: Dar de baja autoevaluación.
- **Notas**: –
- **Excepciones**:
  - El actor es Docente y no participa en el curso como titular o ayudante.
  - unaAutoevaluacion no se encuentra activa.
  - unaAutoevaluacion tiene intentos activos asociados.
- **Salida**: –
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. unaAutoevaluacion debe estar activa.
- **Postcondiciones**: Se asignó a unaAutoevaluacion.Baja el valor verdadero.

---

### seleccionarIntento(idIntento, intentos)
- **Responsabilidades**: Retorna la instancia de unIntento cuyo identificador coincide con idIntento dentro de intentos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-61: Buscar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unIntento.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarCalificaciones(curso, alumno)
- **Responsabilidades**: Retorna, para cada autoevaluación rendida por alumno en curso, la nota y el resultado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-62: Ver calificaciones.
- **Notas**: –
- **Excepciones**: El alumno no rindió ninguna autoevaluación del curso.
- **Salida**: calificaciones.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante.
- **Postcondiciones**: –

---

### iniciarIntento(unaAutoevaluacion)
- **Responsabilidades**: Sortea unaAutoevaluacion.CantidadPreguntas cerradas de los pools asociados a unaAutoevaluacion, con sus opciones de respuesta, y registra el inicio de un nuevo intento del alumno dentro del tiempo límite configurado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**:
  - El alumno no posee una inscripción vigente al curso.
  - unaAutoevaluacion no se encuentra activa.
  - La unidad no se encuentra habilitada según el avance secuencial del alumno.
  - La fecha y hora actual no se encuentra dentro del período habilitado de la autoevaluación.
  - El alumno ya superó el límite de intentos permitidos, si la autoevaluación tiene uno.
- **Salida**: unIntento, preguntas.
- **Precondiciones**: El alumno debe tener una inscripción vigente al curso. unaAutoevaluacion debe estar activa. La unidad en la que se encuentra unaAutoevaluacion debe estar habilitada según el avance secuencial del alumno. La fecha y hora actual debe encontrarse dentro del período habilitado de unaAutoevaluacion. Si unaAutoevaluacion tiene un límite de intentos, el alumno no debe haberlo superado.
- **Postcondiciones**:
  - Se creó una instancia de unIntento.
  - Se asoció unIntento a unaAutoevaluacion.
  - Se asoció unIntento al alumno.
  - Se asignó a unIntento las preguntas sorteadas, según la cantidad configurada en unaAutoevaluacion.

---

### siguientePregunta(preguntas)
- **Responsabilidades**: Retorna la siguiente instancia de unaPregunta no respondida aún dentro de preguntas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaPregunta.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarOpciones(unaPregunta)
- **Responsabilidades**: Retorna las opciones de respuesta de unaPregunta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: opciones.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarOpcion(idOpcion, opciones)
- **Responsabilidades**: Retorna la instancia de unaOpcion cuyo identificador coincide con idOpcion dentro de opciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaOpcion.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### agregarRespuesta(unIntento, unaOpcion)
- **Responsabilidades**: Registra en unIntento la opción elegida por el alumno (unaOpcion) para unaPregunta.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: –
- **Excepciones**: –
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**: Se asoció unIntento a unaOpcion elegida.

---

### entregarIntento(unIntento)
- **Responsabilidades**: Corrige automáticamente unIntento comparando la opción elegida por el alumno con la opción correcta de cada una de sus preguntas, calcula la nota y determina el resultado. Si todas las preguntas fueron respondidas correctamente, registra el progreso de la unidad como completado y, si correspondía a la evaluación final del curso, genera y envía el certificado de finalización.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-63: Realizar intento de autoevaluación.
- **Notas**: La aprobación exige responder correctamente la totalidad de las preguntas del intento; no existe nota de corte parcial. Si se agota el tiempo límite antes de la entrega, el sistema cierra automáticamente el intento con las respuestas dadas hasta ese momento y lo registra como no aprobado.
- **Excepciones**: –
- **Salida**: unIntento.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se asignó a unIntento.Nota el valor obtenido según la corrección.
  - Se asignó a unIntento.FechaEntrega la fecha actual.
  - Si fue aprobado, se registró el progreso del alumno en la unidad como completado, con la fecha de aprobación.
  - Si fue aprobado, se habilitó el acceso a la siguiente unidad.
  - Si correspondía a la evaluación final y fue aprobado, se generó el certificado de finalización.
  - Si correspondía a la evaluación final y fue aprobado, se registró el certificado en la inscripción del alumno.
  - Si correspondía a la evaluación final y fue aprobado, se envió el certificado por correo electrónico.

---

### darDeBajaIntento(unIntento)
- **Responsabilidades**: Da de baja a unIntento ante la detección de fraude, revirtiendo el progreso de unidad y el certificado que ese intento haya generado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-64: Dar de baja intento de autoevaluación.
- **Notas**: –
- **Excepciones**: unIntento no se encuentra activo.
- **Salida**: –
- **Precondiciones**: unIntento debe estar activo.
- **Postcondiciones**:
  - Se asignó a unIntento.Baja el valor verdadero.
  - Si unIntento estaba aprobado y había marcado como completada la unidad correspondiente, se revirtió el progreso de esa unidad a no completada.
  - Si unIntento correspondía a la evaluación final y había generado un certificado, se revirtió la emisión del certificado de la inscripción.

---

## MOD-F-05: Módulo de Clases en Vivo

### buscarClasesEnVivo(unidad, titulo, docente, rangoFechas, estado)
- **Responsabilidades**: Retorna las instancias de unaClase en vivo de la unidad cuyos datos coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-65: Buscar clase en vivo.
- **Notas**: Todos los criterios, salvo unidad, son opcionales.
- **Excepciones**: –
- **Salida**: clases.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos una clase en vivo activa.
- **Postcondiciones**: –

---

### buscarCohortesEnDictado(unPrograma)
- **Responsabilidades**: Retorna las instancias de unaCohorte de unPrograma que poseen fechas de dictado (modalidad con clases en vivo).
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-66: Programar clase en vivo.
- **Notas**: –
- **Excepciones**: –
- **Salida**: cohortes.
- **Precondiciones**: Debe existir al menos una cohorte de unPrograma con fechas de dictado.
- **Postcondiciones**: –

---

### programarClaseEnVivo(unaUnidad, unaCohorte, titulo, fechaHora, duracionEstimada)
- **Responsabilidades**: Registra una nueva clase en vivo en estado Programada, asociada a unaUnidad, al docente y a unaCohorte, y notifica a los alumnos inscriptos en unaCohorte.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-66: Programar clase en vivo.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaUnidad no se encuentra activa.
  - unaCohorte no se encuentra activa.
  - No se completó alguno de los campos obligatorios.
  - La fechaHora ingresada no es posterior al momento actual.
  - La fechaHora no se encuentra dentro de las fechas de dictado de unaCohorte.
  - La clase, considerando la duración estimada, se superpone con otra clase en vivo programada del mismo docente.
- **Salida**: unaClase.
- **Precondiciones**: unaUnidad debe estar activa. unaCohorte debe estar activa. El docente participa en el curso como titular o ayudante. El curso debe tener al menos una cohorte con fechas de dictado.
- **Postcondiciones**:
  - Se creó una instancia de unaClase.
  - Se asoció unaClase a unaUnidad.
  - Se asoció unaClase al docente.
  - Se asoció unaClase a unaCohorte.
  - Se asignó a unaClase.Titulo el valor titulo.
  - Se asignó a unaClase.FechaHora el valor fechaHora.
  - Se asignó a unaClase.DuracionEstimada el valor duracionEstimada.
  - Se asignó a unaClase.Estado el valor Programada.
  - Se asignó a unaClase.Baja el valor falso.
  - Se notificó a los alumnos inscriptos en unaCohorte de la fecha de unaClase.

---

### modificarClaseEnVivo(unaClase, titulo, fechaHora, duracionEstimada)
- **Responsabilidades**: Actualiza el título, la fecha, la hora y la duración estimada de unaClase, siempre que todavía no haya sido transmitida, y notifica a los alumnos inscriptos el cambio.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-67: Modificar clase en vivo.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaClase no se encuentra activa.
  - unaClase no fue registrada por el actor.
  - unaClase no se encuentra programada.
  - Algún campo obligatorio queda vacío, o la fechaHora no es posterior al momento actual.
  - La nueva fechaHora no se encuentra dentro de las fechas de dictado de la cohorte.
  - unaClase se superpone con otra clase en vivo programada del mismo docente.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaClase debe estar activa. unaClase debe haber sido registrada por el actor. unaClase debe estar programada.
- **Postcondiciones**:
  - Se asignó a unaClase.Titulo el valor titulo.
  - Se asignó a unaClase.FechaHora el valor fechaHora.
  - Se asignó a unaClase.DuracionEstimada el valor duracionEstimada.
  - Se notificó a los alumnos inscriptos del cambio, si correspondía.

---

### cancelarClaseEnVivo(unaClase)
- **Responsabilidades**: Da de baja a unaClase programada que todavía no fue transmitida, y notifica a los alumnos inscriptos la cancelación.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-68: Cancelar clase en vivo.
- **Notas**: –
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaClase no se encuentra activa.
  - unaClase no fue registrada por el actor.
  - unaClase no se encuentra programada.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaClase debe estar activa. unaClase debe haber sido registrada por el actor. unaClase debe estar programada.
- **Postcondiciones**:
  - Se asignó a unaClase.Estado el valor cancelado.
  - Se notificó a los alumnos inscriptos de la cancelación.

---

### darDeBajaClaseEnVivo(unaClase)
- **Responsabilidades**: Da de baja a unaClase ya finalizada, retirando también su grabación asociada si existe.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-69: Dar de baja clase en vivo.
- **Notas**: –
- **Excepciones**: unaClase no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaClase debe estar activa. unaClse debe estar finalizada.
- **Postcondiciones**:
  - Se asignó a unaClase.Baja el valor verdadero.
  - Si unaClase generó un material de tipo Grabación, se asignó también su Baja el valor verdadero.

---

### iniciarClaseEnVivo(unaClase)
- **Responsabilidades**: Genera los datos de conexión de la transmisión (URL de streaming y clave privada) de unaClase y la pasa al estado En vivo, quedando disponible para que los alumnos inscriptos ingresen mientras se graba automáticamente vía RTMP desde OBS.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-70: Iniciar clase en vivo.
- **Notas**: La clave de transmisión es privada del docente. La configuración e instalación de OBS está fuera del alcance funcional de este caso de uso.
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaClase no se encuentra activa.
  - unaClase no fue registrada por el actor.
  - unaClase no se encuentra programada.
  - Aún no se alcanzó el horario programado para unaClase.
- **Salida**: datosConexion.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaClase debe estar activa. unaClase debe haber sido registrada por el actor. unaClase debe estar programada. Se alcanzó el horario programado para unaClase.
- **Postcondiciones**: Se asignó a unaClase.Estado el valor En vivo, con sus datos de conexión generados.

---

### finalizarClaseEnVivo(unaClase)
- **Responsabilidades**: Envía la orden de corte de transmisión y grabación al OBS del docente, pasa a unaClase al estado Finalizada, genera la grabación resultante y la carga como material de tipo Grabación de la unidad, en estado publicado, notificando a los alumnos inscriptos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-71: Finalizar clase en vivo.
- **Notas**: La grabación queda disponible por un plazo configurable (cuatro meses por defecto), con aviso previo al alumno antes de su vencimiento y eliminación automática al cumplirse el plazo.
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaClase no se encuentra activa.
  - unaClase no fue registrada por el actor.
  - unaClase no se encuentra en vivo.
- **Salida**: –
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaClase debe estar activa. unaClase debe haber sido registrada por el actor. unaClase debe estar en vivo.
- **Postcondiciones**:
  - Se asignó a unaClase.Estado el valor Finalizada.
  - Se creó unMaterial de tipo Grabación, a partir de la grabación generada.
  - Se asoció unMaterial a la unidad.
  - Se asoció unMaterial a unaClase.
  - Se asignó a unMaterial.Ocutlo el valor falso.
  - Se notificó a los alumnos inscriptos de la disponibilidad de la grabación.

---

### ingresarClaseEnVivo(unaClase)
- **Responsabilidades**: Conecta al alumno a la transmisión en curso de unaClase, verificando que se encuentre en vivo y que el alumno posea inscripción vigente al curso.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-72: Ingresar a clase en vivo.
- **Notas**: –
- **Excepciones**:
  - El alumno no tiene una inscripción vigente al curso al que pertenece unaClase.
  - unaClase todavía no comenzó o ya finalizó.
- **Salida**: –
- **Precondiciones**: El alumno debe tener una inscripción vigente al curso al que pertenece unaClase. unaClase se encuentra en vivo.
- **Postcondiciones**: –

---

## MOD-F-06: Módulo de Generación de Contenido con IA

### generarBancoPreguntas(unaUnidad, guion)
- **Responsabilidades**: Envía la bibliografía y el glosario de unaUnidad, junto con guion si fue ingresado, al modelo de inteligencia artificial local, y registra el pool de preguntas generado (opción múltiple y verdadero/falso, según la proporción configurada), asociado a unaUnidad.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-73: Generar banco de preguntas.
- **Notas**: guion es opcional. El pool queda disponible para revisión del docente antes de utilizarlo en una autoevaluación.
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - El modelo de inteligencia artificial devuelve un banco de preguntas con formato inválido; el sistema descarta el resultado y permite reintentar.
  - unaUnidad no se encuentra activa.
  - unaUnidad no tiene ningún material de tipo bibliográfico ni término de glosario cargado.
- **Salida**: unPool.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe tener al menos un material de tipo Bibliografía o un término de glosario cargado. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unPool.
  - Se asoció unPool a unaUnidad.
  - Se asignó a unPool.Baja el valor falso.
  - Se asignó a unPool las preguntas y opciones generadas por el modelo de inteligencia artificial.

---

### generarResumenUnidad(unaUnidad)
- **Responsabilidades**: Envía la bibliografía cargada de unaUnidad al modelo de inteligencia artificial local, y registra el resumen generado como material de tipo Resumen de unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-74: Generar resumen de unidad.
- **Notas**: El resumen queda disponible para revisión del docente antes de publicarlo.
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaUnidad no se encuentra activa.
  - unaUnidad no tiene ningún material de tipo bibliográfico ni término de glosario cargado.
- **Salida**: unMaterial.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe tener al menos un material de tipo bibliografía cargado. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó unMaterial de tipo Resumen, a partir del resumen generado por el modelo de inteligencia artificial.
  - Se asoció el material a unaUnidad.
  - Se asignó a ese unMaterial.Ocutlo el valor verdadero.

---

### generarPresentacionUnidad(unaUnidad)
- **Responsabilidades**: Envía la bibliografía cargada de unaUnidad al modelo de inteligencia artificial local, y registra la estructura de contenidos recibida, formateada como presentación descargable, como material de tipo Presentación de unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-75: Generar presentación de unidad.
- **Notas**: La presentación queda disponible para revisión del docente antes de publicarla.
- **Excepciones**:
  - El docente no participa en el curso como titular o ayudante.
  - unaUnidad no se encuentra activa.
  - unaUnidad no tiene ningún material de tipo bibliográfico ni término de glosario cargado.
- **Salida**: unMaterial.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. unaUnidad debe tener al menos un material de tipo bibliografía cargado. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó unMaterial de tipo Presentación, a partir de la estructura generada por el modelo de inteligencia artificial.
  - Se asoció el material a unaUnidad.
  - Se asignó a unMaterial.Oculto el valor verdadero.

---

### crearClon(imagen, audio, aceptaTerminos)
- **Responsabilidades**: Envía imagen y audio a HeyGen para crear el avatar y clonar la voz del docente autenticado, y registra los identificadores devueltos (avatar_id, voice_id) junto con la fecha de aceptación de los términos y condiciones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-76: Crear clon.
- **Notas**: –
- **Excepciones**:
  - El docente no se encuentra habilitado para dictar clases.
  - El docente ya posee un avatar y voz clonada registrados.
  - El actor no acepta los términos y condiciones.
  - HeyGen no logra validar la imagen o el audio provistos.
- **Salida**: –
- **Precondiciones**: El docente debe estar activo y habilitado para dictar clases. El docente no debe tener un avatar y voz clonada.
- **Postcondiciones**:
  - Se asignó a unDocente.AvatarId y unDocente.VoiceId los identificadores devueltos por HeyGen.
  - Se asignó unDocente.FechaAceptacionTycClon la fecha actual.
  - Se habilitó al docente para generar clases con Clon de IA.

---

### buscarClasesConClon(unidad, titulo, estado)
- **Responsabilidades**: Retorna las instancias de unaClase con clon de la unidad cuyos datos coincidan con los criterios especificados, con su guión y fecha de generación.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-77: Buscar clase con clon.
- **Notas**: titulo y estado son opcionales.
- **Excepciones**: –
- **Salida**: clases.
- **Precondiciones**: Si el actor es Docente, participa en el curso como titular o ayudante. La unidad debe estar activa. La unidad debe tener al menos una clase con clon activo.
- **Postcondiciones**: –

---

### seleccionarClaseConClon(idClase, clases)
- **Responsabilidades**: Retorna la instancia de unaClase con clon cuyo identificador coincide con idClase dentro de clases.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-77: Buscar clase con clon. CU-79: Modificar clase con clon. CU-80: Dar de baja clase con clon.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaClase.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### generarClaseConClon(unaUnidad, titulo, guion)
- **Responsabilidades**: Registra una clase con clon en unaUnidad en estado Pendiente y envía guion, junto con el avatar_id y voice_id del docente, a HeyGen para animar el avatar y generar el video; al recibirlo, actualiza el estado a Generada y lo carga como material de tipo Grabación de unaUnidad, en estado oculto.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-78: Generar clase con clon.
- **Notas**: –
- **Excepciones**:
  - El docente no tiene registrado su avatar y voz clonada.
  - El docente no se encuentra habilitado para dictar clases.
  - El docente no participa en el curso como titular o ayudante.
  - No se completó alguno de los campos obligatorios (título o guión).
  - HeyGen no logra generar el video; la clase queda en estado Error.
  - unaUnidad no se encuentra activa.
- **Salida**: unaClaseClon.
- **Precondiciones**: El docente participa en el curso como titular o ayudante. El docente se encuentra activo y habilitado para dictar clases. El docente tiene registrado su avatar y voz clonada. unaUnidad debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unaClaseClon.
  - Se asoció unaClaseClon a unaUnidad.
  - Se asignó a unaClaseClon.Baja el valor falso.
  - Se asoció unaClaseClon al docente.
  - Se asignó a unaClaseClon.Titulo el valor titulo.
  - Se asignó a unaClaseClon.Guion el valor guion generado.
  - Se asignó a unaClaseClon.Estado el valor Pendiente.
  - Al recibir el video, se asignó a unaClaseClon.Estado el valor Generada.
  - Al recibir el video, se creó unMaterial de tipo Grabación.
  - Se asoció unMaterial a unaUnidad.
  - Se asoció unMaterial a unaClaseClon.
  - Se asignó a unMaterial.Oculto el valor verdadero.

---

### modificarClaseConClon(unaClase, titulo, guion)
- **Responsabilidades**: Actualiza el título y/o el guión de unaClase con Clon de IA. Si el guión fue modificado, pasa unaClase a estado Pendiente, regenera el video en HeyGen y reemplaza el material de tipo Grabación de la unidad, en estado no publicado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-79: Modificar clase con clon.
- **Notas**: Si solo se modifica el título, no se dispara una nueva generación en HeyGen.
- **Excepciones**:
  - unaClase no se encuentra en estado Generada o Error.
  - El docente no participa como titular o ayudante en el curso de esa unidad.
  - Algún campo obligatorio queda vacío.
  - HeyGen no logra generar el video; unaClase queda en estado Error.
  - unaClase no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaClase debe estar activa. El docente participa en el curso como titular o ayudante. unaClase se encuentra generada o hubo un error.
- **Postcondiciones**:
  - Se asignó a unaClase.Titulo el valor titulo, si fue ingresado.
  - Se asignó a unaClase.Guion el valor guion, si fue ingresado.
  - Si el guión fue modificado, se asignó a unaClase.Estado el valor Generada (o Error, si la generación falló).
  - Si el guión fue modificado, se creó nuevamente unMaterial de tipo Grabación.
  - Se asoció unMaterial a unaUnidad.
  - Se asoció unMaterial a unaClaseClon.
  - Se asignó a unMaterial.Oculto el valor verdadero.

---

### darDeBajaClaseConClon(unaClase)
- **Responsabilidades**: Da de baja a unaClase con clon y a su material asociado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-80: Dar de baja clase con clon.
- **Notas**: –
- **Excepciones**:
  - Si el actor es Docente y no participa en el curso como titular o ayudante
  - unaClase no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaClase debe estar activa. Si el actor es Docente, participa en el curso como titular o ayudante.
- **Postcondiciones**: Se asignó a unaClase.Baja el valor verdadero, junto con el de su material asociado.

---

## MOD-NF-01: Módulo de Usuarios y Notificaciones

### registrarse(nombre, apellido, correo, dni, contrasena)
- **Responsabilidades**: Registra una nueva cuenta con rol Alumno para el interesado, con el correo sin validar, y envía un enlace de validación al correo ingresado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-81: Registrarse.
- **Notas**: –
- **Excepciones**:
  - No se completó alguno de los campos obligatorios.
  - El correo electrónico ya está registrado.
- **Salida**: unUsuario.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unUsuario.
  - Se asignó a unUsuario.Nombre el valor nombre.
  - Se asignó a unUsuario.Apellido el valor apellido.
  - Se asignó a unUsuario.Email el valor correo.
  - Se asignó a unUsuario.Dni el valor dni.
  - Se asignó a unUsuario.Contrasena el valor hasheado de contrasena.
  - Se asignó a unUsuario.Rol el valor Alumno.
  - Se asignó a unUsuario.Baja el valor falso.
  - Se asignó a unUsuario.EmailValidado el valor falso.
  - Se envió un enlace de validación al correo ingresado.

---

### validarCuenta(token)
- **Responsabilidades**: Marca como validado el correo electrónico de la cuenta asociada a token, habilitando el inicio de sesión.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-81: Registrarse.
- **Notas**: –
- **Excepciones**: El token no es válido o ya expiró.
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**: Se asignó a unUsuario.EmailValidado el valor verdadero.

---

### buscarUsuarios(nombre, apellido, correo, dni, rol)
- **Responsabilidades**: Retorna las instancias de unUsuario cuyos datos coincidan con los criterios especificados, con su rol y estado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-82: Buscar usuario.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: usuarios.
- **Precondiciones**: Debe existir al menos un usuario registrado.
- **Postcondiciones**: –

---

### seleccionarUsuario(idUsuario, usuarios)
- **Responsabilidades**: Retorna la instancia de unUsuario cuyo identificador coincide con idUsuario dentro de usuarios.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-82: Buscar usuario. CU-84: Modificar usuario. CU-85: Dar de baja usuario. CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unUsuario.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### buscarRoles()
- **Responsabilidades**: Retorna los roles disponibles para asignar a un usuario: Alumno, Docente y Administrador.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-83: Registrar usuario.
- **Notas**: –
- **Excepciones**: –
- **Salida**: roles.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarRol(idRol, roles)
- **Responsabilidades**: Retorna el rol cuyo identificador coincide con idRol dentro de roles.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-83: Registrar usuario.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unRol.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### registrarUsuario(nombre, apellido, correo, dni, contrasena, telefono, unRol)
- **Responsabilidades**: Registra una nueva cuenta con unRol, junto con los datos personales y de sesión especificados. Si unRol es Docente, se complementa con CU-88: Registrar docente para los datos adicionales del perfil docente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-83: Registrar usuario.
- **Notas**: telefono es opcional. El Administrador puede crear cuentas de otros Administradores, pero no puede modificarlas.
- **Excepciones**:
  - No se completó alguno de los campos obligatorios.
  - El correo electrónico ya está registrado.
- **Salida**: unUsuario.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unUsuario.
  - Se asoció unUsuario a unRol.
  - Se asignó a unUsuario.Nombre el valor nombre.
  - Se asignó a unUsuario.Apellido el valor apellido.
  - Se asignó a unUsuario.Email el valor correo.
  - Se asignó a unUsuario.Dni el valor dni.
  - Se asignó a unUsuario.Contrasena el valor hasheado de contrasena.
  - Se asignó a unUsuario.Telefono el valor telefono.
  - Se asignó a unUsuario.Baja el valor falso.
  - Se envió al correo ingresado las credenciales de sesión.

---

### modificarUsuario(unUsuario, nombre, apellido, correo, dni, telefono, imagenPerfil)
- **Responsabilidades**: Actualiza los datos base de la cuenta de un Alumno (unUsuario).
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-84: Modificar usuario.
- **Notas**: El Administrador puede crear cuentas de otros Administradores, pero no puede modificarlas.
- **Excepciones**:
  - Algún campo obligatorio queda vacío.
  - El correo electrónico ya está registrado por otra cuenta.
  - unUsuario no se encuentra activo.
- **Salida**: –
- **Precondiciones**: unUsuario debe estar activo.
- **Postcondiciones**:
  - Se asignó a unUsuario.Nombre el valor nombre.
  - Se asignó a unUsuario.Apellido el valor apellido.
  - Se asignó a unUsuario.Email el valor correo.
  - Se asignó a unUsuario.Dni el valor dni.
  - Se asignó a unUsuario.Telefono el valor telefono.
  - Se asignó a unUsuario.Imagen el valor imagenPerfil.

---

### darDeBajaUsuario(unUsuario)
- **Responsabilidades**: Da de baja a unUsuario, quitándole el acceso al sistema y cerrando sus sesiones activas.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-85: Dar de baja usuario.
- **Notas**: –
- **Excepciones**:
  - unUsuario posee rol Administrador y es el único administrador activo del sistema.
  - unUsuario posee rol Docente y es titular o ayudante de al menos una cohorte vigente.
  - unUsuario no se encuentra activo.
- **Salida**: –
- **Precondiciones**: unUsuario debe estar activo.
- **Postcondiciones**:
  - Se asignó a unUsuario.Baja el valor verdadero.
  - Las sesiones activas de unUsuario quedaron cerradas.

---

### verPerfil()
- **Responsabilidades**: Retorna los datos de la cuenta del actor autenticado: nombre, apellido, correo, DNI, teléfono e imagen de perfil, y los datos profesionales adicionales si es Docente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-86: Ver perfil.
- **Notas**: –
- **Excepciones**: –
- **Salida**: miPerfil.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### editarPerfil(nombre, apellido, telefono, imagenPerfil)
- **Responsabilidades**: Actualiza los datos base de la cuenta del actor autenticado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-87: Editar perfil.
- **Notas**: –
- **Excepciones**: Algún campo obligatorio queda vacío.
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**:
  - Se asignó a la cuenta del actor Nombre el valor nombre.
  - Se asignó a la cuenta del actor Apellido el valor apellido.
  - Se asignó a la cuenta del actor Telefono el valor telefono.
  - Se asignó a la cuenta del actor ImagenPerfil el valor imagenPerfil.

---

### registrarDocente(unUsuario, biografia, aniosExperiencia, matriculaCnv)
- **Responsabilidades**: Registra la información profesional de unUsuario como Docente, habilitado para dictar clases.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-88: Registrar docente.
- **Notas**: matriculaCnv (matrícula del Registro de Idóneos de la Comisión Nacional de Valores) es opcional. La verificación del título o la matrícula es un control manual y externo que realiza el Administrador antes de cargar los datos.
- **Excepciones**:
  - No se completó alguno de los campos obligatorios.
  - No se declaró ningún título ni matrícula profesional.
  - Los aniosExperiencia ingresados no son un número entero mayor o igual a cero.
- **Salida**: unDocente.
- **Precondiciones**: unUsuario debe estar activo.
- **Postcondiciones**:
  - Se creó una instancia de unDocente.
  - Se asoció unDocente a unUsuario.
  - Se asignó a unDocente.Biografia el valor biografia.
  - Se asignó a unDocente.AniosExperiencia el valor aniosExperiencia.
  - Se asignó a unDocente.MatriculaCnv el valor matriculaCnv.
  - Se asignó a unDocente.Habilitado el valor verdadero.

---

### agregarTitulo(unDocente, titulo, matriculaColegio)
- **Responsabilidades**: Agrega un título universitario a unDocente, con su matrícula del colegio profesional si corresponde.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-88: Registrar docente. CU-89: Modificar docente.
- **Notas**: matriculaColegio es opcional.
- **Excepciones**: unDocente no se encuentra activo o habilitado.
- **Salida**: unTitulo.
- **Precondiciones**: unDocente debe estar activo y habilitado.
- **Postcondiciones**:
  - Se creó una instancia de unTitulo.
  - Se asoció unTitulo a unDocente.
  - Se asignó a unTitulo.Titulo el valor titulo.
  - Se asignó a unTitulo.MatriculaColegio el valor matriculaColegio.

---

### modificarDocente(unDocente, biografia, aniosExperiencia, matriculaCnv, habilitado)
- **Responsabilidades**: Actualiza la biografía, los años de experiencia, la matrícula de la Comisión Nacional de Valores y el estado de habilitación de unDocente para dictar clases.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**:
  - Algún campo obligatorio queda vacío.
  - La modificación deja a unDocente sin ningún título ni matrícula profesional declarados.
  - Los aniosExperiencia ingresados no son un número entero mayor o igual a cero.
  - Se intenta suspender la habilitación y unDocente es titular o ayudante de al menos una cohorte vigente.
  - unDocente no se encuentra activo.
- **Salida**: –
- **Precondiciones**: unDocente debe estar activo.
- **Postcondiciones**:
  - Se asignó a unDocente.Biografia el valor biografia.
  - Se asignó a unDocente.AniosExperiencia el valor aniosExperiencia.
  - Se asignó a unDocente.MatriculaCnv el valor matriculaCnv.
  - Se asignó a unDocente.Habilitado el valor habilitado.
  - Si se suspendió la habilitación, unDocente quedó notificado del cambio de estado.

---

### buscarTitulos(unDocente)
- **Responsabilidades**: Retorna los títulos universitarios registrados de unDocente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**: –
- **Salida**: titulos.
- **Precondiciones**: unDocente debe estar activo y habilitado.
- **Postcondiciones**: –

---

### seleccionarTitulo(idTitulo, titulos)
- **Responsabilidades**: Retorna la instancia de unTitulo cuyo identificador coincide con idTitulo dentro de titulos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unTitulo.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### modificarTitulo(unTitulo, titulo, matriculaColegio)
- **Responsabilidades**: Actualiza el título y la matrícula de colegio profesional de unTitulo.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**: –
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**:
  - Se asignó a unTitulo.Titulo el valor titulo.
  - Se asignó a unTitulo.MatriculaColegio el valor matriculaColegio.

---

### eliminarTitulo(unTitulo)
- **Responsabilidades**: Elimina a unTitulo de los títulos declarados de un docente.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-89: Modificar docente.
- **Notas**: –
- **Excepciones**: El docente quedaría sin ningún título ni matrícula profesional declarados.
- **Salida**: –
- **Precondiciones**: El docente debe conservar al menos un título o matrícula profesional después de eliminar unTitulo.
- **Postcondiciones**: unTitulo dejó de existir.

---

### iniciarSesion(correo, contrasena)
- **Responsabilidades**: Valida las credenciales ingresadas y, si son correctas y no se supera el límite de sesiones concurrentes configurado, registra una nueva sesión activa, con su token, fecha de inicio, IP y dispositivo. La sesión incluye los datos del usuario autenticado (nombre, rol y demás atributos según corresponda) para que el actor sea redirigido a la vista de su rol.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-90: Iniciar sesión.
- **Notas**: –
- **Excepciones**:
  - Las credenciales ingresadas son incorrectas.
  - El correo electrónico todavía no fue validado.
  - El usuario ya alcanzó el límite de sesiones concurrentes permitidas.
  - La cuenta no se encuentra activa.
- **Salida**: unaSesion.
- **Precondiciones**: El actor debe poseer una cuenta registrada. La cuenta debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unaSesion.
  - Se asoció unaSesion al usuario.
  - Se asignó a unaSesion.Token el valor del token generado.
  - Se asignó a unaSesion.FechaInicio la fecha actual.
  - Se asignó a unaSesion.Ip el valor ip.
  - Se asignó a unaSesion.Dispositivo el valor dispositivo.

---

### iniciarSesionConGoogle(tokenGoogle)
- **Responsabilidades**: Valida el token devuelto por Google OAuth y, si es válido y no se supera el límite de sesiones concurrentes configurado, registra una nueva sesión activa.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-90: Iniciar sesión.
- **Notas**: –
- **Excepciones**:
  - El token de Google no es válido.
  - El usuario ya alcanzó el límite de sesiones concurrentes permitidas.
  - La cuenta no se encuentra activa.
- **Salida**: unaSesion.
- **Precondiciones**: El actor debe poseer una cuenta registrada. La cuenta debe estar activa.
- **Postcondiciones**:
  - Se creó una instancia de unaSesion.
  - Se asoció unaSesion al usuario.
  - Se asignó a unaSesion.Token el valor del token generado.
  - Se asignó a unaSesion.FechaInicio la fecha actual.
  - Se asignó a unaSesion.Ip el valor ip.
  - Se asignó a unaSesion.Dispositivo el valor dispositivo.
  - Se asignó a unUsuario.GoogleId el valor del id de Google devuelto, si aún no estaba asignado.

---

### cerrarSesion()
- **Responsabilidades**: Registra la fecha de fin de la sesión activa del actor autenticado.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-91: Cerrar sesión.
- **Notas**: –
- **Excepciones**: –
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**: Se asignó a la sesión activa del actor la FechaFin la fecha actual.

---

### solicitarRecuperacionContrasena(correo)
- **Responsabilidades**: Genera un token de recuperación con su fecha de expiración para la cuenta asociada a correo, y lo envía a ese correo.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-92: Recuperar contraseña.
- **Notas**: –
- **Excepciones**:
  - El correo ingresado no está registrado.
  - La cuenta no se encuentra activa.
- **Salida**: –
- **Precondiciones**: El actor debe tener una cuenta registrada con contraseña propia. La cuenta debe estar activa.
- **Postcondiciones**:
  - Se generó un token de recuperación, con su fecha de expiración.
  - Se envió el token al correo del actor.

---

### restablecerContrasena(token, nuevaContrasena)
- **Responsabilidades**: Valida que token no haya expirado y actualiza la contraseña de la cuenta asociada con nuevaContrasena.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-92: Recuperar contraseña.
- **Notas**: Es la única vía por la que se modifica la contraseña de una cuenta.
- **Excepciones**: El token de recuperación expiró.
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**: Se asignó a unUsuario.Contrasena el valor hasheado de nuevaContrasena.

---

### buscarSesiones(usuario, rangoFechas, ip, dispositivo)
- **Responsabilidades**: Retorna las sesiones del actor autenticado (o de usuario si el actor es Administrador) que coincidan con los criterios especificados (rango de fechas, IP, dispositivo).
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-93: Buscar sesión.
- **Notas**: usuario solo aplica para el Administrador; el resto de los criterios son opcionales.
- **Excepciones**: –
- **Salida**: sesiones.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarSesion(idSesion, sesiones)
- **Responsabilidades**: Retorna la instancia de unaSesion cuyo identificador coincide con idSesion dentro de sesiones.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-93: Buscar sesión. CU-94: Eliminar sesión.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unaSesion.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### eliminarSesion(unaSesion)
- **Responsabilidades**: Registra la fecha de fin de unaSesion, cerrándola de forma forzada.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-94: Eliminar sesión.
- **Notas**: –
- **Excepciones**: unaSesion no se encuentra activa.
- **Salida**: –
- **Precondiciones**: unaSesion debe estar activa.
- **Postcondiciones**: Se asignó a unaSesion.FechaFin la fecha actual.

---

## MOD-NF-02: Módulo de Auditoría

### buscarRegistrosAuditoria(usuario, tipoAccion, entidad, rangoFecha)
- **Responsabilidades**: Retorna los registros de auditoría de acciones críticas del sistema (pagos, altas de curso, cambios de estado de inscripción) cuyos datos coincidan con los criterios especificados.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-95: Consultar auditoría.
- **Notas**: Todos los criterios son opcionales.
- **Excepciones**: –
- **Salida**: registros.
- **Precondiciones**: Debe existir al menos un registro de auditoría.
- **Postcondiciones**: –

---

## MOD-NF-03: Módulo de Reportes y Estadísticas

### generarInformeAlumnos(unCurso, rangoFecha)
- **Responsabilidades**: Recopila los datos de alumnos inscriptos en unCurso, junto con los del resto de los cursos para la comparación, genera el informe de alumnos (comparación entre cursos, evolución en el tiempo, estado de inscripciones) y registra el reporte en el historial.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-96: Generar informe de alumnos de un curso.
- **Notas**: unCurso se resuelve mediante CU-01: Buscar curso.
- **Excepciones**: –
- **Salida**: unInforme.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unReporte.
  - Se asoció unReporte a unCurso.
  - Se asignó a unReporte.TipoReporte el valor Informe de alumnos.
  - Se asignó a unReporte.FechaGeneracion la fecha actual.
  - Se asoció unReporte al administrador que lo generó.
  - El informe de alumnos de unCurso (unInforme) quedó disponible para su descarga.

---

### generarInformeIngresos(unCurso, rangoFecha)
- **Responsabilidades**: Recopila los pagos acreditados de unCurso, junto con los del resto de los cursos para la comparación, genera el informe de ingresos (comparación entre cursos, evolución en el tiempo, ingresos por categoría, monto bruto frente a neto por descuentos) y registra el reporte en el historial.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-97: Generar informe de ingresos de un curso.
- **Notas**: unCurso se resuelve mediante CU-01: Buscar curso.
- **Excepciones**: –
- **Salida**: unInforme.
- **Precondiciones**: –
- **Postcondiciones**:
  - Se creó una instancia de unReporte.
  - Se asoció unReporte a unCurso.
  - Se asignó a unReporte.TipoReporte el valor Informe de ingresos.
  - Se asignó a unReporte.FechaGeneracion la fecha actual.
  - Se asoció unReporte al administrador que lo generó.
  - El informe de ingresos de unCurso (unInforme) quedó disponible para su descarga.

---

### consultarEstadisticas()
- **Responsabilidades**: Retorna los indicadores del sistema en tiempo real: alumnos activos, inscripciones vigentes, ingresos del mes con variación respecto al mes anterior, inscripciones de los últimos 30 días y el ranking de los cinco cursos con más inscriptos.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-98: Consultar estadísticas.
- **Notas**: –
- **Excepciones**: –
- **Salida**: indicadores.
- **Precondiciones**: –
- **Postcondiciones**: –

---

## MOD-NF-04: Módulo de Configuración

### buscarParametros()
- **Responsabilidades**: Retorna el conjunto de parámetros operativos configurados del sistema, con su clave y valor actual.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-99: Configurar parámetros.
- **Notas**: El conjunto de claves está definido de antemano; no admite altas ni bajas de parámetros.
- **Excepciones**: –
- **Salida**: parametros.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### seleccionarParametro(idParametro, parametros)
- **Responsabilidades**: Retorna el parámetro cuyo identificador coincide con idParametro dentro de parametros.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-99: Configurar parámetros.
- **Notas**: –
- **Excepciones**: –
- **Salida**: unParametro.
- **Precondiciones**: –
- **Postcondiciones**: –

---

### modificarParametro(unParametro, valorNuevo)
- **Responsabilidades**: Actualiza el valor de unParametro con valorNuevo.
- **Tipo**: Sistema.
- **Referencias cruzadas**: CU-99: Configurar parámetros.
- **Notas**: El esquema de clave-valor permite incorporar nuevos parámetros sin modificar el esquema de la base de datos.
- **Excepciones**: El valor no fue completado.
- **Salida**: –
- **Precondiciones**: –
- **Postcondiciones**: Se asignó a unParametro.Valor el valorNuevo.