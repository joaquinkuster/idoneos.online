const fs = require('fs');
const path = require('path');

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
      dssMap[cuId] = msgs.slice(0, 3).join(' ➔ ');
    }
  }
}
dssMap['CU-26b'] = 'verContenidoUnidad(unaUnidad) ➔ ModoEdición';

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
            { label: 'Ver mi perfil', cu: 'CU-86' },
            { label: 'Editar datos de perfil', cu: 'CU-87' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  } else if (a.includes('docente') && !a.includes('administrador')) {
    return {
      role: 'Docente Titular',
      name: 'Lic. Fausto Spotorno',
      email: 'fausto.spotorno@idoneos.online',
      initials: 'FS',
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
            { label: 'Mi perfil docente', cu: 'CU-86' },
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
            { label: 'Perfil de administrador', cu: 'CU-86' },
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

  // --- TYPE 1: CARDS VIEW (Mis Cursos / Catálogo) --- CU-01, CU-02, CU-06
  if (['CU-01', 'CU-02', 'CU-06'].includes(id)) {
    const isDocente = id === 'CU-01';
    const isAlumno = id === 'CU-02';
    const isCatalog = id === 'CU-06';

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
            <div class="d-flex align-items-center justify-content-between gap-2">
              <button class="wf-btn-manage-course">
                ${icons.cog6Tooth("w-4 h-4")}
                <span>${id === 'CU-06' ? 'Ver Ficha / Inscribirme' : (isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle'))}</span>
              </button>
              <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
            </div>
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
            <button class="wf-btn-manage-course">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>${isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle')}</span>
            </button>
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
            <button class="wf-btn-manage-course">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>${isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle')}</span>
            </button>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 2: VISTA DEL CURSO ESTILO MOODLE (ALUMNO) --- CU-26, CU-62
  if (id === 'CU-26') {
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-start pb-3 border-bottom">
          <div>
            <div class="small text-muted mb-1" style="font-size: 11px;">
              <span>Mis cursos</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <span>Mercado de Capitales</span> <i class="fa-solid fa-chevron-right" style="font-size: 9px; margin: 0 4px;"></i> <strong>Especialización en Idoneidad Bursátil</strong>
            </div>
            <h3 style="font-size: 19px; font-weight: 800; color: #081426; margin: 4px 0;">Especialización en Idoneidad Bursátil (Cohorte 2026-1)</h3>
            <p class="small text-muted" style="margin: 0;">Docente Titular: Lic. Fausto Spotorno | Duración: 8 Semanas | Programa Vigente</p>
          </div>
          <div class="text-end">
            <span class="wf-badge status-active">Inscripción Vigente</span>
            <div class="small text-muted mt-1">Progreso general: <strong>65%</strong></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-2">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span> Curso</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[4] || 'E'}</span> Participantes</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[5] || 'F'}</span> Calificaciones</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Cronograma</button>
            <button class="wf-tab-btn">Insignias</button>
            <button class="wf-tab-btn">Competencias</button>
          </div>
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
                  <div class="moodle-activity-desc">Video clase dictada por Lic. Fausto Spotorno • 45 min</div>
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
                  <button class="moodle-btn-check">Iniciar intento</button>
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
                <button class="moodle-btn-check">Ver debates</button>
              </div>

              <!-- Clase en Vivo -->
              <div class="moodle-activity-card">
                <div class="moodle-activity-left">
                  <div class="moodle-icon-box moodle-icon-live">
                    <i class="fa-solid fa-video"></i>
                  </div>
                  <div>
                    <a href="#CU-72" class="moodle-activity-title wf-link">Clase en Vivo: Streaming Interactivo con OBS</a>
                    <div class="moodle-activity-desc">Transmisión sincrónica con sala de chat • Jueves 19:00 hs</div>
                  </div>
                </div>
                <button class="moodle-btn-check" style="background: #FEF3C7; color: #92400E; border-color: #FCD34D;"><i class="fa-solid fa-broadcast-tower"></i> Sala en Vivo</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 3: MODO EDICIÓN DEL CURSO --- CU-26b, CU-19, CU-20, CU-21
  if (['CU-26b', 'CU-19', 'CU-20', 'CU-21'].includes(id)) {
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
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
            <div class="text-muted cursor-pointer" title="Ajustes del curso"><i class="fa-solid fa-gear" style="font-size: 18px;"></i></div>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-2">
          <div class="d-flex align-items-center gap-1">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Curso & Unidades</button>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
              <i class="fa-solid fa-plus"></i>
              <span>Añadir secciones</span>
            </button>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <div class="d-flex flex-column gap-4">
        <!-- Unidad 1 en Modo Edición -->
        <div class="wf-unit-box">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
              <strong style="font-size: 14px; color: #081426;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
              <button class="wf-btn wf-btn-sm wf-btn-outline" style="padding: 2px 6px; height: 24px;"><i class="fa-solid fa-pen-to-square"></i></button>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></button>
            </div>
          </div>
          <div class="wf-unit-body d-flex flex-column gap-2">
            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-pdf">
                  <i class="fa-solid fa-file-pdf"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Ley 26.831 y modificatorias (PDF - 2.4 MB)</span>
                  <div class="moodle-activity-desc">Documento adjunto</div>
                </div>
              </div>
              <button class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></button>
            </div>

            <div class="moodle-activity-card">
              <div class="moodle-activity-left">
                <i class="fa-solid fa-bars text-muted" style="cursor: grab;"></i>
                <div class="moodle-icon-box moodle-icon-video">
                  <i class="fa-solid fa-circle-play"></i>
                </div>
                <div>
                  <span class="moodle-activity-title">Grabación: Estructura del Mercado Argentino (45 min)</span>
                  <div class="moodle-activity-desc">Video lección</div>
                </div>
              </div>
              <button class="wf-btn wf-btn-sm wf-btn-outline">Editar <i class="fa-solid fa-chevron-down ms-1" style="font-size: 10px;"></i></button>
            </div>

            <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
              <a href="#CU-28" class="wf-link d-flex align-items-center gap-2" style="font-size: 12px; font-weight: 700; color: #081426;">
                <i class="fa-solid fa-plus-circle" style="color: var(--wf-gold); font-size: 16px;"></i>
                <span>Añade una actividad o un recurso</span>
              </a>
              <span class="pin-badge">${badges[3] || badges[4] || 'D'}</span>
            </div>
          </div>
        </div>
      </div>

      ${id === 'CU-21' ? `
      <div class="wf-card mt-4" style="background: #FFFFFF; max-width: 760px;">
        <div class="d-flex align-items-center gap-2 mb-3">
          <h4 style="font-size: 15px; font-weight: 800; color: #081426; margin: 0;"><i class="fa-solid fa-pen-to-square text-muted me-2"></i>Editar Unidad Seleccionada</h4>
          <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acci\u00f3n: <strong>Editar</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
        </div>
        <div class="mb-3">
          <label class="wf-label">T\u00edtulo de la Unidad</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>
        <div class="mb-4">
          <label class="wf-label">Descripci\u00f3n / Contenido de la Unidad</label>
          <textarea class="wf-input" rows="4">Esta unidad aborda el marco normativo vigente para el mercado de capitales argentino...</textarea>
        </div>
        <div class="d-flex justify-content-end align-items-center gap-2 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary"><i class="fa-solid fa-check me-1"></i> Guardar Cambios</button>
            <span class="pin-badge">${badges[2] || 'C'}</span>
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
          <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1 mb-2"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>Quitar de este programa</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
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
    `;
  }

  // --- SPECIALIZED 3D: MODIFICAR CRONOGRAMA --- CU-24
  if (id === 'CU-24') {
    return `
      <div class="wf-card" style="max-width: 960px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div>
              <div class="d-flex align-items-center gap-2">
                <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Reordenar y Modificar Cronograma</h3>
                <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>Reordenar Cronograma</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
              </div>
              <p class="small text-muted" style="margin: 3px 0 0;">Arrastre los bloques de unidades para modificar su orden secuencial y ajuste las semanas lectivas.</p>
            </div>
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
    `;
  }

  // --- TYPE 4: LISTADOS CONTEXTUALES DE RECURSOS --- CU-27, CU-31, CU-35, CU-53, CU-57, CU-65, CU-25
  if (['CU-27', 'CU-31', 'CU-35', 'CU-53', 'CU-57', 'CU-65', 'CU-25'].includes(id)) {
    const isMaterial = id === 'CU-27';
    const isGlosario = id === 'CU-31';
    const isForo = id === 'CU-35';
    const isPool = id === 'CU-53';
    const isEval = id === 'CU-57';
    const isLive = id === 'CU-65';

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
            <a href="#CU-27" class="wf-tab-btn ${isMaterial ? 'active' : ''}">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn ${isGlosario ? 'active' : ''}">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn ${isEval ? 'active' : ''}">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn ${isPool ? 'active' : ''}">Pools</a>
            <a href="#CU-35" class="wf-tab-btn ${isForo ? 'active' : ''}">Foros</a>
            <a href="#CU-65" class="wf-tab-btn ${isLive ? 'active' : ''}">Clases en Vivo</a>
          </div>
          <div class="d-flex align-items-center gap-2">
            <a href="#${isMaterial ? 'CU-28' : (isGlosario ? 'CU-32' : (isEval ? 'CU-58' : (isPool ? 'CU-54' : 'CU-66')))}" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1">
              <i class="fa-solid fa-plus"></i>
              <span>Nuevo ${isMaterial ? 'Material' : (isGlosario ? 'Término' : (isEval ? 'Autoevaluación' : (isPool ? 'Pool' : 'Elemento')))}</span>
            </a>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <!-- Barra de Filtros y Búsqueda -->
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar en ${isMaterial ? 'materiales' : (isGlosario ? 'glosario' : (isForo ? 'consultas' : (isPool ? 'pools' : 'evaluaciones')))}...">
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
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>${isMaterial ? 'Ley 26.831 de Mercado de Capitales' : (isGlosario ? 'TIR (Tasa Interna de Retorno)' : (isEval ? 'Autoevaluación Unidad 1' : (isPool ? 'Pool Unidad 1' : 'Clase Streaming')))}</strong></td>
              <td>${isMaterial ? 'Documento PDF' : (isGlosario ? 'Definición' : (isEval ? '10 preguntas' : '25 preguntas'))}</td>
              <td>Unidad 1: Marco Regulatorio</td>
              <td><span class="wf-badge status-active">Publicado</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <a href="#${isMaterial ? 'CU-29' : (isGlosario ? 'CU-33' : (isEval ? 'CU-59' : (isPool ? 'CU-55' : 'CU-67')))}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
                  <a href="#${isMaterial ? 'CU-30' : (isGlosario ? 'CU-34' : (isEval ? 'CU-60' : (isPool ? 'CU-56' : 'CU-69')))}" class="wf-btn wf-btn-sm wf-btn-outline text-danger"><i class="fa-solid fa-trash"></i></a>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td><strong>${isMaterial ? 'Guía Teórica de Renta Fija v2.1' : (isGlosario ? 'Duration Modificada' : (isEval ? 'Autoevaluación Unidad 2' : (isPool ? 'Pool Unidad 2' : 'Clase en Vivo')))}</strong></td>
              <td>${isMaterial ? 'Documento PDF' : (isGlosario ? 'Definición' : (isEval ? '10 preguntas' : '30 preguntas'))}</td>
              <td>Unidad 2: Renta Fija</td>
              <td><span class="wf-badge status-active">Publicado</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></button>
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger"><i class="fa-solid fa-trash"></i></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
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

  // --- TYPE 5: MODAL DE AGREGAR CONTENIDO --- CU-28, CU-32, CU-36, CU-54, CU-58, CU-66
  if (['CU-28', 'CU-32', 'CU-36', 'CU-54', 'CU-58', 'CU-66'].includes(id)) {
    const isMaterial = id === 'CU-28';
    const isGlosario = id === 'CU-32';
    const isForo = id === 'CU-36';
    const isPool = id === 'CU-54';
    const isEval = id === 'CU-58';
    const isLive = id === 'CU-66';

    let selectedTitle = 'Material / Documento PDF';
    let inputLabel = 'Nombre / Título del material';
    let inputValue = 'Guía Teórica de Renta Fija v2.0 (PDF)';

    if (isGlosario) {
      selectedTitle = 'Glosario de Términos';
      inputLabel = 'Término a registrar';
      inputValue = 'TIR (Tasa Interna de Retorno)';
    } else if (isForo) {
      selectedTitle = 'Foro de Consultas';
      inputLabel = 'Asunto del tema o consulta';
      inputValue = 'Consulta sobre Cálculo de TIR';
    } else if (isEval) {
      selectedTitle = 'Autoevaluación';
      inputLabel = 'Nombre de la autoevaluación';
      inputValue = 'Autoevaluación Unidad 1: Marco Legal';
    } else if (isPool) {
      selectedTitle = 'Pool de Preguntas';
      inputLabel = 'Nombre del pool';
      inputValue = 'Pool Unidad 1: Mercado de Capitales';
    } else if (isLive) {
      selectedTitle = 'Clase en Vivo (Streaming)';
      inputLabel = 'Título de la transmisión';
      inputValue = 'Clase en Vivo #1: Resolución de Prácticos';
    }

    let modalTriggerLabel = 'Añade una actividad o un recurso';
    if (id === 'CU-36') modalTriggerLabel = '+ Nueva Consulta';
    if (id === 'CU-40') modalTriggerLabel = 'Responder';

    return `
      <div class="wf-modal-box" style="max-width: 900px; margin: 20px auto;">
        <div class="wf-modal-header">
          <div class="d-flex align-items-center gap-2">
            <h3 class="wf-modal-title">Añade una actividad o un recurso</h3>
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>${modalTriggerLabel}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
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
                <div class="wf-modal-option ${isEval ? 'active' : ''}">
                  <i class="fa-solid fa-clipboard-check text-success"></i>
                  <span>Cuestionario / Autoevaluación</span>
                </div>
                <div class="wf-modal-option ${isPool ? 'active' : ''}">
                  <i class="fa-solid fa-list-check text-info"></i>
                  <span>Pool de Preguntas</span>
                </div>
                <div class="wf-modal-option ${isForo ? 'active' : ''}">
                  <i class="fa-solid fa-comments text-muted"></i>
                  <span>Foro de Consultas</span>
                </div>
                <div class="wf-modal-option ${isLive ? 'active' : ''}">
                  <i class="fa-solid fa-video text-danger"></i>
                  <span>Clase en Vivo (Streaming)</span>
                </div>
              </div>
            </div>
            <div class="pt-3 text-end">
              <span class="pin-badge">${badges[0] || 'A'}</span>
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

              ${isForo ? `
                <div class="mb-4">
                  <label class="wf-label">Redacción de la Consulta / Mensaje</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="4">Estimado docente, tengo una duda respecto a la aplicación de la duración modificada cuando los bonos no tienen cupones periódicos...</textarea>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isEval ? `
                <div class="mb-4">
                  <label class="wf-label">Pool de Preguntas Vinculado</label>
                  <div class="wf-input-wrap">
                    <select class="wf-input">
                      <option>Pool Unidad 1: Marco Regulatorio (25 preguntas)</option>
                      <option>Pool Unidad 2: Renta Fija (30 preguntas)</option>
                    </select>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isPool ? `
                <div class="mb-4">
                  <label class="wf-label">Categoría Temática / Dificultad</label>
                  <div class="wf-input-wrap">
                    <select class="wf-input">
                      <option>Nivel Avanzado (Examen Oficial CNV)</option>
                      <option>Nivel Intermedio</option>
                    </select>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isLive ? `
                <div class="mb-4">
                  <label class="wf-label">Fecha y Hora de Transmisión</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="Jueves 28/08/2026 - 19:00 hs">
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}
            </div>

            <div class="d-flex justify-content-end align-items-center gap-3 pt-4 border-top">
              <button class="wf-btn wf-btn-outline wf-btn-sm">Cancelar</button>
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-primary wf-btn-sm"><i class="fa-solid fa-plus me-1"></i> ${isForo ? 'Publicar Consulta' : 'Agregar'}</button>
                <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 6: ESTUDIO DE CLON DIGITAL CON IA (HEYGEN) --- CU-76
  if (id === 'CU-76') {
    return `
      <div class="wf-card" style="max-width: 1020px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 8px; background: #0F172A; display: flex; align-items: center; justify-content: center; color: white;">
              ${icons.sparkles("w-6 h-6 text-white")}
            </div>
            <div>
              <h3 style="font-size: 17px; font-weight: 800; color: #0F172A; margin: 0;">Estudio de Clon Digital con IA (HeyGen API)</h3>
              <p class="small text-muted" style="margin: 0;">Configure su avatar hiperrealista y clone su voz para el dictado autónomo de clases.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>Configurar Clon de IA</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
            <button class="wf-btn wf-btn-primary wf-btn-sm d-flex align-items-center gap-2">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>Configurar API HeyGen</span>
            </button>
          </div>
        </div>

        <div class="row g-4">
          <!-- Columna 1: Captura Facial / Avatar Preview -->
          <div class="col-md-6">
            <div class="p-3 border rounded bg-white h-100 d-flex flex-column justify-content-between shadow-sm">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h4 style="font-size: 14px; font-weight: 700; color: #0F172A; margin: 0;">1. Captura de Rostro & Avatar</h4>
                  <span class="wf-badge status-active">Cámara HD Activa</span>
                </div>
                
                <!-- Preview del Avatar / Cámara con Tracking -->
                <div style="height: 220px; background: #0F172A; border-radius: 6px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #334155;">
                  <!-- Avatar Wireframe Silhouette -->
                  <div style="text-align: center; color: #F8FAFC; z-index: 2;">
                    <div style="width: 80px; height: 80px; border-radius: 50%; background: #1E293B; margin: 0 auto 8px; display: flex; align-items: center; justify-content: center; border: 2px solid #64748B; box-shadow: 0 4px 12px rgba(0,0,0,0.3);">
                      ${icons.user("w-10 h-10 text-slate-300")}
                    </div>
                    <div style="font-size: 13px; font-weight: 700;">Lic. Fausto Spotorno</div>
                    <div style="font-size: 10px; color: #94A3B8;">Modelo: Instant Avatar v2 • 1080p 60fps</div>
                  </div>

                  <!-- Face Tracking Bounding Box Grid -->
                  <div style="position: absolute; width: 120px; height: 130px; border: 1.5px dashed #38BDF8; border-radius: 8px; pointer-events: none; opacity: 0.6;"></div>
                  
                  <div style="position: absolute; top: 10px; left: 10px; display: flex; gap: 6px;">
                    <span style="background: rgba(15,23,42,0.85); color: #38BDF8; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; border: 1px solid rgba(56,189,248,0.3);">REC 1080P</span>
                    <span style="background: rgba(15,23,42,0.85); color: #34D399; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; border: 1px solid rgba(52,211,153,0.3);">● TRACKING ON</span>
                  </div>

                  <div style="position: absolute; bottom: 8px; right: 8px; font-size: 10px; color: #94A3B8; background: rgba(0,0,0,0.6); padding: 2px 6px; border-radius: 3px;">
                    Iluminación: 98% (Óptima)
                  </div>
                </div>
              </div>

              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="small text-muted">Adjuntar archivo o tomar foto web:</span>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
                    ${icons.camera("w-4 h-4")} <span>Tomar Foto</span>
                  </button>
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Columna 2: Calibración y Muestra de Voz -->
          <div class="col-md-6">
            <div class="p-3 border rounded bg-white h-100 d-flex flex-column justify-content-between shadow-sm">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h4 style="font-size: 14px; font-weight: 700; color: #0F172A; margin: 0;">2. Muestra y Clonación de Voz</h4>
                  <span class="wf-badge status-active">Micrófono 48kHz</span>
                </div>

                <div class="p-3 bg-light rounded border mb-3">
                  <div class="small text-muted mb-1 fw-bold">Texto de Calibración Sugerido:</div>
                  <p class="small text-secondary" style="font-style: italic; line-height: 1.4; margin: 0;">
                    "Bienvenidos a la cátedra de Mercado de Capitales. Hoy analizaremos la curva de rendimiento y la duración modificada de los bonos soberanos."
                  </p>
                </div>

                <!-- Visualizador de Espectro de Audio -->
                <div style="height: 90px; background: #0F172A; border-radius: 6px; display: flex; align-items: center; justify-content: center; gap: 5px; padding: 0 20px; border: 1.5px solid #334155; position: relative;">
                  <div style="width: 4px; height: 24px; background: #64748B; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 42px; background: #94A3B8; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 68px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 38px; background: #94A3B8; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 56px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 28px; background: #64748B; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 48px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 20px; background: #64748B; border-radius: 2px;"></div>
                  
                  <div style="position: absolute; bottom: 6px; left: 10px; font-size: 10px; color: #94A3B8;">
                    Ruido ambiente: -52 dB (Bajo)
                  </div>
                </div>
              </div>

              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="small text-muted">Duración grabada: 00:32 / 00:30 min</span>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger d-flex align-items-center gap-1">
                    ${icons.microphone("w-4 h-4")} <span>Grabar Muestra</span>
                  </button>
                  <span class="pin-badge">${badges[2] || 'C'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Consentimiento y Envío a HeyGen API -->
        <div class="mt-4 p-3 bg-light border rounded d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-2">
            <input type="checkbox" id="terms" checked style="width: 16px; height: 16px;">
            <label for="terms" class="small text-muted mb-0">Acepto los términos y condiciones de consentimiento biométrico para síntesis de voz y avatar en HeyGen API.</label>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2">
              ${icons.sparkles("w-4 h-4")}
              <span>Crear Clon en HeyGen</span>
            </button>
            <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 7A: BUSCAR CLASE CON CLON IA --- CU-77
  if (id === 'CU-77') {
    return `
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
                  <option>Unidad 2: Instrumentos de Renta Fija (Bonos y ONs)</option>
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
    `;
  }

  // --- SPECIALIZED 7B: GENERAR / MODIFICAR CLASE CON CLON IA --- CU-78, CU-79
  if (['CU-78', 'Cu-79'].includes(id) || id === 'CU-79') {
    const isGenerate = id === 'CU-78';
    const triggerLabel78_79 = isGenerate ? 'Generar Video con Avatar Clon' : 'Editar Guión';
    const saveBtnLabel78_79 = isGenerate ? 'Sintetizar Video con IA' : 'Actualizar y Regenerar Video';
    const saveBtnBadge78_79 = isGenerate ? (badges[3] || 'D') : (badges[2] || 'C');
    return `
      <div class="wf-card" style="max-width: 1020px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 8px; background: #081426; display: flex; align-items: center; justify-content: center; color: var(--wf-gold);">
              <i class="fa-solid fa-wand-magic-sparkles" style="font-size: 20px;"></i>
            </div>
            <div>
              <div class="d-flex align-items-center gap-2">
                <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${isGenerate ? 'Generar Clase Audiovisual con Clon IA' : 'Modificar Guión de Clase con Clon IA'}</h3>
                <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> <strong>${triggerLabel78_79}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
              </div>
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
                  <i class="fa-solid fa-wand-magic-sparkles"></i> Autogenerar guión con IA
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
              <span>${saveBtnLabel78_79}</span>
            </button>
            <span class="pin-badge">${saveBtnBadge78_79}</span>
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
    // CU-73: [A]="Generar Banco con IA (Ollama)", [B]=prompt, [C]="Generar Preguntas con IA"
    // CU-74: [A]="Generar Resumen de Unidad con IA", [B]="Crear Resumen Automático"
    // CU-75: [A]="Generar Presentación con IA", [B]="Generar Diapositivas"
    const triggerAction = isBank ? 'Generar Banco con IA (Ollama)' : (isSummary ? 'Generar Resumen de Unidad con IA' : 'Generar Presentación con IA');
    const confirmBtn = isBank ? 'Generar Preguntas con IA' : (isSummary ? 'Crear Resumen Automático' : 'Generar Diapositivas');
    const confirmBtnBadge = isBank ? (badges[2] || 'C') : (badges[1] || 'B');

    return `
      <div class="wf-card" style="max-width: 900px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: linear-gradient(135deg, #059669, #10B981); display: flex; align-items: center; justify-content: center; color: white;">
              ${icons.sparkles("w-6 h-6")}
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">${isBank ? 'Generador de Banco de Preguntas con IA (Ollama)' : (isSummary ? 'Generador de Resumen Académico con IA' : 'Generador de Presentación de Diapositivas')}</h3>
              <p class="small text-muted" style="margin: 0;">Procesamiento automático sobre los materiales teóricos y guías subidas a la unidad.</p>
            </div>
          </div>
          <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>${triggerAction}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <label class="wf-label">Unidad Académica de Origen</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</option>
              </select>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Materiales de Referencia a Procesar</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="guia_teorica_u2.pdf, ley_26831.pdf" disabled>
              ${isBank ? `<span class="pin-badge">${badges[1] || 'B'}</span>` : ''}
            </div>
          </div>
        </div>

        <div class="p-3 border rounded bg-light mb-4">
          <div class="small fw-bold text-muted text-uppercase mb-2">Parámetros de Generación</div>
          <div class="row g-3">
            <div class="col-md-4">
              <label class="small text-muted">Nivel de Profundidad:</label>
              <select class="wf-input wf-btn-sm"><option>Avanzado (Examen CNV)</option><option>Intermedio</option></select>
            </div>
            <div class="col-md-4">
              <label class="small text-muted">${isBank ? 'Cantidad de Preguntas:' : (isSlides ? 'Cantidad de Diapositivas:' : 'Extensión del Resumen:')}</label>
              <input type="text" class="wf-input wf-btn-sm" value="${isBank ? '20 preguntas cerradas' : (isSlides ? '12 diapositivas' : '3 páginas síntesis')}">
            </div>
            <div class="col-md-4">
              <label class="small text-muted">Idioma &amp; Formato:</label>
              <input type="text" class="wf-input wf-btn-sm" value="Español (Rioplatense - Financiero)" disabled>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2" style="background: #059669;">
              ${icons.sparkles("w-4 h-4")}
              <span>${confirmBtn}</span>
            </button>
            <span class="pin-badge">${confirmBtnBadge}</span>
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
              <p class="small text-muted" style="margin: 0; color: #94A3B8;">Docente: Lic. Fausto Spotorno | Alumnos Conectados: 48</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            ${isStart ? `
              <div class="d-flex align-items-center gap-2">
                <span class="pin-badge">${badges[0] || 'A'}</span>
                <button class="wf-btn wf-btn-sm wf-btn-primary" style="background: #2563EB;"><i class="fa-solid fa-tower-broadcast me-1"></i> Transmitir en Vivo (Clave OBS)</button>
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
                <span class="badge bg-dark text-white border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer"></i> Acción: <strong>Ingresar a la Sala en Vivo</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
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
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>Comenzar Intento</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
            <span class="wf-badge status-active">En progreso</span>
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

  // --- SPECIALIZED 11A: BUSCAR INSCRIPCIÓN / HISTORIAL --- CU-43, CU-48
  if (['CU-43', 'CU-48'].includes(id)) {
    const isProg = id === 'CU-48';
    return `
      <div class="wf-card mb-4" style="background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${isProg ? 'Seguimiento y Progreso de Alumnos' : 'Gestión e Historial de Inscripciones'}</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Consulte las matriculaciones vigentes, certificados emitidos y porcentaje de avance pedagógico.</p>
          </div>
          <span class="wf-badge status-active">Total: 1.240 Alumnos</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-5">
            <label class="wf-label">Buscar por Alumno o DNI</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Joaquín Küster, 40.123.456...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Estado de la Inscripción</label>
            <select class="wf-input">
              <option>Todas las inscripciones (Activas, Finalizadas, Bajas)</option>
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
                <th>${isProg ? 'Progreso Pedagógico' : 'Estado / Certificado'}</th>
                <th class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>Joaquín Küster</strong><div class="small text-muted">DNI: 40.123.456 • joaquin@idoneos.online</div></td>
                <td>Especialización en Idoneidad Bursátil (2026-1)</td>
                <td>2026-08-15</td>
                <td>
                  ${isProg ? `
                    <div class="d-flex align-items-center gap-2">
                      <div style="flex: 1; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden;">
                        <div style="width: 65%; height: 100%; background: #2563EB;"></div>
                      </div>
                      <span class="small fw-bold">65%</span>
                    </div>
                  ` : `
                    <span class="wf-badge status-active">Activo (Aprobado)</span>
                  `}
                </td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                    <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-certificate me-1 text-warning"></i> Descargar Certificado</button>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
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
            <p class="small text-muted" style="margin: 3px 0 0;">Liquidaciones de aranceles, cobros por MODO QR, tarjetas y descarga de comprobantes.</p>
          </div>
          <span class="wf-badge status-active">Facturación Vigente</span>
        </div>

        <div class="row g-3 align-items-end mb-4">
          <div class="col-md-5">
            <label class="wf-label">Filtrar por Transacción / Alumno</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="ID Pago, DNI o Alumno...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Estado de Transacción</label>
            <select class="wf-input">
              <option>Acreditados / Aprobados</option>
              <option>Pendientes</option>
              <option>Reembolsados</option>
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
                <th>ID Transacción</th>
                <th>Alumno / Pagador</th>
                <th>Concepto / Curso</th>
                <th>Monto / Medio</th>
                <th class="text-end">Comprobante</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>#PAY-2026-8841</strong><div class="small text-muted">2026-08-25 14:22 hs</div></td>
                <td>Joaquín Küster</td>
                <td>Especialización en Idoneidad Bursátil</td>
                <td><strong style="color: #059669;">$120.000 ARS</strong><div class="small text-muted">MODO QR Interoperable</div></td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                    <button class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-file-invoice-dollar me-1 text-primary"></i> Descargar Comprobante</button>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11C: REALIZAR PAGO Y MATRÍCULA (MODO QR) --- CU-44, CU-47
  if (['CU-44', 'CU-47'].includes(id)) {
    const payTriggerLabel = id === 'CU-44' ? 'Inscribirme Ahora' : 'Proceder al Pago';
    return `
      <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">Pasarela de Pago & Matrícula Online</h3>
            <p class="small text-muted" style="margin: 3px 0 0;">Inscripción oficial al curso: <strong>Especialización en Idoneidad Bursátil (Cohorte 2026-1)</strong></p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>${payTriggerLabel}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
            <span class="wf-badge status-active">Arancel: $120.000 ARS</span>
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
              <div class="small fw-bold text-muted text-uppercase mb-2">Escanee para pagar con MODO o App Bancaria</div>
              <div style="width: 170px; height: 170px; background: #081426; border-radius: 8px; display: flex; align-items: center; justify-content: center; position: relative; padding: 12px;">
                <div style="background: white; width: 100%; height: 100%; border-radius: 4px; display: flex; align-items: center; justify-content: center;">
                  <i class="fa-solid fa-qrcode" style="font-size: 110px; color: #081426;"></i>
                </div>
              </div>
              <div class="small text-muted mt-2">Transacción segura BCRA • Acreditación instantánea</div>
              <div class="mt-2">
                <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
          <a href="#CU-06" class="wf-btn wf-btn-outline">Cancelar</a>
          <button class="wf-btn wf-btn-primary" style="background: #059669;"><i class="fa-solid fa-lock me-1"></i> Confirmar Pago Acreditado</button>
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
                <td>${isAudit ? 'Lic. Fausto Spotorno (Docente)' : '2026-1 (En curso)'}</td>
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
              <input type="email" class="wf-input" value="${isLogin ? 'fausto.spotorno@idoneos.online' : 'usuario@correo.com'}">
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
        <div class="wf-card" style="max-width: 800px; margin: 0 auto;">
          <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-circle" style="width: 52px; height: 52px; font-size: 18px;">FS</div>
              <div>
                <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">Perfil de Usuario: Lic. Fausto Spotorno</h3>
                <p class="small text-muted" style="margin: 0;">Docente Titular • Mercado de Capitales & Finanzas</p>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-outline wf-btn-sm">Editar Perfil</button>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="wf-label">Nombre Completo</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Fausto Spotorno">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
            <div class="col-md-6">
              <label class="wf-label">Correo Electrónico</label>
              <input type="email" class="wf-input" value="fausto.spotorno@idoneos.online" disabled class="bg-disabled">
            </div>
            <div class="col-md-6">
              <label class="wf-label">Estado de la Cuenta</label>
              <input type="text" class="wf-input" value="Activo y Habilitado para Dictado" disabled class="bg-disabled">
            </div>
            <div class="col-md-6">
              <label class="wf-label">Clon Digital (HeyGen)</label>
              <input type="text" class="wf-input" value="Vinculado (#avatar_spotorno_v2)" disabled class="bg-disabled">
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary">Guardar Cambios</button>
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
    // CU-22: "Quitar unidad" → confirm = "Confirmar y Quitar"
    if (name.toLowerCase().includes('quitar') && name.includes('unidad')) confirmBtnLabel = 'Confirmar y Quitar';
    // CU-38, CU-42: "foro/respuesta" → confirm = "Confirmar Eliminación"
    if (name.includes('foro') || name.includes('respuesta')) confirmBtnLabel = 'Confirmar Eliminación';
    // CU-68: "Cancelar clase en vivo" → Confirmar Cancelación
    // CU-69: "Dar de baja clase en vivo" → Confirmar Eliminación
    if (name.includes('vivo') && name.toLowerCase().includes('cancelar')) confirmBtnLabel = 'Confirmar Cancelación';
    if (name.includes('vivo') && !name.toLowerCase().includes('cancelar')) confirmBtnLabel = 'Confirmar Eliminación';
    // CU-80: "clon" → confirm = "Confirmar Eliminación"
    if (name.includes('clon')) confirmBtnLabel = 'Confirmar Eliminación';

    let triggerBtnLabel = 'Eliminar';
    if (name.includes('baja')) triggerBtnLabel = 'Dar de baja';
    if (name.includes('cohorte')) triggerBtnLabel = 'Cancelar Cohorte';
    if (name.includes('foro')) triggerBtnLabel = 'Moderar / Eliminar';
    if (name.includes('respuesta')) triggerBtnLabel = 'Eliminar Respuesta';
    if (name.includes('usuario')) triggerBtnLabel = 'Desactivar Cuenta';
    if (name.includes('intento')) triggerBtnLabel = 'Anular Intento por Fraude';
    if (name.includes('clon')) triggerBtnLabel = 'Eliminar Video';
    if (name.includes('vivo')) triggerBtnLabel = 'Eliminar Registro';
    if (name.includes('pool')) triggerBtnLabel = 'Eliminar Pool';
    if (name.includes('unidad')) triggerBtnLabel = 'Quitar de este programa';
    if (name.toLowerCase().includes('sesión') || name.toLowerCase().includes('sesion')) {
      triggerBtnLabel = 'Cerrar Sesión Remota';
      confirmBtnLabel = 'Confirmar Cierre de Sesión';
    }
    // CU-10 (categoría), CU-30 (material), CU-60 (autoevaluación) - but NOT CU-64 (intento):
    // trigger [A] = "Eliminar", confirm [B] = "Confirmar Eliminación"
    if ((name.includes('categoría') || name.includes('material') || name.includes('autoevaluación')) && !name.includes('intento')) {
      triggerBtnLabel = 'Eliminar';
      confirmBtnLabel = 'Confirmar Eliminación';
    }
    // CU-34 (glosario), CU-52 (descuento):
    // trigger [A] = "Eliminar", confirm [B] = "Confirmar Baja"
    if (name.includes('glosario') || name.includes('descuento')) {
      triggerBtnLabel = 'Eliminar';
      confirmBtnLabel = 'Confirmar Baja';
    }

    return `
      <div class="wf-card" style="max-width: 620px; margin: 30px auto; background: #FFFFFF;">
        <div class="wf-card-header text-center mb-4">
          <div class="wf-icon-danger mb-3" style="width: 54px; height: 54px; border-radius: 50%; background: #FEE2E2; display: flex; align-items: center; justify-content: center; margin: 0 auto;">
            <i class="fa-solid fa-triangle-exclamation" style="font-size: 24px; color: #DC2626;"></i>
          </div>
          <div class="d-flex align-items-center justify-content-center gap-2 mb-1">
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${name}</h3>
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>${triggerBtnLabel}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
          </div>
          <p class="small text-muted" style="margin: 4px 0 0;">¿Está seguro de que desea confirmar la operación sobre el elemento seleccionado?</p>
        </div>

        <div class="p-3 mb-4 bg-light border rounded">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="small text-muted">Elemento seleccionado:</span>
            <div class="d-flex align-items-center gap-2">
              <strong style="font-size: 13px; color: #081426;">Registro #${id.replace('CU-', '')} (${name.replace('Dar de baja ', '').replace('Eliminar ', '').replace('Cancelar ', '')})</strong>
            </div>
          </div>
          <div class="d-flex justify-content-between align-items-center">
            <span class="small text-muted">Estado de la entidad:</span>
            <span class="wf-badge status-active">Activo / Vigente</span>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-danger"><i class="fa-solid fa-trash me-1"></i> ${confirmBtnLabel}</button>
            <span class="pin-badge">${badges[1] || badges[badges.length - 1] || 'B'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 15: BÚSQUEDAS / TABLAS DE GESTIÓN ---
  const isSearch = name.toLowerCase().startsWith('buscar') || name.toLowerCase().startsWith('consultar') || name.toLowerCase().startsWith('ver') || name.toLowerCase().startsWith('explorar');
  if (isSearch) {
    let actionBtnLabel = 'Seleccionar / Ver';
    if (name.includes('curso') || name.includes('catálogo')) actionBtnLabel = 'Ver Ficha / Inscribirme';
    if (name.includes('programa')) actionBtnLabel = 'Editar Programa';
    if (name.includes('cohorte')) actionBtnLabel = 'Editar Cohorte';
    if (name.includes('usuario')) actionBtnLabel = 'Editar Usuario';
    if (name.includes('categoría')) actionBtnLabel = 'Editar';
    if (name.includes('descuento')) actionBtnLabel = 'Editar';
    if (name.includes('parámetro')) actionBtnLabel = 'Editar Valor';
    if (name.includes('intento')) actionBtnLabel = 'Revisar Intento';

    return `
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
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="wf-btn wf-btn-sm wf-btn-outline">${actionBtnLabel}</button>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td><strong>Elemento Secundario #${id.replace('CU-', '')}B</strong></td>
              <td>Valuación & Finanzas Corporativas</td>
              <td>2026-08-20</td>
              <td><span class="wf-badge status-active">Activo</span></td>
              <td class="text-end">
                <button class="wf-btn wf-btn-sm wf-btn-outline">${actionBtnLabel}</button>
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

  return `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto; background: #FFFFFF;">
      <div class="wf-card-header mb-4 pb-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
          <div class="d-flex align-items-center gap-2">
            <h3 style="font-size: 18px; font-weight: 800; color: #081426; margin: 0;">${name}</h3>
            <span class="badge bg-light text-dark border d-inline-flex align-items-center gap-1"><i class="fa-solid fa-arrow-pointer text-muted"></i> Acción: <strong>${triggerBtnLabel}</strong> <span class="pin-badge">${badges[0] || 'A'}</span></span>
          </div>
          <p class="small text-muted" style="margin: 3px 0 0;">Complete los datos correspondientes en el sistema.</p>
        </div>
        <span class="wf-badge status-active">${isMod ? 'Modo Edición' : 'Formulario de Alta'}</span>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre / Denominación</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="${name.includes('curso') ? 'Especialización en Idoneidad Bursátil' : (name.includes('categoría') ? 'Mercado de Capitales' : (name.includes('cohorte') ? 'Cohorte 2026-1' : 'Registro de ' + name))}">
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
          <label class="wf-label">${name.includes('curso') ? 'Portada / Archivo de Imagen' : 'Descripción Académica / Contenido'}</label>
          ${name.includes('curso') ? `
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="portada_curso_idoneos.png">
              <button class="wf-btn wf-btn-outline wf-btn-sm"><i class="fa-solid fa-folder-open me-1"></i> Examinar...</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          ` : `
            <div class="wf-input-wrap">
              <textarea class="wf-input" rows="3">Descripción detallada correspondiente al caso de uso ${id} (${name}).</textarea>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          `}
        </div>

        <div class="col-md-4">
          <label class="wf-label">Precio / Arancel</label>
          <input type="text" class="wf-input" value="$120.000 ARS">
        </div>

        <div class="col-md-4">
          <label class="wf-label">Estado</label>
          <input type="text" class="wf-input bg-disabled" value="Habilitado / Activo" disabled>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Docente Responsable</label>
          <input type="text" class="wf-input bg-disabled" value="Lic. Fausto Spotorno" disabled>
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
        .row { display: flex; flex-wrap: wrap; margin-right: -10px; margin-left: -10px; }
        .g-0 { margin-right: 0; margin-left: 0; }
        .g-0 > [class*="col-"] { padding-right: 0; padding-left: 0; }
        .g-2 { margin-right: -4px; margin-left: -4px; }
        .g-2 > [class*="col-"] { padding-right: 4px; padding-left: 4px; }
        .g-3 { margin-right: -8px; margin-left: -8px; }
        .g-3 > [class*="col-"] { padding-right: 8px; padding-left: 8px; }
        .g-4 { margin-right: -12px; margin-left: -12px; }
        .g-4 > [class*="col-"] { padding-right: 12px; padding-left: 12px; }
        
        .col-12 { flex: 0 0 100%; max-width: 100%; padding: 0 10px; }
        .col-md-3 { flex: 0 0 25%; max-width: 25%; padding: 0 10px; }
        .col-md-4 { flex: 0 0 33.3333%; max-width: 33.3333%; padding: 0 10px; }
        .col-md-5 { flex: 0 0 41.6666%; max-width: 41.6666%; padding: 0 10px; }
        .col-md-6 { flex: 0 0 50%; max-width: 50%; padding: 0 10px; }
        .col-md-7 { flex: 0 0 58.3333%; max-width: 58.3333%; padding: 0 10px; }
        .col-md-8 { flex: 0 0 66.6666%; max-width: 66.6666%; padding: 0 10px; }

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
                        <h2 class="wf-hero-title">${cu.id === 'CU-01' ? 'Mis Cursos Asignados' : (cu.id === 'CU-02' ? 'Gestión de Cursos' : cu.name)}</h2>
                        <p class="wf-hero-desc">Bienvenido/a, ${roleInfo.name}</p>
                    </div>
                    ${['CU-01', 'CU-02'].includes(cu.id) ? `
                    <div>
                        <a href="#CU-03" class="wf-btn-gold">
                            <i class="fa-solid fa-plus"></i>
                            <span>Crear Curso</span>
                        </a>
                    </div>
                    ` : ''}
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
