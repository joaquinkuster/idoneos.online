const fs = require('fs');
const h = fs.readFileSync('docs/diseño/Pantallas_CU_Reales.html', 'utf8');

function inspectFull(id) {
  const marker = 'id="' + id + '"';
  const pos = h.indexOf(marker);
  const endPos = h.indexOf('<!-- Epígrafe Formal', pos);
  console.log('=== ' + id + ' FULL HTML ===\n', h.substring(pos, endPos));
}

inspectFull('CU-76');
inspectFull('CU-77');
