# Diagramas de Secuencia del Sistema (DSS) - IdoneosOnline

---

### CU-01: Buscar curso
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita buscar cursos enviando el mensaje `buscarCursos(nombre, categoria, nivel, docentes, modalidades)`. El sistema retorna la lista `cursos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un curso]:
     * El actor envía `seleccionarCurso(idCurso, cursos)` y el sistema retorna `unCurso`.

---

### CU-02: Ver mis cursos
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El alumno consulta sus inscripciones enviando `buscarInscripcionesAlumno(nombreCurso, estadoInscripcion)`. El sistema retorna la lista `inscripciones`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una inscripción]:
     * El alumno envía `seleccionarInscripcion(idInscripcion, inscripciones)` y el sistema retorna `unaInscripcion`.

---

### CU-03: Registrar curso
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita `buscarCategorias()`. El sistema retorna `categorias`.
  2. El administrador selecciona una categoría con `seleccionarCategoria(idCategoria, categorias)`. El sistema retorna `unaCategoria`.
  3. El administrador solicita `buscarNiveles()`. El sistema retorna `niveles`.
  4. El administrador selecciona un nivel con `seleccionarNivel(idNivel, niveles)`. El sistema retorna `unNivel`.
  5. El administrador solicita `buscarDocentes()`. El sistema retorna `docentes`.
  6. El administrador selecciona el titular con `seleccionarDocente(idDocente, docentes)`. El sistema retorna `unDocenteTitular`.
  7. El administrador selecciona a los ayudantes con `seleccionarAyudantes(idsDocentes, docentes)`. El sistema retorna `ayudantes`.
  8. El administrador solicita `buscarModalidades()`. El sistema retorna `listadoModalidades`.
  9. El administrador selecciona las modalidades con `seleccionarModalidades(idsModalidades, listadoModalidades)`. El sistema retorna `modalidades`.
  10. El administrador registra el curso enviando `registrarCurso(nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)`. El sistema retorna `unCurso`.

---

### CU-04: Modificar curso
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-01: Buscar curso` y se obtiene `unCurso`.
  2. **Bloque Alternativo (`alt`):**
     * **Rama 1** [Si el curso no tiene inscripciones activas asociadas]:
       * Solicita `buscarCategorias()` $\rightarrow$ Retorna `categorias`.
       * Selecciona categoría: `seleccionarCategoria(idCategoria, categorias)` $\rightarrow$ Retorna `unaCategoria`.
       * Solicita `buscarNiveles()` $\rightarrow$ Retorna `niveles`.
       * Selecciona nivel: `seleccionarNivel(idNivel, niveles)` $\rightarrow$ Retorna `unNivel`.
       * Solicita `buscarDocentes()` $\rightarrow$ Retorna `docentes`.
       * Selecciona docente titular: `seleccionarDocente(idDocente, docentes)` $\rightarrow$ Retorna `unDocenteTitular`.
       * Selecciona ayudantes: `seleccionarAyudantes(idsDocentes, docentes)` $\rightarrow$ Retorna `ayudantes`.
       * Solicita `buscarModalidades()` $\rightarrow$ Retorna `listadoModalidades`.
       * Selecciona modalidades: `seleccionarModalidades(idsModalidades, listadoModalidades)` $\rightarrow$ Retorna `modalidades`.
       * Ejecuta la modificación completa: `modificarCurso(unCurso, nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)`.
     * **Rama 2** [Si el curso tiene inscripciones activas asociadas]:
       * Ejecuta modificación parcial: `modificarCursoPrecioImagen(unCurso, precio, imagen)`.

---

### CU-05: Dar de baja curso
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-01: Buscar curso`, retornando `unCurso`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaCurso(unCurso)`.

---

### CU-06: Explorar catálogo de cursos
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El alumno consulta la oferta académica abierta enviando `buscarCursosAbiertos(nombre, categoria, nivel, docente, modalidad)`. El sistema retorna `cursos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un curso para ver su ficha]:
     * El alumno envía `seleccionarCurso(idCurso, cursos)` y el sistema retorna `unCurso`.

---

### CU-07: Buscar categoria
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita buscar categorías enviando el mensaje `buscarCategorias(nombre)`. El sistema retorna la lista `categorias`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una categoria]:
     * El administrador envía `seleccionarCategoria(idCategoria, categorias)` y el sistema retorna `unaCategoria`.

---

### CU-08: Registrar categoria
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita el registro enviando el mensaje `registrarCategoria(nombre, descripcion)`. El sistema retorna la entidad creada `unaCategoria`.

---

### CU-09: Modificar categoria
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-07: Buscar categoria` y el sistema retorna `unaCategoria`.
  2. El administrador envía la solicitud de modificación con el mensaje `modificarCategoria(unaCategoria, nombre, descripcion)`.

---

