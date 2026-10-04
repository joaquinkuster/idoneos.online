# Trabajo Final (ASC) - Proyecto Software (LSI) - FCEQyN - UNaM
# Sistema de Gestión Idóneos Online - Küster Joaquín - Martinez Lazaro Ezequiel

---

## Sistema de Gestión Idóneos Online
### Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales

**Idóneos Online S.A.S. · Argentina**

Apóstoles (Misiones), 9 de abril de 2026

Al Prof. Adjunto de las cátedras  
Trabajo Final (ASC) | Proyecto Software (LSI)  
FCEQyN - UNaM  

Lic. Sergio Daniel Caballero  
S/D

---

De nuestra mayor consideración:

Nos dirigimos a Ud. a efectos de presentar la propuesta para el desarrollo de un producto software como tema para las cátedras "Proyecto Software" de la carrera Licenciatura en Sistemas de Información (plan de estudios 2013) y "Trabajo Final" de la carrera Analista en Sistemas de Computación (plan de estudios 2010).

El producto software a desarrollar se denomina: **Sistema de Gestión Idóneos Online**.

Sin otro particular, y quedando a la espera de la evaluación de la propuesta, nos despedimos atte.

---

____________________________  
**[Küster Joaquín]**  
[906560 — Analista de Sistemas de Información]  
[44652101]  

____________________________  
**[Martinez Lazaro Ezequiel]**  
[906047 — Analista de Sistemas de Información]  
[42086981]

---

## Evaluación de la Propuesta
*Reservado para el equipo de cátedra*

| Aspecto | Evaluación |
|---------|------------|
| Nombre del proyecto | Sistema de Gestión Idóneos Online |
| Alumno/a | Küster Joaquín - Martinez Lazaro Ezequiel |
| Fecha de la evaluación | |
| Presentación en general | |
| Objetivos | |
| Requisitos funcionales | |
| Requisitos no funcionales | |
| Procesos automáticos | |
| Planificación | |
| Estrategia de validación / verificación | |
| Metodología | |
| **Resultado final** | **APROBADO - DEBE CORREGIR** |

Firma por el equipo de cátedra:  
____________________________

---

## Contenidos

- Evaluación de la Propuesta
- Contenidos
1. Planteo del Problema
2. Introducción
3. Alcance y Limitaciones
4. Especificación de Módulos
5. Descripción de los Módulos
6. Decisiones Tecnológicas
7. Procesos Automatizados
8. Estimación de Tamaño por Módulo
9. Entorno Tecnológico y Metodológico
10. Planificación de Actividades

---

## 1. Planteo del Problema

### 1.1. El Mercado de la Formación en Finanzas y Economía en Argentina

Idóneos Online es un proyecto impulsado por dos socios: **Fausto Spotorno**, economista recibido en la UCA, director de la maestría de la UADE y titular de la consultora Orlando Ferreres y Asociados, quien ejercerá la dirección académica de la plataforma, y **Sebastián Bordato**, ideólogo del proyecto y responsable del armado inicial de la propuesta, quien además se desempeñará como uno de los docentes. El proyecto ya cuenta con CUIT y se encuentra dado de alta ante la Inspección General de Justicia (IGJ) como Sociedad Anónima Simplificada, y dispone de un sitio web preliminar desarrollado en Wix, aunque todavía no se encuentra en producción ni cuenta con la totalidad de los cursos cargados.

El público objetivo son hombres y mujeres de entre 22 y 45 años, sin exigencia de título de grado ni de conocimientos previos específicos (salvo en los cursos introductorios, que parten de cero), y focalizado exclusivamente en Argentina, dado que gran parte del contenido de los cursos (impuestos, normativa de mercado de capitales, cuestiones contables) es intrínseco a la legislación argentina. Los socios estiman alcanzar más de **2000 alumnos por año**.

### 1.2. Un Vacío entre la Formación de Grado y la Especialización de Élite

El diagnóstico de mercado realizado por los socios identificó que el acceso a profesores de alto nivel académico (directores de maestrías en universidades como la UADE, la Universidad Torcuato Di Tella o la Universidad de San Andrés) estuvo históricamente restringido a quienes completaron una carrera de grado y luego accedían a una maestría, dejando fuera a un público amplio interesado en formarse en finanzas, economía y mercado de capitales sin recorrer ese camino.

Asimismo, los socios evaluaron la oferta de cursos existente en el mercado de finanzas y mercado de capitales argentino y la calificaron como deficiente: los cursos dictados por el mercado de valores (BYMA) están muy segmentados y orientados únicamente al mercado de capitales, mientras que experiencias propias de Bordato en Net Finance (otra empresa del mismo, dedicada al mercado de capitales) con cursos de terceros arrojaron resultados de baja calidad, con profesores poco preparados y diseño académico deficiente.

Frente a este panorama, Idóneos Online se propone cubrir un espacio más amplio que sus competidores (incorporando, además del mercado de capitales, contenidos de contabilidad, impuestos y eficiencia de flujo de caja) apoyándose en dos diferenciales: **docentes de élite** difíciles de encontrar en otras plataformas, y una metodología de tipo **"Netflix"**, con avance secuencial por unidades y estándares de producción audiovisual (iluminación, encuadre, vestimenta) que buscan un nivel de profesionalismo no observado en otras propuestas del mercado.

### 1.3. La Necesidad de una Solución Tecnológica Propia

Actualmente, Idóneos Online cuenta con una landing page, sobre la cual el cliente cargó y probó a modo de ensayo su primer curso. Esa solución resulta estática: cada curso nuevo requiere que la persona de sistemas construya manualmente su propia página y emule el comportamiento de un área privada (donde el alumno accede sólo al contenido pagado), en lugar de generarse automáticamente a partir de la información cargada. Además, las herramientas necesarias para las tres modalidades de dictado (en vivo, con Clon de IA y grabado) no están integradas en la solución actual, por lo que cada una se usa por separado.

Esto hace que cada curso nuevo dependa de la intervención de la persona de sistemas en lugar de que el equipo docente lo cargue de forma autónoma, y que, al no persistir información en una base de datos, no sea posible generar reportes ni estadísticas centralizadas. Este esquema funciona con un catálogo reducido, pero dificulta escalar a medida que crezcan los cursos. Por eso, el proyecto busca resolver estas limitaciones con un sistema propio, administrable y escalable en este sentido, donde la administración pueda cargar y publicar cursos sin depender de intervención técnica. A esto se suma que, al no estar en producción, tampoco existe un método validado de pagos ni control de sesiones concurrentes que prevenga el uso compartido de credenciales.

