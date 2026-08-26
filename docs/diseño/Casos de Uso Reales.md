# 4.8. Casos de Uso Reales

En esta sección se detallan los 99 casos de uso reales del Sistema Idóneos Online, derivados de los casos de uso extendidos y complementados con la información funcional y de interacción disponible en los contratos de módulo, diagramas de secuencia del sistema y diagramas de secuencia del diseño.

---

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
El actor accede a la sección de gestión de cursos, aplica los criterios de búsqueda disponibles y consulta el listado de cursos resultante. Si es Docente, el sistema restringe los resultados a los cursos en los que participa como titular o ayudante. El actor puede seleccionar un curso del listado para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Debe existir al menos un curso activo.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección correspondiente para buscar cursos. |
| 2 | Sistema | Presenta los campos de búsqueda: nombre, categoría, nivel, equipo docente y modalidad. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Buscar': Confirma la búsqueda aplicando los filtros ingresados. |
| 5 | Sistema | Recupera y filtra los cursos que coincidan con los criterios ingresados. Si el actor es Docente, restringe los resultados a los cursos en los que participa como titular o ayudante. |
| 6 | Sistema | Lista los cursos filtrados, mostrando su categoría, nivel, modalidades y equipo docente. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona un curso del listado para consultar su detalle. |
| 8 | Sistema | Muestra el detalle del curso seleccionado. |
| 9 | Actor | [E] Control / Acción: Finaliza la consulta. |

**Postcondición(es)**
- Se recuperan uno o más cursos que cumplen con los criterios de búsqueda, junto con su categoría, nivel, modalidades y equipo docente.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
El Alumno accede a la sección correspondiente para consultar sus inscripciones, aplica los filtros disponibles y visualiza el listado de cursos en los que está inscripto con su progreso general. Puede seleccionar uno para acceder al contenido del programa de su cohorte.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno debe tener al menos una inscripción.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección correspondiente para ver sus cursos inscriptos. |
| 2 | Sistema | Presenta los campos de búsqueda: nombre del curso y estado de la inscripción (Pendiente, En Progreso, Finalizado). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Buscar': Confirma la búsqueda aplicando los filtros ingresados. |
| 5 | Sistema | Recupera y filtra las inscripciones del alumno que coincidan con los criterios ingresados. |
| 6 | Sistema | Lista las inscripciones recuperadas, mostrando el nombre e imagen del curso y el progreso general en cada uno. |
| 7 | Actor | [D] Control de Selección / Item: Selecciona un curso para acceder al contenido del programa de su cohorte. |
| 8 | Sistema | Accede al contenido del curso seleccionado. |

**Postcondición(es)**
- Se recuperan uno o más cursos inscriptos del alumno, y se accede al contenido del curso seleccionado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el actor no posee ninguna inscripción, el sistema informa que no tiene cursos para mostrar y finaliza el caso de uso. |

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
El Administrador accede a la sección de gestión de cursos y selecciona la opción para registrar un nuevo curso. El sistema presenta el formulario con todos los datos requeridos. El Administrador completa la información y el sistema valida y registra el nuevo curso sin cohortes abiertas.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Debe existir al menos una categoría activa.
- Debe existir al menos un docente activo y habilitado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Selecciona la opción para registrar un nuevo curso. |
| 2 | Sistema | Muestra el formulario de registro con los campos: nombre, descripción, precio, imagen de portada (opcional), categoría, nivel, si emite certificado al finalizar, modalidades de dictado, docente titular y docente ayudante (opcional). |
| 3 | Actor | [B] Formulario / Entrada de datos: Completa el campo nombre. |
| 4 | Actor | [C] Formulario / Entrada de datos: Completa el campo descripción. |
| 5 | Actor | [D] Formulario / Entrada de datos: Ingresa el precio. |
| 6 | Actor | [E] Control / Acción: Opcionalmente, adjunta una imagen de portada. |
| 7 | Actor | [F] Control de Selección / Item: Selecciona una categoría del listado disponible. |
| 8 | Actor | [G] Control de Selección / Item: Selecciona un nivel del listado disponible. |
| 9 | Actor | [H] Control / Acción: Indica si el curso emite certificado al finalizar. |
| 10 | Actor | [I] Control de Selección / Item: Selecciona una o más modalidades de dictado. |
| 11 | Actor | [J] Control / Acción: Selecciona el docente titular del listado de docentes habilitados. |
| 12 | Actor | [K] Control de Selección / Item: Opcionalmente, selecciona uno o más docentes ayudantes del listado. |
| 13 | Actor | [L] Botón 'Registrar' / 'Guardar': Confirma el registro del curso. |
| 14 | Sistema | Valida que se hayan completado los campos obligatorios: nombre, descripción, precio, categoría, nivel, al menos una modalidad y docente titular. |
| 15 | Sistema | Valida que el precio ingresado sea mayor o igual a cero. |
| 16 | Sistema | Registra el curso sin cohortes abiertas, con su equipo docente. |
| 17 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El curso queda registrado, sin cohortes abiertas, con su equipo docente (docente titular y, opcionalmente, docente ayudante).
- La fecha de creación refleja el momento del alta.
- Las modalidades de dictado indicadas quedan asociadas al curso.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 14 | Sistema | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y permite al actor corregirlos. |
| 14 | Sistema | Si la categoría o alguno de los docentes no se encuentra activo o habilitado, el sistema informa el error y permite al actor corregirlo. |
| 14 | Sistema | Si el docente titular es también ayudante del curso, el sistema informa el error y permite al actor corregirlo. |
| 15 | Sistema | Si el precio ingresado es menor a cero, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona el curso a modificar mediante CU-01: Buscar curso. El sistema muestra los datos actuales del curso e indica, si corresponde, qué datos pueden modificarse. El Administrador edita los campos habilitados y el sistema valida y actualiza el curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El curso debe estar activo.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el curso a modificar mediante CU-01: Buscar curso. |
| 2 | Sistema | Muestra los datos actuales del curso. Si el curso posee alguna cohorte con inscripción vigente, indica que solo pueden modificarse el precio y la imagen de portada. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica los datos habilitados según el estado del curso: nombre, descripción, categoría, nivel, si emite certificado, modalidades, equipo docente, precio e imagen si no posee cohortes con inscripción vigente; o precio y/o imagen si posee alguna. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que el curso esté activo y, si posee inscripciones activas, que el actor no haya modificado datos distintos al precio o la imagen. |
| 6 | Sistema | Valida que se mantengan completos los campos obligatorios: nombre, descripción, categoría, nivel, al menos una modalidad y docente titular. |
| 7 | Sistema | Valida que el precio, si fue modificado, sea mayor o igual a cero. |
| 8 | Sistema | Actualiza los datos del curso. |
| 9 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El curso queda actualizado con los datos permitidos según si posee cohortes con inscripción vigente o no.
- La fecha de modificación refleja el momento del cambio.
- Las modalidades de dictado quedan actualizadas, si fueron modificadas.
- El equipo docente queda actualizado, si fue modificado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el curso no se encuentra activo, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si el curso tiene alguna inscripción activa asociada y el actor modificó un dato distinto al precio o la imagen, el sistema informa que esos datos no pueden modificarse y cancela la operación. |
| 6 | Sistema | Si algún campo obligatorio queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si la categoría o alguno de los docentes no se encuentra activo o habilitado, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si el docente titular es también ayudante del curso, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si el precio ingresado es menor a cero, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona el curso a dar de baja mediante CU-01: Buscar curso. El sistema verifica las dependencias existentes y, si no hay impedimentos, solicita confirmación. El Administrador confirma y el sistema marca el curso como dado de baja, retirándolo del catálogo público.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El curso debe estar activo.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el curso a dar de baja mediante CU-01: Buscar curso. |
| 2 | Sistema | Verifica que no existan programas activos asociados al curso. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca el curso como dado de baja y lo retira del catálogo público. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El curso queda en baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si el curso tiene algún programa activo asociado, el sistema informa la dependencia y no permite la baja; primero deben darse de baja sus programas (ver CU-18: Dar de baja programa). |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
El Alumno accede al catálogo público de cursos, disponible con o sin sesión iniciada. El sistema lista los cursos con cohortes con inscripción abierta. El Alumno puede filtrar los resultados y seleccionar un curso para consultar su ficha completa, que incluye descripción, nivel, modalidades, precio, contenidos por unidad y las cohortes disponibles.

**Precondición(es)**
- Debe existir al menos un curso con alguna cohorte con inscripción abierta.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede al catálogo público de cursos. |
| 2 | Sistema | Lista los cursos con cohortes abiertas. Permite filtrar por nombre, categoría, nivel, docente y modalidad del curso. |
| 3 | Actor | [B] Control / Acción: Opcionalmente, ingresa criterios de filtro y aplica la búsqueda. |
| 4 | Sistema | Actualiza el listado según los filtros aplicados. |
| 5 | Actor | [C] Control de Selección / Item: Selecciona un curso para ver su ficha. |
| 6 | Sistema | Muestra el detalle público del curso: descripción, nivel, modalidades, precio, contenidos por unidad, y las cohortes con inscripción abierta con su docente titular y cupo disponible si corresponde. |

**Postcondición(es)**
- Se recupera el listado de cursos con cohortes abiertas y, si corresponde, la ficha pública del curso seleccionado con sus cohortes con inscripción abierta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
El Administrador accede a la sección de gestión de categorías, ingresa opcionalmente un criterio de búsqueda por nombre y consulta el listado de categorías resultante. Puede seleccionar una para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Debe existir al menos una categoría activa.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de gestión de categorías. |
| 2 | Sistema | Presenta el campo de búsqueda: nombre. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa el criterio de búsqueda que desea (opcional). |
| 4 | Actor | [C] Botón 'Buscar': Confirma la búsqueda aplicando los filtros ingresados. |
| 5 | Sistema | Recupera y filtra las categorías que coincidan con el criterio ingresado. |
| 6 | Sistema | Lista las categorías filtradas. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona una categoría para ver su detalle. |
| 8 | Sistema | Muestra el detalle de la categoría seleccionada. |

**Postcondición(es)**
- Se recuperan una o más categorías que cumplen con el criterio de búsqueda.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
El Administrador selecciona la opción para registrar una nueva categoría temática. El sistema presenta el formulario con los campos requeridos. El Administrador completa la información y el sistema valida y registra la categoría en estado activo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Selecciona la opción para registrar una nueva categoría. |
| 2 | Sistema | Muestra el formulario con los campos: nombre y descripción (opcional). |
| 3 | Actor | [B] Formulario / Entrada de datos: Completa el campo nombre. |
| 4 | Actor | [C] Control / Acción: Opcionalmente, completa el campo descripción. |
| 5 | Actor | [D] Botón 'Registrar' / 'Guardar': Confirma el registro. |
| 6 | Sistema | Valida que el nombre haya sido completado. |
| 7 | Sistema | Valida que no exista otra categoría activa con el mismo nombre. |
| 8 | Sistema | Registra la categoría en estado activo. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La categoría queda registrada en estado activo.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si el nombre no fue completado, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si ya existe una categoría activa con el mismo nombre, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona la categoría a modificar mediante CU-07: Buscar categoría. El sistema muestra los datos actuales. El Administrador edita el nombre o la descripción y el sistema valida y actualiza los datos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La categoría debe estar activa.
- La categoría no debe tener inscripciones activas asociadas.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la categoría a modificar mediante CU-07: Buscar categoría. |
| 2 | Sistema | Muestra los datos actuales de la categoría: nombre y descripción. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el nombre o la descripción. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que la categoría esté activa y no tenga inscripciones activas asociadas. |
| 6 | Sistema | Valida que el nombre no quede vacío. |
| 7 | Sistema | Valida que el nombre no coincida con el de otra categoría activa. |
| 8 | Sistema | Actualiza los datos de la categoría. |
| 9 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La categoría queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si la categoría no se encuentra activa, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si la categoría tiene inscripciones activas asociadas, el sistema informa que no puede modificarse y cancela la operación. |
| 6 | Sistema | Si el nombre queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si el nombre coincide con el de otra categoría activa, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona la categoría a dar de baja mediante CU-07: Buscar categoría. El sistema verifica que no existan cursos activos asociados. Si no hay impedimentos, solicita confirmación. El Administrador confirma y el sistema marca la categoría como dada de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La categoría debe estar activa.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la categoría a dar de baja mediante CU-07: Buscar categoría. |
| 2 | Sistema | Verifica que no existan cursos activos asociados a la categoría. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca la categoría como dada de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La categoría queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si la categoría tiene cursos activos asociados, el sistema informa la dependencia y no permite la baja. |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
El actor accede a la sección de cohortes de un programa y aplica los criterios de búsqueda disponibles. El sistema lista las cohortes filtradas con sus fechas de inscripción, fechas de dictado si corresponde, cupo máximo y semanas de acceso. El actor puede seleccionar una para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Debe existir al menos una cohorte activa.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de cohortes del programa correspondiente. |
| 2 | Sistema | Presenta los criterios de búsqueda opcionales: estado (Abierta / En dictado / Finalizada) y rango de fechas de inscripción. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Buscar': Confirma la búsqueda aplicando los filtros ingresados. |
| 5 | Sistema | Recupera y filtra las cohortes del programa que coincidan con los criterios ingresados, mostrando sus fechas de inscripción, fechas de dictado si corresponde, cupo máximo y semanas de acceso. |
| 6 | Sistema | Lista las cohortes filtradas. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona una cohorte para ver su detalle. |
| 8 | Sistema | Muestra el detalle de la cohorte seleccionada. |

