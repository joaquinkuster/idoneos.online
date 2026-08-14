# 4.7. Casos de Uso Extendidos

En esta sección se detallan los 90 casos de uso extendidos del Sistema Idóneos Online, organizados según los 10 módulos (6 funcionales y 4 no funcionales). Cada caso de uso se referencia mediante un código correlativo (CU-01 a CU-90).

## MOD-F-01: Módulo del Catálogo de Cursos

### CU-01: Buscar curso
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Administrador o al Docente titular/supervisor buscar uno o más cursos registrados en el sistema, con fines de gestión.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - Existe al menos un curso registrado previamente.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más cursos. |
| 2 | El sistema solicita los criterios de búsqueda: nombre, categoría, modalidad y estado (Publicado / No publicado / Baja). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los cursos que coincidan con los criterios ingresados. Si el actor es Docente, el sistema restringe el resultado a los cursos en los que participa como titular o supervisor en alguno de sus dictados. |
| 5 | El sistema lista los cursos filtrados. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recuperan uno o más cursos que cumplen con los criterios de búsqueda, junto con su categoría, modalidades y estado.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta frecuentemente para la gestión diaria del catálogo.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-02: Registrar curso
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar un nuevo curso, definiendo su información comercial y académica básica.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - Existe al menos una categoría activa.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo curso. |
| 2 | El sistema solicita: nombre, descripción, precio, imagen de portada (opcional), categoría y modalidades de dictado. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios (nombre, descripción, precio, categoría y al menos una modalidad). |
| 5 | El sistema valida que el precio ingresado sea mayor o igual a cero. |
| 6 | El sistema registra el curso en estado no publicado. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El curso queda registrado y no publicado.
    - La fecha de creación refleja el momento del alta.
    - Las modalidades de dictado indicadas quedan asociadas al curso.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3.
    - **Paso 5**: Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — ocurre cada vez que se incorpora un nuevo curso al catálogo.
- **Estabilidad**: Alta
- **Comentarios**: El curso se publica en el catálogo mediante CU-03: Modificar curso, una vez cargado su contenido a través de al menos un programa con sus unidades.

---

### CU-03: Modificar curso
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar los datos de un curso registrado, incluyendo su publicación en el catálogo una vez que su contenido está cargado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El curso existe en el sistema y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso a modificar (ver CU-01: Buscar curso). |
| 2 | El sistema muestra los datos actuales del curso. |
| 3 | El actor modifica los datos que desea (nombre, descripción, precio, imagen, categoría, modalidades, estado de publicación). |
| 4 | El sistema valida que se mantengan completos los campos obligatorios (nombre, descripción, precio, categoría y al menos una modalidad). |
| 5 | El sistema valida que el precio ingresado sea mayor o igual a cero. |
| 6 | El sistema valida que, si se marca el curso como publicado, exista al menos un programa del curso con al menos 10 unidades con material publicado. |
| 7 | El sistema actualiza los datos del curso. |
| 8 | El sistema informa el éxito de la modificación. |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El curso queda actualizado con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
    - Las modalidades de dictado quedan actualizadas, si fueron modificadas.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el precio ingresado es menor a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si se intenta publicar un curso sin al menos un programa con al menos 10 unidades con material publicado, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — se usa para actualizar datos del curso y para publicarlo una vez cargado.
- **Estabilidad**: Alta
- **Comentarios**: El campo de estado de publicación es el que habilita o retira un curso del catálogo público (CU-05: Explorar catálogo de cursos).

---

### CU-04: Eliminar curso
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja un curso. Si el curso posee programas asociados, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El curso existe en el sistema y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso a dar de baja (ver CU-01: Buscar curso). |
| 2 | El sistema verifica que no existan programas asociados al curso. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el curso como dado de baja y lo retira del catálogo público. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: El curso queda en baja y deja de ser visible en el catálogo público.
- **Excepciones**:
    - **Paso 2**: Si el curso posee programas asociados, el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — ocurre cuando un curso deja de ofrecerse definitivamente.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-05: Explorar catálogo de cursos
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno, con o sin sesión iniciada, explorar el catálogo público de cursos publicados y consultar la ficha de un curso específico (temática, modalidades, contenido gratuito de muestra y los dictados con inscripción abierta) antes de decidir inscribirse.
- **Precondición(es)**: Existe al menos un curso publicado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor accede al catálogo público de cursos. |
| 2 | El sistema lista los cursos publicados, pudiendo filtrar por categoría o modalidad. |
| 3 | El actor selecciona un curso para ver su ficha. |
| 4 | El sistema muestra el detalle público del curso: descripción, modalidades, precio, el material marcado como gratuito si existe, y los dictados con inscripción abierta, con su docente titular y cupo disponible si corresponde. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de cursos publicados y, si corresponde, la ficha pública del curso seleccionado con sus dictados con inscripción abierta.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — es la puerta de entrada de cualquier interesado en inscribirse.
- **Estabilidad**: Alta
- **Comentarios**: No requiere sesión iniciada. El material marcado como gratuito funciona como gancho comercial para atraer nuevos alumnos, según lo relevado con el cliente.

---

### CU-06: Buscar categoría
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador buscar una o más categorías temáticas registradas en el sistema.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - Existe al menos una categoría registrada previamente.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar una o más categorías. |
| 2 | El sistema solicita el criterio de búsqueda: nombre. |
| 3 | El actor ingresa el criterio que desea. |
| 4 | El sistema recupera y filtra las categorías que coincidan con el criterio ingresado. |
| 5 | El sistema lista las categorías filtradas. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recuperan una o más categorías que cumplen con el criterio de búsqueda.
- **Excepciones**: No aplica.
- **Frecuencia**: Baja — se consulta al momento de gestionar el catálogo de categorías.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-07: Registrar categoría
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar una nueva categoría temática para clasificar los cursos.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar una nueva categoría. |
| 2 | El sistema solicita: nombre y descripción (opcional). |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que el nombre haya sido completado y que no exista otra categoría activa con el mismo nombre. |
| 5 | El sistema registra la categoría en estado activo. |
| 6 | El sistema informa el éxito del registro. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La categoría queda registrada en estado activo.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si el nombre no fue completado, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si ya existe una categoría activa con el mismo nombre, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Muy baja — el catálogo de categorías se define una vez y varía poco.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-08: Modificar categoría
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar el nombre y la descripción de una categoría registrada.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - La categoría existe en el sistema y se encuentra en estado activo.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar (ver CU-06: Buscar categoría). |
| 2 | El sistema muestra los datos actuales de la categoría. |
| 3 | El actor modifica el nombre o la descripción. |
| 4 | El sistema valida que el nombre no quede vacío y que no coincida con el de otra categoría activa. |
| 5 | El sistema actualiza los datos de la categoría. |
| 6 | El sistema informa el éxito de la modificación. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La categoría queda actualizada con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si el nombre queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el nombre coincide con el de otra categoría activa, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Muy baja — se usa esporádicamente para corregir datos de una categoría.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-09: Eliminar categoría
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja una categoría activa. Si la categoría posee cursos asociados que no están en baja, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - La categoría existe en el sistema y se encuentra en estado activo.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a dar de baja (ver CU-06: Buscar categoría). |
| 2 | El sistema verifica que no existan cursos activos asociados a la categoría. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca la categoría como dada de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: La categoría queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si la categoría posee cursos activos asociados, el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — ocurre cuando una temática deja de ofrecerse.
- **Estabilidad**: Alta
- **Comentarios**: La baja no elimina el registro físicamente; únicamente lo marca como inactivo.

---

### CU-10: Buscar programa
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar los programas (versiones del plan de estudios) registrados para un curso.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El curso existe y posee al menos un programa registrado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar los programas de un curso. |
| 2 | El sistema solicita el curso sobre el que se desea consultar. |
| 3 | El actor selecciona el curso. |
| 4 | El sistema recupera y lista los programas del curso, indicando cuál se encuentra vigente. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de programas del curso seleccionado, con su nombre, descripción, meses de acceso, cantidad de unidades e indicación de cuál es el vigente.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar el contenido o el histórico de ediciones de un curso.
- **Estabilidad**: Alta
- **Comentarios**: Solo el programa vigente admite nuevos dictados (ver CU-17: Registrar dictado); los programas anteriores se conservan para no afectar a los alumnos ya inscritos en dictados de una edición previa.

---

### CU-11: Registrar programa
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar un nuevo programa para un curso, definiendo una nueva versión de su plan de estudios junto con sus unidades.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El curso existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso para el cual desea registrar un nuevo programa (ver CU-01: Buscar curso). |
| 2 | El sistema solicita: nombre, descripción (opcional) y meses de acceso al contenido desde la inscripción. |
| 3 | El actor ingresa los datos solicitados y comienza a cargar unidades: por cada una, el título, la descripción y el número de orden. |
| 4 | El sistema valida que el nombre y los meses de acceso hayan sido completados, y que se haya cargado al menos una unidad. |
| 5 | El sistema valida que los meses de acceso sean un valor entero mayor a cero. |
| 6 | El sistema valida que cada unidad tenga título y número de orden completos, que el número de orden sea un entero mayor a cero y que no se repita entre las unidades cargadas. |
| 7 | El sistema registra el programa con sus unidades, que pasa a ser el vigente del curso al ser el de fecha de creación más reciente, dejando de considerarse vigente al programa anterior, si existía. |
| 8 | El sistema informa el éxito del registro. |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El programa queda registrado con sus unidades, y pasa a ser el vigente del curso, al ser el de fecha de creación más reciente.
    - El programa anterior del curso, si existía, deja de estar vigente, sin perder sus unidades ni afectar a los alumnos ya inscritos en dictados abiertos bajo ese programa.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si el nombre o los meses de acceso no fueron completados, o no se cargó ninguna unidad, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si los meses de acceso ingresados no son un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si alguna unidad tiene el título o el número de orden incompletos, el número de orden no es un entero mayor a cero, o se repite entre las unidades cargadas, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — ocurre al definir el contenido inicial de un curso o al actualizarlo en una nueva edición.
- **Estabilidad**: Alta
- **Comentarios**: El contenido de las unidades (material, glosario, foro, pools y autoevaluaciones) se carga por separado una vez registrado el programa (ver CU-20: Editar contenido de unidad); no se copia automáticamente del programa anterior. El programa debe contar con un mínimo de 10 unidades con material publicado para que el curso pueda publicarse, según lo relevado con el cliente; esta validación se controla en CU-03: Modificar curso al momento de la publicación.

---

### CU-12: Modificar programa
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar el nombre, la descripción o los meses de acceso de un programa, y agregar, editar o quitar sus unidades.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El programa existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a modificar (ver CU-10: Buscar programa). |
| 2 | El sistema muestra los datos actuales del programa, con sus unidades. |
| 3 | El actor modifica el nombre, la descripción o los meses de acceso, o agrega, edita o quita unidades (título, descripción y número de orden de cada una). |
| 4 | El sistema valida que el nombre y los meses de acceso no queden vacíos, y que el programa conserve al menos una unidad. |
| 5 | El sistema valida que los meses de acceso sean un valor entero mayor a cero. |
| 6 | El sistema valida que cada unidad conserve título y número de orden, que el número de orden sea un entero mayor a cero y que no se repita entre las unidades del programa. |
| 7 | El sistema valida que ninguna unidad a quitar tenga algún intento de autoevaluación aprobado registrado por un alumno. |
| 8 | El sistema actualiza los datos del programa y sus unidades. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El programa y sus unidades quedan actualizados con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si el nombre o los meses de acceso quedan vacíos, o el programa queda sin unidades, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si los meses de acceso ingresados no son un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si alguna unidad queda con el título o el número de orden incompletos, el número de orden no es un entero mayor a cero, o se repite entre las unidades del programa, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si alguna unidad que se intenta quitar ya registra un intento de autoevaluación aprobado, el sistema informa la dependencia y no permite quitarla.
- **Frecuencia**: Baja — se usa para corregir datos de un programa ya definido.
- **Estabilidad**: Alta
- **Comentarios**: Modificar los meses de acceso no afecta retroactivamente el vencimiento de acceso ya calculado para alumnos inscriptos en dictados previos de este programa.