Los socios manifestaron interés en desarrollar un nuevo sistema en lugar de continuar con la solución actual, dado que ya enfrentan limitaciones para escalar (carga manual de cada curso, dependencia del área de sistemas). Según Sebastián Bordato, *"me interesa que el sistema nuevo tenga como una especie de panel donde nosotros mismos podamos ir cargando los cursos"*.

---

## 2. Introducción

En el presente documento se definen los objetivos, alcances y limitaciones del sistema de información propuesto para dar soporte a la operación académica, comercial y administrativa de Idóneos Online, una plataforma de cursos online de finanzas, economía y mercado de capitales orientada al público argentino. La iniciativa articula la dimensión técnica del sistema con el modelo de negocio del proyecto: el acceso a docentes de élite mediante cursos estructurados, la sostenibilidad económica a través de la venta de cursos y el uso de inteligencia artificial para escalar la producción de contenido académico.

Idóneos Online constituye un espacio de formación no universitaria en el que un cuerpo docente de alto nivel académico (encabezado por el director académico del proyecto) dicta cursos en tres modalidades posibles: **en vivo, grabada tradicional y mediante un Clon con inteligencia artificial** que reproduce la imagen y la voz reales del docente. Además de su rol formativo, el proyecto busca posicionarse como una alternativa profesional y accesible frente a la oferta actual del mercado de finanzas y economía en Argentina.

El sistema propuesto (denominado en adelante **Sistema Idóneos Online**) surge como solución a las limitaciones detectadas en la plataforma Wix actualmente utilizada por el cliente. Su implementación permitirá gestionar los cursos (desde su estructuración en unidades hasta la evaluación y certificación de los alumnos), automatizar el flujo de inscripción y pago, incorporar la generación de clases mediante Clon con IA, y dar soporte a clases en vivo con grabación y vencimiento automático.

La solución será accesible desde cualquier dispositivo con conexión a internet (celular, PC o notebook) a través de un navegador web, contemplando distintos perfiles de usuario: la administración y el cuerpo docente, y los alumnos que se inscriben a los cursos.

### 2.1. Objetivos

El sistema se propone dar respuesta a las carencias detectadas, aportando las siguientes funcionalidades clave. Cada objetivo está alineado a uno de los diez módulos descritos en las secciones 4 y 5:

1. **Gestionar y explorar el catálogo de cursos.** Administra el catálogo público de cursos: alta y edición de cursos, categorías, programas vigentes (con sus unidades secuenciales) y dictados disponibles (fecha, cupo y equipo docente asociado), con la variante "Supervisado" aplicable a cualquiera de las tres modalidades de dictado (en vivo, con clon de IA y grabada). El sistema muestra la vista pública de cada curso al alumno antes de inscribirse.

2. **Gestionar el contenido de los cursos.** Administra el material de cada unidad (grabación, bibliografía, presentación y resumen), el glosario de términos y el foro de consultas entre alumnos y docentes, para dar soporte al contenido y la interacción.

3. **Gestionar la inscripción y el pago de los alumnos.** Gestiona la inscripción de los alumnos a los cursos y su pago mediante tarjeta de crédito o débito, con generación automática de comprobantes y aplicación de descuentos según las condiciones vigentes, para garantizar la sostenibilidad económica del proyecto.

4. **Evaluar y certificar a los alumnos.** Evalúa el avance de los alumnos mediante autoevaluaciones automáticas por unidad, a partir del pool de preguntas de cada unidad, y emite la constancia de finalización al completar el curso, para certificar el progreso académico alcanzado.

5. **Gestionar las clases en vivo.** Gestiona la transmisión de clases en vivo mediante integración con OBS, con ingreso simultáneo de los alumnos inscriptos y grabación automática disponible por un plazo configurable, para dar soporte a la modalidad de cursada sincrónica.

6. **Generar contenido académico con inteligencia artificial.** Genera, mediante modelos de inteligencia artificial, contenido académico de apoyo a partir del material cargado por los docentes: bancos de preguntas, resúmenes y presentaciones con un modelo ejecutado localmente (Ollama), y clases dictadas por un Clon de IA que reproduce la imagen y la voz reales del docente mediante integración con HeyGen, para reducir el tiempo que dedican a su preparación.

7. **Gestionar usuarios, autenticación y notificaciones.** Gestiona los usuarios del sistema (alumnos, docentes y administradores), su autenticación mediante usuario y contraseña o Google OAuth y el control de sesiones concurrentes, y notifica por correo electrónico los eventos relevantes a cada actor.

8. **Registrar las acciones críticas del sistema.** Registra automáticamente en un log de auditoría las acciones críticas del sistema (pagos, altas de curso y cambios de estado de inscripción), para garantizar trazabilidad y control sobre las operaciones, en particular sobre los pagos.

9. **Generar reportes y estadísticas de gestión.** Genera, para cada curso, informes de alumnos inscriptos e ingresos, y permite consultar en pantalla los indicadores generales de inscripciones e ingresos, para facilitar la toma de decisiones estratégicas de los socios.

10. **Permitir la configuración de los parámetros operativos.** Permite a la administración ajustar los parámetros operativos del sistema (plazo de disponibilidad de grabaciones, sesiones concurrentes permitidas, datos institucionales y credenciales de integración, entre otros) sin intervención técnica sobre el código.

---

## 3. Alcance y Limitaciones

En esta sección se presentan los límites funcionales del sistema, definiendo hasta dónde llegará la solución propuesta. Se describen las áreas que cubrirá el sistema, así como los aspectos que quedan fuera de su alcance según lo relevado con el cliente.

### 3.1. Alcance

El sistema cubre el ciclo completo de la propuesta académica de la empresa, desde la carga de un curso hasta la certificación del alumno, contemplando tres perfiles de usuario:

- **Alumno.** Explora el catálogo de cursos, se inscribe (dentro del período habilitado) y paga el curso elegido, cursa con avance secuencial por unidades durante el plazo de acceso definido para ese curso, resuelve las autoevaluaciones correspondientes y obtiene la constancia de finalización al completar el curso.

- **Docente.** Carga contenido en las unidades de sus cursos, dicta las clases en cualquiera de las 3 modalidades soportadas (en vivo, grabada o mediante Clon de inteligencia artificial) y se apoya en las herramientas de generación de contenido con IA para preparar bancos de preguntas, resúmenes y presentaciones.

- **Administración.** Gestiona los usuarios del sistema, los cursos, los programas de cada curso (con sus unidades), los dictados disponibles, hace seguimiento del negocio mediante informes de alumnos e ingresos por curso y estadísticas generales de inscripciones e ingresos, cuenta con trazabilidad de las operaciones críticas a través del log de auditoría, y ajusta los parámetros operativos del sistema sin intervención técnica.

El detalle funcional de cada uno de estos puntos (altas, bajas, modificaciones y consultas de cada entidad) se desarrolla módulo por módulo en la sección 7.

