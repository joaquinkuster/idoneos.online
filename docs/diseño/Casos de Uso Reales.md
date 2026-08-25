**CU-01: Buscar curso**

* **Actores:** Administrador / Docente
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
1. El actor solicita buscar cursos enviando el mensaje `buscarCursos(nombre, categoria, nivel, docentes, modalidades)`. El sistema retorna la lista `cursos`.
2. **Bloque Opcional (`opt`)** [Si el actor selecciona un curso]:
* El actor envía `seleccionarCurso(idCurso, cursos)` y el sistema retorna `unCurso`.





---

**CU-02: Ver mis cursos**

* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
1. El alumno consulta sus inscripciones enviando `buscarInscripcionesAlumno(nombreCurso, estadoInscripcion)`. El sistema retorna la lista `inscripciones`.
2. **Bloque Opcional (`opt`)** [Si el actor selecciona una inscripción]:
* El alumno envía `seleccionarInscripcion(idInscripcion, inscripciones)` y el sistema retorna `unaInscripcion`.





---

**CU-03: Registrar curso**

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

**CU-04: Modificar curso**

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

**CU-05: Dar de baja curso**

* **Actor:** Administrador
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
1. **Referencia/Inclusión:** Comienza el DSS de `CU-01: Buscar curso`, retornando `unCurso`.
2. El administrador solicita la baja enviando el mensaje `darDeBajaCurso(unCurso)`.



---

**CU-06: Explorar catálogo de cursos**

* **Actor:** Alumno
* **Sistema:** `:IdoneosOnline`
* **Flujo de interacción:**
1. El alumno consulta la oferta académica abierta enviando `buscarCursosAbiertos(nombre, categoria, nivel, docente, modalidad)`. El sistema retorna `cursos`.
2. **Bloque Opcional (`opt`)** [Si el actor selecciona un curso para ver su ficha]:
* El alumno envía `seleccionarCurso(idCurso, cursos)` y el sistema retorna `unCurso`.