---

### CU-13: Eliminar programa
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja un programa que nunca llegó a tener dictados asociados (por ejemplo, un alta registrada por error). Si el programa posee o tuvo algún dictado, el sistema informa la dependencia y no permite la baja, para preservar el historial académico de los alumnos que cursaron bajo él.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El programa existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a dar de baja (ver CU-10: Buscar programa). |
| 2 | El sistema verifica que el programa no tenga ningún dictado asociado. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el programa y sus unidades como dados de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: El programa y sus unidades quedan en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el programa posee algún dictado asociado, el sistema informa la dependencia y no permite la baja; para eliminarlo, primero deben eliminarse sus dictados.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — solo aplica a programas sin dictados asociados.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-14: Buscar unidad
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar las unidades de un programa de un curso, con fines de gestión de su contenido o de la estructura del programa.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El programa existe y posee al menos una unidad registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar las unidades de un curso. |
| 2 | El actor selecciona el curso. |
| 3 | El sistema utiliza el programa vigente del curso, salvo que el actor lo haya cambiado previamente (ver CU-15: Cambiar programa). |
| 4 | El sistema recupera y lista las unidades del programa, ordenadas por su número de orden. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de unidades del programa, con su título, número de orden y cantidad de material cargado.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta cada vez que se gestiona el contenido o la estructura de un curso.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-15: Cambiar programa
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador cambiar el programa de un curso sobre el que está trabajando, para visualizar y gestionar el contenido de un programa distinto al vigente.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El curso posee más de un programa registrado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa del curso al que desea cambiar (ver CU-10: Buscar programa). |
| 2 | El sistema establece el programa seleccionado como el programa de trabajo del actor para ese curso. |
| 3 | El sistema recupera y lista las unidades del programa seleccionado (ver CU-14: Buscar unidad). |
| 4 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de unidades del programa seleccionado, con su título, número de orden y cantidad de material cargado. El contexto de trabajo del actor para ese curso queda orientado a este programa mientras dure la sesión, hasta que lo cambie nuevamente o busque otro curso.
- **Excepciones**: No aplica.
- **Frecuencia**: Baja — se utiliza para consultar o corregir puntualmente el contenido de una edición anterior de un curso.
- **Estabilidad**: Alta
- **Comentarios**: Al buscar un curso (ver CU-01: Buscar curso), el sistema selecciona automáticamente su programa vigente; este caso de uso permite trabajar sobre otro programa del mismo curso sin alterar cuál es el vigente.

---

