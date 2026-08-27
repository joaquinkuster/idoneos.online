const fs = require('fs');
const path = require('path');

const templatesDir = path.join('src', 'main', 'resources', 'templates', 'pages');

function walk(dir) {
  let results = [];
  fs.readdirSync(dir).forEach(file => {
    let full = path.join(dir, file);
    if (fs.statSync(full).isDirectory()) results = results.concat(walk(full));
    else if (file.endsWith('.html')) results.push(full);
  });
  return results;
}

const files = walk(templatesDir);
let fixedDuplicates = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Clean trailing orphan search blocks after </form>
  content = content.replace(/<\/form>\s*<\/div>\s*<div class="col-md-3">\s*<div class="d-flex align-items-center gap-2">\s*<button class="wf-btn wf-btn-primary w-100">Buscar Cursos<\/button>\s*<\/div>\s*<\/div>\s*<\/div>\s*<\/div>/gi, '</form>');

  // Ensure all buttons inside forms have type="submit"
  content = content.replace(/<form([\s\S]*?)>([\s\S]*?)<\/form>/gi, (match, formAttrs, formInner) => {
    let newInner = formInner.replace(/<button([^>]*class=["'][^"']*wf-btn-primary[^"']*["'])(?!.*type=)[^>]*>/gi, '<button type="submit"$1>');
    return `<form${formAttrs}>${newInner}</form>`;
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    fixedDuplicates++;
  }
});

console.log(`✅ Se limpiaron bloques residuales y se configuró type="submit" en ${fixedDuplicates} plantillas.`);
