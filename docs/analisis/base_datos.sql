-- PostgreSQL DDL - Idoneos
-- ==========================================================

-- ==========================================
-- 1. TABLAS CATALOGO / INDEPENDIENTES
-- ==========================================

CREATE TABLE "Rol" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50)
);

CREATE TABLE "TipoAccionAuditoria" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "TipoReporte" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "TipoMaterial" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "Modalidad" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "Categoria" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "descripcion"         VARCHAR(150),
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "EstadoClaseClonIA" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

-- Nombre de tabla EXACTO al @Table(name = "EstadoClaseEnVIvo") de la entidad Java.
CREATE TABLE "EstadoClaseEnVIvo" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "EstadoPago" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "MetodoPago" (
    "id"     SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL
);

CREATE TABLE "Descuento" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "cursos_requeridos"   INT NOT NULL DEFAULT 0,
    "porcentaje"          REAL NOT NULL,
    "vigencia_desde"      TIMESTAMP NOT NULL,
    "vigencia_hasta"      TIMESTAMP NOT NULL,
    "cantidad_limite"     INT NOT NULL,
    "cantidad_usada"      INT NOT NULL DEFAULT 0,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE
);

-- ==========================================
-- 2. USUARIOS Y ROLES (herencia por tabla, PK compartida)
-- ==========================================

CREATE TABLE "Usuario" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "apellido"            VARCHAR(50) NOT NULL,
    "dni"                 VARCHAR(8) NOT NULL,
    "email"               VARCHAR(150) NOT NULL UNIQUE,
    "contrasena"          VARCHAR(255),
    "imagen"              VARCHAR(150),
    "telefono"            VARCHAR(20),
    "token_recuperacion"  VARCHAR(255),
    "expiracion_token"    TIMESTAMP,
    "google_id"           VARCHAR(255),
    "email_validado"      BOOLEAN NOT NULL DEFAULT FALSE,
    "fecha_registro"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE "Usuario Rol" (
    "id"         SERIAL PRIMARY KEY,
    "usuario_id" INT NOT NULL REFERENCES "Usuario"("id"),
    "rol_id"     INT NOT NULL REFERENCES "Rol"("id")
);

-- Alumno, Administrador y Docente: relacion 1 a 0..1 con Usuario mediante
-- clave primaria COMPARTIDA (equivalente a @MapsId + @JoinColumn(name="id")).
CREATE TABLE "Administrador" (
    "id" INT PRIMARY KEY REFERENCES "Usuario"("id") ON DELETE CASCADE
);

CREATE TABLE "Alumno" (
    "id" INT PRIMARY KEY REFERENCES "Usuario"("id") ON DELETE CASCADE
);

