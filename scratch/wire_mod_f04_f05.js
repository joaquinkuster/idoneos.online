const fs = require('fs');

// Wire CU-53 (Buscar Pools)
let cu53 = fs.readFileSync('src/main/resources/templates/pages/evaluaciones/cu-53-buscar-pool.html', 'utf8');
cu53 = cu53.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="p : \${pools}">
    <td><strong style="color: #081426;" th:text="\${p.nombre}">Nombre del Pool</strong></td>
    <td th:text="\${p.unidad != null ? p.unidad.titulo : 'Unidad General'}">Unidad</td>
    <td th:text="\${#temporals.format(p.fechaCreacion, 'dd/MM/yyyy')}">01/01/2026</td>
    <td><span class="wf-badge status-active" th:text="\${p.baja ? 'Inactivo' : 'Activo'}">Activo</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/evaluaciones/pools/' + \${p.id} + '/preguntas'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-list-check me-1"></i> Preguntas</a>
        <a th:href="@{'/evaluaciones/pools/' + \${p.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(pools)}">
    <td colspan="5" class="text-center py-4 text-muted">No se encontraron pools de preguntas registrados.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/evaluaciones/cu-53-buscar-pool.html', cu53, 'utf8');

// Wire CU-57 (Buscar Autoevaluaciones)
let cu57 = fs.readFileSync('src/main/resources/templates/pages/evaluaciones/cu-57-buscar-autoevaluacion.html', 'utf8');
cu57 = cu57.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="a : \${autoevaluaciones}">
    <td><strong style="color: #081426;" th:text="\${a.nombre}">Autoevaluación</strong></td>
    <td th:text="\${a.tiempoLimite + ' minutos'}">30 minutos</td>
    <td th:text="\${a.cantidadPreguntas}">10</td>
    <td th:text="\${#temporals.format(a.fechaApertura, 'dd/MM/yyyy') + ' - ' + (a.fechaCierre != null ? #temporals.format(a.fechaCierre, 'dd/MM/yyyy') : 'Sin cierre')}">Período</td>
    <td><span class="wf-badge status-active" th:text="\${a.baja ? 'Inactiva' : 'Activa'}">Activa</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/evaluaciones/autoevaluaciones/' + \${a.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
        <a th:href="@{'/evaluaciones/rendir/' + \${a.id}}" class="wf-btn wf-btn-sm wf-btn-primary"><i class="fa-solid fa-play me-1"></i> Rendir</a>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(autoevaluaciones)}">
    <td colspan="6" class="text-center py-4 text-muted">No se encontraron autoevaluaciones configuradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/evaluaciones/cu-57-buscar-autoevaluacion.html', cu57, 'utf8');

// Wire CU-65 (Clases en Vivo)
let cu65 = fs.readFileSync('src/main/resources/templates/pages/ia_vivo/cu-65-buscar-clase-en-vivo.html', 'utf8');
cu65 = cu65.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="clase : \${clases}">
    <td><strong style="color: #081426;" th:text="\${clase.titulo}">Título de la Clase</strong></td>
    <td th:text="\${#temporals.format(clase.fechaHora, 'dd/MM/yyyy HH:mm')}">01/01/2026 19:00</td>
    <td th:text="\${clase.duracionEstimada + ' min'}">90 min</td>
    <td><span class="badge bg-danger text-white" th:text="\${clase.estado != null ? clase.estado.nombre : 'Programada'}">Programada</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/vivo/' + \${clase.id} + '/sala'}" class="wf-btn wf-btn-sm wf-btn-primary"><i class="fa-solid fa-video me-1"></i> Ingresar</a>
        <a th:href="@{'/vivo/' + \${clase.id} + '/iniciar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-tower-broadcast me-1"></i> Transmitir</a>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(clases)}">
    <td colspan="5" class="text-center py-4 text-muted">No hay clases en vivo programadas para esta unidad.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/ia_vivo/cu-65-buscar-clase-en-vivo.html', cu65, 'utf8');

console.log('✅ MOD-F-04 Evaluaciones y MOD-F-05 Clases en Vivo cableado dinámicamente con éxito.');
