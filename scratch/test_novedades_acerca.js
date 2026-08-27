const http = require('http');

function test(url) {
  return new Promise((resolve) => {
    http.get(url, (res) => {
      console.log(`${url} -> Status: ${res.statusCode}`);
      resolve(res.statusCode);
    }).on('error', (err) => {
      console.log(`${url} -> Error: ${err.message}`);
      resolve(null);
    });
  });
}

async function run() {
  await test('http://localhost:8080/acercaDe');
  await test('http://localhost:8080/novedades');
}

run();
