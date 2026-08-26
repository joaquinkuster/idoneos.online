const fs = require('fs');
const path = require('path');

const cuSteps = JSON.parse(fs.readFileSync(path.join(__dirname, 'cu_steps_audit.json'), 'utf-8'));
const genCode = fs.readFileSync(path.join(__dirname, '../generate_screens.js'), 'utf-8');

console.log(`Loaded ${cuSteps.length} CUs to cross-reference with generate_screens.js templates...`);

// We want to verify which CUs have specialized screens vs fallback screens
const customHandlers = [
  'CU-01', 'CU-02', 'CU-06', 'CU-26', 'CU-26b', 'CU-19', 'CU-20', 'CU-21', 'CU-22',
  'CU-27', 'CU-31', 'CU-35', 'CU-53', 'CU-57', 'CU-65', 'CU-25',
  'CU-28', 'CU-32', 'CU-36', 'CU-40', 'CU-54', 'CU-58', 'CU-66',
  'CU-76', 'CU-77', 'CU-78', 'CU-79',
  'CU-73', 'CU-74', 'CU-75',
  'CU-63', 'CU-64', 'CU-72', 'CU-70',
  'CU-44', 'CU-45', 'CU-46', 'CU-47', 'CU-48', 'CU-49', 'CU-50', 'CU-51',
  'CU-80', 'CU-81', 'CU-82', 'CU-83', 'CU-84', 'CU-85', 'CU-86', 'CU-87',
  'CU-88', 'CU-89', 'CU-90', 'CU-91', 'CU-92', 'CU-93', 'CU-94', 'CU-95', 'CU-96', 'CU-97', 'CU-98', 'CU-99', 'CU-100',
  'CU-101', 'CU-102', 'CU-103', 'CU-104', 'CU-105', 'CU-106', 'CU-107', 'CU-108'
];

cuSteps.forEach(cu => {
  if (!cu.id) return;
  console.log(`\n--- CU: ${cu.id}: ${cu.name} ---`);
  cu.steps.forEach(s => {
    console.log(`  [Paso ${s.pNum}] Badges: ${s.badges.join(', ')} | Acción: ${s.pAction.substring(0, 80)}...`);
  });
});
