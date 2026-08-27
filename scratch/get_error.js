const http = require('http');

http.get('http://localhost:8080/cursos/catalogo', (res) => {
  let data = '';
  res.on('data', c => data += c);
  res.on('end', () => {
    console.log('Status:', res.statusCode);
    const match = data.match(/Exception:[\s\S]{1,500}/i) || data.match(/There was an unexpected error \(type=[^)]+\)[\s\S]{1,500}/i);
    if (match) {
      console.log('Error snippet:', match[0]);
    } else {
      console.log('Body preview:', data.substring(0, 500));
    }
  });
});
