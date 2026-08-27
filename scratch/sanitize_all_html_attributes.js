const fs = require('fs');
const path = require('path');

const templatesDir = path.join('src', 'main', 'resources', 'templates');

function walk(dir) {
  let results = [];
  fs.readdirSync(dir).forEach(file => {
    let full = path.join(dir, file);
    if (fs.statSync(full).isDirectory()) results = results.concat(walk(full));
    else if (file.endsWith('.html')) results.push(full);
  });
  return results;
}

const files = walk(templatesDir);
let fixedFiles = 0;

// Function to clean duplicate attributes on any HTML element
function cleanTagAttributes(tag) {
  // Regex to match tag name and attribute string
  const openTagMatch = tag.match(/^<([a-zA-Z0-9\-]+)(\s+[\s\S]*)?(\/?>)$/);
  if (!openTagMatch) return tag;

  const tagName = openTagMatch[1];
  const attrString = openTagMatch[2] || '';
  const closing = openTagMatch[3];

  if (!attrString.trim()) return tag;

  // Parse individual attributes
  // Matches attr="...", attr='...', attr=val, or boolean attr
  const attrRegex = /([a-zA-Z0-9\-_:.]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?/g;
  let match;
  const seenAttrs = new Set();
  const attributes = [];

  while ((match = attrRegex.exec(attrString)) !== null) {
    const attrName = match[1].toLowerCase();
    const fullAttr = match[0];
    
    // In Thymeleaf / HTML, only keep the first occurrence of each attribute
    if (!seenAttrs.has(attrName)) {
      seenAttrs.add(attrName);
      attributes.push(fullAttr);
    }
  }

  return `<${tagName} ${attributes.join(' ')}${closing.startsWith('/') ? ' /' : ''}>`;
}

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Clean tags with potential duplicated attributes
  content = content.replace(/<[a-zA-Z0-9\-]+(\s+[^>]+)>/g, (match) => {
    // Only process if it has attributes
    return cleanTagAttributes(match);
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    fixedFiles++;
  }
});

console.log(`✅ Saneamiento completo de atributos duplicados en ${fixedFiles} archivos HTML.`);
