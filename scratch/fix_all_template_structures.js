const fs = require('fs');
const path = require('path');

const templatesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

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
console.log(`Auditing and repairing ${files.length} template files...`);

let modifiedCount = 0;

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // 1. Remove premature closing div right after wf_navbar inclusion
  content = content.replace(
    /(<div\s+th:replace="~\{fragments\/wf_navbar\s*::\s*wf-top-navbar\}"><\/div>)\s*<\/div>/g,
    '$1'
  );

  // 2. Ensure wf-canvas-bg is on body
  content = content.replace(/<body([^>]*)class="([^"]*)"/g, (match, p1, p2) => {
    if (!p2.includes('wf-canvas-bg')) {
      return `<body${p1}class="wf-canvas-bg ${p2}".trim()`;
    }
    return match;
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    modifiedCount++;
    console.log(`Repaired structure in: ${path.basename(file)}`);
  }
});

console.log(`Finished auditing. Repaired ${modifiedCount} templates.`);
