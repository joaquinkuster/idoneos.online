# Aspectos Técnicos del Modelo de Dominio

Este documento describe el modelo de dominio del sistema de gestión para Idóneos Online (plataforma de cursos de educación financiera). Sirve como contexto de referencia para continuar la documentación del Trabajo Final: resume qué representa cada entidad, el detalle de cada uno de sus atributos y sus relaciones, junto con los criterios de diseño que se aplicaron de forma consistente en todo el modelo.

## 1. Criterios de Diseño Aplicados
- No se persiste lo que se puede derivar o calcular (por ejemplo, `Curso.precio = 0` indica gratuito; no existe un atributo `es_gratuito` separado).
- Enums de más de 2 valores se convierten en entidad catálogo (ej. `TipoMaterial`, `EstadoPago`); enums de exactamente 2 valores quedan como atributo booleano, siempre aclarando qué significa cada valor.
- Borrado lógico (`baja`) en las entidades donde tiene sentido inactivar sin perder historial (cursos, material, inscripciones, etc.), no en catálogos de sistema ni en tablas de auditoría/asociativas. En cada entidad, `baja` indica si el registro fue desactivado sin eliminarlo físicamente, preservando el historial.

## 2. Usuarios y Roles
**Usuario**: entidad base de autenticación, de la que heredan **Alumno**, **Docente** y **Administrador**.
- `nombre` (VARCHAR 50) — nombre del usuario; se muestra en la interfaz y en las comunicaciones que le llegan.
- `email` (VARCHAR 150) — dirección de correo del usuario. Funciona como identificador de login y como canal de contacto (recuperación de contraseña, envío de comprobantes, etc.).
- `imagen` (VARCHAR 150) — ruta de la imagen de perfil.
- `contraseña` (VARCHAR 255) — hash de la contraseña de acceso; queda nulo cuando el usuario se registró con Google, ya que en ese caso la autenticación la resuelve el proveedor externo.
- `google_id` (VARCHAR 255) — identificador único que devuelve Google al autenticarse por OAuth; nulo si el usuario se registró con contraseña propia. Permite asociar la cuenta a su perfil de Google sin crear un usuario duplicado.
- `email_validado` (booleano) — indica si el usuario confirmó su dirección de correo (por ejemplo, mediante un link de verificación). Se usa para restringir ciertas funciones a cuentas no verificadas.
- `dni` (VARCHAR 8) — documento de identidad. Es necesario para emitir el Certificado a nombre del alumno, y en el caso del docente es la clave para verificar sus títulos universitarios.
- `apellido` (VARCHAR 50) — apellido del usuario; junto con nombre compone su identificación completa.
- `telefono` (VARCHAR 20) — número de contacto del usuario.
- `token_recuperacion` (VARCHAR 255) — token temporal que se genera cuando el usuario pide restablecer su contraseña; se envía por email y se valida contra este campo antes de permitir el cambio.
- `expiracion_token` (fecha) — fecha y hora hasta la cual `token_recuperacion` es válido. Vencido ese plazo, el token deja de aceptarse y hay que generar uno nuevo.
- `fecha_registro` (fecha) — fecha en la que el usuario creó su cuenta.
- `baja` (booleano) — baja lógica de la cuenta.

**Alumno, Docente, Administrador**: subtipos de Usuario (relación 1 a 0 ... 1 cada uno). Se mantienen como tablas propias — aunque Alumno y Administrador no tienen atributos propios — porque permiten integridad referencial a nivel de base de datos (un FK a Alumno garantiza que este usuario es alumno).
- `anios_experiencia` (entero) — (Docente) años de experiencia profesional que declara el docente; se muestra en su perfil público como dato de trayectoria.
- `matricula_cnv` (VARCHAR 50) — (Docente) número de matrícula del Registro de Idóneos de la Comisión Nacional de Valores, para quienes rindieron este examen. Es opcional: la plataforma no forma para ese examen, es solo un dato adicional de credibilidad para quien ya lo tiene.
- `biografía` (texto largo) — (Docente) texto de presentación que se muestra públicamente en la página del curso.
- `habilitado` (booleano) — (Docente) controla si el docente puede dictar clases en este momento. Permite al administrador desactivarlo temporalmente — por ejemplo ante una suspensión por mala praxis — sin borrar su cuenta ni su historial.

