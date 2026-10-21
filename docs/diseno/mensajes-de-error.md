# Mensajes de error del sistema

Generado desde el código. Los textos con `+` o `${...}` llevan un valor que se completa al mostrarlos.

## Mensajes escritos en el servidor

### Catalogo (controlador)

- Error! El curso no se encuentra disponible.

### Cohorte (controlador)

- Error! Debe seleccionar el programa de la cohorte.
- Error! La cohorte no se encuentra activa.

### Curso (controlador)

- Error! El usuario no tiene el rol de docente.
- Error! El curso se guardó, pero no se pudo guardar la imagen. Modifique el curso para cargarla.

### DocenteCurso (controlador)

- Error! El usuario no tiene el rol de alumno.

### Inscripcion (controlador)

- Error! El usuario no tiene el rol de alumno.

### Pago (controlador)

- Error! La inscripción no se encuentra disponible.
- Error! Esta inscripción no tiene un pago pendiente.
- Error! Debe seleccionar la inscripción.

### PanelCurso (controlador)

- Error! El curso todavía no tiene programas: registrá uno para poder cargar unidades.

### ManejadorErroresConsejo

- Error! Esa dirección no se puede abrir directamente.
- Error! La operación solicitada no está permitida.

### Categoria

- Error! El nombre de la categoría es obligatorio.
- Error! Ya existe una categoría activa con el nombre '{valor}'.
- Error! La categoría no se encuentra activa.
- Error! La categoría tiene inscripciones activas asociadas y no puede modificarse.
- Error! El nombre de la categoría no puede quedar vacío.
- Error! Ya existe una categoría dada de baja con el nombre '{nombre}'.
- Error! El nombre coincide con el de otra categoría activa.
- Error! Debe seleccionar al menos un registro.
- Error! No se dio de baja ningún registro. {motivo de la que falló}

### Certificado

- Error! El certificado no se encuentra vigente o su inscripción está dada de baja.
- Error! El nombre y el DNI del alumno son obligatorios.

### Cohorte

- Error! El programa no se encuentra activo.
- Error! El programa debe tener al menos {N} unidad/unidades con material publicado en su cronograma (tiene {M}).
- Error! La cohorte no se encuentra activa.
- Error! La cohorte tiene inscripciones activas asociadas y no puede modificarse.
- Error! El docente no participa en el curso de la cohorte.
- Error! Debe seleccionar al menos un registro.
- Error! No se dio de baja ningún registro. {motivo de la que falló}
- Error! Faltan completar los campos obligatorios: {lista de campos}.
- Error! La fecha de fin de inscripción debe ser posterior a la fecha de inicio.
- Error! La fecha de fin de dictado debe ser posterior a la fecha de inicio.
- Error! La fecha de inicio de dictado no puede ser anterior a la fecha de fin de inscripción.
- Error! Las semanas de acceso ({valor}) no pueden ser menores a la duración total del cronograma del programa ({valor} semanas).

### ConsultaForo

- Error! El usuario no tiene el rol de alumno.
- Error! La unidad no se encuentra activa.
- Error! No tenés una inscripción vigente con esta unidad habilitada.
- Error! La consulta no se encuentra activa.
- Error! Solo podés modificar tus propias consultas.
- Error! La consulta ya no puede modificarse: pasó el plazo de edición de {valor} minutos.
- Error! Debe completar el texto de la consulta.

### Curso

- Error! No participás en este curso.
- Error! Debe seleccionar un curso.
- Error! El curso no se encuentra activo.
- Error! Ya existe un curso con el nombre '{valor}'.
- Error! El curso tiene inscripciones activas: solo pueden modificarse el precio, el equipo docente y la imagen de portada.
- Error! Debe seleccionar al menos un registro.
- Error! No se dio de baja ningún registro. {motivo de la que falló}
- Error! Faltan completar los campos obligatorios: {lista de campos}.
- Error! La categoría seleccionada no se encuentra activa.
- Error! El nivel seleccionado no existe.
- Error! El docente seleccionado no existe.
- Error! El docente {valor} no se encuentra activo o habilitado.
- Error! El docente titular no puede ser también ayudante del curso.
- Error! El docente {valor} tiene clases y/o material activo en el curso. Dé de baja primero sus clases y materiales para poder desvincularlo.
- Error! No puede quitarse la modalidad '{valor}' porque el curso tiene clases activas de esa modalidad.
- Error! La modalidad seleccionada no existe.

