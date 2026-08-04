# Casos de Uso Extendidos

En esta sección se detallan los 85 casos de uso extendidos del Sistema Idóneos Online, organizados según los diez módulos definidos en la Nota de Presentación (seis funcionales y cuatro no funcionales). Cada caso de uso se referencia mediante un código correlativo (CU-01 a CU-85).

---

## MOD-F-01: Módulo de Cursos

### CU-01
**Buscar curso**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Administrador o al Docente titular/supervisor buscar uno o más cursos registrados en el sistema, con fines de gestión.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - Existe al menos un curso registrado previamente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar uno o más cursos. |
| 2    | El sistema solicita los criterios de búsqueda: nombre, categoría, docente titular, modalidad y estado (Publicado / No publicado / Baja). |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra los cursos que coincidan con los criterios ingresados. Si el actor es Docente, el sistema restringe el resultado a los cursos en los que participa como titular o supervisor. |
| 5    | El sistema lista los cursos filtrados. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más cursos que cumplen con los criterios de búsqueda, junto con su categoría, modalidades, docente titular, docente supervisor y estado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta frecuentemente para la gestión diaria del catálogo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La vista varía según el rol del actor: el Administrador visualiza todos los cursos; el Docente visualiza únicamente los cursos en los que participa.

---

### CU-02
**Registrar curso**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador registrar un nuevo curso, definiendo su información comercial, académica y de acceso.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - Existe al menos una categoría activa.
  - Existe al menos un docente habilitado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar un nuevo curso. |
| 2    | El sistema solicita: nombre, descripción, precio, imagen de portada (opcional), categoría, docente titular, docente supervisor (opcional), modalidades de dictado, fecha de inicio y fin de inscripción, y meses de acceso al contenido. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios (nombre, descripción, precio, categoría, docente titular, al menos una modalidad, fechas de inscripción y meses de acceso). |
| 5    | El sistema valida que el docente indicado como titular se encuentre habilitado. |
| 6    | El sistema valida que el precio ingresado sea mayor o igual a cero. |
| 7    | El sistema valida que la fecha de fin de inscripción sea posterior a la fecha de inicio de inscripción. |
| 8    | El sistema valida que los meses de acceso sean un valor entero mayor a cero. |
| 9    | El sistema registra el curso en estado no publicado. |
| 10   | El sistema informa el éxito del registro. |
| 11   | Fin del caso de uso. |

- **Postcondición(es)**
  - El curso queda registrado y no publicado.
  - La fecha de creación refleja el momento del alta.
  - Las modalidades de dictado indicadas quedan asociadas al curso.
  - El docente titular, y el supervisor si corresponde, quedan asociados al curso.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3. |
| 5    | Si el docente indicado como titular no se encuentra habilitado, el sistema informa el error y vuelve al paso 3. |
| 6    | Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3. |
| 7    | Si la fecha de fin de inscripción no es posterior a la de inicio, el sistema informa el error y vuelve al paso 3. |
| 8    | Si los meses de acceso ingresados no son un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — ocurre cada vez que se incorpora un nuevo curso al catálogo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El sistema valida que exista exactamente un docente titular por curso; el docente supervisor es opcional.
  - El curso se publica en el catálogo mediante CU-03: Modificar curso, una vez cargado su contenido.

---

### CU-03
**Modificar curso**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador modificar los datos de un curso registrado, incluyendo su publicación en el catálogo una vez que su contenido está cargado.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El curso existe en el sistema y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el curso a modificar (ver CU-01: Buscar curso). |
| 2    | El sistema muestra los datos actuales del curso. |
| 3    | El actor modifica los datos que desea (nombre, descripción, precio, imagen, categoría, docente titular, docente supervisor, modalidades, fechas de inscripción, meses de acceso, estado de publicación). |
| 4    | El sistema valida que se mantengan completos los campos obligatorios (nombre, descripción, precio, categoría, docente titular, al menos una modalidad, fechas de inscripción y meses de acceso). |
| 5    | Si se modificó el docente titular, el sistema valida que se encuentre habilitado. |
| 6    | El sistema valida que el precio ingresado sea mayor o igual a cero. |
| 7    | El sistema valida que la fecha de fin de inscripción sea posterior a la de inicio. |
| 8    | El sistema valida que los meses de acceso sean un valor entero mayor a cero. |
| 9    | El sistema valida que si se marca el curso como publicado, exista al menos una unidad con material publicado. |
| 10   | El sistema actualiza los datos del curso. |
| 11   | El sistema informa el éxito de la modificación. |
| 12   | Fin del caso de uso. |

- **Postcondición(es)**
  - El curso queda actualizado con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
  - Las modalidades de dictado quedan actualizadas, si fueron modificadas.
  - El docente titular, y el supervisor si corresponde, quedan actualizados, si fueron modificados.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el docente indicado como titular no se encuentra habilitado, el sistema informa el error y vuelve al paso 3. |
| 6    | Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3. |
| 7    | Si la fecha de fin de inscripción no es posterior a la de inicio, el sistema informa el error y vuelve al paso 3. |
| 8    | Si los meses de acceso ingresados no son un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 9    | Si se intenta publicar un curso sin al menos una unidad con material publicado, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — se usa para actualizar datos del curso y para publicarlo una vez cargado.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El campo de estado de publicación es el que habilita o retira un curso del catálogo público (CU-05: Explorar catálogo de cursos).

---

### CU-04
**Eliminar curso**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja un curso. Si el curso posee alumnos con inscripción vigente, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El curso existe en el sistema y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el curso a dar de baja (ver CU-01: Buscar curso). |
| 2    | El sistema verifica que no existan inscripciones vigentes asociadas al curso. |
| 3    | El actor confirma la baja. |
| 4    | El sistema marca el curso como dado de baja y lo retira del catálogo público. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - El curso queda en baja y deja de ser visible en el catálogo público.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el curso posee alumnos con inscripción vigente, el sistema informa la dependencia y no permite la baja. |
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — ocurre cuando un curso deja de ofrecerse definitivamente.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no elimina el registro físicamente; únicamente lo marca como inactivo para mantener la trazabilidad histórica.

---

### CU-05
**Explorar catálogo de cursos**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno, con o sin sesión iniciada, explorar el catálogo público de cursos publicados y consultar la ficha de un curso específico (temática, docente, modalidades y contenido gratuito de muestra) antes de decidir inscribirse.
- **Precondición(es)**
  - Existe al menos un curso publicado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor accede al catálogo público de cursos. |
| 2    | El sistema lista los cursos publicados, pudiendo filtrar por categoría o modalidad. |
| 3    | El actor selecciona un curso para ver su ficha. |
| 4    | El sistema muestra el detalle público del curso: descripción, docente titular, docente supervisor, modalidades, precio, período de inscripción y el material marcado como gratuito, si existe. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de cursos publicados y, si corresponde, la ficha pública del curso seleccionado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — es la puerta de entrada de cualquier interesado en inscribirse.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No requiere sesión iniciada. El material marcado como gratuito funciona como gancho comercial para atraer nuevos alumnos, según lo relevado con el cliente.

---

### CU-06
**Buscar categoría**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador buscar una o más categorías temáticas registradas en el sistema.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - Existe al menos una categoría registrada previamente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar una o más categorías. |
| 2    | El sistema solicita el criterio de búsqueda: nombre. |
| 3    | El actor ingresa el criterio que desea. |
| 4    | El sistema recupera y filtra las categorías que coincidan con el criterio ingresado. |
| 5    | El sistema lista las categorías filtradas. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan una o más categorías que cumplen con el criterio de búsqueda.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Baja — se consulta al momento de gestionar el catálogo de categorías.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-07
**Registrar categoría**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador registrar una nueva categoría temática para clasificar los cursos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar una nueva categoría. |
| 2    | El sistema solicita: nombre y descripción (opcional). |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que el nombre haya sido completado y que no exista otra categoría activa con el mismo nombre. |
| 5    | El sistema registra la categoría en estado activo. |
| 6    | El sistema informa el éxito del registro. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La categoría queda registrada en estado activo.
  - La fecha de creación refleja el momento del alta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el nombre no fue completado, el sistema informa el error y vuelve al paso 3. |
| 4    | Si ya existe una categoría activa con el mismo nombre, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Muy baja — el catálogo de categorías se define una vez y varía poco.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-08
**Modificar categoría**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador modificar el nombre y la descripción de una categoría registrada.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - La categoría existe en el sistema y se encuentra en estado activo.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar (ver CU-06: Buscar categoría). |
| 2    | El sistema muestra los datos actuales de la categoría. |
| 3    | El actor modifica el nombre o la descripción. |
| 4    | El sistema valida que el nombre no quede vacío y que no coincida con el de otra categoría activa. |
| 5    | El sistema actualiza los datos de la categoría. |
| 6    | El sistema informa el éxito de la modificación. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La categoría queda actualizada con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el nombre queda vacío, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el nombre coincide con el de otra categoría activa, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Muy baja — se usa esporádicamente para corregir datos de una categoría.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-09
**Eliminar categoría**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja una categoría activa. Si la categoría posee cursos asociados que no están en baja, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - La categoría existe en el sistema y se encuentra en estado activo.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la categoría a dar de baja (ver CU-06: Buscar categoría). |
| 2    | El sistema verifica que no existan cursos activos asociados a la categoría. |
| 3    | El actor confirma la baja. |
| 4    | El sistema marca la categoría como dada de baja. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - La categoría queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si la categoría posee cursos activos asociados, el sistema informa la dependencia y no permite la baja. |
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Muy baja — ocurre cuando una temática deja de ofrecerse.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no elimina el registro físicamente; únicamente lo marca como inactivo.

---

### CU-10
**Buscar unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar las unidades que componen un curso, con fines de gestión de su estructura y contenido.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El curso existe y posee al menos una unidad registrada.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar las unidades de un curso. |
| 2    | El sistema solicita el curso sobre el que se desea consultar. |
| 3    | El actor selecciona el curso. |
| 4    | El sistema recupera y lista las unidades del curso, ordenadas por su número de orden. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de unidades del curso seleccionado, con su título, número de orden y cantidad de material cargado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta cada vez que se gestiona el contenido de un curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-11
**Registrar unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador registrar una nueva unidad dentro de un curso, definiendo su posición en el avance secuencial.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El curso existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar una nueva unidad para un curso. |
| 2    | El sistema solicita: título, descripción y número de orden. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que el título y el número de orden hayan sido completados. |
| 5    | El sistema valida que el número de orden sea un valor entero mayor a cero y que no esté ya utilizado por otra unidad del mismo curso. |
| 6    | El sistema registra la unidad. |
| 7    | El sistema informa el éxito del registro. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La unidad queda registrada y asociada al curso.
  - La fecha de creación refleja el momento del alta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el título o el número de orden no fueron completados, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el número de orden ingresado no es un entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el número de orden ya está utilizado por otra unidad del curso, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — ocurre al estructurar el contenido de cada curso nuevo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El curso debe contar con un mínimo de 10 unidades para poder publicarse, según lo relevado con el cliente; esta validación se controla en CU-03: Modificar curso al momento de la publicación.

---

