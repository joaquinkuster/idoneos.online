--
-- ER/Studio Data Architect SQL Code Generation
-- Company :      UNAM
-- Project :      ModeloConceptual.DM1
-- Author :       Joaquín
--
-- Date Created : Tuesday, August 25, 2026 03:43:38
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

ALTER TABLE "Ayudante"
DROP CONSTRAINT RefCurso184
;

ALTER TABLE "Ayudante"
DROP CONSTRAINT RefDocente185
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
DROP CONSTRAINT RefNivel192
;

ALTER TABLE "Curso"
DROP CONSTRAINT RefCategoria193
;

ALTER TABLE "DetalleAuditoria"
DROP CONSTRAINT RefAuditoria194
;

ALTER TABLE "Docente"
DROP CONSTRAINT RefUsuario125
;

ALTER TABLE "Inscripcion"
DROP CONSTRAINT RefCohorte142
;

ALTER TABLE "Inscripcion"
DROP CONSTRAINT RefAlumno177
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
DROP CONSTRAINT RefEstadoPago143
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefMetodoPago144
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefInscripcion145
;

ALTER TABLE "Pago"
DROP CONSTRAINT RefDescuento188
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
DROP CONSTRAINT RefAdministrador146
;

ALTER TABLE "Reporte"
DROP CONSTRAINT RefTipoReporte147
;

ALTER TABLE "Reporte"
DROP CONSTRAINT RefCurso191
;

ALTER TABLE "RespuestaForo"
DROP CONSTRAINT RefConsultaForo161
;

ALTER TABLE "RespuestaForo"
DROP CONSTRAINT RefDocente126
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
    "idAdministrador"  int4    NOT NULL,
    "idUsuario"        int4    NOT NULL,
    CONSTRAINT "PK54" PRIMARY KEY ("idAdministrador")
)
;



-- 
-- TABLE: "Alumno" 
--

CREATE TABLE "Alumno"(
    "idAlumno"   int4    NOT NULL,
    "idUsuario"  int4    NOT NULL,
    CONSTRAINT "PK53" PRIMARY KEY ("idAlumno")
)
;



-- 
-- TABLE: "Auditoria" 
--

CREATE TABLE "Auditoria"(
    "idAuditoria"      int4           NOT NULL,
    "entidadAfectada"  varchar(50)    NOT NULL,
    "idAfectado"       int4           NOT NULL,
    "ipUsuario"        varchar(45)    NOT NULL,
    "fechaHora"        timestamp      NOT NULL,
    "idTipoAuditoria"  int4           NOT NULL,
    "idUsuario"        int4           NOT NULL,
    CONSTRAINT "PK46" PRIMARY KEY ("idAuditoria")
)
;



-- 
-- TABLE: "Autoevaluacion" 
--

CREATE TABLE "Autoevaluacion"(
    "idAutoevaluacion"    int4           NOT NULL,
    nombre                varchar(50)    NOT NULL,
    "tiempoLimite"        int4           NOT NULL,
    "cantidadPreguntas"   int4           NOT NULL,
    "intentosPermitidos"  int4,
    "fechaApertura"       timestamp      NOT NULL,
    "fechaCierre"         timestamp,
    oculto                boolean        NOT NULL,
    "fechaCreacion"       timestamp      NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean        NOT NULL,
    "idUnidad"            int4           NOT NULL,
    CONSTRAINT "PK33" PRIMARY KEY ("idAutoevaluacion")
)
;



-- 
-- TABLE: "Ayudante" 
--

CREATE TABLE "Ayudante"(
    "idAyudante"  int4    NOT NULL,
    "idCurso"     int4    NOT NULL,
    "idDocente"   int4    NOT NULL,
    CONSTRAINT "PK87" PRIMARY KEY ("idAyudante")
)
;



-- 
-- TABLE: "Categoria" 
--

CREATE TABLE "Categoria"(
    "idCategoria"         int4            NOT NULL,
    nombre                varchar(50)     NOT NULL,
    descripcion           varchar(150),
    "fechaCreacion"       timestamp       NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean         NOT NULL,
    CONSTRAINT "PK5" PRIMARY KEY ("idCategoria")
)
;



-- 
-- TABLE: "ClaseClon" 
--

CREATE TABLE "ClaseClon"(
    "idClaseClon"        int4           NOT NULL,
    titulo               varchar(50)    NOT NULL,
    guion                text           NOT NULL,
    "fechaGeneracion"    timestamp      NOT NULL,
    oculto               boolean        NOT NULL,
    baja                 boolean        NOT NULL,
    "idDocente"          int4           NOT NULL,
    "idEstadoClaseClon"  int4           NOT NULL,
    "idMaterial"         int4,
    CONSTRAINT "PK20" PRIMARY KEY ("idClaseClon")
)
;



-- 
-- TABLE: "ClaseEnVivo" 
--

CREATE TABLE "ClaseEnVivo"(
    "idClaseEnVivo"        int4            NOT NULL,
    titulo                 varchar(50)     NOT NULL,
    "fechaHora"            timestamp       NOT NULL,
    "duracionEstimada"     int4            NOT NULL,
    "urlRtmp"              varchar(255)    NOT NULL,
    "claveStream"          varchar(100)    NOT NULL,
    oculto                 boolean         NOT NULL,
    baja                   boolean         NOT NULL,
    "idDocente"            int4            NOT NULL,
    "idEstadoClaseEnVivo"  int4            NOT NULL,
    "idMaterial"           int4,
    "idCohorte"            int4            NOT NULL,
    CONSTRAINT "PK18" PRIMARY KEY ("idClaseEnVivo")
)
;



