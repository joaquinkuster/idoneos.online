const fs = require('fs');
const path = require('path');

const sqlPath = path.join(__dirname, 'docs', 'diseño', 'diseño_base_datos.sql');
const semillaPath = path.join(__dirname, 'src', 'main', 'java', 'com', 'app', 'idoneos', 'service', 'modulo_configuracion', 'SemillaService.java');

const sqlContent = fs.readFileSync(sqlPath, 'utf8');
const semillaContent = fs.readFileSync(semillaPath, 'utf8');

// Extraer restricciones de columnas en SQL (ej. varchar(50))
const columnLimits = {};
const tableRegex = /CREATE\s+TABLE\s+"?([A-Za-z0-9_ ]+)"?\s*\(([\s\S]*?)\)\s*;/gi;
let match;

while ((match = tableRegex.exec(sqlContent)) !== null) {
    const tableName = match[1].trim();
    const body = match[2];
    columnLimits[tableName] = {};
    
    const lines = body.split(/\r?\n/);
    for (let line of lines) {
        line = line.trim();
        if (line.startsWith('CONSTRAINT') || line.startsWith('--') || !line) continue;
        const colMatch = line.match(/^"?([A-Za-z0-9_]+)"?\s+([A-Za-z0-9_]+)(\(([0-9]+)\))?/i);
        if (colMatch) {
            const colName = colMatch[1];
            const colType = colMatch[2].toLowerCase();
            const colLen = colMatch[4] ? parseInt(colMatch[4], 10) : (colType === 'text' ? 100000 : null);
            columnLimits[tableName][colName.toLowerCase()] = { type: colType, maxLen: colLen };
        }
    }
}

console.log("=== ESQUEMA DE LÍMITES SQL CARGADO ===");

// Buscar instanciaciones en SemillaService y comprobar longitud de strings
const issues = [];
const lines = semillaContent.split('\n');

lines.forEach((line, lineNum) => {
    // Buscar todos los strings literales entre comillas
    const strMatches = line.match(/"([^"\\]|\\.)*"/g);
    if (!strMatches) return;

    for (const strLit of strMatches) {
        const val = strLit.slice(1, -1);
        if (line.includes('new Unidad') && val.length > 50 && line.indexOf(strLit) < line.indexOf(',')) {
            issues.push(`Línea ${lineNum + 1}: Unidad.titulo excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new Curso') && val.length > 50 && line.indexOf(strLit) < line.indexOf(',')) {
            issues.push(`Línea ${lineNum + 1}: Curso.nombre excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new Programa') && val.length > 50 && line.indexOf(strLit) < line.indexOf(',')) {
            issues.push(`Línea ${lineNum + 1}: Programa.nombre excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new Pool') && val.length > 50) {
            issues.push(`Línea ${lineNum + 1}: Pool.nombre excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new Autoevaluacion') && val.length > 50) {
            issues.push(`Línea ${lineNum + 1}: Autoevaluacion.nombre excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new TerminoGlosario') && val.length > 50) {
            issues.push(`Línea ${lineNum + 1}: TerminoGlosario.termino excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new ClaseEnVivo') && val.length > 50 && line.indexOf(strLit) < line.indexOf(',')) {
            issues.push(`Línea ${lineNum + 1}: ClaseEnVivo.titulo excede 50 chars (${val.length} chars): "${val}"`);
        }
        if (line.includes('new ClaseClonIA') && val.length > 50 && line.indexOf(strLit) < line.indexOf(',')) {
            issues.push(`Línea ${lineNum + 1}: ClaseClonIA.titulo excede 50 chars (${val.length} chars): "${val}"`);
        }
    }
});

console.log(`Discrepancias de longitud detectadas en SemillaService: ${issues.length}`);
issues.forEach(iss => console.log(" - " + iss));
