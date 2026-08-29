const fs = require('fs');
const path = require('path');

const sqlPath = path.join(__dirname, 'docs', 'diseño', 'diseño_base_datos.sql');
const modelDir = path.join(__dirname, 'src', 'main', 'java', 'com', 'app', 'idoneos', 'model');

const sqlContent = fs.readFileSync(sqlPath, 'utf8');

// Mapa de correspondencia SQL Table -> Java Model File & Class
const sqlToJavaMap = {
    "Administrador": { file: "Administrador.java", class: "Administrador" },
    "Alumno": { file: "Alumno.java", class: "Alumno" },
    "Auditoria": { file: "Auditoria.java", class: "Auditoria" },
    "Autoevaluacion": { file: "Autoevaluacion.java", class: "Autoevaluacion" },
    "Ayudante": { file: "Ayudante.java", class: "Ayudante" },
    "Categoria": { file: "Categoria.java", class: "Categoria" },
    "ClaseClon": { file: "ClaseClonIA.java", class: "ClaseClonIA" },
    "ClaseEnVivo": { file: "ClaseEnVivo.java", class: "ClaseEnVivo" },
    "Cohorte": { file: "Cohorte.java", class: "Cohorte" },
    "Configuracion": { file: "Configuracion.java", class: "Configuracion" },
    "ConsultaForo": { file: "ConsultaForo.java", class: "ConsultaForo" },
    "Cronograma": { file: "Cronograma.java", class: "Cronograma" },
    "Curso": { file: "Curso.java", class: "Curso" },
    "Descuento": { file: "Descuento.java", class: "Descuento" },
    "DetalleAuditoria": { file: "DetalleAuditoria.java", class: "DetalleAuditoria" },
    "Docente": { file: "Docente.java", class: "Docente" },
    "EstadoClaseClon": { file: "EstadoClaseClonIA.java", class: "EstadoClaseClonIA" },
    "EstadoClaseEnVIvo": { file: "EstadoClaseEnVivo.java", class: "EstadoClaseEnVivo" },
    "EstadoPago": { file: "EstadoPago.java", class: "EstadoPago" },
    "Inscripcion": { file: "Inscripcion.java", class: "Inscripcion" },
    "IntentoAutoevaluacion": { file: "IntentoAutoevaluacion.java", class: "IntentoAutoevaluacion" },
    "Material": { file: "Material.java", class: "Material" },
    "MetodoPago": { file: "MetodoPago.java", class: "MetodoPago" },
    "Modalidad": { file: "Modalidad.java", class: "Modalidad" },
    "Modalidad Curso": { file: "ModalidadCurso.java", class: "ModalidadCurso" },
    "Nivel": { file: "Nivel.java", class: "Nivel" },
    "OpcionRespuesta": { file: "OpcionRespuesta.java", class: "OpcionRespuesta" },
    "Pago": { file: "Pago.java", class: "Pago" },
    "Pool": { file: "Pool.java", class: "Pool" },
    "Pool Autoevaluacion": { file: "PoolAutoevaluacion.java", class: "PoolAutoevaluacion" },
    "Pregunta": { file: "Pregunta.java", class: "Pregunta" },
    "Programa": { file: "Programa.java", class: "Programa" },
    "Progreso": { file: "Progreso.java", class: "Progreso" },
    "Reporte": { file: "Reporte.java", class: "Reporte" },
    "RespuestaForo": { file: "RespuestaForo.java", class: "RespuestaForo" },
    "RespuestaIntento": { file: "RespuestaIntento.java", class: "RespuestaIntento" },
    "Rol": { file: "Rol.java", class: "Rol" },
    "Sesion": { file: "Sesion.java", class: "Sesion" },
    "TerminoGlosario": { file: "TerminoGlosario.java", class: "TerminoGlosario" },
    "TipoAccionAuditoria": { file: "TipoAccionAuditoria.java", class: "TipoAccionAuditoria" },
    "TipoMaterial": { file: "TipoMaterial.java", class: "TipoMaterial" },
    "TipoReporte": { file: "TipoReporte.java", class: "TipoReporte" },
    "TituloDocente": { file: "TituloDocente.java", class: "TituloDocente" },
    "Unidad": { file: "Unidad.java", class: "Unidad" },
    "Usuario": { file: "Usuario.java", class: "Usuario" }
};

