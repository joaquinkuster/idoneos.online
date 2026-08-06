# Trabajo Final (ASC) - Proyecto Software (LSI) - FCEQyN - UNaM
## Sistema de Gestión Idóneos Online - Küster Joaquín - Martinez Lazaro Ezequiel

# Sistema de Gestión Idóneos Online
## Plataforma de Cursos Online de Finanzas, Economía y Mercado de Capitales
### Idóneos Online S.A.S. · Argentina

Apóstoles (Misiones), 9 de abril de 2026

---

**Al Prof. Adjunto de las cátedras**
**Trabajo Final (ASC) | Proyecto Software (LSI)**
**FCEQyN - UNaM**

**Lic. Sergio Daniel Caballero**
S/D

---

De nuestra mayor consideración:

Nos dirigimos a Ud. a efectos de presentar la propuesta para el desarrollo de un producto software como tema para las cátedras "Proyecto Software" de la carrera Licenciatura en Sistemas de Información (plan de estudios 2013) y "Trabajo Final" de la carrera Analista en Sistemas de Computación (plan de estudios 2010).

El producto software a desarrollar se denomina: **Sistema de Gestión Idóneos Online**.

Sin otro particular, y quedando a la espera de la evaluación de la propuesta, nos despedimos atte.

---

```
____________________________
[Küster Joaquín]
[906560 — Analista de Sistemas de Información]
[44652101]

____________________________
[Martinez Lazaro Ezequiel]
[906047 — Analista de Sistemas de Información]
[42086981]
```

---

## 1. Evaluación de la Propuesta

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

| Resultado final | APROBADO - DEBE CORREGIR |
|-----------------|--------------------------|

**Firma por el equipo de cátedra:**

---

## 2. Contenidos

1. Evaluación de la Propuesta
2. Contenidos
3. Planteo del Problema
   - 3.1. El Mercado de la Formación en Finanzas y Economía en Argentina
   - 3.2. Un Vacío entre la Formación de Grado y la Especialización de Élite
   - 3.3. La Necesidad de una Solución Tecnológica Propia
4. Introducción
   - 4.1. Objetivos
5. Alcance y Limitaciones
   - 5.1. Alcance
   - 5.2. Limitaciones
6. Especificación de Módulos
   - 6.1. Módulos Funcionales
   - 6.2. Módulos No Funcionales
7. Descripción de los Módulos
   - 7.1. Módulo de Cursos
   - 7.2. Módulo de Inscripción y Pagos
   - 7.3. Módulo de Evaluación y Certificación
   - 7.4. Módulo de Clon con IA
   - 7.5. Módulo de Clases en Vivo
   - 7.6. Módulo de Generación de Contenido con IA
   - 7.7. Módulo de Usuarios y Notificaciones
   - 7.8. Módulo de Auditoría
   - 7.9. Módulo de Reportes y Estadísticas
   - 7.10. Módulo de Configuración
8. Decisiones Tecnológicas
9. Procesos Automatizados
10. Estimación de Tamaño por Módulo
11. Entorno Tecnológico y Metodológico
12. Planificación de Actividades

---

## 3. Planteo del Problema

### 3.1. El Mercado de la Formación en Finanzas y Economía en Argentina

Idóneos Online es un proyecto impulsado por dos socios: Fausto Spotorno, economista recibido en la UCA, director de la maestría de la UADE y titular de la consultora Orlando Ferreres y Asociados, quien ejercerá la dirección académica de la plataforma, y Sebastián Bordato, ideólogo del proyecto y responsable del armado inicial de la propuesta, quien además se desempeñará como uno de los docentes. El proyecto ya cuenta con CUIT y se encuentra dado de alta ante la Inspección General de Justicia (IGJ) como Sociedad Anónima Simplificada, y dispone de un sitio web preliminar desarrollado en Wix, aunque todavía no se encuentra en producción ni cuenta con la totalidad de los cursos cargados.

El público objetivo son hombres y mujeres de entre 22 y 45 años, sin exigencia de título de grado ni de conocimientos previos específicos (salvo en los cursos introductorios, que parten de cero), y focalizado exclusivamente en Argentina, dado que gran parte del contenido de los cursos (impuestos, normativa de mercado de capitales, cuestiones contables) es intrínseco a la legislación argentina. Los socios estiman alcanzar más de 2000 alumnos por año.

### 3.2. Un Vacío entre la Formación de Grado y la Especialización de Élite

El diagnóstico de mercado realizado por los socios identificó que el acceso a profesores de alto nivel académico (directores de maestrías en universidades como la UADE, la Universidad Torcuato Di Tella o la Universidad de San Andrés) estuvo históricamente restringido a quienes completaron una carrera de grado y luego accedían a una maestría, dejando fuera a un público amplio interesado en formarse en finanzas, economía y mercado de capitales sin recorrer ese camino.

Asimismo, los socios evaluaron la oferta de cursos existente en el mercado de finanzas y mercado de capitales argentino y la calificaron como deficiente: los cursos dictados por el mercado de valores (BYMA) están muy segmentados y orientados únicamente al mercado de capitales, mientras que experiencias propias de Bordato en Net Finance (otra empresa del mismo, dedicada al mercado de capitales) con cursos de terceros arrojaron resultados de baja calidad, con profesores poco preparados y diseño académico deficiente.

Frente a este panorama, Idóneos Online se propone cubrir un espacio más amplio que sus competidores (incorporando, además del mercado de capitales, contenidos de contabilidad, impuestos y eficiencia de flujo de caja) apoyándose en dos diferenciales: docentes de élite difíciles de encontrar en otras plataformas, y una metodología de tipo "Netflix", con avance secuencial por unidades y estándares de producción audiovisual (iluminación, encuadre, vestimenta) que buscan un nivel de profesionalismo no observado en otras propuestas del mercado.

### 3.3. La Necesidad de una Solución Tecnológica Propia