### CU-16: Buscar dictado
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar los dictados programados de un programa.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - Existe al menos un dictado registrado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar dictados. |
| 2 | El sistema solicita el programa sobre el que se desea consultar y, opcionalmente, el estado (Planificado / En curso / Finalizado / Cancelado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los dictados que coinciden, restringidos a aquellos en los que el docente participa como titular o supervisor si el actor es Docente. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de dictados del programa seleccionado, con su fecha de inicio, fecha de fin, cupo máximo, docente titular, docente supervisor si corresponde, y estado.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta para el seguimiento de las ediciones abiertas o programadas de un curso.
- **Estabilidad**: Alta
- **Comentarios**: El estado se determina a partir de las fechas del dictado y de si fue dado de baja.

---

### CU-17: Registrar dictado
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar un nuevo dictado para el programa vigente de un curso, definiendo su cronograma, cupo y equipo docente.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El programa existe, se encuentra vigente y no en baja.
    - Existe al menos un docente habilitado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso para el cual desea registrar un nuevo dictado, para el programa vigente de dicho curso (ver CU-01: Buscar curso). |
| 2 | El sistema solicita: fecha de inicio, fecha de fin, cupo máximo de alumnos (opcional), docente titular y docente supervisor (opcional). |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios (fecha de inicio, fecha de fin y docente titular). |
| 5 | El sistema valida que el docente indicado como titular, y el supervisor si corresponde, se encuentren habilitados. |
| 6 | El sistema valida que la fecha de fin sea posterior a la fecha de inicio. |
| 7 | El sistema valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 8 | El sistema registra el dictado, asociado al programa vigente, con el docente titular y, si corresponde, el supervisor. |
| 9 | El sistema informa el éxito del registro. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El dictado queda registrado, asociado al programa vigente del curso, con su equipo docente.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el docente indicado como titular, o el supervisor si corresponde, no se encuentran habilitados, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la fecha de fin no es posterior a la fecha de inicio, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — ocurre cada vez que se abre una nueva edición de un curso.
- **Estabilidad**: Alta
- **Comentarios**: El sistema exige exactamente un docente titular por dictado; el docente supervisor es opcional. Un dictado sin cupo máximo indicado no tiene límite de inscriptos. Solo puede registrarse dictados sobre el programa vigente del curso (ver CU-11: Registrar programa).

---

### CU-18: Modificar dictado
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar la fecha de inicio, la fecha de fin, el cupo máximo o el equipo docente de un dictado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El dictado existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el dictado a modificar (ver CU-16: Buscar dictado). |
| 2 | El sistema muestra los datos actuales del dictado. |
| 3 | El actor modifica los datos que desea. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios (fecha de inicio, fecha de fin y docente titular). |
| 5 | Si se modificó el docente titular, o el supervisor, el sistema valida que se encuentren habilitados. |
| 6 | El sistema valida que la fecha de fin sea posterior a la fecha de inicio. |
| 7 | El sistema valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 8 | El sistema actualiza los datos del dictado. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El dictado queda actualizado con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el docente indicado como titular, o el supervisor, no se encuentran habilitados, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la fecha de fin no es posterior a la fecha de inicio, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para ajustar el cronograma, el cupo o el equipo docente de un dictado ya programado.
- **Estabilidad**: Alta
- **Comentarios**: No es posible modificar un dictado que ya se encuentra Finalizado o Cancelado.

---

### CU-19: Eliminar dictado
- **Objetivo(s) asociado(s)**: OBJ-01: Gestionar y explorar el catálogo de cursos.
- **Requisito(s) de información asociado(s)**: RI-01: Información sobre el catálogo de cursos.
- **Módulo**: MOD-F-01: Módulo del Catálogo de Cursos
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja (cancelar) un dictado. Si el dictado posee alguna inscripción activa (vigente o finalizada, sin dar de baja), el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El dictado existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el dictado a dar de baja (ver CU-16: Buscar dictado). |
| 2 | El sistema verifica que el dictado no registre inscripciones vigentes o finalizadas que no hayan sido dadas de baja. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el dictado como dado de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: El dictado queda en baja (Cancelado).
- **Excepciones**:
    - **Paso 2**: Si el dictado registra alguna inscripción vigente o finalizada que no fue dada de baja, el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — ocurre cuando una edición programada no llega a dictarse.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-F-02: Módulo de Contenido de Unidades

### CU-20: Editar contenido de unidad
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar las unidades de un curso con fines de gestión de contenido, para acceder al contenido de cada una, y registrarlo, modificarlo o eliminarlo.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El programa existe y posee al menos una unidad registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca las unidades de un curso con fines de gestión de contenido (ver CU-14: Buscar unidad). |
| 2 | El sistema lista las unidades del programa, con acceso, para cada una, a su material, pools, autoevaluaciones y clases. |
| 3 | El actor selecciona la unidad cuyo contenido desea gestionar. |
| 4 | El sistema despliega el contenido de la unidad seleccionada, con las opciones para darlo de alta, modificarlo o eliminarlo. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de unidades del curso con acceso para gestionar el contenido de cada una. Al seleccionar una unidad, se despliega su material, sus pools, sus autoevaluaciones y sus clases, con las opciones para gestionarlos.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se activa cada vez que se gestiona el contenido de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-21: Ver contenido de unidad
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno buscar las unidades del programa del curso en el que está inscripto y acceder al contenido publicado de la que seleccione, respetando su avance secuencial.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El alumno posee una inscripción vigente al curso.
    - El programa del dictado del alumno posee al menos una unidad registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno solicita buscar las unidades de un curso en el que está inscripto. |
| 2 | El sistema recupera y lista las unidades del programa del dictado del alumno, ordenadas por su número de orden, indicando si cada una se encuentra habilitada según su avance secuencial. |
| 3 | El alumno selecciona una unidad habilitada. |
| 4 | El sistema verifica que la unidad se encuentre habilitada para el alumno según su avance secuencial (el progreso de la unidad anterior figura como completado, o es la primera unidad del curso). |
| 5 | El sistema despliega el contenido publicado de la unidad: su material, sus términos de glosario, el acceso al foro y, si corresponde, el acceso a su autoevaluación y a su clase en vivo en curso. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de unidades del programa del dictado del alumno, indicando cuáles están habilitadas. Al seleccionar una unidad habilitada, se recupera su material publicado (grabación, bibliografía, presentación o resumen), sus términos de glosario, el acceso a las consultas del foro y, si corresponde, el acceso a la autoevaluación de la unidad y a la clase en vivo en curso.
- **Excepciones**:
    - **Paso 4**: Si la unidad todavía no está habilitada para el alumno, el sistema informa que debe aprobar primero la autoevaluación de la unidad anterior y no despliega su contenido.
- **Frecuencia**: Alta — es la acción central del cursado para el Alumno.
- **Estabilidad**: Alta
- **Comentarios**: El material que ve el Alumno excluye el que el Administrador o el Docente mantienen sin publicar (por ejemplo, contenido generado por IA pendiente de revisión).

---

### CU-22: Buscar material
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar el material (grabaciones, bibliografía, presentaciones y resúmenes) cargado en una unidad, con fines de gestión.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La unidad existe y posee al menos un material cargado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar material dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el tipo de material, el título y si fue generado por IA. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista el material que coincide con los criterios, incluyendo el no publicado. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el material de la unidad, indicando su tipo, título, si fue generado por IA y su estado de publicación.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta cada vez que se gestiona el contenido de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: A diferencia del acceso del Alumno mediante CU-21: Ver contenido de unidad, este CU también recupera el material no publicado, ya que su fin es la gestión y no el consumo por parte del alumno.

---

### CU-23: Subir material
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular/supervisor cargar manualmente un material (grabación, bibliografía o presentación) en una unidad. Cada tipo de material solicita datos específicos.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad existe, no se encuentra en baja y pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor desea subir un nuevo material dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita el tipo de material (Grabación, Bibliografía o Presentación) y el título. |
| 3 | El actor selecciona el tipo e ingresa el título. |
| 4 | Según el tipo elegido, el sistema solicita: el archivo de video (Grabación); el archivo o enlace externo y el autor (Bibliografía); o el archivo de la presentación (Presentación). |
| 5 | El actor ingresa los datos solicitados. |
| 6 | El sistema valida que se hayan completado el título, el tipo y los datos obligatorios según el tipo seleccionado. |
| 7 | El sistema registra el material en estado no publicado. |
| 8 | El sistema informa el éxito de la carga. |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El material queda registrado, asociado a la unidad, en estado no publicado.
    - La fecha de creación refleja el momento de la carga.
- **Excepciones**:
    - **Paso 6**: Si no se completó el título o alguno de los datos obligatorios según el tipo de material, el sistema informa el error y vuelve al paso 5.
- **Frecuencia**: Alta — ocurre por cada material cargado de cada unidad de cada curso.
- **Estabilidad**: Alta
- **Comentarios**: El material de tipo Grabación también puede originarse automáticamente desde CU-63: Finalizar clase en vivo o CU-69: Generar clase con Clon IA; el material de tipo Presentación y Resumen también puede generarse desde el Módulo de Generación de Contenido con IA. Este CU cubre específicamente la carga manual por parte del docente.

---

### CU-24: Modificar material
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular/supervisor modificar el título de un material y, en particular, su estado de publicación para habilitarlo u ocultarlo a los alumnos.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - El material existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a modificar (ver CU-22: Buscar material). |
| 2 | El sistema muestra los datos actuales del material. |
| 3 | El actor modifica el título, el archivo o el estado de publicación. |
| 4 | El sistema valida que el título no quede vacío. |
| 5 | Si se modificó el archivo, el sistema valida que se hayan completado los datos obligatorios correspondientes al tipo de material. |
| 6 | El sistema actualiza los datos del material. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El material queda actualizado con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si el título queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el nuevo archivo no cumple los datos obligatorios del tipo de material, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Alta — se usa para corregir contenido y, principalmente, para publicar material generado por IA o generado en clases en vivo tras su revisión.
- **Estabilidad**: Alta
- **Comentarios**: Publicar un material lo hace visible para los alumnos con acceso habilitado a esa unidad, mediante CU-21: Ver contenido de unidad.

---

### CU-25: Eliminar material
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador dar de baja un material cargado en una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El material existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a dar de baja (ver CU-22: Buscar material). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca el material como dado de baja y deja de mostrarlo a los alumnos. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: El material queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — se usa para retirar material desactualizado o cargado por error.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-26: Buscar término de glosario
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar los términos del glosario cargados en una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La unidad existe y posee al menos un término de glosario cargado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar términos dentro del glosario de una unidad. |
| 2 | El sistema solicita opcionalmente el término o la definición. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los términos de glosario que coinciden con los criterios ingresados. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de términos y definiciones del glosario de la unidad.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar el contenido de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-27: Registrar término de glosario
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular/supervisor registrar un nuevo término y su definición en el glosario de una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad existe, no se encuentra en baja y pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor desea registrar un término de glosario dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita: término y definición. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que el término y la definición hayan sido completados y que el término no esté ya registrado en el glosario de esa unidad. |
| 5 | El sistema registra el término de glosario asociado a la unidad. |
| 6 | El sistema informa el éxito del registro. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**: El término de glosario queda registrado y asociado a la unidad.
- **Excepciones**:
    - **Paso 4**: Si el término o la definición no fueron completados, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el término ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — ocurre al cargar el glosario de cada unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-28: Modificar término de glosario
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular/supervisor modificar el término o la definición de un término de glosario registrado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - El término de glosario existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a modificar (ver CU-26: Buscar término de glosario). |
| 2 | El sistema muestra los datos actuales del término. |
| 3 | El actor modifica el término o la definición. |
| 4 | El sistema valida que ninguno de los dos campos quede vacío. |
| 5 | Si se modificó el término, el sistema valida que no coincida con el de otro término ya registrado en el glosario de esa unidad. |
| 6 | El sistema actualiza el término de glosario. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**: El término de glosario queda actualizado con los nuevos datos.
- **Excepciones**:
    - **Paso 4**: Si el término o la definición quedan vacíos, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el término modificado ya está registrado en el glosario de esa unidad, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para corregir definiciones ya cargadas.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-29: Eliminar término de glosario
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador dar de baja un término del glosario de una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El término de glosario existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el término de glosario a dar de baja (ver CU-26: Buscar término de glosario). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca el término de glosario como dado de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: El término de glosario queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — se usa para retirar términos cargados por error o ya desactualizados.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-30: Buscar consulta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite consultar las preguntas planteadas por los alumnos en el foro de una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno, Docente o Administrador.
    - Existe al menos una consulta de foro registrada en la unidad.
    - Si el actor es Alumno posee una inscripción vigente al curso de la unidad.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar consultas dentro del foro de una unidad. |
| 2 | El sistema solicita opcionalmente el texto o el rango de fechas. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las consultas de foro que coinciden con los criterios, con sus respuestas asociadas si existen, restringidas según el rol del actor. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de consultas del foro de la unidad, junto con sus respuestas.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta cada vez que un alumno o docente revisa el foro de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-31: Registrar consulta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno registrar una consulta en el foro de una unidad del curso en el que está inscripto.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El alumno posee una inscripción vigente al curso de la unidad.
    - La unidad se encuentra habilitada según el avance secuencial del alumno.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar una consulta en el foro dentro de una unidad (ver CU-21: Ver contenido de unidad). |
| 2 | El sistema solicita el texto de la consulta. |
| 3 | El actor ingresa el texto. |
| 4 | El sistema valida que el texto haya sido completado. |
| 5 | El sistema registra la consulta asociada a la unidad y al alumno, con la fecha actual. |
| 6 | El sistema notifica al docente titular y al supervisor, si corresponde, la nueva consulta. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La consulta queda registrada, asociada a la unidad y al alumno.
    - El docente recibe la notificación de la nueva consulta.
- **Excepciones**:
    - **Paso 4**: Si el texto de la consulta no fue completado, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — depende de las dudas que le surjan al alumno durante el cursado.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-32: Modificar consulta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno modificar el texto de una consulta de foro propia, dentro de un plazo límite configurable desde su registro.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - La consulta de foro existe, no se encuentra en baja y fue registrada por el actor.
    - No se superó el plazo límite de edición configurado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro propia a modificar (ver CU-30: Buscar consulta de foro). |
| 2 | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la consulta. |
| 3 | El sistema muestra el texto actual de la consulta. |
| 4 | El actor modifica el texto. |
| 5 | El sistema valida que el texto no quede vacío. |
| 6 | El sistema actualiza la consulta. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**: La consulta queda actualizada con el nuevo texto.
- **Excepciones**:
    - **Paso 2**: Si se superó el plazo límite de edición, el sistema informa que la consulta ya no puede modificarse y finaliza el caso de uso.
    - **Paso 5**: Si el texto queda vacío, el sistema informa el error y vuelve al paso 4.
- **Frecuencia**: Baja — se usa para corregir o ampliar una consulta recién publicada.
- **Estabilidad**: Alta
- **Comentarios**: El plazo límite de edición es un parámetro configurable desde el Módulo de Configuración.

---

### CU-33: Eliminar consulta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja una consulta de foro ante una publicación indebida (por ejemplo, contenido ofensivo o ajeno a la temática de la unidad).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - La consulta de foro existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la consulta de foro a dar de baja (ver CU-30: Buscar consulta de foro). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la consulta como dada de baja, junto con las respuestas asociadas si existen. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: La consulta y sus respuestas asociadas quedan en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — se usa excepcionalmente ante contenido indebido.
- **Estabilidad**: Alta
- **Comentarios**: No surge de la entrevista con el cliente como requisito explícito; se incorpora como criterio de moderación razonable para un foro con alumnos y docentes.

---

### CU-34: Buscar respuesta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite consultar las respuestas registradas a una consulta de foro. La vista varía según el rol del actor, con el mismo criterio que CU-30: Buscar consulta de foro.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno, Docente o Administrador.
    - Existe al menos una respuesta registrada para la consulta.
    - Si el actor es Alumno posee una inscripción vigente al curso de la unidad.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver las respuestas de una consulta de foro. |
| 2 | El sistema recupera y lista las respuestas asociadas a la consulta. |
| 3 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de respuestas asociadas a la consulta de foro.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta junto con CU-30: Buscar consulta de foro.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-35: Registrar respuesta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular o supervisor del curso registrar una respuesta a una consulta de foro planteada por un alumno.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La consulta de foro existe, no se encuentra en baja y pertenece a un curso en el que el actor participa como titular o supervisor.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita responder una consulta de foro (ver CU-30: Buscar consulta de foro). |
| 2 | El sistema solicita el texto de la respuesta. |
| 3 | El actor ingresa el texto. |
| 4 | El sistema valida que el texto haya sido completado. |
| 5 | El sistema registra la respuesta asociada a la consulta y al docente, con la fecha actual. |
| 6 | El sistema notifica al alumno autor de la consulta que fue respondida. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La respuesta queda registrada, asociada a la consulta y al docente.
    - El alumno recibe la notificación de la respuesta.
- **Excepciones**:
    - **Paso 4**: Si el texto de la respuesta no fue completado, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — depende de la cantidad de consultas que reciba cada curso.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-36: Modificar respuesta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente modificar el texto de una respuesta de foro propia, dentro de un plazo límite configurable desde su registro.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La respuesta de foro existe, no se encuentra en baja y fue registrada por el actor.
    - No se superó el plazo límite de edición configurado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro propia a modificar (ver CU-34: Buscar respuesta de foro). |
| 2 | El sistema verifica que no se haya superado el plazo límite de edición desde el registro de la respuesta. |
| 3 | El sistema muestra el texto actual de la respuesta. |
| 4 | El actor modifica el texto. |
| 5 | El sistema valida que el texto no quede vacío. |
| 6 | El sistema actualiza la respuesta. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**: La respuesta queda actualizada con el nuevo texto.
- **Excepciones**:
    - **Paso 2**: Si se superó el plazo límite de edición, el sistema informa que la respuesta ya no puede modificarse y finaliza el caso de uso.
    - **Paso 5**: Si el texto queda vacío, el sistema informa el error y vuelve al paso 4.
- **Frecuencia**: Baja — se usa para corregir o ampliar una respuesta recién publicada.
- **Estabilidad**: Alta
- **Comentarios**: El plazo límite de edición es un parámetro configurable desde el Módulo de Configuración.

---

### CU-37: Eliminar respuesta de foro
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Contenido de Unidades
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja una respuesta de foro ante una publicación indebida.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - La respuesta de foro existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la respuesta de foro a dar de baja (ver CU-34: Buscar respuesta de foro). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la respuesta como dada de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: La respuesta queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — se usa excepcionalmente ante contenido indebido.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-F-03: Módulo de Inscripciones

### CU-38: Buscar inscripción
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno, Administrador
- **Descripción**: Permite consultar una o más inscripciones registradas en el sistema, incluyendo el certificado de finalización emitido cuando corresponda. La vista varía según el rol del actor: el Alumno visualiza únicamente las propias; el Administrador visualiza todas.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
    - Existe al menos una inscripción registrada previamente.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar una o más inscripciones. |
| 2 | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador) y estado (Vigente / Vencida / Dada de baja). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra las inscripciones que coincidan con los criterios ingresados, restringidas a las propias si el actor es Alumno. |
| 5 | El sistema lista las inscripciones filtradas. |
| 6 | El actor puede generar el certificado de la inscripción seleccionada, si fue emitido. |
| 7 | Fin del caso de uso. |

- **Salida**: Se recuperan una o más inscripciones que cumplen con los criterios de búsqueda, con su curso, fecha, fecha de vencimiento de acceso, estado y, si el certificado fue emitido, sus datos (número y fecha de emisión) con el archivo para descargar sí lo necesita.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — el alumno la consulta para ver sus cursos activos; el Administrador, para el seguimiento comercial.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-39: Inscribir curso
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno inscribirse a un dictado con inscripción abierta de un curso publicado, dando inicio al proceso de inscripción que se completa con el pago del curso si tiene costo.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El curso se encuentra publicado y posee al menos un dictado con inscripción abierta.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, tras explorar el catálogo y consultar la ficha pública de un curso (ver CU-05: Explorar catálogo de cursos), selecciona un dictado con inscripción abierta y solicita inscribirse. |
| 2 | El sistema valida que la fecha actual sea anterior a la fecha de inicio del dictado seleccionado. |
| 3 | El sistema valida que el dictado no haya alcanzado su cupo máximo, si tiene uno definido. |
| 4 | El sistema valida que el alumno no posea ya una inscripción vigente a ese dictado. |
| 5 | Si el curso tiene costo, el sistema evalúa si el alumno cumple alguna condición de descuento vigente y, de ser así, calcula el monto a pagar con el descuento aplicado (ver PA-3: Aplicación automática de descuentos), e informa al alumno el costo de la inscripción. |
| 6 | Si el curso tiene costo, el sistema deriva al alumno al pago (CU-42: Realizar pago) y aguarda su resultado. |
| 7 | El sistema registra la inscripción, con la fecha actual, la fecha de vencimiento de acceso calculada según los meses de acceso del programa del dictado y, si el curso tiene costo, asociada al pago realizado. |
| 8 | El sistema registra el progreso inicial del alumno sobre la primera unidad del programa del dictado, sin completar (0% de avance). |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La inscripción queda registrada, con el progreso inicial del alumno registrado sobre la primera unidad del programa, sin completar.
    - El acceso al contenido del curso permanece bloqueado hasta que se confirme el pago mediante CU-42: Realizar pago.