CREATE TABLE "Docente" (
    "id"                INT PRIMARY KEY REFERENCES "Usuario"("id") ON DELETE CASCADE,
    "anios_experiencia" INT NOT NULL DEFAULT 0,
    "matricula_cnv"     VARCHAR(50),
    "biografia"         TEXT,
    "habilitado"        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE "TituloDocente" (
    "id"                SERIAL PRIMARY KEY,
    "titulo"            VARCHAR(100) NOT NULL,
    "matricula_colegio" VARCHAR(50),
    "docente_id"        INT NOT NULL REFERENCES "Docente"("id")
);

-- ==========================================
-- 3. AUDITORIA, SESION, CONFIGURACION, REPORTES
-- ==========================================

CREATE TABLE "Auditoria" (
    "id"                        SERIAL PRIMARY KEY,
    "entidad_afectada"          VARCHAR(50) NOT NULL,
    "id_afectado"               INT NOT NULL,
    "valor_anterior"            TEXT,
    "valor_nuevo"               TEXT,
    "ip_usuario"                VARCHAR(45) NOT NULL,
    "fecha_hora"                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "usuario_id"                INT NOT NULL REFERENCES "Usuario"("id"),
    "tipo_accion_auditoria_id"  INT NOT NULL REFERENCES "TipoAccionAuditoria"("id")
);

CREATE TABLE "Sesion" (
    "id"           SERIAL PRIMARY KEY,
    "token"        VARCHAR(255) NOT NULL,
    "fecha_inicio" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "fecha_fin"    TIMESTAMP NOT NULL,
    "ip"           VARCHAR(45) NOT NULL,
    "dispositivo"  VARCHAR(255) NOT NULL,
    "usuario_id"   INT NOT NULL REFERENCES "Usuario"("id")
);

CREATE TABLE "Configuracion" (
    "id"               SERIAL PRIMARY KEY,
    "clave"            VARCHAR(100) NOT NULL UNIQUE,
    "valor"            TEXT NOT NULL,
    "administrador_id" INT REFERENCES "Administrador"("id")
);

CREATE TABLE "Reporte" (
    "id"                SERIAL PRIMARY KEY,
    "fecha_generacion"  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "tipo_reporte_id"   INT NOT NULL REFERENCES "TipoReporte"("id"),
    "administrador_id"  INT NOT NULL REFERENCES "Administrador"("id")
);

-- ==========================================
-- 4. ACADEMICO (CURSO, PROGRAMA, UNIDAD, DICTADO)
-- ==========================================

CREATE TABLE "Curso" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "descripcion"         VARCHAR(150),
    "precio"              REAL NOT NULL,
    "imagen"              VARCHAR(150),
    "publicado"           BOOLEAN NOT NULL DEFAULT FALSE,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "categoria_id"        INT NOT NULL REFERENCES "Categoria"("id")
);

-- Asociativa M:N Modalidad <-> Curso
CREATE TABLE "Modalidad Curso" (
    "id"           SERIAL PRIMARY KEY,
    "modalidad_id" INT NOT NULL REFERENCES "Modalidad"("id"),
    "curso_id"     INT NOT NULL REFERENCES "Curso"("id")
);

CREATE TABLE "Programa" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "descripcion"         VARCHAR(150),
    "meses_acceso"        INT NOT NULL,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "curso_id"            INT NOT NULL REFERENCES "Curso"("id")
);

CREATE TABLE "Unidad" (
    "id"                  SERIAL PRIMARY KEY,
    "titulo"              VARCHAR(50) NOT NULL,
    "descripcion"         VARCHAR(150),
    "numero_orden"        INT NOT NULL,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "programa_id"         INT NOT NULL REFERENCES "Programa"("id")
);

CREATE TABLE "Dictado" (
    "id"                  SERIAL PRIMARY KEY,
    "fecha_inicio"        TIMESTAMP NOT NULL,
    "fecha_fin"           TIMESTAMP NOT NULL,
    "cupo_maximo"         INT,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "programa_id"         INT NOT NULL REFERENCES "Programa"("id")
);

-- Asociativa M:N Dictado <-> Docente (con flag de supervisor)
CREATE TABLE "Dictado Docente" (
    "id"            SERIAL PRIMARY KEY,
    "dictado_id"    INT NOT NULL REFERENCES "Dictado"("id"),
    "docente_id"    INT NOT NULL REFERENCES "Docente"("id"),
    "es_supervisor" BOOLEAN NOT NULL DEFAULT FALSE
);

-- ==========================================
-- 5. MATERIALES Y CLASES
-- ==========================================

CREATE TABLE "Material" (
    "id"                  SERIAL PRIMARY KEY,
    "titulo"              VARCHAR(50) NOT NULL,
    "ruta_archivo"        VARCHAR(150),
    "contenido"           VARCHAR(500),
    "duracion"            INT,
    "autor"               VARCHAR(50),
    "generado_por_ia"     BOOLEAN NOT NULL DEFAULT FALSE,
    "publicado"           BOOLEAN NOT NULL DEFAULT TRUE,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "unidad_id"           INT NOT NULL REFERENCES "Unidad"("id"),
    "tipo_material_id"    INT NOT NULL REFERENCES "TipoMaterial"("id"),
    "docente_id"          INT REFERENCES "Docente"("id")
);