### CU-10: Dar de baja categoria
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-07: Buscar categoria` y el sistema retorna `unaCategoria`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaCategoria(unaCategoria)`.

---

### CU-11: Buscar cohorte
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarCohortes(programa, estado, fechaInicioInscripcion, fechaFinInscripcion)`. El sistema retorna la lista `cohortes`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una cohorte]:
     * El actor envía `seleccionarCohorte(idCohorte, cohortes)` y el sistema retorna `unaCohorte`.

---

### CU-12: Registrar cohorte
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-15: Buscar programa` y se obtiene `unPrograma`.
  2. El administrador registra la cohorte enviando `registrarCohorte(unPrograma, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)`. El sistema retorna la entidad creada `unaCohorte`.

---

### CU-13: Modificar cohorte
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-11: Buscar cohorte` y se obtiene `unaCohorte`.
  2. El administrador envía la modificación con el mensaje `modificarCohorte(unCohorte, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)`.

---

### CU-14: Dar de baja cohorte
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-11: Buscar cohorte` y se obtiene `unaCohorte`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaCohorte(unaCohorte)`.

---

### CU-15: Buscar programa
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita el programa vigente enviando el mensaje `obtenerProgramaVigente(curso)`. El sistema retorna `unPrograma`.
  2. **Bloque Opcional (`opt`)** [Si el actor desea buscar un programa distinto]:
     * El actor solicita buscar programas con `buscarProgramas(curso, nombre, estado)`. El sistema retorna la lista `programas`.
     * El actor selecciona un programa enviando `seleccionarPrograma(idPrograma, programas)` y el sistema retorna `unPrograma`.

---

### CU-16: Registrar programa
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-01: Buscar curso` y se obtiene `unCurso`.
  2. **Bloque Opcional (`opt`)** [Si el actor desea partir de un programa anterior]:
     * El docente solicita `buscarProgramas(unCurso)`. El sistema retorna `programas`.
     * El docente selecciona el programa base con `seleccionarPrograma(idPrograma, programas)`. El sistema retorna `unProgramaAnterior`.
  3. El docente registra el nuevo programa enviando `registrarPrograma(unCurso, nombre, descripcion, objetivos, cargaHorariaTotal, bibliografia, unProgramaAnterior)`. El sistema retorna la entidad creada `unPrograma`.

---

### CU-17: Modificar programa
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-15: Buscar programa` y se obtiene `unPrograma`.
  2. El docente envía la modificación con el mensaje `modificarPrograma(unPrograma, nombre, descripcion, objetivos, cargaHorariaTotal, bibliografia)`.

---

### CU-18: Dar de baja programa
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-15: Buscar programa` y se obtiene `unPrograma`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaPrograma(unPrograma)`.

---

### CU-19: Buscar unidad
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarUnidades(programa)`. El sistema retorna la lista `unidades`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una unidad]:
     * El actor envía `seleccionarUnidad(idUnidad, unidades)` y el sistema retorna `unaUnidad`.

---

### CU-20: Agregar unidad
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-15: Buscar programa` y se obtiene `unPrograma`.
  2. **Bloque Alternativo (`alt`):**
     * **Rama 1** [Si el actor desea crear una unidad nueva]:
       * El docente crea la unidad enviando `crearUnidad(unPrograma, titulo, descripcion, contenido)`. El sistema retorna `unaUnidad`.
     * **Rama 2** [Si el actor desea agregar una unidad existente de otro programa]:
       * El docente solicita `buscarUnidadesReutilizables(unPrograma)`. El sistema retorna la lista `unidadesReutilizables`.
       * El docente selecciona la unidad mediante `seleccionarUnidad(idUnidad, unidadesReutilizables)`. El sistema retorna `unaUnidad`.
       * El docente asocia la unidad con `agregarUnidadExistente(unPrograma, unaUnidad)`.

---

### CU-21: Modificar unidad
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente envía la modificación con el mensaje `modificarUnidad(unaUnidad, titulo, descripcion, contenido)`.

---

### CU-22: Quitar unidad
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtienen `unPrograma, unaUnidad`.
  2. El actor solicita desvincular la unidad enviando el mensaje `quitarUnidad(unPrograma, unaUnidad)`.

---

### CU-23: Buscar cronograma
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarCronograma(programa)`. El sistema retorna `unCronograma`.

---

