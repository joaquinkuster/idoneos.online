const fs = require('fs');
const path = require('path');

const generateScreensPath = path.join(__dirname, '../generate_screens.js');
let code = fs.readFileSync(generateScreensPath, 'utf8');

// 1. Inject require for specialized forms at the top
if (!code.includes("const specializedFormGenerators = require('./scratch/all_specialized_forms.js');")) {
  code = "const specializedFormGenerators = require('./scratch/all_specialized_forms.js');\n" + code;
}

// 2. Add check inside generateScreenContent
const targetStart = "function generateScreenContent(cu) {\n  const id = cu.id;\n  const name = cu.name;\n  const badges = cu.badges;";
const replacementStart = `function generateScreenContent(cu) {\n  const id = cu.id;\n  const name = cu.name;\n  const badges = cu.badges;\n\n  if (specializedFormGenerators && specializedFormGenerators[id]) {\n    return specializedFormGenerators[id](cu, badges);\n  }`;

if (code.includes(targetStart)) {
  code = code.replace(targetStart, replacementStart);
} else {
  console.log("Warning: targetStart not matched directly, attempting CRLF replacement");
  const targetCRLF = "function generateScreenContent(cu) {\r\n  const id = cu.id;\r\n  const name = cu.name;\r\n  const badges = cu.badges;";
  const replCRLF = "function generateScreenContent(cu) {\r\n  const id = cu.id;\r\n  const name = cu.name;\r\n  const badges = cu.badges;\r\n\r\n  if (specializedFormGenerators && specializedFormGenerators[id]) {\r\n    return specializedFormGenerators[id](cu, badges);\r\n  }";
  code = code.replace(targetCRLF, replCRLF);
}

// 3. Replace isDelete implementation
const oldDeleteBlockStart = code.indexOf('// --- SPECIALIZED 14: BAJA / ELIMINACIÓN GENÉRICA ---');
const oldSearchBlockStart = code.indexOf('// --- SPECIALIZED 15: BÚSQUEDAS / TABLAS DE GESTIÓN ---');