CREATE TABLE "ClaseClonIA" (
    "id"                      SERIAL PRIMARY KEY,
    "titulo"                  VARCHAR(50) NOT NULL,
    "guion"                   TEXT NOT NULL,
    "fecha_generacion"        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "baja"                    BOOLEAN NOT NULL DEFAULT FALSE,
    "estado_clase_clon_ia_id" INT REFERENCES "EstadoClaseClonIA"("id"),
    "material_id"             INT REFERENCES "Material"("id"),
    "unidad_id"               INT NOT NULL REFERENCES "Unidad"("id"),
    "docente_id"              INT NOT NULL REFERENCES "Docente"("id")
);

CREATE TABLE "ClaseEnVivo" (
    "id"                      SERIAL PRIMARY KEY,
    "titulo"                  VARCHAR(50) NOT NULL,
    "fecha_hora"              TIMESTAMP NOT NULL,
    "url_rtmp"                VARCHAR(255) NOT NULL,
    "clave_stream"            VARCHAR(100) NOT NULL,
    "baja"                    BOOLEAN NOT NULL DEFAULT FALSE,
    "estado_clase_en_vivo_id" INT REFERENCES "EstadoClaseEnVIvo"("id"),
    "material_id"             INT REFERENCES "Material"("id"),
    "unidad_id"               INT NOT NULL REFERENCES "Unidad"("id"),
    "docente_id"              INT NOT NULL REFERENCES "Docente"("id")
);

-- ==========================================
-- 6. EVALUACIONES, POOL Y FORO
-- ==========================================

CREATE TABLE "Pool" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "unidad_id"           INT NOT NULL REFERENCES "Unidad"("id")
);

CREATE TABLE "Pregunta" (
    "id"                  SERIAL PRIMARY KEY,
    "texto"               VARCHAR(150) NOT NULL,
    "es_opcion_multiple"  BOOLEAN NOT NULL DEFAULT TRUE,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "pool_id"             INT NOT NULL REFERENCES "Pool"("id")
);

CREATE TABLE "OpcionRespuesta" (
    "id"           SERIAL PRIMARY KEY,
    "texto"        VARCHAR(150) NOT NULL,
    "es_correcta"  BOOLEAN NOT NULL DEFAULT FALSE,
    "baja"         BOOLEAN NOT NULL DEFAULT FALSE,
    "pregunta_id"  INT NOT NULL REFERENCES "Pregunta"("id")
);

CREATE TABLE "Autoevaluacion" (
    "id"                  SERIAL PRIMARY KEY,
    "nombre"              VARCHAR(50) NOT NULL,
    "tiempo_limite"       INT NOT NULL,
    "intentos_permitidos" INT,
    "fecha_apertura"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "fecha_cierre"        TIMESTAMP,
    "fecha_creacion"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ultima_modificacion" TIMESTAMP,
    "baja"                BOOLEAN NOT NULL DEFAULT FALSE,
    "unidad_id"           INT NOT NULL REFERENCES "Unidad"("id")
);

-- Asociativa M:N Pool <-> Autoevaluacion
CREATE TABLE "Pool Autoevaluacion" (
    "id"                SERIAL PRIMARY KEY,
    "pool_id"           INT NOT NULL REFERENCES "Pool"("id"),
    "autoevaluacion_id" INT NOT NULL REFERENCES "Autoevaluacion"("id")
);

CREATE TABLE "ConsultaForo" (
    "id"         SERIAL PRIMARY KEY,
    "texto"      VARCHAR(500) NOT NULL,
    "fecha"      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "baja"       BOOLEAN NOT NULL DEFAULT FALSE,
    "unidad_id"  INT NOT NULL REFERENCES "Unidad"("id"),
    "alumno_id"  INT NOT NULL REFERENCES "Alumno"("id")
);

