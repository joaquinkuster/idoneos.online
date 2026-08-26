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

// 3. Heroicons SVG Library
const icons = {
  academicCap: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M4.26 10.147a60.438 60.438 0 0 0-.491 6.347A48.62 48.62 0 0 1 12 20.904a48.62 48.62 0 0 1 8.232-4.41 60.46 60.46 0 0 0-.491-6.347m-15.482 0a50.636 50.636 0 0 0-2.658-.813A59.906 59.906 0 0 1 12 3.493a59.903 59.903 0 0 1 10.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.717 50.717 0 0 1 12 13.489a50.702 50.702 0 0 1 7.74-3.342M6.75 15a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Zm0 0v-3.675A55.378 55.378 0 0 1 12 8.443m-5.25 6.557c1.764.39 3.58.648 5.25.75" /></svg>`,
  briefcase: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0M12 12.75h.008v.008H12v-.008Z" /></svg>`,
  bolt: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m3.75 13.5 10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75Z" /></svg>`,
  documentText: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" /></svg>`,
  videoCamera: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m15.75 10.5 4.72-4.72a.75.75 0 0 1 1.28.53v11.38a.75.75 0 0 1-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25h-9A2.25 2.25 0 0 0 2.25 7.5v9a2.25 2.25 0 0 0 2.25 2.25Z" /></svg>`,
  presentationChart: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3v11.25A2.25 2.25 0 0 0 6 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0 1 18 16.5h-2.25m-7.5 0h7.5m-7.5 0-1 3m8.5-3 1 3m0 0 .5 1.5m-.5-1.5h-9.5m0 0-.5 1.5m.75-9 3-3 2.143 2.143L15.75 6" /></svg>`,
  bookOpen: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25" /></svg>`,
  clipboardCheck: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M11.35 3.836c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m8.9-4.414c.376.023.75.05 1.124.08 1.131.09 1.976 1.053 1.976 2.188V19.5a2.25 2.25 0 0 1-2.25 2.25H6.75A2.25 2.25 0 0 1 4.5 19.5V6.25c0-1.135.845-2.098 1.976-2.188a48.57 48.57 0 0 1 1.124-.08C7.6 3.98 7.6 3.98 7.6 3.98m3.75 9.75 2.25 2.25 4.5-4.5" /></svg>`,
  queueList: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 12h16.5m-16.5 3.75h16.5M3.75 19.5h16.5M5.625 4.5h12.75a1.875 1.875 0 0 1 0 3.75H5.625a1.875 1.875 0 0 1 0-3.75Z" /></svg>`,
  chatBubble: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a.75.75 0 0 1-.974-.94 5.95 5.95 0 0 0 .97-2.613A8.136 8.136 0 0 1 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" /></svg>`,
  checkCircle: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" /></svg>`,
  lockClosed: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z" /></svg>`,
  cog6Tooth: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 0 1 1.37.49l1.296 2.247a1.125 1.125 0 0 1-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 0 1 0 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 0 1-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 0 1-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 0 1-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 0 1-1.369-.49l-1.297-2.247a1.125 1.125 0 0 1 .26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 0 1 0-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 0 1-.26-1.43l1.297-2.247a1.125 1.125 0 0 1 1.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /></svg>`,
  plus: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" /></svg>`,
  pencilSquare: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10" /></svg>`,
  trash: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" /></svg>`,
  bars3: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" /></svg>`,
  arrowRight: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" /></svg>`,
  exclamationTriangle: (cls="w-6 h-6") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" /></svg>`,
  chevronDown: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5" /></svg>`,
  arrowRightOnRectangle: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15M12 9l-3 3m0 0 3 3m-3-3h12.75" /></svg>`
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
  
  const moduleMatch = block.match(/\*\*Módulo\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const actorsMatch = block.match(/\*\*Actor\(es\)\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const descMatch = block.match(/\*\*Descripción\*\*\s*[\r\n]+([\s\S]*?)(?=\*\*Precondición|\*\*Flujo)/);
  
  const badges = [];
  const badgeRegex = /\[([A-Z])\]/g;
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
            { label: 'Lista de participantes', cu: 'CU-25' }
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
      <!-- Barra superior de búsqueda y filtros -->
      <div class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar cursos por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Idoneidad Bursátil, Finanzas...">
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
                <div class="wf-dropdown-item">☐ Mercado de Capitales</div>
                <div class="wf-dropdown-item">☐ Finanzas Corporativas</div>
                <div class="wf-dropdown-item">☐ Trading & Algoritmos</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100">Buscar</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Grid de Cursos en Cards -->
      <div class="wf-section-title d-flex justify-content-between align-items-center mb-3">
        <h4>${isDocente ? 'Cursos asignados a mi cargo (Selección Obligatoria)' : (isAlumno ? 'Mis Cursos Matriculados' : 'Catálogo Abierto de Cursos')}</h4>
        <span class="wf-badge status-active">3 cursos encontrados</span>
      </div>

      <div class="wf-cards-grid">
        <!-- Card 1 -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-tag">Mercado de Capitales</div>
            <div class="wf-course-thumb-icon">${icons.academicCap("w-10 h-10 text-white")}</div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Especialización en Idoneidad Bursátil CNV</h4>
            <p class="wf-course-desc">Preparación integral para rendir el examen de idoneidad ante la Comisión Nacional de Valores.</p>
            <div class="wf-course-meta">
              <span><strong>Docente:</strong> Fausto Spotorno</span>
              <span><strong>Cohorte:</strong> 2026-1 (Programa Vigente)</span>
            </div>
            ${isAlumno ? `
              <div class="wf-progress-bar-wrap mt-2">
                <div class="d-flex justify-content-between small text-muted mb-1">
                  <span>Progreso: 65%</span>
                  <span>Unidad 3 de 5</span>
                </div>
                <div class="wf-progress"><div class="wf-progress-fill" style="width: 65%;"></div></div>
              </div>
            ` : ''}
          </div>
          <div class="wf-course-card-footer">
            <div class="d-flex align-items-center justify-content-between w-100">
              <span class="wf-badge status-active">Activo</span>
              <div class="d-flex align-items-center gap-2">
                <a href="#CU-${isDocente ? '26b' : '26'}" class="wf-btn wf-btn-primary wf-btn-sm d-flex align-items-center gap-1">
                  <span>${isDocente ? 'Gestionar curso' : (isAlumno ? 'Ingresar al curso' : 'Ver ficha / Inscribirme')}</span>
                  ${icons.arrowRight()}
                </a>
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Card 2 -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-tag">Finanzas de Empresas</div>
            <div class="wf-course-thumb-icon">${icons.briefcase("w-10 h-10 text-white")}</div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Valuación de Empresas & Finanzas Corporativas</h4>
            <p class="wf-course-desc">Métodos DCF, múltiplos comparables y modelación financiera avanzada en Excel.</p>
            <div class="wf-course-meta">
              <span><strong>Docente:</strong> Lic. Juan Pérez</span>
              <span><strong>Cohorte:</strong> 2026-1</span>
            </div>
            ${isAlumno ? `
              <div class="wf-progress-bar-wrap mt-2">
                <div class="d-flex justify-content-between small text-muted mb-1">
                  <span>Progreso: 20%</span>
                  <span>Unidad 1 de 4</span>
                </div>
                <div class="wf-progress"><div class="wf-progress-fill" style="width: 20%;"></div></div>
              </div>
            ` : ''}
          </div>
          <div class="wf-course-card-footer">
            <div class="d-flex align-items-center justify-content-between w-100">
              <span class="wf-badge status-active">Activo</span>
              <button class="wf-btn wf-btn-primary wf-btn-sm d-flex align-items-center gap-1">
                <span>${isDocente ? 'Gestionar curso' : (isAlumno ? 'Ingresar al curso' : 'Ver ficha')}</span>
                ${icons.arrowRight()}
              </button>
            </div>
          </div>
        </div>

        <!-- Card 3 -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-tag">Trading Cuantitativo</div>
            <div class="wf-course-thumb-icon">${icons.bolt("w-10 h-10 text-white")}</div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Análisis Técnico y Algoritmos de Trading</h4>
            <p class="wf-course-desc">Desarrollo de estrategias automatizadas de trading en Python y backtesting.</p>
            <div class="wf-course-meta">
              <span><strong>Docente:</strong> Dr. Carlos Gómez</span>
              <span><strong>Cohorte:</strong> 2025-2</span>
            </div>
            ${isAlumno ? `
              <div class="wf-progress-bar-wrap mt-2">
                <div class="d-flex justify-content-between small text-muted mb-1">
                  <span>Progreso: 100%</span>
                  <span>Completado</span>
                </div>
                <div class="wf-progress"><div class="wf-progress-fill" style="width: 100%; background: #059669;"></div></div>
              </div>
            ` : ''}
          </div>
          <div class="wf-course-card-footer">
            <div class="d-flex align-items-center justify-content-between w-100">
              <span class="wf-badge status-inactive">Finalizado</span>
              <button class="wf-btn wf-btn-outline wf-btn-sm">Ver histórico</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 2: VISTA DEL CURSO ESTILO MOODLE (ALUMNO) --- CU-26, CU-62
  if (id === 'CU-26') {
    return `
      <!-- Breadcrumb & Encabezado del Curso -->
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <div class="wf-breadcrumbs small text-muted mb-1">
              <span>Mis cursos</span> ➔ <span>Mercado de Capitales</span> ➔ <strong>Especialización en Idoneidad Bursátil</strong>
            </div>
            <h3 class="wf-course-main-title">Especialización en Idoneidad Bursátil (Cohorte 2026-1)</h3>
            <p class="small text-muted">Docente Titular: Lic. Fausto Spotorno | Duración: 8 Semanas | Programa Vigente</p>
          </div>
          <div class="text-end">
            <span class="wf-badge status-active">Inscripción Vigente</span>
            <div class="small text-muted mt-1">Progreso general: <strong>65%</strong></div>
          </div>
        </div>

        <!-- Barra de Navegación del Curso (Tabs de Alumno FCEQYN Virtual: Curso, Participantes, Calificaciones, etc.) -->
        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-4">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span> Curso</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[4] || 'E'}</span> Participantes</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[5] || 'F'}</span> Calificaciones</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Cronograma</button>
            <button class="wf-tab-btn">Insignias</button>
            <button class="wf-tab-btn">Competencias</button>
            <button class="wf-tab-btn">Más ▾</button>
          </div>
        </div>
      </div>

      <!-- Acordeón Moodle de Unidades con Heroicons -->
      <div class="wf-moodle-accordion">
        
        <!-- Unidad 1 (Completada) -->
        <div class="wf-unit-box completed mb-3">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <span class="text-success">${icons.checkCircle("w-5 h-5")}</span>
              <strong>Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
            </div>
            <span class="wf-badge status-active">Aprobada (Nota: 9/10)</span>
          </div>
          <div class="wf-unit-body">
            <ul class="wf-content-list">
              <li class="d-flex align-items-center gap-2">${icons.documentText("w-4 h-4 text-muted")} <a href="#" class="wf-link">Ley 26.831 y modificatorias (PDF - 2.4 MB)</a></li>
              <li class="d-flex align-items-center gap-2">${icons.videoCamera("w-4 h-4 text-muted")} <a href="#" class="wf-link">Grabación: Estructura del Mercado Argentino (45 min)</a></li>
              <li class="d-flex align-items-center gap-2">${icons.bookOpen("w-4 h-4 text-muted")} <a href="#" class="wf-link">Glosario de Términos CNV (12 términos)</a></li>
            </ul>
          </div>
        </div>

        <!-- Unidad 2 (En curso / Abierta) -->
        <div class="wf-unit-box active mb-3">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <span class="text-navy">${icons.bookOpen("w-5 h-5")}</span>
              <strong>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</strong>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="wf-badge status-active">En Curso</span>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
          <div class="wf-unit-body">
            <p class="small text-muted mb-3">Conceptos de TIR, Duration, Modified Duration y curvas de rendimiento soberanas.</p>
            
            <div class="wf-subcontent-group">
              <div class="wf-subcontent-title">Materiales de Estudio</div>
              <ul class="wf-content-list">
                <li class="d-flex align-items-center gap-2">${icons.documentText("w-4 h-4 text-muted")} <a href="#" class="wf-link">Guía Teórica de Renta Fija v2.1 (PDF)</a></li>
                <li class="d-flex align-items-center gap-2">${icons.presentationChart("w-4 h-4 text-muted")} <a href="#" class="wf-link">Planilla Excel: Cálculo de TIR y Flujos de Fondos</a></li>
              </ul>
            </div>

            <div class="wf-subcontent-group mt-3">
              <div class="wf-subcontent-title">Actividades & Evaluaciones</div>
              <ul class="wf-content-list">
                <li>
                  <div class="d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center gap-2">
                      ${icons.clipboardCheck("w-4 h-4 text-navy")}
                      <span><a href="#CU-63" class="wf-link"><strong>Autoevaluación Unidad 2: Ejercicios de Rendimiento</strong></a> (3 intentos máx.)</span>
                    </div>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                  </div>
                </li>
                <li class="d-flex align-items-center gap-2">${icons.chatBubble("w-4 h-4 text-muted")} <a href="#CU-35" class="wf-link">Foro de Consultas: Dudas sobre Duración Modificada</a> (4 consultas)</li>
                <li class="d-flex align-items-center gap-2">${icons.videoCamera("w-4 h-4 text-danger")} <a href="#CU-72" class="wf-link">Clase en Vivo: Resolución de Prácticos (Jueves 19:00 hs)</a></li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Unidad 3 (Bloqueada) -->
        <div class="wf-unit-box locked mb-3">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted">${icons.lockClosed("w-5 h-5")}</span>
              <span class="text-muted">Unidad 3: Instrumentos de Renta Variable (Acciones y CEDEARs)</span>
            </div>
            <span class="wf-badge status-inactive">Requiere aprobar Autoevaluación U2</span>
          </div>
        </div>

      </div>
    `;
  }

  // --- TYPE 3: VISTA DEL CURSO EN MODO EDICIÓN (DOCENTE / ADMIN) --- CU-26b, CU-19, CU-20, CU-21, CU-22
  if (['CU-26b', 'CU-19', 'CU-20', 'CU-21', 'CU-22'].includes(id)) {
    return `
      <!-- Breadcrumb & Encabezado del Curso Estilo Moodle (Captura 1) -->
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <h2 class="wf-course-main-title" style="font-size: 24px; font-weight: 700; color: #1E293B;">Especialización en Idoneidad Bursátil</h2>
            <div class="wf-breadcrumbs small text-muted mt-1" style="color: #D97706;">
              <span>Página Principal</span> / <span>Mis cursos</span> / <span>Idoneidad Bursátil</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2 bg-light p-2 border rounded">
              <span class="wf-badge status-active">Modo Edición ACTIVO</span>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
            <div class="text-muted cursor-pointer" title="Ajustes del curso">${icons.cog6Tooth("w-6 h-6 text-slate-600")}</div>
          </div>
        </div>

        <!-- Barra de Navegación Contextual del Curso (Tabs Moodle FCEQYN Virtual) -->
        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-1">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Curso & Unidades</button>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
            <span class="wf-tab-btn text-muted">Más ▾</span>
          </div>
          <div class="d-flex align-items-center gap-2 pb-1">
            <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
              ${icons.plus()}
              <span>Añadir secciones</span>
            </button>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <!-- Estructuración de Unidades Moodle en Modo Edición (Fiel a la Captura 1 del GIF) -->
      <div class="wf-moodle-accordion">
        
        <!-- Sección General / Avisos -->
        <div class="wf-unit-box mb-3 p-3 bg-white border rounded">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
              <span class="d-flex align-items-center gap-1">${icons.chatBubble("w-4 h-4 text-warning")} <strong style="color: #D97706;">Avisos Generales y Novedades</strong></span>
              <span class="text-muted">${icons.pencilSquare("w-3 h-3")}</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="small text-muted">Editar ▾</span>
            </div>
          </div>
          <div class="d-flex justify-content-end mt-2 pt-2 border-top">
            <a href="#CU-28" class="text-decoration-none small fw-bold d-flex align-items-center gap-1" style="color: #475569;">
              ${icons.plus("w-3 h-3")} Añade una actividad o un recurso
            </a>
          </div>
        </div>

        <!-- Unidad 1 / Tema 1 -->
        <div class="wf-unit-box mb-3 p-3 bg-white border rounded">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted cursor-move">${icons.bars3("w-5 h-5")}</span>
              <h4 style="font-size: 16px; font-weight: 700; color: #D97706; margin: 0;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</h4>
              <span class="text-muted">${icons.pencilSquare("w-4 h-4")}</span>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="small text-muted">Editar ▾</span>
            </div>
          </div>

          <!-- Contenidos dentro de la unidad -->
          <div class="wf-unit-body p-0">
            <div class="wf-content-item-row d-flex justify-content-between align-items-center py-2 border-bottom">
              <div class="d-flex align-items-center gap-2 ps-3">
                <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                ${icons.documentText("w-4 h-4 text-muted")}
                <span>Ley 26.831 y modificatorias (PDF - 2.4 MB)</span>
                <span class="text-muted">${icons.pencilSquare("w-3 h-3")}</span>
              </div>
              <span class="small text-muted pe-2">Editar ▾</span>
            </div>

            <div class="wf-content-item-row d-flex justify-content-between align-items-center py-2 border-bottom">
              <div class="d-flex align-items-center gap-2 ps-3">
                <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                ${icons.videoCamera("w-4 h-4 text-muted")}
                <span>Grabación: Estructura del Mercado Argentino (45 min)</span>
                <span class="text-muted">${icons.pencilSquare("w-3 h-3")}</span>
              </div>
              <span class="small text-muted pe-2">Editar ▾</span>
            </div>

            <div class="wf-content-item-row d-flex justify-content-between align-items-center py-2 border-bottom">
              <div class="d-flex align-items-center gap-2 ps-3">
                <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                ${icons.clipboardCheck("w-4 h-4 text-muted")}
                <span>Autoevaluación U1: Marco Legal (10 preguntas)</span>
                <span class="text-muted">${icons.pencilSquare("w-3 h-3")}</span>
              </div>
              <span class="small text-muted pe-2">Editar ▾</span>
            </div>

            <!-- Botón + Añade una actividad o un recurso a la derecha con cursor (como en el GIF) -->
            <div class="d-flex justify-content-end align-items-center gap-2 mt-3 pt-2">
              <a href="#CU-28" class="text-decoration-none small fw-bold d-flex align-items-center gap-1 p-2 rounded" style="color: #1E293B; background: #F1F5F9;">
                ${icons.plus("w-4 h-4")} <strong>Añade una actividad o un recurso</strong>
              </a>
              <span class="pin-badge">${badges[3] || badges[4] || 'D'}</span>
            </div>
          </div>
        </div>

        <!-- Unidad 2 / Tema 2 -->
        <div class="wf-unit-box mb-3 p-3 bg-white border rounded">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted cursor-move">${icons.bars3("w-5 h-5")}</span>
              <h4 style="font-size: 16px; font-weight: 700; color: #D97706; margin: 0;">Unidad 2: Instrumentos de Renta Fija (Bonos y ONs)</h4>
              <span class="text-muted">${icons.pencilSquare("w-4 h-4")}</span>
            </div>
            <span class="small text-muted">Editar ▾</span>
          </div>
          <div class="d-flex justify-content-end mt-3 pt-2 border-top">
            <a href="#CU-28" class="text-decoration-none small fw-bold d-flex align-items-center gap-1 p-2 rounded" style="color: #1E293B; background: #F1F5F9;">
              ${icons.plus("w-4 h-4")} Añade una actividad o un recurso
            </a>
          </div>
        </div>

        <!-- Unidad 3 / Tema 3 -->
        <div class="wf-unit-box mb-3 p-3 bg-white border rounded">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted cursor-move">${icons.bars3("w-5 h-5")}</span>
              <h4 style="font-size: 16px; font-weight: 700; color: #D97706; margin: 0;">Unidad 3: Instrumentos de Renta Variable (Acciones y CEDEARs)</h4>
              <span class="text-muted">${icons.pencilSquare("w-4 h-4")}</span>
            </div>
            <span class="small text-muted">Editar ▾</span>
          </div>
          <div class="d-flex justify-content-end mt-3 pt-2 border-top">
            <a href="#CU-28" class="text-decoration-none small fw-bold d-flex align-items-center gap-1 p-2 rounded" style="color: #1E293B; background: #F1F5F9;">
              ${icons.plus("w-4 h-4")} Añade una actividad o un recurso
            </a>
          </div>
        </div>

        <!-- Añadir secciones al pie -->
        <div class="d-flex justify-content-end mt-2">
          <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1" style="color: #D97706;">
            ${icons.plus("w-4 h-4")} Añadir secciones
          </button>
        </div>

      </div>
    `;
  }

  // --- TYPE 4: LISTADOS CONTEXTUALES DENTRO DEL CURSO (MOODLE STYLE FIEL A LA CAPTURA 5) --- CU-27, CU-31, CU-35, CU-53, CU-57, CU-65, CU-25
  if (['CU-27', 'CU-31', 'CU-35', 'CU-53', 'CU-57', 'CU-65', 'CU-25'].includes(id)) {
    const isMaterial = id === 'CU-27';
    const isGlosario = id === 'CU-31';
    const isForo = id === 'CU-35';
    const isPool = id === 'CU-53';
    const isEval = id === 'CU-57';
    const isLive = id === 'CU-65';
    const isPart = id === 'CU-25';

    return `
      <!-- Encabezado del Curso Estilo Moodle (Captura 5) -->
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <h2 class="wf-course-main-title" style="font-size: 24px; font-weight: 700; color: #1E293B;">Especialización en Idoneidad Bursátil</h2>
            <div class="wf-breadcrumbs small text-muted mt-1" style="color: #D97706;">
              <span>Página Principal</span> / <span>Mis cursos</span> / <span>Idoneidad Bursátil</span> / <span>${name}</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Curso Seleccionado (Programa 2026-1)</span>
            <div class="text-muted cursor-pointer" title="Ajustes del curso">${icons.cog6Tooth("w-6 h-6 text-slate-600")}</div>
          </div>
        </div>

        <!-- Barra Superior de Navegación Contextual del Curso (Tabs Moodle FCEQYN Virtual) -->
        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn ${isMaterial ? 'active' : ''}">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn ${isGlosario ? 'active' : ''}">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn ${isEval ? 'active' : ''}">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn ${isPool ? 'active' : ''}">Pools</a>
            <a href="#CU-35" class="wf-tab-btn ${isForo ? 'active' : ''}">Foros</a>
            <a href="#CU-65" class="wf-tab-btn ${isLive ? 'active' : ''}">Clases</a>
            <span class="wf-tab-btn text-muted">Más ▾</span>
          </div>
          <div class="d-flex align-items-center gap-2 pb-1">
            <a href="#${isMaterial ? 'CU-28' : (isGlosario ? 'CU-32' : (isEval ? 'CU-58' : (isPool ? 'CU-54' : 'CU-66')))}" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1">
              ${icons.plus()}
              <span>Nuevo ${isMaterial ? 'Material' : (isGlosario ? 'Término' : (isEval ? 'Autoevaluación' : (isPool ? 'Pool' : 'Elemento')))}</span>
            </a>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <!-- Sub-Tabs de Acción Estilo Moodle (Previsualizar / Edición / Informes / Calificar) -->
      <div class="d-flex gap-3 px-3 py-2 bg-white border-bottom small fw-bold mb-3" style="color: #64748B;">
        <span class="cursor-pointer text-muted">Previsualizar</span>
        <span class="cursor-pointer pb-1 border-bottom border-2 border-warning" style="color: #D97706;">Edición</span>
        <span class="cursor-pointer text-muted">Informes y Estadísticas</span>
        <span class="cursor-pointer text-muted">Calificaciones</span>
      </div>

      <!-- Tabla Contextual con Iconos de Acción Inline de Moodle (Mover, Ajustes, Duplicar, Borrar) -->
      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Título / Nombre en el Programa</th>
              <th>Tipo de Recurso</th>
              <th>Pertenencia / Unidad</th>
              <th>Estado</th>
              <th class="text-end">Acciones Moodle</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                  <strong>${isMaterial ? 'Ley 26.831 de Mercado de Capitales' : (isGlosario ? 'TIR (Tasa Interna de Retorno)' : (isEval ? 'Autoevaluación Unidad 1: Marco Legal' : 'Pool Unidad 1 - Preguntas'))}</strong>
                </div>
              </td>
              <td>${isMaterial ? 'Documento PDF (2.4 MB)' : (isGlosario ? 'Definición Financiera' : (isEval ? '10 preguntas aleatorias' : '25 preguntas'))}</td>
              <td>Unidad 1: Marco Regulatorio</td>
              <td><span class="wf-badge status-active">Publicado</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <span class="text-muted cursor-pointer" title="Mover">${icons.bars3("w-4 h-4")}</span>
                  <span class="text-muted cursor-pointer" title="Configurar">${icons.cog6Tooth("w-4 h-4")}</span>
                  <button class="wf-btn wf-btn-sm wf-btn-outline">${icons.pencilSquare()}</button>
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger">${icons.trash()}</button>
                  <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                  <strong>${isMaterial ? 'Grabación Clase 1: Introducción' : (isGlosario ? 'Duration Modificada' : (isEval ? 'Autoevaluación Unidad 2: Renta Fija' : 'Pool Unidad 2 - Preguntas'))}</strong>
                </div>
              </td>
              <td>${isMaterial ? 'Grabación de Video (45 min)' : (isGlosario ? 'Definición Financiera' : (isEval ? '12 preguntas aleatorias' : '30 preguntas'))}</td>
              <td>Unidad 2: Renta Fija</td>
              <td><span class="wf-badge status-active">Publicado</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <span class="text-muted cursor-pointer" title="Mover">${icons.bars3("w-4 h-4")}</span>
                  <span class="text-muted cursor-pointer" title="Configurar">${icons.cog6Tooth("w-4 h-4")}</span>
                  <button class="wf-btn wf-btn-sm wf-btn-outline">${icons.pencilSquare()}</button>
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger">${icons.trash()}</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- TYPE 5: MODAL DE AGREGAR CONTENIDO (MOODLE STYLE FIEL AL GIF CAPTURAS 2 Y 3) --- CU-28, CU-32, CU-36, CU-40, CU-54, CU-58, CU-66
  if (['CU-28', 'CU-32', 'CU-36', 'CU-40', 'CU-54', 'CU-58', 'CU-66'].includes(id)) {
    const isMaterial = id === 'CU-28';
    const isGlosario = id === 'CU-32';
    const isForo = id === 'CU-36' || id === 'CU-40';
    const isPool = id === 'CU-54';
    const isEval = id === 'CU-58';
    const isLive = id === 'CU-66';

    let selectedTitle = 'Material / Documento de Estudio';
    let selectedDesc = 'Permite al docente presentar contenidos teóricos, guías prácticas en PDF, videos o presentaciones multimedia organizadas en la unidad pedagógica.';
    let inputLabel = 'Nombre / Título del material';
    let inputValue = 'Guía Teórica de Renta Fija v2.0 (PDF)';

    if (isGlosario) {
      selectedTitle = 'Glosario de Términos';
      selectedDesc = 'Permite a los docentes y participantes crear y mantener una lista de definiciones financieras y conceptos técnicos del mercado bursátil.';
      inputLabel = 'Término a registrar';
      inputValue = 'TIR (Tasa Interna de Retorno)';
    } else if (isForo) {
      selectedTitle = 'Foro de Consultas y Debate';
      selectedDesc = 'Permite a los alumnos y profesores mantener conversaciones asincrónicas sobre los temas de la unidad o publicar respuestas oficiales.';
      inputLabel = 'Asunto / Título de la consulta o respuesta';
      inputValue = 'Respuesta oficial: Dudas sobre Duración Modificada';
    } else if (isEval) {
      selectedTitle = 'Cuestionario / Autoevaluación';
      selectedDesc = 'Permite al profesor diseñar evaluaciones automáticas con preguntas aleatorias extraídas de un Pool temático para evaluar el aprendizaje.';
      inputLabel = 'Nombre de la autoevaluación';
      inputValue = 'Autoevaluación Unidad 1: Marco Regulatorio';
    } else if (isPool) {
      selectedTitle = 'Pool de Preguntas de Evaluación';
      selectedDesc = 'Banco centralizado de preguntas y opciones múltiples asociadas a los temas de la unidad para sorteo en evaluaciones.';
      inputLabel = 'Nombre del pool de preguntas';
      inputValue = 'Pool Unidad 1: Mercado de Capitales (25 preguntas)';
    } else if (isLive) {
      selectedTitle = 'Clase en Vivo (Streaming)';
      selectedDesc = 'Permite programar una sesión sincrónica en tiempo real con sala de streaming interactiva y chat para resolución de casos en directo.';
      inputLabel = 'Título de la clase en vivo';
      inputValue = 'Clase en Vivo #1: Resolución de Prácticos';
    }

    return `
      <!-- Fondo atenuado / Contexto de Unidad de Fondo -->
      <div class="wf-dimmed-bg-preview mb-3 p-3 border rounded" style="opacity: 0.35; background: #F1F5F9;">
        <div class="d-flex justify-content-between align-items-center">
          <div>
            <strong>Especialización en Idoneidad Bursátil</strong> ➔ <span>Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</span>
          </div>
          <span class="wf-badge status-active">Modo Edición Activado</span>
        </div>
      </div>

      <!-- Modal Overlay Moodle-Style Fiel al GIF (Capturas 2 y 3) -->
      <div class="wf-modal-box" style="max-width: 900px; margin: 20px auto; border: 1px solid #CBD5E1; border-radius: 12px; box-shadow: 0 24px 54px rgba(0,0,0,0.18); overflow: hidden; background: #FFFFFF;">
        
        <!-- Header del Modal Moodle con Botón de Cierre -->
        <div class="d-flex justify-content-between align-items-center px-5 py-4 bg-white border-bottom">
          <h3 style="font-size: 19px; font-weight: 700; color: #1E293B; margin: 0;">Añade una actividad o un recurso</h3>
          <span class="wf-close-btn" style="font-size: 22px; color: #94A3B8; cursor: pointer; font-weight: 700;">✕</span>
        </div>

        <!-- Cuerpo del Modal en 2 Columnas con Espaciado Generoso -->
        <div class="row g-0 bg-white" style="min-height: 440px;">
          
          <!-- Columna Izquierda: Lista Vertical de Actividades con Radio Buttons e Iconos Heroicons -->
          <div class="col-md-5 border-end p-4" style="background: #F8FAFC; max-height: 480px; overflow-y: auto; padding: 24px 20px !important;">
            <div class="small fw-bold text-muted text-uppercase mb-3" style="font-size: 11px; letter-spacing: 0.6px; color: #64748B; padding-left: 4px;">ACTIVIDADES & RECURSOS</div>
            
            <div class="d-flex flex-column gap-2">
              <!-- Opción Material -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isMaterial ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isMaterial ? 'checked' : ''}>
                ${icons.documentText("w-5 h-5 text-primary")}
                <span>Material / Documento PDF</span>
              </label>

              <!-- Opción Glosario -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isGlosario ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isGlosario ? 'checked' : ''}>
                ${icons.bookOpen("w-5 h-5 text-warning")}
                <span>Glosario de Términos</span>
              </label>

              <!-- Opción Cuestionario / Autoevaluación -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isEval ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isEval ? 'checked' : ''}>
                ${icons.clipboardCheck("w-5 h-5 text-success")}
                <span>Cuestionario / Autoevaluación</span>
              </label>

              <!-- Opción Pool de Preguntas -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isPool ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isPool ? 'checked' : ''}>
                ${icons.queueList("w-5 h-5 text-info")}
                <span>Pool de Preguntas</span>
              </label>

              <!-- Opción Foro -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isForo ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isForo ? 'checked' : ''}>
                ${icons.chatBubble("w-5 h-5 text-muted")}
                <span>Foro de Consultas</span>
              </label>

              <!-- Opción Clase en Vivo -->
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isLive ? 'bg-white border shadow-sm fw-bold' : 'border border-transparent'}" style="font-size: 13px; color: #1E293B; transition: all 0.15s; margin-bottom: 2px;">
                <input type="radio" name="moodle_act" ${isLive ? 'checked' : ''}>
                ${icons.videoCamera("w-5 h-5 text-danger")}
                <span>Clase en Vivo (Streaming)</span>
              </label>
            </div>

            <div class="mt-4 pt-2 text-end pe-2">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <!-- Columna Derecha: Panel de Descripción y Campos de Configuración Rápida -->
          <div class="col-md-7 d-flex flex-column justify-content-between" style="padding: 32px 36px !important;">
            <div>
              <h4 style="font-size: 17px; font-weight: 700; color: #1E293B; margin-bottom: 10px;">${selectedTitle}</h4>
              <p class="small text-muted" style="line-height: 1.6; margin-bottom: 24px; color: #64748B; font-size: 12px;">
                ${selectedDesc}
              </p>

              <!-- Campos del formulario dentro del modal con espaciado cómodo -->
              <div class="mb-4">
                <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">${inputLabel}</label>
                <div class="wf-input-wrap">
                  <input type="text" class="wf-input" value="${inputValue}" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;">
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </div>

              ${isMaterial ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Archivo adjunto o Enlace URL</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="guia_teorica_u1.pdf (Subido)" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;">
                    <button class="wf-btn wf-btn-outline wf-btn-sm text-nowrap" style="padding: 10px 16px;">Examinar...</button>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isGlosario ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Definición del Concepto Financiero</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="3" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;">Tasa que iguala el valor actual de los flujos de fondos con el precio del bono.</textarea>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isForo ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Cuerpo del Mensaje / Respuesta</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="3" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;">Estimados alumnos, la duración modificada mide la sensibilidad del precio del bono ante cambios en la tasa de interés.</textarea>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isEval ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Pool de Preguntas Seleccionado</label>
                  <div class="wf-input-wrap">
                    <select class="wf-input" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;"><option>Pool Unidad 1 - Renta Fija (25 preguntas disponibles)</option></select>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isPool ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Unidad Académica de Pertenencia</label>
                  <div class="wf-input-wrap">
                    <select class="wf-input" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;"><option>Unidad 1: Marco Regulatorio y Ley CNV</option></select>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isLive ? `
                <div class="mb-4">
                  <label class="wf-label" style="font-weight: 600; margin-bottom: 8px; font-size: 12px;">Fecha y Hora Programada de Transmisión</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="Jueves 28/08/2026 - 19:00 hs (GMT-3)" style="padding: 11px 14px; font-size: 13px; border-radius: 6px;">
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}
            </div>

            <!-- Botones Naranja/Gris idénticos al GIF de Moodle -->
            <div class="d-flex justify-content-end align-items-center gap-3 pt-4 border-top mt-4">
              <button class="wf-btn wf-btn-sm" style="background: #E2E8F0; color: #475569; padding: 10px 20px; border-radius: 6px; font-weight: 600; font-size: 13px;">Cancelar</button>
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm fw-bold text-white" style="background: #F97316; padding: 10px 24px; border-radius: 6px; box-shadow: 0 2px 4px rgba(249,115,22,0.3); font-size: 13px;">Agregar</button>
                <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
              </div>
            </div>
          </div>

        </div>

      </div>
    `;
  }

  // --- TYPE 6: GENERIC CRUD / FORMS / TABLES FOR OTHER CUs ---
  const isSearch = name.toLowerCase().startsWith('buscar') || name.toLowerCase().startsWith('consultar') || name.toLowerCase().startsWith('ver') || name.toLowerCase().startsWith('explorar');
  const isDelete = name.toLowerCase().includes('baja') || name.toLowerCase().includes('cancelar') || name.toLowerCase().includes('eliminar') || name.toLowerCase().includes('quitar');

  if (isDelete) {
    return `
      <div class="wf-card" style="max-width: 600px; margin: 40px auto;">
        <div class="wf-card-header text-center mb-4">
          <div class="wf-icon-danger mb-3">${icons.exclamationTriangle("w-8 h-8 text-danger")}</div>
          <h3>Confirmar Baja / Eliminación</h3>
          <p class="text-muted">¿Está seguro de que desea realizar la operación sobre el elemento seleccionado?</p>
        </div>

        <div class="wf-card p-3 mb-4 bg-light">
          <div class="d-flex justify-content-between mb-2">
            <span class="text-muted">Elemento a dar de baja:</span>
            <strong>Registro Seleccionado #${id.replace('CU-', '')}</strong>
          </div>
          <div class="d-flex justify-content-between">
            <span class="text-muted">Módulo asociado:</span>
            <span>${cu.module}</span>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-3">
          <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-danger">${icons.trash()} Confirmar Baja</button>
            <span class="pin-badge">${badges[badges.length - 1] || 'A'}</span>
          </div>
        </div>
      </div>
    `;
  }

  if (isSearch) {
    return `
      <div class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Criterio de búsqueda / Filtro principal</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ingresar término de búsqueda...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtro Secundario / Estado</label>
            <div class="wf-select-container">
              <div class="wf-input-wrap">
                <div class="wf-input wf-select-trigger">
                  <span>Todos los registros</span>
                  ${icons.chevronDown()}
                </div>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div class="wf-dropdown-menu">
                <div class="wf-dropdown-item active">☑ Todos los registros</div>
                <div class="wf-dropdown-item">☐ Activos / Vigentes</div>
                <div class="wf-dropdown-item">☐ Finalizados / Históricos</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100">Buscar</button>
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
              <th>Fecha / Registro</th>
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
                  <button class="wf-btn wf-btn-sm wf-btn-outline">Seleccionar / Ver</button>
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
                <button class="wf-btn wf-btn-sm wf-btn-outline">Seleccionar / Ver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // Default Form View (Registrar / Modificar)
  return `
    <div class="wf-card">
      <div class="wf-card-header mb-4">
        <h3>${name}</h3>
        <p class="text-muted">Complete los campos requeridos y confirme la operación en el sistema.</p>
      </div>

      <div class="row">
        <div class="col-md-6 mb-3">
          <label class="wf-label">Campo Principal / Nombre</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Ejemplo de Valor para ${name}">
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="col-md-6 mb-3">
          <label class="wf-label">Categoría / Tipo / Asociación</label>
          <div class="wf-select-container">
            <div class="wf-input-wrap">
              <div class="wf-input wf-select-trigger">
                <span>Opción Seleccionada por Defecto</span>
                ${icons.chevronDown()}
              </div>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
            <div class="wf-dropdown-menu">
              <div class="wf-dropdown-item active">☑ Opción Seleccionada por Defecto</div>
              <div class="wf-dropdown-item">☐ Opción Alternativa 1</div>
              <div class="wf-dropdown-item">☐ Opción Alternativa 2</div>
            </div>
          </div>
        </div>

        <div class="col-12 mb-3">
          <label class="wf-label">Descripción / Parámetros Detallados</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Descripción detallada correspondiente al caso de uso ${id} (${name}).</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4 mb-3">
          <label class="wf-label">Parámetro Numérico / Vigencia</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="100">
          </div>
        </div>

        <div class="col-md-4 mb-3">
          <label class="wf-label">Estado / Configuración</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Habilitado / Vigente" disabled class="bg-disabled">
          </div>
        </div>

        <div class="col-md-4 mb-3">
          <label class="wf-label">Responsable</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="Docente / Admin Asignado" disabled class="bg-disabled">
          </div>
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary">Confirmar y Guardar</button>
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
    <style>
        :root {
            --wf-navy-dark: #0A2540;
            --wf-navy-active: #103358;
            --wf-gold: #C5A059;
            --wf-bg: #F8FAFC;
            --wf-border: #CBD5E1;
            --wf-text: #0F172A;
            --wf-text-muted: #64748B;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
        }

        body {
            background-color: #E2E8F0;
            color: var(--wf-text);
            display: flex;
            height: 100vh;
            overflow: hidden;
        }

        /* Heroicons sizing helper */
        svg {
            display: inline-block;
            vertical-align: middle;
            flex-shrink: 0;
        }
        .w-3 { width: 14px; }
        .h-3 { height: 14px; }
        .w-4 { width: 16px; }
        .h-4 { height: 16px; }
        .w-5 { width: 20px; }
        .h-5 { height: 20px; }
        .w-6 { width: 24px; }
        .h-6 { height: 24px; }
        .w-8 { width: 32px; }
        .h-8 { height: 32px; }
        .w-10 { width: 40px; }
        .h-10 { height: 40px; }

        /* Buscador Izquierdo Agrupado por Módulos */
        #nav-sidebar {
            width: 300px;
            background: #0F172A;
            color: #FFFFFF;
            overflow-y: auto;
            border-right: 1px solid #1E293B;
            display: flex;
            flex-direction: column;
            flex-shrink: 0;
        }

        .nav-header {
            padding: 20px;
            border-bottom: 1px solid #1E293B;
            background: #0A1120;
        }

        .nav-header h2 {
            font-size: 15px;
            font-weight: 700;
            letter-spacing: 0.5px;
        }

        .nav-header p {
            font-size: 11px;
            color: var(--wf-text-muted);
            margin-top: 2px;
        }

        .nav-search {
            padding: 12px 16px;
            border-bottom: 1px solid #1E293B;
        }

        .nav-search input {
            width: 100%;
            padding: 8px 12px;
            border-radius: 6px;
            border: 1px solid #334155;
            background: #1E293B;
            color: #FFFFFF;
            font-size: 12px;
            outline: none;
        }

        .module-group {
            border-bottom: 1px solid #1E293B;
        }

        .module-header {
            padding: 10px 16px;
            background: #1E293B;
            font-size: 11px;
            font-weight: 700;
            color: var(--wf-gold);
            text-transform: uppercase;
        }

        .module-list {
            list-style: none;
        }

        .nav-item a {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 7px 16px;
            color: #94A3B8;
            text-decoration: none;
            font-size: 12px;
            transition: all 0.15s;
        }

        .nav-item a:hover {
            background: #1E293B;
            color: #FFFFFF;
            border-left: 3px solid var(--wf-gold);
        }

        .nav-item .cu-tag {
            font-size: 10px;
            font-weight: 700;
            padding: 2px 4px;
            border-radius: 4px;
            background: rgba(255,255,255,0.1);
        }

        /* Viewport Central */
        #viewport {
            flex: 1;
            overflow-y: auto;
            padding: 30px;
            display: flex;
            flex-direction: column;
            gap: 60px;
            align-items: center;
        }

        .figure-wrapper {
            width: 100%;
            max-width: 1200px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .figure-caption {
            font-size: 13px;
            color: #334155;
            text-align: left;
            padding-left: 4px;
        }

        /* Screen Frame Minimalista */
        .screen-frame {
            width: 100%;
            min-height: 680px;
            background: #FFFFFF;
            border-radius: 8px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.06);
            border: 1px solid var(--wf-border);
            display: flex;
            flex-direction: column;
            overflow: visible; /* Para permitir que el floating dropdown se aprecie natural */
        }

        /* Top Header Limpio y Elegante */
        .wf-top-navbar {
            height: 56px;
            background: #FFFFFF;
            border-bottom: 1px solid var(--wf-border);
            padding: 0 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: relative;
            z-index: 100;
        }

        .wf-brand {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 14px;
        }

        .wf-brand strong {
            letter-spacing: 0.5px;
            color: var(--wf-navy-dark);
            font-size: 15px;
        }

        .wf-brand .divider {
            color: #CBD5E1;
        }

        .wf-brand .screen-title {
            font-weight: 600;
            color: #475569;
        }

        /* Contenedor del Dropdown de Usuario Flotante */
        .wf-user-menu-wrapper {
            position: relative;
        }

        .wf-user-trigger-pill {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 4px 12px 4px 4px;
            border-radius: 20px;
            border: 1px solid var(--wf-border);
            background: #F8FAFC;
            cursor: pointer;
        }

        .user-avatar-circle {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: var(--wf-navy-dark);
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            font-weight: 700;
        }

        /* Menú Dropdown Flotante del Usuario (Oculto por defecto, se despliega al hacer clic en el avatar) */
        .wf-user-floating-dropdown {
            position: absolute;
            top: 48px;
            right: 0;
            width: 260px;
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
            z-index: 200;
            overflow: hidden;
            display: none; /* Oculto por defecto para no tapar el modal ni el contenido */
            flex-direction: column;
        }

        .wf-dropdown-user-header {
            padding: 12px 14px;
            background: #F8FAFC;
            border-bottom: 1px solid var(--wf-border);
        }

        .wf-dropdown-user-name {
            font-size: 13px;
            font-weight: 700;
            color: #0F172A;
        }

        .wf-dropdown-user-role {
            font-size: 10px;
            font-weight: 700;
            color: var(--wf-gold);
            text-transform: uppercase;
        }

        .wf-dropdown-user-email {
            font-size: 11px;
            color: var(--wf-text-muted);
            margin-top: 2px;
        }

        .wf-dropdown-section {
            padding: 8px 10px;
            border-bottom: 1px solid #F1F5F9;
        }

        .wf-dropdown-section:last-child {
            border-bottom: none;
        }

        .wf-dropdown-section-title {
            font-size: 10px;
            font-weight: 700;
            color: #94A3B8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
            padding-left: 6px;
        }

        .wf-dropdown-item-btn {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 6px 10px;
            font-size: 12px;
            color: #334155;
            border-radius: 6px;
            text-decoration: none;
            transition: all 0.15s;
        }

        .wf-dropdown-item-btn:hover {
            background: #F1F5F9;
            color: #0F172A;
        }

        .wf-dropdown-item-btn.text-danger {
            color: #DC2626;
            font-weight: 600;
        }

        .wf-dropdown-item-btn.text-danger:hover {
            background: #FEE2E2;
        }

        /* Modo Edición Switch en Dropdown Flotante */
        .wf-dropdown-editing-toggle-box {
            background: #F8FAFC;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            padding: 6px 10px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        /* Body Layout */
        .wf-body {
            flex: 1;
            display: flex;
            background: var(--wf-bg);
            position: relative;
        }

        .wf-main-content {
            flex: 1;
            padding: 28px;
            overflow-y: auto;
        }

        /* Pin Badges */
        .pin-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 28px;
            height: 28px;
            min-width: 28px;
            border-radius: 50%;
            background: #FFFFFF;
            color: #0F172A;
            font-size: 13px;
            font-weight: 700;
            border: 2px solid #334155;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            flex-shrink: 0;
            user-select: none;
        }

        .wf-input-wrap {
            display: flex;
            align-items: center;
            gap: 10px;
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
            top: 46px;
            left: 0;
            width: calc(100% - 40px);
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            z-index: 50;
            overflow: hidden;
        }

        .wf-dropdown-item {
            padding: 8px 14px;
            font-size: 12px;
            color: #334155;
            border-bottom: 1px solid #F1F5F9;
            cursor: pointer;
        }

        .wf-dropdown-item.active {
            background: #E2E8F0;
            font-weight: 600;
            color: #0F172A;
        }

        .wf-card {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.02);
        }

        .wf-label {
            display: block;
            font-size: 12px;
            font-weight: 600;
            color: #334155;
            margin-bottom: 6px;
        }

        .wf-input {
            width: 100%;
            height: 40px;
            padding: 8px 12px;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            font-size: 13px;
            color: var(--wf-text);
            outline: none;
            background: #FFFFFF;
        }

        textarea.wf-input {
            height: auto;
        }

        .bg-disabled {
            background: #F8FAFC !important;
            color: var(--wf-text-muted) !important;
        }

        .wf-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 40px;
            padding: 0 18px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            border: 1px solid transparent;
            text-decoration: none;
            white-space: nowrap;
        }

        .wf-btn-primary {
            background: var(--wf-navy-dark);
            color: #FFFFFF;
        }

        .wf-btn-outline {
            background: #FFFFFF;
            border-color: var(--wf-border);
            color: #334155;
        }

        .wf-btn-danger {
            background: #DC2626;
            color: #FFFFFF;
        }

        .wf-btn-sm {
            height: 32px;
            padding: 0 12px;
            font-size: 12px;
        }

        .wf-link {
            font-size: 13px;
            color: var(--wf-navy-dark);
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
        }

        .wf-table-wrap {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            overflow: hidden;
        }

        .wf-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 13px;
        }

        .wf-table th, .wf-table td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--wf-border);
            text-align: left;
            vertical-align: middle;
        }

        .wf-table th {
            background: #F8FAFC;
            font-weight: 600;
            color: var(--wf-text-muted);
            font-size: 11px;
            text-transform: uppercase;
        }

        .wf-badge {
            font-size: 11px;
            font-weight: 600;
            padding: 3px 8px;
            border-radius: 20px;
        }

        .status-active {
            background: #DEF7EC;
            color: #03543F;
        }

        .status-inactive {
            background: #F1F5F9;
            color: #64748B;
        }

        .wf-icon-danger {
            width: 52px;
            height: 52px;
            background: #FEE2E2;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto;
        }

        /* Grid de Cards de Cursos */
        .wf-cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 18px;
        }

        .wf-course-card {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            box-shadow: 0 2px 6px rgba(0,0,0,0.03);
        }

        .wf-course-card-thumb {
            height: 110px;
            background: linear-gradient(135deg, #0A2540, #1E3A8A);
            position: relative;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .wf-course-tag {
            position: absolute;
            top: 8px;
            left: 8px;
            background: rgba(255,255,255,0.9);
            color: var(--wf-navy-dark);
            font-size: 10px;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 4px;
            text-transform: uppercase;
        }

        .wf-course-card-body {
            padding: 16px;
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .wf-course-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--wf-navy-dark);
            margin-bottom: 6px;
            line-height: 1.3;
        }

        .wf-course-desc {
            font-size: 12px;
            color: var(--wf-text-muted);
            margin-bottom: 12px;
            line-height: 1.4;
            flex: 1;
        }

        .wf-course-meta {
            display: flex;
            flex-direction: column;
            gap: 3px;
            font-size: 11px;
            color: #475569;
            padding-top: 10px;
            border-top: 1px solid #F1F5F9;
        }

        .wf-course-card-footer {
            padding: 12px 16px;
            background: #F8FAFC;
            border-top: 1px solid var(--wf-border);
        }

        .wf-progress {
            height: 6px;
            background: #E2E8F0;
            border-radius: 3px;
            overflow: hidden;
        }

        .wf-progress-fill {
            height: 100%;
            background: var(--wf-gold);
            border-radius: 3px;
        }

        /* Banner de Curso Moodle & Tabs FCEQYN Virtual */
        .wf-course-header-banner {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            padding: 20px 24px 0px 24px;
        }

        .wf-course-main-title {
            font-size: 20px;
            font-weight: 700;
            color: #1E293B;
            letter-spacing: -0.3px;
        }

        .wf-course-nav-tabs {
            border-top: 1px solid #E2E8F0;
            margin-top: 14px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            overflow-x: hidden; /* Sin scroll horizontal */
            padding-bottom: 0px;
        }

        .wf-tab-btn {
            background: none;
            border: none;
            border-bottom: 3px solid transparent;
            padding: 10px 10px 8px 10px;
            font-size: 13px;
            font-weight: 500;
            color: #0284C7; /* Azul Moodle institucional */
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            text-decoration: none;
            transition: all 0.15s ease;
            white-space: nowrap;
        }

        .wf-tab-btn:hover {
            color: #0369A1;
            border-bottom-color: #BAE6FD;
            background: #F8FAFC;
            border-radius: 6px 6px 0 0;
        }

        .wf-tab-btn.active {
            color: #0F172A; /* Texto oscuro activo */
            font-weight: 700;
            border-bottom: 3px solid #0284C7; /* Línea azul activa Moodle FCEQYN */
        }

        /* Acordeón de Unidades Moodle */
        .wf-moodle-accordion {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .wf-unit-box {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
        }

        .wf-unit-box.active {
            border-color: var(--wf-gold);
            box-shadow: 0 0 0 1px var(--wf-gold);
        }

        .wf-unit-box.locked {
            background: #F8FAFC;
            opacity: 0.75;
        }

        .wf-unit-header {
            padding: 12px 18px;
            background: #F8FAFC;
            border-bottom: 1px solid var(--wf-border);
            font-size: 13px;
        }

        .wf-unit-body {
            padding: 14px 18px;
        }

        .wf-content-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .wf-subcontent-title {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--wf-text-muted);
            margin-bottom: 6px;
        }

        /* Modal Overlay Moodle-like */
        .wf-modal-box {
            background: #FFFFFF;
            border: 2px solid var(--wf-navy-dark);
            border-radius: 8px;
            box-shadow: 0 12px 32px rgba(0,0,0,0.15);
            overflow: hidden;
        }

        .wf-modal-header {
            background: var(--wf-navy-dark);
            color: #FFFFFF;
            padding: 14px 20px;
        }

        .wf-modal-header h4 {
            font-size: 14px;
            font-weight: 700;
        }

        .wf-modal-header p {
            color: #94A3B8;
        }

        .wf-close-btn {
            font-size: 16px;
            cursor: pointer;
            color: #94A3B8;
        }

        .wf-radio-card {
            display: block;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            padding: 10px;
            background: #FFFFFF;
            cursor: pointer;
        }

        .wf-radio-card.selected {
            border-color: var(--wf-navy-dark);
            background: #F1F5F9;
            font-weight: 600;
        }

        .wf-radio-title {
            font-size: 12px;
            font-weight: 700;
            color: var(--wf-navy-dark);
        }

        /* Switch Toggle */
        .wf-switch {
            position: relative;
            display: inline-block;
            width: 34px;
            height: 18px;
        }

        .wf-switch input { opacity: 0; width: 0; height: 0; }

        .wf-slider {
            position: absolute;
            cursor: pointer;
            top: 0; left: 0; right: 0; bottom: 0;
            background-color: #CBD5E1;
            transition: .2s;
            border-radius: 18px;
        }

        .wf-slider:before {
            position: absolute;
            content: "";
            height: 12px;
            width: 12px;
            left: 3px;
            bottom: 3px;
            background-color: white;
            transition: .2s;
            border-radius: 50%;
        }

        input:checked + .wf-slider {
            background-color: #059669;
        }

        input:checked + .wf-slider:before {
            transform: translateX(16px);
        }

        /* Panel de Trazabilidad Académica para Figma */
        .wf-traceability-panel {
            background: #F1F5F9;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            padding: 10px 16px;
            font-size: 11px;
            color: #334155;
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            align-items: center;
        }

        .wf-traceability-item {
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .wf-trace-tag {
            font-weight: 700;
            background: #E2E8F0;
            padding: 2px 6px;
            border-radius: 4px;
            color: #0F172A;
        }

        /* Layout Grid & Flex Utilities */
        .row { display: flex; flex-wrap: wrap; margin: -6px; }
        .g-0 { margin: 0 !important; }
        .g-0 > * { padding: 0 !important; }
        .col-12 { width: 100%; padding: 6px; }
        .col-md-12 { width: 100%; padding: 6px; }
        .col-md-8 { width: 66.666%; padding: 6px; }
        .col-md-7 { width: 58.333%; padding: 6px; }
        .col-md-6 { width: 50%; padding: 6px; }
        .col-md-5 { width: 41.666%; padding: 6px; }
        .col-md-4 { width: 33.333%; padding: 6px; }
        .col-md-3 { width: 25%; padding: 6px; }
        .col-md-2 { width: 16.666%; padding: 6px; }

        .d-flex { display: flex; }
        .d-inline-flex { display: inline-flex; }
        .flex-column { flex-direction: column; }
        .align-items-center { align-items: center; }
        .align-items-start { align-items: flex-start; }
        .align-items-end { align-items: flex-end; }
        .justify-content-between { justify-content: space-between; }
        .justify-content-center { justify-content: center; }
        .justify-content-end { justify-content: flex-end; }

        .gap-1 { gap: 4px; }
        .gap-2 { gap: 8px; }
        .gap-3 { gap: 12px; }
        .p-0 { padding: 0 !important; }
        .p-2 { padding: 8px; }
        .p-3 { padding: 14px; }
        .p-4 { padding: 20px; }
        .py-2 { padding-top: 8px; padding-bottom: 8px; }
        .px-3 { padding-left: 14px; padding-right: 14px; }
        .ps-3 { padding-left: 14px; }
        .pe-2 { padding-right: 8px; }
        .mt-1 { margin-top: 4px; }
        .mt-2 { margin-top: 8px; }
        .mt-3 { margin-top: 12px; }
        .mt-4 { margin-top: 16px; }
        .mb-1 { margin-bottom: 4px; }
        .mb-2 { margin-bottom: 8px; }
        .mb-3 { margin-bottom: 12px; }
        .mb-4 { margin-bottom: 16px; }
        .me-1 { margin-right: 4px; }
        .ms-1 { margin-left: 4px; }

        .text-end { text-align: right; }
        .text-center { text-align: center; }
        .text-start { text-align: left; }
        .w-100 { width: 100%; }
        .fw-bold { font-weight: 700; }
        .small { font-size: 11px; }
        .text-muted { color: var(--wf-text-muted); }
        .text-danger { color: #DC2626; }
        .text-success { color: #059669; }
        .text-navy { color: var(--wf-navy-dark); }
        .text-white { color: #FFFFFF; }
        .text-uppercase { text-transform: uppercase; }
        .text-decoration-none { text-decoration: none; }
        .cursor-move { cursor: grab; }
        .cursor-pointer { cursor: pointer; }

        .border { border: 1px solid var(--wf-border); }
        .border-bottom { border-bottom: 1px solid var(--wf-border); }
        .border-top { border-top: 1px solid var(--wf-border); }
        .border-end { border-right: 1px solid var(--wf-border); }
        .border-start { border-left: 1px solid var(--wf-border); }
        .rounded { border-radius: 6px; }
        .rounded-circle { border-radius: 50%; }
        .shadow-sm { box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
        .bg-white { background: #FFFFFF; }
        .bg-light { background: #F8FAFC; }
    </style>
</head>
<body>

    <!-- Buscador Izquierdo Agrupado por Módulos -->
    <div id="nav-sidebar">
        <div class="nav-header">
            <h2>IDÓNEOS ONLINE</h2>
            <p>${cus.length} Wireframes de Casos de Uso</p>
        </div>
        <div class="nav-search">
            <input type="text" id="searchInput" placeholder="Buscar CU (ej: CU-26, Glosario)..." onkeyup="filterCUs()">
        </div>
        <div id="moduleNavList">
            ${modules.map(m => `
                <div class="module-group">
                    <div class="module-header">
                        <span>${m.name}</span>
                    </div>
                    <ul class="module-list">
                        ${m.cus.map(c => `
                            <li class="nav-item">
                                <a href="#${c.id}">
                                    <span>${c.name}</span>
                                    <span class="cu-tag">${c.id}</span>
                                </a>
                            </li>
                        `).join('')}
                    </ul>
                </div>
            `).join('')}
        </div>
    </div>

    <!-- Viewport Central con las Pantallas Wireframe -->
    <div id="viewport">
`;