**TituloDocente**: título universitario o de posgrado de un docente. Relación 1 a N con Docente, porque un docente puede tener más de un título (por ejemplo, contador y licenciado en administración).
- `titulo` (VARCHAR 100) — nombre del título (ej. "Contador Público"). Es obligatorio porque es el dato central de la entidad: no tiene sentido un registro de TituloDocente sin título.
- `matricula_colegio` (VARCHAR 50) — matrícula profesional otorgada por el colegio o consejo que regula esa profesión. Queda nula porque no todas las profesiones exigen matriculación.

**Verificación de identidad y credenciales del docente**: el alta de un docente la realiza el administrador de forma manual, no hay autoregistro. Para validar el título declarado en `TituloDocente`, el administrador consulta el Registro Público de Graduados Universitarios (SIU) usando el `dni` del usuario — es un registro oficial, gratuito y público, que no requiere que el docente suba ningún archivo escaneado. Si la persona no cuenta con título universitario, puede acreditarse en cambio mediante `matricula_cnv` o `matricula_colegio`, quedando a criterio del administrador habilitarlo. Por eso el sistema no modela un flujo de estados de verificación por credencial: el vetting es un control manual y externo, previo a la carga de datos, y `Docente.habilitado` es el único indicador que el sistema necesita persistir sobre esa decisión.

**Rol** (catálogo: Alumno | Docente | Administrador) y **Usuario Rol** (tabla asociativa N a M): se mantienen en paralelo a los subtipos anteriores por conveniencia de consulta (saber los roles de un usuario sin recorrer las tres tablas de subtipo). Es una duplicación consciente que debe mantenerse sincronizada a nivel de aplicación.
- `nombre` (VARCHAR 50) — (Rol) nombre del rol: Alumno, Docente o Administrador. Usuario Rol no tiene atributos propios más allá de las claves foráneas a Usuario y Rol.

**Sesión**: registra cada inicio de sesión de un usuario.
- `token` (VARCHAR 255) — identificador único de la sesión activa; se usa para validar que las peticiones del usuario correspondan a un login vigente.
- `fecha_inicio` (fecha) — momento en que se inició la sesión.
- `fecha_fin` (fecha) — momento en que se cerró la sesión. Nula mientras la sesión sigue activa.
- `ip` (VARCHAR 45) — dirección IP desde la que se conectó, para trazabilidad de seguridad. La longitud contempla direcciones IPv6.
- `dispositivo` (VARCHAR 255) — descripción del dispositivo o navegador usado, para que el usuario pueda reconocer y cerrar sus sesiones activas individualmente.

