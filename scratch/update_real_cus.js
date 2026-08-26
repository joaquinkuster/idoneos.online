const fs = require('fs');
const path = require('path');

// 1. Leer Casos de Uso Extendidos
const rootDir = path.resolve(__dirname, '..');
const extPath = path.join(rootDir, 'docs', 'requisitos', 'Casos de Uso Extendidos.md');
const extContent = fs.readFileSync(extPath, 'utf8');

// 2. Parsear bloques de Extendidos
const blocks = extContent.split(/(?=### CU-)/g);
const header = `# 4.8. Casos de Uso Reales

En esta sección se detallan los 100 casos de uso reales del Sistema Idóneos Online, derivados de los casos de uso extendidos y complementados con la información funcional y de interacción disponible en las pantallas y prototipos de interfaz de usuario. Cada caso de uso se referencia mediante un código correlativo (CU-01 a CU-99, incluyendo CU-26b).

---
`;

// Helper para parsear un bloque de CU Extendido
function parseCUBlock(blockText) {
  const titleMatch = blockText.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) return null;
  const id = titleMatch[1].trim();
  const name = titleMatch[2].trim();

  const objMatch = blockText.match(/- \*\*Objetivo\(s\) asociado\(s\)\*\*:\s*([^\r\n]+)/);
  const riMatch = blockText.match(/- \*\*Requisito\(s\) de información asociado\(s\)\*\*:\s*([^\r\n]+)/);
  const modMatch = blockText.match(/- \*\*Módulo\*\*:\s*([^\r\n]+)/);
  const actMatch = blockText.match(/- \*\*Actor\(es\)\*\*:\s*([^\r\n]+)/);
  const descMatch = blockText.match(/- \*\*Descripción\*\*:\s*([^\r\n]+(?:\r?\n(?!- \*\*)[^\r\n]+)*)/);
  
  // Precondiciones
  const preMatch = blockText.match(/- \*\*Precondición\(es\)\*\*:\s*([\s\S]*?)(?=- \*\*Flujo de eventos\*\*:)/);
  
  // Flujo de eventos (tabla de 2 columnas Paso | Acción)
  const flowTableMatch = blockText.match(/\| Paso \| Acción \|\r?\n\|[-|\s]+\|\r?\n([\s\S]*?)(?=\r?\n- \*\*(?:Postcondición|Salida|Excepciones))/);
  
  // Postcondiciones o Salida
  const postMatch = blockText.match(/- \*\*Postcondición\(es\)\*\*:\s*([\s\S]*?)(?=- \*\*Excepciones\*\*:)/);
  const salidaMatch = blockText.match(/- \*\*Salida\*\*:\s*([\s\S]*?)(?=- \*\*Excepciones\*\*:)/);

  // Excepciones
  const excMatch = blockText.match(/- \*\*Excepciones\*\*:\s*([\s\S]*?)(?=- \*\*Frecuencia\*\*:)/);

  // Metadatos finales
  const frecMatch = blockText.match(/- \*\*Frecuencia\*\*:\s*([^\r\n]+)/);
  const estMatch = blockText.match(/- \*\*Estabilidad\*\*:\s*([^\r\n]+)/);
  const comMatch = blockText.match(/- \*\*Comentarios\*\*:\s*([^\r\n]+(?:\r?\n(?!###|---)[^\r\n]+)*)/);

  // Parsear filas del flujo
  const flowSteps = [];
  if (flowTableMatch) {
    const lines = flowTableMatch[1].trim().split(/\r?\n/);
    for (let l of lines) {
      const parts = l.split('|').map(s => s.trim()).filter(s => s.length > 0);
      if (parts.length >= 2) {
        flowSteps.push({ paso: parseInt(parts[0], 10), accion: parts[1] });
      }
    }
  }

  return {
    id,
    name,
    objetivos: objMatch ? objMatch[1].trim() : '',
    requisitos: riMatch ? riMatch[1].trim() : '',
    modulo: modMatch ? modMatch[1].trim() : '',
    actores: actMatch ? actMatch[1].trim() : '',
    descripcion: descMatch ? descMatch[1].trim() : '',
    precondiciones: preMatch ? preMatch[1].trim() : '',
    flowSteps,
    postcondiciones: postMatch ? postMatch[1].trim() : null,
    salida: salidaMatch ? salidaMatch[1].trim() : null,
    excepciones: excMatch ? excMatch[1].trim() : '',
    frecuencia: frecMatch ? frecMatch[1].trim() : 'Media',
    estabilidad: estMatch ? estMatch[1].trim() : 'Alta',
    comentarios: comMatch ? comMatch[1].trim() : '–'
  };
}

// Generar modificaciones de interacción real para cada paso del Actor
function enrichFlowStepsWithRealInteractions(cu) {
  const id = cu.id;
  const steps = cu.flowSteps.map(s => ({ ...s }));

  // Aplicar conectores precisos por ID o regla general de caso de uso
  steps.forEach(step => {
    let t = step.accion;

    // CU-01 Buscar curso
    if (id === 'CU-01') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar uno o más cursos mediante los campos de búsqueda [A] y filtros por categoría [B].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y confirma la búsqueda mediante el botón "Buscar" [C].';
      if (step.paso === 6) t = 'El actor puede seleccionar uno de los resultados mediante la tarjeta de curso y el botón "Gestionar curso" [D] para ver su detalle.';
    }
    // CU-02 Ver mis cursos
    else if (id === 'CU-02') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar sus inscripciones mediante el buscador [A] y selector de estado [B].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y confirma mediante el botón "Filtrar" [C].';
      if (step.paso === 6) t = 'El actor selecciona un curso mediante el botón "Ingresar al curso" [D] para acceder al contenido del programa de su cohorte.';
    }
    // CU-03 Registrar curso
    else if (id === 'CU-03') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita registrar un nuevo curso mediante el botón "+ Nuevo Curso" [A].';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados mediante el formulario de curso [B], adjunta la portada mediante el botón "Examinar..." [C] y confirma mediante el botón "Guardar Curso" [D].';
    }
    // CU-04 Modificar curso
    else if (id === 'CU-04') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el curso a modificar mediante la tabla y el botón "Modificar" [A] (ver CU-01: Buscar curso).';
      if (step.paso === 3) t = 'El actor modifica los datos habilitados mediante el formulario de edición [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-05 Dar de baja curso
    else if (id === 'CU-05') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el curso a dar de baja mediante el botón "Dar de baja" [A] (ver CU-01: Buscar curso).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Baja" [B] del modal de confirmación.';
    }
    // CU-06 Explorar catálogo
    else if (id === 'CU-06') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor accede al catálogo público de cursos y aplica filtros de búsqueda [A].';
      if (step.paso === 3) t = 'El actor selecciona un curso mediante la tarjeta y el botón "Ver Ficha / Inscribirme" [B].';
    }
    // CU-07 Buscar categoría
    else if (id === 'CU-07') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar una o más categorías mediante la barra de búsqueda [A].';
      if (step.paso === 3) t = 'El actor ingresa el criterio que desea y presiona el botón "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar uno de los resultados mediante la fila de la tabla [C] para ver su detalle.';
    }
    // CU-08 Registrar categoría
    else if (id === 'CU-08') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita registrar una nueva categoría mediante el botón "+ Nueva Categoría" [A].';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados mediante el formulario [B] y confirma mediante el botón "Guardar Categoría" [C].';
    }
    // CU-09 Modificar categoría
    else if (id === 'CU-09') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la categoría a modificar mediante el botón "Editar" [A] (ver CU-07: Buscar categoría).';
      if (step.paso === 3) t = 'El actor modifica el nombre o la descripción mediante el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-10 Dar de baja categoría
    else if (id === 'CU-10') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la categoría a dar de baja mediante el botón "Eliminar" [A] (ver CU-07: Buscar categoría).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del modal.';
    }
    // CU-11 Buscar cohorte
    else if (id === 'CU-11') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar las cohortes de un programa mediante el selector de programa [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea mediante los filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 6) t = 'El actor puede seleccionar uno de los resultados mediante la fila de la cohorte [D] para ver su detalle.';
    }
    // CU-12 Registrar cohorte
    else if (id === 'CU-12') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa mediante el botón "+ Nueva Cohorte" [A] (ver CU-15: Buscar programa).';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados mediante el formulario de cohorte [B], define fechas y cupos [C] y confirma mediante el botón "Guardar Cohorte" [D].';
    }
    // CU-13 Modificar cohorte
    else if (id === 'CU-13') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la cohorte a modificar mediante el botón "Editar" [A] (ver CU-11: Buscar cohorte).';
      if (step.paso === 3) t = 'El actor modifica los campos mediante el formulario [B] y confirma los cambios mediante el botón "Guardar Cambios" [C].';
    }
    // CU-14 Dar de baja cohorte
    else if (id === 'CU-14') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la cohorte a dar de baja mediante el botón "Cancelar Cohorte" [A] (ver CU-11: Buscar cohorte).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Cancelación" [B] del diálogo modal.';
    }
    // CU-15 Buscar programa
    else if (id === 'CU-15') {
      if (step.paso === 3) t = 'Si el actor desea actuar sobre un programa distinto, solicita buscarlo mediante el desplegable de versiones de programa [A].';
      if (step.paso === 5) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Filtrar" [B].';
      if (step.paso === 8) t = 'El actor puede seleccionar uno de los resultados mediante la lista de programas [C] para ver su detalle.';
    }
    // CU-16 Registrar programa
    else if (id === 'CU-16') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el curso mediante el botón "+ Nuevo Programa" [A] (ver CU-01: Buscar curso).';
      if (step.paso === 3) t = 'El actor opcionalmente selecciona un programa anterior del cual partir mediante el desplegable [B], ingresa los datos en el formulario [C] y confirma mediante el botón "Guardar Programa" [D].';
    }
    // CU-17 Modificar programa
    else if (id === 'CU-17') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa a modificar mediante el botón "Editar Programa" [A] (ver CU-15: Buscar programa).';
      if (step.paso === 3) t = 'El actor modifica los datos mediante el formulario de edición [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-18 Dar de baja programa
    else if (id === 'CU-18') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa a dar de baja mediante el botón "Dar de baja" [A] (ver CU-15: Buscar programa).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Baja" [B] del cuadro de confirmación.';
    }
    // CU-19 Buscar unidad
    else if (id === 'CU-19') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar las unidades de un programa desde la pestaña "Curso & Unidades" [A].';
      if (step.paso === 3) t = 'El actor puede seleccionar una unidad mediante el encabezado del acordeón Moodle [B] para gestionar su contenido.';
    }
    // CU-20 Agregar unidad
    else if (id === 'CU-20') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa mediante el botón "+ Añadir secciones" [A] (ver CU-15: Buscar programa).';
      if (step.paso === 3) t = 'El actor ingresa el título y descripción en el modal [B], o selecciona la unidad reutilizable [C], y confirma mediante el botón "Agregar Unidad" [D].';
    }
    // CU-21 Modificar unidad
    else if (id === 'CU-21') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la unidad a modificar mediante el icono del lápiz o menú "Editar" [A] (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor modifica el título, la descripción o el contenido en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-22 Quitar unidad
    else if (id === 'CU-22') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la unidad a quitar mediante la opción "Quitar de este programa" [A] del menú de la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 4) t = 'El actor confirma la operación mediante el botón "Confirmar y Quitar" [B] del diálogo de confirmación.';
    }
    // CU-23 Buscar cronograma
    else if (id === 'CU-23') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa cuyo cronograma desea consultar desde la pestaña "Cronograma" [A].';
    }
    // CU-24 Modificar cronograma
    else if (id === 'CU-24') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el programa cuyo cronograma desea modificar mediante el botón "Reordenar Cronograma" [A] (ver CU-23: Buscar cronograma).';
      if (step.paso === 3) t = 'El actor reordena las unidades arrastrándolas mediante el tirador [B], ajusta la duración en semanas [C] y confirma mediante el botón "Guardar Cronograma" [D].';
    }
    // CU-25 Ver participantes
    else if (id === 'CU-25') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita ver los participantes desde la pestaña "Participantes" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios que desea mediante los filtros [B] y confirma la búsqueda con el botón "Filtrar" [C].';
    }
    // CU-26 Acceder curso (Alumno)
    else if (id === 'CU-26') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno solicita acceder a las unidades de un curso matriculado desde la pestaña "Curso" [A].';
      if (step.paso === 3) t = 'El alumno puede seleccionar alternativamente la pestaña "Cronograma" [B], "Participantes" [E] o "Calificaciones" [F].';
      if (step.paso === 6) t = 'El alumno selecciona una unidad habilitada mediante el acordeón Moodle [C] y accede a la actividad o autoevaluación deseada [D].';
    }
    // CU-27 Buscar material
    else if (id === 'CU-27') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar material desde la pestaña "Materiales" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea mediante el formulario de filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar uno de los resultados mediante la fila del material [D] para ver su detalle.';
    }
    // CU-28 Subir material
    else if (id === 'CU-28') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor hace clic en "+ Añade una actividad o un recurso" [A] en el Modo Edición (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor selecciona la opción "Material / Documento PDF" [A] e ingresa el título en el formulario [B].';
      if (step.paso === 5) t = 'El actor adjunta el archivo mediante el botón "Examinar..." [C] y confirma la carga mediante el botón "Agregar" [D].';
    }
    // CU-29 Modificar material
    else if (id === 'CU-29') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el material a modificar mediante el menú "Editar" [A] (ver CU-27: Buscar material).';
      if (step.paso === 3) t = 'El actor modifica el título, el archivo o el estado de visibilidad mediante el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-30 Dar de baja material
    else if (id === 'CU-30') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el material a dar de baja mediante la opción "Eliminar" [A] (ver CU-27: Buscar material).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del modal.';
    }
    // CU-31 Buscar término de glosario
    else if (id === 'CU-31') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar términos desde la pestaña "Glosario" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante la barra de búsqueda de términos [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar un término en específico mediante la lista [D].';
    }
    // CU-32 Registrar término de glosario
    else if (id === 'CU-32') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor selecciona "Glosario de Términos" [A], ingresa el concepto [B], su definición [C] y confirma mediante el botón "Agregar" [D].';
    }
    // CU-33 Modificar término de glosario
    else if (id === 'CU-33') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el término a modificar mediante el botón "Editar" [A] (ver CU-31: Buscar término de glosario).';
      if (step.paso === 3) t = 'El actor modifica el término o la definición en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-34 Dar de baja término de glosario
    else if (id === 'CU-34') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el término a dar de baja mediante el botón "Eliminar" [A] (ver CU-31: Buscar término de glosario).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Baja" [B] del modal.';
    }
    // CU-35 Buscar consulta de foro
    else if (id === 'CU-35') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar consultas desde la pestaña "Foros" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante el buscador de temas [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar uno de los hilos de consulta [D] para ver su detalle.';
    }
    // CU-36 Registrar consulta de foro
    else if (id === 'CU-36') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno solicita hacer una pregunta mediante el botón "+ Nueva Consulta" [A] en el foro de la unidad.';
      if (step.paso === 4) t = 'El actor ingresa el título [B], redacta la consulta en el editor [C] y confirma la publicación mediante el botón "Publicar Consulta" [D].';
    }
    // CU-37 Modificar consulta de foro
    else if (id === 'CU-37') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor selecciona su consulta mediante la opción "Editar Mensaje" [A] (ver CU-35: Buscar consulta de foro).';
      if (step.paso === 4) t = 'El actor modifica el texto en el editor [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-38 Dar de baja consulta de foro
    else if (id === 'CU-38') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el administrador selecciona la consulta indebida mediante el botón "Moderar / Eliminar" [A] (ver CU-35: Buscar consulta de foro).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del cuadro de moderación.';
    }
    // CU-39 Buscar respuesta de foro
    else if (id === 'CU-39') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor selecciona un hilo de consulta para ver las respuestas mediante el enlace del tema [A].';
      if (step.paso === 3) t = 'El actor puede seleccionar una respuesta en específico [B] para leerla o moderarla.';
    }
    // CU-40 Registrar respuesta de foro
    else if (id === 'CU-40') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona el botón "Responder" [A] en la consulta del foro (ver CU-35: Buscar consulta de foro).';
      if (step.paso === 3) t = 'El actor redacta la respuesta en el área de texto [B] y confirma mediante el botón "Enviar Respuesta" [C].';
    }
    // CU-41 Modificar respuesta de foro
    else if (id === 'CU-41') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona su respuesta mediante la opción "Editar Respuesta" [A] (ver CU-39: Buscar respuesta de foro).';
      if (step.paso === 4) t = 'El actor modifica el texto en el editor [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-42 Dar de baja respuesta de foro
    else if (id === 'CU-42') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el administrador selecciona la respuesta a moderar mediante el botón "Eliminar Respuesta" [A] (ver CU-39: Buscar respuesta de foro).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del modal.';
    }
    // CU-43 Buscar inscripción
    else if (id === 'CU-43') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar inscripciones mediante la barra de filtros [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar una inscripción de la tabla [C] para ver su detalle.';
      if (step.paso === 7) t = 'El actor puede generar el certificado de la inscripción seleccionada mediante el botón "Descargar Certificado" [D], si fue emitido.';
    }
    // CU-44 Inscribir curso
    else if (id === 'CU-44') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno selecciona una cohorte con inscripción abierta y presiona el botón "Inscribirme Ahora" [A] (ver CU-06: Explorar catálogo de cursos).';
    }
    // CU-45 Dar de baja inscripción
    else if (id === 'CU-45') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la inscripción a dar de baja mediante el botón "Dar de baja inscripción" [A] (ver CU-43: Buscar inscripción).';
      if (step.paso === 4) t = 'El actor confirma la baja y, si lo desea, ingresa el motivo en el cuadro de texto [B].';
      if (step.paso === 6) t = 'El actor confirma la advertencia de no reembolso mediante el botón "Confirmar Baja Definitiva" [C].';
    }
    // CU-46 Buscar pago
    else if (id === 'CU-46') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar pagos mediante los filtros de estado y fechas [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar un pago de la lista [C] para ver su detalle.';
      if (step.paso === 7) t = 'El actor puede generar el comprobante mediante el botón "Descargar Comprobante" [D], si el pago fue acreditado.';
    }
    // CU-47 Realizar pago
    else if (id === 'CU-47') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno solicita pagar el curso mediante el botón "Proceder al Pago" [A].';
      if (step.paso === 5) t = 'El alumno selecciona la opción de pago con billetera virtual MODO [B].';
      if (step.paso === 8) t = 'El alumno escanea el código QR interactivo [C] con la app de MODO o banco y completa el pago desde su dispositivo.';
    }
    // CU-48 Buscar progreso
    else if (id === 'CU-48') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita consultar los progresos mediante el buscador de alumnos [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar uno de los alumnos mediante la tabla [C] para ver su avance detallado por unidad [D].';
    }
    // CU-49 Buscar descuento
    else if (id === 'CU-49') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar descuentos mediante el filtro de vigencia [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar un descuento de la tabla [C] para ver su detalle.';
    }
    // CU-50 Registrar descuento
    else if (id === 'CU-50') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita registrar un nuevo descuento mediante el botón "+ Nuevo Descuento" [A].';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados en el formulario [B], define el porcentaje y vigencia [C] y confirma mediante el botón "Guardar Descuento" [D].';
    }
    // CU-51 Modificar descuento
    else if (id === 'CU-51') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el descuento a modificar mediante el botón "Editar" [A] (ver CU-49: Buscar descuento).';
      if (step.paso === 3) t = 'El actor modifica los datos que desea en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-52 Dar de baja descuento
    else if (id === 'CU-52') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el descuento a dar de baja mediante el botón "Eliminar" [A] (ver CU-49: Buscar descuento).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Baja" [B] del modal.';
    }
    // CU-53 Buscar pool
    else if (id === 'CU-53') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar pools desde la pestaña "Pools" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante la barra de filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar uno de los pools de la lista [D] para ver sus preguntas.';
    }
    // CU-54 Crear pool
    else if (id === 'CU-54') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor selecciona "Pool de Preguntas" [A], ingresa el nombre [B], carga las preguntas y respuestas [C] y confirma mediante el botón "Agregar" [D].';
    }
    // CU-55 Modificar pool
    else if (id === 'CU-55') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el pool a modificar mediante el botón "Editar Pool" [A] (ver CU-53: Buscar pool).';
      if (step.paso === 3) t = 'El actor modifica el nombre o edita preguntas en el panel [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-56 Dar de baja pool
    else if (id === 'CU-56') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el pool a dar de baja mediante el botón "Eliminar Pool" [A] (ver CU-53: Buscar pool).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Baja" [B] del cuadro de confirmación.';
    }
    // CU-57 Buscar autoevaluación
    else if (id === 'CU-57') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar autoevaluaciones desde la pestaña "Autoevaluaciones" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante el buscador [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar una autoevaluación de la lista [D] para ver su configuración.';
    }
    // CU-58 Crear autoevaluación
    else if (id === 'CU-58') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor selecciona "Cuestionario / Autoevaluación" [A], completa el título y parámetros [B], selecciona el pool asociado [C] y confirma mediante el botón "Agregar" [D].';
    }
    // CU-59 Modificar autoevaluación
    else if (id === 'CU-59') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a modificar mediante el botón "Editar Cuestionario" [A] (ver CU-57: Buscar autoevaluación).';
      if (step.paso === 3) t = 'El actor modifica los datos habilitados en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-60 Dar de baja autoevaluación
    else if (id === 'CU-60') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la autoevaluación a dar de baja mediante el botón "Eliminar" [A] (ver CU-57: Buscar autoevaluación).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del modal.';
    }
    // CU-61 Buscar intento de autoevaluación
    else if (id === 'CU-61') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar intentos desde la tabla de intentos de la autoevaluación [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante los filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar uno de los intentos de la lista [D] para revisar el detalle de respuestas.';
    }
    // CU-62 Ver calificaciones
    else if (id === 'CU-62') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita consultar las calificaciones desde la pestaña "Calificaciones" [A].';
      if (step.paso === 3) t = 'El actor ingresa el alumno a consultar mediante el selector o buscador [B].';
    }
    // CU-63 Realizar intento de autoevaluación
    else if (id === 'CU-63') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-26: Acceder curso), selecciona el enlace de la autoevaluación y presiona "Comenzar Intento" [A].';
      if (step.paso === 5) t = 'El alumno selecciona una opción de respuesta para cada una de las preguntas mediante los radio buttons [B].';
      if (step.paso === 6) t = 'El alumno confirma la entrega del intento mediante el botón "Terminar Intento y Enviar Todo" [C].';
    }
    // CU-64 Dar de baja intento de autoevaluación
    else if (id === 'CU-64') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el intento a anular mediante el botón "Anular Intento por Fraude" [A] (ver CU-61: Buscar intento de autoevaluación).';
      if (step.paso === 4) t = 'El actor confirma la baja mediante el botón "Confirmar Anulación" [B] del modal de moderación.';
    }
    // CU-65 Buscar clase en vivo
    else if (id === 'CU-65') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar clases en vivo desde la pestaña "Clases en Vivo" [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante los filtros de estado [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar una de las clases de la lista [D] para ver su ficha de transmisión.';
    }
    // CU-66 Programar clase en vivo
    else if (id === 'CU-66') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona "+ Añade una actividad o un recurso" [A] en la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor selecciona "Clase en Vivo (Streaming)" [A], completa el título [B], define la fecha y hora de transmisión [C] y confirma mediante el botón "Agregar" [D].';
    }
    // CU-67 Modificar clase en vivo
    else if (id === 'CU-67') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la clase programada mediante el botón "Reprogramar / Editar" [A] (ver CU-65: Buscar clase en vivo).';
      if (step.paso === 4) t = 'El actor modifica los datos en el formulario [B] y confirma la reprogramación mediante el botón "Guardar Cambios" [C].';
    }
    // CU-68 Cancelar clase en vivo
    else if (id === 'CU-68') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la clase programada a cancelar mediante el botón "Cancelar Clase" [A] (ver CU-65: Buscar clase en vivo).';
      if (step.paso === 3) t = 'El actor confirma la cancelación mediante el botón "Confirmar Cancelación" [B] del modal.';
    }
    // CU-69 Dar de baja clase en vivo
    else if (id === 'CU-69') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la clase finalizada a dar de baja mediante el botón "Eliminar Registro" [A] (ver CU-65: Buscar clase en vivo).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del cuadro modal.';
    }
    // CU-70 Iniciar clase en vivo
    else if (id === 'CU-70') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente busca y selecciona la clase programada y presiona el botón "Transmitir en Vivo" [A] (ver CU-65: Buscar clase en vivo).';
      if (step.paso === 5) t = 'El docente copia la clave privada de transmisión generada [B], la carga en OBS y comienza a transmitir.';
    }
    // CU-71 Finalizar clase en vivo
    else if (id === 'CU-71') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente presiona el botón "Finalizar Transmisión" [A] en el panel de control de la clase en vivo (ver CU-65: Buscar clase en vivo).';
    }
    // CU-72 Ingresar a clase en vivo
    else if (id === 'CU-72') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el alumno, viendo el contenido de la unidad (ver CU-26: Acceder curso), presiona el botón "Ingresar a la Sala en Vivo" [A].';
    }
    // CU-73 Generar banco de preguntas
    else if (id === 'CU-73') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona la opción "Generar Banco con IA (Ollama)" [A] dentro de la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 4) t = 'El actor ingresa opcionalmente un guión en el prompt [B] y confirma la generación mediante el botón "Generar Preguntas con IA" [C].';
    }
    // CU-74 Generar resumen de unidad
    else if (id === 'CU-74') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona la opción "Generar Resumen de Unidad con IA" [A] (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor confirma la generación mediante el botón "Crear Resumen Automático" [B].';
    }
    // CU-75 Generar presentación de unidad
    else if (id === 'CU-75') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona la opción "Generar Presentación con IA" [A] (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor confirma la generación mediante el botón "Generar Diapositivas" [B].';
    }
    // CU-76 Crear clon
    else if (id === 'CU-76') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente solicita crear su clon mediante el botón "Configurar Clon de IA" [A] en su perfil.';
      if (step.paso === 4) t = 'El actor adjunta o toma su foto facial [B] y graba el audio de calibración de voz mediante el botón "Grabar Muestra" [C].';
      if (step.paso === 6) t = 'El actor acepta los términos de uso y confirma mediante el botón "Crear Clon en HeyGen" [D].';
    }
    // CU-77 Buscar clase con clon
    else if (id === 'CU-77') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar clases con Clon de IA desde la sección de contenidos generados [A] (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda en los filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 5) t = 'El actor puede seleccionar una clase de la lista [D] para previsualizar el video generado.';
    }
    // CU-78 Generar clase con clon
    else if (id === 'CU-78') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente selecciona "Generar Video con Avatar Clon" [A] en la unidad (ver CU-19: Buscar unidad).';
      if (step.paso === 3) t = 'El actor ingresa el título [B], redacta el guión en el prompt [C] y confirma la generación mediante el botón "Sintetizar Video con IA" [D].';
    }
    // CU-79 Modificar clase con clon
    else if (id === 'CU-79') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el docente busca y selecciona la clase con clon mediante el botón "Editar Guión" [A] (ver CU-77: Buscar clase con clon).';
      if (step.paso === 3) t = 'El actor modifica el título o el guión en el editor [B] y confirma mediante el botón "Actualizar y Regenerar Video" [C].';
    }
    // CU-80 Dar de baja clase con clon
    else if (id === 'CU-80') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la clase con clon a dar de baja mediante el botón "Eliminar Video" [A] (ver CU-77: Buscar clase con clon).';
      if (step.paso === 3) t = 'El actor confirma la baja mediante el botón "Confirmar Eliminación" [B] del modal.';
    }
    // CU-81 Registrarse
    else if (id === 'CU-81') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el interesado solicita crear una cuenta mediante el botón "Registrarse" [A] en la barra de navegación.';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados en el formulario de registro [B] y presiona el botón "Crear Cuenta" [C].';
      if (step.paso === 7) t = 'El actor accede al enlace de confirmación recibido en su correo electrónico.';
    }
    // CU-82 Buscar usuario
    else if (id === 'CU-82') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita buscar usuarios mediante la barra de filtros [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda que desea y presiona "Buscar" [B].';
      if (step.paso === 6) t = 'El actor puede seleccionar un usuario de la tabla [C] para consultar su expediente.';
    }
    // CU-83 Registrar usuario
    else if (id === 'CU-83') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita registrar manualmente un usuario mediante el botón "+ Nuevo Usuario" [A].';
      if (step.paso === 3) t = 'El actor ingresa los datos solicitados en el formulario [B], asigna el rol correspondiente [C] y confirma mediante el botón "Guardar Usuario" [D].';
    }
    // CU-84 Modificar usuario
    else if (id === 'CU-84') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la cuenta del alumno a modificar mediante el botón "Editar" [A] (ver CU-82: Buscar usuario).';
      if (step.paso === 3) t = 'El actor modifica los datos en el formulario de cuenta [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-85 Dar de baja usuario
    else if (id === 'CU-85') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la cuenta a dar de baja mediante el botón "Desactivar Cuenta" [A] (ver CU-82: Buscar usuario).';
      if (step.paso === 6) t = 'El actor confirma la baja mediante el botón "Confirmar Desactivación" [B] del modal de seguridad.';
    }
    // CU-86 Ver perfil
    else if (id === 'CU-86') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor despliega el menú de usuario [A] en la barra superior y selecciona la opción "Ver Perfil" [B].';
    }
    // CU-87 Editar perfil
    else if (id === 'CU-87') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor selecciona la opción "Editar Perfil" [A] desde el menú de usuario o su ficha de perfil.';
      if (step.paso === 3) t = 'El actor modifica sus datos personales en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-88 Registrar docente
    else if (id === 'CU-88') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita registrar un nuevo docente mediante el botón "+ Nuevo Docente" [A].';
      if (step.paso === 3) t = 'El actor ingresa los datos personales y profesionales en el formulario docente [B], adjunta títulos y matrículas [C] y confirma mediante el botón "Guardar Docente" [D].';
    }
    // CU-89 Modificar docente
    else if (id === 'CU-89') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona el docente a modificar mediante el botón "Editar Perfil Docente" [A] (ver CU-82: Buscar usuario).';
      if (step.paso === 3) t = 'El actor modifica la biografía, experiencia, títulos o estado de habilitación en el formulario [B] y confirma mediante el botón "Guardar Cambios" [C].';
    }
    // CU-90 Iniciar sesión
    else if (id === 'CU-90') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor accede a la pantalla de autenticación y solicita iniciar sesión [A].';
      if (step.paso === 3) t = 'El actor ingresa sus credenciales en el formulario [B] y presiona "Iniciar Sesión" [C], o selecciona el botón "Continuar con Google" [D].';
    }
    // CU-91 Cerrar sesión
    else if (id === 'CU-91') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor hace clic en el trigger del usuario [A] y selecciona la opción "Cerrar sesión" [B].';
    }
    // CU-92 Restablecer contraseña
    else if (id === 'CU-92') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor presiona el enlace "¿Olvidaste tu contraseña?" [A] en la pantalla de inicio de sesión.';
      if (step.paso === 3) t = 'El actor ingresa su correo en el campo de recuperación [B] y presiona el botón "Enviar Enlace" [C].';
      if (step.paso === 6) t = 'El actor accede al enlace recibido en su correo, ingresa la nueva contraseña en el formulario [D] y confirma mediante el botón "Restablecer Contraseña" [E].';
    }
    // CU-93 Buscar sesión
    else if (id === 'CU-93') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita ver las sesiones activas desde la sección de seguridad [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda en los filtros [B] y presiona "Buscar" [C].';
      if (step.paso === 6) t = 'El actor puede seleccionar una sesión en específico de la tabla [D] para ver sus detalles de IP y dispositivo.';
    }
    // CU-94 Cerrar sesión activa
    else if (id === 'CU-94') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor busca y selecciona la sesión activa a cerrar mediante el botón "Cerrar Sesión Remota" [A] (ver CU-93: Buscar sesión).';
      if (step.paso === 2) t = 'El actor confirma el cierre mediante el botón "Confirmar Cierre de Sesión" [B] del diálogo modal.';
    }
    // CU-95 Consultar auditoría
    else if (id === 'CU-95') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita consultar el registro de auditoría desde el menú administrativo [A].';
      if (step.paso === 3) t = 'El actor ingresa los criterios de búsqueda mediante los filtros de eventos [B] y presiona "Filtrar Eventos" [C].';
    }
    // CU-96 Generar informe de alumnos de un curso
    else if (id === 'CU-96') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita generar un informe de alumnos desde el módulo de reportes [A].';
      if (step.paso === 3) t = 'El actor selecciona el curso y el rango de fechas en los controles [B] y presiona el botón "Generar Informe" [C].';
      if (step.paso === 6) t = 'El sistema pone el informe a disposición del actor mediante el botón "Descargar PDF / Excel" [D].';
    }
    // CU-97 Generar informe de ingresos de un curso
    else if (id === 'CU-97') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor solicita generar un informe de ingresos desde el módulo de reportes [A].';
      if (step.paso === 3) t = 'El actor selecciona el curso y el período en el panel de parámetros [B] y presiona el botón "Generar Reporte de Ingresos" [C].';
      if (step.paso === 6) t = 'El sistema pone el informe a disposición del actor mediante el botón "Descargar Reporte" [D].';
    }
    // CU-98 Consultar estadísticas
    else if (id === 'CU-98') {
      if (step.paso === 1) t = 'El caso de uso inicia cuando el actor accede al dashboard de estadísticas e indicadores clave [A].';
    }
    // CU-99 Configurar parámetros
    else if (id === 'CU-99') {
      if (step.paso === 2) t = 'El actor selecciona un parámetro existente de la tabla de configuración mediante el botón "Editar Valor" [A].';
      if (step.paso === 4) t = 'El actor ingresa el nuevo valor en el formulario de parámetro [B] y confirma mediante el botón "Guardar Parámetro" [C].';
    }

    step.accion = t;
  });

  return steps;
}

