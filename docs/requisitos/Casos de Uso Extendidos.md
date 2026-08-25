# 4.7. Casos de Uso Extendidos

En esta sección se detallan los 99 casos de uso extendidos del Sistema Idóneos Online, organizados según los 10 módulos (6 funcionales y 4 no funcionales). Cada caso de uso se referencia mediante un código correlativo (CU-01 a CU-99).

## MOD-F-01: Módulo de Cursos

### CU-01: Buscar curso

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Administrador buscar uno o más cursos registrados en el sistema, o al Docente buscar entre los cursos en los que participa como titular o ayudante, con fines de gestión.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Debe existir al menos un curso activo.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más cursos. |
| 2 | El sistema solicita los criterios de búsqueda: nombre, categoría, nivel, equipo docente y modalidad. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los cursos que coincidan con los criterios ingresados. Si el actor es Docente, el sistema restringe el resultado a los cursos en los que participa como titular o ayudante. |
| 5 | El sistema lista los cursos filtrados. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más cursos que cumplen con los criterios de búsqueda, junto con su categoría, nivel, modalidades y equipo docente.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-02: Ver mis cursos

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno acceder al listado de cursos en los que está inscripto, para acceder al contenido de cualquiera de ellos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno debe tener al menos una inscripción.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar sus inscripciones. |
| 2 | El sistema solicita los criterios de búsqueda: nombre del curso y estado de la inscripción (Pendiente, En Progreso, Finalizado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra las inscripciones que coincidan con los criterios ingresados. |
| 5 | El sistema lista las inscripciones recuperadas, con su nombre e imagen del curso y el progreso general en cada uno. |
| 6 | El actor selecciona un curso para acceder al contenido del programa de su cohorte. |
| 7 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más cursos inscritos del alumno, y se accede al contenido del curso seleccionado.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el actor no posee ninguna inscripción, el sistema informa que no tiene cursos para mostrar y finaliza el caso de uso. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-03: Registrar curso

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar un nuevo curso, definiendo su información comercial y académica básica.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Debe existir al menos una categoría activa.
- Debe existir al menos un docente activo y habilitado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo curso. |
| 2 | El sistema solicita: nombre, descripción, precio, imagen de portada (opcional), categoría, nivel, si emite certificado al finalizar, modalidades de dictado, docente titular y, opcionalmente, docente ayudante. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios (nombre, descripción, precio, categoría, nivel, al menos una modalidad y docente titular). |
| 5 | El sistema valida que el precio ingresado sea mayor o igual a cero. |
| 6 | El sistema registra el curso, sin cohortes abiertas, con su equipo docente. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El curso queda registrado, sin cohortes abiertas, con su equipo docente (docente titular y, opcionalmente, docente ayudante).
- La fecha de creación refleja el momento del alta.
- Las modalidades de dictado indicadas quedan asociadas al curso.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3. |
| 4 | Si la categoría o alguno de los docentes no se encuentra activo o habilitado, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el docente titular es también ayudante del curso, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-04: Modificar curso

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar los datos de un curso registrado. Mientras el curso no posea cohortes con inscripción vigente, pueden modificarse todos sus datos (nombre, descripción, categoría, nivel, si emite certificado, modalidades, equipo docente, precio e imagen). Si posee alguna cohorte con inscripción vigente, sólo pueden modificarse el precio y la imagen de portada, que no son datos que afecten a quien ya pagó por el curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El curso debe estar activo.
- El curso no debe tener inscripciones activas asociadas.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso a modificar (ver CU-01: Buscar curso). |
| 2 | El sistema muestra los datos actuales del curso y, si posee alguna cohorte con inscripción vigente, indica que sólo pueden modificarse el precio y la imagen. |
| 3 | El actor modifica los datos habilitados: nombre, descripción, categoría, nivel, si emite certificado, modalidades, equipo docente, precio e imagen si el curso no posee cohortes con inscripción vigente; o precio y/o imagen si posee alguna. |
| 4 | El sistema valida que el curso esté activo, y sólo si posee alguna inscripción activa asociada, el actor no haya modificado datos distintos al precio o la imagen. |
| 5 | El sistema valida que se mantengan completos los campos obligatorios (nombre, descripción, categoría, nivel, al menos una modalidad y docente titular). |
| 6 | El sistema valida que el precio ingresado, si se modificó, sea mayor o igual a cero. |
| 7 | El sistema actualiza los datos del curso. |
| 8 | El sistema informa el éxito de la modificación. |
| 9 | Fin del caso de uso. |

**Postcondición(es)**
- El curso queda actualizado con los datos permitidos según si posee cohortes con inscripción vigente o no.
- La fecha de modificación refleja el momento del cambio.
- Las modalidades de dictado quedan actualizadas, si fueron modificadas.
- El equipo docente queda actualizado, si fue modificado.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el curso no se encuentra activo, el sistema informa error y cancela la operación. |
| 4 | Si el curso tiene alguna inscripción activa asociada y el actor modificó un dato distinto al precio o la imagen, el sistema informa que esos datos no pueden modificarse (ya que son datos que el alumno vio y pagó al inscribirse) y cancela la operación. |
| 5 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5 | Si la categoría o alguno de los docentes no se encuentra activo o habilitado, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el docente titular es también ayudante del curso, el sistema informa el error y vuelve al paso 3. |
| 6 | Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-05: Dar de baja curso

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja un curso. Si el curso posee programas asociados que no están en baja, el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El curso debe estar activo.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso a dar de baja (ver CU-01: Buscar curso). |
| 2 | El sistema verifica que no existan programas activos asociados al curso. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el curso como dado de baja y lo retira del catálogo público. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- El curso queda en baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el curso tiene algún programa activo asociado, el sistema informa la dependencia y no permite la baja; primero deben darse de baja sus programas (ver CU-19: Dar de baja programa). |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-06: Explorar catálogo de cursos

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno, con o sin sesión iniciada, explorar el catálogo público de cursos con cohortes abiertas y consultar la ficha de un curso específico (temática, nivel, modalidades, contenido gratuito de muestra y las cohortes con inscripción abierta) antes de decidir inscribirse.

**Precondición(es)**
- Debe existir al menos un curso con alguna cohorte con inscripción abierta.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor accede al catálogo público de cursos. |
| 2 | El sistema lista los cursos con cohortes abiertas, pudiendo filtrar por nombre, categoría, nivel, docente o modalidad del curso. |
| 3 | El actor selecciona un curso para ver su ficha. |
| 4 | El sistema muestra el detalle público del curso: descripción, nivel, modalidades, precio, contenidos por unidad, y las cohortes con inscripción abierta, con su docente titular y cupo disponible si corresponde. |
| 5 | Fin del caso de uso. |

**Salida**
Se recupera el listado de cursos con cohortes abiertas y, si corresponde, la ficha pública del curso seleccionado con sus cohortes con inscripción abierta.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-07: Buscar categoría

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador buscar una o más categorías temáticas registradas en el sistema.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Debe existir al menos una categoría activa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar una o más categorías. |
| 2 | El sistema solicita el criterio de búsqueda: nombre. |
| 3 | El actor ingresa el criterio que desea. |
| 4 | El sistema recupera y filtra las categorías que coincidan con el criterio ingresado. |
| 5 | El sistema lista las categorías filtradas. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | Fin del caso de uso. |

**Salida**
Se recuperan una o más categorías que cumplen con el criterio de búsqueda.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-08: Registrar categoría

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar una nueva categoría temática para clasificar los cursos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar una nueva categoría. |
| 2 | El sistema solicita: nombre y descripción (opcional). |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que el nombre haya sido completado y que no exista otra categoría activa con el mismo nombre. |
| 5 | El sistema registra la categoría en estado activo. |
| 6 | El sistema informa el éxito del registro. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La categoría queda registrada en estado activo.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el nombre no fue completado, el sistema informa el error y vuelve al paso 3. |
| 4 | Si ya existe una categoría activa con el mismo nombre, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-09: Modificar categoría

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar el nombre y la descripción de una categoría registrada, mientras no existan inscripciones vigentes que la utilicen.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La categoría debe estar activa.
- La categoría no debe tener inscripciones activas asociadas.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar (ver CU-07: Buscar categoría). |
| 2 | El sistema muestra los datos actuales de la categoría. |
| 3 | El actor modifica el nombre o la descripción. |
| 4 | El sistema valida que la categoría esté activa y no tenga inscripciones activas asociadas. |
| 5 | El sistema valida que el nombre no quede vacío y que no coincida con el de otra categoría activa. |
| 6 | El sistema actualiza los datos de la categoría. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La categoría queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si la categoría no se encuentra activa, el sistema informa error y cancela la operación. |
| 4 | Si la categoría tiene inscripciones activas asociadas, el sistema informa que la categoría no puede modificarse (ya que es un dato que el alumno vio y pagó al inscribirse) y cancela la operación. |
| 5 | Si el nombre queda vacío, el sistema informa el error y vuelve al paso 3. |
| 6 | Si el nombre coincide con el de otra categoría activa, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-10: Dar de baja categoría

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja una categoría activa. Si la categoría posee cursos asociados que no están en baja, el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La categoría debe estar activa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a dar de baja (ver CU-07: Buscar categoría). |
| 2 | El sistema verifica que no existan cursos activos asociados a la categoría. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca la categoría como dada de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La categoría queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si la categoría tiene cursos activos asociados, el sistema informa la dependencia y no permite la baja. |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-11: Buscar cohorte

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar las cohortes programadas de un programa.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Debe existir al menos una cohorte activa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar las cohortes de un programa. |
| 2 | El sistema solicita, opcionalmente, el estado (Abierta / En dictado / Finalizada) y el rango de fechas de inscripción. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra las cohortes del programa que coincidan con los criterios ingresados, con sus fechas de inscripción, fechas de dictado si corresponde, cupo máximo y semanas de acceso. |
| 5 | El sistema lista las cohortes filtradas. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | Fin del caso de uso. |

**Salida**
Se recupera el listado de cohortes del programa seleccionado.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-12: Registrar cohorte

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar una nueva cohorte para el programa vigente de un curso, definiendo su ventana de inscripción, cupo y semanas de acceso, y las fechas de dictado si la modalidad del curso incluye clases en vivo. Es lo que hace público el curso en el catálogo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El programa debe estar activo y vigente.
- El programa debe tener el mínimo de unidades establecido con material publicado en su cronograma.
- El curso no debe tener cohortes de programas anteriores sin finalizar.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa para el cual desea registrar un nuevo cohorte (ver CU-15: Buscar programa). |
| 2 | El sistema solicita: fecha de inicio y fin de inscripción, cupo máximo (opcional), semanas de acceso al contenido desde la inscripción y, si la modalidad del curso incluye clases en vivo, fecha de inicio y fin de dictado. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que el programa esté activo, vigente y cuente con el mínimo de unidades establecido con material publicado en su cronograma. Asimismo, valida que el curso no tenga ninguna cohorte de un programa anterior sin finalizar. |
| 5 | El sistema valida que se hayan completado los campos obligatorios (fechas de inscripción y semanas de acceso) y, si corresponde, las fechas de dictado. |
| 6 | El sistema valida que, si el curso incluye la modalidad En vivo, se hayan completado las fechas de dictado. |
| 7 | El sistema valida que la fecha de fin de inscripción sea posterior a la fecha de inicio de inscripción y, si corresponde, que la fecha de fin de dictado sea posterior a la fecha de inicio de dictado. |
| 8 | El sistema valida que, si corresponde, la fecha de inicio de dictado no sea anterior a la fecha de fin de inscripción. |
| 9 | El sistema valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 10 | El sistema valida que las semanas de acceso ingresados no sean menores a la duración total del cronograma del programa. |
| 11 | El sistema registra la cohorte, asociada al programa. |
| 12 | El sistema informa el éxito del registro. |
| 13 | Fin del caso de uso. |

**Postcondición(es)**
- La cohorte queda registrada, asociada al programa vigente del curso.
- El curso queda visible en el catálogo público mientras la cohorte tenga inscripción abierta.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el programa no se encuentra activo o vigente, el sistema informa el error y cancela la operación. |
| 4 | Si el programa no cuenta con el mínimo de unidades establecido con material publicado en su cronograma, el sistema informa el error y no permite registrar la cohorte. |
| 4 | Si el curso tiene cohortes de programas anteriores sin finalizar, el sistema informa el error y cancela la operación. |
| 5 | Si algún campo obligatorio no fue completado, el sistema informa cuáles faltan y vuelve al paso 3. |
| 6 | Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado, el sistema informa el error y vuelve al paso 3. |
| 7 | Si la fecha de fin de inscripción no es posterior a la fecha de inicio de inscripción, o la fecha de fin de dictado no es posterior a la fecha de inicio de dictado, el sistema informa el error y vuelve al paso 3. |
| 8 | Si la fecha de inicio de dictado es anterior a la fecha de fin de inscripción, el sistema informa el error y vuelve al paso 3. |
| 9 | Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 10 | Si las semanas de acceso ingresados son menores a la duración total del cronograma del programa, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-13: Modificar cohorte

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar la ventana de inscripción, el cupo máximo, las semanas de acceso o las fechas de dictado de una cohorte, mientras no posea inscripciones activas.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La cohorte debe estar activa y pertenecer al programa vigente.
- La cohorte no debe tener inscripciones activas asociadas.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cohorte a modificar (ver CU-11: Buscar cohorte). |
| 2 | El sistema muestra los datos actuales de la cohorte. |
| 3 | El actor modifica la fecha de inicio o fin de inscripción, el cupo máximo, las semanas de acceso o, si corresponde, las fechas de dictado. |
| 4 | El sistema valida que la cohorte esté activa, pertenezca al programa vigente y no posea inscripciones activas asociadas. |
| 5 | El sistema valida que se mantengan completos los campos obligatorios (fechas de inscripción y semanas de acceso) y, si corresponde, las fechas de dictado. |
| 6 | El sistema valida que, si el curso incluye la modalidad En vivo, se hayan completado las fechas de dictado. |
| 7 | El sistema valida que la fecha de fin de inscripción sea posterior a la fecha de inicio de inscripción y, si corresponde, que la fecha de fin de dictado sea posterior a la fecha de inicio de dictado. |
| 8 | El sistema valida que, si corresponde, la fecha de inicio de dictado no sea anterior a la fecha de fin de inscripción. |
| 9 | El sistema valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 10 | El sistema valida que las semanas de acceso ingresados no sean menores a la duración total del cronograma del programa. |
| 11 | El sistema actualiza los datos de la cohorte. |
| 12 | El sistema informa el éxito de la modificación. |
| 13 | Fin del caso de uso. |

**Postcondición(es)**
- La cohorte queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si la cohorte no se encuentra activa o no pertenece al programa vigente, el sistema informa el error y cancela la operación. |
| 4 | Si la cohorte tiene inscripciones activas asociadas, el sistema informa que estos datos no pueden modificarse (ya que son datos que el alumno vio y pagó al inscribirse) y cancela la operación. |
| 5 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 6 | Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado, el sistema informa el error y vuelve al paso 3. |
| 7 | Si la fecha de fin de inscripción no es posterior a la fecha de inicio de inscripción, o la fecha de fin de dictado no es posterior a la fecha de inicio de dictado, el sistema informa el error y vuelve al paso 3. |
| 8 | Si la fecha de inicio de dictado es anterior a la fecha de fin de inscripción, el sistema informa el error y vuelve al paso 3. |
| 9 | Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 10 | Si las semanas de acceso ingresados son menores a la duración total del cronograma del programa, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-14: Dar de baja cohorte

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-01: Módulo de Cursos

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja (cancelar) una cohorte. Si la cohorte posee alguna inscripción activa (vigente o finalizada, sin dar de baja), el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La cohorte debe estar activa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cohorte a dar de baja (ver CU-11: Buscar cohorte). |
| 2 | El sistema verifica que la cohorte no posea inscripciones activas. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca la cohorte como dada de baja (Cancelada). |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La cohorte queda en baja (Cancelada).

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si la cohorte tiene inscripciones activas asociadas, el sistema informa la dependencia y no permite la baja. |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-02: Módulo de Gestión Académica

### CU-15: Buscar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar los programas (versiones del plan de estudios) registrados para un curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El curso debe estar activo.
- El curso debe tener al menos un programa activo.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso se invoca, ya sea directamente por el actor o indirectamente desde otro caso de uso, para resolver el programa de un curso sobre el cual actuar. |
| 2 | Por defecto, el sistema resuelve al programa vigente del curso, o al último programa al que el actor lo haya cambiado previamente. |
| 3 | Si el actor desea actuar sobre un programa distinto, solicita buscarlo. |
| 4 | El sistema solicita, opcionalmente, el nombre y el estado (Vigente / Anterior / Dado de baja). |
| 5 | El actor ingresa los criterios de búsqueda que desea. |
| 6 | El sistema recupera y filtra los programas del curso que coincidan con los criterios ingresados, indicando cuál se encuentra vigente. |
| 7 | El sistema lista los programas filtrados. |
| 8 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 9 | Fin del caso de uso. |

**Salida**
Se recupera el listado de programas del curso seleccionado, con su nombre, descripción, objetivos, carga horaria total, cantidad de unidades en su cronograma e indicación de cuál es el vigente.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-16: Registrar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante registrar un nuevo programa para un curso, definiendo una nueva versión de su plan de estudios, opcionalmente a partir de la información de un programa anterior del mismo curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El curso debe estar activo.
- El docente participa en él como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso para el cual desea registrar un nuevo programa (ver CU-01: Buscar curso). |
| 2 | El sistema le ofrece iniciar de la información de un programa anterior del curso, si existe alguno, y solicita: nombre, descripción (opcional), objetivos, carga horaria total (opcional) y bibliografía. |
| 3 | El actor opcionalmente selecciona un programa anterior del cual partir, e ingresa o modifica los datos solicitados. |
| 4 | El sistema valida que el curso esté activo, el docente participa en él como titular o ayudante, y el nombre, los objetivos y la bibliografía hayan sido completados. |
| 5 | El sistema registra el programa, que pasa a ser el vigente del curso al ser el de fecha de creación más reciente, dejando de considerarse vigente al programa anterior, si existía. |
| 6 | Si el actor seleccionó partir de un programa anterior, el sistema copia el cronograma de ese programa (unidades, número de orden y semanas de duración de cada una) al nuevo programa. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El programa queda registrado y pasa a ser el vigente del curso, al ser el de fecha de creación más reciente.
- El programa anterior del curso, si existía, deja de estar vigente, sin perder su cronograma ni afectar a los alumnos ya inscriptos en cohortes abiertas bajo ese programa.
- Si se partió de un programa anterior, el nuevo programa queda con el mismo cronograma que ese programa, como punto de partida editable.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el curso no se encuentra activo, el sistema informa el error y cancela la operación. |
| 4 | Si el docente no participa en él como titular o ayudante, el sistema informa el error y cancela la operación. |
| 4 | Si el nombre, los objetivos o la bibliografía no fueron completados, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-17: Modificar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante modificar el nombre, la descripción, los objetivos, la carga horaria total o la bibliografía de un programa.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa debe estar activo.
- El programa no debe tener inscripciones activas asociadas.
- El docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a modificar (ver CU-15: Buscar programa). |
| 2 | El sistema muestra los datos actuales del programa. |
| 3 | El actor modifica el nombre, la descripción, los objetivos, la carga horaria total o la bibliografía. |
| 4 | El sistema valida que el programa esté activo, no existan inscripciones activas asociadas y el docente participa en él como titular o ayudante. |
| 5 | El sistema valida que el nombre, los objetivos y la bibliografía no queden vacíos. |
| 6 | El sistema actualiza los datos del programa. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El programa queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el programa no se encuentra activo, el sistema informa el error y cancela la operación. |
| 4 | Si el programa tiene inscripciones activas asociadas, el sistema informa que estos datos no pueden modificarse (ya que son datos que el alumno vio y pagó al inscribirse) y cancela la operación. |
| 4 | Si el docente no participa en él como titular o ayudante, el sistema informa el error y cancela la operación. |
| 5 | Si el nombre, los objetivos o la bibliografía quedan vacíos, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-18: Dar de baja programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador dar de baja un programa que nunca llegó a tener cohortes asociadas (por ejemplo, un alta registrada por error). Si el programa posee o tuvo alguna cohorte, el sistema informa la dependencia y no permite la baja, para preservar el historial académico de los alumnos que cursaron bajo él. La baja por parte del Administrador se reserva a casos de moderación.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa debe estar activo.
- Si el actor es Docente, participa en él como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a dar de baja (ver CU-15: Buscar programa). |
| 2 | El sistema verifica que el docente participe en él como titular o ayudante, y que el programa no tenga ninguna cohorte asociada. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el programa como dado de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- El programa queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el docente no participa en él como titular o ayudante, el sistema informa el error y cancela la operación. |
| 2 | Si el programa tiene cohortes activas asociadas, el sistema informa la dependencia y no permite la baja; para eliminarlo, primero deben darse de baja sus cohortes (ver CU-14: Dar de baja cohorte). |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-19: Buscar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar las unidades incluidas en el cronograma de un programa de un curso, con fines de gestión de su contenido.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa debe estar activo.
- Si el actor es Docente, participa en él como titular o ayudante.
- El programa debe tener al menos una unidad incluida dentro de su cronograma.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar las unidades de un programa. |
| 2 | El sistema recupera y lista las unidades incluidas en el cronograma del programa, mostrando el título y la descripción de cada unidad. |
| 3 | El actor puede seleccionar una unidad para gestionar su contenido. |
| 4 | El sistema despliega el contenido de la unidad seleccionada: su material, sus términos de glosario, sus pools de preguntas, sus autoevaluaciones y sus clases (en vivo y con Clon de IA), con las opciones para darlo de alta, modificarlo o eliminarlo. |
| 5 | Fin del caso de uso. |

**Salida**
Se recupera el listado de unidades que componen el cronograma del programa. Al seleccionar una unidad, se despliega su material, sus pools, sus autoevaluaciones y sus clases, con las opciones para gestionarlos.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-20: Agregar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante registrar una nueva unidad, o incorporar una ya existente en otro programa del mismo curso, al cronograma del programa vigente del curso. En ambos casos, la unidad queda incorporada al final del cronograma, con una semana de duración por defecto.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa debe estar activo.
- El docente participa en él como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa para el cual desea agregar una nueva unidad (ver CU-15: Buscar programa). |
| 2 | El sistema solicita si desea crear una unidad nueva o incorporar una ya existente en otro programa del mismo curso y, en este último caso, recupera y lista las unidades de otros programas del curso que todavía no formen parte del cronograma del programa vigente. |
| 3 | El actor ingresa el título, la descripción (opcional) y el contenido si crea una unidad nueva, o selecciona la unidad existente a incorporar. |
| 4 | El sistema valida que, el programa esté activo, el docente participe en él como titular o ayudante, y si se creó una unidad nueva, que el título y el contenido hayan sido completados. |
| 5 | El sistema registra la unidad, si es nueva. |
| 6 | El sistema incorpora la unidad (nueva o existente) al cronograma del programa vigente del curso, en la última posición, con una semana de duración por defecto. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La unidad queda registrada, si era nueva.
- La unidad (nueva o existente) queda incorporada al cronograma del programa vigente del curso, con el número de orden siguiente al último y una semana de duración.
- La fecha de creación refleja el momento del alta, si la unidad era nueva.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el programa no se encuentra activo, el sistema informa el error y cancela la operación. |
| 4 | Si el docente no participa en él como titular o ayudante, el sistema informa el error y cancela la operación. |
| 4 | Si el título o el contenido no fueron completados, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-21: Modificar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante modificar el título, la descripción o el contenido de una unidad, mientras no exista alguna cohorte con inscripción vigente en un programa cuyo cronograma la incluya.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe y no se encuentra en baja, y el docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la unidad a modificar (ver CU-20: Buscar unidad). |
| 2 | El sistema muestra los datos actuales de la unidad. |
| 3 | El actor modifica el título, la descripción o el contenido. |
| 4 | El sistema valida que la unidad no pertenezca al cronograma de un programa con alguna cohorte con inscripción vigente. |
| 5 | El sistema valida que el título y el contenido no queden vacíos. |
| 6 | El sistema actualiza los datos de la unidad. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La unidad queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si la unidad pertenece al cronograma de un programa con alguna cohorte con inscripción vigente, el sistema informa que su contenido no puede modificarse mientras haya alumnos inscriptos en cohortes que la incluyen (ya que es contenido que el alumno ya vio al inscribirse, sin necesidad de haber pagado) y cancela la operación. |
| 5 | Si el título o el contenido quedan vacíos, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-22: Quitar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador (con fines de moderación) quitar una unidad del cronograma de un programa. Si al quitarla deja de formar parte del cronograma de cualquier otro programa, el sistema la da de baja automáticamente.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe, no se encuentra en baja, y pertenece al cronograma del programa desde el cual se la quiere quitar.
- Si el actor es Docente, participa como titular o ayudante en el curso de ese programa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la unidad a dar de baja (ver CU-20: Buscar unidad). |
| 2 | El sistema valida que no existan intentos de autoevaluación asociados por algún alumno sobre autoevaluaciones de esta unidad. |
| 3 | El sistema valida que la unidad no pertenezca al cronograma de un programa con alguna cohorte con inscripción vigente. |
| 4 | El actor confirma la operación. |
| 5 | El sistema quita la unidad del cronograma de ese programa. |
| 6 | El sistema valida si la unidad sigue formando parte del cronograma de algún otro programa. |
| 7 | Si ya no forma parte de ningún cronograma, el sistema la marca como dada de baja. |
| 8 | El sistema informa el éxito de la operación. |
| 9 | Fin del caso de uso. |

**Postcondición(es)**
- La unidad queda quitada del cronograma de ese programa.
- Si ya no pertenece a ningún otro cronograma, queda además en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si ya existe algún intento de autoevaluación asociado sobre esta unidad, el sistema informa la dependencia y no permite la operación. |
| 3 | Si el programa posee alguna cohorte con inscripción vigente, el sistema informa que no puede quitarse mientras haya alumnos activos y cancela la operación. |
| 4 | Si el actor no confirma, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-23: Buscar cronograma

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador consultar el cronograma de un programa, incluyendo las unidades que lo componen, en qué orden y con qué duración.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa cuyo cronograma desea consultar. |
| 2 | El sistema recupera y lista las unidades del cronograma del programa, ordenadas por su número de orden, con su duración en semanas. |
| 3 | Fin del caso de uso. |

**Salida**
Se recupera el cronograma del programa seleccionado: sus unidades, con título, número de orden, semanas de duración y cantidad de material cargado.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-24: Modificar cronograma

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante modificar el cronograma de un programa: reordenar sus unidades arrastrándolas y soltándolas, modificar la duración de cada una.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa existe, no se encuentra en baja, y el docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa cuyo cronograma desea modificar (ver CU-23: Buscar cronograma). |
| 2 | El sistema muestra las unidades del cronograma actual, en su orden y con su duración. |
| 3 | El actor reordena las unidades y/o modifica la duración en semanas de una o más unidades. |
| 4 | El sistema valida que no existan cohortes de este programa con al menos una inscripción vigente. |
| 5 | El sistema valida que cada duración ingresada sea un número entero de semanas mayor a cero. |
| 6 | El sistema actualiza el cronograma. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El cronograma del programa queda actualizado, con sus unidades en el nuevo orden, con la nueva duración.
- La fecha de modificación del programa refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el programa posee alguna cohorte con al menos una inscripción vigente, el sistema informa que el cronograma no puede modificarse mientras haya alumnos cursando bajo este programa (ya que es lo que el alumno vio y pagó al inscribirse) y cancela la operación. |
| 5 | Si alguna duración ingresada no es un número entero de semanas mayor a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-25: Ver participantes

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catálogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catálogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador consultar los participantes de un curso (el equipo docente y los alumnos inscriptos en todas sus cohortes).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Si es Docente, participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver los participantes de un curso. |
| 2 | El sistema solicita, opcionalmente, nombre o apellido y el rol (Alumno / Docente). |
| 3 | El actor ingresa los criterios que desea. |
| 4 | El sistema recupera y lista el equipo docente y los alumnos de todas las cohortes del curso, filtrados por los criterios. |
| 5 | Fin del caso de uso. |

**Salida**
Se recupera el listado de participantes que cumplen los criterios, con nombre, apellido, rol y, para el equipo docente, si es titular o ayudante.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si ningún participante coincide con los criterios, el sistema informa que no se encontraron resultados. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-26: Acceder curso

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno acceder a la información de un curso en el que está inscripto: el cronograma con su avance, el contenido de sus unidades, los participantes del curso y sus propias calificaciones.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente al curso. Si la cohorte del alumno posee fechas de dictado, la fecha actual no debe ser anterior a la fecha de inicio de dictado.
- El programa de la cohorte del alumno posee al menos una unidad en su cronograma.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno solicita buscar las unidades de un curso en el que está inscripto. |
| 2 | El alumno puede, alternativamente: (a) consultar el cronograma del curso; (b) buscar las unidades del curso para acceder al contenido de una de ellas; (c) consultar los participantes del curso; (d) consultar sus propias calificaciones. |
| 3 | Si el alumno solicitó el cronograma, el sistema recupera y lista las unidades del programa de su cohorte, con título, número de orden y duración en semanas, y calcula la semana esperada de avance según la fecha de inicio de dictado o de inscripción de la cohorte y la duración acumulada de las unidades; si el alumno va por detrás de lo esperado respecto de la última unidad con progreso completado, el sistema lo indica. |
| 4 | Si el alumno solicitó buscar las unidades, el sistema recupera y lista las unidades del programa de su cohorte, indicando si cada una se encuentra habilitada según su avance secuencial, para que el alumno seleccione una. |
| 5 | Si el alumno seleccionó una unidad, el sistema verifica que se encuentre habilitada según su avance secuencial (el progreso de la unidad anterior figura como completado, o es la primera unidad del curso). |
| 6 | El sistema despliega el contenido publicado de la unidad: su material, sus términos de glosario, el acceso al foro y, si corresponde, el acceso a las autoevaluaciones (junto con los intentos que el alumno ya haya registrado sobre cada una) y a las clases en vivo programadas para poder ingresar. |
| 7 | Si el alumno solicitó ver los participantes, el sistema recupera y lista el equipo docente completo y los alumnos de su propia cohorte. |
| 8 | Si el alumno solicitó ver sus calificaciones, el sistema recupera, para cada autoevaluación rendida en el programa de su cohorte, la nota y el resultado. |
| 9 | Fin del caso de uso. |

**Salida**
Según lo que el alumno consulte: el cronograma del programa de su cohorte (unidades, orden, duración) con indicación de atraso si corresponde; o el listado de unidades indicando cuáles están habilitadas, con el contenido publicado de la seleccionada (material, glosario, foro, autoevaluaciones con intentos propios y clases en vivo programadas); o el listado de participantes del curso (equipo docente y compañeros de cohorte); o las calificaciones propias por autoevaluación rendida.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 5 | Si la unidad todavía no está habilitada para el alumno, el sistema informa que debe aprobar primero la autoevaluación de la unidad anterior y no despliega su contenido. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-27: Buscar material

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar el material (grabaciones, bibliografía, presentaciones y resúmenes) cargado en una unidad, con fines de gestión.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe y posee al menos un material cargado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar material dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el tipo de material, el título y si fue generado por IA. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista el material que coincide con los criterios, incluyendo el no publicado. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el material de la unidad, indicando su tipo, título, si fue generado por IA y su estado de publicación.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-28: Subir material

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante cargar manualmente un material (grabación, bibliografía o presentación) en una unidad. Cada tipo de material solicita datos específicos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe, no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor desea subir un nuevo material dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita el tipo de material (Grabación, Bibliografía o Presentación) y el título. |
| 3 | El actor selecciona el tipo e ingresa el título. |
| 4 | Según el tipo elegido, el sistema solicita: el archivo de video (Grabación); el archivo o enlace externo y el autor (Bibliografía); o el archivo de la presentación (Presentación). |
| 5 | El actor ingresa los datos solicitados. |
| 6 | El sistema valida que se hayan completado el título, el tipo y los datos obligatorios según el tipo seleccionado. |
| 7 | El sistema registra el material en estado oculto. |
| 8 | El sistema informa el éxito de la carga. |
| 9 | Fin del caso de uso. |

**Postcondición(es)**
- El material queda registrado, asociado a la unidad, en estado no publicado.
- La fecha de creación refleja el momento de la carga.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 6 | Si no se completó el título o alguno de los datos obligatorios según el tipo de material, el sistema informa el error y vuelve al paso 5. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-29: Modificar material

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante modificar el título de un material y, en particular, su estado de publicación para habilitarlo u ocultarlo a los alumnos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El material existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a modificar (ver CU-28: Buscar material). |
| 2 | El sistema muestra los datos actuales del material. |
| 3 | El actor modifica el título, el archivo o el estado de publicación. |
| 4 | El sistema valida que el título no quede vacío. |
| 5 | Si se modificó el archivo, el sistema valida que se hayan completado los datos obligatorios correspondientes al tipo de material. |
| 6 | El sistema actualiza los datos del material. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El material queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el título queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el nuevo archivo no cumple los datos obligatorios del tipo de material, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- Publicar un material lo hace visible para los alumnos con acceso habilitado a esa unidad.

---

### CU-30: Dar de baja material

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador dar de baja un material cargado en una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El material existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a dar de baja (ver CU-28: Buscar material). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca el material como dado de baja y deja de mostrarlo a los alumnos. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- El material queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-31: Buscar término de glosario

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar los términos del glosario cargados en una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe y posee al menos un término de glosario cargado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar términos dentro del glosario de una unidad. |
| 2 | El sistema solicita opcionalmente el término o la definición. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los términos de glosario que coinciden con los criterios ingresados. |
| 5 | El actor puede seleccionar un término en específico. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el listado de términos y definiciones del glosario de la unidad.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-32: Registrar término de glosario

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante registrar un nuevo término y su definición en el glosario de una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe, no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor desea registrar un término de glosario dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita: término y definición. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que el término y la definición hayan sido completados y que el término no esté ya registrado en el glosario de esa unidad. |
| 5 | El sistema registra el término de glosario asociado a la unidad. |
| 6 | El sistema informa el éxito del registro. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- El término de glosario queda registrado y asociado a la unidad.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el término o la definición no fueron completados, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el término ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-33: Modificar término de glosario

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular/ayudante modificar el término o la definición de un término de glosario registrado.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El término de glosario existe y no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a modificar (ver CU-32: Buscar término de glosario). |
| 2 | El sistema muestra los datos actuales del término. |
| 3 | El actor modifica el término o la definición. |
| 4 | El sistema valida que ninguno de los dos campos quede vacío. |
| 5 | Si se modificó el término, el sistema valida que no coincida con el de otro término ya registrado en el glosario de esa unidad. |
| 6 | El sistema actualiza el término de glosario. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El término de glosario queda actualizado con los nuevos datos.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el término o la definición quedan vacíos, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el término modificado ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-34: Dar de baja término de glosario

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador dar de baja un término del glosario de una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El término de glosario existe y no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a dar de baja (ver CU-32: Buscar término de glosario). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca el término de glosario como dado de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- El término de glosario queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-35: Buscar consulta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite consultar las preguntas planteadas por los alumnos en el foro de una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una consulta de foro registrada en la unidad.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar consultas dentro del foro de una unidad. |
| 2 | El sistema solicita opcionalmente el texto o el rango de fechas. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las consultas de foro que coinciden con los criterios, con sus respuestas asociadas si existen. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el listado de consultas del foro de la unidad, junto con sus respuestas.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-36: Registrar consulta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno registrar una consulta en el foro de una unidad del curso en el que está inscripto.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente a una cohorte de un programa cuyo cronograma incluye la unidad.
- La unidad se encuentra habilitada según el avance secuencial del alumno.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar una consulta en el foro dentro de una unidad (ver CU-27: Ver contenido de un curso). |
| 2 | El sistema solicita el texto de la consulta. |
| 3 | El actor ingresa el texto. |
| 4 | El sistema valida que el texto haya sido completado. |
| 5 | El sistema registra la consulta asociada a la unidad y al alumno, con la fecha actual. |
| 6 | El sistema notifica al docente titular y al ayudante, si corresponde, la nueva consulta. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La consulta queda registrada, asociada a la unidad y al alumno.
- El docente recibe la notificación de la nueva consulta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el texto de la consulta no fue completado, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-37: Modificar consulta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno modificar el texto de una consulta de foro propia, dentro de un plazo límite configurable desde su registro.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- La consulta de foro existe, no se encuentra en baja y fue registrada por el actor.
- No se superó el plazo límite de edición configurado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro propia a modificar (ver CU-36: Buscar consulta de foro). |
| 2 | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la consulta. |
| 3 | El sistema muestra el texto actual de la consulta. |
| 4 | El actor modifica el texto. |
| 5 | El sistema valida que el texto no quede vacío. |
| 6 | El sistema actualiza la consulta. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La consulta queda actualizada con el nuevo texto.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si se superó el plazo límite de edición, el sistema informa que la consulta ya no puede modificarse y finaliza el caso de uso. |
| 5 | Si el texto queda vacío, el sistema informa el error y vuelve al paso 4. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-38: Dar de baja consulta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja una consulta de foro ante una publicación indebida (por ejemplo, contenido ofensivo o ajeno a la temática de la unidad).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La consulta de foro existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro a dar de baja (ver CU-36: Buscar consulta de foro). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la consulta como dada de baja, junto con las respuestas asociadas si existen. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- La consulta y sus respuestas asociadas quedan en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- No surge de la entrevista con el cliente como requisito explícito; se incorpora como criterio de moderación razonable para un foro con alumnos y docentes.

---

### CU-39: Buscar respuesta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite consultar las respuestas registradas a una consulta de foro.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una respuesta registrada para la consulta.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver las respuestas de una consulta de foro. |
| 2 | El sistema recupera y lista las respuestas asociadas a la consulta. |
| 3 | El actor puede seleccionar un respuesta en específico. |
| 4 | Fin del caso de uso. |

**Salida**
Se recupera el listado de respuestas asociadas a la consulta de foro.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-40: Registrar respuesta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular o ayudante del curso registrar una respuesta a una consulta de foro planteada por un alumno.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La consulta de foro existe, no se encuentra en baja y el actor participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita responder una consulta de foro (ver CU-36: Buscar consulta de foro). |
| 2 | El sistema solicita el texto de la respuesta. |
| 3 | El actor ingresa el texto. |
| 4 | El sistema valida que el texto haya sido completado. |
| 5 | El sistema registra la respuesta asociada a la consulta y al docente, con la fecha actual. |
| 6 | El sistema notifica al alumno autor de la consulta que fue respondida. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La respuesta queda registrada, asociada a la consulta y al docente.
- El alumno recibe la notificación de la respuesta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el texto de la respuesta no fue completado, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-41: Modificar respuesta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente

**Descripción**
Permite al Docente modificar el texto de una respuesta de foro propia, dentro de un plazo límite configurable desde su registro.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La respuesta de foro existe, no se encuentra en baja y fue registrada por el actor.
- No se superó el plazo límite de edición configurado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro propia a modificar (ver CU-40: Buscar respuesta de foro). |
| 2 | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la respuesta. |
| 3 | El sistema muestra el texto actual de la respuesta. |
| 4 | El actor modifica el texto. |
| 5 | El sistema valida que el texto no quede vacío. |
| 6 | El sistema actualiza la respuesta. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La respuesta queda actualizada con el nuevo texto.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si se superó el plazo límite de edición, el sistema informa que la respuesta ya no puede modificarse y finaliza el caso de uso. |
| 5 | Si el texto queda vacío, el sistema informa el error y vuelve al paso 4. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-42: Dar de baja respuesta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja una respuesta de foro ante una publicación indebida.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La respuesta de foro existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro a dar de baja (ver CU-40: Buscar respuesta de foro). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la respuesta como dada de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- La respuesta queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-03: Módulo de Inscripciones

### CU-43: Buscar inscripción

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Alumno, Administrador

**Descripción**
Permite consultar una o más inscripciones registradas en el sistema, incluyendo el certificado de finalización emitido cuando corresponda. La vista varía según el rol del actor: el Alumno visualiza únicamente las propias; el Administrador visualiza todas.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
- Existe al menos una inscripción registrada previamente.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar una o más inscripciones. |
| 2 | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador) y estado (Vigente / Vencida / Dada de baja). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra las inscripciones que coincidan con los criterios ingresados, restringidas a las propias si el actor es Alumno. |
| 5 | El sistema lista las inscripciones filtradas. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | El actor puede generar el certificado de la inscripción seleccionada, si fue emitido. |
| 8 | Fin del caso de uso. |

