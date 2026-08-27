const fs = require('fs');
const file = 'src/main/resources/templates/pages/seguridad/cu-82-buscar-usuario.html';
let content = fs.readFileSync(file, 'utf8');

// Connect search form
content = content.replace(/<div class="p-3 bg-light rounded border mb-4">[\s\S]*?<\/div>\s*<\/div>/, `<form th:action="@{/seguridad/usuarios}" method="get" class="p-3 bg-light rounded border mb-4">
  <div class="row g-3 align-items-end">
    <div class="col-md-9">
      <label class="wf-label">Buscar por Nombre, Apellido o Correo</label>
      <div class="wf-input-wrap">
        <input type="text" name="busqueda" th:value="\${busqueda}" class="wf-input" placeholder="Ej: Elena, Valenzuela, Joaquín, admin@idoneos.online...">
      </div>
    </div>
    <div class="col-md-3">
      <button type="submit" class="wf-btn wf-btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Buscar Usuarios</button>
    </div>
  </div>
</form>`);

// Replace table tbody with dynamic iteration
content = content.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="u : \${usuarios}">
    <td>
      <div class="d-flex align-items-center gap-2">
        <div class="user-avatar-circle" style="width: 32px; height: 32px; font-size: 11px; background: #081426; color: #E4BE6C; font-weight: 700; display: flex; align-items: center; justify-content: center; border-radius: 50%;" th:text="\${#strings.substring(u.nombre, 0, 1) + #strings.substring(u.apellido != null ? u.apellido : '', 0, 1)}">U</div>
        <div>
          <strong style="color: #081426; font-size: 13px;" th:text="\${u.nombreCompleto}">Nombre</strong>
        </div>
      </div>
    </td>
    <td><code style="font-size: 12px;" th:text="\${u.correo}">correo@idoneos.online</code></td>
    <td th:text="\${u.dni != null ? u.dni : 'Sin DNI'}">DNI</td>
    <td><span class="wf-badge status-active" th:text="\${u.rol != null ? u.rol.nombre : 'Sin Rol'}">Rol</span></td>
    <td><span th:class="\${u.baja ? 'text-danger fw-bold' : 'text-success fw-bold'}" th:text="\${u.baja ? 'Baja' : 'Activo'}">Activo</span></td>
    <td class="text-end">
      <div class="d-inline-flex align-items-center gap-2">
        <a th:href="@{'/seguridad/usuarios/' + \${u.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square me-1"></i> Editar</a>
        <form th:action="@{'/seguridad/usuarios/' + \${u.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" title="Baja"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
</tbody>`);

fs.writeFileSync(file, content, 'utf8');
console.log('✅ cu-82-buscar-usuario.html sincronizado con la BD con éxito!');