### CU-24: Modificar cronograma
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-15: Buscar programa` y se obtiene `unPrograma`.
  2. El docente envía la modificación del cronograma con el mensaje `modificarCronograma(unPrograma, ordenUnidades, duraciones)`.

---

### CU-25: Ver participantes
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor consulta los participantes enviando el mensaje `buscarParticipantes(curso, nombreCompleto, rol)`. El sistema retorna la lista `participantes`.

---

### CU-26: Acceder curso
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El alumno solicita listar las unidades enviando `buscarUnidades(programa)`. El sistema retorna la lista `unidades`.
  2. **Bloque Alternativo (`alt`):**
     * **Rama 1** [Si el actor desea acceder al contenido de una unidad]:
       * Selecciona una unidad enviando `seleccionarUnidad(idUnidad, unidades)` $\rightarrow$ Retorna `unaUnidad`.
       * Consulta el detalle enviando `verContenidoUnidad(unaUnidad)` $\rightarrow$ Retorna `materiales, glosario, foro, autoevaluaciones, clases`.
       * **Bloque Opcional anidado (`opt`)** [Si el actor selecciona una autoevaluacion]:
         * Envía `seleccionarAutoevaluacion(idAutoevaluacion, autoevaluaciones)` $\rightarrow$ Retorna `unaAutoevaluacion`.
         * Solicita los intentos con `buscarIntentos(unaAutoevaluacion)`.
       * **Bloque Opcional anidado (`opt`)** [Si el actor selecciona una clase en vivo]:
         * Envía `seleccionarClaseEnVivo(idClase, clases)` $\rightarrow$ Retorna `unaClase`.
     * **Rama 2** [Si el actor desea consultar los participantes de la cohorte]:
       * Envía `buscarParticipantesPorCohorte(cohorte, nombreCompleto, rol)` $\rightarrow$ Retorna `participantes`.
     * **Rama 3** [Si el actor desea consultar sus calificaciones]:
       * Envía `buscarCalificacionesAlumno(inscripcion)` $\rightarrow$ Retorna `calificaciones`.
     * **Rama 4** [Si el actor desea consultar el cronograma]:
       * Envía `buscarCronogramaAlumno(inscripcion)` $\rightarrow$ Retorna `cronograma`.

---

### CU-27: Buscar material
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando `buscarMateriales(unidad, tipo, titulo, generadoIA)`. El sistema retorna la lista `materiales`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un material]:
     * El actor envía `seleccionarMaterial(idMaterial, materiales)` y el sistema retorna `unMaterial`.

---

### CU-28: Subir material
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente solicita `buscarTiposMateriales()`. El sistema retorna `tiposMateriales`.
  3. El docente selecciona el tipo enviando `seleccionarTipoMaterial(idTipoMaterial, tiposMateriales)`. El sistema retorna `unTipoMaterial`.
  4. **Bloque Alternativo (`alt`):**
     * **Rama 1** [Si el tipo de material es una grabacion]:
       * El docente envía `subirGrabacion(unaUnidad, titulo, archivoVideo, unTipoMaterial)` $\rightarrow$ Retorna `unMaterial`.
     * **Rama 2** [Si el tipo de material es una bibliografia]:
       * El docente envía `subirBibliografia(unaUnidad, titulo, archivoOEnlace, autor, unTipoMaterial)` $\rightarrow$ Retorna `unMaterial`.
     * **Rama 3** [Si el tipo de material es una presentacion]:
       * El docente envía `subirPresentacion(unaUnidad, titulo, archivoPresentacion, unTipoMaterial)` $\rightarrow$ Retorna `unMaterial`.

---

### CU-29: Modificar material
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-27: Buscar material` y se obtiene `unMaterial`.
  2. El docente envía la modificación con el mensaje `modificarMaterial(unMaterial, titulo, archivo, estadoPublicacion)`.

---

### CU-30: Dar de baja material
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-27: Buscar material` y se obtiene `unMaterial`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaMaterial(unMaterial)`.

---

### CU-31: Buscar termino de glosario
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita buscar términos enviando el mensaje `terminos = buscarTerminosGlosario(unidad, termino, definicion)`. El sistema retorna la lista `terminos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un termino]:
     * El actor envía `seleccionarTerminoGlosario(idTermino, terminos)` y el sistema retorna `unTermino`.

---

### CU-32: Registrar termino de glosario
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente registra el término enviando `registrarTerminoGlosario(unaUnidad, termino, definicion)`. El sistema retorna la entidad creada `unTermino`.

---

### CU-33: Modificar termino de glosario
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-31: Buscar termino de glosario` y se obtiene `unTermino`.
  2. El docente envía la modificación con el mensaje `modificarTerminoGlosario(unTermino, termino, definicion)`.

---

### CU-34: Dar de baja termino de glosario
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-31: Buscar termino de glosario` y se obtiene `unTermino`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaTerminoGlosario(unTermino)`.

---

### CU-35: Buscar consulta de foro
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarConsultasForo(unidad, texto, rangoFechas)`. El sistema retorna la lista `consultas`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una consulta]:
     * El actor envía `seleccionarConsultaForo(idConsulta, consultas)` y el sistema retorna `unaConsulta`.

---

### CU-36: Registrar consulta de foro
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-26: Ver contenido de un curso` (Acceder curso) y se obtiene `unaUnidad`.
  2. El alumno registra la consulta enviando el mensaje `registrarConsultaForo(unaUnidad, texto)`. El sistema retorna la entidad creada `unaConsulta`.

