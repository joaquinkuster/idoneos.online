const fs = require('fs');
const path = require('path');

const mdPath = path.join(__dirname, '../docs/diseño/Casos de Uso Reales.md');
let md = fs.readFileSync(mdPath, 'utf8');

const replacements = [
  // CU-12: Registrar cohorte
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa mediante el botón "+ Nueva Cohorte" [A] (ver CU-15: Buscar programa). |
| 2 | El sistema solicita: fecha de inicio y fin de inscripción, cupo máximo (opcional), semanas de acceso al contenido desde la inscripción y, si la modalidad del curso incluye clases en vivo, fecha de inicio y fin de dictado. |
| 3 | El actor ingresa los datos solicitados mediante el formulario de cohorte [B], define fechas y cupos [C] y confirma mediante el botón "Guardar Cohorte" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa mediante el botón "+ Nueva Cohorte" [A] (ver CU-15: Buscar programa). |
| 2 | El sistema solicita los datos de la cohorte: nombre [B], fecha de inicio de inscripción [C], fecha de fin de inscripción [D], cupo máximo de alumnos [E], semanas de acceso al contenido [F] y, si incluye modalidad en vivo, fecha de inicio [G] y fecha de fin de dictado [H]. |
| 3 | El actor completa los campos del formulario y confirma el alta mediante el botón "Guardar Cohorte" [I]. |`
  },
  // CU-13: Modificar cohorte
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la cohorte a modificar mediante el botón "Editar" [A] (ver CU-11: Buscar cohorte). |
| 2 | El sistema muestra los datos actuales de la cohorte. |
| 3 | El actor modifica los campos mediante el formulario [B] y confirma los cambios mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la cohorte a modificar mediante el botón "Editar Cohorte" [A] (ver CU-11: Buscar cohorte). |
| 2 | El sistema muestra los datos actuales en el formulario de edición: fecha de inicio de inscripción [B], fecha de fin de inscripción [C], cupo máximo [D], semanas de acceso [E] y fechas de dictado en vivo [F]. |
| 3 | El actor modifica los parámetros habilitados y confirma la actualización mediante el botón "Guardar Cambios" [G]. |`
  },
  // CU-16: Registrar programa
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso mediante el botón "+ Nuevo Programa" [A] (ver CU-01: Buscar curso). |
| 2 | El sistema le ofrece iniciar de la información de un programa anterior del curso, si existe alguno, y solicita: nombre, descripción (opcional), objetivos, carga horaria total (opcional) y bibliografía. |
| 3 | El actor opcionalmente selecciona un programa anterior del cual partir mediante el desplegable [B], ingresa los datos en el formulario [C] y confirma mediante el botón "Guardar Programa" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el curso mediante el botón "+ Nuevo Programa" [A] (ver CU-01: Buscar curso). |
| 2 | El sistema permite seleccionar opcionalmente un programa base [B] y solicita: nombre del programa [C], descripción académica [D], objetivos formativos [E], carga horaria total en horas [F] y bibliografía obligatoria [G]. |
| 3 | El actor completa los campos solicitados y confirma el registro mediante el botón "Guardar Programa" [H]. |`
  },
  // CU-17: Modificar programa
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a modificar mediante el botón "Editar Programa" [A] (ver CU-15: Buscar programa). |
| 2 | El sistema muestra los datos actuales del programa. |
| 3 | El actor modifica los datos mediante el formulario de edición [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa a modificar mediante el botón "Editar Programa" [A] (ver CU-15: Buscar programa). |
| 2 | El sistema muestra los datos actuales en el formulario: nombre [B], descripción [C], objetivos [D], carga horaria [E] y bibliografía [F]. |
| 3 | El actor modifica los campos habilitados y confirma la actualización mediante el botón "Guardar Cambios" [G]. |`
  },
  // CU-20: Agregar unidad
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el programa mediante el botón "+ Añadir secciones" [A] (ver CU-15: Buscar programa). |
| 2 | El sistema solicita si desea crear una unidad nueva o incorporar una ya existente en otro programa del mismo curso y, en este último caso, recupera y lista las unidades de otros programas del curso que todavía no formen parte del cronograma del programa vigente. |
| 3 | El actor ingresa el título y descripción en el modal [B], o selecciona la unidad reutilizable [C], y confirma mediante el botón "Agregar Unidad" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita añadir una sección mediante el botón "+ Añadir secciones" [A] (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos de la unidad temática: título de la sección [B], descripción de objetivos [C] y contenido temático [D], o permite seleccionar una unidad existente [E]. |
| 3 | El actor ingresa los datos de la unidad y confirma la creación mediante el botón "Agregar Unidad" [F]. |`
  },
  // CU-21: Modificar unidad
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la unidad a modificar mediante el icono del lápiz o menú "Editar" [A] (ver CU-19: Buscar unidad). |
| 2 | El sistema muestra los datos actuales de la unidad. |
| 3 | El actor modifica el título, la descripción o el contenido en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la unidad a modificar mediante el menú "Editar" [A] (ver CU-19: Buscar unidad). |
| 2 | El sistema muestra los datos actuales de la unidad en el formulario: título [B], descripción [C] y contenido temático [D]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [E]. |`
  },
  // CU-28: Registrar material
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita el tipo de material a subir (archivo / texto enriquecido / enlace externo / video) y sus datos correspondientes: título, archivo o contenido, y visibilidad (visible / oculto a los alumnos). |
| 3 | El actor selecciona "Recurso / Material de Estudio" [A], completa los campos [B], adjunta el archivo [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: tipo de material [B], título del recurso [C], archivo o documento adjunto [D] mediante el botón "Examinar..." [E], y estado de visibilidad [F]. |
| 3 | El actor completa los campos solicitados y confirma mediante el botón "Agregar" [G]. |`
  },
  // CU-29: Modificar material
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a modificar mediante el menú "Editar" [A] (ver CU-27: Buscar material). |
| 2 | El sistema muestra los datos actuales del material. |
| 3 | El actor modifica el título, el archivo o el estado de visibilidad mediante el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el material a modificar mediante el menú "Editar" [A] (ver CU-27: Buscar material). |
| 2 | El sistema muestra los datos actuales en el formulario: título [B], archivo o documento adjunto [C] mediante "Examinar..." [D], y estado de visibilidad [E]. |
| 3 | El actor modifica los datos y confirma la actualización mediante el botón "Guardar Cambios" [F]. |`
  },
  // CU-32: Registrar término de glosario
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: término y definición. |
| 3 | El actor selecciona "Glosario de Términos" [A], ingresa el concepto [B], su definición [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos del término: concepto / término [B] y definición técnica [C]. |
| 3 | El actor completa el concepto y su definición y confirma mediante el botón "Agregar" [D]. |`
  },
  // CU-33: Modificar término de glosario
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el término a modificar mediante el botón "Editar" [A] (ver CU-31: Buscar término de glosario). |
| 2 | El sistema muestra los datos actuales del término. |
| 3 | El actor modifica el término o la definición en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el término a modificar mediante el botón "Editar" [A] (ver CU-31: Buscar término de glosario). |
| 2 | El sistema muestra los datos actuales en el formulario: concepto / término [B] y definición técnica [C]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [D]. |`
  },
  // CU-36: Registrar consulta de foro
  {
    target: `| 1 | El caso de uso inicia cuando el alumno solicita hacer una pregunta mediante el botón "+ Nueva Consulta" [A] en el foro de la unidad. |
| 2 | El sistema valida que el alumno posea una inscripción vigente al curso, que la unidad se encuentre activa y habilitada según su avance secuencial. |
| 3 | El sistema solicita el texto de la consulta. |
| 4 | El actor ingresa el título [B], redacta la consulta en el editor [C] y confirma la publicación mediante el botón "Publicar Consulta" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el alumno solicita hacer una pregunta mediante el botón "+ Nueva Consulta" [A] en el foro de la unidad. |
| 2 | El sistema valida que el alumno posea una inscripción vigente al curso, que la unidad se encuentre activa y habilitada según su avance secuencial. |
| 3 | El sistema solicita los datos de la consulta: asunto / título [B] y texto del mensaje [C]. |
| 4 | El actor ingresa el asunto y redacta el mensaje en el editor y confirma la publicación mediante el botón "Publicar Consulta" [D]. |`
  },
  // CU-37: Modificar consulta de foro
  {
    target: `| 1 | El caso de uso inicia cuando el alumno busca y selecciona su consulta mediante el botón "Editar Mensaje" [A] (ver CU-35: Buscar foro). |
| 2 | El sistema muestra el texto actual de la consulta. |
| 3 | El alumno modifica el texto en el editor [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el alumno busca y selecciona su consulta mediante el botón "Editar Mensaje" [A] (ver CU-35: Buscar foro). |
| 2 | El sistema muestra los datos actuales: asunto [B] y texto del mensaje [C]. |
| 3 | El alumno modifica el texto en el editor y confirma mediante el botón "Guardar Cambios" [D]. |`
  },
  // CU-40: Registrar respuesta de foro
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona la consulta y presiona el botón "Responder" [A]. |
| 2 | El sistema valida que el actor posea una inscripción vigente al curso o sea docente del curso, que la unidad esté activa y habilitada. |
| 3 | El sistema solicita el texto de la respuesta. |
| 4 | El actor redacta la respuesta en el cuadro de texto [B] y confirma mediante el botón "Publicar Respuesta" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona la consulta y presiona el botón "Responder" [A]. |
| 2 | El sistema valida que el actor posea una inscripción vigente al curso o sea docente del curso, que la unidad esté activa y habilitada. |
| 3 | El sistema solicita el texto de la respuesta en el editor de contenido [B]. |
| 4 | El actor redacta la respuesta y confirma la publicación mediante el botón "Publicar Respuesta" [C]. |`
  },
  // CU-41: Modificar respuesta de foro
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona su respuesta mediante el botón "Editar Respuesta" [A] (ver CU-35: Buscar foro). |
| 2 | El sistema muestra el texto actual de la respuesta. |
| 3 | El actor modifica el texto en el editor [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona su respuesta mediante el botón "Editar Respuesta" [A] (ver CU-35: Buscar foro). |
| 2 | El sistema muestra el texto actual de la respuesta en el editor [B]. |
| 3 | El actor modifica el texto y confirma mediante el botón "Guardar Cambios" [C]. |`
  },
  // CU-50: Registrar descuento
  {
    target: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento mediante el botón "+ Nuevo Descuento" [A]. |
| 2 | El sistema solicita: código de cupón, porcentaje de descuento (número entre 1 y 100), fecha de inicio y fecha de fin de vigencia. |
| 3 | El actor ingresa los datos solicitados en el formulario [B], define el porcentaje [C] y confirma mediante el botón "Guardar Descuento" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento mediante el botón "+ Nuevo Descuento" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema solicita los datos del descuento: código de cupón / beca [B], porcentaje de descuento [C], fecha de inicio de vigencia [D] y fecha de fin de vigencia [E]. |
| 3 | El actor completa los campos del formulario y confirma mediante el botón "Guardar Descuento" [F]. |`
  },
  // CU-51: Modificar descuento
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar mediante el botón "Editar" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema muestra los datos actuales del descuento. |
| 3 | El actor modifica los datos en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar mediante el botón "Editar" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema muestra los datos actuales en el formulario: código de cupón [B], porcentaje de descuento [C], fecha de inicio [D] y fecha de fin [E]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [F]. |`
  },
  // CU-54: Registrar pool de preguntas
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: nombre y descripción (opcional). |
| 3 | El actor selecciona "Pool de Preguntas" [A], ingresa el nombre [B], su descripción [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos del banco de preguntas: nombre del pool [B] y descripción temática [C]. |
| 3 | El actor completa los datos del pool y confirma mediante el botón "Agregar" [D]. |`
  },
  // CU-55: Modificar pool de preguntas
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar mediante el botón "Editar Pool" [A] (ver CU-53: Buscar pool). |
| 2 | El sistema muestra los datos actuales del pool. |
| 3 | El actor modifica el nombre o la descripción en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar mediante el botón "Editar Pool" [A] (ver CU-53: Buscar pool). |
| 2 | El sistema muestra los datos actuales en el formulario: nombre del pool [B] y descripción temática [C]. |
| 3 | El actor actualiza los campos y confirma mediante el botón "Guardar Cambios" [D]. |`
  },
  // CU-58: Registrar autoevaluación
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre (opcional), cantidad de intentos permitidos (opcional; si se deja vacía, no hay límite), y el o los pools de preguntas a asociar, el de la propia unidad y, si es la última unidad del programa, opcionalmente los de otras unidades, para conformar la evaluación final del curso. |
| 3 | El actor selecciona "Cuestionario / Autoevaluación" [A], completa el título y parámetros [B], selecciona el pool asociado [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los parámetros de la autoevaluación: nombre del cuestionario [B], tiempo límite en minutos [C], cantidad de preguntas sorteables [D], fecha de apertura [E], fecha de cierre [F], cantidad máxima de intentos permitidos [G], y los pools de preguntas asociados [H]. |
| 3 | El actor completa los parámetros solicitados y confirma mediante el botón "Agregar" [I]. |`
  },
  // CU-59: Modificar autoevaluación
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar mediante el botón "Editar Cuestionario" [A] (ver CU-57: Buscar autoevaluación). |
| 2 | El sistema muestra los datos actuales de la autoevaluación y, si esta ya registra intentos, indica que su contenido queda protegido y solo puede extenderse la fecha de cierre, ocultarse/mostrarse o ampliarse la cantidad de intentos permitidos. |
| 3 | El actor modifica los datos habilitados en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar mediante el botón "Editar Cuestionario" [A] (ver CU-57: Buscar autoevaluación). |
| 2 | El sistema muestra los datos actuales en el formulario de edición: nombre del cuestionario [B], tiempo límite [C], cantidad de preguntas [D], fecha de apertura [E], fecha de cierre [F], intentos permitidos [G], y pools asociados [H]. |
| 3 | El actor modifica los campos habilitados y confirma la actualización mediante el botón "Guardar Cambios" [I]. |`
  },
  // CU-66: Registrar clase en vivo
  {
    target: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: título, cohorte destinataria, fecha y hora de inicio, duración estimada en minutos, y enlace a la sala de videoconferencia (Meet, Zoom, etc.). |
| 3 | El actor selecciona "Clase en Vivo (Streaming)" [A], completa los campos [B], define la fecha y enlace [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos de la sesión en vivo: título de la clase [B], cohorte destinataria [C], fecha y hora de inicio [D], duración estimada en minutos [E], y enlace a la sala de videoconferencia [F]. |
| 3 | El actor completa los datos de la sesión y confirma la programación mediante el botón "Agregar" [G]. |`
  },
  // CU-67: Modificar clase en vivo
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase programada mediante el botón "Reprogramar / Editar" [A] (ver CU-65: Buscar clase en vivo). |
| 2 | El sistema valida que el docente participe en el curso como titular o ayudante, que la clase esté activa, que haya sido registrada por el actor, y que se encuentre programada. |
| 3 | El sistema muestra los datos actuales de la clase. |
| 4 | El actor modifica los datos en el formulario [B] y confirma la reprogramación mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase programada mediante el botón "Reprogramar / Editar" [A] (ver CU-65: Buscar clase en vivo). |
| 2 | El sistema valida que el docente participe en el curso como titular o ayudante, que la clase esté activa, que haya sido registrada por el actor, y que se encuentre programada. |
| 3 | El sistema muestra los datos actuales en el formulario: título de la clase [B], fecha y hora [C], duración estimada [D] y enlace a la sala [E]. |
| 4 | El actor reprograma los datos y confirma mediante el botón "Guardar Cambios" [F]. |`
  },
  // CU-78: Generar clase con clon
  {
    target: `| 1 | El caso de uso inicia cuando el actor solicita generar una clase sintética mediante el botón "Generar Nueva Clase con Clon" [A]. |
| 2 | El sistema solicita: título, guión (texto que dirá el avatar), unidad a la que pertenece, avatar del clon a utilizar y voz sintética configurada. |
| 3 | El actor ingresa el título [B], redacta el guión [C], selecciona el avatar y voz [D] y confirma mediante el botón "Generar Video en HeyGen" [E]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita generar una clase sintética mediante el botón "Generar Nueva Clase con Clon" [A] (ver CU-77: Buscar clase con clon). |
| 2 | El sistema solicita los parámetros de generación: título de la clase [B], unidad temática [C], guión del video [D], avatar hiperrealista HeyGen [E] y voz sintética [F]. |
| 3 | El actor completa los parámetros y confirma la renderización mediante el botón "Generar Video en HeyGen" [G]. |`
  },
  // CU-79: Modificar clase con clon
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase a modificar mediante el botón "Editar Guión" [A] (ver CU-77: Buscar clase con clon). |
| 2 | El sistema muestra los datos actuales del video generado. |
| 3 | El actor modifica el título o el guión en el formulario [B] y confirma la regeneración mediante el botón "Regenerar Video" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la clase a modificar mediante el botón "Editar Guión" [A] (ver CU-77: Buscar clase con clon). |
| 2 | El sistema muestra los datos actuales en el editor: título de la clase [B], guión textual [C], avatar [D] y voz sintética [E]. |
| 3 | El actor actualiza el guión y confirma mediante el botón "Regenerar Video" [F]. |`
  },
  // CU-83: Registrar usuario
  {
    target: `| 1 | El caso de uso inicia cuando el actor solicita registrar manualmente un usuario mediante el botón "+ Nuevo Usuario" [A]. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI, contraseña, teléfono (opcional) y el rol a asignar (Alumno, Docente o Administrador). |
| 3 | El actor ingresa los datos solicitados en el formulario [B], asigna el rol correspondiente [C] y confirma mediante el botón "Guardar Usuario" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita registrar manualmente un usuario mediante el botón "+ Nuevo Usuario" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema solicita los datos de la cuenta: nombre [B], apellido [C], correo electrónico [D], DNI [E], contraseña de acceso [F], teléfono [G] y rol del sistema [H]. |
| 3 | El actor completa los datos personales y de acceso y confirma el alta mediante el botón "Guardar Usuario" [I]. |`
  },
  // CU-84: Modificar usuario
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta del alumno a modificar mediante el botón "Editar" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema muestra los datos actuales de la cuenta. |
| 3 | El actor modifica los datos en el formulario de cuenta [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la cuenta a modificar mediante el botón "Editar Usuario" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema muestra los datos actuales en el formulario: nombre [B], apellido [C], correo electrónico [D], DNI [E], teléfono [F] y foto de perfil [G]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [H]. |`
  },
  // CU-88: Registrar docente
  {
    target: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo docente mediante el botón "+ Nuevo Docente" [A]. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI, teléfono, biografía, años de experiencia, título o títulos universitarios o de posgrado con su matrícula profesional cuando corresponda, y matrícula del Registro de Idóneos de la Comisión Nacional de Valores cuando aplique. |
| 3 | El actor ingresa los datos personales y profesionales en el formulario docente [B], adjunta títulos y matrículas [C] y confirma mediante el botón "Guardar Docente" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo docente mediante el botón "+ Nuevo Docente" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema solicita los antecedentes del docente: nombre y apellido [B], correo electrónico [C], DNI y teléfono [D], biografía profesional [E], años de experiencia [F], títulos universitarios [G] y matrícula CNV / profesional [H]. |
| 3 | El actor completa los datos profesionales y confirma mediante el botón "Guardar Docente" [I]. |`
  },
  // CU-89: Modificar docente
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el docente a modificar mediante el botón "Editar Perfil Docente" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema muestra los datos profesionales actuales del docente. |
| 3 | El actor modifica la biografía, experiencia, títulos o estado de habilitación en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el docente a modificar mediante el botón "Editar Perfil Docente" [A] (ver CU-82: Buscar usuario). |
| 2 | El sistema muestra los antecedentes actuales: biografía profesional [B], años de experiencia [C], títulos universitarios [D], matrícula CNV [E] y estado de habilitación docente [F]. |
| 3 | El actor actualiza los antecedentes habilitados y confirma mediante el botón "Guardar Cambios" [G]. |`
  },
  // CU-99: Configurar parámetros
  {
    target: `| 1 | El sistema lista los parámetros configurados: Plazo de disponibilidad de grabaciones y antelación del aviso previo; Cantidad máxima de sesiones concurrentes por usuario; Datos institucionales utilizados en el sitio, comprobantes y constancias (razón social, CUIT, domicilio, logo, email de contacto y teléfono de contacto); Credenciales de integración con Google OAuth, con la pasarela de pagos y con HeyGen; Plazo máximo de espera para la confirmación de un pago antes de registrarlo como rechazado; Proporción de tipos de pregunta en los bancos generados con IA; Cantidad máxima semanal, por docente, de clases generadas con clon y de contenidos generados con IA (bancos de preguntas, resúmenes y presentaciones); Tiempo límite de edición de consultas y respuestas del foro; Mínimo de unidades por programa antes de publicarlo (registrar cohorte). |
| 2 | El actor selecciona un parámetro existente de la tabla de configuración mediante el botón "Editar Valor" [A]. |
| 3 | El sistema valida que el valor haya sido completado. |
| 4 | El actor ingresa el nuevo valor en el formulario de parámetro [B] y confirma mediante el botón "Guardar Parámetro" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita configurar los parámetros del sistema listados en la tabla de configuración [A]. |
| 2 | El actor selecciona el parámetro a modificar mediante el botón "Editar Valor" [B]. |
| 3 | El sistema solicita el nuevo valor y muestra la descripción del impacto [C]. |
| 4 | El actor ingresa el nuevo valor del parámetro y confirma la actualización mediante el botón "Guardar Parámetro" [D]. |`
  }
];

let applied = 0;
replacements.forEach(r => {
  if (md.includes(r.target)) {
    md = md.replace(r.target, r.replacement);
    applied++;
  } else {
    console.log('Target not found for replacement');
  }
});

console.log('Applied ' + applied + ' replacements out of ' + replacements.length);
fs.writeFileSync(mdPath, md, 'utf8');
