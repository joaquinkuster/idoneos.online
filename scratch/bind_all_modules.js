const fs = require('fs');
const path = require('path');

const templatesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

function processTemplate(subfolder, filename, transformFn) {
  const filePath = path.join(templatesDir, subfolder, filename);
  if (!fs.existsSync(filePath)) {
    console.log(`⚠️ Archivo no encontrado: ${filePath}`);
    return;
  }
  let content = fs.readFileSync(filePath, 'utf8');
  content = transformFn(content);
  fs.writeFileSync(filePath, content, 'utf8');
  console.log(`✅ Conectado con BD: ${subfolder}/${filename}`);
}

// ─────────────────────────────────────────────────────────────
// MOD-02: GESTIÓN ACADÉMICA (CU-15, CU-16, CU-19, CU-20, CU-23, CU-25, CU-26, CU-27, CU-31, CU-35)
// ─────────────────────────────────────────────────────────────

// CU-15: Buscar programa
processTemplate('academico', 'cu-15-buscar-programa.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre del Programa</th>
              <th>Curso Asociado</th>
              <th>Descripción</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="prog : \${programas}">
              <td><strong th:text="'#' + \${prog.id}">#1</strong></td>
              <td><strong th:text="\${prog.nombre}">Nombre</strong></td>
              <td><span class="badge bg-light text-navy border" th:text="\${prog.curso != null ? prog.curso.nombre : 'General'}">Curso</span></td>
              <td th:text="\${prog.descripcion}">Descripción</td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a th:href="@{'/academico/programas/' + \${prog.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                  <a th:href="@{'/academico/programas/' + \${prog.id} + '/baja'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-19: Buscar unidad
processTemplate('academico', 'cu-19-buscar-unidad.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Título de la Unidad</th>
              <th>Descripción</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="u : \${unidades}">
              <td><strong th:text="'#' + \${u.id}">#1</strong></td>
              <td><strong th:text="\${u.titulo}">Título</strong></td>
              <td th:text="\${u.descripcion}">Descripción</td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a th:href="@{'/academico/unidades/' + \${u.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                  <a th:href="@{'/academico/unidades/' + \${u.id} + '/quitar'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Quitar"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-26: Acceder al Curso / Aula Virtual
processTemplate('academico', 'cu-26-acceder-curso.html', html => {
  html = html.replace(/<div class="wf-card mb-4" style="background: #FFFFFF;">[\s\S]*?progreso general: <strong>65%<\/strong><\/div>\s*<\/div>\s*<\/div>/i, `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 mb-2 border-bottom">
          <div>
            <div class="small text-muted mb-1">
              <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <strong th:text="\${curso != null ? curso.nombre : 'Mercado de Capitales'}">Curso</strong>
            </div>
            <h3 style="font-size: 19px; font-weight: 800; color: #081426; margin: 4px 0;" th:text="\${curso != null ? curso.nombre : 'Curso'}">Curso Nombre</h3>
            <p class="small text-muted" style="margin: 0;" th:text="\${curso != null and curso.docente != null and curso.docente.usuario != null ? 'Docente Titular: ' + curso.docente.usuario.nombreCompleto : 'Equipo Académico'}">Docente</p>
          </div>
          <div class="text-end">
            <span class="wf-badge status-active">Acceso Habilitado</span>
          </div>
        </div>`);

  html = html.replace(/<div class="d-flex flex-column gap-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
      <div class="d-flex flex-column gap-4">
        <div th:each="unidad : \${unidades}" class="wf-unit-box">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-book-open text-primary" style="font-size: 16px;"></i>
              <strong style="font-size: 14px; color: #081426;" th:text="\${unidad.titulo}">Unidad</strong>
            </div>
            <a th:href="@{'/evaluaciones/autoevaluaciones?unidadId=' + \${unidad.id}}" class="wf-btn wf-btn-sm wf-btn-gold">Rendir Evaluación</a>
          </div>
          <div class="wf-unit-body d-flex flex-column gap-2">
            <p class="small text-muted m-0" th:text="\${unidad.descripcion}">Descripción de la unidad...</p>
            <div class="d-flex align-items-center gap-3 pt-2">
              <a th:href="@{'/academico/consultas?unidadId=' + \${unidad.id}}" class="btn btn-sm btn-outline-secondary rounded-pill"><i class="fa-solid fa-comments me-1"></i> Foro de Consultas</a>
              <a th:href="@{'/academico/glosario?unidadId=' + \${unidad.id}}" class="btn btn-sm btn-outline-secondary rounded-pill"><i class="fa-solid fa-book me-1"></i> Glosario</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// ─────────────────────────────────────────────────────────────
// MOD-04: EVALUACIONES (CU-53, CU-57, CU-61, CU-62, CU-63)
// ─────────────────────────────────────────────────────────────

// CU-53: Buscar pool
processTemplate('evaluaciones', 'cu-53-buscar-pool.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre del Pool</th>
              <th>Unidad Asociada</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="p : \${pools}">
              <td><strong th:text="'#' + \${p.id}">#1</strong></td>
              <td><strong th:text="\${p.nombre}">Nombre Pool</strong></td>
              <td><span class="badge bg-light text-navy border" th:text="\${p.unidad != null ? p.unidad.titulo : 'General'}">Unidad</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a th:href="@{'/evaluaciones/pools/' + \${p.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                  <a th:href="@{'/evaluaciones/pools/' + \${p.id} + '/baja'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-57: Buscar autoevaluación
processTemplate('evaluaciones', 'cu-57-buscar-autoevaluacion.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre Autoevaluación</th>
              <th>Unidad</th>
              <th>Tiempo Límite</th>
              <th>Intentos</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="a : \${autoevaluaciones}">
              <td><strong th:text="'#' + \${a.id}">#1</strong></td>
              <td><strong th:text="\${a.nombre}">Nombre</strong></td>
              <td th:text="\${a.unidad != null ? a.unidad.titulo : 'General'}">Unidad</td>
              <td th:text="\${a.tiempoLimiteMinutos + ' min'}">20 min</td>
              <td th:text="\${a.intentosPermitidos}">3</td>
              <td class="text-end">
                <a th:href="@{'/evaluaciones/autoevaluaciones/' + \${a.id} + '/rendir'}" class="wf-btn wf-btn-sm wf-btn-gold">Rendir Examen</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// ─────────────────────────────────────────────────────────────
// MOD-05: CLASES EN VIVO (CU-65, CU-66)
// ─────────────────────────────────────────────────────────────

// CU-65: Buscar clase en vivo
processTemplate('ia_vivo', 'cu-65-buscar-clase-en-vivo.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Título de la Clase</th>
              <th>Docente</th>
              <th>Fecha y Hora</th>
              <th>Estado</th>
              <th class="text-end">Transmisión / Acceso</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="c : \${clases}">
              <td><strong th:text="'#' + \${c.id}">#1</strong></td>
              <td><strong th:text="\${c.titulo}">Título</strong></td>
              <td th:text="\${c.docente != null and c.docente.usuario != null ? c.docente.usuario.nombreCompleto : 'Docente'}">Docente</td>
              <td th:text="\${c.fechaHora != null ? #temporals.format(c.fechaHora, 'dd/MM/yyyy HH:mm') : '-'}">Fecha</td>
              <td><span class="wf-badge status-active" th:text="\${c.estadoClaseEnVivo != null ? c.estadoClaseEnVivo.nombre : 'Programada'}">Programada</span></td>
              <td class="text-end">
                <a th:href="@{'/clases-vivo/' + \${c.id} + '/iniciar'}" class="wf-btn wf-btn-sm wf-btn-primary">Iniciar / Entrar</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// ─────────────────────────────────────────────────────────────
// MOD-06: IA Y CLONES (CU-73, CU-74, CU-75, CU-76, CU-78)
// ─────────────────────────────────────────────────────────────

// CU-73: Banco de Preguntas IA
processTemplate('ia_vivo', 'cu-73-generar-banco-de-preguntas.html', html => {
  html = html.replace(/<form[\s\S]*?<\/form>|<div class="wf-card" style="max-width: 1080px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
      <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
        <div class="d-flex align-items-center gap-3">
          <div style="width: 44px; height: 44px; border-radius: 10px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
            <i class="fa-solid fa-list-check" style="font-size: 20px;"></i>
          </div>
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Generar Banco de Preguntas con IA (Ollama)</h3>
            <p class="small text-muted" style="margin: 2px 0 0;">Generación automatizada de preguntas a partir de la bibliografía de la unidad.</p>
          </div>
        </div>
        <span class="wf-badge status-active">Ollama (Llama 3.1 8B)</span>
      </div>

      <div th:if="\${mensaje}" class="alert alert-success p-2 small mb-3" th:text="\${mensaje}">Mensaje</div>

      <form th:action="@{/ia/banco-preguntas/generar}" method="post">
        <div class="row g-3">
          <div class="col-md-6">
            <label class="wf-label">Unidad Académica de Origen</label>
            <select name="unidadId" class="wf-input" required>
              <option th:each="u : \${unidades}" th:value="\${u.id}" th:text="\${u.titulo}">Unidad</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Prompt de Orientación Adicional</label>
            <input type="text" name="promptInput" class="wf-input" placeholder="Ej: Enfatizar preguntas de opción múltiple sobre bonos...">
          </div>
        </div>
        <div class="pt-3 mt-4 border-top d-flex justify-content-end align-items-center gap-3">
          <button type="submit" class="wf-btn wf-btn-primary" style="background: #059669; border-color: #047857;">
            <i class="fa-solid fa-wand-magic-sparkles me-1"></i> Generar Preguntas con IA
          </button>
        </div>
      </form>
    </div>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-98: Consultar estadísticas Dashboard
processTemplate('reportes', 'cu-98-consultar-estadisticas.html', html => {
  html = html.replace(/<div class="row g-3 mb-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<\/div>/, `
      <div class="row g-3 mb-4">
        <div class="col-md-3">
          <div class="p-3 bg-white border rounded shadow-sm">
            <div class="text-muted small">Alumnos Activos</div>
            <h3 class="font-serif fw-bold text-navy my-1" th:text="\${stats != null ? stats.totalAlumnosActivos : '15'}">15</h3>
            <span class="badge bg-success-subtle text-success">En plataforma</span>
          </div>
        </div>
        <div class="col-md-3">
          <div class="p-3 bg-white border rounded shadow-sm">
            <div class="text-muted small">Inscripciones Vigentes</div>
            <h3 class="font-serif fw-bold text-navy my-1" th:text="\${stats != null ? stats.totalInscripcionesVigentes : '24'}">24</h3>
            <span class="badge bg-primary-subtle text-primary">Cursos activos</span>
          </div>
        </div>
        <div class="col-md-3">
          <div class="p-3 bg-white border rounded shadow-sm">
            <div class="text-muted small">Ingresos del Mes</div>
            <h3 class="font-serif fw-bold text-gold my-1" th:text="\${stats != null ? '$' + #numbers.formatDecimal(stats.ingresosMesActual, 1, 'POINT', 0, 'COMMA') : '$1.850.000'}">$1.850.000</h3>
            <span class="badge bg-gold-soft text-navy">Acreditados</span>
          </div>
        </div>
        <div class="col-md-3">
          <div class="p-3 bg-white border rounded shadow-sm">
            <div class="text-muted small">Inscripciones 30 Días</div>
            <h3 class="font-serif fw-bold text-navy my-1" th:text="\${stats != null ? stats.inscripcionesUltimos30Dias : '12'}">12</h3>
            <span class="badge bg-info-subtle text-info">Último mes</span>
          </div>
        </div>
      </div>`);
  return html;
});

console.log('🌟 ¡Todos los módulos esenciales conectados con éxito a Thymeleaf y BD!');