-- 
-- TABLE: "Cohorte" 
--

CREATE TABLE "Cohorte"(
    "idCohorte"               int4         NOT NULL,
    "fechaInicioInscripcion"  timestamp    NOT NULL,
    "fechaFinInscripcion"     timestamp    NOT NULL,
    "fechaInicioDictado"      timestamp,
    "fechaFinDictado"         timestamp,
    "cupoMaximo"              int4,
    "semanasAcceso"           int4         NOT NULL,
    baja                      boolean      NOT NULL,
    "fechaCreacion"           timestamp    NOT NULL,
    "ultimaModificacion"      timestamp,
    "idPrograma"              int4         NOT NULL,
    CONSTRAINT "PK76" PRIMARY KEY ("idCohorte")
)
;



-- 
-- TABLE: "Configuracion" 
--

CREATE TABLE "Configuracion"(
    "idConfiguracion"  int4            NOT NULL,
    clave              varchar(100)    NOT NULL,
    valor              text            NOT NULL,
    "idAdministrador"  int4            NOT NULL,
    CONSTRAINT "PK49" PRIMARY KEY ("idConfiguracion")
)
;



-- 
-- TABLE: "ConsultaForo" 
--

CREATE TABLE "ConsultaForo"(
    "idConsulta"  int4            NOT NULL,
    texto         varchar(500)    NOT NULL,
    fecha         timestamp       NOT NULL,
    baja          boolean         NOT NULL,
    "idAlumno"    int4            NOT NULL,
    "idUnidad"    int4            NOT NULL,
    CONSTRAINT "PK21" PRIMARY KEY ("idConsulta")
)
;



-- 
-- TABLE: "Cronograma" 
--

CREATE TABLE "Cronograma"(
    "idCronograma"     int4    NOT NULL,
    "numeroOrden"      int4    NOT NULL,
    "semanasDuracion"  int4    NOT NULL,
    "idPrograma"       int4    NOT NULL,
    "idUnidad"         int4    NOT NULL,
    CONSTRAINT "PK86" PRIMARY KEY ("idCronograma")
)
;



-- 
-- TABLE: "Curso" 
--

CREATE TABLE "Curso"(
    "idCurso"             int4            NOT NULL,
    nombre                varchar(50)     NOT NULL,
    descripcion           varchar(150),
    precio                float4          NOT NULL,
    imagen                varchar(150),
    "emiteCertificado"    boolean         NOT NULL,
    "fechaCreacion"       timestamp       NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean         NOT NULL,
    "idDocente"           int4            NOT NULL,
    "idNivel"             int4            NOT NULL,
    "idCategoria"         int4            NOT NULL,
    CONSTRAINT "PK4" PRIMARY KEY ("idCurso")
)
;



-- 
-- TABLE: "Descuento" 
--

CREATE TABLE "Descuento"(
    "idDescuento"         int4           NOT NULL,
    nombre                varchar(50)    NOT NULL,
    "cursosRequeridos"    int4           NOT NULL,
    porcentaje            float4         NOT NULL,
    "vigenciaDesde"       timestamp      NOT NULL,
    "vigenciaHasta"       timestamp      NOT NULL,
    "cantidadLimite"      int4           NOT NULL,
    "cantidadUsada"       int4           NOT NULL,
    "fechaCreacion"       timestamp      NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean        NOT NULL,
    CONSTRAINT "PK28" PRIMARY KEY ("idDescuento")
)
;



-- 
-- TABLE: "DetalleAuditoria" 
--

CREATE TABLE "DetalleAuditoria"(
    "idDetalleAuditoria"  int4           NOT NULL,
    campo                 varchar(50),
    "valorAnterior"       text,
    "valorNuevo"          text,
    "idAuditoria"         int4           NOT NULL,
    CONSTRAINT "PK88" PRIMARY KEY ("idDetalleAuditoria")
)
;



-- 
-- TABLE: "Docente" 
--

CREATE TABLE "Docente"(
    "idDocente"               int4            NOT NULL,
    "aniosExperiencia"        int4            NOT NULL,
    "matriculaCnv"            varchar(50),
    biografia                 text,
    habilitado                boolean         NOT NULL,
    "fechaAceptacionTycClon"  timestamp,
    "avatarId"                varchar(100),
    "voiceId"                 varchar(100),
    "idUsuario"               int4            NOT NULL,
    CONSTRAINT "PK42" PRIMARY KEY ("idDocente")
)
;



-- 
-- TABLE: "EstadoClaseClon" 
--

CREATE TABLE "EstadoClaseClon"(
    "idEstadoClaseClon"  int4           NOT NULL,
    nombre               varchar(50)    NOT NULL,
    CONSTRAINT "PK19" PRIMARY KEY ("idEstadoClaseClon")
)
;



-- 
-- TABLE: "EstadoClaseEnVIvo" 
--

CREATE TABLE "EstadoClaseEnVIvo"(
    "idEstadoClaseEnVivo"  int4           NOT NULL,
    nombre                 varchar(50)    NOT NULL,
    CONSTRAINT "PK17" PRIMARY KEY ("idEstadoClaseEnVivo")
)
;



