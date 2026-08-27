const fs = require('fs');
const path = require('path');

const templatesRoot = path.join(__dirname, '..', 'src', 'main', 'resources', 'templates', 'pages');

// Mapeo exhaustivo de CU -> Ruta URL Spring MVC
const cuRouteMap = {
  // MOD-F-01: Módulo de Cursos
  'CU-01': '/cursos',
  'CU-02': '/cursos/mis-cursos',
  'CU-03': '/cursos/nuevo',
  'CU-04': '/cursos',
  'CU-05': '/cursos',
  'CU-06': '/cursos/catalogo',
  'CU-07': '/cursos/categorias',
  'CU-08': '/cursos/categorias/nueva',
  'CU-09': '/cursos/categorias',
  'CU-10': '/cursos/categorias',
  'CU-11': '/cursos/cohortes',
  'CU-12': '/cursos/cohortes/nueva',
  'CU-13': '/cursos/cohortes',
  'CU-14': '/cursos/cohortes',

  // MOD-F-02: Módulo de Gestión Académica
  'CU-15': '/academico/programas',
  'CU-16': '/academico/programas/nuevo',
  'CU-17': '/academico/programas',
  'CU-18': '/academico/programas',
  'CU-19': '/academico/unidades',
  'CU-20': '/academico/unidades/nueva',
  'CU-21': '/academico/unidades',
  'CU-22': '/academico/unidades',
  'CU-23': '/academico/cronograma',
  'CU-24': '/academico/cronograma',
  'CU-25': '/academico/participantes',
  'CU-26': '/academico/aula',
  'CU-26b': '/academico/aula?modoEdicion=true',
  'CU-27': '/academico/materiales',
  'CU-28': '/academico/materiales/nuevo',
  'CU-29': '/academico/materiales',
  'CU-30': '/academico/materiales',
  'CU-31': '/academico/glosario',
  'CU-32': '/academico/glosario/nuevo',
  'CU-33': '/academico/glosario',
  'CU-34': '/academico/glosario',
  'CU-35': '/academico/consultas',
  'CU-36': '/academico/consultas/nueva',
  'CU-37': '/academico/consultas',
  'CU-38': '/academico/consultas',
  'CU-39': '/academico/respuestas',
  'CU-40': '/academico/respuestas/nueva',
  'CU-41': '/academico/respuestas',
  'CU-42': '/academico/respuestas',

  // MOD-F-03: Módulo de Inscripciones
  'CU-43': '/inscripciones',
  'CU-44': '/inscripciones/nueva',
  'CU-45': '/inscripciones',
  'CU-46': '/inscripciones/pagos',
  'CU-47': '/inscripciones/pagar',
  'CU-48': '/inscripciones/progreso',
  'CU-49': '/inscripciones/descuentos',
  'CU-50': '/inscripciones/descuentos/nuevo',
  'CU-51': '/inscripciones/descuentos',
  'CU-52': '/inscripciones/descuentos',

  // MOD-F-04: Módulo de Evaluaciones
  'CU-53': '/evaluaciones/pools',
  'CU-54': '/evaluaciones/pools/nuevo',
  'CU-55': '/evaluaciones/pools',
  'CU-56': '/evaluaciones/pools',
  'CU-57': '/evaluaciones/autoevaluaciones',
  'CU-58': '/evaluaciones/autoevaluaciones/nueva',
  'CU-59': '/evaluaciones/autoevaluaciones',
  'CU-60': '/evaluaciones/autoevaluaciones',
  'CU-61': '/evaluaciones/intentos',
  'CU-62': '/evaluaciones/calificaciones',
  'CU-63': '/evaluaciones/autoevaluaciones',
  'CU-64': '/evaluaciones/intentos',

  // MOD-F-05: Módulo de Clases en Vivo
  'CU-65': '/clases-vivo',
  'CU-66': '/clases-vivo/nueva',
  'CU-67': '/clases-vivo',
  'CU-68': '/clases-vivo',
  'CU-69': '/clases-vivo',
  'CU-70': '/clases-vivo',
  'CU-71': '/clases-vivo',
  'CU-72': '/clases-vivo',

  // MOD-F-06: Módulo de Generación de Contenido con IA
  'CU-73': '/ia/banco-preguntas',
  'CU-74': '/ia/resumen-unidad',
  'CU-75': '/ia/presentacion-unidad',
  'CU-76': '/ia/clon/nuevo',
  'CU-77': '/ia/clon/clases',
  'CU-78': '/ia/clon/generar-clase',
  'CU-79': '/ia/clon/clases',
  'CU-80': '/ia/clon/clases',

  // MOD-NF-01: Módulo de Usuarios y Notificaciones
  'CU-81': '/seguridad/registro',
  'CU-82': '/seguridad/usuarios',
  'CU-83': '/seguridad/usuarios/nuevo',
  'CU-84': '/seguridad/usuarios',
  'CU-85': '/seguridad/usuarios',
  'CU-86': '/seguridad/perfil',
  'CU-87': '/seguridad/perfil/editar',
  'CU-88': '/seguridad/docentes/nuevo',
  'CU-89': '/seguridad/docentes',
  'CU-90': '/seguridad/login',
  'CU-91': '/seguridad/logout',
  'CU-92': '/seguridad/recuperar-contrasena',
  'CU-93': '/seguridad/sesiones',
  'CU-94': '/seguridad/sesiones',

  // MOD-NF-02: Módulo de Auditoría
  'CU-95': '/auditoria',

  // MOD-NF-03: Módulo de Reportes y Estadísticas
  'CU-96': '/reportes/alumnos',
  'CU-97': '/reportes/ingresos',
  'CU-98': '/reportes/estadisticas',

  // MOD-NF-04: Módulo de Configuración
  'CU-99': '/configuracion'
};