- **Excepciones**:
    - **Paso 2**: Si la fecha actual no es anterior a la fecha de inicio del dictado, el sistema informa que la inscripción a ese dictado ya no está habilitada y finaliza el caso de uso.
    - **Paso 3**: Si el dictado ya alcanzó su cupo máximo, el sistema lo informa y finaliza el caso de uso.
    - **Paso 4**: Si el alumno ya posee una inscripción vigente a ese dictado, el sistema lo informa y finaliza el caso de uso.
    - **Paso 6**: En caso de que el curso sea gratuito, se omite el paso 6 y el sistema continúa directamente al paso 7.
- **Frecuencia**: Alta — ocurre cada vez que un alumno decide comenzar un curso.
- **Estabilidad**: Alta
- **Comentarios**: El sistema informa los detalles del dictado al alumno antes de confirmar la inscripción desde la ficha pública del curso.

---

### CU-40: Dar de baja inscripción
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno darse de baja de un curso en el que está inscripto, registrando el abandono. La baja no genera reembolso del pago realizado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - La inscripción existe, pertenece al actor, se encuentra vigente y con pago acreditado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la inscripción a dar de baja (ver CU-38: Buscar inscripción). |
| 2 | El sistema solicita confirmación y, opcionalmente, un motivo u observación de la baja. |
| 3 | El actor confirma la baja y, si lo desea, ingresa el motivo. |
| 4 | El sistema informa que la baja no genera reembolso del pago realizado y solicita confirmación final. |
| 5 | El actor confirma. |
| 6 | El sistema registra la baja de la inscripción, con la observación ingresada si corresponde. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La inscripción queda en estado de baja.
    - El alumno pierde el acceso al contenido del curso.
- **Excepciones**:
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — ocurre cuando un alumno decide abandonar un curso antes de finalizarlo.
- **Estabilidad**: Alta
- **Comentarios**: No existe mecanismo de reembolso: la política de no devolución fue confirmada por el cliente, análoga a la habitual en el ámbito universitario.

---

### CU-41: Buscar pago
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno, Administrador
- **Descripción**: Permite consultar uno o más pagos registrados en el sistema. La vista varía según el rol del actor: el Alumno visualiza únicamente los propios; el Administrador visualiza todos.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
    - Existe al menos un pago registrado previamente.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más pagos. |
| 2 | El sistema solicita los criterios de búsqueda: curso, alumno (solo para Administrador), estado (Pendiente / Acreditado / Rechazado) y rango de fecha. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los pagos que coincidan con los criterios ingresados, restringidos a los propios si el actor es Alumno. |
| 5 | El sistema lista los pagos filtrados. |
| 6 | El actor puede generar el comprobante del pago seleccionado, si el pago fue acreditado. |
| 7 | Fin del caso de uso. |

- **Salida**: Se recuperan uno o más pagos que cumplen con los criterios de búsqueda, con su monto, fecha, método, estado y, si el pago fue acreditado, los datos del comprobante (número y fecha de emisión) con el archivo para descargar sí lo necesita.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — el alumno la consulta para verificar sus pagos; el Administrador, para el control de acreditaciones.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-42: Realizar pago
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno, indirectamente mediante CU-39: Inscribir curso
- **Descripción**: Permite al Alumno pagar de forma online un curso al que se desea inscribir, con MODO como método de pago mediante integración con la API de MODO, por el total del curso con el descuento aplicado si corresponde (ver PA-2: Pago online con billetera virtual).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El curso para el cual se desea realizar un pago debe estar publicado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno solicita pagar el curso al que desea inscribirse. |
| 2 | El sistema muestra el monto a pagar, con el descuento aplicado si corresponde. |
| 3 | El sistema le pide al alumno que elija el medio de pago. |
| 4 | El alumno selecciona MODO como medio de pago. |
| 5 | El sistema arma la solicitud de pago por ese monto y se la envía a MODO. |
| 6 | El sistema muestra el modal de pago de MODO: en computadora, un código QR para escanear con la app de MODO o con la app del banco; en celular, lo redirige a una pantalla para elegir con qué app quiere pagar. |
| 7 | El alumno escanea el código QR (o abre la app elegida desde el celular) y completa el pago desde ahí, con la tarjeta o el saldo de cuenta que prefiera. |
| 8 | El sistema registra el pago como pendiente, con los datos que devuelve MODO al crear la solicitud. |
| 9 | MODO procesa el pago y le avisa al sistema el resultado (acreditado o rechazado) más adelante, de forma automática. |
| 10 | El sistema actualiza el pago con el resultado, el nombre del pagador y, si fue con tarjeta, sus últimos cuatro dígitos, y guarda la fecha en que se aprobó. |
| 11 | Si el pago fue acreditado, el sistema habilita el acceso al curso, genera los datos del comprobante y se lo envía al alumno por correo, con el archivo para descargar cuando lo necesite. |
| 12 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El pago queda registrado con su resultado.
    - Si el pago fue acreditado: el acceso al curso queda habilitado, y los datos del comprobante quedan registrados en el pago y enviados por correo electrónico al alumno.
    - Si el pago fue rechazado: la inscripción permanece sin acceso habilitado y se notifica al alumno.
    - Si el pago queda pendiente, el alumno recibe la notificación correspondiente. Cuando MODO confirme el resultado de forma asincrónica, el sistema actualiza el estado del pago y notifica al alumno.
- **Excepciones**:
    - **Paso 5**: Si el alumno cancela el pago desde el modal de MODO, el sistema informa que la operación fue cancelada y finaliza el caso de uso.
    - **Paso 7**: Si MODO rechaza el pago, el sistema registra el pago como rechazado y notifica al alumno el motivo, permitiéndole reintentar.
    - **Paso 7**: Si no se recibe la confirmación de MODO dentro del plazo configurado (ver CU-90: Configurar parámetros), el pago queda pendiente y el sistema notifica al alumno que su pago está en proceso.
- **Frecuencia**: Alta — ocurre por cada inscripción confirmada.
- **Estabilidad**: Media — depende de las condiciones de integración de la API de MODO.
- **Comentarios**: –

---

### CU-43: Buscar progreso
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite consultar el progreso de un alumno en las unidades del programa del dictado en el que está inscripto. La vista varía según el rol del actor: el Alumno visualiza únicamente el propio; el Docente y Administrador visualizan el de cualquier alumno.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno, Docente o Administrador.
    - El alumno posee una inscripción registrada al curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita consultar el progreso de un alumno en un curso. |
| 2 | El sistema solicita los criterios de búsqueda: curso y alumno (solo para Docente y Administrador). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera el progreso del alumno en cada unidad del programa de su dictado, restringido al propio si el actor es Alumno. |
| 5 | El sistema lista el progreso recuperado. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recupera, para cada unidad del programa del dictado del alumno, si fue completada y, en caso afirmativo, la fecha en que se completó.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — el alumno la consulta para verificar su avance; el Docente y Administrador, para el seguimiento del cursado e identificar atascos.
- **Estabilidad**: Alta
- **Comentarios**: El progreso de una unidad se registra automáticamente al aprobar el intento de autoevaluación correspondiente (ver CU-57: Realizar intento de autoevaluación).

---