### CU-12
**Modificar unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador modificar el título, la descripción o el número de orden de una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la unidad a modificar (ver CU-10: Buscar unidad). |
| 2    | El sistema muestra los datos actuales de la unidad. |
| 3    | El actor modifica los datos que desea. |
| 4    | El sistema valida que el título y el número de orden no queden vacíos. |
| 5    | El sistema valida que el número de orden sea un valor entero mayor a cero. |
| 6    | El sistema valida que, si se modificó el número de orden, no coincida con el de otra unidad del mismo curso. |
| 7    | El sistema actualiza los datos de la unidad. |
| 8    | El sistema informa el éxito de la modificación. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - La unidad queda actualizada con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el título o el número de orden quedan vacíos, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el número de orden ingresado no es un entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 6    | Si el número de orden coincide con el de otra unidad del curso, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para reordenar o corregir unidades ya cargadas.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-13
**Eliminar unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador dar de baja una unidad. Si algún alumno ya registra un intento de autoevaluación aprobado en esa unidad, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la unidad a dar de baja (ver CU-10: Buscar unidad). |
| 2    | El sistema verifica que ningún alumno registre un intento de autoevaluación aprobado en esa unidad. |
| 3    | El actor confirma la baja. |
| 4    | El sistema marca la unidad como dada de baja. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - La unidad queda en estado de baja y deja de estar disponible para los alumnos.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si algún alumno ya registra un intento de autoevaluación aprobado en esa unidad, el sistema informa la dependencia y no permite la baja. |
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — se usa cuando el docente decide reorganizar o eliminar contenido de un curso aún no cursado.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no elimina el registro físicamente; únicamente lo marca como inactivo, preservando la trazabilidad del contenido ya cursado.

---

### CU-14
**Ver contenido de unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno acceder al contenido de una unidad de un curso en el que está inscripto: su material publicado (grabación, bibliografía, presentación o resumen), los términos de su glosario y el acceso al foro de consultas de esa unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El alumno posee una inscripción vigente al curso al que pertenece la unidad.
  - La unidad se encuentra habilitada según el avance secuencial del alumno (la autoevaluación de la unidad anterior fue aprobada, o es la primera unidad del curso).
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el alumno selecciona una unidad del curso en el que está inscripto. |
| 2    | El sistema verifica que la unidad se encuentre habilitada para el alumno según su avance secuencial. |
| 3    | El sistema recupera el material publicado de la unidad, sus términos de glosario y las consultas del foro asociadas. |
| 4    | El sistema muestra el contenido de la unidad al alumno. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el material publicado de la unidad (grabación, bibliografía, presentación o resumen), sus términos de glosario y el acceso a las consultas del foro.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si la unidad todavía no está habilitada para el alumno, el sistema informa que debe aprobar primero la autoevaluación de la unidad anterior y no muestra el contenido. |

- **Frecuencia**
  - Alta — es la acción central del cursado, se repite en cada unidad de cada curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El material que se muestra excluye el que el Administrador o el Docente mantienen sin publicar (por ejemplo, contenido generado por IA pendiente de revisión).

---

### CU-15
**Buscar material**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar el material (grabaciones, bibliografía, presentaciones y resúmenes) cargado en una unidad, con fines de gestión.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y posee al menos un material cargado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar el material de una unidad. |
| 2    | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el tipo de material. |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y lista el material que coincide con los criterios, incluyendo el no publicado. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el material de la unidad, indicando su tipo, título, si fue generado por IA y su estado de publicación.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta cada vez que se gestiona el contenido de una unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - A diferencia de CU-14: Ver contenido de unidad, este CU también recupera el material no publicado, ya que su fin es la gestión y no el consumo por parte del alumno.

---

### CU-16
**Subir material**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador cargar manualmente un material (grabación, bibliografía o presentación) en una unidad. Cada tipo de material solicita datos específicos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita subir un nuevo material a una unidad. |
| 2    | El sistema solicita el tipo de material (Grabación, Bibliografía o Presentación) y el título. |
| 3    | El actor selecciona el tipo e ingresa el título. |
| 4    | Según el tipo elegido, el sistema solicita: el archivo de video (Grabación); el archivo o enlace externo y el autor (Bibliografía); o el archivo de la presentación (Presentación). |
| 5    | El actor ingresa los datos solicitados. |
| 6    | El sistema valida que se hayan completado el título, el tipo y los datos obligatorios según el tipo seleccionado. |
| 7    | El sistema registra el material en estado no publicado. |
| 8    | El sistema informa el éxito de la carga. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - El material queda registrado, asociado a la unidad, en estado no publicado.
  - La fecha de creación refleja el momento de la carga.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 6    | Si no se completó el título o alguno de los datos obligatorios según el tipo de material, el sistema informa el error y vuelve al paso 5. |

- **Frecuencia**
  - Alta — ocurre por cada material cargado de cada unidad de cada curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El material de tipo Grabación también puede originarse automáticamente desde CU-61: Finalizar clase en vivo o CU-55: Generar clase con Clon IA; el material de tipo Presentación y Resumen también puede generarse desde el Módulo de Generación de Contenido con IA. Este CU cubre específicamente la carga manual por parte del docente.

---

### CU-17
**Modificar material**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador modificar el título de un material y, en particular, su estado de publicación para habilitarlo u ocultarlo a los alumnos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El material existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el material a modificar (ver CU-15: Buscar material). |
| 2    | El sistema muestra los datos actuales del material. |
| 3    | El actor modifica el título, el archivo o el estado de publicación. |
| 4    | El sistema valida que el título no quede vacío. |
| 5    | Si se modificó el archivo, el sistema valida que se hayan completado los datos obligatorios correspondientes al tipo de material, con el mismo criterio que en CU-16: Subir material. |
| 6    | El sistema actualiza los datos del material. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - El material queda actualizado con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el título queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el nuevo archivo no cumple los datos obligatorios del tipo de material, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Alta — se usa para corregir contenido y, principalmente, para publicar material generado por IA o generado en clases en vivo tras su revisión.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Publicar un material lo hace visible en CU-14: Ver contenido de unidad para los alumnos con acceso habilitado a esa unidad.

---

### CU-18
**Eliminar material**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador dar de baja un material cargado en una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El material existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el material a dar de baja (ver CU-15: Buscar material). |
| 2    | El actor confirma la baja. |
| 3    | El sistema marca el material como dado de baja y deja de mostrarlo a los alumnos. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - El material queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — se usa para retirar material desactualizado o cargado por error.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no elimina el archivo físicamente del servidor; únicamente lo marca como inactivo.

---

### CU-19
**Buscar término de glosario**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar los términos del glosario cargados en una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y posee al menos un término de glosario cargado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar los términos de glosario de una unidad. |
| 2    | El sistema solicita la unidad sobre la que se desea consultar. |
| 3    | El actor selecciona la unidad. |
| 4    | El sistema recupera y lista los términos de glosario de la unidad. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de términos y definiciones del glosario de la unidad.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se consulta al gestionar el contenido de una unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-20
**Registrar término de glosario**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador registrar un nuevo término y su definición en el glosario de una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar un término de glosario para una unidad. |
| 2    | El sistema solicita: término y definición. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que el término y la definición hayan sido completados y que el término no esté ya registrado en el glosario de esa unidad. |
| 5    | El sistema registra el término de glosario asociado a la unidad. |
| 6    | El sistema informa el éxito del registro. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El término de glosario queda registrado y asociado a la unidad.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el término o la definición no fueron completados, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el término ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — ocurre al cargar el glosario de cada unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El glosario también puede completarse a partir de la bibliografía mediante el Módulo de Generación de Contenido con IA, aunque esa generación produce resúmenes y no términos de glosario en el PMV.

---

### CU-21
**Modificar término de glosario**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador modificar el término o la definición de un término de glosario registrado.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El término de glosario existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a modificar (ver CU-19: Buscar término de glosario). |
| 2    | El sistema muestra los datos actuales del término. |
| 3    | El actor modifica el término o la definición. |
| 4    | El sistema valida que ninguno de los dos campos quede vacío. |
| 5    | Si se modificó el término, el sistema valida que no coincida con el de otro término ya registrado en el glosario de esa unidad. |
| 6    | El sistema actualiza el término de glosario. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - El término de glosario queda actualizado con los nuevos datos.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el término o la definición quedan vacíos, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el término modificado ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para corregir definiciones ya cargadas.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-22
**Eliminar término de glosario**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador dar de baja un término del glosario de una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El término de glosario existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a dar de baja (ver CU-19: Buscar término de glosario). |
| 2    | El actor confirma la baja. |
| 3    | El sistema marca el término de glosario como dado de baja. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - El término de glosario queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — se usa para retirar términos cargados por error o ya desactualizados.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-23
**Buscar consulta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite consultar las preguntas planteadas por los alumnos en el foro de una unidad. La vista varía según el rol del actor: el Alumno visualiza las consultas de las unidades de los cursos en los que está inscripto; el Docente titular/supervisor visualiza las de sus cursos; el Administrador visualiza todas.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno, Docente o Administrador.
  - Existe al menos una consulta de foro registrada en la unidad.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita ver las consultas del foro de una unidad. |
| 2    | El sistema recupera y lista las consultas de foro de la unidad, con sus respuestas asociadas si existen. |
| 3    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de consultas del foro de la unidad, junto con sus respuestas.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor es Alumno y no posee inscripción vigente al curso de la unidad, el sistema no muestra las consultas. |

- **Frecuencia**
  - Alta — se consulta cada vez que un alumno o docente revisa el foro de una unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-24
**Registrar consulta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno registrar una consulta en el foro de una unidad del curso en el que está inscripto.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El alumno posee una inscripción vigente al curso de la unidad.
  - La unidad se encuentra habilitada según el avance secuencial del alumno.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar una consulta en el foro de una unidad. |
| 2    | El sistema solicita el texto de la consulta. |
| 3    | El actor ingresa el texto. |
| 4    | El sistema valida que el texto haya sido completado. |
| 5    | El sistema registra la consulta asociada a la unidad y al alumno, con la fecha actual. |
| 6    | El sistema notifica al docente titular y al supervisor, si corresponde, la nueva consulta. |
| 7    | El sistema informa el éxito del registro. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La consulta queda registrada, asociada a la unidad y al alumno.
  - El docente recibe la notificación de la nueva consulta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el texto de la consulta no fue completado, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — depende de las dudas que le surjan al alumno durante el cursado.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-25
**Modificar consulta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno modificar el texto de una consulta de foro propia, dentro de un plazo límite configurable desde su registro.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - La consulta de foro existe, no se encuentra en baja y fue registrada por el actor.
  - No se superó el plazo límite de edición configurado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro propia a modificar (ver CU-23: Buscar consulta de foro). |
| 2    | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la consulta. |
| 3    | El sistema muestra el texto actual de la consulta. |
| 4    | El actor modifica el texto. |
| 5    | El sistema valida que el texto no quede vacío. |
| 6    | El sistema actualiza la consulta. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La consulta queda actualizada con el nuevo texto.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si se superó el plazo límite de edición, el sistema informa que la consulta ya no puede modificarse y finaliza el caso de uso. |
| 5    | Si el texto queda vacío, el sistema informa el error y vuelve al paso 4. |

- **Frecuencia**
  - Baja — se usa para corregir o ampliar una consulta recién publicada.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El plazo límite de edición es un parámetro configurable desde el Módulo de Configuración.