---

### CU-37: Modificar consulta de foro
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-35: Buscar consulta de foro` y se obtiene `unaConsulta`.
  2. El alumno envía la modificación con el mensaje `modificarConsultaForo(unaConsulta, texto)`.

---

### CU-38: Dar de baja consulta de foro
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-35: Buscar consulta de foro` y se obtiene `unaConsulta`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaConsultaForo(unaConsulta)`.

---

### CU-39: Buscar respuesta de foro
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarRespuestasForo(consulta)`. El sistema retorna la lista `respuestas`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una respuesta]:
     * El actor envía `seleccionarRespuestaForo(idRespuestas, respuestas)` y el sistema retorna `unaRespuesta`.

---

### CU-40: Registrar respuesta de foro
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-35: Buscar consulta de foro` y se obtiene `unaConsulta`.
  2. El docente registra la respuesta enviando el mensaje `registrarRespuestaForo(unaConsulta, texto)`. El sistema retorna la entidad creada `unaRespuesta`.

---

### CU-41: Modificar respuesta de foro
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-39: Buscar respuesta de foro` y se obtiene `unaRespuesta`.
  2. El docente envía la modificación con el mensaje `modificarRespuestaForo(unaRespuesta, texto)`.

---

### CU-42: Dar de baja respuesta de foro
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-39: Buscar respuesta de foro` y se obtiene `unaRespuesta`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaRespuestaForo(unaRespuesta)`.

---

### CU-43: Buscar inscripcion
* **Actores:** Alumno / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `buscarInscripciones(curso, alumno, estado)`. El sistema retorna la lista `inscripciones`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una inscripcion]:
     * El actor envía `seleccionarInscripcion(idInscripcion, inscripciones)` y el sistema retorna `unaInscripcion`.
  3. **Bloque Opcional (`opt`)** [Si el actor desea generar el certificado de una inscripcion, si fue emitido]:
     * El actor envía `generarCertificado(unaInscripcion)` y el sistema retorna `certificado`.

---

### CU-44: Inscribir curso
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-06: Explorar catalogo de cursos` y se obtiene `unCurso`.
  2. El alumno solicita buscar cohortes abiertas con el mensaje `buscarCohortesAbiertas(unCurso)`. El sistema retorna la lista `cohortes`.
  3. El alumno selecciona una cohorte enviando `seleccionarCohorte(idCohorte, cohortes)`. El sistema retorna `unaCohorte`.
  4. El alumno registra la inscripción mediante `registrarInscripcion(unaCohorte)`. El sistema retorna `unaInscripcion`.
  5. **Bloque Opcional (`opt`)** [Si el curso tiene costo]:
     * **Referencia/Inclusión:** Comienza el DSS de `CU-47: Realizar pago` y se obtiene `unPago`.

---

### CU-45: Dar de baja inscripcion
* **Actores:** Administrador / Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-43: Buscar inscripcion` y se obtiene `unaInscripcion`.
  2. El actor solicita la baja de la inscripción enviando el mensaje `darDeBajaInscripcion(unaInscripcion, motivo)`.

---

### CU-46: Buscar pago
* **Actores:** Alumno / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita buscar pagos enviando el mensaje `buscarPagos(curso, alumno, estado, rangoFechas)`. El sistema retorna la lista `pagos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un pago]:
     * El actor envía `seleccionarPago(idPago, pagos)` y el sistema retorna `unPago`.
  3. **Bloque Opcional (`opt`)** [Si el actor desea generar el comprobante de un pago, si fue acreditado]:
     * El actor envía `generarComprobantePago(unPago)` y el sistema retorna `comprobante`.

---

### CU-47: Realizar pago
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El alumno solicita el cálculo del valor a abonar enviando `calcularMonto(unCurso)`. El sistema retorna `monto, unDescuento`.
  2. El alumno solicita los medios de pago disponibles mediante `buscarMediosPago()`. El sistema retorna `mediosPago`.
  3. El alumno elige una opción enviando `seleccionarMedioPago(idMedioPago, mediosPago)`. El sistema retorna `unMedioPago`.
  4. El alumno ejecuta la operación enviando `realizarPago(unaInscripcion, unMedioPago)`. El sistema retorna la entidad `unPago`.

---

### CU-48: Buscar progreso
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda del avance enviando el mensaje `buscarProgreso(curso, alumno)`. El sistema retorna la lista `progresos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un progreso]:
     * El actor envía `seleccionarPago(idProgreso, progresos)` y el sistema retorna `unProgreso`.

---

