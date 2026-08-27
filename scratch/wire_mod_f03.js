const fs = require('fs');

// Wire CU-43 (Buscar Inscripción)
let cu43 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-43-buscar-inscripcion.html', 'utf8');
cu43 = cu43.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="i : \${inscripciones}">
    <td>
      <strong style="color: #081426; font-size: 13px;" th:text="\${i.alumno.usuario.nombreCompleto}">Alumno</strong>
      <div class="small text-muted" th:text="\${i.alumno.usuario.email}">correo@idoneos.online</div>
    </td>
    <td>
      <strong th:text="\${i.cohorte.programa.curso.nombre}">Curso</strong>
      <div class="small text-muted" th:text="'Cohorte #' + \${i.cohorte.id}">Cohorte</div>
    </td>
    <td th:text="\${#temporals.format(i.fecha, 'dd/MM/yyyy')}">01/01/2026</td>
    <td th:text="\${#temporals.format(i.fechaVencimientoAcceso, 'dd/MM/yyyy')}">01/05/2026</td>
    <td><span class="wf-badge status-active" th:text="\${i.baja ? 'Baja' : 'Vigente'}">Vigente</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:if="\${i.numeroCertificado != null}" th:href="@{'/inscripciones/' + \${i.id} + '/certificado'}" class="wf-btn wf-btn-sm wf-btn-outline text-success" title="Certificado"><i class="fa-solid fa-award"></i></a>
        <form th:action="@{'/inscripciones/' + \${i.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Dar de baja esta inscripción?');" title="Baja"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(inscripciones)}">
    <td colspan="6" class="text-center py-4 text-muted">No se encontraron inscripciones registradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-43-buscar-inscripcion.html', cu43, 'utf8');

// Wire CU-44 (Inscribir Curso)
let cu44 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-44-inscribir-curso.html', 'utf8');
cu44 = cu44.replace(/<form[^>]*>/, `<form th:action="@{'/pago/checkout/' + \${curso.id}}" method="get">`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-44-inscribir-curso.html', cu44, 'utf8');

// Wire CU-47 (Realizar Pago)
let cu47 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-47-realizar-pago.html', 'utf8');
cu47 = cu47.replace(/<form[^>]*>/, `<form th:action="@{'/pago/procesar/' + \${curso.id}}" method="post">`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-47-realizar-pago.html', cu47, 'utf8');

// Wire CU-49 (Descuentos)
let cu49 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-49-buscar-descuento.html', 'utf8');
cu49 = cu49.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="d : \${descuentos}">
    <td><strong style="color: #081426; font-size: 13px;" th:text="\${d.nombre}">Descuento</strong></td>
    <td><strong class="text-success" th:text="\${d.porcentaje} + '%'">15%</strong></td>
    <td th:text="\${#temporals.format(d.vigenciaDesde, 'dd/MM/yyyy') + ' - ' + #temporals.format(d.vigenciaHasta, 'dd/MM/yyyy')}">Vigencia</td>
    <td th:text="\${d.cantidadUsada + ' / ' + d.cantidadLimite}">0 / 100</td>
    <td><span class="wf-badge status-active" th:text="\${d.baja ? 'Inactivo' : 'Activo'}">Activo</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/inscripciones/descuentos/' + \${d.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(descuentos)}">
    <td colspan="6" class="text-center py-4 text-muted">No se encontraron descuentos vigentes.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-49-buscar-descuento.html', cu49, 'utf8');

console.log('✅ MOD-F-03 Inscripciones y Pagos cableado dinámicamente con éxito.');
