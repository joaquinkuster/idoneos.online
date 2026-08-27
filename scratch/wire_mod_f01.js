const fs = require('fs');

// Wire CU-01
let cu01 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', 'utf8');
// Connect search form
cu01 = cu01.replace(/<div class="wf-search-grid mb-4">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>/, `<form th:action="@{/cursos}" method="get" class="wf-search-grid mb-4">
  <div class="wf-input-wrap">
    <input type="text" name="busqueda" th:value="\${busqueda}" class="wf-input" placeholder="Buscar cursos por nombre, código o palabras clave...">
    <i class="fa-solid fa-magnifying-glass wf-input-icon"></i>
  </div>
  <div class="wf-input-wrap">
    <select name="categoriaId" class="wf-input">
      <option value="">Todas las Categorías</option>
      <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}" th:selected="\${categoriaId != null and categoriaId == cat.id}">Categoría</option>
    </select>
  </div>
  <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-filter me-1"></i> Buscar Cursos</button>
</form>`);

// Dynamic grid of courses
cu01 = cu01.replace(/<div class="row g-4" id="coursesGrid">[\s\S]*?<\/div>\s*<!-- Paginación/, `<div class="row g-4" id="coursesGrid">
  <div th:each="c : \${cursos}" class="col-md-6 col-lg-4">
    <div class="wf-card h-100 d-flex flex-column justify-content-between p-3 border shadow-sm">
      <div>
        <div class="d-flex justify-content-between align-items-center mb-2">
          <span class="wf-badge" style="background: #E0E7FF; color: #3730A3; font-weight: 700; font-size: 11px;" th:text="\${c.categoria != null ? c.categoria.nombre : 'General'}">Categoría</span>
          <span class="badge bg-light text-dark border" th:text="\${c.nivel != null ? c.nivel.nombre : 'Intermedio'}">Nivel</span>
        </div>
        <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin-bottom: 8px;" th:text="\${c.nombre}">Nombre del Curso</h4>
        <p class="small text-muted mb-3" style="line-height: 1.4;" th:text="\${c.descripcion}">Descripción del curso...</p>
      </div>
      <div>
        <div class="d-flex align-items-center justify-content-between pt-2 border-top mb-3">
          <span class="small text-muted"><i class="fa-solid fa-chalkboard-user me-1 text-primary"></i> <span th:text="\${c.docenteTitular != null ? c.docenteTitular.usuario.nombreCompleto : 'Docente Asignado'}">Docente</span></span>
          <strong style="color: #059669; font-size: 15px;" th:text="'$' + \${#numbers.formatDecimal(c.precio, 1, 'POINT', 2, 'COMMA')}">$0.00</strong>
        </div>
        <div class="d-flex gap-2">
          <a th:href="@{'/cursos/' + \${c.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline w-100 text-center"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
          <form th:action="@{'/cursos/' + \${c.id} + '/baja'}" method="post" class="w-100">
            <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger w-100" onclick="return confirm('¿Confirmar baja del curso?');"><i class="fa-solid fa-trash me-1"></i> Baja</button>
          </form>
        </div>
      </div>
    </div>
  </div>
  <div th:if="\${#lists.isEmpty(cursos)}" class="col-12 text-center py-5">
    <div class="p-4 bg-light rounded border text-muted">No se encontraron cursos activos con los criterios especificados.</div>
  </div>
</div>
<!-- Paginación`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-01-buscar-curso.html', cu01, 'utf8');

// Wire CU-02
let cu02 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-02-ver-mis-cursos.html', 'utf8');
cu02 = cu02.replace(/<div class="row g-4" id="coursesContainer">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer/, `<div class="row g-4" id="coursesContainer">
  <div th:each="c : \${misCursos}" class="col-md-6 col-lg-4">
    <div class="wf-card h-100 d-flex flex-column justify-content-between p-3 border shadow-sm">
      <div>
        <div class="d-flex justify-content-between align-items-center mb-2">
          <span class="wf-badge status-active">En Curso</span>
          <span class="badge bg-light text-muted border" th:text="\${c.categoria != null ? c.categoria.nombre : 'Idóneos'}">Finanzas</span>
        </div>
        <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin-bottom: 8px;" th:text="\${c.nombre}">Nombre del Curso</h4>
        <p class="small text-muted mb-3" th:text="\${c.descripcion}">Descripción...</p>
      </div>
      <div>
        <div class="mb-3">
          <div class="d-flex justify-content-between small text-muted mb-1">
            <span>Progreso del Programa</span>
            <strong class="text-dark">En Curso</strong>
          </div>
          <div class="progress" style="height: 6px;">
            <div class="progress-bar bg-success" style="width: 45%;"></div>
          </div>
        </div>
        <a th:href="@{'/academico/aula?cursoId=' + \${c.id}}" class="wf-btn wf-btn-primary w-100 text-center"><i class="fa-solid fa-door-open me-1"></i> Ingresar al Aula Virtual</a>
      </div>
    </div>
  </div>
  <div th:if="\${#lists.isEmpty(misCursos)}" class="col-12 text-center py-5">
    <div class="p-4 bg-light rounded border text-muted">
      <i class="fa-solid fa-graduation-cap fa-3x mb-3 text-muted"></i>
      <p class="mb-2">Aún no estás inscripto en ningún curso.</p>
      <a th:href="@{/cursos/catalogo}" class="wf-btn wf-btn-primary mt-2">Explorar Catálogo de Cursos</a>
    </div>
  </div>
</div>
</div>
</div>
</div>
<!-- Footer`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-02-ver-mis-cursos.html', cu02, 'utf8');

// Wire CU-03
let cu03 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-03-registrar-curso.html', 'utf8');
cu03 = cu03.replace(/<div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">[\s\S]*?<\/div>\s*<\/div>\s*<\/div>\s*<!-- Footer/, `<form th:action="@{/cursos/guardar}" method="post" class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
  <div class="pb-3 mb-4 border-bottom">
    <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Registrar Nuevo Curso</h3>
    <p class="small text-muted" style="margin: 2px 0 0;">Complete los datos comerciales y académicos para dar de alta un nuevo curso en la plataforma.</p>
  </div>
  <div class="row g-3 mb-4">
    <div class="col-md-8">
      <label class="wf-label">Nombre del Curso <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <input type="text" name="nombre" class="wf-input" placeholder="Ej: Especialización en Renta Fija y Bonos Soberanos" required>
      </div>
    </div>
    <div class="col-md-4">
      <label class="wf-label">Categoría Temática <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <select name="categoriaId" class="wf-input" required>
          <option value="">Seleccionar Categoría...</option>
          <option th:each="cat : \${categorias}" th:value="\${cat.id}" th:text="\${cat.nombre}">Categoría</option>
        </select>
      </div>
    </div>
    <div class="col-md-4">
      <label class="wf-label">Nivel de Dificultad <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <select name="nivelId" class="wf-input" required>
          <option value="">Seleccionar Nivel...</option>
          <option th:each="n : \${niveles}" th:value="\${n.id}" th:text="\${n.nombre}">Nivel</option>
        </select>
      </div>
    </div>
    <div class="col-md-4">
      <label class="wf-label">Precio de Arancel (ARS) <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <input type="number" step="0.01" min="0" name="precio" class="wf-input" placeholder="Ej: 45000" required>
      </div>
    </div>
    <div class="col-md-4">
      <label class="wf-label">Docente Titular <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <select name="docenteTitularId" class="wf-input" required>
          <option value="">Seleccionar Docente...</option>
          <option th:each="d : \${docentes}" th:value="\${d.id}" th:text="\${d.usuario.nombreCompleto}">Docente</option>
        </select>
      </div>
    </div>
    <div class="col-12">
      <label class="wf-label">Descripción del Curso <span class="text-danger">*</span></label>
      <div class="wf-input-wrap">
        <textarea name="descripcion" rows="3" class="wf-input" placeholder="Descripción detallada del contenido del curso..." required></textarea>
      </div>
    </div>
    <div class="col-md-6">
      <label class="wf-label">URL de Imagen de Portada</label>
      <div class="wf-input-wrap">
        <input type="text" name="imagen" class="wf-input" placeholder="https://ejemplo.com/portada.jpg">
      </div>
    </div>
    <div class="col-md-6 d-flex align-items-center">
      <div class="form-check mt-3">
        <input class="form-check-input" type="checkbox" name="emiteCertificado" value="true" id="certCheck" checked>
        <label class="form-check-label fw-bold small text-dark" for="certCheck">Emite Certificado Oficial al Aprobar</label>
      </div>
    </div>
  </div>
  <div class="pt-3 border-top d-flex justify-content-end gap-3">
    <a th:href="@{/cursos}" class="wf-btn wf-btn-outline">Cancelar</a>
    <button type="submit" class="wf-btn wf-btn-primary"><i class="fa-solid fa-floppy-disk me-1"></i> Guardar Curso</button>
  </div>
</form>
</div>
</div>
<!-- Footer`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-03-registrar-curso.html', cu03, 'utf8');

// Wire CU-07 (Categorías)
let cu07 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-07-buscar-categoria.html', 'utf8');
cu07 = cu07.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="cat : \${categorias}">
    <td><strong style="color: #081426; font-size: 13px;" th:text="\${cat.nombre}">Nombre</strong></td>
    <td th:text="\${cat.descripcion != null ? cat.descripcion : 'Sin descripción'}">Descripción</td>
    <td><span class="wf-badge status-active" th:text="\${cat.baja ? 'Inactiva' : 'Activa'}">Activa</span></td>
    <td class="text-end">
      <div class="d-inline-flex align-items-center gap-2">
        <a th:href="@{'/cursos/categorias/' + \${cat.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
        <form th:action="@{'/cursos/categorias/' + \${cat.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Dar de baja esta categoría?');"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(categorias)}">
    <td colspan="4" class="text-center py-4 text-muted">No se encontraron categorías registradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-07-buscar-categoria.html', cu07, 'utf8');

// Wire CU-08 (Registrar Categoría)
let cu08 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-08-registrar-categoria.html', 'utf8');
cu08 = cu08.replace(/<form[^>]*>/, `<form th:action="@{/cursos/categorias/guardar}" method="post">`);
cu08 = cu08.replace(/<input type="text" class="wf-input" placeholder="Ej: Renta Fija">/, `<input type="text" name="nombre" class="wf-input" placeholder="Ej: Renta Fija" required>`);
cu08 = cu08.replace(/<textarea class="wf-input" rows="3" placeholder="Descripción de la categoría..."><\/textarea>/, `<textarea name="descripcion" class="wf-input" rows="3" placeholder="Descripción de la categoría..."></textarea>`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-08-registrar-categoria.html', cu08, 'utf8');

console.log('✅ MOD-F-01 Cursos (CU-01 a CU-08) cableado dinámicamente con éxito.');