## 3. Cursos y Estructura de Contenido
**Categoría**: clasificación temática de los cursos.
- `nombre` (VARCHAR 50) — nombre de la categoría (ej. "Finanzas personales").
- `descripción` (VARCHAR 150) — breve descripción de qué tipo de cursos agrupa.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**Curso**: ficha comercial y catálogo. Se relaciona con Categoría (1 a N) y con Programa (1 a N); el contenido y la programación de fechas se organizan a través de sus Programas y Dictados (ver más abajo).
- `nombre` (VARCHAR 50) — nombre comercial del curso.
- `descripción` (VARCHAR 150) — descripción que se muestra en el listado y la ficha del curso.
- `precio` (decimal) — precio del curso. El valor 0 indica que es gratuito, evitando un atributo booleano redundante.
- `publicado` (booleano) — si el curso es visible públicamente en el catálogo.
- `imagen` (VARCHAR 150) — ruta de la imagen de portada usada en el listado público.
- `baja` (booleano) — baja lógica del curso.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**Programa**: versión del plan de estudios de un curso. Relación N a 1 con Curso — un mismo curso puede tener más de un programa, por ejemplo para actualizar el contenido de una edición sin afectar a los alumnos ya inscriptos en dictados de otro programa — y 1 a N con Unidad, ya que el contenido pertenece al programa.
- `nombre` (VARCHAR 50) — nombre del programa (ej. "Edición 2026").
- `descripción` (VARCHAR 150) — descripción del programa.
- `meses_acceso` (entero) — cantidad de meses que el alumno mantiene acceso al contenido desde que se inscribe a un Dictado de este programa. Se usa para calcular `Inscripcion.fecha_vencimiento_acceso`. Representa el tiempo estimado que te dan para terminar el curso, y depende de cada programa, en función de la cantidad de unidades que se dictan y que tan extensas de contenido son.
- `baja` (booleano) — baja lógica del programa.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**Dictado**: dictado de clases programado de un Programa, con su propio cronograma, cupo y equipo docente — permite que un mismo programa se ofrezca en más de una oportunidad. Relación N a 1 con Programa, 1 a N con Inscripción (el alumno se inscribe a un dictado puntual) y M a N con Docente vía Dictado Docente.
- `fecha_inicio` (fecha) — fecha en la que arranca este dictado.
- `fecha_fin` (fecha) — fecha en la que termina este dictado.
- `cupo_maximo` (entero) — cantidad máxima de alumnos que pueden inscribirse a este dictado, en caso de ser nulo, no hay límites.
- `baja` (booleano) — baja lógica del dictado.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**Dictado Docente**: tabla asociativa entre Dictado y Docente. Cada instancia tiene su propio equipo docente, por lo que una misma persona puede estar a cargo de una edición y no de otra. La regla “exactamente un titular por dictado” no está garantizada por la estructura — se valida en el código de la aplicación.
- `es_supervisor` (booleano) — distingue si, dentro de esta relación M a N, el docente participa como supervisor (false) o como titular (true) de ese dictado. Resuelve ambos roles con un único atributo en vez de dos relaciones separadas.

**Modalidad** (catálogo: En vivo | Grabada | Clon IA), relacionado con Curso vía tabla asociativa Modalidad Curso (M a N).
- `nombre` (VARCHAR 50) — nombre de la modalidad.

**Unidad**: subdivisión de un Programa; el contenido pertenece a esa versión puntual del plan de estudios. Es el nodo central del que dependen Material, TerminoGlosario, Pool, Autoevaluacion, ClaseEnVivo y ClaseClonIA — todo el contenido de una unidad cuelga directo de ella, no de Programa, para que esté disponible desde que se crea, sin depender de si ya existe contenido generado, permitiendo reutilizar el contenido en otro programa del curso.
- `título` (VARCHAR 50) — título de la unidad.
- `descripción` (VARCHAR 150) — descripción breve de lo que cubre la unidad.
- `numero_orden` (entero) — posición de la unidad dentro del curso, usada para ordenar su presentación al alumno.
- `baja` (booleano) — baja lógica de la unidad.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

## 4. Material y Contenido
**TipoMaterial** (catálogo: Grabación | Bibliografía | Presentación | Resumen).
- `nombre` (VARCHAR 50) — nombre del tipo de material.

**Material**: entidad unificada para todo archivo o contenido de lectura de una unidad, sea subido por el docente o generado por IA. Se unifica Grabación, Presentación, Resumen y Bibliografía en una sola tabla con catálogo de tipo, en vez de una tabla por cada uno.
- `título` (VARCHAR 50) — título del material.
- `fecha_carga` (fecha) — momento en que se subió o generó el material.
- `publicado` (booleano) — si el material está visible para los alumnos.
- `ruta_archivo` (VARCHAR 150) — ubicación del archivo (grabación, presentación o bibliografía), según el tipo de material.
- `contenido` (VARCHAR 500) — texto del material cuando corresponde (por ejemplo, un resumen), en vez de un archivo.
- `generado_por_ia` (booleano) — distingue si el contenido lo generó la IA o lo subió el docente.
- `duración` (entero) — duración en minutos; solo aplica a grabaciones.
- `autor` (VARCHAR 50) — autor de referencia; solo aplica a bibliografía.
- `baja` (booleano) — baja lógica del material.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**TerminoGlosario**: cuelga directo de Unidad, no de Material — es dato estructurado (por término-definición), no un archivo o contenido de lectura genérico.
- `término` (VARCHAR 50) — palabra o expresión del glosario.
- `definición` (VARCHAR 150) — explicación del término.
- `baja` (booleano) — baja lógica del término.