---

### CU-26
**Eliminar consulta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja una consulta de foro ante una publicación indebida (por ejemplo, contenido ofensivo o ajeno a la temática de la unidad).
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - La consulta de foro existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro a dar de baja (ver CU-23: Buscar consulta de foro). |
| 2    | El actor confirma la baja. |
| 3    | El sistema marca la consulta como dada de baja, junto con las respuestas asociadas si existen. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - La consulta y sus respuestas asociadas quedan en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Muy baja — se usa excepcionalmente ante contenido indebido.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No surge de la entrevista con el cliente como requisito explícito; se incorpora como criterio de moderación razonable para un foro con alumnos y docentes.

---

### CU-27
**Buscar respuesta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite consultar las respuestas registradas a una consulta de foro. La vista varía según el rol del actor, con el mismo criterio que CU-23: Buscar consulta de foro.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno, Docente o Administrador.
  - Existe al menos una respuesta registrada para la consulta.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita ver las respuestas de una consulta de foro. |
| 2    | El sistema recupera y lista las respuestas asociadas a la consulta. |
| 3    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de respuestas asociadas a la consulta de foro.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta junto con CU-23: Buscar consulta de foro.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-28
**Registrar respuesta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente titular o supervisor del curso registrar una respuesta a una consulta de foro planteada por un alumno.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La consulta de foro existe, no se encuentra en baja y pertenece a un curso en el que el actor participa como titular o supervisor.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita responder una consulta de foro. |
| 2    | El sistema solicita el texto de la respuesta. |
| 3    | El actor ingresa el texto. |
| 4    | El sistema valida que el texto haya sido completado. |
| 5    | El sistema registra la respuesta asociada a la consulta y al docente, con la fecha actual. |
| 6    | El sistema notifica al alumno autor de la consulta que fue respondida. |
| 7    | El sistema informa el éxito del registro. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La respuesta queda registrada, asociada a la consulta y al docente.
  - El alumno recibe la notificación de la respuesta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el texto de la respuesta no fue completado, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — depende de la cantidad de consultas que reciba cada curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-29
**Modificar respuesta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente modificar el texto de una respuesta de foro propia, dentro de un plazo límite configurable desde su registro.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La respuesta de foro existe, no se encuentra en baja y fue registrada por el actor.
  - No se superó el plazo límite de edición configurado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro propia a modificar (ver CU-27: Buscar respuesta de foro). |
| 2    | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la respuesta. |
| 3    | El sistema muestra el texto actual de la respuesta. |
| 4    | El actor modifica el texto. |
| 5    | El sistema valida que el texto no quede vacío. |
| 6    | El sistema actualiza la respuesta. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La respuesta queda actualizada con el nuevo texto.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si se superó el plazo límite de edición, el sistema informa que la respuesta ya no puede modificarse y finaliza el caso de uso. |
| 5    | Si el texto queda vacío, el sistema informa el error y vuelve al paso 4. |

- **Frecuencia**
  - Baja — se usa para corregir o ampliar una respuesta recién publicada.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El plazo límite de edición es un parámetro configurable desde el Módulo de Configuración.

---

### CU-30
**Eliminar respuesta de foro**

- **Objetivo(s) asociado(s)**
  - OBJ-01: Gestionar los cursos.
- **Requisito(s) de información asociado(s)**
  - RI-01: Información sobre cursos.
- **Módulo**
  - MOD-F-01: Módulo de Cursos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja una respuesta de foro ante una publicación indebida.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - La respuesta de foro existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro a dar de baja (ver CU-27: Buscar respuesta de foro). |
| 2    | El actor confirma la baja. |
| 3    | El sistema marca la respuesta como dada de baja. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - La respuesta queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Muy baja — se usa excepcionalmente ante contenido indebido.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Mismo criterio de moderación que CU-26: Eliminar consulta de foro.

---

## MOD-F-02: Módulo de Inscripción y Pagos

### CU-31
**Buscar inscripción**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno, Administrador
- **Descripción**
  - Permite consultar una o más inscripciones registradas en el sistema. La vista varía según el rol del actor: el Alumno visualiza únicamente las propias; el Administrador visualiza todas.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
  - Existe al menos una inscripción registrada previamente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar una o más inscripciones. |
| 2    | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador) y estado (Vigente / Vencida / Dada de baja). |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra las inscripciones que coincidan con los criterios ingresados, restringidas a las propias si el actor es Alumno. |
| 5    | El sistema lista las inscripciones filtradas. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan una o más inscripciones que cumplen con los criterios de búsqueda, con su curso, fecha, fecha de vencimiento de acceso y estado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — el alumno la consulta para ver sus cursos activos; el Administrador, para el seguimiento comercial.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-32
**Inscribir curso**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno inscribirse a un curso publicado, dentro del período habilitado, dando inicio al proceso de inscripción que se completa con el pago del curso.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El curso se encuentra publicado.
  - La fecha actual está dentro del período de inscripción del curso.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el alumno solicita inscribirse a un curso desde su ficha pública. |
| 2    | El sistema valida que la fecha actual esté dentro del período de inscripción del curso. |
| 3    | El sistema valida que el alumno no posea ya una inscripción vigente a ese curso. |
| 4    | El sistema evalúa automáticamente si el alumno cumple alguna condición de descuento vigente y, de ser así, calcula el monto a pagar con el descuento aplicado (ver PA-3: Aplicación automática de descuentos). |
| 5    | El sistema registra la inscripción, con la fecha actual. |
| 6    | El sistema deriva al alumno al pago del curso (CU-35: Realizar pago). |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La inscripción queda registrada.
  - El acceso al contenido del curso permanece bloqueado hasta que se confirme el pago mediante CU-35: Realizar pago.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si la fecha actual no está dentro del período de inscripción del curso, el sistema informa que la inscripción no está habilitada y finaliza el caso de uso. |
| 3    | Si el alumno ya posee una inscripción vigente a ese curso, el sistema lo informa y finaliza el caso de uso. |

- **Frecuencia**
  - Alta — ocurre cada vez que un alumno decide comenzar un curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El sistema informa los detalles del curso al alumno antes de confirmar la inscripción desde su ficha pública.

---

### CU-33
**Dar de baja inscripción**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno darse de baja de un curso en el que está inscripto, registrando el abandono. La baja no genera reembolso del pago realizado.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - La inscripción existe, pertenece al actor y se encuentra vigente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la inscripción a dar de baja (ver CU-31: Buscar inscripción). |
| 2    | El sistema solicita confirmación y, opcionalmente, un motivo u observación de la baja. |
| 3    | El actor confirma la baja y, si lo desea, ingresa el motivo. |
| 4    | El sistema informa que la baja no genera reembolso del pago realizado y solicita confirmación final. |
| 5    | El actor confirma. |
| 6    | El sistema registra la baja de la inscripción, con la observación ingresada si corresponde. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La inscripción queda en estado de baja.
  - El alumno pierde el acceso al contenido del curso.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — ocurre cuando un alumno decide abandonar un curso antes de finalizarlo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No existe mecanismo de reembolso: la política de no devolución fue confirmada por el cliente, análoga a la habitual en el ámbito universitario.

---

### CU-34
**Buscar pago**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno, Administrador
- **Descripción**
  - Permite consultar uno o más pagos registrados en el sistema. La vista varía según el rol del actor: el Alumno visualiza únicamente los propios; el Administrador visualiza todos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
  - Existe al menos un pago registrado previamente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar uno o más pagos. |
| 2    | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador), estado (Pendiente / Acreditado / Rechazado) y rango de fecha. |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra los pagos que coincidan con los criterios ingresados, restringidos a los propios si el actor es Alumno. |
| 5    | El sistema lista los pagos filtrados. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más pagos que cumplen con los criterios de búsqueda, con su monto, fecha, método y estado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — el alumno la consulta para verificar sus pagos; el Administrador, para el control de acreditaciones.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-35
**Realizar pago**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno pagar un curso al que se inscribió, con tarjeta de crédito o débito o con saldo de cuenta, mediante integración con la Checkout API de Mercado Pago, por el total del curso con el descuento aplicado si corresponde. Ver PA-2: Pago con tarjeta integrado.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El alumno posee una inscripción registrada y pendiente de pago para el curso.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el alumno solicita pagar el curso al que se inscribió. |
| 2    | El sistema muestra el monto a pagar, con el descuento aplicado si corresponde. |
| 3    | El sistema solicita los datos de pago: tarjeta de crédito, tarjeta de débito o saldo de cuenta. |
| 4    | El actor ingresa los datos de pago solicitados. |
| 5    | El sistema valida que se hayan completado los datos obligatorios del medio de pago elegido. |
| 6    | El sistema envía la operación a la Checkout API de Mercado Pago para su procesamiento. |
| 7    | Mercado Pago valida los fondos y la autorización del banco emisor, y confirma o rechaza el pago. |
| 8    | El sistema registra el pago con el resultado informado por Mercado Pago, incluyendo el identificador de la transacción (payment_id) y la fecha actual. |
| 9    | Si el pago fue acreditado, el sistema habilita el acceso al curso, genera el comprobante (CU-36) y notifica al alumno el resultado. |
| 10   | Fin del caso de uso. |

- **Postcondición(es)**
  - El pago queda registrado con su resultado.
  - Si el pago fue acreditado: el acceso al curso queda habilitado, se genera el comprobante y se notifica al alumno.
  - Si el pago fue rechazado: la inscripción permanece sin acceso habilitado y se notifica al alumno.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 5    | Si no se completaron los datos obligatorios del medio de pago, el sistema informa el error y vuelve al paso 4. |
| 7    | Si Mercado Pago rechaza el pago, el sistema registra el pago como rechazado, informa el motivo al alumno y le permite reintentar. |

- **Frecuencia**
  - Alta — ocurre por cada inscripción confirmada.
- **Estabilidad**
  - Media — depende de las condiciones de integración de la Checkout API de Mercado Pago.
- **Comentarios**
  - El sistema procesa exclusivamente pagos con tarjeta de crédito, débito o saldo de cuenta, unificados en la Checkout API de Mercado Pago.

---

### CU-36
**Generar comprobante**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Alumno, indirectamente mediante CU-35: Realizar pago
- **Descripción**
  - Genera automáticamente el comprobante de un pago acreditado y lo envía por correo electrónico al alumno.
- **Precondición(es)**
  - Un pago fue registrado con estado Acreditado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia automáticamente cuando un pago queda registrado con estado Acreditado. |
| 2    | El sistema genera un número de comprobante único y el archivo descargable correspondiente. |
| 3    | El sistema registra el comprobante asociado al pago, con la fecha de emisión actual. |
| 4    | El sistema envía el comprobante al alumno por correo electrónico. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - El comprobante queda registrado y asociado al pago.
  - El alumno recibe el comprobante por correo electrónico.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — ocurre por cada pago acreditado.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Es un comprobante simple, no una factura electrónica; la emisión de factura A o B se gestiona de forma manual por correo electrónico ante solicitud del alumno, fuera del alcance del sistema.
  - El alumno puede volver a consultarlo y descargarlo mediante CU-34: Buscar pago.

---