Actualmente, Idóneos Online ya cuenta con una solución desarrollada en Wix, sobre la cual el cliente llegó a cargar y probar su primer curso. Sin embargo, esa solución resultó ser estática: cada curso requirió que la persona de sistemas construyera manualmente su propia página y emule el comportamiento de un área privada (donde el alumno accede sólo al contenido pagado), en lugar de generarse automáticamente a partir de la información cargada. Además, las plantillas predefinidas de Wix no permiten integrar de forma unificada las herramientas usadas para las tres modalidades de dictado (en vivo, con Clon de IA y grabado) ni el avance secuencial por unidades, por lo que cada integración quedó resuelta de forma aislada; el cliente valoró la posibilidad de unificarlas en la herramienta propuesta.

Esto hace que cada curso nuevo dependa de la intervención de la persona de sistemas en lugar de que el equipo docente lo cargue de forma autónoma, y que la información de alumnos, tráfico o pagos quede atada a cómo se construyó cada página, sin datos centralizados. Este esquema funciona con un catálogo reducido, pero dificulta escalar a medida que crezcan los cursos. Por eso, el proyecto no busca reemplazar Wix sino resolver estas limitaciones con un sistema propio, más dinámico, administrable y escalable, donde la administración pueda cargar y publicar cursos sin depender de intervención técnica. A esto se suma que, al no estar en producción, tampoco existe un método validado de pagos ni mecanismos de trazabilidad ante fallas de acreditación o uso indebido de credenciales, por lo que los socios buscan un desarrollo propio, con código fuente propio, que sostenga el crecimiento del proyecto a mediano y largo plazo.

---

## 4. Introducción

En el presente documento se definen los objetivos, alcances y limitaciones del sistema de información propuesto para dar soporte a la operación académica, comercial y administrativa de Idóneos Online, una plataforma de cursos online de finanzas, economía y mercado de capitales orientada al público argentino. La iniciativa articula la dimensión técnica del sistema con el modelo de negocio del proyecto: el acceso a docentes de élite mediante cursos estructurados, la sostenibilidad económica a través de la venta de cursos y el uso de inteligencia artificial para escalar la producción de contenido académico.

Idóneos Online constituye un espacio de formación no universitaria en el que un cuerpo docente de alto nivel académico (encabezado por el director académico del proyecto) dicta cursos en tres modalidades posibles: en vivo, grabada tradicional y mediante un Clon con inteligencia artificial que reproduce la imagen y la voz reales del docente. Además de su rol formativo, el proyecto busca posicionarse como una alternativa profesional y accesible frente a la oferta actual del mercado de finanzas y economía en Argentina.

El sistema propuesto (denominado en adelante Sistema Idóneos Online) surge como solución a las limitaciones detectadas en la plataforma Wix actualmente utilizada por el cliente. Su implementación permitirá gestionar los cursos (desde su estructuración en unidades hasta la evaluación y certificación de los alumnos), automatizar el flujo de inscripción y pago, incorporar la generación de clases mediante Clon con IA, y dar soporte a clases en vivo con grabación y vencimiento automático.

La solución será accesible desde cualquier dispositivo con conexión a internet (celular, PC o notebook) a través de un navegador web, contemplando distintos perfiles de usuario: la administración y el cuerpo docente, y los alumnos que se inscriben a los cursos.

### 4.1. Objetivos

El sistema se propone dar respuesta a las carencias detectadas, aportando las siguientes funcionalidades clave. Cada objetivo está alineado a uno de los diez módulos descritos en las secciones 6 y 7:

- **Gestionar los cursos.** Administra el catálogo de cursos y su estructura interna: alta y edición de cursos y categorías, armado de cada curso en un mínimo de 10 unidades secuenciales (grabación, bibliografía, presentación, resumen, glosario y autoevaluación), y las tres modalidades de dictado disponibles (en vivo, con Clon de IA y grabada), incluyendo la variante "Supervisado" aplicable a cualquiera de ellas. El sistema clasifica automáticamente como "curso con Clon" a todo curso en el que más del 50% de sus unidades sea dictado mediante Clon de IA, informando al alumno antes de inscribirse.

- **Gestionar la inscripción y el pago de los alumnos.** Gestiona la inscripción de los alumnos a los cursos y su pago mediante tarjeta de crédito o débito, con generación automática de comprobantes y aplicación de descuentos según las condiciones vigentes, para garantizar la sostenibilidad económica del proyecto.

- **Evaluar y certificar a los alumnos.** Evalúa el avance de los alumnos mediante autoevaluaciones automáticas por unidad, a partir del pool de preguntas de cada unidad, y emite la constancia de finalización al completar el curso, para certificar el progreso académico alcanzado.

- **Generar clases con Clon de inteligencia artificial.** Genera clases dictadas por un Clon de inteligencia artificial a partir del guión que el docente redacta mediante un prompt ingresado desde la plataforma, mediante integración con HeyGen, previa validación de identidad y consentimiento del docente para el uso de su imagen y voz (proceso de "Avatar Consent" que HeyGen ejecuta directamente en su propia plataforma, mediante una breve grabación en video, y que queda fuera del alcance del sistema propuesto).

- **Gestionar las clases en vivo.** Gestiona la transmisión de clases en vivo mediante integración con OBS, con ingreso simultáneo de los alumnos inscriptos y grabación automática disponible por un plazo configurable, para dar soporte a la modalidad de cursada sincrónica.

- **Generar contenido académico con inteligencia artificial.** Genera, mediante un modelo de inteligencia artificial ejecutado localmente (Ollama), contenido académico de apoyo a partir del material cargado por los docentes (bancos de preguntas, resúmenes de la bibliografía de cada unidad y presentaciones), para reducir el tiempo que dedican a su preparación.

- **Gestionar usuarios, autenticación y notificaciones.** Gestiona los usuarios del sistema (alumnos, docentes y administradores), su autenticación mediante usuario y contraseña o Google OAuth y el control de sesiones concurrentes, y notifica por correo electrónico y dentro de la plataforma los eventos relevantes a cada actor.

- **Registrar las acciones críticas del sistema.** Registra automáticamente en un log de auditoría las acciones críticas del sistema (pagos, acreditaciones, altas de curso y cambios de estado de inscripción), para garantizar trazabilidad y control sobre las operaciones, en particular sobre los pagos.

- **Generar reportes y estadísticas de gestión.** Genera reportes y estadísticas de alumnos inscriptos, tráfico de la plataforma e ingresos por pagos, para facilitar la toma de decisiones estratégicas de los socios.

