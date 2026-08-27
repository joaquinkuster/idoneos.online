const fs = require('fs');
const h = fs.readFileSync('docs/diseño/Pantallas_CU_Reales.html', 'utf8');

function showBody(id) {
  const marker = 'id="' + id + '"';
  const pos = h.indexOf(marker);
  if (pos !== -1) {
    const endPos = h.indexOf('<!-- Epígrafe Formal', pos);
    const chunk = h.substring(pos, endPos !== -1 ? endPos : pos + 4000);
    const bodyPos = chunk.indexOf('<div class="wf-main-content">');
    console.log('=== ' + id + ' BODY ===\n', chunk.substring(bodyPos, bodyPos + 1800));
  }
}

showBody('CU-78');
showBody('CU-79');
