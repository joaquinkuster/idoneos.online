package com.app.idoneos.config;

import com.app.idoneos.model.Auditoria;
import com.app.idoneos.model.TipoAccionAuditoria;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.modulo_auditoria.AuditoriaRepository;
import com.app.idoneos.repository.modulo_auditoria.TipoAccionAuditoriaRepository;
import com.app.idoneos.repository.modulo_usuarios.UsuarioRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Aspecto de Spring AOP para registrar de forma automática la auditoría
 * de acciones críticas (Crear, Modificar, Eliminar) sobre entidades principales.
 */
@Aspect
@Component
public class AuditoriaAspect {

    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private TipoAccionAuditoriaRepository tipoAccionRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @AfterReturning(pointcut = "execution(* com.app.idoneos.service..*.guardar*(..))", returning = "result")
    public void auditarGuardar(JoinPoint joinPoint, Object result) {
        registrarAuditoria("Crear", result);
    }

    @AfterReturning(pointcut = "execution(* com.app.idoneos.service..*.modificar*(..))", returning = "result")
    public void auditarModificar(JoinPoint joinPoint, Object result) {
        registrarAuditoria("Modificar", result);
    }

    @AfterReturning(pointcut = "execution(* com.app.idoneos.service..*.borrar*(..))")
    public void auditarBorrar(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            registrarAuditoria("Eliminar", args[0]);
        }
    }

    private void registrarAuditoria(String accionNombre, Object entidad) {
        try {
            if (entidad == null) return;

            TipoAccionAuditoria tipo = tipoAccionRepository.findByNombre(accionNombre)
                    .orElseGet(() -> tipoAccionRepository.save(new TipoAccionAuditoria(accionNombre)));

            String nombreEntidad = entidad.getClass().getSimpleName();
            int idAfectado = 0;

            try {
                var method = entidad.getClass().getMethod("getId");
                idAfectado = (Integer) method.invoke(entidad);
            } catch (Exception ignored) {}

            Usuario usuarioActual = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                if (auth.getPrincipal() instanceof Usuario) {
                    usuarioActual = (Usuario) auth.getPrincipal();
                } else if (auth.getName() != null) {
                    usuarioActual = usuarioRepository.findByCorreo(auth.getName()).orElse(null);
                }
            }

            if (usuarioActual == null) {
                usuarioActual = usuarioRepository.findByCorreo("admin@idoneos.online").orElse(null);
            }

            if (usuarioActual != null) {
                Auditoria audit = new Auditoria(nombreEntidad, idAfectado, usuarioActual, tipo);
                audit.setFechaHora(LocalDateTime.now());
                auditoriaRepository.save(audit);
            }

        } catch (Exception e) {
            // Silencioso para no romper la ejecución principal
        }
    }
}