### CU-37
**Buscar descuento**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador buscar uno o más descuentos registrados en el sistema.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - Existe al menos un descuento registrado previamente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar uno o más descuentos. |
| 2    | El sistema solicita los criterios de búsqueda: nombre y vigencia (Vigente / Vencido / Agotado). |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra los descuentos que coincidan con los criterios ingresados. |
| 5    | El sistema lista los descuentos filtrados. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más descuentos que cumplen con los criterios de búsqueda, con su porcentaje, vigencia, cantidad límite y cantidad usada.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se consulta al gestionar promociones y campañas comerciales.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-38
**Registrar descuento**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador registrar un nuevo descuento a aplicar automáticamente a los alumnos que cumplan la condición configurada.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento. |
| 2    | El sistema solicita: nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite ofertada y, opcionalmente, la cantidad de cursos que el alumno debe haber comprado como condición. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios (nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite). |
| 5    | El sistema valida que el porcentaje ingresado sea un valor entre 1 y 100. |
| 6    | El sistema valida que la vigencia hasta sea posterior a la vigencia desde. |
| 7    | El sistema valida que la cantidad límite ingresada sea un número entero mayor a cero. |
| 8    | Si el actor completó la cantidad de cursos requeridos, el sistema valida que sea un número entero mayor o igual a cero. |
| 9    | El sistema registra el descuento, con cantidad usada en cero. |
| 10   | El sistema informa el éxito del registro. |
| 11   | Fin del caso de uso. |

- **Postcondición(es)**
  - El descuento queda registrado y activo.
  - La fecha de creación refleja el momento del alta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3. |
| 5    | Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3. |
| 6    | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3. |
| 7    | Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 8    | Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa al lanzar una nueva campaña o promoción comercial.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La cantidad de cursos comprados previamente es, por el momento, la única condición de aplicación contemplada en el sistema.

---

### CU-39
**Modificar descuento**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador modificar los datos de un descuento registrado.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El descuento existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar (ver CU-37: Buscar descuento). |
| 2    | El sistema muestra los datos actuales del descuento. |
| 3    | El actor modifica los datos que desea. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios. |
| 5    | El sistema valida que el porcentaje ingresado sea un valor entre 1 y 100. |
| 6    | El sistema valida que la vigencia hasta sea posterior a la vigencia desde. |
| 7    | El sistema valida que la cantidad límite ingresada sea un número entero mayor a cero. |
| 8    | Si se completó la cantidad de cursos requeridos, el sistema valida que sea un número entero mayor o igual a cero. |
| 9    | El sistema actualiza los datos del descuento. |
| 10   | El sistema informa el éxito de la modificación. |
| 11   | Fin del caso de uso. |

- **Postcondición(es)**
  - El descuento queda actualizado con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3. |
| 6    | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3. |
| 7    | Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 8    | Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para ajustar los términos de una promoción vigente.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El sistema desactiva automáticamente el descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero (ver PA-3: Aplicación automática de descuentos).

---

### CU-40
**Eliminar descuento**

- **Objetivo(s) asociado(s)**
  - OBJ-02: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-02: Información sobre inscripciones y pagos.
- **Módulo**
  - MOD-F-02: Módulo de Inscripción y Pagos
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja un descuento antes de su vencimiento natural.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El descuento existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el descuento a dar de baja (ver CU-37: Buscar descuento). |
| 2    | El actor confirma la baja. |
| 3    | El sistema marca el descuento como dado de baja, dejando de aplicarlo a nuevas inscripciones. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - El descuento queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Muy baja — ocurre cuando se decide cancelar una promoción antes de su vencimiento.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no afecta los descuentos ya aplicados a inscripciones previas.

---

## MOD-F-03: Módulo de Evaluación y Certificación

### CU-41
**Buscar pool**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar los pools de preguntas registrados en una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - La unidad existe y posee al menos un pool registrado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar los pools de una unidad. |
| 2    | El sistema solicita la unidad sobre la que se desea consultar. |
| 3    | El actor selecciona la unidad. |
| 4    | El sistema recupera y lista los pools de la unidad, con su cantidad de preguntas cargadas. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de pools de la unidad, con su nombre y cantidad de preguntas.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se consulta al gestionar las evaluaciones de una unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-42
**Crear pool**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente crear un nuevo pool de preguntas para una unidad, cargando manualmente sus preguntas de opción múltiple o verdadero/falso junto con las opciones de respuesta de cada una.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La unidad existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita crear un nuevo pool para una unidad. |
| 2    | El sistema solicita el nombre del pool. |
| 3    | El actor ingresa el nombre y comienza a cargar preguntas: por cada una, el tipo (opción múltiple o verdadero/falso), el enunciado y sus opciones de respuesta, marcando cuál es correcta. |
| 4    | El sistema valida que el nombre del pool haya sido completado y que se haya cargado al menos una pregunta. |
| 5    | El sistema valida que cada pregunta tenga al menos dos opciones de respuesta y que exactamente una esté marcada como correcta. |
| 6    | El sistema registra el pool con sus preguntas y opciones. |
| 7    | El sistema informa el éxito del registro. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - El pool queda registrado, asociado a la unidad, con sus preguntas y opciones de respuesta.
  - La fecha de creación refleja el momento del alta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el nombre no fue completado o no se cargó ninguna pregunta, el sistema informa el error y vuelve al paso 3. |
| 5    | Si alguna pregunta tiene menos de dos opciones, o no tiene exactamente una opción marcada como correcta, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — ocurre al preparar las evaluaciones de cada unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El pool también puede generarse automáticamente a partir del material de la unidad mediante CU-63: Generar banco de preguntas, como método alternativo a la carga manual.

---

### CU-43
**Modificar pool**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente modificar el nombre de un pool y agregar, editar o eliminar sus preguntas y opciones de respuesta.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - El pool existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar (ver CU-41: Buscar pool). |
| 2    | El sistema muestra los datos actuales del pool, con sus preguntas y opciones. |
| 3    | El actor modifica el nombre del pool, o agrega, edita o elimina preguntas y sus opciones. |
| 4    | El sistema valida que el nombre no quede vacío y que el pool conserve al menos una pregunta. |
| 5    | El sistema valida que cada pregunta conserve al menos dos opciones y exactamente una marcada como correcta. |
| 6    | El sistema actualiza el pool. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - El pool queda actualizado con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el nombre queda vacío o el pool queda sin preguntas, el sistema informa el error y vuelve al paso 3. |
| 5    | Si alguna pregunta queda con menos de dos opciones o sin una única opción correcta, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — se usa para actualizar o ampliar el banco de preguntas de una unidad.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-44
**Eliminar pool**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente dar de baja un pool. Si el pool está asociado a alguna autoevaluación activa, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - El pool existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el pool a dar de baja (ver CU-41: Buscar pool). |
| 2    | El sistema verifica que el pool no esté asociado a ninguna autoevaluación activa. |
| 3    | El actor confirma la baja. |
| 4    | El sistema marca el pool como dado de baja. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - El pool queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el pool está asociado a una autoevaluación activa, el sistema informa la dependencia y no permite la baja. |
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — se usa cuando un pool queda obsoleto.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-45
**Buscar autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar las autoevaluaciones registradas en un curso o unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - Existe al menos una autoevaluación registrada.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar autoevaluaciones. |
| 2    | El sistema solicita el curso o la unidad sobre la que se desea consultar. |
| 3    | El actor ingresa el criterio de búsqueda. |
| 4    | El sistema recupera y lista las autoevaluaciones que coinciden, indicando si son de unidad o evaluaciones finales. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de autoevaluaciones, con su nombre, pools asociados, tiempo límite e intentos permitidos.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se consulta al gestionar la evaluación de un curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-46
**Crear autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente crear una autoevaluación acotada a una unidad, o una evaluación final que integra los pools de varias unidades de un curso, asociándola a uno o más pools de preguntas.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - Existe al menos un pool activo en el curso o unidad correspondiente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita crear una nueva autoevaluación. |
| 2    | El sistema solicita: nombre, tiempo límite, cantidad de intentos permitidos y los pools de preguntas a asociar (uno para una autoevaluación de unidad, uno o más para una evaluación final). |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios y que se haya seleccionado al menos un pool. |
| 5    | El sistema valida que el tiempo límite y la cantidad de intentos permitidos sean valores enteros mayores a cero. |
| 6    | El sistema valida que los pools seleccionados, en conjunto, tengan al menos 10 preguntas activas para poder sortear un intento. |
| 7    | El sistema registra la autoevaluación. |
| 8    | El sistema informa el éxito del registro. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - La autoevaluación queda registrada y asociada a los pools seleccionados.
  - La fecha de creación refleja el momento del alta.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completaron los campos obligatorios o no se seleccionó ningún pool, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el tiempo límite o la cantidad de intentos no son valores enteros mayores a cero, el sistema informa el error y vuelve al paso 3. |
| 6    | Si los pools seleccionados no reúnen al menos 10 preguntas activas en conjunto, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — ocurre al estructurar la evaluación de un curso, típicamente una vez por unidad y una vez por curso para la evaluación final.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Cada intento de un alumno sortea 10 preguntas de los pools asociados a la autoevaluación (ver CU-50: Realizar intento de autoevaluación).

---

### CU-47
**Modificar autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente modificar el nombre, tiempo límite, cantidad de intentos permitidos y los pools asociados a una autoevaluación.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La autoevaluación existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar (ver CU-45: Buscar autoevaluación). |
| 2    | El sistema muestra los datos actuales de la autoevaluación. |
| 3    | El actor modifica los datos que desea. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios y al menos un pool asociado. |
| 5    | El sistema valida que el tiempo límite y la cantidad de intentos permitidos sean valores enteros mayores a cero. |
| 6    | El sistema valida que los pools asociados reúnan al menos 10 preguntas activas en conjunto. |
| 7    | El sistema actualiza la autoevaluación. |
| 8    | El sistema informa el éxito de la modificación. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - La autoevaluación queda actualizada con los nuevos datos.
  - La fecha de modificación refleja el momento del cambio.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío o sin pools asociados, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el tiempo límite o la cantidad de intentos no son valores enteros mayores a cero, el sistema informa el error y vuelve al paso 3. |
| 6    | Si los pools asociados no reúnen al menos 10 preguntas activas, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para ajustar parámetros de una evaluación ya configurada.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Los cambios aplican a los intentos que se realicen a partir de la modificación; no afectan los intentos ya registrados.

---

### CU-48
**Eliminar autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente dar de baja una autoevaluación. Si algún alumno ya registra un intento sobre ella, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La autoevaluación existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a dar de baja (ver CU-45: Buscar autoevaluación). |
| 2    | El sistema verifica que ningún alumno registre intentos sobre esa autoevaluación. |
| 3    | El actor confirma la baja. |
| 4    | El sistema marca la autoevaluación como dada de baja. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - La autoevaluación queda en estado de baja.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si algún alumno ya registra un intento sobre esa autoevaluación, el sistema informa la dependencia y no permite la baja. |
| 3    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — se usa cuando una evaluación aún no cursada queda obsoleta.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-49
**Buscar intento de autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Alumno, Docente
- **Descripción**
  - Permite consultar el historial de intentos de una autoevaluación. La vista varía según el rol del actor: el Alumno visualiza únicamente sus propios intentos; el Docente visualiza los de los alumnos inscriptos en su curso.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno o Docente.
  - Existe al menos un intento registrado para la autoevaluación.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita ver el historial de intentos de una autoevaluación. |
