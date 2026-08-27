const fs = require('fs');
const path = require('path');

const templatesDir = path.join('src', 'main', 'resources', 'templates');

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
console.log(`Auditoría exhaustiva de atributos duplicados y sintaxis en ${files.length} plantillas HTML:\n`);

let fixedCount = 0;
let errorsFound = [];

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // 1. Fix duplicate type="..."
  content = content.replace(/(type=["'][^"']*["'])\s+type=["'][^"']*["']/gi, '$1');
  
  // 2. Fix duplicate class="..."
  content = content.replace(/(class=["'][^"']*["'])\s+class=["'][^"']*["']/gi, '$1');

  // 3. Fix duplicate method="..."
  content = content.replace(/(method=["'][^"']*["'])\s+method=["'][^"']*["']/gi, '$1');

  // 4. Fix duplicate name="..."
  content = content.replace(/(name=["'][^"']*["'])\s+name=["'][^"']*["']/gi, '$1');

  // 5. Fix duplicate th:action="..."
  content = content.replace(/(th:action=["'][^"']*["'])\s+th:action=["'][^"']*["']/gi, '$1');

  // 6. Fix duplicate th:href="..."
  content = content.replace(/(th:href=["'][^"']*["'])\s+th:href=["'][^"']*["']/gi, '$1');

  // 7. Check if there are any remaining double attributes
  const doubleAttrRegex = /<([a-zA-Z0-9\-]+)\s+[^>]*?([a-zA-Z0-9\-_:]+)=["'][^"']*["']\s+[^>]*?\2=["'][^"']*["'][^>]*>/gi;
  let match;
  while ((match = doubleAttrRegex.exec(content)) !== null) {
    errorsFound.push({ file: path.relative(process.cwd(), file), tag: match[0] });
  }

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    fixedCount++;
  }
});

console.log(`✅ Se corrigieron atributos duplicados en ${fixedCount} plantillas.`);
if (errorsFound.length > 0) {
  console.log(`⚠️ Atributos duplicados restantes encontrados:`, errorsFound);
} else {
  console.log(`🎉 ¡Cero etiquetas con atributos duplicados en todo el sistema!`);
}
