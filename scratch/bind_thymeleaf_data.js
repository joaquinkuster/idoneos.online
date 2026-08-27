const fs = require('fs');
const path = require('path');

const templatesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

console.log('🚀 Iniciando cableado dinámico de Thymeleaf a la Base de Datos...');

// Helper para leer y escribir
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
// 1. MOD-01: CURSOS (CU-01, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14)
// ─────────────────────────────────────────────────────────────

// CU-01: Buscar curso
processTemplate('cursos', 'cu-01-buscar-curso.html', html => {
  // Conectar formulario de búsqueda
  html = html.replace(/<div class="wf-card mb-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/, `
      <form th:action="@{/cursos}" method="get" class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar cursos por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" name="busqueda" th:value="\${busqueda}" class="wf-input" placeholder="Ej: Planificación Fiscal, Cripto, Mercado de Capitales...">
              <span class="pin-badge">A</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Categoría</label>
            <select name="categoriaId" class="wf-input">
              <option value="">Todas las categorías</option>
              <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}" th:selected="\${categoriaSeleccionada == cat.id}">Categoría</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button type="submit" class="wf-btn wf-btn-primary w-100">Buscar Cursos</button>
              <span class="pin-badge">C</span>
            </div>
          </div>
        </div>
      </form>`);

  // Conectar Grid dinámico de cursos
  html = html.replace(/<div class="wf-cards-grid">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
      <div class="wf-cards-grid">
        <div th:each="curso : \${cursos}" class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon"><i class="fa-solid fa-graduation-cap w-8 h-8 text-white"></i></div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag" th:text="\${curso.categoria != null ? curso.categoria.nombre : 'Finanzas'}">Categoría</span>
              <span class="wf-pill-status" th:text="\${curso.nivel != null ? curso.nivel.nombre : 'General'}">Nivel</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title" th:text="\${curso.nombre}">Nombre Curso</h4>
            <p class="wf-course-desc" th:text="\${curso.descripcion}">Descripción</p>
            <div class="wf-course-info-row">
              <span>📄 <strong th:text="\${curso.docente != null and curso.docente.usuario != null ? curso.docente.usuario.nombreCompleto : 'Equipo Académico'}">Docente</strong></span>
              <span class="wf-course-price" th:text="\${curso.precio <= 0 ? 'GRATIS' : '$' + #numbers.formatDecimal(curso.precio, 1, 'POINT', 0, 'COMMA')}">$0</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            <div class="d-flex flex-column gap-2 w-100">
              <a th:href="@{'/academico/aula?cursoId=' + \${curso.id}}" class="wf-btn wf-btn-sm wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; height: 38px;">
                <i class="fa-solid fa-arrow-right-to-bracket"></i>
                <span>Ingresar al Curso</span>
              </a>
              <div class="d-flex align-items-center gap-2 w-100">
                <a th:href="@{/evaluaciones/calificaciones}" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1" style="font-weight: 600; color: #081426;">
                  <i class="fa-solid fa-chart-simple text-primary"></i>
                  <span>Calificaciones</span>
                </a>
                <a th:href="@{'/cursos/' + \${curso.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1">
                  <i class="fa-solid fa-pen-to-square"></i>
                  <span>Editar</span>
                </a>
                <a th:href="@{'/cursos/' + \${curso.id} + '/baja'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger flex-grow-1 d-flex align-items-center justify-content-center gap-1" title="Dar de baja curso">
                  <i class="fa-solid fa-trash me-1"></i>
                  <span>Baja</span>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-06: Catálogo de Cursos
processTemplate('cursos', 'cu-06-explorar-catalogo-de-cursos.html', html => {
  // Buscador y filtros
  html = html.replace(/<div class="wf-card mb-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/, `
      <form th:action="@{/cursos/catalogo}" method="get" class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar cursos por temática o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" name="busqueda" th:value="\${busqueda}" class="wf-input" placeholder="Ej: Bonos, Futuros, Renta Fija, Criptomonedas...">
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Categoría</label>
            <select name="categoriaId" class="wf-input">
              <option value="">Todas las categorías</option>
              <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}" th:selected="\${categoriaSeleccionada == cat.id}">Categoría</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button type="submit" class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Explorar Catálogo</button>
            </div>
          </div>
        </div>
      </form>`);

  // Lista dinámica de cursos y detalle
  html = html.replace(/<div class="row g-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
      <div class="row g-4">
        <div class="col-lg-7" style="border-right: 2px solid #E2E8F0; padding-right: 24px;">
          <div class="d-flex flex-column gap-3">
            <div th:each="curso : \${cursos}" class="wf-course-card border-primary mb-2" style="border: 2px solid var(--wf-gold);">
              <div class="wf-course-card-thumb">
                <div class="wf-course-thumb-icon"><i class="fa-solid fa-graduation-cap w-8 h-8 text-white"></i></div>
                <div class="wf-course-pills-row">
                  <span class="wf-pill-tag" th:text="\${curso.categoria != null ? curso.categoria.nombre : 'Mercado de Capitales'}">Categoría</span>
                  <span class="wf-pill-status" style="background: #ECFDF5; color: #047857; font-weight: 700;">● Inscripción Abierta</span>
                </div>
              </div>
              <div class="wf-course-card-body">
                <h4 class="wf-course-title" th:text="\${curso.nombre}">Nombre Curso</h4>
                <p class="wf-course-desc" th:text="\${curso.descripcion}">Descripción</p>
                <div class="wf-course-info-row">
                  <span>👨‍🏫 <strong th:text="\${curso.docente != null and curso.docente.usuario != null ? curso.docente.usuario.nombreCompleto : 'Docente'}">Docente</strong></span>
                  <span class="wf-course-price" th:text="\${curso.precio <= 0 ? 'GRATIS' : '$' + #numbers.formatDecimal(curso.precio, 1, 'POINT', 0, 'COMMA')}">$0</span>
                </div>
              </div>
              <div class="wf-course-card-footer">
                <a th:href="@{'/inscripciones/nueva?cursoId=' + \${curso.id}}" class="wf-btn wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="text-decoration: none;">
                  <i class="fa-solid fa-ticket"></i>
                  <span>Inscribirme a este Curso</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-5" style="padding-left: 20px;">
          <div class="wf-card shadow-sm" style="background: #FFFFFF; border: 1px solid #CBD5E1; border-radius: 10px; position: sticky; top: 20px;">
            <div class="pb-3 mb-3 border-bottom d-flex justify-content-between align-items-center">
              <div>
                <span class="wf-badge status-active mb-1">Ficha Informativa</span>
                <h3 style="font-size: 17px; font-weight: 800; color: #081426; margin: 0;">Metodología Idóneos</h3>
              </div>
            </div>
            <div class="p-3 bg-light rounded mb-3" style="font-size: 12px;">
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted">Modalidades:</span>
                <strong>En Vivo, Grabada y Clon IA</strong>
              </div>
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted">Certificación:</span>
                <strong class="text-success"><i class="fa-solid fa-circle-check me-1"></i> Oficial con Código QR</strong>
              </div>
            </div>
            <div class="p-3 border rounded" style="background: #F8FAFC; font-size: 12px;">
              <div class="d-flex align-items-center gap-2 text-muted mb-3">
                <i class="fa-solid fa-chalkboard-user"></i>
                <span>Cuerpo Docente de Directores y Especialistas</span>
              </div>
              <a th:href="@{/seguridad/registro}" class="wf-btn wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="font-weight: 700; text-decoration: none;">
                <i class="fa-solid fa-user-plus"></i>
                <span>Crear Cuenta Gratuita</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-03: Registrar curso Form
processTemplate('cursos', 'cu-03-registrar-curso.html', html => {
  html = html.replace(/<div class="wf-card" style="max-width: 860px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{/cursos/guardar}" method="post" class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nuevo Curso</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina los parámetros académicos, docentes y comerciales del programa de capacitación.</p>
        </div>
        <span class="wf-badge status-active">Formulario de Alta</span>
      </div>

      <div class="row g-3">
        <div class="col-md-8">
          <label class="wf-label">Nombre del Curso</label>
          <input type="text" name="nombre" class="wf-input" required placeholder="Nombre del curso">
        </div>
        <div class="col-md-4">
          <label class="wf-label">Categoría Temática</label>
          <select name="categoriaId" class="wf-input" required>
            <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}">Categoría</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Nivel de Dificultad</label>
          <select name="nivelId" class="wf-input">
            <option th:each="niv : \${niveles}" th:value="\${niv.id}" th:text="\${niv.nombre}">Nivel</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Precio / Arancel (ARS)</label>
          <input type="number" name="precio" class="wf-input" value="0" min="0" required>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Docente Titular Responsable</label>
          <select name="docenteTitularId" class="wf-input" required>
            <option th:each="doc : \${docentes}" th:value="\${doc.id}" th:text="\${doc.usuario != null ? doc.usuario.nombreCompleto : 'Docente'}">Docente</option>
          </select>
        </div>
        <div class="col-12">
          <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
            <div>
              <strong>Emisión Automática de Certificado Oficial</strong>
              <p class="small text-muted m-0">Generar credencial digital al aprobar el 100% de autoevaluaciones.</p>
            </div>
            <input type="checkbox" name="emiteCertificado" value="true" checked style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción Académica y Objetivos</label>
          <textarea name="descripcion" class="wf-input" rows="3" required placeholder="Descripción del curso..."></textarea>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a th:href="@{/cursos}" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Curso</button>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-04: Modificar curso Form
processTemplate('cursos', 'cu-04-modificar-curso.html', html => {
  html = html.replace(/<div class="wf-card" style="max-width: 860px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{'/cursos/' + \${curso.id} + '/editar'}" method="post" class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Curso: <span th:text="\${curso.nombre}">Curso</span></h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Actualice los parámetros del curso en la base de datos.</p>
        </div>
        <span class="wf-badge status-active">Modo Edición</span>
      </div>

      <div class="row g-3">
        <div class="col-md-8">
          <label class="wf-label">Nombre del Curso</label>
          <input type="text" name="nombre" th:value="\${curso.nombre}" class="wf-input" required>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Categoría Temática</label>
          <select name="categoriaId" class="wf-input" required>
            <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}" th:selected="\${curso.categoria != null and curso.categoria.id == cat.id}">Categoría</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Nivel de Dificultad</label>
          <select name="nivelId" class="wf-input">
            <option th:each="niv : \${niveles}" th:value="\${niv.id}" th:text="\${niv.nombre}" th:selected="\${curso.nivel != null and curso.nivel.id == niv.id}">Nivel</option>
          </select>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Precio / Arancel (ARS)</label>
          <input type="number" name="precio" th:value="\${curso.precio}" class="wf-input" min="0" required>
        </div>
        <div class="col-md-4">
          <label class="wf-label">Docente Titular Responsable</label>
          <select name="docenteTitularId" class="wf-input" required>
            <option th:each="doc : \${docentes}" th:value="\${doc.id}" th:text="\${doc.usuario != null ? doc.usuario.nombreCompleto : 'Docente'}" th:selected="\${curso.docente != null and curso.docente.id == doc.id}">Docente</option>
          </select>
        </div>
        <div class="col-12">
          <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
            <div>
              <strong>Emisión Automática de Certificado Oficial</strong>
              <p class="small text-muted m-0">Generar credencial digital al aprobar el 100% de autoevaluaciones.</p>
            </div>
            <input type="checkbox" name="emiteCertificado" value="true" th:checked="\${curso.emiteCertificado}" style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
          </div>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción Académica y Objetivos</label>
          <textarea name="descripcion" th:text="\${curso.descripcion}" class="wf-input" rows="3" required></textarea>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a th:href="@{/cursos}" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-05: Dar de baja curso
processTemplate('cursos', 'cu-05-dar-de-baja-curso.html', html => {
  html = html.replace(/<div class="wf-modal-dialog shadow-sm"[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{'/cursos/' + \${curso.id} + '/baja'}" method="post" class="wf-modal-dialog shadow-sm" style="max-width: 620px; margin: 40px auto; background: #FFFFFF; border-radius: 12px; border: 1px solid #E2E8F0; overflow: hidden;">
      <div class="p-3 border-bottom d-flex justify-content-between align-items-center" style="background: #F8FAFC;">
        <div class="d-flex align-items-center gap-2">
          <i class="fa-solid fa-triangle-exclamation text-danger"></i>
          <strong style="font-size: 14px; color: #081426;">Dar de baja curso</strong>
        </div>
        <span class="wf-badge status-active">Confirmación Requerida</span>
      </div>

      <div class="p-4 text-center">
        <div class="mb-3" style="width: 56px; height: 56px; border-radius: 50%; background: #FEE2E2; color: #DC2626; display: flex; align-items: center; justify-content: center; margin: 0 auto; font-size: 24px;">
          <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin-bottom: 6px;">¿Confirma la baja del curso?</h3>
        <p class="small text-muted mb-4" th:text="\${curso.nombre}">Curso Nombre</p>

        <div class="p-3 mb-4 bg-light border rounded text-start" style="font-size: 13px;">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="text-muted">Identificador:</span>
            <strong style="color: #081426;" th:text="'ID: ' + \${curso.id}">ID: 1</strong>
          </div>
          <div class="d-flex justify-content-between align-items-center">
            <span class="text-muted">Estado actual:</span>
            <span class="wf-badge status-active">Activo</span>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a th:href="@{/cursos}" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
          <button type="submit" class="wf-btn wf-btn-danger"><i class="fa-solid fa-trash me-1"></i> Confirmar Baja</button>
        </div>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-07: Buscar categoría
processTemplate('cursos', 'cu-07-buscar-categoria.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre de Categoría</th>
              <th>Descripción</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="cat : \${categorias}">
              <td><strong th:text="'#' + \${cat.id}">#1</strong></td>
              <td><strong th:text="\${cat.nombre}">Nombre</strong></td>
              <td th:text="\${cat.descripcion}">Descripción</td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a th:href="@{'/cursos/categorias/' + \${cat.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                  <a th:href="@{'/cursos/categorias/' + \${cat.id} + '/baja'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-08: Registrar categoría
processTemplate('cursos', 'cu-08-registrar-categoria.html', html => {
  html = html.replace(/<div class="wf-card" style="max-width: 760px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{/cursos/categorias/guardar}" method="post" class="wf-card" style="max-width: 760px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nueva Categoría</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Defina una nueva categoría temática para la clasificación del catálogo.</p>
        </div>
        <span class="wf-badge status-active">Formulario de Alta</span>
      </div>

      <div class="row g-3">
        <div class="col-12">
          <label class="wf-label">Nombre de la Categoría</label>
          <input type="text" name="nombre" class="wf-input" placeholder="Ej: Finanzas Corporativas, Mercado de Capitales..." required>
        </div>
        <div class="col-12">
          <label class="wf-label">Descripción Temática y Alcance</label>
          <textarea name="descripcion" class="wf-input" rows="4" placeholder="Describa el alcance de los cursos comprendidos en esta categoría..."></textarea>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <a th:href="@{/cursos/categorias}" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
        <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Categoría</button>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-99: Configuración de Parámetros
processTemplate('configuracion', 'cu-99-configurar-parametros.html', html => {
  html = html.replace(/<div class="wf-table-wrap mb-4">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap mb-4">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Clave / Parámetro</th>
              <th>Valor Configurado</th>
              <th class="text-end">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="param : \${parametros}">
              <td><strong th:text="'#' + \${param.id}">#1</strong></td>
              <td><strong th:text="\${param.clave}">CLAVE</strong></td>
              <td><code th:text="\${param.valor}">VALOR</code></td>
              <td class="text-end">
                <form th:action="@{'/configuracion/borrar/' + \${param.id}}" method="post" class="d-inline">
                  <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Eliminar"><i class="fa-solid fa-trash"></i></button>
                </form>
              </td>
            </tr>
          </tbody>
        </table>
      </div>`);

  html = html.replace(/<div class="p-4 bg-light rounded border">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
      <form th:action="@{/configuracion/guardar}" method="post" class="p-4 bg-light rounded border">
        <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin-bottom: 12px;">Crear o Actualizar Parámetro</h4>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="wf-label">Clave del Parámetro</label>
            <input type="text" name="clave" class="wf-input" placeholder="Ej: heygen.api_key, sesiones.max" required>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Valor</label>
            <input type="text" name="valor" class="wf-input" placeholder="Valor asignado" required>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-3 border-top">
          <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Parámetro</button>
        </div>
      </form>
    </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-95: Auditoría
processTemplate('auditoria', 'cu-95-consultar-auditoria.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Fecha y Hora</th>
              <th>Usuario Responsable</th>
              <th>Acción</th>
              <th>Entidad Afectada</th>
              <th>Detalle / Operación</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="log : \${registros}">
              <td><strong th:text="'#' + \${log.id}">#1</strong></td>
              <td><strong th:text="\${#temporals.format(log.fechaHora, 'dd/MM/yyyy HH:mm:ss')}">27/08/2026</strong></td>
              <td th:text="\${log.usuario != null ? log.usuario.nombreCompleto : 'Sistema'}">Admin</td>
              <td><span class="wf-badge status-active" th:text="\${log.tipoAccion != null ? log.tipoAccion.nombre : 'Operación'}">Crear</span></td>
              <td th:text="\${log.entidadAfectada != null ? log.entidadAfectada : '-'}">Curso</td>
              <td th:text="\${log.detalleOperacion != null ? log.detalleOperacion : '-'}">Detalle</td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-43: Buscar inscripción
processTemplate('inscripciones', 'cu-43-buscar-inscripcion.html', html => {
  html = html.replace(/<div class="wf-table-wrap">[\s\S]*?<\/table>\s*<\/div>/, `
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Alumno</th>
              <th>Curso & Cohorte</th>
              <th>Fecha Matrícula</th>
              <th>Vencimiento</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="insc : \${inscripciones}">
              <td><strong th:text="'#' + \${insc.id}">#1</strong></td>
              <td>
                <strong th:text="\${insc.alumno != null and insc.alumno.usuario != null ? insc.alumno.usuario.nombreCompleto : 'Alumno'}">Nombre</strong>
                <div class="small text-muted" th:text="\${insc.alumno != null and insc.alumno.usuario != null ? insc.alumno.usuario.correo : ''}">correo</div>
              </td>
              <td>
                <strong th:text="\${insc.cohorte != null and insc.cohorte.programa != null and insc.cohorte.programa.curso != null ? insc.cohorte.programa.curso.nombre : 'Curso'}">Curso</strong>
              </td>
              <td th:text="\${insc.fecha != null ? #temporals.format(insc.fecha, 'dd/MM/yyyy') : '-'}">01/01/2026</td>
              <td th:text="\${insc.fechaVencimientoAcceso != null ? #temporals.format(insc.fechaVencimientoAcceso, 'dd/MM/yyyy') : '-'}">01/06/2026</td>
              <td><span class="wf-badge status-active" th:text="\${insc.baja ? 'Dada de Baja' : 'Vigente'}">Vigente</span></td>
            </tr>
          </tbody>
        </table>
      </div>`);
  return html;
});

// CU-81: Registrarse
processTemplate('seguridad', 'cu-81-registrarse.html', html => {
  html = html.replace(/<div class="wf-card" style="max-width: 580px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{/seguridad/registro}" method="post" class="wf-card" style="max-width: 580px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom text-center">
        <h3 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Crear Cuenta de Alumno</h3>
        <p class="small text-muted" style="margin: 3px 0 0;">Acceda a cursos de finanzas, economía y mercado de capitales</p>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre</label>
          <input type="text" name="nombre" class="wf-input" placeholder="Nombre" required>
        </div>
        <div class="col-md-6">
          <label class="wf-label">Apellido</label>
          <input type="text" name="apellido" class="wf-input" placeholder="Apellido" required>
        </div>
        <div class="col-md-7">
          <label class="wf-label">Correo Electrónico</label>
          <input type="email" name="email" class="wf-input" placeholder="correo@ejemplo.com" required>
        </div>
        <div class="col-md-5">
          <label class="wf-label">DNI / Documento</label>
          <input type="text" name="dni" class="wf-input" placeholder="DNI" required>
        </div>
        <div class="col-12">
          <label class="wf-label">Contraseña</label>
          <input type="password" name="contrasena" class="wf-input" placeholder="Contraseña segura" required>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-between gap-3 mt-4 pt-3 border-top">
        <a th:href="@{/seguridad/login}" class="wf-btn wf-btn-outline">Ya tengo cuenta (Login)</a>
        <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-user-plus me-1"></i> Crear Cuenta</button>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

// CU-90: Iniciar sesión
processTemplate('seguridad', 'cu-90-iniciar-sesion.html', html => {
  html = html.replace(/<div class="wf-card" style="max-width: 480px;[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer Oficial/m, `
    <form th:action="@{/seguridad/login}" method="post" class="wf-card" style="max-width: 480px; margin: 40px auto; box-shadow: 0 12px 32px rgba(0,0,0,0.08); background: #FFFFFF;">
      <div class="text-center mb-4">
        <h3 style="font-size: 20px; font-weight: 800; color: #081426;">Iniciar Sesión en Idóneos Online</h3>
        <p class="small text-muted">Ingrese sus credenciales académicas para acceder</p>
      </div>

      <div th:if="\${error}" class="alert alert-danger p-2 small mb-3" th:text="\${error}">Error</div>
      <div th:if="\${mensaje}" class="alert alert-success p-2 small mb-3" th:text="\${mensaje}">Mensaje</div>

      <div class="mb-3">
        <label class="wf-label">Correo Electrónico</label>
        <input type="email" name="username" class="wf-input" placeholder="correo@ejemplo.com" required>
      </div>

      <div class="mb-2">
        <label class="wf-label mb-0">Contraseña</label>
        <input type="password" name="password" class="wf-input" placeholder="••••••••••••" required>
      </div>

      <div class="d-flex flex-column gap-2 mt-4">
        <button type="submit" class="wf-btn wf-btn-primary w-100">Iniciar Sesión</button>
        <div class="text-center my-2 text-muted small">o continúe con</div>
        <a th:href="@{/oauth2/authorization/google}" class="wf-btn wf-btn-outline w-100 text-center text-decoration-none"><i class="fa-brands fa-google me-1"></i> Continuar con Google</a>
      </div>
      <div class="text-center mt-3">
        <a th:href="@{/seguridad/registro}" class="small text-primary fw-bold text-decoration-none">¿No tenés cuenta? Registrate gratis</a>
      </div>
    </form>
  </div>
  </div>
  <!-- Footer Oficial`);
  return html;
});

console.log('🎉 ¡Cableado y conexión con la base de datos completado exitosamente!');
