# Convenciones del proyecto Idóneos

Documento de referencia del refactor general. Reúne las decisiones tomadas para nombres, estructura y arquitectura. Todo
cambio de código del refactor debe seguirlo. Si algo no está acá, se decide primero y se agrega acá.

Estado: **propuesta para revisión**. Ningún archivo de código se modificó todavía.

---

## 1. Principio de vocabulario

| Qué | Idioma | Ejemplos |
|---|---|---|
| Capas, framework y componentes de interfaz | **Inglés** | `controller`, `service`, `repository`, `model`, `config`, `util`, `advice`, `card`, `navbar`, `sidebar`, `drawer`, `board`, `empty`, `icon` |
| Dominio del negocio: entidades y verbos | **Español** | `Curso`, `Cohorte`, `Recurso`, `buscar`, `registrar`, `modificar`, `darDeBaja` |

Reglas derivadas:

- Una palabra de interfaz se escribe igual en **todas partes**: archivos, clases CSS, atributos `data-*`, variables y
  funciones de JS, comentarios y pruebas. Si la jerga es *card*, no existe «tarjeta» en ningún lado.
- Los nombres que combinan capa y dominio llevan el dominio en español y el sufijo de capa en inglés:
  `CursoController`, `CursoService`, `CargaArchivosConfig`, `TareasProgramadasConfig`.
- Excepción: los nombres que son jerga propia de Spring van completos en inglés (`SecurityConfig`).
- Excepción: `formulario` se mantiene en español (decisión del equipo).
- Los parámetros de objetos del framework van en inglés: `model`, `request`, `authentication`, `redirectAttributes`.
- Nada de rutas, plantillas, controladores ni servicios con un nombre que **no exista en el modelo**. Si el controlador es
  de `Recurso`, la ruta es `/recurso`, nunca `/material`.

---

## 2. Backend

### 2.1 Paquetes (todos en minúscula)

```
com.app.idoneos
  config/                  SecurityConfig, CargaArchivosConfig, TareasProgramadasConfig, WebConfig
    interceptor/           BloqueoDeIntentoInterceptor
  controller/              <Entidad>Controller
    advice/                (hoy «consejo»)
  service/
    <entidad>/             <Entidad>Service, <Entidad>ServiceImpl
  repository/              <Entidad>Repository
  model/                   60 entidades + enums (TipoRecurso)
  util/                    <Tema>Util
```

- Renombres de paquete: `controlador→controller`, `servicio→service`, `repositorio→repository`, `modelo→model`,
  `configuracion→config`, `utilidades→util`, `consejo→advice`. Las subcarpetas por entidad pasan a minúscula
  (`servicio.Curso` → `service.curso`).
- Sufijos: `Controlador→Controller`, `Servicio→Service`, `ServicioImpl→ServiceImpl`, `Repositorio→Repository`,
  `Configuracion→Config`, `Utilidad→Util`.
- Los nombres de campo siguen a la clase: `cursoServicio → cursoService`.
- Nombres de beans en pruebas y `@Qualifier` cambian con la clase.

### 2.2 Servicios

- Un servicio por entidad: **interfaz + `Impl`**, sin excepciones.
- Los controladores **inyectan siempre la interfaz**, nunca la `Impl`.
- Los tipos auxiliares que cruzan capas (resultados, contextos, detalles) van **anidados en la interfaz**, no en la `Impl` ni
  en archivos sueltos: `PagoService.DetallePago`, `CursoService.ContextoCurso`, `CursoService.ResumenCursos`,
  `ProgresoService.DetalleProgreso`.
- `CursoService` absorbe el cálculo del diseño del curso (hoy `DisenoCursoServicio`, una clase suelta). Un `Disenio` suelto no
  existe: es lógica de `Curso`.
- `ProgramaService` absorbe el mínimo de contenido (hoy `MinimoDeContenidoValidador`) como método de la interfaz. `Recurso`,
  `Tema` y `TemaCronograma` lo usan inyectando `ProgramaService`. `ProgramaServiceImpl` ya inyecta `TemaCronogramaService`, así que
  esa inyección es `@Lazy` para cortar el ciclo (alternativa pendiente: que el controlador llame a `modificarCronograma`).
- `UsuarioDetallesService` se mantiene con ese nombre: implementa `UserDetailsService` de Spring y llamarlo igual obligaría a
  escribir el nombre completo en cada uso.