### 3.2. Limitaciones

- **Reembolsos.** Al tratarse de cursos de nivel académico, se aplica una política de no reembolso análoga a la habitual en el ámbito universitario, donde el pago no es reembolsable una vez realizado. El sistema no contemplará un mecanismo de solicitud ni devolución de pagos, en línea con lo confirmado por el cliente durante la entrevista.

- **Clases presenciales.** Toda la propuesta académica de la empresa se dicta bajo modalidad virtual (en vivo, grabada o con Clon de IA). El sistema no contemplará la gestión de encuentros presenciales.

- **Facturación fiscal.** El sistema emitirá únicamente un comprobante automático al confirmarse el pago. La emisión de factura A o B se gestionará de forma manual por correo electrónico ante solicitud del alumno, sin integración con AFIP ni con organismos fiscales.

- **Firmas electrónicas o digitales certificadas.** La constancia de finalización de curso emitida por el sistema tiene valor informativo e institucional, pero no incorporará mecanismos de firma digital certificada.

- **Contabilidad institucional.** El sistema no se integrará con herramientas de contabilidad de la sociedad ni gestionará los libros contables de la empresa.

- **Gestión de sueldos y recursos humanos.** El sistema no contemplará la liquidación de haberes de los socios ni de eventual personal contratado.

- **Validación oficial de cursos ante organismos.** Por ejemplo, la Comisión Nacional de Valores. El cliente manifestó estar trabajando en que ciertos cursos obtengan reconocimiento de organismos gubernamentales, pero se trata de una gestión externa e institucional, ajena a las funcionalidades del sistema. Se plantea como una mejora a futuro que el sistema pueda emitir certificados con validez oficial una vez que esas gestiones se concreten.

---

## 4. Especificación de Módulos

En esta sección se detallan los módulos que componen el sistema. Cada módulo corresponde a una funcionalidad o característica específica del sistema, diseñada para cumplir con los objetivos planteados. Se distinguen módulos funcionales (unidades lógicas, autónomas y reutilizables que agrupan funciones y datos específicos para realizar una tarea particular) y módulos no funcionales (que establecen las propiedades de calidad transversales del sistema).

### 4.1. Módulos Funcionales

Los siguientes módulos constituyen el comportamiento observable del sistema. Cada uno responde a uno o más objetivos específicos:

- Módulo del Catálogo de Cursos.
- Módulo de Contenido de Unidades.
- Módulo de Inscripciones.
- Módulo de Evaluaciones.
- Módulo de Clases en Vivo.
- Módulo de Generación de Contenido con IA.

### 4.2. Módulos No Funcionales

Estos módulos establecen las propiedades de calidad y control del sistema, como seguridad, auditoría y análisis:

- Módulo de Usuarios y Notificaciones.
- Módulo de Auditoría.
- Módulo de Reportes y Estadísticas.
- Módulo de Configuración.

---

## 5. Descripción de los Módulos

### 5.1. Módulo del Catálogo de Cursos

Este módulo constituye el catálogo público de la oferta académica. Gestiona los cursos, las categorías en que se organizan, los programas vigentes (con las unidades secuenciales que los componen) que definen qué se enseña en cada uno, y los dictados disponibles para inscripción, con sus fechas y cupos.

- **Gestionar cursos.** Alta, modificación, baja y consulta de cursos (nombre, descripción, precio, imagen de portada, categoría temática y modalidades de dictado asociadas); la publicación de un curso exige que su programa vigente cuente con al menos 10 unidades con material publicado; incluye la consulta pública de la vista de cada curso previa a la inscripción, para que el usuario decida si comprarlo, o la publicación de contenido gratuito que sirva como gancho para atraer nuevos usuarios.

- **Gestionar categorías.** Alta, modificación, baja y consulta de las categorías temáticas (nombre, descripción) en las que se organizan los cursos.

- **Gestionar programas.** Alta, modificación, baja y consulta de los programas de un curso (nombre, descripción y meses de acceso al contenido desde la inscripción), permitiendo mantener más de uno por curso para actualizar su contenido en nuevas ediciones sin afectar a los alumnos ya inscritos en dictados de un programa anterior.

- **Gestionar unidades.** Alta, modificación, baja y consulta de las unidades que componen cada programa (título, descripción y número de orden), pudiendo agregar, editar o quitar unidades en ediciones posteriores; el número de orden determina el avance secuencial del alumno, exigiendo aprobar la autoevaluación de una unidad para habilitar el acceso a la siguiente.

- **Gestionar dictados.** Alta, modificación, baja y consulta de los dictados de un programa (fecha de inicio, fecha de fin y cupo máximo de alumnos), con su propio equipo docente asociado (docente titular y, opcionalmente, docente supervisor); el sistema valida que cada dictado tenga exactamente un docente titular.

### 5.2. Módulo de Contenido de Unidades

Este módulo gestiona el contenido de las unidades de cada programa: el material de estudio, el glosario de términos y el foro de consultas entre alumnos y docentes, disponibles para el alumno una vez inscrito.

- **Gestionar material.** Alta, modificación, baja y consulta del material de cada unidad, según su tipo. Todo material permite modificar su visibilidad (publicado u oculto) para el alumno:
  - **Grabación.** Ruta del archivo de video en el servidor local y su duración. Corresponde a la clase grabada, ya sea transmitida en vivo (el archivo se genera automáticamente al finalizar la transmisión vía OBS), grabada de forma tradicional (subida directamente por el docente) o generada mediante Clon de IA (descargada automáticamente desde HeyGen).
  - **Bibliografía.** Título y autor de la fuente de referencia, junto con un enlace externo o la ruta del archivo correspondiente, utilizada como material complementario de lectura.
  - **Presentación.** Ruta del archivo de la presentación (diapositivas) utilizada como apoyo visual de la clase, subida por el docente o generada automáticamente a partir del guión mediante un modelo de IA local (Módulo de Generación de Contenido con IA).
  - **Resumen.** Texto o ruta del archivo con la síntesis del contenido de la unidad, subido por el docente o generado automáticamente a partir de la bibliografía de la unidad mediante un modelo de IA local (Módulo de Generación de Contenido con IA).

- **Gestionar términos del glosario.** Alta, modificación, baja y consulta de los términos y sus definiciones asociados a cada unidad.

- **Gestionar consultas del foro.** Alta, modificación, baja y consulta de las preguntas que un alumno plantea en el foro de una unidad.

- **Gestionar respuestas del foro.** Alta, modificación, baja y consulta de las respuestas del docente (titular o supervisor) a las consultas planteadas en el foro de una unidad.

### 5.3. Módulo de Inscripciones

