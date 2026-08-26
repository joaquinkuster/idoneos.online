# Diagramas de Secuencia del Diseño (DSD) Actualizados - Módulo 1

---

### DSD buscarCursos(nombre, categoria, nivel, docentes, modalidades) | CU-01: Buscar curso
* **Actores:** Administrador / Docente
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `cursos` (Colección de cursos)
  * `unCurso : Curso` (Instancia de la entidad Curso)
* **Variables locales / Notas:**
  * `Lista cursosFiltrados = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El actor solicita la búsqueda enviando `cursos = buscarCursos(nombre, categoria, nivel, docentes, modalidades)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `cursosFiltrados` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya cursos activos]:
     * `:IdoneosOnline` solicita a la colección `cursos`: `unCurso = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unCurso`: `sePuedeAgregar = cumpleFiltros(nombre, categoria, nivel, docentes, modalidades)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se agrega el elemento mediante `cursosFiltrados.agregar(unCurso)`.
  4. `:IdoneosOnline` retorna la lista `cursos` al actor.

---

### DSD seleccionarCurso(idCurso, cursos) | CU-01: Buscar curso
* **Actores:** Administrador / Docente
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `cursos` (Colección de cursos)
  * `unCurso : Curso` (Instancia de la entidad Curso)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El actor envía `unCurso = seleccionarCurso(idCurso, cursos)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya cursos activos && retorno == false]:
     * `:IdoneosOnline` solicita a `cursos`: `unCurso = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unCurso`: `retorno = tieneId(idCurso)`.
  4. `:IdoneosOnline` retorna la instancia encontrada `unCurso` al actor.

---

### DSD buscarInscripcionesAlumno(nombreCurso, estadoInscripcion) | CU-02: Ver mis cursos
* **Actor:** Alumno
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `inscripciones` (Colección de inscripciones)
  * `unaInscripcion : Inscripcion` (Instancia de la entidad Inscripción)
* **Variables locales / Notas:**
  * `Lista inscripcionesFiltradas = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El alumno envía `inscripciones = buscarInscripcionesAlumno(nombreCurso, estadoInscripcion)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `inscripcionesFiltradas` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya inscripciones del alumno]:
     * `:IdoneosOnline` solicita a la colección `inscripciones`: `unaInscripcion = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaInscripcion`: `sePuedeAgregar = cumpleFiltros(unAlumno, nombreCurso, estadoInscripcion)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se agrega a la lista mediante `inscripcionesFiltradas.agregar(unaInscripcion)`.
  4. `:IdoneosOnline` retorna la lista `inscripciones` al alumno.

---

### DSD seleccionarInscripcion(idInscripcion, inscripciones) | CU-02: Ver mis cursos
* **Actor:** Alumno
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `inscripciones` (Colección de inscripciones)
  * `unaInscripcion : Inscripcion` (Instancia de la entidad Inscripción)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El alumno envía `unaInscripcion = seleccionarInscripcion(idInscripcion, inscripciones)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya inscripciones del alumno && retorno == false]:
     * `:IdoneosOnline` solicita a `inscripciones`: `unaInscripcion = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaInscripcion`: `retorno = tieneId(idInscripcion)`.
  4. `:IdoneosOnline` retorna la entidad `unaInscripcion` al alumno.

---

### DSD buscarNiveles() | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `niveles` (Colección de niveles)
* **Flujo de interacción:**
  1. El administrador envía el mensaje `niveles = buscarNiveles()` a `:IdoneosOnline`.
  2. `:IdoneosOnline` delega la consulta enviando `niveles = obtenerNiveles()` a la colección `niveles`.
  3. La colección `niveles` retorna la lista de niveles a `:IdoneosOnline`.
  4. `:IdoneosOnline` retorna los niveles al administrador.

---