- **Permitir la configuración de los parámetros operativos.** Permite a la administración ajustar los parámetros operativos del sistema (plazo de disponibilidad de grabaciones, sesiones concurrentes permitidas, datos institucionales y credenciales de integración, entre otros) sin intervención técnica sobre el código.

---

## 5. Alcance y Limitaciones

En esta sección se presentan los límites funcionales del sistema, definiendo hasta dónde llegará la solución propuesta. Se describen las áreas que cubrirá el sistema, así como los aspectos que quedan fuera de su alcance según lo relevado con el cliente.

### 5.1. Alcance

El sistema cubre el ciclo completo de la propuesta académica de la empresa, desde la carga de un curso hasta la certificación del alumno, contemplando tres perfiles de usuario:

- **Alumno.** Explora el catálogo de cursos, se inscribe (dentro del período habilitado) y paga el curso elegido, cursa con avance secuencial por unidades durante el plazo de acceso definido para ese curso, resuelve las autoevaluaciones correspondientes y obtiene la constancia de finalización al completar el curso.

- **Docente.** Carga y estructura sus cursos en unidades, dicta las clases en cualquiera de las 3 modalidades soportadas (en vivo, grabada o mediante Clon de inteligencia artificial) y se apoya en las herramientas de generación de contenido con IA para preparar bancos de preguntas, resúmenes y presentaciones.

- **Administración.** Gestiona los usuarios del sistema, hace seguimiento del negocio mediante reportes y estadísticas de alumnos, tráfico e ingresos, cuenta con trazabilidad de las operaciones críticas a través del log de auditoría, y ajusta los parámetros operativos del sistema sin intervención técnica.

El detalle funcional de cada uno de estos puntos (altas, bajas, modificaciones y consultas de cada entidad) se desarrolla módulo por módulo en la sección 7.

### 5.2. Limitaciones

- **Reembolsos.** Al tratarse de cursos de nivel académico, se aplica una política de no reembolso análoga a la habitual en el ámbito universitario, donde el pago no es reembolsable una vez realizado. El sistema no contemplará un mecanismo de solicitud ni devolución de pagos, en línea con lo confirmado por el cliente durante la entrevista.

- **Clases presenciales.** Toda la propuesta académica de la empresa se dicta bajo modalidad virtual (en vivo, grabada o con Clon de IA). El sistema no contemplará la gestión de encuentros presenciales.

- **Facturación fiscal.** El sistema emitirá únicamente un comprobante automático al confirmarse el pago. La emisión de factura A o B se gestionará de forma manual por correo electrónico ante solicitud del alumno, sin integración con AFIP ni con organismos fiscales.

- **Firmas electrónicas o digitales certificadas.** La constancia de finalización de curso emitida por el sistema tiene valor informativo e institucional, pero no incorporará mecanismos de firma digital certificada.

- **Contabilidad institucional.** El sistema no se integrará con herramientas de contabilidad de la sociedad ni gestionará los libros contables de la empresa.

- **Gestión de sueldos y recursos humanos.** El sistema no contemplará la liquidación de haberes de los socios ni de eventual personal contratado.

- **Validación oficial de cursos ante organismos.** Por ejemplo, la Comisión Nacional de Valores. El cliente manifestó estar trabajando en que ciertos cursos obtengan reconocimiento de organismos gubernamentales, pero se trata de una gestión externa e institucional, ajena a las funcionalidades del sistema. Se plantea como una mejora a futuro que el sistema pueda emitir certificados con validez oficial una vez que esas gestiones se concreten.

---

## 6. Especificación de Módulos

En esta sección se detallan los módulos que componen el sistema. Cada módulo corresponde a una funcionalidad o característica específica del sistema, diseñada para cumplir con los objetivos planteados. Se distinguen módulos funcionales (unidades lógicas, autónomas y reutilizables que agrupan funciones y datos específicos para realizar una tarea particular) y módulos no funcionales (que establecen las propiedades de calidad transversales del sistema).

### 6.1. Módulos Funcionales

Los siguientes módulos constituyen el comportamiento observable del sistema. Cada uno responde a uno o más objetivos específicos:

- Módulo de Cursos.
- Módulo de Inscripción y Pagos.
- Módulo de Evaluación y Certificación.
- Módulo de Clon con IA.
- Módulo de Clases en Vivo.
- Módulo de Generación de Contenido con IA.

### 6.2. Módulos No Funcionales

Estos módulos establecen las propiedades de calidad y control del sistema, como seguridad, auditoría y análisis:

- Módulo de Usuarios y Notificaciones.
- Módulo de Auditoría.
- Módulo de Reportes y Estadísticas.
- Módulo de Configuración.

---

## 7. Descripción de los Módulos

### 7.1. Módulo de Cursos

Este módulo constituye el núcleo académico del sistema. Gestiona los cursos, las categorías en que se organizan, las modalidades de dictado disponibles, y la estructura de cada curso en unidades secuenciales con su material, glosario y foros.

- **Gestionar cursos.** Alta, modificación, baja y consulta de cursos (nombre, descripción, precio, imagen de portada, categoría temática, modalidades de dictado asociadas, docente titular y, opcionalmente, docente supervisor, período habilitado para inscribirse y duración del acceso al contenido una vez inscripto); el sistema valida que cada curso tenga exactamente un docente titular; incluye la consulta pública de la vista de cada curso previa a la inscripción, para que el usuario decida si comprarlo, o la publicación de contenido gratuito que sirva como gancho para atraer nuevos usuarios.

- **Gestionar categorías.** Alta, modificación, baja y consulta de las categorías temáticas (nombre, descripción) en las que se organizan los cursos.

- **Gestionar unidades.** Alta, modificación, baja y consulta de las unidades que componen cada curso (título, descripción, número de orden); el número de orden determina el avance secuencial, exigiendo aprobar la autoevaluación de una unidad para habilitar el acceso a la siguiente.