**Salida**
Se recuperan una o más inscripciones que cumplen con los criterios de búsqueda, con su curso, fecha, fecha de vencimiento de acceso, estado y, si el certificado fue emitido, sus datos (número y fecha de emisión) con el archivo para descargar sí lo necesita.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-44: Inscribir curso

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno inscribirse a una cohorte con inscripción abierta de un curso publicado, dando inicio al proceso de inscripción que se completa con el pago del curso si tiene costo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El curso posee al menos una cohorte con inscripción abierta.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, tras explorar el catálogo y consultar la ficha pública de un curso (ver CU-06: Explorar catálogo de cursos), selecciona una cohorte con inscripción abierta y solicita inscribirse. |
| 2 | El sistema valida que la fecha actual se encuentre dentro de la ventana de inscripción de la cohorte seleccionada. |
| 3 | El sistema valida que la cohorte no haya alcanzado su cupo máximo, si tiene uno definido. |
| 4 | El sistema valida que el alumno no posea ya una inscripción vigente a esa cohorte. |
| 5 | El sistema registra la inscripción, con la fecha actual. Si la cohorte no posee fechas de dictado, calcula la fecha de vencimiento de acceso sumando las semanas de acceso de la cohorte (duración del cronograma) a la fecha actual. Si la cohorte posee fechas de dictado, calcula tanto el inicio como el vencimiento de acceso a partir de la fecha de inicio de dictado (fecha de inicio de dictado + semanas de acceso), alineando todas las inscripciones con el dictado de clases en vivo y cronograma. |
| 6 | El sistema registra el progreso inicial del alumno sobre la primera unidad del cronograma del programa de la cohorte, sin completar (0% de avance). |
| 7 | Si el curso tiene costo, el sistema deriva al alumno al pago (CU-48: Realizar pago) y aguarda su resultado. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La inscripción queda registrada, con el progreso inicial del alumno registrado sobre la primera unidad del cronograma del programa de la cohorte, sin completar.
- Si el curso tiene costo, la inscripción queda sin el acceso al contenido hasta que se confirme el pago, y, si la cohorte posee fechas de dictado, hasta que además se alcance la fecha de inicio de dictado.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si la fecha actual está fuera de la ventana de inscripción de la cohorte, el sistema informa que la inscripción a esa cohorte ya no está habilitada y finaliza el caso de uso. |
| 3 | Si la cohorte ya alcanzó su cupo máximo, el sistema lo informa y finaliza el caso de uso. |
| 4 | Si el alumno ya posee una inscripción vigente a esa cohorte, el sistema lo informa y finaliza el caso de uso. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-45: Dar de baja inscripción

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Alumno, Administrador