Este módulo gestiona el proceso mediante el cual un alumno se inscribe a un curso y realiza el pago correspondiente.

- **Gestionar inscripciones.** Alta de la inscripción de un alumno a un dictado (fecha), con habilitación automática del acceso una vez confirmado el pago y vencimiento automático al cumplirse el plazo de acceso definido para ese dictado, y consulta de las inscripciones realizadas; la baja de una inscripción permite registrar el abandono del alumno (con una observación opcional, por ejemplo el motivo), aunque no habilita reembolso.

- **Gestionar pagos.** Alta de un pago (monto, fecha y datos de la transacción devueltos por la pasarela) mediante la API de MODO, por el total del curso; consulta de los pagos realizados.

- **Gestionar descuentos.** Alta, modificación, baja y consulta de descuentos aplicados a los alumnos (nombre, porcentaje, vigencia desde/hasta, cantidad límite ofertada, cantidad usada, y cantidad de cursos comprados como condición de aplicación, única condición contemplada en el MVP); el sistema desactiva automáticamente un descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero.

- **Gestionar comprobantes.** Alta automática de un comprobante al confirmarse un pago (número, fecha de emisión, archivo descargable), con envío por correo electrónico al alumno; consulta y descarga del comprobante emitido.

- **Gestionar progreso.** Registro automático del avance del alumno por unidad (si fue completada y la fecha en que se completó), asociado a la inscripción correspondiente y creado inicialmente sobre la primera unidad del programa al confirmarse la inscripción; consulta del progreso alcanzado por el alumno en cada curso.

### 5.4. Módulo de Evaluaciones

Este módulo gestiona las autoevaluaciones de cada unidad, los intentos de los alumnos y la emisión de la constancia de finalización del curso.

- **Gestionar pool.** Alta, modificación, baja y consulta del pool de preguntas de cada unidad (nombre), generado por el docente o mediante un modelo de IA local como método alternativo (Módulo de Generación de Contenido con IA); incluye las preguntas (texto, tipo opción múltiple o verdadero/falso) y sus opciones de respuesta (texto, si es correcta o no).

- **Gestionar autoevaluaciones.** Alta, modificación, baja y consulta de autoevaluaciones (nombre, fecha de apertura y fecha de cierre), asociadas a uno o varios pools de preguntas de los que sortea las preguntas de cada intento; permite tanto autoevaluaciones acotadas a una unidad como evaluaciones finales que integran los pools de varias unidades de un curso.

- **Gestionar intentos de autoevaluación.** Alta de un intento por autoevaluación (fecha, nota), con selección aleatoria de 10 preguntas cerradas de los pools asociados, registro de la opción elegida por el alumno para cada pregunta sorteada, corrección automática y reintento obligatorio del cuestionario completo si no se aprueban todas las respuestas; consulta del historial de intentos por alumno.

- **Gestionar certificados.** Alta automática de la constancia de finalización (número de certificado, fecha de emisión, archivo descargable) al aprobar la autoevaluación que completa el curso, asociada a la inscripción del alumno, enviada por correo electrónico; consulta y descarga del certificado emitido.

### 5.5. Módulo de Clases en Vivo

Este módulo gestiona el dictado de clases en vivo mediante integración con el software OBS, y la disponibilidad temporal de la grabación resultante en la plataforma.

- **Programar clase en vivo.** Alta, modificación, baja y consulta de las clases en vivo programadas por el docente (titular o supervisor) para una unidad (título, fecha y hora), quedando en estado Programada.

- **Iniciar clase en vivo.** Al llegar el horario programado, el docente dispara esta acción y el sistema pasa la clase a estado En vivo y genera los datos de conexión (URL RTMP y clave de stream); el docente los carga en OBS para transmitir desde ahí, y el sistema recibe esa señal y la redistribuye simultáneamente a todos los alumnos inscriptos mediante el enlace de conexión.

- **Finalizar clase en vivo.** Al concluir la transmisión, el sistema pasa la clase a estado Finalizada y genera la grabación resultante, que queda cargada como material de tipo Grabación de la unidad correspondiente (Módulo de Contenido de Unidades).

- Al finalizar la transmisión, la grabación queda disponible por un plazo de 4 meses (configurable), con aviso automático antes del vencimiento y eliminación automática al cumplirse el plazo.

Cabe aclarar que, según el cliente, la configuración e instalación del software OBS para cada docente está a cargo del equipo de sistemas de la empresa, previo a la transmisión.

### 5.6. Módulo de Generación de Contenido con IA

Este módulo utiliza modelos de inteligencia artificial para generar automáticamente contenido académico de apoyo a partir del material cargado por los docentes: bancos de preguntas, resúmenes y presentaciones mediante un modelo ejecutado localmente (Ollama), y clases dictadas por un Clon de IA que reproduce la imagen y la voz reales del docente mediante integración con HeyGen, orientado a docentes de alto nivel académico con disponibilidad horaria limitada.

- **Generar banco de preguntas.** El docente aporta el material de referencia de la unidad (bibliografía, glosario) y, opcionalmente, redacta el guión de la clase mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma; el sistema envía ese contenido al modelo de IA local (Ollama), que genera un banco de preguntas cerradas (opción múltiple y verdadero/falso) siguiendo la proporción configurada y lo devuelve al sistema, quedando cargado como pool de preguntas de la unidad (Módulo de Evaluaciones).

- **Generar resumen de unidad.** El docente solicita resumir el contenido de una unidad; el sistema envía la bibliografía cargada al modelo de IA local (Ollama), que genera un resumen estructurado del contenido y lo devuelve al sistema, quedando cargado como material de tipo Resumen de la unidad correspondiente (Módulo de Contenido de Unidades).

- **Generar presentación de unidad.** El docente solicita generar una presentación de una unidad; el sistema envía la bibliografía cargada al modelo de IA local (Ollama), que devuelve una estructura de contenidos (títulos, subtítulos, puntos clave), y el sistema le da formato como presentación descargable, quedando cargada como material de tipo Presentación de la unidad correspondiente (Módulo de Contenido de Unidades).

- **Gestionar clases con Clon de IA.** El docente (titular o supervisor del curso) redacta el guión de la clase para una unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma, y dispara esta acción; el sistema registra la clase en estado Pendiente, guarda el guión y lo envía junto con el Avatar ya validado del docente a HeyGen, mediante integración con esa plataforma; al recibir el video generado, el sistema actualiza el estado a Generada (o a Error si la generación falla) y lo carga como material de tipo Grabación de la unidad correspondiente (Módulo de Contenido de Unidades); consulta del estado, del guión y de las clases generadas por unidad.

### 5.7. Módulo de Usuarios y Notificaciones

