const fs = require('fs');
const path = require('path');

const pagesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');
let totalModified = 0;
let totalBadgesRemoved = 0;

function cleanBadges(dir) {
  const items = fs.readdirSync(dir);
  for (const item of items) {
    const full = path.join(dir, item);
    if (fs.statSync(full).isDirectory()) {
      cleanBadges(full);
    } else if (item.endsWith('.html')) {
      let content = fs.readFileSync(full, 'utf8');
      // Matches any span with class containing pin-badge
      const badgeRegex = /<span\s+class=["'][^"']*pin-badge[^"']*["'][^>]*>.*?<\/span>/gis;
      const matches = content.match(badgeRegex);
      if (matches) {
        totalBadgesRemoved += matches.length;
        content = content.replace(badgeRegex, '');
        fs.writeFileSync(full, content, 'utf8');
        totalModified++;
      }
    }
  }
}

cleanBadges(pagesDir);
console.log('Archivos modificados: ' + totalModified);
console.log('Total pin-badges eliminados: ' + totalBadgesRemoved);
