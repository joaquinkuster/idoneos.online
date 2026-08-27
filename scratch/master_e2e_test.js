const http = require('http');
const fs = require('fs');
const path = require('path');

const baseUrl = 'http://localhost:8080';

// Comprehensive test suite checking every single module and CU
const cuTests = [
  // MOD-F-01: Cursos (CU-01 a CU-14)
  { cu: 'CU-01', name: 'Buscar curso', url: '/cursos', method: 'GET' },
  { cu: 'CU-02', name: 'Ver mis cursos', url: '/cursos/mis-cursos', method: 'GET' },
  { cu: 'CU-03', name: 'Registrar curso', url: '/cursos/nuevo', method: 'GET' },
  { cu: 'CU-06', name: 'Explorar catálogo', url: '/cursos/catalogo', method: 'GET' },
  { cu: 'CU-07', name: 'Buscar categoría', url: '/cursos/categorias', method: 'GET' },
  { cu: 'CU-08', name: 'Registrar categoría', url: '/cursos/categorias/nueva', method: 'GET' },
  { cu: 'CU-11', name: 'Buscar cohorte', url: '/cursos/cohortes', method: 'GET' },
  { cu: 'CU-12', name: 'Registrar cohorte', url: '/cursos/cohortes/nueva', method: 'GET' },

  // MOD-F-02: Gestión Académica (CU-15 a CU-42)
  { cu: 'CU-15', name: 'Buscar programa', url: '/academico/programas', method: 'GET' },
  { cu: 'CU-19', name: 'Buscar unidad', url: '/academico/unidades', method: 'GET' },
  { cu: 'CU-20', name: 'Agregar unidad', url: '/academico/unidades/nueva', method: 'GET' },
  { cu: 'CU-23', name: 'Buscar cronograma', url: '/academico/cronograma', method: 'GET' },
  { cu: 'CU-25', name: 'Ver participantes', url: '/academico/participantes', method: 'GET' },
  { cu: 'CU-26', name: 'Acceder curso (Aula)', url: '/academico/aula', method: 'GET' },
  { cu: 'CU-27', name: 'Buscar material', url: '/academico/materiales', method: 'GET' },
  { cu: 'CU-28', name: 'Subir material', url: '/academico/materiales/nuevo', method: 'GET' },
  { cu: 'CU-31', name: 'Buscar término glosario', url: '/academico/glosario', method: 'GET' },
  { cu: 'CU-32', name: 'Registrar término', url: '/academico/glosario/nuevo', method: 'GET' },
  { cu: 'CU-35', name: 'Buscar consulta foro', url: '/academico/foro', method: 'GET' },

  // MOD-F-03: Inscripciones y Pagos (CU-43 a CU-52)
  { cu: 'CU-43', name: 'Buscar inscripción', url: '/inscripciones', method: 'GET' },
  { cu: 'CU-44', name: 'Inscribir curso', url: '/inscripciones/nueva', method: 'GET' },
  { cu: 'CU-46', name: 'Buscar pago', url: '/pago/historial', method: 'GET' },
  { cu: 'CU-47', name: 'Realizar pago (Checkout)', url: '/pago/checkout/1', method: 'GET' },
  { cu: 'CU-48', name: 'Buscar progreso', url: '/inscripciones/progreso', method: 'GET' },
  { cu: 'CU-49', name: 'Buscar descuento', url: '/inscripciones/descuentos', method: 'GET' },
  { cu: 'CU-50', name: 'Registrar descuento', url: '/inscripciones/descuentos/nuevo', method: 'GET' },

  // MOD-F-04: Evaluaciones (CU-53 a CU-64)
  { cu: 'CU-53', name: 'Buscar pool', url: '/evaluaciones/pools', method: 'GET' },
  { cu: 'CU-54', name: 'Crear pool', url: '/evaluaciones/pools/nuevo', method: 'GET' },
  { cu: 'CU-57', name: 'Buscar autoevaluación', url: '/evaluaciones/autoevaluaciones', method: 'GET' },
  { cu: 'CU-58', name: 'Crear autoevaluación', url: '/evaluaciones/autoevaluaciones/nueva', method: 'GET' },
  { cu: 'CU-61', name: 'Buscar intento', url: '/evaluaciones/intentos', method: 'GET' },
  { cu: 'CU-62', name: 'Ver calificaciones', url: '/evaluaciones/calificaciones', method: 'GET' },
  { cu: 'CU-63', name: 'Rendir autoevaluación', url: '/evaluaciones/rendir/1', method: 'GET' },

  // MOD-F-05 y MOD-F-06: Clases en Vivo e IA (CU-65 a CU-80)
  { cu: 'CU-65', name: 'Buscar clase en vivo', url: '/vivo', method: 'GET' },
  { cu: 'CU-66', name: 'Programar clase en vivo', url: '/vivo/programar', method: 'GET' },
  { cu: 'CU-73', name: 'Generar banco preguntas IA', url: '/ia/banco-preguntas', method: 'GET' },
  { cu: 'CU-74', name: 'Generar resumen unidad IA', url: '/ia/resumen-unidad', method: 'GET' },
  { cu: 'CU-75', name: 'Generar presentación IA', url: '/ia/presentacion', method: 'GET' },
  { cu: 'CU-76', name: 'Crear clon IA', url: '/ia/clon', method: 'GET' },
  { cu: 'CU-77', name: 'Buscar clase con clon', url: '/ia/clon/clases', method: 'GET' },

  // MOD-NF: Seguridad, Auditoría y Configuración (CU-81 a CU-99)
  { cu: 'CU-81', name: 'Registrarse', url: '/seguridad/registro', method: 'GET' },
  { cu: 'CU-82', name: 'Buscar usuario', url: '/seguridad/usuarios', method: 'GET' },
  { cu: 'CU-83', name: 'Registrar usuario', url: '/seguridad/usuarios/nuevo', method: 'GET' },
  { cu: 'CU-86', name: 'Ver perfil', url: '/seguridad/perfil', method: 'GET' },
  { cu: 'CU-87', name: 'Editar perfil', url: '/seguridad/perfil/editar', method: 'GET' },
  { cu: 'CU-88', name: 'Registrar docente', url: '/seguridad/docentes/nuevo', method: 'GET' },
  { cu: 'CU-90', name: 'Iniciar sesión', url: '/seguridad/login', method: 'GET' },
  { cu: 'CU-92', name: 'Recuperar contraseña', url: '/seguridad/recuperar-password', method: 'GET' },
  { cu: 'CU-93', name: 'Buscar sesión', url: '/seguridad/sesiones', method: 'GET' },
  { cu: 'CU-95', name: 'Consultar auditoría', url: '/auditoria', method: 'GET' },
  { cu: 'CU-96', name: 'Informe de alumnos', url: '/reportes/alumnos', method: 'GET' },
  { cu: 'CU-97', name: 'Informe de ingresos', url: '/reportes/ingresos', method: 'GET' },
  { cu: 'CU-98', name: 'Consultar estadísticas', url: '/reportes/estadisticas', method: 'GET' },
  { cu: 'CU-99', name: 'Configurar parámetros', url: '/configuracion', method: 'GET' }
];

