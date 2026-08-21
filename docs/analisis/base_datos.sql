--
-- ER/Studio Data Architect SQL Code Generation
-- Company :      UNAM
-- Project :      ModeloConceptual.DM1
-- Author :       Joaquín
--
-- Date Created : Friday, August 21, 2026 00:03:00
-- Target DBMS : PostgreSQL 9.x
--

ALTER TABLE "Administrador"
DROP CONSTRAINT RefUsuario139
;

ALTER TABLE "Alumno"
DROP CONSTRAINT RefUsuario140
;

ALTER TABLE "Auditoria"
DROP CONSTRAINT RefTipoAccionAuditoria136
;

ALTER TABLE "Auditoria"
DROP CONSTRAINT RefUsuario137
;

ALTER TABLE "Autoevaluacion"
DROP CONSTRAINT RefUnidad150
;

ALTER TABLE "ClaseClon"
DROP CONSTRAINT RefMaterial175
;

ALTER TABLE "ClaseClon"
DROP CONSTRAINT RefDocente129
;

ALTER TABLE "ClaseClon"
DROP CONSTRAINT RefEstadoClaseClon133
;

ALTER TABLE "ClaseEnVivo"
DROP CONSTRAINT RefMaterial176
;

ALTER TABLE "ClaseEnVivo"
DROP CONSTRAINT RefCohorte186
;

ALTER TABLE "ClaseEnVivo"
DROP CONSTRAINT RefDocente128
;

ALTER TABLE "ClaseEnVivo"
DROP CONSTRAINT RefEstadoClaseEnVIvo132
;

ALTER TABLE "Cohorte"
DROP CONSTRAINT RefPrograma122
;

ALTER TABLE "Configuracion"
DROP CONSTRAINT RefAdministrador138
;

ALTER TABLE "ConsultaForo"
DROP CONSTRAINT RefAlumno162
;

ALTER TABLE "ConsultaForo"
DROP CONSTRAINT RefUnidad163
;

ALTER TABLE "Cronograma"
DROP CONSTRAINT RefPrograma182
;

ALTER TABLE "Cronograma"
DROP CONSTRAINT RefUnidad183
;

ALTER TABLE "Curso"
DROP CONSTRAINT RefDocente187
;

ALTER TABLE "Curso"
DROP CONSTRAINT RefCategoria119
;

ALTER TABLE "Curso"
DROP CONSTRAINT RefNivel120
;

ALTER TABLE "Docente"
DROP CONSTRAINT RefUsuario125
;

ALTER TABLE "Inscripcion"
DROP CONSTRAINT RefAlumno177
;

ALTER TABLE "Inscripcion"
DROP CONSTRAINT RefCohorte142
;

ALTER TABLE "IntentoAutoevaluacion"
DROP CONSTRAINT RefInscripcion149
;

ALTER TABLE "IntentoAutoevaluacion"
DROP CONSTRAINT RefAutoevaluacion151
;

ALTER TABLE "Material"
DROP CONSTRAINT RefTipoMaterial171
;

ALTER TABLE "Material"
DROP CONSTRAINT RefUnidad172
;

ALTER TABLE "Material"
DROP CONSTRAINT RefDocente127
;

ALTER TABLE "Modalidad Curso"
DROP CONSTRAINT RefModalidad67
;

ALTER TABLE "Modalidad Curso"
DROP CONSTRAINT RefCurso68
;

ALTER TABLE "OpcionRespuesta"
DROP CONSTRAINT RefPregunta155
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefDescuento188
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefEstadoPago143
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefMetodoPago144
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefInscripcion145
;

ALTER TABLE "Pool"
DROP CONSTRAINT RefUnidad158
;

ALTER TABLE "Pool Autoevaluacion"
DROP CONSTRAINT RefPool64
;

ALTER TABLE "Pool Autoevaluacion"
DROP CONSTRAINT RefAutoevaluacion65
;

ALTER TABLE "Pregunta"
DROP CONSTRAINT RefPool154
;

ALTER TABLE "Programa"
DROP CONSTRAINT RefCurso121
;

ALTER TABLE "Progreso"
DROP CONSTRAINT RefUnidad159
;

ALTER TABLE "Progreso"
DROP CONSTRAINT RefInscripcion160
;

ALTER TABLE "Reporte"
DROP CONSTRAINT RefCurso191
;

ALTER TABLE "Reporte"
DROP CONSTRAINT RefAdministrador146
;

ALTER TABLE "Reporte"
DROP CONSTRAINT RefTipoReporte147
;

ALTER TABLE "RespuestaForo"
DROP CONSTRAINT RefDocente126
;

ALTER TABLE "RespuestaForo"
DROP CONSTRAINT RefConsultaForo161
;

ALTER TABLE "RespuestaIntento"
DROP CONSTRAINT RefIntentoAutoevaluacion153
;

ALTER TABLE "RespuestaIntento"
DROP CONSTRAINT RefOpcionRespuesta157
;

ALTER TABLE "Sesion"
DROP CONSTRAINT RefUsuario135
;

ALTER TABLE "Supervisor"
DROP CONSTRAINT RefCurso184
;

ALTER TABLE "Supervisor"
DROP CONSTRAINT RefDocente185
;

ALTER TABLE "TerminoGlosario"
DROP CONSTRAINT RefUnidad134
;

ALTER TABLE "TituloDocente"
DROP CONSTRAINT RefDocente124
;

ALTER TABLE "Usuario"
DROP CONSTRAINT RefRol189
;

-- 
-- TABLE: "Administrador" 
--

CREATE TABLE "Administrador"(
    id_administrador    int4    NOT NULL,
    id_usuario          int4    NOT NULL
)
;



-- 
-- TABLE: "Alumno" 
--

CREATE TABLE "Alumno"(
    id_alumno     int4    NOT NULL,
    id_usuario    int4    NOT NULL
)
;



-- 
-- TABLE: "Auditoria" 
--