- Nombres de métodos: verbo + criterio. `buscar`, `buscarPorPrograma`, `buscarPorCursoYCohorte`. Cada criterio debe ser un
  concepto del modelo. Ver 2.5.

### 2.3 Configuración

- `AutenticacionConfiguracion` pasa a **`SecurityConfig`** (define autorización por rol, login y cadena de filtros).
- `BloqueoDeIntentoInterceptor` se queda con ese nombre y va en `config/interceptor/`.

### 2.4 Controladores y rutas

Formato `EntidadController`. La ruta base es el **nombre de la entidad** en minúscula inicial, y el segundo tramo es el verbo:
`entidad/verbo`.

| Verbo | Uso |
|---|---|
| `buscar` | Listado. Una sola ruta por entidad; la vista cambia según el rol |
| `ver` | Detalle de un registro |
| `registrar` | Alta (`POST`) |
| `modificar` | `GET /entidad/modificar/{id}` devuelve la ventana o el formulario; `POST` con la misma ruta guarda |
| `darDeBaja` | Baja lógica (`POST /entidad/darDeBaja/{id}`) y `darDeBajaMasiva` |
| `corregir` | Corrección de un dato ya emitido (certificado) |
| `acceder`, `iniciar`, `entregar`, `pagar`, … | Verbos propios del caso de uso |

- Un criterio se escribe `buscarPor<Criterio>`; un rol, `<verbo>Por<Rol>`.
- **Listados por rol.** Se elimina `misCursos`, `misPagos` y `misCertificados`. Cada entidad tiene `GET /entidad/buscar` y el
  controlador elige la vista:
  `buscarPorAdministrador`, `buscarPorDocente`, `buscarPorAlumno` (nombres de plantilla y de método del controlador).
  `AutenticacionConfiguracion` deja de filtrar por ruta separada y pasa a filtrar por rol sobre la misma ruta.
- **Curso, vista del administrador.** Es una variante de `buscarPorAdministrador`:
  `GET /curso/buscar?vista=tabla` (por defecto, el ABM) y `?vista=cards` (las tarjetas de «Mis cursos»). La grilla de cards es
  un fragmento compartido con `buscarPorDocente`.
- **Modificar por rol.** `curso/modificar` (administrador) y `curso/modificarPorDocente` (el docente completa los datos académicos,
  hoy `completarDatos`).

#### Mapeo de rutas actuales a rutas por entidad

| Hoy | Pasa a | Nota |
|---|---|---|
| `/material` | `/recurso` | El modelo no tiene «material» |
| `/foro` (consultas) | `/consulta` | `ConsultaForo` |
| `/foro` (respuestas) | `/respuesta` | `RespuestaForo`, `/respuesta/darDeBaja` |
| `/glosario` | `/termino` | `TerminoGlosario` |
| `/intento` + `SeguimientoIntento` | `/intento` | Un solo `IntentoController` |
| `/calificacion/buscar` | `/intento/buscarPorAutoevaluacion` | Una fila por alumno y autoevaluación |
| `/calificacion/ver/{id}` | `/intento/buscarPorInscripcion/{id}` | Vista del alumno |
| `/misCursos` (alumno) | `/inscripcion/buscar` | Vista `buscarPorAlumno` |
| `/misPagos` | `/pago/buscar` | Vista `buscarPorAlumno` |
| `/misCertificados` | `/certificado/buscar` | Vista `buscarPorAlumno` |
| `modalEditar/{id}`, `modalModificar/{id}` | `modificar/{id}` | `GET` devuelve la ventana |
| `modalCertificado/{id}` | `/certificado/corregir/{id}` | |
| `/curso/misCursos` (administrador) | `/curso/buscar?vista=cards` | |
| `/curso/completarDatos` | `/curso/modificarPorDocente` | |

Excepciones documentadas (no son entidades del modelo): `/participante` (fusión de `Inscripcion` y `DocenteCurso`),
`/catalogo`, `/cronograma`, `/inicio`, `/login`, `/acercaDe`.

La etiqueta «Calificaciones» se mantiene en la interfaz; en menús y barras solo cambia el enlace.

**Foros y glosarios.** Siguen la regla de rutas por entidad: las consultas y respuestas del foro viven en `/consulta` y
`/respuesta`, y los términos del glosario en `/termino`. Los listados «por tema» usan el criterio del modelo
(`buscarPorTema`). El cronograma se mantiene como `/cronograma` (excepción documentada arriba).