### Descuento

- Error! Ya existe un descuento con el nombre '{valor}'.
- Error! El descuento se encuentra dado de baja.
- Error! La cantidad límite no puede ser menor a la cantidad ya usada ({valor}).
- Error! Ya existe un descuento dado de baja con el nombre '{nombre}'.
- Error! El nombre coincide con el de otro descuento.
- Error! Debe seleccionar al menos un registro.
- Error! No se dio de baja ningún registro. {motivo de la que falló}
- Error! Faltan completar los campos obligatorios: {lista de campos}.
- Error! La vigencia hasta debe ser posterior a la vigencia desde.

### Inscripcion

- Error! No tenés una inscripción vigente a este curso.
- Error! Tu inscripción a este curso no está habilitada.
- Error! Tu acceso a este curso venció el {valor}.
- Error! El dictado de tu cohorte comienza el {valor}.
- Error! Solo los alumnos pueden inscribirse a un curso.
- Error! Debe aceptar los términos y condiciones de las políticas de privacidad para inscribirse.
- Error! Debe seleccionar el alumno.
- Error! El alumno no se encuentra activo.
- Error! Debe confirmar que el alumno acepta los términos y condiciones de las políticas de privacidad.
- Error! La inscripción no se encuentra activa.
- Error! La fecha de vencimiento del acceso es obligatoria.
- Error! La fecha de vencimiento del acceso no puede ser anterior al {valor}, que resulta de la duración del cronograma del programa.
- Error! La cohorte alcanzó su cupo máximo.
- Error! La inscripción no te pertenece.
- Error! Debe seleccionar al menos una inscripción.
- Error! No se dio de baja ninguna inscripción. {motivo de la que falló}
- Error! No se puede habilitar la inscripción: los pagos aprobados no llegan al precio del curso. Registrá o acreditá el pago primero.
- Error! No se puede habilitar la inscripción: no tiene ningún pago aprobado. Registrá o acreditá el pago primero.
- Error! Debe seleccionar la cohorte.
- Error! La cohorte no se encuentra disponible.
- Error! La inscripción a esta cohorte ya no está habilitada.
- Error! El alumno ya tiene una inscripción vigente a esta cohorte.

### Material

- Error! La unidad no se encuentra activa.
- Error! Debe seleccionar un tipo de material de la lista.
- Error! El material no se encuentra activo.
- Error! Este tipo de material no se puede modificar manualmente.
- Error! Debe seleccionar al menos un material.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! Debe completar el título.
- Error! Este tipo de material no admite un enlace externo.
- Error! Indique un archivo o un enlace externo, no ambos.
- Error! Debe seleccionar el archivo de video.
- Error! Debe seleccionar el archivo de la presentación.
- Error! Debe seleccionar un archivo o ingresar un enlace externo.

### Pago

- Error! El pago no se encuentra disponible.
- Error! La inscripción no se encuentra activa.
- Error! No tenés una inscripción pendiente de pago para este curso.
- Error! El pago de esta inscripción ya está registrado.
- Error! Debe completar el monto y el número de comprobante de la transferencia.
- Error! El pago no se encuentra rechazado o su inscripción ya tiene el acceso habilitado.
- Error! El pago no se encuentra pendiente o rechazado.
- Error! Debe marcar el pago como aprobado o rechazado.
- Error! El monto es obligatorio.
- Error! Debe completar el código de referencia y el número de comprobante de MODO para acreditar el pago.
- Error! Debe completar el número de comprobante de la transferencia.
- Error! La cohorte alcanzó su cupo máximo: no se puede pagar esta inscripción.
- Error! El pago de esta inscripción ya está aprobado.
- Error! La inscripción ya tiene un pago aprobado que no cubre el precio: el resto lo acredita el administrador.
- Error! La inscripción no tiene un pago pendiente.
- Error! El monto a pagar (${valor}) es menor al mínimo que admite el medio de pago (${valor}).
- Error! El monto debe ser mayor a cero.
- Error! Ya existe un pago registrado con ese código de referencia.
- Error! Ya existe un pago registrado con ese número de comprobante.
- Error! El monto no puede superar lo que debe la inscripción (${valor}).

### Parametro

- Error! La clave del parámetro es obligatoria.
- Error! El valor del parámetro «{valor}» es obligatorio.

### PasarelaPagoModo