**Postcondición(es)**
- Se recupera el listado de cohortes del programa seleccionado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
El Administrador busca y selecciona el programa para el cual desea registrar una nueva cohorte mediante CU-15: Buscar programa. El sistema presenta el formulario correspondiente. El Administrador completa los datos y el sistema realiza las validaciones necesarias antes de registrar la cohorte, haciendo público al curso en el catálogo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El programa debe estar activo y vigente.
- El programa debe tener el mínimo de unidades establecido con material publicado en su cronograma.
- El curso no debe tener cohortes de programas anteriores sin finalizar.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa para el cual desea registrar una nueva cohorte mediante CU-15: Buscar programa. |
| 2 | Sistema | Muestra el formulario de registro con los campos: fecha de inicio de inscripción, fecha de fin de inscripción, cupo máximo (opcional), semanas de acceso al contenido desde la inscripción y, si la modalidad del curso incluye clases en vivo, fecha de inicio y fin de dictado. |
| 3 | Actor | [B] Formulario / Entrada de datos: Completa los datos solicitados. |
| 4 | Actor | [C] Botón 'Registrar' / 'Guardar': Confirma el registro. |
| 5 | Sistema | Valida que el programa esté activo, vigente y cuente con el mínimo de unidades establecido con material publicado. |
| 6 | Sistema | Valida que el curso no tenga cohortes de programas anteriores sin finalizar. |
| 7 | Sistema | Valida que se hayan completado los campos obligatorios: fechas de inscripción y semanas de acceso. |
| 8 | Sistema | Valida que, si el curso incluye la modalidad En vivo, se hayan completado las fechas de dictado. |
| 9 | Sistema | Valida que la fecha de fin de inscripción sea posterior a la fecha de inicio de inscripción y, si corresponde, que la fecha de fin de dictado sea posterior a la fecha de inicio de dictado. |
| 10 | Sistema | Valida que, si corresponde, la fecha de inicio de dictado no sea anterior a la fecha de fin de inscripción. |
| 11 | Sistema | Valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 12 | Sistema | Valida que las semanas de acceso no sean menores a la duración total del cronograma del programa. |
| 13 | Sistema | Registra la cohorte, asociada al programa. |
| 14 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La cohorte queda registrada, asociada al programa vigente del curso.
- El curso queda visible en el catálogo público mientras la cohorte tenga inscripción abierta.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el programa no se encuentra activo o vigente, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si el programa no cuenta con el mínimo de unidades con material publicado, el sistema informa el error y no permite registrar la cohorte. |
| 6 | Sistema | Si el curso tiene cohortes de programas anteriores sin finalizar, el sistema informa el error y cancela la operación. |
| 7 | Sistema | Si algún campo obligatorio no fue completado, el sistema informa cuáles faltan y permite al actor corregirlos. |
| 8 | Sistema | Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado, el sistema informa el error y permite al actor corregirlo. |
| 9 | Sistema | Si la fecha de fin de inscripción no es posterior a la de inicio, o la fecha de fin de dictado no es posterior a la de inicio, el sistema informa el error y permite al actor corregirlo. |
| 10 | Sistema | Si la fecha de inicio de dictado es anterior a la fecha de fin de inscripción, el sistema informa el error y permite al actor corregirlo. |
| 11 | Sistema | Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y permite al actor corregirlo. |
| 12 | Sistema | Si las semanas de acceso son menores a la duración total del cronograma, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona la cohorte a modificar mediante CU-11: Buscar cohorte. El sistema muestra los datos actuales. El Administrador modifica los datos habilitados y el sistema realiza las validaciones correspondientes antes de actualizar la cohorte.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La cohorte debe estar activa y pertenecer al programa vigente.
- La cohorte no debe tener inscripciones activas asociadas.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la cohorte a modificar mediante CU-11: Buscar cohorte. |
| 2 | Sistema | Muestra los datos actuales de la cohorte: fechas de inscripción, cupo máximo, semanas de acceso y, si corresponde, fechas de dictado. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica la fecha de inicio o fin de inscripción, el cupo máximo, las semanas de acceso o, si corresponde, las fechas de dictado. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que la cohorte esté activa, pertenezca al programa vigente y no posea inscripciones activas asociadas. |
| 6 | Sistema | Valida que se mantengan completos los campos obligatorios. |
| 7 | Sistema | Valida que, si el curso incluye la modalidad En vivo, se hayan completado las fechas de dictado. |
| 8 | Sistema | Valida las relaciones entre fechas de inscripción y dictado. |
| 9 | Sistema | Valida que, si se indicó cupo máximo, sea un valor entero mayor a cero. |
| 10 | Sistema | Valida que las semanas de acceso no sean menores a la duración total del cronograma. |
| 11 | Sistema | Actualiza los datos de la cohorte. |
| 12 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La cohorte queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si la cohorte no se encuentra activa o no pertenece al programa vigente, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si la cohorte tiene inscripciones activas asociadas, el sistema informa que estos datos no pueden modificarse y cancela la operación. |
| 6 | Sistema | Si algún campo obligatorio queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si el curso incluye la modalidad En vivo y no se completaron las fechas de dictado, el sistema informa el error y permite al actor corregirlo. |
| 8 | Sistema | Si alguna relación entre fechas es inválida, el sistema informa el error y permite al actor corregirlo. |
| 9 | Sistema | Si el cupo máximo ingresado no es un número entero mayor a cero, el sistema informa el error y permite al actor corregirlo. |
| 10 | Sistema | Si las semanas de acceso son menores a la duración del cronograma, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona la cohorte a dar de baja mediante CU-11: Buscar cohorte. El sistema verifica que no posea inscripciones activas. Si no hay impedimentos, solicita confirmación. El Administrador confirma y el sistema marca la cohorte como dada de baja (Cancelada).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La cohorte debe estar activa.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la cohorte a dar de baja mediante CU-11: Buscar cohorte. |
| 2 | Sistema | Verifica que la cohorte no posea inscripciones activas. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca la cohorte como dada de baja (Cancelada). |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La cohorte queda en baja (Cancelada).

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si la cohorte tiene inscripciones activas asociadas, el sistema informa la dependencia y no permite la baja. |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

## MOD-F-02: Módulo de Gestin Acadmica

### CU-15: Buscar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor accede a los programas de un curso. Por defecto, el sistema resuelve al programa vigente. Si el actor desea actuar sobre un programa distinto, puede buscarlo aplicando criterios opcionales. El sistema lista los programas del curso indicando cuál es el vigente. El actor puede seleccionar uno para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El curso debe estar activo.
- El curso debe tener al menos un programa activo.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la gestin de programas de un curso. |
| 2 | Sistema | Resuelve y presenta por defecto el programa vigente del curso, o el ltimo al que el actor lo haya cambiado previamente. |
| 3 | Actor | [B] Control / Acción: Si desea actuar sobre un programa distinto, solicita buscarlo. |
| 4 | Sistema | Presenta los criterios de bsqueda opcionales: nombre y estado (Vigente / Anterior / Dado de baja). |
| 5 | Actor | [C] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea. |
| 6 | Actor | [D] Botón 'Confirmar': Confirma la bsqueda. |
| 7 | Sistema | Recupera y filtra los programas del curso que coincidan con los criterios, indicando cuál se encuentra vigente. |
| 8 | Sistema | Lista los programas filtrados con su nombre, descripción, objetivos, carga horaria total, cantidad de unidades e indicacin de cuál es el vigente. |
| 9 | Actor | [E] Control de Selección / Item: Opcionalmente, selecciona un programa para ver su detalle. |
| 10 | Sistema | Muestra el detalle del programa seleccionado. |

**Postcondición(es)**
- Se recupera el listado de programas del curso seleccionado, con su nombre, descripción, objetivos, carga horaria total, cantidad de unidades en su cronograma e indicacin de cuál es el vigente.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-16: Registrar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente busca y selecciona el curso para el cual desea registrar un nuevo programa mediante CU-01: Buscar curso. El sistema ofrece la posibilidad de partir de un programa anterior si existe. El Docente completa los datos requeridos y el sistema registra el nuevo programa, que pasa a ser el vigente del curso.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El curso debe estar activo.
- El docente participa en l como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el curso para el cual desea registrar un nuevo programa mediante CU-01: Buscar curso. |
| 2 | Sistema | Ofrece la opción de iniciar a partir de la información de un programa anterior del curso, si existe alguno. Muestra el formulario con los campos: nombre, descripción (opcional), objetivos, carga horaria total (opcional) y bibliografía. |
| 3 | Actor | [B] Control de Selección / Item: Opcionalmente, selecciona un programa anterior del cual partir. |
| 4 | Actor | [C] Formulario / Entrada de datos: Completa o modifica los campos: nombre, objetivos y bibliografía (obligatorios), y descripción y carga horaria total (opcionales). |
| 5 | Actor | [D] Botón 'Registrar' / 'Guardar': Confirma el registro. |
| 6 | Sistema | Valida que el curso está activo y que el docente participe como titular o ayudante. |
| 7 | Sistema | Valida que el nombre, los objetivos y la bibliografía hayan sido completados. |
| 8 | Sistema | Registra el programa, que pasa a ser el vigente del curso. |
| 9 | Sistema | Si el actor seleccion partir de un programa anterior, copia el cronograma de ese programa (unidades, número de orden y semanas de duración) al nuevo programa. |
| 10 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El programa queda registrado y pasa a ser el vigente del curso.
- El programa anterior del curso, si exista, deja de estar vigente, sin perder su cronograma ni afectar a los alumnos ya inscriptos.
- Si se parti de un programa anterior, el nuevo programa queda con el mismo cronograma como punto de partida editable.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si el curso no se encuentra activo, el sistema informa el error y cancela la operación. |
| 6 | Sistema | Si el docente no participa como titular o ayudante, el sistema informa el error y cancela la operación. |
| 7 | Sistema | Si el nombre, los objetivos o la bibliografía no fueron completados, el sistema informa el error y permite al actor corregirlos. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-17: Modificar programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente busca y selecciona el programa a modificar mediante CU-15: Buscar programa. El sistema muestra los datos actuales. El Docente edita los campos correspondientes y el sistema valida y actualiza el programa.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa debe estar activo.
- El programa no debe tener inscripciones activas asociadas.
- El docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa a modificar mediante CU-15: Buscar programa. |
| 2 | Sistema | Muestra los datos actuales del programa: nombre, descripción, objetivos, carga horaria total y bibliografía. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el nombre, la descripción, los objetivos, la carga horaria total o la bibliografía. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que el programa está activo, no tenga inscripciones activas y el docente participe como titular o ayudante. |
| 6 | Sistema | Valida que el nombre, los objetivos y la bibliografía no queden vacos. |
| 7 | Sistema | Actualiza los datos del programa. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El programa queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el programa no se encuentra activo, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si el programa tiene inscripciones activas, el sistema informa que estos datos no pueden modificarse y cancela la operación. |
| 5 | Sistema | Si el docente no participa como titular o ayudante, el sistema informa el error y cancela la operación. |
| 6 | Sistema | Si el nombre, los objetivos o la bibliografía quedan vacos, el sistema informa el error y permite al actor corregirlos. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-18: Dar de baja programa

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor busca y selecciona el programa a dar de baja mediante CU-15: Buscar programa. El sistema verifica que el docente participe en el curso y que el programa no tenga cohortes asociadas. Si no hay impedimentos, solicita confirmación. El actor confirma y el sistema marca el programa como dado de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa debe estar activo.
- Si el actor es Docente, participa en l como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa a dar de baja mediante CU-15: Buscar programa. |
| 2 | Sistema | Verifica que, si el actor es Docente, participe en el curso como titular o ayudante. Verifica que el programa no tenga ninguna cohorte asociada. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca el programa como dado de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El programa queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si el docente no participa como titular o ayudante, el sistema informa el error y cancela la operación. |
| 2 | Sistema | Si el programa tiene cohortes activas asociadas, el sistema informa la dependencia y no permite la baja; primero deben darse de baja sus cohortes (ver CU-14: Dar de baja cohorte). |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-19: Buscar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor accede al cronograma del programa vigente y consulta el listado de unidades. Al seleccionar una unidad, el sistema despliega su contenido completo: material, términos de glosario, pools de preguntas, autoevaluaciones y clases, con las opciones de gestin correspondientes.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa debe estar activo.
- Si el actor es Docente, participa en l como titular o ayudante.
- El programa debe tener al menos una unidad incluida dentro de su cronograma.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de unidades del programa correspondiente. |
| 2 | Sistema | Recupera y lista las unidades del cronograma del programa, mostrando el título y la descripción de cada unidad. |
| 3 | Actor | [B] Control de Selección / Item: Selecciona una unidad para gestionar su contenido. |
| 4 | Sistema | Despliega el contenido de la unidad seleccionada: su material, sus términos de glosario, sus pools de preguntas, sus autoevaluaciones y sus clases (en vivo y con Clon de IA), con las opciones para darlos de alta, modificarlos o eliminarlos. |