- **Gestionar material.** Alta, modificación, baja y consulta del material de cada unidad, según su tipo. Todo material permite modificar su visibilidad (publicado u oculto) para el alumno:
  - **Grabación.** Ruta del archivo de video en el servidor local y su duración. Corresponde a la clase grabada, ya sea transmitida en vivo (el archivo se genera automáticamente al finalizar la transmisión vía OBS), grabada de forma tradicional (subida directamente por el docente) o generada mediante Clon de IA (descargada automáticamente desde HeyGen).
  - **Bibliografía.** Título y autor de la fuente de referencia, junto con un enlace externo o la ruta del archivo correspondiente, utilizada como material complementario de lectura.
  - **Presentación.** Ruta del archivo de la presentación (diapositivas) utilizada como apoyo visual de la clase, subida por el docente o generada automáticamente a partir del guión mediante un modelo de IA local (Módulo de Generación de Contenido con IA).
  - **Resumen.** Texto o ruta del archivo con la síntesis del contenido de la unidad, subido por el docente o generado automáticamente a partir de la bibliografía de la unidad mediante un modelo de IA local (Módulo de Generación de Contenido con IA).

- **Gestionar términos del glosario.** Alta, modificación, baja y consulta de los términos y sus definiciones asociados a cada unidad.

- **Gestionar consultas del foro.** Alta, modificación, baja y consulta de las preguntas que un alumno plantea en el foro de una unidad.

- **Gestionar respuestas del foro.** Alta, modificación, baja y consulta de las respuestas del docente (titular o supervisor) a las consultas planteadas en el foro de una unidad.

### 7.2. Módulo de Inscripción y Pagos

Este módulo gestiona el proceso mediante el cual un alumno se inscribe a un curso y realiza el pago correspondiente.

- **Gestionar inscripciones.** Alta de la inscripción de un alumno a un curso (fecha), con habilitación automática del acceso una vez confirmado el pago y vencimiento automático al cumplirse el plazo de acceso definido para ese curso, y consulta de las inscripciones realizadas; la baja de una inscripción permite registrar el abandono del alumno (con una observación opcional, por ejemplo el motivo), aunque no habilita reembolso.

- **Gestionar pagos.** Alta de un pago (monto, fecha y datos de la transacción devueltos por la pasarela) mediante Checkout API de Mercado Pago, ya sea con saldo de cuenta o con tarjeta de crédito o débito, por el total del curso; consulta de los pagos realizados.

- **Gestionar descuentos.** Alta, modificación, baja y consulta de descuentos aplicados a los alumnos (nombre, porcentaje, vigencia desde/hasta, cantidad límite ofertada, cantidad usada, y cantidad de cursos comprados como condición de aplicación, única condición contemplada en el MVP); el sistema desactiva automáticamente un descuento al vencer su vigencia o alcanzar la cantidad límite, lo que ocurra primero.

- **Gestionar comprobantes.** Alta automática de un comprobante al confirmarse un pago (número, fecha de emisión, archivo descargable), con envío por correo electrónico al alumno; consulta y descarga del comprobante emitido.

- **Gestionar progreso.** Registro automático del avance del alumno por unidad (si fue completada y la fecha en que se completó), asociado a la inscripción correspondiente; consulta del progreso alcanzado por el alumno en cada curso.

### 7.3. Módulo de Evaluación y Certificación

Este módulo gestiona las autoevaluaciones de cada unidad, los intentos de los alumnos y la emisión de la constancia de finalización del curso.

- **Gestionar pool.** Alta, modificación, baja y consulta del pool de preguntas de cada unidad (nombre), generado por el docente o mediante un modelo de IA local como método alternativo (Módulo de Generación de Contenido con IA); incluye las preguntas (texto, tipo opción múltiple o verdadero/falso) y sus opciones de respuesta (texto, si es correcta o no).

- **Gestionar autoevaluaciones.** Alta, modificación, baja y consulta de autoevaluaciones (nombre), asociadas a uno o varios pools de preguntas de los que sortea las preguntas de cada intento; permite tanto autoevaluaciones acotadas a una unidad como evaluaciones finales que integran los pools de varias unidades de un curso.

- **Gestionar intentos de autoevaluación.** Alta de un intento por autoevaluación (fecha, nota), con selección aleatoria de 10 preguntas cerradas de los pools asociados, registro de la opción elegida por el alumno para cada pregunta sorteada, corrección automática y reintento obligatorio del cuestionario completo si no se aprueban todas las respuestas; consulta del historial de intentos por alumno.

- **Gestionar certificados.** Alta automática de la constancia de finalización (número de certificado, fecha de emisión, archivo descargable) al aprobar la autoevaluación que completa el curso, asociada a la inscripción del alumno, enviada por correo electrónico; consulta y descarga del certificado emitido.

### 7.4. Módulo de Clon con IA

Este módulo gestiona la generación de clases dictadas por un Clon con inteligencia artificial, que reproduce la imagen y la voz reales de un docente a partir de un guión, mediante integración con la plataforma HeyGen. Está orientado a docentes de alto nivel académico con disponibilidad horaria limitada.

- **Gestionar habilitación de Clon.** Alta y baja del estado de habilitación de un docente para dictar clases con Clon de IA, según la validación de Avatar Consent en HeyGen; consulta del estado.

- **Generar clase con Clon de IA.** El docente (titular o supervisor del curso) redacta el guión de la clase para una unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma y dispara esta acción; el sistema registra la clase en estado Pendiente y envía el guión junto con el Avatar ya validado del docente a HeyGen, mediante integración con esa plataforma; al recibir el video generado, el sistema actualiza el estado a Generada (o a Error si la generación falla) y lo carga como material de tipo Grabación de la unidad correspondiente (Módulo de Cursos); consulta del estado y de las clases generadas por unidad.

### 7.5. Módulo de Clases en Vivo

Este módulo gestiona el dictado de clases en vivo mediante integración con el software OBS, y la disponibilidad temporal de la grabación resultante en la plataforma.

- **Programar clase en vivo.** Alta, modificación, baja y consulta de las clases en vivo programadas por el docente (titular o supervisor) para una unidad (título, fecha y hora), quedando en estado Programada.