if (oldDeleteBlockStart !== -1 && oldSearchBlockStart !== -1) {
  const newDeleteBlock = `// --- SPECIALIZED 14: BAJA / ELIMINACIÓN GENÉRICA ---
  const isDelete = name.toLowerCase().includes('baja') || name.toLowerCase().includes('cancelar') || name.toLowerCase().includes('eliminar') || name.toLowerCase().includes('quitar');
  if (isDelete) {
    let confirmBtnLabel = 'Confirmar Eliminación';
    if (name.includes('baja') && !name.includes('usuario') && !name.includes('categoría')) confirmBtnLabel = 'Confirmar Baja';
    if (name.includes('cohorte')) confirmBtnLabel = 'Confirmar Cancelación';
    if (name.includes('usuario')) confirmBtnLabel = 'Confirmar Desactivación';
    if (name.includes('intento')) confirmBtnLabel = 'Confirmar Anulación';
    if (name.toLowerCase().includes('quitar') && name.includes('unidad')) confirmBtnLabel = 'Confirmar y Quitar';
    if (name.includes('foro') || name.includes('respuesta')) confirmBtnLabel = 'Confirmar Eliminación';
    if (name.includes('vivo') && name.toLowerCase().includes('cancelar')) confirmBtnLabel = 'Confirmar Cancelación';
    if (name.includes('vivo') && !name.toLowerCase().includes('cancelar')) confirmBtnLabel = 'Confirmar Eliminación';
    if (name.includes('clon')) confirmBtnLabel = 'Confirmar Eliminación';
    if (name.includes('glosario') || name.includes('descuento')) confirmBtnLabel = 'Confirmar Baja';

    let bgEntityName = \`Registro #\${id.replace('CU-', '')}\`;
    let bgExtraInfo = 'Sin dependencias bloqueantes';

    if (id === 'CU-64' || name.includes('intento')) {
      bgEntityName = 'Intento #64 — Alumno: Joaquín Küster';
      bgExtraInfo = 'Alerta de moderación: Patrón de tiempo sospechoso / posible suplantación';
    } else if (id === 'CU-10' || name.includes('categoría')) {
      bgEntityName = 'Categoría: Mercado de Capitales & Finanzas';
      bgExtraInfo = 'Validación: No posee cursos activos asociados';
    } else if (id === 'CU-14' || name.includes('cohorte')) {
      bgEntityName = 'Cohorte 2026-1 — Especialización en Idoneidad Bursátil';
      bgExtraInfo = 'Validación: Sin inscripciones activas registradas';
    } else if (id === 'CU-18' || name.includes('programa')) {
      bgEntityName = 'Programa 2026-A (Idoneidad Bursátil)';
      bgExtraInfo = 'Validación: Sin cohortes asociadas en el historial';
    } else if (id === 'CU-22' || (name.includes('unidad') && name.toLowerCase().includes('quitar'))) {
      bgEntityName = 'Unidad 5: Futuros y Opciones Financieras';
      bgExtraInfo = 'Validación: Unidad sin cohortes con inscripción activa';
    } else if (id === 'CU-30' || name.includes('material')) {
      bgEntityName = 'Material: Guía Práctica de TIR y Duration (PDF)';
      bgExtraInfo = 'Impacto: El archivo dejará de estar disponible en la unidad';
    } else if (id === 'CU-34' || name.includes('glosario')) {
      bgEntityName = 'Término: Duration Modificada (Glosario U2)';
      bgExtraInfo = 'Impacto: Se retirará de las definiciones de la unidad';
    } else if (id === 'CU-38' || name.includes('consulta')) {
      bgEntityName = 'Consulta: Duda sobre paridad en bonos';
      bgExtraInfo = 'Moderación: Se dará de baja el hilo junto con sus respuestas';
    } else if (id === 'CU-42' || name.includes('respuesta')) {
      bgEntityName = 'Respuesta de Foro en Hilo #35';
      bgExtraInfo = 'Moderación: Se ocultará la respuesta del foro';
    } else if (id === 'CU-48' || name.includes('inscripción') || name.includes('inscripcion')) {
      bgEntityName = 'Inscripción #48 — Alumno: Joaquín Küster';
      bgExtraInfo = 'Procesamiento de anulación y reembolso si corresponde';
    } else if (id === 'CU-52' || name.includes('descuento')) {
      bgEntityName = 'Beca / Cupón: UNAM2026 (25% off)';
      bgExtraInfo = 'Validación: Inhabilitará el código para futuras matrículas';
    } else if (id === 'CU-56' || name.includes('pool')) {
      bgEntityName = 'Banco de Preguntas: Renta Fija U2';
      bgExtraInfo = 'Validación: No está asignado a evaluaciones con intentos activos';
    } else if (id === 'CU-60' || name.includes('autoevaluación') || name.includes('autoevaluacion')) {
      bgEntityName = 'Autoevaluación: Cuestionario U2 Renta Fija';
      bgExtraInfo = 'Validación: No registra intentos de alumnos completados';
    } else if (id === 'CU-68' || id === 'CU-69' || name.includes('vivo')) {
      bgEntityName = 'Clase en Vivo: Taller Práctico de Curvas';
      bgExtraInfo = 'Notificación: Se enviará correo de cancelación a los inscriptos';
    } else if (id === 'CU-80' || name.includes('clon')) {
      bgEntityName = 'Video Clon IA: Explicación de Convexidad';
      bgExtraInfo = 'Impacto: Se eliminará del repositorio de medios de la unidad';
    } else if (id === 'CU-85' || name.includes('usuario')) {
      bgEntityName = 'Usuario: Lic. Fausto Spotorno (fausto.spotorno@idoneos.online)';
      bgExtraInfo = 'Validación: Desactivación de credenciales y accesos';
    }

    return \`
      <div class="wf-modal-dialog shadow-sm" style="max-width: 620px; margin: 40px auto; background: #FFFFFF; border-radius: 12px; border: 1px solid #E2E8F0; overflow: hidden;">
        <!-- Cabecera del Diálogo -->
        <div class="p-3 border-bottom d-flex justify-content-between align-items-center" style="background: #F8FAFC;">
          <div class="d-flex align-items-center gap-2">
            <i class="fa-solid fa-triangle-exclamation text-danger"></i>
            <strong style="font-size: 14px; color: #081426;">\${name}</strong>
          </div>
          <span class="wf-badge status-active">Confirmación Requerida</span>
        </div>

        <!-- Cuerpo del Diálogo -->
        <div class="p-4 text-center">
          <div class="mb-3" style="width: 56px; height: 56px; border-radius: 50%; background: #FEE2E2; color: #DC2626; display: flex; align-items: center; justify-content: center; margin: 0 auto; font-size: 24px;">
            <i class="fa-solid fa-triangle-exclamation"></i>
          </div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin-bottom: 6px;">¿Confirma la operación de \${name.toLowerCase()}?</h3>
          <p class="small text-muted mb-4" style="line-height: 1.5;">Esta acción procesará el cambio de estado en la base de datos y afectará la disponibilidad del elemento en el sistema.</p>

          <div class="p-3 mb-4 bg-light border rounded text-start" style="font-size: 13px;">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Registro afectado:</span>
              <strong style="color: #081426;">\${bgEntityName}</strong>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Estado actual:</span>
              <span class="wf-badge status-active">Activo / Vigente</span>
            </div>
            <div class="d-flex justify-content-between align-items-center">
              <span class="text-muted">Validación de dependencias:</span>
              <span class="small text-success fw-bold"><i class="fa-solid fa-circle-check me-1"></i> \${bgExtraInfo}</span>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
            <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-danger"><i class="fa-solid fa-trash me-1"></i> \${confirmBtnLabel}</button>
              <span class="pin-badge">\${badges[1] || badges[badges.length - 1] || 'B'}</span>
            </div>
          </div>
        </div>
      </div>
    \`;
  }
\n  `;

  code = code.substring(0, oldDeleteBlockStart) + newDeleteBlock + code.substring(oldSearchBlockStart);
}