**Panel del curso.** `PanelCursoController` (clase base con helpers) pasó a composición: el bean `PanelCursoComponent` (en `controller/component`) se inyecta en cada
controlador (`panel.contextoDelPanel(...)`, `panel.cargarContexto(...)`) y cada uno inyecta los servicios que usa.

### 2.5 Repositorios

- Consultas **derivadas** de Spring Data: prefijo del framework (`findBy…`, `existsBy…`, `countBy…`).
- Consultas con `@Query`: `buscarPor<Criterio>`, `contarPor<Criterio>`.
- Cada criterio es un campo o una relación del modelo.

### 2.6 Utilidades

- Estáticas y **sin dependencias** (si necesita inyección, es un servicio, no una utilidad).
- Una clase por tema: `DuracionUtil`, `FiltroUtil`, `PaginacionUtil`, `TextoUtil`, `MensajeErrorUtil`, `RespuestaFormularioUtil`,
  `ManejadorArchivosUtil`.
- Todos los métodos empiezan con un verbo: `formatear`, `convertirAMinutos`, `calcularUnidadEnDias`, `filtrar`, `paginar`.
  Si dependen de un criterio, `filtrarPorPrograma`.
- `RepartoUtilidad` no se usa en el código: se elimina (y su test).
- Los helpers que hoy se repiten (por ejemplo `dia(...)`, copiado en 6 controladores) viven en la utilidad correspondiente.
- Las columnas de filtro de cada listado salen del controlador a una clase de filtros por entidad en `util/filtro` (`CursoFiltros`, `PagoFiltros`, …).

---

## 3. Frontend

### 3.1 Estructura de plantillas

```
templates/
  index.html
  fragments/
    layout/        head, navbar, navbarCurso, navbarIntento, sidebar, userMenu, footer, panel, scripts, breadcrumb
    components/    avatar, empty, modalBaja, iconRecurso, cardCurso, inputs/duracion, inputs/tiempo
  pages/
    <entidad>/     páginas de la entidad y sus fragments (formulario, modificar, baja, …)
```

Solo existen `layout` y `components` en `fragments/`. Todo lo que pertenece a una entidad vive en `pages/<entidad>/`.
Cada plantilla se nombra como la **acción que realiza**, igual que la ruta.

### 3.2 Mapeo de fragments actuales

| Hoy | Pasa a |
|---|---|
| `cajonCronograma` | `pages/cronograma/drawer` |
| `cronogramaTablero` | `pages/cronograma/board` |
| `campoDuracion`, `campoTiempo` | `components/inputs/duracion`, `components/inputs/tiempo` (verificar si se pueden unificar) |
| `formAutoevaluacion` | `pages/autoevaluacion/formulario` |
| `formCurso` | `pages/curso/formulario` |
| `formDatosCurso` | `pages/curso/modificarPorDocente` |
| `formPool` | `pages/pool/formulario` |
| `iconoMaterial` | `components/iconRecurso` |
| `intentoCabecera` | se fusiona en `layout/navbarIntento` |
| `menus` + `sidebar` | `layout/sidebar` (un solo archivo) |
| `menuUsuario` | `layout/userMenu` |
| `pestaniasCurso` | `layout/navbarCurso` |
| `modalAsignarPrograma` | `pages/programa/asignar` |
| `modalBajaInscripcion` | `pages/inscripcion/baja` |
| `modalCertificado` | `pages/certificado/corregir` |
| `modalEditar<Entidad>`, `modalModificarPago` | `pages/<entidad>/modificar` (autoevaluacion, categoria, cohorte, curso, descuento, inscripcion, **recurso**, pool, **respuesta**, **termino**, pago) |
| `selectorRecurso` | `pages/recurso/selector` |
| `tarjetaCurso` | `components/cardCurso` |
| `vacio` | `components/empty` |
| `ficha` (página) | `pages/curso/ficha` |

Carpetas de páginas: `material → recurso`, `foro → consulta` y `respuesta`, `glosario → termino`, `calificacion → intento`.

### 3.2.1 Estructura de static

```
static/
  css/   base/ layout/ components/ pages/<entidad>/      (el orden de carga está en fragments/layout/styles.html)
  js/    core/ layout/ components/ pages/<entidad>/      (cada plantilla declara los módulos que usa)
  img/   logos/ cursos/
```

