const fs = require('fs');
const path = require('path');

const cuSteps = JSON.parse(fs.readFileSync(path.join(__dirname, 'cu_steps_audit.json'), 'utf-8'));
const html = fs.readFileSync(path.join(__dirname, '../docs/diseño/Pantallas_CU_Reales.html'), 'utf-8');

// Split html by figure-wrapper
const figures = html.split('<div class="figure-wrapper"');
figures.shift(); // remove everything before first figure

const screenMap = {};
figures.forEach(fig => {
  const match = fig.match(/id="(CU-[^"]+)"/);
  if (match) {
    screenMap[match[1]] = fig;
  }
});

console.log(`Parsed ${Object.keys(screenMap).length} screen figures from Pantallas_CU_Reales.html`);

const detailedAudit = [];

cuSteps.forEach(cu => {
  if (!cu.id) return;
  const screenContent = screenMap[cu.id];
  if (!screenContent) {
    detailedAudit.push({ id: cu.id, name: cu.name, error: 'NO_SCREEN_FIGURE' });
    return;
  }

  const missingBadges = [];
  const buttonMatches = [];
  const missingButtons = [];

  cu.steps.forEach(st => {
    // 1. Check Badges
    st.badges.forEach(b => {
      const hasBadge = screenContent.includes(`>${b}</span>`) || 
                       screenContent.includes(`>${b} <`) || 
                       screenContent.includes(`>${b}\n`);
      if (!hasBadge) {
        missingBadges.push({ badge: b, step: st.pNum, action: st.pAction });
      }
    });

    // 2. Check Quoted Button/Action Labels
    const quotedTerms = [...st.pAction.matchAll(/"([^"]+)"\s*\[([A-Z])\]/g)];
    quotedTerms.forEach(qt => {
      const termName = qt[1];
      const badge = qt[2];
      const cleanTerm = termName.replace(/^[+\s]+/, '').trim();
      
      // Let's check exact or case-insensitive presence in screenContent
      const present = screenContent.toLowerCase().includes(cleanTerm.toLowerCase());
      if (present) {
        buttonMatches.push({ badge, term: termName, found: true });
      } else {
        missingButtons.push({ badge, step: st.pNum, term: termName, action: st.pAction });
      }
    });
  });

  detailedAudit.push({
    id: cu.id,
    name: cu.name,
    totalSteps: cu.steps.length,
    missingBadges,
    missingButtons,
    buttonMatches
  });
});

fs.writeFileSync(path.join(__dirname, 'full_screen_audit_report.json'), JSON.stringify(detailedAudit, null, 2));

const issuesCount = detailedAudit.filter(a => a.missingBadges.length > 0 || a.missingButtons.length > 0).length;
console.log(`\n======================================================`);
console.log(`AUDITORÍA MINUCIOSA DE 100 CASOS DE USO Y PANTALLAS`);
console.log(`======================================================`);
console.log(`Total CUs evaluados: ${detailedAudit.length}`);
console.log(`CUs con coincidencia 100% en Badges y Textos de Botón: ${detailedAudit.length - issuesCount}`);
console.log(`CUs con observaciones de texto de botón: ${issuesCount}\n`);

detailedAudit.filter(a => a.missingBadges.length > 0 || a.missingButtons.length > 0).forEach(a => {
  console.log(`[${a.id}] ${a.name}:`);
  if (a.missingBadges.length > 0) {
    a.missingBadges.forEach(mb => console.log(`   🔴 Badge faltante: [${mb.badge}] en paso ${mb.step}`));
  }
  if (a.missingButtons.length > 0) {
    a.missingButtons.forEach(mb => console.log(`   ⚠️ Botón no literal: [${mb.badge}] "${mb.term}" (Paso ${mb.step})`));
  }
});