Este módulo administra el acceso al sistema, las funcionalidades disponibles para cada tipo de usuario según su rol, y el envío de notificaciones sobre los distintos eventos del sistema, asegurando que cada actor reciba la información relevante en el momento oportuno.

- **Gestionar alumnos.** Alta, modificación, baja y consulta de alumnos (nombre, correo electrónico, contraseña o cuenta de Google).

- **Gestionar docentes.** Alta, modificación, baja y consulta de docentes (nombre, correo electrónico, teléfono, biografía, años de experiencia, título o títulos universitarios o de posgrado con su matrícula profesional cuando corresponde, matrícula del Registro de Idóneos de la Comisión Nacional de Valores cuando aplica y cursos asignados); el alta la realiza el administrador de forma manual, verificando el título declarado contra el Registro Público de Graduados Universitarios o, en su defecto, la matrícula profesional o de la CNV informada; el administrador puede habilitar o suspender temporalmente a un docente (por ejemplo, ante una suspensión por mala praxis) sin eliminar su cuenta ni su historial.

- **Gestionar administradores.** Alta, modificación, baja y consulta de administradores (nombre y correo electrónico).

- **Autenticación de usuarios.** Inicio de sesión mediante usuario y contraseña o Google OAuth como método alternativo, con validación de la cuenta por correo electrónico al registrarse.

- **Gestionar sesiones.** Alta automática de una sesión al iniciar sesión (token, fecha de inicio, IP, dispositivo); cierre individual o automático de una sesión (fecha de fin); consulta de las sesiones activas de un usuario; el sistema limita la cantidad de sesiones concurrentes permitidas por usuario, para mitigar el uso compartido de credenciales detectada como riesgo por el cliente.

- **Envío de notificaciones.** Envío automático de notificaciones ante los eventos del sistema (validación de cuenta, confirmación de pago, habilitación de acceso al curso, pagos no acreditados (ya sea por rechazo del pago o por demora en la confirmación de la pasarela), vencimiento próximo de una grabación, finalización de un curso con envío de constancia, y publicación de nuevas unidades o cursos), enviadas por correo electrónico mediante JavaMailSender.

### 5.8. Módulo de Auditoría

Este módulo registra de forma automática y continua las acciones relevantes realizadas dentro del sistema, con especial foco en el circuito de pagos, generando un historial que permite rastrear qué ocurrió, cuándo y sobre qué operación, ante la necesidad de trazabilidad manifestada por el cliente.

- **Gestionar registros de auditoría.** Alta automática de un registro ante cada operación crítica del sistema (crear, modificar, eliminar o consultar) sobre una entidad relevante (por ejemplo, pagos, inscripciones o cursos), con el usuario responsable, el tipo de acción, la entidad afectada, el identificador del registro puntual, la fecha y hora exacta, la dirección IP desde la que se realizó la acción y, cuando corresponde, el estado del registro antes y después del cambio; consulta restringida al administrador.

### 5.9. Módulo de Reportes y Estadísticas

Este módulo permite generar informes consolidados sobre los distintos procesos del sistema, orientados a la toma de decisiones de los socios del proyecto.

- **Generar reportes.** Alta automática de un registro (tipo de reporte, fecha, usuario que lo generó) al generar, para un curso, uno de los siguientes informes; consulta del historial de reportes generados y descarga del informe:
  - **Informe de alumnos.** Compara el curso frente al resto en cantidad de inscriptos, muestra la evolución de sus inscripciones en el tiempo y el estado de sus inscripciones (completadas, vigentes, dadas de baja).
  - **Informe de ingresos.** Compara el curso frente al resto en ingresos por pagos acreditados, muestra la evolución de sus ingresos en el tiempo, los ingresos por categoría de curso y el monto bruto frente al neto por descuentos aplicados.

- **Consultar estadísticas.** Consulta en pantalla de los indicadores generales del sistema (alumnos activos, cantidad de inscripciones vigentes, ingresos del mes con variación respecto al mes anterior, inscripciones de los últimos 30 días y ranking de los cinco cursos con más inscriptos), sin necesidad de generar un reporte.

### 5.10. Módulo de Configuración

Este módulo permite a la administración ajustar los parámetros operativos del sistema para adaptarlo a las necesidades de Idóneos Online sin requerir intervención técnica sobre el código.

- **Gestionar parámetros.** Alta, modificación, baja y consulta de parámetros operativos del sistema mediante un esquema clave-valor (clave, valor), que permite incorporar nuevos parámetros sin modificar el esquema de base de datos. Los parámetros configurados en el MVP incluyen: plazo de disponibilidad de grabaciones y antelación del aviso previo; cantidad máxima de sesiones concurrentes por usuario; datos institucionales utilizados en el sitio, comprobantes y constancias (razón social, CUIT, domicilio, logo, email de contacto y teléfono de contacto); credenciales de integración con servicios externos (Google OAuth, MODO y SMTP de Gmail); plazo máximo de espera para la confirmación de un pago antes de registrarlo como incidencia pendiente; proporción de tipos de pregunta (opción múltiple / verdadero-falso) en los bancos generados con IA; y tiempo límite de edición de consultas y respuestas del foro.

---

## 6. Decisiones Tecnológicas

Esta sección reúne los fundamentos de las decisiones tecnológicas y de diseño mencionadas en la sección 5, para no interrumpir la lectura funcional de cada módulo. Al tratarse de un proyecto académico sin presupuesto propio, se priorizaron herramientas gratuitas, ya utilizadas por el cliente o de costo variable ligado al uso real del sistema, evitando alternativas que exigieran una inversión inicial no contemplada.

**¿Por qué almacenamiento local?** Se optó por almacenar el material (grabaciones, presentaciones y bibliografía) en el servidor local durante el desarrollo del trabajo final. Esta decisión responde a que el proyecto académico no cuenta con presupuesto para contratar un servicio de almacenamiento en la nube y, además, las pruebas de funcionamiento se realizarán con un volumen reducido de datos de prueba, muy inferior a la demanda estimada de 2.000 alumnos por año para un entorno de producción. En este contexto, el almacenamiento local resulta suficiente para demostrar el funcionamiento del sistema. Esta decisión se plantea como un riesgo a evaluar en el análisis de riesgos (sección 10), ya que la puesta en producción y un crecimiento de la demanda requerirán adoptar una solución de almacenamiento distribuido. Además, se trabajará con el sistema de archivos propio del servidor en lugar de almacenar los archivos directamente en la base de datos, ya que se trata de archivos pesados (grabaciones y demás material multimedia, de cientos de MB a varios GB por archivo) y guardarlos en la base de datos no resulta escalable: cada descarga obligaría a cargar el archivo completo en memoria y el tamaño de la base de datos, junto con sus copias de respaldo, crecería en consecuencia.

