const fs = require('fs');
const path = require('path');

const md = fs.readFileSync(path.join(__dirname, '../docs/diseño/Casos de Uso Reales.md'), 'utf8');
const dss = fs.readFileSync(path.join(__dirname, '../docs/analisis/DSS.md'), 'utf8');

// Parse DSS operations
const dssBlocks = dss.split(/(?=### CU-)/g);
const dssMap = {};
for (let b of dssBlocks) {
  const m = b.match(/### (CU-[^:\r\n]+):/);
  if (!m) continue;
  const id = m[1].trim();
  const ops = [...b.matchAll(/`([a-zA-Z0-9_]+)\(([^)]*)\)`/g)].map(x => ({ op: x[1], params: x[2].split(',').map(p => p.trim()).filter(Boolean) }));
  dssMap[id] = ops;
}

// Parse CU blocks
const cuBlocks = md.split(/(?=### CU-)/g);

const results = [];
for (let i = 1; i < cuBlocks.length; i++) {
  const block = cuBlocks[i];
  const titleMatch = block.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) continue;
  const id = titleMatch[1].trim();
  const name = titleMatch[2].trim();
  const dssOps = dssMap[id] || [];
  
  // Look for main mutations/operations in DSS
  const mutOps = dssOps.filter(o => o.op.startsWith('registrar') || o.op.startsWith('modificar') || o.op.startsWith('crear') || o.op.startsWith('generar') || o.op.startsWith('responder') || o.op.startsWith('enviar') || o.op.startsWith('configurar') || o.op.startsWith('agregar'));
  
  const stepLines = block.split('\n').filter(l => l.trim().startsWith('|') && !l.includes('Paso') && !l.includes('---'));
  
  results.push({
    id,
    name,
    mutOps,
    hasGenericFormPhrase: block.includes('mediante el formulario') || block.includes('formulario de') || block.includes('ingresa los datos solicitados') || block.includes('los campos solicitados'),
    badges: [...new Set([...block.matchAll(/\[([A-Z0-9]+)\]/g)].map(x => x[1]))],
    stepLines
  });
}

const toAudit = results.filter(r => r.mutOps.length > 0 || r.name.toLowerCase().startsWith('registrar') || r.name.toLowerCase().startsWith('modificar') || r.name.toLowerCase().startsWith('crear') || r.name.toLowerCase().startsWith('generar') || r.name.toLowerCase().startsWith('agregar'));

console.log(`Total CUs de alta/modificación analizados: ${toAudit.length}\n`);

const report = toAudit.map(r => {
  return {
    id: r.id,
    name: r.name,
    dssOperations: r.mutOps,
    badges: r.badges,
    steps: r.stepLines
  };
});

fs.writeFileSync(path.join(__dirname, 'cu_forms_dss_audit.json'), JSON.stringify(report, null, 2));
console.log('Saved report to scratch/cu_forms_dss_audit.json');