**Descripción**
Permite dar de baja una inscripción. El Alumno puede darse de baja de un curso en el que está inscripto, registrando el abandono; el Administrador puede darla de baja ante la detección de fraude (uso compartido de credenciales, suplantación en una autoevaluación, etc.). Ninguna de las dos vías genera reembolso del pago realizado si corresponde.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- La inscripción existe, se encuentra vigente y, si el actor es Alumno, le pertenece.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la inscripción a dar de baja (ver CU-44: Buscar inscripción). |
| 2 | El sistema solicita confirmación y, opcionalmente, un motivo u observación de la baja. |
| 3 | El actor confirma la baja y, si lo desea, ingresa el motivo. |
| 4 | El sistema informa que la baja no genera reembolso del pago realizado y solicita confirmación final. |
| 5 | El actor confirma. |
| 6 | El sistema registra la baja de la inscripción, con la observación ingresada si corresponde. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La inscripción queda en estado de baja.
- El alumno pierde el acceso al contenido del curso.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- No existe mecanismo de reembolso: la política de no devolución fue confirmada por el cliente, análoga a la habitual en el ámbito universitario.

---

### CU-46: Buscar pago

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Alumno, Administrador

**Descripción**
Permite consultar uno o más pagos registrados en el sistema. La vista varía según el rol del actor: el Alumno visualiza únicamente los propios; el Administrador visualiza todos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
- Existe al menos un pago registrado previamente.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más pagos. |
| 2 | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador), estado (Pendiente / Acreditado / Rechazado) y rango de fecha. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los pagos que coincidan con los criterios ingresados, restringidos a los propios si el actor es Alumno. |
| 5 | El sistema lista los pagos filtrados. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | El actor puede generar el comprobante del pago seleccionado, si el pago fue acreditado. |
| 8 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más pagos que cumplen con los criterios de búsqueda, con su monto, fecha, método, estado y, si el pago fue acreditado, los datos del comprobante (número y fecha de emisión) con el archivo para descargar sí lo necesita.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-47: Realizar pago

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Alumno, indirectamente mediante CU-44: Inscribir curso

