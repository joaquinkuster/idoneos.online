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
let replacedCount = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  const original = content;

  // Replace existing inline <div class="wf-top-navbar">...</div> with Thymeleaf replace fragment
  content = content.replace(/<!-- Top Navbar Oficial -->[\s\S]*?<div class="wf-top-navbar">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<\/div>/, `<!-- Top Navbar Oficial Dinámica -->\n                <div th:replace="~{fragments/wf_navbar :: wf-top-navbar}"></div>`);

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    replacedCount++;
  }
});

console.log(`✅ Navbar dinámico centralizado integrado en ${replacedCount} plantillas.`);
