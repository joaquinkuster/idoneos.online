const fs = require('fs');
const path = require('path');

const cuSteps = JSON.parse(fs.readFileSync(path.join(__dirname, 'cu_steps_audit.json'), 'utf-8'));

// Build specialized screens for every CU group or CU ID so each matches its exact workflow
const specializedScreenGenerators = {
  // CU-23: Buscar cronograma
  'CU-23': (cu, badges) => `
    <div class="wf-card" style="max-width: 960px; margin: 0 auto; background: #FFFFFF;">
      <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Cronograma de Dictado Académico</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Visualice la secuencia pedagógica temporal y semanas asignadas por unidad.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <a href="#CU-24" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1">
            <i class="fa-solid fa-arrow-down-short-wide"></i>
            <span>Reordenar Cronograma</span>
          </a>
          <span class="pin-badge">${badges[0] || 'A'}</span>
        </div>
      </div>

      <div class="row g-3 mb-4">
        <div class="col-md-6">
          <label class="wf-label">Programa Vigente</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option>Especialización en Idoneidad Bursátil (Programa 2026-A)</option>
            </select>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Duración Total Estimada</label>
          <input type="text" class="wf-input bg-disabled" value="8 Semanas (40 Horas Cátedra)" disabled>
        </div>
      </div>

      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th style="width: 80px;">Semana</th>
              <th>Unidad Temática</th>
              <th>Dedicación / Duración</th>
              <th>Actividades Clave</th>
              <th class="text-end">Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>Sem 1-2</strong></td>
              <td>Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</td>
              <td>2 Semanas (10 hs)</td>
              <td>Lectura Ley 26.831 • Autoevaluación U1</td>
              <td class="text-end"><span class="wf-badge status-active">Publicada</span></td>
            </tr>
            <tr>
              <td><strong>Sem 3-5</strong></td>
              <td>Unidad 2: Instrumentos de Renta Fija (Bonos y ONs)</td>
              <td>3 Semanas (15 hs)</td>
              <td>Clase HeyGen • Planilla TIR • Streaming OBS</td>
              <td class="text-end"><span class="wf-badge status-active">En Dictado</span></td>
            </tr>
            <tr>
              <td><strong>Sem 6-8</strong></td>
              <td>Unidad 3: Instrumentos de Renta Variable y Derivados</td>
              <td>3 Semanas (15 hs)</td>
              <td>Valuación Acciones • Examen Integrador</td>
              <td class="text-end"><span class="wf-badge status-inactive">Programada</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,

  // CU-24: Modificar cronograma
  'CU-24': (cu, badges) => `
    <div class="wf-card" style="max-width: 960px; margin: 0 auto; background: #FFFFFF;">
      <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Reordenar y Modificar Cronograma</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Arrastre los bloques de unidades para modificar su orden secuencial y ajuste las semanas lectivas.</p>
        </div>
        <span class="wf-badge status-active">Modo Edición Cronograma</span>
      </div>

      <div class="d-flex flex-column gap-3 mb-4">
        <!-- Unidad 1 Drag Box -->
        <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm">
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
            <div>
              <strong style="font-size: 14px; color: #081426;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
              <div class="small text-muted">Contiene 3 materiales, 1 foro y 1 autoevaluación</div>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <label class="wf-label mb-0" style="font-size: 10px;">Semanas:</label>
              <input type="number" class="wf-input" value="2" style="width: 70px; height: 36px; padding: 4px 8px;">
            </div>
          </div>
        </div>

        <!-- Unidad 2 Drag Box -->
        <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm">
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
            </div>
            <div>
              <strong style="font-size: 14px; color: #081426;">Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</strong>
              <div class="small text-muted">Contiene 4 materiales, 1 autoevaluación, 1 clase en vivo y 1 video clon</div>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <label class="wf-label mb-0" style="font-size: 10px;">Semanas:</label>
              <input type="number" class="wf-input" value="3" style="width: 70px; height: 36px; padding: 4px 8px;">
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>

        <!-- Unidad 3 Drag Box -->
        <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm">
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
            </div>
            <div>
              <strong style="font-size: 14px; color: #081426;">Unidad 3: Instrumentos de Renta Variable y Derivados Financieros</strong>
              <div class="small text-muted">Contiene 2 materiales y 1 evaluación final</div>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2">
              <label class="wf-label mb-0" style="font-size: 10px;">Semanas:</label>
              <input type="number" class="wf-input" value="3" style="width: 70px; height: 36px; padding: 4px 8px;">
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
        <a href="#CU-23" class="wf-btn wf-btn-outline">Cancelar</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cronograma</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-77: Buscar clase con clon
  'CU-77': (cu, badges) => `
    <div class="wf-card" style="max-width: 1000px; margin: 0 auto; background: #FFFFFF;">
      <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Clases con Clon de IA (HeyGen)</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Listado y búsqueda de videos generados mediante síntesis de avatar hiperrealista y voz.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <a href="#CU-78" class="wf-btn wf-btn-primary wf-btn-sm d-flex align-items-center gap-1">
            <i class="fa-solid fa-plus"></i>
            <span>Generar Nueva Clase con Clon</span>
          </a>
          <span class="pin-badge">${badges[0] || 'A'}</span>
        </div>
      </div>

      <div class="p-3 bg-light rounded border mb-4">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Unidad Académica</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Todas las unidades (o Unidad 2: Renta Fija)</option>
              </select>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Estado de Render</label>
            <select class="wf-input">
              <option>Todos los estados (Generada, Pendiente, Error)</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Video / Título de la Clase</th>
              <th>Unidad Pertenencia</th>
              <th>Avatar & Voz</th>
              <th>Duración / Render</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <i class="fa-solid fa-circle-play text-primary" style="font-size: 16px;"></i>
                  <strong>Explicación Teórica: Duración Modificada</strong>
                </div>
              </td>
              <td>Unidad 2: Renta Fija</td>
              <td>Fausto Spotorno HD (HeyGen)</td>
              <td>03:40 min • 1080p</td>
              <td><span class="wf-badge status-active">Generada</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-1">
                  <a href="#CU-78" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-play me-1"></i> Previsualizar</a>
                  <span class="pin-badge">${badges[3] || 'D'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <i class="fa-solid fa-circle-play text-primary" style="font-size: 16px;"></i>
                  <strong>Introducción a la Ley de Mercado de Capitales</strong>
                </div>
              </td>
              <td>Unidad 1: Marco Legal</td>
              <td>Fausto Spotorno HD (HeyGen)</td>
              <td>05:12 min • 1080p</td>
              <td><span class="wf-badge status-active">Generada</span></td>
              <td class="text-end">
                <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-play me-1"></i> Previsualizar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,

  // CU-78: Generar clase con clon
  'CU-78': (cu, badges) => `
    <div class="wf-card" style="max-width: 1020px; margin: 0 auto; background: #FFFFFF;">
      <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div class="d-flex align-items-center gap-3">
          <div style="width: 44px; height: 44px; border-radius: 8px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
            <i class="fa-solid fa-wand-magic-sparkles" style="font-size: 20px;"></i>
          </div>
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Generar Clase Audiovisual con Clon IA</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Ingrese el título, seleccione la unidad y redacte el guión que el avatar HeyGen sintetizará.</p>
          </div>
        </div>
        <span class="wf-badge status-active">Clon Activo: Lic. Fausto Spotorno HD</span>
      </div>

      <div class="row g-4">
        <div class="col-md-7">
          <div class="mb-3">
            <label class="wf-label">Título de la Clase / Video</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Explicación Teórica: Duración Modificada y Convexidad en Bonos">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="wf-label">Unidad Académica de Pertenencia</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</option>
              </select>
            </div>
          </div>

          <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <label class="wf-label mb-0">Guión Académico de Locución (Prompt Speech)</label>
              <a href="#CU-74" class="small text-decoration-none d-flex align-items-center gap-1" style="color: #081426; font-weight: 700;">
                <i class="fa-solid fa-sparkles"></i> Autogenerar guión con IA
              </a>
            </div>
            <div class="wf-input-wrap">
              <textarea class="wf-input" rows="6">En esta clase abordaremos el concepto de modified duration. Cuando la tasa de interés se incrementa, el precio de los títulos cae en proporción inversa a su duración ponderada. Analizaremos la aproximación por series de Taylor...</textarea>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>

        <div class="col-md-5">
          <div class="p-3 border rounded bg-white shadow-sm mb-3">
            <div class="small fw-bold text-muted text-uppercase mb-2">Previsualización de Render</div>
            <div style="height: 180px; background: #081426; border-radius: 8px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #1E3A5F;">
              <div style="text-align: center; color: white;">
                <div style="width: 56px; height: 56px; border-radius: 50%; background: #0F233D; margin: 0 auto 6px; display: flex; align-items: center; justify-content: center; font-weight: 700; border: 2px solid var(--wf-gold);">
                  <i class="fa-solid fa-user" style="font-size: 24px; color: #CBD5E1;"></i>
                </div>
                <div style="font-size: 11px; font-weight: 700;">Avatar HeyGen v2.0</div>
                <div style="font-size: 9px; color: #94A3B8;">Lic. Fausto Spotorno</div>
              </div>
              <div style="position: absolute; top: 8px; left: 8px; background: #DC2626; color: white; padding: 2px 6px; border-radius: 4px; font-size: 9px; font-weight: 700; letter-spacing: 0.5px;">
                PREVIEW
              </div>
              <div style="position: absolute; bottom: 8px; right: 8px; font-size: 10px; color: #34D399; background: rgba(0,0,0,0.6); padding: 2px 6px; border-radius: 4px;">
                1080p 60fps
              </div>
            </div>
            <div class="d-flex justify-content-between small text-muted mt-2">
              <span>Tiempo estimado: ~3 min 40s</span>
              <span>Voz: Fausto_ES_AR_v1</span>
            </div>
          </div>

          <div class="d-flex flex-column gap-2">
            <div class="d-flex justify-content-between align-items-center p-2 border rounded bg-light">
              <span class="small text-muted">Voz clonada:</span>
              <span class="small fw-bold">Fausto_ES_AR_v1</span>
            </div>
            <div class="d-flex justify-content-between align-items-center p-2 border rounded bg-light">
              <span class="small text-muted">Escenario / Fondo:</span>
              <span class="small fw-bold">Oficina Virtual FCEQyN</span>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
        <a href="#CU-77" class="wf-btn wf-btn-outline">Cancelar</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2">
            <i class="fa-solid fa-wand-magic-sparkles"></i>
            <span>Sintetizar Video con IA</span>
          </button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `
};

console.log("Generators defined for specialized screens:", Object.keys(specializedScreenGenerators));
