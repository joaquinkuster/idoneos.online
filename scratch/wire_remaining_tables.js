const fs = require('fs');

// 1. CU-11 (Buscar Cohorte)
let cu11 = fs.readFileSync('src/main/resources/templates/pages/cursos/cu-11-buscar-cohorte.html', 'utf8');
cu11 = cu11.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="coh : \${cohortes}">
    <td><strong style="color: #081426;" th:text="\${coh.programa != null ? coh.programa.curso.nombre : 'Curso'}">Curso</strong></td>
    <td th:text="\${#temporals.format(coh.fechaInicioInscripcion, 'dd/MM/yyyy') + ' al ' + #temporals.format(coh.fechaFinInscripcion, 'dd/MM/yyyy')}">Inscripción</td>
    <td th:text="\${coh.fechaInicioDictado != null ? #temporals.format(coh.fechaInicioDictado, 'dd/MM/yyyy') + ' al ' + #temporals.format(coh.fechaFinDictado, 'dd/MM/yyyy') : 'Modalidad Asincrónica'}">Dictado</td>
    <td th:text="\${coh.cupoMaximo != null ? coh.cupoMaximo : 'Ilimitado'}">50</td>
    <td><span class="wf-badge status-active" th:text="\${coh.baja ? 'Inactiva' : 'Activa'}">Activa</span></td>
    <td class="text-end">
      <div class="d-inline-flex gap-2">
        <a th:href="@{'/cursos/cohortes/' + \${coh.id} + '/editar'}" class="wf-btn wf-btn-sm wf-btn-outline"><i class="fa-solid fa-pen-to-square"></i></a>
        <form th:action="@{'/cursos/cohortes/' + \${coh.id} + '/baja'}" method="post" class="d-inline">
          <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Dar de baja esta cohorte?');"><i class="fa-solid fa-trash"></i></button>
        </form>
      </div>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(cohortes)}">
    <td colspan="6" class="text-center py-4 text-muted">No se encontraron cohortes registradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/cursos/cu-11-buscar-cohorte.html', cu11, 'utf8');

// 2. CU-23 (Buscar Cronograma)
let cu23 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-23-buscar-cronograma.html', 'utf8');
cu23 = cu23.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="c : \${cronogramas}">
    <td><strong style="color: #081426;" th:text="'#' + \${c.numeroOrden}">#1</strong></td>
    <td th:text="\${c.unidad.titulo}">Unidad</td>
    <td th:text="\${c.semanasDuracion + ' semanas'}">2 semanas</td>
  </tr>
  <tr th:if="\${#lists.isEmpty(cronogramas)}">
    <td colspan="3" class="text-center py-4 text-muted">No se configuró cronograma para este programa.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-23-buscar-cronograma.html', cu23, 'utf8');

