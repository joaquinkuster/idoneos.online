const fs = require('fs');
const path = require('path');

// We will construct specialized form generators for each entity that needs individual field mapping
const specializedFormGenerators = {
  // CU-08: Registrar categoría
  'CU-08': (cu, badges) => `
    <div class="wf-card" style="max-width: 760px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nueva Categoría</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina una nueva categoría temática para la clasificación del catálogo.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Nombre de la Categoría</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" placeholder="Ej: Finanzas Corporativas, Mercado de Capitales...">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción Temática y Alcance</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="4" placeholder="Describa el alcance de los cursos comprendidos en esta categoría..."></textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-07" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Categoría</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-09: Modificar categoría
  'CU-09': (cu, badges) => `
    <div class="wf-card" style="max-width: 760px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Categoría</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice la denominación y alcance de la categoría seleccionada.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Nombre de la Categoría</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Mercado de Capitales & Finanzas">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción Temática y Alcance</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="4">Cursos especializados en instrumentos financieros, renta fija, renta variable, derivados y normativa regulatoria CNV.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-07" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-12: Registrar cohorte
  'CU-12': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nueva Cohorte</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Programa: Especialización en Idoneidad Bursátil (Programa 2026-A)</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Denominación / Código de Cohorte</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Cohorte 2026-1">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-3">
          <label class="wf-label">Inicio de Inscripción</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-01">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
        <div class="col-md-3">
          <label class="wf-label">Fin de Inscripción</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-31">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Cupo Máximo de Alumnos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="30" placeholder="Ilimitado si se deja en blanco">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Semanas de Acceso al Contenido</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="16">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Fecha Inicio de Dictado (Clases en Vivo)</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-04-05">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Fecha Fin de Dictado (Clases en Vivo)</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-06-30">
            <span class="pin-badge">${badges[7] || 'H'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-11" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cohorte</button>
          <span class="pin-badge">${badges[8] || 'I'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-13: Modificar cohorte
  'CU-13': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Cohorte 2026-1</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Ajuste los plazos de inscripción, cupos y semanas lectivas de la cohorte.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Fecha Inicio de Inscripción</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-01">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Fecha Fin de Inscripción</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-31">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Cupo Máximo</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="35">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Semanas de Acceso</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="18">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Fechas de Dictado en Vivo</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="05/04/2026 al 30/06/2026">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-11" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-16: Registrar programa
  'CU-16': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nuevo Programa Académico</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina la estructura curricular, objetivos y bibliografía del programa.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Partir de Programa Anterior (Opcional)</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Crear programa desde cero</option>
              <option>Programa 2025 (Versión Anterior)</option>
            </select>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Nombre del Programa</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Programa Oficial 2026-A">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Descripción Académica y Alcance</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Estructura curricular alineada con el examen de certificación CNV 2026.</textarea>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Objetivos Formativos y Competencias</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Capacitar en valuación de bonos, acciones, derivados y marco normativo bursátil.</textarea>
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Carga Horaria Total (Horas)</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="40">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>

        <div class="col-md-8">
          <label class="wf-label">Bibliografía Obligatoria y Complementaria</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Ley 26.831, Normas CNV TO 2013, Guía IAMC 2026">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-15" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Programa</button>
          <span class="pin-badge">${badges[7] || 'H'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-17: Modificar programa
  'CU-17': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Programa: 2026-A</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice las especificaciones académicas del programa seleccionado.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre del Programa</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Programa Oficial 2026-A">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Carga Horaria Total (Horas)</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="45">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Descripción Académica</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Estructura curricular con actualización de normativa bursátil vigente.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Objetivos Formativos</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Dominio técnico de cálculo financiero, instrumentos bursátiles y régimen sancionatorio.</textarea>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Bibliografía Obligatoria</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Ley 26.831, Normas CNV TO 2013, Manual de Valuación IAMC">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-15" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-20: Agregar unidad
  'CU-20': (cu, badges) => `
    <div class="wf-card" style="max-width: 820px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Agregar Unidad Pedagógica</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Incorpore un nuevo módulo al cronograma académico del programa.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Título de la Sección / Unidad</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Unidad 5: Futuros y Opciones Financieras">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción de Objetivos</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Comprensión de coberturas con contratos de futuros y estrategias de opciones (Calls y Puts).</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Contenido Temático Detallado</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">1. Mercados de derivados (Matba Rofex) • 2. Cobertura vs especulación • 3. Valuación Black-Scholes • 4. Garantías y liquidación diaria.</textarea>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Reutilizar Unidad de Otro Programa (Opcional)</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>-- No reutilizar (Crear nueva unidad) --</option>
              <option>Unidad 3: Derivados (Programa 2025)</option>
            </select>
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-19" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Agregar Unidad</button>
          <span class="pin-badge">${badges[5] || 'F'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-21: Modificar unidad
  'CU-21': (cu, badges) => `
    <div class="wf-card" style="max-width: 820px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Unidad 2: Instrumentos de Renta Fija</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice el título, objetivos y contenidos pedagógicos de la unidad.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Título de la Unidad</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción de Objetivos</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="2">Cálculo de rendimiento (TIR), duration modificada y análisis de riesgo crediticio soberano y corporativo.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Contenido Temático Detallado</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Curvas soberanas en USD • Bonos CER • Obligaciones Negociables Hard Dollar • Medición de riesgo país.</textarea>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-19" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[4] || 'E'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-28: Subir material
  'CU-28': (cu, badges) => `
    <div class="wf-card" style="max-width: 780px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Subir Material de Estudio</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Unidad 2: Instrumentos de Renta Fija</p>
        </div>
        <div>
          <span class="wf-badge status-active">Nuevo Recurso</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Tipo de Material</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Documento PDF / Presentación</option>
              <option>Planilla de Cálculo (Excel / CSV)</option>
              <option>Enlace Externo / Video</option>
            </select>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Título del Recurso</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Guía Práctica: Cálculo de TIR y Duration de Bonos">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Archivo o Documento Adjunto</label>
          <div class="d-flex align-items-center gap-2">
            <div class="wf-input-wrap flex-grow-1">
              <input type="text" class="wf-input" value="guia_practica_renta_fija_2026.pdf">
              <span class="pin-badge">${badges[3] || 'D'}</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-outline"><i class="fa-solid fa-folder-open me-1"></i> Examinar...</button>
              <span class="pin-badge">${badges[4] || 'E'}</span>
            </div>
          </div>
        </div>

        <div class="col-12">
          <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
            <div>
              <strong>Estado de Visibilidad para Alumnos</strong>
              <p class="small text-muted m-0">Permite que el material sea visible inmediatamente o permanezca oculto como borrador.</p>
            </div>
            <div class="d-flex align-items-center gap-2">
              <label class="d-flex align-items-center gap-2 cursor-pointer mb-0">
                <input type="checkbox" checked style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
                <span class="small fw-bold">Visible</span>
              </label>
              <span class="pin-badge">${badges[5] || 'F'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-27" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Agregar</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-29: Modificar material
  'CU-29': (cu, badges) => `
    <div class="wf-card" style="max-width: 780px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Material de Estudio</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice el título, archivo adjunto o visibilidad del recurso.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Título del Recurso</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Guía Práctica: Cálculo de TIR y Duration de Bonos (Revisión 2)">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Archivo Adjunto</label>
          <div class="d-flex align-items-center gap-2">
            <div class="wf-input-wrap flex-grow-1">
              <input type="text" class="wf-input" value="guia_practica_renta_fija_2026_rev2.pdf">
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-outline"><i class="fa-solid fa-folder-open me-1"></i> Examinar...</button>
              <span class="pin-badge">${badges[3] || 'D'}</span>
            </div>
          </div>
        </div>

        <div class="col-12">
          <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
            <div>
              <strong>Estado de Visibilidad</strong>
              <p class="small text-muted m-0">Publicar para los alumnos matriculados en la cohorte.</p>
            </div>
            <div class="d-flex align-items-center gap-2">
              <label class="d-flex align-items-center gap-2 cursor-pointer mb-0">
                <input type="checkbox" checked style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
                <span class="small fw-bold">Visible</span>
              </label>
              <span class="pin-badge">${badges[4] || 'E'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-27" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[5] || 'F'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-32: Registrar término de glosario
  'CU-32': (cu, badges) => `
    <div class="wf-card" style="max-width: 760px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Agregar Término al Glosario</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Unidad 2: Instrumentos de Renta Fija</p>
        </div>
        <div>
          <span class="wf-badge status-active">Nuevo Concepto</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Concepto / Término Técnico</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Duration Modificada (Modified Duration)">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Definición Técnica y Fórmula</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="4">Medida de la sensibilidad del precio de un bono ante variaciones en la tasa de interés de mercado. Expresa el cambio porcentual aproximado en el precio del activo financiero ante un movimiento del 1% en la TIR.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-31" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Agregar</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-33: Modificar término de glosario
  'CU-33': (cu, badges) => `
    <div class="wf-card" style="max-width: 760px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Término de Glosario</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice la definición del concepto financiero.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Concepto / Término</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Duration Modificada">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Definición Técnica</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="4">Medida de la elasticidad precio-rendimiento en instrumentos de renta fija. Se calcula como la Duration de Macaulay dividida por (1 + TIR/m).</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-31" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-36: Registrar consulta de foro
  'CU-36': (cu, badges) => `
    <div class="wf-card" style="max-width: 800px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Nueva Consulta en el Foro</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Foro Unidad 2: Instrumentos de Renta Fija</p>
        </div>
        <div>
          <span class="wf-badge status-active">Publicación de Alumno</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Asunto / Título de la Consulta</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Duda sobre el cálculo de la paridad en bonos bajo la par">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Texto del Mensaje / Consulta</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="5">Buenas tardes profesor, revisando la planilla de cálculo de bonos soberanos me surgió una duda sobre cuándo se considera que un bono cotiza 'bajo la par' y cómo impacta el cupón corrido en el precio clean vs dirty. ¿Podría aclararme este punto?</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-35" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-paper-plane me-1"></i> Publicar Consulta</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-37: Modificar consulta de foro
  'CU-37': (cu, badges) => `
    <div class="wf-card" style="max-width: 800px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Editar Consulta de Foro</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Plazo de edición habilitado (dentro de los primeros 15 minutos)</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Asunto</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Duda sobre el cálculo de la paridad en bonos bajo la par">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Texto del Mensaje</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="5">Buenas tardes profesor, revisando la planilla de bonos soberanos me surgió una duda sobre cuándo cotiza 'bajo la par' y cómo impacta el cupón corrido en el precio clean vs dirty en Bonos GD30. Muchas gracias.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-35" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[3] || 'D'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-40: Registrar respuesta de foro
  'CU-40': (cu, badges) => `
    <div class="wf-card" style="max-width: 800px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Responder Consulta de Foro</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Respuesta oficial por el docente a cargo</p>
        </div>
        <div>
          <span class="wf-badge status-active">Docente Titular</span>
        </div>
      </div>

      <div class="p-3 bg-light rounded border mb-3">
        <div class="small fw-bold text-muted">Consulta de Alumno (Joaquín Küster):</div>
        <p class="small m-0 text-dark">"Duda sobre el cálculo de la paridad en bonos bajo la par..."</p>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Texto de la Respuesta Académica</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="5">Estimado Joaquín: Un bono cotiza bajo la par cuando su precio de mercado es inferior a su valor técnico (paridad menor al 100%). El precio dirty incluye los intereses corridos devengados desde el último pago de cupón, mientras que el clean los descuenta. En la clase práctica del martes veremos la fórmula paso a paso.</textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-39" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-paper-plane me-1"></i> Publicar Respuesta</button>
          <span class="pin-badge">${badges[2] || 'C'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-41: Modificar respuesta de foro
  'CU-41': (cu, badges) => `
    <div class="wf-card" style="max-width: 800px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Respuesta de Foro</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice el texto de la respuesta enviada.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Texto de la Respuesta</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="5">Estimado Joaquín: Un bono cotiza bajo la par cuando su precio es menor al valor técnico. Recordá revisar la planilla Excel subida a la Unidad 2 con el ejemplo del AL30.</textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-39" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[2] || 'C'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-50: Registrar descuento
  'CU-50': (cu, badges) => `
    <div class="wf-card" style="max-width: 840px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Cupón / Beca de Descuento</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Cree promociones comerciales y convenios institucionales de aranceles.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre del Descuento / Beca</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Beca Convenio UNaM 2026">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Porcentaje de Descuento</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="25" min="1" max="100">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Vigencia Desde</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-01">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Vigencia Hasta</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-12-31">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Cantidad Límite de Usos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="100">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Cantidad de Cursos Requeridos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="1" placeholder="0 = Sin requisito previo">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-49" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Descuento</button>
          <span class="pin-badge">${badges[7] || 'H'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-51: Modificar descuento
  'CU-51': (cu, badges) => `
    <div class="wf-card" style="max-width: 840px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Descuento: Convenio UNaM</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice las fechas de vigencia y el porcentaje aplicable.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre del Descuento</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Beca Convenio UNaM 2026">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Porcentaje de Descuento</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="30">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Vigencia Desde</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-03-01">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Vigencia Hasta</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-12-31">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Cantidad Límite</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="150">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Cursos Requeridos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="1">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-49" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[7] || 'H'}</span>
        </div>
      </div>
    </div>
  `,


  // CU-58: Crear autoevaluación
  'CU-58': (cu, badges) => `
    <div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Crear Cuestionario / Autoevaluación</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina los parámetros de evaluación pedagógica y pools vinculados.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Formulario de Alta</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-8">
          <label class="wf-label">Nombre del Cuestionario</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Autoevaluación Unidad 2: Renta Fija y Bonos">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Tiempo Límite (Minutos)</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="30">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Cantidad de Preguntas</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="10">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Fecha de Apertura</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-04-01">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Fecha de Cierre</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-06-30">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Intentos Permitidos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="3" placeholder="Vacío = Ilimitados">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
        <div class="col-md-8">
          <label class="wf-label">Pools de Preguntas Asociados</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Banco U2: Renta Fija (25 preguntas disponibles)</option>
              <option>Banco Integrador General</option>
            </select>
            <span class="pin-badge">${badges[7] || 'H'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-57" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Agregar</button>
          <span class="pin-badge">${badges[8] || 'I'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-59: Modificar autoevaluación
  'CU-59': (cu, badges) => `
    <div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Autoevaluación: Renta Fija</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice las fechas de vigencia y parámetros del cuestionario.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-8">
          <label class="wf-label">Nombre del Cuestionario</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Autoevaluación Unidad 2: Renta Fija">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Tiempo Límite (Minutos)</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="40">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Cantidad de Preguntas</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="12">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Fecha de Apertura</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-04-01">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Fecha de Cierre</label>
          <div class="wf-input-wrap">
            <input type="date" class="wf-input" value="2026-07-15">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Intentos Permitidos</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="4">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
        <div class="col-md-8">
          <label class="wf-label">Pools Asociados</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Banco U2: Renta Fija (25 preguntas)</option>
            </select>
            <span class="pin-badge">${badges[7] || 'H'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-57" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[8] || 'I'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-66: Programar clase en vivo
  'CU-66': (cu, badges) => `
    <div class="wf-card" style="max-width: 840px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Programar Clase en Vivo</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina la sesión sincrónica de videoconferencia para la cohorte.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Nueva Sesión</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-8">
          <label class="wf-label">Título de la Clase</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Taller en Vivo: Análisis de Curvas y Duration en Tiempo Real">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Cohorte Destinataria</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Cohorte 2026-1</option>
              <option>Cohorte 2026-2</option>
            </select>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Fecha y Hora de Inicio</label>
          <div class="wf-input-wrap">
            <input type="datetime-local" class="wf-input" value="2026-04-15T19:00">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Duración Estimada (Minutos)</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="90">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Enlace a Sala de Videoconferencia</label>
          <div class="wf-input-wrap">
            <input type="url" class="wf-input" value="https://meet.google.com/ido-bour-cnv">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-65" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-video me-1"></i> Programar Clase</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-67: Modificar clase en vivo
  'CU-67': (cu, badges) => `
    <div class="wf-card" style="max-width: 840px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Reprogramar Clase en Vivo</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice el horario o la sala de transmisión sincrónica.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Título de la Clase</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Taller en Vivo: Análisis de Curvas y Duration en Tiempo Real">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Nueva Fecha y Hora</label>
          <div class="wf-input-wrap">
            <input type="datetime-local" class="wf-input" value="2026-04-18T19:30">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Duración Estimada</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="100">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Enlace de Transmisión</label>
          <div class="wf-input-wrap">
            <input type="url" class="wf-input" value="https://meet.google.com/ido-bour-cnv">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-65" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[5] || 'F'}</span>
        </div>
      </div>
    </div>
  `,



  // CU-81: Registrarse
  'CU-81': (cu, badges) => `
    <div class="wf-card" style="max-width: 620px; margin: 30px auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom text-center">
        <h3 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Crear Cuenta de Usuario</h3>
        <p class="small text-muted" style="margin: 4px 0 0;">Regístrese en Idóneos Online para acceder a la plataforma y cursos.</p>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Joaquín">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Apellido</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Küster">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-7">
          <label class="wf-label">Correo Electrónico</label>
          <div class="wf-input-wrap">
            <input type="email" class="wf-input" value="joaquin.kuster@gmail.com">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-5">
          <label class="wf-label">DNI / Documento</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="42.890.123">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Contraseña Segura</label>
          <div class="wf-input-wrap">
            <input type="password" class="wf-input" value="••••••••••••">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-90" class="wf-btn wf-btn-outline">Ya tengo cuenta (Login)</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-user-plus me-1"></i> Crear Cuenta</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-83: Registrar usuario
  'CU-83': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Usuario en el Sistema</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Alta manual de cuentas de alumnos, docentes o administradores.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Alta de Usuario</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Elena">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Apellido</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Valenzuela">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Correo Electrónico Institucional</label>
          <div class="wf-input-wrap">
            <input type="email" class="wf-input" value="elena.valenzuela@idoneos.online">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">DNI / Identificación</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="35.456.789">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Contraseña Temporal</label>
          <div class="wf-input-wrap">
            <input type="password" class="wf-input" value="••••••••••••">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Teléfono de Contacto</label>
          <div class="wf-input-wrap">
            <input type="tel" class="wf-input" value="+54 376 4123456">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Rol Asignado en el Sistema</label>
          <div class="wf-input-wrap">
            <select class="wf-input">
              <option selected>Docente</option>
              <option>Alumno</option>
              <option>Administrador</option>
            </select>
            <span class="pin-badge">${badges[7] || 'H'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-82" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Usuario</button>
          <span class="pin-badge">${badges[8] || 'I'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-84: Modificar usuario
  'CU-84': (cu, badges) => `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Cuenta de Usuario</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice los datos personales y de acceso del usuario seleccionado.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Elena">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Apellido</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Valenzuela">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Correo Electrónico</label>
          <div class="wf-input-wrap">
            <input type="email" class="wf-input" value="elena.valenzuela@idoneos.online">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">DNI</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="35.456.789">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Teléfono de Contacto</label>
          <div class="wf-input-wrap">
            <input type="tel" class="wf-input" value="+54 376 4123456">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Foto de Perfil / Avatar</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="avatar_elena_valenzuela.png">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-82" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[7] || 'H'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-88: Registrar docente
  'CU-88': (cu, badges) => `
    <div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nuevo Perfil Docente</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Cargue los antecedentes académicos, títulos y matrícula profesional CNV.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Alta de Docente</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre y Apellido</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Dr. Roberto Cachanosky">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Correo Institucional</label>
          <div class="wf-input-wrap">
            <input type="email" class="wf-input" value="roberto.cachanosky@idoneos.online">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">DNI y Teléfono</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="DNI: 14.567.890 • Tel: +54 11 4321-9876">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Años de Experiencia en Mercado</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="30">
            <span class="pin-badge">${badges[5] || 'F'}</span>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Biografía Profesional y Trayectoria</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Licenciado en Economía por la Universidad Católica Argentina. Consultor económico y financiero, columnista en medios nacionales y docente de posgrado.</textarea>
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Títulos Universitarios y Posgrados</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Lic. en Economía (UCA), Doctor en Economía">
            <span class="pin-badge">${badges[6] || 'G'}</span>
          </div>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Matrícula CNV / Registro Profesional</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Matrícula CNV Idóneo #1042">
            <span class="pin-badge">${badges[7] || 'H'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-87" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Docente</button>
          <span class="pin-badge">${badges[8] || 'I'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-89: Modificar docente
  'CU-89': (cu, badges) => `
    <div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Perfil: Lic. Fausto Spotorno</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice los antecedentes académicos y estado de habilitación docente.</p>
        </div>
        <div>
          <span class="wf-badge status-active">Modo Edición</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Biografía Profesional</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Economista Jefe de Orlando J. Ferreres & Asociados. Director del Centro de Estudios Económicos de la UADE y especialista en Mercado de Capitales.</textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Años de Experiencia</label>
          <div class="wf-input-wrap">
            <input type="number" class="wf-input" value="22">
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Títulos Universitarios</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Lic. en Economía (UADE), Máster en Finanzas (UCEMA)">
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Matrícula CNV</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="CNV #5821">
            <span class="pin-badge">${badges[4] || 'E'}</span>
          </div>
        </div>

        <div class="col-12">
          <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
            <div>
              <strong>Estado de Habilitación Académica</strong>
              <p class="small text-muted m-0">Permite al docente dictar cohortes y corregir actividades.</p>
            </div>
            <div class="d-flex align-items-center gap-2">
              <label class="d-flex align-items-center gap-2 cursor-pointer mb-0">
                <input type="checkbox" checked style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
                <span class="small fw-bold">Habilitado para Dictado</span>
              </label>
              <span class="pin-badge">${badges[5] || 'F'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a href="#CU-87" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
          <span class="pin-badge">${badges[6] || 'G'}</span>
        </div>
      </div>
    </div>
  `,

  // CU-99: Configurar parámetros
  'CU-99': (cu, badges) => `
    <div class="wf-card" style="max-width: 960px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Configuración de Parámetros Generales del Sistema</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Ajuste de variables operativas, límites de IA y constantes institucionales.</p>
        </div>
        <span class="wf-badge status-active">Parámetros Globales</span>
      </div>

      <!-- Tabla de Configuración (Badge A) -->
      <div class="wf-table-wrap mb-4">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Parámetro / Variable</th>
              <th>Descripción y Alcance</th>
              <th>Valor Configurado</th>
              <th class="text-end">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr class="table-active" style="background: #FEF3C7; border-left: 3px solid var(--wf-gold);">
              <td><strong>LIMITE_SESIONES_CONCURRENTES</strong></td>
              <td>Cantidad máxima de sesiones simultáneas por usuario</td>
              <td><code>2 sesiones</code></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-primary"><i class="fa-solid fa-pen-to-square me-1"></i> Editar Valor</button>
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td><strong>PLAZO_EDICION_FORO_MIN</strong></td>
              <td>Tiempo límite de edición para consultas y respuestas de foros</td>
              <td><code>15 minutos</code></td>
              <td class="text-end">
                <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar Valor</button>
              </td>
            </tr>
            <tr>
              <td><strong>HEYGEN_MAX_VIDEOS_DOCENTE_SEMANAL</strong></td>
              <td>Cantidad máxima semanal de videos sintéticos generables por docente</td>
              <td><code>10 videos / semana</code></td>
              <td class="text-end">
                <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar Valor</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Formulario de Edición del Parámetro Seleccionado (Badges C y D) -->
      <div class="p-4 bg-light rounded border">
        <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin-bottom: 12px;">Editar Parámetro: LIMITE_SESIONES_CONCURRENTES</h4>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="wf-label">Nuevo Valor Numérico</label>
            <div class="wf-input-wrap">
              <input type="number" class="wf-input" value="2" min="1" max="10">
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Impacto Operativo</label>
            <input type="text" class="wf-input bg-disabled" value="Cierra automáticamente la sesión más antigua si excede el límite" disabled>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Parámetro</button>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
      </div>
    </div>
  `
};

module.exports = specializedFormGenerators;
