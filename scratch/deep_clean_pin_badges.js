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
files.forEach(f => {
  let content = fs.readFileSync(f, 'utf8');
  content = content.replace(/<span\s+class=["'][^"']*pin-badge[^"']*["'][^>]*>[\s\S]*?<\/span>/gi, '');
  content = content.replace(/<div\s+class=["'][^"']*pin-badge[^"']*["'][^>]*>[\s\S]*?<\/div>/gi, '');
  fs.writeFileSync(f, content, 'utf8');
});

console.log('✅ Barrido total de pin badges completado.');