| 2    | El sistema recupera y lista los intentos, restringidos a los propios si el actor es Alumno. |
| 3    | Fin del caso de uso. |

- **Salida**
  - Se recupera el historial de intentos de la autoevaluación, con su fecha, nota y resultado (aprobado / no aprobado).
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — el alumno la consulta para revisar su desempeño; el docente, para hacer seguimiento del curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-50
**Realizar intento de autoevaluación**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno realizar un intento de una autoevaluación, respondiendo un cuestionario de 10 preguntas sorteadas de los pools asociados, con corrección automática. Ver PA-6: Emisión automática de certificados.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El alumno posee una inscripción vigente al curso.
  - La unidad o el curso al que pertenece la autoevaluación se encuentra habilitado según el avance secuencial del alumno.
  - El alumno no superó la cantidad de intentos permitidos para esa autoevaluación.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el alumno inicia un intento de una autoevaluación. |
| 2    | El sistema sortea 10 preguntas cerradas de los pools asociados a la autoevaluación, con sus opciones de respuesta. |
| 3    | El sistema presenta el cuestionario al alumno, dentro del tiempo límite configurado. |
| 4    | El alumno selecciona una opción de respuesta para cada una de las 10 preguntas. |
| 5    | El alumno confirma la entrega del intento. |
| 6    | El sistema valida que se haya respondido a las 10 preguntas. |
| 7    | El sistema corrige automáticamente el intento, comparando la opción elegida por el alumno con la opción correcta de cada pregunta. |
| 8    | El sistema calcula la nota del intento y registra el intento con la fecha actual. |
| 9    | Si el alumno respondió correctamente las 10 preguntas, el sistema aprueba el intento, habilita el acceso a la siguiente unidad y, si correspondía a la evaluación final del curso, dispara la emisión del certificado (CU-52). |
| 10   | Si el alumno no respondió correctamente las 10 preguntas, el sistema no aprueba el intento e informa que debe reintentar el cuestionario completo. |
| 11   | Fin del caso de uso. |

- **Postcondición(es)**
  - El intento queda registrado, con la opción elegida por el alumno en cada pregunta sorteada, la nota obtenida y el resultado.
  - Si fue aprobado, se habilita la siguiente unidad del curso o se dispara la emisión del certificado si correspondía a la evaluación final.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 6    | Si se agota el tiempo límite sin que el alumno haya respondido las 10 preguntas, el sistema cierra automáticamente el intento con las respuestas dadas hasta ese momento y lo registra como no aprobado. |

- **Frecuencia**
  - Alta — se repite en cada unidad de cada curso cursado por cada alumno.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La aprobación exige responder correctamente la totalidad de las 10 preguntas del intento, según lo confirmado por el cliente; no existe una nota de corte parcial.
  - Si el alumno agota la cantidad de intentos permitidos sin aprobar, queda bloqueado para continuar el curso hasta que el Administrador o el Docente amplíen sus intentos mediante CU-47: Modificar autoevaluación.

---

### CU-51
**Buscar certificado**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Alumno, Administrador
- **Descripción**
  - Permite consultar y descargar los certificados emitidos. La vista varía según el rol del actor: el Alumno visualiza únicamente los propios; el Administrador visualiza todos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
  - Existe al menos un certificado emitido.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar certificados. |
| 2    | El sistema recupera y lista los certificados emitidos, restringidos a los propios si el actor es Alumno. |
| 3    | El actor puede descargar el archivo del certificado seleccionado. |
| 4    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más certificados, con su número, fecha de emisión, curso y alumno.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — el alumno la consulta al finalizar un curso; el Administrador, para el seguimiento académico.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-52
**Generar certificado**

- **Objetivo(s) asociado(s)**
  - OBJ-03: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**
  - RI-03: Información sobre evaluación y certificación.
- **Módulo**
  - MOD-F-03: Módulo de Evaluación y Certificación
- **Actor(es)**
  - Alumno, indirectamente mediante CU-50: Realizar intento de autoevaluación
- **Descripción**
  - Genera automáticamente la constancia de finalización de un curso cuando el alumno aprueba la autoevaluación que completa el curso, y la envía por correo electrónico. Ver PA-6: Emisión automática de certificados.
- **Precondición(es)**
  - El alumno aprobó la autoevaluación final del curso, o la última unidad si el curso no posee evaluación final integradora.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia automáticamente cuando el alumno aprueba la autoevaluación que completa el curso. |
| 2    | El sistema verifica que se hayan completado todas las unidades del curso. |
| 3    | El sistema genera un número de certificado único y el archivo descargable correspondiente. |
| 4    | El sistema registra el certificado, asociado a la inscripción del alumno, con la fecha de emisión actual. |
| 5    | El sistema envía el certificado al alumno por correo electrónico. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - El certificado queda registrado y asociado a la inscripción del alumno.
  - El alumno recibe el certificado por correo electrónico y puede consultarlo mediante CU-51: Buscar certificado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — ocurre por cada alumno que finaliza un curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La constancia tiene valor informativo e institucional; no incorpora firma digital certificada. Su valor oficial ante organismos como la Comisión Nacional de Valores queda fuera del alcance del sistema.

---

## MOD-F-04: Módulo de Clon con IA

### CU-53
**Habilitar Clon IA**

- **Objetivo(s) asociado(s)**
  - OBJ-04: Generar clases con Clon de inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-04: Información sobre clases con Clon de inteligencia artificial.
- **Módulo**
  - MOD-F-04: Módulo de Clon con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente habilitarse para dictar clases mediante Clon de inteligencia artificial, una vez completado el proceso de "Avatar Consent" en HeyGen, mediante el cual valida su identidad y da su conformidad para el uso de su imagen y voz.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - El docente completó el proceso de validación de identidad y consentimiento de uso de imagen y voz en HeyGen.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente, tras completar la validación de su identidad y consentimiento en HeyGen, solicita habilitarse en la plataforma para dictar clases con Clon de IA. |
| 2    | El sistema solicita confirmación de que el proceso de validación en HeyGen fue completado. |
| 3    | El actor confirma. |
| 4    | El sistema registra la fecha de habilitación del docente para dictar clases con Clon de IA. |
| 5    | El sistema informa el éxito de la habilitación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - El docente queda habilitado para dictar clases mediante Clon de IA.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Muy baja — ocurre una única vez por docente, antes de su primera clase con Clon de IA.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El proceso de "Avatar Consent" (grabación de validación de identidad, voz e imagen) lo ejecuta HeyGen directamente en su propia plataforma y queda fuera del alcance del sistema propuesto; este caso de uso solo registra que ese proceso externo fue completado.

---

### CU-54
**Deshabilitar Clon IA**

- **Objetivo(s) asociado(s)**
  - OBJ-04: Generar clases con Clon de inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-04: Información sobre clases con Clon de inteligencia artificial.
- **Módulo**
  - MOD-F-04: Módulo de Clon con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente revocar su habilitación para dictar clases mediante Clon de inteligencia artificial.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - El docente se encuentra habilitado para dictar clases con Clon de IA.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita deshabilitarse para dictar clases con Clon de IA. |
| 2    | El sistema solicita confirmación. |
| 3    | El actor confirma. |
| 4    | El sistema quita la habilitación del docente para dictar clases con Clon de IA. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - El docente deja de estar habilitado para dictar clases mediante Clon de IA.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Muy baja — ocurre cuando un docente decide dejar de dictar clases con Clon de IA.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No afecta las clases con Clon de IA ya generadas previamente; sólo impide generar nuevas mientras el docente permanezca deshabilitado.

---

### CU-55
**Generar clase con Clon IA**

- **Objetivo(s) asociado(s)**
  - OBJ-04: Generar clases con Clon de inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-04: Información sobre clases con Clon de inteligencia artificial.
- **Módulo**
  - MOD-F-04: Módulo de Clon con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente titular o supervisor de un curso generar una clase para una unidad mediante un Clon de inteligencia artificial, a partir de un guión que redacta como prompt, integrando con la plataforma HeyGen. Ver PA-5: Generación de videos Clon IA.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - El docente se encuentra habilitado para dictar clases con Clon de IA.
  - La unidad existe y pertenece a un curso en el que el docente participa como titular o supervisor.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita generar una clase con Clon de IA para una unidad. |
| 2    | El sistema solicita el título de la clase y el guión, ingresado como un prompt de texto. |
| 3    | El actor ingresa el título y redacta el guión. |
| 4    | El sistema valida que el título y el guión hayan sido completados. |
| 5    | El sistema registra la clase en estado Pendiente. |
| 6    | El sistema envía el guión, junto con el Avatar ya validado del docente, a HeyGen. |
| 7    | HeyGen genera el video de la clase a partir del guión y el Avatar del docente. |
| 8    | El sistema descarga el video generado y actualiza el estado de la clase a Generada. |
| 9    | El sistema carga el video como material de tipo Grabación de la unidad correspondiente, en estado no publicado. |
| 10   | El sistema notifica al docente que el material está disponible para su revisión antes de publicarlo. |
| 11   | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase con Clon de IA queda registrada, en estado Generada.
  - El video generado queda cargado como material de la unidad, sin publicar.
  - El docente recibe la notificación para revisar el material.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el título o el guión no fueron completados, el sistema informa el error y vuelve al paso 3. |
| 7    | Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente. |

- **Frecuencia**
  - Media — ocurre según la disponibilidad horaria de cada docente de alto nivel académico.
- **Estabilidad**
  - Media — depende de las capacidades y condiciones de integración de HeyGen.
- **Comentarios**
  - –

---

## MOD-F-05: Módulo de Clases en Vivo

### CU-56
**Buscar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente, Administrador
- **Descripción**
  - Permite al Docente titular/supervisor o al Administrador buscar las clases en vivo programadas para una unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - Existe al menos una clase en vivo registrada.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar clases en vivo. |
| 2    | El sistema solicita los criterios de búsqueda: unidad, docente y estado (Programada / En vivo / Finalizada). |
| 3    | El actor ingresa los criterios que desea. |
| 4    | El sistema recupera y lista las clases en vivo que coinciden con los criterios ingresados. |
| 5    | Fin del caso de uso. |

- **Salida**
  - Se recuperan una o más clases en vivo, con su título, fecha y hora, docente y estado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se consulta para el seguimiento del cronograma de clases en vivo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-57
**Programar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente titular o supervisor de un curso programar una clase en vivo para una unidad, definiendo su fecha y hora de transmisión.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La unidad existe y pertenece a un curso en el que el docente participa como titular o supervisor.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita programar una clase en vivo para una unidad. |
| 2    | El sistema solicita: título, fecha y hora de la clase. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios. |
| 5    | El sistema valida que la fecha y hora ingresadas sean posteriores al momento actual. |
| 6    | El sistema registra la clase en estado Programada. |
| 7    | El sistema notifica a los alumnos inscriptos en el curso la fecha de la clase. |
| 8    | El sistema informa el éxito del registro. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase en vivo queda registrada en estado Programada, asociada a la unidad y al docente.
  - Los alumnos inscriptos reciben la notificación.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 5    | Si la fecha y hora ingresadas no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — ocurre al planificar el cronograma de clases en vivo de un curso.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La configuración e instalación del software OBS para cada docente está a cargo del equipo de sistemas de la empresa, previo a la transmisión, y queda fuera del alcance funcional de este caso de uso.