- **Iniciar clase en vivo.** Al llegar el horario programado, el docente dispara esta acción y el sistema pasa la clase a estado En vivo y genera los datos de conexión (URL RTMP y clave de stream); el docente los carga en OBS para transmitir desde ahí, y el sistema recibe esa señal y la redistribuye simultáneamente a todos los alumnos inscriptos mediante el enlace de conexión.

- **Finalizar clase en vivo.** Al concluir la transmisión, el sistema pasa la clase a estado Finalizada y genera la grabación resultante, que queda cargada como material de tipo Grabación de la unidad correspondiente (Módulo de Cursos).

Al finalizar la transmisión, la grabación queda disponible por un plazo de 4 meses (configurable), con aviso automático antes del vencimiento y eliminación automática al cumplirse el plazo.

Cabe aclarar que, según el cliente, la configuración e instalación del software OBS para cada docente está a cargo del equipo de sistemas de la empresa, previo a la transmisión.

### 7.6. Módulo de Generación de Contenido con IA

Este módulo utiliza un modelo de inteligencia artificial ejecutado localmente mediante Ollama, para generar automáticamente contenido académico de apoyo a partir del material cargado por los docentes: bancos de preguntas para las autoevaluaciones, resúmenes de la bibliografía de cada unidad y presentaciones.

- **Generar banco de preguntas.** El docente aporta el material de referencia de la unidad (bibliografía, glosario) y, opcionalmente, redacta el guión de la clase mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma; el sistema envía ese contenido al modelo de IA local (Ollama), que genera un banco de preguntas cerradas (opción múltiple y verdadero/falso) siguiendo la proporción configurada y lo devuelve al sistema, quedando cargado como pool de preguntas de la unidad (Módulo de Evaluación y Certificación).

- **Generar resumen de unidad.** El docente solicita resumir el contenido de una unidad; el sistema envía la bibliografía cargada al modelo de IA local (Ollama), que genera un resumen estructurado del contenido y lo devuelve al sistema, quedando cargado como material de tipo Resumen de la unidad correspondiente (Módulo de Cursos).

- **Generar presentación de clase.** El docente redacta el guión de la unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma; el sistema lo envía al modelo de IA local (Ollama), que devuelve una estructura de contenidos (títulos, subtítulos, puntos clave), y el sistema le da formato como presentación descargable, quedando cargada como material de tipo Presentación de la unidad correspondiente (Módulo de Cursos).

### 7.7. Módulo de Usuarios y Notificaciones

Este módulo administra el acceso al sistema, las funcionalidades disponibles para cada tipo de usuario según su rol, y el envío de notificaciones sobre los distintos eventos del sistema, asegurando que cada actor reciba la información relevante en el momento oportuno.

- **Gestionar alumnos.** Alta, modificación, baja y consulta de alumnos (nombre, correo electrónico, contraseña o cuenta de Google).

- **Gestionar docentes.** Alta, modificación, baja y consulta de docentes (nombre, correo electrónico, teléfono, biografía, años de experiencia, título o títulos universitarios o de posgrado con su matrícula profesional cuando corresponde, matrícula del Registro de Idóneos de la Comisión Nacional de Valores cuando aplica, cursos asignados y estado de habilitación para Clon de IA); el alta la realiza el administrador de forma manual, verificando el título declarado contra el Registro Público de Graduados Universitarios o, en su defecto, la matrícula profesional o de la CNV informada; el administrador puede habilitar o suspender temporalmente a un docente (por ejemplo, ante una suspensión por mala praxis) sin eliminar su cuenta ni su historial.

- **Gestionar administradores.** Alta, modificación, baja y consulta de administradores (nombre y correo electrónico).

- **Autenticación de usuarios.** Inicio de sesión mediante usuario y contraseña o Google OAuth como método alternativo, con validación de la cuenta por correo electrónico al registrarse.

- **Gestionar sesiones.** Alta automática de una sesión al iniciar sesión (token, fecha de inicio, IP, dispositivo); cierre individual o automático de una sesión (fecha de fin); consulta de las sesiones activas de un usuario; el sistema limita la cantidad de sesiones concurrentes permitidas por usuario, para mitigar el uso compartido de credenciales detectada como riesgo por el cliente.

- **Envío de notificaciones.** Envío automático de notificaciones ante los eventos del sistema (validación de cuenta, confirmación de pago, habilitación de acceso al curso, pagos no acreditados (ya sea por rechazo del pago o por demora en la confirmación de la pasarela), vencimiento próximo de una grabación, finalización de un curso con envío de constancia, y publicación de nuevas unidades o cursos), enviadas por correo electrónico.

### 7.8. Módulo de Auditoría

Este módulo registra de forma automática y continua las acciones relevantes realizadas dentro del sistema, con especial foco en el circuito de pagos, generando un historial que permite rastrear qué ocurrió, cuándo y sobre qué operación, ante la necesidad de trazabilidad manifestada por el cliente.

- **Gestionar registros de auditoría.** Alta automática de un registro ante cada operación crítica del sistema (crear, modificar, eliminar o consultar) sobre una entidad relevante (por ejemplo, pagos, inscripciones o cursos), con el usuario responsable, el tipo de acción, la entidad afectada, el identificador del registro puntual y la fecha y hora exacta; consulta restringida al administrador.

### 7.9. Módulo de Reportes y Estadísticas

Este módulo permite generar informes consolidados sobre los distintos procesos del sistema, orientados a la toma de decisiones de los socios del proyecto.

- **Generar reportes.** Alta automática de un registro (tipo de reporte, fecha, usuario que lo generó) al generar un reporte de alumnos inscriptos, tráfico de la plataforma o ingresos por pagos; consulta del historial de reportes generados.

- **Consultar estadísticas.** Consulta en pantalla de los indicadores del sistema (alumnos inscriptos, tráfico e ingresos), sin necesidad de generar un reporte.

### 7.10. Módulo de Configuración

Este módulo permite a la administración ajustar los parámetros operativos del sistema para adaptarlo a las necesidades de Idóneos Online sin requerir intervención técnica sobre el código.