**Postcondición(es)**
- Se recupera el listado de unidades del cronograma del programa. Al seleccionar una, se despliega su material, pools, autoevaluaciones y clases con las opciones de gestin.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-20: Agregar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente busca y selecciona el programa para el cual desea agregar una unidad mediante CU-15: Buscar programa. El sistema ofrece crear una unidad nueva o incorporar una ya existente de otro programa del mismo curso. Segn la opción elegida, el Docente completa los datos o selecciona la unidad existente, y el sistema la incorpora al final del cronograma con una semana de duración por defecto.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa debe estar activo.
- El docente participa en l como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa para el cual desea agregar una nueva unidad mediante CU-15: Buscar programa. |
| 2 | Sistema | Solicita si desea crear una unidad nueva o incorporar una ya existente en otro programa del mismo curso. Si hay unidades reutilizables, las lista para su seleccin. |
| 3 | Actor | [B] Control / Acción: Si crea una unidad nueva: ingresa el título, la descripción (opcional) y el contenido. Si incorpora una existente: selecciona la unidad del listado. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la operación. |
| 5 | Sistema | Valida que el programa está activo y que el docente participe como titular o ayudante. |
| 6 | Sistema | Si se cre una unidad nueva, valida que el título y el contenido hayan sido completados. |
| 7 | Sistema | Registra la unidad, si es nueva. |
| 8 | Sistema | Incorpora la unidad (nueva o existente) al cronograma del programa vigente del curso, en la ltima posición, con una semana de duración. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La unidad queda registrada, si era nueva.
- La unidad (nueva o existente) queda incorporada al cronograma del programa vigente del curso, con el número de orden siguiente al ltimo y una semana de duración.
- La fecha de creación refleja el momento del alta, si la unidad era nueva.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el programa no se encuentra activo, el sistema informa el error y cancela la operación. |
| 5 | Sistema | Si el docente no participa como titular o ayudante, el sistema informa el error y cancela la operación. |
| 6 | Sistema | Si el título o el contenido no fueron completados, el sistema informa el error y permite al actor corregirlos. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-21: Modificar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente selecciona la unidad a modificar desde la vista de unidades del programa (ver CU-19: Buscar unidad). El sistema muestra los datos actuales. El Docente edita el título, la descripción o el contenido, y el sistema valida y actualiza la unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe y no se encuentra en baja, y el docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Selecciona la unidad a modificar desde la vista de unidades del programa (ver CU-19: Buscar unidad). |
| 2 | Sistema | Muestra los datos actuales de la unidad: título, descripción y contenido. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el título, la descripción o el contenido. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que la unidad no pertenezca al cronograma de un programa con alguna cohorte con inscripción vigente. |
| 6 | Sistema | Valida que el título y el contenido no queden vacos. |
| 7 | Sistema | Actualiza los datos de la unidad. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La unidad queda actualizada con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si la unidad pertenece al cronograma de un programa con alguna cohorte con inscripción vigente, el sistema informa que su contenido no puede modificarse mientras haya alumnos inscriptos y cancela la operación. |
| 6 | Sistema | Si el título o el contenido quedan vacos, el sistema informa el error y permite al actor corregirlos. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-22: Quitar unidad

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor selecciona la unidad a quitar desde la vista de unidades del programa (ver CU-19: Buscar unidad). El sistema verifica las condiciones necesarias y, si no hay impedimentos, solicita confirmación. El actor confirma y el sistema quita la unidad del cronograma del programa. Si ya no pertenece a ningn otro cronograma, la da de baja automáticamente.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe, no se encuentra en baja, y pertenece al cronograma del programa desde el cual se la quiere quitar.
- Si el actor es Docente, participa como titular o ayudante en el curso de ese programa.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Selecciona la unidad a quitar desde la vista de unidades del programa (ver CU-19: Buscar unidad). |
| 2 | Sistema | Verifica que no existan intentos de autoevaluación asociados sobre autoevaluaciones de esta unidad. |
| 3 | Sistema | Verifica que la unidad no pertenezca al cronograma de un programa con alguna cohorte con inscripción vigente. |
| 4 | Actor | [B] Botón 'Confirmar': Confirma la operación. |
| 5 | Sistema | Quita la unidad del cronograma de ese programa. |
| 6 | Sistema | Verifica si la unidad sigue formando parte del cronograma de algn otro programa. |
| 7 | Sistema | Si ya no forma parte de ningn cronograma, marca la unidad como dada de baja. |
| 8 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La unidad queda quitada del cronograma de ese programa.
- Si ya no pertenece a ningn otro cronograma, queda además en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si ya existe algn intento de autoevaluación sobre esta unidad, el sistema informa la dependencia y no permite la operación. |
| 3 | Sistema | Si el programa posee alguna cohorte con inscripción vigente, el sistema informa que no puede quitarse mientras haya alumnos activos y cancela la operación. |
| 4 | Actor | Si el actor no confirma, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-23: Buscar cronograma

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor accede al cronograma de un programa seleccionado. El sistema recupera y lista las unidades del cronograma en su orden, con su duración en semanas y la cantidad de material cargado en cada una.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa cuyo cronograma desea consultar. |
| 2 | Sistema | Recupera y lista las unidades del cronograma del programa, ordenadas por número de orden, mostrando el título, la duración en semanas y la cantidad de material cargado de cada una. |

**Postcondición(es)**
- Se recupera el cronograma del programa seleccionado: sus unidades, con título, número de orden, semanas de duración y cantidad de material cargado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-24: Modificar cronograma

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente accede al cronograma del programa mediante CU-23: Buscar cronograma. El sistema muestra las unidades en su orden actual con sus duraciones. El Docente reordena las unidades y/o modifica las duraciones. El sistema valida y actualiza el cronograma.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El programa existe, no se encuentra en baja, y el docente participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede al cronograma del programa que desea modificar mediante CU-23: Buscar cronograma. |
| 2 | Sistema | Muestra las unidades del cronograma en su orden actual, con su duración en semanas. |
| 3 | Actor | [B] Control / Acción: Reordena las unidades arrastrndolas y soltndolas, y/o modifica la duración en semanas de una o más unidades. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que no existan cohortes del programa con al menos una inscripción vigente. |
| 6 | Sistema | Valida que cada duración ingresada sea un número entero de semanas mayor a cero. |
| 7 | Sistema | Actualiza el cronograma. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El cronograma del programa queda actualizado, con sus unidades en el nuevo orden y con la nueva duración.
- La fecha de modificación del programa refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el programa posee alguna cohorte con al menos una inscripción vigente, el sistema informa que el cronograma no puede modificarse y cancela la operación. |
| 6 | Sistema | Si alguna duración ingresada no es un número entero de semanas mayor a cero, el sistema informa el error y permite al actor corregirlo. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- –

---

### CU-25: Ver participantes

**Objetivo(s) asociado(s)**
- OBJ-01: Gestionar y explorar el catlogo de cursos.

**Requisito(s) de información asociado(s)**
- RI-01: Información sobre el catlogo de cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor accede a la sección de participantes de un curso. El sistema permite filtrar por nombre, apellido y rol. Se listan el equipo docente y los alumnos inscriptos en todas las cohortes del curso que coincidan con los criterios.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Si es Docente, participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de participantes del curso. |
| 2 | Sistema | Presenta los criterios de bsqueda opcionales: nombre o apellido y el rol (Alumno / Docente). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios que desea. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y lista el equipo docente y los alumnos de todas las cohortes del curso, filtrados por los criterios ingresados. Para el equipo docente, indica si es titular o ayudante. |

**Postcondición(es)**
- Se recupera el listado de participantes que cumplen los criterios, con nombre, apellido, rol y, para el equipo docente, si es titular o ayudante.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si ningn participante coincide con los criterios, el sistema informa que no se encontraron resultados. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Alumno

**Descripción**
El Alumno selecciona un curso en el que está inscripto y accede a su contenido. Puede consultar el cronograma con su avance, navegar las unidades habilitadas según su progreso secuencial, ver los participantes de su cohorte o consultar sus calificaciones. Al acceder a una unidad, el sistema despliega su material publicado, glosario, foro y autoevaluaciones con sus intentos previos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente al curso. Si la cohorte del alumno posee fechas de dictado, la fecha actual no debe ser anterior a la fecha de inicio de dictado.
- El programa de la cohorte del alumno posee al menos una unidad en su cronograma.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Selecciona un curso en el que está inscripto (ver CU-02: Ver mis cursos). |
| 2 | Sistema | Presenta las opciones disponibles: (a) consultar el cronograma del curso; (b) buscar y acceder a una unidad; (c) consultar los participantes del curso; (d) consultar sus propias calificaciones. |
| 3a | Actor | [B] Control / Acción: Si solicita el cronograma: selecciona la opción correspondiente. |
| 3b | Sistema | Recupera y lista las unidades del programa de su cohorte con título, número de orden y duración en semanas. Calcula la semana esperada de avance y, si el alumno va por detrs de lo esperado, lo indica. |
| 4a | Actor | [C] Control / Acción: Si solicita buscar unidades: selecciona la opción correspondiente. |
| 4b | Sistema | Lista las unidades del programa de su cohorte, indicando cuáles están habilitadas según su avance secuencial. |
| 5 | Actor | [D] Control de Selección / Item: Selecciona una unidad habilitada. |
| 6 | Sistema | Verifica que la unidad se encuentre habilitada para el alumno según su avance secuencial. |
| 7 | Sistema | Despliega el contenido publicado de la unidad: material, términos de glosario, acceso al foro, autoevaluaciones (con los intentos que el alumno ya haya registrado) y clases en vivo programadas. |
| 8a | Actor | [E] Control / Acción: Si solicita ver participantes: selecciona la opción correspondiente. |
| 8b | Sistema | Recupera y lista el equipo docente completo y los alumnos de su propia cohorte. |
| 9a | Actor | [F] Control / Acción: Si solicita ver sus calificaciones: selecciona la opción correspondiente. |
| 9b | Sistema | Recupera y lista, para cada autoevaluación rendida en el programa de su cohorte, la nota y el resultado. |

**Postcondición(es)**
- Segn lo que el alumno consulte: el cronograma con indicacin de atraso si corresponde; el listado de unidades con las habilitadas y el contenido de la seleccionada; los participantes del curso; o las calificaciones propias.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si la unidad no está habilitada para el alumno, el sistema informa que debe aprobar primero la autoevaluación de la unidad anterior y no despliega su contenido. |

**Frecuencia**
- Alta

**Estabilidad**
**Descripción**
El actor accede al cronograma de un programa seleccionado. El sistema recupera y lista las unidades del cronograma en su orden, con su duración en semanas y la cantidad de material cargado en cada una.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El programa existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el programa cuyo cronograma desea consultar. |
| 2 | Sistema | Recupera y lista las unidades del cronograma del programa, ordenadas por número de orden, mostrando el título, la duración en semanas y la cantidad de material cargado de cada una. |

**Postcondición(es)**
- Se recupera el cronograma del programa seleccionado: sus unidades, con título, número de orden, semanas de duración y cantidad de material cargado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- –

### CU-26b: Acceder curso — Modo Edición (Docente / Administrador)

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestión Académica

**Actor(es)**
- Docente, Administrador

**Descripción**
El Docente o Administrador accede a la vista del curso (idéntica a la del Alumno) y activa el toggle "Modo Edición". Desde esa vista puede gestionar el contenido de cada unidad (materiales, glosario, foro, autoevaluaciones) directamente sobre la misma pantalla, sin navegar a paneles de gestión externos.

> **Nota de diseño (Variante de pantalla):** Este CU no es un Caso de Uso Extendido independiente; es una variante de la vista del curso que todos los roles comparten. El flujo de negocio real (agregar/modificar/eliminar material, glosario, etc.) se documenta en CU-27 a CU-42. Este CU-26b documenta únicamente la interfaz de entrada al "Modo Edición" y las opciones que éste habilita. La precondición de acceso al curso sigue siendo la misma que en CU-01 y CU-19.

**Precondición(es)**
- El actor ha iniciado sesión con rol Docente o Administrador.
- El curso existe y no está dado de baja.
- Si el actor es Docente, participa en el curso como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Descripción de UI |
| :--- | :--- | :--- |
| 1 | Actor | [A] Acción: Busca y selecciona un curso desde "Mis Cursos" (tarjetas de curso, ver CU-01). |
| 2 | Sistema | [B] Pantalla: Muestra la Vista del Curso estilo Moodle: breadcrumb del curso, lista de unidades en acordeón (colapsadas por defecto), barra de navegación superior con tabs: `Unidades \| Materiales \| Glosario \| Foro \| Evaluaciones \| Participantes`. |
| 3 | Actor | [C] Control: Activa el toggle "Modo Edición" (switch visible en la barra superior de la vista del curso). |
| 4 | Sistema | [D] Pantalla: La vista cambia al Modo Edición. Cada unidad del acordeón muestra al pie opciones inline: `+ Agregar material`, `+ Agregar término de glosario`, `+ Agregar pregunta al foro`, `+ Agregar autoevaluación`, `+ Programar clase en vivo`. Los ítems existentes dentro de cada unidad muestran acciones de edición y baja. |
| 5 | Actor | [E] Acción: Selecciona una acción inline dentro de una unidad (ej. `+ Agregar material`). |
| 6 | Sistema | [F] Modal: Abre un modal de selección de tipo de contenido con radio buttons. Al confirmar, despliega el formulario de alta correspondiente (ver CU-28: Subir material, CU-32: Registrar término, CU-36: Registrar consulta de foro, CU-40: Registrar respuesta, CU-56: Registrar clase en vivo). |
| 7 | Actor | [G] Acción: Completa el formulario y confirma. El sistema cierra el modal y actualiza la unidad en tiempo real. |
| 8 | Sistema | [H] Confirmación: El contenido recién agregado aparece en la unidad correspondiente del acordeón. |

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 3 | Sistema | Si el actor no tiene permisos de edición sobre el curso (ej. Docente sin asignación de titular/ayudante), el toggle "Modo Edición" no es visible. |

**Frecuencia**
- Alta

**Estabilidad**
- Alta

**Comentarios**
- El toggle Modo Edición es un mecanismo de UI únicamente. No genera un mensaje DSS propio; cada acción de gestión que realiza el actor desde este modo desencadena los flujos DSS de los CU-27 a CU-42 según corresponda.
- Esta vista es el punto de entrada obligatorio para todos los CU de gestión de contenido (CU-27 a CU-42). El actor siempre debe estar en el contexto de un curso seleccionado.

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Solicita buscar material dentro de una unidad (ver CU-19: Buscar unidad). |
| 2 | Sistema | Solicita la unidad sobre la que se desea consultar y, opcionalmente, el tipo de material, el título y si fue generado por IA. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Actor | [C] Botón 'Buscar': Confirma la búsqueda. |
| 5 | Sistema | Recupera y lista el material que coincide con los criterios, incluyendo el no publicado. |
| 6 | Actor | [D] Control de Selección / Item: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recupera el material de la unidad, indicando su tipo, título, si fue generado por IA y su estado de publicación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Solicita subir un nuevo material dentro de una unidad (ver CU-19: Buscar unidad). |
| 2 | Sistema | Muestra el formulario solicitando el tipo de material (Grabación, Bibliografía o Presentación) y el título. |
| 3 | Actor | [B] Formulario / Entrada de datos: Selecciona el tipo e ingresa el título. |
| 4 | Sistema | Según el tipo elegido, solicita: el archivo de video (Grabación); el archivo o enlace externo y autor (Bibliografía); o el archivo de la presentación (Presentación). |
| 5 | Actor | [C] Formulario / Entrada de datos: Ingresa los datos solicitados y adjunta el archivo o enlace. |
| 6 | Actor | [D] Botón 'Confirmar': Confirma la carga del material. |
| 7 | Sistema | Valida que se hayan completado el título, el tipo y los datos obligatorios según el tipo seleccionado. |
| 8 | Sistema | Registra el material en estado oculto (no publicado). |
| 9 | Sistema | Informa el éxito de la carga. |