**Descripción**
Permite al Alumno pagar de forma online un curso al que se desea inscribir, con MODO como método de pago mediante integración con la API de MODO, por el total del curso con el descuento aplicado si corresponde (ver PA-2: Pago online con billetera virtual).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno ya posee una inscripción registrada para el curso, sin el pago realizado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno solicita pagar el curso al que se inscribió. |
| 2 | El sistema evalúa si el alumno cumple alguna condición de descuento vigente y, de ser así, calcula el monto a pagar con el descuento aplicado (ver PA-3: Aplicación automática de descuentos), y muestra al alumno el monto a pagar. |
| 3 | El sistema recupera y muestra los medios de pago disponibles. |
| 4 | El alumno selecciona MODO como medio de pago. |
| 5 | El sistema arma la solicitud de pago por ese monto y se la envía a MODO. |
| 6 | El sistema muestra el modal de pago de MODO: en computadora, un código QR para escanear con la app de MODO o con la app del banco; en celular, lo redirige a una pantalla para elegir con qué app quiere pagar. |
| 7 | El alumno escanea el código QR (o abre la app elegida desde el celular) y completa el pago desde ahí, con la tarjeta o el saldo de cuenta que prefiera. |
| 8 | El sistema registra el pago como pendiente, con los datos que devuelve MODO al crear la solicitud. |
| 9 | MODO procesa el pago y le avisa al sistema el resultado (acreditado o rechazado) más adelante, de forma automática. |
| 10 | El sistema actualiza el pago con el resultado, el nombre del pagador y, si fue con tarjeta, sus últimos cuatro dígitos, y guarda la fecha en que se aprobó. |
| 11 | Si el pago fue acreditado, el sistema habilita el acceso al curso, genera los datos del comprobante y se lo envía al alumno por correo, con el archivo para descargar cuando lo necesite. |
| 12 | Fin del caso de uso. |

**Postcondición(es)**
- El pago queda registrado con su resultado, asociado a la inscripción.
- Si el pago fue acreditado: el acceso al curso de la inscripción queda habilitado, y los datos del comprobante quedan registrados en el pago y enviados por correo electrónico al alumno.
- Si el pago fue rechazado: la inscripción permanece sin acceso habilitado y se notifica al alumno.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 6 | Si el alumno cancela el pago desde el modal de MODO, el sistema informa que la operación fue cancelada y finaliza el caso de uso. |
| 9 | Si MODO rechaza el pago, el sistema registra el pago como rechazado y notifica al alumno el motivo, permitiéndole reintentar. |
| 9 | Si no se recibe la confirmación de MODO dentro del plazo configurado, el sistema registra el pago como rechazado, cancela la operación y notifica al alumno el motivo, permitiéndole reintentar. |

**Frecuencia**
- Alta

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-48: Buscar progreso

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente o Administrador consultar el progreso de los alumnos en las unidades del programa de la cohorte en la que está inscripto.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita consultar los progresos de los alumnos en un curso. |
| 2 | El sistema solicita opcionalmente los criterios de búsqueda: alumno. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera los progresos de los alumnos en el curso. |
| 5 | El sistema lista el progreso recuperado. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle en cada unidad del programa de su cohorte. |
| 7 | Fin del caso de uso. |