### CU-44: Buscar descuento
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador buscar uno o más descuentos registrados en el sistema.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - Existe al menos un descuento registrado previamente.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más descuentos. |
| 2 | El sistema solicita los criterios de búsqueda: nombre y vigencia (Vigente / Vencido / Agotado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los descuentos que coincidan con los criterios ingresados. |
| 5 | El sistema lista los descuentos filtrados. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recuperan uno o más descuentos que cumplen con los criterios de búsqueda, con su porcentaje, vigencia, cantidad límite y cantidad usada.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar promociones y campañas comerciales.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-45: Registrar descuento
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar un nuevo descuento a aplicar automáticamente a los alumnos que cumplan la condición configurada.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

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

- **Postcondición(es)**:
    - El descuento queda registrado y activo.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y vuelve al paso 3.
    - **Paso 5**: Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 8**: Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa al lanzar una nueva campaña o promoción comercial.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-46: Modificar descuento
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar los datos de un descuento registrado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El descuento existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar (ver CU-44: Buscar descuento). |
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

- **Postcondición(es)**:
    - El descuento queda actualizado con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el porcentaje ingresado no está entre 1 y 100, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si la cantidad límite ingresada no es un número entero mayor a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 8**: Si la cantidad de cursos requeridos ingresada no es un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para ajustar los términos de una promoción vigente.
- **Estabilidad**: Alta
- **Comentarios**: El sistema desactiva automáticamente el descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero (ver PA-3: Aplicación automática de descuentos).

---

### CU-47: Eliminar descuento
- **Objetivo(s) asociado(s)**: OBJ-03: Gestionar la inscripción y el pago de los alumnos.
- **Requisito(s) de información asociado(s)**: RI-03: Información sobre inscripciones.
- **Módulo**: MOD-F-03: Módulo de Inscripciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja un descuento antes de su vencimiento natural. Si el descuento ya fue aplicado a alguna inscripción, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El descuento existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a dar de baja (ver CU-44: Buscar descuento). |
| 2 | El sistema verifica que la cantidad usada del descuento sea cero. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el descuento como dado de baja, y ya no poder aplicarlo en inscripciones. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: El descuento queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el descuento ya fue aplicado a alguna inscripción (cantidad usada mayor a cero), el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — ocurre cuando se decide cancelar una promoción antes de su vencimiento.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-F-04: Módulo de Evaluaciones

### CU-48: Buscar pool
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar los pools de preguntas registrados en una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La unidad existe y posee al menos un pool registrado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar pools dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre del pool. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los pools que coinciden con los criterios, con su cantidad de preguntas cargadas. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de pools de la unidad, con su nombre y cantidad de preguntas.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar las evaluaciones de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-49: Crear pool
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente crear un nuevo pool de preguntas para una unidad, cargando manualmente sus preguntas de opción múltiple o verdadero/falso junto con las opciones de respuesta de cada una.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad existe, no se encuentra en baja y pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea crear un nuevo pool dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita el nombre del pool. |
| 3 | El actor ingresa el nombre y comienza a cargar preguntas: por cada una, el tipo (opción múltiple o verdadero/falso), el enunciado y sus opciones de respuesta, marcando cuál es correcta. |
| 4 | El sistema valida que el nombre del pool haya sido completado y que se haya cargado al menos una pregunta. |
| 5 | El sistema valida que cada pregunta tenga al menos dos opciones de respuesta y que exactamente una esté marcada como correcta. |
| 6 | El sistema registra el pool con sus preguntas y opciones. |
| 7 | El sistema informa el éxito del registro. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El pool queda registrado, asociado a la unidad, con sus preguntas y opciones de respuesta.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si el nombre no fue completado o no se cargó ninguna pregunta, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si alguna pregunta tiene menos de dos opciones, o no tiene exactamente una opción marcada como correcta, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — ocurre al preparar las evaluaciones de cada unidad.
- **Estabilidad**: Alta
- **Comentarios**: El pool también puede generarse automáticamente a partir del material de la unidad mediante CU-65: Generar banco de preguntas, como método alternativo a la carga manual.

---

### CU-50: Modificar pool
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente modificar el nombre de un pool y agregar, editar o eliminar sus preguntas y opciones de respuesta.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - El pool existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar (ver CU-48: Buscar pool). |
| 2 | El sistema muestra los datos actuales del pool, con sus preguntas y opciones. |
| 3 | El actor modifica el nombre del pool, o agrega, edita o elimina preguntas y sus opciones. |
| 4 | El sistema valida que el nombre no quede vacío y que el pool conserve al menos una pregunta. |
| 5 | El sistema valida que cada pregunta conserve al menos dos opciones y exactamente una marcada como correcta. |
| 6 | El sistema actualiza el pool. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El pool queda actualizado con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si el nombre queda vacío o el pool queda sin preguntas, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si alguna pregunta queda con menos de dos opciones o sin una única opción correcta, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — se usa para actualizar o ampliar el banco de preguntas de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-51: Eliminar pool
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente o Administrador dar de baja un pool. Si el pool está asociado a alguna autoevaluación activa, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - El pool existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a dar de baja (ver CU-48: Buscar pool). |
| 2 | El sistema verifica que el pool no esté asociado a ninguna autoevaluación activa. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca el pool como dado de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: El pool queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si el pool está asociado a una autoevaluación activa, el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — se usa cuando un pool queda obsoleto.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-52: Buscar autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar las autoevaluaciones registradas en una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - Existe al menos una autoevaluación registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar autoevaluaciones dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre de la autoevaluación. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las autoevaluaciones que coinciden con los criterios. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de autoevaluaciones de la unidad, con su nombre, pools asociados, tiempo límite, intentos permitidos y si integra pools de otras unidades como evaluación final.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar la evaluación de un curso.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-53: Crear autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente crear una autoevaluación para una unidad, asociándose a uno o más pools de preguntas. Si la unidad es la última del programa, la autoevaluación puede integrar también pools de otras unidades, conformando la evaluación final del curso.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - Existe al menos un pool activo en la unidad correspondiente.
    - La unidad existe, no se encuentra en baja y pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea crear una autoevaluación dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita: nombre, tiempo límite, fecha de apertura, fecha de cierre (opcional), cantidad de intentos permitidos (opcional; si se deja vacía, no hay límite), y el o los pools de preguntas a asociar —el de la propia unidad y, si es la última unidad del programa, opcionalmente los de otras unidades, para conformar la evaluación final del curso. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios y que se haya seleccionado al menos un pool. |
| 5 | El sistema valida que el tiempo límite sea un valor entero mayor a cero y, si se indicó cantidad de intentos permitidos, que también lo sea. |
| 6 | El sistema valida que, si se especificó una fecha de cierre, esta sea posterior a la fecha de apertura. |
| 7 | El sistema valida que los pools seleccionados, en conjunto, tengan al menos 10 preguntas activas para poder sortear un intento. |
| 8 | El sistema registra la autoevaluación. |
| 9 | El sistema informa el éxito del registro. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La autoevaluación queda registrada y asociada a los pools seleccionados, con su fecha de apertura y, si corresponde, su fecha de cierre.
    - La fecha de creación refleja el momento del alta.
- **Excepciones**:
    - **Paso 4**: Si no se completaron los campos obligatorios o no se seleccionó ningún pool, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el tiempo límite, o la cantidad de intentos cuando se indicó, no son valores enteros mayores a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la fecha de cierre especificada no es posterior a la fecha de apertura, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si los pools seleccionados no reúnen al menos 10 preguntas activas en conjunto, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — ocurre al estructurar la evaluación de cada unidad, incluyendo la evaluación final en la última unidad del programa.
- **Estabilidad**: Alta
- **Comentarios**: Cada intento de un alumno sortea 10 preguntas de los pools asociados a la autoevaluación (ver CU-57: Realizar intento de autoevaluación).

---

### CU-54: Modificar autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente modificar el nombre, tiempo límite, fecha de apertura, fecha de cierre, cantidad de intentos permitidos y los pools asociados a una autoevaluación.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La autoevaluación existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar (ver CU-52: Buscar autoevaluación). |
| 2 | El sistema muestra los datos actuales de la autoevaluación. |
| 3 | El actor modifica los datos que desea. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios y al menos un pool asociado. |
| 5 | El sistema valida que el tiempo límite sea un valor entero mayor a cero y, si se indicó cantidad de intentos permitidos, que también lo sea. |
| 6 | El sistema valida que, si se especificó una fecha de cierre, esta sea posterior a la fecha de apertura. |
| 7 | El sistema valida que los pools asociados reúnan al menos 10 preguntas activas en conjunto. |
| 8 | El sistema actualiza la autoevaluación. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La autoevaluación queda actualizada con los nuevos datos.
    - La fecha de modificación refleja el momento del cambio.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío o sin pools asociados, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el tiempo límite, o la cantidad de intentos cuando se indicó, no son valores enteros mayores a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si la fecha de cierre especificada no es posterior a la fecha de apertura, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si los pools asociados no reúnen al menos 10 preguntas activas, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para ajustar parámetros de una evaluación ya configurada.
- **Estabilidad**: Alta
- **Comentarios**: Los cambios aplican a los intentos que se realicen a partir de la modificación; no afectan los intentos ya registrados.

---

### CU-55: Eliminar autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente o Administrador dar de baja una autoevaluación. Si algún alumno ya registra un intento sobre ella, el sistema informa la dependencia y no permite la baja.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La autoevaluación existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a dar de baja (ver CU-52: Buscar autoevaluación). |
| 2 | El sistema verifica que ningún alumno registre intentos sobre esa autoevaluación. |
| 3 | El actor confirma la baja. |
| 4 | El sistema marca la autoevaluación como dada de baja. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**: La autoevaluación queda en estado de baja.
- **Excepciones**:
    - **Paso 2**: Si algún alumno ya registra un intento sobre esa autoevaluación, el sistema informa la dependencia y no permite la baja.
    - **Paso 3**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — se usa cuando una evaluación aún no cursada queda obsoleta.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-56: Buscar intento de autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Alumno, Docente
- **Descripción**: Permite consultar el historial de intentos de una autoevaluación. La vista varía según el rol del actor: el Alumno visualiza únicamente sus propios intentos; el Docente visualiza los de los alumnos inscriptos en su curso.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno o Docente.
    - Existe al menos un intento registrado para la autoevaluación.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar intentos dentro de una autoevaluación. |
| 2 | El sistema solicita la autoevaluación sobre la que se desea consultar y, opcionalmente, el alumno (solo para Docente), el rango de fechas y el resultado (aprobado / no aprobado). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista los intentos que coinciden con los criterios, restringidos a los propios si el actor es Alumno. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recupera el historial de intentos de la autoevaluación, con su fecha, nota y resultado (aprobado / no aprobado).
- **Excepciones**: No aplica.
- **Frecuencia**: Media — el alumno la consulta para revisar su desempeño; el docente, para hacer seguimiento del curso.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-57: Realizar intento de autoevaluación
- **Objetivo(s) asociado(s)**: OBJ-04: Evaluar y certificar a los alumnos.
- **Requisito(s) de información asociado(s)**: RI-04: Información sobre evaluaciones.
- **Módulo**: MOD-F-04: Módulo de Evaluaciones
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno realizar un intento de una autoevaluación, respondiendo un cuestionario de 10 preguntas sorteadas de los pools asociados, con corrección automática (ver PA-6: Emisión automática de certificados).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El alumno posee una inscripción vigente al curso.
    - La unidad o el curso al que pertenece la autoevaluación se encuentra habilitado según el avance secuencial del alumno.
    - La fecha y hora actual se encuentra dentro del período habilitado de la autoevaluación (posterior a su fecha de apertura y, si corresponde, anterior a su fecha de cierre).
    - Si la autoevaluación tiene un límite de intentos, el alumno no lo superó para esa autoevaluación.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-21: Ver contenido de unidad), inicia un intento de autoevaluación. |
| 2 | El sistema sortea 10 preguntas cerradas de los pools asociados a la autoevaluación, con sus opciones de respuesta. |
| 3 | El sistema presenta el cuestionario al alumno, dentro del tiempo límite configurado. |
| 4 | El alumno selecciona una opción de respuesta para cada una de las 10 preguntas. |
| 5 | El alumno confirma la entrega del intento. |
| 6 | El sistema valida que se haya respondido a las 10 preguntas. |
| 7 | El sistema corrige automáticamente el intento, comparando la opción elegida por el alumno con la opción correcta de cada pregunta. |
| 8 | El sistema calcula la nota del intento y registra el intento con la fecha actual. |
| 9 | Si el alumno respondió correctamente las 10 preguntas, el sistema aprueba el intento, registra el progreso de la unidad como completada y, si correspondía a la evaluación final del curso, genera los datos del certificado de finalización (número y fecha de emisión), los registra en la inscripción del alumno, y los envía por correo electrónico (ver PA-6: Emisión automática de certificados). |
| 10 | Si el alumno no respondió correctamente las 10 preguntas, el sistema no aprueba el intento e informa que debe reintentar el cuestionario completo. |
| 11 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El intento queda registrado, con la opción elegida por el alumno en cada pregunta sorteada, la nota obtenida y el resultado.
    - Si fue aprobado: el progreso del alumno en la unidad queda registrado como completado, con la fecha de aprobación, y se habilita el acceso a la siguiente unidad del curso; y, si correspondía a la evaluación final, el certificado queda registrado en la inscripción del alumno, y enviado por correo electrónico.
- **Excepciones**:
    - **Paso 6**: Si se agota el tiempo límite sin que el alumno haya respondido las 10 preguntas, el sistema cierra automáticamente el intento con las respuestas dadas hasta ese momento y lo registra como no aprobado.
- **Frecuencia**: Alta — se repite en cada unidad de cada curso cursado por cada alumno.
- **Estabilidad**: Alta
- **Comentarios**:
    - La aprobación exige responder correctamente la totalidad de las 10 preguntas del intento, según lo confirmado por el cliente; no existe una nota de corte parcial.
    - Si la autoevaluación tiene un límite de intentos y el alumno los agota sin aprobar, queda bloqueado para continuar el curso hasta que el Docente amplíe sus intentos mediante CU-54: Modificar autoevaluación.

---

## MOD-F-05: Módulo de Clases en Vivo

### CU-58: Buscar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar las clases en vivo programadas para una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - Existe al menos una clase en vivo registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar clases en vivo dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el título, el docente, el rango de fechas y el estado (Programada / En vivo / Finalizada). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las clases en vivo que coinciden con los criterios ingresados. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recuperan una o más clases en vivo, con su título, fecha y hora, docente y estado.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta para el seguimiento del cronograma de clases en vivo.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-59: Programar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular o supervisor de un curso programar una clase en vivo para una unidad, definiendo su fecha y hora de transmisión.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad existe, pertenece a un curso en el que el docente participa como titular o supervisor, y al programa vigente de dicho curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea programar una clase en vivo dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita: título, fecha y hora de la clase. |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios. |
| 5 | El sistema valida que la fecha y hora ingresadas sean posteriores al momento actual. |
| 6 | El sistema registra la clase en estado Programada. |
| 7 | El sistema notifica a los alumnos inscriptos en el curso la fecha de la clase. |
| 8 | El sistema informa el éxito del registro. |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase en vivo queda registrada en estado Programada, asociada a la unidad y al docente.
    - Los alumnos inscriptos reciben la notificación.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si la fecha y hora ingresadas no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Media — ocurre al planificar el cronograma de clases en vivo de un curso.
- **Estabilidad**: Alta
- **Comentarios**: La configuración e instalación del software OBS para cada docente está a cargo del equipo de sistemas de la empresa, previo a la transmisión, y queda fuera del alcance funcional de este caso de uso.

---

### CU-60: Modificar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente modificar el título, la fecha o la hora de una clase en vivo, siempre que todavía no haya sido transmitida.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a modificar (ver CU-58: Buscar clase en vivo). |
| 2 | El sistema muestra los datos actuales de la clase. |
| 3 | El actor modifica el título, la fecha o la hora. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios y que la fecha y hora sean posteriores al momento actual. |
| 5 | El sistema actualiza los datos de la clase. |
| 6 | El sistema notifica a los alumnos inscriptos el cambio de fecha u horario, si corresponde. |
| 7 | El sistema informa el éxito de la modificación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase en vivo queda actualizada con los nuevos datos.
    - Los alumnos inscriptos reciben la notificación del cambio, si corresponde.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, o la fecha y hora no son posteriores al momento actual, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para reprogramar una clase antes de su transmisión.
- **Estabilidad**: Alta
- **Comentarios**: No es posible modificar una clase que ya está En vivo o Finalizada.

---

### CU-61: Cancelar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente o Administrador cancelar una clase en vivo programada que todavía no fue transmitida.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase en vivo programada a cancelar (ver CU-58: Buscar clase en vivo). |
| 2 | El actor confirma la cancelación. |
| 3 | El sistema marca la clase como dada de baja. |
| 4 | El sistema notifica a los alumnos inscriptos la cancelación de la clase. |
| 5 | El sistema informa el éxito de la operación. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase en vivo queda dada de baja.
    - Los alumnos inscriptos reciben la notificación de la cancelación.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la cancelación, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — ocurre cuando un docente no puede sostener una clase ya programada.