// Render each CU Screen
cus.forEach((cu, index) => {
  const roleInfo = getRoleInfo(cu.actors, cu.id);
  const dssMessage = dssMap[cu.id] || `operacion(${cu.id.toLowerCase()})`;

  html += `
    <div class="figure-wrapper" id="${cu.id}">
        <div class="screen-frame">
            
            <!-- Top Header Limpio con Trigger de Perfil y Dropdown Flotante Representativo -->
            <div class="wf-top-navbar">
                <div class="wf-brand">
                    <strong>IDÓNEOS ONLINE</strong>
                    <span class="divider">|</span>
                    <span class="screen-title">${cu.name}</span>
                </div>

                <!-- Menú de Usuario con Dropdown Flotante Interactivo (Click para alternar/desplegar) -->
                <div class="wf-user-menu-wrapper">
                    <div class="wf-user-trigger-pill" onclick="toggleUserDropdown(this)" title="Haz clic para abrir/cerrar el menú de perfil">
                        <div class="user-avatar-circle">${roleInfo.initials}</div>
                        <div class="d-flex flex-column text-start">
                            <span class="small fw-bold">${roleInfo.name}</span>
                            <span style="font-size: 10px; color: var(--wf-text-muted);">${roleInfo.role}</span>
                        </div>
                        ${icons.chevronDown("w-3 h-3 text-muted ms-1")}
                    </div>

                    <!-- Dropdown Flotante Superpuesto -->
                    <div class="wf-user-floating-dropdown">
                        <div class="wf-dropdown-user-header">
                            <div class="wf-dropdown-user-name">${roleInfo.name}</div>
                            <div class="wf-dropdown-user-role">${roleInfo.role}</div>
                            <div class="wf-dropdown-user-email">${roleInfo.email}</div>
                        </div>

                        ${roleInfo.dropdownSections.map(sec => `
                            <div class="wf-dropdown-section">
                                <div class="wf-dropdown-section-title">${sec.title}</div>
                                
                                ${sec.hasEditingToggle ? `
                                    <div class="wf-dropdown-editing-toggle-box">
                                        <span class="small fw-bold">Activar Edición</span>
                                        <label class="wf-switch">
                                            <input type="checkbox" ${sec.isEditingActive ? 'checked' : ''}>
                                            <span class="wf-slider round"></span>
                                        </label>
                                    </div>
                                ` : ''}

                                ${sec.items ? sec.items.map(item => `
                                    <a href="#${item.cu}" class="wf-dropdown-item-btn ${item.isDanger ? 'text-danger' : ''}">
                                        <span class="d-flex align-items-center gap-2">
                                            ${item.isDanger ? icons.arrowRightOnRectangle("w-3 h-3") : ''}
                                            ${item.label}
                                        </span>
                                    </a>
                                `).join('') : ''}
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>

            <!-- Área de Contenido Principal (A Ancho Completo sin barra lateral divisoria) -->
            <div class="wf-body">
                <div class="wf-main-content">
                    ${generateScreenContent(cu)}
                </div>
            </div>
        </div>

        <!-- Panel de Trazabilidad Académica para Figma / Profesor -->
        <div class="wf-traceability-panel">
            <div class="wf-traceability-item">
                <span class="wf-trace-tag">CU Real:</span>
                <strong>${cu.id}: ${cu.name}</strong>
            </div>
            <div class="wf-traceability-item">
                <span class="wf-trace-tag">Módulo:</span>
                <span>${cu.module}</span>
            </div>
            <div class="wf-traceability-item">
                <span class="wf-trace-tag">Actor(es):</span>
                <span>${cu.actors}</span>
            </div>
            <div class="wf-traceability-item">
                <span class="wf-trace-tag">DSS Asociado:</span>
                <code>${dssMessage}</code>
            </div>
        </div>

        <!-- Pie de Figura Académico -->
        <div class="figure-caption">
            <strong>Figura ${index + 1}.</strong> Caso de uso real para la interfaz de <em>${cu.name}</em> (${cu.id}).
        </div>
    </div>
  `;
});

// Closing tags & client-side filter
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

console.log(`Successfully updated wireframes with floating dropdown menu in ${outputPath}`);