**¿Por qué MODO?** Se eligió MODO como alternativa a la cátedra además de tener una documentación (API) más completa, que ayuda a la hora de implementarla: a diferencia de la de Ualá Bis, que se divide en autenticación, órdenes de pago y devoluciones, la de MODO separa la integración backend (rutas de la API, igual que Ualá) de la integración frontend, incluyendo el código HTML para el botón de pago y el JavaScript para crear la solicitud de pago y mostrar el modal. Además, su documentación incluye un entorno de pruebas explícito ("apk MODO Testing") para hacer cobros ficticios antes de ir a producción y demostrar el proceso automatizado, mediante una Aplicación de Stage (APK, sólo compatible con Android) con un flujo paso a paso: descarga de la app, inicio de sesión y un listado de tarjetas de prueba. En cuanto a límites de uso, la documentación de MODO no especifica ninguno; existe sí un límite máximo de $2.000.000 por transferencia, muy por encima del precio aproximado de un curso (120 mil pesos, según el cliente).

**¿Por qué JavaMailSender?** Se eligió JavaMailSender, la solución nativa de Spring para el envío de correos electrónicos por SMTP, en lugar de contratar una API externa como Brevo o Resend: solo requiere un servidor SMTP que envíe los correos. Como entregable para el Trabajo Final (PMV), se optó por usar directamente el SMTP de Gmail, que además le resulta útil al cliente porque ya usa Gmail para gran parte de su operación (según lo relevado en la entrevista). El envío automatizado por este medio permite 100 correos por día; superado ese límite, Gmail bloquea el envío por 24 horas. Para el alcance del Trabajo Final, con datos de prueba y volumen reducido, ese límite sobra; para una puesta en producción real, con la demanda proyectada de 2.000 alumnos por año, podría quedar corto en días de mucha actividad, y la alternativa sería pasar a Google Workspace (pago), que sube el límite a 2.000 correos por día por usuario.

**¿Por qué Ollama para generar el contenido académico con IA?** Se eligió Ollama porque Spring, el framework utilizado, cuenta con una librería nativa para este motor de IA: Spring AI da un cliente (ChatClient) para comunicarse con Ollama desde el código Java con la misma interfaz que se usaría para cualquier otro proveedor, apuntando a localhost en lugar de a una API externa, sin necesidad de API keys ni cobro por uso; la librería es solo el conector, Ollama debe seguir corriendo por su cuenta en el servidor. Con él se generan el banco de preguntas, el resumen de cada unidad (a partir de su bibliografía, sin necesidad de transcribir audio) y la presentación, en lugar de recurrir a una API externa: así el contenido académico de los cursos no sale de la red local, sin costo por uso, y un modelo como Llama 3.1 (8B de parámetros, 128K de contexto) corre en el mismo servidor sin GPU dedicada, a una velocidad adecuada para una generación asincrónica que el docente solicita y espera. Ollama corre modelos de lenguaje (LLM) que generan texto y sirven para chatear, resumir, generar preguntas o estructurar contenido en palabras, pero no generan video, imagen ni voz clonada de una persona: al tratarse de una tecnología completamente distinta a la de clonación de video/voz, esa funcionalidad queda fuera de su alcance, y por eso las clases con Clon de IA no se generan con Ollama sino con HeyGen. En cuanto al contexto, Ollama no impone un límite de tokens propio: el límite real es el parámetro configurable num_ctx, bajo por defecto (2048-4096 tokens) pero ajustable manualmente al ejecutar el modelo; el techo real lo pone el hardware, ya que a mayor contexto solicitado, mayor memoria consumida, y sin la VRAM suficiente el modelo no carga o responde muy lento (128K tokens es adecuado para un entorno de pruebas, según la documentación). Si en producción se necesitará más, la solución no es pagar más al servicio, sino invertir en mejor hardware (más VRAM/GPU) para poder subir el num_ctx sin degradar el rendimiento. Como alternativa a mejorar el propio hardware, Ollama lanzó en 2025 su propio servicio de inferencia en la nube, Ollama Cloud, con planes Free ($0), Pro (US$20/mes) y Max (US$100/mes), que no cobran por token sino por uso de GPU (tiempo de cómputo), con cupos que se resetean cada 5 horas y límites semanales; a mayor plan, mayor cupo y cantidad de modelos concurrentes.

**¿Por qué OBS?** Es la herramienta gratuita que el cliente ya utiliza para las clases en vivo, y compone la señal en escenas propias (cámara, diapositivas) transmitiéndola vía RTMP directo al servidor de Idóneos Online. Esto permite guardar la clase automáticamente como material de la plataforma, para que los alumnos que no pudieron asistir la vean después. Zoom limita su plan gratuito a 40 minutos por reunión y no ofrece grabación en la nube (sólo local, en la computadora del docente); Google Meet directamente no ofrece grabación en cuentas personales gratuitas. En ambos casos hay que pagar un plan superior para tener la clase disponible automáticamente en una plataforma propia. OBS no tiene un límite de duración incorporado, ni para grabar ni para transmitir; el límite real pasa por dos lados ajenos a la herramienta: del lado del servidor, si se graba la clase para dejarla disponible después (y no solo transmitirla en vivo), el archivo resultante ocupa espacio en el servidor que recibe el RTMP, y ahí el límite es el almacenamiento disponible, algo que se puede resolver contratando más infraestructura en la nube; y del lado del docente, la estabilidad de su conexión de subida (upload) durante toda la clase, ya que un corte de Internet interrumpe la transmisión independientemente de OBS y del sistema de Idóneos Online.

**¿Por qué HeyGen y no un modelo de clonación local?** Se investigaron alternativas de código abierto para generar los Clones (SadTalker, Wav2Lip), pero exigen una GPU dedicada (6-8GB de VRAM como mínimo, con un consumo que además crece con la duración del video) muy por encima de lo que necesita Ollama, por lo que no es técnicamente viable para el proyecto. Wav2Lip, además, restringe su uso a fines no comerciales, incompatible con la operación real de Idóneos Online. HeyGen integra en un solo servicio la clonación de voz, la generación de video y la validación de identidad del docente (Avatar Consent), y ya lo paga el cliente, independientemente del proyecto de software. En cuanto a límites de uso, HeyGen no impone un tope fijo de minutos sino un sistema de créditos mensuales: el plan gratuito permite 3 videos por mes de hasta 1 minuto, en 720p y con marca de agua, por lo que no es viable para producción. A partir del plan pago Creator (US$29/mes) se asignan 600 créditos mensuales y videos de hasta 30 minutos sin marca de agua; el consumo de créditos varía según el tipo de contenido, y la generación de un clon con avatar realista (Avatar IV/V) (la funcionalidad que requiere el clon con IA) consume 20 créditos por minuto (frente a los 2 créditos por minuto de otras funciones, como el doblaje de audio), lo que con el plan Creator equivale a un máximo de 30 minutos de contenido de Clon por mes. Si el volumen necesario lo supera, la solución es actualizar a un plan superior (Pro, desde US$49/mes), que otorga entre 1.000 y 100.000 créditos mensuales según la tarifa elegida, además de exportación en 4K: el costo escala en forma directa con la cantidad de minutos de Clon necesarios por mes, por lo que el límite no es una restricción técnica del servicio sino una variable de costo ajustable según el uso proyectado del sistema.

