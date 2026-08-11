--
-- ER/Studio Data Architect SQL Code Generation
-- Company :      UNAM
-- Project :      ModeloConceptual.DM1
-- Author :       Joaquín
--
-- Date Created : Tuesday, August 11, 2026 14:44:56
-- Target DBMS : PostgreSQL 9.x
--

-- 
-- TABLE: "Administrador" 
--

CREATE TABLE "Administrador"(
    id    int4    NOT NULL,
    CONSTRAINT "PK54" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Alumno" 
--

CREATE TABLE "Alumno"(
    id    int4    NOT NULL,
    CONSTRAINT "PK53" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Auditoria" 
--

CREATE TABLE "Auditoria"(
    id                  int4           NOT NULL,
    entidad_afectada    varchar(50)    NOT NULL,
    id_afectado         int4           NOT NULL,
    valor_anterior      text,
    valor_nuevo         text,
    ip_usuario          varchar(45)    NOT NULL,
    fecha_hora          timestamp      NOT NULL,
    CONSTRAINT "PK46" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Autoevaluacion" 
--

CREATE TABLE "Autoevaluacion"(
    id                     int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    tiempo_limite          int4           NOT NULL,
    intentos_permitidos    int4,
    fecha_apertura         timestamp      NOT NULL,
    fecha_cierre           timestamp,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL,
    CONSTRAINT "PK33" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Categoria" 
--

CREATE TABLE "Categoria"(
    id                     int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    CONSTRAINT "PK5" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "ClaseClonIA" 
--

CREATE TABLE "ClaseClonIA"(
    id                  int4           NOT NULL,
    titulo              varchar(50)    NOT NULL,
    guion               text           NOT NULL,
    fecha_generacion    timestamp      NOT NULL,
    baja                boolean        NOT NULL,
    CONSTRAINT "PK20" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "ClaseEnVivo" 
--

CREATE TABLE "ClaseEnVivo"(
    id              int4            NOT NULL,
    titulo          varchar(50)     NOT NULL,
    fecha_hora      timestamp       NOT NULL,
    url_rtmp        varchar(255)    NOT NULL,
    clave_stream    varchar(100)    NOT NULL,
    baja            boolean         NOT NULL,
    CONSTRAINT "PK18" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Configuracion" 
--

CREATE TABLE "Configuracion"(
    id       int4            NOT NULL,
    clave    varchar(100)    NOT NULL,
    valor    text            NOT NULL,
    CONSTRAINT "PK49" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "ConsultaForo" 
--

CREATE TABLE "ConsultaForo"(
    id       int4            NOT NULL,
    texto    varchar(500)    NOT NULL,
    fecha    timestamp       NOT NULL,
    baja     boolean         NOT NULL,
    CONSTRAINT "PK21" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Curso" 
--

CREATE TABLE "Curso"(
    id                     int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    precio                 float4          NOT NULL,
    imagen                 varchar(150),
    publicado              boolean         NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    CONSTRAINT "PK4" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Descuento" 
--

CREATE TABLE "Descuento"(
    id                     int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    cursos_requeridos      int4           NOT NULL,
    porcentaje             float4         NOT NULL,
    vigencia_desde         timestamp      NOT NULL,
    vigencia_hasta         timestamp      NOT NULL,
    cantidad_limite        int4           NOT NULL,
    cantidad_usada         int4           NOT NULL,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL,
    CONSTRAINT "PK28" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Dictado" 
--

CREATE TABLE "Dictado"(
    id                     int4         NOT NULL,
    fecha_inicio           timestamp    NOT NULL,
    fecha_fin              timestamp    NOT NULL,
    cupo_maximo            int4,
    baja                   boolean      NOT NULL,
    fecha_creacion         timestamp    NOT NULL,
    ultima_modificacion    timestamp,
    CONSTRAINT "PK76" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Dictado Docente" 
--

CREATE TABLE "Dictado Docente"(
    id    int4    NOT NULL,
    CONSTRAINT "PK77" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Docente" 
--

CREATE TABLE "Docente"(
    id                   int4           NOT NULL,
    anios_experiencia    int4           NOT NULL,
    matricula_cnv        varchar(50),
    biografia            text,
    habilitado           boolean        NOT NULL,
    CONSTRAINT "PK42" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "EstadoClaseClonIA" 
--

CREATE TABLE "EstadoClaseClonIA"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK19" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "EstadoClaseEnVIvo" 
--

CREATE TABLE "EstadoClaseEnVIvo"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK17" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "EstadoPago" 
--

CREATE TABLE "EstadoPago"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK24" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Inscripcion" 
--

CREATE TABLE "Inscripcion"(
    id                           int4            NOT NULL,
    fecha                        timestamp       NOT NULL,
    fecha_vencimiento_acceso     timestamp       NOT NULL,
    observaciones                varchar(500),
    numero_certificado           varchar(100),
    fecha_emision_certificado    timestamp,
    certificado_enviado          boolean         NOT NULL,
    baja                         boolean         NOT NULL,
    CONSTRAINT "PK23" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "IntentoAutoevaluacion" 
--

CREATE TABLE "IntentoAutoevaluacion"(
    id       int4         NOT NULL,
    fecha    timestamp    NOT NULL,
    nota     float4       NOT NULL,
    CONSTRAINT "PK34" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Material" 
--

CREATE TABLE "Material"(
    id                     int4            NOT NULL,
    titulo                 varchar(50)     NOT NULL,
    ruta_archivo           varchar(150),
    contenido              varchar(500),
    duracion               int4,
    autor                  varchar(50),
    generado_por_ia        boolean         NOT NULL,
    fecha_carga            timestamp       NOT NULL,
    publicado              boolean         NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    CONSTRAINT "PK10" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "MetodoPago" 
--

CREATE TABLE "MetodoPago"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK26" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Modalidad" 
--

CREATE TABLE "Modalidad"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK6" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Modalidad Curso" 
--

CREATE TABLE "Modalidad Curso"(
    id    int4    NOT NULL,
    CONSTRAINT "PK59" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "OpcionRespuesta" 
--

CREATE TABLE "OpcionRespuesta"(
    id             int4            NOT NULL,
    texto          varchar(150)    NOT NULL,
    es_correcta    boolean         NOT NULL,
    baja           boolean         NOT NULL,
    CONSTRAINT "PK32" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Pago" 
--

CREATE TABLE "Pago"(
    id                           int4            NOT NULL,
    monto                        float4          NOT NULL,
    fecha                        timestamp       NOT NULL,
    payment_request_id           varchar(50),
    external_intention_id        varchar(50)     NOT NULL,
    reference_code               varchar(20),
    tipo_pago                    varchar(20),
    ultimos_digitos_tarjeta      varchar(4),
    detalle_estado               varchar(100),
    fecha_aprobacion             timestamp,
    nombre_pagador               varchar(50),
    numero_comprobante           varchar(100),
    fecha_emision_comprobante    timestamp,
    comprobante_enviado          boolean         NOT NULL,
    CONSTRAINT "PK25" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Pool" 
--

CREATE TABLE "Pool"(
    id                     int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL,
    CONSTRAINT "PK30" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Pool Autoevaluacion" 
--

CREATE TABLE "Pool Autoevaluacion"(
    id    int4    NOT NULL,
    CONSTRAINT "PK58" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Pregunta" 
--

CREATE TABLE "Pregunta"(
    id                    int4            NOT NULL,
    texto                 varchar(150)    NOT NULL,
    es_opcion_multiple    boolean         NOT NULL,
    baja                  boolean         NOT NULL,
    CONSTRAINT "PK31" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Programa" 
--

CREATE TABLE "Programa"(
    id                     int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    meses_acceso           int4            NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    CONSTRAINT "PK74" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Progreso" 
--

CREATE TABLE "Progreso"(
    id                  int4         NOT NULL,
    completada          boolean      NOT NULL,
    fecha_completada    timestamp,
    CONSTRAINT "PK70" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Reporte" 
--

CREATE TABLE "Reporte"(
    id                  int4         NOT NULL,
    fecha_generacion    timestamp    NOT NULL,
    CONSTRAINT "PK48" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "RespuestaForo" 
--

CREATE TABLE "RespuestaForo"(
    id       int4            NOT NULL,
    texto    varchar(500)    NOT NULL,
    fecha    timestamp       NOT NULL,
    baja     boolean         NOT NULL,
    CONSTRAINT "PK22" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "RespuestaIntento" 
--

CREATE TABLE "RespuestaIntento"(
    id    int4    NOT NULL,
    CONSTRAINT "PK37" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Rol" 
--

CREATE TABLE "Rol"(
    id        int4           NOT NULL,
    nombre    varchar(50),
    CONSTRAINT "PK40" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Sesion" 
--

CREATE TABLE "Sesion"(
    id              int4            NOT NULL,
    token           varchar(255)    NOT NULL,
    fecha_inicio    timestamp       NOT NULL,
    fecha_fin       timestamp       NOT NULL,
    ip              varchar(45)     NOT NULL,
    dispositivo     varchar(255)    NOT NULL,
    CONSTRAINT "PK44" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "TerminoGlosario" 
--

CREATE TABLE "TerminoGlosario"(
    id            int4            NOT NULL,
    termino       varchar(50)     NOT NULL,
    definicion    varchar(150)    NOT NULL,
    baja          boolean         NOT NULL,
    CONSTRAINT "PK16" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "TipoAccionAuditoria" 
--

CREATE TABLE "TipoAccionAuditoria"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK45" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "TipoMaterial" 
--

CREATE TABLE "TipoMaterial"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK9" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "TipoReporte" 
--

CREATE TABLE "TipoReporte"(
    id        int4           NOT NULL,
    nombre    varchar(50)    NOT NULL,
    CONSTRAINT "PK47" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "TituloDocente" 
--

CREATE TABLE "TituloDocente"(
    id                   int4            NOT NULL,
    titulo               varchar(100)    NOT NULL,
    matricula_colegio    varchar(50),
    CONSTRAINT "PK66" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Unidad" 
--

CREATE TABLE "Unidad"(
    id                     int4            NOT NULL,
    titulo                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    numero_orden           int4            NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    CONSTRAINT "PK8" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Usuario" 
--

CREATE TABLE "Usuario"(
    id                    int4            NOT NULL,
    nombre                varchar(50)     NOT NULL,
    apellido              varchar(50)     NOT NULL,
    dni                   varchar(8)      NOT NULL,
    email                 varchar(150)    NOT NULL,
    contrasena            varchar(255),
    imagen                varchar(150),
    telefono              varchar(20),
    token_recuperacion    varchar(255),
    expiracion_token      timestamp,
    google_id             varchar(255),
    email_validado        boolean         NOT NULL,
    fecha_registro        timestamp       NOT NULL,
    baja                  boolean         NOT NULL,
    CONSTRAINT "PK36" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Usuario Rol" 
--

CREATE TABLE "Usuario Rol"(
    id    int4    NOT NULL,
    CONSTRAINT "PK57" PRIMARY KEY (id)
)
;



-- 
-- TABLE: "Administrador" 
--

ALTER TABLE "Administrador" ADD CONSTRAINT "RefUsuario361" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;


-- 
-- TABLE: "Alumno" 
--

ALTER TABLE "Alumno" ADD CONSTRAINT "RefUsuario331" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;


-- 
-- TABLE: "Auditoria" 
--

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefUsuario371" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefTipoAccionAuditoria381" 
    FOREIGN KEY (id)
    REFERENCES "TipoAccionAuditoria"(id)
;


-- 
-- TABLE: "Autoevaluacion" 
--

ALTER TABLE "Autoevaluacion" ADD CONSTRAINT "RefUnidad311" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;


-- 
-- TABLE: "ClaseClonIA" 
--

ALTER TABLE "ClaseClonIA" ADD CONSTRAINT "RefEstadoClaseClonIA431" 
    FOREIGN KEY (id)
    REFERENCES "EstadoClaseClonIA"(id)
;

ALTER TABLE "ClaseClonIA" ADD CONSTRAINT "RefMaterial461" 
    FOREIGN KEY (id)
    REFERENCES "Material"(id)
;

ALTER TABLE "ClaseClonIA" ADD CONSTRAINT "RefUnidad471" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;

ALTER TABLE "ClaseClonIA" ADD CONSTRAINT "RefDocente561" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "ClaseEnVivo" 
--

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefEstadoClaseEnVIvo421" 
    FOREIGN KEY (id)
    REFERENCES "EstadoClaseEnVIvo"(id)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefMaterial451" 
    FOREIGN KEY (id)
    REFERENCES "Material"(id)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefUnidad481" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefDocente551" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "Configuracion" 
--

ALTER TABLE "Configuracion" ADD CONSTRAINT "RefAdministrador401" 
    FOREIGN KEY (id)
    REFERENCES "Administrador"(id)
;


-- 
-- TABLE: "ConsultaForo" 
--

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefUnidad51" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefAlumno531" 
    FOREIGN KEY (id)
    REFERENCES "Alumno"(id)
;


-- 
-- TABLE: "Curso" 
--

ALTER TABLE "Curso" ADD CONSTRAINT "RefCategoria21" 
    FOREIGN KEY (id)
    REFERENCES "Categoria"(id)
;


-- 
-- TABLE: "Dictado" 
--

ALTER TABLE "Dictado" ADD CONSTRAINT "RefPrograma1001" 
    FOREIGN KEY (id)
    REFERENCES "Programa"(id)
;


-- 
-- TABLE: "Dictado Docente" 
--

ALTER TABLE "Dictado Docente" ADD CONSTRAINT "RefDictado971" 
    FOREIGN KEY (id)
    REFERENCES "Dictado"(id)
;

ALTER TABLE "Dictado Docente" ADD CONSTRAINT "RefDocente981" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "Docente" 
--

ALTER TABLE "Docente" ADD CONSTRAINT "RefUsuario101" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;


-- 
-- TABLE: "Inscripcion" 
--

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefDescuento171" 
    FOREIGN KEY (id)
    REFERENCES "Descuento"(id)
;

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefAlumno351" 
    FOREIGN KEY (id)
    REFERENCES "Alumno"(id)
;

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefDictado991" 
    FOREIGN KEY (id)
    REFERENCES "Dictado"(id)
;


-- 
-- TABLE: "IntentoAutoevaluacion" 
--

ALTER TABLE "IntentoAutoevaluacion" ADD CONSTRAINT "RefAutoevaluacion271" 
    FOREIGN KEY (id)
    REFERENCES "Autoevaluacion"(id)
;


-- 
-- TABLE: "Material" 
--

ALTER TABLE "Material" ADD CONSTRAINT "RefUnidad81" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;

ALTER TABLE "Material" ADD CONSTRAINT "RefTipoMaterial91" 
    FOREIGN KEY (id)
    REFERENCES "TipoMaterial"(id)
;

ALTER TABLE "Material" ADD CONSTRAINT "RefDocente541" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "Modalidad Curso" 
--

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefModalidad671" 
    FOREIGN KEY (id)
    REFERENCES "Modalidad"(id)
;

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefCurso681" 
    FOREIGN KEY (id)
    REFERENCES "Curso"(id)
;


-- 
-- TABLE: "OpcionRespuesta" 
--

ALTER TABLE "OpcionRespuesta" ADD CONSTRAINT "RefPregunta231" 
    FOREIGN KEY (id)
    REFERENCES "Pregunta"(id)
;


-- 
-- TABLE: "Pago" 
--

ALTER TABLE "Pago" ADD CONSTRAINT "RefInscripcion181" 
    FOREIGN KEY (id)
    REFERENCES "Inscripcion"(id)
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefEstadoPago191" 
    FOREIGN KEY (id)
    REFERENCES "EstadoPago"(id)
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefMetodoPago201" 
    FOREIGN KEY (id)
    REFERENCES "MetodoPago"(id)
;


-- 
-- TABLE: "Pool" 
--

ALTER TABLE "Pool" ADD CONSTRAINT "RefUnidad301" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;


-- 
-- TABLE: "Pool Autoevaluacion" 
--

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefPool641" 
    FOREIGN KEY (id)
    REFERENCES "Pool"(id)
;

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefAutoevaluacion651" 
    FOREIGN KEY (id)
    REFERENCES "Autoevaluacion"(id)
;


-- 
-- TABLE: "Pregunta" 
--

ALTER TABLE "Pregunta" ADD CONSTRAINT "RefPool221" 
    FOREIGN KEY (id)
    REFERENCES "Pool"(id)
;


-- 
-- TABLE: "Programa" 
--

ALTER TABLE "Programa" ADD CONSTRAINT "RefCurso941" 
    FOREIGN KEY (id)
    REFERENCES "Curso"(id)
;


-- 
-- TABLE: "Progreso" 
--

ALTER TABLE "Progreso" ADD CONSTRAINT "RefUnidad871" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;

ALTER TABLE "Progreso" ADD CONSTRAINT "RefInscripcion901" 
    FOREIGN KEY (id)
    REFERENCES "Inscripcion"(id)
;


-- 
-- TABLE: "Reporte" 
--

ALTER TABLE "Reporte" ADD CONSTRAINT "RefTipoReporte391" 
    FOREIGN KEY (id)
    REFERENCES "TipoReporte"(id)
;

ALTER TABLE "Reporte" ADD CONSTRAINT "RefAdministrador411" 
    FOREIGN KEY (id)
    REFERENCES "Administrador"(id)
;


-- 
-- TABLE: "RespuestaForo" 
--

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefConsultaForo61" 
    FOREIGN KEY (id)
    REFERENCES "ConsultaForo"(id)
;

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefDocente521" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "RespuestaIntento" 
--

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefIntentoAutoevaluacion281" 
    FOREIGN KEY (id)
    REFERENCES "IntentoAutoevaluacion"(id)
;

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefOpcionRespuesta851" 
    FOREIGN KEY (id)
    REFERENCES "OpcionRespuesta"(id)
;


-- 
-- TABLE: "Sesion" 
--

ALTER TABLE "Sesion" ADD CONSTRAINT "RefUsuario111" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;


-- 
-- TABLE: "TerminoGlosario" 
--

ALTER TABLE "TerminoGlosario" ADD CONSTRAINT "RefUnidad71" 
    FOREIGN KEY (id)
    REFERENCES "Unidad"(id)
;


-- 
-- TABLE: "TituloDocente" 
--

ALTER TABLE "TituloDocente" ADD CONSTRAINT "RefDocente781" 
    FOREIGN KEY (id)
    REFERENCES "Docente"(id)
;


-- 
-- TABLE: "Unidad" 
--

ALTER TABLE "Unidad" ADD CONSTRAINT "RefPrograma1011" 
    FOREIGN KEY (id)
    REFERENCES "Programa"(id)
;


-- 
-- TABLE: "Usuario Rol" 
--

ALTER TABLE "Usuario Rol" ADD CONSTRAINT "RefUsuario611" 
    FOREIGN KEY (id)
    REFERENCES "Usuario"(id)
;

ALTER TABLE "Usuario Rol" ADD CONSTRAINT "RefRol621" 
    FOREIGN KEY (id)
    REFERENCES "Rol"(id)
;