// Formatear un CU a Markdown idéntico a Casos de Uso Extendidos pero con los badges en el flujo
function formatRealCUMarkdown(cu, steps) {
  const formatList = (str) => {
    if (!str) return '';
    return str.split('\n')
      .map(l => l.trim())
      .filter(l => l.length > 0)
      .map(l => l.startsWith('-') ? '  ' + l : '  - ' + l)
      .join('\n');
  };

  let md = `### ${cu.id}: ${cu.name}
- **Objetivo(s) asociado(s)**: ${cu.objetivos}
- **Requisito(s) de información asociado(s)**: ${cu.requisitos}
- **Módulo**: ${cu.modulo}
- **Actor(es)**: ${cu.actores}
- **Descripción**: ${cu.descripcion}
- **Precondición(es)**:
${formatList(cu.precondiciones)}
- **Flujo de eventos**:

| Paso | Acción |
|------|--------|
`;

  steps.forEach(s => {
    md += `| ${s.paso} | ${s.accion} |\n`;
  });

  if (cu.postcondiciones) {
    md += `\n- **Postcondición(es)**:\n${formatList(cu.postcondiciones)}\n`;
  } else if (cu.salida) {
    md += `\n- **Salida**: ${cu.salida}\n`;
  }

  md += `- **Excepciones**:\n`;
  if (cu.excepciones.trim() === 'No aplica.' || cu.excepciones.trim() === 'Ninguna.') {
    md += `  - No aplica.\n`;
  } else {
    md += `${formatList(cu.excepciones)}\n`;
  }

  md += `- **Frecuencia**: ${cu.frecuencia}
- **Estabilidad**: ${cu.estabilidad}
- **Comentarios**: ${cu.comentarios}
`;

  return md;
}

