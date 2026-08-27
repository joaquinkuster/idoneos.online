const http = require('http');

const baseUrl = 'http://localhost:8080';

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

async function runFull100Audit() {
  console.log(`🚀 INICIANDO AUDITORÍA INTEGRAL DE 100 CASOS DE USO Y SERVICIOS...\n`);

  const alumnoCookie = await loginUser('alumno@correo.com', '123456');
  const docenteCookie = await loginUser('fausto.spotorno@idoneos.online', '123456');
  const adminCookie = await loginUser('admin@idoneos.online', '123456');

  const allEndpoints = [
    // MOD-F-01: Cursos (CU-01 a CU-14)
    { cu: 'CU-01', name: 'Buscar curso', url: '/cursos', cookie: docenteCookie },
    { cu: 'CU-02', name: 'Ver mis cursos', url: '/cursos/mis-cursos', cookie: alumnoCookie },
    { cu: 'CU-03', name: 'Registrar curso', url: '/cursos/nuevo', cookie: docenteCookie },
    { cu: 'CU-06', name: 'Explorar catálogo', url: '/cursos/catalogo', cookie: '' },
    { cu: 'CU-07', name: 'Buscar categoría', url: '/cursos/categorias', cookie: adminCookie },
    { cu: 'CU-08', name: 'Registrar categoría', url: '/cursos/categorias/nueva', cookie: adminCookie },
    { cu: 'CU-11', name: 'Buscar cohorte', url: '/cursos/cohortes', cookie: adminCookie },
    { cu: 'CU-12', name: 'Registrar cohorte', url: '/cursos/cohortes/nueva', cookie: adminCookie },

    // MOD-F-02: Gestión Académica (CU-15 a CU-42)
    { cu: 'CU-15', name: 'Buscar programa', url: '/academico/programas', cookie: docenteCookie },
    { cu: 'CU-19', name: 'Buscar unidad', url: '/academico/unidades', cookie: docenteCookie },
    { cu: 'CU-20', name: 'Agregar unidad', url: '/academico/unidades/nueva', cookie: docenteCookie },
    { cu: 'CU-23', name: 'Buscar cronograma', url: '/academico/cronograma', cookie: docenteCookie },
    { cu: 'CU-25', name: 'Ver participantes', url: '/academico/participantes', cookie: docenteCookie },
    { cu: 'CU-26', name: 'Acceder curso (Aula)', url: '/academico/aula', cookie: alumnoCookie },
    { cu: 'CU-27', name: 'Buscar material', url: '/academico/materiales', cookie: docenteCookie },
    { cu: 'CU-28', name: 'Subir material', url: '/academico/materiales/nuevo', cookie: docenteCookie },
    { cu: 'CU-31', name: 'Buscar término glosario', url: '/academico/glosario', cookie: alumnoCookie },
    { cu: 'CU-32', name: 'Registrar término', url: '/academico/glosario/nuevo', cookie: docenteCookie },
    { cu: 'CU-35', name: 'Buscar consulta foro', url: '/academico/foro', cookie: alumnoCookie },

    // MOD-F-03: Inscripciones y Pagos (CU-43 a CU-52)
    { cu: 'CU-43', name: 'Buscar inscripción', url: '/inscripciones', cookie: adminCookie },
    { cu: 'CU-44', name: 'Inscribir curso', url: '/inscripciones/nueva', cookie: alumnoCookie },
    { cu: 'CU-46', name: 'Buscar pago', url: '/pago/historial', cookie: adminCookie },
    { cu: 'CU-48', name: 'Buscar progreso', url: '/inscripciones/progreso', cookie: alumnoCookie },
    { cu: 'CU-49', name: 'Buscar descuento', url: '/inscripciones/descuentos', cookie: adminCookie },
    { cu: 'CU-50', name: 'Registrar descuento', url: '/inscripciones/descuentos/nuevo', cookie: adminCookie },

    // MOD-F-04: Evaluaciones (CU-53 a CU-64)
    { cu: 'CU-53', name: 'Buscar pool', url: '/evaluaciones/pools', cookie: docenteCookie },
    { cu: 'CU-54', name: 'Crear pool', url: '/evaluaciones/pools/nuevo', cookie: docenteCookie },
    { cu: 'CU-57', name: 'Buscar autoevaluación', url: '/evaluaciones/autoevaluaciones', cookie: docenteCookie },
    { cu: 'CU-58', name: 'Crear autoevaluación', url: '/evaluaciones/autoevaluaciones/nueva', cookie: docenteCookie },
    { cu: 'CU-61', name: 'Buscar intento', url: '/evaluaciones/intentos', cookie: docenteCookie },
    { cu: 'CU-62', name: 'Ver calificaciones', url: '/evaluaciones/calificaciones', cookie: docenteCookie },

    // MOD-F-05 y MOD-F-06: Clases en Vivo e IA (CU-65 a CU-80)
    { cu: 'CU-65', name: 'Buscar clase en vivo', url: '/vivo', cookie: docenteCookie },
    { cu: 'CU-66', name: 'Programar clase en vivo', url: '/vivo/programar', cookie: docenteCookie },
    { cu: 'CU-73', name: 'Generar banco preguntas IA', url: '/ia/banco-preguntas', cookie: docenteCookie },
    { cu: 'CU-74', name: 'Generar resumen unidad IA', url: '/ia/resumen-unidad', cookie: docenteCookie },
    { cu: 'CU-75', name: 'Generar presentación IA', url: '/ia/presentacion', cookie: docenteCookie },
    { cu: 'CU-76', name: 'Crear clon IA', url: '/ia/clon', cookie: docenteCookie },
    { cu: 'CU-77', name: 'Buscar clase con clon', url: '/ia/clon/clases', cookie: docenteCookie },

    // MOD-NF: Seguridad, Auditoría y Configuración (CU-81 a CU-99)
    { cu: 'CU-81', name: 'Registrarse', url: '/seguridad/registro', cookie: '' },
    { cu: 'CU-82', name: 'Buscar usuario', url: '/seguridad/usuarios', cookie: adminCookie },
    { cu: 'CU-83', name: 'Registrar usuario', url: '/seguridad/usuarios/nuevo', cookie: adminCookie },
    { cu: 'CU-86', name: 'Ver perfil', url: '/seguridad/perfil', cookie: alumnoCookie },
    { cu: 'CU-87', name: 'Editar perfil', url: '/seguridad/perfil/editar', cookie: alumnoCookie },
    { cu: 'CU-88', name: 'Registrar docente', url: '/seguridad/docentes/nuevo', cookie: adminCookie },
    { cu: 'CU-90', name: 'Iniciar sesión', url: '/seguridad/login', cookie: '' },
    { cu: 'CU-92', name: 'Recuperar contraseña', url: '/seguridad/recuperar-password', cookie: '' },
    { cu: 'CU-93', name: 'Buscar sesión', url: '/seguridad/sesiones', cookie: adminCookie },
    { cu: 'CU-95', name: 'Consultar auditoría', url: '/auditoria', cookie: adminCookie },
    { cu: 'CU-96', name: 'Informe de alumnos', url: '/reportes/alumnos', cookie: adminCookie },
    { cu: 'CU-97', name: 'Informe de ingresos', url: '/reportes/ingresos', cookie: adminCookie },
    { cu: 'CU-98', name: 'Consultar estadísticas', url: '/reportes/estadisticas', cookie: adminCookie },
    { cu: 'CU-99', name: 'Configurar parámetros', url: '/configuracion', cookie: adminCookie }
  ];

  let passed = 0;
  let failed = 0;

  for (const ep of allEndpoints) {
    const res = await request({
      hostname: 'localhost',
      port: 8080,
      path: ep.url,
      method: 'GET',
      headers: ep.cookie ? { 'Cookie': ep.cookie } : {}
    });

    const isWhitelabel = res.body.includes('Whitelabel Error Page') || res.body.includes('TemplateInputException');
    const isOk = res.status === 200 && !isWhitelabel;

    if (isOk) {
      console.log(`✅ [${res.status}] ${ep.cu}: ${ep.name} -> ${ep.url}`);
      passed++;
    } else {
      console.log(`❌ [${res.status}] ${ep.cu}: ${ep.name} -> ${ep.url} (Error: ${isWhitelabel ? 'Template/Whitelabel' : 'HTTP Status'})`);
      failed++;
    }
  }

  console.log(`\n======================================================`);
  console.log(`📊 REPORTE DE AUDITORÍA GLOBAL DE INTEGRACIÓN:`);
  console.log(`- Rutas Evaluadas: ${allEndpoints.length}`);
  console.log(`- Exitosas (HTTP 200 Sin Excepción): ${passed}`);
  console.log(`- Fallidas: ${failed}`);
  console.log(`- Tasa de Éxito: ${((passed / allEndpoints.length) * 100).toFixed(1)}%`);
  console.log(`======================================================\n`);
}

runFull100Audit();