-- 
-- TABLE: "EstadoPago" 
--

CREATE TABLE "EstadoPago"(
    "idEstadoPago"  int4           NOT NULL,
    nombre          varchar(50)    NOT NULL,
    CONSTRAINT "PK24" PRIMARY KEY ("idEstadoPago")
)
;



-- 
-- TABLE: "Inscripcion" 
--

CREATE TABLE "Inscripcion"(
    "idInscripcion"            int4            NOT NULL,
    fecha                      timestamp       NOT NULL,
    "fechaVencimientoAcceso"   timestamp       NOT NULL,
    observaciones              varchar(500),
    "numeroCertificado"        varchar(100),
    "nombreAlumno"             varchar(100),
    "dniAlumno"                varchar(8),
    "textoCertificado"         text,
    "fechaEmisionCertificado"  timestamp,
    "certificadoEnviado"       boolean         NOT NULL,
    baja                       boolean         NOT NULL,
    "idCohorte"                int4            NOT NULL,
    "idAlumno"                 int4            NOT NULL,
    CONSTRAINT "PK23" PRIMARY KEY ("idInscripcion")
)
;



-- 
-- TABLE: "IntentoAutoevaluacion" 
--

CREATE TABLE "IntentoAutoevaluacion"(
    "idIntentoAutoevaluacion"  int4         NOT NULL,
    "fechaEntrega"             timestamp,
    nota                       float4,
    baja                       boolean      NOT NULL,
    "idInscripcion"            int4         NOT NULL,
    "idAutoevaluacion"         int4         NOT NULL,
    CONSTRAINT "PK34" PRIMARY KEY ("idIntentoAutoevaluacion")
)
;



-- 
-- TABLE: "Material" 
--

CREATE TABLE "Material"(
    "idMaterial"          int4            NOT NULL,
    titulo                varchar(50)     NOT NULL,
    "rutaArchivo"         varchar(150),
    contenido             varchar(500),
    autor                 varchar(50),
    "generadoPorIa"       boolean         NOT NULL,
    oculto                boolean         NOT NULL,
    "fechaCreacion"       timestamp       NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean         NOT NULL,
    "idDocente"           int4            NOT NULL,
    "idTipoMaterial"      int4            NOT NULL,
    "idUnidad"            int4            NOT NULL,
    CONSTRAINT "PK10" PRIMARY KEY ("idMaterial")
)
;



-- 
-- TABLE: "MetodoPago" 
--

CREATE TABLE "MetodoPago"(
    "idMetodoPago"  int4           NOT NULL,
    nombre          varchar(50)    NOT NULL,
    CONSTRAINT "PK26" PRIMARY KEY ("idMetodoPago")
)
;



-- 
-- TABLE: "Modalidad" 
--

CREATE TABLE "Modalidad"(
    "idModalidad"  int4           NOT NULL,
    nombre         varchar(50)    NOT NULL,
    CONSTRAINT "PK6" PRIMARY KEY ("idModalidad")
)
;



-- 
-- TABLE: "Modalidad Curso" 
--

CREATE TABLE "Modalidad Curso"(
    "idModalidad"  int4    NOT NULL,
    "idCurso"      int4    NOT NULL,
    CONSTRAINT "PK59" PRIMARY KEY ("idModalidad", "idCurso")
)
;



-- 
-- TABLE: "Nivel" 
--

CREATE TABLE "Nivel"(
    "idNivel"  int4           NOT NULL,
    nombre     varchar(50)    NOT NULL,
    CONSTRAINT "PK81" PRIMARY KEY ("idNivel")
)
;



-- 
-- TABLE: "OpcionRespuesta" 
--

CREATE TABLE "OpcionRespuesta"(
    "idOpcionRespuesta"  int4            NOT NULL,
    texto                varchar(150)    NOT NULL,
    "esCorrecta"         boolean         NOT NULL,
    baja                 boolean         NOT NULL,
    "idPregunta"         int4            NOT NULL,
    CONSTRAINT "PK32" PRIMARY KEY ("idOpcionRespuesta")
)
;



-- 
-- TABLE: "Pago" 
--

CREATE TABLE "Pago"(
    "idPago"                   int4            NOT NULL,
    monto                      float4          NOT NULL,
    fecha                      timestamp       NOT NULL,
    "paymentRequestId"         varchar(50),
    "externalIntentionId"      varchar(50)     NOT NULL,
    "referenceCode"            varchar(20),
    "ultimosDigitosTarjeta"    varchar(4),
    "detalleEstado"            varchar(100),
    "fechaAprobacion"          timestamp,
    "nombrePagador"            varchar(50),
    "dniPagador"               varchar(8),
    "numeroComprobante"        varchar(100),
    "fechaEmisionComprobante"  timestamp,
    "comprobanteEnviado"       boolean         NOT NULL,
    "idEstadoPago"             int4            NOT NULL,
    "idMetodoPago"             int4            NOT NULL,
    "idInscripcion"            int4            NOT NULL,
    "idDescuento"              int4,
    CONSTRAINT "PK25" PRIMARY KEY ("idPago")
)
;



-- 
-- TABLE: "Pool" 
--

CREATE TABLE "Pool"(
    "idPool"              int4           NOT NULL,
    nombre                varchar(50)    NOT NULL,
    "fechaCreacion"       timestamp      NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean        NOT NULL,
    "idUnidad"            int4           NOT NULL,
    CONSTRAINT "PK30" PRIMARY KEY ("idPool")
)
;