- **Gestionar parámetros.** Alta, modificación, baja y consulta de parámetros operativos del sistema mediante un esquema clave-valor (clave, valor), que permite incorporar nuevos parámetros sin modificar el esquema de base de datos. Los parámetros configurados en el MVP incluyen: plazo de disponibilidad de grabaciones y antelación del aviso previo; cantidad máxima de sesiones concurrentes por usuario; datos institucionales utilizados en el sitio, comprobantes y constancias (razón social, CUIT, domicilio, logo, email de contacto y teléfono de contacto); credenciales de integración con servicios externos (Google OAuth y Mercado Pago); plazo máximo de espera para la confirmación de un pago antes de registrarlo como incidencia pendiente; proporción de tipos de pregunta (opción múltiple / verdadero-falso) en los bancos generados con IA; y tiempo límite de edición de consultas y respuestas del foro.

---

## 8. Decisiones Tecnológicas

Esta sección reúne los fundamentos de las decisiones tecnológicas y de diseño mencionadas en la sección 7, para no interrumpir la lectura funcional de cada módulo. Al tratarse de un proyecto académico sin presupuesto propio, se priorizaron herramientas gratuitas, ya utilizadas por el cliente o de costo variable ligado al uso real del sistema, evitando alternativas que exigieran una inversión inicial no contemplada.

- **¿Por qué almacenamiento local?** Se optó por almacenar el material (grabaciones, presentaciones y bibliografía) en el servidor local durante el desarrollo del trabajo final. Esta decisión responde a que el proyecto académico no cuenta con presupuesto para contratar un servicio de almacenamiento en la nube y, además, las pruebas de funcionamiento se realizarán con un volumen reducido de datos de prueba, muy inferior a la demanda estimada de 2.000 alumnos por año para un entorno de producción. En este contexto, el almacenamiento local resulta suficiente para demostrar el funcionamiento del sistema. Esta decisión se plantea como un riesgo a evaluar en el análisis de riesgos (sección 12), ya que la puesta en producción y un crecimiento de la demanda requerirán adoptar una solución de almacenamiento distribuido.

- **¿Por qué Mercado Pago?** Se eligió Mercado Pago porque es el método que el propio cliente identificó como necesario para el lanzamiento junto con la tarjeta de crédito/débito, y porque no exige una inversión inicial: cobra una comisión sobre cada cobro efectivamente acreditado, en lugar de un costo fijo de alta o mantenimiento, algo relevante para un proyecto sin presupuesto propio. Además, permite unificar en una misma API tanto el pago con saldo de cuenta como con tarjeta, evitando integrar más de una pasarela. *Checkout API - Mercado Pago Developers*

- **¿Por qué generar el contenido académico con modelos de IA locales (Ollama)?** El banco de preguntas, el resumen de cada unidad (a partir de su bibliografía) y la presentación se generan con un modelo local ejecutado mediante Ollama, en lugar de una API externa. Así el contenido académico de los cursos no sale de la red local, sin costo por uso, y un modelo como Llama 3.1 (8B de parámetros, 128K de contexto) corre en el mismo servidor sin GPU dedicada, a una velocidad adecuada para una generación asincrónica que el docente solicita y espera. *Ollama - llama 3.1 - Meta AI - Introducing Llama 3.1*

- **¿Por qué Ollama y no otra alternativa local, como LM Studio?** Ambas son gratuitas, incluso para uso comercial, y permiten exponer un modelo local mediante una API REST sin depender de una GPU dedicada. Se eligió Ollama porque, al ser de código abierto, no depende de que una empresa decida cambiar las condiciones de uso: LM Studio exigió una licencia comercial para uso empresarial hasta julio de 2025, y recién después la eliminó. Con Ollama ese riesgo no existe, ya que el código es público y se puede seguir usando y manteniendo sin depender de una decisión comercial de terceros, el mismo tipo de cambio que ya se dio con Gemini, cuyos modelos Pro dejaron de ser gratuitos en abril de 2026. *Ollama - LM Studio is free for use at work*

- **¿Por qué OBS?** Es la herramienta gratuita que el cliente ya utiliza para las clases en vivo, y compone la señal en escenas propias (cámara, diapositivas) transmitiendo vía RTMP directo al servidor de Idóneos Online. Esto permite guardar la clase automáticamente como material de la plataforma, para que los alumnos que no pudieron asistir la vean después. Zoom limita su plan gratuito a 40 minutos por reunión y no ofrece grabación en la nube (sólo local, en la computadora del docente); Google Meet directamente no ofrece grabación en cuentas personales gratuitas. En ambos casos hay que pagar un plan superior para tener la clase disponible automáticamente en una plataforma propia. *OBS Project - Zoom - Pricing Plans - Google Workspace - Pricing - Google Meet Help Community - free account 60-minute limit*

- **¿Por qué HeyGen y no un modelo de clonación local?** Se investigaron alternativas de código abierto para generar los Clones (SadTalker, Wav2Lip), pero exigen una GPU dedicada (6-8GB de VRAM como mínimo, con un consumo que además crece con la duración del video) muy por encima de lo que necesita Ollama, por lo que no es técnicamente viable para el proyecto. Wav2Lip, además, restringe su uso a fines no comerciales, incompatible con la operación real de Idóneos Online. HeyGen integra en un solo servicio la clonación de voz, la generación de video y la validación de identidad del docente (Avatar Consent), y ya lo paga el cliente, independientemente del proyecto de software. *GitHub - OpenTalker/SadTalker - GitHub - Rudrabha/Wav2Lip - HeyGen - Pricing*

- **¿Por qué Google OAuth?** Se eligió Google OAuth porque el cliente ya centraliza en Gmail buena parte de su operación (validación de cuentas, confirmaciones de pago, comunicación con alumnos y docentes, según lo relevado en la entrevista), y porque su integración no tiene costo. Permite además que el alumno reutilice una cuenta de Gmail que ya posee en lugar de crear una contraseña nueva, reduciendo la fricción del registro. *Identity Platform pricing | Google Cloud*

---

## 9. Procesos Automatizados

A continuación se describen los procesos automatizados del sistema. Cada proceso es considerado un módulo inteligente en tanto genera valor agregado real para el proyecto, no forma parte de la versión mínima viable (MVP) del sistema, trasciende la lógica de una simple consulta a la base de datos, se compone de una secuencia de 4 a 5 pasos encadenados y requiere la intervención articulada de al menos dos o tres módulos del sistema.