## 5. Clases
**EstadoClaseEnVivo** (catálogo: Programada | En vivo | Finalizada).
- `nombre` (VARCHAR 50) — nombre del estado.

**ClaseEnVivo**: clase dictada en vivo por un docente. Se relaciona directo con Unidad (obligatorio desde que se programa) y con Docente (quien dicta — puede ser titular o supervisor). La relación con Material (la grabación resultante) es opcional (0 a 1), porque se completa recién cuando termina la clase.
- `título` (VARCHAR 50) — título de la clase.
- `fecha_hora` (fecha) — fecha y hora en que está programada o se dictó la clase.
- `url_rtmp` (VARCHAR 255) — dirección del servidor de streaming a la que el docente transmite y desde la que los alumnos ven la clase en vivo.
- `clave_stream` (VARCHAR 100) — clave privada que autentica al docente frente al servidor de streaming, para que solo él pueda transmitir con esa URL.
- `baja` (booleano) — baja lógica de la clase.

**EstadoClaseClonIA** (catálogo: Pendiente | Generada | Error).
- `nombre` (VARCHAR 50) — nombre del estado.

**ClaseClonIA**: clase generada mediante un avatar con IA a partir de un guión. Mismo patrón que ClaseEnVivo: relación directa y obligatoria con Unidad y con Docente, relación opcional con Material (el video resultante). El estado (Pendiente/Generada/Error) refleja que es un proceso de generación asincrónica, no instantáneo.
- `título` (VARCHAR 50) — título de la clase.
- `guión` (texto largo) — texto que se le envía al servicio de IA (ej. HeyGen) para generar el video. Se persiste para poder reutilizar el contenido que generó cada clase.
- `fecha_generacion` (fecha) — momento en que se generó el video con IA.
- `baja` (booleano) — baja lógica de la clase.

## 6. Foro
**ConsultaForo**: pregunta o consulta de un alumno dentro de una unidad. Se decidió un foro por unidad (no uno general por curso) para mantener las consultas agrupadas por tema.
- `texto` (VARCHAR 500) — contenido de la consulta.
- `fecha` (fecha) — momento en que se realizó la consulta.
- `baja` (booleano) — baja lógica de la consulta.

**RespuestaForo**: respuesta de un docente (titular o supervisor) a una ConsultaForo.
- `texto` (VARCHAR 500) — contenido de la respuesta.
- `fecha` (fecha) — momento en que se respondió.
- `baja` (booleano) — baja lógica de la respuesta.

## 7. Evaluación
**Pool**: banco de preguntas de una unidad. Relación 1 a 0 ... 1 con Unidad.
- `nombre` (VARCHAR 50) — nombre del banco de preguntas.
- `baja` (booleano) — baja lógica del pool.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**Pregunta**:
- `texto` (VARCHAR 150) — enunciado de la pregunta.
- `es_opcion_multiple` (boolean) — true si la pregunta es de opción múltiple, false si es de verdadero/falso.
- `baja` (booleano) — baja lógica de la pregunta.

**OpcionRespuesta**:
- `texto` (VARCHAR 150) — texto de la opción de respuesta.
- `es_correcta` (booleano) — marca si esa opción es la respuesta correcta de la pregunta.
- `baja` (booleano) — baja lógica de la opción.

