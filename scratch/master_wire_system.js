const fs = require('fs');
const path = require('path');

const rootTemplatesDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates');
const pagesDir = path.join(rootTemplatesDir, 'pages');

console.log('🚀 Iniciando script maestro para conectar todos los CUs y sincronizar templates...');

// 1. Asegurar que las carpetas admin, docente, alumno, perfil existan y tengan vistas adecuadas
const missingDirs = ['admin', 'docente', 'alumno', 'perfil'];
missingDirs.forEach(d => {
  const target = path.join(pagesDir, d);
  if (!fs.existsSync(target)) {
    fs.mkdirSync(target, { recursive: true });
    console.log(`📁 Carpeta creada: pages/${d}`);
  }
});

// Helper para actualizar o crear un archivo template
function writeTemplateIfMissing(relPath, baseContentSupplier) {
  const fullPath = path.join(rootTemplatesDir, relPath);
  if (!fs.existsSync(fullPath)) {
    fs.writeFileSync(fullPath, baseContentSupplier(), 'utf8');
    console.log(`✨ Template creado: ${relPath}`);
  }
}

// 2. Crear plantillas alias/directas que llaman controllers específicos
writeTemplateIfMissing('pages/admin/panel.html', () => `<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Panel de Administración | Idóneos Online</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="wf-canvas-bg">
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <div class="container my-4">
    <div class="wf-hero-banner mb-4">
      <div>
        <div class="wf-hero-tag"><i class="fa-solid fa-shield-halved text-warning"></i><span>ADMINISTRACIÓN CENTRAL</span></div>
        <h2 class="wf-hero-title">Dashboard Global del Sistema</h2>
        <p class="wf-hero-desc">Métricas consolidadas, gestión operativa y auditoría de Idóneos Online.</p>
      </div>
    </div>
    <div class="row g-3 mb-4">
      <div class="col-md-3">
        <div class="wf-card p-3 text-center">
          <div class="text-muted small">Total Cursos</div>
          <h3 class="fw-bold text-navy" th:text="\${totalCursos}">0</h3>
          <a th:href="@{/cursos}" class="small text-primary text-decoration-none">Ver gestión <i class="fa-solid fa-arrow-right"></i></a>
        </div>
      </div>
      <div class="col-md-3">
        <div class="wf-card p-3 text-center">
          <div class="text-muted small">Total Usuarios</div>
          <h3 class="fw-bold text-navy" th:text="\${totalUsuarios}">0</h3>
          <a th:href="@{/seguridad/usuarios}" class="small text-primary text-decoration-none">Ver usuarios <i class="fa-solid fa-arrow-right"></i></a>
        </div>
      </div>
      <div class="col-md-3">
        <div class="wf-card p-3 text-center">
          <div class="text-muted small">Inscripciones</div>
          <h3 class="fw-bold text-navy" th:text="\${totalInscripciones}">0</h3>
          <a th:href="@{/inscripciones}" class="small text-primary text-decoration-none">Ver inscripciones <i class="fa-solid fa-arrow-right"></i></a>
        </div>
      </div>
      <div class="col-md-3">
        <div class="wf-card p-3 text-center">
          <div class="text-muted small">Ingresos Totales</div>
          <h3 class="fw-bold text-success" th:text="'$' + #numbers.formatDecimal(totalIngresos != null ? totalIngresos : 0, 1, 'POINT', 0, 'COMMA')">$0</h3>
          <a th:href="@{/reportes/ingresos}" class="small text-success text-decoration-none">Ver informe <i class="fa-solid fa-arrow-right"></i></a>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`);

writeTemplateIfMissing('pages/admin/usuarios.html', () => `<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Gestión de Usuarios | Idóneos Online</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="wf-canvas-bg">
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <div class="container my-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2>Usuarios Registrados</h2>
      <a th:href="@{/admin/usuarios/nuevo}" class="wf-btn wf-btn-primary"><i class="fa-solid fa-user-plus me-1"></i> Nuevo Usuario</a>
    </div>
    <div class="wf-card">
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre Completo</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="u : \${usuarios}">
              <td th:text="'#' + \${u.id}">#1</td>
              <td th:text="\${u.nombreCompleto}">Nombre</td>
              <td th:text="\${u.correo}">correo</td>
              <td><span class="wf-badge status-active" th:text="\${u.rol != null ? u.rol.nombre : 'Sin Rol'}">Rol</span></td>
              <td><span th:text="\${u.baja ? 'Baja' : 'Activo'}" th:class="\${u.baja ? 'text-danger fw-bold' : 'text-success fw-bold'}">Activo</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>`);

