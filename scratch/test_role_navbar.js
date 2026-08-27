const http = require('http');

const baseUrl = 'http://localhost:8080';

// Follow redirects helper
function fetchUrl(urlPath, cookie = '') {
  return new Promise((resolve) => {
    http.get(baseUrl + urlPath, { headers: { 'Cookie': cookie } }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          path: urlPath,
          statusCode: res.statusCode,
          headers: res.headers,
          data
        });
      });
    }).on('error', err => resolve({ path: urlPath, statusCode: 0, error: err.message, data: '' }));
  });
}

async function testRoles() {
  console.log('🧪 Iniciando prueba de renderizado con Navbar Dinámico por Roles:\n');

  // 1. Visitante (sin autenticar)
  const visitanteRes = await fetchUrl('/seguridad/login');
  const hasVisitanteNavbar = visitanteRes.data.includes('wf-top-navbar');
  const hasIngresar = visitanteRes.data.includes('Ingresar') || visitanteRes.data.includes('Iniciar');
  console.log(`👤 Rol Visitante (Público):`);
  console.log(`   - Status: ${visitanteRes.statusCode}`);
  console.log(`   - Navbar presente: ${hasVisitanteNavbar ? 'SÍ' : 'NO'}`);
  console.log(`   - Botón Ingresar/Crear Cuenta: ${hasIngresar ? 'SÍ' : 'NO'}`);

  // 2. Comprobar páginas públicas con contenido
  const registroRes = await fetchUrl('/seguridad/registro');
  console.log(`\n📝 Vista Registro:`);
  console.log(`   - Status: ${registroRes.statusCode}`);
  console.log(`   - Navbar presente: ${registroRes.data.includes('wf-top-navbar') ? 'SÍ' : 'NO'}`);

  // 3. Comprobar renderizado de Cursos Públicos
  const catRes = await fetchUrl('/cursos/categorias');
  console.log(`\n📚 Vista Categorías de Cursos:`);
  console.log(`   - Status: ${catRes.statusCode}`);
  console.log(`   - Navbar presente: ${catRes.data.includes('wf-top-navbar') ? 'SÍ' : 'NO'}`);
  console.log(`   - Tabla con iteración: ${catRes.data.includes('tbody') ? 'SÍ' : 'NO'}`);
}

testRoles();
