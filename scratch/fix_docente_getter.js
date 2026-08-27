const fs = require('fs');

// Fix CU-01
let cu01 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', 'utf8');
cu01 = cu01.replace(/curso\.docenteTitular/g, 'curso.docente');
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', cu01, 'utf8');

// Fix CU-06
let cu06 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-06-explorar-catalogo-de-cursos.html', 'utf8');
cu06 = cu06.replace(/curso\.docenteTitular/g, 'curso.docente');
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-06-explorar-catalogo-de-cursos.html', cu06, 'utf8');

console.log('✅ Corregido curso.docente en CU-01 y CU-06');
