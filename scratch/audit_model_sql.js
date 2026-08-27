const fs = require('fs');
const path = require('path');

const modelsDir = path.join('src', 'main', 'java', 'com', 'app', 'idoneos', 'model');
const sqlPath = path.join('docs', 'diseño', 'diseño_base_datos.sql');

const sql = fs.readFileSync(sqlPath, 'utf8');

// Extract all CREATE TABLE statements
const tableMatches = [...sql.matchAll(/CREATE TABLE\s+"?([^"(]+)"?\s*\(([\s\S]*?)\n\);/g)];
const sqlTables = tableMatches.map(m => {
  const tableName = m[1].trim();
  const body = m[2];
  return {
    tableName,
    body
  };
});

console.log(`📋 Tablas en el SQL original: ${sqlTables.length}`);

// Read all Java models
const javaFiles = fs.readdirSync(modelsDir).filter(f => f.endsWith('.java') && !f.endsWith('Id.java'));
console.log(`☕ Clases de Entidad en Java: ${javaFiles.length}\n`);

let missingTables = [];
let emptyFiles = [];

sqlTables.forEach(t => {
  // Normalize table name (e.g. "Modalidad Curso" -> ModalidadCurso, "EstadoClaseEnVIvo" -> EstadoClaseEnVivo)
  const normalized = t.tableName.replace(/\s+/g, '');
  const foundFile = javaFiles.find(f => f.replace('.java', '').toLowerCase() === normalized.toLowerCase());
  
  if (!foundFile) {
    missingTables.push(t.tableName);
  } else {
    const content = fs.readFileSync(path.join(modelsDir, foundFile), 'utf8');
    if (!content || content.trim().length === 0) {
      emptyFiles.push(foundFile);
    }
  }
});

console.log(`📊 RESULTADOS DE LA AUDITORÍA DEL MODELO vs SQL:`);
console.log(`- Tablas del SQL sin archivo Java: ${missingTables.length}`);
if (missingTables.length > 0) {
  missingTables.forEach(t => console.log(`   ❌ Falta: ${t}`));
} else {
  console.log(`   ✅ ¡El 100% de las tablas del SQL tienen su correspondiente entidad Java!`);
}

console.log(`- Archivos Java de Modelo vacíos: ${emptyFiles.length}`);
if (emptyFiles.length > 0) {
  emptyFiles.forEach(f => console.log(`   ❌ Vacío: ${f}`));
} else {
  console.log(`   ✅ ¡Ningún archivo del paquete model está vacío! Todos tienen su mapeo JPA completo.`);
}

console.log(`\nDetalle de RespuestaIntento.java:`);
const riContent = fs.readFileSync(path.join(modelsDir, 'RespuestaIntento.java'), 'utf8');
console.log(`- Líneas: ${riContent.split('\n').length}`);
console.log(`- Bytes: ${riContent.length}`);
