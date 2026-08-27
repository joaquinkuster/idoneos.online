const fs = require('fs');

// Fix CU-01: exactly matching prototype markup
let cu01 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', 'utf8');

// Replace card grid with valid Thymeleaf iteration maintaining exact CSS
const cu01CardGrid = `<div class="wf-cards-grid">
  <div th:each="curso : \${cursos}" class="wf-course-card">
    <div class="wf-course-card-thumb">
      <div class="wf-course-thumb-icon"><i class="fa-solid fa-graduation-cap w-8 h-8 text-white"></i></div>
      <div class="wf-course-pills-row">
        <span class="wf-pill-tag" th:text="\${curso.categoria != null ? curso.categoria.nombre : 'Mercado de Capitales'}">Categoría</span>
        <span class="wf-pill-status" th:text="\${curso.nivel != null ? curso.nivel.nombre : 'General'}">Nivel</span>
      </div>
    </div>
    <div class="wf-course-card-body">
      <h4 class="wf-course-title" th:text="\${curso.nombre}">Nombre Curso</h4>
      <p class="wf-course-desc" th:text="\${curso.descripcion}">Descripción</p>
      <div class="wf-course-info-row">
        <span>📄 <strong th:text="\${curso.docente != null and curso.docente.usuario != null ? curso.docente.usuario.nombreCompleto : (curso.docenteTitular != null and curso.docenteTitular.usuario != null ? curso.docenteTitular.usuario.nombreCompleto : 'Docente')}">Docente</strong></span>
        <span class="wf-course-price" th:text="\${curso.precio <= 0 ? 'GRATIS' : '$' + #numbers.formatDecimal(curso.precio, 1, 'POINT', 2, 'COMMA')}">$0.00</span>
      </div>
    </div>
    <div class="wf-course-card-footer">
      <div class="d-flex flex-column gap-2 w-100">
        <div class="d-flex align-items-center gap-2 w-100">
          <a th:href="@{'/academico/aula?cursoId=' + \${curso.id}}" class="wf-btn wf-btn-sm wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; height: 38px;">
            <i class="fa-solid fa-arrow-right-to-bracket"></i>
            <span>Ingresar al Curso</span>
          </a>
        </div>
        <div class="d-flex align-items-center gap-2 w-100">
          <a th:href="@{'/evaluaciones/calificaciones?cursoId=' + \${curso.id}}" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1" style="font-weight: 600; color: #081426;">
            <i class="fa-solid fa-chart-simple text-primary"></i>
            <span>Ver Calificaciones</span>
          </a>
        </div>
        <div class="d-flex align-items-center gap-2 w-100">
          <a th:href="@{'/cursos/' + \${curso.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1">
            <i class="fa-solid fa-pen-to-square"></i>
            <span>Editar Curso</span>
          </a>
          <form th:action="@{'/cursos/' + \${curso.id} + '/baja'}" method="post" class="flex-grow-1">
            <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger w-100 d-flex align-items-center justify-content-center gap-1" onclick="return confirm('¿Confirmar baja del curso?');">
              <i class="fa-solid fa-trash me-1"></i>
              <span>Dar de baja</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
  <div th:if="\${#lists.isEmpty(cursos)}" class="col-12 text-center py-5 bg-white border rounded">
    <div class="text-muted">No se encontraron cursos activos con los filtros aplicados.</div>
  </div>
</div>`;

// Correct body replacement in CU-01
cu01 = cu01.replace(/<div class="wf-cards-grid">[\s\S]*?<!-- Bootstrap Bundle JS -->/, `${cu01CardGrid}\n</div>\n</div>\n<!-- Footer Oficial -->\n<div class="wf-screen-footer">\n<div><strong>Idóneos <span>Online</span> S.A.S.</strong> • Plataforma de Educación Financiera</div>\n<div>FCEQyN — UNaM • Proyecto Software (LSI)</div>\n</div>\n</div>\n</div>\n<!-- Bootstrap Bundle JS -->`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', cu01, 'utf8');

// Fix CU-06: exactly matching prototype markup
let cu06 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-06-explorar-catalogo-de-cursos.html', 'utf8');

const cu06CardGrid = `<div class="wf-cards-grid">
  <div th:each="curso : \${cursos}" class="wf-course-card">
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
        <span>👨‍🏫 <strong th:text="\${curso.docente != null and curso.docente.usuario != null ? curso.docente.usuario.nombreCompleto : (curso.docenteTitular != null and curso.docenteTitular.usuario != null ? curso.docenteTitular.usuario.nombreCompleto : 'Docente')}">Docente</strong></span>
        <span class="wf-course-price" th:text="\${curso.precio <= 0 ? 'GRATIS' : '$' + #numbers.formatDecimal(curso.precio, 1, 'POINT', 2, 'COMMA')}">$0.00</span>
      </div>
    </div>
    <div class="wf-course-card-footer">
      <a th:href="@{'/inscripciones/nueva?cursoId=' + \${curso.id}}" class="wf-btn wf-btn-sm wf-btn-primary w-100 text-center" style="background: var(--wf-gold); color: #081426; border-color: var(--wf-gold); font-weight: 700; height: 38px; display: flex; align-items: center; justify-content: center; gap: 8px;">
        <i class="fa-solid fa-cart-shopping"></i>
        <span>Ver Ficha / Inscribirme</span>
      </a>
    </div>
  </div>
  <div th:if="\${#lists.isEmpty(cursos)}" class="col-12 text-center py-5 bg-white border rounded">
    <div class="text-muted">No se encontraron cursos con inscripción abierta en este momento.</div>
  </div>
</div>`;

cu06 = cu06.replace(/<div class="wf-cards-grid">[\s\S]*?<!-- Bootstrap Bundle JS -->/, `${cu06CardGrid}\n</div>\n</div>\n<!-- Footer Oficial -->\n<div class="wf-screen-footer">\n<div><strong>Idóneos <span>Online</span> S.A.S.</strong> • Plataforma de Educación Financiera</div>\n<div>FCEQyN — UNaM • Proyecto Software (LSI)</div>\n</div>\n</div>\n</div>\n<!-- Bootstrap Bundle JS -->`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-06-explorar-catalogo-de-cursos.html', cu06, 'utf8');

console.log('✅ CU-01 y CU-06 corregidas fielmente según el prototipo original.');