---

### CU-58
**Modificar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente modificar el título, la fecha o la hora de una clase en vivo, siempre que todavía no haya sido transmitida.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a modificar (ver CU-56: Buscar clase en vivo). |
| 2    | El sistema muestra los datos actuales de la clase. |
| 3    | El actor modifica el título, la fecha o la hora. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios y que la fecha y hora sean posteriores al momento actual. |
| 5    | El sistema actualiza los datos de la clase. |
| 6    | El sistema notifica a los alumnos inscriptos el cambio de fecha u horario, si corresponde. |
| 7    | El sistema informa el éxito de la modificación. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase en vivo queda actualizada con los nuevos datos.
  - Los alumnos inscriptos reciben la notificación del cambio, si corresponde.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, o la fecha y hora no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para reprogramar una clase antes de su transmisión.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No es posible modificar una clase que ya está En vivo o Finalizada.

---

### CU-59
**Cancelar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente cancelar una clase en vivo programada que todavía no fue transmitida.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a cancelar (ver CU-56: Buscar clase en vivo). |
| 2    | El actor confirma la cancelación. |
| 3    | El sistema marca la clase como dada de baja. |
| 4    | El sistema notifica a los alumnos inscriptos la cancelación de la clase. |
| 5    | El sistema informa el éxito de la operación. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase en vivo queda dada de baja.
  - Los alumnos inscriptos reciben la notificación de la cancelación.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el actor no confirma la cancelación, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Baja — ocurre cuando un docente no puede sostener una clase ya programada.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No es posible cancelar una clase que ya está En vivo o Finalizada.

---

### CU-60
**Iniciar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente iniciar la transmisión de una clase en vivo programada, generando los datos de conexión que utilizará desde OBS. Ver PA-4: Clases en Vivo.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
  - Se alcanzó el horario programado para la clase.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente, en el horario programado, solicita iniciar la clase en vivo. |
| 2    | El sistema genera los datos de conexión de la transmisión (URL de streaming y clave privada de transmisión). |
| 3    | El sistema pasa la clase al estado En vivo. |
| 4    | El docente carga los datos de conexión en OBS y comienza a transmitir. |
| 5    | El sistema recibe la señal transmitida y la redistribuye en simultáneo a los alumnos inscriptos que ingresan a la clase (ver CU-62: Ingresar a clase en vivo), mientras graba automáticamente la transmisión. |
| 6    | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase en vivo queda en estado En vivo, con sus datos de conexión generados.
  - La transmisión queda disponible para los alumnos inscriptos y se graba automáticamente.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — ocurre por cada clase en vivo programada que efectivamente se dicta.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La clave de transmisión es privada del docente, de forma que solo él pueda transmitir con los datos de conexión generados para esa clase.

---

### CU-61
**Finalizar clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente finalizar la transmisión de una clase en vivo, dando de baja la señal en OBS de forma remota y generando la grabación resultante como material de la unidad. Ver PA-4: Clases en Vivo.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La clase en vivo existe, fue registrada por el actor y se encuentra en estado En vivo.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita finalizar la transmisión de la clase en vivo. |
| 2    | El sistema envía la orden de corte de transmisión y grabación al OBS del docente. |
| 3    | El sistema pasa la clase al estado Finalizada. |
| 4    | El sistema genera la grabación resultante de la transmisión. |
| 5    | El sistema carga la grabación como material de tipo Grabación de la unidad correspondiente, en estado publicado. |
| 6    | El sistema notifica a los alumnos inscriptos que la grabación ya está disponible. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La clase en vivo queda en estado Finalizada.
  - La grabación queda cargada como material publicado de la unidad.
  - Los alumnos inscriptos reciben la notificación de disponibilidad de la grabación.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — ocurre por cada clase en vivo que se transmite.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La grabación resultante queda disponible por un plazo configurable (cuatro meses por defecto), con aviso previo al alumno antes de su vencimiento y eliminación automática al cumplirse el plazo.

---

### CU-62
**Ingresar a clase en vivo**

- **Objetivo(s) asociado(s)**
  - OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**
  - RI-05: Información sobre clases en vivo.
- **Módulo**
  - MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite al Alumno ingresar a la transmisión de una clase en vivo mientras se encuentra en curso.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Alumno.
  - El alumno posee una inscripción vigente al curso de la unidad.
  - La clase en vivo se encuentra en estado En vivo.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el alumno solicita ingresar a una clase en vivo que se encuentra en curso. |
| 2    | El sistema verifica que la clase se encuentre en estado En vivo y que el alumno posea inscripción vigente al curso. |
| 3    | El sistema conecta al alumno a la transmisión en curso. |
| 4    | Fin del caso de uso. |

- **Salida**
  - El alumno queda conectado a la transmisión en vivo de la clase.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si la clase todavía no comenzó o ya finalizó, el sistema informa que la transmisión no está disponible en este momento. |

- **Frecuencia**
  - Alta — ocurre por cada alumno que asiste a una clase en vivo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El alumno que no pudo asistir en vivo puede acceder posteriormente a la grabación mediante CU-14: Ver contenido de unidad, una vez finalizada la clase.

---

## MOD-F-06: Módulo de Generación de Contenido con IA

### CU-63
**Generar banco de preguntas**

- **Objetivo(s) asociado(s)**
  - OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**
  - MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente generar automáticamente un pool de preguntas para una unidad, a partir de la bibliografía y el glosario cargados, mediante un modelo de inteligencia artificial ejecutado localmente (Ollama). Ver PA-9: Generación de banco de preguntas.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La unidad posee al menos un material de tipo Bibliografía o un término de glosario cargado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita generar un banco de preguntas para una unidad. |
| 2    | El sistema solicita, opcionalmente, un guión adicional ingresado como prompt de texto para orientar la generación. |
| 3    | El actor confirma la generación, con o sin el guión adicional. |
| 4    | El sistema envía la bibliografía, el glosario de la unidad y el guión, si fue ingresado, al modelo de inteligencia artificial local. |
| 5    | El modelo de inteligencia artificial genera un banco de preguntas cerradas de opción múltiple y verdadero/falso, siguiendo la proporción configurada. |
| 6    | El sistema recibe el banco de preguntas generado y valida que cada pregunta tenga al menos dos opciones y exactamente una marcada como correcta. |
| 7    | El sistema registra el pool generado, asociado a la unidad. |
| 8    | El sistema notifica al docente que el pool está disponible para su revisión antes de publicarse. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - El pool generado queda registrado, asociado a la unidad.
  - El docente recibe la notificación para revisar el pool antes de utilizarlo en una autoevaluación.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 6    | Si el modelo de inteligencia artificial devuelve un banco de preguntas con un formato inválido, el sistema descarta el resultado, informa el error al docente y le permite reintentar. |

- **Frecuencia**
  - Media — se usa como alternativa a la carga manual de preguntas (CU-42: Crear pool).
- **Estabilidad**
  - Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**
  - El pool queda registrado sin publicar como pool de referencia hasta que el docente lo revise; su uso efectivo en una autoevaluación se define mediante CU-46: Crear autoevaluación.
  - El guión ingresado como prompt no se conserva en el sistema; es un dato de uso único hacia el modelo de inteligencia artificial.

---

### CU-64
**Generar resumen de unidad**

- **Objetivo(s) asociado(s)**
  - OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**
  - MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente generar automáticamente un resumen del contenido de una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local. Ver PA-8: Generación de resúmenes de unidad.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
  - La unidad posee al menos un material de tipo Bibliografía cargado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita generar un resumen para una unidad. |
| 2    | El actor confirma la generación. |
| 3    | El sistema envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4    | El modelo de inteligencia artificial genera un resumen estructurado del contenido. |
| 5    | El sistema recibe el resumen y lo registra como material de tipo Resumen de la unidad, en estado no publicado. |
| 6    | El sistema notifica al docente que el resumen está disponible para su revisión antes de publicarlo. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El resumen queda registrado como material de la unidad, sin publicar.
  - El docente recibe la notificación para revisarlo.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se usa como alternativa a la redacción manual de un resumen.
- **Estabilidad**
  - Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**
  - El docente publica el resumen generado mediante CU-17: Modificar material, una vez revisado su contenido.

---

### CU-65
**Generar presentación de clase**

- **Objetivo(s) asociado(s)**
  - OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**
  - RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**
  - MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**
  - Docente
- **Descripción**
  - Permite al Docente generar automáticamente una presentación descargable para una unidad, a partir de un guión que redacta como prompt, mediante el modelo de inteligencia artificial local. Ver PA-7: Generación de presentaciones para clases.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Docente.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el docente solicita generar una presentación para una unidad. |
| 2    | El sistema solicita el guión de la unidad, ingresado como un prompt de texto. |
| 3    | El actor redacta el guión. |
| 4    | El sistema valida que el guión haya sido completado. |
| 5    | El sistema envía el guión al modelo de inteligencia artificial local. |
| 6    | El modelo de inteligencia artificial devuelve una estructura de contenidos (títulos, subtítulos y puntos clave). |
| 7    | El sistema da formato a la estructura recibida como una presentación descargable y la registra como material de tipo Presentación de la unidad, en estado no publicado. |
| 8    | El sistema notifica al docente que la presentación está disponible para su revisión antes de publicarla. |
| 9    | Fin del caso de uso. |

- **Postcondición(es)**
  - La presentación queda registrada como material de la unidad, sin publicar.
  - El docente recibe la notificación para revisarla.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el guión no fue completado, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Media — se usa como alternativa a la carga manual de una presentación.
- **Estabilidad**
  - Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**
  - El guión ingresado no se conserva en el sistema; es un dato de uso único hacia el modelo de inteligencia artificial, igual que en CU-55: Generar clase con Clon IA.

---

## MOD-NF-01: Módulo de Usuarios y Notificaciones

### CU-66
**Registrarse**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno
- **Descripción**
  - Permite a un interesado, todavía sin cuenta, crear su propia cuenta de Alumno en la plataforma, mediante correo electrónico y contraseña, validando la cuenta a través de un enlace enviado por email.
- **Precondición(es)**
  - –
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el interesado solicita crear una cuenta. |
| 2    | El sistema solicita: nombre, apellido, correo electrónico, DNI y contraseña. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 5    | El sistema registra la cuenta con rol Alumno, con el correo sin validar. |
| 6    | El sistema envía un enlace de validación al correo electrónico ingresado. |
| 7    | El actor accede al enlace recibido. |
| 8    | El sistema marca el correo electrónico como validado. |
| 9    | El sistema informa el éxito del registro y habilita el inicio de sesión. |
| 10   | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta queda registrada con rol Alumno.
  - La cuenta queda validada una vez que el actor accede al enlace enviado por correo.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el correo electrónico ya está registrado, el sistema informa el error y sugiere iniciar sesión o recuperar la contraseña. |