writeTemplateIfMissing('pages/perfil/verPerfil.html', () => `<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Mi Perfil | Idóneos Online</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="wf-canvas-bg">
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <div class="container my-4" style="max-width: 700px;">
    <div class="wf-card p-4">
      <div class="text-center mb-4">
        <div class="rounded-circle bg-navy text-gold fw-bold d-inline-flex align-items-center justify-content-center mb-2" style="width: 70px; height: 70px; font-size: 24px;" th:text="\${usuario != null ? #strings.substring(usuario.nombre, 0, 1) : 'U'}">U</div>
        <h3 class="fw-bold mb-0" th:text="\${usuario != null ? usuario.nombreCompleto : 'Usuario'}">Usuario</h3>
        <p class="text-muted small" th:text="\${usuario != null ? usuario.correo : 'correo@idoneos.online'}">correo</p>
      </div>
      <form th:action="@{'/usuario/modificar/' + \${usuario != null ? usuario.id : 1}}" method="post">
        <div class="mb-3">
          <label class="wf-label">Nombre</label>
          <input type="text" name="nombre" class="wf-input" th:value="\${usuario != null ? usuario.nombre : ''}" required>
        </div>
        <div class="mb-3">
          <label class="wf-label">Apellido</label>
          <input type="text" name="apellido" class="wf-input" th:value="\${usuario != null ? usuario.apellido : ''}" required>
        </div>
        <div class="mb-3">
          <label class="wf-label">Correo Electrónico</label>
          <input type="email" name="correo" class="wf-input" th:value="\${usuario != null ? usuario.correo : ''}" required>
        </div>
        <button type="submit" class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-floppy-disk me-1"></i> Guardar Cambios</button>
      </form>
    </div>
  </div>
</body>
</html>`);

writeTemplateIfMissing('pages/perfil/sesiones.html', () => `<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sesiones Activas | Idóneos Online</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="wf-canvas-bg">
  <div th:replace="~{fragments/navbar :: navbar}"></div>
  <div class="container my-4">
    <h2>Historial de Sesiones Activas y Seguridad</h2>
    <div class="wf-card">
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>IP / Dispositivo</th>
              <th>Inicio</th>
              <th>Fin / Cierre</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="s : \${sesiones}">
              <td th:text="'#' + \${s.id}">#1</td>
              <td th:text="\${s.ipAddress != null ? s.ipAddress : '127.0.0.1'}">127.0.0.1</td>
              <td th:text="\${s.fechaInicio != null ? #temporals.format(s.fechaInicio, 'dd/MM/yyyy HH:mm') : '-'}">01/01/2026</td>
              <td th:text="\${s.fechaFin != null ? #temporals.format(s.fechaFin, 'dd/MM/yyyy HH:mm') : 'Activa'}">Activa</td>
              <td><span class="wf-badge status-active" th:text="\${s.fechaFin == null ? 'Conectada' : 'Cerrada'}">Conectada</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>`);

// 3. Procesar dinámicamente TODAS las plantillas en pages/**/*.html
function walkDir(dir, callback) {
  fs.readdirSync(dir).forEach(f => {
    let dirPath = path.join(dir, f);
    let isDirectory = fs.statSync(dirPath).isDirectory();
    if (isDirectory) {
      walkDir(dirPath, callback);
    } else if (f.endsWith('.html')) {
      callback(dirPath);
    }
  });
}

let modifiedCount = 0;
walkDir(pagesDir, (filePath) => {
  let content = fs.readFileSync(filePath, 'utf8');
  let original = content;

  // Actualizar usuario dinámico
  content = content.replace(/<span style="font-size: 11px; font-weight: 700;">Joaquín Küster<\/span>/g, '<span style="font-size: 11px; font-weight: 700;" th:text="\${usuario != null ? usuario.nombreCompleto : \'Usuario Activo\'}">Usuario</span>');
  content = content.replace(/<div class="user-avatar-circle">JK<\/div>/g, '<div class="user-avatar-circle" th:text="\${usuario != null ? #strings.substring(usuario.nombre, 0, 1) + #strings.substring(usuario.apellido != null ? usuario.apellido : \'\', 0, 1) : \'IO\'}">IO</div>');
  content = content.replace(/<div class="wf-dropdown-user-name">Joaquín Küster<\/div>/g, '<div class="wf-dropdown-user-name" th:text="\${usuario != null ? usuario.nombreCompleto : \'Usuario Activo\'}">Usuario Activo</div>');
  content = content.replace(/<div class="wf-dropdown-user-email">joaquin.kuster@idoneos.online<\/div>/g, '<div class="wf-dropdown-user-email" th:text="\${usuario != null ? usuario.correo : \'usuario@idoneos.online\'}">usuario@idoneos.online</div>');
  content = content.replace(/<div class="wf-dropdown-user-role">Alumno<\/div>/g, '<div class="wf-dropdown-user-role" th:text="\${usuario != null and usuario.rol != null ? usuario.rol.nombre : \'Alumno\'}">Alumno</div>');

  if (content !== original) {
    fs.writeFileSync(filePath, content, 'utf8');
    modifiedCount++;
  }
});

console.log(`✅ Sincronizados y actualizados ${modifiedCount} templates con usuario en vivo.`);
console.log('🏁 Proceso finalizado con éxito.');