| Denominación | Módulos que intervienen | Descripción |
|--------------|-------------------------|-------------|
| **PA-1: Login con Google**<br>*Küster Joaquín* | • Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración (credenciales de OAuth parametrizables) | **Disparador:** el usuario desea iniciar sesión sin crear una cuenta nueva y selecciona "Ingresar con Google".<br><br>**Pasos:** (1) El sistema redirige al usuario a la pantalla de inicio de sesión de Google, solicitando permiso para acceder a su correo electrónico y datos básicos de perfil; (2) Google devuelve un token de acceso validado; (3) El sistema busca una cuenta asociada al correo recibido; (4) Si no existe, crea automáticamente una cuenta con rol de Alumno e inicia la sesión, notificando el alta. |
| **PA-2: Pago con tarjeta integrado**<br>*Martinez Lazaro Ezequiel* | • Módulo de Inscripción y Pagos<br>• Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración | **Disparador:** el alumno desea pagar el curso e ingresa los datos de su tarjeta de crédito o débito directamente en la plataforma.<br><br>**Pasos:** (1) El alumno ingresa los datos de su tarjeta en el formulario de la plataforma; (2) Mercado Pago valida los datos de la tarjeta; (3) el sistema envía la operación a la Checkout API de Mercado Pago para procesar el pago; (4) Mercado Pago valida los fondos y la autorización del banco emisor, y confirma o rechaza el pago; (5) si fue aprobado, el sistema registra el pago, genera el comprobante automático, habilita el acceso al curso sin intervención manual del administrador y notifica al alumno el resultado. |
| **PA-3: Aplicación automática de descuentos**<br>*Küster Joaquín* | • Módulo de Inscripción y Pagos<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el alumno desea inscribirse a un curso y confirma la inscripción.<br><br>**Pasos:** (1) El sistema evalúa automáticamente si cumple alguna condición de descuento vigente; (2) Si la cumple, aplica el descuento al monto a pagar; (3) Descuenta una unidad de la cantidad límite ofertada; (4) Desactiva automáticamente el descuento al vencer su vigencia o al alcanzar la cantidad límite, lo que ocurra primero, y notifica al alumno el descuento aplicado. |
| **PA-4: Clases en Vivo**<br>*Martinez Lazaro Ezequiel* | • Módulo de Clases en Vivo<br>• Módulo de Usuarios y Notificaciones<br>• Módulo de Configuración | **Disparador:** el docente dispara la acción "Iniciar clase en vivo" para la clase programada.<br><br>**Pasos:** (1) El sistema pasa la clase a estado En vivo y genera los datos de conexión (RTMP), que el docente carga en OBS para transmitir desde ahí; (2) El sistema recibe la señal y la transmite en simultáneo a todos los alumnos inscriptos, mientras graba automáticamente la transmisión; (3) Al finalizar la transmisión, el docente dispara la acción "Finalizar clase en vivo": el sistema pasa la clase a estado Finalizada y el video queda disponible como material de la unidad, con vencimiento configurable (aviso previo y eliminación automática programados); (4) Notifica a los alumnos inscriptos que la grabación ya está disponible. |
| **PA-5: Generación de videos Clon IA**<br>*Küster Joaquín* | • Módulo de Clon con IA<br>• Módulo de Cursos<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar una clase con Clon de IA y redacta el guión de una unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma.<br><br>**Pasos:** (1) El sistema registra la clase con Clon de IA en estado Pendiente y envía el guión de la escena junto con el identificador del Avatar ya validado del docente (registrado en HeyGen); (2) HeyGen genera el video de la escena; (3) El sistema descarga el video, actualiza el estado a Generada (o a Error si la generación falla) y lo carga como material de la unidad, en estado oculto; (4) Notifica al docente que el material está disponible para su revisión antes de publicarlo. |
| **PA-6: Emisión automática de certificados**<br>*Küster Joaquín* | • Módulo de Evaluación y Certificación<br>• Módulo de Cursos<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el alumno desea finalizar el curso y completar el intento de autoevaluación de la última unidad.<br><br>**Pasos:** (1) El sistema corrige y aprueba automáticamente el intento, y verifica que se hayan completado todas las unidades del curso; (2) Genera un número de certificado único; (3) Genera la constancia de finalización y la envía por correo electrónico, notificando al alumno; (4) La pone disponible para su consulta y descarga desde el perfil del alumno. |
| **PA-7: Generación de presentaciones para clases**<br>*Martinez Lazaro Ezequiel* | • Módulo de Cursos<br>• Módulo de Generación de Contenido con IA<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar una presentación automática y redacta el guión de la unidad mediante un prompt, ingresado desde un campo de texto (prompt input) de la plataforma.<br><br>**Pasos:** (1) El Módulo de Generación de Contenido con IA envía el guión al modelo de IA local (Ollama); (2) Ollama devuelve una estructura de contenidos (títulos, subtítulos, puntos clave); (3) El sistema da formato al resultado como una presentación descargable y la carga como material de la unidad, en estado oculto; (4) Notifica al docente que está disponible para su revisión antes de publicarla. |
| **PA-8: Generación de resúmenes de unidad**<br>*Küster Joaquín* | • Módulo de Cursos<br>• Módulo de Generación de Contenido con IA<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar un resumen automático de una unidad a partir de su bibliografía y solicita su generación.<br><br>**Pasos:** (1) El Módulo de Generación de Contenido con IA recopila la bibliografía cargada de la unidad y la envía al modelo de IA local (Ollama); (2) Ollama genera un resumen estructurado del contenido de la unidad; (3) El sistema le da formato y lo carga como material de la unidad, en estado oculto; (4) Notifica al docente que está disponible para su revisión antes de publicarlo. |
| **PA-9: Generación de banco de preguntas**<br>*Martinez Lazaro Ezequiel* | • Módulo de Generación de Contenido con IA<br>• Módulo de Evaluación y Certificación<br>• Módulo de Usuarios y Notificaciones | **Disparador:** el docente desea generar un banco de preguntas automático y aporta el material de referencia de la unidad (bibliografía, glosario) y, opcionalmente, redacta el guión mediante un prompt (prompt input).<br><br>**Pasos:** (1) El Módulo de Generación de Contenido con IA envía el material al modelo de IA local (Ollama); (2) Ollama genera un banco de preguntas cerradas (opción múltiple y verdadero/falso) siguiendo la proporción configurada; (3) El sistema valida el formato de las preguntas generadas y las carga como pool de la unidad; (4) Notifica al docente que el pool está disponible para su revisión antes de publicarse. |