**Salida**
Se recupera el progreso de los alumnos en el curso. Al seleccionar un progreso, se muestra detalladamente cada unidad del programa de la cohorte del alumno, si fue completada y, en caso afirmativo, la fecha en que se completó.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-49: Buscar descuento

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador buscar uno o más descuentos registrados en el sistema.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Existe al menos un descuento registrado previamente.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más descuentos. |
| 2 | El sistema solicita los criterios de búsqueda: nombre y vigencia (Vigente / Vencido / Agotado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los descuentos que coincidan con los criterios ingresados. |
| 5 | El sistema lista los descuentos filtrados. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más descuentos que cumplen con los criterios de búsqueda, con su porcentaje, vigencia, cantidad límite y cantidad usada.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-50: Registrar descuento

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar un nuevo descuento a aplicar automáticamente a los alumnos que cumplan la condición configurada.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento. |
| 2 | El sistema solicita: nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite ofertada y, opcionalmente, la cantidad de cursos que el alumno debe haber comprado como condición. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios (nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite). |
| 5 | El sistema valida que el porcentaje ingresado sea un valor entre 1 y 100. |
| 6 | El sistema valida que la vigencia hasta sea posterior a la vigencia desde. |
| 7 | El sistema valida que la cantidad límite ingresada sea un número entero mayor a cero. |
| 8 | Si el actor completó la cantidad de cursos requeridos, el sistema valida que sea un número entero mayor o igual a cero. |
| 9 | El sistema registra el descuento, con cantidad usada en cero. |
| 10 | El sistema informa el éxito del registro. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- El descuento queda registrado y activo.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3. |
| 5 | Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3. |
| 6 | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3. |
| 7 | Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 8 | Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-51: Modificar descuento

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar los datos de un descuento registrado.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El descuento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar (ver CU-50: Buscar descuento). |
| 2 | El sistema muestra los datos actuales del descuento. |
| 3 | El actor modifica los datos que desea. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios. |
| 5 | El sistema valida que el porcentaje ingresado sea un valor entre 1 y 100. |
| 6 | El sistema valida que la vigencia hasta sea posterior a la vigencia desde. |
| 7 | El sistema valida que la cantidad límite ingresada sea un número entero mayor a cero. |
| 8 | Si se completó la cantidad de cursos requeridos, el sistema valida que sea un número entero mayor o igual a cero. |
| 9 | El sistema actualiza los datos del descuento. |
| 10 | El sistema informa el éxito de la modificación. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- El descuento queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3. |
| 6 | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3. |
| 7 | Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3. |
| 8 | Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- El sistema desactiva automáticamente el descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero.

---

### CU-52: Dar de baja descuento

**Objetivo(s) asociado(s)**
- OBJ-03: Gestionar la inscripción y el pago de los alumnos.

**Requisito(s) de información asociado(s)**
- RI-03: Información sobre inscripciones.

**Módulo**
- MOD-F-03: Módulo de Inscripciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja un descuento antes de su vencimiento natural. Si el descuento ya fue aplicado a alguna inscripción, el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El descuento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a dar de baja (ver CU-50: Buscar descuento). |
| 2 | El sistema verifica que la cantidad usada del descuento sea cero. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el descuento como dado de baja, y ya no poder aplicarlo en inscripciones. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- El descuento queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el descuento ya fue aplicado a alguna inscripción (cantidad usada mayor a cero), el sistema informa la dependencia y no permite la baja. |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-04: Módulo de Evaluaciones

### CU-53: Buscar pool

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar los pools de preguntas registrados en una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe y posee al menos un pool registrado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar pools dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre del pool. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los pools que coinciden con los criterios, con su cantidad de preguntas cargadas. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el listado de pools de la unidad, con su nombre y cantidad de preguntas.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-54: Crear pool

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente

**Descripción**
Permite al Docente crear un nuevo pool de preguntas para una unidad, cargando manualmente sus preguntas de opción múltiple o verdadero/falso junto con las opciones de respuesta de cada una.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea crear un nuevo pool dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita el nombre del pool. |
| 3 | El actor ingresa el nombre y comienza a cargar preguntas: por cada una, el tipo (opción múltiple o verdadero/falso), el enunciado y sus opciones de respuesta, marcando cuál es correcta. |
| 4 | El sistema valida que el nombre del pool haya sido completado y que se haya cargado al menos una pregunta. |
| 5 | El sistema valida que cada pregunta tenga al menos dos opciones de respuesta y que exactamente una esté marcada como correcta. |
| 6 | El sistema registra el pool con sus preguntas y opciones. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El pool queda registrado, asociado a la unidad, con sus preguntas y opciones de respuesta.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el nombre no fue completado o no se cargó ninguna pregunta, el sistema informa el error y vuelve al paso 3. |
| 5 | Si alguna pregunta tiene menos de dos opciones, o no tiene exactamente una opción marcada como correcta, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-55: Modificar pool

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente

**Descripción**
Permite al Docente modificar el nombre de un pool y agregar, editar o eliminar sus preguntas y opciones de respuesta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El pool existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar (ver CU-54: Buscar pool). |
| 2 | El sistema muestra los datos actuales del pool, con sus preguntas y opciones. |
| 3 | El actor modifica el nombre del pool, o agrega, edita o elimina preguntas y sus opciones. |
| 4 | El sistema valida que el pool no esté asociado a ninguna autoevaluación con intentos registrados. |
| 5 | El sistema valida que el nombre no quede vacío y que el pool conserve al menos una pregunta. |
| 6 | El sistema valida que cada pregunta conserve al menos dos opciones y exactamente una marcada como correcta. |
| 7 | El sistema actualiza el pool. |
| 8 | El sistema informa el éxito de la modificación. |
| 9 | Fin del caso de uso. |

**Postcondición(es)**
- El pool queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el pool está asociado a alguna autoevaluación con intentos registrados, el sistema informa que su contenido no puede modificarse (alterarlo sería inconsistente con lo que algún alumno ya rindió) y cancela la operación. |
| 5 | Si el nombre queda vacío o el pool queda sin preguntas, el sistema informa el error y vuelve al paso 3. |
| 6 | Si alguna pregunta queda con menos de dos opciones o sin una única opción correcta, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-56: Dar de baja pool

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente o Administrador dar de baja un pool. Si el pool está asociado a alguna autoevaluación activa, el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El pool existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a dar de baja (ver CU-54: Buscar pool). |
| 2 | El sistema verifica que el pool no esté asociado a ninguna autoevaluación activa. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el pool como dado de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- El pool queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el pool está asociado a una autoevaluación activa, el sistema informa la dependencia y no permite la baja. |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-57: Buscar autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar las autoevaluaciones registradas en una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una autoevaluación registrada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar autoevaluaciones dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre de la autoevaluación. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las autoevaluaciones que coinciden con los criterios. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el listado de autoevaluaciones de la unidad, con su nombre, pools asociados, tiempo límite, intentos permitidos y si integra pools de otras unidades como evaluación final.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-58: Crear autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente

**Descripción**
Permite al Docente crear una autoevaluación para una unidad, asociándose a uno o más pools de preguntas. Si la unidad es la última del programa, la autoevaluación puede integrar también pools de otras unidades, conformando la evaluación final del curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- Existe al menos un pool activo en la unidad correspondiente.
- La unidad existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea crear una autoevaluación dentro de una unidad (ver CU-20: Buscar unidad) |
| 2 | El sistema solicita: nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre (opcional), cantidad de intentos permitidos (opcional; si se deja vacía, no hay límite), y el o los pools de preguntas a asociar, el de la propia unidad y, si es la última unidad del programa, opcionalmente los de otras unidades, para conformar la evaluación final del curso. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios y que se haya seleccionado al menos un pool. |
| 5 | El sistema valida que el tiempo límite, cantidad de preguntas y cantidad de intentos permitidos (si corresponde) sea un valor entero mayor a cero. |
| 6 | El sistema valida que, si se especificó una fecha de cierre, esta sea posterior a la fecha de apertura. |
| 7 | El sistema valida que los pools seleccionados, en conjunto, tengan como mínimo la cantidad de preguntas ingresada para poder sortear un intento. |
| 8 | El sistema registra la autoevaluación. |
| 9 | El sistema informa el éxito del registro. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La autoevaluación queda registrada y asociada a los pools seleccionados, con su fecha de apertura y, si corresponde, su fecha de cierre.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completaron los campos obligatorios o no se seleccionó ningún pool, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el tiempo límite, la cantidad de preguntas, o la cantidad de intentos cuando se indicó, no son valores enteros mayores a cero, el sistema informa el error y vuelve al paso 3. |
| 6 | Si la fecha de cierre especificada no es posterior a la fecha de apertura, el sistema informa el error y vuelve al paso 3. |
| 7 | Si los pools seleccionados no reúnen como mínimo la cantidad de preguntas ingresada en conjunto, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-59: Modificar autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente

**Descripción**
Permite al Docente modificar una autoevaluación. Mientras no registre intentos, puede modificar todos sus datos (nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre, cantidad de intentos permitidos y pools asociados). Una vez que registra al menos un intento, su contenido queda protegido y solo puede extenderse la fecha de cierre, ocultarse o mostrarse nuevamente, o ampliarse la cantidad de intentos permitidos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La autoevaluación existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar (ver CU-58: Buscar autoevaluación). |
| 2 | El sistema muestra los datos actuales de la autoevaluación y, si esta ya registra intentos, indica que su contenido queda protegido y solo puede extenderse la fecha de cierre, ocultarse/mostrarse o ampliarse la cantidad de intentos permitidos. |
| 3 | El actor modifica los datos habilitados: nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre, cantidad de intentos permitidos y pools asociados si la autoevaluación no registra intentos; o fecha de cierre, visibilidad y/o cantidad de intentos permitidos si ya registra intentos. |
| 4 | El sistema valida que, si la autoevaluación registra intentos, el actor no haya modificado el nombre, el tiempo límite, la fecha de apertura ni los pools asociados. |
| 5 | El sistema valida que se mantengan completos los campos obligatorios y al menos un pool asociado. |
| 6 | El sistema valida que el tiempo límite sea un valor entero mayor a cero y, si se indicó cantidad de intentos permitidos, que también lo sea y, de existir intentos registrados, que sea mayor a la cantidad actual. |
| 7 | El sistema valida que, si se especificó una fecha de cierre, ésta sea posterior a la fecha de apertura y, de existir intentos registrados, también posterior a la fecha de cierre actual. |
| 8 | El sistema valida que los pools asociados reúnan como mínimo la cantidad de preguntas ingresada en conjunto. |
| 9 | El sistema actualiza la autoevaluación. |
| 10 | El sistema informa el éxito de la modificación. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- La autoevaluación queda actualizada con los datos permitidos según si registra intentos o no.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si la autoevaluación registra intentos y el actor modificó el nombre, el tiempo límite, la fecha de apertura o los pools asociados, el sistema informa que ese contenido queda protegido una vez registrados intentos y cancela la operación. |
| 5 | Si algún campo obligatorio queda vacío o sin pools asociados, el sistema informa el error y vuelve al paso 3. |
| 6 | Si el tiempo límite, la cantidad de preguntas o la cantidad de intentos cuando se indicó, no son valores enteros mayores a cero (o, con intentos registrados, la cantidad no es mayor a la actual), el sistema informa el error y vuelve al paso 3. |
| 7 | Si la fecha de cierre especificada no es posterior a la fecha de apertura (o, con intentos registrados, tampoco a la fecha de cierre actual), el sistema informa el error y vuelve al paso 3. |
| 8 | Si los pools asociados no reúnen como mínimo la cantidad de preguntas ingresada, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-60: Dar de baja autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente o Administrador dar de baja una autoevaluación. Si algún alumno ya registra un intento sobre ella, el sistema informa la dependencia y no permite la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La autoevaluación existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a dar de baja (ver CU-58: Buscar autoevaluación). |
| 2 | El sistema verifica que ningún alumno registre intentos sobre esa autoevaluación. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca la autoevaluación como dada de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La autoevaluación queda en estado de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si algún alumno ya registra un intento sobre esa autoevaluación, el sistema informa la dependencia y no permite la baja. |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-61: Buscar intento de autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador consultar el historial de intentos de los alumnos inscriptos en su curso sobre una autoevaluación, con fines de seguimiento.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos un intento registrado para la autoevaluación.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar intentos dentro de una autoevaluación. |
| 2 | El sistema solicita la autoevaluación sobre la que se desea consultar y, opcionalmente, el alumno, el rango de fechas y el resultado (aprobado / no aprobado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los intentos que coinciden con los criterios. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera el historial de intentos de la autoevaluación, con su fecha, alumno, nota y resultado (aprobado / no aprobado).

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-62: Ver calificaciones

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente o Administrador consultar las calificaciones de cualquier alumno inscripto en un curso, en las autoevaluaciones rendidas.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Si es Docente, participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita consultar las calificaciones de un alumno en un curso (ver CU-01: Buscar curso). |
| 2 | El sistema solicita el alumno a consultar. |
| 3 | El actor ingresa el alumno a consultar, si corresponde. |
| 4 | El sistema recupera, para cada autoevaluación rendida por el alumno en el programa de su cohorte, la nota y el resultado. |
| 5 | El sistema lista las calificaciones recuperadas. |
| 6 | Fin del caso de uso. |

**Salida**
Se recupera, por cada autoevaluación rendida, la nota y el resultado.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el alumno no rindió ninguna autoevaluación del curso, el sistema informa que no hay calificaciones para mostrar. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-63: Realizar intento de autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno realizar un intento de una autoevaluación, respondiendo un cuestionario de preguntas sorteadas de los pools asociados, con corrección automática (ver PA-6: Emisión automática de certificados).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente al curso.
- La unidad en la que se encuentra la autoevaluación se encuentra habilitada según el avance secuencial del alumno.
- La fecha y hora actual se encuentra dentro del período habilitado de la autoevaluación (posterior a su fecha de apertura y, si corresponde, anterior a su fecha de cierre).
- Si la autoevaluación tiene un límite de intentos, el alumno no lo superó para esa autoevaluación.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-26: Acceder curso), selecciona una autoevaluación para iniciar un intento de autoevaluación. |
| 2 | El sistema sortea la cantidad de preguntas cerradas de la autoevaluación de los pools asociados, con sus opciones de respuesta. |
| 3 | El sistema presenta el cuestionario al alumno, dentro del tiempo límite configurado. |
| 4 | El alumno selecciona una opción de respuesta para cada una de las preguntas. |
| 5 | El alumno confirma la entrega del intento. |
| 6 | El sistema valida que se haya respondido todas las preguntas. |
| 7 | El sistema corrige automáticamente el intento, comparando la opción elegida por el alumno con la opción correcta de cada pregunta. |
| 8 | El sistema calcula la nota del intento y registra el intento con la fecha actual. |
| 9 | Si el alumno respondió correctamente todas las preguntas, el sistema aprueba el intento, registra el progreso de la unidad como completada y, si correspondía a la evaluación final del curso, genera los datos del certificado de finalización (número y fecha de emisión), los registra en la inscripción del alumno, y los envía por correo electrónico (ver PA-6: Emisión automática de certificados). |
| 10 | Si el alumno no respondió correctamente todas las preguntas, el sistema no aprueba el intento e informa que debe reintentar el cuestionario completo. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- El intento queda registrado, con la opción elegida por el alumno en cada pregunta sorteada, la nota obtenida y el resultado.
- Si fue aprobado: el progreso del alumno en la unidad queda registrado como completado, con la fecha de aprobación, y se habilita el acceso a la siguiente unidad del curso; y, si correspondía a la evaluación final, el certificado queda registrado en la inscripción del alumno, y enviado por correo electrónico.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 6 | Si se agota el tiempo límite sin que el alumno haya respondido todas las preguntas, el sistema cierra automáticamente el intento con las respuestas dadas hasta ese momento y lo registra como no aprobado. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- La aprobación exige responder correctamente la totalidad de las preguntas del intento, según lo confirmado por el cliente; no existe una nota de corte parcial.

---

### CU-64: Dar de baja intento de autoevaluación

**Objetivo(s) asociado(s)**
- OBJ-04: Evaluar y certificar a los alumnos.

**Requisito(s) de información asociado(s)**
- RI-04: Información sobre evaluaciones.

**Módulo**
- MOD-F-04: Módulo de Evaluaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja un intento de autoevaluación ante la detección de fraude (por ejemplo, suplantación de identidad), revirtiendo el progreso de unidad y el certificado que ese intento haya generado, si corresponde.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El intento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el intento a dar de baja por fraude (ver CU-62: Buscar intento de autoevaluación). |
| 2 | El sistema solicita confirmación de la baja. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el intento como dado de baja. |
| 5 | Si el intento estaba aprobado y había marcado como completada la unidad correspondiente, el sistema revierte el progreso de esa unidad a no completada. |
| 6 | Si el intento correspondía a la evaluación final del curso y había generado un certificado, el sistema revierte la emisión del certificado de la inscripción. |
| 7 | El sistema notifica al alumno la anulación del intento. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- El intento queda en estado de baja.
- Si correspondía, el progreso de la unidad y el certificado emitido quedan revertidos.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 3 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-05: Módulo de Clases en Vivo

### CU-65: Buscar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar las clases en vivo programadas para una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una clase en vivo registrada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar clases en vivo dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el título, el docente, el rango de fechas y el estado (Programada / En vivo / Finalizada). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las clases en vivo que coinciden con los criterios ingresados. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 6 | Fin del caso de uso. |

**Salida**
Se recuperan una o más clases en vivo, con su título, fecha y hora, docente y estado.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-66: Programar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular o ayudante de un curso programar una clase en vivo para una unidad, definiendo su fecha y hora de transmisión.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe y está incluida en el cronograma de algún programa de un curso en el que el docente participa como titular o ayudante.
- El curso posee al menos una cohorte con fechas de dictado (modalidad con clases en vivo).

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea programar una clase en vivo dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita: la cohorte a la que se dirige la clase, título, fecha y hora, y duración estimada. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios. |
| 5 | El sistema valida que la fecha y hora ingresadas sean posteriores al momento actual. |
| 6 | El sistema valida que la fecha y hora de la clase se encuentren dentro de las fechas de dictado de la cohorte seleccionada. |
| 7 | El sistema valida que la fecha y hora, considerando la duración estimada, no se superpongan con otra clase en vivo programada del mismo docente. |
| 8 | El sistema registra la clase en estado Programada, asociada a la unidad, al docente y a la cohorte. |
| 9 | El sistema notifica a los alumnos inscriptos en la cohorte la fecha de la clase. |
| 10 | El sistema informa el éxito del registro. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda registrada en estado Programada, asociada a la unidad, al docente y a la cohorte.
- Los alumnos inscriptos en la cohorte reciben la notificación.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 5 | Si la fecha y hora ingresadas no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3. |
| 6 | Si la fecha y hora no se encuentran dentro de las fechas de dictado de la cohorte, el sistema informa el error y vuelve al paso 3. |
| 7 | Si la clase se superpone con otra clase en vivo programada del mismo docente, el sistema informa el conflicto de horario y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- La configuración e instalación del software OBS para cada docente está a cargo del equipo de sistemas de la empresa, previo a la transmisión, y queda fuera del alcance funcional de este caso de uso.

---

### CU-67: Modificar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente

**Descripción**
Permite al Docente modificar el título, la fecha o la hora de una clase en vivo, siempre que todavía no haya sido transmitida.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a modificar (ver CU-66: Buscar clase en vivo). |
| 2 | El sistema muestra los datos actuales de la clase. |
| 3 | El actor modifica el título, la fecha, la hora o la duración estimada. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios y que la fecha y hora sean posteriores al momento actual. |
| 5 | El sistema valida que la fecha y hora se encuentren dentro de las fechas de dictado de la cohorte de la clase. |
| 6 | El sistema valida que la fecha y hora, considerando la duración estimada, no se superpongan con otra clase en vivo programada del mismo docente. |
| 7 | El sistema actualiza los datos de la clase. |
| 8 | El sistema notifica a los alumnos inscriptos el cambio de fecha u horario, si corresponde. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda actualizada con los nuevos datos.
- Los alumnos inscriptos reciben la notificación del cambio, si corresponde.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si algún campo obligatorio queda vacío, o la fecha y hora no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3. |
| 5 | Si la nueva fecha y hora no se encuentran dentro de las fechas de dictado de la cohorte, el sistema informa el error y vuelve al paso 3. |
| 6 | Si la clase se superpone con otra clase en vivo programada del mismo docente, el sistema informa el conflicto de horario y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-68: Cancelar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente

**Descripción**
Permite al Docente cancelar una clase en vivo programada que todavía no fue transmitida.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a cancelar (ver CU-66: Buscar clase en vivo). |
| 2 | El actor confirma la cancelación. |
| 3 | El sistema marca la clase como dada de baja. |
| 4 | El sistema notifica a los alumnos inscriptos la cancelación de la clase. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda dada de baja.
- Los alumnos inscriptos reciben la notificación de la cancelación.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la cancelación, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-69: Dar de baja clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja una clase en vivo ya finalizada (por ejemplo, ante contenido indebido transmitido o un registro erróneo), retirando también su grabación asociada si existe.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La clase en vivo existe, no se encuentra en baja y se encuentra en estado Finalizada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo finalizada a dar de baja (ver CU-66: Buscar clase en vivo). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la clase como dada de baja. |
| 4 | Si la clase generó un material de tipo Grabación, el sistema también lo marca como dado de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda en estado de baja.
- Su grabación, si existía, también queda en baja y deja de estar disponible para los alumnos.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-70: Iniciar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente

**Descripción**
Permite al Docente iniciar la transmisión de una clase en vivo programada, generando los datos de conexión que utilizará desde OBS (ver PA-4: Clases en Vivo).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- Se alcanzó el horario programado para la clase.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase en vivo programada (ver CU-66: Buscar clase en vivo) y, en el horario programado, solicita iniciarla. |
| 2 | El sistema genera los datos de conexión de la transmisión (URL de streaming y clave privada de transmisión). |
| 3 | El sistema pasa la clase al estado En vivo. |
| 4 | El docente carga los datos de conexión en OBS y comienza a transmitir. |
| 5 | El sistema recibe la señal transmitida y la redistribuye en simultáneo a los alumnos inscriptos que ingresan a la clase (ver CU-73: Ingresar a clase en vivo), mientras graba automáticamente la transmisión mediante el protocolo RTMP desde OBS. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda en estado En vivo, con sus datos de conexión generados.
- La transmisión queda disponible para los alumnos inscriptos y se graba automáticamente.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- La clave de transmisión es privada del docente, de forma que solo él pueda transmitir con los datos de conexión generados para esa clase.

---

### CU-71: Finalizar clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Docente

**Descripción**
Permite al Docente finalizar la transmisión de una clase en vivo, dando de baja la señal en OBS de forma remota y generando la grabación resultante como material de la unidad (ver PA-4: Clases en Vivo).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado En vivo.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase en vivo que se encuentra en curso (ver CU-66: Buscar clase en vivo) y solicita finalizar la transmisión. |
| 2 | El sistema envía la orden de corte de transmisión y grabación al OBS del docente. |
| 3 | El sistema pasa la clase al estado Finalizada. |
| 4 | El sistema genera la grabación resultante de la transmisión. |
| 5 | El sistema carga la grabación como material de tipo Grabación de la unidad correspondiente, en estado publicado. |
| 6 | El sistema notifica a los alumnos inscriptos que la grabación ya está disponible. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La clase en vivo queda en estado Finalizada.
- La grabación queda cargada como material publicado de la unidad.
- Los alumnos inscriptos reciben la notificación de disponibilidad de la grabación.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- La grabación resultante queda disponible por un plazo configurable (cuatro meses por defecto), con aviso previo al alumno antes de su vencimiento y eliminación automática al cumplirse el plazo.

---

### CU-72: Ingresar a clase en vivo

**Objetivo(s) asociado(s)**
- OBJ-05: Gestionar las clases en vivo.

**Requisito(s) de información asociado(s)**
- RI-05: Información sobre clases en vivo.

**Módulo**
- MOD-F-05: Módulo de Clases en Vivo

**Actor(es)**
- Alumno

**Descripción**
Permite al Alumno ingresar a la transmisión de una clase en vivo mientras se encuentra en curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente al curso al que pertenece la clase en vivo.
- La clase en vivo se encuentra en estado En vivo.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-26: Acceder curso), selecciona una clase en vivo para ingresar. |
| 2 | El sistema verifica que la clase se encuentre en estado En vivo y que el alumno posea inscripción vigente al curso. |
| 3 | El sistema conecta al alumno a la transmisión en curso. |
| 4 | Fin del caso de uso. |