**Autoevaluación**: se relaciona con Pool mediante tabla asociativa Pool Autoevaluación (M a N) — una autoevaluación puede sortear preguntas de varios pools (por ejemplo, un examen final que integra todas las unidades de un curso).
- `nombre` (VARCHAR 50) — nombre de la autoevaluación.
- `tiempo_limite` (entero) — minutos que tiene el alumno para completar un intento antes de que se cierre automáticamente.
- `intentos_permitidos` (entero) — cantidad máxima de veces que un alumno puede intentar la autoevaluación, en caso de ser nulo, no hay límites.
- `fecha_apertura` (fecha) — desde cuándo la autoevaluación está disponible para rendir.
- `fecha_cierre` (fecha) — hasta cuándo está disponible; pasado este momento no se pueden iniciar nuevos intentos.
- `baja` (booleano) — baja lógica de la autoevaluación.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

**IntentoAutoevaluacion**: un intento de un alumno sobre una autoevaluación (10 preguntas sorteadas del pool).
- `fecha` (fecha) — momento en que se realizó el intento.
- `nota` (decimal) — calificación obtenida en el intento, usada para determinar si el alumno aprueba.

**RespuestaIntento**: registra la opción elegida por el alumno para cada una de las preguntas sorteadas en un intento. Sin atributos propios más allá de sus claves foráneas. Se relaciona con IntentoAutoevaluacion (obligatorio, 1 a 1..N) y con OpcionRespuesta (obligatorio, ya que el sistema exige responder las 10 preguntas para finalizar — no hace falta relación directa con Pregunta, se llega a ella vía OpcionRespuesta).
- La aprobación de una autoevaluación depende únicamente de la nota obtenida — no hay componente de asistencia.

## 8. Inscripción y Pagos
**Inscripción**: vínculo entre un alumno (Usuario) y un Dictado, es decir la instancia puntual del curso a la que se inscribió. El certificado no es una entidad aparte: sus datos se guardan como atributos de la propia inscripción, sin persistir el PDF (se genera bajo demanda).
- `fecha` (fecha) — momento en que el alumno se inscribió.
- `observaciones` (VARCHAR 500) — notas de texto libre, por ejemplo el motivo de una baja.
- `fecha_vencimiento_acceso` (fecha) — fecha calculada a partir de `fecha` + `Programa.meses_acceso` (a través del Dictado al que pertenece la inscripción); define hasta cuándo el alumno puede acceder al contenido del curso.
- `baja` (booleano) — permite registrar el abandono de un alumno aunque no haya reembolso, sin perder el historial de la inscripción.
- `numero_certificado` (VARCHAR 100) — número del certificado.
- `fecha_emision_certificado` (fecha) — momento en que se emitió el certificado.
- `certificado_enviado` (booleano) — si ya se le envió el certificado al alumno por email.

**Progreso**: registra si un alumno completó una unidad dentro de un curso. Se relaciona con Inscripción (para saber a qué alumno y curso corresponde, incluso si el curso es gratuito) y con Unidad (qué unidad puntual se completó).
- `completada` (booleano) — si el alumno terminó el contenido de esa unidad.
- `fecha_completada` (fecha) — momento en que se marcó como completada; nula mientras la unidad sigue sin completarse.

**EstadoPago** (catálogo: Pendiente | Acreditado | Rechazado), **MetodoPago** (catálogo: Tarjeta de crédito | Tarjeta de débito | Saldo de cuenta). El sistema procesa pagos con tarjeta o saldo de cuenta, a través de la API de MODO.
- `nombre` (VARCHAR 50) — nombre del estado o del método, según la entidad.

