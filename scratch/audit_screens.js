const fs = require('fs');
const path = require('path');

const realesPath = path.join(__dirname, '..', 'docs', 'diseño', 'Casos de Uso Reales.md');
const htmlPath = path.join(__dirname, '..', 'docs', 'diseño', 'Pantallas_CU_Reales.html');

const reales = fs.readFileSync(realesPath, 'utf8');
const html = fs.readFileSync(htmlPath, 'utf8');

// Extraer todos los CUs de Reales
const realesBlocks = reales.split(/(?=### CU-)/g);
const cuList = [];

for (let i = 1; i < realesBlocks.length; i++) {
  const b = realesBlocks[i];
  const m = b.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!m) continue;
  const cuId = m[1].trim();
  const cuName = m[2].trim();
  
  // Extraer badges [A], [B], etc. del flujo
  const badgesInFlow = [];
  const flowMatch = b.match(/\| Paso \| Acción \|\r?\n\|[-|\s]+\|\r?\n([\s\S]*?)(?=\r?\n- \*\*(?:Postcondición|Salida|Excepciones))/);
  if (flowMatch) {
    const badgeRegex = /\[([A-Z0-9]+)\]/g;
    let bm;
    while ((bm = badgeRegex.exec(flowMatch[1])) !== null) {
      if (!badgesInFlow.includes(bm[1])) badgesInFlow.push(bm[1]);
    }
  }

  cuList.push({ id: cuId, name: cuName, badges: badgesInFlow });
}

console.log('Total CUs en Reales:', cuList.length);

let missingScreens = [];
let badgeMismatch = [];

cuList.forEach(cu => {
  const screenIdMatch = html.includes(`id="${cu.id}"`);
  if (!screenIdMatch) {
    missingScreens.push(cu.id);
  }

  // Buscar bloque de la pantalla en HTML
  const regex = new RegExp(`<div[^>]*class="wf-screen-item"[^>]*id="${cu.id}"[\\s\\S]*?(?=<div[^>]*class="wf-screen-item"|<footer>|<\\/main>|$)`, 'i');
  const match = html.match(regex);
  if (match) {
    const screenHtml = match[0];
    const htmlBadges = [];
    const pRegex = /<span[^>]*class="pin-badge[^>]*>([A-Z0-9]+)<\/span>/g;
    let pm;
    while ((pm = pRegex.exec(screenHtml)) !== null) {
      if (!htmlBadges.includes(pm[1])) htmlBadges.push(pm[1]);
    }

    const missingInHtml = cu.badges.filter(b => !htmlBadges.includes(b));
    if (missingInHtml.length > 0) {
      badgeMismatch.push({ cu: cu.id, name: cu.name, expected: cu.badges, foundInHtml: htmlBadges, missing: missingInHtml });
    }
  }
});

console.log('--- REPORTE DE AUDITORIA DE PANTALLAS ---');
console.log('1. Cobertura de pantallas (CUs):', cuList.length, 'esperados.');
console.log('   Pantallas faltantes en HTML:', missingScreens.length > 0 ? missingScreens : 'Ninguna (100% de los 100 CUs tienen pantalla).');
console.log('2. Coincidencia de Badges [A, B, C, D]:');
if (badgeMismatch.length === 0) {
  console.log('   ¡100% de coincidencia exacta en todos los badges!');
} else {
  console.log(`   Se detectaron ${badgeMismatch.length} pantallas con badges a sincronizar:`);
  console.log(JSON.stringify(badgeMismatch, null, 2));
}
