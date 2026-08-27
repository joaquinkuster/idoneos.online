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
console.log(`Auditoría exhaustiva sobre ${files.length} plantillas HTML:\n`);

let report = [];

files.forEach(file => {
  const content = fs.readFileSync(file, 'utf8');
  const relPath = path.relative(process.cwd(), file);
  
  const hasThAction = content.includes('th:action');
  const hasThEach = content.includes('th:each');
  const hasThHref = content.includes('th:href');
  const hasThValue = content.includes('th:value');
  const hasThText = content.includes('th:text');
  const hasDeadHref = /href=["']#["']/i.test(content);
  const hasPinBadge = /pin-badge/i.test(content);
  const hasStaticTableBody = /<tbody>[\s\S]*?<tr>[\s\S]*?<\/tr>[\s\S]*?<\/tbody>/i.test(content) && !content.includes('th:each');

  report.push({
    file: relPath,
    hasThAction,
    hasThEach,
    hasThHref,
    hasThValue,
    hasThText,
    hasDeadHref,
    hasPinBadge,
    hasStaticTableBody
  });
});

const deadHrefs = report.filter(r => r.hasDeadHref);
const pinBadges = report.filter(r => r.hasPinBadge);
const staticTables = report.filter(r => r.hasStaticTableBody);
const withoutThymeleaf = report.filter(r => !r.hasThAction && !r.hasThEach && !r.hasThHref && !r.hasThValue && !r.hasThText);

console.log(`📊 RESULTADOS DE LA AUDITORÍA:`);
console.log(`- Total plantillas analizadas: ${report.length}`);
console.log(`- Plantillas con enlaces muertos (href="#"): ${deadHrefs.length}`);
console.log(`- Plantillas con pin-badges: ${pinBadges.length}`);
console.log(`- Plantillas con tablas estáticas sin th:each: ${staticTables.length}`);
console.log(`- Plantillas sin ninguna directiva Thymeleaf: ${withoutThymeleaf.length}`);

if (deadHrefs.length > 0) {
  console.log(`\n⚠️ Archivos con href="#":`);
  deadHrefs.forEach(d => console.log(`   - ${d.file}`));
}

if (staticTables.length > 0) {
  console.log(`\n⚠️ Archivos con tablas estáticas sin iteración:`);
  staticTables.forEach(s => console.log(`   - ${s.file}`));
}

fs.writeFileSync('scratch/audit_report_full.json', JSON.stringify(report, null, 2), 'utf8');