**Salida**
El alumno queda conectado a la transmisión en vivo de la clase.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si la clase todavía no comenzó o ya finalizó, el sistema informa que la transmisión no está disponible en este momento. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-06: Módulo de Generación de Contenido con IA

### CU-73: Generar banco de preguntas

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente generar automáticamente un pool de preguntas para una unidad, a partir de la bibliografía y el glosario cargados, mediante un modelo de inteligencia artificial ejecutado localmente (Ollama) (ver PA-9: Generación de banco de preguntas).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía o un término de glosario cargado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar un banco de preguntas dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita, opcionalmente, un guión adicional ingresado como prompt de texto para orientar la generación. |
| 3 | El actor confirma la generación, con o sin el guión adicional. |
| 4 | El sistema envía la bibliografía, el glosario de la unidad y el guión, si fue ingresado, al modelo de inteligencia artificial local. |
| 5 | El modelo de inteligencia artificial genera un banco de preguntas cerradas de opción múltiple y verdadero/falso, siguiendo la proporción configurada. |
| 6 | El sistema recibe el banco de preguntas generado y valida que cada pregunta tenga al menos dos opciones y exactamente una marcada como correcta. |
| 7 | El sistema registra el pool generado, asociado a la unidad. |
| 8 | El sistema notifica al docente que el pool está disponible para su revisión antes de publicarse. |
| 9 | Fin del caso de uso. |

**Postcondición(es)**
- El pool generado queda registrado, asociado a la unidad.
- El docente recibe la notificación para revisar el pool antes de utilizarlo en una autoevaluación.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 6 | Si el modelo de inteligencia artificial devuelve un banco de preguntas con un formato inválido, el sistema descarta el resultado, informa el error al docente y le permite reintentar. |

**Frecuencia**
- Media

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-74: Generar resumen de unidad

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente generar automáticamente un resumen del contenido de una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local (ver PA-8: Generación de resúmenes de unidad).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía cargado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar un resumen dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El actor confirma la generación. |
| 3 | El sistema envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | El modelo de inteligencia artificial genera un resumen estructurado del contenido. |
| 5 | El sistema recibe el resumen y lo registra como material de tipo Resumen de la unidad, en estado no publicado. |
| 6 | El sistema notifica al docente que el resumen está disponible para su revisión antes de publicarlo. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- El resumen queda registrado como material de la unidad, sin publicar.
- El docente recibe la notificación para revisarlo.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-75: Generar presentación de unidad

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente generar automáticamente una presentación descargable para una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local (ver PA-7: Generación de presentaciones de unidad).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía cargado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar una presentación dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El actor confirma la generación. |
| 3 | El sistema envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | El modelo de inteligencia artificial devuelve una estructura de contenidos (títulos, subtítulos y puntos clave). |
| 5 | El sistema da formato a la estructura recibida como una presentación descargable y la registra como material de tipo Presentación de la unidad, en estado no publicado. |
| 6 | El sistema notifica al docente que la presentación está disponible para su revisión antes de publicarla. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La presentación queda registrada como material de la unidad, sin publicar.
- El docente recibe la notificación para revisarla.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-76: Crear clon

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente registrar su clon de inteligencia artificial (avatar visual y voz), requisito previo para generar clases mediante CU-79: Generar clase con clon.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El docente se encuentra habilitado para dictar clases.
- El docente no posee un avatar y voz clonada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente solicita crear su clon de inteligencia artificial. |
| 2 | El sistema solicita una imagen con el rostro del docente (adjunta o tomada con la cámara web) y un guión de ejemplo para grabar el audio. |
| 3 | El actor adjunta o toma la foto, y graba un audio de uno a dos minutos leyendo el guión de ejemplo con su voz. |
| 4 | El sistema muestra los términos y condiciones de uso del clon y solicita su aceptación. |
| 5 | El actor acepta los términos y condiciones. |
| 6 | El sistema envía la imagen y el audio a HeyGen para crear el avatar y clonar la voz. |
| 7 | HeyGen valida que la imagen y el audio sean aptos para generar el avatar y la voz. |
| 8 | HeyGen crea el avatar y clona la voz, y devuelve sus identificadores (avatar_id y voice_id). |
| 9 | El sistema registra el avatar_id, el voice_id y la fecha de aceptación de los términos y condiciones en el perfil del docente. |
| 10 | El sistema informa el éxito del registro. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- El docente queda con su avatar_id y voice_id registrados, habilitado para generar clases con Clon de IA.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 5 | Si el actor no acepta los términos y condiciones, el sistema cancela la operación y finaliza el caso de uso. |
| 7 | Si HeyGen no logra validar la imagen o el audio provistos, el sistema informa el error y solicita al docente que los reintente, volviendo al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-77: Buscar clase con clon

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o al Administrador buscar las clases generadas mediante Clon de inteligencia artificial en una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una clase con Clon de IA registrada.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar clases con Clon de IA dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el título y el estado (Pendiente / Generada / Error). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las clases con Clon de IA que coinciden con los criterios. |
| 5 | El actor puede seleccionar uno de los resultados para ver su detalle, replicando el mecanismo de selección de CU-06: Explorar catálogo de cursos. |
| 6 | Fin del caso de uso. |