**Postcondición(es)**
- El material queda registrado, asociado a la unidad, en estado no publicado.
- La fecha de creación refleja el momento de la carga.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 7 | Sistema | Si no se completó el título o alguno de los datos obligatorios según el tipo de material, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el material a modificar (ver CU-27: Buscar material). |
| 2 | Sistema | Muestra los datos actuales del material. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el título, el archivo o el estado de publicación. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la modificación. |
| 5 | Sistema | Valida que el título no quede vacío. |
| 6 | Sistema | Si se modificó el archivo, valida que se hayan completado los datos obligatorios correspondientes al tipo de material. |
| 7 | Sistema | Actualiza los datos del material. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El material queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el título queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si el nuevo archivo no cumple los datos obligatorios del tipo de material, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor busca y selecciona el material a dar de baja (ver CU-28: Buscar material). Confirma la operación y el sistema marca el material como dado de baja, dejando de mostrarlo a los alumnos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El material existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el material a dar de baja (ver CU-28: Buscar material). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca el material como dado de baja y deja de mostrarlo a los alumnos. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El material queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
Desde la vista de una unidad, el actor accede al glosario y consulta los términos cargados, filtrando opcionalmente por término o definición. Puede seleccionar un término específico para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- La unidad existe y posee al menos un término de glosario cargado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de glosario de la unidad (ver CU-19: Buscar unidad). |
| 2 | Sistema | Presenta los criterios de bsqueda opcionales: término y definición. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y lista los términos de glosario de la unidad que coincidan con los criterios ingresados. |
| 6 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona un término para ver su detalle. |
| 7 | Sistema | Muestra el término y la definición del término seleccionado. |

**Postcondición(es)**
- Se recupera el listado de términos y definiciones del glosario de la unidad.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente selecciona la unidad (ver CU-20: Buscar unidad) y selecciona la opción para registrar un nuevo término en su glosario. El sistema presenta el formulario con los campos requeridos. El Docente completa el término y su definición, y el sistema valida y registra el término.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad existe, no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Selecciona la unidad (ver CU-20: Buscar unidad) y selecciona la opción para registrar un nuevo término de glosario. |
| 2 | Sistema | Muestra el formulario con los campos: término y definición. |
| 3 | Actor | [B] Formulario / Entrada de datos: Completa el campo término. |
| 4 | Actor | [C] Formulario / Entrada de datos: Completa el campo definición. |
| 5 | Actor | [D] Botón 'Registrar' / 'Guardar': Confirma el registro. |
| 6 | Sistema | Valida que el término y la definición hayan sido completados. |
| 7 | Sistema | Valida que el término no está ya registrado en el glosario de esa unidad. |
| 8 | Sistema | Registra el término de glosario asociado a la unidad. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El término de glosario queda registrado y asociado a la unidad.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si el término o la definición no fueron completados, el sistema informa el error y permite al actor corregirlos. |
| 7 | Sistema | Si el término ya está registrado en el glosario de esa unidad, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente busca y selecciona el término de glosario a modificar (ver CU-31: Buscar término de glosario). El sistema muestra los datos actuales. El Docente edita el término o la definición y el sistema valida y actualiza los datos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El término de glosario existe y no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el término de glosario a modificar (ver CU-31: Buscar término de glosario). |
| 2 | Sistema | Muestra los datos actuales del término: término y definición. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el término o la definición. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que el término y la definición no queden vacos. |
| 6 | Sistema | Si se modific el término, valida que no coincida con el de otro término ya registrado en el glosario de esa unidad. |
| 7 | Sistema | Actualiza el término de glosario. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El término de glosario queda actualizado con los nuevos datos.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el término o la definición quedan vacos, el sistema informa el error y permite al actor corregirlos. |
| 6 | Sistema | Si el término modificado ya está registrado en el glosario de esa unidad, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor busca y selecciona el término de glosario a dar de baja (ver CU-31: Buscar término de glosario). Confirma la operación y el sistema marca el término como dado de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- El término de glosario existe y no se encuentra en baja, y el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el término de glosario a dar de baja (ver CU-31: Buscar término de glosario). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca el término de glosario como dado de baja. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El término de glosario queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
Desde la vista del foro de una unidad, el actor consulta las preguntas planteadas por los alumnos, filtrando opcionalmente por texto o rango de fechas. Puede seleccionar una consulta para ver su detalle con sus respuestas asociadas.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una consulta de foro registrada en la unidad.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede al foro de la unidad (ver CU-19: Buscar unidad). |
| 2 | Sistema | Presenta los criterios de bsqueda opcionales: texto de la consulta y rango de fechas. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y lista las consultas de foro que coincidan con los criterios, mostrando el texto de la consulta, la fecha y el alumno autor de cada una, junto con sus respuestas si existen. |
| 6 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona una consulta para ver su detalle. |
| 7 | Sistema | Muestra el detalle de la consulta seleccionada con sus respuestas. |

**Postcondición(es)**
- Se recupera el listado de consultas del foro de la unidad, junto con sus respuestas.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Alumno

**Descripción**
El Alumno, desde el foro de una unidad a la que tiene acceso (ver CU-26: Acceder curso), selecciona la opción para plantear una nueva consulta. El sistema presenta el campo de texto. El Alumno redacta la consulta y la enva. El sistema la registra y notifica al equipo docente.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente a una cohorte de un programa cuyo cronograma incluye la unidad.
- La unidad se encuentra habilitada según el avance secuencial del alumno.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede al foro de la unidad (ver CU-26: Acceder curso) y selecciona la opción para registrar una nueva consulta. |
| 2 | Sistema | Presenta el campo para ingresar el texto de la consulta. |
| 3 | Actor | [B] Formulario / Entrada de datos: Redacta el texto de la consulta. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma el envo. |
| 5 | Sistema | Valida que el texto de la consulta haya sido completado. |
| 6 | Sistema | Registra la consulta asociada a la unidad y al alumno, con la fecha actual. |
| 7 | Sistema | Notifica al docente titular y al ayudante, si corresponde, de la nueva consulta. |
| 8 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La consulta queda registrada, asociada a la unidad y al alumno.
- El docente recibe la notificación de la nueva consulta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el texto de la consulta no fue completado, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Alumno

**Descripción**
El Alumno busca y selecciona una de sus propias consultas de foro (ver CU-35: Buscar consulta de foro). El sistema verifica que está dentro del plazo límite de edición y muestra el texto actual. El Alumno lo modifica y el sistema valida y actualiza la consulta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- La consulta de foro existe, no se encuentra en baja y fue registrada por el actor.
- No se super el plazo límite de edición configurado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la consulta de foro propia a modificar (ver CU-35: Buscar consulta de foro). |
| 2 | Sistema | Verifica que no se haya superado el plazo límite de edición desde el registro de la consulta. |
| 3 | Sistema | Muestra el texto actual de la consulta. |
| 4 | Actor | [B] Formulario / Entrada de datos: Modifica el texto de la consulta. |
| 5 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 6 | Sistema | Valida que el texto no quede vaco. |
| 7 | Sistema | Actualiza la consulta. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La consulta queda actualizada con el nuevo texto.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si se super el plazo límite de edición, el sistema informa que la consulta ya no puede modificarse y finaliza el caso de uso. |
| 6 | Sistema | Si el texto queda vaco, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Administrador

**Descripción**
El Administrador busca y selecciona la consulta de foro a dar de baja (ver CU-35: Buscar consulta de foro). Confirma la operación y el sistema marca la consulta y sus respuestas asociadas como dadas de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La consulta de foro existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la consulta de foro a dar de baja (ver CU-35: Buscar consulta de foro). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca la consulta como dada de baja, junto con las respuestas asociadas si existen. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La consulta y sus respuestas asociadas quedan en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- No surge de la entrevista con el cliente como requisito explcito; se incorpora como criterio de moderacin razonable para un foro con alumnos y docentes.

---

### CU-39: Buscar respuesta de foro

**Objetivo(s) asociado(s)**
- OBJ-02: Gestionar el contenido de los cursos.

**Requisito(s) de información asociado(s)**
- RI-02: Información sobre el contenido de los cursos.

**Módulo**
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente, Administrador

**Descripción**
El actor selecciona una consulta de foro y solicita ver sus respuestas. El sistema recupera y lista las respuestas registradas para esa consulta. El actor puede seleccionar una respuesta específica para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
- Existe al menos una respuesta registrada para la consulta.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Selecciona una consulta de foro y solicita ver sus respuestas (ver CU-35: Buscar consulta de foro). |
| 2 | Sistema | Recupera y lista las respuestas asociadas a la consulta, mostrando el texto de la respuesta, la fecha y el docente autor de cada una. |
| 3 | Actor | [B] Control de Selección / Item: Opcionalmente, selecciona una respuesta específica para ver su detalle. |
| 4 | Sistema | Muestra el detalle de la respuesta seleccionada. |

**Postcondición(es)**
- Se recupera el listado de respuestas asociadas a la consulta de foro.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente selecciona una consulta de foro (ver CU-35: Buscar consulta de foro) y selecciona la opción para registrar una respuesta. El sistema presenta el campo de texto. El Docente redacta la respuesta y la enva. El sistema la registra y notifica al alumno autor de la consulta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La consulta de foro existe, no se encuentra en baja y el actor participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Selecciona la consulta de foro a responder (ver CU-35: Buscar consulta de foro) y selecciona la opción para registrar una respuesta. |
| 2 | Sistema | Presenta el campo para ingresar el texto de la respuesta. |
| 3 | Actor | [B] Formulario / Entrada de datos: Redacta el texto de la respuesta. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma el envo. |
| 5 | Sistema | Valida que el texto de la respuesta haya sido completado. |
| 6 | Sistema | Registra la respuesta asociada a la consulta y al docente, con la fecha actual. |
| 7 | Sistema | Notifica al alumno autor de la consulta que fue respondida. |
| 8 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La respuesta queda registrada, asociada a la consulta y al docente.
- El alumno recibe la notificación de la respuesta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el texto de la respuesta no fue completado, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Docente

**Descripción**
El Docente busca y selecciona una de sus propias respuestas de foro (ver CU-39: Buscar respuesta de foro). El sistema verifica que está dentro del plazo límite de edición y muestra el texto actual. El Docente lo modifica y el sistema valida y actualiza la respuesta.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La respuesta de foro existe, no se encuentra en baja y fue registrada por el actor.
- No se super el plazo límite de edición configurado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la respuesta de foro propia a modificar (ver CU-39: Buscar respuesta de foro). |
| 2 | Sistema | Verifica que no se haya superado el plazo límite de edición desde el registro de la respuesta. |
| 3 | Sistema | Muestra el texto actual de la respuesta. |
| 4 | Actor | [B] Formulario / Entrada de datos: Modifica el texto. |
| 5 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 6 | Sistema | Valida que el texto no quede vaco. |
| 7 | Sistema | Actualiza la respuesta. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La respuesta queda actualizada con el nuevo texto.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si se super el plazo límite de edición, el sistema informa que la respuesta ya no puede modificarse y finaliza el caso de uso. |
| 6 | Sistema | Si el texto queda vaco, el sistema informa el error y permite al actor corregirlo. |

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
- MOD-F-02: Módulo de Gestin Acadmica

**Actor(es)**
- Administrador

**Descripción**
El Administrador busca y selecciona la respuesta de foro a dar de baja (ver CU-39: Buscar respuesta de foro). Confirma la operación y el sistema marca la respuesta como dada de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La respuesta de foro existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la respuesta de foro a dar de baja (ver CU-39: Buscar respuesta de foro). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca la respuesta como dada de baja. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La respuesta queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
El actor accede a la sección de inscripciones y aplica los criterios de bsqueda disponibles. El sistema lista las inscripciones filtradas. El actor puede seleccionar una para ver su detalle y, si el certificado fue emitido, descargarlo.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
- Existe al menos una inscripción registrada previamente.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de inscripciones. |
| 2 | Sistema | Presenta los criterios de bsqueda: curso y estado (Vigente / Vencida / Dada de baja). Si el actor es Administrador, también presenta el campo alumno. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y filtra las inscripciones que coincidan con los criterios. Si el actor es Alumno, restringe los resultados a las propias. |
| 6 | Sistema | Lista las inscripciones filtradas mostrando: curso, fecha de inscripción, fecha de vencimiento de acceso y estado. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona una inscripción para ver su detalle. |
| 8 | Sistema | Muestra el detalle de la inscripción seleccionada. Si el certificado fue emitido, muestra su número y fecha de emisin junto con la opción de descargarlo. |
| 9 | Actor | [E] Botón 'Descargar': Opcionalmente, descarga el certificado de la inscripción seleccionada si fue emitido. |