const modules = [
  'cursos',
  'academico',
  'foro',
  'inscripciones',
  'evaluaciones',
  'ia_vivo',
  'seguridad',
  'auditoria',
  'reportes',
  'configuracion'
];

let totalReplaced = 0;
let filesModified = 0;

modules.forEach(mod => {
  const dir = path.join(templatesRoot, mod);
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.html'));

  files.forEach(file => {
    const fullPath = path.join(dir, file);
    let content = fs.readFileSync(fullPath, 'utf8');
    let modified = false;

    // 1. Reemplazar enlaces mockeados href="#CU-XX" por th:href="@{/ruta}"
    content = content.replace(/href="#(CU-[0-9a-zA-Z_]+)"/g, (match, cuKey) => {
      const route = cuRouteMap[cuKey];
      if (route) {
        modified = true;
        totalReplaced++;
        return `th:href="@{${route}}"`;
      }
      return match;
    });

    // 2. Dinamizar datos de usuario en la barra superior si existe
    if (content.includes('Admin General') || content.includes('admin@idoneos.online')) {
      content = content.replace(/<div class="wf-dropdown-user-name">.*?<\/div>/, '<div class="wf-dropdown-user-name" th:text="${usuario != null ? usuario.nombreCompleto : \'Admin General\'}">Admin General</div>');
      content = content.replace(/<div class="wf-dropdown-user-role">.*?<\/div>/, '<div class="wf-dropdown-user-role" th:text="${usuario != null and usuario.rol != null ? usuario.rol.nombre : \'Administrador\'}">Administrador</div>');
      content = content.replace(/<div class="wf-dropdown-user-email">.*?<\/div>/, '<div class="wf-dropdown-user-email" th:text="${usuario != null ? usuario.correo : \'admin@idoneos.online\'}">admin@idoneos.online</div>');
      content = content.replace(/<span style="font-size: 11px; font-weight: 700;">Admin General<\/span>/, '<span style="font-size: 11px; font-weight: 700;" th:text="${usuario != null ? usuario.nombreCompleto : \'Admin General\'}">Admin General</span>');
      content = content.replace(/<div class="user-avatar-circle">AG<\/div>/, '<div class="user-avatar-circle" th:text="${usuario != null ? #strings.substring(usuario.nombre,0,1) + #strings.substring(usuario.apellido,0,1) : \'AG\'}">AG</div>');
      modified = true;
    }

    if (modified) {
      filesModified++;
      fs.writeFileSync(fullPath, content, 'utf8');
    }
  });
});

console.log(`✅ ¡TRANSFORMACIÓN COMPLETA FINALIZADA!`);
console.log(`Archivos modificados: ${filesModified}`);
console.log(`Total enlaces mockeados reemplazados por rutas Spring Thymeleaf: ${totalReplaced}`);