CREATE TABLE "Auditoria"(
    id_auditoria                int4           NOT NULL,
    entidad_afectada            varchar(50)    NOT NULL,
    id_afectado                 int4           NOT NULL,
    valor_anterior              text,
    valor_nuevo                 text,
    ip_usuario                  varchar(45)    NOT NULL,
    fecha_hora                  timestamp      NOT NULL,
    id_tipo_accion_auditoria    int4           NOT NULL,
    id_usuario                  int4           NOT NULL
)
;



-- 
-- TABLE: "Autoevaluacion" 
--

CREATE TABLE "Autoevaluacion"(
    id_autoevaluacion      int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    tiempo_limite          int4           NOT NULL,
    intentos_permitidos    int4,
    fecha_apertura         timestamp      NOT NULL,
    fecha_cierre           timestamp,
    oculto                 boolean        NOT NULL,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL,
    id_unidad              int4           NOT NULL
)
;



-- 
-- TABLE: "Categoria" 
--

CREATE TABLE "Categoria"(
    id_categoria           int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL
)
;



-- 
-- TABLE: "ClaseClon" 
--

CREATE TABLE "ClaseClon"(
    id_clase_clon           int4           NOT NULL,
    titulo                  varchar(50)    NOT NULL,
    guion                   text           NOT NULL,
    fecha_generacion        timestamp      NOT NULL,
    oculto                  boolean        NOT NULL,
    baja                    boolean        NOT NULL,
    id_docente              int4           NOT NULL,
    id_estado_clase_clon    int4           NOT NULL,
    id_material             int4
)
;



-- 
-- TABLE: "ClaseEnVivo" 
--

CREATE TABLE "ClaseEnVivo"(
    id_clase_en_vivo           int4            NOT NULL,
    titulo                     varchar(50)     NOT NULL,
    fecha_hora                 timestamp       NOT NULL,
    duracion_estimada          int4            NOT NULL,
    url_rtmp                   varchar(255)    NOT NULL,
    clave_stream               varchar(100)    NOT NULL,
    oculto                     boolean         NOT NULL,
    baja                       boolean         NOT NULL,
    id_docente                 int4            NOT NULL,
    id_estado_clase_en_vivo    int4            NOT NULL,
    id_material                int4,
    id_cohorte                 int4            NOT NULL
)
;



-- 
-- TABLE: "Cohorte" 
--

CREATE TABLE "Cohorte"(
    id_cohorte                  int4         NOT NULL,
    fecha_inicio_inscripcion    timestamp    NOT NULL,
    fecha_fin_inscripcion       timestamp    NOT NULL,
    fecha_inicio_dictado        timestamp,
    fecha_fin_dictado           timestamp,
    cupo_maximo                 int4,
    semanas_acceso              int4         NOT NULL,
    baja                        boolean      NOT NULL,
    fecha_creacion              timestamp    NOT NULL,
    ultima_modificacion         timestamp,
    id_programa                 int4         NOT NULL
)
;



-- 
-- TABLE: "Configuracion" 
--

CREATE TABLE "Configuracion"(
    id_configuracion    int4            NOT NULL,
    clave               varchar(100)    NOT NULL,
    valor               text            NOT NULL,
    id_administrador    int4            NOT NULL
)
;



-- 
-- TABLE: "ConsultaForo" 
--

CREATE TABLE "ConsultaForo"(
    id_consulta_foro    int4            NOT NULL,
    texto               varchar(500)    NOT NULL,
    fecha               timestamp       NOT NULL,
    baja                boolean         NOT NULL,
    id_alumno           int4            NOT NULL,
    id_unidad           int4            NOT NULL
)
;



-- 
-- TABLE: "Cronograma" 
--

CREATE TABLE "Cronograma"(
    id                  int4    NOT NULL,
    numero_orden        int4    NOT NULL,
    semanas_duracion    int4    NOT NULL,
    id_programa         int4    NOT NULL,
    id_unidad           int4    NOT NULL
)
;



-- 
-- TABLE: "Curso" 
--

CREATE TABLE "Curso"(
    id_curso               int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    precio                 float4          NOT NULL,
    imagen                 varchar(150),
    emite_certificado      boolean         NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    id_categoria           int4            NOT NULL,
    id_nivel               int4            NOT NULL,
    id_docente             int4            NOT NULL
)
;



-- 
-- TABLE: "Descuento" 
--

CREATE TABLE "Descuento"(
    id_descuento           int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    cursos_requeridos      int4           NOT NULL,
    porcentaje             float4         NOT NULL,
    vigencia_desde         timestamp      NOT NULL,
    vigencia_hasta         timestamp      NOT NULL,
    cantidad_limite        int4           NOT NULL,
    cantidad_usada         int4           NOT NULL,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL
)
;



-- 
-- TABLE: "Docente" 
--

CREATE TABLE "Docente"(
    id_docente                   int4            NOT NULL,
    anios_experiencia            int4            NOT NULL,
    matricula_cnv                varchar(50),
    biografia                    text,
    habilitado                   boolean         NOT NULL,
    fecha_aceptacion_tyc_clon    timestamp,
    avatar_id                    varchar(100),
    voice_id                     varchar(100),
    id_usuario                   int4            NOT NULL
)
;



-- 
-- TABLE: "EstadoClaseClon" 
--