**Salida**
Se recuperan una o más clases con Clon de IA, con su título, guión, estado y fecha de generación.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-78: Generar clase con clon

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular o ayudante de un curso generar una clase para una unidad mediante un Clon de inteligencia artificial, a partir de un guión que redacta como prompt, integrando con la plataforma HeyGen (ver PA-5: Generación de videos Clon IA).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El docente se encuentra habilitado para dictar clases.
- El docente tiene registrado su avatar y voz clonada.
- La unidad existe y está incluida en el cronograma de algún programa de un curso en el que el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar una clase con Clon de IA dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | El sistema solicita el título de la clase y el guión, ingresado como un prompt de texto. |
| 3 | El actor ingresa el título y redacta el guión. |
| 4 | El sistema valida que el título y el guión hayan sido completados. |
| 5 | El sistema registra la clase en estado Pendiente. |
| 6 | El sistema envía el guión a HeyGen, junto con el avatar_id y el voice_id del docente. |
| 7 | HeyGen anima el avatar del docente, moviendo la boca y los gestos faciales al ritmo del guión y con la voz clonada del docente, y genera el video de la clase. |
| 8 | El sistema descarga el video generado y actualiza el estado de la clase a Generada. |
| 9 | El sistema carga el video como material de tipo Grabación de la unidad correspondiente, en estado no publicado. |
| 10 | El sistema notifica al docente que el material está disponible para su revisión antes de publicarlo. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- La clase con Clon de IA queda registrada, en estado Generada.
- El video generado queda cargado como material de la unidad, sin publicar.
- El docente recibe la notificación para revisar el material.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el título o el guión no fueron completados, el sistema informa el error y vuelve al paso 3. |
| 7 | Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente. |

**Frecuencia**
- Media

**Estabilidad**
- Media

**Comentarios**
- –

---

### CU-79: Modificar clase con clon

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente

**Descripción**
Permite al Docente titular o ayudante modificar el título y/o el guión de una clase con Clon de inteligencia artificial de una unidad, regenerando el video mediante HeyGen si el guión fue modificado.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase con Clon de IA existe, no se encuentra en baja, y pertenece a una unidad incluida en el cronograma de algún programa de un curso en el que el docente participa como titular o ayudante.
- La clase se encuentra en estado Generada o Error.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase con Clon de IA a modificar (ver CU-78: Buscar clase con clon). |
| 2 | El sistema muestra el título y el guión actuales de la clase. |
| 3 | El actor modifica el título y/o el guión. |
| 4 | El sistema valida que el título y el guión se mantengan completos. |
| 5 | Si el guión fue modificado, el sistema actualiza el estado de la clase a Pendiente y envía el nuevo guión, junto con el Avatar del docente, a HeyGen. |
| 6 | HeyGen genera el nuevo video de la clase a partir del guión actualizado. |
| 7 | El sistema descarga el video generado, actualiza el estado de la clase a Generada y reemplaza el material de tipo Grabación de la unidad, en estado no publicado. |
| 8 | El sistema actualiza la clase con los nuevos datos. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La clase con Clon de IA queda actualizada con el nuevo título y/o guión.
- Si el guión fue modificado, el video queda regenerado y el material de la unidad, reemplazado sin publicar.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el título o el guión quedan vacíos, el sistema informa el error y vuelve al paso 3. |
| 6 | Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente. |

**Frecuencia**
- Baja

**Estabilidad**
- Media

**Comentarios**
- Si solo se modifica el título, no se dispara una nueva generación en HeyGen.

---

### CU-80: Dar de baja clase con clon

**Objetivo(s) asociado(s)**
- OBJ-06: Generar contenido académico con inteligencia artificial.

**Requisito(s) de información asociado(s)**
- RI-06: Información sobre generación de contenido con inteligencia artificial.

**Módulo**
- MOD-F-06: Módulo de Generación de Contenido con IA

**Actor(es)**
- Docente, Administrador

**Descripción**
Permite al Docente titular/ayudante o Administrador dar de baja una clase con Clon de inteligencia artificial de una unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La clase con Clon de IA existe, no se encuentra en baja, y pertenece a una unidad incluida en el cronograma de algún programa de un curso en el que el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase con Clon de IA a dar de baja (ver CU-78: Buscar clase con clon). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la clase y su material asociado como dados de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- La clase con Clon de IA y su material asociado quedan dados de baja.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-NF-01: Módulo de Usuarios y Notificaciones

### CU-81: Registrarse

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno

**Descripción**
Permite a un interesado, todavía sin cuenta, crear su propia cuenta de Alumno en la plataforma, mediante correo electrónico y contraseña, validando la cuenta a través de un enlace enviado por email.

**Precondición(es)**
- –

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el interesado solicita crear una cuenta. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI y contraseña. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 5 | El sistema registra la cuenta con rol Alumno, con el correo sin validar. |
| 6 | El sistema envía un enlace de validación al correo electrónico ingresado. |
| 7 | El actor accede al enlace recibido. |
| 8 | El sistema marca el correo electrónico como validado. |
| 9 | El sistema informa el éxito del registro y habilita el inicio de sesión. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta queda registrada con rol Alumno.
- La cuenta queda validada una vez que el actor accede al enlace enviado por correo.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el correo electrónico ya está registrado, el sistema informa el error y sugiere iniciar sesión o recuperar la contraseña. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-82: Buscar usuario

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador buscar los usuarios registrados en el sistema, con fines de gestión.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Existe al menos un usuario registrado.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más usuarios. |
| 2 | El sistema solicita los criterios de búsqueda: nombre, apellido, correo electrónico, DNI y rol (Alumno / Docente / Administrador). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los usuarios que coincidan con los criterios ingresados. |
| 5 | El sistema lista los usuarios filtrados. |
| 6 | El actor puede seleccionar uno de los resultados para ver su detalle. |
| 7 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más usuarios que cumplen con los criterios de búsqueda, con su rol y estado.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-83: Registrar usuario

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar manualmente la cuenta de un Alumno, Docente u otro Administrador, para los casos en que el alta no ocurre por autoregistro.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar manualmente un usuario. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI, telefono (opcional) y el rol a asignar (Alumno, Docente o Administrador). |
| 3 | El actor ingresa los datos solicitados. |
| 4 | Si el rol seleccionado es Docente, se ejecuta el CU-89: Registrar docente, para solicitar los datos adicionales del perfil docente (biografía, años de experiencia, títulos y matrículas). |
| 5 | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 6 | El sistema registra la cuenta con el rol indicado y envía al correo ingresado un enlace para que el usuario defina su contraseña. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta queda registrada con el rol indicado y, si corresponde, con los datos adicionales del perfil docente.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- El Administrador puede crear cuentas de otros Administradores, pero no puede modificarlas.

---

### CU-84: Modificar usuario

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar los datos base de la cuenta de un Alumno.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El usuario existe, posee rol Alumno y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta del alumno a modificar (ver CU-83: Buscar usuario). |
| 2 | El sistema muestra los datos actuales de la cuenta. |
| 3 | El actor modifica el nombre, apellido, correo electrónico, DNI, teléfono o imagen de perfil. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios y que el correo electrónico, si fue modificado, no esté ya registrado por otra cuenta. |
| 5 | El sistema actualiza los datos de la cuenta. |
| 6 | El sistema informa el éxito de la modificación. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta del alumno queda actualizada con los nuevos datos.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el correo electrónico ya está registrado por otra cuenta, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-85: Dar de baja usuario

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador dar de baja la cuenta de un usuario, quitándole el acceso al sistema.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El usuario existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta del usuario a dar de baja (ver CU-83: Buscar usuario). |
| 2 | Si el usuario posee rol Administrador, el sistema valida que existan otros administradores activos en el sistema además de él. |
| 3 | Si el usuario posee rol Docente, el sistema verifica que no sea titular ni ayudante de ninguna cohorte vigente. |
| 4 | Si el usuario posee rol Alumno y posee inscripciones vigentes, el sistema advierte que la baja le hará perder el acceso al contenido de esos cursos, sin derecho a reembolso. |
| 5 | El actor confirma la baja. |
| 6 | El sistema marca la cuenta como dada de baja y cierra sus sesiones activas. |
| 7 | El sistema informa el éxito de la operación. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta queda en estado de baja y pierde acceso al sistema.
- Las sesiones activas del usuario quedan cerradas.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 2 | Si el usuario posee rol Administrador y es el único administrador activo del sistema, el sistema informa que no puede quedar sin administradores y no permite la baja. |
| 3 | Si el docente es titular o ayudante de al menos una cohorte vigente, el sistema informa la dependencia y no permite la baja hasta que se lo reemplace como titular o ayudante en esa cohorte o se dé de baja la cohorte. |
| 5 | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- El sistema debe garantizar en todo momento la existencia de al menos un Administrador activo, para evitar que el sistema quede sin gestión posible.

---

### CU-86: Ver perfil

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a cualquier usuario autenticado consultar los datos de su propia cuenta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver su perfil. |
| 2 | El sistema recupera los datos de la cuenta del actor. |
| 3 | El sistema muestra los datos al actor. |
| 4 | Fin del caso de uso. |

**Salida**
Se recuperan los datos de la cuenta del actor: nombre, apellido, correo electrónico, DNI, teléfono e imagen de perfil, y los datos profesionales adicionales si el actor es Docente.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-87: Editar perfil

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a cualquier usuario autenticado editar los datos base de su propia cuenta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita editar su perfil. |
| 2 | El sistema muestra los datos actuales de la cuenta del actor. |
| 3 | El actor modifica el nombre, apellido, teléfono o imagen de perfil. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios. |
| 5 | El sistema actualiza los datos de la cuenta. |
| 6 | El sistema informa el éxito de la modificación. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta del actor queda actualizada con los nuevos datos.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-88: Registrar docente

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador registrar manualmente la cuenta de un nuevo docente, verificando previamente sus credenciales académicas o profesionales. El alta de un docente no admite autoregistro.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo docente. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI, teléfono, biografía, años de experiencia, título o títulos universitarios o de posgrado con su matrícula profesional cuando corresponda, y matrícula del Registro de Idóneos de la Comisión Nacional de Valores cuando aplique. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El actor verifica el título declarado contra el Registro Público de Graduados Universitarios, o bien la matrícula profesional o de la Comisión Nacional de Valores informada. |
| 5 | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 6 | El sistema valida que se haya declarado al menos un título universitario o de posgrado, o al menos una matrícula profesional (colegio o Comisión Nacional de Valores). |
| 7 | El sistema valida que los años de experiencia ingresados sean un número entero mayor o igual a cero. |
| 8 | El sistema registra la cuenta con rol Docente, habilitada, y envía al correo ingresado un enlace para que el docente defina su contraseña. |
| 9 | El sistema informa el éxito del registro. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La cuenta del docente queda registrada, habilitada y con su información profesional cargada.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 5 | Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3. |
| 5 | Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3. |
| 6 | Si no se declaró ningún título ni ninguna matrícula profesional, el sistema informa el error y vuelve al paso 3. |
| 7 | Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- La verificación del título o la matrícula es un control manual y externo que realiza el Administrador antes de cargar los datos; el sistema no modela un flujo de estados de verificación por credencial.

---

### CU-89: Modificar docente

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador modificar la información profesional de un docente y habilitarlo o suspenderlo temporalmente para dictar clases, sin eliminar su cuenta ni su historial.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El docente existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el docente a modificar (ver CU-83: Buscar usuario). |
| 2 | El sistema muestra los datos profesionales actuales del docente. |
| 3 | El actor modifica la biografía, los años de experiencia, los títulos, la matrícula, o el estado de habilitación para dictar clases. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios. |
| 5 | El sistema valida que se mantenga declarado al menos un título universitario o de posgrado, o al menos una matrícula profesional (colegio o Comisión Nacional de Valores). |
| 6 | Si se modificaron los años de experiencia, el sistema valida que sean un número entero mayor o igual a cero. |
| 7 | Si el actor intenta suspender la habilitación del docente, el sistema verifica que no sea titular ni ayudante de ninguna cohorte vigente. |
| 8 | El sistema actualiza los datos del docente. |
| 9 | Si el actor suspendió la habilitación del docente, el sistema le notifica el cambio de estado. |
| 10 | El sistema informa el éxito de la modificación. |
| 11 | Fin del caso de uso. |

