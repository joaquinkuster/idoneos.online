const fs = require('fs');
const path = require('path');

const templatesRoot = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

const modules = [
  'cursos',
  'academico',
  'foro',
  'inscripciones',
  'evaluaciones',
  'ia_vivo',
  'seguridad',
  'auditoria',
  'reportes',
  'configuracion'
];

let totalFiles = 0;
let mockHrefsCount = 0;
let filesWithMockHrefs = [];

modules.forEach(mod => {
  const dir = path.join(templatesRoot, mod);
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));
  files.forEach(file => {
    totalFiles++;
    const content = fs.readFileSync(path.join(dir, file), 'utf8');
    const matches = content.match(/href="#CU-[0-9a-zA-Z_]+"/g);
    if (matches && matches.length > 0) {
      mockHrefsCount += matches.length;
      filesWithMockHrefs.push({ file: `${mod}/${file}`, count: matches.length, matches });
    }
  });
});

console.log(`📊 AUDITORÍA DE ENLACES MOCKEADOS:`);
console.log(`Total archivos analizados: ${totalFiles}`);
console.log(`Total enlaces href="#CU-XX" encontrados: ${mockHrefsCount}`);
console.log(`Archivos que aún contienen href mockeado: ${filesWithMockHrefs.length}`);
console.log('---------------------------------------------------------');
filesWithMockHrefs.slice(0, 15).forEach(f => {
  console.log(`- ${f.file}: ${f.count} enlaces mockeados`);
});
