const fs = require('fs');
const path = require('path');

// 1. Analizar Controladores Spring
const controllersRoot = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'app', 'idoneos', 'controller');
const templatesRoot = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

const controllerRoutes = [];

function scanControllers(dir) {
  if (!fs.existsSync(dir)) return;
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      scanControllers(fullPath);
    } else if (entry.name.endsWith('.java')) {
      const content = fs.readFileSync(fullPath, 'utf8');
      
      // RequestMapping en clase
      let baseMapping = '';
      const classMapMatch = content.match(/@RequestMapping\((?:value\s*=\s*)?["']([^"']+)["']\)/);
      if (classMapMatch) {
        baseMapping = classMapMatch[1];
      }

      // Methods
      const methodMatches = content.matchAll(/@(GetMapping|PostMapping|RequestMapping)\((?:value\s*=\s*)?(?:\{)?["']([^"']+)["'](?:[^\)]*)\)/g);
      for (const m of methodMatches) {
        const httpMethod = m[1];
        const routePath = m[2];
        const fullRoute = (baseMapping + routePath).replace(/\/\//g, '/');
        
        // Buscar view name retornado
        controllerRoutes.push({
          controller: entry.name,
          httpMethod: httpMethod,
          route: fullRoute
        });
      }
    }
  }
}

scanControllers(controllersRoot);
console.log(`📡 Controladores escaneados: ${controllerRoutes.length} endpoints registrados.`);

// 2. Analizar las 100 plantillas HTML
const modules = [
  'cursos',
  'academico',
  'foro',
  'inscripciones',
  'evaluaciones',
  'ia_vivo',
  'seguridad',
  'auditoria',
  'reportes',
  'configuracion'
];

let totalTemplates = 0;
const auditResults = [];

modules.forEach(mod => {
  const dir = path.join(templatesRoot, mod);
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));

  files.forEach(file => {
    totalTemplates++;
    const fullPath = path.join(dir, file);
    const content = fs.readFileSync(fullPath, 'utf8');

    // Analizar variables Thymeleaf utilizadas en expresiones ${...}
    const thVars = [];
    const varMatches = content.matchAll(/\$\{(?:!?#strings\.[a-zA-Z]+\()?([a-zA-Z0-9_]+)(?:\.[a-zA-Z0-9_]+)*[^\}]*\}/g);
    for (const vm of varMatches) {
      const rootVar = vm[1];
      if (rootVar && !thVars.includes(rootVar) && !['strings', 'dates', 'numbers', 'httpServletRequest', '_csrf', 'param', 'session'].includes(rootVar)) {
        thVars.push(rootVar);
      }
    }

    // Analizar formularios th:action
    const formActions = [];
    const formMatches = content.matchAll(/th:action="@\{([^"'\?\}]+)/g);
    for (const fm of formMatches) {
      formActions.push(fm[1]);
    }

    // Analizar enlaces th:href
    const linkHrefs = [];
    const hrefMatches = content.matchAll(/th:href="@\{([^"'\?\}]+)/g);
    for (const hm of hrefMatches) {
      if (!linkHrefs.includes(hm[1])) {
        linkHrefs.push(hm[1]);
      }
    }

    auditResults.push({
      module: mod,
      file: file,
      thVars: thVars,
      formActions: formActions,
      linkHrefsCount: linkHrefs.length
    });
  });
});

console.log(`📊 Auditoría de ${totalTemplates} plantillas completada.`);
console.log(`Detalle de variables Thymeleaf detectadas:`);
const allVars = new Set();
auditResults.forEach(r => r.thVars.forEach(v => allVars.add(v)));
console.log(Array.from(allVars));

fs.writeFileSync(path.join(__dirname, 'audit_report.json'), JSON.stringify({
  controllers: controllerRoutes,
  templates: auditResults
}, null, 2));