- **Estabilidad**: Alta
- **Comentarios**: No es posible cancelar una clase que ya está En vivo o Finalizada.

---

### CU-62: Iniciar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente iniciar la transmisión de una clase en vivo programada, generando los datos de conexión que utilizará desde OBS (ver PA-4: Clases en Vivo).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
    - Se alcanzó el horario programado para la clase.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase en vivo programada (ver CU-58: Buscar clase en vivo) y, en el horario programado, solicita iniciarla. |
| 2 | El sistema genera los datos de conexión de la transmisión (URL de streaming y clave privada de transmisión). |
| 3 | El sistema pasa la clase al estado En vivo. |
| 4 | El docente carga los datos de conexión en OBS y comienza a transmitir. |
| 5 | El sistema recibe la señal transmitida y la redistribuye en simultáneo a los alumnos inscriptos que ingresan a la clase (ver CU-64: Ingresar a clase en vivo), mientras graba automáticamente la transmisión mediante el protocolo RTMP desde OBS. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase en vivo queda en estado En vivo, con sus datos de conexión generados.
    - La transmisión queda disponible para los alumnos inscriptos y se graba automáticamente.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — ocurre por cada clase en vivo programada que efectivamente se dicta.
- **Estabilidad**: Alta
- **Comentarios**: La clave de transmisión es privada del docente, de forma que solo él pueda transmitir con los datos de conexión generados para esa clase.

---

### CU-63: Finalizar clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente finalizar la transmisión de una clase en vivo, dando de baja la señal en OBS de forma remota y generando la grabación resultante como material de la unidad (ver PA-4: Clases en Vivo).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La clase en vivo existe, fue registrada por el actor y se encuentra en estado En vivo.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase en vivo que se encuentra en curso (ver CU-58: Buscar clase en vivo) y solicita finalizar la transmisión. |
| 2 | El sistema envía la orden de corte de transmisión y grabación al OBS del docente. |
| 3 | El sistema pasa la clase al estado Finalizada. |
| 4 | El sistema genera la grabación resultante de la transmisión. |
| 5 | El sistema carga la grabación como material de tipo Grabación de la unidad correspondiente, en estado publicado. |
| 6 | El sistema notifica a los alumnos inscriptos que la grabación ya está disponible. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase en vivo queda en estado Finalizada.
    - La grabación queda cargada como material publicado de la unidad.
    - Los alumnos inscriptos reciben la notificación de disponibilidad de la grabación.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — ocurre por cada clase en vivo que se transmite.
- **Estabilidad**: Alta
- **Comentarios**: La grabación resultante queda disponible por un plazo configurable (cuatro meses por defecto), con aviso previo al alumno antes de su vencimiento y eliminación automática al cumplirse el plazo.

---

### CU-64: Ingresar a clase en vivo
- **Objetivo(s) asociado(s)**: OBJ-05: Gestionar las clases en vivo.
- **Requisito(s) de información asociado(s)**: RI-05: Información sobre clases en vivo.
- **Módulo**: MOD-F-05: Módulo de Clases en Vivo
- **Actor(es)**: Alumno
- **Descripción**: Permite al Alumno ingresar a la transmisión de una clase en vivo mientras se encuentra en curso.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Alumno.
    - El alumno posee una inscripción vigente al curso de la unidad.
    - La clase en vivo se encuentra en estado En vivo.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-21: Ver contenido de unidad), selecciona el acceso a la clase en vivo. |
| 2 | El sistema verifica que la clase se encuentre en estado En vivo y que el alumno posea inscripción vigente al curso. |
| 3 | El sistema conecta al alumno a la transmisión en curso. |
| 4 | Fin del caso de uso. |

- **Salida**: El alumno queda conectado a la transmisión en vivo de la clase.
- **Excepciones**:
    - **Paso 2**: Si la clase todavía no comenzó o ya finalizó, el sistema informa que la transmisión no está disponible en este momento.
- **Frecuencia**: Alta — ocurre por cada alumno que asiste a una clase en vivo.
- **Estabilidad**: Alta
- **Comentarios**: El alumno que no pudo asistir en vivo puede acceder posteriormente a la grabación mediante CU-21: Ver contenido de unidad, una vez finalizada la clase.

---

## MOD-F-06: Módulo de Generación de Contenido con IA

### CU-65: Generar banco de preguntas
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente generar automáticamente un pool de preguntas para una unidad, a partir de la bibliografía y el glosario cargados, mediante un modelo de inteligencia artificial ejecutado localmente (Ollama) (ver PA-9: Generación de banco de preguntas).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad posee al menos un material de tipo Bibliografía o un término de glosario cargado.
    - La unidad pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar un banco de preguntas dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita, opcionalmente, un guión adicional ingresado como prompt de texto para orientar la generación. |
| 3 | El actor confirma la generación, con o sin el guión adicional. |
| 4 | El sistema envía la bibliografía, el glosario de la unidad y el guión, si fue ingresado, al modelo de inteligencia artificial local. |
| 5 | El modelo de inteligencia artificial genera un banco de preguntas cerradas de opción múltiple y verdadero/falso, siguiendo la proporción configurada. |
| 6 | El sistema recibe el banco de preguntas generado y valida que cada pregunta tenga al menos dos opciones y exactamente una marcada como correcta. |
| 7 | El sistema registra el pool generado, asociado a la unidad. |
| 8 | El sistema notifica al docente que el pool está disponible para su revisión antes de publicarse. |
| 9 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El pool generado queda registrado, asociado a la unidad.
    - El docente recibe la notificación para revisar el pool antes de utilizarlo en una autoevaluación.
- **Excepciones**:
    - **Paso 6**: Si el modelo de inteligencia artificial devuelve un banco de preguntas con un formato inválido, el sistema descarta el resultado, informa el error al docente y le permite reintentar.
- **Frecuencia**: Media — se usa como alternativa a la carga manual de preguntas (CU-49: Crear pool).
- **Estabilidad**: Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**: El pool queda registrado sin publicar como pool de referencia hasta que el docente lo revise; su uso efectivo en una autoevaluación se define mediante CU-53: Crear autoevaluación.

---

### CU-66: Generar resumen de unidad
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente generar automáticamente un resumen del contenido de una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local (ver PA-8: Generación de resúmenes de unidad).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad posee al menos un material de tipo Bibliografía cargado.
    - La unidad pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar un resumen dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El actor confirma la generación. |
| 3 | El sistema envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | El modelo de inteligencia artificial genera un resumen estructurado del contenido. |
| 5 | El sistema recibe el resumen y lo registra como material de tipo Resumen de la unidad, en estado no publicado. |
| 6 | El sistema notifica al docente que el resumen está disponible para su revisión antes de publicarlo. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El resumen queda registrado como material de la unidad, sin publicar.
    - El docente recibe la notificación para revisarlo.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se usa como alternativa a la redacción manual de un resumen.
- **Estabilidad**: Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**: El docente publica el resumen generado mediante CU-24: Modificar material, una vez revisado su contenido.

---

### CU-67: Generar presentación de unidad
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente generar automáticamente una presentación descargable para una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local (ver PA-7: Generación de presentaciones de unidad).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La unidad posee al menos un material de tipo Bibliografía cargado.
    - La unidad pertenece al programa vigente del curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar una presentación dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El actor confirma la generación. |
| 3 | El sistema envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | El modelo de inteligencia artificial devuelve una estructura de contenidos (títulos, subtítulos y puntos clave). |
| 5 | El sistema da formato a la estructura recibida como una presentación descargable y la registra como material de tipo Presentación de la unidad, en estado no publicado. |
| 6 | El sistema notifica al docente que la presentación está disponible para su revisión antes de publicarla. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La presentación queda registrada como material de la unidad, sin publicar.
    - El docente recibe la notificación para revisarla.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se usa como alternativa a la carga manual de una presentación.
- **Estabilidad**: Media — depende de la disponibilidad y el desempeño del modelo de inteligencia artificial local.
- **Comentarios**: El docente publica la presentación generada mediante CU-24: Modificar material, una vez revisado su contenido, igual que en CU-66: Generar resumen de unidad.

---

### CU-68: Buscar clase con Clon IA
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o al Administrador buscar las clases generadas mediante Clon de inteligencia artificial en una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - Existe al menos una clase con Clon de IA registrada.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar clases con Clon de IA dentro de una unidad. |
| 2 | El sistema solicita la unidad sobre la que se desea consultar y, opcionalmente, el título y el estado (Pendiente / Generada / Error). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y lista las clases con Clon de IA que coinciden con los criterios. |
| 5 | Fin del caso de uso. |

- **Salida**: Se recuperan una o más clases con Clon de IA, con su título, guión, estado y fecha de generación.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se consulta al gestionar las clases con Clon de IA de una unidad.
- **Estabilidad**: Alta
- **Comentarios**: El guión se conserva para poder reutilizarlo si la clase se modifica y se regenera (ver CU-70: Modificar clase con Clon IA).

---

### CU-69: Generar clase con Clon IA
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular o supervisor de un curso generar una clase para una unidad mediante un Clon de inteligencia artificial, a partir de un guión que redacta como prompt, integrando con la plataforma HeyGen (ver PA-5: Generación de videos Clon IA).
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - El docente se encuentra habilitado para dictar clases.
    - La unidad existe, pertenece a un curso en el que el docente participa como titular o supervisor, y al programa vigente de dicho curso.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente desea generar una clase con Clon de IA dentro de una unidad (ver CU-14: Buscar unidad). |
| 2 | El sistema solicita el título de la clase y el guión, ingresado como un prompt de texto. |
| 3 | El actor ingresa el título y redacta el guión. |
| 4 | El sistema valida que el título y el guión hayan sido completados. |
| 5 | El sistema registra la clase en estado Pendiente. |
| 6 | El sistema envía el guión, junto con el Avatar ya validado del docente, a HeyGen. |
| 7 | HeyGen genera el video de la clase a partir del guión y el Avatar del docente. |
| 8 | El sistema descarga el video generado y actualiza el estado de la clase a Generada. |
| 9 | El sistema carga el video como material de tipo Grabación de la unidad correspondiente, en estado no publicado. |
| 10 | El sistema notifica al docente que el material está disponible para su revisión antes de publicarlo. |
| 11 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase con Clon de IA queda registrada, en estado Generada.
    - El video generado queda cargado como material de la unidad, sin publicar.
    - El docente recibe la notificación para revisar el material.
- **Excepciones**:
    - **Paso 4**: Si el título o el guión no fueron completados, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente.
- **Frecuencia**: Media — ocurre según la disponibilidad horaria de cada docente de alto nivel académico.
- **Estabilidad**: Media — depende de las capacidades y condiciones de integración de HeyGen.
- **Comentarios**: –

---

### CU-70: Modificar clase con Clon IA
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente
- **Descripción**: Permite al Docente titular o supervisor modificar el título y/o el guión de una clase con Clon de inteligencia artificial de una unidad, regenerando el video mediante HeyGen si el guión fue modificado.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente.
    - La clase con Clon de IA existe, no se encuentra en baja, y pertenece a una unidad de un curso en el que el docente participa como titular o supervisor.
    - La clase se encuentra en estado Generada o Error.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase con Clon de IA a modificar (ver CU-68: Buscar clase con Clon IA). |
| 2 | El sistema muestra el título y el guión actuales de la clase. |
| 3 | El actor modifica el título y/o el guión. |
| 4 | El sistema valida que el título y el guión se mantengan completos. |
| 5 | Si el guión fue modificado, el sistema actualiza el estado de la clase a Pendiente y envía el nuevo guión, junto con el Avatar del docente, a HeyGen. |
| 6 | HeyGen genera el nuevo video de la clase a partir del guión actualizado. |
| 7 | El sistema descarga el video generado, actualiza el estado de la clase a Generada y reemplaza el material de tipo Grabación de la unidad, en estado no publicado. |
| 8 | El sistema actualiza la clase con los nuevos datos. |
| 9 | El sistema informa el éxito de la modificación. |
| 10 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La clase con Clon de IA queda actualizada con el nuevo título y/o guión.
    - Si el guión fue modificado, el video queda regenerado y el material de la unidad, reemplazado sin publicar.