**Pago**: registra cada transacción procesada mediante la API de MODO. El comprobante tampoco es una entidad aparte: sus datos se guardan como atributos de este mismo registro, sin persistir el PDF (se genera bajo demanda).
- `monto` (decimal) — importe pagado.
- `fecha` (fecha) — momento en que se generó el registro de pago.
- `payment_request_id` (VARCHAR 50) — identificador de la solicitud de pago (Payment Request) que devuelve MODO al crearla; se usa para consultar su estado y conciliar la confirmación asincrónica (webhook) con este registro.
- `external_intention_id` (VARCHAR 50) — identificador propio que el sistema genera y envía a MODO al crear la solicitud de pago, para poder reconocer la operación incluso antes de que MODO le asigne su `payment_request_id`.
- `reference_code` (VARCHAR 20) — código de referencia de la transacción que devuelve MODO una vez procesado el pago.
- `nombre_pagador` (VARCHAR 50) — nombre del titular de la tarjeta informado por la pasarela.
- `tipo_pago` (VARCHAR 20) — si el pago fue con tarjeta de crédito, débito o saldo de cuenta.
- `ultimos_digitos_tarjeta` (VARCHAR 4) — últimos cuatro dígitos de la tarjeta, para que el alumno reconozca con qué tarjeta pagó sin exponer el número completo.
- `detalle_estado` (VARCHAR 100) — motivo puntual que devuelve la pasarela cuando el pago queda pendiente o es rechazado (ej. fondos insuficientes).
- `fecha_aprobacion` (fecha) — momento en que la pasarela confirmó el pago; puede ser posterior a `fecha` si la acreditación no es instantánea.
- `numero_comprobante` (VARCHAR 100) — número del comprobante. Es VARCHAR porque el formato incluye guiones y ceros a la izquierda.
- `fecha_emision_comprobante` (fecha) — momento en que se emitió el comprobante.
- `comprobante_enviado` (booleano) — si ya se le envió el comprobante al alumno por email.

**Descuento**:
- `nombre` (VARCHAR 50) — nombre del descuento.
- `porcentaje` (decimal) — porcentaje a descontar del precio del curso.
- `vigencia_desde` (fecha) — desde cuándo el descuento está activo.
- `vigencia_hasta` (fecha) — hasta cuándo el descuento está activo.
- `cantidad_limite` (entero) — tope de usos totales permitidos para este descuento.
- `cantidad_usada` (entero) — contador de cuántas veces ya se aplicó, para compararlo contra `cantidad_limite`.
- `cursos_requeridos` (entero) — cantidad de cursos que un alumno debe haber comprado antes para acceder a este descuento — la única condición de descuento existente, por eso no hay una entidad CondicionDescuento aparte.
- `baja` (booleano) — baja lógica del descuento.
- `fecha_creacion` (fecha) — momento en que se creó el registro.
- `ultima_modificacion` (fecha) — momento de la última actualización del registro; se actualiza automáticamente cada vez que cambia algún dato.

## 9. Sistema y Soporte
**TipoAccionAuditoria** (catálogo genérico: Crear | Modificar | Eliminar | Consultar — no valores de negocio, para poder reutilizar un interceptor genérico tipo Spring AOP).
- `nombre` (VARCHAR 50) — nombre del tipo de acción.

**Auditoría**: relacionada con Usuario (quién) y con TipoAccionAuditoria (qué tipo de acción).
- `entidad_afectada` (VARCHAR 50) — nombre de la tabla sobre la que se realizó la acción.
- `id_afectado` (entero) — identificador del registro puntual afectado dentro de esa tabla.
- `fecha_hora` (fecha) — momento en que ocurrió la acción.
- `ip_usuario` (VARCHAR 45) — dirección IP desde la que se realizó la acción, mismo criterio que `Sesion.ip` (la longitud contempla IPv6).
- `valor_anterior` (texto largo) — estado del registro antes del cambio (formato serializado, ej. JSON), para poder reconstruir qué se modificó.
- `valor_nuevo` (texto largo) — estado del registro después del cambio, en el mismo formato que `valor_anterior`.

**TipoReporte** (catálogo: Alumnos inscriptos | Ingresos), **Reporte**: generado por un Administrador.
- `nombre` (VARCHAR 50) — (TipoReporte) nombre del tipo de reporte.
- `fecha_generacion` (fecha) — (Reporte) momento en que se generó el reporte.

**Configuración**: modelo clave-valor en vez de una tabla con una columna por parámetro — más flexible para agregar configuraciones nuevas sin modificar el esquema.
- `clave` (VARCHAR 100) — nombre del parámetro de configuración.
- `valor` (texto largo) — valor asociado a esa clave. Se guarda como texto para poder representar cualquier tipo de dato sin cambiar el esquema.