const http = require('http');

function get(url) {
  return new Promise((resolve) => {
    http.get('http://localhost:8080' + url, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          url,
          status: res.statusCode,
          hasException: data.includes('Exception') || data.includes('Whitelabel Error Page'),
          length: data.length,
          bodySnippet: data.substring(0, 300)
        });
      });
    }).on('error', err => resolve({ url, status: 0, error: err.message }));
  });
}

async function run() {
  console.log('Testing /seguridad/login...');
  const res = await get('/seguridad/login');
  console.log('Status:', res.status);
  console.log('Has exception:', res.hasException);
  console.log('Body snippet:\n', res.bodySnippet);
}

run();
