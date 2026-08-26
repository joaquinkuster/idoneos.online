const fs = require('fs');
const path = require('path');

const md = fs.readFileSync(path.join(__dirname, '../docs/diseño/Casos de Uso Reales.md'), 'utf-8');
const cus = md.split(/(?=### CU-\d+)/g).filter(x => x.startsWith('### CU-'));

const cuData = cus.map(c => {
  const match = c.match(/### (CU-\d+):\s*([^\n\r]+)/);
  const id = match ? match[1] : '';
  const name = match ? match[2].trim() : '';

  const steps = [];
  const lines = c.split('\n');
  lines.forEach(l => {
    const stepMatch = l.match(/\|\s*(\d+)\s*\|\s*(.*?)\s*\|/);
    if (stepMatch && !l.includes('Paso')) {
      const pNum = stepMatch[1];
      const pAction = stepMatch[2];
      const badges = [...pAction.matchAll(/\[([A-Z])\]/g)].map(m => m[1]);
      if (badges.length > 0) {
        steps.push({ pNum, pAction, badges });
      }
    }
  });

  return { id, name, steps };
});

fs.writeFileSync(path.join(__dirname, 'cu_steps_audit.json'), JSON.stringify(cuData, null, 2));
console.log(`Parsed ${cuData.length} CUs with step details into scratch/cu_steps_audit.json`);
