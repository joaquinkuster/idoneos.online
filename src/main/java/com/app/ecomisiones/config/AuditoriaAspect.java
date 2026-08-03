package com.app.ecomisiones.config;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
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

    @AfterReturning(pointcut = "execution(* com.app.ecomisiones.service..*.guardar*(..))", returning = "result")
    public void auditarGuardar(JoinPoint joinPoint, Object result) {
        registrarAuditoria("Crear", result);
    }

    @AfterReturning(pointcut = "execution(* com.app.ecomisiones.service..*.modificar*(..))", returning = "result")
    public void auditarModificar(JoinPoint joinPoint, Object result) {
        registrarAuditoria("Modificar", result);
    }

    @AfterReturning(pointcut = "execution(* com.app.ecomisiones.service..*.borrar*(..))")
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
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                usuarioActual = (Usuario) auth.getPrincipal();
            }

            Auditoria audit = new Auditoria(nombreEntidad, idAfectado, usuarioActual, tipo);
            audit.setFechaHora(LocalDateTime.now());
            auditoriaRepository.save(audit);

        } catch (Exception e) {
            // Silencioso para no romper la ejecución principal
        }
    }
}
