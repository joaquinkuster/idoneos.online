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
let modifiedFiles = 0;

modules.forEach(mod => {
  const dir = path.join(templatesRoot, mod);
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));

  files.forEach(file => {
    totalFiles++;
    const fullPath = path.join(dir, file);
    let content = fs.readFileSync(fullPath, 'utf8');
    let originalContent = content;

    // 1. Reemplazar contenedores fijos
    content = content.replace(/<div class="cu-page-container"[^>]*>/g, '<div class="cu-page-container">');
    
    // 2. Limpiar estilos innecesarios en screen-frame o body
    content = content.replace(/style="max-width:\s*1400px;[^"]*"/g, '');
    content = content.replace(/style="max-width:\s*1200px;[^"]*"/g, '');

    // 3. Asegurar que las tarjetas de login, logout y formularios tengan anchos fluidos y responsive
    content = content.replace(/style="max-width:\s*480px;\s*margin:\s*60px auto;/g, 'style="max-width: 540px; width: 100%; margin: 40px auto;');
    content = content.replace(/style="max-width:\s*460px;\s*margin:\s*60px auto;/g, 'style="max-width: 540px; width: 100%; margin: 40px auto;');

    if (content !== originalContent) {
      fs.writeFileSync(fullPath, content, 'utf8');
      modifiedFiles++;
    }
  });
});

console.log(`✅ ¡Limpieza de contenedores completada!`);
console.log(`Total archivos inspeccionados: ${totalFiles}`);
console.log(`Archivos optimizados: ${modifiedFiles}`);
