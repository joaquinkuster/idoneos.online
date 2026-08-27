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
console.log(`Auditoría exhaustiva de botones y formularios sobre ${files.length} plantillas HTML:\n`);

let buttonsReport = [];

files.forEach(file => {
  const content = fs.readFileSync(file, 'utf8');
  const relPath = path.relative(process.cwd(), file);

  // Match all <button> elements
  const buttons = content.match(/<button[\s\S]*?<\/button>/gi) || [];
  // Match all <a> elements that look like buttons
  const linkButtons = content.match(/<a[^>]*class=["'][^"']*(?:wf-btn|btn)[^"']*["'][^>]*>[\s\S]*?<\/a>/gi) || [];

  let deadButtons = 0;
  let unhandledButtons = 0;

  buttons.forEach(btn => {
    // Check if button is type="submit" inside a form or has an onclick/action
    const isSubmit = /type=["']submit["']/i.test(btn);
    const hasOnClick = /onclick=/i.test(btn);
    const hasDataBs = /data-bs-/i.test(btn);
    if (!isSubmit && !hasOnClick && !hasDataBs) {
      unhandledButtons++;
    }
  });

  linkButtons.forEach(a => {
    if (/href=["']#["']/i.test(a) && !/th:href/i.test(a)) {
      deadButtons++;
    }
  });

  if (deadButtons > 0 || unhandledButtons > 0) {
    buttonsReport.push({
      file: relPath,
      totalButtons: buttons.length,
      totalLinkButtons: linkButtons.length,
      deadButtons,
      unhandledButtons
    });
  }
});

console.log(`📊 RESULTADO DE LA AUDITORÍA DE BOTONES:`);
console.log(`- Plantillas con botones huérfanos o no interactivos: ${buttonsReport.length}`);

if (buttonsReport.length > 0) {
  buttonsReport.forEach(r => {
    console.log(`   - ${r.file}: ${r.unhandledButtons} botones planos, ${r.deadButtons} links muertos`);
  });
} else {
  console.log(`✅ ¡Todos los botones y enlaces del sistema tienen acción, submit o navegación asignada!`);
}