- **Frecuencia**
  - Alta — ocurre por cada nuevo alumno que se suma a la plataforma.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Es la vía de autoregistro con correo y contraseña; el alta mediante Google OAuth se resuelve automáticamente en CU-75: Iniciar sesión (ver PA-1: Login con Google).

---

### CU-67
**Buscar usuario**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador buscar los usuarios registrados en el sistema, con fines de gestión.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - Existe al menos un usuario registrado.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita buscar uno o más usuarios. |
| 2    | El sistema solicita los criterios de búsqueda: nombre, apellido, correo electrónico, DNI y rol (Alumno / Docente / Administrador). |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra los usuarios que coincidan con los criterios ingresados. |
| 5    | El sistema lista los usuarios filtrados. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más usuarios que cumplen con los criterios de búsqueda, con su rol y estado.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta frecuentemente para la gestión de cuentas.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Para los usuarios con rol Docente, este CU también permite verificar su estado de habilitación y de habilitación para Clon de IA.

---

### CU-68
**Registrar usuario**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador registrar manualmente la cuenta de un Alumno o de otro Administrador, para los casos en que el alta no ocurre por autoregistro (por ejemplo, la inscripción corporativa de una empresa para sus empleados).
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar manualmente un usuario. |
| 2    | El sistema solicita: nombre, apellido, correo electrónico, DNI y el rol a asignar (Alumno o Administrador). |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 5    | El sistema registra la cuenta con el rol indicado y envía al correo ingresado un enlace para que el usuario defina su contraseña. |
| 6    | El sistema informa el éxito del registro. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta queda registrada con el rol indicado.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para altas manuales excepcionales; el alta habitual de un Alumno ocurre por autoregistro (CU-66) y la de un Docente por CU-73: Registrar docente.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El Administrador puede crear cuentas de otros Administradores, pero no puede modificarlas ni darlas de baja una vez creadas.

---

### CU-69
**Modificar usuario**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador modificar los datos base de la cuenta de un Alumno.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El usuario existe, posee rol Alumno y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la cuenta del alumno a modificar (ver CU-67: Buscar usuario). |
| 2    | El sistema muestra los datos actuales de la cuenta. |
| 3    | El actor modifica el nombre, apellido, correo electrónico, DNI, teléfono o imagen de perfil. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios y que el correo electrónico, si fue modificado, no esté ya registrado por otra cuenta. |
| 5    | El sistema actualiza los datos de la cuenta. |
| 6    | El sistema informa el éxito de la modificación. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta del alumno queda actualizada con los nuevos datos.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el correo electrónico ya está registrado por otra cuenta, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa ante un reclamo o corrección de datos solicitada por el alumno.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No incluye el cambio de contraseña, que se gestiona exclusivamente mediante CU-77: Recuperar contraseña. Los datos propios del Docente (información profesional) se gestionan mediante CU-74: Modificar docente, no con este CU.

---

### CU-70
**Dar de baja usuario**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de baja la cuenta de un usuario, quitándole el acceso al sistema.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El usuario existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la cuenta del usuario a dar de baja (ver CU-67: Buscar usuario). |
| 2    | Si el usuario posee rol Administrador, el sistema valida que existan otros administradores activos en el sistema además de él. |
| 3    | Si el usuario posee rol Docente, el sistema verifica que no sea titular de ningún curso publicado y no esté dado de baja. |
| 4    | El actor confirma la baja. |
| 5    | El sistema marca la cuenta como dada de baja y cierra sus sesiones activas. |
| 6    | El sistema informa el éxito de la operación. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta queda en estado de baja y pierde acceso al sistema.
  - Las sesiones activas del usuario quedan cerradas.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 2    | Si el usuario posee rol Administrador y es el único administrador activo del sistema, el sistema informa que no puede quedar sin administradores y no permite la baja. |
| 3    | Si el docente es titular de al menos un curso publicado y no dado de baja, el sistema informa la dependencia y no permite la baja hasta que se le asigne otro docente titular o se despublique el curso. |
| 4    | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

- **Frecuencia**
  - Muy baja — ocurre ante un cierre de cuenta definitivo.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La baja no elimina el registro físicamente, para preservar el historial de inscripciones, pagos y certificados.
  - El sistema debe garantizar en todo momento la existencia de al menos un Administrador activo, para evitar que el sistema quede sin gestión posible.

---

### CU-71
**Ver perfil**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a cualquier usuario autenticado consultar los datos de su propia cuenta.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita ver su perfil. |
| 2    | El sistema recupera los datos de la cuenta del actor. |
| 3    | El sistema muestra los datos al actor. |
| 4    | Fin del caso de uso. |

- **Salida**
  - Se recuperan los datos de la cuenta del actor: nombre, apellido, correo electrónico, DNI, teléfono e imagen de perfil, y los datos profesionales adicionales si el actor es Docente.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta cada vez que un usuario accede a su perfil.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Si el actor es Docente, también se muestran sus datos profesionales (biografía, años de experiencia, títulos, matrícula y estado de habilitación), aunque solo el Administrador puede modificarlos mediante CU-74: Modificar docente.

---

### CU-72
**Editar perfil**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a cualquier usuario autenticado editar los datos base de su propia cuenta.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita editar su perfil. |
| 2    | El sistema muestra los datos actuales de la cuenta del actor. |
| 3    | El actor modifica el nombre, apellido, teléfono o imagen de perfil. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios. |
| 5    | El sistema actualiza los datos de la cuenta. |
| 6    | El sistema informa el éxito de la modificación. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta del actor queda actualizada con los nuevos datos.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa cuando el usuario decide actualizar sus datos de contacto o su foto de perfil.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No incluye el correo electrónico ni el cambio de contraseña, que se gestionan mediante CU-77: Recuperar contraseña. Si el actor es Docente, sus datos profesionales no se editan aquí; quedan a cargo del Administrador mediante CU-74: Modificar docente.

---

### CU-73
**Registrar docente**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador registrar manualmente la cuenta de un nuevo docente, verificando previamente sus credenciales académicas o profesionales. El alta de un docente no admite autoregistro.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita registrar un nuevo docente. |
| 2    | El sistema solicita: nombre, apellido, correo electrónico, DNI, teléfono, biografía, años de experiencia, título o títulos universitarios o de posgrado con su matrícula profesional cuando corresponda, y matrícula del Registro de Idóneos de la Comisión Nacional de Valores cuando aplique. |
| 3    | El actor ingresa los datos solicitados. |
| 4    | El actor verifica el título declarado contra el Registro Público de Graduados Universitarios, o bien la matrícula profesional o de la Comisión Nacional de Valores informada. |
| 5    | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 6    | El sistema valida que se haya declarado al menos un título universitario o de posgrado, o al menos una matrícula profesional (colegio o Comisión Nacional de Valores). |
| 7    | El sistema valida que los años de experiencia ingresados sean un número entero mayor o igual a cero. |
| 8    | El sistema registra la cuenta con rol Docente, habilitada, y envía al correo ingresado un enlace para que el docente defina su contraseña. |
| 9    | El sistema informa el éxito del registro. |
| 10   | Fin del caso de uso. |

- **Postcondición(es)**
  - La cuenta del docente queda registrada, habilitada y con su información profesional cargada.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 5    | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 5    | Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3. |
| 6    | Si no se declaró ningún título ni ninguna matrícula profesional, el sistema informa el error y vuelve al paso 3. |
| 7    | Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — ocurre cada vez que se incorpora un nuevo docente de élite a la plataforma.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La verificación del título o la matrícula es un control manual y externo que realiza el Administrador antes de cargar los datos; el sistema no modela un flujo de estados de verificación por credencial.

---

### CU-74
**Modificar docente**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador modificar la información profesional de un docente y habilitarlo o suspenderlo temporalmente para dictar clases, sin eliminar su cuenta ni su historial.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - El docente existe y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona el docente a modificar (ver CU-67: Buscar usuario). |
| 2    | El sistema muestra los datos profesionales actuales del docente. |
| 3    | El actor modifica la biografía, los años de experiencia, los títulos, la matrícula, los cursos asignados, o el estado de habilitación para dictar clases. |
| 4    | El sistema valida que se mantengan completos los campos obligatorios. |
| 5    | El sistema valida que se mantenga declarado al menos un título universitario o de posgrado, o al menos una matrícula profesional (colegio o Comisión Nacional de Valores). |
| 6    | Si se modificaron los años de experiencia, el sistema valida que sean un número entero mayor o igual a cero. |
| 7    | El sistema actualiza los datos del docente. |
| 8    | Si el actor suspendió la habilitación del docente, el sistema le notifica el cambio de estado. |
| 9    | El sistema informa el éxito de la modificación. |
| 10   | Fin del caso de uso. |

- **Postcondición(es)**
  - Los datos profesionales del docente quedan actualizados.
  - Si se modificó su estado de habilitación, el docente queda habilitado o suspendido para dictar clases, según corresponda.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5    | Si la modificación deja al docente sin ningún título ni matrícula profesional declarados, el sistema informa el error y vuelve al paso 3. |
| 6    | Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa para actualizar credenciales o ante una suspensión, por ejemplo por mala praxis.
- **Estabilidad**
  - Alta
- **Comentarios**
  - La suspensión no elimina la cuenta del docente ni su historial; solo le impide dictar nuevas clases mientras permanezca suspendido. Es distinta de CU-70: Dar de baja usuario, que sí quita el acceso a la cuenta.
  - La información profesional del docente es responsabilidad exclusiva del Administrador; el docente no la autoedita.

---

### CU-75
**Iniciar sesión**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a un usuario iniciar sesión en el sistema mediante correo electrónico y contraseña, o mediante Google OAuth como método alternativo. Ver PA-1: Login con Google.
- **Precondición(es)**
  - El actor posee una cuenta registrada y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita iniciar sesión. |
| 2    | El sistema solicita el correo electrónico y la contraseña, u ofrece la opción de ingresar con Google. |
| 3    | El actor ingresa sus credenciales, o selecciona ingresar con Google y autoriza el acceso a su correo y datos básicos de perfil. |
| 4    | El sistema valida las credenciales ingresadas, o el token devuelto por Google, según el método elegido. |
| 5    | El sistema valida que la cantidad de sesiones concurrentes activas del usuario no supere el límite configurado. |
| 6    | El sistema registra una nueva sesión, con su token, fecha de inicio, IP y dispositivo. |
| 7    | El sistema informa el éxito del inicio de sesión. |
| 8    | Fin del caso de uso. |

- **Postcondición(es)**
  - La sesión queda registrada y activa.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si las credenciales ingresadas son incorrectas, el sistema informa el error y vuelve al paso 3. |
| 4    | Si el correo electrónico todavía no fue validado, el sistema informa que debe validarlo antes de iniciar sesión. |
| 5    | Si el usuario ya alcanzó el límite de sesiones concurrentes permitidas, el sistema informa el error y le solicita cerrar una sesión activa antes de continuar. |

- **Frecuencia**
  - Muy alta — ocurre en cada acceso de cada usuario al sistema.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Si el usuario ingresa con Google y no posee una cuenta asociada a ese correo, el sistema crea automáticamente una cuenta con rol Alumno (ver PA-1: Login con Google). El límite de sesiones concurrentes busca mitigar el uso compartido de credenciales.

---