**Postcondición(es)**
- Se recuperan una o más inscripciones con su curso, fecha, fecha de vencimiento de acceso, estado y, si el certificado fue emitido, sus datos con el archivo para descargar.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
El Alumno explora el catlogo de cursos (ver CU-06: Explorar catlogo de cursos), consulta la ficha de un curso y selecciona una cohorte con inscripción abierta para inscribirse. El sistema realiza las validaciones necesarias, registra la inscripción con su progreso inicial y, si el curso tiene costo, inicia el proceso de pago (ver CU-47: Realizar pago).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El curso posee al menos una cohorte con inscripción abierta.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Explora el catlogo (ver CU-06: Explorar catlogo de cursos), selecciona un curso y luego selecciona una cohorte con inscripción abierta y solicita inscribirse. |
| 2 | Sistema | Valida que la fecha actual se encuentre dentro de la ventana de inscripción de la cohorte seleccionada. |
| 3 | Sistema | Valida que la cohorte no haya alcanzado su cupo máximo, si tiene uno definido. |
| 4 | Sistema | Valida que el alumno no posea ya una inscripción vigente a esa cohorte. |
| 5 | Sistema | Registra la inscripción con la fecha actual. Calcula la fecha de vencimiento de acceso: si la cohorte no posee fechas de dictado, sumando las semanas de acceso a la fecha actual; si posee fechas de dictado, a partir de la fecha de inicio de dictado. |
| 6 | Sistema | Registra el progreso inicial del alumno sobre la primera unidad del cronograma del programa de la cohorte, sin completar. |
| 7 | Sistema | Si el curso tiene costo, inicia el proceso de pago (ver CU-47: Realizar pago) y aguarda su resultado. |

**Postcondición(es)**
- La inscripción queda registrada, con el progreso inicial del alumno registrado sobre la primera unidad del cronograma.
- Si el curso tiene costo, la inscripción queda sin acceso al contenido hasta que se confirme el pago y, si la cohorte posee fechas de dictado, hasta que además se alcance la fecha de inicio de dictado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si la fecha actual está fuera de la ventana de inscripción, el sistema informa que la inscripción a esa cohorte ya no está habilitada y finaliza el caso de uso. |
| 3 | Sistema | Si la cohorte ya alcanz su cupo máximo, el sistema lo informa y finaliza el caso de uso. |
| 4 | Sistema | Si el alumno ya posee una inscripción vigente a esa cohorte, el sistema lo informa y finaliza el caso de uso. |

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
El actor busca y selecciona la inscripción a dar de baja (ver CU-43: Buscar inscripción). El sistema solicita confirmación e informa que la baja no genera reembolso. El actor confirma la baja, opcionalmente ingresa un motivo u observacin, y el sistema registra la baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
- La inscripción existe, se encuentra vigente y, si el actor es Alumno, le pertenece.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la inscripción a dar de baja (ver CU-43: Buscar inscripción). |
| 2 | Sistema | Solicita confirmación e informa que la baja no genera reembolso del pago realizado. Ofrece opcionalmente un campo para ingresar el motivo u observacin de la baja. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja y, si lo desea, ingresa el motivo u observacin. |
| 4 | Sistema | Informa nuevamente que la baja no genera reembolso del pago realizado y solicita confirmación final. |
| 5 | Actor | [C] Botón 'Confirmar': Confirma la operación. |
| 6 | Sistema | Registra la baja de la inscripción con la observacin ingresada si corresponde. |

**Postcondición(es)**
- La inscripción queda en estado de baja.
- El alumno pierde el acceso al contenido del curso.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- No existe mecanismo de reembolso: la poltica de no devolucin fue confirmada por el cliente, anloga a la habitual en el mbito universitario.

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
El actor accede a la sección de pagos y aplica los criterios de bsqueda disponibles. El sistema lista los pagos filtrados. El actor puede seleccionar uno para ver su detalle y, si el pago fue acreditado, descargar el comprobante.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno o Administrador.
- Existe al menos un pago registrado previamente.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de pagos. |
| 2 | Sistema | Presenta los criterios de bsqueda: curso, estado (Pendiente / Acreditado / Rechazado) y rango de fecha. Si el actor es Administrador, también presenta el campo alumno. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y filtra los pagos que coincidan con los criterios. Si el actor es Alumno, restringe los resultados a los propios. |
| 6 | Sistema | Lista los pagos filtrados mostrando: monto, fecha, mtodo, estado. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona un pago para ver su detalle. |
| 8 | Sistema | Muestra el detalle del pago seleccionado. Si el pago fue acreditado, muestra el número y la fecha de emisin del comprobante junto con la opción de descargarlo. |
| 9 | Actor | [E] Botón 'Descargar': Opcionalmente, descarga el comprobante del pago seleccionado si fue acreditado. |

**Postcondición(es)**
- Se recuperan uno o más pagos con su monto, fecha, mtodo, estado y, si fue acreditado, los datos del comprobante con el archivo para descargar.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
Tras inscribirse a un curso con costo (ver CU-44: Inscribir curso), el sistema deriva al Alumno al proceso de pago. El sistema muestra el monto a pagar con el descuento aplicado si corresponde y los medios de pago disponibles. El Alumno selecciona MODO, el sistema genera la solicitud de pago y muestra el modal de MODO para completar el pago. El resultado (acreditado o rechazado) se recibe de forma automática.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno ya posee una inscripción registrada para el curso, sin el pago realizado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita pagar el curso al que se inscribi. |
| 2 | Sistema | Evala si el alumno cumple alguna condicin de descuento vigente y, de ser as, calcula el monto a pagar con el descuento aplicado. Muestra al alumno el monto a pagar. |
| 3 | Sistema | Recupera y muestra los medios de pago disponibles. |
| 4 | Actor | [B] Control / Acción: Selecciona MODO como medio de pago. |
| 5 | Sistema | Arma la solicitud de pago por ese monto y la enva a MODO. |
| 6 | Sistema | Muestra el modal de pago de MODO: en computadora, un cdigo QR para escanear con la app de MODO o con la app del banco; en celular, redirige a una pantalla para elegir con qué app pagar. |
| 7 | Actor | [C] Control / Acción: Escanea el cdigo QR (o abre la app elegida desde el celular) y completa el pago desde ah, con la tarjeta o el saldo de cuenta que prefiera. |
| 8 | Sistema | Registra el pago como pendiente con los datos que devuelve MODO al crear la solicitud. |
| 9 | Sistema | MODO procesa el pago y notifica al sistema el resultado (acreditado o rechazado) de forma automática. |
| 10 | Sistema | Actualiza el pago con el resultado, el nombre del pagador y, si fue con tarjeta, sus ltimos cuatro dgitos, y guarda la fecha en que se aprob. |
| 11 | Sistema | Si el pago fue acreditado: habilita el acceso al curso, genera los datos del comprobante y se lo enva al alumno por correo con el archivo para descargar. |

**Postcondición(es)**
- El pago queda registrado con su resultado, asociado a la inscripción.
- Si el pago fue acreditado: el acceso al curso queda habilitado, y los datos del comprobante quedan registrados y enviados por correo al alumno.
- Si el pago fue rechazado: la inscripción permanece sin acceso habilitado y se notifica al alumno.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Actor | Si el alumno cancela el pago desde el modal de MODO, el sistema informa que la operación fue cancelada y finaliza el caso de uso. |
| 9 | Sistema | Si MODO rechaza el pago, el sistema registra el pago como rechazado y notifica al alumno el motivo, permitindole reintentar. |
| 9 | Sistema | Si no se recibe la confirmación de MODO dentro del plazo configurado, el sistema registra el pago como rechazado, cancela la operación y notifica al alumno el motivo, permitindole reintentar. |

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
El actor accede a la sección de progresos del curso y aplica opcionalmente el filtro por alumno. El sistema lista el progreso de los alumnos. El actor puede seleccionar un progreso específico para ver el detalle por unidad del alumno.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de progresos de un curso. |
| 2 | Sistema | Presenta el criterio de bsqueda opcional: alumno. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa el criterio de bsqueda que desea. |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera los progresos de los alumnos en el curso. |
| 6 | Sistema | Lista el progreso recuperado, mostrando el nombre del alumno y su avance general. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona un progreso para ver el detalle del alumno en cada unidad. |
| 8 | Sistema | Muestra el detalle del progreso del alumno, indicando por cada unidad del programa de su cohorte si fue completada y, en caso afirmativo, la fecha en que se complet. |

**Postcondición(es)**
- Se recupera el progreso de los alumnos en el curso. Al seleccionar uno, se muestra el detalle por unidad con su estado y fecha de completado si corresponde.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
El Administrador accede a la sección de descuentos y aplica los criterios de bsqueda disponibles. El sistema lista los descuentos filtrados. El actor puede seleccionar uno para ver su detalle.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Existe al menos un descuento registrado previamente.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede a la sección de descuentos. |
| 2 | Sistema | Presenta los criterios de bsqueda: nombre y vigencia (Vigente / Vencido / Agotado). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de bsqueda que desea (todos son opcionales). |
| 4 | Actor | [C] Botón 'Confirmar': Confirma la bsqueda. |
| 5 | Sistema | Recupera y filtra los descuentos que coincidan con los criterios ingresados. |
| 6 | Sistema | Lista los descuentos filtrados mostrando: nombre, porcentaje, vigencia, cantidad límite y cantidad usada. |
| 7 | Actor | [D] Control de Selección / Item: Opcionalmente, selecciona un descuento para ver su detalle. |
| 8 | Sistema | Muestra el detalle del descuento seleccionado. |