-- 
-- TABLE: "Pool Autoevaluacion" 
--

CREATE TABLE "Pool Autoevaluacion"(
    "idPool"            int4    NOT NULL,
    "idAutoevaluacion"  int4    NOT NULL,
    CONSTRAINT "PK58" PRIMARY KEY ("idPool", "idAutoevaluacion")
)
;



-- 
-- TABLE: "Pregunta" 
--

CREATE TABLE "Pregunta"(
    "idPregunta"        int4            NOT NULL,
    texto               varchar(150)    NOT NULL,
    "esOpcionMultiple"  boolean         NOT NULL,
    baja                boolean         NOT NULL,
    "idPool"            int4            NOT NULL,
    CONSTRAINT "PK31" PRIMARY KEY ("idPregunta")
)
;



-- 
-- TABLE: "Programa" 
--

CREATE TABLE "Programa"(
    "idPrograma"          int4            NOT NULL,
    nombre                varchar(50)     NOT NULL,
    descripcion           varchar(150),
    objetivos             text            NOT NULL,
    "cargaHorariaTotal"   int4,
    bibliografia          text            NOT NULL,
    "fechaCreacion"       timestamp       NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean         NOT NULL,
    "idCurso"             int4            NOT NULL,
    CONSTRAINT "PK74" PRIMARY KEY ("idPrograma")
)
;



-- 
-- TABLE: "Progreso" 
--

CREATE TABLE "Progreso"(
    "idProgreso"       int4         NOT NULL,
    completada         boolean      NOT NULL,
    "fechaCompletada"  timestamp,
    "idUnidad"         int4         NOT NULL,
    "idInscripcion"    int4         NOT NULL,
    CONSTRAINT "PK70" PRIMARY KEY ("idProgreso")
)
;



-- 
-- TABLE: "Reporte" 
--

CREATE TABLE "Reporte"(
    "idReporte"        int4         NOT NULL,
    "fechaGeneracion"  timestamp    NOT NULL,
    "idAdministrador"  int4         NOT NULL,
    "idTipoReporte"    int4         NOT NULL,
    "idCurso"          int4         NOT NULL,
    CONSTRAINT "PK48" PRIMARY KEY ("idReporte")
)
;



-- 
-- TABLE: "RespuestaForo" 
--

CREATE TABLE "RespuestaForo"(
    "idRespuesta"  int4            NOT NULL,
    texto          varchar(500)    NOT NULL,
    fecha          timestamp       NOT NULL,
    baja           boolean         NOT NULL,
    "idDocente"    int4            NOT NULL,
    "idConsulta"   int4            NOT NULL,
    CONSTRAINT "PK22" PRIMARY KEY ("idRespuesta")
)
;



-- 
-- TABLE: "RespuestaIntento" 
--

CREATE TABLE "RespuestaIntento"(
    "idRespuestaIntento"       int4    NOT NULL,
    "idIntentoAutoevaluacion"  int4    NOT NULL,
    "idOpcionRespuesta"        int4    NOT NULL,
    CONSTRAINT "PK37" PRIMARY KEY ("idRespuestaIntento")
)
;



-- 
-- TABLE: "Rol" 
--

CREATE TABLE "Rol"(
    "idRol"   int4           NOT NULL,
    nombre    varchar(50),
    CONSTRAINT "PK40" PRIMARY KEY ("idRol")
)
;



-- 
-- TABLE: "Sesion" 
--

CREATE TABLE "Sesion"(
    "idSesion"     int4            NOT NULL,
    token          varchar(255)    NOT NULL,
    "fechaInicio"  timestamp       NOT NULL,
    "fechaFin"     timestamp       NOT NULL,
    ip             varchar(45)     NOT NULL,
    dispositivo    varchar(255)    NOT NULL,
    "idUsuario"    int4            NOT NULL,
    CONSTRAINT "PK44" PRIMARY KEY ("idSesion")
)
;



-- 
-- TABLE: "TerminoGlosario" 
--

CREATE TABLE "TerminoGlosario"(
    "idTermino"   int4            NOT NULL,
    termino       varchar(50)     NOT NULL,
    definicion    varchar(150)    NOT NULL,
    baja          boolean         NOT NULL,
    "idUnidad"    int4            NOT NULL,
    CONSTRAINT "PK16" PRIMARY KEY ("idTermino")
)
;



-- 
-- TABLE: "TipoAccionAuditoria" 
--

CREATE TABLE "TipoAccionAuditoria"(
    "idTipoAuditoria"  int4           NOT NULL,
    nombre             varchar(50)    NOT NULL,
    CONSTRAINT "PK45" PRIMARY KEY ("idTipoAuditoria")
)
;



-- 
-- TABLE: "TipoMaterial" 
--

CREATE TABLE "TipoMaterial"(
    "idTipoMaterial"  int4           NOT NULL,
    nombre            varchar(50)    NOT NULL,
    CONSTRAINT "PK9" PRIMARY KEY ("idTipoMaterial")
)
;



-- 
-- TABLE: "TipoReporte" 
--

CREATE TABLE "TipoReporte"(
    "idTipoReporte"  int4           NOT NULL,
    nombre           varchar(50)    NOT NULL,
    CONSTRAINT "PK47" PRIMARY KEY ("idTipoReporte")
)
;