// 4. Update isSearch button labels to remove + prefixes
code = code.replace(/createBtnLabel = '\+ ([^']+)'/g, "createBtnLabel = '$1'");

// 5. Update Hero Banner in generate_screens.js
const heroSearchButtons = `
                    \${cu.id === 'CU-01' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-03" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Curso</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
                    \${cu.id === 'CU-07' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-08" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Categoría</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
                    \${cu.id === 'CU-11' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-12" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Cohorte</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
                    \${cu.id === 'CU-15' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-16" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Programa</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
                    \${cu.id === 'CU-49' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-50" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Descuento</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
                    \${cu.id === 'CU-82' ? \`
                    <div class="d-flex align-items-center gap-2">
                        <a href="#CU-83" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Usuario</span>
                        </a>
                        <span class="pin-badge">A</span>
                    </div>
                    \` : ''}
`;

const oldHeroBtnStart = code.indexOf("${cu.id === 'CU-01' ? `");
const oldHeroBtnEnd = code.indexOf("${generateScreenContent(cu)}");

if (oldHeroBtnStart !== -1 && oldHeroBtnEnd !== -1) {
  const heroBannerClosePos = code.lastIndexOf('</div>', oldHeroBtnEnd);
  code = code.substring(0, oldHeroBtnStart) + heroSearchButtons + code.substring(heroBannerClosePos);
}

fs.writeFileSync(generateScreensPath, code, 'utf8');
console.log('Successfully updated generate_screens.js safely.');