- Error! El pago online no está disponible en este momento: falta configurar el medio de pago.
- Error! No se pudo iniciar el pago con MODO. Intentá nuevamente en unos minutos.

### Programa

- Error! El curso no se encuentra activo.
- Error! Debe seleccionar un programa de origen de la lista.
- Error! El programa tiene inscripciones activas: sus datos no pueden modificarse porque son los que el alumno vio y pagó al inscribirse.
- Error! Debe seleccionar al menos un programa.
- Error! El programa no se encuentra activo.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! Debe completar el nombre, los objetivos y la bibliografía.

### Progreso

- Error! La inscripción seleccionada no existe.

### RespuestaForo

- Error! La consulta no se encuentra activa.
- Error! Solo podés modificar tus propias respuestas.
- Error! La respuesta ya no puede modificarse: pasó el plazo de edición de {valor} minutos.
- Error! La respuesta no se encuentra activa.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! Debe completar el texto de la respuesta.

### TerminoGlosario

- Error! La unidad no se encuentra activa.
- Error! El término ya está registrado en el glosario de esta unidad.
- Error! El término no se encuentra activo.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! Debe completar el término y la definición.

### Unidad

- Error! La unidad que se quiere incorporar no se encuentra activa en este curso.
- Error! La unidad ya forma parte del cronograma de este programa.
- Error! La unidad no forma parte del cronograma de ningún otro programa del curso.
- Error! La unidad no se encuentra activa.
- Error! La unidad tiene inscripciones activas asociadas: no puede modificarse porque es parte de lo que el alumno vio y pagó al inscribirse.
- Error! Debe seleccionar al menos una unidad.
- Error! La unidad '{valor}' no pertenece al cronograma de este programa.
- Error! El programa no se encuentra activo.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! Debe completar el título y el contenido.
- Error! Ya existe una unidad con ese título en el curso.

### UnidadCronograma

- Error! El programa no se encuentra activo.
- Error! El usuario no tiene el rol de docente.
- Error! No participás en este curso.
- Error! El programa tiene inscripciones activas: el cronograma no puede modificarse porque es lo que el alumno vio y pagó al inscribirse.
- Error! Una de las unidades no se encuentra activa en este curso.
- Error! La unidad '{valor}' no se puede incorporar: no forma parte del cronograma de ningún programa del curso.

### Usuario

- Error! El usuario no se encuentra activo.
- Error! El usuario no tiene el rol seleccionado.

### ManejadorArchivosUtilidad

- Error! El archivo seleccionado está vacío.
- Error! No se pudo leer el archivo. Intente nuevamente.
- Error! No se pudo guardar el archivo. Intente nuevamente.
- Error! La imagen seleccionada está vacía.
- Error! No se pudo leer la imagen. Intente nuevamente.
- Error! No se pudo guardar la imagen. Intente nuevamente.

### MensajeBajaUtilidad

- Error! No se puede dar de baja {valor} «{valor}»: {valor}.

## Mensajes de formato, largo y tamaño (MensajeValidacionUtilidad)

Forma: `Error! <Sujeto> no es válido. Formato válido: <formato>.`, `... es demasiado largo. Máximo: N unidad.`, `... es demasiado grande. Máximo: N unidad.`

### ManejadorErroresConsejo

- formatoInvalido: "El cupo máximo" -> "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO
- formatoInvalido: "Las semanas de acceso" -> "número entero de 1 a " + Cohorte.SEMANAS_ACCESO_MAXIMAS
- formatoInvalido: "El valor ingresado en «{campo}»" -> número entero / número / fecha aaaa-mm-dd
- demasiadoGrande: "El archivo" -> 500, "MB"

### Certificado

- demasiadoLargo: "El nombre" -> (límite del campo), "caracteres"
- formatoInvalido: "El DNI" -> "solo números, 7 u 8 dígitos"

### Cohorte

- formatoInvalido: "El cupo máximo" -> "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO
- formatoInvalido: "Las semanas de acceso" -> "número entero de 1 a " + Cohorte.SEMANAS_ACCESO_MAXIMAS

### ConsultaForo

- demasiadoLargo: "El texto" -> ConsultaForo.LONGITUD_TEXTO, "caracteres"

### Curso