### DSD seleccionarNivel(idNivel, niveles) | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `niveles` (Colección de niveles)
  * `unNivel : Nivel` (Instancia de la entidad Nivel)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El administrador envía `unNivel = seleccionarNivel(idNivel, niveles)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya niveles && retorno == false]:
     * `:IdoneosOnline` solicita a `niveles`: `unNivel = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unNivel`: `retorno = tieneId(idNivel)`.
  4. `:IdoneosOnline` retorna el objeto `unNivel` al administrador.

---

### DSD buscarModalidades() | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `modalidades` (Colección de modalidades)
* **Flujo de interacción:**
  1. El administrador solicita `listadoModalidades = buscarModalidades()` a `:IdoneosOnline`.
  2. `:IdoneosOnline` envía a la colección `modalidades`: `modalidades = obtenerModalidades()`.
  3. La colección `modalidades` retorna los elementos a `:IdoneosOnline`.
  4. `:IdoneosOnline` retorna el listado de modalidades al administrador.

---

### DSD seleccionarModalidades(idsModalidades, listadoModalidades) | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `modalidades` (Colección de modalidades)
  * `unaModalidad : Modalidad` (Instancia de la entidad Modalidad)
* **Variables locales / Notas:**
  * `Lista modalidadesSeleccionadas = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El administrador envía `modalidades = seleccionarModalidades(idsModalidades, listadoModalidades)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `modalidadesSeleccionadas` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya modalidades]:
     * `:IdoneosOnline` obtiene de la lista `modalidades`: `unaModalidad = obtenerSiguiente()`.
     * `:IdoneosOnline` valida enviando a `unaModalidad`: `sePuedeAgregar = tieneId(idsModalidades)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se agrega el elemento mediante la acción interna: `modalidadesSeleccionadas.agregar(unaModalidad)`.
  4. `:IdoneosOnline` retorna la lista filtrada `modalidades` al administrador.

---

### DSD buscarDocentes() | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `docentes` (Colección de docentes)
* **Flujo de interacción:**
  1. El administrador envía `docentes = buscarDocentes()` a `:IdoneosOnline`.
  2. `:IdoneosOnline` envía a la colección `docentes`: `docentes = obtenerDocentes()`.
  3. La colección `docentes` retorna la lista a `:IdoneosOnline`.
  4. `:IdoneosOnline` retorna los docentes al administrador.

---

### DSD seleccionarDocente(idDocente, docentes) | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `docentes` (Colección de docentes)
  * `unDocente : Docente` (Instancia de la entidad Docente)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El administrador envía `unDocenteTitular = seleccionarDocente(idDocente, docentes)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya docentes activos y habilitados && retorno == false]:
     * `:IdoneosOnline` solicita a `docentes`: `unDocente = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unDocente`: `retorno = tieneId(idDocente)`.
  4. `:IdoneosOnline` retorna la instancia `unDocenteTitular` al administrador.

---

### DSD seleccionarSupervisores(idsDocentes, docentes) | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `docentes` (Colección de docentes)
  * `unDocente : Docente` (Instancia de la entidad Docente)
* **Variables locales / Notas:**
  * `Lista supervisores = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El administrador envía `supervisores = seleccionarSupervisores(idsDocentes, docentes)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `supervisores` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya docentes activos y habilitados]:
     * `:IdoneosOnline` solicita a la colección `docentes`: `unDocente = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unDocente`: `sePuedeAgregar = tieneId(idsDocentes)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se añade mediante la acción interna: `supervisores.agregar(unDocente)`.
  4. `:IdoneosOnline` retorna la lista `supervisores` al administrador.

---

### DSD registrarCurso(nombre, descripcion, ...) | CU-03: Registrar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
  * `unDocenteTitular : Docente` (Instancia de la entidad Docente)
  * `ayudantes` (Colección de docentes ayudantes)
  * `unDocente : Docente` (Instancia de la entidad Docente ayudante)
  * `unCurso : Curso` (Instancia de la entidad Curso)
* **Variables locales / Notas:**
  * `boolean categoriaActiva`
  * `boolean docenteTitularActivo`
  * `boolean ayudanteActivo = true`