**¿Por qué Google OAuth?** Se eligió Google OAuth porque las notificaciones del sistema se envían por Gmail (mediante JavaMailSender): que el usuario inicie sesión con esa misma cuenta de Gmail ayuda a asegurar que las reciba. A esto se suma que el cliente ya centraliza en Gmail buena parte de su operación (validación de cuentas, confirmaciones de pago, comunicación con alumnos y docentes, según lo relevado en la entrevista), que su integración no tiene costo, y que permite que el alumno reutilice una cuenta de Gmail que ya posee en lugar de crear una contraseña nueva, reduciendo la fricción del registro.

---

## 7. Procesos Automatizados

A continuación se describen los procesos automatizados del sistema. Cada proceso es considerado un módulo inteligente en tanto genera valor agregado real para el proyecto, no forma parte de la versión mínima viable (MVP) del sistema, trasciende la lógica de una simple consulta a la base de datos, se compone de una secuencia de 4 a 5 pasos encadenados y requiere la intervención articulada de al menos dos o tres módulos del sistema.

| Denominación | Responsable | Módulos que intervienen | Descripción |
|--------------|-------------|------------------------|-------------|
| **PA-1: Login con Google** | Küster Joaquín | • Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración (credenciales de OAuth parametrizables) | **Disparador:** el usuario desea iniciar sesión sin crear una cuenta nueva y selecciona "Ingresar con Google".<br>**Pasos:** (1) El sistema redirige al usuario a la pantalla de inicio de sesión de Google, solicitando permiso para acceder a su correo electrónico y datos básicos de perfil; (2) Google devuelve un token de acceso validado; (3) El sistema busca una cuenta asociada al correo recibido; (4) Si no existe, crea automáticamente una cuenta con rol de Alumno e inicia la sesión, notificando el alta. |
| **PA-2: Pago online con billetera virtual** | Martinez Lazaro Ezequiel | • Módulo de Inscripciones<br>• Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración | **Disparador:** el alumno decide pagar el curso y confirma el pago con MODO.<br>**Pasos:** (1) el sistema genera la solicitud de pago ante MODO, indicando el monto del curso y un identificador único para esa operación, y recibe a cambio un código QR y un enlace de pago; (2) el sistema muestra el código QR en pantalla si el alumno está en una computadora, o lo redirige directamente a elegir su aplicación de pago mediante el enlace recibido si está desde el celular; (3) el alumno escanea el código o abre la aplicación elegida, selecciona su tarjeta y confirma el pago; (4) MODO le avisa al sistema si el pago fue aprobado o rechazado; (5) si fue aprobado, el sistema registra el pago, genera el comprobante automático, habilita el acceso al curso sin intervención manual del administrador y notifica al alumno el resultado. |
| **PA-3: Aplicación automática de descuentos** | Küster Joaquín | • Módulo de Inscripciones<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el alumno desea inscribirse a un curso y confirma la inscripción.<br>**Pasos:** (1) El sistema evalúa automáticamente si cumple alguna condición de descuento vigente; (2) Si la cumple, aplica el descuento al monto a pagar; (3) Descuenta una unidad de la cantidad límite ofertada; (4) Desactiva automáticamente el descuento al vencer su vigencia o al alcanzar la cantidad límite, lo que ocurra primero, y notifica al alumno el descuento aplicado. |
| **PA-4: Clases en Vivo** | Martinez Lazaro Ezequiel | • Módulo de Clases en Vivo<br>• Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración | **Disparador:** el docente dispara la acción "Iniciar clase en vivo" para la clase programada.<br>**Pasos:** (1) El sistema pasa la clase a estado En vivo y genera los datos de conexión (RTMP), que el docente carga en OBS para transmitir desde ahí; (2) El sistema recibe la señal y la transmite en simultáneo a todos los alumnos inscriptos, mientras graba automáticamente la transmisión; (3) Al finalizar la transmisión, el docente dispara la acción "Finalizar clase en vivo": el sistema pasa la clase a estado Finalizada y el video queda disponible como material de la unidad, con vencimiento configurable (aviso previo y eliminación automática programados); (4) Notifica a los alumnos inscriptos que la grabación ya está disponible. |
| **PA-5: Generación de videos Clon IA** | Küster Joaquín | • Módulo de Generación de Contenido con IA<br>• Módulo de Contenido de Unidades<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar una clase con Clon de IA y redacta el guión de una unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma.<br>**Pasos:** (1) El sistema registra la clase con Clon de IA en estado Pendiente y envía el guión de la escena junto con el identificador del Avatar ya validado del docente (registrado en HeyGen); (2) HeyGen genera el video de la escena; (3) El sistema descarga el video, actualiza el estado a Generada (o a Error si la generación falla) y lo carga como material de la unidad, en estado oculto; (4) Notifica al docente que el material está disponible para su revisión antes de publicarlo. |
| **PA-6: Emisión automática de certificados** | Küster Joaquín | • Módulo de Evaluaciones<br>• Módulo de Contenido de Unidades<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el alumno desea finalizar el curso y completar el intento de autoevaluación de la última unidad.<br>**Pasos:** (1) El sistema corrige y aprueba automáticamente el intento, y verifica que se hayan completado todas las unidades del curso; (2) Genera un número de certificado único; (3) Genera la constancia de finalización y la envía por correo electrónico, notificando al alumno; (4) La pone disponible para su consulta y descarga desde el perfil del alumno. |
| **PA-7: Generación de presentaciones de unidad** | Martinez Lazaro Ezequiel | • Módulo de Contenido de Unidades<br>• Módulo de Generación de Contenido con IA<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar una presentación automática de una unidad y solicita su generación.<br>**Pasos:** (1) El Módulo de Generación de Contenido con IA recopila la bibliografía cargada de la unidad y la envía al modelo de IA local (Ollama); (2) Ollama devuelve una estructura de contenidos (títulos, subtítulos, puntos clave); (3) El sistema da formato al resultado como una presentación descargable y la carga como material de la unidad, en estado oculto; (4) Notifica al docente que está disponible para su revisión antes de publicarla. |
| **PA-8: Generación de resúmenes de unidad** | Küster Joaquín | • Módulo de Contenido de Unidades<br>• Módulo de Generación de Contenido con IA<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar un resumen automático de una unidad a partir de su bibliografía y solicita su generación.<br>**Pasos:** (1) El Módulo de Generación de Contenido con IA recopila la bibliografía cargada de la unidad y la envía al modelo de IA local (Ollama); (2) Ollama genera un resumen estructurado del contenido de la unidad; (3) El sistema le da formato y lo carga como material de la unidad, en estado oculto; (4) Notifica al docente que está disponible para su revisión antes de publicarlo. |
| **PA-9: Generación de banco de preguntas** | Martinez Lazaro Ezequiel | • Módulo de Generación de Contenido con IA<br>• Módulo de Evaluaciones<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar un banco de preguntas automático y aporta el material de referencia de la unidad (bibliografía, glosario) y, opcionalmente, redacta el guión mediante un prompt (prompt input).<br>**Pasos:** (1) El Módulo de Generación de Contenido con IA envía el material al modelo de IA local (Ollama); (2) Ollama genera un banco de preguntas cerradas (opción múltiple y verdadero/falso) siguiendo la proporción configurada; (3) El sistema valida el formato de las preguntas generadas y las carga como pool de la unidad; (4) Notifica al docente que el pool está disponible para su revisión antes de publicarse. |