- formatoInvalido: "El precio" -> "número desde 0 hasta $" + String.format("%,.0f", Curso.PRECIO_MAXIMO

### Descuento

- demasiadoLargo: "El nombre" -> (límite del campo), "caracteres"
- formatoInvalido: "El porcentaje" -> "número de 1 a 99"
- formatoInvalido: "La cantidad límite" -> "número entero mayor a cero"
- formatoInvalido: "La cantidad de cursos requeridos" -> "número entero mayor o igual a cero"

### Inscripcion

- demasiadoLargo: "Las observaciones" -> LONGITUD_OBSERVACIONES, "caracteres"
- demasiadoLargo: "El motivo" -> LONGITUD_MOTIVO, "caracteres"

### Material

- demasiadoLargo: "El título" -> 50, "caracteres"
- demasiadoLargo: "El autor" -> 50, "caracteres"
- demasiadoLargo: "El enlace" -> 500, "caracteres"
- formatoInvalido: "El enlace" -> "comienza con http:// o https://"

### Pago

- demasiadoLargo: "Las observaciones" -> LONGITUD_OBSERVACIONES, "caracteres"
- formatoInvalido: "El nombre o el DNI del pagador" -> "nombre completo y DNI de 7 u 8 números"
- formatoInvalido: "El código de referencia" -> "letras y números, sin guiones, de 6 a 12 caracteres"
- formatoInvalido: "El número de comprobante" -> "solo números, de 4 a 100 dígitos"
- formatoInvalido: "El número de comprobante" -> "letras, números y guiones, de 4 a 100 caracteres"

### Programa

- demasiadoLargo: "El nombre" -> Programa.(límite del campo), "caracteres"
- demasiadoLargo: "La descripción" -> Programa.LONGITUD_DESCRIPCION, "caracteres"
- formatoInvalido: "La carga horaria" -> "número entero mayor a cero"

### RespuestaForo

- demasiadoLargo: "El texto" -> RespuestaForo.LONGITUD_TEXTO, "caracteres"

### TerminoGlosario

- demasiadoLargo: "El término" -> TerminoGlosario.LONGITUD_TERMINO, "caracteres"
- demasiadoLargo: "La definición" -> TerminoGlosario.LONGITUD_DEFINICION, "caracteres"

### Unidad

- demasiadoLargo: "El título" -> Unidad.LONGITUD_TITULO, "caracteres"
- demasiadoLargo: "La descripción" -> Unidad.LONGITUD_DESCRIPCION, "caracteres"

### UnidadCronograma

- formatoInvalido: "Los datos del cronograma" -> "una duración entera en semanas para cada unidad del programa"
- demasiadoLargo: "El cronograma" -> maximo, "semanas"
- formatoInvalido: "La duración de cada unidad" -> "número entero de semanas mayor a cero"

### ManejadorArchivosUtilidad

- demasiadoGrande: "El archivo" -> tamanioMaximo / (1024 * 1024
- formatoInvalido: "El archivo" -> formatos
- demasiadoGrande: "La imagen" -> 2, "MB"
- formatoInvalido: "La imagen" -> "JPG, PNG o WebP"


## Mensajes que muestra el navegador (debajo de cada campo)

- Este campo es obligatorio. / Debe seleccionar al menos una opción. / Debe seleccionar un archivo.
- Es demasiado largo. Máximo: {N} caracteres.
- No es válido. Formato válido: número.
- No es válido. Formato válido: número mayor o igual a {mínimo}.
- No es válido. Formato válido: número menor o igual a {máximo}.
- El archivo no es válido. Formato válido: {formatos}. / El archivo es demasiado grande. Máximo: {N} MB.
- La imagen no es válida. Formato válido: JPG, PNG o WebP. / La imagen es demasiado grande. Máximo: 2 MB.
- Debe seleccionar un archivo o ingresar un enlace externo. / Indique un archivo o un enlace externo, no ambos.
- El enlace no es válido. Formato válido: comienza con http:// o https://.
- Error! El cronograma es demasiado largo. Máximo: {N} semanas. (cronograma)
- Error! No se pudo conectar con el servidor. Intente nuevamente. / Error! Ocurrió un error inesperado. Intente nuevamente. (cualquier envío)

## Otros mensajes del servidor

- El valor ingresado en «{campo}» no es válido. Formato válido: número entero / número / fecha con el formato aaaa-mm-dd. (valor que no se puede convertir)
- Error! El archivo es demasiado grande. Máximo: 500 MB. (subida que supera el límite del servidor)
- El monto a pagar cambió: el descuento que se aplicaba ya no está disponible. Revisá la caja antes de pagar. (pago online, código 409)
