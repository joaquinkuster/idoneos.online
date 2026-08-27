const http = require('http');

const baseUrl = 'http://localhost:8080';

// Cookie containers for authenticated roles
let alumnoCookie = '';
let docenteCookie = '';
let adminCookie = '';

function request(options, postData = null) {
  return new Promise((resolve) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          status: res.statusCode,
          headers: res.headers,
          body: data
        });
      });
    });
    req.on('error', err => resolve({ status: 0, error: err.message, body: '' }));
    if (postData) {
      req.write(postData);
    }
    req.end();
  });
}

async function loginUser(email, password) {
  const postData = `username=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;
  const res = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/seguridad/login',
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Content-Length': Buffer.byteLength(postData)
    }
  }, postData);

  const cookies = res.headers['set-cookie'];
  if (cookies) {
    return cookies.map(c => c.split(';')[0]).join('; ');
  }
  return '';
}

async function runRoleTests() {
  console.log(`🔐 INICIANDO AUTENTICACIÓN Y VALIDACIÓN DE PERFILES...\n`);

  alumnoCookie = await loginUser('alumno@correo.com', '123456');
  docenteCookie = await loginUser('fausto.spotorno@idoneos.online', '123456');
  adminCookie = await loginUser('admin@idoneos.online', '123456');

  console.log(`- Alumno Auth: ${alumnoCookie ? 'OK ✅' : 'FAIL ❌'}`);
  console.log(`- Docente Auth: ${docenteCookie ? 'OK ✅' : 'FAIL ❌'}`);
  console.log(`- Admin Auth: ${adminCookie ? 'OK ✅' : 'FAIL ❌'}\n`);

  const tests = [
    // Public / Visitor
    { name: 'Login Page', url: '/seguridad/login', cookie: '', expect: 200 },
    { name: 'Registro Page', url: '/seguridad/registro', cookie: '', expect: 200 },
    { name: 'Catálogo Cursos', url: '/cursos/catalogo', cookie: '', expect: 200 },

    // Alumno endpoints
    { name: 'Alumno - Mis Cursos', url: '/cursos/mis-cursos', cookie: alumnoCookie, expect: 200 },
    { name: 'Alumno - Progreso', url: '/inscripciones/progreso', cookie: alumnoCookie, expect: 200 },
    { name: 'Alumno - Historial Pagos', url: '/pago/historial', cookie: alumnoCookie, expect: 200 },
    { name: 'Alumno - Perfil', url: '/seguridad/perfil', cookie: alumnoCookie, expect: 200 },

    // Docente endpoints
    { name: 'Docente - Gestión Cursos', url: '/cursos', cookie: docenteCookie, expect: 200 },
    { name: 'Docente - Registrar Curso', url: '/cursos/nuevo', cookie: docenteCookie, expect: 200 },
    { name: 'Docente - Unidades', url: '/academico/unidades', cookie: docenteCookie, expect: 200 },
    { name: 'Docente - Materiales', url: '/academico/materiales', cookie: docenteCookie, expect: 200 },
    { name: 'Docente - Evaluaciones Pools', url: '/evaluaciones/pools', cookie: docenteCookie, expect: 200 },
    { name: 'Docente - IA Banco Preguntas', url: '/ia/banco-preguntas', cookie: docenteCookie, expect: 200 },

    // Admin endpoints
    { name: 'Admin - Categorías', url: '/cursos/categorias', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Cohortes', url: '/cursos/cohortes', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Usuarios', url: '/seguridad/usuarios', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Auditoría', url: '/auditoria', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Configuración', url: '/configuracion', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Reporte Alumnos', url: '/reportes/alumnos', cookie: adminCookie, expect: 200 },
    { name: 'Admin - Reporte Ingresos', url: '/reportes/ingresos', cookie: adminCookie, expect: 200 }
  ];

  let passed = 0;
  let failed = 0;

  for (const t of tests) {
    const res = await request({
      hostname: 'localhost',
      port: 8080,
      path: t.url,
      method: 'GET',
      headers: t.cookie ? { 'Cookie': t.cookie } : {}
    });

    const isWhitelabel = res.body.includes('Whitelabel Error Page') || res.body.includes('TemplateInputException');
    const isOk = res.status === t.expect && !isWhitelabel;

    if (isOk) {
      console.log(`✅ [${res.status}] ${t.name} -> ${t.url}`);
      passed++;
    } else {
      console.log(`❌ [${res.status}] ${t.name} -> ${t.url} (IsWhitelabel: ${isWhitelabel})`);
      failed++;
    }
  }

  console.log(`\n======================================================`);
  console.log(`📊 REPORTE DE VALIDACIÓN POR ROLES:`);
  console.log(`- Pruebas Ejecutadas: ${tests.length}`);
  console.log(`- Exitosas: ${passed}`);
  console.log(`- Fallidas: ${failed}`);
  console.log(`======================================================\n`);
}

runRoleTests();