* **Flujo de interacción:**
  1. El administrador envía la solicitud inicial: `unCurso = registrarCurso(nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)` a `:IdoneosOnline`.
  2. Se declaran e inicializan las variables locales `categoriaActiva`, `docenteTitularActivo` y `ayudanteActivo = true`.
  3. `:IdoneosOnline` consulta el estado de la categoría enviando a `unaCategoria`: `categoriaActiva = estaActiva()`.
  4. **Bloque Opcional (`opt`)** [!categoriaActiva]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoria no se encuentra activa")`.
  5. `:IdoneosOnline` consulta el estado del titular enviando a `unDocenteTitular`: `docenteTitularActivo = estaActivo()`.
  6. **Bloque Opcional (`opt`)** [!docenteTitularActivo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El docente titular no se encuentra activo o habilitado")`.
  7. **Bloque Bucle (`loop`)** [Mientras haya ayudantes && ayudanteActivo]:
     * `:IdoneosOnline` solicita a la colección `ayudantes`: `unDocente = obtenerSiguiente()`.
     * `:IdoneosOnline` valida el estado enviando a `unDocente`: `ayudanteActivo = estaActivo()`.
  8. **Bloque Opcional (`opt`)** [!ayudanteActivo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("Un docente ayudante no se encuentra activo o habilitado")`.
  9. **Bloque Opcional (`opt`)** [ayudantes.contiene(unDocenteTitular)]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El docente titular no puede ser también supervisor del curso")`.
  10. `:IdoneosOnline` crea la instancia enviando al constructor: `Curso unCurso = registrar(nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)`.
  11. `:IdoneosOnline` registra el curso en la colección enviando: `agregarCurso(unCurso)`.
  12. `:IdoneosOnline` asocia el curso al docente titular mediante: `agregarCursoTitular(unCurso)`.
  13. **Bloque Bucle (`loop`)** [Mientras haya ayudantes]:
      * `:IdoneosOnline` solicita a la colección `ayudantes`: `unDocente = obtenerSiguiente()`.
      * `:IdoneosOnline` asocia el curso a cada ayudante enviando: `agregarCursoAyudante(unCurso)`.
  14. `:IdoneosOnline` retorna la instancia creada `unCurso` al administrador.

---

### DSD modificarCurso(unCurso, nombre, descripcion, ...) | CU-04: Modificar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unCurso : Curso` (Instancia de la entidad Curso)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
  * `unDocenteTitular : Docente` (Instancia de la entidad Docente Titular)
  * `ayudantes` (Colección de docentes ayudantes)
  * `unDocente : Docente` (Instancia de la entidad Docente ayudante)
  * `inscripciones` (Colección de inscripciones)
  * `unaInscripcion : Inscripcion` (Instancia de la entidad Inscripción)
* **Variables locales / Notas:**
  * En `:IdoneosOnline`:
    * `boolean cursoActivo`
    * `boolean categoriaActiva`
    * `boolean docenteTitularActivo`
    * `boolean ayudanteActivo = true`
    * `boolean sePuedeModificar`
  * En `unCurso`:
    * `boolean seEncontroInscripcionActiva = false`
* **Flujo de interacción:**
  1. El administrador envía el mensaje: `modificarCurso(unCurso, nombre, descripcion, precio, imagen, unaCategoria, unNivel, emiteCertificado, modalidades, unDocenteTitular, ayudantes)` a `:IdoneosOnline`.
  2. Se declaran/inicializan las variables locales en `:IdoneosOnline`.
  3. `:IdoneosOnline` consulta el estado enviando a `unCurso`: `cursoActivo = estaActivo()`.
  4. **Bloque Opcional (`opt`)** [!cursoActivo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El curso no se encuentra activo")`.
  5. `:IdoneosOnline` consulta a `unCurso`: `sePuedeModificar = !tieneInscripcionesActivas()`.
  6. Dentro de `unCurso`, se inicializa `seEncontroInscripcionActiva = false`.
  7. **Bloque Bucle (`loop`)** [Mientras haya inscripciones activas && seEncontroInscripcionActiva == false]:
     * `unCurso` solicita a la colección `inscripciones`: `Inscripcion unaInscripcion = obtenerSiguiente()`.
     * `unCurso` consulta a `unaInscripcion`: `seEncontroInscripcionActiva = perteneceACurso(unCurso)`.
  8. `unCurso` retorna `sePuedeModificar` a `:IdoneosOnline`.
  9. **Bloque Alternativo / Opcional (`opt` / `1..*`)** [!sePuedeModificar]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El curso no puede modificarse mientras existan inscripciones activas que la utilicen")`.
  10. `:IdoneosOnline` consulta a `unaCategoria`: `categoriaActiva = estaActiva()`.
  11. **Bloque Opcional (`opt`)** [!categoriaActiva]:
      * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoria no se encuentra activa")`.
  12. `:IdoneosOnline` consulta a `unDocenteTitular`: `docenteTitularActivo = estaActivo()`.
  13. **Bloque Opcional (`opt`)** [!docenteTitularActivo]:
      * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El docente titular no se encuentra activo o habilitado")`.
  14. **Bloque Bucle (`loop`)** [Mientras haya ayudantes]:
      * `:IdoneosOnline` obtiene de la colección `ayudantes`: `unDocente = obtenerSiguiente()`.
      * `:IdoneosOnline` consulta a `unDocente`: `ayudanteActivo = estaActivo()`.
  15. **Bloque Opcional (`opt`)** [!ayudanteActivo]:
      * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("Un docente ayudante no se encuentra activo o habilitado")`.
  16. **Bloque Opcional (`opt`)** [ayudantes.contiene(unDocenteTitular)]:
      * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El docente titular no puede ser también supervisor del curso")`.
  17. Finalmente, `:IdoneosOnline` actualiza los atributos y relaciones de `unCurso` enviando sucesivamente:
      * `setNombre(nombre)`
      * `setDescripcion(descripcion)`
      * `setPrecio(precio)`
      * `setImagen(imagen)`
      * `setNivel(unNivel)`
      * `setEmiteCertificado(emiteCertificado)`
      * `cambiarCategoria(unaCategoria)`
      * `cambiarDocenteTitular(unDocenteTitular)`
      * `cambiarAyudantes(ayudantes)`
      * `cambiarModalidades(modalidades)`

---

### DSD modificarCursoPrecioImagen(unCurso, precio, imagen) | CU-04: Modificar curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unCurso : Curso` (Instancia de la entidad Curso)
* **Flujo de interacción:**
  1. El administrador envía el mensaje: `modificarCursoPrecioImagen(unCurso, precio, imagen)` a `:IdoneosOnline`.
  2. `:IdoneosOnline` consulta enviando a `unCurso`: `cursoActivo = estaActivo()`.
  3. **Bloque Opcional (`opt`)** [!cursoActivo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El curso no se encuentra activo")`.
  4. `:IdoneosOnline` actualiza los datos enviando a `unCurso`:
     * `setPrecio(precio)`
     * `setImagen(imagen)`

---

### DSD darDeBajaCurso(unCurso) | CU-05: Dar de baja curso
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unCurso : Curso` (Instancia de la entidad Curso)
* **Variables locales / Notas:**
  * `boolean estaActivo`
  * `boolean sePuedeDarDeBaja`
* **Flujo de interacción:**
  1. El administrador envía el mensaje: `darDeBajaCurso(unCurso)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `estaActivo` y `sePuedeDarDeBaja`.
  3. `:IdoneosOnline` consulta enviando a `unCurso`: `estaActivo = estaActivo()`.
  4. **Bloque Alternativo (`alt`)** [!estaActivo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El curso no se encuentra activo")`.
  5. `:IdoneosOnline` verifica enviando a `unCurso`: `sePuedeDarDeBaja = !tieneProgramasActivos()`.
  6. **Bloque Alternativo (`alt`)** [!sePuedeDarDeBaja]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El curso no puede darse de baja mientras tenga programas activos asociados")`.
  7. `:IdoneosOnline` aplica la baja lógica enviando a `unCurso`: `setBaja(true)`.

---

### DSD buscarCursosAbiertos(nombre, categoria, nivel, docente, modalidad) | CU-06: Explorar catalogo de cursos
* **Actor:** Alumno
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `cursos` (Colección de cursos)
  * `unCurso : Curso` (Instancia de la entidad Curso)
  * `cohortes` (Colección de cohortes)
  * `unaCohorte : Cohorte` (Instancia de la entidad Cohorte)
* **Variables locales / Notas:**
  * En `:IdoneosOnline`:
    * `Lista cursosFiltrados = nueva Lista()`
    * `boolean sePuedeAgregar`
    * `boolean estaPublicado`
  * En `unCurso`:
    * `boolean seEncontroCohorteAbierta = false`
    * `boolean estaAbierta`
* **Flujo de interacción:**
  1. El alumno envía el mensaje inicial `cursos = buscarCursosAbiertos(nombre, categoria, nivel, docente, modalidad)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales en `:IdoneosOnline` (`cursosFiltrados`, `sePuedeAgregar`, `estaPublicado`).
  3. **Bloque Bucle (`loop`)** [Mientras haya cursos activos]:
     * `:IdoneosOnline` solicita a la colección `cursos`: `unCurso = obtenerSiguiente()`.
     * `:IdoneosOnline` valida los filtros enviando a `unCurso`: `sePuedeAgregar = cumpleFiltros(nombre, categoria, nivel, docentes, modalidades)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * `:IdoneosOnline` consulta a `unCurso`: `estaPublicado = tieneCohortesAbiertas()`.
       * Dentro de `unCurso`, se inicializan `seEncontroCohorteAbierta = false` y `estaAbierta`.
       * **Bloque Bucle interno (`loop`)** [Mientras haya cohortes activas && seEncontroCohorteAbierta == false]:
         * `unCurso` obtiene la siguiente cohorte desde `cohortes`: `Cohorte unaCohorte = obtenerSiguiente()`.
         * `unCurso` consulta a `unaCohorte`: `estaAbierta = permiteInscripciones()`.
         * **Bloque Alternativo interno (`alt`)** [estaAbierta]:
           * `unCurso` verifica la pertenencia consultando a `unaCohorte`: `seEncontroCohorteAbierta = perteneceACurso(unCurso)`.
       * `unCurso` responde el valor booleano resultante `estaPublicado` a `:IdoneosOnline`.
       * **Bloque Alternativo (`alt`)** [estaPublicado]:
         * `:IdoneosOnline` ejecuta la acción interna: `cursosFiltrados.agregar(unCurso)`.
  4. `:IdoneosOnline` retorna la lista resultante `cursos` al alumno.

---

### DSD buscarCategorias(nombre) | CU-07: Buscar categoria
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `categorias` (Colección de categorías)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
* **Variables locales / Notas:**
  * `Lista categoriasFiltradas = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El administrador envía el mensaje inicial `categorias = buscarCategorias(nombre)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `categoriasFiltradas` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya categorías activas]:
     * `:IdoneosOnline` solicita a la colección `categorias`: `unaCategoria = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaCategoria`: `sePuedeAgregar = cumpleFiltros(nombre)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se agrega la entidad mediante la acción interna: `categoriasFiltradas.agregar(unaCategoria)`.
  4. `:IdoneosOnline` retorna la lista resultante `categorias` al administrador.

---

### DSD seleccionarCategoria(idCategoria, categorias) | CU-07: Buscar categoria
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `categorias` (Colección de categorías)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El administrador envía: `unaCategoria = seleccionarCategoria(idCategoria, categorias)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya categorías activas && retorno == false]:
     * `:IdoneosOnline` solicita a la colección `categorias`: `unaCategoria = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaCategoria`: `retorno = tieneId(idCategoria)`.
  4. `:IdoneosOnline` retorna la entidad coincidente `unaCategoria` al administrador.

---

### DSD registrarCategoria(nombre, descripcion) | CU-08: Registrar categoria
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `categorias` (Colección de categorías)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
* **Variables locales / Notas:**
  * `boolean nombreDuplicado = false`
* **Flujo de interacción:**
  1. El administrador envía el mensaje: `unaCategoria = registrarCategoria(nombre, descripcion)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `nombreDuplicado = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya categorías activas && nombreDuplicado == false]:
     * `:IdoneosOnline` solicita a la colección `categorias`: `Categoria unaCategoria = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaCategoria`: `nombreDuplicado = tieneMismoNombre(nombre)`.
  4. **Bloque Alternativo (`alt`)** [nombre.isEmpty() || nombreDuplicado]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El nombre no puede estar vacío ni coincidir con el de otra categoría activa")`.
  5. `:IdoneosOnline` instancia la nueva categoría mediante el constructor: `Categoria unaCategoria = registrar(nombre, descripcion, habilitada=true)`.
  6. `:IdoneosOnline` retorna la entidad creada `unaCategoria` al administrador.

---

### DSD modificarCategoria(unaCategoria, nombre, descripcion) | CU-09: Modificar categoria
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
  * `inscripciones` (Colección de inscripciones)
  * `unaInscripcion : Inscripcion` (Instancia de la entidad Inscripción)
* **Variables locales / Notas:**
  * En `:IdoneosOnline`:
    * `boolean estaActiva`
    * `boolean sePuedeModificar`
    * `boolean nombreDuplicado`
  * En `unaCategoria`:
    * `boolean seEncontroInscripcionActiva = false`
* **Flujo de interacción:**
  1. El administrador envía: `modificarCategoria(unaCategoria, nombre, descripcion)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales en el controlador.
  3. `:IdoneosOnline` consulta enviando a `unaCategoria`: `estaActiva = estaActiva()`.
  4. **Bloque Opcional (`opt`)** [!estaActiva]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoría no se encuentra activa")`.
  5. `:IdoneosOnline` consulta a `unaCategoria`: `nombreDuplicado = tieneMismoNombre(nombre)`.
  6. **Bloque Opcional (`opt`)** [nombre.isEmpty() || nombreDuplicado]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("El nombre no puede estar vacío ni coincidir con el de otra categoría activa")`.
  7. `:IdoneosOnline` consulta a `unaCategoria`: `sePuedeModificar = !tieneInscripcionesActivas()`.
  8. Dentro de `unaCategoria`, se inicializa `seEncontroInscripcionActiva = false`.
  9. **Bloque Bucle (`loop`)** [Mientras haya inscripciones activas && seEncontroInscripcionActiva == false]:
     * `unaCategoria` solicita a `inscripciones`: `Inscripcion unaInscripcion = obtenerSiguiente()`.
     * `unaCategoria` consulta a `unaInscripcion`: `seEncontroInscripcionActiva = perteneceACategoria(unaCategoria)`.
  10. `unaCategoria` responde `sePuedeModificar` a `:IdoneosOnline`.
  11. **Bloque Alternativo (`alt`):**
      * **Rama Principal** [sePuedeModificar]:
        * `:IdoneosOnline` envía a `unaCategoria`: `setNombre(nombre)`.
        * `:IdoneosOnline` envía a `unaCategoria`: `setDescripcion(descripcion)`.
        * `:IdoneosOnline` envía a `unaCategoria`: `setUltimaModificacion(fechaActual)`.
      * **Rama Alternativa / Else (`1..*`)** [!sePuedeModificar]:
        * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoría no puede modificarse mientras existan inscripciones activas que la utilicen")`.

---

### DSD darDeBajaCategoria(unaCategoria) | CU-10: Dar de baja categoria
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unaCategoria : Categoria` (Instancia de la entidad Categoria)
* **Variables locales / Notas:**
  * `boolean estaActiva`
  * `boolean sePuedeDarDeBaja`
* **Flujo de interacción:**
  1. El administrador envía: `darDeBajaCategoria(unaCategoria)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `estaActiva` y `sePuedeDarDeBaja`.
  3. `:IdoneosOnline` consulta enviando a `unaCategoria`: `estaActiva = estaActiva()`.
  4. **Bloque Alternativo (`alt`)** [!estaActiva]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoría no se encuentra activa")`.
  5. `:IdoneosOnline` verifica si tiene cursos enviando a `unaCategoria`: `sePuedeDarDeBaja = !tieneCursosActivos()`.
  6. **Bloque Alternativo (`alt`):**
     * **Rama Principal** [sePuedeDarDeBaja]:
       * `:IdoneosOnline` envía a `unaCategoria`: `setBaja(true)`.
     * **Rama Alternativa / Else** [!sePuedeDarDeBaja]:
       * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La categoría no puede darse de baja mientras existan cursos activos que la utilicen")`.

---

### DSD buscarCohortes(programa, estado, fechaInicioInscripcion, fechaFinInscripcion) | CU-11: Buscar cohorte
* **Actores:** Administrador / Docente
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `cohortes` (Colección de cohortes)
  * `unaCohorte : Cohorte` (Instancia de la entidad Cohorte)
* **Variables locales / Notas:**
  * `Lista cohortesFiltradas = nueva Lista()`
  * `boolean sePuedeAgregar`
* **Flujo de interacción:**
  1. El actor solicita: `cohortes = buscarCohortes(programa, estado, fechaInicioInscripcion, fechaFinInscripcion)` a `:IdoneosOnline`.
  2. Se inicializan las variables locales `cohortesFiltradas` y `sePuedeAgregar`.
  3. **Bloque Bucle (`loop`)** [Mientras haya cohortes activas]:
     * `:IdoneosOnline` solicita a la colección `cohortes`: `unaCohorte = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaCohorte`: `sePuedeAgregar = cumpleFiltros(programa, estado, fechaInicioInscripcion, fechaFinInscripcion)`.
     * **Bloque Alternativo (`alt`)** [sePuedeAgregar]:
       * Se añade la entidad mediante la acción interna: `cohortesFiltradas.agregar(unaCohorte)`.
  4. `:IdoneosOnline` retorna la lista `cohortes` al actor.

---

### DSD seleccionarCohorte(idCohorte, cohortes) | CU-11: Buscar cohorte
* **Actores:** Administrador / Docente
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `cohortes` (Colección de cohortes)
  * `unaCohorte : Cohorte` (Instancia de la entidad Cohorte)
* **Variables locales / Notas:**
  * `boolean retorno = false`
* **Flujo de interacción:**
  1. El actor envía: `unaCohorte = seleccionarCohorte(idCohorte, cohortes)` a `:IdoneosOnline`.
  2. Se inicializa la variable local `retorno = false`.
  3. **Bloque Bucle (`loop`)** [Mientras haya cohortes activas && retorno == false]:
     * `:IdoneosOnline` solicita a la colección `cohortes`: `unaCohorte = obtenerSiguiente()`.
     * `:IdoneosOnline` consulta a `unaCohorte`: `retorno = tieneId(idCohorte)`.
  4. `:IdoneosOnline` retorna la instancia encontrada `unaCohorte` al actor.

---

### DSD registrarCohorte(unPrograma, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado) | CU-12: Registrar cohorte
* **Actor:** Administrador
* **Objetos / Instancias participantes:**
  * `:IdoneosOnline` (Controlador / Sistema)
  * `unPrograma : Programa` (Instancia de la entidad Programa)
  * `unaCohorte : Cohorte` (Instancia de la entidad Cohorte)
* **Flujo de interacción:**
  1. El administrador envía la solicitud para crear la cohorte enviando: `unaCohorte = registrarCohorte(unPrograma, fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)` a `:IdoneosOnline`.
  2. `:IdoneosOnline` consulta enviando a `unPrograma`: `activo = estaActivo()`.
  3. **Bloque Opcional (`opt`)** [!activo]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("unPrograma no se encuentra activo")`.
  4. `:IdoneosOnline` valida contenidos enviando a `unPrograma`: `cumpleMinimoUnidades = tieneMinimoUnidadesConMaterialPublicado()`.
  5. **Bloque Opcional (`opt`)** [!cumpleMinimoUnidades]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("unPrograma no tiene el mínimo de unidades establecido con material publicado en su cronograma")`.
  6. `:IdoneosOnline` consulta la duración total enviando a `unPrograma`: `duracionCronograma = obtenerDuracionTotalCronograma()`.
  7. **Bloque Opcional (`opt`)** [semanasAcceso < duracionCronograma]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("Las semanasAcceso es menor a la duración total del cronograma de unPrograma")`.
  8. **Bloque Opcional (`opt`)** [!fechaFinInscripcion.esPosterior(fechaInicioInscripcion) || !fechaFinDictado.esPosterior(fechaInicioDictado)]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La fechaFinInscripcion no es posterior a fechaInicioInscripcion, o fechaFinDictado no es posterior a fechaInicioDictado")`.
  9. **Bloque Opcional (`opt`)** [fechaInicioDictado.esAnterior(fechaFinInscripcion)]:
     * `:IdoneosOnline` retorna una excepción al administrador: `Excepcion("La fechaInicioDictado es anterior a fechaFinInscripcion")`.
  10. `:IdoneosOnline` crea la nueva cohorte invocando al constructor: `unaCohorte = registrar(fechaInicioInscripcion, fechaFinInscripcion, cupoMaximo, semanasAcceso, fechaInicioDictado, fechaFinDictado)`.
  11. `:IdoneosOnline` vincula la cohorte enviando a `unPrograma`: `agregarCohorte(unaCohorte)`.
  12. `:IdoneosOnline` retorna la instancia creada `unaCohorte` al administrador.