CREATE TABLE "RespuestaForo" (
    "id"               SERIAL PRIMARY KEY,
    "texto"            VARCHAR(500) NOT NULL,
    "fecha"            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "baja"             BOOLEAN NOT NULL DEFAULT FALSE,
    "consulta_foro_id" INT NOT NULL REFERENCES "ConsultaForo"("id"),
    "docente_id"       INT NOT NULL REFERENCES "Docente"("id")
);

CREATE TABLE "TerminoGlosario" (
    "id"         SERIAL PRIMARY KEY,
    "termino"    VARCHAR(50) NOT NULL,
    "definicion" VARCHAR(150) NOT NULL,
    "baja"       BOOLEAN NOT NULL DEFAULT FALSE,
    "unidad_id"  INT NOT NULL REFERENCES "Unidad"("id")
);

-- ==========================================
-- 7. INSCRIPCIONES, PAGOS Y PROGRESO
-- ==========================================

CREATE TABLE "Inscripcion" (
    "id"                        SERIAL PRIMARY KEY,
    "fecha"                     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "fecha_vencimiento_acceso"  TIMESTAMP NOT NULL,
    "observaciones"             VARCHAR(500),
    "numero_certificado"        VARCHAR(100),
    "fecha_emision_certificado" TIMESTAMP,
    "certificado_enviado"       BOOLEAN NOT NULL DEFAULT FALSE,
    "baja"                      BOOLEAN NOT NULL DEFAULT FALSE,
    "alumno_id"                 INT NOT NULL REFERENCES "Alumno"("id"),
    "dictado_id"                INT NOT NULL REFERENCES "Dictado"("id"),
    "descuento_id"              INT REFERENCES "Descuento"("id")
);

CREATE TABLE "Pago" (
    "id"                        SERIAL PRIMARY KEY,
    "monto"                     REAL NOT NULL,
    "fecha"                     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "payment_request_id"        VARCHAR(50),
    "external_intention_id"     VARCHAR(50) NOT NULL DEFAULT '',
    "reference_code"            VARCHAR(20),
    "ultimos_digitos_tarjeta"   VARCHAR(4),
    "detalle_estado"            VARCHAR(100),
    "fecha_aprobacion"          TIMESTAMP,
    "nombre_pagador"            VARCHAR(50),
    "dni_pagador"                VARCHAR(8),
    "numero_comprobante"        VARCHAR(100),
    "fecha_emision_comprobante" TIMESTAMP,
    "comprobante_enviado"       BOOLEAN NOT NULL DEFAULT FALSE,
    "inscripcion_id"            INT NOT NULL REFERENCES "Inscripcion"("id"),
    "estado_pago_id"            INT NOT NULL REFERENCES "EstadoPago"("id"),
    "metodo_pago_id"            INT REFERENCES "MetodoPago"("id")
);

CREATE TABLE "Progreso" (
    "id"                SERIAL PRIMARY KEY,
    "completada"        BOOLEAN NOT NULL DEFAULT FALSE,
    "fecha_completada"  TIMESTAMP,
    "unidad_id"         INT NOT NULL REFERENCES "Unidad"("id"),
    "inscripcion_id"    INT NOT NULL REFERENCES "Inscripcion"("id")
);

CREATE TABLE "IntentoAutoevaluacion" (
    "id"                SERIAL PRIMARY KEY,
    "fecha"             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "nota"              REAL NOT NULL,
    "autoevaluacion_id" INT NOT NULL REFERENCES "Autoevaluacion"("id")
);

CREATE TABLE "RespuestaIntento" (
    "id"                         SERIAL PRIMARY KEY,
    "intento_autoevaluacion_id"  INT NOT NULL REFERENCES "IntentoAutoevaluacion"("id"),
    "opcion_respuesta_id"        INT NOT NULL REFERENCES "OpcionRespuesta"("id")
);

-- ==========================================================
-- Fin del script. Total: 43 tablas.
-- ==========================================================