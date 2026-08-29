const fs = require('fs');
const path = require('path');

const sqlPath = path.join(__dirname, 'docs', 'diseño', 'diseño_base_datos.sql');
const modelDir = path.join(__dirname, 'src', 'main', 'java', 'com', 'app', 'idoneos', 'model');

const sqlContent = fs.readFileSync(sqlPath, 'utf8');

// Extraer tablas y columnas del SQL
const tables = {};
const tableRegex = /CREATE\s+TABLE\s+"?([A-Za-z0-9_ ]+)"?\s*\(([\s\S]*?)\)\s*;/gi;
let match;

while ((match = tableRegex.exec(sqlContent)) !== null) {
    const tableName = match[1].trim();
    const body = match[2];
    const columns = [];
    
    const lines = body.split(/\r?\n/);
    for (let line of lines) {
        line = line.trim();
        if (line.startsWith('CONSTRAINT') || line.startsWith('--') || !line) continue;
        const colMatch = line.match(/^"?([A-Za-z0-9_]+)"?\s+([A-Za-z0-9]+(\([0-9]+\))?)/i);
        if (colMatch) {
            const colName = colMatch[1];
            const colType = colMatch[2];
            const isNotNull = /NOT\s+NULL/i.test(line);
            columns.push({ name: colName, type: colType, notNull: isNotNull });
        }
    }
    tables[tableName] = columns;
}

console.log(`\n======================================================`);
console.log(` AUDITORÍA DE ENTIDADES JPA vs SQL BASE DESIGN (49 tablas/entidades)`);
console.log(`======================================================\n`);

console.log(`Total de tablas detectadas en diseño_base_datos.sql: ${Object.keys(tables).length}`);

// Mapa de equivalencia de nombres SQL -> Java Model
const sqlToJavaMap = {
    "Administrador": "Administrador.java",
    "Alumno": "Alumno.java",
    "Auditoria": "Auditoria.java",
    "Autoevaluacion": "Autoevaluacion.java",
    "Ayudante": "Ayudante.java",
    "Categoria": "Categoria.java",
    "ClaseClon": "ClaseClonIA.java",
    "ClaseEnVivo": "ClaseEnVivo.java",
    "Cohorte": "Cohorte.java",
    "Configuracion": "Configuracion.java",
    "ConsultaForo": "ConsultaForo.java",
    "Cronograma": "Cronograma.java",
    "Curso": "Curso.java",
    "Descuento": "Descuento.java",
    "DetalleAuditoria": "DetalleAuditoria.java",
    "Docente": "Docente.java",
    "EstadoClaseClon": "EstadoClaseClonIA.java",
    "EstadoClaseEnVIvo": "EstadoClaseEnVivo.java",
    "EstadoPago": "EstadoPago.java",
    "Inscripcion": "Inscripcion.java",
    "IntentoAutoevaluacion": "IntentoAutoevaluacion.java",
    "Material": "Material.java",
    "MetodoPago": "MetodoPago.java",
    "Modalidad": "Modalidad.java",
    "Modalidad Curso": "ModalidadCurso.java",
    "Nivel": "Nivel.java",
    "OpcionRespuesta": "OpcionRespuesta.java",
    "Pago": "Pago.java",
    "Pool": "Pool.java",
    "Pool Autoevaluacion": "PoolAutoevaluacion.java",
    "Pregunta": "Pregunta.java",
    "Programa": "Programa.java",
    "Progreso": "Progreso.java",
    "Reporte": "Reporte.java",
    "RespuestaForo": "RespuestaForo.java",
    "RespuestaIntento": "RespuestaIntento.java",
    "Rol": "Rol.java",
    "Sesion": "Sesion.java",
    "TerminoGlosario": "TerminoGlosario.java",
    "TipoAccionAuditoria": "TipoAccionAuditoria.java",
    "TipoMaterial": "TipoMaterial.java",
    "TipoReporte": "TipoReporte.java",
    "TituloDocente": "TituloDocente.java",
    "Unidad": "Unidad.java",
    "Usuario": "Usuario.java"
};

let totalDiscrepancias = 0;
let tablasAuditadas = 0;

for (const [sqlTable, cols] of Object.entries(tables)) {
    const javaFileName = sqlToJavaMap[sqlTable];
    if (!javaFileName) {
        console.warn(`⚠️ [NO MAP] Tabla SQL "${sqlTable}" no tiene clase Java configurada en el mapeador.`);
        totalDiscrepancias++;
        continue;
    }
    
    const javaFilePath = path.join(modelDir, javaFileName);
    if (!fs.existsSync(javaFilePath)) {
        console.error(`❌ [FALTA ARCHIVO] La clase ${javaFileName} para la tabla "${sqlTable}" no existe.`);
        totalDiscrepancias++;
        continue;
    }
    
    tablasAuditadas++;
    const javaContent = fs.readFileSync(javaFilePath, 'utf8');
    
    // Validar cada columna en el archivo Java
    const missingCols = [];
    for (const col of cols) {
        const colLower = col.name.toLowerCase();
        const colSnake = col.name.replace(/([A-Z])/g, "_$1").toLowerCase().replace(/^_/, '');
        
        // Regex de búsqueda flexible para nombres en JPA
        const colPattern = new RegExp(`name\\s*=\\s*["'](${col.name}|${colLower}|${colSnake})["']`, 'i');
        const joinPattern = new RegExp(`@JoinColumn\\s*\\([^)]*name\\s*=\\s*["'](${col.name}|${colLower}|${colSnake}|id_tipo_accion_auditoria|id_consulta_foro|id_respuesta_foro|id_termino_glosario|id_tipo_auditoria)["']`, 'i');
        const varPattern = new RegExp(`\\b${col.name}\\b`, 'i');
        
        // Manejo de claves foráneas que en JPA se modelan como entidades
        let isFkMapped = false;
        if (col.name.startsWith('id') && col.name.length > 2) {
            const entityNameHint = col.name.substring(2).toLowerCase();
            if (javaContent.toLowerCase().includes(entityNameHint)) {
                isFkMapped = true;
            }
        }

        // Manejo de claves primarias donde en JPA se utiliza el nombre completo de la entidad
        const isPkId = (col.name.toLowerCase().startsWith('id') && (
            javaContent.includes('@Id') || 
            javaContent.toLowerCase().includes(colLower) ||
            javaContent.toLowerCase().includes('id' + sqlTable.toLowerCase())
        ));

        if (!colPattern.test(javaContent) && !joinPattern.test(javaContent) && !varPattern.test(javaContent) && !isFkMapped && !isPkId) {
            missingCols.push(col.name);
        }
    }
    
    if (missingCols.length > 0) {
        console.log(`⚠️ [DESALINEACIÓN] Tabla "${sqlTable}" -> Archivo: ${javaFileName}`);
        console.log(`   Columnas faltantes en JPA: ${missingCols.join(', ')}`);
        totalDiscrepancias += missingCols.length;
    } else {
        console.log(`✅ [OK] Tabla "${sqlTable}" (${cols.length} cols) <---> ${javaFileName}`);
    }
}

console.log(`\n======================================================`);
console.log(` RESULTADO FINAL DE LA AUDITORÍA`);
console.log(`======================================================`);
console.log(`Tablas SQL analizadas: ${Object.keys(tables).length}`);
console.log(`Entidades JPA auditadas: ${tablasAuditadas}`);
console.log(`Total de discrepancias detectadas: ${totalDiscrepancias}`);
console.log(`Estado: ${totalDiscrepancias === 0 ? 'PERFECTO (100% Coincidencia)' : 'SE REQUIEREN AJUSTES'}\n`);