**Postcondición(es)**
- Se recuperan uno o más descuentos con su porcentaje, vigencia, cantidad límite y cantidad usada.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
|  |  | Ninguna especificada. |

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
El Administrador selecciona la opción para registrar un nuevo descuento. El sistema presenta el formulario. El Administrador completa los datos y el sistema realiza las validaciones antes de registrar el descuento.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Selecciona la opción para registrar un nuevo descuento. |
| 2 | Sistema | Muestra el formulario con los campos: nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite ofertada y, opcionalmente, la cantidad de cursos que el alumno debe haber comprado como condicin. |
| 3 | Actor | [B] Formulario / Entrada de datos: Completa los datos solicitados. |
| 4 | Actor | [C] Botón 'Registrar' / 'Guardar': Confirma el registro. |
| 5 | Sistema | Valida que se hayan completado los campos obligatorios: nombre, porcentaje, vigencia desde, vigencia hasta y cantidad límite. |
| 6 | Sistema | Valida que el porcentaje sea un valor entre 1 y 100. |
| 7 | Sistema | Valida que la vigencia hasta sea posterior a la vigencia desde. |
| 8 | Sistema | Valida que la cantidad límite sea un número entero mayor a cero. |
| 9 | Sistema | Si se completó la cantidad de cursos requeridos, valida que sea un número entero mayor o igual a cero. |
| 10 | Sistema | Registra el descuento, con cantidad usada en cero. |
| 11 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El descuento queda registrado y activo.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si no se completó alguno de los campos obligatorios, el sistema informa cuáles faltan y permite al actor corregirlos. |
| 6 | Sistema | Si el porcentaje no está entre 1 y 100, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y permite al actor corregirlo. |
| 8 | Sistema | Si la cantidad límite no es un número entero mayor a cero, el sistema informa el error y permite al actor corregirlo. |
| 9 | Sistema | Si la cantidad de cursos requeridos no es un número entero mayor o igual a cero, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona el descuento a modificar (ver CU-49: Buscar descuento). El sistema muestra los datos actuales. El Administrador modifica los datos que desea y el sistema valida y actualiza el descuento.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El descuento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el descuento a modificar (ver CU-49: Buscar descuento). |
| 2 | Sistema | Muestra los datos actuales del descuento. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica los datos que desea. |
| 4 | Actor | [C] Botón 'Guardar cambios': Confirma la modificación. |
| 5 | Sistema | Valida que se mantengan completos los campos obligatorios. |
| 6 | Sistema | Valida que el porcentaje sea un valor entre 1 y 100. |
| 7 | Sistema | Valida que la vigencia hasta sea posterior a la vigencia desde. |
| 8 | Sistema | Valida que la cantidad límite sea un número entero mayor a cero. |
| 9 | Sistema | Si se completó la cantidad de cursos requeridos, valida que sea un número entero mayor o igual a cero. |
| 10 | Sistema | Actualiza los datos del descuento. |
| 11 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El descuento queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si algn campo obligatorio queda vaco, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si el porcentaje no está entre 1 y 100, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si la vigencia hasta no es posterior a la vigencia desde, el sistema informa el error y permite al actor corregirlo. |
| 8 | Sistema | Si la cantidad límite no es un número entero mayor a cero, el sistema informa el error y permite al actor corregirlo. |
| 9 | Sistema | Si la cantidad de cursos requeridos no es un número entero mayor o igual a cero, el sistema informa el error y permite al actor corregirlo. |

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
El Administrador busca y selecciona el descuento a dar de baja (ver CU-49: Buscar descuento). El sistema verifica que la cantidad usada sea cero. Si no hay impedimentos, solicita confirmación. El Administrador confirma y el sistema marca el descuento como dado de baja.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El descuento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el descuento a dar de baja (ver CU-49: Buscar descuento). |
| 2 | Sistema | Verifica que la cantidad usada del descuento sea cero. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca el descuento como dado de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El descuento queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si el descuento ya fue aplicado a alguna inscripción (cantidad usada mayor a cero), el sistema informa la dependencia y no permite la baja. |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar pools dentro de una unidad. |
| 2 | Sistema | Solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre del pool. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y lista los pools que coinciden con los criterios, con su cantidad de preguntas cargadas. |
| 5 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recupera el listado de pools de la unidad, con su nombre y cantidad de preguntas.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita crear un nuevo pool dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Muestra el formulario solicitando el nombre del pool. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa el nombre y comienza a cargar preguntas: por cada una, el tipo (opción múltiple o verdadero/falso), el enunciado y sus opciones de respuesta, marcando cuál es correcta. |
| 4 | Sistema | Valida que el nombre del pool haya sido completado y que se haya cargado al menos una pregunta. |
| 5 | Sistema | Valida que cada pregunta tenga al menos dos opciones de respuesta y que exactamente una esté marcada como correcta. |
| 6 | Sistema | Registra el pool con sus preguntas y opciones. |
| 7 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El pool queda registrado, asociado a la unidad, con sus preguntas y opciones de respuesta.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el nombre no fue completado o no se cargó ninguna pregunta, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si alguna pregunta tiene menos de dos opciones, o no tiene exactamente una opción marcada como correcta, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el pool a modificar (ver CU-53: Buscar pool). |
| 2 | Sistema | Muestra los datos actuales del pool, con sus preguntas y opciones. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el nombre del pool, o agrega, edita o elimina preguntas y sus opciones. |
| 4 | Sistema | Valida que el pool no esté asociado a ninguna autoevaluación con intentos registrados. |
| 5 | Sistema | Valida que el nombre no quede vacío y que el pool conserve al menos una pregunta. |
| 6 | Sistema | Valida que cada pregunta conserve al menos dos opciones y exactamente una marcada como correcta. |
| 7 | Sistema | Actualiza el pool. |
| 8 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- El pool queda actualizado con los nuevos datos.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el pool está asociado a alguna autoevaluación con intentos registrados, el sistema informa que su contenido no puede modificarse y cancela la operación. |
| 5 | Sistema | Si el nombre queda vacío o el pool queda sin preguntas, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si alguna pregunta queda con menos de dos opciones o sin una única opción correcta, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el pool a dar de baja (ver CU-53: Buscar pool). |
| 2 | Sistema | Verifica que el pool no esté asociado a ninguna autoevaluación activa. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca el pool como dado de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El pool queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si el pool está asociado a una autoevaluación activa, el sistema informa la dependencia y no permite la baja. |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar autoevaluaciones dentro de una unidad. |
| 2 | Sistema | Solicita la unidad sobre la que se desea consultar y, opcionalmente, el nombre de la autoevaluación. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y lista las autoevaluaciones que coinciden con los criterios. |
| 5 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recupera el listado de autoevaluaciones de la unidad, con su nombre, pools asociados, tiempo límite, intentos permitidos y si integra pools de otras unidades como evaluación final.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita crear una autoevaluación dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Muestra el formulario solicitando: nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre (opcional), cantidad de intentos permitidos (opcional), y el o los pools de preguntas a asociar (el de la propia unidad y, si es la última unidad del programa, opcionalmente los de otras unidades). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los datos solicitados y selecciona los pools. |
| 4 | Sistema | Valida que se hayan completado los campos obligatorios y que se haya seleccionado al menos un pool. |
| 5 | Sistema | Valida que el tiempo límite, cantidad de preguntas y cantidad de intentos permitidos (si corresponde) sea un valor entero mayor a cero. |
| 6 | Sistema | Valida que, si se especificó una fecha de cierre, esta sea posterior a la fecha de apertura. |
| 7 | Sistema | Valida que los pools seleccionados, en conjunto, tengan como mínimo la cantidad de preguntas ingresada para poder sortear un intento. |
| 8 | Sistema | Registra la autoevaluación. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La autoevaluación queda registrada y asociada a los pools seleccionados, con su fecha de apertura y, si corresponde, su fecha de cierre.
- La fecha de creación refleja el momento del alta.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si no se completaron los campos obligatorios o no se seleccionó ningún pool, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si el tiempo límite, la cantidad de preguntas, o la cantidad de intentos cuando se indicó, no son valores enteros mayores a cero, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si la fecha de cierre especificada no es posterior a la fecha de apertura, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si los pools seleccionados no reúnen como mínimo la cantidad de preguntas ingresada en conjunto, el sistema informa el error y permite al actor corregirlo. |

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
Permite al Docente modificar una autoevaluación. Mientras no registre intentos, puede modificar todos sus datos. Una vez que registra al menos un intento, su contenido queda protegido y solo puede extenderse la fecha de cierre, ocultarse o mostrarse nuevamente, o ampliarse la cantidad de intentos permitidos.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La autoevaluación existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la autoevaluación a modificar (ver CU-57: Buscar autoevaluación). |
| 2 | Sistema | Muestra los datos actuales de la autoevaluación y, si esta ya registra intentos, indica que su contenido queda protegido y solo puede extenderse la fecha de cierre, ocultarse/mostrarse o ampliarse la cantidad de intentos permitidos. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica los datos habilitados según registre intentos o no. |
| 4 | Sistema | Valida que, si la autoevaluación registra intentos, el actor no haya modificado el nombre, el tiempo límite, la fecha de apertura ni los pools asociados. |
| 5 | Sistema | Valida que se mantengan completos los campos obligatorios y al menos un pool asociado. |
| 6 | Sistema | Valida que el tiempo límite sea un valor entero mayor a cero y, si se indicó cantidad de intentos permitidos, que también lo sea y, de existir intentos registrados, que sea mayor a la cantidad actual. |
| 7 | Sistema | Valida que, si se especificó una fecha de cierre, ésta sea posterior a la fecha de apertura y, de existir intentos registrados, también posterior a la fecha de cierre actual. |
| 8 | Sistema | Valida que los pools asociados reúnan como mínimo la cantidad de preguntas ingresada en conjunto. |
| 9 | Sistema | Actualiza la autoevaluación. |
| 10 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La autoevaluación queda actualizada con los datos permitidos según si registra intentos o no.
- La fecha de modificación refleja el momento del cambio.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si la autoevaluación registra intentos y el actor modificó datos protegidos, el sistema informa que ese contenido queda protegido y cancela la operación. |
| 5 | Sistema | Si algún campo obligatorio queda vacío o sin pools asociados, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si las cantidades o tiempos no son enteros mayores a cero (o la cantidad de intentos no es mayor a la actual si hay intentos), el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si la fecha de cierre no es posterior a la de apertura o a la actual si hay intentos, el sistema informa el error y permite al actor corregirlo. |
| 8 | Sistema | Si los pools asociados no reúnen la cantidad mínima de preguntas, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la autoevaluación a dar de baja (ver CU-57: Buscar autoevaluación). |
| 2 | Sistema | Verifica que ningún alumno registre intentos sobre esa autoevaluación. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca la autoevaluación como dada de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La autoevaluación queda en estado de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si algún alumno ya registra un intento sobre esa autoevaluación, el sistema informa la dependencia y no permite la baja. |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar intentos dentro de una autoevaluación. |
| 2 | Sistema | Solicita la autoevaluación sobre la que se desea consultar y, opcionalmente, el alumno, el rango de fechas y el resultado (aprobado / no aprobado). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y lista los intentos que coinciden con los criterios. |
| 5 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recupera el historial de intentos de la autoevaluación, con su fecha, alumno, nota y resultado (aprobado / no aprobado).

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita consultar las calificaciones de un alumno en un curso (ver CU-01: Buscar curso). |
| 2 | Sistema | Solicita el alumno a consultar. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa o selecciona el alumno a consultar. |
| 4 | Sistema | Recupera, para cada autoevaluación rendida por el alumno en el programa de su cohorte, la nota y el resultado. |
| 5 | Sistema | Lista las calificaciones recuperadas. |

**Postcondición(es)**
- Se recupera, por cada autoevaluación rendida, la nota y el resultado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el alumno no rindió ninguna autoevaluación del curso, el sistema informa que no hay calificaciones para mostrar. |

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
Permite al Alumno realizar un intento de una autoevaluación, respondiendo un cuestionario de preguntas sorteadas de los pools asociados, con corrección automática.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Alumno.
- El alumno posee una inscripción vigente al curso.
- La unidad en la que se encuentra la autoevaluación se encuentra habilitada según el avance secuencial del alumno.
- La fecha y hora actual se encuentra dentro del período habilitado de la autoevaluación (posterior a su fecha de apertura y, si corresponde, anterior a su fecha de cierre).
- Si la autoevaluación tiene un límite de intentos, el alumno no lo superó para esa autoevaluación.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Selecciona una autoevaluación para iniciar un intento viendo el contenido de la unidad (ver CU-26: Acceder curso). |
| 2 | Sistema | Sortea la cantidad de preguntas cerradas de la autoevaluación de los pools asociados, con sus opciones de respuesta. |
| 3 | Sistema | Presenta el cuestionario al alumno dentro del tiempo límite configurado. |
| 4 | Actor | [B] Control de Selección / Item: Selecciona una opción de respuesta para cada una de las preguntas. |
| 5 | Actor | [C] Botón 'Confirmar': Confirma la entrega del intento. |
| 6 | Sistema | Valida que se hayan respondido todas las preguntas. |
| 7 | Sistema | Corrige automáticamente el intento, comparando la opción elegida por el alumno con la opción correcta de cada pregunta. |
| 8 | Sistema | Calcula la nota del intento y registra el intento con la fecha actual. |
| 9 | Sistema | Si el alumno respondió correctamente todas las preguntas, aprueba el intento, registra el progreso de la unidad como completada y, si correspondía a la evaluación final del curso, genera los datos del certificado de finalización, los registra en la inscripción del alumno y los envía por correo electrónico. |
| 10 | Sistema | Si el alumno no respondió correctamente todas las preguntas, no aprueba el intento e informa que debe reintentar el cuestionario completo. |

**Postcondición(es)**
- El intento queda registrado, con la opción elegida por el alumno en cada pregunta sorteada, la nota obtenida y el resultado.
- Si fue aprobado: el progreso del alumno en la unidad queda registrado como completado, con la fecha de aprobación, y se habilita el acceso a la siguiente unidad; y, si correspondía a la evaluación final, el certificado queda registrado en la inscripción y enviado por correo.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si se agota el tiempo límite sin que el alumno haya respondido todas las preguntas, el sistema cierra automáticamente el intento con las respuestas dadas hasta ese momento y lo registra como no aprobado. |

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
Permite al Administrador dar de baja un intento de autoevaluación ante la detección de fraude, revirtiendo el progreso de unidad y el certificado que ese intento haya generado, si corresponde.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El intento existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el intento a dar de baja por fraude (ver CU-61: Buscar intento de autoevaluación). |
| 2 | Sistema | Solicita confirmación de la baja. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 4 | Sistema | Marca el intento como dado de baja. |
| 5 | Sistema | Si el intento estaba aprobado y había marcado como completada la unidad correspondiente, revierte el progreso de esa unidad a no completada. |
| 6 | Sistema | Si el intento correspondía a la evaluación final del curso y había generado un certificado, revierte la emisión del certificado de la inscripción. |
| 7 | Sistema | Notifica al alumno la anulación del intento. |

**Postcondición(es)**
- El intento queda en estado de baja.
- Si correspondía, el progreso de la unidad y el certificado emitido quedan revertidos.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 3 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar clases en vivo dentro de una unidad. |
| 2 | Sistema | Solicita la unidad sobre la que se desea consultar y, opcionalmente, el título, el docente, el rango de fechas y el estado (Programada / En vivo / Finalizada). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y lista las clases en vivo que coinciden con los criterios ingresados. |
| 5 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recuperan una o más clases en vivo, con su título, fecha y hora, docente y estado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita programar una clase en vivo dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Muestra el formulario solicitando: la cohorte a la que se dirige la clase, título, fecha y hora, y duración estimada. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los datos solicitados. |
| 4 | Sistema | Valida que se hayan completado los campos obligatorios. |
| 5 | Sistema | Valida que la fecha y hora ingresadas sean posteriores al momento actual. |
| 6 | Sistema | Valida que la fecha y hora de la clase se encuentren dentro de las fechas de dictado de la cohorte seleccionada. |
| 7 | Sistema | Valida que la fecha y hora, considerando la duración estimada, no se superpongan con otra clase en vivo programada del mismo docente. |
| 8 | Sistema | Registra la clase en estado Programada, asociada a la unidad, al docente y a la cohorte. |
| 9 | Sistema | Notifica a los alumnos inscriptos en la cohorte la fecha de la clase. |
| 10 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La clase en vivo queda registrada en estado Programada, asociada a la unidad, al docente y a la cohorte.
- Los alumnos inscriptos en la cohorte reciben la notificación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si no se completó alguno de los campos obligatorios, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si la fecha y hora ingresadas no son posteriores al momento actual, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si la fecha y hora no se encuentran dentro de las fechas de dictado de la cohorte, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si la clase se superpone con otra clase en vivo programada del mismo docente, el sistema informa el conflicto de horario y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase en vivo programada a modificar (ver CU-65: Buscar clase en vivo). |
| 2 | Sistema | Muestra los datos actuales de la clase. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el título, la fecha, la hora o la duración estimada. |
| 4 | Sistema | Valida que se mantengan completos los campos obligatorios y que la fecha y hora sean posteriores al momento actual. |
| 5 | Sistema | Valida que la fecha y hora se encuentren dentro de las fechas de dictado de la cohorte de la clase. |
| 6 | Sistema | Valida que la fecha y hora, considerando la duración estimada, no se superpongan con otra clase en vivo programada del mismo docente. |
| 7 | Sistema | Actualiza los datos de la clase. |
| 8 | Sistema | Notifica a los alumnos inscriptos el cambio de fecha u horario, si corresponde. |
| 9 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La clase en vivo queda actualizada con los nuevos datos.
- Los alumnos inscriptos reciben la notificación del cambio, si corresponde.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si algún campo obligatorio queda vacío, o la fecha y hora no son posteriores al momento actual, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si la nueva fecha y hora no se encuentran dentro de las fechas de dictado de la cohorte, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si la clase se superpone con otra clase en vivo programada del mismo docente, el sistema informa el conflicto de horario y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase en vivo programada a cancelar (ver CU-65: Buscar clase en vivo). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la cancelación. |
| 3 | Sistema | Marca la clase como dada de baja. |
| 4 | Sistema | Notifica a los alumnos inscriptos la cancelación de la clase. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La clase en vivo queda dada de baja.
- Los alumnos inscriptos reciben la notificación de la cancelación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la cancelación, el sistema cancela la operación y finaliza el caso de uso. |

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
Permite al Administrador dar de baja una clase en vivo ya finalizada, retirando también su grabación asociada si existe.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- La clase en vivo existe, no se encuentra en baja y se encuentra en estado Finalizada.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase en vivo finalizada a dar de baja (ver CU-65: Buscar clase en vivo). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca la clase como dada de baja. |
| 4 | Sistema | Si la clase generó un material de tipo Grabación, el sistema también lo marca como dado de baja. |
| 5 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La clase en vivo queda en estado de baja.
- Su grabación, si existía, también queda en baja y deja de estar disponible para los alumnos.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
Permite al Docente iniciar la transmisión de una clase en vivo programada, generando los datos de conexión que utilizará desde OBS.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado Programada.
- Se alcanzó el horario programado para la clase.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase en vivo programada (ver CU-65: Buscar clase en vivo) y solicita iniciarla en el horario programado. |
| 2 | Sistema | Genera los datos de conexión de la transmisión (URL de streaming y clave privada de transmisión) y los presenta al docente. |
| 3 | Sistema | Pasa la clase al estado En vivo. |
| 4 | Actor | [B] Control / Acción: Carga los datos de conexión en OBS y comienza a transmitir. |
| 5 | Sistema | Recibe la señal transmitida y la redistribuye en simultáneo a los alumnos inscriptos que ingresan a la clase (ver CU-72: Ingresar a clase en vivo), mientras graba automáticamente la transmisión mediante el protocolo RTMP desde OBS. |