-- 
-- TABLE: "TituloDocente" 
--

CREATE TABLE "TituloDocente"(
    "idTituloDocente"   int4            NOT NULL,
    titulo              varchar(100)    NOT NULL,
    "matriculaColegio"  varchar(50),
    "idDocente"         int4            NOT NULL,
    CONSTRAINT "PK66" PRIMARY KEY ("idTituloDocente")
)
;



-- 
-- TABLE: "Unidad" 
--

CREATE TABLE "Unidad"(
    "idUnidad"            int4            NOT NULL,
    titulo                varchar(50)     NOT NULL,
    descripcion           varchar(150),
    contenido             text            NOT NULL,
    "fechaCreacion"       timestamp       NOT NULL,
    "ultimaModificacion"  timestamp,
    baja                  boolean         NOT NULL,
    CONSTRAINT "PK8" PRIMARY KEY ("idUnidad")
)
;



-- 
-- TABLE: "Usuario" 
--

CREATE TABLE "Usuario"(
    "idUsuario"          int4            NOT NULL,
    nombre               varchar(50)     NOT NULL,
    apellido             varchar(50)     NOT NULL,
    dni                  varchar(8)      NOT NULL,
    email                varchar(150)    NOT NULL,
    contrasena           varchar(255),
    imagen               varchar(150),
    telefono             varchar(20),
    "tokenRecuperacion"  varchar(255),
    "expiracionToken"    timestamp,
    "googleId"           varchar(255),
    "emailValidado"      boolean         NOT NULL,
    "fechaRegistro"      timestamp       NOT NULL,
    baja                 boolean         NOT NULL,
    "idRol"              int4            NOT NULL,
    CONSTRAINT "PK36" PRIMARY KEY ("idUsuario")
)
;



-- 
-- INDEX: "Ref36139" 
--

CREATE INDEX "Ref36139" ON "Administrador"("idUsuario")
;
-- 
-- INDEX: "Ref36140" 
--

CREATE INDEX "Ref36140" ON "Alumno"("idUsuario")
;
-- 
-- INDEX: "Ref45136" 
--

CREATE INDEX "Ref45136" ON "Auditoria"("idTipoAuditoria")
;
-- 
-- INDEX: "Ref36137" 
--

CREATE INDEX "Ref36137" ON "Auditoria"("idUsuario")
;
-- 
-- INDEX: "Ref8150" 
--

CREATE INDEX "Ref8150" ON "Autoevaluacion"("idUnidad")
;
-- 
-- INDEX: "Ref4184" 
--

CREATE INDEX "Ref4184" ON "Ayudante"("idCurso")
;
-- 
-- INDEX: "Ref42185" 
--

CREATE INDEX "Ref42185" ON "Ayudante"("idDocente")
;
-- 
-- INDEX: "Ref10175" 
--

CREATE INDEX "Ref10175" ON "ClaseClon"("idMaterial")
;
-- 
-- INDEX: "Ref42129" 
--

CREATE INDEX "Ref42129" ON "ClaseClon"("idDocente")
;
-- 
-- INDEX: "Ref19133" 
--

CREATE INDEX "Ref19133" ON "ClaseClon"("idEstadoClaseClon")
;
-- 
-- INDEX: "Ref10176" 
--

CREATE INDEX "Ref10176" ON "ClaseEnVivo"("idMaterial")
;
-- 
-- INDEX: "Ref76186" 
--

CREATE INDEX "Ref76186" ON "ClaseEnVivo"("idCohorte")
;
-- 
-- INDEX: "Ref42128" 
--

CREATE INDEX "Ref42128" ON "ClaseEnVivo"("idDocente")
;
-- 
-- INDEX: "Ref17132" 
--

CREATE INDEX "Ref17132" ON "ClaseEnVivo"("idEstadoClaseEnVivo")
;
-- 
-- INDEX: "Ref74122" 
--

CREATE INDEX "Ref74122" ON "Cohorte"("idPrograma")
;
-- 
-- INDEX: "Ref54138" 
--

CREATE INDEX "Ref54138" ON "Configuracion"("idAdministrador")
;
-- 
-- INDEX: "Ref53162" 
--

CREATE INDEX "Ref53162" ON "ConsultaForo"("idAlumno")
;
-- 
-- INDEX: "Ref8163" 
--

CREATE INDEX "Ref8163" ON "ConsultaForo"("idUnidad")
;
-- 
-- INDEX: "Ref74182" 
--

CREATE INDEX "Ref74182" ON "Cronograma"("idPrograma")
;
-- 
-- INDEX: "Ref8183" 
--

CREATE INDEX "Ref8183" ON "Cronograma"("idUnidad")
;
-- 
-- INDEX: "Ref42187" 
--

CREATE INDEX "Ref42187" ON "Curso"("idDocente")
;
-- 
-- INDEX: "Ref81192" 
--

CREATE INDEX "Ref81192" ON "Curso"("idNivel")
;
-- 
-- INDEX: "Ref5193" 
--

CREATE INDEX "Ref5193" ON "Curso"("idCategoria")
;
-- 
-- INDEX: "Ref46194" 
--

CREATE INDEX "Ref46194" ON "DetalleAuditoria"("idAuditoria")
;
-- 
-- INDEX: "Ref36125" 
--

CREATE INDEX "Ref36125" ON "Docente"("idUsuario")
;
-- 
-- INDEX: "Ref76142" 
--

