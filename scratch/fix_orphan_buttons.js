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
let fixedFiles = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Clean duplicate card-footers after form closure
  content = content.replace(/<\/form>\s*<\/div>\s*<\/div>\s*<!-- Footer con Guardar[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/gi, `</form>\n</div>\n</div>`);

  // Transform orphan buttons with Cancelar/Volver into real links
  content = content.replace(/<button([^>]*class=["'][^"']*wf-btn-outline[^"']*["'][^>]*)>(?:Cancelar|Volver|Cancelar \/ Volver)<\/button>/gi, `<a th:href="@{/cursos}" class="wf-btn wf-btn-outline">Cancelar</a>`);

  // If a form exists with a submit button without explicit type, add type="submit"
  content = content.replace(/<button([^>]*class=["'][^"']*wf-btn-primary[^"']*["'])(?!.*type=)[^>]*>([\s\S]*?Guardar[\s\S]*?)<\/button>/gi, `<button type="submit"$1>$2</button>`);

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    fixedFiles++;
  }
});

console.log(`✅ Se sanearon botones duplicados y planos en ${fixedFiles} plantillas.`);