**Postcondición(es)**
- Los datos profesionales del docente quedan actualizados.
- Si se modificó su estado de habilitación, el docente queda habilitado o suspendido para dictar clases, según corresponda.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3. |
| 5 | Si la modificación deja al docente sin ningún título ni matrícula profesional declarados, el sistema informa el error y vuelve al paso 3. |
| 6 | Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3. |
| 7 | Si el docente que se intenta suspender es titular o ayudante de al menos una cohorte vigente, el sistema informa la dependencia y no permite la suspensión hasta que se lo reemplace en esa cohorte. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-90: Iniciar sesión

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a un usuario iniciar sesión en el sistema mediante correo electrónico y contraseña, o mediante Google OAuth como método alternativo (ver PA-1: Login con Google).

**Precondición(es)**
- El actor posee una cuenta registrada y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita iniciar sesión. |
| 2 | El sistema solicita el correo electrónico y la contraseña, u ofrece la opción de ingresar con Google. |
| 3 | El actor ingresa sus credenciales, o selecciona ingresar con Google y autoriza el acceso a su correo y datos básicos de perfil. |
| 4 | El sistema valida las credenciales ingresadas, o el token devuelto por Google, según el método elegido. |
| 5 | El sistema valida que la cantidad de sesiones concurrentes activas del usuario no supere el límite configurado. |
| 6 | El sistema registra una nueva sesión, con su token, fecha de inicio, IP y dispositivo. |
| 7 | El sistema informa el éxito del inicio de sesión. |
| 8 | Fin del caso de uso. |

**Postcondición(es)**
- La sesión queda registrada y activa.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si las credenciales ingresadas son incorrectas, el sistema informa el error y vuelve al paso 3. |
| 4 | Si el correo electrónico todavía no fue validado, el sistema informa que debe validarlo antes de iniciar sesión. |
| 5 | Si el usuario ya alcanzó el límite de sesiones concurrentes permitidas, el sistema informa el error y le solicita cerrar una sesión activa antes de continuar. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- El límite de sesiones concurrentes busca mitigar el uso compartido de credenciales.

---

### CU-91: Cerrar sesión

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a un usuario cerrar su propia sesión activa en el sistema.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita cerrar su sesión. |
| 2 | El sistema registra la fecha de fin de la sesión activa. |
| 3 | El sistema redirige al actor a la pantalla de inicio de sesión. |
| 4 | Fin del caso de uso. |

**Postcondición(es)**
- La sesión queda cerrada.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-92: Recuperar contraseña

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a un usuario restablecer su contraseña cuando la olvidó, mediante un token temporal enviado a su correo electrónico. Es la única vía por la que se modifica la contraseña de una cuenta.

**Precondición(es)**
- El actor posee una cuenta registrada con contraseña propia y no se encuentra en baja.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor selecciona la opción de recuperar contraseña desde la pantalla de inicio de sesión. |
| 2 | El sistema solicita el correo electrónico asociado a la cuenta. |
| 3 | El actor ingresa el correo electrónico. |
| 4 | El sistema valida que el correo está registrado. |
| 5 | El sistema genera un token de recuperación con su fecha de expiración y lo envía al correo del actor. |
| 6 | El actor accede al enlace recibido e ingresa la nueva contraseña. |
| 7 | El sistema valida que el token no haya expirado. |
| 8 | El sistema actualiza la contraseña de la cuenta. |
| 9 | El sistema informa el éxito de la operación. |
| 10 | Fin del caso de uso. |

**Postcondición(es)**
- La contraseña de la cuenta queda actualizada.
- El actor puede iniciar sesión con la nueva contraseña.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 4 | Si el correo ingresado no está registrado, el sistema informa el error y vuelve al paso 3. |
| 7 | Si el token de recuperación expiró, el sistema informa el error y le solicita generar uno nuevo, volviendo al paso 2. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- No aplica a las cuentas que se autentican exclusivamente mediante Google OAuth, ya que no poseen contraseña propia en el sistema.

---

### CU-93: Buscar sesión

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a un usuario consultar sus propias sesiones activas. El Administrador puede además consultar las sesiones activas de cualquier usuario.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver las sesiones activas. |
| 2 | El sistema solicita, opcionalmente, el usuario (solo para Administrador), el rango de fechas y la IP o el dispositivo. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra las sesiones del actor (o del usuario indicado si es Administrador) que coincidan con los criterios ingresados. |
| 5 | El sistema lista las sesiones filtradas, con su fecha de inicio, fecha de fin, IP y dispositivo. |
| 6 | El actor puede seleccionar una sesión en específico. |
| 7 | Fin del caso de uso. |

**Salida**
Se recupera el listado de sesiones activas, con su fecha de inicio, IP y dispositivo.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-94: Eliminar sesión

**Objetivo(s) asociado(s)**
- OBJ-07: Gestionar usuarios, autenticación y notificaciones.

**Requisito(s) de información asociado(s)**
- RI-07: Información sobre usuarios y notificaciones.

**Módulo**
- MOD-NF-01: Módulo de Usuarios y Notificaciones

**Actor(es)**
- Alumno, Docente, Administrador

**Descripción**
Permite a un usuario cerrar de forma forzada una sesión activa propia distinta de la actual. El Administrador puede además cerrar una sesión activa de cualquier usuario.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema.
- La sesión a cerrar existe y se encuentra activa.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la sesión activa a cerrar (ver CU-94: Buscar sesión). |
| 2 | El actor confirma el cierre. |
| 3 | El sistema registra la fecha de fin de esa sesión. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

**Postcondición(es)**
- La sesión seleccionada queda cerrada.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-NF-02: Módulo de Auditoría

### CU-95: Consultar auditoría

**Objetivo(s) asociado(s)**
- OBJ-08: Registrar las acciones críticas del sistema.

**Requisito(s) de información asociado(s)**
- RI-08: Información sobre auditoría.

**Módulo**
- MOD-NF-02: Módulo de Auditoría

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador consultar el registro de auditoría de las acciones críticas del sistema (pagos, altas de curso y cambios de estado de inscripción), para garantizar trazabilidad sobre las operaciones.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Existe al menos un registro de auditoría.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita consultar el registro de auditoría. |
| 2 | El sistema solicita los criterios de búsqueda: usuario responsable, tipo de acción (Crear / Modificar / Eliminar / Consultar), entidad afectada y rango de fecha. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los registros de auditoría que coincidan con los criterios ingresados. |
| 5 | El sistema lista los registros filtrados. |
| 6 | Fin del caso de uso. |

**Salida**
Se recuperan uno o más registros de auditoría, con el usuario responsable, el tipo de acción, la entidad afectada, el identificador del registro puntual, el valor anterior y el valor nuevo del dato modificado (cuando corresponda), la dirección IP desde la que se realizó la acción y la fecha y hora exacta.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-NF-03: Módulo de Reportes y Estadísticas

### CU-96: Generar informe de alumnos de un curso

**Objetivo(s) asociado(s)**
- OBJ-09: Generar reportes y estadísticas de gestión.

**Requisito(s) de información asociado(s)**
- RI-09: Información sobre reportes y estadísticas.

**Módulo**
- MOD-NF-03: Módulo de Reportes y Estadísticas

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador generar un informe de alumnos de un curso, con 4 vistas: comparación del curso frente al resto en cantidad de inscriptos (barras horizontales); evolución de sus inscripciones en el tiempo, con una línea por cada programa del curso (línea); tasa de abandono de las inscripciones (torta); y comparación entre los programas del curso según su cantidad de inscripciones, para identificar cuál resultó más efectivo captando alumnos (barras horizontales).
Le sirve a la empresa para saber cómo le va a un curso frente al resto, si sus inscripciones vienen subiendo o bajando, qué tan bien retiene a los alumnos que se anotan, y qué programa del curso resultó más efectivo captando alumnos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita generar un informe de alumnos de un curso. |
| 2 | El sistema solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | El actor ingresa los criterios solicitados. |
| 4 | El sistema recopila los datos de alumnos inscriptos al curso seleccionado (agrupados por programa para la evolución temporal y la comparación de inscripciones), junto con los del resto de los cursos para la comparación general, y genera el informe. |
| 5 | El sistema registra el reporte generado del curso, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6 | El sistema pone el informe a disposición del actor para su descarga. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- El reporte queda registrado en el historial de reportes generados.
- El informe de alumnos del curso seleccionado queda disponible para su descarga.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-97: Generar informe de ingresos de un curso

**Objetivo(s) asociado(s)**
- OBJ-09: Generar reportes y estadísticas de gestión.

**Requisito(s) de información asociado(s)**
- RI-09: Información sobre reportes y estadísticas.

**Módulo**
- MOD-NF-03: Módulo de Reportes y Estadísticas

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador generar un informe de ingresos de un curso, con 4 vistas: comparación del curso frente al resto en ingresos por pagos acreditados (barras horizontales); evolución de sus ingresos en el tiempo, con una línea por cada programa del curso (línea); ingresos por categoría de curso (torta); y comparación entre los programas del curso según el ingreso neto generado, para identificar cuál resultó más efectivo en términos de facturación (barras horizontales).
Le sirve a la empresa para saber si el curso es uno de los que genera más ingresos, si su facturación viene creciendo, si el rubro del curso es uno de los más rentables, y qué programa del curso resultó más efectivo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita generar un informe de ingresos de un curso. |
| 2 | El sistema solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | El actor ingresa los criterios solicitados. |
| 4 | El sistema recopila los pagos acreditados del curso seleccionado (agrupados por programa para la evolución temporal y la comparación de facturación), junto con los del resto de los cursos para las comparaciones generales, y genera el informe. |
| 5 | El sistema registra el reporte generado del curso, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6 | El sistema pone el informe a disposición del actor para su descarga. |
| 7 | Fin del caso de uso. |

**Postcondición(es)**
- El reporte queda registrado en el historial de reportes generados.
- El informe de ingresos del curso seleccionado queda disponible para su descarga.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-98: Consultar estadísticas

**Objetivo(s) asociado(s)**
- OBJ-09: Generar reportes y estadísticas de gestión.

**Requisito(s) de información asociado(s)**
- RI-09: Información sobre reportes y estadísticas.

**Módulo**
- MOD-NF-03: Módulo de Reportes y Estadísticas

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador consultar en pantalla los indicadores del sistema (alumnos inscriptos e ingresos), sin necesidad de generar un reporte descargable.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor accede al panel de estadísticas. |
| 2 | El sistema recupera y muestra en pantalla los indicadores de alumnos inscriptos e ingresos. |
| 3 | Fin del caso de uso. |

**Salida**
Se muestran en pantalla los indicadores de: alumnos activos; cantidad total de inscripciones vigentes al momento, ingresos del mes, con variación respecto al mes anterior, inscripciones de los últimos 30 días (línea) y ranking (top 5) de los cinco cursos con más inscriptos (barras horizontales).
Le sirve a la empresa para tener un pantallazo rápido de cómo viene el negocio sin esperar a generar un informe.

**Excepciones**
- *Ninguna especificada.*

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-NF-04: Módulo de Configuración

### CU-99: Configurar parámetros

**Objetivo(s) asociado(s)**
- OBJ-10: Permitir la configuración de los parámetros operativos.

**Requisito(s) de información asociado(s)**
- RI-10: Información sobre configuración.

**Módulo**
- MOD-NF-04: Módulo de Configuración

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador consultar y modificar el valor de los parámetros operativos del sistema mediante un esquema de clave-valor, sin requerir intervención técnica sobre el código. No admite dar de alta nuevos parámetros ni dar de baja los existentes: el conjunto de claves está definido de antemano.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Acción |
| :--- | :--- |
| 1 | El sistema lista los parámetros configurados: Plazo de disponibilidad de grabaciones y antelación del aviso previo; Cantidad máxima de sesiones concurrentes por usuario; Datos institucionales utilizados en el sitio, comprobantes y constancias (razón social, CUIT, domicilio, logo, email de contacto y teléfono de contacto); Credenciales de integración con Google OAuth, con la pasarela de pagos y con HeyGen; Plazo máximo de espera para la confirmación de un pago antes de registrarlo como rechazado; Proporción de tipos de pregunta en los bancos generados con IA; Cantidad máxima semanal, por docente, de clases generadas con clon y de contenidos generados con IA (bancos de preguntas, resúmenes y presentaciones); Tiempo límite de edición de consultas y respuestas del foro; Mínimo de unidades por programa antes de publicarlo (registrar cohorte). |
| 2 | El actor selecciona un parámetro existente para modificar su valor. |
| 3 | El sistema valida que el valor haya sido completado. |
| 4 | El sistema registra o actualiza el parámetro. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

**Postcondición(es)**
- El parámetro queda registrado o actualizado con el nuevo valor.

**Excepciones**

| Paso | Acción |
| :--- | :--- |
| 3 | Si el valor no fue completado, el sistema informa el error y vuelve al paso 3. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- El esquema de clave-valor permite incorporar nuevos parámetros sin modificar el esquema de la base de datos.