**Tabla 1. Tabla de Procesos Automatizados del Sistema.**

---

## 8. Estimación de Tamaño por Módulo

Se expresa en porcentaje con respecto al total del producto, calculado en proporción a la cantidad de funcionalidades relevadas para cada módulo en la sección 5 (35 en total, sobre los 10 módulos). La asignación de responsables busca una distribución de carga equitativa entre ambos integrantes.

Sumando, da como resultado: **Küster: 48%** (5 módulos) y **Martinez: 52%** (5 módulos). De esta forma, queda en línea con quién quedó como responsable de cada proceso automatizado en la sección 7.

| Módulo | % Participación | Responsable |
|--------|----------------|-------------|
| Módulo del Catálogo de Cursos | 14% | Martinez Lazaro Ezequiel |
| Módulo de Contenido de Unidades | 12% | Martinez Lazaro Ezequiel |
| Módulo de Inscripciones | 14% | Martinez Lazaro Ezequiel |
| Módulo de Evaluaciones | 11% | Küster Joaquín |
| Módulo de Clases en Vivo | 9% | Martinez Lazaro Ezequiel |
| Módulo de Generación de Contenido con IA | 11% | Küster Joaquín |
| Módulo de Usuarios y Notificaciones | 17% | Küster Joaquín |
| Módulo de Auditoría | 3% | Martinez Lazaro Ezequiel |
| Módulo de Reportes y Estadísticas | 6% | Küster Joaquín |
| Módulo de Configuración | 3% | Küster Joaquín |
| **Total** | **100%** | |

**Tabla 2. Tabla de Estimación de Tamaño por Módulo.**

---

## 9. Entorno Tecnológico y Metodológico

| Aspecto | Tecnología / Metodología |
|---------|--------------------------|
| Lenguajes de programación | Java, JavaScript |
| Framework | Front-End: Bootstrap<br>Back-End: Spring Boot, Thymeleaf |
| Arquitectura | Arquitectura de Microservicios |
| Motor de base de datos | PostgreSQL |
| Metodología seleccionada | Proceso de Desarrollo Unificado |
| Tipo de proyecto | Con Cliente Final |

**Tabla 3. Tabla de Entorno Tecnológico y Metodológico.**

---

## 10. Planificación de Actividades

El cronograma sigue las fases de UP (Requisitos, Análisis, Diseño), alineado a las plantillas de la cátedra y a la tabla de planificación de Sommerville, con un mínimo de una semana de duración por tarea (Sommerville, 9ª ed., pág. 626).

Se aplicó también el margen de contingencia de Sommerville (30%-50% sobre la estimación inicial, pág. 627), concentrado en la tarea de mayor esfuerzo (Programación), dado que la fecha límite es acotada.

| Tarea | Actividad | Duración en días | Plazo aprox. | Predecesor |
|-------|-----------|------------------|--------------|------------|
| **Planificación y relevamiento** | | | | |
| 1 | Planificación de actividades | 2 | 31/08/2026 - 01/09/2026 | - |
| 2 | Coordinación y entrevista con el cliente | 3 | 02/09/2026 - 04/09/2026 | 1 |
| 3 | Informe de relevamiento | 2 | 05/09/2026 - 06/09/2026 | 2 |
| **Factibilidad y análisis de riesgos** | | | | |
| 4 | Estudio de factibilidad | 4 | 07/09/2026 - 10/09/2026 | 3 |
| 5 | Análisis de riesgos | 3 | 11/09/2026 - 13/09/2026 | 4 |
| **Requisitos** | | | | |
| 6 | Descripción del escenario y objetivos del sistema | 2 | 14/09/2026 - 15/09/2026 | 5 |
| 7 | Requisitos funcionales y no funcionales | 2 | 16/09/2026 - 17/09/2026 | 6 |
| 8 | Diagrama de casos de uso | 3 | 18/09/2026 - 20/09/2026 | 7 |
| 9 | Casos de uso extendidos | 5 | 21/09/2026 - 25/09/2026 | 8 |
| 10 | Matriz de rastreabilidad | 2 | 26/09/2026 - 27/09/2026 | 9 |
| **Análisis** | | | | |
| 11 | Modelo de dominio | 3 | 28/09/2026 - 30/09/2026 | 10 |
| 12 | Diagramas de secuencia del sistema y contratos | 4 | 01/10/2026 - 04/10/2026 | 11 |
| **Diseño** | | | | |
| 13 | Diseño de interfaz y casos de uso reales | 2 | 05/10/2026 - 06/10/2026 | 12 |
| 14 | Diagrama de secuencia de diseño | 2 | 07/10/2026 - 08/10/2026 | 13 |
| 15 | Diagrama de clases | 2 | 09/10/2026 - 10/10/2026 | 14 |
| 16 | Diseño de base de datos | 2 | 11/10/2026 - 12/10/2026 | 15 |
| **Programación** | | | | |
| 17 | Programación | 32 | 13/10/2026 - 13/11/2026 | 16 |
| **Pruebas** | | | | |
| 18 | Diseño de casos de prueba | 4 | 14/11/2026 - 17/11/2026 | 17 |
| 19 | Ejecución de pruebas | 3 | 18/11/2026 - 20/11/2026 | 18 |
| | **Días en total:** | **82** | **31/08/2026 - 20/11/2026** | |

**Tabla 4. Tabla de Planificación de Actividades.**