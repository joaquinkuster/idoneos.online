const fs = require('fs');
const path = require('path');

const cuSteps = JSON.parse(fs.readFileSync(path.join(__dirname, 'cu_steps_audit.json'), 'utf-8'));
const html = fs.readFileSync(path.join(__dirname, '../docs/diseño/Pantallas_CU_Reales.html'), 'utf-8');

console.log(`Auditing all 100 CU screens in Pantallas_CU_Reales.html against Casos de Uso Reales.md steps & button text...`);

// Split html by CU screens
const screens = html.split('<div class="screen-frame mb-5"');
screens.shift(); // remove header before first screen

const screenMap = {};
screens.forEach(s => {
  const match = s.match(/id="(CU-[^"]+)"/);
  if (match) {
    screenMap[match[1]] = s;
  }
});

const report = [];

cuSteps.forEach(cu => {
  if (!cu.id) return;
  const screenContent = screenMap[cu.id];
  if (!screenContent) {
    report.push({ id: cu.id, name: cu.name, error: 'PANTALLA NO ENCONTRADA EN HTML' });
    return;
  }

  // Check each step for badge and keyword in action
  const stepIssues = [];
  cu.steps.forEach(st => {
    st.badges.forEach(b => {
      // Check if badge exists in screen
      const hasBadge = screenContent.includes(`>${b}</span>`) || screenContent.includes(`>${b} <`) || screenContent.includes(`>${b}\n`);
      if (!hasBadge) {
        stepIssues.push(`Falta Badge [${b}] en Paso ${st.pNum}`);
      }
    });

    // Extract button or element name from action e.g. "botón \"Guardar\" [C]"
    const quotedTerms = [...st.pAction.matchAll(/"([^"]+)"\s*\[([A-Z])\]/g)];
    quotedTerms.forEach(qt => {
      const termName = qt[1];
      const badge = qt[2];
      // Check if termName or similar is in screenContent
      const cleanTerm = termName.replace(/^[+\s]+/, '').trim();
      const hasTerm = screenContent.toLowerCase().includes(cleanTerm.toLowerCase());
      if (!hasTerm) {
        stepIssues.push(`Posible discrepancia de texto en botón [${badge}]: "${termName}" no encontrado textualmente en la pantalla.`);
      }
    });
  });

  if (stepIssues.length > 0) {
    report.push({ id: cu.id, name: cu.name, issues: stepIssues });
  }
});

console.log(`\n=== REPORTE DE AUDITORÍA DETALLADA DE BOTONES Y TEXTOS ===`);
console.log(`Total CUs auditados: ${Object.keys(screenMap).length}`);
console.log(`CUs con observaciones o discrepancias de texto: ${report.length}\n`);

report.forEach(r => {
  console.log(`[${r.id}] ${r.name}:`);
  if (r.error) console.log(`   ERROR: ${r.error}`);
  if (r.issues) {
    r.issues.forEach(iss => console.log(`   - ${iss}`));
  }
});

fs.writeFileSync(path.join(__dirname, 'detailed_button_audit_report.json'), JSON.stringify(report, null, 2));