### CU-49: Buscar descuento
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita la búsqueda enviando el mensaje `buscarDescuentos(nombre, vigencia)`. El sistema retorna la lista `descuentos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un descuento]:
     * El administrador envía `seleccionarDescuento(idDescuento, descuentos)` y el sistema retorna `unDescuento`.

---

### CU-50: Registrar descuento
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador envía la solicitud para registrar el descuento mediante `registrarDescuento(nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite, cantidadCursosRequeridos)`. El sistema retorna la entidad creada `unDescuento`.

---

### CU-51: Modificar descuento
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-49: Buscar descuento` y se obtiene `unDescuento`.
  2. El administrador envía la modificación con el mensaje `modificarDescuento(unDescuento, nombre, porcentaje, vigenciaDesde, vigenciaHasta, cantidadLimite, cantidadCursosRequeridos)`.

---

### CU-52: Dar de baja descuento
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-49: Buscar descuento` y se obtiene `unDescuento`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaDescuento(unDescuento)`.

---

### CU-53: Buscar pool
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `pools = buscarPools(unidad, nombre)`. El sistema retorna la lista `pools`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un pool]:
     * El actor envía `unPool = seleccionarPool(idPool, pools)` y el sistema retorna `unPool`.

---

### CU-54: Crear pool
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente crea el pool enviando `unPool = crearPool(unaUnidad, nombre)`. El sistema retorna `unPool`.
  3. **Bloque Bucle (`loop`)** [Mientras haya preguntas para agregar]:
     * El docente envía `unaPregunta = agregarPreguntaUnPool(tipo, enunciado)` y el sistema retorna `unaPregunta`.
     * **Bloque Bucle anidado (`loop`)** [Mientras haya opciones para agregar a la pregunta]:
       * El docente envía `agregarOpcion(unaPregunta, texto, esCorrecta)`.

---

### CU-55: Modificar pool
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-53: Buscar pool` y se obtiene `unPool`.
  2. El docente modifica el pool enviando `modificarPool(unPool, nombre)`.
  3. El docente solicita las preguntas del pool mediante `preguntas = buscarPreguntas(unPool)`. El sistema retorna la lista `preguntas`.
  4. **Bloque Bucle (`loop`)** [Mientras haya preguntas para agregar, editar o eliminar]:
     * **Bloque Alternativo (`alt`):**
       * **Rama 1** [Si el actor desea agregar una pregunta]:
         * Envía `unaPregunta = agregarPreguntaUnPool(tipo, enunciado)` $\rightarrow$ Retorna `unaPregunta`.
         * **Bloque Bucle anidado (`loop`)** [Mientras haya opciones para agregar a la pregunta]:
           * Envía `agregarOpcion(unaPregunta, texto, esCorrecta)`.
       * **Rama 2** [Si el actor desea editar una pregunta]:
         * Envía `unaPregunta = seleccionarPregunta(idPregunta, preguntas)` $\rightarrow$ Retorna `unaPregunta`.
         * Envía `modificarPregunta(unaPregunta, tipo, enunciado)`.
         * **Bloque Bucle anidado (`loop`)** [Mientras haya opciones para agregar, editar o eliminar en esa pregunta]:
           * **Bloque Alternativo interno (`alt`):**
             * [Si el actor desea agregar una opcion]: Envía `agregarOpcion(unaPregunta, texto, esCorrecta)`.
             * [Si el actor desea editar una opcion]: Envía `modificarOpcion(unaOpcion, texto, esCorrecta)`.
             * [Si el actor desea eliminar una opcion]: Envía `eliminarOpcion(unaOpcion)`.
       * **Rama 3** [Si el actor desea eliminar una pregunta]:
         * Envía `unaPregunta = seleccionarPregunta(idPregunta, preguntas)` $\rightarrow$ Retorna `unaPregunta`.
         * Envía `eliminarPregunta(unaPregunta)`.

---

### CU-56: Dar de baja pool
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-53: Buscar pool` y se obtiene `unPool`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaPool(unPool)`.

---

### CU-57: Buscar autoevaluacion
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `autoevaluaciones = buscarAutoevaluaciones(unidad, nombre)`. El sistema retorna la lista `autoevaluaciones`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una autoevaluacion]:
     * El actor envía `unaAutoevaluacion = seleccionarAutoevaluacion(idAutoevaluacion, autoevaluaciones)` y el sistema retorna `unaAutoevaluacion`.

---

### CU-58: Crear autoevaluacion
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente solicita los bancos de preguntas disponibles con `pools = buscarPools(unaUnidad)`. El sistema retorna la lista `pools`.
  3. El docente selecciona los pools enviando `poolsSeleccionados = seleccionarPools(idsPools, pools)`. El sistema retorna `poolsSeleccionados`.
  4. El docente crea la autoevaluación enviando `unaAutoevaluacion = crearAutoevaluacion(unaUnidad, nombre, tiempoLimite, cantidadPreguntas, fechaApertura, fechaCierre, cantidadIntentosPermitidos, poolsSeleccionados)`. El sistema retorna la entidad creada `unaAutoevaluacion`.

---

### CU-59: Modificar autoevaluacion
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-57: Buscar autoevaluacion` y se obtiene la referencia del contexto (`unaUnidad` / `unaAutoevaluacion`).
  2. **Bloque Alternativo (`alt`):**
     * **Rama 1** [Si la autoevaluacion no tiene intentos activos asociados]:
       * Solicita `pools = buscarPools(unaUnidad)` $\rightarrow$ Retorna `pools`.
       * Selecciona los pools: `poolsSeleccionados = seleccionarPools(idsPools, pools)` $\rightarrow$ Retorna `poolsSeleccionados`.
       * Ejecuta la modificación completa: `modificarAutoevaluacion(unaAutoevaluacion, nombre, tiempoLimite, cantidadPreguntas, fechaApertura, fechaCierre, cantidadIntentosPermitidos, poolsSeleccionados)`.
     * **Rama 2** [Si la autoevaluacion tiene intentos activos asociados]:
       * Ejecuta la modificación restringida: `modificarAutoevaluacion(unaAutoevaluacion, fechaCierre, visible, cantidadIntentosPermitidos)`.

