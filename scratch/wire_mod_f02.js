const fs = require('fs');

// Wire CU-19 (Buscar Unidades)
let cu19 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-19-buscar-unidad.html', 'utf8');
cu19 = cu19.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="u : \${unidades}">
    <td><strong style="color: #081426; font-size: 14px;" th:text="\${u.titulo}">Título de la Unidad</strong></td>
    <td th:text="\${u.descripcion != null ? u.descripcion : 'Sin descripción'}">Descripción</td>
    <td><span class="wf-badge status-active" th:text="\${u.baja ? 'Inactiva' : 'Activa'}">Activa</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/academico/materiales?unidadId=' + \${u.id}}" class="wf-btn wf-btn-sm wf-btn-outline" title="Materiales"><i class="fa-solid fa-folder-open me-1"></i> Materiales</a>
        <a th:href="@{'/academico/glosario?unidadId=' + \${u.id}}" class="wf-btn wf-btn-sm wf-btn-outline" title="Glosario"><i class="fa-solid fa-book-bookmark me-1"></i> Glosario</a>
        <a th:href="@{'/academico/unidades/' + \${u.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(unidades)}">
    <td colspan="4" class="text-center py-4 text-muted">No se encontraron unidades registradas para este programa.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-19-buscar-unidad.html', cu19, 'utf8');

// Wire CU-20 (Agregar Unidad)
let cu20 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-20-agregar-unidad.html', 'utf8');
cu20 = cu20.replace(/<form[^>]*>/, `<form th:action="@{/academico/unidades/guardar}" method="post">`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-20-agregar-unidad.html', cu20, 'utf8');

// Wire CU-27 (Buscar Materiales)
let cu27 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-27-buscar-material.html', 'utf8');
cu27 = cu27.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="m : \${materiales}">
    <td><strong style="color: #081426;" th:text="\${m.titulo}">Título del Material</strong></td>
    <td><span class="badge bg-light text-dark border" th:text="\${m.tipoMaterial != null ? m.tipoMaterial.nombre : 'General'}">Tipo</span></td>
    <td th:text="\${m.autor != null ? m.autor : 'Idóneos Online'}">Autor</td>
    <td><span th:class="\${m.oculto ? 'wf-badge status-inactive' : 'wf-badge status-active'}" th:text="\${m.oculto ? 'Oculto' : 'Publicado'}">Publicado</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/academico/materiales/' + \${m.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
        <form th:action="@{'/academico/materiales/' + \${m.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Dar de baja este material?');"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(materiales)}">
    <td colspan="5" class="text-center py-4 text-muted">No se encontraron materiales cargados en esta unidad.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-27-buscar-material.html', cu27, 'utf8');

// Wire CU-31 (Glosario)
let cu31 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-31-buscar-termino-de-glosario.html', 'utf8');
cu31 = cu31.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="t : \${terminos}">
    <td><strong style="color: #081426; font-size: 14px;" th:text="\${t.termino}">Término</strong></td>
    <td th:text="\${t.definicion}">Definición técnica del término financiero...</td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/academico/glosario/' + \${t.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
        <form th:action="@{'/academico/glosario/' + \${t.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Eliminar este término?');"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(terminos)}">
    <td colspan="3" class="text-center py-4 text-muted">No hay términos registrados en el glosario de esta unidad.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-31-buscar-termino-de-glosario.html', cu31, 'utf8');

console.log('✅ MOD-F-02 Gestión Académica cableado dinámicamente con éxito.');