CREATE INDEX "Ref76142" ON "Inscripcion"("idCohorte")
;
-- 
-- INDEX: "Ref53177" 
--

CREATE INDEX "Ref53177" ON "Inscripcion"("idAlumno")
;
-- 
-- INDEX: "Ref23149" 
--

CREATE INDEX "Ref23149" ON "IntentoAutoevaluacion"("idInscripcion")
;
-- 
-- INDEX: "Ref33151" 
--

CREATE INDEX "Ref33151" ON "IntentoAutoevaluacion"("idAutoevaluacion")
;
-- 
-- INDEX: "Ref9171" 
--

CREATE INDEX "Ref9171" ON "Material"("idTipoMaterial")
;
-- 
-- INDEX: "Ref8172" 
--

CREATE INDEX "Ref8172" ON "Material"("idUnidad")
;
-- 
-- INDEX: "Ref42127" 
--

CREATE INDEX "Ref42127" ON "Material"("idDocente")
;
-- 
-- INDEX: "Ref667" 
--

CREATE INDEX "Ref667" ON "Modalidad Curso"("idModalidad")
;
-- 
-- INDEX: "Ref468" 
--

CREATE INDEX "Ref468" ON "Modalidad Curso"("idCurso")
;
-- 
-- INDEX: "Ref31155" 
--

CREATE INDEX "Ref31155" ON "OpcionRespuesta"("idPregunta")
;
-- 
-- INDEX: "Ref24143" 
--

CREATE INDEX "Ref24143" ON "Pago"("idEstadoPago")
;
-- 
-- INDEX: "Ref26144" 
--

CREATE INDEX "Ref26144" ON "Pago"("idMetodoPago")
;
-- 
-- INDEX: "Ref23145" 
--

CREATE INDEX "Ref23145" ON "Pago"("idInscripcion")
;
-- 
-- INDEX: "Ref28188" 
--

CREATE INDEX "Ref28188" ON "Pago"("idDescuento")
;
-- 
-- INDEX: "Ref8158" 
--

CREATE INDEX "Ref8158" ON "Pool"("idUnidad")
;
-- 
-- INDEX: "Ref3064" 
--

CREATE INDEX "Ref3064" ON "Pool Autoevaluacion"("idPool")
;
-- 
-- INDEX: "Ref3365" 
--

CREATE INDEX "Ref3365" ON "Pool Autoevaluacion"("idAutoevaluacion")
;
-- 
-- INDEX: "Ref30154" 
--

CREATE INDEX "Ref30154" ON "Pregunta"("idPool")
;
-- 
-- INDEX: "Ref4121" 
--

CREATE INDEX "Ref4121" ON "Programa"("idCurso")
;
-- 
-- INDEX: "Ref8159" 
--

CREATE INDEX "Ref8159" ON "Progreso"("idUnidad")
;
-- 
-- INDEX: "Ref23160" 
--

CREATE INDEX "Ref23160" ON "Progreso"("idInscripcion")
;
-- 
-- INDEX: "Ref54146" 
--

CREATE INDEX "Ref54146" ON "Reporte"("idAdministrador")
;
-- 
-- INDEX: "Ref47147" 
--

CREATE INDEX "Ref47147" ON "Reporte"("idTipoReporte")
;
-- 
-- INDEX: "Ref4191" 
--

CREATE INDEX "Ref4191" ON "Reporte"("idCurso")
;
-- 
-- INDEX: "Ref21161" 
--

CREATE INDEX "Ref21161" ON "RespuestaForo"("idConsulta")
;
-- 
-- INDEX: "Ref42126" 
--

CREATE INDEX "Ref42126" ON "RespuestaForo"("idDocente")
;
-- 
-- INDEX: "Ref34153" 
--

CREATE INDEX "Ref34153" ON "RespuestaIntento"("idIntentoAutoevaluacion")
;
-- 
-- INDEX: "Ref32157" 
--

CREATE INDEX "Ref32157" ON "RespuestaIntento"("idOpcionRespuesta")
;
-- 
-- INDEX: "Ref36135" 
--

CREATE INDEX "Ref36135" ON "Sesion"("idUsuario")
;
-- 
-- INDEX: "Ref8134" 
--

CREATE INDEX "Ref8134" ON "TerminoGlosario"("idUnidad")
;
-- 
-- INDEX: "Ref42124" 
--

CREATE INDEX "Ref42124" ON "TituloDocente"("idDocente")
;
-- 
-- INDEX: "Ref40189" 
--

CREATE INDEX "Ref40189" ON "Usuario"("idRol")
;
-- 
-- TABLE: "Administrador" 
--

ALTER TABLE "Administrador" ADD CONSTRAINT "RefUsuario1391" 
    FOREIGN KEY ("idUsuario")
    REFERENCES "Usuario"("idUsuario")
;


-- 
-- TABLE: "Alumno" 
--

ALTER TABLE "Alumno" ADD CONSTRAINT "RefUsuario1401" 
    FOREIGN KEY ("idUsuario")
    REFERENCES "Usuario"("idUsuario")
;


-- 
-- TABLE: "Auditoria" 
--

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefTipoAccionAuditoria1361" 
    FOREIGN KEY ("idTipoAuditoria")
    REFERENCES "TipoAccionAuditoria"("idTipoAuditoria")
;