// Definición especial de CU-26b (Modo Edición) adaptada al formato estándar de 2 columnas
const cu26bMarkdown = `### CU-26b: Acceder curso — Modo Edición (Docente / Administrador)
- **Objetivo(s) asociado(s)**: OBJ-02: Gestionar el contenido de los cursos.
- **Requisito(s) de información asociado(s)**: RI-02: Información sobre el contenido de los cursos.
- **Módulo**: MOD-F-02: Módulo de Gestión Académica
- **Actor(es)**: Docente, Administrador
- **Descripción**: Permite al Docente o Administrador acceder a la vista contextual del curso e interactuar con el toggle de "Modo Edición" para habilitar las acciones de gestión inline sobre cada unidad pedagógica del programa, añadiendo secciones o actividades sin salir de la vista principal.
- **Precondición(es)**:
  - El actor ha iniciado sesión en el sistema con el rol Docente o Administrador.
  - El curso debe estar activo.
  - Si el actor es Docente, participa en el curso como titular o ayudante.
- **Flujo de eventos**:

| Paso | Acción |
|------|--------|
| 1 | El caso de uso inicia cuando el actor busca y selecciona un curso desde su listado de cursos asignados (ver CU-01: Buscar curso). |
| 2 | El sistema muestra la vista del curso con sus unidades pedagógicas en acordeón y las pestañas contextuales de navegación [B]. |
| 3 | El actor activa el switch "Modo Edición" [C] ubicado en la cabecera superior del curso. |
| 4 | El sistema habilita los controles de edición inline: botón "+ Añadir secciones" [A], menú "Editar" por unidad y botones "+ Añade una actividad o un recurso" [D] al pie de cada tema. |
| 5 | El actor puede seleccionar añadir una nueva sección [A] o incorporar una actividad o recurso a una unidad [D]. |
| 6 | Fin del caso de uso. |

- **Postcondición(es)**:
  - La vista del curso queda en Modo Edición con los controles y accesos directos de gestión habilitados para el actor.
- **Excepciones**:
  - **Paso 2**: Si el actor es Docente y no participa en el curso como titular o ayudante, el sistema informa el error y deniega el acceso al modo edición.
- **Frecuencia**: Alta
- **Estabilidad**: Alta
- **Comentarios**: Variante de interfaz unificada estilo Moodle FCEQYN Virtual que centraliza la administración académica directa en la misma vista de navegación.
`;

// Procesar todos los CUs
let fullMarkdown = header;
let currentMod = '';

for (let i = 1; i < blocks.length; i++) {
  const parsed = parseCUBlock(blocks[i]);
  if (!parsed) continue;

  if (parsed.modulo !== currentMod) {
    currentMod = parsed.modulo;
    fullMarkdown += `\n## ${currentMod}\n\n`;
  }

  const enrichedSteps = enrichFlowStepsWithRealInteractions(parsed);
  fullMarkdown += formatRealCUMarkdown(parsed, enrichedSteps);
  fullMarkdown += `\n---\n\n`;

  // Insertar CU-26b justo después de CU-26
  if (parsed.id === 'CU-26') {
    fullMarkdown += cu26bMarkdown;
    fullMarkdown += `\n---\n\n`;
  }
}

// Escribir Casos de Uso Reales.md
const outMdPath = path.join(rootDir, 'docs', 'diseño', 'Casos de Uso Reales.md');
fs.writeFileSync(outMdPath, fullMarkdown, 'utf8');
console.log('Successfully generated Casos de Uso Reales.md with identical 2-column structure and UI badges!');
