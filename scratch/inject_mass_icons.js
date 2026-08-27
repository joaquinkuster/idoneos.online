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
console.log(`Enriching ${files.length} HTML templates with modern FontAwesome 6 icons...`);

let updatedCount = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Enhance buttons and badges with crisp contextual icons if missing
  content = content.replace(/<button([^>]*)type="submit"([^>]*)>\s*(Buscar|Guardar|Registrar|Modificar|Filtrar|Aceptar|Confirmar)\s*<\/button>/gi, (m, p1, p2, text) => {
    let icon = 'fa-check';
    if (text.toLowerCase().includes('buscar') || text.toLowerCase().includes('filtrar')) icon = 'fa-magnifying-glass';
    if (text.toLowerCase().includes('guardar') || text.toLowerCase().includes('registrar')) icon = 'fa-floppy-disk';
    if (text.toLowerCase().includes('modificar')) icon = 'fa-pen-to-square';
    return `<button${p1}type="submit"${p2}><i class="fa-solid ${icon} me-1"></i> <span>${text}</span></button>`;
  });

  // Enhance common action links
  content = content.replace(/<a([^>]*)class="([^"]*wf-btn[^"]*)"([^>]*)>\s*\+?\s*Nuevo\s+([^<]+)<\/a>/gi, (m, p1, p2, p3, noun) => {
    return `<a${p1}class="${p2}"${p3}><i class="fa-solid fa-plus me-1"></i> <span>Nuevo ${noun.trim()}</span></a>`;
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    updatedCount++;
  }
});

console.log(`Successfully enriched ${updatedCount} templates with icons.`);
