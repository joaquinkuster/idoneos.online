const fs = require('fs');
const path = require('path');

const templatesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates');

function getHtmlFiles(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    if (stat && stat.isDirectory()) {
      results = results.concat(getHtmlFiles(filePath));
    } else if (file.endsWith('.html')) {
      results.push(filePath);
    }
  });
  return results;
}

const files = getHtmlFiles(templatesDir);
console.log(`Auditing and enriching ${files.length} HTML files with modern icons...`);

let updatedCount = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // 1. Search buttons in filter bars: <button ...>Buscar...</button> without icon
  content = content.replace(/<button([^>]*)>\s*(Buscar[a-zA-ZáéíóúÁÉÍÓÚ\s]*)\s*<\/button>/gi, (match, attrs, text) => {
    if (match.includes('<i class=')) return match;
    return `<button${attrs}><i class="fa-solid fa-magnifying-glass me-1"></i> <span>${text.trim()}</span></button>`;
  });

  // 2. Save / Submit buttons without icon
  content = content.replace(/<button([^>]*)type="submit"([^>]*)>\s*(Guardar[a-zA-ZáéíóúÁÉÍÓÚ\s]*|Registrar[a-zA-ZáéíóúÁÉÍÓÚ\s]*|Crear[a-zA-ZáéíóúÁÉÍÓÚ\s]*|Actualizar[a-zA-ZáéíóúÁÉÍÓÚ\s]*)\s*<\/button>/gi, (match, a1, a2, text) => {
    if (match.includes('<i class=')) return match;
    return `<button${a1}type="submit"${a2}><i class="fa-solid fa-check me-1"></i> <span>${text.trim()}</span></button>`;
  });

  // 3. Cancel / Return buttons
  content = content.replace(/<(a|button)([^>]*)class="([^"]*wf-btn-outline[^"]*)"([^>]*)>\s*(Cancelar\s*\/\s*Volver|Cancelar|Volver)\s*<\/\1>/gi, (match, tag, a1, cls, a2, text) => {
    if (match.includes('<i class=')) return match;
    return `<${tag}${a1}class="${cls}"${a2}><i class="fa-solid fa-arrow-left me-1"></i> <span>${text.trim()}</span></${tag}>`;
  });

  // 4. Delete / Baja buttons
  content = content.replace(/<(a|button)([^>]*)class="([^"]*text-danger[^"]*)"([^>]*)>\s*(Dar de baja|Eliminar|Baja)\s*<\/\1>/gi, (match, tag, a1, cls, a2, text) => {
    if (match.includes('<i class=')) return match;
    return `<${tag}${a1}class="${cls}"${a2}><i class="fa-solid fa-trash me-1"></i> <span>${text.trim()}</span></${tag}>`;
  });

  // 5. Edit buttons
  content = content.replace(/<(a|button)([^>]*)class="([^"]*wf-btn[^"]*)"([^>]*)>\s*(Editar|Modificar)\s*<\/\1>/gi, (match, tag, a1, cls, a2, text) => {
    if (match.includes('<i class=')) return match;
    return `<${tag}${a1}class="${cls}"${a2}><i class="fa-solid fa-pen-to-square me-1"></i> <span>${text.trim()}</span></${tag}>`;
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    updatedCount++;
  }
});

console.log(`✅ Enrichment complete! Updated ${updatedCount} files with contextual icons.`);