ALTER TABLE "Auditoria" ADD CONSTRAINT "RefUsuario1371" 
    FOREIGN KEY ("idUsuario")
    REFERENCES "Usuario"("idUsuario")
;


-- 
-- TABLE: "Autoevaluacion" 
--

ALTER TABLE "Autoevaluacion" ADD CONSTRAINT "RefUnidad1501" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;


-- 
-- TABLE: "Ayudante" 
--

ALTER TABLE "Ayudante" ADD CONSTRAINT "RefCurso1841" 
    FOREIGN KEY ("idCurso")
    REFERENCES "Curso"("idCurso")
;

ALTER TABLE "Ayudante" ADD CONSTRAINT "RefDocente1851" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;


-- 
-- TABLE: "ClaseClon" 
--

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefMaterial1751" 
    FOREIGN KEY ("idMaterial")
    REFERENCES "Material"("idMaterial")
;

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefDocente1291" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;

ALTER TABLE "ClaseClon" ADD CONSTRAINT "RefEstadoClaseClon1331" 
    FOREIGN KEY ("idEstadoClaseClon")
    REFERENCES "EstadoClaseClon"("idEstadoClaseClon")
;


-- 
-- TABLE: "ClaseEnVivo" 
--

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefMaterial1761" 
    FOREIGN KEY ("idMaterial")
    REFERENCES "Material"("idMaterial")
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefCohorte1861" 
    FOREIGN KEY ("idCohorte")
    REFERENCES "Cohorte"("idCohorte")
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefDocente1281" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;

ALTER TABLE "ClaseEnVivo" ADD CONSTRAINT "RefEstadoClaseEnVIvo1321" 
    FOREIGN KEY ("idEstadoClaseEnVivo")
    REFERENCES "EstadoClaseEnVIvo"("idEstadoClaseEnVivo")
;


-- 
-- TABLE: "Cohorte" 
--

ALTER TABLE "Cohorte" ADD CONSTRAINT "RefPrograma1221" 
    FOREIGN KEY ("idPrograma")
    REFERENCES "Programa"("idPrograma")
;


-- 
-- TABLE: "Configuracion" 
--

ALTER TABLE "Configuracion" ADD CONSTRAINT "RefAdministrador1381" 
    FOREIGN KEY ("idAdministrador")
    REFERENCES "Administrador"("idAdministrador")
;


-- 
-- TABLE: "ConsultaForo" 
--

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefAlumno1621" 
    FOREIGN KEY ("idAlumno")
    REFERENCES "Alumno"("idAlumno")
;

ALTER TABLE "ConsultaForo" ADD CONSTRAINT "RefUnidad1631" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;


-- 
-- TABLE: "Cronograma" 
--

ALTER TABLE "Cronograma" ADD CONSTRAINT "RefPrograma1821" 
    FOREIGN KEY ("idPrograma")
    REFERENCES "Programa"("idPrograma")
;

ALTER TABLE "Cronograma" ADD CONSTRAINT "RefUnidad1831" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;


-- 
-- TABLE: "Curso" 
--

ALTER TABLE "Curso" ADD CONSTRAINT "RefDocente1871" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;

ALTER TABLE "Curso" ADD CONSTRAINT "RefNivel1921" 
    FOREIGN KEY ("idNivel")
    REFERENCES "Nivel"("idNivel")
;

ALTER TABLE "Curso" ADD CONSTRAINT "RefCategoria1931" 
    FOREIGN KEY ("idCategoria")
    REFERENCES "Categoria"("idCategoria")
;


-- 
-- TABLE: "DetalleAuditoria" 
--

ALTER TABLE "DetalleAuditoria" ADD CONSTRAINT "RefAuditoria1941" 
    FOREIGN KEY ("idAuditoria")
    REFERENCES "Auditoria"("idAuditoria")
;


-- 
-- TABLE: "Docente" 
--

ALTER TABLE "Docente" ADD CONSTRAINT "RefUsuario1251" 
    FOREIGN KEY ("idUsuario")
    REFERENCES "Usuario"("idUsuario")
;


-- 
-- TABLE: "Inscripcion" 
--

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefCohorte1421" 
    FOREIGN KEY ("idCohorte")
    REFERENCES "Cohorte"("idCohorte")
;

ALTER TABLE "Inscripcion" ADD CONSTRAINT "RefAlumno1771" 
    FOREIGN KEY ("idAlumno")
    REFERENCES "Alumno"("idAlumno")
;


-- 
-- TABLE: "IntentoAutoevaluacion" 
--

ALTER TABLE "IntentoAutoevaluacion" ADD CONSTRAINT "RefInscripcion1491" 
    FOREIGN KEY ("idInscripcion")
    REFERENCES "Inscripcion"("idInscripcion")
;

ALTER TABLE "IntentoAutoevaluacion" ADD CONSTRAINT "RefAutoevaluacion1511" 
    FOREIGN KEY ("idAutoevaluacion")
    REFERENCES "Autoevaluacion"("idAutoevaluacion")
;


-- 
-- TABLE: "Material" 
--

ALTER TABLE "Material" ADD CONSTRAINT "RefTipoMaterial1711" 
    FOREIGN KEY ("idTipoMaterial")
    REFERENCES "TipoMaterial"("idTipoMaterial")
;

ALTER TABLE "Material" ADD CONSTRAINT "RefUnidad1721" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;

ALTER TABLE "Material" ADD CONSTRAINT "RefDocente1271" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;