---

### CU-60: Dar de baja autoevaluacion
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-57: Buscar autoevaluacion` y se obtiene `unaAutoevaluacion`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaAutoevaluacion(unaAutoevaluacion)`.

---

### CU-61: Buscar intento de autoevaluacion
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `intentos = buscarIntentos(autoevaluacion, alumno, rangoFechas, resultado)`. El sistema retorna la lista `intentos`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una intento]:
     * El actor envía `unIntento = seleccionarIntento(idIntento, intentos)` y el sistema retorna `unIntento`.

---

### CU-62: Ver calificaciones
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la consulta de notas enviando el mensaje `calificaciones = buscarCalificaciones(curso, alumno)`. El sistema retorna la lista `calificaciones`.

---

### CU-63: Realizar intento de autoevaluacion
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-26: Acceder curso` y se obtiene `unaAutoevaluacion`.
  2. El alumno solicita iniciar la evaluación con el mensaje `unIntento, preguntas = iniciarIntento(unaAutoevaluacion)`. El sistema retorna la entidad `unIntento` y la colección `preguntas`.
  3. **Bloque Bucle (`loop`)** [Mientras haya preguntas en la autoevaluación]:
     * El alumno solicita la siguiente pregunta con `unaPregunta = siguientePregunta(preguntas)`. El sistema retorna `unaPregunta`.
     * El alumno solicita las alternativas de la pregunta con `opciones = buscarOpciones(unaPregunta)`. El sistema retorna la lista `opciones`.
     * El alumno elige una opción enviando `unaOpcion = seleccionarOpcion(idOpcion, opciones)`. El sistema retorna `unaOpcion`.
     * El alumno guarda su respuesta mediante `agregarRespuesta(unIntento, unaOpcion)`.
  4. El alumno finaliza y envía la evaluación con `unIntento = entregarIntento(unIntento)`. El sistema retorna el intento procesado `unIntento`.

---

### CU-64: Dar de baja intento de autoevaluacion
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-61: Buscar intento de autoevaluacion` y se obtiene `unIntento`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaIntento(unIntento)`.

---

### CU-65: Buscar clase en vivo
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita buscar clases enviando el mensaje `clases = buscarClasesEnVivo(unidad, titulo, docente, rangoFechas, estado)`. El sistema retorna la lista `clases`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una clase]:
     * El actor envía `unaClase = seleccionarClaseEnVivo(idClase, clases)` y el sistema retorna `unaClase`.

---

### CU-66: Programar clase en vivo
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtienen `unPrograma, unaUnidad`.
  2. El docente solicita las cohortes activas enviando `cohortes = buscarCohortesEnDictado(unPrograma)`. El sistema retorna la lista `cohortes`.
  3. El docente selecciona una cohorte enviando `unaCohorte = seleccionarCohorte(idCohorte, cohortes)`. El sistema retorna `unaCohorte`.
  4. El docente programa la sesión mediante `unaClase = programarClaseEnVivo(unaUnidad, unaCohorte, titulo, fechaHora, duracionEstimada)`. El sistema retorna la entidad creada `unaClase`.

---

### CU-67: Modificar clase en vivo
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-65: Buscar clase en vivo` y se obtiene `unaClase`.
  2. El docente envía la modificación con el mensaje `modificarClaseEnVivo(unaClase, titulo, fechaHora, duracionEstimada)`.

---

### CU-68: Cancelar clase en vivo
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-65: Buscar clase en vivo` y se obtiene `unaClase`.
  2. El docente solicita la cancelación enviando el mensaje `cancelarClaseEnVivo(unaClase)`.

---

### CU-69: Dar de baja clase en vivo
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-65: Buscar clase en vivo` y se obtiene `unaClase`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaClaseEnVivo(unaClase)`.

---

### CU-70: Iniciar clase en vivo
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-65: Buscar clase en vivo` y se obtiene `unaClase`.
  2. El docente solicita iniciar la sesión con el mensaje `datosConexion = iniciarClaseEnVivo(unaClase)`. El sistema retorna `datosConexion`.