// Extraer Foreign Keys del SQL: ALTER TABLE "Tabla" ADD CONSTRAINT "FK" FOREIGN KEY ("col") REFERENCES "RefTabla"("refCol")
const fkRegex = /ALTER\s+TABLE\s+"?([A-Za-z0-9_ ]+)"?\s+ADD\s+CONSTRAINT\s+"?([A-Za-z0-9_]+)"?\s+FOREIGN\s+KEY\s*\("?([A-Za-z0-9_]+)"?\)\s+REFERENCES\s+"?([A-Za-z0-9_ ]+)"?\s*\("?([A-Za-z0-9_]+)"?\)\s*;/gi;

const relations = [];
let match;
while ((match = fkRegex.exec(sqlContent)) !== null) {
    relations.push({
        sourceTable: match[1].trim(),
        constraintName: match[2].trim(),
        sourceCol: match[3].trim(),
        targetTable: match[4].trim(),
        targetCol: match[5].trim()
    });
}

console.log(`\n========================================================================================`);
console.log(` AUDITORÍA DE RELACIONES (FOREIGN KEYS) SQL vs MODELO DE ENTIDADES JPA (Spring Boot)`);
console.log(`========================================================================================\n`);
console.log(`Total de restricciones Foreign Key en SQL: ${relations.length}\n`);

let totalFkAuditadas = 0;
let totalOk = 0;
let discrepancias = [];

for (const rel of relations) {
    totalFkAuditadas++;
    const sourceInfo = sqlToJavaMap[rel.sourceTable];
    const targetInfo = sqlToJavaMap[rel.targetTable];

    if (!sourceInfo) {
        discrepancias.push(`❌ [TABLA ORIGEN NO MAP] Tabla SQL "${rel.sourceTable}" sin entidad JPA configurada.`);
        continue;
    }
    if (!targetInfo) {
        discrepancias.push(`❌ [TABLA DESTINO NO MAP] Tabla destino "${rel.targetTable}" sin entidad JPA configurada.`);
        continue;
    }

    const sourceFilePath = path.join(modelDir, sourceInfo.file);
    if (!fs.existsSync(sourceFilePath)) {
        discrepancias.push(`❌ [ARCHIVO FALTANTE] ${sourceInfo.file} no existe en ${modelDir}.`);
        continue;
    }

    const sourceCode = fs.readFileSync(sourceFilePath, 'utf8');
    const targetClass = targetInfo.class;
    
    // Variantes del nombre de la columna en JoinColumn: snake_case, camelCase, original
    const colSnake = rel.sourceCol.replace(/([A-Z])/g, "_$1").toLowerCase().replace(/^_/, '');
    const colLower = rel.sourceCol.toLowerCase();

    // Verificamos si en la entidad origen existe un @ManyToOne o @OneToOne hacia la entidad destino
    // o un @JoinColumn que apunte a la columna FK
    const targetClassRegex = new RegExp(`private\\s+${targetClass}\\s+([A-Za-z0-9_]+);`, 'i');
    const joinColRegex = new RegExp(`@JoinColumn\\s*\\([^)]*name\\s*=\\s*["'](${rel.sourceCol}|${colSnake}|${colLower}|id_${colSnake})["']`, 'i');
    
    const hasTargetEntityField = targetClassRegex.test(sourceCode);
    const hasJoinColumn = joinColRegex.test(sourceCode);

    if (hasTargetEntityField || hasJoinColumn) {
        console.log(`✅ [OK] FK [${rel.constraintName}] ${rel.sourceTable}."${rel.sourceCol}" -> ${rel.targetTable}."${rel.targetCol}" | Mapeado en ${sourceInfo.file} -> ${targetClass}`);
        totalOk++;
    } else {
        console.log(`⚠️ [FALLO] FK [${rel.constraintName}] ${rel.sourceTable}."${rel.sourceCol}" -> ${rel.targetTable}."${rel.targetCol}" | NO encontrado en ${sourceInfo.file}`);
        discrepancias.push(`FK ${rel.constraintName}: ${rel.sourceTable}.${rel.sourceCol} -> ${rel.targetTable} no mapeada en ${sourceInfo.file}`);
    }
}

console.log(`\n========================================================================================`);
console.log(` RESUMEN DE AUDITORÍA DE RELACIONES JPA`);
console.log(`========================================================================================`);
console.log(`Total de Foreign Keys analizadas: ${totalFkAuditadas}`);
console.log(`Relaciones JPA perfectamente sincronizadas: ${totalOk}`);
console.log(`Discrepancias encontradas: ${discrepancias.length}`);
console.log(`Estado: ${discrepancias.length === 0 ? 'PERFECTO (100% Coincidencia en Relaciones)' : 'ATENCIÓN REQUERIDA'}\n`);

if (discrepancias.length > 0) {
    console.log(`Detalle de discrepancias:`);
    discrepancias.forEach(d => console.log(` - ${d}`));
}
