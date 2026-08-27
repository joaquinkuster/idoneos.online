const fs = require('fs');
const path = require('path');

const mdPath = path.join(__dirname, '../docs/diseño/Casos de Uso Reales.md');
let md = fs.readFileSync(mdPath, 'utf8');

// Replace all "+ Name" with "Name" in quotes
let count = 0;
md = md.replace(/"\+\s*([^"]+)"/g, (match, p1) => {
  count++;
  return `"${p1}"`;
});

console.log(`Cleaned ${count} occurrences of '+' in button quotes.`);
fs.writeFileSync(mdPath, md, 'utf8');