---

### CU-71: Finalizar clase en vivo
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-65: Buscar clase en vivo` y se obtiene `unaClase`.
  2. El docente solicita la finalización enviando el mensaje `finalizarClaseEnVivo(unaClase)`.

---

### CU-72: Ingresar a clase en vivo
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-26: Acceder curso` y se obtiene `unaClase`.
  2. El alumno solicita unirse a la sesión enviando el mensaje `ingresarClaseEnVivo(unaClase)`.

---

### CU-73: Generar banco de preguntas
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente solicita generar el banco enviando el mensaje `unPool = generarBancoPreguntas(unaUnidad, guion)`. El sistema retorna `unPool`.

---

### CU-74: Generar resumen de unidad
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente solicita la generación del resumen enviando `unMaterial = generarResumenUnidad(unaUnidad)`. El sistema retorna `unMaterial`.

---

### CU-75: Generar presentacion de unidad
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente solicita generar la presentación enviando `unMaterial = generarPresentacionUnidad(unaUnidad)`. El sistema retorna `unMaterial`.

---

### CU-76: Crear clon
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El docente envía la solicitud para crear el clon con el mensaje `crearClon(imagen, audio, comprobacionVoz)`.

---

### CU-77: Buscar clase con clon
* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando el mensaje `clases = buscarClasesConClon(unidad, titulo, estado)`. El sistema retorna la lista `clases`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una clase]:
     * El actor envía `unaClase = seleccionarClaseConClon(idClase, clases)` y el sistema retorna `unaClase`.

---

### CU-78: Generar clase con clon
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-19: Buscar unidad` y se obtiene `unaUnidad`.
  2. El docente genera la clase enviando el mensaje `unaClaseClon = generarClaseConClon(unaUnidad, titulo, guion)`. El sistema retorna la entidad `unaClaseClon`.

---

### CU-79: Modificar clase con clon
* **Actor:** Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-77: Buscar clase con clon` y se obtiene `unaClase`.
  2. El docente envía la modificación con el mensaje `modificarClaseConClon(unaClase, titulo, guion)`.

---

### CU-80: Dar de baja clase con clon
* **Actores:** Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-77: Buscar clase con clon` y se obtiene `unaClase`.
  2. El actor solicita la baja enviando el mensaje `darDeBajaClaseConClon(unaClase)`.

---

### CU-81: Registrarse
* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El alumno solicita el registro con `unUsuario = registrarse(nombre, apellido, correo, dni, contrasena)`. El sistema retorna `unUsuario`.
  2. El alumno confirma su cuenta enviando `validarCuenta(token)`.

---

### CU-82: Buscar usuario
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita la búsqueda con `usuarios = buscarUsuarios(nombre, apellido, correo, dni, rol)`. El sistema retorna la lista `usuarios`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un usuario]:
     * El administrador envía `unUsuario = seleccionarUsuario(idUsuario, usuarios)` y el sistema retorna `unUsuario`.

---

### CU-83: Registrar usuario
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita roles disponibles enviando `roles = buscarRoles()`. El sistema retorna `roles`.
  2. El administrador selecciona un rol con `unRol = seleccionarRol(idRol, roles)`. El sistema retorna `unRol`.
  3. El administrador registra los datos enviando `unUsuario = registrarUsuario(nombre, apellido, correo, dni, telefono, unRol)`. El sistema retorna `unUsuario`.
  4. **Bloque Opcional (`opt`)** [Si el rol es docente]:
     * **Referencia/Inclusión:** Comienza el DSS de `CU-88: Registrar docente` y se obtiene `unDocente`.

---

### CU-84: Modificar usuario
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-82: Buscar usuario` y se obtiene `unUsuario`.
  2. El administrador envía la modificación con el mensaje `modificarUsuario(unUsuario, nombre, apellido, correo, dni, telefono, imagenPerfil)`.

---

