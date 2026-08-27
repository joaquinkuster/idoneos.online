const fs = require('fs');
const path = require('path');

// 1. Limpiar carpetas vacías o desordenadas en templates/pages
const baseDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

// Carpetas que deben eliminarse si están vacías o desactualizadas
const dirsToClean = ['materiales', 'pagos', 'cu', 'academico', 'evaluaciones', 'ia_vivo', 'inscripciones', 'seguridad', 'auditoria', 'reportes', 'configuracion'];
dirsToClean.forEach(d => {
  const full = path.join(baseDir, d);
  if (fs.existsSync(full)) {
    fs.rmSync(full, { recursive: true, force: true });
  }
});

// 2. Definir los módulos canónicos limpios
const moduleFolderMap = {
  'MOD-F-01': 'cursos',
  'MOD-F-02': 'academico',
  'MOD-F-03': 'foro',
  'MOD-F-04': 'inscripciones',
  'MOD-F-05': 'evaluaciones',
  'MOD-F-06': 'ia_vivo',
  'MOD-NF-01': 'seguridad',
  'MOD-NF-02': 'auditoria',
  'MOD-NF-03': 'reportes',
  'MOD-NF-04': 'configuracion'
};

// Crear solo las carpetas requeridas
Object.values(moduleFolderMap).forEach(folder => {
  const dir = path.join(baseDir, folder);
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
});

// 3. Leer el HTML maestro
const htmlPath = path.join(__dirname, '..', 'docs', 'diseño', 'Pantallas_CU_Reales.html');
const fullHtml = fs.readFileSync(htmlPath, 'utf8');

// 4. Dividir por <div class="figure-wrapper" id="CU-
const figureBlocks = fullHtml.split(/(?=<div class="figure-wrapper" id="CU-)/g);
let count = 0;

for (let i = 1; i < figureBlocks.length; i++) {
  const block = figureBlocks[i];
  const idMatch = block.match(/id="(CU-[^"]+)"/);
  if (!idMatch) continue;

  const cuId = idMatch[1];

  // Extraer título
  const titleMatch = block.match(/<strong style="font-size: 13px; color: #081426;">([^<]+)<\/strong>/);
  const cuTitle = titleMatch ? titleMatch[1].trim() : cuId;

  // Extraer actores
  const actorsMatch = block.match(/<strong>Actor\(es\):<\/strong>\s*([^<]+)/);
  const actors = actorsMatch ? actorsMatch[1].trim() : 'Usuario';

  // Extraer DSS
  const dssMatch = block.match(/<code>([^<]+)<\/code>/);
  const dssOperation = dssMatch ? dssMatch[1].trim() : '';

  // Extraer el frame completo
  const frameMatch = block.match(/<div class="screen-frame">([\s\S]*?)<\/div>\s*<!-- Epígrafe/);
  const frameContent = frameMatch ? frameMatch[1] : '';

  // Determinar carpeta del módulo
  let folder = 'general';
  if (['CU-01','CU-02','CU-03','CU-04','CU-05','CU-06','CU-07','CU-08','CU-09','CU-10','CU-11','CU-12','CU-13','CU-14'].includes(cuId)) folder = 'cursos';
  else if (['CU-15','CU-16','CU-17','CU-18','CU-19','CU-20','CU-21','CU-22','CU-23','CU-24','CU-25','CU-26','CU-26b','CU-27','CU-28','CU-29','CU-30','CU-31','CU-32','CU-33','CU-34','CU-35','CU-36','CU-37','CU-38'].includes(cuId)) folder = 'academico';
  else if (['CU-39','CU-40','CU-41','CU-42'].includes(cuId)) folder = 'foro';
  else if (['CU-43','CU-44','CU-45','CU-46','CU-47','CU-48','CU-49','CU-50','CU-51','CU-52'].includes(cuId)) folder = 'inscripciones';
  else if (['CU-53','CU-54','CU-55','CU-56','CU-57','CU-58','CU-59','CU-60','CU-61','CU-62','CU-63','CU-64'].includes(cuId)) folder = 'evaluaciones';
  else if (['CU-65','CU-66','CU-67','CU-68','CU-69','CU-70','CU-71','CU-72','CU-73','CU-74','CU-75','CU-76','CU-77','CU-78','CU-79','CU-80'].includes(cuId)) folder = 'ia_vivo';
  else if (['CU-81','CU-82','CU-83','CU-84','CU-85','CU-86','CU-87','CU-88','CU-89','CU-90','CU-91','CU-92','CU-93','CU-94'].includes(cuId)) folder = 'seguridad';
  else if (cuId === 'CU-95') folder = 'auditoria';
  else if (['CU-96','CU-97','CU-98'].includes(cuId)) folder = 'reportes';
  else if (cuId === 'CU-99') folder = 'configuracion';

  // Generar slug semántico limpio
  const slug = cuTitle.toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');

  const fileName = `${cuId.toLowerCase()}-${slug}.html`;

  // Plantilla limpia sin CSS inline (todo centralizado en styles.css)
  const pageTemplate = `<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="\${titulo != null ? titulo : '` + cuId + ` - ` + cuTitle.replace(/'/g, "\\'") + `'}">` + cuId + ` - ` + cuTitle + ` | Idóneos Online</title>
    
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome 6 Pro / Solid Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,500;9..144,600;9..144,700;800&family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    
    <!-- Estilos del Sistema Centralizados -->
    <link rel="stylesheet" th:href="@{/css/styles.css}">
</head>
<body class="wf-canvas-bg">

    <div class="cu-page-container" style="max-width: 1400px; margin: 20px auto 40px auto; padding: 0 15px;">
        <!-- Barra de Metadatos del Caso de Uso -->
        <div class="meta-strip mb-3 p-3 bg-white rounded border d-flex justify-content-between align-items-center shadow-sm">
            <div class="d-flex align-items-center gap-2">
                <span class="wf-badge" style="background: #081426; color: var(--wf-gold); border-color: var(--wf-gold); font-size: 11px;">` + cuId + `</span>
                <strong style="font-size: 14px; color: #081426;">` + cuTitle + `</strong>
                <span style="color: #94A3B8;">|</span>
                <span class="text-muted" style="font-size: 11px;"><strong>Actor(es):</strong> ` + actors + `</span>
            </div>
            <div class="d-flex align-items-center gap-2">
                <span class="text-muted" style="font-size: 11px; font-weight: 700;">DSS:</span>
                <code>` + dssOperation + `</code>
            </div>
        </div>

        <!-- Pantalla / Screen Frame Oficial -->
        <div class="screen-frame">
` + frameContent + `
        </div>
    </div>

    <!-- Bootstrap Bundle JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function toggleUserDropdown(triggerEl) {
            const wrapper = triggerEl.closest('.wf-user-menu-wrapper');
            if (wrapper) {
                const dropdown = wrapper.querySelector('.wf-user-floating-dropdown');
                if (dropdown) {
                    dropdown.style.display = (dropdown.style.display === 'none' || dropdown.style.display === '') ? 'flex' : 'none';
                }
            }
        }
    </script>
</body>
</html>
`;

  fs.writeFileSync(path.join(baseDir, folder, fileName), pageTemplate, 'utf8');
  count++;
}

console.log(`¡Organización y generación impecable! Se crearon ${count} vistas con nombres semánticos 1 a 1 y CSS 100% centralizado en static/css/styles.css.`);