// 3. CU-25 (Ver Participantes)
let cu25 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-25-ver-participantes.html', 'utf8');
cu25 = cu25.replace(/href=["']#["']/g, 'th:href="@{/academico/participantes}"');
cu25 = cu25.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="p : \${participantes}">
    <td><strong style="color: #081426;" th:text="\${p.nombreCompleto}">Nombre Completo</strong></td>
    <td><code th:text="\${p.email}">correo@idoneos.online</code></td>
    <td><span class="wf-badge status-active" th:text="\${p.rol != null ? p.rol.nombre : 'Alumno'}">Alumno</span></td>
    <td th:text="\${p.cohorte != null ? 'Cohorte #' + p.cohorte.id : 'General'}">Cohorte</td>
  </tr>
  <tr th:if="\${#lists.isEmpty(participantes)}">
    <td colspan="4" class="text-center py-4 text-muted">No hay participantes registrados en este curso.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-25-ver-participantes.html', cu25, 'utf8');

// 4. CU-35 (Buscar Consulta de Foro)
let cu35 = fs.readFileSync('src/main/resources/templates/pages/academico/cu-35-buscar-consulta-de-foro.html', 'utf8');
cu35 = cu35.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="c : \${consultas}">
    <td><strong style="color: #081426;" th:text="\${c.texto}">Texto de la consulta</strong></td>
    <td th:text="\${c.alumno != null ? c.alumno.usuario.nombreCompleto : 'Alumno'}">Alumno</td>
    <td th:text="\${#temporals.format(c.fecha, 'dd/MM/yyyy HH:mm')}">01/01/2026</td>
    <td class="text-end">
      <a th:href="@{'/academico/foro/' + \${c.id} + '/respuestas'}" class="wf-btn wf-btn-sm wf-btn-primary"><i class="fa-solid fa-comments me-1"></i> Ver Respuestas</a>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(consultas)}">
    <td colspan="4" class="text-center py-4 text-muted">No hay consultas de foro registradas en esta unidad.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/academico/cu-35-buscar-consulta-de-foro.html', cu35, 'utf8');

// 5. CU-39 (Buscar Respuesta de Foro)
if (fs.existsSync('src/main/resources/templates/pages/foro/cu-39-buscar-respuesta-de-foro.html')) {
  let cu39 = fs.readFileSync('src/main/resources/templates/pages/foro/cu-39-buscar-respuesta-de-foro.html', 'utf8');
  cu39 = cu39.replace(/href=["']#["']/g, 'th:href="@{/cursos}"');
  fs.writeFileSync('src/main/resources/templates/pages/foro/cu-39-buscar-respuesta-de-foro.html', cu39, 'utf8');
}

// 6. CU-46 (Buscar Pago)
let cu46 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-46-buscar-pago.html', 'utf8');
cu46 = cu46.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="p : \${pagos}">
    <td><strong style="color: #081426;" th:text="\${p.numeroComprobante != null ? p.numeroComprobante : 'TRANS-' + p.id}">COMP-001</strong></td>
    <td th:text="\${p.inscripcion.cohorte.programa.curso.nombre}">Curso</td>
    <td><strong class="text-success" th:text="'$' + \${#numbers.formatDecimal(p.monto, 1, 'POINT', 2, 'COMMA')}">$0.00</strong></td>
    <td th:text="\${#temporals.format(p.fecha, 'dd/MM/yyyy HH:mm')}">01/01/2026</td>
    <td><span class="wf-badge status-active" th:text="\${p.estadoPago != null ? p.estadoPago.nombre : 'Acreditado'}">Acreditado</span></td>
  </tr>
  <tr th:if="\${#lists.isEmpty(pagos)}">
    <td colspan="5" class="text-center py-4 text-muted">No se encontraron pagos registrados.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-46-buscar-pago.html', cu46, 'utf8');

// 7. CU-48 (Buscar Progreso)
let cu48 = fs.readFileSync('src/main/resources/templates/pages/inscripciones/cu-48-buscar-progreso.html', 'utf8');
cu48 = cu48.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="prog : \${progresos}">
    <td><strong style="color: #081426;" th:text="\${prog.inscripcion.alumno.usuario.nombreCompleto}">Alumno</strong></td>
    <td th:text="\${prog.unidad.titulo}">Unidad</td>
    <td><span th:class="\${prog.completada ? 'wf-badge status-active' : 'wf-badge status-inactive'}" th:text="\${prog.completada ? 'Completada' : 'Pendiente'}">Completada</span></td>
    <td th:text="\${prog.fechaCompletada != null ? #temporals.format(prog.fechaCompletada, 'dd/MM/yyyy') : '-'}">-</td>
  </tr>
  <tr th:if="\${#lists.isEmpty(progresos)}">
    <td colspan="4" class="text-center py-4 text-muted">No se encontraron registros de avance académico.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/inscripciones/cu-48-buscar-progreso.html', cu48, 'utf8');

// 8. CU-61 (Buscar Intento de Autoevaluación)
let cu61 = fs.readFileSync('src/main/resources/templates/pages/evaluaciones/cu-61-buscar-intento-de-autoevaluacion.html', 'utf8');
cu61 = cu61.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="intento : \${intentos}">
    <td><strong style="color: #081426;" th:text="\${intento.autoevaluacion.nombre}">Autoevaluación</strong></td>
    <td th:text="\${intento.inscripcion.alumno.usuario.nombreCompleto}">Alumno</td>
    <td><strong class="text-primary" th:text="\${intento.nota != null ? #numbers.formatDecimal(intento.nota, 1, 1) + ' / 10' : 'En Curso'}">10 / 10</strong></td>
    <td th:text="\${intento.fechaEntrega != null ? #temporals.format(intento.fechaEntrega, 'dd/MM/yyyy HH:mm') : 'Pendiente'}">Fecha</td>
  </tr>
  <tr th:if="\${#lists.isEmpty(intentos)}">
    <td colspan="4" class="text-center py-4 text-muted">No hay intentos registrados.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/evaluaciones/cu-61-buscar-intento-de-autoevaluacion.html', cu61, 'utf8');

// 9. CU-62 (Ver Calificaciones)
let cu62 = fs.readFileSync('src/main/resources/templates/pages/evaluaciones/cu-62-ver-calificaciones.html', 'utf8');
cu62 = cu62.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="cal : \${calificaciones}">
    <td><strong style="color: #081426;" th:text="\${cal.autoevaluacion.nombre}">Examen</strong></td>
    <td th:text="\${cal.autoevaluacion.unidad.titulo}">Unidad</td>
    <td><strong class="text-success" th:text="\${cal.nota != null ? #numbers.formatDecimal(cal.nota, 1, 1) : '-'}">10.0</strong></td>
    <td><span class="wf-badge status-active" th:text="\${cal.nota != null and cal.nota >= 7 ? 'Aprobado' : 'Reprobado'}">Aprobado</span></td>
  </tr>
  <tr th:if="\${#lists.isEmpty(calificaciones)}">
    <td colspan="4" class="text-center py-4 text-muted">No hay calificaciones registradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/evaluaciones/cu-62-ver-calificaciones.html', cu62, 'utf8');

// 10. CU-93 (Buscar Sesión)
let cu93 = fs.readFileSync('src/main/resources/templates/pages/seguridad/cu-93-buscar-sesion.html', 'utf8');
cu93 = cu93.replace(/<tbody>[\s\S]*?<\/tbody>/, `<tbody>
  <tr th:each="s : \${sesiones}">
    <td><strong style="color: #081426;" th:text="\${s.usuario.nombreCompleto}">Usuario</strong></td>
    <td th:text="\${s.dispositivo}">Navegador Web / Windows</td>
    <td><code th:text="\${s.ip}">127.0.0.1</code></td>
    <td th:text="\${#temporals.format(s.fechaInicio, 'dd/MM/yyyy HH:mm')}">Inicio</td>
    <td class="text-end">
      <form th:action="@{'/seguridad/sesiones/' + \${s.id} + '/eliminar'}" method="post" class="d-inline">
        <button type="submit" class="wf-btn wf-btn-sm wf-btn-outline text-danger" onclick="return confirm('¿Cerrar sesión forzosamente?');"><i class="fa-solid fa-power-off me-1"></i> Cerrar</button>
      </form>
    </td>
  </tr>
  <tr th:if="\${#lists.isEmpty(sesiones)}">
    <td colspan="5" class="text-center py-4 text-muted">No hay sesiones activas registradas.</td>
  </tr>
</tbody>`);
fs.writeFileSync('src/main/resources/templates/pages/seguridad/cu-93-buscar-sesion.html', cu93, 'utf8');

console.log('✅ Se cablearon todas las tablas estáticas restantes.');
