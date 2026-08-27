const http = require('http');

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

async function run() {
  const cookie = await loginUser('fausto.spotorno@idoneos.online', '123456');
  console.log('Docente authenticated, testing Modo Edición endpoints...');
  
  const resAula = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/academico/curso/1',
    method: 'GET',
    headers: { 'Cookie': cookie }
  });
  console.log(`CU-26 Aula Virtual (/academico/curso/1) -> Status: ${resAula.status}`);

  const resEdicion = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/academico/curso/1/edicion',
    method: 'GET',
    headers: { 'Cookie': cookie }
  });
  console.log(`CU-26b Modo Edición (/academico/curso/1/edicion) -> Status: ${resEdicion.status}`);
}

run();