Los nombres siguen a las plantillas (`components/cardCurso`, `layout/navbarCurso`, `pages/cronograma/drawer`). Los CSS se cortaron
en bloques contiguos: **el orden de `styles.html` es el de la cascada original**; reordenarlos puede cambiar estilos.
`panel.js` se divide en `core/panel.js` (arranque), `core/tabla.js` (listados) y `core/validacion.js` (formularios); `validacion`
se carga antes que `tabla` (los filtros usan `window.iniciarMultiselect`).

### 3.3 Jerga de interfaz (glosario)

| Español | Inglés (único válido) |
|---|---|
| tarjeta | card |
| pestañas | navbar (`navbarCurso`) |
| cajón | drawer |
| tablero | board |
| vacío | empty |
| ícono | icon |
| menú de usuario | userMenu |
| selector | selector |

Alcance de cada renombre: archivos de plantilla y JS, clases CSS (`cc-carta → cc-card`), atributos `data-*`
(`data-tarjeta(s) → data-card(s)`), variables y funciones de JS, comentarios.

---

## 4. Limpieza

Código a eliminar o revisar (lista inicial, verificada con el código actual):

- `RepartoUtilidad` (sin uso).
- Filtros de texto y estado de los `buscarConFiltros` de los servicios, que quedaron sin uso desde que los listados filtran
  en el servidor con `FiltroUtil` (los controladores pasan `null`). Se conserva el alcance por curso y docente.
- Inyecciones de `…ServicioImpl` en controladores (hoy ~20): pasar a la interfaz.
- Helper `dia(...)` repetido.
- `InteresAlumno`: está en el diagrama y en el código, pero ninguna clase la usa todavía. Es una función diseñada y no
  implementada: **se conserva** (decisión del equipo; se trabajará más adelante). La limpieza no la borra, ni a su
  repositorio.
- El resto (métodos, plantillas, JS y CSS sin referencias) se detecta con una pasada de búsqueda de referencias antes de
  borrar, y siempre con una pasada de compilación después.

Modelo: el diagrama tiene 60 entidades y el código 60 `@Entity`, con los mismos nombres. Los 61 archivos de `model/` son esas
60 más el enum `TipoRecurso`, que no es una tabla.

---

## 5. Plan por fases

Cada fase es un commit propio. Verificación mínima: `mvn -o clean compile` (el `compile` normal puede dar éxito con clases
viejas del IDE). Al cerrar cada fase se recomienda correr la suite completa; es la forma de detectar vistas y rutas rotas por un
renombre.

0. **Capas.** Paquetes, sufijos, campos, parámetros de framework y nombres de beans. Mecánico y global: va primero y solo.
1. **Limpieza y estructura.** Código muerto y carpetas de `fragments/`, `components/` y `pages/<entidad>/`.
2. **Interfaz.** Glosario de jerga (card, drawer, board, empty, icon), fusión de fragments y plantillas por rol.
3. **Servicios.** Tipos auxiliares anidados, `Curso` absorbe el diseño, `Programa` absorbe el mínimo de contenido,
   inyección por interfaz.
4. **Controladores y rutas.** Mapeo de la sección 2.4. Es la fase más riesgosa: seguridad por ruta, pruebas, `fetch` del
   JS y enlaces de plantillas.
5. **Repositorios y utilidades.** `findBy…` y `buscarPor…`, verbos en utilidades y clases de filtros por entidad.

Fuera del refactor, sin urgencia: partir `panel.js` (1.117 líneas) y el CSS (10.178 líneas en 8 archivos) en módulos.

---

## 6. Decisiones tomadas (registro)

- Variante del administrador en curso: `buscar?vista=tabla|cards`.
- `modificarPorDocente` (y en general `verboPorRol`).
- `SecurityConfig`; `UsuarioDetallesService` se mantiene.
- Paquetes en minúscula.
- Parámetros de framework en inglés.
- Fragments solo en `layout` y `components`; lo demás va con su entidad en `pages`.
- Rutas, plantillas y controladores siguen los nombres del modelo.
- Jerga de interfaz y de capas en inglés; dominio en español.
- `InteresAlumno` se conserva aunque hoy no se use.
- Foros y glosarios por entidad: `/consulta`, `/respuesta`, `/termino`.