**Postcondición(es)**
- La clase en vivo queda en estado En vivo, con sus datos de conexión generados.
- La transmisión queda disponible para los alumnos inscriptos y se graba automáticamente.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Docente finalizar la transmisión de una clase en vivo, dando de baja la señal en OBS de forma remota y generando la grabación resultante como material de la unidad.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La clase en vivo existe, fue registrada por el actor y se encuentra en estado En vivo.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase en vivo en curso (ver CU-65: Buscar clase en vivo) y solicita finalizar la transmisión. |
| 2 | Sistema | Envía la orden de corte de transmisión y grabación al OBS del docente. |
| 3 | Sistema | Pasa la clase al estado Finalizada. |
| 4 | Sistema | Genera la grabación resultante de la transmisión. |
| 5 | Sistema | Carga la grabación como material de tipo Grabación de la unidad correspondiente, en estado publicado. |
| 6 | Sistema | Notifica a los alumnos inscriptos que la grabación ya está disponible. |

**Postcondición(es)**
- La clase en vivo queda en estado Finalizada.
- La grabación queda cargada como material publicado de la unidad.
- Los alumnos inscriptos reciben la notificación de disponibilidad de la grabación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Selecciona una clase en vivo para ingresar viendo el contenido de la unidad (ver CU-26: Acceder curso). |
| 2 | Sistema | Verifica que la clase se encuentre en estado En vivo y que el alumno posea inscripción vigente al curso. |
| 3 | Sistema | Conecta al alumno a la transmisión en curso y muestra el reproductor. |

**Postcondición(es)**
- El alumno queda conectado a la transmisión en vivo de la clase.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si la clase todavía no comenzó o ya finalizó, el sistema informa que la transmisión no está disponible en este momento. |

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
Permite al Docente generar automáticamente un pool de preguntas para una unidad, a partir de la bibliografía y el glosario cargados, mediante un modelo de inteligencia artificial ejecutado localmente (Ollama).

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía o un término de glosario cargado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar un banco de preguntas dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Solicita, opcionalmente, un guión adicional ingresado como prompt de texto para orientar la generación. |
| 3 | Actor | [B] Botón 'Confirmar': Confirma la generación, con o sin el guión adicional. |
| 4 | Sistema | Envía la bibliografía, el glosario de la unidad y el guión (si fue ingresado) al modelo de inteligencia artificial local. |
| 5 | Sistema | El modelo genera un banco de preguntas cerradas de opción múltiple y verdadero/falso siguiendo la proporción configurada. |
| 6 | Sistema | Recibe el banco de preguntas y valida que cada pregunta tenga al menos dos opciones y exactamente una marcada como correcta. |
| 7 | Sistema | Registra el pool generado, asociado a la unidad. |
| 8 | Sistema | Notifica al docente que el pool está disponible para su revisión antes de publicarse. |

**Postcondición(es)**
- El pool generado queda registrado, asociado a la unidad.
- El docente recibe la notificación para revisar el pool antes de utilizarlo en una autoevaluación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 6 | Sistema | Si el modelo de inteligencia artificial devuelve un banco de preguntas con un formato inválido, el sistema descarta el resultado, informa el error al docente y le permite reintentar. |

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
Permite al Docente generar automáticamente un resumen del contenido de una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía cargado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar un resumen dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la generación. |
| 3 | Sistema | Envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | Sistema | El modelo de inteligencia artificial genera un resumen estructurado del contenido. |
| 5 | Sistema | Recibe el resumen y lo registra como material de tipo Resumen de la unidad, en estado no publicado. |
| 6 | Sistema | Notifica al docente que el resumen está disponible para su revisión antes de publicarlo. |

**Postcondición(es)**
- El resumen queda registrado como material de la unidad, sin publicar.
- El docente recibe la notificación para revisarlo.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Docente generar automáticamente una presentación descargable para una unidad, a partir de su bibliografía cargada, mediante el modelo de inteligencia artificial local.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- La unidad posee al menos un material de tipo Bibliografía cargado.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar una presentación dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la generación. |
| 3 | Sistema | Envía la bibliografía cargada de la unidad al modelo de inteligencia artificial local. |
| 4 | Sistema | El modelo de inteligencia artificial devuelve una estructura de contenidos (títulos, subtítulos y puntos clave). |
| 5 | Sistema | Da formato a la estructura recibida como una presentación descargable y la registra como material de tipo Presentación de la unidad, en estado no publicado. |
| 6 | Sistema | Notifica al docente que la presentación está disponible para su revisión antes de publicarla. |

**Postcondición(es)**
- La presentación queda registrada como material de la unidad, sin publicar.
- El docente recibe la notificación para revisarla.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Docente registrar su clon de inteligencia artificial (avatar visual y voz), requisito previo para generar clases con clon.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El docente se encuentra habilitado para dictar clases.
- El docente no posee un avatar y voz clonada.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita crear su clon de inteligencia artificial. |
| 2 | Sistema | Solicita una imagen con el rostro del docente (adjunta o tomada con la cámara web) y presenta un guión de ejemplo para grabar el audio. |
| 3 | Actor | [B] Control / Acción: Adjunta o toma la foto, y graba un audio de uno a dos minutos leyendo el guión de ejemplo con su voz. |
| 4 | Sistema | Muestra los términos y condiciones de uso del clon y solicita su aceptación. |
| 5 | Actor | [C] Control / Acción: Acepta los términos y condiciones. |
| 6 | Sistema | Envía la imagen y el audio a HeyGen para crear el avatar y clonar la voz. |
| 7 | Sistema | HeyGen valida que la imagen y el audio sean aptos, crea el avatar y clona la voz, y devuelve sus identificadores (avatar_id y voice_id). |
| 8 | Sistema | Registra el avatar_id, el voice_id y la fecha de aceptación de los términos y condiciones en el perfil del docente. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- El docente queda con su avatar_id y voice_id registrados, habilitado para generar clases con Clon de IA.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Actor | Si el actor no acepta los términos y condiciones, el sistema cancela la operación y finaliza el caso de uso. |
| 7 | Sistema | Si HeyGen no logra validar la imagen o el audio provistos, el sistema informa el error y solicita al docente que los reintente. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar clases con Clon de IA dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Solicita la unidad sobre la que se desea consultar y, opcionalmente, el título y el estado (Pendiente / Generada / Error). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y lista las clases con Clon de IA que coinciden con los criterios. |
| 5 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recuperan una o más clases con Clon de IA, con su título, guión, estado y fecha de generación.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Docente titular o ayudante de un curso generar una clase para una unidad mediante un Clon de inteligencia artificial, a partir de un guión redactado como prompt, integrando con la plataforma HeyGen.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Docente.
- El docente se encuentra habilitado para dictar clases.
- El docente tiene registrado su avatar y voz clonada.
- La unidad existe y está incluida en el cronograma de algún programa de un curso en el que el docente participa como titular o ayudante.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar una clase con Clon de IA dentro de una unidad (ver CU-20: Buscar unidad). |
| 2 | Sistema | Muestra el formulario solicitando el título de la clase y el guión (ingresado como prompt de texto). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa el título y redacta el guión. |
| 4 | Sistema | Valida que el título y el guión hayan sido completados. |
| 5 | Sistema | Registra la clase en estado Pendiente. |
| 6 | Sistema | Envía el guión a HeyGen, junto con el avatar_id y el voice_id del docente. |
| 7 | Sistema | HeyGen anima el avatar del docente con gestos faciales y la voz clonada al ritmo del guión, generando el video de la clase. |
| 8 | Sistema | Descarga el video generado y actualiza el estado de la clase a Generada. |
| 9 | Sistema | Carga el video como material de tipo Grabación de la unidad correspondiente, en estado no publicado. |
| 10 | Sistema | Notifica al docente que el material está disponible para su revisión antes de publicarlo. |

**Postcondición(es)**
- La clase con Clon de IA queda registrada, en estado Generada.
- El video generado queda cargado como material de la unidad, sin publicar.
- El docente recibe la notificación para revisar el material.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el título o el guión no fueron completados, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase con Clon de IA a modificar (ver CU-77: Buscar clase con clon). |
| 2 | Sistema | Muestra el título y el guión actuales de la clase. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el título y/o el guión. |
| 4 | Sistema | Valida que el título y el guión se mantengan completos. |
| 5 | Sistema | Si el guión fue modificado, actualiza el estado de la clase a Pendiente y envía el nuevo guión con el Avatar del docente a HeyGen. |
| 6 | Sistema | HeyGen genera el nuevo video de la clase a partir del guión actualizado. |
| 7 | Sistema | Descarga el video generado, actualiza el estado de la clase a Generada y reemplaza el material de tipo Grabación de la unidad, en estado no publicado. |
| 8 | Sistema | Actualiza la clase con los nuevos datos. |
| 9 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La clase con Clon de IA queda actualizada con el nuevo título y/o guión.
- Si el guión fue modificado, el video queda regenerado y el material de la unidad, reemplazado sin publicar.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el título o el guión quedan vacíos, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si HeyGen no logra generar el video, el sistema actualiza el estado de la clase a Error y notifica al docente para que reintente. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la clase con Clon de IA a dar de baja (ver CU-77: Buscar clase con clon). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 3 | Sistema | Marca la clase y su material asociado como dados de baja. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La clase con Clon de IA y su material asociado quedan dados de baja.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

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
Permite a un interesado crear su propia cuenta de Alumno en la plataforma, mediante correo electrónico y contraseña, validando la cuenta a través de un enlace enviado por email.

**Precondición(es)**
- –

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita crear una cuenta. |
| 2 | Sistema | Muestra el formulario solicitando: nombre, apellido, correo electrónico, DNI y contraseña. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los datos solicitados. |
| 4 | Sistema | Valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 5 | Sistema | Registra la cuenta con rol Alumno, con el correo sin validar. |
| 6 | Sistema | Envía un enlace de validación al correo electrónico ingresado. |
| 7 | Actor | [C] Enlace/Opción: Accede al enlace recibido en su correo electrónico. |
| 8 | Sistema | Marca el correo electrónico como validado. |
| 9 | Sistema | Informa el éxito del registro y habilita el inicio de sesión. |

**Postcondición(es)**
- La cuenta queda registrada con rol Alumno.
- La cuenta queda validada una vez que el actor accede al enlace enviado por correo.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si no se completó alguno de los campos obligatorios, el sistema informa el error y permite al actor corregirlo. |
| 4 | Sistema | Si el correo electrónico ya está registrado, el sistema informa el error y sugiere iniciar sesión o recuperar la contraseña. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita buscar uno o más usuarios. |
| 2 | Sistema | Presenta los criterios de búsqueda: nombre, apellido, correo electrónico, DNI y rol (Alumno / Docente / Administrador). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y filtra los usuarios que coincidan con los criterios ingresados. |
| 5 | Sistema | Lista los usuarios filtrados con su rol y estado. |
| 6 | Actor | [C] Control / Acción: Puede seleccionar uno de los resultados para ver su detalle. |

**Postcondición(es)**
- Se recuperan uno o más usuarios que cumplen con los criterios de búsqueda, con su rol y estado.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Administrador registrar manualmente la cuenta de un Alumno, Docente u otro Administrador.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita registrar manualmente un usuario. |
| 2 | Sistema | Muestra el formulario solicitando: nombre, apellido, correo electrónico, DNI, teléfono (opcional) y el rol a asignar (Alumno, Docente o Administrador). |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los datos solicitados. |
| 4 | Sistema | Si el rol seleccionado es Docente, se ejecuta el CU-88: Registrar docente para solicitar los datos adicionales del perfil docente. |
| 5 | Sistema | Valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 6 | Sistema | Registra la cuenta con el rol indicado y envía al correo ingresado un enlace para que el usuario defina su contraseña. |
| 7 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La cuenta queda registrada con el rol indicado y, si corresponde, con los datos adicionales del perfil docente.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si no se completó alguno de los campos obligatorios, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si el correo electrónico ya está registrado, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la cuenta del alumno a modificar (ver CU-82: Buscar usuario). |
| 2 | Sistema | Muestra los datos actuales de la cuenta. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el nombre, apellido, correo electrónico, DNI, teléfono o imagen de perfil. |
| 4 | Sistema | Valida que se mantengan completos los campos obligatorios y que el correo electrónico, si fue modificado, no esté ya registrado por otra cuenta. |
| 5 | Sistema | Actualiza los datos de la cuenta. |
| 6 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La cuenta del alumno queda actualizada con los nuevos datos.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si algún campo obligatorio queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 4 | Sistema | Si el correo electrónico ya está registrado por otra cuenta, el sistema informa el error y permite al actor corregirlo. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la cuenta del usuario a dar de baja (ver CU-82: Buscar usuario). |
| 2 | Sistema | Si el usuario posee rol Administrador, valida que existan otros administradores activos en el sistema. |
| 3 | Sistema | Si el usuario posee rol Docente, verifica que no sea titular ni ayudante de ninguna cohorte vigente. |
| 4 | Sistema | Si el usuario posee rol Alumno y posee inscripciones vigentes, advierte que la baja le hará perder el acceso al contenido, sin derecho a reembolso. |
| 5 | Actor | [B] Botón 'Confirmar': Confirma la baja. |
| 6 | Sistema | Marca la cuenta como dada de baja y cierra sus sesiones activas. |
| 7 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La cuenta queda en estado de baja y pierde acceso al sistema.
- Las sesiones activas del usuario quedan cerradas.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 2 | Sistema | Si el usuario es Administrador y es el único administrador activo, informa que no puede quedar sin administradores y cancela la operación. |
| 3 | Sistema | Si el docente es titular o ayudante de una cohorte vigente, informa la dependencia y no permite la baja. |
| 5 | Actor | Si el actor no confirma la baja, el sistema cancela la operación y finaliza el caso de uso. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- El sistema debe garantizar en todo momento la existencia de al menos un Administrador activo.

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita ver su perfil. |
| 2 | Sistema | Recupera los datos de la cuenta del actor. |
| 3 | Sistema | Muestra los datos: nombre, apellido, correo electrónico, DNI, teléfono e imagen de perfil, y los datos profesionales adicionales si el actor es Docente. |

