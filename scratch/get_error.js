const http = require('http');

async function get500() {
  const postData = `username=${encodeURIComponent('fausto.spotorno@idoneos.online')}&password=123456`;
  const loginRes = await new Promise(resolve => {
    const req = http.request({
      hostname: 'localhost',
      port: 8080,
      path: '/seguridad/login',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(postData)
      }
    }, res => {
      const cookie = res.headers['set-cookie'] ? res.headers['set-cookie'].map(c => c.split(';')[0]).join('; ') : '';
      resolve(cookie);
    });
    req.write(postData);
    req.end();
  });

  http.get({
    hostname: 'localhost',
    port: 8080,
    path: '/academico/curso/1',
    headers: { 'Cookie': loginRes }
  }, res => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => console.log('RESPONSE BODY:\n', data.slice(0, 1500)));
  });
}

get500();
