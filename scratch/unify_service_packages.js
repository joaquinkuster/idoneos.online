const fs = require('fs');
const path = require('path');

const baseJava = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'app', 'idoneos');
const serviceDir = path.join(baseJava, 'service');

// 1. Move EvaluacionService.java to modulo_evaluaciones
const evalOld = path.join(serviceDir, 'Evaluacion', 'EvaluacionService.java');
const evalNew = path.join(serviceDir, 'modulo_evaluaciones', 'EvaluacionService.java');
if (fs.existsSync(evalOld)) {
  let content = fs.readFileSync(evalOld, 'utf8');
  content = content.replace('package com.app.idoneos.service.Evaluacion;', 'package com.app.idoneos.service.modulo_evaluaciones;');
  fs.writeFileSync(evalNew, content, 'utf8');
  fs.unlinkSync(evalOld);
  fs.rmdirSync(path.join(serviceDir, 'Evaluacion'));
  console.log('Moved EvaluacionService.java');
}

// 2. Move Reportes files to modulo_reportes
const reportesOldDir = path.join(serviceDir, 'Reportes');
const reportesNewDir = path.join(serviceDir, 'modulo_reportes');
if (fs.existsSync(reportesOldDir)) {
  fs.readdirSync(reportesOldDir).forEach(file => {
    const oldFile = path.join(reportesOldDir, file);
    const newFile = path.join(reportesNewDir, file);
    let content = fs.readFileSync(oldFile, 'utf8');
    content = content.replace('package com.app.idoneos.service.Reportes;', 'package com.app.idoneos.service.modulo_reportes;');
    fs.writeFileSync(newFile, content, 'utf8');
    fs.unlinkSync(oldFile);
    console.log('Moved ' + file + ' to modulo_reportes');
  });
  fs.rmdirSync(reportesOldDir);
}

// 3. Move UsuarioDetallesService.java to modulo_usuarios
const userOld = path.join(serviceDir, 'Usuario', 'UsuarioDetallesService.java');
const userNew = path.join(serviceDir, 'modulo_usuarios', 'UsuarioDetallesService.java');
if (fs.existsSync(userOld)) {
  let content = fs.readFileSync(userOld, 'utf8');
  content = content.replace('package com.app.idoneos.service.Usuario;', 'package com.app.idoneos.service.modulo_usuarios;');
  fs.writeFileSync(userNew, content, 'utf8');
  fs.unlinkSync(userOld);
  fs.rmdirSync(path.join(serviceDir, 'Usuario'));
  console.log('Moved UsuarioDetallesService.java');
}

// 4. Update imports across all Java files in src/main/java
function updateImports(dir) {
  fs.readdirSync(dir).forEach(item => {
    const full = path.join(dir, item);
    if (fs.statSync(full).isDirectory()) {
      updateImports(full);
    } else if (item.endsWith('.java')) {
      let c = fs.readFileSync(full, 'utf8');
      let changed = false;
      if (c.includes('com.app.idoneos.service.Evaluacion')) {
        c = c.replace(/com\.app\.idoneos\.service\.Evaluacion/g, 'com.app.idoneos.service.modulo_evaluaciones');
        changed = true;
      }
      if (c.includes('com.app.idoneos.service.Reportes')) {
        c = c.replace(/com\.app\.idoneos\.service\.Reportes/g, 'com.app.idoneos.service.modulo_reportes');
        changed = true;
      }
      if (c.includes('com.app.idoneos.service.Usuario')) {
        c = c.replace(/com\.app\.idoneos\.service\.Usuario/g, 'com.app.idoneos.service.modulo_usuarios');
        changed = true;
      }
      if (changed) {
        fs.writeFileSync(full, c, 'utf8');
        console.log('Updated imports in ' + path.basename(full));
      }
    }
  });
}

updateImports(baseJava);
console.log('All legacy service packages migrated successfully!');
