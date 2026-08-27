const fs = require('fs');
const path = require('path');

const mdPath = path.join(__dirname, '../docs/diseño/Casos de Uso Reales.md');
let md = fs.readFileSync(mdPath, 'utf8');

const replacements = [
  // CU-09: Modificar categoría
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar mediante el botón "Editar" [A] (ver CU-07: Buscar categoría). |
| 2 | El sistema muestra los datos actuales de la categoría. |
| 3 | El actor modifica el nombre o la descripción mediante el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar mediante el botón "Editar" [A] (ver CU-07: Buscar categoría). |
| 2 | El sistema muestra los datos actuales en el formulario: nombre de la categoría [B] y descripción temática [C]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [D]. |`
  },
  // CU-50: Registrar descuento
  {
    target: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento mediante el botón "+ Nuevo Descuento" [A]. |
| 2 | El sistema solicita: nombre, porcentaje, vigencia desde, vigencia hasta, cantidad límite ofertada y, opcionalmente, la cantidad de cursos que el alumno debe haber comprado como condición. |
| 3 | El actor ingresa los datos solicitados en el formulario [B], define el porcentaje y vigencia [C] y confirma mediante el botón "Guardar Descuento" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor solicita registrar un nuevo descuento mediante el botón "+ Nuevo Descuento" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema solicita los parámetros del beneficio: nombre del descuento [B], porcentaje aplicable [C], vigencia desde [D], vigencia hasta [E], cantidad límite de usos [F] y cantidad de cursos requeridos [G]. |
| 3 | El actor completa los campos solicitados y confirma el registro mediante el botón "Guardar Descuento" [H]. |`
  },
  // CU-51: Modificar descuento
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar mediante el botón "Editar" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema muestra los datos actuales del descuento. |
| 3 | El actor modifica los datos que desea en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar mediante el botón "Editar" [A] (ver CU-49: Buscar descuento). |
| 2 | El sistema muestra los datos actuales en el formulario: nombre [B], porcentaje [C], vigencia desde [D], vigencia hasta [E], cantidad límite [F] y cursos requeridos [G]. |
| 3 | El actor actualiza los datos habilitados y confirma la modificación mediante el botón "Guardar Cambios" [H]. |`
  },
  // CU-54: Registrar pool de preguntas
  {
    target: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita el nombre del pool. |
| 3 | El actor selecciona "Pool de Preguntas" [A], ingresa el nombre [B], carga las preguntas y respuestas [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos del banco de preguntas: nombre del pool [B] y descripción temática [C]. |
| 3 | El actor completa el nombre y descripción del pool y confirma la creación mediante el botón "Agregar" [D]. |`
  },
  // CU-55: Modificar pool
  {
    target: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar mediante el botón "Editar Pool" [A] (ver CU-53: Buscar pool). |
| 2 | El sistema muestra los datos actuales del pool, con sus preguntas y opciones. |
| 3 | El actor modifica el nombre o edita preguntas en el panel [B] y confirma mediante el botón "Guardar Cambios" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el actor busca y selecciona el pool a modificar mediante el botón "Editar Pool" [A] (ver CU-53: Buscar pool). |
| 2 | El sistema muestra los datos actuales en el panel: nombre del pool [B], descripción [C] y editor de preguntas y opciones [D]. |
| 3 | El actor actualiza los campos habilitados y confirma mediante el botón "Guardar Cambios" [E]. |`
  },
  // CU-58: Crear autoevaluación
  {
    target: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: nombre, tiempo límite, cantidad de preguntas, fecha de apertura, fecha de cierre (opcional), cantidad de intentos permitidos (opcional; si se deja vacía, no hay límite), y el o los pools de preguntas a asociar, el de la propia unidad y, si es la última unidad del programa, opcionalmente los de otras unidades, para conformar la evaluación final del curso. |
| 3 | El actor selecciona "Cuestionario / Autoevaluación" [A], completa el título y parámetros [B], selecciona el pool asociado [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los parámetros del cuestionario: nombre de la autoevaluación [B], tiempo límite en minutos [C], cantidad de preguntas sorteables [D], fecha de apertura [E], fecha de cierre [F], intentos permitidos [G] y pools asociados [H]. |
| 3 | El actor completa los parámetros solicitados y confirma la creación mediante el botón "Agregar" [I]. |`
  },
  // CU-66: Programar clase en vivo
  {
    target: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita: la cohorte a la que se dirige la clase, título, fecha y hora, y duración estimada. |
| 3 | El actor selecciona "Clase en Vivo (Streaming)" [A], completa el título [B], define la fecha y hora de transmisión [C] y confirma mediante el botón "Agregar" [D]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad). |
| 2 | El sistema solicita los datos de la transmisión: título de la clase [B], cohorte destinataria [C], fecha y hora de inicio [D], duración estimada en minutos [E] y enlace a la sala de streaming [F]. |
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
| 2 | El sistema solicita los parámetros de renderizado: título de la clase [B], unidad temática [C], guión textual [D], avatar HeyGen [E] y voz sintética [F]. |
| 3 | El actor completa los parámetros y confirma la generación mediante el botón "Generar Video en HeyGen" [G]. |`
  },
  // CU-79: Modificar clase con clon
  {
    target: `| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase con clon mediante el botón "Editar Guión" [A] (ver CU-77: Buscar clase con clon). |
| 2 | El sistema muestra el título y el guión actuales de la clase. |
| 3 | El actor modifica el título o el guión en el editor [B] y confirma mediante el botón "Actualizar y Regenerar Video" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el docente busca y selecciona la clase con clon mediante el botón "Editar Guión" [A] (ver CU-77: Buscar clase con clon). |
| 2 | El sistema muestra los datos actuales en el editor: título de la clase [B], guión textual [C], avatar [D] y voz [E]. |
| 3 | El actor actualiza el guión y confirma mediante el botón "Actualizar y Regenerar Video" [F]. |`
  },
  // CU-81: Registrarse
  {
    target: `| 1 | El caso de uso inicia cuando el interesado solicita crear una cuenta mediante el botón "Registrarse" [A] en la barra de navegación. |
| 2 | El sistema solicita: nombre, apellido, correo electrónico, DNI y contraseña. |
| 3 | El actor ingresa los datos solicitados en el formulario de registro [B] y presiona el botón "Crear Cuenta" [C]. |`,
    replacement: `| 1 | El caso de uso inicia cuando el interesado solicita crear una cuenta mediante el botón "Registrarse" [A] en la barra de navegación. |
| 2 | El sistema solicita los datos de la cuenta de alumno: nombre [B], apellido [C], correo electrónico [D], DNI [E] y contraseña de seguridad [F]. |
| 3 | El actor completa los datos de registro y confirma la creación mediante el botón "Crear Cuenta" [G]. |`
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
    replacement: `| 1 | El sistema lista los parámetros configurados de la plataforma [A]. |
| 2 | El actor selecciona un parámetro existente de la tabla de configuración mediante el botón "Editar Valor" [B]. |
| 3 | El sistema solicita el nuevo valor y muestra la descripción del impacto [C]. |
| 4 | El actor ingresa el nuevo valor del parámetro y confirma la actualización mediante el botón "Guardar Parámetro" [D]. |`
  }
];

let applied = 0;
replacements.forEach((r, idx) => {
  if (md.includes(r.target)) {
    md = md.replace(r.target, r.replacement);
    applied++;
  } else {
    console.log(`Target index ${idx} not found`);
  }
});

console.log(`Applied ${applied} replacements out of ${replacements.length}`);
fs.writeFileSync(mdPath, md, 'utf8');
