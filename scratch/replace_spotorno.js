const fs = require('fs');
const path = require('path');

const targetPath = path.join(__dirname, '..', 'generate_screens.js');
let file = fs.readFileSync(targetPath, 'utf8');

file = file.split('Lic. Fausto Spotorno').join('Mg. Elena Valenzuela');
file = file.split('Fausto Spotorno').join('Elena Valenzuela');
file = file.split('fausto.spotorno@idoneos.online').join('elena.valenzuela@idoneos.online');
file = file.split('fausto.spotorno').join('elena.valenzuela');
file = file.split('Lic. Spotorno').join('Mg. Valenzuela');
file = file.split('Spotorno').join('Valenzuela');
file = file.split('#avatar_spotorno_v2').join('#avatar_valenzuela_v2');
file = file.split('stream_u2_spotorno').join('stream_u2_valenzuela');
file = file.split('clase_vivo_u2_spotorno_hd.mp4').join('clase_vivo_u2_valenzuela_hd.mp4');
file = file.split("initials: 'FS'").join("initials: 'EV'");

fs.writeFileSync(targetPath, file, 'utf8');
console.log('Successfully updated all Fausto/Spotorno occurrences in generate_screens.js');
