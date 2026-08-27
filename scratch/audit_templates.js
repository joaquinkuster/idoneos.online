const fs = require('fs');
const path = require('path');

// 1. Leer Casos de Uso Reales
const mdPath = path.join(__dirname, '..', 'docs', 'diseño', 'Casos de Uso Reales.md');
const mdContent = fs.readFileSync(mdPath, 'utf8');

const cuBlocks = mdContent.split(/(?=### CU-)/g);
const cus = [];

for (let i = 1; i < cuBlocks.length; i++) {
  const block = cuBlocks[i];
  const titleMatch = block.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) continue;
  
  const id = titleMatch[1].trim();
  const name = titleMatch[2].trim();
  
  const moduleMatch = block.match(/\*\*Módulo\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Módulo\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const actorsMatch = block.match(/\*\*Actor\(es\)\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Actor\(es\)\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  
  cus.push({
    id,
    name,
    module: moduleMatch ? moduleMatch[1].trim() : 'Módulo General',
    actors: actorsMatch ? actorsMatch[1].trim() : 'Usuario'
  });
}

console.log(`Total CUs cargados: ${cus.length}`);

// Clasificación por carpetas de destino
const routes = {
  'MOD-F-01': 'cursos',
  'MOD-F-02': 'academico',
  'MOD-F-03': 'materiales',
  'MOD-F-04': 'inscripciones',
  'MOD-F-05': 'evaluaciones',
  'MOD-F-06': 'ia_vivo',
  'MOD-NF-01': 'seguridad',
  'MOD-NF-02': 'auditoria',
  'MOD-NF-03': 'reportes',
  'MOD-NF-04': 'configuracion'
};

const moduleCounts = {};
cus.forEach(c => {
  const modCode = c.module.split(':')[0].trim();
  moduleCounts[modCode] = (moduleCounts[modCode] || 0) + 1;
});

console.log('Distribución por Módulos:', moduleCounts);