CREATE TABLE "EstadoClaseClon"(
    id_estado_clase_clon    int4           NOT NULL,
    nombre                  varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "EstadoClaseEnVIvo" 
--

CREATE TABLE "EstadoClaseEnVIvo"(
    id_estado_clase_en_vivo    int4           NOT NULL,
    nombre                     varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "EstadoPago" 
--

CREATE TABLE "EstadoPago"(
    id_estado_pago    int4           NOT NULL,
    nombre            varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "Inscripcion" 
--

CREATE TABLE "Inscripcion"(
    id_inscripcion               int4            NOT NULL,
    fecha                        timestamp       NOT NULL,
    fecha_vencimiento_acceso     timestamp       NOT NULL,
    observaciones                varchar(500),
    numero_certificado           varchar(100),
    nombre_alumno                varchar(100),
    dni_alumno                   varchar(8),
    texto_certificado            text,
    fecha_emision_certificado    timestamp,
    certificado_enviado          boolean         NOT NULL,
    baja                         boolean         NOT NULL,
    id_cohorte                   int4            NOT NULL,
    id_alumno                    int4            NOT NULL
)
;



-- 
-- TABLE: "IntentoAutoevaluacion" 
--

CREATE TABLE "IntentoAutoevaluacion"(
    id_intento_autoevaluacion    int4         NOT NULL,
    fecha_entrega                timestamp,
    nota                         float4,
    baja                         boolean      NOT NULL,
    id_inscripcion               int4         NOT NULL,
    id_autoevaluacion            int4         NOT NULL
)
;



-- 
-- TABLE: "Material" 
--

CREATE TABLE "Material"(
    id_material            int4            NOT NULL,
    titulo                 varchar(50)     NOT NULL,
    ruta_archivo           varchar(150),
    contenido              varchar(500),
    duracion               int4,
    autor                  varchar(50),
    generado_por_ia        boolean         NOT NULL,
    oculto                 boolean         NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    id_docente             int4            NOT NULL,
    id_tipo_material       int4            NOT NULL,
    id_unidad              int4            NOT NULL
)
;



-- 
-- TABLE: "MetodoPago" 
--

CREATE TABLE "MetodoPago"(
    id_metodo_pago    int4           NOT NULL,
    nombre            varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "Modalidad" 
--

CREATE TABLE "Modalidad"(
    id_modalidad    int4           NOT NULL,
    nombre          varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "Modalidad Curso" 
--

CREATE TABLE "Modalidad Curso"(
    id_modalidad    int4    NOT NULL,
    id_curso        int4    NOT NULL
)
;



-- 
-- TABLE: "Nivel" 
--

CREATE TABLE "Nivel"(
    id_nivel    int4           NOT NULL,
    nombre      varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "OpcionRespuesta" 
--

CREATE TABLE "OpcionRespuesta"(
    id_opcion_respuesta    int4            NOT NULL,
    texto                  varchar(150)    NOT NULL,
    es_correcta            boolean         NOT NULL,
    baja                   boolean         NOT NULL,
    id_pregunta            int4            NOT NULL
)
;



-- 
-- TABLE: "Pago" 
--

CREATE TABLE "Pago"(
    id_pago                      int4            NOT NULL,
    monto                        float4          NOT NULL,
    fecha                        timestamp       NOT NULL,
    payment_request_id           varchar(50),
    external_intention_id        varchar(50)     NOT NULL,
    reference_code               varchar(20),
    ultimos_digitos_tarjeta      varchar(4),
    detalle_estado               varchar(100),
    fecha_aprobacion             timestamp,
    nombre_pagador               varchar(50),
    dni_pagador                  varchar(8),
    numero_comprobante           varchar(100),
    fecha_emision_comprobante    timestamp,
    comprobante_enviado          boolean         NOT NULL,
    id_estado_pago               int4            NOT NULL,
    id_metodo_pago               int4            NOT NULL,
    id_inscripcion               int4            NOT NULL,
    id_descuento                 int4
)
;



-- 
-- TABLE: "Pool" 
--

CREATE TABLE "Pool"(
    id_pool                int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    fecha_creacion         timestamp      NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean        NOT NULL,
    id_unidad              int4           NOT NULL
)
;



-- 
-- TABLE: "Pool Autoevaluacion" 
--

CREATE TABLE "Pool Autoevaluacion"(
    id_pool              int4    NOT NULL,
    id_autoevaluacion    int4    NOT NULL
)
;



-- 
-- TABLE: "Pregunta" 
--

CREATE TABLE "Pregunta"(
    id_pregunta           int4            NOT NULL,
    texto                 varchar(150)    NOT NULL,
    es_opcion_multiple    boolean         NOT NULL,
    baja                  boolean         NOT NULL,
    id_pool               int4            NOT NULL
)
;



-- 
-- TABLE: "Programa" 
--

CREATE TABLE "Programa"(
    id_programa            int4            NOT NULL,
    nombre                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    objetivos              text            NOT NULL,
    carga_horaria_total    int4,
    bibliografia           text            NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL,
    id_curso               int4            NOT NULL
)
;



-- 
-- TABLE: "Progreso" 
--

CREATE TABLE "Progreso"(
    id_progreso         int4         NOT NULL,
    completada          boolean      NOT NULL,
    fecha_completada    timestamp,
    id_unidad           int4         NOT NULL,
    id_inscripcion      int4         NOT NULL
)
;



-- 
-- TABLE: "Reporte" 
--

CREATE TABLE "Reporte"(
    id_reporte          int4         NOT NULL,
    fecha_generacion    timestamp    NOT NULL,
    id_administrador    int4         NOT NULL,
    id_tipo_reporte     int4         NOT NULL,
    id_curso            int4         NOT NULL
)
;



-- 
-- TABLE: "RespuestaForo" 
--

CREATE TABLE "RespuestaForo"(
    id_respuesta_foro    int4            NOT NULL,
    texto                varchar(500)    NOT NULL,
    fecha                timestamp       NOT NULL,
    baja                 boolean         NOT NULL,
    id_docente           int4            NOT NULL,
    id_consulta_foro     int4            NOT NULL
)
;



-- 
-- TABLE: "RespuestaIntento" 
--

CREATE TABLE "RespuestaIntento"(
    id_respuesta_intento         int4    NOT NULL,
    id_intento_autoevaluacion    int4    NOT NULL,
    id_opcion_respuesta          int4    NOT NULL
)
;



-- 
-- TABLE: "Rol" 
--

CREATE TABLE "Rol"(
    id_rol    int4           NOT NULL,
    nombre    varchar(50)
)
;



-- 
-- TABLE: "Sesion" 
--

CREATE TABLE "Sesion"(
    id_sesion       int4            NOT NULL,
    token           varchar(255)    NOT NULL,
    fecha_inicio    timestamp       NOT NULL,
    fecha_fin       timestamp       NOT NULL,
    ip              varchar(45)     NOT NULL,
    dispositivo     varchar(255)    NOT NULL,
    id_usuario      int4            NOT NULL
)
;



-- 
-- TABLE: "Supervisor" 
--

CREATE TABLE "Supervisor"(
    id            int4    NOT NULL,
    id_curso      int4    NOT NULL,
    id_docente    int4    NOT NULL
)
;



-- 
-- TABLE: "TerminoGlosario" 
--

CREATE TABLE "TerminoGlosario"(
    id_termino_glosario    int4            NOT NULL,
    termino                varchar(50)     NOT NULL,
    definicion             varchar(150)    NOT NULL,
    baja                   boolean         NOT NULL,
    id_unidad              int4            NOT NULL
)
;



-- 
-- TABLE: "TipoAccionAuditoria" 
--

CREATE TABLE "TipoAccionAuditoria"(
    id_tipo_accion_auditoria    int4           NOT NULL,
    nombre                      varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "TipoMaterial" 
--

CREATE TABLE "TipoMaterial"(
    id_tipo_material    int4           NOT NULL,
    nombre              varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "TipoReporte" 
--

CREATE TABLE "TipoReporte"(
    id_tipo_reporte    int4           NOT NULL,
    nombre             varchar(50)    NOT NULL
)
;



-- 
-- TABLE: "TituloDocente" 
--

CREATE TABLE "TituloDocente"(
    id_titulo_docente    int4            NOT NULL,
    titulo               varchar(100)    NOT NULL,
    matricula_colegio    varchar(50),
    id_docente           int4            NOT NULL
)
;



-- 
-- TABLE: "Unidad" 
--

CREATE TABLE "Unidad"(
    id_unidad              int4            NOT NULL,
    titulo                 varchar(50)     NOT NULL,
    descripcion            varchar(150),
    contenido              text            NOT NULL,
    fecha_creacion         timestamp       NOT NULL,
    ultima_modificacion    timestamp,
    baja                   boolean         NOT NULL
)
;



-- 
-- TABLE: "Usuario" 
--

CREATE TABLE "Usuario"(
    id_usuario            int4            NOT NULL,
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
    id_rol                int4            NOT NULL
)
;



-- 
-- INDEX: "Ref36139" 
--

CREATE INDEX "Ref36139" ON "Administrador"(id_usuario)
;
-- 
-- INDEX: "Ref36140" 
--

CREATE INDEX "Ref36140" ON "Alumno"(id_usuario)
;
-- 
-- INDEX: "Ref45136" 
--

CREATE INDEX "Ref45136" ON "Auditoria"(id_tipo_accion_auditoria)
;
-- 
-- INDEX: "Ref36137" 
--

CREATE INDEX "Ref36137" ON "Auditoria"(id_usuario)
;
-- 
-- INDEX: "Ref8150" 
--

CREATE INDEX "Ref8150" ON "Autoevaluacion"(id_unidad)
;
-- 
-- INDEX: "Ref10175" 
--

CREATE INDEX "Ref10175" ON "ClaseClon"(id_material)
;
-- 
-- INDEX: "Ref42129" 
--

CREATE INDEX "Ref42129" ON "ClaseClon"(id_docente)
;
-- 
-- INDEX: "Ref19133" 
--

CREATE INDEX "Ref19133" ON "ClaseClon"(id_estado_clase_clon)
;
-- 
-- INDEX: "Ref10176" 
--

CREATE INDEX "Ref10176" ON "ClaseEnVivo"(id_material)
;
-- 
-- INDEX: "Ref76186" 
--

CREATE INDEX "Ref76186" ON "ClaseEnVivo"(id_cohorte)
;
-- 
-- INDEX: "Ref42128" 
--

CREATE INDEX "Ref42128" ON "ClaseEnVivo"(id_docente)
;
-- 
-- INDEX: "Ref17132" 
--

CREATE INDEX "Ref17132" ON "ClaseEnVivo"(id_estado_clase_en_vivo)
;
-- 
-- INDEX: "Ref74122" 
--

CREATE INDEX "Ref74122" ON "Cohorte"(id_programa)
;
-- 
-- INDEX: "Ref54138" 
--

CREATE INDEX "Ref54138" ON "Configuracion"(id_administrador)
;
-- 
-- INDEX: "Ref53162" 
--

CREATE INDEX "Ref53162" ON "ConsultaForo"(id_alumno)
;
-- 
-- INDEX: "Ref8163" 
--

CREATE INDEX "Ref8163" ON "ConsultaForo"(id_unidad)
;
-- 
-- INDEX: "Ref74182" 
--

CREATE INDEX "Ref74182" ON "Cronograma"(id_programa)
;
-- 
-- INDEX: "Ref8183" 
--

CREATE INDEX "Ref8183" ON "Cronograma"(id_unidad)
;
-- 
-- INDEX: "Ref42187" 
--

CREATE INDEX "Ref42187" ON "Curso"(id_docente)
;
-- 
-- INDEX: "Ref5119" 
--

CREATE INDEX "Ref5119" ON "Curso"(id_categoria)
;
-- 
-- INDEX: "Ref81120" 
--

CREATE INDEX "Ref81120" ON "Curso"(id_nivel)
;
-- 
-- INDEX: "Ref36125" 
--

CREATE INDEX "Ref36125" ON "Docente"(id_usuario)
;
-- 
-- INDEX: "Ref53177" 
--

CREATE INDEX "Ref53177" ON "Inscripcion"(id_alumno)
;
-- 
-- INDEX: "Ref76142" 
--

CREATE INDEX "Ref76142" ON "Inscripcion"(id_cohorte)
;
-- 
-- INDEX: "Ref23149" 
--

CREATE INDEX "Ref23149" ON "IntentoAutoevaluacion"(id_inscripcion)
;
-- 
-- INDEX: "Ref33151" 
--

CREATE INDEX "Ref33151" ON "IntentoAutoevaluacion"(id_autoevaluacion)
;
-- 
-- INDEX: "Ref9171" 
--

CREATE INDEX "Ref9171" ON "Material"(id_tipo_material)
;
-- 
-- INDEX: "Ref8172" 
--

CREATE INDEX "Ref8172" ON "Material"(id_unidad)
;
-- 
-- INDEX: "Ref42127" 
--

CREATE INDEX "Ref42127" ON "Material"(id_docente)
;
-- 
-- INDEX: "Ref667" 
--

CREATE INDEX "Ref667" ON "Modalidad Curso"(id_modalidad)
;
-- 
-- INDEX: "Ref468" 
--

CREATE INDEX "Ref468" ON "Modalidad Curso"(id_curso)
;
-- 
-- INDEX: "Ref31155" 
--

CREATE INDEX "Ref31155" ON "OpcionRespuesta"(id_pregunta)
;
-- 
-- INDEX: "Ref28188" 
--

CREATE INDEX "Ref28188" ON "Pago"(id_descuento)
;
-- 
-- INDEX: "Ref24143" 
--

CREATE INDEX "Ref24143" ON "Pago"(id_estado_pago)
;
-- 
-- INDEX: "Ref26144" 
--

CREATE INDEX "Ref26144" ON "Pago"(id_metodo_pago)
;
-- 
-- INDEX: "Ref23145" 
--

CREATE INDEX "Ref23145" ON "Pago"(id_inscripcion)
;
-- 
-- INDEX: "Ref8158" 
--

CREATE INDEX "Ref8158" ON "Pool"(id_unidad)
;
-- 
-- INDEX: "Ref3064" 
--

CREATE INDEX "Ref3064" ON "Pool Autoevaluacion"(id_pool)
;
-- 
-- INDEX: "Ref3365" 
--

CREATE INDEX "Ref3365" ON "Pool Autoevaluacion"(id_autoevaluacion)
;
-- 
-- INDEX: "Ref30154" 
--

CREATE INDEX "Ref30154" ON "Pregunta"(id_pool)
;
-- 
-- INDEX: "Ref4121" 
--

CREATE INDEX "Ref4121" ON "Programa"(id_curso)
;
-- 
-- INDEX: "Ref8159" 
--

CREATE INDEX "Ref8159" ON "Progreso"(id_unidad)
;
-- 
-- INDEX: "Ref23160" 
--

CREATE INDEX "Ref23160" ON "Progreso"(id_inscripcion)
;
-- 
-- INDEX: "Ref4191" 
--

CREATE INDEX "Ref4191" ON "Reporte"(id_curso)
;
-- 
-- INDEX: "Ref54146" 
--

CREATE INDEX "Ref54146" ON "Reporte"(id_administrador)
;
-- 
-- INDEX: "Ref47147" 
--

CREATE INDEX "Ref47147" ON "Reporte"(id_tipo_reporte)
;
-- 
-- INDEX: "Ref42126" 
--

CREATE INDEX "Ref42126" ON "RespuestaForo"(id_docente)
;
-- 
-- INDEX: "Ref21161" 
--

CREATE INDEX "Ref21161" ON "RespuestaForo"(id_consulta_foro)
;
-- 
-- INDEX: "Ref34153" 
--

CREATE INDEX "Ref34153" ON "RespuestaIntento"(id_intento_autoevaluacion)
;
-- 
-- INDEX: "Ref32157" 
--

CREATE INDEX "Ref32157" ON "RespuestaIntento"(id_opcion_respuesta)
;
-- 
-- INDEX: "Ref36135" 
--

CREATE INDEX "Ref36135" ON "Sesion"(id_usuario)
;
-- 
-- INDEX: "Ref4184" 
--

CREATE INDEX "Ref4184" ON "Supervisor"(id_curso)
;
-- 
-- INDEX: "Ref42185" 
--

CREATE INDEX "Ref42185" ON "Supervisor"(id_docente)
;
-- 
-- INDEX: "Ref8134" 
--

CREATE INDEX "Ref8134" ON "TerminoGlosario"(id_unidad)
;
-- 
-- INDEX: "Ref42124" 
--

CREATE INDEX "Ref42124" ON "TituloDocente"(id_docente)
;
-- 
-- INDEX: "Ref40189" 
--

CREATE INDEX "Ref40189" ON "Usuario"(id_rol)
;
-- 
-- TABLE: "Administrador" 
--

ALTER TABLE "Administrador" ADD 
    CONSTRAINT "PK54" PRIMARY KEY (id_administrador)
;

-- 
-- TABLE: "Alumno" 
--

ALTER TABLE "Alumno" ADD 
    CONSTRAINT "PK53" PRIMARY KEY (id_alumno)
;

-- 
-- TABLE: "Auditoria" 
--

ALTER TABLE "Auditoria" ADD 
    CONSTRAINT "PK46" PRIMARY KEY (id_auditoria)
;

-- 
-- TABLE: "Autoevaluacion" 
--

ALTER TABLE "Autoevaluacion" ADD 
    CONSTRAINT "PK33" PRIMARY KEY (id_autoevaluacion)
;

-- 
-- TABLE: "Categoria" 
--

ALTER TABLE "Categoria" ADD 
    CONSTRAINT "PK5" PRIMARY KEY (id_categoria)
;

-- 
-- TABLE: "ClaseClon" 
--

ALTER TABLE "ClaseClon" ADD 
    CONSTRAINT "PK20" PRIMARY KEY (id_clase_clon)
;

-- 
-- TABLE: "ClaseEnVivo" 
--

ALTER TABLE "ClaseEnVivo" ADD 
    CONSTRAINT "PK18" PRIMARY KEY (id_clase_en_vivo)
;

-- 
-- TABLE: "Cohorte" 
--

ALTER TABLE "Cohorte" ADD 
    CONSTRAINT "PK76" PRIMARY KEY (id_cohorte)
;

-- 
-- TABLE: "Configuracion" 
--

ALTER TABLE "Configuracion" ADD 
    CONSTRAINT "PK49" PRIMARY KEY (id_configuracion)
;

-- 
-- TABLE: "ConsultaForo" 
--

ALTER TABLE "ConsultaForo" ADD 
    CONSTRAINT "PK21" PRIMARY KEY (id_consulta_foro)
;

-- 
-- TABLE: "Cronograma" 
--

ALTER TABLE "Cronograma" ADD 
    CONSTRAINT "PK86" PRIMARY KEY (id)
;

-- 
-- TABLE: "Curso" 
--

ALTER TABLE "Curso" ADD 
    CONSTRAINT "PK4" PRIMARY KEY (id_curso)
;

-- 
-- TABLE: "Descuento" 
--

ALTER TABLE "Descuento" ADD 
    CONSTRAINT "PK28" PRIMARY KEY (id_descuento)
;

-- 
-- TABLE: "Docente" 
--

ALTER TABLE "Docente" ADD 
    CONSTRAINT "PK42" PRIMARY KEY (id_docente)
;

-- 
-- TABLE: "EstadoClaseClon" 
--

ALTER TABLE "EstadoClaseClon" ADD 
    CONSTRAINT "PK19" PRIMARY KEY (id_estado_clase_clon)
;

-- 
-- TABLE: "EstadoClaseEnVIvo" 
--

ALTER TABLE "EstadoClaseEnVIvo" ADD 
    CONSTRAINT "PK17" PRIMARY KEY (id_estado_clase_en_vivo)
;

-- 
-- TABLE: "EstadoPago" 
--

ALTER TABLE "EstadoPago" ADD 
    CONSTRAINT "PK24" PRIMARY KEY (id_estado_pago)
;

-- 
-- TABLE: "Inscripcion" 
--

ALTER TABLE "Inscripcion" ADD 
    CONSTRAINT "PK23" PRIMARY KEY (id_inscripcion)
;

-- 
-- TABLE: "IntentoAutoevaluacion" 
--

ALTER TABLE "IntentoAutoevaluacion" ADD 
    CONSTRAINT "PK34" PRIMARY KEY (id_intento_autoevaluacion)
;

-- 
-- TABLE: "Material" 
--

ALTER TABLE "Material" ADD 
    CONSTRAINT "PK10" PRIMARY KEY (id_material)
;

-- 
-- TABLE: "MetodoPago" 
--

ALTER TABLE "MetodoPago" ADD 
    CONSTRAINT "PK26" PRIMARY KEY (id_metodo_pago)
;

-- 
-- TABLE: "Modalidad" 
--

ALTER TABLE "Modalidad" ADD 
    CONSTRAINT "PK6" PRIMARY KEY (id_modalidad)
;

-- 
-- TABLE: "Modalidad Curso" 
--

ALTER TABLE "Modalidad Curso" ADD 
    CONSTRAINT "PK59" PRIMARY KEY (id_modalidad, id_curso)
;

-- 
-- TABLE: "Nivel" 
--

ALTER TABLE "Nivel" ADD 
    CONSTRAINT "PK81" PRIMARY KEY (id_nivel)
;

-- 
-- TABLE: "OpcionRespuesta" 
--

ALTER TABLE "OpcionRespuesta" ADD 
    CONSTRAINT "PK32" PRIMARY KEY (id_opcion_respuesta)
;

-- 
-- TABLE: "Pago" 
--

ALTER TABLE "Pago" ADD 
    CONSTRAINT "PK25" PRIMARY KEY (id_pago)
;

-- 
-- TABLE: "Pool" 
--

ALTER TABLE "Pool" ADD 
    CONSTRAINT "PK30" PRIMARY KEY (id_pool)
;

-- 
-- TABLE: "Pool Autoevaluacion" 
--

ALTER TABLE "Pool Autoevaluacion" ADD 
    CONSTRAINT "PK58" PRIMARY KEY (id_pool, id_autoevaluacion)
;

-- 
-- TABLE: "Pregunta" 
--

ALTER TABLE "Pregunta" ADD 
    CONSTRAINT "PK31" PRIMARY KEY (id_pregunta)
;

-- 
-- TABLE: "Programa" 
--

ALTER TABLE "Programa" ADD 
    CONSTRAINT "PK74" PRIMARY KEY (id_programa)
;

-- 
-- TABLE: "Progreso" 
--

ALTER TABLE "Progreso" ADD 
    CONSTRAINT "PK70" PRIMARY KEY (id_progreso)
;

-- 
-- TABLE: "Reporte" 
--

ALTER TABLE "Reporte" ADD 
    CONSTRAINT "PK48" PRIMARY KEY (id_reporte)
;

-- 
-- TABLE: "RespuestaForo" 
--

ALTER TABLE "RespuestaForo" ADD 
    CONSTRAINT "PK22" PRIMARY KEY (id_respuesta_foro)
;

-- 
-- TABLE: "RespuestaIntento" 
--

ALTER TABLE "RespuestaIntento" ADD 
    CONSTRAINT "PK37" PRIMARY KEY (id_respuesta_intento)
;

-- 
-- TABLE: "Rol" 
--

ALTER TABLE "Rol" ADD 
    CONSTRAINT "PK40" PRIMARY KEY (id_rol)
;

-- 
-- TABLE: "Sesion" 
--

ALTER TABLE "Sesion" ADD 
    CONSTRAINT "PK44" PRIMARY KEY (id_sesion)
;

-- 
-- TABLE: "Supervisor" 
--

ALTER TABLE "Supervisor" ADD 
    CONSTRAINT "PK87" PRIMARY KEY (id)
;

-- 
-- TABLE: "TerminoGlosario" 
--

ALTER TABLE "TerminoGlosario" ADD 
    CONSTRAINT "PK16" PRIMARY KEY (id_termino_glosario)
;

-- 
-- TABLE: "TipoAccionAuditoria" 
--

ALTER TABLE "TipoAccionAuditoria" ADD 
    CONSTRAINT "PK45" PRIMARY KEY (id_tipo_accion_auditoria)
;

-- 
-- TABLE: "TipoMaterial" 
--

ALTER TABLE "TipoMaterial" ADD 
    CONSTRAINT "PK9" PRIMARY KEY (id_tipo_material)
;

-- 
-- TABLE: "TipoReporte" 
--

ALTER TABLE "TipoReporte" ADD 
    CONSTRAINT "PK47" PRIMARY KEY (id_tipo_reporte)
;

-- 
-- TABLE: "TituloDocente" 
--

ALTER TABLE "TituloDocente" ADD 
    CONSTRAINT "PK66" PRIMARY KEY (id_titulo_docente)
;

-- 
-- TABLE: "Unidad" 
--

ALTER TABLE "Unidad" ADD 
    CONSTRAINT "PK8" PRIMARY KEY (id_unidad)
;

-- 
-- TABLE: "Usuario" 
--

ALTER TABLE "Usuario" ADD 
    CONSTRAINT "PK36" PRIMARY KEY (id_usuario)
;

-- 
-- TABLE: "Administrador" 
--

ALTER TABLE "Administrador" ADD CONSTRAINT "RefUsuario1391" 
    FOREIGN KEY (id_usuario)
    REFERENCES "Usuario"(id_usuario)
;


-- 
-- TABLE: "Alumno" 
--

ALTER TABLE "Alumno" ADD CONSTRAINT "RefUsuario1401" 
    FOREIGN KEY (id_usuario)
    REFERENCES "Usuario"(id_usuario)
;


-- 
-- TABLE: "Auditoria" 
--

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefTipoAccionAuditoria1361" 
    FOREIGN KEY (id_tipo_accion_auditoria)
    REFERENCES "TipoAccionAuditoria"(id_tipo_accion_auditoria)
;

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefUsuario1371" 
    FOREIGN KEY (id_usuario)
    REFERENCES "Usuario"(id_usuario)
;


-- 
-- TABLE: "Autoevaluacion" 
--

ALTER TABLE "Autoevaluacion" ADD CONSTRAINT "RefUnidad1501" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;


-- 
-- TABLE: "ClaseClon" 
--

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefMaterial1751" 
    FOREIGN KEY (id_material)
    REFERENCES "Material"(id_material)
;

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefDocente1291" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefEstadoClaseClon1331" 
    FOREIGN KEY (id_estado_clase_clon)
    REFERENCES "EstadoClaseClon"(id_estado_clase_clon)
;


-- 
-- TABLE: "ClaseEnVivo" 
--

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefMaterial1761" 
    FOREIGN KEY (id_material)
    REFERENCES "Material"(id_material)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefCohorte1861" 
    FOREIGN KEY (id_cohorte)
    REFERENCES "Cohorte"(id_cohorte)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefDocente1281" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefEstadoClaseEnVIvo1321" 
    FOREIGN KEY (id_estado_clase_en_vivo)
    REFERENCES "EstadoClaseEnVIvo"(id_estado_clase_en_vivo)
;


-- 
-- TABLE: "Cohorte" 
--

ALTER TABLE "Cohorte" ADD CONSTRAINT "RefPrograma1221" 
    FOREIGN KEY (id_programa)
    REFERENCES "Programa"(id_programa)
;


-- 
-- TABLE: "Configuracion" 
--

ALTER TABLE "Configuracion" ADD CONSTRAINT "RefAdministrador1381" 
    FOREIGN KEY (id_administrador)
    REFERENCES "Administrador"(id_administrador)
;


-- 
-- TABLE: "ConsultaForo" 
--

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefAlumno1621" 
    FOREIGN KEY (id_alumno)
    REFERENCES "Alumno"(id_alumno)
;

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefUnidad1631" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;


-- 
-- TABLE: "Cronograma" 
--

ALTER TABLE "Cronograma" ADD CONSTRAINT "RefPrograma1821" 
    FOREIGN KEY (id_programa)
    REFERENCES "Programa"(id_programa)
;

ALTER TABLE "Cronograma" ADD CONSTRAINT "RefUnidad1831" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;


-- 
-- TABLE: "Curso" 
--

ALTER TABLE "Curso" ADD CONSTRAINT "RefDocente1871" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;

ALTER TABLE "Curso" ADD CONSTRAINT "RefCategoria1191" 
    FOREIGN KEY (id_categoria)
    REFERENCES "Categoria"(id_categoria)
;

ALTER TABLE "Curso" ADD CONSTRAINT "RefNivel1201" 
    FOREIGN KEY (id_nivel)
    REFERENCES "Nivel"(id_nivel)
;


-- 
-- TABLE: "Docente" 
--

ALTER TABLE "Docente" ADD CONSTRAINT "RefUsuario1251" 
    FOREIGN KEY (id_usuario)
    REFERENCES "Usuario"(id_usuario)
;


-- 
-- TABLE: "Inscripcion" 
--

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefAlumno1771" 
    FOREIGN KEY (id_alumno)
    REFERENCES "Alumno"(id_alumno)
;

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefCohorte1421" 
    FOREIGN KEY (id_cohorte)
    REFERENCES "Cohorte"(id_cohorte)
;


-- 
-- TABLE: "IntentoAutoevaluacion" 
--

ALTER TABLE "IntentoAutoevaluacion" ADD CONSTRAINT "RefInscripcion1491" 
    FOREIGN KEY (id_inscripcion)
    REFERENCES "Inscripcion"(id_inscripcion)
;

ALTER TABLE "IntentoAutoevaluacion" ADD CONSTRAINT "RefAutoevaluacion1511" 
    FOREIGN KEY (id_autoevaluacion)
    REFERENCES "Autoevaluacion"(id_autoevaluacion)
;


-- 
-- TABLE: "Material" 
--

ALTER TABLE "Material" ADD CONSTRAINT "RefTipoMaterial1711" 
    FOREIGN KEY (id_tipo_material)
    REFERENCES "TipoMaterial"(id_tipo_material)
;

ALTER TABLE "Material" ADD CONSTRAINT "RefUnidad1721" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;

ALTER TABLE "Material" ADD CONSTRAINT "RefDocente1271" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;


-- 
-- TABLE: "Modalidad Curso" 
--

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefModalidad671" 
    FOREIGN KEY (id_modalidad)
    REFERENCES "Modalidad"(id_modalidad)
;

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefCurso681" 
    FOREIGN KEY (id_curso)
    REFERENCES "Curso"(id_curso)
;


-- 
-- TABLE: "OpcionRespuesta" 
--

ALTER TABLE "OpcionRespuesta" ADD CONSTRAINT "RefPregunta1551" 
    FOREIGN KEY (id_pregunta)
    REFERENCES "Pregunta"(id_pregunta)
;


-- 
-- TABLE: "Pago" 
--

ALTER TABLE "Pago" ADD CONSTRAINT "RefDescuento1881" 
    FOREIGN KEY (id_descuento)
    REFERENCES "Descuento"(id_descuento)
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefEstadoPago1431" 
    FOREIGN KEY (id_estado_pago)
    REFERENCES "EstadoPago"(id_estado_pago)
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefMetodoPago1441" 
    FOREIGN KEY (id_metodo_pago)
    REFERENCES "MetodoPago"(id_metodo_pago)
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefInscripcion1451" 
    FOREIGN KEY (id_inscripcion)
    REFERENCES "Inscripcion"(id_inscripcion)
;


-- 
-- TABLE: "Pool" 
--

ALTER TABLE "Pool" ADD CONSTRAINT "RefUnidad1581" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;


-- 
-- TABLE: "Pool Autoevaluacion" 
--

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefPool641" 
    FOREIGN KEY (id_pool)
    REFERENCES "Pool"(id_pool)
;

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefAutoevaluacion651" 
    FOREIGN KEY (id_autoevaluacion)
    REFERENCES "Autoevaluacion"(id_autoevaluacion)
;


-- 
-- TABLE: "Pregunta" 
--

ALTER TABLE "Pregunta" ADD CONSTRAINT "RefPool1541" 
    FOREIGN KEY (id_pool)
    REFERENCES "Pool"(id_pool)
;


-- 
-- TABLE: "Programa" 
--

ALTER TABLE "Programa" ADD CONSTRAINT "RefCurso1211" 
    FOREIGN KEY (id_curso)
    REFERENCES "Curso"(id_curso)
;


-- 
-- TABLE: "Progreso" 
--

ALTER TABLE "Progreso" ADD CONSTRAINT "RefUnidad1591" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;

ALTER TABLE "Progreso" ADD CONSTRAINT "RefInscripcion1601" 
    FOREIGN KEY (id_inscripcion)
    REFERENCES "Inscripcion"(id_inscripcion)
;


-- 
-- TABLE: "Reporte" 
--

ALTER TABLE "Reporte" ADD CONSTRAINT "RefCurso1911" 
    FOREIGN KEY (id_curso)
    REFERENCES "Curso"(id_curso)
;

ALTER TABLE "Reporte" ADD CONSTRAINT "RefAdministrador1461" 
    FOREIGN KEY (id_administrador)
    REFERENCES "Administrador"(id_administrador)
;

ALTER TABLE "Reporte" ADD CONSTRAINT "RefTipoReporte1471" 
    FOREIGN KEY (id_tipo_reporte)
    REFERENCES "TipoReporte"(id_tipo_reporte)
;


-- 
-- TABLE: "RespuestaForo" 
--

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefDocente1261" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefConsultaForo1611" 
    FOREIGN KEY (id_consulta_foro)
    REFERENCES "ConsultaForo"(id_consulta_foro)
;


-- 
-- TABLE: "RespuestaIntento" 
--

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefIntentoAutoevaluacion1531" 
    FOREIGN KEY (id_intento_autoevaluacion)
    REFERENCES "IntentoAutoevaluacion"(id_intento_autoevaluacion)
;

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefOpcionRespuesta1571" 
    FOREIGN KEY (id_opcion_respuesta)
    REFERENCES "OpcionRespuesta"(id_opcion_respuesta)
;


-- 
-- TABLE: "Sesion" 
--

ALTER TABLE "Sesion" ADD CONSTRAINT "RefUsuario1351" 
    FOREIGN KEY (id_usuario)
    REFERENCES "Usuario"(id_usuario)
;


-- 
-- TABLE: "Supervisor" 
--

ALTER TABLE "Supervisor" ADD CONSTRAINT "RefCurso1841" 
    FOREIGN KEY (id_curso)
    REFERENCES "Curso"(id_curso)
;

ALTER TABLE "Supervisor" ADD CONSTRAINT "RefDocente1851" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;


-- 
-- TABLE: "TerminoGlosario" 
--

ALTER TABLE "TerminoGlosario" ADD CONSTRAINT "RefUnidad1341" 
    FOREIGN KEY (id_unidad)
    REFERENCES "Unidad"(id_unidad)
;


-- 
-- TABLE: "TituloDocente" 
--

ALTER TABLE "TituloDocente" ADD CONSTRAINT "RefDocente1241" 
    FOREIGN KEY (id_docente)
    REFERENCES "Docente"(id_docente)
;


-- 
-- TABLE: "Usuario" 
--

ALTER TABLE "Usuario" ADD CONSTRAINT "RefRol1891" 
    FOREIGN KEY (id_rol)
    REFERENCES "Rol"(id_rol)
;

