const http = require('http');

const baseUrl = 'http://localhost:8080';

const endpoints = [
  '/inicio',
  '/cursos/catalogo',
  '/acercaDe',
  '/seguridad/login',
  '/seguridad/registro',
  '/cursos',
  '/cursos/nuevo',
  '/cursos/categorias',
  '/cursos/cohortes',
  '/academico/unidades',
  '/academico/materiales',
  '/academico/glosario',
  '/inscripciones',
  '/evaluaciones/pools',
  '/evaluaciones/autoevaluaciones',
  '/vivo',
  '/ia/banco-preguntas',
  '/ia/resumen-unidad',
  '/seguridad/usuarios',
  '/seguridad/perfil',
  '/seguridad/sesiones',
  '/auditoria',
  '/configuracion'
];

async function checkUrl(urlPath) {
  return new Promise((resolve) => {
    http.get(baseUrl + urlPath, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        const hasNavbar = data.includes('wf-top-navbar');
        const hasError = data.includes('Whitelabel Error Page') || data.includes('TemplateInputException');
        resolve({
          path: urlPath,
          statusCode: res.statusCode,
          hasNavbar,
          hasError
        });
      });
    }).on('error', (err) => {
      resolve({
        path: urlPath,
        statusCode: 0,
        hasNavbar: false,
        hasError: true,
        error: err.message
      });
    });
  });
}

async function runTests() {
  console.log('🚀 Iniciando batería de pruebas sobre endpoints de Idóneos Online:\n');
  let passed = 0;
  let failed = 0;

  for (const ep of endpoints) {
    const res = await checkUrl(ep);
    if (res.statusCode === 200 || res.statusCode === 302) {
      console.log(`✅ [${res.statusCode}] ${res.path} - Navbar: ${res.hasNavbar ? 'OK' : 'N/A'} - Error: ${res.hasError ? 'FAIL' : 'NO'}`);
      passed++;
    } else {
      console.log(`❌ [${res.statusCode}] ${res.path} - Error al cargar`);
      failed++;
    }
  }

  console.log(`\n📊 RESULTADOS DEL TESTING:`);
  console.log(`- Exitosos: ${passed}`);
  console.log(`- Fallidos: ${failed}`);
}

runTests();