- **Excepciones**:
    - **Paso 4**: Si el título o el guión quedan vacíos, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente.
- **Frecuencia**: Baja — se usa para corregir o mejorar el contenido de una clase ya generada.
- **Estabilidad**: Media — depende de las capacidades y condiciones de integración de HeyGen.
- **Comentarios**: Si solo se modifica el título, no se dispara una nueva generación en HeyGen.

---

### CU-71: Eliminar clase con Clon IA
- **Objetivo(s) asociado(s)**: OBJ-06: Generar contenido académico con inteligencia artificial.
- **Requisito(s) de información asociado(s)**: RI-06: Información sobre generación de contenido con inteligencia artificial.
- **Módulo**: MOD-F-06: Módulo de Generación de Contenido con IA
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente titular/supervisor o Administrador dar de baja una clase con Clon de inteligencia artificial de una unidad.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
    - La clase con Clon de IA existe, no se encuentra en baja, y pertenece a una unidad de un curso en el que el docente participa como titular o supervisor.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase con Clon de IA a dar de baja (ver CU-68: Buscar clase con Clon IA). |
| 2 | El actor confirma la baja. |
| 3 | El sistema marca la clase y su material asociado como dados de baja. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: La clase con Clon de IA y su material asociado quedan dados de baja.
- **Excepciones**:
    - **Paso 2**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Baja — se usa cuando una clase generada queda obsoleta.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-NF-01: Módulo de Usuarios y Notificaciones

### CU-72: Registrarse
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno
- **Descripción**: Permite a un interesado, todavía sin cuenta, crear su propia cuenta de Alumno en la plataforma, mediante correo electrónico y contraseña, validando la cuenta a través de un enlace enviado por email.
- **Precondición(es)**: –
- **Flujo de eventos**:

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

- **Postcondición(es)**:
    - La cuenta queda registrada con rol Alumno.
    - La cuenta queda validada una vez que el actor accede al enlace enviado por correo.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el correo electrónico ya está registrado, el sistema informa el error y sugiere iniciar sesión o recuperar la contraseña.
- **Frecuencia**: Alta — ocurre por cada nuevo alumno que se suma a la plataforma.
- **Estabilidad**: Alta
- **Comentarios**: Es la vía de autoregistro con correo y contraseña; el alta mediante Google OAuth se resuelve automáticamente en CU-81: Iniciar sesión (ver PA-1: Login con Google).

---

### CU-73: Buscar usuario
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador buscar los usuarios registrados en el sistema, con fines de gestión.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - Existe al menos un usuario registrado.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita buscar uno o más usuarios. |
| 2 | El sistema solicita los criterios de búsqueda: nombre, apellido, correo electrónico, DNI y rol (Alumno / Docente / Administrador). |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los usuarios que coincidan con los criterios ingresados. |
| 5 | El sistema lista los usuarios filtrados. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recuperan uno o más usuarios que cumplen con los criterios de búsqueda, con su rol y estado.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta frecuentemente para la gestión de cuentas.
- **Estabilidad**: Alta
- **Comentarios**: Para los usuarios con rol Docente, este CU también permite verificar su estado de habilitación para dictar clases.

---

### CU-74: Registrar usuario
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar manualmente la cuenta de un Alumno o de otro Administrador, para los casos en que el alta no ocurre por autoregistro (por ejemplo, la inscripción corporativa de una empresa para sus empleados).
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita registrar manualmente un usuario. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI y el rol a asignar (Alumno o Administrador). |
| 3 | El actor ingresa los datos solicitados. |
| 4 | El sistema valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 5 | El sistema registra la cuenta con el rol indicado y envía al correo ingresado un enlace para que el usuario defina su contraseña. |
| 6 | El sistema informa el éxito del registro. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**: La cuenta queda registrada con el rol indicado.
- **Excepciones**:
    - **Paso 4**: Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa para altas manuales excepcionales; el alta habitual de un Alumno ocurre por autoregistro (CU-72) y la de un Docente por CU-79: Registrar docente.
- **Estabilidad**: Alta
- **Comentarios**: El Administrador puede crear cuentas de otros Administradores, pero no puede modificarlas.

---

### CU-75: Modificar usuario
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar los datos base de la cuenta de un Alumno.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El usuario existe, posee rol Alumno y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta del alumno a modificar (ver CU-73: Buscar usuario). |
| 2 | El sistema muestra los datos actuales de la cuenta. |
| 3 | El actor modifica el nombre, apellido, correo electrónico, DNI, teléfono o imagen de perfil. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios y que el correo electrónico, si fue modificado, no esté ya registrado por otra cuenta. |
| 5 | El sistema actualiza los datos de la cuenta. |
| 6 | El sistema informa el éxito de la modificación. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**: La cuenta del alumno queda actualizada con los nuevos datos.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el correo electrónico ya está registrado por otra cuenta, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa ante un reclamo o corrección de datos solicitada por el alumno.
- **Estabilidad**: Alta
- **Comentarios**: No incluye el cambio de contraseña, que se gestiona exclusivamente mediante CU-83: Recuperar contraseña. Los datos propios del Docente (información profesional) se gestionan mediante CU-80: Modificar docente, no con este CU.

---

### CU-76: Dar de baja usuario
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de baja la cuenta de un usuario, quitándole el acceso al sistema.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El usuario existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta del usuario a dar de baja (ver CU-73: Buscar usuario). |
| 2 | Si el usuario posee rol Administrador, el sistema valida que existan otros administradores activos en el sistema además de él. |
| 3 | Si el usuario posee rol Docente, el sistema verifica que no sea titular ni supervisor de ningún dictado vigente. |
| 4 | Si el usuario posee rol Alumno y posee inscripciones vigentes, el sistema advierte que la baja le hará perder el acceso al contenido de esos cursos, sin derecho a reembolso. |
| 5 | El actor confirma la baja. |
| 6 | El sistema marca la cuenta como dada de baja y cierra sus sesiones activas. |
| 7 | El sistema informa el éxito de la operación. |
| 8 | Fin del caso de uso. |

- **Postcondición(es)**:
    - La cuenta queda en estado de baja y pierde acceso al sistema.
    - Las sesiones activas del usuario quedan cerradas.
- **Excepciones**:
    - **Paso 2**: Si el usuario posee rol Administrador y es el único administrador activo del sistema, el sistema informa que no puede quedar sin administradores y no permite la baja.
    - **Paso 3**: Si el docente es titular o supervisor de al menos un dictado vigente, el sistema informa la dependencia y no permite la baja hasta que se lo reemplace como titular o supervisor en ese dictado o se dé de baja el dictado.
    - **Paso 5**: Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso.
- **Frecuencia**: Muy baja — ocurre ante un cierre de cuenta definitivo.
- **Estabilidad**: Alta
- **Comentarios**: El sistema debe garantizar en todo momento la existencia de al menos un Administrador activo, para evitar que el sistema quede sin gestión posible.

---

### CU-77: Ver perfil
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a cualquier usuario autenticado consultar los datos de su propia cuenta.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver su perfil. |
| 2 | El sistema recupera los datos de la cuenta del actor. |
| 3 | El sistema muestra los datos al actor. |
| 4 | Fin del caso de uso. |

- **Salida**: Se recuperan los datos de la cuenta del actor: nombre, apellido, correo electrónico, DNI, teléfono e imagen de perfil, y los datos profesionales adicionales si el actor es Docente.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta cada vez que un usuario accede a su perfil.
- **Estabilidad**: Alta
- **Comentarios**: Si el actor es Docente, también se muestran sus datos profesionales (biografía, años de experiencia, títulos, matrícula y estado de habilitación), aunque solo el Administrador puede modificarlos mediante CU-80: Modificar docente.

---

### CU-78: Editar perfil
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a cualquier usuario autenticado editar los datos base de su propia cuenta.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita editar su perfil. |
| 2 | El sistema muestra los datos actuales de la cuenta del actor. |
| 3 | El actor modifica el nombre, apellido, teléfono o imagen de perfil. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios. |
| 5 | El sistema actualiza los datos de la cuenta. |
| 6 | El sistema informa el éxito de la modificación. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**: La cuenta del actor queda actualizada con los nuevos datos.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa cuando el usuario decide actualizar sus datos de contacto o su foto de perfil.
- **Estabilidad**: Alta
- **Comentarios**: No incluye el correo electrónico ni el cambio de contraseña, que se gestionan mediante CU-83: Recuperar contraseña. Si el actor es Docente, sus datos profesionales no se editan aquí; quedan a cargo del Administrador mediante CU-80: Modificar docente.

---

### CU-79: Registrar docente
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador registrar manualmente la cuenta de un nuevo docente, verificando previamente sus credenciales académicas o profesionales. El alta de un docente no admite autoregistro.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

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

- **Postcondición(es)**: La cuenta del docente queda registrada, habilitada y con su información profesional cargada.
- **Excepciones**:
    - **Paso 5**: Si no se completó alguno de los campos obligatorios, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si el correo electrónico ya está registrado, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si no se declaró ningún título ni ninguna matrícula profesional, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — ocurre cada vez que se incorpora un nuevo docente de élite a la plataforma.
- **Estabilidad**: Alta
- **Comentarios**: La verificación del título o la matrícula es un control manual y externo que realiza el Administrador antes de cargar los datos; el sistema no modela un flujo de estados de verificación por credencial.

---

### CU-80: Modificar docente
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador modificar la información profesional de un docente y habilitarlo o suspenderlo temporalmente para dictar clases, sin eliminar su cuenta ni su historial.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - El docente existe y no se encuentra en baja.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona el docente a modificar (ver CU-73: Buscar usuario). |
| 2 | El sistema muestra los datos profesionales actuales del docente. |
| 3 | El actor modifica la biografía, los años de experiencia, los títulos, la matrícula, o el estado de habilitación para dictar clases. |
| 4 | El sistema valida que se mantengan completos los campos obligatorios. |
| 5 | El sistema valida que se mantenga declarado al menos un título universitario o de posgrado, o al menos una matrícula profesional (colegio o Comisión Nacional de Valores). |
| 6 | Si se modificaron los años de experiencia, el sistema valida que sean un número entero mayor o igual a cero. |
| 7 | Si el actor intenta suspender la habilitación del docente, el sistema verifica que no sea titular ni supervisor de ningún dictado vigente. |
| 8 | El sistema actualiza los datos del docente. |
| 9 | Si el actor suspendió la habilitación del docente, el sistema le notifica el cambio de estado. |
| 10 | El sistema informa el éxito de la modificación. |
| 11 | Fin del caso de uso. |

- **Postcondición(es)**:
    - Los datos profesionales del docente quedan actualizados.
    - Si se modificó su estado de habilitación, el docente queda habilitado o suspendido para dictar clases, según corresponda.
- **Excepciones**:
    - **Paso 4**: Si algún campo obligatorio queda vacío, el sistema informa el error y vuelve al paso 3.
    - **Paso 5**: Si la modificación deja al docente sin ningún título ni matrícula profesional declarados, el sistema informa el error y vuelve al paso 3.
    - **Paso 6**: Si los años de experiencia ingresados no son un número entero mayor o igual a cero, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si el docente que se intenta suspender es titular o supervisor de al menos un dictado vigente, el sistema informa la dependencia y no permite la suspensión hasta que se lo reemplace en ese dictado.
- **Frecuencia**: Baja — se usa para actualizar credenciales o ante una suspensión, por ejemplo por mala praxis.
- **Estabilidad**: Alta
- **Comentarios**: La suspensión no elimina la cuenta del docente ni su historial; solo le impide participar en nuevos dictados o dictar nuevas clases mientras permanezca suspendido.

---

### CU-81: Iniciar sesión
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a un usuario iniciar sesión en el sistema mediante correo electrónico y contraseña, o mediante Google OAuth como método alternativo (ver PA-1: Login con Google).
- **Precondición(es)**: El actor posee una cuenta registrada y no se encuentra en baja.
- **Flujo de eventos**:

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

