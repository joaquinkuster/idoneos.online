const fs = require('fs');
const path = require('path');

// 1. Leer y parsear DSS.md
const dssFilePath = path.join(__dirname, '..', 'docs', 'analisis', 'DSS.md');
const dssContent = fs.readFileSync(dssFilePath, 'utf8');

const cuDssMap = {};

// Regex para parsear cada sección de CU en DSS.md
// Ejemplo:
// ### CU-03: Registrar curso
// * **Actor:** Administrador
// * **Sistema:** `:IdoneosOnline`
// * **Flujo de interacción:** ...
const cuSections = dssContent.split(/###\s+(CU-[0-9a-zA-Z_]+):\s*([^\r\n]+)/);

for (let i = 1; i < cuSections.length; i += 3) {
  const cuId = cuSections[i].trim();
  const cuName = cuSections[i + 1].trim();
  const body = cuSections[i + 2] || '';

  // Extraer actores
  let actores = 'Administrador, Docente, Alumno';
  const actorMatch = body.match(/\*\s*\*\*Actor(?:es)?:\*\*\s*([^\r\n]+)/);
  if (actorMatch) {
    actores = actorMatch[1].trim();
  }

  // Extraer flujo / secuencia DSS
  const calls = [];
  const codeMatches = body.matchAll(/`([^`\(\)]+\([^`\)]*\))`|`([a-zA-Z0-9_]+\(\))`|([a-zA-Z0-9_]+\([^\)]*\))/g);
  for (const m of codeMatches) {
    const fn = (m[1] || m[2] || m[3] || '').trim();
    if (fn && !calls.includes(fn) && !fn.startsWith('CU-') && !fn.includes(':')) {
      calls.push(fn);
    }
  }

  let dssSequence = calls.join(' ➔ ');
  if (!dssSequence) {
    dssSequence = `${cuName.toLowerCase().replace(/\s+/g, '')}()`;
  }

  cuDssMap[cuId] = {
    id: cuId,
    nombre: cuName,
    actores: actores,
    dss: dssSequence
  };
}

console.log(`✅ Parseados ${Object.keys(cuDssMap).length} Casos de Uso desde DSS.md`);

// 2. Procesar las 100 plantillas en templates/pages/
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

let filesProcessed = 0;

modules.forEach(mod => {
  const dir = path.join(templatesRoot, mod);
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));

  files.forEach(file => {
    const fullPath = path.join(dir, file);
    let content = fs.readFileSync(fullPath, 'utf8');

    // Identificar CU id del archivo (ej: cu-03-registrar-curso.html -> CU-03, cu-26b -> CU-26b)
    const fileCuMatch = file.match(/^cu-([0-9a-zA-Z]+)-/i);
    let cuKey = fileCuMatch ? `CU-${fileCuMatch[1].toUpperCase()}` : null;
    if (file.includes('cu-26b')) cuKey = 'CU-26b';

    let dssInfo = cuDssMap[cuKey];
    if (!dssInfo) {
      // Fallback
      dssInfo = {
        id: cuKey || 'CU',
        nombre: file.replace(/^cu-[0-9a-zA-Z]+-/, '').replace('.html', '').replace(/-/g, ' '),
        actores: 'Administrador / Docente / Alumno',
        dss: 'operacionDelSistema()'
      };
    }

    // 1. Eliminar visualmente la barra meta-strip si existe
    content = content.replace(/<!-- Barra de Metadatos del Caso de Uso -->[\s\S]*?<!-- Pantalla \/ Screen Frame Oficial -->/g, '<!-- Pantalla / Screen Frame Oficial -->');
    content = content.replace(/<div class="meta-strip[\s\S]*?<\/div>\s*<\/div>\s*<!-- Pantalla/g, '<!-- Pantalla');

    // 2. Crear script de console logger enriquecido
    const consoleLoggerScript = `
    <!-- DSS & Case of Use Console Logger -->
    <script>
        (function() {
            const cuId = '${dssInfo.id}';
            const cuNombre = '${dssInfo.nombre.replace(/'/g, "\\'")}';
            const cuActores = '${dssInfo.actores.replace(/'/g, "\\'")}';
            const cuDss = '${dssInfo.dss.replace(/'/g, "\\'")}';

            console.log(
                '%c[IDÓNEOS ONLINE] %c ' + cuId + ' %c ' + cuNombre + ' ',
                'color: #C9982F; font-weight: 800; font-size: 13px;',
                'background: #081426; color: #E4BE6C; padding: 3px 8px; border-radius: 4px; font-weight: 700; font-size: 12px; border: 1px solid #C9982F;',
                'color: #081426; font-weight: 800; font-size: 13px;'
            );
            console.log(
                '%c👤 Actor(es): %c' + cuActores,
                'color: #64748B; font-weight: 700; font-size: 11px;',
                'color: #0F172A; font-weight: 600; font-size: 11px;'
            );
            console.log(
                '%c⚡ DSS: %c' + cuDss,
                'color: #64748B; font-weight: 700; font-size: 11px;',
                'color: #0284C7; font-family: monospace; font-size: 11px; font-weight: 600;'
            );
        })();
    </script>
</body>`;

    // Reemplazar o inyectar antes de </body>
    content = content.replace(/<!-- DSS & Case of Use Console Logger -->[\s\S]*?<\/body>/g, '</body>');
    content = content.replace('</body>', consoleLoggerScript);

    fs.writeFileSync(fullPath, content, 'utf8');
    filesProcessed++;
  });
});

console.log(`🎉 ¡ÉXITO! Se actualizaron ${filesProcessed} plantillas HTML.`);
console.log(`- Barra .meta-strip eliminada de pantalla.`);
console.log(`- Inyectado Logger de Consola con DSS interactivo y estilizado.`);