### CU-85: Dar de baja usuario
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-82: Buscar usuario` y se obtiene `unUsuario`.
  2. El administrador solicita la baja enviando el mensaje `darDeBajaUsuario(unUsuario)`.

---

### CU-86: Ver perfil
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor consulta sus datos enviando el mensaje `miPerfil = verPerfil()`. El sistema retorna `miPerfil`.

---

### CU-87: Editar perfil
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor actualiza su perfil enviando el mensaje `editarPerfil(nombre, apellido, telefono, imagenPerfil)`.

---

### CU-88: Registrar docente
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita el alta enviando `unDocente = registrarDocente(unUsuario, biografia, aniosExperiencia, matriculaCnv)`. El sistema retorna `unDocente`.
  2. **Bloque Bucle (`loop`)** [Mientras haya titulos para agregar]:
     * El administrador envía `unTitulo = agregarTitulo(unDocente, titulo, matriculaColegio)` y el sistema retorna `unTitulo`.

---

### CU-89: Modificar docente
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-82: Buscar usuario` y se obtiene `unDocente`.
  2. El administrador actualiza los datos principales enviando `modificarDocente(unDocente, biografia, aniosExperiencia, matriculaCnv, habilitado)`.
  3. El administrador consulta los títulos existentes mediante `titulos = buscarTitulos(unDocente)`. El sistema retorna `titulos`.
  4. **Bloque Bucle (`loop`)** [Mientras haya titulos para agregar, editar o eliminar]:
     * **Bloque Alternativo (`alt`):**
       * **Rama 1** [Si el actor desea agregar un titulo]:
         * Envía `unTitulo = agregarTitulo(unDocente, titulo, matriculaColegio)` $\rightarrow$ Retorna `unTitulo`.
       * **Rama 2** [Si el actor desea editar un titulo]:
         * Envía `unTitulo = seleccionarTitulo(idTitulo, titulos)` $\rightarrow$ Retorna `unTitulo`.
         * Envía `modificarTitulo(unTitulo, titulo, matriculaColegio)`.
       * **Rama 3** [Si el actor desea eliminar un titulo]:
         * Envía `unTitulo = seleccionarTitulo(idTitulo, titulos)` $\rightarrow$ Retorna `unTitulo`.
         * Envía `eliminarTitulo(unTitulo)`.

---

### CU-90: Iniciar sesion
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  * **Bloque Alternativo (`alt`):**
    * **Rama 1** [Si el actor desea ingresar con correo y contraseña]:
      * El actor envía `unaSesion = iniciarSesion(correo, contrasena)`. El sistema retorna `unaSesion`.
    * **Rama 2** [Si el actor desea ingresar con google]:
      * El actor envía `unaSesion = iniciarSesionConGoogle(tokenGoogle)`. El sistema retorna `unaSesion`.

---

### CU-91: Cerrar sesion
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor finaliza la sesión enviando el mensaje `cerrarSesion()`.

---

### CU-92: Recuperar contraseña
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor solicita la recuperación enviando `solicitarRecuperacionContrasena(correo)`.
  2. El actor define su nueva clave enviando `restablecerContrasena(token, nuevaContrasena)`.

---

### CU-93: Buscar sesion
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El actor consulta el listado con `sesiones = buscarSesiones(usuario, rangoFechas, ip, dispositivo)`. El sistema retorna la lista `sesiones`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona una sesion]:
     * El actor envía `unaSesion = seleccionarSesion(idSesion, sesiones)` y el sistema retorna `unaSesion`.

---

### CU-94: Eliminar sesion
* **Actores:** Alumno / Docente / Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. **Referencia/Inclusión:** Comienza el DSS de `CU-93: Buscar sesion` y se obtiene `unaSesion`.
  2. El actor solicita la baja enviando el mensaje `eliminarSesion(unaSesion)`.

---

### CU-95: Consultar auditoria
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita consultar los registros enviando el mensaje `registros = buscarRegistrosAuditoria(usuario, tipoAccion, entidad, rangoFechas)`. El sistema retorna la lista `registros`.

---

### CU-96: Generar informe de alumnos de un curso
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita buscar cursos con `cursos = buscarCursos()`. El sistema retorna la lista `cursos`.
  2. El administrador selecciona un curso enviando `unCurso = seleccionarCurso(idCurso, cursos)`. El sistema retorna `unCurso`.
  3. El administrador genera el reporte enviando el mensaje `unInforme = generarInformeAlumnos(unCurso, rangoFechas)`. El sistema retorna `unInforme`.

---

### CU-97: Generar informe de ingresos de un curso
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita buscar cursos con `cursos = buscarCursos()`. El sistema retorna la lista `cursos`.
  2. El administrador selecciona un curso enviando `unCurso = seleccionarCurso(idCurso, cursos)`. El sistema retorna `unCurso`.
  3. El administrador genera el reporte financiero enviando el mensaje `unInforme = generarInformeIngresos(unCurso, rangoFechas)`. El sistema retorna `unInforme`.

---

### CU-98: Consultar estadisticas
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita las métricas globales enviando el mensaje `indicadores = consultarEstadisticas()`. El sistema retorna `indicadores`.

---

### CU-99: Configurar parametros
* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
  1. El administrador solicita los parámetros del sistema con `parametros = buscarParametros()`. El sistema retorna la lista `parametros`.
  2. **Bloque Opcional (`opt`)** [Si el actor selecciona un parametro para modificarlo]:
     * El administrador envía `unParametro = seleccionarParametro(idParametro, parametros)` y el sistema retorna `unParametro`.
     * El administrador actualiza el valor enviando `modificarParametro(unParametro, valorNuevo)`.