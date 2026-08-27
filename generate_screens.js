const specializedFormGenerators = require('./scratch/all_specialized_forms.js');
const fs = require('fs');
const path = require('path');

// Avatar docente preview base64
let avatarDocenteImg = 'docs/diseño/avatar_docente_preview.png';
try {
  if (fs.existsSync(path.join(__dirname, 'scratch', 'avatar_base64.txt'))) {
    avatarDocenteImg = fs.readFileSync(path.join(__dirname, 'scratch', 'avatar_base64.txt'), 'utf8');
  }
} catch(e) {}

// 1. Read Casos de Uso Reales
const mdPath = path.join(__dirname, 'docs', 'diseño', 'Casos de Uso Reales.md');
const mdContent = fs.readFileSync(mdPath, 'utf8');

// 2. Read DSS.md for exact message traceability
const dssPath = path.join(__dirname, 'docs', 'analisis', 'DSS.md');
let dssContent = '';
try {
  dssContent = fs.readFileSync(dssPath, 'utf8');
} catch(e) {}

// Parse DSS messages by CU-XX
const dssMap = {};
if (dssContent) {
  const dssBlocks = dssContent.split(/(?=### CU-)/g);
  for (let b of dssBlocks) {
    const m = b.match(/### (CU-[^:\r\n]+):/);
    if (m) {
      const cuId = m[1].trim();
      const msgs = [];
      const msgRegex = /`([a-zA-Z0-9_]+\([^)]*\))`|\$?\\rightarrow\$?\s*`([^`]+)`/g;
      let mm;
      while ((mm = msgRegex.exec(b)) !== null) {
        const found = mm[1] || mm[2];
        if (found && !msgs.includes(found)) msgs.push(found);
      }
      dssMap[cuId] = msgs.length > 0 ? msgs.join(' ➔ ') : 'Operación del sistema correspondiente';
    }
  }
}
dssMap['CU-26b'] = 'verContenidoUnidad(unaUnidad) ➔ activarModoEdicion(unCurso)';

// 3. FontAwesome 6 Helper Functions
const fa = {
  graduationCap: (cls="text-slate-400") => `<i class="fa-solid fa-graduation-cap ${cls}"></i>`,
  briefcase: (cls="text-slate-400") => `<i class="fa-solid fa-briefcase ${cls}"></i>`,
  bolt: (cls="") => `<i class="fa-solid fa-bolt ${cls}"></i>`,
  filePdf: (cls="") => `<i class="fa-solid fa-file-pdf ${cls}"></i>`,
  fileLines: (cls="") => `<i class="fa-solid fa-file-lines ${cls}"></i>`,
  video: (cls="") => `<i class="fa-solid fa-video ${cls}"></i>`,
  chartLine: (cls="") => `<i class="fa-solid fa-chart-line ${cls}"></i>`,
  chartBar: (cls="") => `<i class="fa-solid fa-chart-simple ${cls}"></i>`,
  book: (cls="") => `<i class="fa-solid fa-book ${cls}"></i>`,
  bookOpen: (cls="") => `<i class="fa-solid fa-book-open ${cls}"></i>`,
  clipboardCheck: (cls="") => `<i class="fa-solid fa-clipboard-check ${cls}"></i>`,
  listCheck: (cls="") => `<i class="fa-solid fa-list-check ${cls}"></i>`,
  comments: (cls="") => `<i class="fa-solid fa-comments ${cls}"></i>`,
  circleCheck: (cls="") => `<i class="fa-solid fa-circle-check ${cls}"></i>`,
  lock: (cls="") => `<i class="fa-solid fa-lock ${cls}"></i>`,
  gear: (cls="") => `<i class="fa-solid fa-gear ${cls}"></i>`,
  plus: (cls="") => `<i class="fa-solid fa-plus ${cls}"></i>`,
  penToSquare: (cls="") => `<i class="fa-solid fa-pen-to-square ${cls}"></i>`,
  trash: (cls="") => `<i class="fa-solid fa-trash ${cls}"></i>`,
  bars: (cls="") => `<i class="fa-solid fa-bars ${cls}"></i>`,
  arrowRight: (cls="") => `<i class="fa-solid fa-arrow-right ${cls}"></i>`,
  triangleExclamation: (cls="") => `<i class="fa-solid fa-triangle-exclamation ${cls}"></i>`,
  chevronDown: (cls="") => `<i class="fa-solid fa-chevron-down ${cls}"></i>`,
  wandMagicSparkles: (cls="") => `<i class="fa-solid fa-wand-magic-sparkles ${cls}"></i>`,
  microphone: (cls="") => `<i class="fa-solid fa-microphone ${cls}"></i>`,
  camera: (cls="") => `<i class="fa-solid fa-camera ${cls}"></i>`,
  towerBroadcast: (cls="") => `<i class="fa-solid fa-tower-broadcast ${cls}"></i>`,
  creditCard: (cls="") => `<i class="fa-solid fa-credit-card ${cls}"></i>`,
  user: (cls="") => `<i class="fa-solid fa-user ${cls}"></i>`,
  users: (cls="") => `<i class="fa-solid fa-users ${cls}"></i>`,
  shieldHalved: (cls="") => `<i class="fa-solid fa-shield-halved ${cls}"></i>`,
  tag: (cls="") => `<i class="fa-solid fa-tag ${cls}"></i>`,
  bell: (cls="") => `<i class="fa-solid fa-bell ${cls}"></i>`,
  magnifyingGlass: (cls="") => `<i class="fa-solid fa-magnifying-glass ${cls}"></i>`,
  xmark: (cls="") => `<i class="fa-solid fa-xmark ${cls}"></i>`,
  image: (cls="") => `<i class="fa-solid fa-image ${cls}"></i>`,
  arrowRightFromBracket: (cls="") => `<i class="fa-solid fa-arrow-right-from-bracket ${cls}"></i>`,
  circlePlay: (cls="") => `<i class="fa-solid fa-circle-play ${cls}"></i>`
};

// Aliases for compatibility
const icons = {
  academicCap: (cls="") => fa.graduationCap(cls),
  briefcase: (cls="") => fa.briefcase(cls),
  bolt: (cls="") => fa.bolt(cls),
  documentText: (cls="") => fa.fileLines(cls),
  videoCamera: (cls="") => fa.video(cls),
  presentationChart: (cls="") => fa.chartLine(cls),
  bookOpen: (cls="") => fa.bookOpen(cls),
  clipboardCheck: (cls="") => fa.clipboardCheck(cls),
  queueList: (cls="") => fa.listCheck(cls),
  chatBubble: (cls="") => fa.comments(cls),
  checkCircle: (cls="") => fa.circleCheck(cls),
  lockClosed: (cls="") => fa.lock(cls),
  cog6Tooth: (cls="") => fa.gear(cls),
  plus: (cls="") => fa.plus(cls),
  pencilSquare: (cls="") => fa.penToSquare(cls),
  trash: (cls="") => fa.trash(cls),
  bars3: (cls="") => fa.bars(cls),
  arrowRight: (cls="") => fa.arrowRight(cls),
  exclamationTriangle: (cls="") => fa.triangleExclamation(cls),
  chevronDown: (cls="") => fa.chevronDown(cls),
  sparkles: (cls="") => fa.wandMagicSparkles(cls),
  microphone: (cls="") => fa.microphone(cls),
  camera: (cls="") => fa.camera(cls),
  signal: (cls="") => fa.towerBroadcast(cls),
  creditCard: (cls="") => fa.creditCard(cls),
  chartBar: (cls="") => fa.chartBar(cls),
  user: (cls="") => fa.user(cls),
  users: (cls="") => fa.users(cls),
  shieldCheck: (cls="") => fa.shieldHalved(cls),
  tag: (cls="") => fa.tag(cls),
  bell: (cls="") => fa.bell(cls),
  magnifyingGlass: (cls="") => fa.magnifyingGlass(cls),
  xMark: (cls="") => fa.xmark(cls),
  photo: (cls="") => fa.image(cls)
};

// 4. Parse Markdown CUs
const cuBlocks = mdContent.split(/(?=### CU-)/g);
const cus = [];

for (let i = 1; i < cuBlocks.length; i++) {
  const block = cuBlocks[i];
  const titleMatch = block.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) continue;
  
  const id = titleMatch[1].trim();
  const name = titleMatch[2].trim();
  
  // Soporte tanto para formato en línea (**Módulo**: ...) como con salto de línea
  const moduleMatch = block.match(/\*\*Módulo\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Módulo\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const actorsMatch = block.match(/\*\*Actor\(es\)\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Actor\(es\)\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const descMatch = block.match(/\*\*Descripción\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Descripción\*\*\s*[\r\n]+([\s\S]*?)(?=\*\*Precondición|\*\*Flujo)/);
  
  const badges = [];
  const badgeRegex = /\[([A-Z0-9]+)\]/g;
  let bm;
  while ((bm = badgeRegex.exec(block)) !== null) {
    if (!badges.includes(bm[1])) badges.push(bm[1]);
  }
  
  cus.push({
    id,
    name,
    module: moduleMatch ? moduleMatch[1].trim() : 'Módulo General',
    actors: actorsMatch ? actorsMatch[1].trim() : 'Usuario',
    description: descMatch ? descMatch[1].trim() : '',
    badges: badges.length > 0 ? badges : ['A', 'B', 'C', 'D']
  });
}

// Group by Module for left sidebar
const modules = [];
const moduleMap = {};
cus.forEach(c => {
  if (!moduleMap[c.module]) {
    moduleMap[c.module] = [];
    modules.push({ name: c.module, cus: moduleMap[c.module] });
  }
  moduleMap[c.module].push(c);
});

// Role & Dropdown Helper (Clean & organized)
function getRoleInfo(actors, cuId) {
  const a = (actors || '').toLowerCase();
  const isEditingMode = ['CU-26b', 'CU-19', 'CU-20', 'CU-21', 'CU-22', 'CU-27', 'CU-28', 'CU-29', 'CU-30', 'CU-31', 'CU-32', 'CU-33', 'CU-34', 'CU-53', 'CU-54', 'CU-55', 'CU-56', 'CU-57', 'CU-58', 'CU-59', 'CU-60', 'CU-65', 'CU-66', 'CU-67', 'CU-68', 'CU-69', 'CU-70', 'CU-71'].includes(cuId);

  if (a.includes('alumno') && !a.includes('docente') && !a.includes('administrador')) {
    return {
      role: 'Alumno',
      name: 'Joaquín Küster',
      email: 'joaquin.kuster@idoneos.online',
      initials: 'JK',
      isDocente: false,
      isAdmin: false,
      dropdownSections: [
        {
          title: 'Navegación',
          items: [
            { label: 'Mis cursos matriculados', cu: 'CU-02' },
            { label: 'Explorar catálogo abierto', cu: 'CU-06' },
            { label: 'Historial de pagos e inscripciones', cu: 'CU-46' },
            { label: 'Mis calificaciones obtenidas', cu: 'CU-62' }
          ]
        },
        {
          title: 'Cuenta',
          items: [
            { label: 'Ver perfil', cu: 'CU-86' },
            { label: 'Editar datos de perfil', cu: 'CU-87' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  } else if (a.includes('docente') && !a.includes('administrador')) {
    return {
      role: 'Docente Titular',
      name: 'Mg. Elena Valenzuela',
      email: 'elena.valenzuela@idoneos.online',
      initials: 'EV',
      isDocente: true,
      isAdmin: false,
      dropdownSections: [
        {
          title: 'Modo Edición',
          hasEditingToggle: true,
          isEditingActive: isEditingMode
        },
        {
          title: 'Gestión de Cursos',
          items: [
            { label: 'Mis cursos a cargo', cu: 'CU-01' },
            { label: 'Estructura de unidades', cu: 'CU-19' },
            { label: 'Cronograma y avance', cu: 'CU-23' },
            { label: 'Lista de participantes', cu: 'CU-25' },
            { label: 'Estudio de Clon IA (HeyGen)', cu: 'CU-76' }
          ]
        },
        {
          title: 'Cuenta & Sesión',
          items: [
            { label: 'Ver perfil', cu: 'CU-86' },
            { label: 'Editar perfil', cu: 'CU-87' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  } else {
    return {
      role: 'Administrador',
      name: 'Admin General',
      email: 'admin@idoneos.online',
      initials: 'AG',
      isDocente: false,
      isAdmin: true,
      dropdownSections: [
        {
          title: 'Modo Edición',
          hasEditingToggle: true,
          isEditingActive: isEditingMode
        },
        {
          title: 'Administración',
          items: [
            { label: 'Gestión de cursos', cu: 'CU-01' },
            { label: 'Cohortes y programas', cu: 'CU-11' },
            { label: 'Gestión de usuarios', cu: 'CU-82' },
            { label: 'Auditoría de eventos', cu: 'CU-95' },
            { label: 'Reportes y estadísticas', cu: 'CU-98' },
            { label: 'Configuración general', cu: 'CU-99' }
          ]
        },
        {
          title: 'Cuenta',
          items: [
            { label: 'Ver perfil', cu: 'CU-86' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  }
}

// 5. Generate Wireframe Content
function generateScreenContent(cu) {
  const id = cu.id;
  const name = cu.name;
  const badges = cu.badges;

  if (specializedFormGenerators && specializedFormGenerators[id]) {
    return specializedFormGenerators[id](cu, badges);
  }

  // --- TYPE 1: CARDS VIEW (Mis Cursos / Catálogo) --- CU-01, CU-02, CU-06
  if (['CU-01', 'CU-02', 'CU-06'].includes(id)) {
    const isDocente = id === 'CU-01';
    const isAlumno = id === 'CU-02';
    const isCatalog = id === 'CU-06';

    if (isCatalog) {
      return `
        <!-- Filtros del Catálogo de Cursos -->
        <div class="wf-card mb-4">
          <div class="row align-items-end">
            <div class="col-md-5">
              <label class="wf-label">Buscar cursos por temática o palabra clave</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" placeholder="Ej: Bonos, Futuros, Renta Fija, Criptomonedas...">
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="wf-label">Filtrar por Categoría / Nivel</label>
              <div class="wf-select-container">
                <div class="wf-input-wrap">
                  <div class="wf-input wf-select-trigger">
                    <span>Todas las categorías</span>
                    ${icons.chevronDown()}
                  </div>
                </div>
                <div class="wf-dropdown-menu">
                  <div class="wf-dropdown-item active">☑ Todas las categorías</div>
                  <div class="wf-dropdown-item">☐ Mercado de Capitales & Finanzas</div>
                  <div class="wf-dropdown-item">☐ Impuestos & Contabilidad</div>
                  <div class="wf-dropdown-item">☐ Finanzas Cuantitativas & Cripto</div>
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Explorar Catálogo</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Layout de Catálogo con Panel Lateral de Detalle Público (Paso 4) -->
        <div class="row g-4">
          <!-- Columna Izquierda: Grid de Cursos del Catálogo -->
          <div class="col-lg-6" style="border-right: 2px solid #E2E8F0; padding-right: 24px;">
            <div class="d-flex flex-column gap-3">
              <!-- Curso 1: Especialización en Idoneidad Bursátil (Seleccionado) -->
              <div class="wf-course-card border-primary" style="box-shadow: 0 4px 12px rgba(212,175,55,0.15); border: 2px solid var(--wf-gold);">
                <div class="wf-course-card-thumb">
                  <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
                  <div class="wf-course-pills-row">
                    <span class="wf-pill-tag">Mercado de Capitales</span>
                    <span class="wf-pill-status" style="background: #ECFDF5; color: #047857; font-weight: 700;">● Inscripción Abierta</span>
                  </div>
                </div>
                <div class="wf-course-card-body">
                  <h4 class="wf-course-title">Especialización en Idoneidad Bursátil CNV</h4>
                  <p class="wf-course-desc">Preparación integral para el examen de Idóneo en Mercado de Capitales.</p>
                  <div class="wf-course-info-row">
                    <span>📄 4 Unidades temáticas</span>
                    <span class="wf-course-price">$120.000 ARS</span>
                  </div>
                </div>
                <div class="wf-course-card-footer">
                  <div class="d-flex align-items-center justify-content-between gap-2">
                    <button class="wf-btn wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2">
                      <i class="fa-solid fa-ticket"></i>
                      <span>Ver Ficha / Inscribirme</span>
                    </button>
                    <span class="pin-badge">${badges[1] || 'B'}</span>
                  </div>
                </div>
              </div>

              <!-- Curso 2: Operativa Cripto y DeFi -->
              <div class="wf-course-card">
                <div class="wf-course-card-thumb">
                  <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
                  <div class="wf-course-pills-row">
                    <span class="wf-pill-tag">Finanzas & Cripto</span>
                    <span class="wf-pill-status">● Inscripción Abierta</span>
                  </div>
                </div>
                <div class="wf-course-card-body">
                  <h4 class="wf-course-title">Operativa Cripto y DeFi Profesional</h4>
                  <p class="wf-course-desc">Trading algorítmico, finanzas descentralizadas y custodia institucional.</p>
                  <div class="wf-course-info-row">
                    <span>📄 3 Unidades</span>
                    <span class="wf-course-price">$95.000 ARS</span>
                  </div>
                </div>
                <div class="wf-course-card-footer">
                  <button class="wf-btn wf-btn-outline w-100 d-flex align-items-center justify-content-center gap-2">
                    <i class="fa-solid fa-ticket"></i>
                    <span>Ver Ficha / Inscribirme</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Columna Derecha: Vista Previa y Ficha Pública del Curso Seleccionado (Paso 4) -->
          <div class="col-lg-6" style="padding-left: 20px;">
            <div class="wf-card shadow-sm" style="background: #FFFFFF; border: 1px solid #CBD5E1; border-radius: 10px; position: sticky; top: 20px;">
              <div class="pb-3 mb-3 border-bottom d-flex justify-content-between align-items-center">
                <div>
                  <span class="wf-badge status-active mb-1">Ficha Informativa</span>
                  <h3 style="font-size: 17px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h3>
                </div>
                <div class="text-end">
                  <strong style="font-size: 18px; color: var(--wf-gold);">$120.000</strong>
                  <div class="small text-muted">Arancel único</div>
                </div>
              </div>

              <!-- Metadatos del Curso -->
              <div class="p-3 bg-light rounded mb-3" style="font-size: 12px;">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted">Nivel:</span>
                  <strong>Intermedio / Profesional</strong>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted">Modalidad:</span>
                  <strong>Online Asincrónico + Clases en Vivo</strong>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted">Certificación:</span>
                  <strong class="text-success"><i class="fa-solid fa-circle-check me-1"></i> Certificado de Idoneidad</strong>
                </div>
              </div>

              <!-- Contenidos por Unidad (Paso 4 - Badge C) -->
              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <label class="wf-label m-0" style="font-size: 12px;">Estructura de Contenidos por Unidad</label>
                  <span class="pin-badge">${badges[2] || 'C'}</span>
                </div>
                <div class="d-flex flex-column gap-2" style="font-size: 12px;">
                  <div class="p-2 border rounded bg-white d-flex justify-content-between align-items-center">
                    <span><strong>Unidad 1:</strong> Marco Regulatorio & Ley de Mercado de Capitales</span>
                    <span class="badge bg-secondary">Muestra Gratis</span>
                  </div>
                  <div class="p-2 border rounded bg-white">
                    <strong>Unidad 2:</strong> Instrumentos de Renta Fija (Bonos y ONs)
                  </div>
                  <div class="p-2 border rounded bg-white">
                    <strong>Unidad 3:</strong> Renta Variable y Valuación de Acciones
                  </div>
                  <div class="p-2 border rounded bg-white">
                    <strong>Unidad 4:</strong> Derivados Financieros (Opciones y Futuros)
                  </div>
                </div>
              </div>

              <!-- Cohortes Abiertas y Docente (Paso 4 - Badge D) -->
              <div>
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <label class="wf-label m-0" style="font-size: 12px;">Cohortes Abiertas Disponibles</label>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
                <div class="p-3 border rounded" style="background: #F8FAFC; font-size: 12px;">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <strong>Cohorte 2026-1 (Inicio 01/03/2026)</strong>
                    <span class="wf-badge status-active">15 cupos libres</span>
                  </div>
                  <div class="d-flex align-items-center gap-2 text-muted mb-3">
                    <i class="fa-solid fa-chalkboard-user"></i>
                    <span>Docente Titular: <strong>Mg. Elena Valenzuela</strong></span>
                  </div>
                  <a href="#CU-44" class="wf-btn wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="font-weight: 700; text-decoration: none;">
                    <i class="fa-solid fa-ticket"></i>
                    <span>Inscribirme Ahora</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      `;
    }

    return `
      <div class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar cursos por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Planificación Fiscal, Cripto, Mercado de Capitales...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Categoría / Estado</label>
            <div class="wf-select-container">
              <div class="wf-input-wrap">
                <div class="wf-input wf-select-trigger">
                  <span>Todas las categorías</span>
                  ${icons.chevronDown()}
                </div>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div class="wf-dropdown-menu">
                <div class="wf-dropdown-item active">☑ Todas las categorías</div>
                <div class="wf-dropdown-item">☐ Impuestos y Contabilidad</div>
                <div class="wf-dropdown-item">☐ Mercado de Capitales</div>
                <div class="wf-dropdown-item">☐ Finanzas y Cripto</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100">${isDocente ? 'Buscar Cursos' : (isAlumno ? 'Filtrar' : 'Explorar')}</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-cards-grid">
        <!-- Curso 1: Planificación Fiscal -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Impuestos y Contabilidad</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Planificación Fiscal y Tributaria</h4>
            <p class="wf-course-desc">Estrategias impositivas para pymes y profesionales.</p>
            <div class="wf-course-info-row">
              <span>📄 1 Programas</span>
              <span class="wf-course-price">$220.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            ${isDocente ? `
              <div class="d-flex flex-column gap-2 w-100">
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-26b" class="wf-btn wf-btn-sm wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; height: 38px;">
                    <i class="fa-solid fa-arrow-right-to-bracket"></i>
                    <span>Ingresar al Curso</span>
                  </a>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-62" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1" style="font-weight: 600; color: #081426;">
                    <i class="fa-solid fa-chart-simple text-primary"></i>
                    <span>Ver Calificaciones</span>
                  </a>
                </div>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-04" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1">
                    <i class="fa-solid fa-pen-to-square"></i>
                    <span>Editar Curso</span>
                  </a>
                  <a href="#CU-05" class="wf-btn wf-btn-sm wf-btn-outline text-danger flex-grow-1 d-flex align-items-center justify-content-center gap-1" title="Dar de baja curso">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Dar de baja</span>
                  </a>
                </div>
              </div>
            ` : `
              <a href="#CU-26" class="wf-btn-manage-course" style="text-decoration: none;">
                <i class="fa-solid fa-arrow-right-to-bracket"></i>
                <span>${isAlumno ? 'Ingresar al Curso' : 'Ver Ficha / Inscribirme'}</span>
              </a>
            `}
          </div>
        </div>

        <!-- Curso 2: Operativa Cripto -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Mercado de Capitales</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Operativa Cripto y DeFi</h4>
            <p class="wf-course-desc">Trading, DeFi y custodia institucional.</p>
            <div class="wf-course-info-row">
              <span>📄 1 Programas</span>
              <span class="wf-course-price">$120.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            ${isDocente ? `
              <div class="d-flex flex-column gap-2 w-100">
                <a href="#CU-26b" class="wf-btn wf-btn-sm wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; height: 38px;">
                  <i class="fa-solid fa-arrow-right-to-bracket"></i>
                  <span>Ingresar al Curso</span>
                </a>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-62" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1" style="font-weight: 600; color: #081426;">
                    <i class="fa-solid fa-chart-simple text-primary"></i>
                    <span>Ver Calificaciones</span>
                  </a>
                </div>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-04" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1">
                    <i class="fa-solid fa-pen-to-square"></i>
                    <span>Editar Curso</span>
                  </a>
                  <a href="#CU-05" class="wf-btn wf-btn-sm wf-btn-outline text-danger flex-grow-1 d-flex align-items-center justify-content-center gap-1" title="Dar de baja curso">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Dar de baja</span>
                  </a>
                </div>
              </div>
            ` : `
              <a href="#CU-26" class="wf-btn-manage-course" style="text-decoration: none;">
                <i class="fa-solid fa-arrow-right-to-bracket"></i>
                <span>${isAlumno ? 'Ingresar al Curso' : 'Ver Ficha / Inscribirme'}</span>
              </a>
            `}
          </div>
        </div>

        <!-- Curso 3: Mercado de Capitales Argentino -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Mercado de Capitales</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Mercado de Capitales Argentino</h4>
            <p class="wf-course-desc">Análisis de bonos, acciones y estructura de la CNV.</p>
            <div class="wf-course-info-row">
              <span>📄 2 Programas</span>
              <span class="wf-course-price">$45.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            ${isDocente ? `
              <div class="d-flex flex-column gap-2 w-100">
                <a href="#CU-26b" class="wf-btn wf-btn-sm wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; height: 38px;">
                  <i class="fa-solid fa-arrow-right-to-bracket"></i>
                  <span>Ingresar al Curso</span>
                </a>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-62" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1" style="font-weight: 600; color: #081426;">
                    <i class="fa-solid fa-chart-simple text-primary"></i>
                    <span>Ver Calificaciones</span>
                  </a>
                </div>
                <div class="d-flex align-items-center gap-2 w-100">
                  <a href="#CU-04" class="wf-btn wf-btn-sm wf-btn-outline flex-grow-1 d-flex align-items-center justify-content-center gap-1">
                    <i class="fa-solid fa-pen-to-square"></i>
                    <span>Editar Curso</span>
                  </a>
                  <a href="#CU-05" class="wf-btn wf-btn-sm wf-btn-outline text-danger flex-grow-1 d-flex align-items-center justify-content-center gap-1" title="Dar de baja curso">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Dar de baja</span>
                  </a>
                </div>
              </div>
            ` : `
              <a href="#CU-26" class="wf-btn-manage-course" style="text-decoration: none;">
                <i class="fa-solid fa-arrow-right-to-bracket"></i>
                <span>${isAlumno ? 'Ingresar al Curso' : 'Ver Ficha / Inscribirme'}</span>
              </a>
            `}
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 2: VISTA DEL CURSO ESTILO MOODLE (ALUMNO) --- CU-26
  if (id === 'CU-26') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mercado de Capitales</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <strong>Especialización en Idoneidad Bursátil</strong>
            </div>
            <h3 style="font-size: 19px; font-weight: 800; color: #081426; margin: 4px 0;">Especialización en Idoneidad Bursátil (Cohorte 2026-1)</h3>
            <p class="small text-muted" style="margin: 0;">Docente Titular: Mg. Elena Valenzuela | Duración: 8 Semanas | Programa Vigente</p>
          </div>
          <div class="text-end">
            <span class="wf-badge status-active">Inscripción Vigente</span>
            <div class="small text-muted mt-1">Progreso general: <strong>65%</strong></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-2">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span> Curso</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Cronograma</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[4] || 'E'}</span> Participantes</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[5] || 'F'}</span> Calificaciones</button>
          </div>
        </div>
      </div>

      <!-- Banner de Transmisión en Vivo Ahora Mismo -->
      <div class="p-3 mb-4 rounded border shadow-sm d-flex justify-content-between align-items-center" style="background: linear-gradient(135deg, #0F172A, #1E293B); color: #FFFFFF; border-left: 5px solid #DC2626 !important;">
        <div class="d-flex align-items-center gap-3">
          <div style="width: 44px; height: 44px; border-radius: 50%; background: #DC2626; display: flex; align-items: center; justify-content: center; color: white; animation: pulse 1.8s infinite;">
            <i class="fa-solid fa-broadcast-tower" style="font-size: 18px;"></i>
          </div>
          <div>
            <div class="d-flex align-items-center gap-2">
              <span class="badge" style="background: #DC2626; color: white; font-size: 10px; font-weight: 700; letter-spacing: 0.5px;">● EN DIRECTO AHORA</span>
              <span class="small text-muted" style="color: #94A3B8 !important;">Unidad 2 • Mg. Elena Valenzuela</span>
            </div>
            <h4 style="font-size: 15px; font-weight: 700; color: #FFFFFF; margin: 3px 0 0;">Clase Magistral: Resolución de Prácticos de Renta Fija y Valuación de Bonos</h4>
          </div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <a href="#CU-72" class="wf-btn wf-btn-sm d-flex align-items-center gap-2" style="background: #DC2626; border-color: #DC2626; color: #FFFFFF; font-weight: 700; border-radius: 20px; padding: 8px 18px; text-decoration: none; box-shadow: 0 4px 12px rgba(220,38,38,0.4);">
            <i class="fa-solid fa-video"></i>
            <span>Ingresar a la Sala en Vivo</span>
          </a>
        </div>
      </div>

      <div class="d-flex flex-column gap-4">
        <!-- Unidad 1 -->
        <div class="wf-unit-box">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-circle-check text-success" style="font-size: 16px;"></i>
              <strong style="font-size: 14px; color: #081426;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
            </div>
            <span class="wf-badge status-active">Aprobada (Nota: 9/10)</span>
          </div>
          <div class="wf-unit-body d-flex flex-column gap-2">
            <!-- Item 1: PDF -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <div class="moodle-icon-box moodle-icon-pdf">
                  <i class="fa-solid fa-file-pdf"></i>
                </div>
                <div>
                  <a href="#" class="moodle-activity-title wf-link">Ley 26.831 y modificatorias CNV</a>
                  <div class="moodle-activity-desc">Documento PDF oficial descargable • 2.4 MB</div>
                </div>
              </div>
              <button class="moodle-btn-check completed"><i class="fa-solid fa-check"></i> Hecho</button>
            </div>

            <!-- Item 2: Video -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <div class="moodle-icon-box moodle-icon-video">
                  <i class="fa-solid fa-circle-play"></i>
                </div>
                <div>
                  <a href="#" class="moodle-activity-title wf-link">Grabación: Estructura del Mercado Argentino</a>
                  <div class="moodle-activity-desc">Video clase dictada por Mg. Elena Valenzuela • 45 min</div>
                </div>
              </div>
              <button class="moodle-btn-check completed"><i class="fa-solid fa-check"></i> Hecho</button>
            </div>

            <!-- Item 3: Glosario -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <div class="moodle-icon-box moodle-icon-glossary">
                  <i class="fa-solid fa-book-open"></i>
                </div>
                <div>
                  <a href="#" class="moodle-activity-title wf-link">Glosario de Términos CNV</a>
                  <div class="moodle-activity-desc">Diccionario con 12 conceptos normativos clave</div>
                </div>
              </div>
              <button class="moodle-btn-check completed"><i class="fa-solid fa-check"></i> Hecho</button>
            </div>
          </div>
        </div>

        <!-- Unidad 2 -->
        <div class="wf-unit-box">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-book-open" style="color: var(--wf-gold); font-size: 16px;"></i>
              <strong style="font-size: 14px; color: #081426;">Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</strong>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="wf-badge status-active">En Curso</span>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
          <div class="wf-unit-body d-flex flex-column gap-3">
            <p class="small text-muted" style="margin: 0;">Conceptos de TIR, Duration, Modified Duration y curvas de rendimiento soberanas.</p>
            
            <div class="d-flex flex-column gap-2">
              <div class="wf-subcontent-title">Materiales de Estudio</div>
              
              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-pdf">
                    <i class="fa-solid fa-file-pdf"></i>
                  </div>
                  <div>
                    <a href="#" class="moodle-activity-title wf-link">Guía Teórica de Renta Fija v2.1</a>
                    <div class="moodle-activity-desc">Material principal de lectura (PDF - 4.1 MB)</div>
                  </div>
                </div>
                <button class="moodle-btn-check completed"><i class="fa-solid fa-check"></i> Hecho</button>
              </div>

              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-video">
                    <i class="fa-solid fa-file-excel" style="color: #16A34A;"></i>
                  </div>
                  <div>
                    <a href="#" class="moodle-activity-title wf-link">Planilla Excel: Cálculo de TIR y Flujos de Fondos</a>
                    <div class="moodle-activity-desc">Plantilla de ejercicios prácticos (.xlsx - 850 KB)</div>
                  </div>
                </div>
                <button class="moodle-btn-check">Marcar como hecha</button>
              </div>
            </div>

            <div class="d-flex flex-column gap-2 mt-2">
              <div class="wf-subcontent-title">Actividades & Evaluaciones</div>
              
              <!-- Autoevaluación -->
              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-quiz">
                    <i class="fa-solid fa-clipboard-check"></i>
                  </div>
                  <div>
                    <a href="#CU-63" class="moodle-activity-title wf-link">Autoevaluación Unidad 2: Ejercicios de Rendimiento</a>
                    <div class="moodle-activity-desc">10 preguntas aleatorias • 3 intentos máximos permitidos • Aprobación: 70%</div>
                  </div>
                </div>
                <div class="d-flex align-items-center gap-2">
                  <a href="#CU-63" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1" style="font-weight: 700; text-decoration: none;">
                    <i class="fa-solid fa-play"></i>
                    <span>Comenzar Intento</span>
                  </a>
                  <span class="pin-badge">${badges[3] || 'D'}</span>
                </div>
              </div>

              <!-- Foro de Consultas -->
              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-forum">
                    <i class="fa-solid fa-comments"></i>
                  </div>
                  <div>
                    <a href="#CU-35" class="moodle-activity-title wf-link">Foro de Consultas: Dudas sobre Duración Modificada</a>
                    <div class="moodle-activity-desc">Espacio de debate e interacción con el docente titular (4 consultas activas)</div>
                  </div>
                </div>
                <a href="#CU-35" class="moodle-btn-check" style="text-decoration: none;">Ver debates</a>
              </div>

              <!-- Clase en Vivo -->
              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-live">
                    <i class="fa-solid fa-video"></i>
                  </div>
                  <div>
                    <a href="#CU-72" class="moodle-activity-title wf-link">Clase en Vivo: Streaming Interactivo con OBS</a>
                    <div class="moodle-activity-desc">Sesión sincrónica semanal • Jueves 19:00 hs</div>
                  </div>
                </div>
                <a href="#CU-72" class="moodle-btn-check" style="background: #FEF3C7; color: #92400E; border-color: #FCD34D; text-decoration: none;"><i class="fa-solid fa-broadcast-tower"></i> Sala en Vivo</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 3: MODO EDICIÓN DEL CURSO --- CU-26b, CU-19, CU-20, CU-21
  if (['CU-26b', 'CU-19', 'CU-20', 'CU-21'].includes(id)) {
    // Semantic Badge Mapping per CU:
    // CU-19: Pestaña "Curso & Unidades" [A], Acordeón Moodle [B]
    // CU-26b: Botón "Añadir secciones" [A], Pestaña "Curso & Unidades" [B], Switch "Modo Edición" [C], "+ Añade una actividad o un recurso" [D]
    // CU-20: "+ Añadir secciones" [A], Formulario [B], "Guardar Unidad" [C]
    // CU-21: Botón contextual "Editar" [A], "Título de la Unidad" [B], "Guardar Cambios" [C]

    let badgeTab = '';
    let badgeToggle = '';
    let badgeAddSection = '';
    let badgeAddActivity = '';
    let badgeAccordionUnit1 = '';
    let badgeEditUnit1 = '';

    if (id === 'CU-19') {
      badgeTab = '<span class="pin-badge me-1">A</span>';
      badgeAccordionUnit1 = '<span class="pin-badge ms-2">B</span>';
      badgeAddSection = '<span class="pin-badge ms-1">A</span>';
    } else if (id === 'CU-26b') {
      badgeAddSection = '<span class="pin-badge ms-1">A</span>';
      badgeTab = '<span class="pin-badge me-1">B</span>';
      badgeToggle = '<span class="pin-badge ms-2">C</span>';
      badgeAddActivity = '<span class="pin-badge">D</span>';
    } else if (id === 'CU-20') {
      badgeAddSection = '<span class="pin-badge ms-1">A</span>';
      badgeTab = '<span class="pin-badge me-1">B</span>';
    } else if (id === 'CU-21') {
      badgeEditUnit1 = '<span class="pin-badge ms-1">A</span>';
    }

    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h2>
            <div class="small text-muted mt-1">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Idoneidad Bursátil</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2 bg-light p-2 border rounded">
              <span class="wf-badge status-active"><i class="fa-solid fa-pen-to-square me-1"></i> Modo Edición ACTIVO</span>
              ${badgeToggle}
            </div>
            <div class="text-muted cursor-pointer" title="Ajustes del curso"><i class="fa-solid fa-gear" style="font-size: 18px;"></i></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <button class="wf-tab-btn active">${badgeTab}Curso & Unidades</button>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
          </div>
        </div>
      </div>

      <div class="d-flex flex-column gap-4">
        <!-- Unidad 1 en Modo Edición con desglose completo del CU Real -->
        <div class="wf-unit-box" style="border: 1px solid #E2E8F0; border-radius: 8px; overflow: hidden; background: #FFFFFF; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
          <!-- Cabecera de la Unidad: Título completo arriba + Toolbar organizada abajo -->
          <div class="wf-unit-header p-3 border-bottom" style="background: #F8FAFC;">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div class="d-flex align-items-center gap-2">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab; font-size: 16px;"></i>
                <h3 style="font-size: 16px; font-weight: 800; color: #081426; margin: 0;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</h3>
                ${badgeAccordionUnit1}
              </div>
              <div class="d-flex align-items-center gap-2">
                <span class="wf-badge status-active"><i class="fa-solid fa-circle-check me-1"></i> Unidad Activa</span>
                ${badgeEditUnit1}
              </div>
            </div>

            <!-- Barra de Acciones y Herramientas con IA -->
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 pt-3 border-top">
              <!-- Herramientas de Inteligencia Artificial (Izquierda en 2 filas limpias con fondo relleno) -->
              <div class="d-flex flex-column gap-2" style="flex: 1; min-width: 320px;">
                <div class="d-flex flex-wrap align-items-center gap-2">
                  <a href="#CU-73" class="wf-btn wf-btn-sm d-inline-flex align-items-center gap-1 shadow-sm" style="background: #059669; border-color: #059669; color: #FFFFFF; font-weight: 700;">
                    <i class="fa-solid fa-list-check"></i>
                    <span>Generar Banco con IA (Ollama)</span>
                  </a>
                  <a href="#CU-74" class="wf-btn wf-btn-sm d-inline-flex align-items-center gap-1 shadow-sm" style="background: #0D9488; border-color: #0D9488; color: #FFFFFF; font-weight: 700;">
                    <i class="fa-solid fa-file-lines"></i>
                    <span>Generar Resumen de Unidad con IA</span>
                  </a>
                  <a href="#CU-75" class="wf-btn wf-btn-sm d-inline-flex align-items-center gap-1 shadow-sm" style="background: #0284C7; border-color: #0284C7; color: #FFFFFF; font-weight: 700;">
                    <i class="fa-solid fa-person-chalkboard"></i>
                    <span>Generar Presentación con IA</span>
                  </a>
                </div>
                <div class="d-flex flex-wrap align-items-center gap-2">
                  <a href="#CU-78" class="wf-btn wf-btn-sm d-inline-flex align-items-center gap-1 shadow-sm" style="background: #7C3AED; border-color: #7C3AED; color: #FFFFFF; font-weight: 700;">
                    <i class="fa-solid fa-video"></i>
                    <span>Generar Video con Avatar Clon</span>
                  </a>
                  <a href="#CU-77" class="wf-btn wf-btn-sm d-inline-flex align-items-center gap-1 shadow-sm" style="background: #4F46E5; border-color: #4F46E5; color: #FFFFFF; font-weight: 700;">
                    <i class="fa-solid fa-film"></i>
                    <span>Buscar clases con Clon de IA</span>
                  </a>
                </div>
              </div>

              <!-- Barra Divisoria Vertical + Bloque Derecho de Acciones de Unidad -->
              <div class="d-flex align-items-center gap-3">
                <div class="d-none d-md-block" style="width: 1px; height: 60px; background: #CBD5E1;"></div>
                <div class="d-flex flex-column gap-2">
                  <a href="#CU-21" class="wf-btn wf-btn-sm d-inline-flex align-items-center justify-content-center gap-1 shadow-sm" style="background: #2563EB; border-color: #2563EB; color: #FFFFFF; font-weight: 700; min-width: 170px;">
                    <i class="fa-solid fa-pen-to-square me-1"></i>
                    <span>Editar</span>
                  </a>
                  <a href="#CU-22" class="wf-btn wf-btn-sm text-white d-inline-flex align-items-center justify-content-center gap-1 shadow-sm" style="background: #DC2626; border-color: #DC2626; color: #FFFFFF; font-weight: 700; min-width: 170px;" title="Quitar de este programa">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Quitar de este programa</span>
                  </a>
                </div>
              </div>
            </div>
          </div>
          <div class="wf-unit-body p-3 d-flex flex-column gap-2">
            <!-- 1. Material de Estudio (PDF) -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-pdf">
                  <i class="fa-solid fa-file-pdf"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Ley 26.831 de Mercado de Capitales y Resoluciones CNV (PDF - 2.4 MB)</span>
                  <div class="moodle-activity-desc">Material bibliográfico de estudio • Estado: Publicado</div>
                </div>
              </div>
              <div class="d-flex align-items-center gap-2">
                <a href="#CU-29" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
              </div>
            </div>

            <!-- 2. Término de Glosario -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-glossary">
                  <i class="fa-solid fa-book-open"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Glosario de la Unidad: Agente de Liquidación y Compensación (ALyC)</span>
                  <div class="moodle-activity-desc">Término técnico con definición normativa CNV</div>
                </div>
              </div>
              <a href="#CU-33" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
            </div>

            <!-- 3. Pool de Preguntas -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-quiz" style="background: #E0F2FE; color: #0284C7;">
                  <i class="fa-solid fa-list-check"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Pool de Preguntas: Marco Regulatorio Bursátil (25 preguntas cargadas)</span>
                  <div class="moodle-activity-desc">Banco de reactivos teóricos con retroalimentación automática</div>
                </div>
              </div>
              <a href="#CU-55" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
            </div>

            <!-- 4. Autoevaluación -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-quiz">
                  <i class="fa-solid fa-clipboard-check"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Autoevaluación Unidad 1: Examen Formativo de Régimen Normativo</span>
                  <div class="moodle-activity-desc">Cuestionario aleatorio (10 preguntas) • 3 intentos máximos</div>
                </div>
              </div>
              <a href="#CU-59" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
            </div>

            <!-- 5. Clase en Vivo (Streaming) -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-live">
                  <i class="fa-solid fa-video"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Clase en Vivo: Taller de Análisis Práctico de Casos CNV</span>
                  <div class="moodle-activity-desc">Transmisión sincrónica programada con docente titular</div>
                </div>
              </div>
              <a href="#CU-67" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
            </div>

            <!-- 6. Clase con Clon de IA (HeyGen) -->
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box" style="background: #F3E8FF; color: #7C3AED;">
                  <i class="fa-solid fa-wand-magic-sparkles"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Clase con Clon de IA: Explicación de Órganos del Mercado (Avatar Mg. Valenzuela)</span>
                  <div class="moodle-activity-desc">Video generado mediante HeyGen API (05:12 min • 1080p)</div>
                </div>
              </div>
              <div class="d-flex align-items-center gap-2">
                <a href="#CU-77" class="wf-btn wf-btn-sm wf-btn-outline" title="Buscar clases con Clon de IA"><i class="fa-solid fa-magnifying-glass me-1"></i> Ver Clones</a>
                <a href="#CU-79" class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></a>
              </div>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
              <div class="d-flex align-items-center gap-3">
                <a href="#CU-28" class="wf-link d-flex align-items-center gap-2" style="font-size: 12px; font-weight: 700; color: #081426;">
                  <i class="fa-solid fa-plus-circle" style="color: var(--wf-gold); font-size: 16px;"></i>
                  <span>Añade una actividad o un recurso</span>
                </a>
              </div>
              ${badgeAddActivity}
            </div>
          </div>
        </div>
      </div>

      ${id === 'CU-20' ? `
      <!-- Modal Contextual para Agregar Unidad (DSS: crearUnidad(unPrograma, titulo, descripcion, contenido) / agregarUnidadExistente(unPrograma, unaUnidad)) -->
      <div class="wf-card mt-4 shadow-sm" style="background: #FFFFFF; max-width: 820px; border-left: 4px solid var(--wf-gold);">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom">
          <div>
            <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin: 0;"><i class="fa-solid fa-plus-circle me-2" style="color: var(--wf-gold);"></i>Agregar Unidad al Programa</h4>
            <p class="small text-muted m-0">Incorpore una sección pedagógica nueva o reutilice una existente de otro programa</p>
          </div>
          <span class="wf-badge status-active">Cohorte 2026-1</span>
        </div>

        <div class="row g-3 mb-3">
          <div class="col-12">
            <label class="wf-label">Título de la Unidad</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej. Unidad 3: Análisis Técnico y Fundamental en Acciones">
              <span class="pin-badge">B</span>
            </div>
          </div>
          <div class="col-12">
            <label class="wf-label">O Seleccionar Unidad Reutilizable de Otro Programa</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>-- Crear nueva unidad desde cero --</option>
                <option>Unidad Reutilizable: Marco Legal y Regulatorio Bursátil (Programa 2025-2)</option>
                <option>Unidad Reutilizable: Renta Fija y Valuación de Bonos (Programa 2025-1)</option>
              </select>
              <span class="pin-badge">C</span>
            </div>
          </div>
          <div class="col-12">
            <label class="wf-label">Descripción Académica / Contenido Inicial</label>
            <textarea class="wf-input" rows="3" placeholder="Resumen temático de los módulos pedagógicos a incluir..."></textarea>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-plus me-1"></i> Agregar Unidad</button>
            <span class="pin-badge">D</span>
          </div>
        </div>
      </div>
      ` : ''}

      ${id === 'CU-21' ? `
      <!-- Panel Contextual de Modificación de Unidad (DSS: modificarUnidad(unaUnidad, titulo, descripcion, contenido)) -->
      <div class="wf-card mt-4 shadow-sm" style="background: #FFFFFF; max-width: 820px; border-left: 4px solid #2563EB;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom">
          <div>
            <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin: 0;"><i class="fa-solid fa-pen-to-square text-primary me-2"></i>Ajustes de la Unidad 1</h4>
            <p class="small text-muted m-0">Edición de parámetros y contenido temático según plan de estudios</p>
          </div>
          <span class="wf-badge status-active">Unidad Activa</span>
        </div>
        
        <div class="row g-3 mb-3">
          <div class="col-12">
            <label class="wf-label">Título de la Unidad</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales">
              <span class="pin-badge">B</span>
            </div>
          </div>
          <div class="col-12">
            <label class="wf-label">Descripción Académica / Resumen</label>
            <textarea class="wf-input" rows="2">Marco normativo general, Ley de Mercado de Capitales (Ley 26.831), Decreto Reglamentario 1023/13 y Resoluciones Generales CNV aplicables a Idóneos.</textarea>
          </div>
          <div class="col-12">
            <label class="wf-label">Contenido Pedagógico Detallado</label>
            <textarea class="wf-input" rows="4">1. Estructura y órganos del mercado argentino (CNV, BYMA, MAE, Rofex, Caja de Valores).\n2. Tipos de agentes registrados: ALyC, AN, AGyC, ACYDI.\n3. Régimen de transparencia y deber de idoneidad profesional.</textarea>
          </div>
        </div>
        
        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
            <span class="pin-badge">C</span>
          </div>
        </div>
      </div>
      ` : ''}
    `;
  }

  // --- SPECIALIZED 3B: QUITAR UNIDAD DEL PROGRAMA --- CU-22
  if (id === 'CU-22') {
    return `
      <div class="wf-card" style="max-width: 650px; margin: 30px auto; background: #FFFFFF;">
        <div class="text-center mb-4">
          <div class="wf-icon-danger mb-3"><i class="fa-solid fa-triangle-exclamation" style="font-size: 24px; color: #DC2626;"></i></div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Quitar Unidad del Programa</h3>
          <p class="small text-muted" style="margin: 4px 0 0;">Esta acción desvinculará la sección pedagógica de este programa académico.</p>
        </div>

        <div class="p-3 bg-light rounded border mb-4">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="small text-muted">Unidad a desvincular:</span>
            <div class="d-flex align-items-center gap-2">
              <strong style="font-size: 13px; color: #081426;">Unidad 1: Marco Regulatorio</strong>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="d-flex justify-content-between align-items-center">
            <span class="small text-muted">Programa de destino:</span>
            <strong style="font-size: 13px; color: #081426;">Especialización en Idoneidad Bursátil</strong>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-19" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-danger"><i class="fa-solid fa-trash me-1"></i> Confirmar y Quitar</button>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 3C: BUSCAR CRONOGRAMA --- CU-23
  if (id === 'CU-23') {
    return `
      <div class="wf-card" style="max-width: 980px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Cronograma de Dictado Académico</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Visualice la secuencia pedagógica temporal, calendario de semanas lectivas y fechas asignadas por unidad.</p>
          </div>
          <span class="wf-badge status-active">Programa Vigente</span>
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
            <input type="text" class="wf-input bg-disabled" value="8 Semanas Lectivas (40 Horas Cátedra)" disabled>
          </div>
        </div>

        <!-- Calendario Minimalista de Celdas y Semanas Interactivas -->
        <div class="mb-4 p-3 bg-white rounded border shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-regular fa-calendar-days text-primary" style="font-size: 16px;"></i>
              <span class="small fw-bold text-dark text-uppercase">Calendario Secuencial de Cursado (Semanas 1 a 8)</span>
            </div>
            <span class="small text-muted">Período de Dictado: <strong>05/04/2026 al 30/05/2026</strong></span>
          </div>

          <!-- Matriz de Celdas Semanales -->
          <div class="row g-2 mb-3 text-center" style="font-size: 11px;">
            <div class="col">
              <div class="p-2 rounded border" style="background: #1E3A5F; color: white;">
                <div class="fw-bold">SEM 1</div>
                <div style="font-size: 9px; opacity: 0.85;">05/04 - 11/04</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 1</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #1E3A5F; color: white;">
                <div class="fw-bold">SEM 2</div>
                <div style="font-size: 9px; opacity: 0.85;">12/04 - 18/04</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 1</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #2563EB; color: white;">
                <div class="fw-bold">SEM 3</div>
                <div style="font-size: 9px; opacity: 0.85;">19/04 - 25/04</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 2</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #2563EB; color: white;">
                <div class="fw-bold">SEM 4</div>
                <div style="font-size: 9px; opacity: 0.85;">26/04 - 02/05</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 2</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #2563EB; color: white;">
                <div class="fw-bold">SEM 5</div>
                <div style="font-size: 9px; opacity: 0.85;">03/05 - 09/05</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 2</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #0D9488; color: white;">
                <div class="fw-bold">SEM 6</div>
                <div style="font-size: 9px; opacity: 0.85;">10/05 - 16/05</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 3</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #0D9488; color: white;">
                <div class="fw-bold">SEM 7</div>
                <div style="font-size: 9px; opacity: 0.85;">17/05 - 23/05</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 3</div>
              </div>
            </div>
            <div class="col">
              <div class="p-2 rounded border" style="background: #0D9488; color: white;">
                <div class="fw-bold">SEM 8</div>
                <div style="font-size: 9px; opacity: 0.85;">24/05 - 30/05</div>
                <div class="mt-1 badge bg-light text-dark" style="font-size: 9px;">Unidad 3</div>
              </div>
            </div>
          </div>
        </div>

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th style="width: 140px;">Semanas Lectivas</th>
                <th>Rango de Fechas</th>
                <th>Unidad Temática del Programa</th>
                <th>Dedicación</th>
                <th>Hitos Pedagógicos</th>
                <th class="text-end">Estado</th>
              </tr>
            </thead>
            <tbody>
              <!-- Unidad 1 -->
              <tr>
                <td>
                  <span class="badge" style="background: #1E3A5F; color: white; padding: 6px 10px; font-size: 11px;">
                    <i class="fa-solid fa-calendar-check me-1"></i> Sem 1 a 2
                  </span>
                </td>
                <td><strong style="color: #081426;">05/04/2026</strong> al <strong>18/04/2026</strong></td>
                <td>
                  <strong style="color: #081426;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
                  <div class="small text-muted">Régimen informativo, CNV y agentes del mercado</div>
                </td>
                <td>2 Semanas (10 hs)</td>
                <td>Lectura Ley 26.831 • Autoevaluación U1</td>
                <td class="text-end"><span class="wf-badge status-active">Publicada</span></td>
              </tr>

              <!-- Unidad 2 -->
              <tr>
                <td>
                  <span class="badge" style="background: #2563EB; color: white; padding: 6px 10px; font-size: 11px;">
                    <i class="fa-solid fa-calendar-check me-1"></i> Sem 3 a 5
                  </span>
                </td>
                <td><strong style="color: #081426;">19/04/2026</strong> al <strong>09/05/2026</strong></td>
                <td>
                  <strong style="color: #081426;">Unidad 2: Instrumentos de Renta Fija (Bonos y ONs)</strong>
                  <div class="small text-muted">Cálculo de TIR, duration modificada y cupones soberanos</div>
                </td>
                <td>3 Semanas (15 hs)</td>
                <td>Clase HeyGen • Planilla TIR • Streaming OBS</td>
                <td class="text-end"><span class="wf-badge status-active">En Dictado</span></td>
              </tr>

              <!-- Unidad 3 -->
              <tr>
                <td>
                  <span class="badge" style="background: #0D9488; color: white; padding: 6px 10px; font-size: 11px;">
                    <i class="fa-solid fa-calendar-check me-1"></i> Sem 6 a 8
                  </span>
                </td>
                <td><strong style="color: #081426;">10/05/2026</strong> al <strong>30/05/2026</strong></td>
                <td>
                  <strong style="color: #081426;">Unidad 3: Instrumentos de Renta Variable y Derivados</strong>
                  <div class="small text-muted">Valuación de acciones, ratios bursátiles y futuros</div>
                </td>
                <td>3 Semanas (15 hs)</td>
                <td>Valuación Acciones • Examen Integrador</td>
                <td class="text-end"><span class="wf-badge status-inactive">Programada</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 3D: MODIFICAR CRONOGRAMA --- CU-24
  if (id === 'CU-24') {
    return `
      <div class="wf-card" style="max-width: 980px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Reordenar y Modificar Cronograma</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Arrastre los bloques de unidades para modificar su secuencia y ajuste las semanas lectivas en el calendario interactivo.</p>
          </div>
          <span class="wf-badge status-active">Modo Edición Cronograma</span>
        </div>

        <!-- Calendario Dinámico con Celdas Pintadas -->
        <div class="mb-4 p-3 bg-white rounded border shadow-sm">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <span class="small fw-bold text-dark text-uppercase">Vista Previa Dinámica del Calendario (8 Semanas)</span>
            <span class="small text-muted">Total Acumulado: <strong>8 Semanas Lectivas</strong></span>
          </div>
          <div class="row g-2 mb-2 text-center" style="font-size: 11px;">
            <div class="col"><div class="p-2 rounded border" style="background: #1E3A5F; color: white; font-weight: 700;">SEM 1 (U1)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #1E3A5F; color: white; font-weight: 700;">SEM 2 (U1)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #2563EB; color: white; font-weight: 700;">SEM 3 (U2)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #2563EB; color: white; font-weight: 700;">SEM 4 (U2)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #2563EB; color: white; font-weight: 700;">SEM 5 (U2)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #0D9488; color: white; font-weight: 700;">SEM 6 (U3)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #0D9488; color: white; font-weight: 700;">SEM 7 (U3)</div></div>
            <div class="col"><div class="p-2 rounded border" style="background: #0D9488; color: white; font-weight: 700;">SEM 8 (U3)</div></div>
          </div>
        </div>

        <div class="d-flex flex-column gap-3 mb-4">
          <!-- Unidad 1 Drag Box -->
          <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm" style="border-left: 5px solid #1E3A5F !important;">
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div>
                <strong style="font-size: 14px; color: #081426;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
                <div class="small text-muted">Período: <strong>05/04/2026 al 18/04/2026</strong> (Semanas 1 a 2)</div>
              </div>
            </div>
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <label class="wf-label mb-0" style="font-size: 11px; font-weight: 700;">Duración:</label>
                <div class="d-flex align-items-center gap-1">
                  <input type="number" class="wf-input text-center" value="2" style="width: 65px; height: 36px; padding: 4px 8px; font-weight: 700;">
                  <span class="small text-muted">sem</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Unidad 2 Drag Box -->
          <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm" style="border-left: 5px solid #2563EB !important;">
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
              </div>
              <div>
                <strong style="font-size: 14px; color: #081426;">Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</strong>
                <div class="small text-muted">Período: <strong>19/04/2026 al 09/05/2026</strong> (Semanas 3 a 5)</div>
              </div>
            </div>
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <label class="wf-label mb-0" style="font-size: 11px; font-weight: 700;">Duración:</label>
                <div class="d-flex align-items-center gap-1">
                  <input type="number" class="wf-input text-center" value="3" style="width: 65px; height: 36px; padding: 4px 8px; font-weight: 700;">
                  <span class="small text-muted">sem</span>
                  <span class="pin-badge ms-1">${badges[2] || 'C'}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Unidad 3 Drag Box -->
          <div class="p-3 border rounded bg-white d-flex justify-content-between align-items-center shadow-sm" style="border-left: 5px solid #0D9488 !important;">
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <i class="fa-solid fa-grip-vertical text-muted" style="cursor: grab; font-size: 18px;"></i>
              </div>
              <div>
                <strong style="font-size: 14px; color: #081426;">Unidad 3: Instrumentos de Renta Variable y Derivados Financieros</strong>
                <div class="small text-muted">Período: <strong>10/05/2026 al 30/05/2026</strong> (Semanas 6 a 8)</div>
              </div>
            </div>
            <div class="d-flex align-items-center gap-3">
              <div class="d-flex align-items-center gap-2">
                <label class="wf-label mb-0" style="font-size: 11px; font-weight: 700;">Duración:</label>
                <div class="d-flex align-items-center gap-1">
                  <input type="number" class="wf-input text-center" value="3" style="width: 65px; height: 36px; padding: 4px 8px; font-weight: 700;">
                  <span class="small text-muted">sem</span>
                </div>
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
    `;
  }

  // --- SPECIALIZED 4B: FORO DE CONSULTAS ESTILO MOODLE FCEQYN --- CU-35
  if (id === 'CU-35') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Idoneidad Bursátil</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Foro de Consultas Académicas</strong>
            </div>
            <div class="d-flex align-items-center gap-3 mt-2">
              <div class="moodle-icon-box moodle-icon-forum" style="width: 44px; height: 44px; font-size: 20px;">
                <i class="fa-solid fa-comments"></i>
              </div>
              <div>
                <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Foro: Consultas sobre Mercado de Capitales & Renta Fija</h2>
                <p class="small text-muted mb-0 mt-1">Espacio de debate e intercambio académico para resolver dudas sobre ejercicios y normativas CNV.</p>
              </div>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Cohorte 2026-1</span>
            <div class="text-muted cursor-pointer" title="Ajustes del foro"><i class="fa-solid fa-gear" style="font-size: 18px;"></i></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Foros</button>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros y Búsqueda Moodle -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-center">
          <div class="col-12">
            <label class="wf-label mb-1">Buscar consultas en el foro</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar consultas por palabra clave, contenido, tema o autor en este foro...">
              <span class="pin-badge">${badges[1] || 'B'}</span>
              <button class="wf-btn wf-btn-primary" style="height: 44px; padding: 0 20px;"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
        <div class="small text-muted mt-2">
          <span>Grupos separados: <strong>Cohorte 2026-1 (Grupo 4)</strong></span>
        </div>
      </div>

      <!-- Tabla de Debates y Consultas estilo Moodle FCEQyN -->
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th style="width: 40px;"><i class="fa-regular fa-star text-muted"></i></th>
              <th>Debate / Asunto</th>
              <th>Grupo / Cohorte</th>
              <th>Comenzado por</th>
              <th>Último mensaje <i class="fa-solid fa-arrow-down-long ms-1"></i></th>
              <th class="text-center">Réplicas</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <!-- Fila 1: Consulta 1 -->
            <tr>
              <td><i class="fa-regular fa-star text-warning" style="cursor: pointer;"></i></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <a href="#CU-39" class="wf-link fw-bold" style="color: #0284C7; font-size: 13px; text-decoration: none;">
                    Duda con TP N° 1: Punto 4 - Cálculo de TIR y Duración de Bonos
                  </a>
                  <span class="pin-badge">${badges[3] || 'D'}</span>
                </div>
                <div class="small text-muted mt-1">Unidad 2: Instrumentos de Renta Fija • Bonos Soberanos AL30</div>
              </td>
              <td><span class="badge bg-light text-dark border">Cohorte 2026-1</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #2563EB; color: white;">JK</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">Joaquín Küster</span>
                    <div class="small text-muted" style="font-size: 10px;">11 abr 2026</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #081426; color: white;">FS</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">Mg. Elena Valenzuela</span>
                    <div class="small text-muted" style="font-size: 10px;">12 abr 2026, 10:15</div>
                  </div>
                </div>
              </td>
              <td class="text-center">
                <span class="badge bg-primary text-white" style="font-size: 11px; padding: 4px 8px;">3</span>
              </td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#CU-37" class="wf-btn wf-btn-sm wf-btn-outline" title="Editar Mensaje"><i class="fa-solid fa-pen-to-square"></i></a>
                  <a href="#CU-38" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Moderar / Eliminar"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>

            <!-- Fila 2: Consulta 2 -->
            <tr>
              <td><i class="fa-regular fa-star text-muted" style="cursor: pointer;"></i></td>
              <td>
                <a href="#CU-39" class="wf-link fw-bold" style="color: #0284C7; font-size: 13px; text-decoration: none;">
                  Régimen Informativo y Deber de Idoneidad según Ley 26.831
                </a>
                <div class="small text-muted mt-1">Unidad 1: Marco Regulatorio • Normas CNV</div>
              </td>
              <td><span class="badge bg-light text-dark border">Cohorte 2026-1</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #0D9488; color: white;">MB</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">María Benítez</span>
                    <div class="small text-muted" style="font-size: 10px;">09 abr 2026</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #0D9488; color: white;">MB</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">María Benítez</span>
                    <div class="small text-muted" style="font-size: 10px;">09 abr 2026, 18:30</div>
                  </div>
                </div>
              </td>
              <td class="text-center">
                <span class="badge bg-light text-muted border" style="font-size: 11px; padding: 4px 8px;">0</span>
              </td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#CU-40" class="wf-btn wf-btn-sm wf-btn-outline" title="Responder Consulta"><i class="fa-solid fa-reply me-1"></i> Responder</a>
                  <a href="#CU-38" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Moderar"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>

            <!-- Fila 3: Consulta 3 -->
            <tr>
              <td><i class="fa-regular fa-star text-muted" style="cursor: pointer;"></i></td>
              <td>
                <a href="#CU-39" class="wf-link fw-bold" style="color: #0284C7; font-size: 13px; text-decoration: none;">
                  Diferencia práctica entre ALyC Propio e Integral
                </a>
                <div class="small text-muted mt-1">Unidad 1: Marco Regulatorio • Tipología de Agentes</div>
              </td>
              <td><span class="badge bg-light text-dark border">Cohorte 2026-1</span></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #E11D48; color: white;">LR</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">Lucas Romero</span>
                    <div class="small text-muted" style="font-size: 10px;">04 abr 2026</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 28px; height: 28px; font-size: 10px; background: #081426; color: white;">FS</div>
                  <div>
                    <span class="fw-bold" style="font-size: 12px; color: #081426;">Mg. Elena Valenzuela</span>
                    <div class="small text-muted" style="font-size: 10px;">05 abr 2026, 11:20</div>
                  </div>
                </div>
              </td>
              <td class="text-center">
                <span class="badge bg-primary text-white" style="font-size: 11px; padding: 4px 8px;">2</span>
              </td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#CU-40" class="wf-btn wf-btn-sm wf-btn-outline" title="Responder Consulta"><i class="fa-solid fa-reply me-1"></i> Responder</a>
                  <a href="#CU-38" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Moderar"><i class="fa-solid fa-trash"></i></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Paginación Moodle -->
      <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
        <div class="small text-muted">Mostrando 1 - 3 de 14 consultas registradas en este foro</div>
        <div class="d-flex gap-1" style="font-size: 12px;">
          <span class="badge bg-dark text-white px-2 py-1">1</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">2</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">»</span>
        </div>
      </div>
    `;
  }

  // --- TYPE 4: LISTADOS CONTEXTUALES DE RECURSOS --- CU-27, CU-31, CU-53
  if (['CU-27', 'CU-31', 'CU-53'].includes(id)) {
    const isMaterial = id === 'CU-27';
    const isGlosario = id === 'CU-31';
    const isPool = id === 'CU-53';

    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h2>
            <div class="small text-muted mt-1">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>${name}</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Curso Seleccionado</span>
            <div class="text-muted cursor-pointer" title="Ajustes del curso"><i class="fa-solid fa-gear" style="font-size: 18px;"></i></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn ${isMaterial ? 'active' : ''}">${isMaterial ? `<span class="pin-badge me-1">${badges[0] || 'A'}</span>` : ''}Materiales</a>
            <a href="#CU-31" class="wf-tab-btn ${isGlosario ? 'active' : ''}">${isGlosario ? `<span class="pin-badge me-1">${badges[0] || 'A'}</span>` : ''}Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn ${isPool ? 'active' : ''}">${isPool ? `<span class="pin-badge me-1">${badges[0] || 'A'}</span>` : ''}Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros y Búsqueda -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar en ${isMaterial ? 'materiales' : (isGlosario ? 'glosario' : 'pools')}...">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Unidad</label>
            <select class="wf-input">
              <option>Todas las unidades temáticas</option>
              <option>Unidad 1: Marco Regulatorio</option>
              <option>Unidad 2: Renta Fija</option>
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
              <th>Título / Recurso</th>
              <th>Tipo</th>
              <th>Pertenencia</th>
              <th>Estado & IA</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <strong>${isMaterial ? 'Ley 26.831 de Mercado de Capitales' : (isGlosario ? 'Duration Modificada (Modified Duration)' : 'Pool Unidad 1: Marco Regulatorio')}</strong>
                ${isGlosario ? `<div class="small text-muted mt-1">Sensibilidad del precio del bono ante variaciones de 100 pbs en la TIR.</div>` : ''}
              </td>
              <td>${isMaterial ? 'Documento PDF (2.4 MB)' : (isGlosario ? 'Concepto Financiero' : '25 preguntas')}</td>
              <td>Unidad 1: Marco Regulatorio</td>
              <td>
                <span class="wf-badge status-active">Publicado</span>
                ${isMaterial ? `<span class="badge bg-light text-secondary border ms-1" style="font-size: 10px;">Docente</span>` : ''}
              </td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#${isMaterial ? 'CU-29' : (isGlosario ? 'CU-33' : 'CU-55')}" class="wf-btn wf-btn-sm wf-btn-outline">
                    <i class="fa-solid fa-pen-to-square me-1"></i>
                    <span>${isPool ? 'Editar Pool' : 'Editar'}</span>
                  </a>
                  <a href="#${isMaterial ? 'CU-30' : (isGlosario ? 'CU-34' : 'CU-56')}" class="wf-btn wf-btn-sm wf-btn-outline text-danger">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Eliminar</span>
                  </a>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td>
                <strong>${isMaterial ? 'Resumen Teórico: Valuación y Curvas de Bonos' : (isGlosario ? 'TIR (Tasa Interna de Retorno)' : 'Pool Unidad 2: Renta Fija y Bonos')}</strong>
                ${isGlosario ? `<div class="small text-muted mt-1">Tasa de descuento que iguala el VAN de los flujos de fondos con el precio dirty.</div>` : ''}
                ${isPool ? `<div class="small text-muted mt-1">Banco de 30 preguntas de opción múltiple y V/F sobre bonos</div>` : ''}
              </td>
              <td>${isMaterial ? 'Síntesis PDF (Generado IA)' : (isGlosario ? 'Concepto Financiero' : '30 preguntas')}</td>
              <td>Unidad 2: Renta Fija</td>
              <td>
                <span class="wf-badge status-active">Publicado</span>
                ${isMaterial ? `<span class="badge bg-primary text-white border ms-1" style="font-size: 10px;">Clon IA HeyGen</span>` : ''}
              </td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#${isPool ? 'CU-55' : '#'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i><span>${isPool ? 'Editar Pool' : 'Editar'}</span></a>
                  <a href="#${isPool ? 'CU-56' : '#'}" class="wf-btn wf-btn-sm wf-btn-outline text-danger"><i class="fa-solid fa-trash me-1"></i><span>Eliminar</span></a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- SPECIALIZED 8D: BUSCAR AUTOEVALUACIÓN (DETALLE A LA DERECHA) --- CU-57
  if (id === 'CU-57') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h2>
            <div class="small text-muted mt-1">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Autoevaluaciones</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Módulo Evaluaciones</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Autoevaluaciones</button>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros y Búsqueda -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar por nombre de autoevaluación</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Autoevaluación Unidad 2: Renta Fija...">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Unidad Temática</label>
            <select class="wf-input">
              <option>Todas las unidades</option>
              <option>Unidad 1: Marco Regulatorio</option>
              <option selected>Unidad 2: Renta Fija y Bonos</option>
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

      <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
        <!-- Columna Izquierda: Listado de Autoevaluaciones -->
        <div style="flex: 1 1 56%; min-width: 320px;">
          <div class="wf-table-wrap">
            <table class="wf-table">
              <thead>
                <tr>
                  <th>Cuestionario / Unidad</th>
                  <th>Preguntas & Tiempo</th>
                  <th>Estado</th>
                  <th class="text-end">Acciones</th>
                </tr>
              </thead>
              <tbody>
                <!-- Autoevaluación Seleccionada -->
                <tr class="table-active" style="background: #F0FDF4; border-left: 4px solid #16A34A;">
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <strong style="color: #081426;">Autoevaluación Unidad 2: Renta Fija</strong>
                      <span class="pin-badge">${badges[3] || 'D'}</span>
                    </div>
                    <div class="small text-muted">Unidad 2: Instrumentos de Renta Fija</div>
                  </td>
                  <td>10 preguntas • 20 min</td>
                  <td><span class="wf-badge status-active">Activa</span></td>
                  <td class="text-end">
                    <div class="d-inline-flex align-items-center gap-1">
                      <a href="#CU-59" class="wf-btn wf-btn-sm wf-btn-outline d-inline-flex align-items-center gap-1" title="Editar Cuestionario">
                        <i class="fa-solid fa-pen-to-square"></i>
                        <span>Editar cuestionario</span>
                      </a>
                      <a href="#CU-60" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1" title="Eliminar">
                        <i class="fa-solid fa-trash"></i>
                        <span>Eliminar</span>
                      </a>
                    </div>
                  </td>
                </tr>

                <!-- Autoevaluación 1 -->
                <tr>
                  <td>
                    <strong style="color: #081426;">Autoevaluación Unidad 1: Marco Regulatorio</strong>
                    <div class="small text-muted">Unidad 1: Marco Regulatorio CNV</div>
                  </td>
                  <td>10 preguntas • 15 min</td>
                  <td><span class="wf-badge status-active">Activa</span></td>
                  <td class="text-end">
                    <div class="d-inline-flex align-items-center gap-1">
                      <a href="#CU-59" class="wf-btn wf-btn-sm wf-btn-outline d-inline-flex align-items-center gap-1">
                        <i class="fa-solid fa-pen-to-square"></i>
                        <span>Editar cuestionario</span>
                      </a>
                      <a href="#CU-60" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1">
                        <i class="fa-solid fa-trash"></i>
                        <span>Eliminar</span>
                      </a>
                    </div>
                  </td>
                </tr>

                <!-- Examen Final -->
                <tr>
                  <td>
                    <strong style="color: #081426;">Evaluación Final Integradora (Certificación)</strong>
                    <div class="small text-muted">Unidad 3: Multi-pool Integrador</div>
                  </td>
                  <td>30 preguntas • 60 min</td>
                  <td><span class="wf-badge" style="background: #E0E7FF; color: #3730A3;">Examen Final</span></td>
                  <td class="text-end">
                    <div class="d-inline-flex align-items-center gap-1">
                      <a href="#CU-59" class="wf-btn wf-btn-sm wf-btn-outline d-inline-flex align-items-center gap-1">
                        <i class="fa-solid fa-pen-to-square"></i>
                        <span>Editar cuestionario</span>
                      </a>
                      <a href="#CU-60" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1">
                        <i class="fa-solid fa-trash"></i>
                        <span>Eliminar</span>
                      </a>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Barra Divisoria Vertical en el medio -->
        <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

        <!-- Columna Derecha: Panel de Configuración y Detalle [D] -->
        <div style="flex: 1 1 44%; min-width: 320px;">
          <div class="p-4 bg-white rounded border shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid #16A34A !important;">
            <div>
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <span class="badge bg-success text-white mb-1">Detalle de Configuración [D]</span>
                  <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin: 0;">Autoevaluación Unidad 2: Renta Fija</h4>
                </div>
                <a href="#CU-61" class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1" title="Ver intentos rendidos">
                  <i class="fa-solid fa-chart-line text-primary"></i> <span>Ver Intentos</span>
                </a>
              </div>

              <div class="d-flex flex-column gap-2" style="font-size: 12px;">
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Pools de Preguntas Asociados:</span>
                  <strong>Pool Unidad 2: Renta Fija (30 preg)</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Preguntas por Intento:</span>
                  <strong>10 preguntas sorteables</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Tiempo Límite:</span>
                  <strong>20 minutos</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Intentos Permitidos:</span>
                  <strong>3 intentos máximos</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Criterio de Aprobación:</span>
                  <strong class="text-success">100% de respuestas correctas</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Período de Apertura:</span>
                  <strong>19/04/2026 00:00 al 09/05/2026 23:59</strong>
                </div>
                <div class="p-2 border rounded bg-light d-flex justify-content-between">
                  <span class="text-muted">Integración Multi-Pool:</span>
                  <span>No (Evaluación específica de unidad)</span>
                </div>
              </div>
            </div>

            <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
              <span class="small text-muted">Total intentos registrados: <strong>28</strong></span>
              <a href="#CU-59" class="wf-btn wf-btn-sm wf-btn-primary d-inline-flex align-items-center gap-1">
                <i class="fa-solid fa-pen-to-square me-1"></i>
                <span>Editar cuestionario</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8E: BUSCAR INTENTO DE AUTOEVALUACIÓN --- CU-61
  if (id === 'CU-61') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Autoevaluaciones</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Autoevaluación Unidad 2: Renta Fija</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Historial de Intentos</strong>
            </div>
            <h2 style="font-size: 19px; font-weight: 800; color: #081426; margin: 2px 0 0;">Intentos de Autoevaluación: Renta Fija</h2>
            <p class="small text-muted" style="margin: 2px 0 0;">Supervisión de calificaciones, tiempos de entrega y anulación por sospecha de fraude.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">28 Intentos Totales</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Historial de Intentos</button>
            <a href="#CU-62" class="wf-tab-btn">Calificaciones</a>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar Alumno por Nombre o DNI</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar por alumno o DNI...">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Resultado</label>
            <select class="wf-input">
              <option>Todos los resultados (Aprobado / No aprobado)</option>
              <option>Solo Aprobados</option>
              <option>Solo No Aprobados</option>
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

      <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
        <!-- Columna Izquierda: Tabla de Intentos -->
        <div style="flex: 1 1 56%; min-width: 320px;">
          <div class="wf-table-wrap">
            <table class="wf-table">
              <thead>
                <tr>
                  <th>Alumno & Intento</th>
                  <th>Fecha & Duración</th>
                  <th>Nota / Resultado</th>
                  <th class="text-end">Acciones</th>
                </tr>
              </thead>
              <tbody>
                <!-- Intento 1 (Seleccionado) -->
                <tr class="table-active" style="background: #F0FDF4; border-left: 4px solid #16A34A;">
                  <td>
                    <div class="d-flex align-items-center gap-2">
                      <strong style="color: #081426;">Joaquín Küster</strong>
                      <span class="pin-badge">${badges[3] || 'D'}</span>
                    </div>
                    <div class="small text-muted">Intento #1 • DNI: 40.123.456</div>
                  </td>
                  <td>22/04/2026 14:15<div class="small text-muted">Duración: 14 min 30 s</div></td>
                  <td>
                    <strong style="color: #16A34A; font-size: 14px;">10 / 10</strong>
                    <div><span class="badge bg-success text-white" style="font-size: 10px;">Aprobado</span></div>
                  </td>
                  <td class="text-end">
                    <div class="d-inline-flex align-items-center gap-1">
                      <a href="#CU-64" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1" title="Anular Intento por Fraude">
                        <i class="fa-solid fa-ban me-1"></i>
                        <span>Anular Intento por Fraude</span>
                      </a>
                    </div>
                  </td>
                </tr>

                <!-- Intento 2 -->
                <tr>
                  <td>
                    <strong style="color: #081426;">María Benítez</strong>
                    <div class="small text-muted">Intento #2 • DNI: 38.945.112</div>
                  </td>
                  <td>21/04/2026 19:40<div class="small text-muted">Duración: 18 min 10 s</div></td>
                  <td>
                    <strong style="color: #DC2626; font-size: 14px;">7 / 10</strong>
                    <div><span class="badge bg-danger text-white" style="font-size: 10px;">No Aprobado</span></div>
                  </td>
                  <td class="text-end">
                    <a href="#CU-64" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1" title="Anular Intento por Fraude">
                      <i class="fa-solid fa-ban me-1"></i>
                      <span>Anular Intento por Fraude</span>
                    </a>
                  </td>
                </tr>

                <!-- Intento 3 -->
                <tr>
                  <td>
                    <strong style="color: #081426;">Lucas Romero</strong>
                    <div class="small text-muted">Intento #1 • DNI: 42.887.654</div>
                  </td>
                  <td>20/04/2026 11:05<div class="small text-muted">Duración: 12 min 00 s</div></td>
                  <td>
                    <strong style="color: #16A34A; font-size: 14px;">10 / 10</strong>
                    <div><span class="badge bg-success text-white" style="font-size: 10px;">Aprobado</span></div>
                  </td>
                  <td class="text-end">
                    <a href="#CU-64" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-inline-flex align-items-center gap-1" title="Anular Intento por Fraude">
                      <i class="fa-solid fa-ban me-1"></i>
                      <span>Anular Intento por Fraude</span>
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Barra Divisoria Vertical en el medio -->
        <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

        <!-- Columna Derecha: Detalle de Respuestas del Intento [D] -->
        <div style="flex: 1 1 44%; min-width: 320px;">
          <div class="p-4 bg-white rounded border shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid #2563EB !important;">
            <div>
              <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                  <span class="badge bg-primary text-white mb-1">Revisión de Respuestas [D]</span>
                  <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin: 0;">Intento #1 — Joaquín Küster</h4>
                  <div class="small text-muted">Calificación Obtenida: <strong>100% (10/10)</strong> • Aprobado</div>
                </div>
                <span class="wf-badge status-active">Completado</span>
              </div>

              <!-- Desglose de preguntas respondidas -->
              <div class="d-flex flex-column gap-2 mb-3" style="font-size: 12px; max-height: 280px; overflow-y: auto;">
                <div class="p-2 border rounded bg-light" style="border-left: 3px solid #16A34A !important;">
                  <div class="fw-bold text-dark mb-1">1. ¿Qué representa la Duration Modificada de un bono?</div>
                  <div class="text-success"><i class="fa-solid fa-check me-1"></i> <strong>Respuesta del alumno:</strong> La variación porcentual del precio ante un cambio del 1% en el rendimiento.</div>
                </div>
                <div class="p-2 border rounded bg-light" style="border-left: 3px solid #16A34A !important;">
                  <div class="fw-bold text-dark mb-1">2. ¿Una curva de rendimientos invertida indica generalmente expectativas recesivas?</div>
                  <div class="text-success"><i class="fa-solid fa-check me-1"></i> <strong>Respuesta del alumno:</strong> Verdadero.</div>
                </div>
                <div class="p-2 border rounded bg-light" style="border-left: 3px solid #16A34A !important;">
                  <div class="fw-bold text-dark mb-1">3. ¿Qué función cumple la paridad en la cotización de un título público?</div>
                  <div class="text-success"><i class="fa-solid fa-check me-1"></i> <strong>Respuesta del alumno:</strong> Relación porcentual entre el valor de mercado y su valor técnico.</div>
                </div>
              </div>
            </div>

            <div class="pt-3 border-top d-flex justify-content-between align-items-center">
              <span class="small text-muted">IP Origen: 181.44.12.98</span>
              <a href="#CU-64" class="wf-btn wf-btn-sm wf-btn-outline text-danger d-flex align-items-center gap-1">
                <i class="fa-solid fa-ban me-1"></i> <span>Anular por Fraude</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8F: VER CALIFICACIONES --- CU-62
  if (id === 'CU-62') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Especialización en Idoneidad Bursátil</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Libro de Calificaciones</strong>
            </div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 2px 0 0;">Calificaciones del Alumno en el Curso</h2>
            <p class="small text-muted" style="margin: 2px 0 0;">Consulte las autoevaluaciones rendidas por el alumno en el programa de su cohorte, con nota numérica y resultado.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Cohorte 2026-1</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso</a>
            <a href="#CU-23" class="wf-tab-btn">Cronograma</a>
            <a href="#CU-25" class="wf-tab-btn">Participantes</a>
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Calificaciones</button>
          </div>
        </div>
      </div>

      <!-- Selector / Buscador de Alumno [B] -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-6">
            <label class="wf-label">Seleccionar Alumno a Consultar</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar alumno por nombre, apellido o DNI..." value="Joaquín Küster (DNI: 40.123.456)">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-3">
            <label class="wf-label">Cohorte Asignada</label>
            <select class="wf-input">
              <option selected>Cohorte 2026-1 (Programa 2026-A)</option>
            </select>
          </div>
          <div class="col-md-3">
            <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Consultar Calificaciones</button>
          </div>
        </div>
      </div>

      <!-- Ficha del Alumno y Tabla de Calificaciones -->
      <div class="wf-card" style="background: #FFFFFF;">
        <div class="p-3 bg-light rounded border mb-4 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-3">
            <div class="user-avatar-circle" style="width: 44px; height: 44px; font-size: 16px; background: #081426; color: white;">JK</div>
            <div>
              <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin: 0;">Joaquín Küster</h4>
              <div class="small text-muted">DNI: 40.123.456 • joaquin.kuster@idoneos.online • Inscripción: Vigente</div>
            </div>
          </div>
          <div class="d-flex gap-3 text-end" style="font-size: 12px;">
            <div>
              <div class="text-muted">Promedio General:</div>
              <strong style="font-size: 16px; color: #16A34A;">9.5 / 10</strong>
            </div>
            <div>
              <div class="text-muted">Unidades Completadas:</div>
              <strong style="font-size: 16px; color: #2563EB;">2 de 3</strong>
            </div>
          </div>
        </div>

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>Unidad Temática</th>
                <th>Autoevaluación Rendida</th>
                <th>Fecha de Entrega</th>
                <th>Intentos Usados</th>
                <th>Nota Obtenida</th>
                <th>Resultado</th>
              </tr>
            </thead>
            <tbody>
              <!-- Unidad 1 -->
              <tr>
                <td><strong>Unidad 1: Marco Regulatorio</strong></td>
                <td>Autoevaluación Unidad 1: Marco Normativo y CNV</td>
                <td>10/04/2026 18:22</td>
                <td>1 / 3</td>
                <td><strong style="color: #16A34A; font-size: 15px;">9.00 / 10</strong></td>
                <td><span class="wf-badge status-active">Aprobado</span></td>
              </tr>

              <!-- Unidad 2 -->
              <tr>
                <td><strong>Unidad 2: Renta Fija</strong></td>
                <td>Autoevaluación Unidad 2: Ejercicios de Rendimiento</td>
                <td>22/04/2026 14:15</td>
                <td>1 / 3</td>
                <td><strong style="color: #16A34A; font-size: 15px;">10.00 / 10</strong></td>
                <td><span class="wf-badge status-active">Aprobado</span></td>
              </tr>

              <!-- Unidad 3 (Pendiente) -->
              <tr>
                <td><strong>Unidad 3: Derivados & Integración</strong></td>
                <td>Evaluación Final Integradora CNV</td>
                <td><span class="text-muted fst-italic">Pendiente de rendición</span></td>
                <td>0 / 3</td>
                <td><span class="text-muted">-</span></td>
                <td><span class="badge bg-light text-muted border">Sin rendir</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8G: BUSCAR CLASE EN VIVO --- CU-65
  if (id === 'CU-65') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h2>
            <div class="small text-muted mt-1">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Clases en Vivo</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Módulo Clases en Vivo</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Clases en Vivo</button>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros y Búsqueda -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar por título o docente</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Taller de Renta Fija, Mg. Valenzuela...">
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Estado de Transmisión</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Todos los estados (Programada / En vivo / Finalizada)</option>
                <option selected>En vivo / Programadas activas</option>
                <option>Solo Finalizadas</option>
              </select>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
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
              <th>Título de la Clase en Vivo</th>
              <th>Docente Titular</th>
              <th>Fecha, Hora & Duración</th>
              <th>Estado</th>
              <th class="text-end">Acciones Disponibles</th>
            </tr>
          </thead>
          <tbody>
            <!-- Fila 1: En Vivo Ahora Mismo -->
            <tr style="background: #FEF2F2; border-left: 4px solid #DC2626;">
              <td>
                <div class="d-flex align-items-center gap-2">
                  <strong style="color: #081426;">Clase Magistral: Resolución de Prácticos de Renta Fija</strong>
                  <span class="pin-badge">${badges[3] || 'D'}</span>
                </div>
                <div class="small text-muted">Unidad 2: Renta Fija • Cohorte 2026-1</div>
              </td>
              <td>Mg. Elena Valenzuela</td>
              <td>Hoy, 19:00 hs (Duración: 90 min)</td>
              <td><span class="wf-badge" style="background: #DC2626; color: white;">● EN VIVO</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#CU-71" class="wf-btn wf-btn-sm wf-btn-danger d-flex align-items-center gap-1" title="Finalizar Transmisión">
                    <i class="fa-solid fa-stop"></i>
                    <span>Finalizar Transmisión</span>
                  </a>
                  <a href="#CU-67" class="wf-btn wf-btn-sm wf-btn-outline" title="Reprogramar clase">
                    <i class="fa-solid fa-clock-rotate-left me-1"></i>
                    <span>Reprogramar / Editar</span>
                  </a>
                  <a href="#CU-68" class="wf-btn wf-btn-sm wf-btn-outline text-warning" style="color: #D97706; border-color: #FCD34D;" title="Cancelar Clase">
                    <i class="fa-solid fa-calendar-xmark me-1"></i>
                    <span>Cancelar Clase</span>
                  </a>
                  <a href="#CU-69" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Eliminar Registro">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Eliminar Registro</span>
                  </a>
                </div>
              </td>
            </tr>

            <!-- Fila 2: Programada Próxima Semana -->
            <tr>
              <td>
                <strong style="color: #081426;">Taller en Vivo: Análisis Técnico con TradingView</strong>
                <div class="small text-muted">Unidad 3: Renta Variable • Cohorte 2026-1</div>
              </td>
              <td>Mg. Elena Valenzuela</td>
              <td>Jueves 30/04, 19:00 hs (60 min)</td>
              <td><span class="wf-badge status-active">Programada</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#CU-70" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1">
                    <i class="fa-solid fa-tower-broadcast"></i>
                    <span>Iniciar Clase</span>
                  </a>
                  <a href="#CU-67" class="wf-btn wf-btn-sm wf-btn-outline">
                    <i class="fa-solid fa-pen-to-square me-1"></i>
                    <span>Reprogramar / Editar</span>
                  </a>
                  <a href="#CU-68" class="wf-btn wf-btn-sm wf-btn-outline text-warning" style="color: #D97706; border-color: #FCD34D;">
                    <i class="fa-solid fa-calendar-xmark me-1"></i>
                    <span>Cancelar Clase</span>
                  </a>
                  <a href="#CU-69" class="wf-btn wf-btn-sm wf-btn-outline text-danger">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Eliminar Registro</span>
                  </a>
                </div>
              </td>
            </tr>

            <!-- Fila 3: Finalizada -->
            <tr>
              <td>
                <strong style="color: #081426;">Clase Inaugural: Marco Legal CNV y Ética Profesional</strong>
                <div class="small text-muted">Unidad 1: Marco Regulatorio • Cohorte 2026-1</div>
              </td>
              <td>Mg. Elena Valenzuela</td>
              <td>Jueves 09/04, 19:00 hs (Grabación disponible)</td>
              <td><span class="wf-badge status-inactive">Finalizada</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline" title="Ver Grabación"><i class="fa-solid fa-video me-1"></i> Ver Grabación</button>
                  <a href="#CU-69" class="wf-btn wf-btn-sm wf-btn-outline text-danger">
                    <i class="fa-solid fa-trash me-1"></i>
                    <span>Eliminar Registro</span>
                  </a>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- SPECIALIZED 4: VER PARTICIPANTES ESTILO MOODLE FCEQYN --- CU-25
  if (id === 'CU-25') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 0;">Especialización en Idoneidad Bursátil</h2>
            <div class="small text-muted mt-1">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Participantes</span>
            </div>
          </div>
          <span class="wf-badge status-active">146 participantes encontrados</span>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso</a>
            <a href="#CU-23" class="wf-tab-btn">Cronograma</a>
            <a href="#CU-25" class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span>Participantes</a>
            <a href="#CU-62" class="wf-tab-btn">Calificaciones</a>
          </div>
        </div>
      </div>

      <!-- Filtros de Iniciales A-Z Moodle FCEQyN Virtual -->
      <div class="wf-card mb-3 p-3 bg-light border">
        <div class="d-flex flex-column gap-2" style="font-size: 11px;">
          <!-- Nombre A-Z -->
          <div class="d-flex flex-wrap align-items-center gap-1">
            <strong style="width: 70px; color: #081426;">Nombre:</strong>
            <span class="badge bg-dark text-white px-2 py-1 cursor-pointer">Todos</span>
            ${['A','B','C','D','E','F','G','H','I','J','K','L','M','N','Ñ','O','P','Q','R','S','T','U','V','W','X','Y','Z'].map(l => `<span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">${l}</span>`).join('')}
          </div>
          <!-- Apellido A-Z -->
          <div class="d-flex flex-wrap align-items-center gap-1">
            <strong style="width: 70px; color: #081426;">Apellido:</strong>
            <span class="badge bg-dark text-white px-2 py-1 cursor-pointer">Todos</span>
            ${['A','B','C','D','E','F','G','H','I','J','K','L','M','N','Ñ','O','P','Q','R','S','T','U','V','W','X','Y','Z'].map(l => `<span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">${l}</span>`).join('')}
          </div>
        </div>
      </div>

      <!-- Barra de Filtros Principales -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar por nombre, apellido o email</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Joaquín Küster, Elena Valenzuela...">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Rol o Cohorte</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option selected>Todos los participantes (Estudiantes y Docentes)</option>
                <option>Estudiantes (Cohorte 2026-1)</option>
                <option>Equipo Docente (Titulares y Ayudantes)</option>
              </select>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-filter me-1"></i> Filtrar</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabla de Participantes Estilo Moodle -->
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th style="width: 36px;"><input type="checkbox" style="width: 16px; height: 16px;"></th>
              <th>Nombre / Apellidos</th>
              <th>Roles</th>
              <th>Grupos / Cohorte</th>
              <th>Último acceso al curso</th>
            </tr>
          </thead>
          <tbody>
            <!-- Docente Titular -->
            <tr>
              <td><input type="checkbox" style="width: 16px; height: 16px;"></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 34px; height: 34px; font-size: 12px; background: #081426; color: white;">FS</div>
                  <div>
                    <a href="#" class="wf-link fw-bold" style="color: #081426; text-decoration: none;">Elena Valenzuela</a>
                    <div class="small text-muted">elena.valenzuela@idoneos.online</div>
                  </div>
                </div>
              </td>
              <td><span class="badge bg-primary text-white">Profesor</span></td>
              <td>Sin grupos</td>
              <td><span class="small text-success fw-bold">hace 18 minutos</span></td>
            </tr>

            <!-- Alumno 1 -->
            <tr>
              <td><input type="checkbox" style="width: 16px; height: 16px;"></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 34px; height: 34px; font-size: 12px; background: #2563EB; color: white;">JK</div>
                  <div>
                    <a href="#" class="wf-link fw-bold" style="color: #081426; text-decoration: none;">Joaquín Küster</a>
                    <div class="small text-muted">joaquin.kuster@idoneos.online</div>
                  </div>
                </div>
              </td>
              <td><span class="badge bg-light text-dark border">Estudiante</span></td>
              <td>Cohorte 2026-1</td>
              <td><span class="small text-muted">hace 2 horas</span></td>
            </tr>

            <!-- Alumno 2 -->
            <tr>
              <td><input type="checkbox" style="width: 16px; height: 16px;"></td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="user-avatar-circle" style="width: 34px; height: 34px; font-size: 12px; background: #0D9488; color: white;">MB</div>
                  <div>
                    <a href="#" class="wf-link fw-bold" style="color: #081426; text-decoration: none;">María Benítez</a>
                    <div class="small text-muted">maria.benitez@idoneos.online</div>
                  </div>
                </div>
              </td>
              <td><span class="badge bg-light text-dark border">Estudiante</span></td>
              <td>Cohorte 2026-1</td>
              <td><span class="small text-muted">126 días 21 horas</span></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Paginación Moodle -->
      <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
        <div class="small text-muted">Mostrando 1 - 20 de 146 participantes</div>
        <div class="d-flex gap-1" style="font-size: 12px;">
          <span class="badge bg-dark text-white px-2 py-1">1</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">2</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">3</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">4</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">5</span>
          <span class="badge bg-white text-dark border px-2 py-1 cursor-pointer">»</span>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 4C: REGISTRAR CONSULTA DE FORO --- CU-36
  if (id === 'CU-36') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span>Idoneidad Bursátil</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <a href="#CU-35" class="text-muted" style="text-decoration: none;">Foro de Consultas</a> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Añadir un nuevo tema de debate</strong>
            </div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 6px 0 0;"><i class="fa-solid fa-comments text-muted me-2"></i>Nueva Consulta de Foro</h2>
            <p class="small text-muted mb-0 mt-1">Plantee su duda pedagógica al equipo docente. Las consultas son públicas para todos los compañeros de cohorte.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Cohorte 2026-1</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="mt-4">
          <div class="mb-4">
            <label class="wf-label">Asunto / Título de la Consulta</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Duda con el cálculo de la Tasa Interna de Retorno (TIR) en bonos a tasa fija">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>

          <div class="mb-4">
            <label class="wf-label">Mensaje Pedagógico / Explicación del Inconveniente</label>
            <div class="wf-input-wrap">
              <textarea class="wf-input" rows="6" placeholder="Buenas tardes profesor, estoy intentando resolver el ejercicio 4 del TP 1 de la Unidad 2, pero me surge una discrepancia al descontar los cupones con fecha de liquidación T+2..."></textarea>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
            <a href="#CU-35" class="wf-btn wf-btn-outline">Cancelar</a>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-paper-plane me-1"></i> Publicar Consulta</button>
              <span class="pin-badge">${badges[3] || 'D'}</span>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 4D: MODIFICAR CONSULTA DE FORO --- CU-37
  if (id === 'CU-37') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF; max-width: 900px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-start pb-3 mb-4 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <a href="#CU-35" class="text-muted" style="text-decoration: none;">Foro de Consultas</a> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Editar Mensaje</strong>
            </div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 4px 0 0;"><i class="fa-solid fa-pen-to-square text-primary me-2"></i>Modificar Consulta de Foro</h3>
            <p class="small text-muted mb-0 mt-1">Dispone de hasta 30 minutos desde la publicación original para realizar modificaciones al texto.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-warning text-dark border"><i class="fa-regular fa-clock me-1"></i> Tiempo restante: 24 min</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="mb-4">
          <label class="wf-label">Asunto / Título del Debate</label>
          <input type="text" class="wf-input" value="Duda con TP N° 1: Punto 4 - Cálculo de TIR y Duración de Bonos">
        </div>

        <div class="mb-4">
          <label class="wf-label">Texto de la Consulta</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="7">Buenas tardes profesor,\n\nEstoy intentando resolver el ejercicio 4 del Trabajo Práctico 1 correspondiente a la Unidad 2 sobre Bonos Soberanos AL30.\nAl calcular la duración modificada con una tasa de descuento anualizada del 14.5%, la fórmula me da un resultado de 2.85 años, pero el apunte de cátedra indica 3.12 años.\n\n¿Debo considerar la amortización parcial semestral en el flujo del período 2026? Agradezco mucho su aclaración.</textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-35" class="wf-btn wf-btn-outline">Cancelar</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 4E: BUSCAR RESPUESTA / VER HILO DE FORO ESTILO MOODLE FCEQYN --- CU-39
  if (id === 'CU-39') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Página Principal</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <a href="#CU-35" class="text-muted" style="text-decoration: none;">Foro PostgreSQL</a> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Duda con TP N° 1: Punto 4</strong>
            </div>
            <h2 style="font-size: 20px; font-weight: 800; color: #081426; margin: 6px 0 0;">Duda con TP N° 1: Punto 4 - Cálculo de TIR y Duración de Bonos</h2>
            <div class="small text-muted mt-1">
              <span class="text-muted">◄ Tema anterior: <a href="#" class="text-primary" style="text-decoration: none;">Régimen Informativo CNV</a></span>
              <span class="mx-2">•</span>
              <span class="text-muted">Tema siguiente: <a href="#" class="text-primary" style="text-decoration: none;">Diferencia entre ALyC Propio e Integral</a> ►</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <a href="#CU-35" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-arrow-left me-1"></i> Volver al Foro</a>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center py-2 border-bottom mb-4 bg-light px-3 rounded">
          <span class="small text-muted">Modo de visualización: <strong>Mostrar respuestas anidadas</strong></span>
          <div class="dropdown">
            <button class="wf-btn wf-btn-sm wf-btn-outline" style="font-size: 11px;">Configuraciones <i class="fa-solid fa-chevron-down ms-1"></i></button>
          </div>
        </div>

        <!-- Mensaje Raíz de la Consulta (Alumno) -->
        <div class="p-3 border rounded bg-white mb-4 shadow-sm" style="border-left: 4px solid #2563EB !important;">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-circle" style="width: 38px; height: 38px; font-size: 13px; background: #2563EB; color: white;">JK</div>
              <div>
                <strong style="font-size: 13px; color: #081426;">Duda con TP N° 1: Punto 4 - Cálculo de TIR y Duración</strong>
                <div class="small text-muted">de <strong style="color: #081426;">Joaquín Küster</strong> - miércoles, 11 de abril de 2026, 14:20</div>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <a href="#CU-37" class="wf-btn wf-btn-sm wf-btn-outline" style="font-size: 10px; height: 28px; padding: 0 8px;"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
              <a href="#CU-38" class="wf-btn wf-btn-sm wf-btn-outline text-danger" style="font-size: 10px; height: 28px; padding: 0 8px;"><i class="fa-solid fa-trash me-1"></i> Borrar</a>
            </div>
          </div>
          <div style="font-size: 13px; color: #1E293B; line-height: 1.6; padding-left: 48px;">
            <p>Buenas tardes profesor,</p>
            <p>Estoy intentando calcular la duración modificada y la TIR para el bono soberano AL30 según el enunciado del TP 1. Al descontar los cupones semestrales me surge una discrepancia con el valor de la guía teórica.</p>
            <p class="mb-0">¿Debemos considerar la convención 30/360 o Actual/Actual para el devengamiento de intereses?</p>
          </div>
          <div class="d-flex justify-content-end gap-3 mt-3 pt-2 border-top" style="font-size: 11px;">
            <a href="#" class="text-muted" style="text-decoration: none;">Enlace permanente</a>
            <a href="#CU-40" class="text-primary fw-bold" style="text-decoration: none;"><i class="fa-solid fa-reply me-1"></i> Responder</a>
          </div>
        </div>

        <!-- Respuesta 1 Anidada (Docente Titular) -->
        <div class="p-3 border rounded bg-light mb-3 shadow-sm" style="margin-left: 48px; border-left: 4px solid var(--wf-navy) !important;">
          <div class="d-flex justify-content-between align-items-start mb-3">
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-circle" style="width: 36px; height: 36px; font-size: 12px; background: #081426; color: white;">FS</div>
              <div>
                <div class="d-flex align-items-center gap-2">
                  <strong style="font-size: 13px; color: #081426;">Re: Duda con TP N° 1: Punto 4</strong>
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
                <div class="small text-muted">de <strong style="color: #081426;">Mg. Elena Valenzuela</strong> (Docente Titular) - jueves, 12 de abril de 2026, 10:15</div>
              </div>
            </div>
            <span class="badge bg-primary text-white">Profesor</span>
          </div>
          <div style="font-size: 13px; color: #1E293B; line-height: 1.6; padding-left: 46px;">
            <p>Estimado Joaquín,</p>
            <p>Para los bonos soberanos emitidos bajo ley argentina (como el AL30) la convención estándar de mercado utilizada por el MAE y BYMA es <strong>Actual/Actual</strong> para el cálculo del cupón corrido y TIR.</p>
            <p class="mb-0">Revisa también que estés tomando el precio Clean para calcular el rendimiento hasta el vencimiento.</p>
          </div>
          <div class="d-flex justify-content-end gap-3 mt-3 pt-2 border-top" style="font-size: 11px;">
            <a href="#" class="text-muted" style="text-decoration: none;">Enlace permanente</a>
            <a href="#CU-40" class="text-primary fw-bold" style="text-decoration: none;"><i class="fa-solid fa-reply me-1"></i> Responder</a>
          </div>
        </div>

        <!-- Respuesta 2 Anidada de Agradecimiento (Alumno) -->
        <div class="p-3 border rounded bg-white mb-3 shadow-sm" style="margin-left: 96px; border-left: 4px solid #10B981 !important;">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-circle" style="width: 32px; height: 32px; font-size: 11px; background: #2563EB; color: white;">JK</div>
              <div>
                <strong style="font-size: 12px; color: #081426;">Solucionado, muchas gracias</strong>
                <div class="small text-muted">de <strong style="color: #081426;">Joaquín Küster</strong> - viernes, 13 de abril de 2026, 09:05</div>
              </div>
            </div>
            <span class="badge bg-success text-white" style="font-size: 10px;">Solucionado</span>
          </div>
          <div style="font-size: 12px; color: #334155; line-height: 1.5; padding-left: 42px;">
            <p class="mb-0">Excelente profesor, al cambiar a convención Actual/Actual la duración me dio exactamente 3.12 años. Ya pude subir la entrega del práctico.</p>
          </div>
          <div class="d-flex justify-content-end gap-3 mt-2 pt-2 border-top" style="font-size: 11px;">
            <a href="#" class="text-muted" style="text-decoration: none;">Enlace permanente</a>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 4A: REGISTRAR RESPUESTA DE FORO --- CU-40
  if (id === 'CU-40') {
    return `
      <div class="wf-card" style="max-width: 780px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <div class="d-flex align-items-center gap-2">
              <h3 style="font-size: 18px; font-weight: 700; color: #081426; margin: 0;"><i class="fa-solid fa-comments text-muted me-2"></i>Responder Consulta de Foro</h3>
              <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>Responder</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
            </div>
            <p class="small text-muted" style="margin: 3px 0 0;">Consulta del alumno: <em>¿Cómo se calcula la TIR en bonos soberanos a tasa fija?</em></p>
          </div>
          <span class="wf-badge status-active">Consulta Activa</span>
        </div>

        <div class="mb-4">
          <label class="wf-label">Texto de la Respuesta</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="6" placeholder="Redacte aquí su respuesta académica al alumno...">La TIR (Tasa Interna de Retorno) de un bono soberano se calcula mediante el método iterativo de flujos descontados, igualando el precio de mercado con el valor presente neto de todos los cupones y el capital de amortización al vencimiento...</textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-35" class="wf-btn wf-btn-outline">Cancelar</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-paper-plane me-1"></i> Enviar Respuesta</button>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 5: MODAL DE AGREGAR CONTENIDO --- CU-28, CU-32, CU-58, CU-66
  if (['CU-28', 'CU-32', 'CU-58', 'CU-66'].includes(id)) {
    const isMaterial = id === 'CU-28';
    const isGlosario = id === 'CU-32';
    const isEval = id === 'CU-58';
    const isLive = id === 'CU-66';

    let modalTriggerLabel = 'Añade una actividad o un recurso';
    if (id === 'CU-58') modalTriggerLabel = 'Nueva Autoevaluación';
    if (id === 'CU-66') modalTriggerLabel = 'Programar clase en vivo';

    if (isEval) {
      return `
        <div class="wf-modal-box" style="max-width: 900px; margin: 20px auto; background: #FFFFFF;">
          <div class="wf-modal-header d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <h3 class="wf-modal-title m-0" style="font-size: 18px; font-weight: 800; color: #081426;">Nueva Autoevaluación</h3>
              <span class="wf-btn wf-btn-xs wf-btn-outline active" style="font-weight: 700;">
                <i class="fa-solid fa-clipboard-check me-1 text-success"></i> ${modalTriggerLabel}
              </span>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
            <span style="font-size: 18px; color: #94A3B8; cursor: pointer;"><i class="fa-solid fa-xmark"></i></span>
          </div>

          <div class="row g-3">
            <div class="col-12">
              <label class="wf-label">Nombre de la Autoevaluación</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Autoevaluación Unidad 2: Instrumentos de Renta Fija">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Tiempo Límite (minutos)</label>
              <div class="wf-input-wrap">
                <input type="number" class="wf-input" value="20">
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Cantidad de Preguntas Sorteables</label>
              <div class="wf-input-wrap">
                <input type="number" class="wf-input" value="10">
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Fecha y Hora de Apertura</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="19/04/2026 00:00">
                <span class="pin-badge">${badges[4] || 'E'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Fecha y Hora de Cierre (Opcional)</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="09/05/2026 23:59">
                <span class="pin-badge">${badges[5] || 'F'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Intentos Permitidos</label>
              <div class="wf-input-wrap">
                <input type="number" class="wf-input" value="3">
                <span class="pin-badge">${badges[6] || 'G'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Pools de Preguntas Asociados</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option selected>Pool Unidad 2: Renta Fija y Bonos (30 preguntas)</option>
                  <option>Pool Unidad 1: Marco Regulatorio (25 preguntas)</option>
                </select>
                <span class="pin-badge">${badges[7] || 'H'}</span>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-4 mt-3 border-top">
            <button class="wf-btn wf-btn-outline wf-btn-sm">Cancelar</button>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary wf-btn-sm"><i class="fa-solid fa-plus me-1"></i> Agregar</button>
              <span class="pin-badge">${badges[8] || badges[badges.length - 1] || 'I'}</span>
            </div>
          </div>
        </div>
      `;
    }

    if (isLive) {
      return `
        <div class="wf-modal-box" style="max-width: 860px; margin: 20px auto; background: #FFFFFF;">
          <div class="wf-modal-header d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <h3 class="wf-modal-title m-0" style="font-size: 18px; font-weight: 800; color: #081426;">Programar Clase en Vivo</h3>
              <span class="wf-btn wf-btn-xs wf-btn-outline active" style="font-weight: 700;">
                <i class="fa-solid fa-video me-1 text-danger"></i> ${modalTriggerLabel}
              </span>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
            <span style="font-size: 18px; color: #94A3B8; cursor: pointer;"><i class="fa-solid fa-xmark"></i></span>
          </div>

          <div class="row g-3">
            <div class="col-12">
              <label class="wf-label">Título de la Sesión en Vivo</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Clase Magistral: Resolución de Prácticos de Renta Fija y Valuación">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Cohorte Destinataria</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option selected>Cohorte 2026-1 (Modalidad con clases en vivo)</option>
                  <option>Cohorte 2026-2</option>
                </select>
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Fecha y Hora de Inicio</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="28/08/2026 19:00 hs">
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Duración Estimada (minutos)</label>
              <div class="wf-input-wrap">
                <input type="number" class="wf-input" value="90">
                <span class="pin-badge">${badges[4] || 'E'}</span>
              </div>
            </div>

            <div class="col-md-6">
              <label class="wf-label">Enlace / Servidor a la Sala de Streaming</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="rtmp://live.idoneos.online/app/stream_u2_valenzuela">
                <span class="pin-badge">${badges[5] || 'F'}</span>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-4 mt-3 border-top">
            <button class="wf-btn wf-btn-outline wf-btn-sm">Cancelar</button>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary wf-btn-sm"><i class="fa-solid fa-plus me-1"></i> Agregar</button>
              <span class="pin-badge">${badges[6] || badges[badges.length - 1] || 'G'}</span>
            </div>
          </div>
        </div>
      `;
    }

    let selectedTitle = 'Material / Documento PDF';
    let inputLabel = 'Nombre / Título del material';
    let inputValue = 'Guía Teórica de Renta Fija v2.0 (PDF)';

    if (isGlosario) {
      selectedTitle = 'Glosario de Términos';
      inputLabel = 'Término a registrar';
      inputValue = 'TIR (Tasa Interna de Retorno)';
    }

    return `
      <div class="wf-modal-box" style="max-width: 900px; margin: 20px auto;">
        <div class="wf-modal-header d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-3">
            <h3 class="wf-modal-title m-0">Añade una actividad o un recurso</h3>
            <span class="wf-btn wf-btn-xs wf-btn-outline active" style="font-weight: 700;">
              <i class="fa-solid fa-plus me-1"></i> ${modalTriggerLabel}
            </span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
          <span style="font-size: 18px; color: #94A3B8; cursor: pointer;"><i class="fa-solid fa-xmark"></i></span>
        </div>

        <div class="wf-modal-grid">
          <div class="wf-modal-sidebar">
            <div>
              <div class="small fw-bold text-muted text-uppercase mb-3">ACTIVIDADES & RECURSOS</div>
              <div class="d-flex flex-column gap-1">
                <div class="wf-modal-option ${isMaterial ? 'active' : ''}">
                  <i class="fa-solid fa-file-pdf text-primary"></i>
                  <span>Material / Documento PDF</span>
                </div>
                <div class="wf-modal-option ${isGlosario ? 'active' : ''}">
                  <i class="fa-solid fa-book-open text-warning"></i>
                  <span>Glosario de Términos</span>
                </div>
                <div class="wf-modal-option">
                  <i class="fa-solid fa-clipboard-check text-success"></i>
                  <span>Cuestionario / Autoevaluación</span>
                </div>
                <div class="wf-modal-option">
                  <i class="fa-solid fa-list-check text-info"></i>
                  <span>Pool de Preguntas</span>
                </div>
                <div class="wf-modal-option">
                  <i class="fa-solid fa-comments text-muted"></i>
                  <span>Foro de Consultas</span>
                </div>
                <div class="wf-modal-option">
                  <i class="fa-solid fa-video text-danger"></i>
                  <span>Clase en Vivo (Streaming)</span>
                </div>
              </div>
            </div>
          </div>

          <div class="wf-modal-content">
            <div>
              <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin-bottom: 16px;">${selectedTitle}</h4>
              
              <div class="mb-4">
                <label class="wf-label">${inputLabel}</label>
                <div class="wf-input-wrap">
                  <input type="text" class="wf-input" value="${inputValue}">
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </div>

              ${isMaterial ? `
                <div class="mb-4">
                  <label class="wf-label">Archivo adjunto o URL</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="guia_teorica_u1.pdf">
                    <button class="wf-btn wf-btn-outline wf-btn-sm"><i class="fa-solid fa-folder-open me-1"></i> Examinar...</button>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isGlosario ? `
                <div class="mb-4">
                  <label class="wf-label">Definición conceptual</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="4">Tasa que iguala el valor actual de los flujos de fondos con el precio del bono.</textarea>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}
            </div>

            <div class="d-flex justify-content-end align-items-center gap-3 pt-4 border-top">
              <button class="wf-btn wf-btn-outline wf-btn-sm">Cancelar</button>
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-primary wf-btn-sm"><i class="fa-solid fa-plus me-1"></i> Agregar</button>
                <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 5B: FINALIZAR CLASE EN VIVO --- CU-71
  if (id === 'CU-71') {
    return `
      <div class="wf-modal-box" style="max-width: 680px; margin: 30px auto; background: #FFFFFF; border-top: 5px solid #16A34A;">
        <div class="wf-modal-header d-flex justify-content-between align-items-center bg-white border-bottom p-4">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 50%; background: #DCFCE7; color: #16A34A; display: flex; align-items: center; justify-content: center; font-size: 20px;">
              <i class="fa-solid fa-circle-check"></i>
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Clase en Vivo Finalizada con Éxito</h3>
              <p class="small text-muted mb-0 mt-1">Transmisión sincrónica concluida y grabación procesada.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active" style="background: #DCFCE7; color: #166534; border-color: #86EFAC;">Transmisión Finalizada</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="p-4 bg-white">
          <!-- Detalle de la Operación y Notificaciones Automáticas -->
          <div class="d-flex flex-column gap-3 mb-4" style="font-size: 13px;">
            <div class="p-3 bg-light rounded border">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <strong style="color: #081426; font-size: 14px;">Taller de Análisis Práctico de Casos CNV</strong>
                <span class="badge bg-dark text-white">Unidad 2: Renta Fija</span>
              </div>
              <div class="small text-muted mb-2">
                <i class="fa-solid fa-clock me-1 text-primary"></i> Duración total registrada: <strong>01:28:45 hs</strong> • Docente: <strong>Mg. Elena Valenzuela</strong>
              </div>
              <div class="small text-muted">
                <i class="fa-solid fa-satellite-dish me-1 text-danger"></i> Orden de corte de señal y grabación enviada a OBS Studio remoto con éxito.
              </div>
            </div>

            <!-- Lista de Resultados Automáticos -->
            <div class="d-flex flex-column gap-2" style="font-size: 12px;">
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light">
                <span class="text-muted"><i class="fa-solid fa-video text-primary me-2"></i>Grabación Generada:</span>
                <strong class="text-dark">clase_vivo_u2_valenzuela_hd.mp4 (1080p)</strong>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light">
                <span class="text-muted"><i class="fa-solid fa-file-arrow-up text-success me-2"></i>Carga como Material:</span>
                <strong class="text-success"><i class="fa-solid fa-check-circle me-1"></i> Publicado en Unidad 2 (Tipo Grabación)</strong>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light">
                <span class="text-muted"><i class="fa-solid fa-bell text-warning me-2"></i>Notificación a Estudiantes:</span>
                <strong class="text-dark">Enviada a 146 alumnos inscriptos</strong>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light">
                <span class="text-muted"><i class="fa-solid fa-calendar-check text-info me-2"></i>Vigencia de Disponibilidad:</span>
                <span class="fw-bold">Disponible por 4 meses (Eliminación automática programada)</span>
              </div>
            </div>
          </div>

          <div class="p-3 bg-light rounded border small text-muted text-center" style="font-style: italic;">
            <i class="fa-solid fa-circle-info text-primary me-1"></i> El sistema ha guardado todos los cambios y notificado a la cohorte. No se requieren acciones adicionales.
          </div>
        </div>

        <div class="p-3 bg-light border-top d-flex justify-content-end align-items-center">
          <a href="#CU-65" class="wf-btn wf-btn-sm wf-btn-primary" style="font-weight: 700;">
            <i class="fa-solid fa-arrow-left me-1"></i> Volver a Clases en Vivo
          </a>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 6: ESTUDIO DE CLON DIGITAL CON IA (HEYGEN) --- CU-76
  if (id === 'CU-76') {
    return `
      <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
        <!-- Cabecera -->
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
              <i class="fa-solid fa-user-astronaut" style="font-size: 20px;"></i>
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Configuración de Avatar y Clon de Voz (HeyGen)</h3>
              <p class="small text-muted" style="margin: 2px 0 0;">Calibre su fotografía facial y muestra de locución para sintetizar su clon docente.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Docente Habilitado</span>
          </div>
        </div>

        <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
          <!-- Columna Izquierda: Línea de Tiempo de Pasos (Timeline Visual) -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div style="position: relative; padding-left: 36px;">
              <!-- Línea vertical conectora continua -->
              <div style="position: absolute; left: 14px; top: 12px; bottom: 24px; width: 3px; background: #E2E8F0; border-radius: 2px;"></div>

              <!-- Paso 1: Rostro -->
              <div style="position: relative; margin-bottom: 24px;">
                <!-- Nodo circular Paso 1 -->
                <div style="position: absolute; left: -36px; top: 0; width: 28px; height: 28px; border-radius: 50%; background: #081426; color: var(--wf-gold); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800; border: 3px solid #FFFFFF; box-shadow: 0 2px 8px rgba(8,20,38,0.25); z-index: 2;">
                  1
                </div>
                <div class="p-3 border rounded bg-white shadow-sm" style="border-left: 3px solid var(--wf-gold) !important;">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <strong style="font-size: 14px; color: #081426;">Paso 1: Captura Facial del Docente</strong>
                    <span class="badge bg-light text-dark border" style="font-size: 10px;">Cámara HD Activa</span>
                  </div>
                  <p class="small text-muted mb-3" style="font-size: 11px;">Mire de frente con buena iluminación natural para calibrar el modelo 3D de HeyGen.</p>
                  <div class="d-flex align-items-center justify-content-between pt-2 border-top">
                    <span class="small text-muted"><i class="fa-solid fa-image me-1 text-primary"></i> Imagen: <strong>foto_docente_hd.jpg</strong></span>
                    <div class="d-flex align-items-center gap-2">
                      <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
                        <i class="fa-solid fa-camera"></i> <span>Tomar foto facial</span>
                      </button>
                      <span class="pin-badge">${badges[1] || 'B'}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Paso 2: Voz -->
              <div style="position: relative;">
                <!-- Nodo circular Paso 2 -->
                <div style="position: absolute; left: -36px; top: 0; width: 28px; height: 28px; border-radius: 50%; background: #2563EB; color: #FFFFFF; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800; border: 3px solid #FFFFFF; box-shadow: 0 2px 8px rgba(37,99,235,0.3); z-index: 2;">
                  2
                </div>
                <div class="p-3 border rounded bg-white shadow-sm" style="border-left: 3px solid #2563EB !important;">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <strong style="font-size: 14px; color: #081426;">Paso 2: Muestra de Audio y Voz</strong>
                    <span class="badge bg-light text-primary border" style="font-size: 10px;">Micrófono 48kHz</span>
                  </div>
                  <div class="p-2 bg-light rounded border mb-2 small text-secondary" style="font-style: italic; font-size: 11px;">
                    "Bienvenidos a la cátedra de Mercado de Capitales. Hoy analizaremos la curva de rendimiento y la duración modificada."
                  </div>
                  <div class="d-flex align-items-center justify-content-between pt-2 border-top">
                    <span class="small text-muted"><i class="fa-solid fa-wave-square me-1 text-primary"></i> Audio: <strong>00:32 / 00:30 min</strong></span>
                    <div class="d-flex align-items-center gap-2">
                      <button class="wf-btn wf-btn-sm wf-btn-outline text-danger d-flex align-items-center gap-1">
                        <i class="fa-solid fa-microphone"></i> <span>Grabar Muestra</span>
                      </button>
                      <span class="pin-badge">${badges[2] || 'C'}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Barra Divisoria Vertical en el medio -->
          <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

          <!-- Columna Derecha: Resumen de Calibración con Avatar Completo -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="p-4 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid var(--wf-gold) !important;">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <div class="small fw-bold text-muted text-uppercase">Resumen de Calibración de Avatar</div>
                  <span class="badge bg-success text-white" style="font-size: 10px;"><i class="fa-solid fa-check me-1"></i> Biometría Óptima</span>
                </div>
                
                <!-- Preview Visual del Avatar Completo de la Docente -->
                <div style="height: 240px; background: linear-gradient(180deg, #0F172A 0%, #081426 100%); border-radius: 8px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #1E293B;">
                  <img src="${avatarDocenteImg}" style="height: 100%; width: auto; max-width: 100%; object-fit: contain; display: block;" alt="Foto Facial Docente">
                  
                  <div style="position: absolute; top: 10px; left: 10px; background: rgba(22,163,74,0.9); color: white; padding: 3px 8px; border-radius: 4px; font-size: 10px; font-weight: 700;">
                    <i class="fa-solid fa-camera me-1"></i> FOTO OK
                  </div>
                  
                  <div style="position: absolute; top: 10px; right: 10px; background: rgba(8,20,38,0.85); border: 1px solid rgba(255,255,255,0.2); color: var(--wf-gold); padding: 3px 8px; border-radius: 4px; font-size: 10px; font-weight: 700;">
                    HeyGen Instant Avatar v2
                  </div>

                  <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(0deg, rgba(8,20,38,0.95) 0%, rgba(8,20,38,0.7) 70%, transparent 100%); padding: 10px 14px; display: flex; justify-content: space-between; align-items: flex-end;">
                    <div>
                      <strong style="color: white; font-size: 13px;">Mg. Elena Valenzuela</strong>
                      <div style="font-size: 10px; color: #94A3B8;">Docente Titular • #avatar_valenzuela_v2</div>
                    </div>
                    <span style="font-size: 10px; color: #38BDF8; font-weight: 600;">1080p HD</span>
                  </div>
                </div>

                <div class="d-flex flex-column gap-2 mt-3" style="font-size: 11px;">
                  <div class="d-flex justify-content-between p-2 border rounded bg-light">
                    <span class="text-muted">Estado Biométrico:</span>
                    <strong class="text-success"><i class="fa-solid fa-circle-check me-1"></i> Calibración Facial Completa</strong>
                  </div>
                  <div class="d-flex justify-content-between p-2 border rounded bg-light">
                    <span class="text-muted">Identificador del Clon:</span>
                    <span class="fw-bold text-dark">#avatar_valenzuela_v2</span>
                  </div>
                </div>
              </div>

              <div class="pt-3 mt-3 border-top d-flex justify-content-end align-items-center gap-2">
                <a href="#CU-86" class="wf-btn wf-btn-sm wf-btn-outline">Cancelar</a>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-2" style="font-weight: 700; background: #7C3AED; border-color: #6D28D9;">
                    <i class="fa-solid fa-wand-magic-sparkles"></i>
                    <span>Crear Clon en HeyGen</span>
                  </button>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 7A: BUSCAR CLASE CON CLON IA --- CU-77
  if (id === 'CU-77') {
    return `
      <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Clases con Clon de IA (HeyGen)</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Listado y búsqueda de videos generados mediante síntesis de avatar hiperrealista y voz.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Módulo de Clones IA</span>
          </div>
        </div>

        <!-- Filtros de Búsqueda -->
        <div class="p-3 bg-light rounded border mb-4">
          <div class="row g-3 align-items-end">
            <div class="col-md-5">
              <label class="wf-label">Unidad Académica</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option selected>Unidad 2: Instrumentos de Renta Fija (Bonos y ONs)</option>
                  <option>Unidad 1: Marco Regulatorio Bursátil</option>
                </select>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="wf-label">Estado de Generación</label>
              <select class="wf-input">
                <option selected>Todos los estados (Generada, Pendiente, Error)</option>
                <option>Generada (Lista)</option>
                <option>Pendiente de Render</option>
                <option>Error</option>
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

        <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
          <!-- Columna Izquierda: Cards de Clases con Clon y Paginación -->
          <div style="flex: 1 1 50%; min-width: 320px;" class="d-flex flex-column justify-content-between">
            <div class="d-flex flex-column gap-3">
              <!-- Card 1: Seleccionada / Activa -->
              <div class="p-3 rounded border shadow-sm" style="background: #F8FAFC; border-left: 4px solid var(--wf-gold) !important;">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <div class="d-flex align-items-center gap-2">
                    <div style="width: 34px; height: 34px; border-radius: 8px; background: #081426; color: var(--wf-gold); display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0;">
                      <i class="fa-solid fa-play"></i>
                    </div>
                    <div>
                      <strong style="color: #081426; font-size: 13px;">Explicación Teórica: Duración Modificada</strong>
                      <div class="small text-muted" style="font-size: 11px;">Avatar Docente • 03:40 min • 1080p HD</div>
                    </div>
                  </div>
                  <span class="wf-badge status-active">Generada</span>
                </div>
                <div class="d-flex justify-content-end align-items-center gap-2 pt-2 border-top">
                  <button class="wf-btn wf-btn-xs wf-btn-primary" style="font-size: 11px; height: 30px; padding: 0 10px;">
                    <i class="fa-solid fa-eye me-1"></i> Ver Video
                  </button>
                  <span class="pin-badge">${badges[3] || 'D'}</span>
                  <a href="#CU-79" class="wf-btn wf-btn-xs wf-btn-outline" style="font-size: 11px; height: 30px; padding: 0 10px;">
                    <i class="fa-solid fa-pen-to-square me-1"></i> Editar Guión
                  </a>
                </div>
              </div>

              <!-- Card 2: Generada -->
              <div class="p-3 rounded border bg-white shadow-sm">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <div class="d-flex align-items-center gap-2">
                    <div style="width: 34px; height: 34px; border-radius: 8px; background: #F1F5F9; color: #475569; display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0;">
                      <i class="fa-solid fa-play"></i>
                    </div>
                    <div>
                      <strong style="color: #081426; font-size: 13px;">Introducción a la Ley de Mercado de Capitales</strong>
                      <div class="small text-muted" style="font-size: 11px;">Avatar Docente • 05:12 min • 1080p HD</div>
                    </div>
                  </div>
                  <span class="wf-badge status-active">Generada</span>
                </div>
                <div class="d-flex justify-content-end align-items-center gap-2 pt-2 border-top">
                  <button class="wf-btn wf-btn-xs wf-btn-outline" style="font-size: 11px; height: 30px; padding: 0 10px;">
                    <i class="fa-solid fa-eye me-1"></i> Ver Video
                  </button>
                  <a href="#CU-79" class="wf-btn wf-btn-xs wf-btn-outline" style="font-size: 11px; height: 30px; padding: 0 10px;">
                    <i class="fa-solid fa-pen-to-square me-1"></i> Editar Guión
                  </a>
                </div>
              </div>

              <!-- Card 3: Pendiente de Procesamiento -->
              <div class="p-3 rounded border bg-white shadow-sm">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <div class="d-flex align-items-center gap-2">
                    <div style="width: 34px; height: 34px; border-radius: 8px; background: #FEF3C7; color: #D97706; display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0;">
                      <i class="fa-solid fa-spinner fa-spin"></i>
                    </div>
                    <div>
                      <strong style="color: #081426; font-size: 13px;">Valuación de Bonos Bullet vs Amortizables</strong>
                      <div class="small text-muted" style="font-size: 11px;">En cola de procesamiento HeyGen...</div>
                    </div>
                  </div>
                  <span class="badge bg-light text-warning border" style="font-size: 10px;">Pendiente</span>
                </div>
                <div class="d-flex justify-content-end align-items-center gap-2 pt-2 border-top">
                  <span class="small text-muted fst-italic" style="font-size: 10px;"><i class="fa-solid fa-clock me-1"></i> Renderizando síntesis...</span>
                </div>
              </div>
            </div>

            <!-- Paginación de Cards -->
            <div class="d-flex justify-content-between align-items-center pt-3 mt-3 border-top" style="font-size: 11px;">
              <span class="text-muted">Mostrando 1-3 de 3 clases con clon</span>
              <div class="d-flex align-items-center gap-1">
                <button class="wf-btn wf-btn-xs wf-btn-outline" disabled style="height: 26px; padding: 0 8px;">«</button>
                <button class="wf-btn wf-btn-xs wf-btn-primary" style="height: 26px; min-width: 26px; padding: 0 6px;">1</button>
                <button class="wf-btn wf-btn-xs wf-btn-outline" style="height: 26px; min-width: 26px; padding: 0 6px;">2</button>
                <button class="wf-btn wf-btn-xs wf-btn-outline" style="height: 26px; padding: 0 8px;">»</button>
              </div>
            </div>
          </div>

          <!-- Barra Divisoria Vertical en el medio -->
          <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

          <!-- Columna Derecha: Panel de Previsualización del Video con Avatar Completo -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="p-4 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid var(--wf-gold) !important;">
              <div>
                <div class="d-flex justify-content-between align-items-center pb-2 mb-3 border-bottom">
                  <div>
                    <span class="small text-muted">Previsualización de Clase con Clon</span>
                    <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin: 2px 0 0;">Explicación Teórica: Duración Modificada</h4>
                  </div>
                </div>

                <!-- Reproductor de Video Simulado con Imagen Completa de la Docente -->
                <div style="height: 240px; background: linear-gradient(180deg, #0F172A 0%, #081426 100%); border-radius: 8px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #1E293B; margin-bottom: 12px;">
                  <img src="${avatarDocenteImg}" style="height: 100%; width: auto; max-width: 100%; object-fit: contain; display: block;" alt="Foto Clon Docente">
                  
                  <div style="position: absolute; top: 10px; right: 10px; background: rgba(0,0,0,0.7); border: 1px solid rgba(255,255,255,0.2); padding: 3px 8px; border-radius: 4px; font-size: 10px; color: var(--wf-gold); font-weight: 700;">
                    <i class="fa-solid fa-wand-magic-sparkles me-1"></i> HeyGen AI Studio
                  </div>

                  <div style="position: absolute; bottom: 8px; left: 8px; right: 8px; display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.75); padding: 6px 12px; border-radius: 6px; font-size: 11px; color: white;">
                    <div class="d-flex align-items-center gap-2">
                      <i class="fa-solid fa-play text-warning"></i>
                      <span>01:24 / 03:40</span>
                    </div>
                    <span class="badge bg-secondary" style="font-size: 9px;">1080p HD</span>
                  </div>
                </div>

                <!-- Guión Sincronizado -->
                <div class="p-2 border rounded bg-light mb-3" style="font-size: 11px;">
                  <strong class="text-dark mb-1 d-block">Guión del Prompt Sintetizado:</strong>
                  <p class="text-muted m-0" style="line-height: 1.4; font-style: italic;">
                    "En esta clase abordaremos el concepto de modified duration. Cuando la tasa de interés se incrementa, el precio de los títulos cae en proporción inversa..."
                  </p>
                </div>
              </div>

              <div class="d-flex justify-content-between align-items-center pt-2 border-top">
                <a href="#CU-79" class="wf-btn wf-btn-xs wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar Guión</a>
                <a href="#CU-80" class="wf-btn wf-btn-xs wf-btn-outline text-danger"><i class="fa-solid fa-trash me-1"></i> Eliminar Video</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 7B: GENERAR CLASE CON CLON IA --- CU-78
  if (id === 'CU-78') {
    return `
      <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
              <i class="fa-solid fa-video" style="font-size: 20px;"></i>
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Generar Clase con Clon de IA</h3>
              <p class="small text-muted" style="margin: 3px 0 0;">Genere una clase audiovisual mediante síntesis en HeyGen a partir de su guión prompt.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active"><i class="fa-solid fa-circle-check me-1"></i> Avatar & Voz Calibrada</span>
          </div>
        </div>

        <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
          <!-- Columna Izquierda: Formulario Real (Título y Prompt) -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="mb-3">
              <label class="wf-label">Título de la Clase</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" placeholder="Ej: Explicación Teórica: Duración Modificada y Convexidad" value="Explicación Teórica: Duración Modificada y Convexidad en Bonos">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>

            <div class="mb-3">
              <label class="wf-label">Guión de la Clase (Prompt de Texto)</label>
              <div class="wf-input-wrap">
                <textarea class="wf-input" rows="7" placeholder="Redacte el guión que el avatar de HeyGen sintetizará...">En esta clase abordaremos el concepto de modified duration. Cuando la tasa de interés se incrementa, el precio de los títulos cae en proporción inversa a su duración ponderada. Analizaremos la aproximación por series de Taylor y los ejercicios prácticos del examen CNV.</textarea>
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
              <div class="small text-muted mt-1" style="font-size: 11px;">El guión será enviado a HeyGen junto con el avatar_id y voice_id registrados en su perfil.</div>
            </div>
          </div>

          <!-- Barra Divisoria Vertical en el medio -->
          <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

          <!-- Columna Derecha: Previsualización de Avatar Completo y Parámetros -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="p-4 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid var(--wf-gold) !important;">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="small fw-bold text-muted text-uppercase">Configuración de Síntesis HeyGen</div>
                  <span class="badge bg-primary text-white" style="font-size: 9px;">HD 1080p</span>
                </div>
                
                <!-- Preview Visual del Avatar Completo -->
                <div style="height: 220px; background: linear-gradient(180deg, #0F172A 0%, #081426 100%); border-radius: 8px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #1E3A5F; margin-bottom: 12px;">
                  <img src="${avatarDocenteImg}" style="height: 100%; width: auto; max-width: 100%; object-fit: contain; display: block;" alt="Avatar HeyGen">
                  
                  <div style="position: absolute; top: 8px; left: 8px; background: #2563EB; color: white; padding: 2px 6px; border-radius: 4px; font-size: 9px; font-weight: 700;">
                    Avatar HeyGen v2.0
                  </div>
                  
                  <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(0deg, rgba(8,20,38,0.9) 0%, transparent 100%); padding: 8px 12px;">
                    <div style="font-size: 12px; font-weight: 700; color: white;">Mg. Elena Valenzuela</div>
                    <div style="font-size: 9px; color: #94A3B8;">Docente Titular (#avatar_valenzuela_v2)</div>
                  </div>
                </div>

                <div class="d-flex flex-column gap-2" style="font-size: 11px;">
                  <div class="d-flex justify-content-between p-2 border rounded bg-light">
                    <span class="text-muted">Voz configurada:</span>
                    <strong class="text-dark">Docente_ES_AR_v1 (Voice Clone)</strong>
                  </div>
                  <div class="d-flex justify-content-between p-2 border rounded bg-light">
                    <span class="text-muted">Destino:</span>
                    <span class="fw-bold">Material de Grabación (No Publicado)</span>
                  </div>
                </div>
              </div>

              <div class="pt-3 mt-3 border-top d-flex justify-content-end align-items-center gap-3">
                <a href="#CU-19" class="wf-btn wf-btn-sm wf-btn-outline">Cancelar</a>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-2" style="font-weight: 700; background: #7C3AED; border-color: #6D28D9;">
                    <i class="fa-solid fa-wand-magic-sparkles"></i>
                    <span>Sintetizar Video con IA</span>
                  </button>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 7C: MODIFICAR CLASE CON CLON IA --- CU-79
  if (id === 'CU-79') {
    return `
      <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
              <i class="fa-solid fa-pen-to-square" style="font-size: 20px;"></i>
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Clase con Clon de IA</h3>
              <p class="small text-muted" style="margin: 3px 0 0;">Actualice el título y/o el guión textual de la clase para regenerar el video en HeyGen.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active"><i class="fa-solid fa-pen-to-square me-1"></i> Modo Edición</span>
          </div>
        </div>

        <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
          <!-- Columna Izquierda: Editor de Título y Guión -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="mb-3">
              <label class="wf-label">Título de la Clase</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Explicación Teórica: Duración Modificada y Convexidad en Bonos">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>

            <div class="mb-3">
              <label class="wf-label">Guión Textual (Prompt de Síntesis)</label>
              <div class="wf-input-wrap">
                <textarea class="wf-input" rows="7">En esta clase abordaremos el concepto de modified duration. Cuando la tasa de interés se incrementa, el precio de los títulos cae en proporción inversa a su duración ponderada. Analizaremos la aproximación por series de Taylor y los ejercicios prácticos actualizados del examen CNV.</textarea>
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>

            <div class="p-2 border rounded bg-light small text-muted">
              <i class="fa-solid fa-circle-info text-primary me-1"></i> <em>Si solo se modifica el título, no se dispara una nueva generación en HeyGen. Si se modifica el guión, el video se regenerará automáticamente.</em>
            </div>
          </div>

          <!-- Barra Divisoria Vertical en el medio -->
          <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

          <!-- Columna Derecha: Previsualización de Clon Completo y Parámetros -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="p-4 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid var(--wf-gold) !important;">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div class="small fw-bold text-muted text-uppercase">Parámetros del Clon Vinculado</div>
                  <span class="badge bg-success text-white" style="font-size: 9px;">Clon Activo</span>
                </div>

                <!-- Preview Visual del Avatar Completo en el Reproductor -->
                <div style="height: 190px; background: linear-gradient(180deg, #0F172A 0%, #081426 100%); border-radius: 8px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #1E293B; margin-bottom: 12px;">
                  <img src="${avatarDocenteImg}" style="height: 100%; width: auto; max-width: 100%; object-fit: contain; display: block;" alt="Docente">
                  
                  <div style="position: absolute; top: 8px; right: 8px; background: rgba(0,0,0,0.7); border: 1px solid rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 4px; font-size: 9px; color: var(--wf-gold);">
                    HeyGen Studio
                  </div>
                  
                  <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(0deg, rgba(8,20,38,0.9) 0%, transparent 100%); padding: 6px 12px; display: flex; justify-content: space-between; align-items: flex-end;">
                    <div>
                      <strong style="font-size: 11px; color: white;">Mg. Elena Valenzuela</strong>
                      <div class="small text-muted" style="font-size: 9px; color: #94A3B8 !important;">#avatar_valenzuela_v2</div>
                    </div>
                    <span class="pin-badge" style="font-size: 10px; width: 20px; height: 20px;">${badges[3] || 'D'}</span>
                  </div>
                </div>

                <div class="mb-2">
                  <label class="wf-label mb-1" style="font-size: 11px;">Voz Clonada</label>
                  <div class="wf-input-wrap">
                    <div class="p-2 rounded border bg-light d-flex align-items-center justify-content-between w-100">
                      <div class="d-flex align-items-center gap-2">
                        <i class="fa-solid fa-microphone text-primary"></i>
                        <div>
                          <strong style="font-size: 11px; color: #081426;">Docente_ES_AR_v1</strong>
                          <div class="small text-muted" style="font-size: 9px;">Voz en Español Rioplatense • 48kHz</div>
                        </div>
                      </div>
                      <span class="badge bg-success text-white" style="font-size: 9px;">Calibrada</span>
                    </div>
                    <span class="pin-badge">${badges[4] || 'E'}</span>
                  </div>
                </div>

                <div class="p-2 border rounded bg-light small text-muted" style="font-size: 10px;">
                  Estado de la clase: <strong class="text-success">Generada</strong> (Reemplazará grabación anterior)
                </div>
              </div>

              <div class="pt-3 mt-3 border-top d-flex justify-content-end align-items-center gap-3">
                <a href="#CU-77" class="wf-btn wf-btn-sm wf-btn-outline">Cancelar</a>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-2" style="font-weight: 700; background: #7C3AED; border-color: #6D28D9;">
                    <i class="fa-solid fa-rotate me-1"></i>
                    <span>Actualizar y Regenerar Video</span>
                  </button>
                  <span class="pin-badge">${badges[5] || badges[badges.length - 1] || 'F'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8: GENERACIÓN IA DE BANCO, RESUMEN Y SLIDES --- CU-73, CU-74, CU-75
  if (['CU-73', 'CU-74', 'CU-75'].includes(id)) {
    const isBank = id === 'CU-73';
    const isSummary = id === 'CU-74';
    const isSlides = id === 'CU-75';

    let pageTitle = 'Generar Banco de Preguntas con IA (Ollama)';
    let pageSubtitle = 'Generación automatizada de preguntas cerradas de opción múltiple y V/F a partir de la bibliografía cargada.';
    let iconHeader = 'fa-solid fa-list-check';
    let confirmBtn = 'Generar Preguntas con IA';
    let confirmBadge = badges[2] || 'C';

    if (isSummary) {
      pageTitle = 'Generar Resumen de Unidad con IA';
      pageSubtitle = 'Síntesis conceptual estructurada a partir de los documentos teóricos cargados en la unidad.';
      iconHeader = 'fa-solid fa-file-lines';
      confirmBtn = 'Crear Resumen Automático';
      confirmBadge = badges[1] || 'B';
    } else if (isSlides) {
      pageTitle = 'Generar Presentación con IA';
      pageSubtitle = 'Estructuración de diapositivas descargables (títulos, subtítulos y conceptos clave) desde la bibliografía.';
      iconHeader = 'fa-solid fa-person-chalkboard';
      confirmBtn = 'Generar Diapositivas';
      confirmBadge = badges[1] || 'B';
    }

    return `
      <div class="wf-card" style="max-width: 1080px; margin: 0 auto; background: #FFFFFF;">
        <!-- Cabecera Limpia sin badge A -->
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
              <i class="${iconHeader}" style="font-size: 20px;"></i>
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${pageTitle}</h3>
              <p class="small text-muted" style="margin: 2px 0 0;">${pageSubtitle}</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Ollama Local (LLaMA-3 8B)</span>
          </div>
        </div>

        <div class="d-flex flex-column flex-lg-row align-items-stretch justify-content-between gap-4">
          <!-- Columna Izquierda: Configuración y Parámetros de Entrada -->
          <div style="flex: 1 1 50%; min-width: 320px;" class="d-flex flex-column justify-content-between">
            <div>
              <div class="mb-3">
                <label class="wf-label">Unidad Académica de Origen</label>
                <div class="wf-input-wrap">
                  <select class="wf-input">
                    <option selected>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</option>
                  </select>
                </div>
              </div>

              <div class="mb-3">
                <label class="wf-label">Fuentes de Contenido (Bibliografía & Glosario)</label>
                <div class="p-2 border rounded bg-light d-flex flex-column gap-1" style="font-size: 12px;">
                  <div class="d-flex justify-content-between align-items-center">
                    <span><i class="fa-solid fa-file-pdf text-danger me-1"></i> Guía Teórica de Renta Fija v2.0.pdf</span>
                    <span class="badge bg-success text-white" style="font-size: 9px;">Cargado</span>
                  </div>
                  <div class="d-flex justify-content-between align-items-center">
                    <span><i class="fa-solid fa-book-open text-warning me-1"></i> Glosario de Unidad (8 términos técnicos)</span>
                    <span class="badge bg-success text-white" style="font-size: 9px;">Cargado</span>
                  </div>
                </div>
              </div>

              ${isBank ? `
                <div class="mb-3">
                  <label class="wf-label">Guión Adicional / Prompt de Orientación (Opcional)</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="4" placeholder="Ej: Enfatizar preguntas sobre cálculo de TIR, duration de Macaulay y convexidad de bonos soberanos...">Enfatizar preguntas sobre cálculo de TIR, duration de Macaulay y convexidad de bonos soberanos con casos prácticos.</textarea>
                    <span class="pin-badge">${badges[1] || 'B'}</span>
                  </div>
                  <div class="small text-muted mt-1" style="font-size: 11px;">El modelo Ollama utilizará este guión junto con la bibliografía de la unidad para generar los enunciados.</div>
                </div>
              ` : ''}

              ${isSummary ? `
                <div class="p-3 bg-light rounded border mb-3">
                  <div class="small fw-bold text-muted text-uppercase mb-2">Parámetros de Síntesis</div>
                  <div class="d-flex justify-content-between align-items-center small text-muted">
                    <span>Extensión estimada:</span>
                    <strong class="text-dark">3 a 4 páginas estructuradas</strong>
                  </div>
                  <div class="d-flex justify-content-between align-items-center small text-muted mt-1">
                    <span>Nivel de detalle:</span>
                    <strong class="text-dark">Avanzado (Énfasis en conceptos CNV)</strong>
                  </div>
                </div>
              ` : ''}

              ${isSlides ? `
                <div class="p-3 bg-light rounded border mb-3">
                  <div class="small fw-bold text-muted text-uppercase mb-2">Formato de Presentación</div>
                  <div class="d-flex justify-content-between align-items-center small text-muted">
                    <span>Estructura:</span>
                    <strong class="text-dark">12 diapositivas temáticas con puntos clave</strong>
                  </div>
                  <div class="d-flex justify-content-between align-items-center small text-muted mt-1">
                    <span>Exportación:</span>
                    <strong class="text-dark">Formato PPTX / PDF descargable</strong>
                  </div>
                </div>
              ` : ''}
            </div>

            <div class="p-2 border rounded bg-white shadow-sm d-flex align-items-center justify-content-between mt-2">
              <span class="small text-muted"><i class="fa-solid fa-server me-1 text-primary"></i> Estado de Inferencia: <strong>Local Ollama API</strong></span>
              <span class="badge bg-success text-white" style="font-size: 10px;">En Línea</span>
            </div>
          </div>

          <!-- Barra Divisoria Vertical en el medio -->
          <div class="d-none d-lg-block" style="width: 1px; background: #CBD5E1; min-height: 100%;"></div>

          <!-- Columna Derecha: Previsualización del Resultado IA -->
          <div style="flex: 1 1 50%; min-width: 320px;">
            <div class="p-4 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid #059669 !important;">
              <div>
                <div class="d-flex justify-content-between align-items-center pb-2 mb-3 border-bottom">
                  <strong style="font-size: 14px; color: #081426;"><i class="fa-solid fa-eye me-1 text-primary"></i>Previsualización del Resultado IA</strong>
                  <span class="badge bg-light text-muted border">Estado: No Publicado</span>
                </div>

                ${isBank ? `
                  <div class="d-flex flex-column gap-2" style="font-size: 12px;">
                    <div class="p-3 border rounded bg-light">
                      <div class="fw-bold text-dark mb-1">1. ¿Qué mide la modified duration en un título de renta fija?</div>
                      <div class="text-success small"><i class="fa-solid fa-circle-check me-1"></i> A) La sensibilidad porcentual del precio ante variaciones en la TIR. (Correcta)</div>
                      <div class="text-muted small ms-3">B) El plazo promedio ponderado de los flujos de fondos.</div>
                      <div class="text-muted small ms-3">C) El cupón de interés nominal anual.</div>
                    </div>
                    <div class="p-3 border rounded bg-light">
                      <div class="fw-bold text-dark mb-1">2. Ante una suba en la tasa de interés, el precio de un bono con alta convexidad cae más que uno de baja convexidad.</div>
                      <div class="text-success small"><i class="fa-solid fa-circle-check me-1"></i> Falso (La convexidad amortigua la caída de precio). (Correcta)</div>
                    </div>
                  </div>
                ` : ''}

                ${isSummary ? `
                  <div class="p-3 bg-light rounded border" style="font-size: 12px; line-height: 1.5;">
                    <strong style="color: #081426; font-size: 13px;">Resumen Temático: Valuación y Riesgo de Tasa en Renta Fija</strong>
                    <hr class="my-2">
                    <p class="mb-2"><strong>1. Conceptos Fundamentales:</strong> Los bonos son instrumentos de deuda emitidos por Estados o empresas. Su precio es el valor presente de sus cupones futuros descontados a la TIR.</p>
                    <p class="mb-0"><strong>2. Medición de Riesgo:</strong> La duración modificada cuantifica la volatilidad ante desplazamientos paralelos de la curva de rendimientos.</p>
                  </div>
                ` : ''}

                ${isSlides ? `
                  <div class="p-3 rounded border text-white" style="background: #081426; font-size: 12px;">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <span class="badge bg-primary">Diapositiva 1 / 12</span>
                      <span class="small text-muted">Idóneos Online • Presentación Oficial</span>
                    </div>
                    <h5 style="color: var(--wf-gold); font-size: 15px; font-weight: 800; margin: 4px 0 8px;">Instrumentos de Renta Fija: Bonos & Curva de Rendimientos</h5>
                    <ul class="ps-3 mb-0" style="font-size: 11px; color: #CBD5E1;">
                      <li>Morfología de flujos: bullet vs. amortizables.</li>
                      <li>Relación inversa entre precio y tasa (TIR).</li>
                      <li>Curvas Soberanas: spot vs. forward.</li>
                    </ul>
                  </div>
                ` : ''}
              </div>

              <div class="pt-3 mt-4 border-top d-flex justify-content-end align-items-center gap-3">
                <a href="#CU-19" class="wf-btn wf-btn-sm wf-btn-outline">Cancelar</a>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-2" style="background: #059669; border-color: #047857; font-weight: 700;">
                    <i class="fa-solid fa-wand-magic-sparkles"></i>
                    <span>${confirmBtn}</span>
                  </button>
                  <span class="pin-badge">${confirmBadge}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }


  // --- SPECIALIZED 8B: CREAR POOL DE PREGUNTAS (DSS BUCLE PREGUNTAS Y OPCIONES) --- CU-54
  if (id === 'CU-54') {
    return `
      <div class="wf-card" style="max-width: 1040px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Unidad 1: Marco Regulatorio</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Nuevo Pool de Preguntas</strong>
            </div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Crear Pool de Preguntas</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Defina el banco de ítems evaluativos, agregando preguntas con sus enunciados, tipos y opciones de respuesta.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Docente Titular</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <!-- Datos del Pool -->
        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <label class="wf-label">Nombre del Pool de Preguntas</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Pool Unidad 1: Marco Regulatorio y CNV" value="Pool Unidad 1: Marco Regulatorio y CNV">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Descripción Temática</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Banco evaluativo de preguntas sobre la Ley 26.831" value="Banco evaluativo de preguntas sobre la Ley 26.831 y ALyCs">
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>

        <!-- Editor de Preguntas y Opciones (DSS Loop de Preguntas y Opciones) -->
        <div class="p-4 bg-light rounded border mb-4">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <strong style="font-size: 15px; color: #081426;"><i class="fa-solid fa-list-check me-2 text-primary"></i>Editor de Preguntas del Pool</strong>
            <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-plus me-1"></i> Añadir Pregunta</button>
          </div>

          <!-- Pregunta 1: Opción Múltiple -->
          <div class="p-3 bg-white rounded border mb-3 shadow-sm">
            <div class="row g-3 align-items-center mb-3">
              <div class="col-md-8">
                <label class="wf-label mb-1">Enunciado de la Pregunta 1</label>
                <input type="text" class="wf-input" style="width: 100%;" value="¿Cuál es la función principal de la Comisión Nacional de Valores (CNV) según la Ley 26.831?">
              </div>
              <div class="col-md-4">
                <label class="wf-label mb-1">Tipo de Pregunta</label>
                <select class="wf-input" style="width: 100%;">
                  <option selected>Opción Múltiple (Múltiple Choice)</option>
                  <option>Verdadero / Falso</option>
                </select>
              </div>
            </div>

            <div class="small fw-bold text-muted mb-2">Opciones de Respuesta (Marque la opción correcta):</div>
            <div class="d-flex flex-column gap-2 mb-2" style="font-size: 12px;">
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light" style="border-left: 4px solid #16A34A !important;">
                <div class="d-flex align-items-center gap-2 flex-grow-1 me-3">
                  <input type="radio" name="p1_opt" checked style="width: 16px; height: 16px; flex-shrink: 0;">
                  <input type="text" class="wf-input" style="flex: 1;" value="Supervisar, regular y fiscalizar a los agentes y los mercados de capitales en Argentina.">
                </div>
                <span class="badge bg-success text-white" style="flex-shrink: 0; padding: 5px 8px;">Opción Correcta</span>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center gap-2 flex-grow-1 me-3">
                  <input type="radio" name="p1_opt" style="width: 16px; height: 16px; flex-shrink: 0;">
                  <input type="text" class="wf-input" style="flex: 1;" value="Emitir moneda y fijar la tasa de política monetaria en el sistema financiero.">
                </div>
                <span class="badge bg-light text-muted border" style="flex-shrink: 0; padding: 5px 8px;">Incorrecta</span>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center gap-2 flex-grow-1 me-3">
                  <input type="radio" name="p1_opt" style="width: 16px; height: 16px; flex-shrink: 0;">
                  <input type="text" class="wf-input" style="flex: 1;" value="Garantizar la rentabilidad de las inversiones bursátiles de los inversores minoristas.">
                </div>
                <span class="badge bg-light text-muted border" style="flex-shrink: 0; padding: 5px 8px;">Incorrecta</span>
              </div>
            </div>
            <button class="btn btn-link btn-sm p-0 text-decoration-none mt-1" style="font-size: 11px;"><i class="fa-solid fa-plus me-1"></i> Añadir otra opción</button>
          </div>

          <!-- Pregunta 2: Verdadero o Falso -->
          <div class="p-3 bg-white rounded border shadow-sm">
            <div class="row g-3 align-items-center mb-3">
              <div class="col-md-8">
                <label class="wf-label mb-1">Enunciado de la Pregunta 2</label>
                <input type="text" class="wf-input" style="width: 100%;" value="Un Agente de Liquidación y Compensación Propio (ALyC Propio) puede liquidar operaciones de terceros sin límite.">
              </div>
              <div class="col-md-4">
                <label class="wf-label mb-1">Tipo de Pregunta</label>
                <select class="wf-input" style="width: 100%;">
                  <option>Opción Múltiple (Múltiple Choice)</option>
                  <option selected>Verdadero / Falso</option>
                </select>
              </div>
            </div>

            <div class="small fw-bold text-muted mb-2">Opciones de Respuesta:</div>
            <div class="d-flex gap-3" style="font-size: 12px;">
              <label class="p-2 border rounded d-flex align-items-center gap-2 flex-grow-1 cursor-pointer">
                <input type="radio" name="p2_opt" style="width: 16px; height: 16px;">
                <span>Verdadero</span>
              </label>
              <label class="p-2 border rounded d-flex align-items-center justify-content-between flex-grow-1 bg-light cursor-pointer" style="border-left: 4px solid #16A34A !important;">
                <div class="d-flex align-items-center gap-2">
                  <input type="radio" name="p2_opt" checked style="width: 16px; height: 16px;">
                  <span>Falso (Solo puede liquidar operaciones propias)</span>
                </div>
                <span class="badge bg-success text-white" style="padding: 5px 8px;">Opción Correcta</span>
              </label>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-19" class="wf-btn wf-btn-outline">Cancelar</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar y Crear Pool</button>
            <span class="pin-badge">${badges[3] || 'D'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8C: MODIFICAR POOL DE PREGUNTAS --- CU-55
  if (id === 'CU-55') {
    return `
      <div class="wf-card" style="max-width: 1040px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Pools de Preguntas</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Modificar Pool</strong>
            </div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Modificar Pool de Preguntas</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Actualice el nombre, descripción y administre las preguntas y opciones del banco evaluativo.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Sin intentos asociados</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <label class="wf-label">Nombre del Pool</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Pool Unidad 1: Marco Regulatorio y CNV">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Descripción Temática</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Banco evaluativo de preguntas sobre la Ley 26.831 y ALyCs">
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>

        <div class="p-4 bg-light rounded border mb-4">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="d-flex align-items-center gap-2">
              <strong style="font-size: 15px; color: #081426;"><i class="fa-solid fa-list-check me-2 text-primary"></i>Preguntas y Opciones del Pool</strong>
              <span class="pin-badge">${badges[3] || 'D'}</span>
            </div>
            <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-plus me-1"></i> Añadir Pregunta</button>
          </div>

          <div class="p-3 bg-white rounded border mb-3 shadow-sm">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div class="flex-grow-1 me-3">
                <label class="wf-label mb-1">Pregunta 1: Enunciado</label>
                <input type="text" class="wf-input" style="width: 100%;" value="¿Cuál es la función principal de la Comisión Nacional de Valores (CNV) según la Ley 26.831?">
              </div>
              <button class="btn btn-outline-danger btn-sm mt-4" title="Eliminar pregunta"><i class="fa-solid fa-trash"></i></button>
            </div>
            <div class="d-flex flex-column gap-2 mb-2" style="font-size: 12px;">
              <div class="p-2 border rounded d-flex align-items-center justify-content-between bg-light" style="border-left: 4px solid #16A34A !important;">
                <div class="d-flex align-items-center gap-2 flex-grow-1 me-3">
                  <input type="radio" name="p1_edit" checked style="width: 16px; height: 16px; flex-shrink: 0;">
                  <input type="text" class="wf-input" style="flex: 1;" value="Supervisar, regular y fiscalizar a los agentes y los mercados de capitales en Argentina.">
                </div>
                <span class="badge bg-success text-white" style="flex-shrink: 0; padding: 5px 8px;">Correcta</span>
              </div>
              <div class="p-2 border rounded d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center gap-2 flex-grow-1 me-3">
                  <input type="radio" name="p1_edit" style="width: 16px; height: 16px; flex-shrink: 0;">
                  <input type="text" class="wf-input" style="flex: 1;" value="Emitir moneda y fijar la tasa de política monetaria en el sistema financiero.">
                </div>
                <span class="badge bg-light text-muted border" style="flex-shrink: 0; padding: 5px 8px;">Incorrecta</span>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-53" class="wf-btn wf-btn-outline">Cancelar</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
            <span class="pin-badge">${badges[4] || badges[badges.length - 1] || 'E'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 9: CLASE EN VIVO (STREAMING) --- CU-70, CU-71, CU-72
  if (['CU-70', 'CU-71', 'CU-72'].includes(id)) {
    const isStart = id === 'CU-70';
    const isEnd = id === 'CU-71';
    const isStudent = id === 'CU-72';

    return `
      <div class="wf-card" style="max-width: 1050px; margin: 0 auto; background: #0F172A; color: #FFFFFF; border-color: #1E293B;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom border-secondary">
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge" style="background: #DC2626; color: white; animation: pulse 2s infinite;">● EN VIVO</span>
            <div>
              <h3 style="font-size: 16px; font-weight: 700; color: #FFFFFF; margin: 0;">Clase Magistral: Resolución de Prácticos de Renta Fija</h3>
              <p class="small text-muted" style="margin: 0; color: #94A3B8;">Docente: Mg. Elena Valenzuela | Alumnos Conectados: 48</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            ${isStart ? `
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-2" style="background: #2563EB; font-weight: 700;">
                  <i class="fa-solid fa-key text-warning"></i>
                  <span>Copiar clave para transmitir en vivo por OBS</span>
                  <i class="fa-solid fa-copy ms-1"></i>
                </button>
                <span class="pin-badge">${badges[1] || badges[badges.length - 1] || 'B'}</span>
              </div>
            ` : ''}
            ${isEnd ? `
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm wf-btn-danger">Finalizar Transmisión</button>
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            ` : ''}
            ${isStudent ? `
              <div class="d-flex align-items-center gap-2">
                <span class="wf-badge" style="background: #DC2626; color: white;">● En vivo</span>
                <span class="wf-badge status-active">Conectado a la Sala</span>
              </div>
            ` : ''}
          </div>
        </div>

        <div class="row g-3">
          <div class="col-md-8">
            <div style="height: 380px; background: #020617; border-radius: 8px; display: flex; align-items: center; justify-content: center; position: relative; border: 1px solid #334155;">
              <div class="text-center text-muted">
                <div style="width: 64px; height: 64px; border-radius: 50%; background: #1E293B; margin: 0 auto 10px; display: flex; align-items: center; justify-content: center;">
                  ${icons.signal("w-8 h-8 text-white")}
                </div>
                <div class="small fw-bold text-light">Transmisión RTMP / WebRTC en Directo (1080p 60fps)</div>
                <div style="font-size: 11px; color: #64748B;">Latencia ultra-baja: 420 ms</div>
              </div>
              <div style="position: absolute; bottom: 12px; left: 12px; display: flex; gap: 8px;">
                <span class="badge bg-dark border border-secondary text-light">Micro: ON</span>
                <span class="badge bg-dark border border-secondary text-light">Cámara: ON</span>
                <span class="badge bg-dark border border-secondary text-light">Grabando automático</span>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div style="height: 380px; background: #1E293B; border-radius: 8px; display: flex; flex-direction: column; border: 1px solid #334155;">
              <div class="p-3 border-bottom border-secondary small fw-bold text-light d-flex justify-content-between">
                <span>Chat en Vivo</span>
                <span class="text-muted">48 participantes</span>
              </div>
              <div class="p-3 flex-1 overflow-y-auto small" style="font-size: 12px; display: flex; flex-direction: column; gap: 8px; color: #CBD5E1;">
                <div><strong class="text-info">Joaquín Küster:</strong> ¿La modified duration incluye cupones corridos?</div>
                <div><strong class="text-warning">Docente:</strong> Sí Joaquín, se calcula sobre el precio dirty.</div>
                <div><strong class="text-info">María Benítez:</strong> ¡Excelente explicación profe!</div>
              </div>
              <div class="p-2 border-top border-secondary">
                <input type="text" class="wf-input" placeholder="Escribir mensaje en el chat..." style="background: #0F172A; border-color: #475569; color: white;">
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 10: AUTOEVALUACIÓN ACTIVA (EXAMEN) --- CU-63
  if (id === 'CU-63') {
    return `
      <div class="wf-card" style="max-width: 900px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">Autoevaluación Unidad 2: Renta Fija y Bonos</h3>
            <p class="small text-muted" style="margin: 0;">Intento 1 de 3 • Tiempo restante: <strong class="text-danger">18:45 min</strong></p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">En progreso</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <!-- Pregunta 1 -->
        <div class="p-4 border rounded mb-4 bg-white shadow-sm">
          <div class="d-flex justify-content-between mb-2">
            <span class="small fw-bold text-muted">PREGUNTA 1 DE 10</span>
            <span class="small text-muted">Valor: 1.00 punto</span>
          </div>
          <p class="fw-bold mb-3" style="font-size: 14px; color: #1E293B;">
            Si la tasa interna de retorno (TIR) de un bono soberano a tasa fija sube 100 puntos básicos, ¿qué sucede con su precio de mercado?
          </p>

          <div class="d-flex flex-column gap-2">
            <label class="d-flex align-items-center gap-3 p-2 rounded border bg-light cursor-pointer">
              <input type="radio" name="p1" checked>
              <span>El precio cae, en una magnitud aproximada a su Modified Duration.</span>
            </label>
            <label class="d-flex align-items-center gap-3 p-2 rounded border cursor-pointer">
              <input type="radio" name="p1">
              <span>El precio sube proporcionalmente al plazo de madurez.</span>
            </label>
            <label class="d-flex align-items-center gap-3 p-2 rounded border cursor-pointer">
              <input type="radio" name="p1">
              <span>El precio se mantiene inalterado ya que el cupón es fijo.</span>
            </label>
          </div>
          <div class="mt-2 text-end">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Guardar y Continuar Luego</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary" style="background: #059669;">Terminar Intento y Enviar Todo</button>
            <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11A: BUSCAR INSCRIPCIÓN / HISTORIAL --- CU-43
  if (id === 'CU-43') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Gestión e Historial de Inscripciones</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Consulte las matriculaciones vigentes, cohortes asignadas, certificados emitidos y gestión de bajas.</p>
          </div>
          <span class="wf-badge status-active">Total: 146 Inscripciones Activas</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-5">
            <label class="wf-label">Buscar por Alumno o DNI</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Joaquín Küster, 40.123.456, María Benítez...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Estado de la Inscripción</label>
            <select class="wf-input">
              <option>Todas las inscripciones (Vigente / Vencida / Dada de baja)</option>
              <option>Vigente (Con acceso activo)</option>
              <option>Vencida (Plazo cumplido)</option>
              <option>Dada de baja</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>Alumno / DNI</th>
                <th>Curso & Cohorte</th>
                <th>Fecha Matrícula</th>
                <th>Vencimiento Acceso</th>
                <th>Estado</th>
                <th class="text-end">Acciones / Certificado</th>
              </tr>
            </thead>
            <tbody>
              <!-- Alumno 1: Joaquín Küster (Aprobado con Certificado) -->
              <tr>
                <td>
                  <strong>Joaquín Küster</strong>
                  <div class="small text-muted">DNI: 40.123.456 • joaquin@idoneos.online</div>
                </td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <span class="fw-bold" style="color: #081426;">Especialización en Idoneidad Bursátil</span>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                  <div class="small text-muted">Cohorte 2026-1 (Programa 2026-A)</div>
                </td>
                <td>15/03/2026</td>
                <td>15/07/2026</td>
                <td><span class="wf-badge status-active">Vigente (Aprobado)</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button class="wf-btn wf-btn-sm wf-btn-outline" title="Descargar Certificado Oficial">
                      <i class="fa-solid fa-award me-1" style="color: var(--wf-gold);"></i>
                      <span>Descargar Certificado</span>
                    </button>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                    <a href="#CU-45" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja inscripción">
                      <i class="fa-solid fa-user-xmark me-1"></i>
                      <span>Dar de baja</span>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Alumno 2: María Benítez (En Curso) -->
              <tr>
                <td>
                  <strong>María Benítez</strong>
                  <div class="small text-muted">DNI: 38.945.112 • maria.benitez@idoneos.online</div>
                </td>
                <td>
                  <span class="fw-bold" style="color: #081426;">Especialización en Idoneidad Bursátil</span>
                  <div class="small text-muted">Cohorte 2026-1 (Programa 2026-A)</div>
                </td>
                <td>18/03/2026</td>
                <td>18/07/2026</td>
                <td><span class="wf-badge status-active">Vigente (En curso)</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button class="wf-btn wf-btn-sm wf-btn-outline" disabled title="Certificado no emitido aún (Requiere aprobar todas las unidades)">
                      <i class="fa-solid fa-award me-1 text-muted"></i>
                      <span class="text-muted">Certificado Pendiente</span>
                    </button>
                    <a href="#CU-45" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja inscripción">
                      <i class="fa-solid fa-user-xmark me-1"></i>
                      <span>Dar de baja</span>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Alumno 3: Lucas Romero (Operativa Cripto) -->
              <tr>
                <td>
                  <strong>Lucas Romero</strong>
                  <div class="small text-muted">DNI: 42.887.654 • lucas.romero@idoneos.online</div>
                </td>
                <td>
                  <span class="fw-bold" style="color: #081426;">Operativa Cripto y DeFi Profesional</span>
                  <div class="small text-muted">Cohorte 2026-1 (Programa 2026-C)</div>
                </td>
                <td>01/04/2026</td>
                <td>01/08/2026</td>
                <td><span class="wf-badge status-active">Vigente (En curso)</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button class="wf-btn wf-btn-sm wf-btn-outline" disabled>
                      <i class="fa-solid fa-award me-1 text-muted"></i>
                      <span class="text-muted">Certificado Pendiente</span>
                    </button>
                    <a href="#CU-45" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja inscripción">
                      <i class="fa-solid fa-user-xmark me-1"></i>
                      <span>Dar de baja</span>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Alumno 4: Carlos Fernández (Baja por Abandono) -->
              <tr>
                <td>
                  <strong>Carlos Fernández</strong>
                  <div class="small text-muted">DNI: 35.612.890 • carlos.f@idoneos.online</div>
                </td>
                <td>
                  <span class="fw-bold" style="color: #081426;">Mercado de Capitales Argentino</span>
                  <div class="small text-muted">Cohorte 2025-2 (Programa 2025-B)</div>
                </td>
                <td>10/11/2025</td>
                <td>10/03/2026</td>
                <td><span class="wf-badge status-inactive" style="color: #DC2626; border-color: #FCA5A5; background: #FEF2F2;">Dada de baja</span></td>
                <td class="text-end">
                  <span class="small text-muted fst-italic">Baja registrada el 22/12/2025</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11A-4: BUSCAR USUARIO --- CU-82
  if (id === 'CU-82') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Gestión y Búsqueda de Usuarios</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Consulte, filtre y gestione los expedientes de usuarios registrados en el sistema con sus roles y credenciales.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Módulo de Usuarios</span>
          </div>
        </div>

        <!-- Barra de Filtros [A] -->
        <div class="p-3 bg-light rounded border mb-4">
          <div class="row g-3 align-items-end">
            <div class="col" style="flex: 1 1 22%; min-width: 180px;">
              <label class="wf-label">Buscar por Nombre o Apellido</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" placeholder="Ej: Elena, Valenzuela, Joaquín...">
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            </div>
            <div class="col" style="flex: 1 1 22%; min-width: 180px;">
              <label class="wf-label">Correo Electrónico</label>
              <div class="wf-input-wrap">
                <input type="email" class="wf-input" placeholder="ejemplo@idoneos.online">
              </div>
            </div>
            <div class="col" style="flex: 1 1 16%; min-width: 130px;">
              <label class="wf-label">DNI</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" placeholder="DNI sin puntos...">
              </div>
            </div>
            <div class="col" style="flex: 1 1 18%; min-width: 140px;">
              <label class="wf-label">Rol del Usuario</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option selected>Todos los roles</option>
                  <option>Docente</option>
                  <option>Alumno</option>
                  <option>Administrador</option>
                </select>
              </div>
            </div>
            <div class="col-auto" style="min-width: 120px;">
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Listado de Usuarios Filtrados con Acción [C] -->
        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>Usuario / Datos Personales</th>
                <th>Correo Electrónico</th>
                <th>DNI / Identificación</th>
                <th>Rol Asignado</th>
                <th>Estado</th>
                <th class="text-end">Acciones / Perfil</th>
              </tr>
            </thead>
            <tbody>
              <!-- Usuario 1: Mg. Elena Valenzuela (Docente Seleccionada) -->
              <tr style="background: #F0F9FF; border-left: 4px solid var(--wf-navy);">
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <img src="${avatarDocenteImg}" style="width: 36px; height: 36px; min-width: 36px; border-radius: 50%; object-fit: cover; aspect-ratio: 1 / 1; flex-shrink: 0; border: 1.5px solid var(--wf-gold);" alt="Docente">
                    <div>
                      <strong style="color: #081426;">Mg. Elena Valenzuela</strong>
                      <div class="small text-muted">Especialista en Finanzas & Mercado CNV</div>
                    </div>
                  </div>
                </td>
                <td>elena.valenzuela@idoneos.online</td>
                <td><span class="fw-bold">35.456.789</span></td>
                <td><span class="badge bg-primary text-white">Docente</span></td>
                <td><span class="wf-badge status-active">Activo</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <a href="#CU-86" class="wf-btn wf-btn-sm wf-btn-primary" title="Consultar perfil del usuario">
                      <i class="fa-solid fa-user me-1"></i>
                      <span>Ver Perfil</span>
                    </a>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                    <a href="#CU-84" class="wf-btn wf-btn-sm wf-btn-outline" title="Modificar usuario">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Usuario 2: Joaquín Küster (Alumno) -->
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar-circle" style="width: 36px; height: 36px; min-width: 36px; border-radius: 50%; aspect-ratio: 1 / 1; flex-shrink: 0; background: #2563EB; color: white;">JK</div>
                    <div>
                      <strong style="color: #081426;">Joaquín Küster</strong>
                      <div class="small text-muted">Estudiante de Especialización</div>
                    </div>
                  </div>
                </td>
                <td>joaquin.kuster@idoneos.online</td>
                <td><span class="fw-bold">40.123.456</span></td>
                <td><span class="badge bg-light text-dark border">Alumno</span></td>
                <td><span class="wf-badge status-active">Activo</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <a href="#CU-86" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-user me-1"></i>
                      <span>Ver Perfil</span>
                    </a>
                    <a href="#CU-84" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Usuario 3: Dr. Roberto Cachanosky (Docente) -->
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar-circle" style="width: 36px; height: 36px; min-width: 36px; border-radius: 50%; aspect-ratio: 1 / 1; flex-shrink: 0; background: #081426; color: var(--wf-gold);">RC</div>
                    <div>
                      <strong style="color: #081426;">Dr. Roberto Cachanosky</strong>
                      <div class="small text-muted">Docente Titular de Economía</div>
                    </div>
                  </div>
                </td>
                <td>roberto.cachanosky@idoneos.online</td>
                <td><span class="fw-bold">14.567.890</span></td>
                <td><span class="badge bg-primary text-white">Docente</span></td>
                <td><span class="wf-badge status-active">Activo</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <a href="#CU-86" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-user me-1"></i>
                      <span>Ver Perfil</span>
                    </a>
                    <a href="#CU-84" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </a>
                  </div>
                </td>
              </tr>

              <!-- Usuario 4: Admin Central (Administrador) -->
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="user-avatar-circle" style="width: 36px; height: 36px; min-width: 36px; border-radius: 50%; aspect-ratio: 1 / 1; flex-shrink: 0; background: #DC2626; color: white;">AD</div>
                    <div>
                      <strong style="color: #081426;">Administrador General</strong>
                      <div class="small text-muted">Gestión Central Idóneos Online</div>
                    </div>
                  </div>
                </td>
                <td>admin@idoneos.online</td>
                <td><span class="fw-bold">28.990.112</span></td>
                <td><span class="badge bg-danger text-white">Administrador</span></td>
                <td><span class="wf-badge status-active">Activo</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <a href="#CU-86" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-user me-1"></i>
                      <span>Ver Perfil</span>
                    </a>
                    <a href="#CU-84" class="wf-btn wf-btn-sm wf-btn-outline">
                      <i class="fa-solid fa-pen-to-square"></i>
                    </a>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11A-3: BUSCAR DESCUENTO --- CU-49
  if (id === 'CU-49') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Gestión de Descuentos y Becas Arancelarias</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Configure las reglas de descuento por cantidad de cursos, promociones temporales y límites de uso.</p>
          </div>
          <span class="wf-badge status-active">Módulo de Facturación</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-5">
            <label class="wf-label">Buscar Descuento por Nombre</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Beca Alumno Destacado, Promo Lanzamiento...">
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtro por Vigencia</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Todos los estados (Vigente / Vencido / Agotado)</option>
                <option selected>Vigente (Activo para aplicar)</option>
                <option>Vencido (Fecha límite expirada)</option>
                <option>Agotado (Cupo máximo alcanzado)</option>
              </select>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>Nombre del Beneficio</th>
                <th>Porcentaje</th>
                <th>Vigencia (Desde / Hasta)</th>
                <th>Usos / Límite</th>
                <th>Condición Requerida</th>
                <th>Estado</th>
                <th class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <!-- Descuento 1: Promo Lanzamiento -->
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <strong style="color: #081426;">Promo Lanzamiento 2026</strong>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                  <div class="small text-muted">Descuento general para nuevos ingresantes</div>
                </td>
                <td><strong style="color: #059669; font-size: 14px;">20% OFF</strong></td>
                <td>01/02/2026 al 31/03/2026</td>
                <td><strong>14</strong> / 50 usados</td>
                <td>Sin cursos previos</td>
                <td><span class="wf-badge status-active">Vigente</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <a href="#CU-51" class="wf-btn wf-btn-sm wf-btn-outline" title="Editar descuento"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                    <a href="#CU-52" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja descuento"><i class="fa-solid fa-trash me-1"></i> Eliminar</a>
                  </div>
                </td>
              </tr>

              <!-- Descuento 2: Paquete Mercado de Capitales -->
              <tr>
                <td>
                  <strong style="color: #081426;">Beca Alumno Continuo (FCEQyN)</strong>
                  <div class="small text-muted">Reconocimiento académico a graduados</div>
                </td>
                <td><strong style="color: #059669; font-size: 14px;">30% OFF</strong></td>
                <td>01/01/2026 al 31/12/2026</td>
                <td><strong>8</strong> / 20 usados</td>
                <td>≥ 1 curso aprobado</td>
                <td><span class="wf-badge status-active">Vigente</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <a href="#CU-51" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                    <a href="#CU-52" class="wf-btn wf-btn-sm wf-btn-outline text-danger"><i class="fa-solid fa-trash me-1"></i> Eliminar</a>
                  </div>
                </td>
              </tr>

              <!-- Descuento 3: Expirado 2025 -->
              <tr>
                <td>
                  <strong style="color: #081426;">Black Friday Bursátil 2025</strong>
                  <div class="small text-muted">Campaña especial de fin de año</div>
                </td>
                <td><strong style="color: #64748B; font-size: 14px;">40% OFF</strong></td>
                <td>20/11/2025 al 30/11/2025</td>
                <td><strong>30</strong> / 30 usados</td>
                <td>Sin cursos previos</td>
                <td><span class="wf-badge status-inactive">Agotado</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <a href="#CU-51" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
                    <span class="small text-muted fst-italic">No eliminable</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11A-2: BUSCAR PROGRESO DE ALUMNOS --- CU-48
  if (id === 'CU-48') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Seguimiento y Progreso Pedagógico de Alumnos</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Consulte el progreso académico general y el desglose de avance por unidad con fechas de completitud.</p>
          </div>
          <span class="wf-badge status-active">Cohorte 2026-1 (8 Semanas)</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-5">
            <label class="wf-label">Buscar Alumno por Nombre o DNI</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar alumno por nombre, apellido o DNI...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Cohorte / Curso</label>
            <select class="wf-input">
              <option>Especialización en Idoneidad Bursátil (Cohorte 2026-1)</option>
              <option>Operativa Cripto y DeFi Profesional (Cohorte 2026-1)</option>
              <option>Mercado de Capitales Argentino (Cohorte 2025-2)</option>
            </select>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="row g-4">
          <!-- Columna Izquierda: Listado de Alumnos -->
          <div class="col-lg-7" style="border-right: 2px solid #E2E8F0; padding-right: 20px;">
            <div class="wf-table-wrap">
              <table class="wf-table">
                <thead>
                  <tr>
                    <th>Alumno / Contacto</th>
                    <th>Estado</th>
                    <th style="min-width: 140px;">Progreso</th>
                  </tr>
                </thead>
                <tbody>
                  <!-- Alumno 1: Joaquín Küster (Seleccionado) -->
                  <tr style="background: #F0F9FF; border-left: 4px solid var(--wf-navy);">
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="user-avatar-circle" style="width: 32px; height: 32px; font-size: 11px; background: #2563EB; color: white;">JK</div>
                        <div>
                          <div class="d-flex align-items-center gap-2">
                            <strong>Joaquín Küster</strong>
                            <span class="pin-badge">${badges[2] || 'C'}</span>
                          </div>
                          <div class="small text-muted">DNI: 40.123.456</div>
                        </div>
                      </div>
                    </td>
                    <td><span class="wf-badge status-active">Al día</span></td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div style="flex: 1; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden;">
                          <div style="width: 100%; height: 100%; background: #16A34A;"></div>
                        </div>
                        <span class="small fw-bold text-success">100%</span>
                      </div>
                    </td>
                  </tr>

                  <!-- Alumno 2: María Benítez -->
                  <tr>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="user-avatar-circle" style="width: 32px; height: 32px; font-size: 11px; background: #0D9488; color: white;">MB</div>
                        <div>
                          <strong>María Benítez</strong>
                          <div class="small text-muted">DNI: 38.945.112</div>
                        </div>
                      </div>
                    </td>
                    <td><span class="badge bg-light text-primary border">En curso</span></td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div style="flex: 1; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden;">
                          <div style="width: 66%; height: 100%; background: #2563EB;"></div>
                        </div>
                        <span class="small fw-bold text-primary">66%</span>
                      </div>
                    </td>
                  </tr>

                  <!-- Alumno 3: Lucas Romero -->
                  <tr>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div class="user-avatar-circle" style="width: 32px; height: 32px; font-size: 11px; background: #E11D48; color: white;">LR</div>
                        <div>
                          <strong>Lucas Romero</strong>
                          <div class="small text-muted">DNI: 42.887.654</div>
                        </div>
                      </div>
                    </td>
                    <td><span class="badge bg-light text-warning border">Atrasado</span></td>
                    <td>
                      <div class="d-flex align-items-center gap-2">
                        <div style="flex: 1; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden;">
                          <div style="width: 33%; height: 100%; background: #D97706;"></div>
                        </div>
                        <span class="small fw-bold text-warning">33%</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Columna Derecha: Panel de Avance Detallado por Unidad (Badge D) -->
          <div class="col-lg-5">
            <div class="p-3 border rounded bg-white shadow-sm">
              <div class="d-flex justify-content-between align-items-center pb-2 mb-3 border-bottom">
                <div>
                  <span class="small text-muted">Detalle Académico del Alumno</span>
                  <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin: 2px 0 0;">Joaquín Küster</h4>
                </div>
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>

              <div class="d-flex flex-column gap-3" style="font-size: 12px;">
                <!-- Unidad 1 -->
                <div class="p-2 border rounded bg-light">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <strong>Unidad 1: Marco Regulatorio</strong>
                    <span class="badge bg-success text-white">Completada</span>
                  </div>
                  <div class="small text-muted">Fecha de completitud: <strong>20/03/2026</strong></div>
                  <div class="small text-muted">Autoevaluación U1: <strong>Nota 10/10</strong></div>
                </div>

                <!-- Unidad 2 -->
                <div class="p-2 border rounded bg-light">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <strong>Unidad 2: Renta Fija (Bonos y ONs)</strong>
                    <span class="badge bg-success text-white">Completada</span>
                  </div>
                  <div class="small text-muted">Fecha de completitud: <strong>10/04/2026</strong></div>
                  <div class="small text-muted">Autoevaluación U2: <strong>Nota 9/10</strong></div>
                </div>

                <!-- Unidad 3 -->
                <div class="p-2 border rounded bg-light">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <strong>Unidad 3: Renta Variable & Derivados</strong>
                    <span class="badge bg-success text-white">Completada</span>
                  </div>
                  <div class="small text-muted">Fecha de completitud: <strong>02/05/2026</strong></div>
                  <div class="small text-muted">Examen Integrador: <strong>Nota 9.5/10</strong></div>
                </div>
              </div>

              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="small text-muted">Certificado Oficial Emitido:</span>
                <span class="badge status-active"><i class="fa-solid fa-award me-1"></i> ID-2026-8812</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11B: BUSCAR PAGOS / COMPROBANTES --- CU-46
  if (id === 'CU-46') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Historial y Registro de Pagos</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Liquidaciones de aranceles, cobros por MODO QR, tarjetas y descarga de comprobantes oficiales.</p>
          </div>
          <span class="wf-badge status-active">Facturación Vigente</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-4">
            <label class="wf-label">Estado de Transacción</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Todos los estados (Acreditado, Pendiente, Rechazado)</option>
                <option>Acreditados (Aprobados)</option>
                <option>Pendientes de Pago</option>
                <option>Rechazados / Cancelados</option>
              </select>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-5">
            <label class="wf-label">Rango de Fechas / Transacción</label>
            <div class="d-flex align-items-center gap-2">
              <input type="date" class="wf-input" value="2026-03-01">
              <span class="text-muted small">a</span>
              <input type="date" class="wf-input" value="2026-08-30">
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar</button>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>ID Transacción</th>
                <th>Alumno / Pagador</th>
                <th>Concepto / Curso</th>
                <th>Monto / Medio</th>
                <th>Estado</th>
                <th class="text-end">Comprobante Oficial</th>
              </tr>
            </thead>
            <tbody>
              <!-- Pago 1: Acreditado MODO QR -->
              <tr>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <strong style="color: #081426;">#PAY-2026-8841</strong>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                  <div class="small text-muted">15/03/2026 • 14:22 hs</div>
                </td>
                <td>
                  <strong>Joaquín Küster</strong>
                  <div class="small text-muted">DNI: 40.123.456</div>
                </td>
                <td>
                  <span class="fw-bold">Especialización en Idoneidad Bursátil</span>
                  <div class="small text-muted">Cohorte 2026-1</div>
                </td>
                <td>
                  <strong style="color: #059669;">$120.000 ARS</strong>
                  <div class="small text-muted"><i class="fa-solid fa-qrcode me-1 text-primary"></i> MODO Billetera QR</div>
                </td>
                <td><span class="wf-badge status-active">Acreditado</span></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-2">
                    <button class="wf-btn wf-btn-sm wf-btn-outline" title="Descargar comprobante en PDF">
                      <i class="fa-solid fa-receipt me-1 text-primary"></i>
                      <span>Descargar Comprobante</span>
                    </button>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                  </div>
                </td>
              </tr>

              <!-- Pago 2: Acreditado Tarjeta -->
              <tr>
                <td>
                  <strong style="color: #081426;">#PAY-2026-8912</strong>
                  <div class="small text-muted">18/03/2026 • 10:45 hs</div>
                </td>
                <td>
                  <strong>María Benítez</strong>
                  <div class="small text-muted">DNI: 38.945.112</div>
                </td>
                <td>
                  <span class="fw-bold">Especialización en Idoneidad Bursátil</span>
                  <div class="small text-muted">Cohorte 2026-1</div>
                </td>
                <td>
                  <strong style="color: #059669;">$120.000 ARS</strong>
                  <div class="small text-muted"><i class="fa-solid fa-credit-card me-1 text-muted"></i> Tarjeta Débito (*** 4129)</div>
                </td>
                <td><span class="wf-badge status-active">Acreditado</span></td>
                <td class="text-end">
                  <button class="wf-btn wf-btn-sm wf-btn-outline">
                    <i class="fa-solid fa-receipt me-1 text-primary"></i>
                    <span>Descargar Comprobante</span>
                  </button>
                </td>
              </tr>

              <!-- Pago 3: Operativa Cripto -->
              <tr>
                <td>
                  <strong style="color: #081426;">#PAY-2026-9055</strong>
                  <div class="small text-muted">01/04/2026 • 19:10 hs</div>
                </td>
                <td>
                  <strong>Lucas Romero</strong>
                  <div class="small text-muted">DNI: 42.887.654</div>
                </td>
                <td>
                  <span class="fw-bold">Operativa Cripto y DeFi Profesional</span>
                  <div class="small text-muted">Cohorte 2026-1</div>
                </td>
                <td>
                  <strong style="color: #059669;">$95.000 ARS</strong>
                  <div class="small text-muted"><i class="fa-solid fa-qrcode me-1 text-primary"></i> MODO Billetera QR</div>
                </td>
                <td><span class="wf-badge status-active">Acreditado</span></td>
                <td class="text-end">
                  <button class="wf-btn wf-btn-sm wf-btn-outline">
                    <i class="fa-solid fa-receipt me-1 text-primary"></i>
                    <span>Descargar Comprobante</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11C: INSCRIBIR CURSO (CONFIRMACIÓN DE MATRÍCULA Y DERIVACIÓN A PAGO) --- CU-44
  if (id === 'CU-44') {
    return `
      <div class="wf-card" style="max-width: 820px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 mb-4 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Catálogo de Cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <span style="color: #081426; font-weight: 700;">Inscripción Online</span>
            </div>
            <h3 style="font-size: 20px; font-weight: 800; color: #081426; margin: 4px 0 0;">Confirmación de Inscripción al Curso</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Verifique los detalles de la cohorte seleccionada y confirme su matriculación para proceder al pago.</p>
          </div>
          <span class="wf-badge status-active">Inscripción Abierta</span>
        </div>

        <div class="row g-4 mb-4">
          <!-- Datos del Curso y Cohorte -->
          <div class="col-md-7">
            <div class="p-3 bg-light rounded border h-100">
              <div class="d-flex align-items-center gap-2 mb-2">
                <span class="wf-pill-tag">Mercado de Capitales</span>
                <span class="small text-muted">Duración: <strong>8 Semanas (40 Horas)</strong></span>
              </div>
              <h4 style="font-size: 16px; font-weight: 800; color: #081426; margin-bottom: 8px;">Especialización en Idoneidad Bursátil CNV</h4>
              <p class="small text-muted mb-3">Preparación integral para el examen oficial de Idóneo con clases sincrónicas, materiales normativos y simuladores de examen.</p>
              
              <div class="p-2 bg-white rounded border" style="font-size: 12px;">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted">Cohorte asignada:</span>
                  <strong>Cohorte 2026-1</strong>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted">Inicio de dictado:</span>
                  <strong>01 de Marzo de 2026</strong>
                </div>
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted">Docente Titular:</span>
                  <strong>Mg. Elena Valenzuela</strong>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted">Progreso inicial:</span>
                  <strong class="text-primary">0% (Unidad 1: Marco Regulatorio)</strong>
                </div>
              </div>
            </div>
          </div>

          <!-- Resumen Arancelario y Derivación al Pago -->
          <div class="col-md-5">
            <div class="p-3 border rounded bg-white shadow-sm h-100 d-flex flex-column justify-content-between">
              <div>
                <span class="wf-label mb-2">Resumen de Matrícula</span>
                <div class="d-flex justify-content-between mb-2 small">
                  <span class="text-muted">Arancel del Programa:</span>
                  <strong>$120.000 ARS</strong>
                </div>
                <div class="d-flex justify-content-between mb-2 small text-success">
                  <span>Descuento Aplicado:</span>
                  <strong>-$0 ARS</strong>
                </div>
                <div class="d-flex justify-content-between border-top pt-2" style="font-size: 16px; color: #081426;">
                  <span class="fw-bold">Total a Pagar:</span>
                  <strong style="color: var(--wf-navy);">$120.000 ARS</strong>
                </div>
              </div>

              <div class="mt-4 pt-3 border-top">
                <div class="small text-muted mb-3" style="line-height: 1.3;">
                  <i class="fa-solid fa-shield-halved text-success me-1"></i> El acceso se habilitará automáticamente al confirmarse la acreditación del pago.
                </div>
                <div class="d-flex align-items-center gap-2">
                  <a href="#CU-47" class="wf-btn wf-btn-primary w-100 d-flex align-items-center justify-content-center gap-2" style="font-weight: 700; height: 42px; text-decoration: none;">
                    <i class="fa-solid fa-credit-card"></i>
                    <span>Proceder al Pago</span>
                  </a>
                  <span class="pin-badge">${badges[0] || 'A'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
          <a href="#CU-06" class="wf-btn wf-btn-outline"><i class="fa-solid fa-arrow-left me-1"></i> Volver al Catálogo</a>
          <span class="small text-muted">ID Inscripción Temporal: <strong>#INS-2026-9042</strong></span>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11C-2: REALIZAR PAGO ONLINE CON MODO QR --- CU-47
  if (id === 'CU-47') {
    return `
      <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Inscripción</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> 
              <strong style="color: #081426;">Pasarela de Pago MODO API</strong>
            </div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Realizar Pago Online</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Curso: <strong>Especialización en Idoneidad Bursátil (Cohorte 2026-1)</strong></p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Total: $120.000 ARS</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="row g-4">
          <div class="col-md-6">
            <div class="mb-3">
              <label class="wf-label">Seleccione el Medio de Pago</label>
              <div class="d-flex flex-column gap-2">
                <label class="p-3 border rounded d-flex align-items-center justify-content-between bg-light cursor-pointer" style="border-color: #2563EB !important;">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-qrcode text-primary" style="font-size: 18px;"></i>
                    <strong>MODO Billetera Virtual (QR Interoperable)</strong>
                  </div>
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </label>
                <label class="p-3 border rounded d-flex align-items-center justify-content-between cursor-pointer">
                  <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-credit-card text-muted"></i>
                    <span>Tarjeta de Débito / Crédito</span>
                  </div>
                </label>
              </div>
            </div>

            <div class="p-3 border rounded bg-light">
              <div class="d-flex justify-content-between mb-2"><span>Arancel Oficial:</span><strong>$120.000 ARS</strong></div>
              <div class="d-flex justify-content-between mb-2 text-success"><span>Beca / Descuento:</span><strong>-$0 ARS</strong></div>
              <div class="d-flex justify-content-between border-top pt-2" style="font-size: 15px; color: #081426;">
                <span>Total a Pagar:</span><strong>$120.000 ARS</strong>
              </div>
            </div>
          </div>

          <div class="col-md-6 text-center">
            <div class="p-3 border rounded bg-white shadow-sm h-100 d-flex flex-column align-items-center justify-content-center">
              <div class="small fw-bold text-muted text-uppercase mb-2">Escanee con App MODO o Billetera Bancaria</div>
              <div style="width: 170px; height: 170px; background: #081426; border-radius: 8px; display: flex; align-items: center; justify-content: center; position: relative; padding: 12px;">
                <div style="background: white; width: 100%; height: 100%; border-radius: 4px; display: flex; align-items: center; justify-content: center;">
                  <i class="fa-solid fa-qrcode" style="font-size: 110px; color: #081426;"></i>
                </div>
              </div>
              <div class="small text-muted mt-2">Transacción encriptada BCRA • Acreditación automática</div>
              <div class="mt-2">
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-3 mt-4 border-top">
          <a href="#CU-44" class="wf-btn wf-btn-outline">Cancelar</a>
          <div class="small text-muted"><i class="fa-solid fa-circle-check text-success me-1"></i> Aguardando notificación de webhook de MODO API...</div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11D: DAR DE BAJA INSCRIPCIÓN --- CU-45
  if (id === 'CU-45') {
    return `
      <div class="wf-card" style="max-width: 640px; margin: 30px auto; background: #FFFFFF;">
        <div class="text-center mb-4">
          <div class="wf-icon-danger mb-3" style="width: 54px; height: 54px; border-radius: 50%; background: #FEE2E2; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
            <i class="fa-solid fa-triangle-exclamation" style="font-size: 24px; color: #DC2626;"></i>
          </div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Dar de Baja Inscripción</h3>
          <p class="small text-muted" style="margin: 4px 0 0;">Esta acción cancelará su matrícula en la cohorte seleccionada sin derecho a reembolso.</p>
        </div>

        <div class="p-3 bg-light rounded border mb-4">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="small text-muted">Inscripción a dar de baja:</span>
            <div class="d-flex align-items-center gap-2">
              <strong style="font-size: 13px; color: #081426;">Idoneidad Bursátil (Cohorte 2026-1)</strong>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="d-flex justify-content-between align-items-center">
            <span class="small text-muted">Alumno matriculado:</span>
            <strong style="font-size: 13px; color: #081426;">Joaquín Küster (DNI: 40.123.456)</strong>
          </div>
        </div>

        <div class="mb-4">
          <label class="wf-label">Motivo de la baja (Opcional):</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3" placeholder="Indique brevemente el motivo de su baja..."></textarea>
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <a href="#CU-43" class="wf-btn wf-btn-outline">Cancelar / Volver</a>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-danger"><i class="fa-solid fa-user-xmark me-1"></i> Confirmar Baja Definitiva</button>
            <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 12: ESTADÍSTICAS, INFORMES & AUDITORÍA --- CU-95, CU-96, CU-97, CU-98
  if (['CU-95', 'CU-96', 'CU-97', 'CU-98'].includes(id)) {
    const isAudit = id === 'CU-95';
    const isStats = id === 'CU-98';

    return `
      <div class="wf-card" style="max-width: 1100px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">${isAudit ? 'Registro de Auditoría del Sistema' : 'Tablero Analítico & Estadísticas Clave'}</h3>
            <p class="small text-muted" style="margin: 0;">Supervisión en tiempo real de ingresos, métricas de retención y logs de eventos.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Actualizado al Instante</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        ${!isAudit ? `
          <!-- KPI Cards -->
          <div class="row g-3 mb-4">
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Total Alumnos Matriculados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #0F172A; margin: 4px 0;">1,420</h2>
                <div class="small text-success">↑ +14% este mes</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Ingresos Facturados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #059669; margin: 4px 0;">$17.4M ARS</h2>
                <div class="small text-success">↑ +22% vs trimestre anterior</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Tasa de Aprobación</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #2563EB; margin: 4px 0;">88.5%</h2>
                <div class="small text-muted">Promedio en autoevaluaciones</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Clases Clon Generadas</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #7C3AED; margin: 4px 0;">64</h2>
                <div class="small text-muted">HeyGen AI Studio</div>
              </div>
            </div>
          </div>
        ` : ''}

        <div class="wf-card mb-4" style="background: #FFFFFF;">
          <div class="row g-3 align-items-end">
            <div class="col-md-5">
              <label class="wf-label">Criterio de búsqueda / Filtro principal</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" placeholder="Buscar en ${name}...">
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="wf-label">Filtro Secundario / Rango</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option>Últimos 30 días</option>
                  <option>Últimos 90 días</option>
                  <option>Todo el historial</option>
                </select>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
            <div class="col-md-3">
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-${id === 'CU-97' ? 'file-invoice-dollar' : 'filter'} me-1"></i> ${id === 'CU-97' ? 'Generar Reporte de Ingresos' : 'Filtrar Eventos'}</button>
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>
          </div>
        </div>

        ${isStats ? `
          <div class="row g-3 mb-4">
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Total Alumnos Matriculados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #081426; margin: 4px 0;">1.240</h2>
                <div class="small text-success">↑ +14% este mes</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Ingresos Facturados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #059669; margin: 4px 0;">$17.4M ARS</h2>
                <div class="small text-success">↑ +22% vs trimestre anterior</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Tasa de Aprobación</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #2563EB; margin: 4px 0;">88.5%</h2>
                <div class="small text-muted">Promedio en autoevaluaciones</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Clases Clon Generadas</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #7C3AED; margin: 4px 0;">64</h2>
                <div class="small text-muted">HeyGen AI Studio</div>
              </div>
            </div>
          </div>
        ` : ''}

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>${isAudit ? 'Timestamp' : 'Curso / Programa'}</th>
                <th>${isAudit ? 'Usuario / Actor' : 'Cohorte'}</th>
                <th>${isAudit ? 'Acción / Evento' : 'Alumnos Activos'}</th>
                <th>${isAudit ? 'Módulo Afectado' : 'Facturación'}</th>
                <th class="text-end">Acciones / Descargas</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>${isAudit ? '2026-08-26 07:45:12' : 'Especialización en Idoneidad Bursátil'}</strong></td>
                <td>${isAudit ? 'Mg. Elena Valenzuela (Docente)' : '2026-1 (En curso)'}</td>
                <td>${isAudit ? 'Crear Clon de IA (voice_id: #v92)' : '840 inscriptos'}</td>
                <td>${isAudit ? 'MOD-F-06: IA' : '$10.080.000 ARS'}</td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-file-pdf me-1 text-danger"></i> ${id === 'CU-97' ? 'Descargar Reporte' : 'Descargar PDF / Excel'}</button>
                    <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 13: CUENTA / PERFIL / LOGIN / RECUPERO --- CU-81, CU-86, CU-87, CU-90, CU-92
  if (['CU-81', 'CU-86', 'CU-87', 'CU-90', 'CU-92'].includes(id)) {
    const isLogin = id === 'CU-90';
    const isRegister = id === 'CU-81';
    const isRecovery = id === 'CU-92';
    const isProfile = id === 'CU-86' || id === 'CU-87';

    if (isRecovery) {
      return `
        <div class="wf-card" style="max-width: 500px; margin: 30px auto; background: #FFFFFF;">
          <div class="text-center mb-4">
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1 mb-2"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>¿Olvidaste tu contraseña?</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
            <h3 style="font-size: 19px; font-weight: 800; color: #081426; margin: 0;">Recuperar y Restablecer Contraseña</h3>
            <p class="small text-muted" style="margin: 4px 0 0;">Ingrese su correo institucional para recibir el enlace de recuperación.</p>
          </div>

          <div class="mb-3">
            <label class="wf-label">Correo Electrónico Registrado</label>
            <div class="wf-input-wrap">
              <input type="email" class="wf-input" value="usuario@correo.com">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top mt-4">
            <a href="#CU-90" class="wf-btn wf-btn-outline">Volver al Login</a>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-envelope me-1"></i> Enviar Enlace</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>

          <hr class="my-4">
          <div class="small text-muted text-center mb-3">Una vez recibido el enlace, cree su nueva contraseña:</div>
          <div class="mb-3">
            <label class="wf-label">Nueva Contraseña Segura</label>
            <div class="wf-input-wrap">
              <input type="password" class="wf-input" value="••••••••••••">
              <span class="pin-badge">${badges[3] || 'D'}</span>
            </div>
          </div>
          <div class="mb-3">
            <label class="wf-label">Confirmar Nueva Contraseña</label>
            <div class="wf-input-wrap">
              <input type="password" class="wf-input" value="••••••••••••">
            </div>
          </div>
          <div class="d-flex justify-content-end align-items-center gap-2 pt-3 border-top">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-key me-1"></i> Restablecer Contraseña</button>
            <span class="pin-badge">${badges[4] || badges[badges.length - 1] || 'E'}</span>
          </div>
        </div>
      `;
    }

    if (isLogin || isRegister) {
      return `
        <div class="wf-card" style="max-width: 480px; margin: 40px auto; box-shadow: 0 12px 32px rgba(0,0,0,0.08); background: #FFFFFF;">
          <div class="text-center mb-4">
            <h3 style="font-size: 20px; font-weight: 800; color: #081426;">${isLogin ? 'Iniciar Sesión en Idóneos Online' : 'Crear Cuenta de Usuario'}</h3>
            <p class="small text-muted">${isLogin ? 'Ingrese sus credenciales académicas para acceder' : 'Complete sus datos para registrarse en la plataforma'}</p>
          </div>

          <div class="mb-3">
            <label class="wf-label">Correo Electrónico Institucional</label>
            <div class="wf-input-wrap">
              <input type="email" class="wf-input" value="${isLogin ? 'elena.valenzuela@idoneos.online' : 'usuario@correo.com'}">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <div class="mb-3">
            <label class="wf-label">Contraseña</label>
            <div class="wf-input-wrap">
              <input type="password" class="wf-input" value="••••••••••••">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>

          <div class="d-flex flex-column gap-2 mt-4">
            <div class="d-flex align-items-center gap-2 w-100">
              <button class="wf-btn wf-btn-primary w-100">${isLogin ? 'Iniciar Sesión' : 'Registrarme'}</button>
              <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
            </div>
            
            ${isLogin ? `
              <div class="text-center my-2 text-muted small">o continúe con</div>
              <div class="d-flex align-items-center gap-2 w-100">
                <button class="wf-btn wf-btn-outline w-100"><i class="fa-brands fa-google me-1"></i> Continuar con Google</button>
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>
            ` : ''}
          </div>
        </div>
      `;
    }

    if (isProfile) {
      return `
        <div class="wf-card" style="max-width: 880px; margin: 0 auto; background: #FFFFFF;">
          <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <div style="width: 56px; height: 56px; border-radius: 50%; background: #081426; border: 2px solid var(--wf-gold); display: flex; align-items: center; justify-content: center; color: var(--wf-gold); font-size: 24px; box-shadow: 0 4px 12px rgba(8,20,38,0.15);">
                <i class="fa-solid fa-user-tie"></i>
              </div>
              <div>
                <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Perfil de Usuario: Mg. Elena Valenzuela</h3>
                <p class="small text-muted" style="margin: 2px 0 0;"><i class="fa-solid fa-graduation-cap me-1 text-primary"></i> Docente Titular • Mercado de Capitales & Finanzas</p>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <a href="#CU-76" class="wf-btn wf-btn-sm wf-btn-primary d-inline-flex align-items-center gap-2" style="background: #7C3AED; border-color: #6D28D9; font-weight: 700;">
                <i class="fa-solid fa-wand-magic-sparkles"></i>
                <span>Configurar Clon de IA</span>
              </a>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="wf-label">Nombre Completo</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Elena Valenzuela">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
            <div class="col-md-6">
              <label class="wf-label">Correo Electrónico Institucional</label>
              <input type="email" class="wf-input bg-light" value="elena.valenzuela@idoneos.online" disabled>
            </div>
            <div class="col-md-6">
              <label class="wf-label">Estado de Habilitación Docente</label>
              <div class="p-2 rounded border bg-light d-flex align-items-center justify-content-between">
                <span class="small fw-bold text-success"><i class="fa-solid fa-circle-check me-1"></i> Activo y Habilitado para Dictado</span>
                <span class="badge bg-success text-white" style="font-size: 10px;">Vigente</span>
              </div>
            </div>
            <div class="col-md-6">
              <label class="wf-label">Estado de Clon Digital (HeyGen)</label>
              <div class="p-2 rounded border bg-light d-flex align-items-center justify-content-between">
                <span class="small text-muted"><i class="fa-solid fa-robot me-1 text-primary"></i> Avatar vinculado: <strong>#avatar_valenzuela_v2</strong></span>
                <a href="#CU-76" class="small fw-bold text-primary text-decoration-none">Reconfigurar</a>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
            <button class="wf-btn wf-btn-outline wf-btn-sm">Cancelar</button>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary wf-btn-sm"><i class="fa-solid fa-floppy-disk me-1"></i> Guardar Cambios</button>
              <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
            </div>
          </div>
        </div>
      `;
    }
  }

  // --- SPECIALIZED 14: BAJA / ELIMINACIÓN GENÉRICA ---
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

    let bgEntityName = `Registro #${id.replace('CU-', '')}`;
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
      bgEntityName = 'Usuario: Mg. Elena Valenzuela (elena.valenzuela@idoneos.online)';
      bgExtraInfo = 'Validación: Desactivación de credenciales y accesos';
    }

    return `
      <div class="wf-modal-dialog shadow-sm" style="max-width: 620px; margin: 40px auto; background: #FFFFFF; border-radius: 12px; border: 1px solid #E2E8F0; overflow: hidden;">
        <!-- Cabecera del Diálogo -->
        <div class="p-3 border-bottom d-flex justify-content-between align-items-center" style="background: #F8FAFC;">
          <div class="d-flex align-items-center gap-2">
            <i class="fa-solid fa-triangle-exclamation text-danger"></i>
            <strong style="font-size: 14px; color: #081426;">${name}</strong>
          </div>
          <span class="wf-badge status-active">Confirmación Requerida</span>
        </div>

        <!-- Cuerpo del Diálogo -->
        <div class="p-4 text-center">
          <div class="mb-3" style="width: 56px; height: 56px; border-radius: 50%; background: #FEE2E2; color: #DC2626; display: flex; align-items: center; justify-content: center; margin: 0 auto; font-size: 24px;">
            <i class="fa-solid fa-triangle-exclamation"></i>
          </div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin-bottom: 6px;">¿Confirma la operación de ${name.toLowerCase()}?</h3>
          <p class="small text-muted mb-4" style="line-height: 1.5;">Esta acción procesará el cambio de estado en la base de datos y afectará la disponibilidad del elemento en el sistema.</p>

          <div class="p-3 mb-4 bg-light border rounded text-start" style="font-size: 13px;">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Registro afectado:</span>
              <strong style="color: #081426;">${bgEntityName}</strong>
            </div>
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="text-muted">Estado actual:</span>
              <span class="wf-badge status-active">Activo / Vigente</span>
            </div>
            <div class="d-flex justify-content-between align-items-center">
              <span class="text-muted">Validación de dependencias:</span>
              <span class="small text-success fw-bold"><i class="fa-solid fa-circle-check me-1"></i> ${bgExtraInfo}</span>
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
            <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-danger"><i class="fa-solid fa-trash me-1"></i> ${confirmBtnLabel}</button>
              <span class="pin-badge">${badges[1] || badges[badges.length - 1] || 'B'}</span>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 15: BÚSQUEDAS / TABLAS DE GESTIÓN ---
  const isSearch = name.toLowerCase().startsWith('buscar') || name.toLowerCase().startsWith('consultar') || name.toLowerCase().startsWith('ver') || name.toLowerCase().startsWith('explorar');
  if (isSearch) {
    let actionBtnLabel = 'Seleccionar / Ver';
    let createBtnLabel = '';
    let createCuTarget = '';

    if (name.includes('categoría')) {
      actionBtnLabel = 'Editar';
      createBtnLabel = 'Nueva Categoría';
      createCuTarget = 'CU-08';
    } else if (name.includes('cohorte')) {
      actionBtnLabel = 'Editar Cohorte';
      createBtnLabel = 'Nueva Cohorte';
      createCuTarget = 'CU-12';
    } else if (name.includes('programa')) {
      actionBtnLabel = 'Editar Programa';
      createBtnLabel = 'Nuevo Programa';
      createCuTarget = 'CU-16';
    } else if (name.includes('descuento')) {
      actionBtnLabel = 'Editar';
      createBtnLabel = 'Nuevo Descuento';
      createCuTarget = 'CU-50';
    } else if (name.includes('usuario')) {
      actionBtnLabel = 'Editar Usuario';
      createBtnLabel = 'Nuevo Usuario';
      createCuTarget = 'CU-81';
    } else if (name.includes('docente')) {
      actionBtnLabel = 'Editar Perfil';
      createBtnLabel = 'Nuevo Docente';
      createCuTarget = 'CU-83';
    } else if (name.includes('curso') || name.includes('catálogo')) {
      actionBtnLabel = 'Ver Ficha / Inscribirme';
    } else if (name.includes('parámetro')) {
      actionBtnLabel = 'Editar Valor';
    } else if (name.includes('intento')) {
      actionBtnLabel = 'Revisar Intento';
    }

    return `
      <!-- Cabecera de Gestión -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h3 style="font-size: 19px; font-weight: 800; color: #081426; margin: 0;">${name}</h3>
          <p class="small text-muted m-0">Consulte, filtre y gestione los registros activos del sistema</p>
        </div>
      </div>

      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Criterio de búsqueda / Filtro principal</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar en ${name}...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtro Secundario / Estado</label>
            <div class="wf-select-container">
              <div class="wf-input-wrap">
                <div class="wf-input wf-select-trigger">
                  <span>Todos los registros</span>
                  <i class="fa-solid fa-chevron-down" style="font-size: 11px;"></i>
                </div>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div class="wf-dropdown-menu">
                <div class="wf-dropdown-item active">☑ Todos los registros</div>
                <div class="wf-dropdown-item">☐ Activos / Vigentes</div>
                <div class="wf-dropdown-item">☐ Históricos</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> ${name.includes('mis cursos') || name.includes('participantes') || name.includes('programa') ? 'Filtrar' : 'Buscar'}</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Identificador / Nombre</th>
              <th>Detalle / Contexto</th>
              <th>Registro</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>Elemento Principal #${id.replace('CU-', '')}A</strong></td>
              <td>Mercado de Capitales & Finanzas</td>
              <td>2026-08-25</td>
              <td><span class="wf-badge status-active">Activo</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> ${actionBtnLabel}</button>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                  ${!name.includes('curso') && !name.includes('catálogo') && !name.includes('participantes') ? `
                    <button class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja"><i class="fa-solid fa-trash"></i></button>
                  ` : ''}
                </div>
              </td>
            </tr>
            <tr>
              <td><strong>Elemento Secundario #${id.replace('CU-', '')}B</strong></td>
              <td>Valuación & Finanzas Corporativas</td>
              <td>2026-08-20</td>
              <td><span class="wf-badge status-active">Activo</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> ${actionBtnLabel}</button>
                  ${!name.includes('curso') && !name.includes('catálogo') && !name.includes('participantes') ? `
                    <button class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Dar de baja"><i class="fa-solid fa-trash"></i></button>
                  ` : ''}
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- SPECIALIZED 16: FORMULARIOS DE REGISTRO / MODIFICACIÓN ESPECÍFICOS ---
  const isMod = name.toLowerCase().startsWith('modificar') || name.toLowerCase().startsWith('editar');
  let saveBtnText = isMod ? 'Guardar Cambios' : 'Confirmar y Guardar';
  if (name.includes('curso') && !isMod) saveBtnText = 'Guardar Curso';
  if (name.includes('categoría') && !isMod) saveBtnText = 'Guardar Categoría';
  if (name.includes('cohorte') && !isMod) saveBtnText = 'Guardar Cohorte';
  if (name.includes('programa') && !isMod) saveBtnText = 'Guardar Programa';
  if (name.includes('descuento') && !isMod) saveBtnText = 'Guardar Descuento';
  if (name.includes('usuario') && !isMod) saveBtnText = 'Guardar Usuario';
  if (name.includes('docente') && !isMod) saveBtnText = 'Guardar Docente';
  if (name.includes('parámetro')) saveBtnText = 'Guardar Parámetro';

  let triggerBtnLabel = isMod ? 'Editar' : `+ Nuevo ${name.replace('Registrar ', '').replace('Crear ', '')}`;
  if (name.includes('curso') && !isMod) triggerBtnLabel = '+ Nuevo Curso';
  if (name.includes('categoría') && !isMod) triggerBtnLabel = '+ Nueva Categoría';
  if (name.includes('cohorte') && !isMod) triggerBtnLabel = '+ Nueva Cohorte';
  if (name.includes('programa') && !isMod) triggerBtnLabel = '+ Nuevo Programa';
  if (name.includes('descuento') && !isMod) triggerBtnLabel = '+ Nuevo Descuento';
  if (name.includes('usuario') && !isMod) triggerBtnLabel = '+ Nuevo Usuario';
  if (name.includes('docente') && !isMod) triggerBtnLabel = '+ Nuevo Docente';
  if (name.includes('programa') && isMod) triggerBtnLabel = 'Editar Programa';
  if (name.includes('docente') && isMod) triggerBtnLabel = 'Editar Perfil Docente';
  if (name.includes('pool') && isMod) triggerBtnLabel = 'Editar Pool';
  if (name.includes('autoevaluación') && isMod) triggerBtnLabel = 'Editar Cuestionario';
  if (name.includes('consulta') && isMod) triggerBtnLabel = 'Editar Mensaje';
  if (name.includes('respuesta') && isMod) triggerBtnLabel = 'Editar Respuesta';
  if (name.includes('clon') && isMod) triggerBtnLabel = 'Editar Guión';
  if (name.includes('vivo') && isMod) triggerBtnLabel = 'Reprogramar / Editar';
  if (name.includes('parámetro')) triggerBtnLabel = 'Editar Valor';

  if (id === 'CU-03' || id === 'CU-04') {
    return `
      <div class="wf-card" style="max-width: 900px; margin: 0 auto; background: #FFFFFF;">
        <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${isMod ? 'Modificar Curso' : 'Registrar Nuevo Curso'}</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Defina los parámetros académicos, docentes y comerciales del programa de capacitación.</p>
          </div>
          <div>
            <span class="wf-badge status-active">${isMod ? 'Modo Edición' : 'Formulario de Alta'}</span>
          </div>
        </div>

        <div class="row g-3">
          <!-- Nombre [B] -->
          <div class="col-md-8">
            <label class="wf-label">Nombre del Curso</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Especialización en Idoneidad Bursátil CNV">
              <span class="pin-badge">B</span>
            </div>
          </div>

          <!-- Categoría [C] -->
          <div class="col-md-4">
            <label class="wf-label">Categoría Temática</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option selected>Mercado de Capitales & Finanzas</option>
                <option>Impuestos & Contabilidad</option>
                <option>Cripto & DeFi</option>
              </select>
              <span class="pin-badge">C</span>
            </div>
          </div>

          <!-- Nivel [D] -->
          <div class="col-md-4">
            <label class="wf-label">Nivel de Dificultad</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option selected>Intermedio / Profesional</option>
                <option>Inicial / Básico</option>
                <option>Avanzado</option>
              </select>
              <span class="pin-badge">D</span>
            </div>
          </div>

          <!-- Precio [E] -->
          <div class="col-md-4">
            <label class="wf-label">Precio / Arancel de Inscripción</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="$120.000 ARS">
              <span class="pin-badge">E</span>
            </div>
          </div>

          <!-- Modalidades [F] (Checkbox interactivos) -->
          <div class="col-md-4">
            <label class="wf-label">Modalidades de Dictado</label>
            <div class="wf-input-wrap">
              <div class="wf-input d-flex align-items-center gap-3" style="padding: 8px 14px; font-size: 12px; background: #FFFFFF;">
                <label class="d-flex align-items-center gap-1 cursor-pointer mb-0" style="font-weight: 600; color: #081426;">
                  <input type="checkbox" checked style="width: 16px; height: 16px; accent-color: var(--wf-gold);">
                  <span>Online Asincrónico</span>
                </label>
                <label class="d-flex align-items-center gap-1 cursor-pointer mb-0" style="font-weight: 600; color: #081426;">
                  <input type="checkbox" checked style="width: 16px; height: 16px; accent-color: var(--wf-gold);">
                  <span>En Vivo</span>
                </label>
              </div>
              <span class="pin-badge">F</span>
            </div>
          </div>

          <!-- Docente Titular [G] -->
          <div class="col-md-6">
            <label class="wf-label">Docente Titular Responsable</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option selected>Mg. Elena Valenzuela (Economista & Consultor)</option>
                <option>Dr. Roberto Cachanosky</option>
              </select>
              <span class="pin-badge">G</span>
            </div>
          </div>

          <!-- Docentes Ayudantes [H] -->
          <div class="col-md-6">
            <label class="wf-label">Equipo de Docentes Ayudantes</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="Lic. Joaquín Küster, Mg. Elena Valenzuela">
              <span class="pin-badge">H</span>
            </div>
          </div>

          <!-- Emite Certificado [I] -->
          <div class="col-12">
            <div class="p-3 bg-light rounded border d-flex justify-content-between align-items-center">
              <div>
                <strong>Emisión Automática de Certificado Oficial</strong>
                <p class="small text-muted m-0">Generar credencial digital verificable con código QR al aprobar el 100% de autoevaluaciones.</p>
              </div>
              <div class="d-flex align-items-center gap-2">
                <input type="checkbox" checked style="width: 18px; height: 18px; accent-color: var(--wf-gold);">
                <span class="pin-badge">I</span>
              </div>
            </div>
          </div>

          <!-- Descripción [J] -->
          <div class="col-12">
            <label class="wf-label">Descripción Académica y Objetivos</label>
            <div class="wf-input-wrap">
              <textarea class="wf-input" rows="3">Programa intensivo diseñado para brindar los conocimientos teóricos, regulatorios y prácticos requeridos para rendir y aprobar el examen de Idóneo en Mercado de Capitales ante la Comisión Nacional de Valores (CNV).</textarea>
              <span class="pin-badge">J</span>
            </div>
          </div>

          <!-- Portada [K] y Examinar [L] -->
          <div class="col-12">
            <label class="wf-label">Imagen de Portada / Banner del Curso</label>
            <div class="d-flex align-items-center gap-2">
              <div class="wf-input-wrap flex-grow-1">
                <input type="text" class="wf-input" value="portada_idoneidad_bursatil_2026.png">
                <span class="pin-badge">K</span>
              </div>
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-outline"><i class="fa-solid fa-folder-open me-1"></i> Examinar...</button>
                <span class="pin-badge">L</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer con Guardar [M] -->
        <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> ${isMod ? 'Guardar Cambios' : 'Guardar Curso'}</button>
            <span class="pin-badge">M</span>
          </div>
        </div>
      </div>
    `;
  }

  return `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <!-- Barra Superior de la Tarjeta con Acción de Disparo [A] -->
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${isMod ? 'Modificar' : 'Registrar'} ${name.replace('Registrar ', '').replace('Modificar ', '').replace('Crear ', '')}</h3>
          <p class="small text-muted" style="margin: 3px 0 0;">Complete los parámetros requeridos por la operación del sistema.</p>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="wf-btn wf-btn-sm wf-btn-outline active" style="font-weight: 700;">
            <i class="fa-solid ${isMod ? 'fa-pen-to-square' : 'fa-plus'} me-1"></i>
            ${triggerBtnLabel}
          </span>
          <span class="pin-badge">${badges[0] || 'A'}</span>
          <span class="wf-badge status-active ms-2">${isMod ? 'Modo Edición' : 'Formulario de Alta'}</span>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre / Denominación</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="${name.includes('categoría') ? 'Mercado de Capitales' : (name.includes('cohorte') ? 'Cohorte 2026-1' : (name.includes('descuento') ? 'Beca Convenio UNaM 2026' : (name.includes('docente') ? 'Mg. Elena Valenzuela' : 'Registro de ' + name)))}">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Categoría / Asociación</label>
          <div class="wf-select-container">
            <div class="wf-input-wrap">
              <div class="wf-input wf-select-trigger">
                <span>Mercado de Capitales</span>
                <i class="fa-solid fa-chevron-down" style="font-size: 11px;"></i>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Descripción Académica / Contenido</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Descripción y especificaciones correspondientes al registro de ${name}.</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> ${saveBtnText}</button>
          <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
        </div>
      </div>
    </div>
  `;
}

// 6. Build Complete HTML Page with User Profile Floating Dropdown Menu
let html = `<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Wireframes UI — Idóneos Online (${cus.length} Casos de Uso Reales)</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <style>
        /* ==========================================================================
           ESTILO OFICIAL DEFINITIVO — IDÓNEOS ONLINE
           Paleta: Deep Navy (#081426, #0F233D), Acento Oro (#D4A03D, #C59030), Blanco (#FFFFFF)
           Bordes suaves, tarjetas con pill badges de categoría/aranceles y botones dorados.
           ========================================================================== */
        :root {
            --wf-navy-dark: #081426;
            --wf-navy-header: #071324;
            --wf-navy-card-top: #132A4A;
            --wf-gold: #D4A03D;
            --wf-gold-hover: #C59030;
            --wf-gold-light: #FEF3C7;
            --wf-gold-text: #92400E;
            --wf-bg: #F4F6F9;
            --wf-canvas: #E9ECEF;
            --wf-border: #DDE2E8;
            --wf-text: #0F172A;
            --wf-text-muted: #64748B;
            --wf-surface: #FFFFFF;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            -webkit-font-smoothing: antialiased;
        }

        body {
            background-color: var(--wf-canvas);
            color: var(--wf-text);
            display: flex;
            height: 100vh;
            overflow: hidden;
        }

        i {
            display: inline-block;
            vertical-align: middle;
        }

        /* Navegación Lateral */
        #nav-sidebar {
            width: 310px;
            background: #081426;
            color: #FFFFFF;
            overflow-y: auto;
            border-right: 1px solid #142844;
            display: flex;
            flex-direction: column;
            flex-shrink: 0;
        }

        .nav-header {
            padding: 20px;
            border-bottom: 1px solid #142844;
            background: #050E1B;
        }

        .nav-header h2 {
            font-size: 14px;
            font-weight: 800;
            letter-spacing: 0.5px;
            color: #FFFFFF;
        }

        .nav-header p {
            font-size: 11px;
            color: #94A3B8;
            margin-top: 3px;
        }

        .nav-search {
            padding: 12px 16px;
            border-bottom: 1px solid #142844;
        }

        .nav-search input {
            width: 100%;
            padding: 8px 12px;
            border-radius: 6px;
            border: 1px solid #1E3A5F;
            background: #0F233D;
            color: #FFFFFF;
            font-size: 12px;
            outline: none;
            transition: all 0.15s;
        }

        .nav-search input:focus {
            border-color: var(--wf-gold);
        }

        .module-group {
            border-bottom: 1px solid #142844;
        }

        .module-header {
            padding: 10px 16px;
            background: #0D2038;
            font-size: 11px;
            font-weight: 700;
            color: var(--wf-gold);
            letter-spacing: 0.3px;
            border-left: 3px solid var(--wf-gold);
        }

        .module-list {
            list-style: none;
        }

        .nav-item a {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px 16px;
            color: #94A3B8;
            text-decoration: none;
            font-size: 12px;
            transition: all 0.15s;
        }

        .nav-item a:hover {
            background: #132A4A;
            color: #FFFFFF;
            padding-left: 20px;
        }

        .nav-item .cu-tag {
            font-size: 10px;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 4px;
            background: rgba(255,255,255,0.08);
            color: #E2E8F0;
            border: 1px solid rgba(255,255,255,0.1);
        }

        /* Viewport Central (Canvas de Figma) */
        #viewport {
            flex: 1;
            overflow-y: auto;
            padding: 40px;
            display: flex;
            flex-direction: column;
            gap: 64px;
            align-items: center;
        }

        .figure-wrapper {
            width: 100%;
            max-width: 1160px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .figure-caption {
            font-size: 13px;
            font-weight: 600;
            color: #475569;
            text-align: left;
            padding-left: 2px;
        }

        /* Marco de Pantalla / Screen Frame Oficial */
        .screen-frame {
            width: 100%;
            min-height: 680px;
            background: #F8FAFC;
            border-radius: 8px;
            border: 1px solid #CBD5E1;
            box-shadow: 0 10px 30px rgba(8, 20, 38, 0.08);
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        /* Top Navbar Oficial (Navy Header) */
        .wf-top-navbar {
            height: 58px;
            background: var(--wf-navy-header);
            padding: 0 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: relative;
            z-index: 100;
            border-bottom: 1px solid #142844;
        }

        .wf-brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .wf-brand-logo {
            font-size: 16px;
            font-weight: 800;
            color: #FFFFFF;
            letter-spacing: -0.3px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .wf-brand-logo span {
            color: var(--wf-gold);
        }

        .wf-brand-sub {
            font-size: 9px;
            font-weight: 700;
            letter-spacing: 0.8px;
            color: #94A3B8;
            text-transform: uppercase;
        }

        .wf-top-nav-links {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .wf-top-nav-link {
            color: #CBD5E1;
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            transition: color 0.15s;
        }

        .wf-top-nav-link:hover {
            color: #FFFFFF;
        }

        .wf-top-nav-pill {
            padding: 5px 12px;
            border-radius: 20px;
            border: 1px solid rgba(212, 160, 61, 0.4);
            color: var(--wf-gold);
            font-size: 11px;
            font-weight: 700;
            background: rgba(212, 160, 61, 0.08);
        }

        .wf-user-trigger-pill {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 3px 12px 3px 4px;
            border-radius: 20px;
            border: 1px solid #1E3A5F;
            background: #0F233D;
            cursor: pointer;
            color: #FFFFFF;
        }

        .user-avatar-circle {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: var(--wf-gold);
            color: #081426;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 800;
        }

        /* Hero Banner del Cuerpo Docente */
        .wf-hero-banner {
            background: var(--wf-navy-header);
            padding: 24px 32px 28px;
            color: #FFFFFF;
            border-bottom: 1px solid #142844;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }

        .wf-hero-tag {
            font-size: 10px;
            font-weight: 800;
            color: var(--wf-gold);
            letter-spacing: 0.8px;
            text-transform: uppercase;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .wf-hero-title {
            font-size: 22px;
            font-weight: 800;
            color: #FFFFFF;
            letter-spacing: -0.3px;
        }

        .wf-hero-desc {
            font-size: 12px;
            color: #94A3B8;
            margin-top: 2px;
        }

        .wf-btn-gold {
            background: var(--wf-gold);
            color: #081426;
            font-size: 12px;
            font-weight: 700;
            padding: 8px 18px;
            border-radius: 20px;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(212, 160, 61, 0.25);
            transition: all 0.15s;
            text-decoration: none;
        }

        .wf-btn-gold:hover {
            background: var(--wf-gold-hover);
        }

        /* Main Content */
        .wf-body {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #F4F6F9;
            position: relative;
        }

        .wf-main-content {
            flex: 1;
            padding: 28px 32px;
            overflow-y: auto;
        }

        /* Footer Inferior Oficial */
        .wf-screen-footer {
            height: 48px;
            background: var(--wf-navy-header);
            border-top: 1px solid #142844;
            padding: 0 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #94A3B8;
        }

        .wf-screen-footer strong {
            color: #FFFFFF;
        }

        .wf-screen-footer strong span {
            color: var(--wf-gold);
        }

        /* Tarjetas de Cursos Oficiales */
        .wf-cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }

        .wf-course-card {
            background: #FFFFFF;
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid #E2E8F0;
            box-shadow: 0 6px 16px rgba(8, 20, 38, 0.05);
            display: flex;
            flex-direction: column;
            transition: transform 0.15s, box-shadow 0.15s;
        }

        .wf-course-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 24px rgba(8, 20, 38, 0.08);
        }

        .wf-course-card-thumb {
            height: 120px;
            background: var(--wf-navy-card-top);
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 12px;
        }

        .wf-course-thumb-icon {
            color: rgba(255,255,255,0.25);
            font-size: 32px;
            margin-bottom: 8px;
        }

        .wf-course-pills-row {
            display: flex;
            align-items: center;
            gap: 6px;
            width: 100%;
            justify-content: flex-start;
        }

        .wf-pill-tag {
            background: var(--wf-gold);
            color: #081426;
            font-size: 9px;
            font-weight: 800;
            padding: 3px 8px;
            border-radius: 12px;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }

        .wf-pill-status {
            background: var(--wf-gold);
            color: #081426;
            font-size: 9px;
            font-weight: 800;
            padding: 3px 8px;
            border-radius: 12px;
            letter-spacing: 0.3px;
        }

        .wf-course-card-body {
            padding: 18px 20px;
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .wf-course-title {
            font-size: 15px;
            font-weight: 800;
            color: #081426;
            margin-bottom: 6px;
            line-height: 1.3;
        }

        .wf-course-desc {
            font-size: 11px;
            color: #64748B;
            margin-bottom: 14px;
            line-height: 1.4;
            flex: 1;
        }

        .wf-course-info-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #64748B;
            font-weight: 600;
            padding-bottom: 12px;
            border-bottom: 1px solid #F1F5F9;
        }

        .wf-course-price {
            font-size: 13px;
            font-weight: 800;
            color: #081426;
        }

        .wf-course-card-footer {
            padding: 12px 20px 16px;
            background: #FFFFFF;
        }

        .wf-btn-manage-course {
            width: 100%;
            background: var(--wf-gold);
            color: #081426;
            font-size: 12px;
            font-weight: 700;
            padding: 9px 0;
            border-radius: 8px;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            cursor: pointer;
            transition: background 0.15s;
            text-decoration: none;
        }

        .wf-btn-manage-course:hover {
            background: var(--wf-gold-hover);
        }

        /* Pin Badges */
        .pin-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 26px;
            height: 26px;
            min-width: 26px;
            border-radius: 50%;
            background: #081426;
            color: #FFFFFF;
            font-size: 12px;
            font-weight: 800;
            border: 2px solid var(--wf-gold);
            box-shadow: 0 2px 6px rgba(8, 20, 38, 0.3);
            flex-shrink: 0;
            user-select: none;
        }

        /* Modal Box Modern 2-Columns */
        .wf-modal-box {
            background: #FFFFFF;
            border-radius: 12px;
            box-shadow: 0 20px 50px rgba(8, 20, 38, 0.15);
            border: 1px solid #CBD5E1;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }

        .wf-modal-header {
            padding: 18px 24px;
            background: #FFFFFF;
            border-bottom: 1px solid #E2E8F0;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .wf-modal-title {
            font-size: 17px;
            font-weight: 800;
            color: #081426;
        }

        .wf-modal-grid {
            display: grid;
            grid-template-columns: 320px 1fr;
            min-height: 420px;
        }

        .wf-modal-sidebar {
            background: #F8FAFC;
            border-right: 1px solid #E2E8F0;
            padding: 20px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .wf-modal-option {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 14px;
            border-radius: 8px;
            border: 1.5px solid transparent;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            color: #334155;
            transition: all 0.15s;
            margin-bottom: 6px;
        }

        .wf-modal-option:hover {
            background: #FFFFFF;
            border-color: #CBD5E1;
        }

        .wf-modal-option.active {
            background: #FFFFFF;
            border-color: var(--wf-gold);
            color: #081426;
            font-weight: 700;
            box-shadow: 0 2px 8px rgba(8, 20, 38, 0.04);
        }

        .wf-modal-content {
            padding: 28px 32px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        /* Inputs y Formularios Oficiales */
        .wf-card {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 10px;
            padding: 28px 32px;
            box-shadow: 0 4px 14px rgba(8, 20, 38, 0.04);
        }

        .wf-label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            color: #475569;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 9px;
            margin-top: 4px;
        }

        .wf-input {
            width: 100%;
            height: 44px;
            padding: 10px 16px;
            border: 1.5px solid #CBD5E1;
            border-radius: 8px;
            font-size: 13px;
            color: #081426;
            outline: none;
            background: #FFFFFF;
            transition: all 0.15s;
        }

        .wf-input:focus {
            border-color: #081426;
            box-shadow: 0 0 0 3px rgba(8, 20, 38, 0.08);
        }

        textarea.wf-input {
            height: auto;
            min-height: 90px;
            line-height: 1.5;
        }

        .bg-disabled {
            background: #F8FAFC !important;
            color: #64748B !important;
            border-style: dashed !important;
        }

        .wf-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 42px;
            padding: 0 20px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
            cursor: pointer;
            border: 1.5px solid transparent;
            text-decoration: none;
            white-space: nowrap;
            transition: all 0.15s;
            gap: 6px;
        }

        .wf-btn-primary {
            background: #081426;
            color: #FFFFFF;
            border-color: #081426;
        }

        .wf-btn-primary:hover {
            background: #102540;
        }

        .wf-btn-outline {
            background: #FFFFFF;
            border-color: #CBD5E1;
            color: #334155;
        }

        .wf-btn-outline:hover {
            background: #F8FAFC;
            border-color: #081426;
            color: #081426;
        }

        .wf-btn-danger {
            background: #DC2626;
            border-color: #DC2626;
            color: #FFFFFF;
        }

        .wf-btn-sm {
            height: 34px;
            padding: 0 14px;
            font-size: 11px;
            border-radius: 6px;
        }

        .wf-link {
            font-size: 13px;
            color: #081426;
            font-weight: 700;
            text-decoration: underline;
            text-underline-offset: 2px;
            cursor: pointer;
        }

        /* Moodle Style Activities & Resources */
        .moodle-activity-card {
            background: #FFFFFF;
            border: 1px solid #E2E8F0;
            border-radius: 8px;
            padding: 14px 18px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            transition: all 0.15s ease;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
            text-decoration: none;
            color: inherit;
        }

        .moodle-activity-card:hover {
            border-color: var(--wf-gold);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(8, 20, 38, 0.06);
        }

        .moodle-activity-left {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .moodle-icon-box {
            width: 42px;
            height: 42px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            flex-shrink: 0;
        }

        .moodle-icon-pdf { background: #FEE2E2; color: #DC2626; }
        .moodle-icon-video { background: #DBEAFE; color: #2563EB; }
        .moodle-icon-quiz { background: #DCFCE7; color: #16A34A; }
        .moodle-icon-forum { background: #FFEDD5; color: #EA580C; }
        .moodle-icon-glossary { background: #FEF3C7; color: #D97706; }
        .moodle-icon-live { background: #FCE7F3; color: #DB2777; }

        .moodle-activity-title {
            font-size: 13px;
            font-weight: 700;
            color: #081426;
            line-height: 1.3;
        }

        .moodle-activity-desc {
            font-size: 11px;
            color: #64748B;
            margin-top: 2px;
        }

        .moodle-btn-check {
            border: 1px solid #CBD5E1;
            background: #F8FAFC;
            color: #475569;
            font-size: 11px;
            font-weight: 600;
            padding: 6px 14px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
            transition: all 0.15s;
        }

        .moodle-btn-check:hover {
            background: #E2E8F0;
            color: #081426;
        }

        .moodle-btn-check.completed {
            background: #DCFCE7;
            border-color: #86EFAC;
            color: #166534;
        }

        /* Tablas */
        .wf-table-wrap {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
        }

        .wf-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
        }

        .wf-table th, .wf-table td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--wf-border);
            text-align: left;
            vertical-align: middle;
        }

        .wf-table th {
            background: #F8FAFC;
            font-weight: 700;
            color: #475569;
            font-size: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* Badges de Estado */
        .wf-badge {
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 4px;
            border: 1px solid #CBD5E1;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }

        .status-active {
            background: #FEF3C7;
            color: #92400E;
            border-color: #FCD34D;
        }

        .status-inactive {
            background: #FFFFFF;
            color: #94A3B8;
            border-color: #E2E8F0;
        }

        .wf-icon-danger {
            width: 48px;
            height: 48px;
            background: #FEE2E2;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto;
        }

        /* Meta-Strip Superior */
        .meta-strip {
            padding: 8px 16px;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #64748B;
        }

        .meta-strip strong {
            color: #081426;
        }

        .meta-strip code {
            background: #F1F5F9;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            color: #081426;
            border: 1px solid #E2E8F0;
        }

        /* Dropdown Flotante */
        .wf-user-menu-wrapper {
            position: relative;
        }

        .wf-user-floating-dropdown {
            position: absolute;
            top: 44px;
            right: 0;
            width: 250px;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
            z-index: 200;
            overflow: hidden;
            display: none;
            flex-direction: column;
        }

        .wf-dropdown-user-header {
            padding: 12px 14px;
            background: #F8FAFC;
            border-bottom: 1px solid #CBD5E1;
        }

        .wf-dropdown-user-name {
            font-size: 12px;
            font-weight: 700;
            color: #081426;
        }

        .wf-dropdown-user-role {
            font-size: 10px;
            font-weight: 700;
            color: var(--wf-gold);
            text-transform: uppercase;
        }

        .wf-dropdown-user-email {
            font-size: 11px;
            color: #64748B;
            margin-top: 2px;
        }

        .wf-dropdown-section {
            padding: 6px 8px;
            border-bottom: 1px solid #F1F5F9;
        }

        .wf-dropdown-section:last-child {
            border-bottom: none;
        }

        .wf-dropdown-section-title {
            font-size: 9px;
            font-weight: 700;
            color: #94A3B8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 2px;
            padding-left: 6px;
        }

        .wf-dropdown-item-btn {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 7px 10px;
            font-size: 12px;
            color: #334155;
            border-radius: 4px;
            text-decoration: none;
            transition: all 0.1s;
        }

        .wf-dropdown-item-btn:hover {
            background: #F1F5F9;
            color: #081426;
        }

        .wf-dropdown-item-btn.text-danger {
            color: #DC2626;
            font-weight: 600;
        }

        .wf-dropdown-editing-toggle-box {
            background: #F8FAFC;
            border: 1px solid #CBD5E1;
            border-radius: 4px;
            padding: 6px 10px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .wf-tab-btn {
            padding: 8px 14px;
            font-size: 12px;
            font-weight: 600;
            color: #64748B;
            background: transparent;
            border: none;
            border-bottom: 2px solid transparent;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
        }

        .wf-tab-btn.active {
            color: #081426;
            border-bottom-color: var(--wf-gold);
            font-weight: 800;
        }

        .wf-unit-box {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
        }

        .wf-unit-header {
            padding: 14px 18px;
            background: #F8FAFC;
            border-bottom: 1px solid var(--wf-border);
        }

        .wf-unit-body {
            padding: 18px;
        }

        .wf-content-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .wf-subcontent-title {
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
            color: #64748B;
            margin-bottom: 6px;
        }

        .wf-input-wrap {
            display: flex;
            align-items: center;
            gap: 8px;
            width: 100%;
        }

        .wf-input-wrap input, .wf-input-wrap textarea, .wf-input-wrap .wf-select-trigger, .wf-input-wrap select {
            flex: 1;
        }

        .wf-select-container {
            position: relative;
            width: 100%;
        }

        .wf-select-trigger {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #FFFFFF;
            cursor: pointer;
        }

        .wf-dropdown-menu {
            position: absolute;
            top: 44px;
            left: 0;
            width: 100%;
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            z-index: 50;
            overflow: hidden;
        }

        .wf-dropdown-item {
            padding: 8px 12px;
            font-size: 12px;
            color: #334155;
            border-bottom: 1px solid #F1F5F9;
            cursor: pointer;
        }

        .wf-dropdown-item.active {
            background: #FEF3C7;
            font-weight: 700;
            color: #92400E;
        }

        /* Helper Grid Classes (Bootstrap-like) */
        .row { display: flex; flex-wrap: wrap; margin-right: -12px; margin-left: -12px; }
        .g-0 { margin-right: 0; margin-left: 0; }
        .g-0 > [class*="col-"] { padding-right: 0; padding-left: 0; }
        .g-2 { margin-right: -6px; margin-left: -6px; }
        .g-2 > [class*="col-"] { padding-right: 6px; padding-left: 6px; padding-bottom: 12px; }
        .g-3 { margin-right: -10px; margin-left: -10px; }
        .g-3 > [class*="col-"] { padding-right: 10px; padding-left: 10px; padding-bottom: 18px; }
        .g-4 { margin-right: -14px; margin-left: -14px; }
        .g-4 > [class*="col-"] { padding-right: 14px; padding-left: 14px; padding-bottom: 24px; }
        
        .col-12 { flex: 0 0 100%; max-width: 100%; padding: 0 12px; }
        .col { flex: 1 0 0%; min-width: 0; padding: 0 12px; }
        .col-auto { flex: 0 0 auto; width: auto; }
        .col-md-3 { flex: 0 0 25%; max-width: 25%; padding: 0 12px; }
        .col-md-4 { flex: 0 0 33.3333%; max-width: 33.3333%; padding: 0 12px; }
        .col-md-5 { flex: 0 0 41.6666%; max-width: 41.6666%; padding: 0 12px; }
        .col-md-6 { flex: 0 0 50%; max-width: 50%; padding: 0 12px; }
        .col-md-7 { flex: 0 0 58.3333%; max-width: 58.3333%; padding: 0 12px; }
        .col-md-8 { flex: 0 0 66.6666%; max-width: 66.6666%; padding: 0 12px; }

        .col-lg-auto { flex: 0 0 auto; width: auto; }
        .col-lg-5 { flex: 0 0 41.6666%; max-width: 41.6666%; padding: 0 12px; }
        .col-lg-6 { flex: 0 0 50%; max-width: 50%; padding: 0 12px; }
        .col-lg-7 { flex: 0 0 58.3333%; max-width: 58.3333%; padding: 0 12px; }
        .pe-lg-4 { padding-right: 20px; }
        .ps-lg-4 { padding-left: 20px; }
        .position-relative { position: relative; }
        .position-absolute { position: absolute; }

        .d-flex { display: flex; }
        .d-inline-flex { display: inline-flex; }
        .flex-column { flex-direction: column; }
        .align-items-center { align-items: center; }
        .align-items-start { align-items: flex-start; }
        .align-items-end { align-items: flex-end; }
        .justify-content-between { justify-content: space-between; }
        .justify-content-end { justify-content: flex-end; }
        .justify-content-center { justify-content: center; }
        
        .gap-1 { gap: 4px; }
        .gap-2 { gap: 8px; }
        .gap-3 { gap: 12px; }
        .gap-4 { gap: 16px; }

        .mb-1 { margin-bottom: 4px; }
        .mb-2 { margin-bottom: 8px; }
        .mb-3 { margin-bottom: 12px; }
        .mb-4 { margin-bottom: 16px; }
        .mt-1 { margin-top: 4px; }
        .mt-2 { margin-top: 8px; }
        .mt-3 { margin-top: 12px; }
        .mt-4 { margin-top: 16px; }
        .me-1 { margin-right: 4px; }
        .me-2 { margin-right: 8px; }
        .ms-1 { margin-left: 4px; }
        .ms-2 { margin-left: 8px; }
        
        .p-2 { padding: 8px; }
        .p-3 { padding: 12px; }
        .p-4 { padding: 16px; }
        .px-3 { padding-left: 12px; padding-right: 12px; }
        .py-2 { padding-top: 8px; padding-bottom: 8px; }
        .pt-2 { padding-top: 8px; }
        .pt-3 { padding-top: 12px; }
        .pt-4 { padding-top: 16px; }
        .pb-1 { padding-bottom: 4px; }
        .pb-2 { padding-bottom: 8px; }
        .pb-3 { padding-bottom: 12px; }
        
        .flex-row { flex-direction: row !important; }
        .flex-column { flex-direction: column !important; }
        .flex-wrap { flex-wrap: wrap !important; }
        .flex-nowrap { flex-wrap: nowrap !important; }
        .flex-grow-1 { flex-grow: 1 !important; }
        .flex-shrink-0 { flex-shrink: 0 !important; }

        @media (min-width: 900px) {
            .flex-lg-row { flex-direction: row !important; }
            .flex-lg-column { flex-direction: column !important; }
            .d-lg-block { display: block !important; }
            .d-lg-none { display: none !important; }
            .d-lg-flex { display: flex !important; }
        }

        .border-top { border-top: 1px solid #E2E8F0; }
        .border-bottom { border-bottom: 1px solid #E2E8F0; }
        .border-end { border-right: 1px solid #E2E8F0; }
        .border { border: 1px solid #E2E8F0; }
        .rounded { border-radius: 6px; }
        .w-100 { width: 100%; }
        .h-100 { height: 100%; }
        .text-muted { color: #64748B; }
        .text-danger { color: #DC2626; }
        .text-success { color: #16A34A; }
        .text-primary { color: #2563EB; }
        .text-warning { color: #D97706; }
        .text-info { color: #0284C7; }
        .text-end { text-align: right; }
        .text-center { text-align: center; }
        .fw-bold { font-weight: 700; }
        .small { font-size: 11px; }
        .cursor-pointer { cursor: pointer; }
        .bg-white { background: #FFFFFF; }
        .bg-light { background: #F8FAFC; }
        .shadow-sm { box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
    </style>
</head>
<body>
    <div id="nav-sidebar">
        <div class="nav-header">
            <h2>IDÓNEOS ONLINE</h2>
            <p>${cus.length} Wireframes de Casos de Uso</p>
        </div>
        <div class="nav-search">
            <input type="text" id="searchInput" placeholder="Buscar CU o nombre..." oninput="filterCUs()">
        </div>
        <div id="moduleList">
`;

modules.forEach(m => {
  html += `
            <div class="module-group">
                <div class="module-header">${m.name}</div>
                <ul class="module-list">
  `;
  m.cus.forEach(c => {
    html += `
                    <li class="nav-item">
                        <a href="#${c.id}">
                            <span>${c.name}</span>
                            <span class="cu-tag">${c.id}</span>
                        </a>
                    </li>
    `;
  });
  html += `
                </ul>
            </div>
  `;
});

html += `
        </div>
    </div>

    <div id="viewport">
`;

let figNumber = 1;
cus.forEach(cu => {
  const roleInfo = getRoleInfo(cu.actors, cu.id);
  const dssMessage = dssMap[cu.id] || 'Operación de sistema correspondiente';

  html += `
        <div class="figure-wrapper" id="${cu.id}">
            <!-- Franja Superior de Metadatos y Trazabilidad Formal -->
            <div class="meta-strip">
                <div class="d-flex align-items-center gap-2">
                    <span class="wf-badge" style="background: #081426; color: var(--wf-gold); border-color: var(--wf-gold); font-size: 11px;">${cu.id}</span>
                    <strong style="font-size: 13px; color: #081426;">${cu.name}</strong>
                    <span style="color: #94A3B8;">|</span>
                    <span class="text-muted" style="font-size: 11px;"><strong>Actor(es):</strong> ${cu.actors}</span>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="text-muted" style="font-size: 11px; font-weight: 700;">DSS:</span>
                    <code>${dssMessage}</code>
                </div>
            </div>

            <div class="screen-frame">
                <!-- Top Navbar Oficial -->
                <div class="wf-top-navbar">
                    <div class="wf-brand">
                        <div class="d-flex flex-column">
                            <div class="wf-brand-logo"><i class="fa-solid fa-chart-line" style="color: var(--wf-gold);"></i> Idóneos <span>Online</span></div>
                            <div class="wf-brand-sub">FINANZAS • ECONOMÍA • MERCADO DE CAPITALES</div>
                        </div>
                    </div>

                    <div class="wf-top-nav-links">
                        <a href="#CU-06" class="wf-top-nav-link">Inicio</a>
                        <a href="#CU-06" class="wf-top-nav-link">Catálogo de Cursos</a>
                        <span class="wf-top-nav-pill">${roleInfo.isAdmin ? 'Panel Administrador' : (roleInfo.isDocente ? 'Panel Docente' : 'Panel Alumno')}</span>
                        <a href="#CU-99" class="wf-top-nav-link">Acerca de</a>

                        <div class="wf-user-menu-wrapper">
                            <div class="wf-user-trigger-pill" onclick="toggleUserDropdown(this)">
                                <div class="user-avatar-circle">${roleInfo.initials}</div>
                                <span style="font-size: 11px; font-weight: 700;">${roleInfo.name}</span>
                                ${icons.chevronDown("w-3 h-3 text-white ms-1")}
                            </div>

                            <div class="wf-user-floating-dropdown">
                                <div class="wf-dropdown-user-header">
                                    <div class="wf-dropdown-user-name">${roleInfo.name}</div>
                                    <div class="wf-dropdown-user-role">${roleInfo.role}</div>
                                    <div class="wf-dropdown-user-email">${roleInfo.email}</div>
                                </div>
  `;

  roleInfo.dropdownSections.forEach(sec => {
    html += `
                                <div class="wf-dropdown-section">
                                    <div class="wf-dropdown-section-title">${sec.title}</div>
    `;
    if (sec.hasEditingToggle) {
      html += `
                                    <div class="wf-dropdown-editing-toggle-box">
                                        <span class="small fw-bold">${sec.isEditingActive ? 'Modo Edición Activado' : 'Activar Edición'}</span>
                                        <span class="wf-badge ${sec.isEditingActive ? 'status-active' : 'status-inactive'}">${sec.isEditingActive ? 'ON' : 'OFF'}</span>
                                    </div>
      `;
    }
    if (sec.items) {
      sec.items.forEach(it => {
        html += `
                                    <a href="#${it.cu}" class="wf-dropdown-item-btn ${it.isDanger ? 'text-danger' : ''}">
                                        <span>${it.label}</span>
                                    </a>
        `;
      });
    }
    html += `
                                </div>
    `;
  });

  html += `
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Hero Banner Contextual Superior (Navy) -->
                <div class="wf-hero-banner">
                    <div>
                        <div class="wf-hero-tag">
                            <i class="fa-solid fa-graduation-cap" style="color: var(--wf-gold);"></i>
                            <span>${roleInfo.isAdmin ? 'PANEL DE ADMINISTRACIÓN' : (roleInfo.isDocente ? 'CUERPO DOCENTE • IDÓNEOS ONLINE' : 'PORTAL DEL ALUMNO')}</span>
                        </div>
                        <h2 class="wf-hero-title">${cu.id === 'CU-01' ? 'Gestión de Cursos' : (cu.id === 'CU-02' ? 'Mis Cursos Asignados' : cu.name)}</h2>
                        <p class="wf-hero-desc">Bienvenido/a, ${roleInfo.name}</p>
                    </div>
                    
                    <div class="d-flex align-items-center gap-2">
                    ${cu.id === 'CU-01' ? `
                        <a href="#CU-03" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Curso</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-07' ? `
                        <a href="#CU-08" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Categoría</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-11' ? `
                        <a href="#CU-12" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Cohorte</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-15' ? `
                        <a href="#CU-16" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Programa</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-19' || cu.id === 'CU-26b' ? `
                        <a href="#CU-20" class="wf-btn-gold" style="padding: 6px 14px; font-size: 11px; height: 32px;">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Unidad</span>
                        </a>
                        <span class="pin-badge">A</span>
                        <a href="#CU-54" class="wf-btn-gold" style="padding: 6px 14px; font-size: 11px; height: 32px;">
                            <i class="fa-solid fa-list-check"></i>
                            <span>Nuevo Pool</span>
                        </a>
                        <a href="#CU-66" class="wf-btn-gold" style="padding: 6px 14px; font-size: 11px; height: 32px;">
                            <i class="fa-solid fa-video"></i>
                            <span>Programar clase en vivo</span>
                        </a>
                    ` : ''}
                    ${cu.id === 'CU-23' ? `
                        <a href="#CU-24" class="wf-btn-gold">
                            <i class="fa-solid fa-arrow-down-short-wide"></i>
                            <span>Reordenar Cronograma</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-27' ? `
                        <a href="#CU-28" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Material</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-31' ? `
                        <a href="#CU-32" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Término</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-35' ? `
                        <a href="#CU-36" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Consulta</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-57' ? `
                        <a href="#CU-58" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nueva Autoevaluación</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-49' ? `
                        <a href="#CU-50" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Descuento</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    ${cu.id === 'CU-82' ? `
                        <a href="#CU-83" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Nuevo Usuario</span>
                        </a>
                        <span class="pin-badge">A</span>
                    ` : ''}
                    </div>
</div>

                <div class="wf-body">
                    <div class="wf-main-content">
                        ${generateScreenContent(cu)}
                    </div>
                </div>

                <!-- Footer Oficial -->
                <div class="wf-screen-footer">
                    <div>
                        <strong>Idóneos <span>Online</span> S.A.S.</strong> • Plataforma de Educación Financiera, Economía & Mercado de Capitales
                    </div>
                    <div>
                        FCEQyN — UNaM • Proyecto Software (LSI) / Trabajo Final (ASI)
                    </div>
                </div>
            </div>

            <!-- Epígrafe Formal de Tesis / Wireframe -->
            <div class="figure-caption" style="margin-top: 4px; text-align: center; color: #64748B;">
                <strong>Figura ${figNumber++}.</strong> Prototipo UI / Wireframe para <em>${cu.name}</em> (${cu.id}).
            </div>
        </div>
  `;
});

html += `
    </div>

    <script>
        function filterCUs() {
            const query = document.getElementById('searchInput').value.toLowerCase();
            const groups = document.querySelectorAll('.module-group');
            
            groups.forEach(group => {
                const items = group.querySelectorAll('.nav-item');
                let hasVisible = false;
                
                items.forEach(item => {
                    const text = item.textContent.toLowerCase();
                    if (text.includes(query)) {
                        item.style.display = 'block';
                        hasVisible = true;
                    } else {
                        item.style.display = 'none';
                    }
                });
                
                group.style.display = hasVisible ? 'block' : 'none';
            });
        }

        function toggleUserDropdown(triggerEl) {
            const wrapper = triggerEl.closest('.wf-user-menu-wrapper');
            if (wrapper) {
                const dropdown = wrapper.querySelector('.wf-user-floating-dropdown');
                if (dropdown) {
                    if (dropdown.style.display === 'none') {
                        dropdown.style.display = 'flex';
                    } else {
                        dropdown.style.display = 'none';
                    }
                }
            }
        }
    </script>
</body>
</html>
`;

// 7. Write output file
const outputPath = path.join(__dirname, 'docs', 'diseño', 'Pantallas_CU_Reales.html');
fs.writeFileSync(outputPath, html, 'utf8');

console.log(`Successfully generated specialized wireframes in ${outputPath}`);