### CU-76
**Cerrar sesión**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a un usuario cerrar su propia sesión activa en el sistema.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita cerrar su sesión. |
| 2    | El sistema registra la fecha de fin de la sesión activa. |
| 3    | El sistema redirige al actor a la pantalla de inicio de sesión. |
| 4    | Fin del caso de uso. |

- **Postcondición(es)**
  - La sesión queda cerrada.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — ocurre en cada cierre de sesión de cada usuario.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-77
**Recuperar contraseña**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a un usuario restablecer su contraseña cuando la olvidó, mediante un token temporal enviado a su correo electrónico. Es la única vía por la que se modifica la contraseña de una cuenta.
- **Precondición(es)**
  - El actor posee una cuenta registrada con contraseña propia y no se encuentra en baja.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor selecciona la opción de recuperar contraseña desde la pantalla de inicio de sesión. |
| 2    | El sistema solicita el correo electrónico asociado a la cuenta. |
| 3    | El actor ingresa el correo electrónico. |
| 4    | El sistema valida que el correo esté registrado. |
| 5    | El sistema genera un token de recuperación con su fecha de expiración y lo envía al correo del actor. |
| 6    | El actor accede al enlace recibido e ingresa la nueva contraseña. |
| 7    | El sistema valida que el token no haya expirado. |
| 8    | El sistema actualiza la contraseña de la cuenta. |
| 9    | El sistema informa el éxito de la operación. |
| 10   | Fin del caso de uso. |

- **Postcondición(es)**
  - La contraseña de la cuenta queda actualizada.
  - El actor puede iniciar sesión con la nueva contraseña.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si el correo ingresado no está registrado, el sistema informa el error y vuelve al paso 3. |
| 7    | Si el token de recuperación expiró, el sistema informa el error y le solicita generar uno nuevo, volviendo al paso 2. |

- **Frecuencia**
  - Media — ocurre cada vez que un usuario olvida su contraseña.
- **Estabilidad**
  - Alta
- **Comentarios**
  - No aplica a las cuentas que se autentican exclusivamente mediante Google OAuth, ya que no poseen contraseña propia en el sistema.

---

### CU-78
**Buscar sesión**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a un usuario consultar sus propias sesiones activas. El Administrador puede además consultar las sesiones activas de cualquier usuario.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita ver las sesiones activas. |
| 2    | El sistema recupera y lista las sesiones activas del actor, o del usuario indicado si el actor es Administrador. |
| 3    | Fin del caso de uso. |

- **Salida**
  - Se recupera el listado de sesiones activas, con su fecha de inicio, IP y dispositivo.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Baja — se consulta cuando el usuario quiere revisar desde dónde tiene acceso activo a su cuenta.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-79
**Eliminar sesión**

- **Objetivo(s) asociado(s)**
  - OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**
  - RI-07: Información sobre usuarios y notificaciones.
- **Módulo**
  - MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**
  - Alumno, Docente, Administrador
- **Descripción**
  - Permite a un usuario cerrar de forma forzada una sesión activa propia distinta de la actual. El Administrador puede además cerrar una sesión activa de cualquier usuario.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema.
  - La sesión a cerrar existe y se encuentra activa.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor busca y selecciona la sesión activa a cerrar (ver CU-78: Buscar sesión). |
| 2    | El actor confirma el cierre. |
| 3    | El sistema registra la fecha de fin de esa sesión. |
| 4    | El sistema informa el éxito de la operación. |
| 5    | Fin del caso de uso. |

- **Postcondición(es)**
  - La sesión seleccionada queda cerrada.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Baja — se usa ante la sospecha de uso compartido de credenciales o para liberar un cupo de sesión concurrente.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

## MOD-NF-02: Módulo de Auditoría

### CU-80
**Consultar auditoría**

- **Objetivo(s) asociado(s)**
  - OBJ-08: Registrar las acciones críticas del sistema.
- **Requisito(s) de información asociado(s)**
  - RI-08: Información sobre auditoría.
- **Módulo**
  - MOD-NF-02: Módulo de Auditoría
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador consultar el registro de auditoría de las acciones críticas del sistema (pagos, acreditaciones, altas de curso y cambios de estado de inscripción, entre otras), para garantizar trazabilidad y control sobre las operaciones.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
  - Existe al menos un registro de auditoría.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita consultar el registro de auditoría. |
| 2    | El sistema solicita los criterios de búsqueda: usuario responsable, tipo de acción (Crear / Modificar / Eliminar / Consultar), entidad afectada y rango de fecha. |
| 3    | El actor ingresa los criterios de búsqueda que desea. |
| 4    | El sistema recupera y filtra los registros de auditoría que coincidan con los criterios ingresados. |
| 5    | El sistema lista los registros filtrados. |
| 6    | Fin del caso de uso. |

- **Salida**
  - Se recuperan uno o más registros de auditoría, con el usuario responsable, el tipo de acción, la entidad afectada, el identificador del registro puntual y la fecha y hora exacta.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Baja — se consulta ante la necesidad puntual de rastrear una operación, en especial sobre pagos.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El alta de los registros de auditoría es automática, generada por el propio sistema ante cada operación crítica; no existe un caso de uso de registro manual. La consulta está restringida al Administrador.

---

## MOD-NF-03: Módulo de Reportes y Estadísticas

### CU-81
**Generar informe de alumnos**

- **Objetivo(s) asociado(s)**
  - OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**
  - RI-09: Información sobre reportes y estadísticas.
- **Módulo**
  - MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador generar un informe de alumnos inscriptos, para facilitar la toma de decisiones estratégicas de los socios.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita generar un informe de alumnos. |
| 2    | El sistema solicita el rango de fecha y, opcionalmente, el curso sobre el que se desea informar. |
| 3    | El actor ingresa los criterios solicitados. |
| 4    | El sistema recopila los datos de alumnos inscriptos según los criterios ingresados y genera el informe. |
| 5    | El sistema registra el reporte generado, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6    | El sistema pone el informe a disposición del actor para su descarga. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El reporte queda registrado en el historial de reportes generados.
  - El informe de alumnos queda disponible para su descarga.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se genera periódicamente para el seguimiento comercial y académico.
- **Estabilidad**
  - Alta
- **Comentarios**
  - –

---

### CU-82
**Generar informe de tráfico**

- **Objetivo(s) asociado(s)**
  - OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**
  - RI-09: Información sobre reportes y estadísticas.
- **Módulo**
  - MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador generar un informe del tráfico de la plataforma, incluyendo las visitas al catálogo público de cursos.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita generar un informe de tráfico. |
| 2    | El sistema solicita el rango de fecha sobre el que se desea informar. |
| 3    | El actor ingresa el rango solicitado. |
| 4    | El sistema recopila los datos de tráfico de la plataforma según el rango ingresado y genera el informe. |
| 5    | El sistema registra el reporte generado, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6    | El sistema pone el informe a disposición del actor para su descarga. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El reporte queda registrado en el historial de reportes generados.
  - El informe de tráfico queda disponible para su descarga.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se genera periódicamente para evaluar el alcance de las campañas comerciales.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El tráfico incluye las visitas al catálogo público (CU-05: Explorar catálogo de cursos), relevante para medir la efectividad del contenido gratuito usado como gancho comercial.

---

### CU-83
**Generar informe de ingresos por pagos**

- **Objetivo(s) asociado(s)**
  - OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**
  - RI-09: Información sobre reportes y estadísticas.
- **Módulo**
  - MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador generar un informe de ingresos por pagos acreditados, para facilitar la toma de decisiones estratégicas de los socios.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita generar un informe de ingresos por pagos. |
| 2    | El sistema solicita el rango de fecha y, opcionalmente, el curso sobre el que se desea informar. |
| 3    | El actor ingresa los criterios solicitados. |
| 4    | El sistema recopila los pagos acreditados según los criterios ingresados y genera el informe. |
| 5    | El sistema registra el reporte generado, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6    | El sistema pone el informe a disposición del actor para su descarga. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El reporte queda registrado en el historial de reportes generados.
  - El informe de ingresos queda disponible para su descarga.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Media — se genera periódicamente para el control financiero del proyecto.
- **Estabilidad**
  - Alta
- **Comentarios**
  - Solo contempla pagos con estado Acreditado.

---

### CU-84
**Consultar estadísticas**

- **Objetivo(s) asociado(s)**
  - OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**
  - RI-09: Información sobre reportes y estadísticas.
- **Módulo**
  - MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador consultar en pantalla los indicadores del sistema (alumnos inscriptos, tráfico e ingresos), sin necesidad de generar un reporte descargable.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor accede al panel de estadísticas. |
| 2    | El sistema recupera y muestra en pantalla los indicadores de alumnos inscriptos, tráfico e ingresos. |
| 3    | Fin del caso de uso. |

- **Salida**
  - Se muestran en pantalla los indicadores de alumnos inscriptos, tráfico e ingresos, sin generar un archivo descargable.
- **Excepciones**

| Paso | Acción |
|------|--------|

- **Frecuencia**
  - Alta — se consulta habitualmente como panel de control del negocio.
- **Estabilidad**
  - Alta
- **Comentarios**
  - A diferencia de CU-81, CU-82 y CU-83, esta consulta no queda registrada como un reporte generado.

---

## MOD-NF-04: Módulo de Configuración

### CU-85
**Configurar parámetros**

- **Objetivo(s) asociado(s)**
  - OBJ-10: Permitir la configuración de los parámetros operativos.
- **Requisito(s) de información asociado(s)**
  - RI-10: Información sobre configuración.
- **Módulo**
  - MOD-NF-04: Módulo de Configuración
- **Actor(es)**
  - Administrador
- **Descripción**
  - Permite al Administrador dar de alta, modificar, dar de baja y consultar los parámetros operativos del sistema mediante un esquema de clave-valor, sin requerir intervención técnica sobre el código.
- **Precondición(es)**
  - El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**

| Paso | Acción |
|------|--------|
| 1    | El caso de uso inicia cuando el actor solicita gestionar los parámetros de configuración. |
| 2    | El sistema lista los parámetros configurados: plazo de disponibilidad de grabaciones y antelación del aviso previo; cantidad máxima de sesiones concurrentes por usuario; datos institucionales usados en el sitio, comprobantes y constancias; credenciales de integración con Google OAuth y con la pasarela de pagos; proporción de tipos de pregunta en los bancos generados con IA; y tiempo límite de edición de consultas y respuestas del foro. |
| 3    | El actor selecciona un parámetro existente para modificar su valor, o ingresa una nueva clave y su valor para incorporar un parámetro nuevo. |
| 4    | El sistema valida que la clave y el valor hayan sido completados. |
| 5    | El sistema registra o actualiza el parámetro. |
| 6    | El sistema informa el éxito de la operación. |
| 7    | Fin del caso de uso. |

- **Postcondición(es)**
  - El parámetro queda registrado o actualizado con el nuevo valor.
- **Excepciones**

| Paso | Acción |
|------|--------|
| 4    | Si la clave o el valor no fueron completados, el sistema informa el error y vuelve al paso 3. |

- **Frecuencia**
  - Baja — se usa al ajustar el comportamiento operativo del sistema.
- **Estabilidad**
  - Alta
- **Comentarios**
  - El esquema de clave-valor permite incorporar nuevos parámetros sin modificar el esquema de la base de datos.