**Tabla 1. Tabla de Procesos Automatizados del Sistema.**

---

## 10. Estimación de Tamaño por Módulo

Se expresa en porcentaje con respecto al total del producto, calculado en proporción a la cantidad de funcionalidades relevadas para cada módulo en la sección 7 (34 en total, sobre los 10 módulos). La asignación de responsables busca una distribución de carga equitativa entre ambos integrantes.

Sumando, da como resultado: Küster: 50% (5 módulos) y Martinez: 50% (5 módulos). De esta forma, queda en línea con quién quedó como responsable de cada proceso automatizado en la sección 9.

| Módulo | % Participación | Responsable |
|--------|:---------------:|-------------|
| Módulo de Cursos | 20% | Martinez Lazaro Ezequiel |
| Módulo de Inscripción y Pagos | 15% | Martinez Lazaro Ezequiel |
| Módulo de Evaluación y Certificación | 12% | Küster Joaquín |
| Módulo Clon con IA | 6% | Küster Joaquín |
| Módulo Clases en Vivo | 9% | Martinez Lazaro Ezequiel |
| Módulo de Generación de Contenido con IA | 9% | Küster Joaquín |
| Módulo de Usuarios y Notificaciones | 17% | Küster Joaquín |
| Módulo de Auditoría | 3% | Martinez Lazaro Ezequiel |
| Módulo de Reportes y Estadísticas | 6% | Küster Joaquín |
| Módulo de Configuración | 3% | Martinez Lazaro Ezequiel |
| **Total** | **100%** | |

**Tabla 2. Tabla de Estimación de Tamaño por Módulo.**

---

## 11. Entorno Tecnológico y Metodológico

| Aspecto | Descripción |
|---------|-------------|
| Lenguajes de programación | Java, JavaScript |
| Framework | Front-End: Bootstrap<br>Back-End: Spring Boot, Thymeleaf |
| Arquitectura | Arquitectura de Microservicios |
| Motor de base de datos | PostgreSQL |
| Metodología seleccionada | Proceso de Desarrollo Unificado |
| Tipo de proyecto | Con Cliente Final |

**Tabla 3. Tabla de Entorno Tecnológico y Metodológico.**

---

## 12. Planificación de Actividades

El cronograma sigue las fases de UP (Requisitos, Análisis, Diseño), alineado a las plantillas de la cátedra y a la tabla de planificación de Sommerville, con un mínimo de una semana de duración por tarea (Sommerville, 9ª ed., pág. 626).

Se aplicó también el margen de contingencia de Sommerville (30%-50% sobre la estimación inicial, pág. 627), concentrado en la tarea de mayor esfuerzo (Programación), dado que la fecha límite es acotada.

| Tarea | Actividad | Duración en días | Plazo aprox. | Predecesor |
|-------|-----------|:----------------:|--------------|:----------:|
| **Planificación** | | | | |
| 1 | Planificación de actividades | 7 | 13/05/2025 - 19/05/2025 | - |
| 2 | Análisis de riesgos | 7 | 20/05/2025 - 26/05/2025 | 1 |
| **Relevamiento** | | | | |
| 3 | Entrevistas y relevamiento con el cliente | 7 | 27/05/2025 - 02/06/2025 | 2 |
| 4 | Informe de relevamiento | 7 | 03/06/2025 - 09/06/2025 | 3 |
| **Factibilidad** | | | | |
| 5 | Factibilidad técnica, económica y operativa | 7 | 10/06/2025 - 16/06/2025 | 4 |
| **Requisitos** | | | | |
| 6 | Descripción del escenario | 7 | 17/06/2025 - 23/06/2025 | 5 |
| 7 | Objetivos del sistema | 7 | 24/06/2025 - 30/06/2025 | 6 |
| 8 | Requisitos funcionales y no funcionales | 7 | 01/07/2025 - 07/07/2025 | 7 |
| 9 | Diagrama de casos de uso | 7 | 08/07/2025 - 14/07/2025 | 8 |
| 10 | Casos de uso extendidos | 10 | 15/07/2025 - 24/07/2025 | 9 |
| 11 | Matriz de rastreabilidad | 7 | 25/07/2025 - 31/07/2025 | 10 |
| **Análisis** | | | | |
| 12 | Modelo de dominio | 7 | 01/08/2025 - 07/08/2025 | 11 |
| 13 | Diagramas de secuencia del sistema y contratos | 10 | 08/08/2025 - 17/08/2025 | 12 |
| **Diseño** | | | | |
| 14 | Diseño de interfaz y casos de uso reales | 10 | 18/08/2025 - 27/08/2025 | 13 |
| 15 | Diagrama de secuencia de diseño | 10 | 28/08/2025 - 06/09/2025 | 14 |
| 16 | Diagrama de clases | 7 | 07/09/2025 - 13/09/2025 | 15 |
| 17 | Diseño de base de datos | 7 | 14/09/2025 - 20/09/2025 | 16 |
| **Programación** | | | | |
| 18 | Programación | 30 | 21/09/2025 - 20/10/2025 | 17 |
| **Pruebas** | | | | |
| 19 | Plan de pruebas y casos de prueba | 7 | 21/10/2025 - 27/10/2025 | 18 |
| 20 | Depuración (ejecución de pruebas) | 10 | 28/10/2025 - 06/11/2025 | 19 |
| **Entrega** | | | | |
| 21 | Manual de usuario | 7 | 07/11/2025 - 13/11/2025 | 20 |
| 22 | Informe final del trabajo | 7 | 14/11/2025 - 20/11/2025 | 21 |
| **Días en total:** | **192** | | **13/05/2025 - 20/11/2025** | |

**Tabla 4. Tabla de Planificación de Actividades.**