- **Postcondición(es)**: La sesión queda registrada y activa.
- **Excepciones**:
    - **Paso 4**: Si las credenciales ingresadas son incorrectas, el sistema informa el error y vuelve al paso 3.
    - **Paso 4**: Si el correo electrónico todavía no fue validado, el sistema informa que debe validarlo antes de iniciar sesión.
    - **Paso 5**: Si el usuario ya alcanzó el límite de sesiones concurrentes permitidas, el sistema informa el error y le solicita cerrar una sesión activa antes de continuar.
- **Frecuencia**: Muy alta — ocurre en cada acceso de cada usuario al sistema.
- **Estabilidad**: Alta
- **Comentarios**: El límite de sesiones concurrentes busca mitigar el uso compartido de credenciales.

---

### CU-82: Cerrar sesión
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a un usuario cerrar su propia sesión activa en el sistema.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita cerrar su sesión. |
| 2 | El sistema registra la fecha de fin de la sesión activa. |
| 3 | El sistema redirige al actor a la pantalla de inicio de sesión. |
| 4 | Fin del caso de uso. |

- **Postcondición(es)**: La sesión queda cerrada.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — ocurre en cada cierre de sesión de cada usuario.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-83: Recuperar contraseña
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a un usuario restablecer su contraseña cuando la olvidó, mediante un token temporal enviado a su correo electrónico. Es la única vía por la que se modifica la contraseña de una cuenta.
- **Precondición(es)**: El actor posee una cuenta registrada con contraseña propia y no se encuentra en baja.
- **Flujo de eventos**:

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

- **Postcondición(es)**:
    - La contraseña de la cuenta queda actualizada.
    - El actor puede iniciar sesión con la nueva contraseña.
- **Excepciones**:
    - **Paso 4**: Si el correo ingresado no está registrado, el sistema informa el error y vuelve al paso 3.
    - **Paso 7**: Si el token de recuperación expiró, el sistema informa el error y le solicita generar uno nuevo, volviendo al paso 2.
- **Frecuencia**: Media — ocurre cada vez que un usuario olvida su contraseña.
- **Estabilidad**: Alta
- **Comentarios**: No aplica a las cuentas que se autentican exclusivamente mediante Google OAuth, ya que no poseen contraseña propia en el sistema.

---

### CU-84: Buscar sesión
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a un usuario consultar sus propias sesiones activas. El Administrador puede además consultar las sesiones activas de cualquier usuario.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita ver las sesiones activas. |
| 2 | El sistema recupera y lista las sesiones activas del actor, o del usuario indicado si el actor es Administrador. |
| 3 | Fin del caso de uso. |

- **Salida**: Se recupera el listado de sesiones activas, con su fecha de inicio, IP y dispositivo.
- **Excepciones**: No aplica.
- **Frecuencia**: Baja — se consulta cuando el usuario quiere revisar desde dónde tiene acceso activo a su cuenta.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-85: Eliminar sesión
- **Objetivo(s) asociado(s)**: OBJ-07: Gestionar usuarios, autenticación y notificaciones.
- **Requisito(s) de información asociado(s)**: RI-07: Información sobre usuarios y notificaciones.
- **Módulo**: MOD-NF-01: Módulo de Usuarios y Notificaciones
- **Actor(es)**: Alumno, Docente, Administrador
- **Descripción**: Permite a un usuario cerrar de forma forzada una sesión activa propia distinta de la actual. El Administrador puede además cerrar una sesión activa de cualquier usuario.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema.
    - La sesión a cerrar existe y se encuentra activa.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor busca y selecciona la sesión activa a cerrar (ver CU-84: Buscar sesión). |
| 2 | El actor confirma el cierre. |
| 3 | El sistema registra la fecha de fin de esa sesión. |
| 4 | El sistema informa el éxito de la operación. |
| 5 | Fin del caso de uso. |

- **Postcondición(es)**: La sesión seleccionada queda cerrada.
- **Excepciones**: No aplica.
- **Frecuencia**: Baja — se usa ante la sospecha de uso compartido de credenciales o para liberar un cupo de sesión concurrente.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-NF-02: Módulo de Auditoría

### CU-86: Consultar auditoría
- **Objetivo(s) asociado(s)**: OBJ-08: Registrar las acciones críticas del sistema.
- **Requisito(s) de información asociado(s)**: RI-08: Información sobre auditoría.
- **Módulo**: MOD-NF-02: Módulo de Auditoría
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador consultar el registro de auditoría de las acciones críticas del sistema (pagos, altas de curso y cambios de estado de inscripción), para garantizar trazabilidad sobre las operaciones.
- **Precondición(es)**:
    - El actor ha iniciado sesión en el sistema con el rol Administrador.
    - Existe al menos un registro de auditoría.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita consultar el registro de auditoría. |
| 2 | El sistema solicita los criterios de búsqueda: usuario responsable, tipo de acción (Crear / Modificar / Eliminar / Consultar), entidad afectada y rango de fecha. |
| 3 | El actor ingresa los criterios de búsqueda que desea. |
| 4 | El sistema recupera y filtra los registros de auditoría que coincidan con los criterios ingresados. |
| 5 | El sistema lista los registros filtrados. |
| 6 | Fin del caso de uso. |

- **Salida**: Se recuperan uno o más registros de auditoría, con el usuario responsable, el tipo de acción, la entidad afectada, el identificador del registro puntual, el valor anterior y el valor nuevo del dato modificado (cuando corresponda), la dirección IP desde la que se realizó la acción y la fecha y hora exacta.
- **Excepciones**: No aplica.
- **Frecuencia**: Baja — se consulta ante la necesidad puntual de rastrear una operación, en especial sobre pagos.
- **Estabilidad**: Alta
- **Comentarios**: El alta de los registros de auditoría es automática, generada por el propio sistema ante cada operación crítica; no existe un caso de uso de registro manual. La consulta está restringida al Administrador.

---

## MOD-NF-03: Módulo de Reportes y Estadísticas

### CU-87: Generar informe de alumnos de un curso
- **Objetivo(s) asociado(s)**: OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**: RI-09: Información sobre reportes y estadísticas.
- **Módulo**: MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador generar un informe de alumnos de un curso, con 3 vistas: comparación del curso frente al resto en cantidad de inscriptos (barras horizontales), evolución de sus inscripciones en el tiempo (línea), y estado de las inscripciones; completadas, vigentes, dadas de baja (barras apiladas). Le sirve a la empresa para saber cómo le va a un curso frente al resto, si sus inscripciones vienen subiendo o bajando, y qué tan bien retiene a los alumnos que se anotan.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita generar un informe de alumnos de un curso. |
| 2 | El sistema solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | El actor ingresa los criterios solicitados. |
| 4 | El sistema recopila los datos de alumnos inscriptos al curso seleccionado, junto con los del resto de los cursos para la comparación, y genera el informe. |
| 5 | El sistema registra el reporte generado, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6 | El sistema pone el informe a disposición del actor para su descarga. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El reporte queda registrado en el historial de reportes generados.
    - El informe de alumnos del curso seleccionado queda disponible para su descarga.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se genera periódicamente para el seguimiento comercial y académico.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-88: Generar informe de ingresos de un curso
- **Objetivo(s) asociado(s)**: OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**: RI-09: Información sobre reportes y estadísticas.
- **Módulo**: MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador generar un informe de ingresos de un curso, con 4 vistas: comparación del curso frente al resto en ingresos por pagos acreditados (barras horizontales), evolución de sus ingresos en el tiempo (línea), ingresos por categoría de curso (torta), y monto bruto frente a monto neto por descuentos aplicados (barras comparativas). Le sirve a la empresa para saber si el curso es uno de los que genera más ingresos, si su facturación viene creciendo, si el rubro del curso es uno de los más rentables, y cuánto le está costando en la práctica la política de descuentos.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita generar un informe de ingresos de un curso. |
| 2 | El sistema solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | El actor ingresa los criterios solicitados. |
| 4 | El sistema recopila los pagos acreditados del curso seleccionado, junto con los del resto de los cursos para las comparaciones, y genera el informe. |
| 5 | El sistema registra el reporte generado, con el tipo de reporte, la fecha y el usuario que lo generó. |
| 6 | El sistema pone el informe a disposición del actor para su descarga. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**:
    - El reporte queda registrado en el historial de reportes generados.
    - El informe de ingresos del curso seleccionado queda disponible para su descarga.
- **Excepciones**: No aplica.
- **Frecuencia**: Media — se genera periódicamente para el control financiero del proyecto.
- **Estabilidad**: Alta
- **Comentarios**: –

---

### CU-89: Consultar estadísticas
- **Objetivo(s) asociado(s)**: OBJ-09: Generar reportes y estadísticas de gestión.
- **Requisito(s) de información asociado(s)**: RI-09: Información sobre reportes y estadísticas.
- **Módulo**: MOD-NF-03: Módulo de Reportes y Estadísticas
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador consultar en pantalla los indicadores del sistema (alumnos inscriptos e ingresos), sin necesidad de generar un reporte descargable.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor accede al panel de estadísticas. |
| 2 | El sistema recupera y muestra en pantalla los indicadores de alumnos inscriptos e ingresos. |
| 3 | Fin del caso de uso. |

- **Salida**: Se muestran en pantalla los indicadores de: alumnos activos; cantidad total de inscripciones vigentes al momento, ingresos del mes, con variación respecto al mes anterior, inscripciones de los últimos 30 días (línea) y ranking (top 5) de los cinco cursos con más inscriptos (barras horizontales). Le sirve a la empresa para tener un pantallazo rápido de cómo viene el negocio sin esperar a generar un informe.
- **Excepciones**: No aplica.
- **Frecuencia**: Alta — se consulta habitualmente como panel de control del negocio.
- **Estabilidad**: Alta
- **Comentarios**: –

---

## MOD-NF-04: Módulo de Configuración

### CU-90: Configurar parámetros
- **Objetivo(s) asociado(s)**: OBJ-10: Permitir la configuración de los parámetros operativos.
- **Requisito(s) de información asociado(s)**: RI-10: Información sobre configuración.
- **Módulo**: MOD-NF-04: Módulo de Configuración
- **Actor(es)**: Administrador
- **Descripción**: Permite al Administrador dar de alta, modificar, dar de baja y consultar los parámetros operativos del sistema mediante un esquema de clave-valor, sin requerir intervención técnica sobre el código.
- **Precondición(es)**: El actor ha iniciado sesión en el sistema con el rol Administrador.
- **Flujo de eventos**:

| Paso | Acción |
| :--- | :--- |
| 1 | El caso de uso inicia cuando el actor solicita gestionar los parámetros de configuración. |
| 2 | El sistema lista los parámetros configurados: plazo de disponibilidad de grabaciones y antelación del aviso previo; cantidad máxima de sesiones concurrentes por usuario; datos institucionales utilizados en el sitio, comprobantes y constancias (razón social, CUIT, domicilio, logo, email de contacto y teléfono de contacto); credenciales de integración con Google OAuth y con la pasarela de pagos; plazo máximo de espera para la confirmación de un pago antes de registrarlo como incidencia pendiente; proporción de tipos de pregunta en los bancos generados con IA; y tiempo límite de edición de consultas y respuestas del foro. |
| 3 | El actor selecciona un parámetro existente para modificar su valor, o ingresa una nueva clave y su valor para incorporar un parámetro nuevo. |
| 4 | El sistema valida que la clave y el valor hayan sido completados. |
| 5 | El sistema registra o actualiza el parámetro. |
| 6 | El sistema informa el éxito de la operación. |
| 7 | Fin del caso de uso. |

- **Postcondición(es)**: El parámetro queda registrado o actualizado con el nuevo valor.
- **Excepciones**:
    - **Paso 4**: Si la clave o el valor no fueron completados, el sistema informa el error y vuelve al paso 3.
- **Frecuencia**: Baja — se usa al ajustar el comportamiento operativo del sistema.
- **Estabilidad**: Alta
- **Comentarios**: El esquema de clave-valor permite incorporar nuevos parámetros sin modificar el esquema de la base de datos.