**Postcondición(es)**
- Se recuperan los datos de la cuenta del actor.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita editar su perfil. |
| 2 | Sistema | Muestra el formulario con los datos actuales de la cuenta del actor. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica el nombre, apellido, teléfono o imagen de perfil. |
| 4 | Sistema | Valida que se mantengan completos los campos obligatorios. |
| 5 | Sistema | Actualiza los datos de la cuenta. |
| 6 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- La cuenta del actor queda actualizada con los nuevos datos.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si algún campo obligatorio queda vacío, el sistema informa el error y permite al actor corregirlo. |

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
Permite al Administrador registrar manualmente la cuenta de un nuevo docente, verificando previamente sus credenciales académicas o profesionales.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita registrar un nuevo docente. |
| 2 | Sistema | Muestra el formulario solicitando: nombre, apellido, correo electrónico, DNI, teléfono, biografía, años de experiencia, títulos universitarios/posgrado con matrícula profesional cuando corresponda, y matrícula de CNV cuando aplique. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los datos solicitados. |
| 4 | Actor | [C] Control / Acción: Verifica externamente el título o matrícula declarada. |
| 5 | Sistema | Valida que se hayan completado los campos obligatorios y que el correo electrónico no esté ya registrado. |
| 6 | Sistema | Valida que se haya declarado al menos un título universitario/posgrado o al menos una matrícula profesional. |
| 7 | Sistema | Valida que los años de experiencia sean un número entero mayor o igual a cero. |
| 8 | Sistema | Registra la cuenta con rol Docente, habilitada, y envía al correo ingresado un enlace para que el docente defina su contraseña. |
| 9 | Sistema | Informa el éxito del registro. |

**Postcondición(es)**
- La cuenta del docente queda registrada, habilitada y con su información profesional cargada.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si no se completó algún campo obligatorio o el correo ya está registrado, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si no se declaró ningún título ni matrícula, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si los años de experiencia no son un entero mayor o igual a cero, el sistema informa el error y permite al actor corregirlo. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- La verificación del título o la matrícula es un control manual y externo que realiza el Administrador antes de cargar los datos.

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
Permite al Administrador modificar la información profesional de un docente y habilitarlo o suspenderlo temporalmente para dictar clases.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- El docente existe y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona el docente a modificar (ver CU-82: Buscar usuario). |
| 2 | Sistema | Muestra los datos profesionales actuales del docente. |
| 3 | Actor | [B] Formulario / Entrada de datos: Modifica la biografía, años de experiencia, títulos, matrícula, o el estado de habilitación para dictar clases. |
| 4 | Sistema | Valida que se mantengan completos los campos obligatorios. |
| 5 | Sistema | Valida que se mantenga declarado al menos un título o matrícula profesional. |
| 6 | Sistema | Valida que los años de experiencia sean un número entero mayor o igual a cero. |
| 7 | Sistema | Si el actor intenta suspender la habilitación, verifica que no sea titular ni ayudante de ninguna cohorte vigente. |
| 8 | Sistema | Actualiza los datos del docente. |
| 9 | Sistema | Si se suspendió la habilitación, notifica al docente el cambio de estado. |
| 10 | Sistema | Informa el éxito de la modificación. |

**Postcondición(es)**
- Los datos profesionales del docente quedan actualizados.
- Si se modificó su estado de habilitación, el docente queda habilitado o suspendido para dictar clases.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si algún campo obligatorio queda vacío, el sistema informa el error y permite al actor corregirlo. |
| 5 | Sistema | Si la modificación deja al docente sin títulos ni matrículas declaradas, el sistema informa el error y permite al actor corregirlo. |
| 6 | Sistema | Si los años de experiencia no son un entero mayor o igual a cero, el sistema informa el error y permite al actor corregirlo. |
| 7 | Sistema | Si el docente a suspender es titular o ayudante de una cohorte vigente, informa la dependencia y no permite la suspensión. |

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
Permite a un usuario iniciar sesión en el sistema mediante correo electrónico y contraseña, o mediante Google OAuth como método alternativo.

**Precondición(es)**
- El actor posee una cuenta registrada y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita iniciar sesión. |
| 2 | Sistema | Muestra el formulario solicitando correo electrónico y contraseña, u ofreciendo la opción de ingresar con Google. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa sus credenciales, o selecciona ingresar con Google y autoriza el acceso a su cuenta. |
| 4 | Sistema | Valida las credenciales ingresadas, o el token devuelto por Google. |
| 5 | Sistema | Valida que la cantidad de sesiones concurrentes activas del usuario no supere el límite configurado. |
| 6 | Sistema | Registra una nueva sesión, con su token, fecha de inicio, IP y dispositivo. |
| 7 | Sistema | Informa el éxito del inicio de sesión y redirige al panel principal correspondiente a su rol. |

**Postcondición(es)**
- La sesión queda registrada y activa.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si las credenciales ingresadas son incorrectas, el sistema informa el error y permite reintentar. |
| 4 | Sistema | Si el correo electrónico todavía no fue validado, el sistema informa que debe validarlo antes de iniciar sesión. |
| 5 | Sistema | Si el usuario ya alcanzó el límite de sesiones concurrentes, informa el error y solicita cerrar una sesión activa. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita cerrar su sesión. |
| 2 | Sistema | Registra la fecha de fin de la sesión activa. |
| 3 | Sistema | Redirige al actor a la pantalla de inicio de sesión. |

**Postcondición(es)**
- La sesión queda cerrada.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite a un usuario restablecer su contraseña cuando la olvidó, mediante un token temporal enviado a su correo electrónico.

**Precondición(es)**
- El actor posee una cuenta registrada con contraseña propia y no se encuentra en baja.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Selecciona la opción de recuperar contraseña desde la pantalla de inicio de sesión. |
| 2 | Sistema | Solicita el correo electrónico asociado a la cuenta. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa el correo electrónico. |
| 4 | Sistema | Valida que el correo esté registrado. |
| 5 | Sistema | Genera un token de recuperación con su fecha de expiración y lo envía al correo del actor. |
| 6 | Actor | [C] Enlace/Opción: Accede al enlace recibido e ingresa la nueva contraseña. |
| 7 | Sistema | Valida que el token no haya expirado. |
| 8 | Sistema | Actualiza la contraseña de la cuenta. |
| 9 | Sistema | Informa el éxito de la operación y permite iniciar sesión con la nueva contraseña. |

**Postcondición(es)**
- La contraseña de la cuenta queda actualizada.
- El actor puede iniciar sesión con la nueva contraseña.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 4 | Sistema | Si el correo ingresado no está registrado, el sistema informa el error y permite corregirlo. |
| 7 | Sistema | Si el token de recuperación expiró, el sistema informa el error y solicita generar uno nuevo. |

**Frecuencia**
- Media

**Estabilidad**
- Alta

**Comentarios**
- No aplica a las cuentas que se autentican exclusivamente mediante Google OAuth.

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita ver las sesiones activas. |
| 2 | Sistema | Solicita, opcionalmente, el usuario (solo para Administrador), el rango de fechas y la IP o el dispositivo. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y filtra las sesiones del actor (o del usuario indicado si es Administrador) que coincidan con los criterios. |
| 5 | Sistema | Lista las sesiones filtradas, con su fecha de inicio, fecha de fin, IP y dispositivo. |
| 6 | Actor | [C] Control / Acción: Puede seleccionar una sesión específica. |

**Postcondición(es)**
- Se recupera el listado de sesiones activas, con su fecha de inicio, IP y dispositivo.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control de Selección / Item: Busca y selecciona la sesión activa a cerrar (ver CU-93: Buscar sesión). |
| 2 | Actor | [B] Botón 'Confirmar': Confirma el cierre de la sesión. |
| 3 | Sistema | Registra la fecha de fin de esa sesión. |
| 4 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- La sesión seleccionada queda cerrada.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
- OBJ-08: Registrar las acciónones críticas del sistema.

**Requisito(s) de información asociado(s)**
- RI-08: Información sobre auditoría.

**Módulo**
- MOD-NF-02: Módulo de Auditoría

**Actor(es)**
- Administrador

**Descripción**
Permite al Administrador consultar el registro de auditoría de las acciónones críticas del sistema para garantizar trazabilidad sobre las operaciones.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.
- Existe al menos un registro de auditoría.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Control / Acción: Solicita consultar el registro de auditoría. |
| 2 | Sistema | Presenta los criterios de búsqueda: usuario responsable, tipo de acciónón (Crear / Modificar / Eliminar / Consultar), entidad afectada y rango de fecha. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios de búsqueda que desea. |
| 4 | Sistema | Recupera y filtra los registros de auditoría que coincidan con los criterios ingresados. |
| 5 | Sistema | Lista los registros filtrados, mostrando: usuario responsable, tipo de acciónón, entidad afectada, identificador puntual, valor anterior y nuevo (si aplica), IP y fecha/hora exacta. |

**Postcondición(es)**
- Se recuperan uno o más registros de auditoría que cumplen con los criterios.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Administrador generar un informe de alumnos de un curso con 4 vistas gráficas y comparativas, registrándolo en el historial de reportes y permitiendo su descarga.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar un informe de alumnos de un curso. |
| 2 | Sistema | Solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios solicitados. |
| 4 | Sistema | Recopila los datos de alumnos inscriptos al curso (agrupados por programa para evolución temporal y comparación) junto con los del resto de cursos, y genera el informe con sus 4 vistas. |
| 5 | Sistema | Registra el reporte generado del curso, con tipo de reporte, fecha y usuario responsable. |
| 6 | Sistema | Pone el informe a disposición del actor para su descarga. |

**Postcondición(es)**
- El reporte queda registrado en el historial de reportes generados.
- El informe de alumnos del curso seleccionado queda disponible para su descarga.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Administrador generar un informe de ingresos de un curso con 4 vistas analíticas y comparativas, registrándolo en el historial y permitiendo su descarga.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Botón / Opción de acción: Solicita generar un informe de ingresos de un curso. |
| 2 | Sistema | Solicita el rango de fecha y el curso sobre el que se desea informar. |
| 3 | Actor | [B] Formulario / Entrada de datos: Ingresa los criterios solicitados. |
| 4 | Sistema | Recopila los pagos acreditados del curso seleccionado agrupados por programa y respecto al resto de cursos, generando el informe con sus 4 vistas. |
| 5 | Sistema | Registra el reporte generado, con tipo de reporte, fecha y usuario responsable. |
| 6 | Sistema | Pone el informe a disposición del actor para su descarga. |

**Postcondición(es)**
- El reporte queda registrado en el historial de reportes generados.
- El informe de ingresos del curso seleccionado queda disponible para su descarga.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Actor | [A] Enlace/Opción: Accede al panel de estadísticas. |
| 2 | Sistema | Recupera y muestra en pantalla los indicadores de: alumnos activos, cantidad total de inscripciones vigentes, ingresos del mes con variación respecto al mes anterior, inscripciones de los últimos 30 días y ranking (top 5) de cursos con más inscriptos. |

**Postcondición(es)**
- Se muestran en pantalla los indicadores de negocio y avance de la plataforma.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| — | — | Ninguna especificada. |

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
Permite al Administrador consultar y modificar el valor de los parámetros operativos del sistema mediante un esquema de clave-valor predefinido.

**Precondición(es)**
- El actor ha iniciado sesión en el sistema con el rol Administrador.

**Flujo de eventos**

| Paso | Actor / Sistema | Interacción |
| :--- | :--- | :--- |
| 1 | Sistema | Presenta y lista los parámetros configurados en el sistema (plazos, sesiones concurrentes, datos institucionales, credenciales externas, tiempos de espera, etc.). |
| 2 | Actor | [A] Control de Selección / Item: Selecciona un parámetro existente para modificar su valor. |
| 3 | Sistema | Muestra el valor actual y el campo para ingresar el nuevo valor. |
| 4 | Actor | [B] Formulario / Entrada de datos: Ingresa el nuevo valor y confirma la modificación. |
| 5 | Sistema | Valida que el valor haya sido completado. |
| 6 | Sistema | Registra o actualiza el parámetro con el nuevo valor. |
| 7 | Sistema | Informa el éxito de la operación. |

**Postcondición(es)**
- El parámetro queda registrado o actualizado con el nuevo valor.

**Excepciones**

| Paso | Actor / Sistema | Situación |
| :--- | :--- | :--- |
| 5 | Sistema | Si el valor no fue completado, el sistema informa el error y permite al actor corregirlo. |

**Frecuencia**
- Baja

**Estabilidad**
- Alta

**Comentarios**
- El esquema de clave-valor permite incorporar nuevos parámetros sin modificar el esquema de la base de datos.