-- 
-- TABLE: "Modalidad Curso" 
--

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefModalidad671" 
    FOREIGN KEY ("idModalidad")
    REFERENCES "Modalidad"("idModalidad")
;

ALTER TABLE "Modalidad Curso" ADD CONSTRAINT "RefCurso681" 
    FOREIGN KEY ("idCurso")
    REFERENCES "Curso"("idCurso")
;


-- 
-- TABLE: "OpcionRespuesta" 
--

ALTER TABLE "OpcionRespuesta" ADD CONSTRAINT "RefPregunta1551" 
    FOREIGN KEY ("idPregunta")
    REFERENCES "Pregunta"("idPregunta")
;


-- 
-- TABLE: "Pago" 
--

ALTER TABLE "Pago" ADD CONSTRAINT "RefEstadoPago1431" 
    FOREIGN KEY ("idEstadoPago")
    REFERENCES "EstadoPago"("idEstadoPago")
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefMetodoPago1441" 
    FOREIGN KEY ("idMetodoPago")
    REFERENCES "MetodoPago"("idMetodoPago")
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefInscripcion1451" 
    FOREIGN KEY ("idInscripcion")
    REFERENCES "Inscripcion"("idInscripcion")
;

ALTER TABLE "Pago" ADD CONSTRAINT "RefDescuento1881" 
    FOREIGN KEY ("idDescuento")
    REFERENCES "Descuento"("idDescuento")
;


-- 
-- TABLE: "Pool" 
--

ALTER TABLE "Pool" ADD CONSTRAINT "RefUnidad1581" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;


-- 
-- TABLE: "Pool Autoevaluacion" 
--

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefPool641" 
    FOREIGN KEY ("idPool")
    REFERENCES "Pool"("idPool")
;

ALTER TABLE "Pool Autoevaluacion" ADD CONSTRAINT "RefAutoevaluacion651" 
    FOREIGN KEY ("idAutoevaluacion")
    REFERENCES "Autoevaluacion"("idAutoevaluacion")
;


-- 
-- TABLE: "Pregunta" 
--

ALTER TABLE "Pregunta" ADD CONSTRAINT "RefPool1541" 
    FOREIGN KEY ("idPool")
    REFERENCES "Pool"("idPool")
;


-- 
-- TABLE: "Programa" 
--

ALTER TABLE "Programa" ADD CONSTRAINT "RefCurso1211" 
    FOREIGN KEY ("idCurso")
    REFERENCES "Curso"("idCurso")
;


-- 
-- TABLE: "Progreso" 
--

ALTER TABLE "Progreso" ADD CONSTRAINT "RefUnidad1591" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;

ALTER TABLE "Progreso" ADD CONSTRAINT "RefInscripcion1601" 
    FOREIGN KEY ("idInscripcion")
    REFERENCES "Inscripcion"("idInscripcion")
;


-- 
-- TABLE: "Reporte" 
--

ALTER TABLE "Reporte" ADD CONSTRAINT "RefAdministrador1461" 
    FOREIGN KEY ("idAdministrador")
    REFERENCES "Administrador"("idAdministrador")
;

ALTER TABLE "Reporte" ADD CONSTRAINT "RefTipoReporte1471" 
    FOREIGN KEY ("idTipoReporte")
    REFERENCES "TipoReporte"("idTipoReporte")
;

ALTER TABLE "Reporte" ADD CONSTRAINT "RefCurso1911" 
    FOREIGN KEY ("idCurso")
    REFERENCES "Curso"("idCurso")
;


-- 
-- TABLE: "RespuestaForo" 
--

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefConsultaForo1611" 
    FOREIGN KEY ("idConsulta")
    REFERENCES "ConsultaForo"("idConsulta")
;

ALTER TABLE "RespuestaForo" ADD CONSTRAINT "RefDocente1261" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;


-- 
-- TABLE: "RespuestaIntento" 
--

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefIntentoAutoevaluacion1531" 
    FOREIGN KEY ("idIntentoAutoevaluacion")
    REFERENCES "IntentoAutoevaluacion"("idIntentoAutoevaluacion")
;

ALTER TABLE "RespuestaIntento" ADD CONSTRAINT "RefOpcionRespuesta1571" 
    FOREIGN KEY ("idOpcionRespuesta")
    REFERENCES "OpcionRespuesta"("idOpcionRespuesta")
;


-- 
-- TABLE: "Sesion" 
--

ALTER TABLE "Sesion" ADD CONSTRAINT "RefUsuario1351" 
    FOREIGN KEY ("idUsuario")
    REFERENCES "Usuario"("idUsuario")
;


-- 
-- TABLE: "TerminoGlosario" 
--

ALTER TABLE "TerminoGlosario" ADD CONSTRAINT "RefUnidad1341" 
    FOREIGN KEY ("idUnidad")
    REFERENCES "Unidad"("idUnidad")
;


-- 
-- TABLE: "TituloDocente" 
--

ALTER TABLE "TituloDocente" ADD CONSTRAINT "RefDocente1241" 
    FOREIGN KEY ("idDocente")
    REFERENCES "Docente"("idDocente")
;


-- 
-- TABLE: "Usuario" 
--

ALTER TABLE "Usuario" ADD CONSTRAINT "RefRol1891" 
    FOREIGN KEY ("idRol")
    REFERENCES "Rol"("idRol")
;