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
console.log(`Detectando e higienizando estructuras cortadas o scripts insertados en medio del HTML en ${files.length} archivos...\n`);

let fixedFiles = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Check if there are multiple </html> or script bundles inside the body before footer
  const closingHtmlMatches = content.match(/<\/html>/gi);
  if (closingHtmlMatches && closingHtmlMatches.length > 1) {
    console.log(`⚠️ Archivo con múltiples </html>: ${path.relative(process.cwd(), file)}`);
    
    // Find where the premature </html> happened and repair it
    // Usually premature snippet: `<script src=" https: cdn.jsdelivr.net ... </html>`
    const parts = content.split(/<\/html>\s*/i);
    // Combine properly: keep the real bottom scripts at the true end
    const lastPart = parts[parts.length - 1];
    const firstPart = parts[0];

    // Clean any broken script from firstPart
    let cleanedFirst = firstPart.replace(/<script[^>]*src=["']\s*https:[^>]*>[\s\S]*?(<\/script>)?/gi, '');
    cleanedFirst = cleanedFirst.replace(/<script>[\s\S]*?toggleUserDropdown[\s\S]*?<\/script>/gi, '');
    cleanedFirst = cleanedFirst.replace(/<script>[\s\S]*?DSS & Case of Use[\s\S]*?<\/script>/gi, '');
    
    // Let's reconstruct properly
    let finalContent = cleanedFirst + lastPart;
    if (!finalContent.includes('</html>')) {
      finalContent += '\n</body>\n</html>';
    }
    
    // Also fix any unclosed price spans or tags like th:text="${curso.precio <= 0 ? 'GRATIS' : ...}"
    finalContent = finalContent.replace(/th:text="\$\{curso\.precio\s*0\s*GRATIS\s*:\s*-->[\s\S]*?\+ #numbers\.formatDecimal\(curso\.precio,\s*1,\s*'POINT',\s*0,\s*'COMMA'\)\}"/gi, 'th:text="${curso.precio != null && curso.precio <= 0 ? \'GRATIS\' : \'$ \' + #numbers.formatDecimal(curso.precio != null ? curso.precio : 0, 1, \'POINT\', 0, \'COMMA\')}"');
    
    finalContent = finalContent.replace(/th:text="\$\{curso\.precio\s*<=\s*0\s*\?\s*'GRATIS'\s*:\s*'\s*[\s\S]*?\+ #numbers\.formatDecimal\(curso\.precio,\s*1,\s*'POINT',\s*0,\s*'COMMA'\)\}"/gi, 'th:text="${curso.precio != null && curso.precio <= 0 ? \'GRATIS\' : \'$ \' + #numbers.formatDecimal(curso.precio != null ? curso.precio : 0, 1, \'POINT\', 0, \'COMMA\')}"');

    fs.writeFileSync(file, finalContent, 'utf8');
    fixedFiles++;
  }
});

console.log(`\n✅ Se repararon ${fixedFiles} archivos con cierres prematuros.`);
