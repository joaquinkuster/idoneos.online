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
let totalModified = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Remove <span class="pin-badge">X</span> or similar
  content = content.replace(/<span\s+class=["']pin-badge["'][^>]*>[\s\S]*?<\/span>/gi, '');
  // Remove badge-dss if any
  content = content.replace(/<div\s+class=["']badge-dss["'][^>]*>[\s\S]*?<\/div>/gi, '');

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    totalModified++;
  }
});

console.log(`✅ Se eliminaron los pin-badges en ${totalModified} plantillas.`);