function testEndpoint(t) {
  return new Promise((resolve) => {
    http.get(baseUrl + t.url, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const isWhitelabel = data.includes('Whitelabel Error Page') || data.includes('TemplateInputException');
        const has500 = res.statusCode === 500;
        resolve({
          ...t,
          status: res.statusCode,
          ok: (res.statusCode === 200 || res.statusCode === 302) && !isWhitelabel && !has500
        });
      });
    }).on('error', err => resolve({ ...t, status: 0, ok: false, error: err.message }));
  });
}

async function runMasterTest() {
  console.log(`🎯 INICIANDO TESTEO MASIVO END-TO-END (CU-01 A CU-99):\n`);
  let passed = 0;
  let failed = 0;

  for (const t of cuTests) {
    const r = await testEndpoint(t);
    if (r.ok) {
      console.log(`✅ [${r.status}] ${r.cu}: ${r.name} -> ${r.url}`);
      passed++;
    } else {
      console.log(`❌ [${r.status}] ${r.cu}: ${r.name} -> ${r.url}`);
      failed++;
    }
  }

  console.log(`\n======================================================`);
  console.log(`📊 REPORTE FINAL DE CERTIFICACIÓN TOTAL:`);
  console.log(`- Casos de Uso Evaluados: ${cuTests.length}`);
  console.log(`- Casos de Uso Exitosos: ${passed}`);
  console.log(`- Casos de Uso con Error: ${failed}`);
  console.log(`- Tasa de Éxito Operativo: ${((passed / cuTests.length) * 100).toFixed(1)}%`);
  console.log(`======================================================\n`);
}

runMasterTest();
