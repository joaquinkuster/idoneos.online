const fs = require('fs');

// Wire CU-82 (Buscar Usuario)
let cu82 = fs.readFileSync('src/main/resources/templates/pages/seguridad/cu-82-buscar-usuario.html', 'utf8');
cu82 = cu82.replace(/<form[^>]*>/, `<form th:action="@{/seguridad/usuarios}" method="get">`);
cu82 = cu82.replace(/<input type="text" class="wf-input" placeholder="Buscar por nombre, email o DNI...">/, `<input type="text" name="busqueda" th:value="\${busqueda}" class="wf-input" placeholder="Buscar por nombre, email o DNI...">`);
cu82 = cu82.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="u : \${usuarios}">
    <td>
      <strong style="color: #081426; font-size: 13px;" th:text="\${u.nombreCompleto}">Nombre</strong>
    </td>
    <td><code th:text="\${u.email}">correo@idoneos.online</code></td>
    <td th:text="\${u.dni}">12345678</td>
    <td><span class="wf-badge status-active" th:text="\${u.rol != null ? u.rol.nombre : 'Usuario'}">Rol</span></td>
    <td><span th:class="\${u.baja ? 'text-danger fw-bold' : 'text-success fw-bold'}" th:text="\${u.baja ? 'Baja' : 'Activo'}">Activo</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/seguridad/usuarios/' + \${u.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
        <form th:action="@{'/seguridad/usuarios/' + \${u.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Dar de baja a este usuario?');"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(usuarios)}">
    <td colspan="6" class="text-center py-4 text-muted">No se encontraron usuarios registrados.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/seguridad/cu-82-buscar-usuario.html', cu82, 'utf8');

// Wire CU-95 (Auditoría)
let cu95 = fs.readFileSync('src/main/resources/templates/pages/auditoria/cu-95-consultar-auditoria.html', 'utf8');
cu95 = cu95.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="a : \${registros}">
    <td th:text="\${#temporals.format(a.fechaHora, 'dd/MM/yyyy HH:mm:ss')}">01/01/2026 12:00:00</td>
    <td><strong th:text="\${a.usuario != null ? a.usuario.nombreCompleto : 'Sistema'}">Usuario</strong></td>
    <td><span class="wf-badge status-active" th:text="\${a.tipoAuditoria != null ? a.tipoAuditoria.nombre : 'ACCIÓN'}">ACCIÓN</span></td>
    <td th:text="\${a.entidadAfectada + ' #' + a.idAfectado}">Entidad</td>
    <td><code th:text="\${a.ipUsuario}">127.0.0.1</code></td>
  </tr>
  <tr th:if="\${#lists.isEmpty(registros)}">
    <td colspan="5" class="text-center py-4 text-muted">No hay eventos de auditoría registrados.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/auditoria/cu-95-consultar-auditoria.html', cu95, 'utf8');

// Wire CU-99 (Configuración)
let cu99 = fs.readFileSync('src/main/resources/templates/pages/configuracion/cu-99-configurar-parametros.html', 'utf8');
cu99 = cu99.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="p : \${parametros}">
    <td><code style="font-size: 13px; font-weight: 700;" th:text="\${p.clave}">CLAVE</code></td>
    <td>
      <form th:action="@{/configuracion/guardar}" method="post" class="d-flex gap-2">
        <input type="hidden" name="clave" th:value="\${p.clave}">
        <input type="text" name="valor" th:value="\${p.valor}" class="wf-input" style="max-width: 320px;">
        <button type="submit" class="wf-btn wf-btn-sm wf-btn-primary"><i class="fa-solid fa-save"></i> Guardar</button>
      </form>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(parametros)}">
    <td colspan="2" class="text-center py-4 text-muted">No hay parámetros de configuración disponibles.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/configuracion/cu-99-configurar-parametros.html', cu99, 'utf8');

console.log('✅ MOD-NF Seguridad, Auditoría y Configuración cableado dinámicamente con éxito.');
