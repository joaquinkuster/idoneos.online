package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Entidad que representa un usuario del sistema. */
@Entity
@Table(name = "Usuario")
@Check(name = "ck_usuario_dni", constraints = "dni ~ '^[0-9]{7,8}$'")
@Check(name = "ck_usuario_nombre", constraints = "trim(nombre) <> '' and trim(apellido) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private int idUsuario;

    /** Rol por defecto de este registro. */
    @ManyToOne
    @JoinColumn(name = "idRol", nullable = true)
    private Rol rolPorDefecto;

    /** Nombre del usuario. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Apellido del usuario. */
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    /** Documento Nacional de Identidad. */
    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    /** Correo electrónico. */
    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    /** Contraseña cifrada del usuario. */
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    /** Ruta de la imagen de perfil. */
    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    /** Teléfono de contacto. */
    @Column(name = "telefono", nullable = true, length = 20)
    private String telefono;

    /** Token temporal de un solo uso para verificar el correo. */
    @Column(name = "tokenVerificacion", nullable = true, length = 255, unique = true)
    private String tokenVerificacion;

    /** Fecha y hora de vencimiento del token de verificación. */
    @Column(name = "vencimientoTokenVerificacion", nullable = true)
    private LocalDateTime vencimientoTokenVerificacion;

    /** Identificador de la cuenta de Google vinculada, si existe. */
    @Column(name = "googleId", nullable = true, length = 255, unique = true)
    private String googleId;

    /** Indica si el correo electrónico fue validado. */
    @Column(name = "emailValidado", nullable = false)
    private Boolean emailValidado = false;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Perfil de docente del usuario, si lo tiene. */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Docente docente;

    /** Sesiones que referencian a este registro. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Sesion> sesiones = new HashSet<>();

    /** Auditorias que referencian a este registro. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Auditoria> auditorias = new HashSet<>();

    /** Perfil de alumno del usuario, si lo tiene. */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Alumno alumno;

    /** Perfil de administrador del usuario, si lo tiene. */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Administrador administrador;

    /** Roles que referencian a este registro. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RolUsuario> roles = new HashSet<>();

    /** Crea el usuario con los datos básicos. */
    public Usuario(String nombre, String apellido, String dni, String correo, String contrasena) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del usuario. */
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

    /** Obtiene las iniciales del nombre y el apellido, para el avatar cuando el usuario no tiene foto de perfil. */
    public String getIniciales() {
        String inicialNombre = nombre == null || nombre.isBlank() ? "" : nombre.trim().substring(0, 1);
        String inicialApellido = apellido == null || apellido.isBlank() ? "" : apellido.trim().substring(0, 1);
        return (inicialNombre + inicialApellido).toUpperCase();
    }

    /** Obtiene la dirección de la foto de perfil. */
    public String getUrlImagen() {
        return imagen == null || imagen.isBlank() ? null : "/img/usuarios/" + imagen;
    }

    /** Obtiene el nombre completo del usuario. */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /** Verifica si el usuario tiene un rol vigente (no dado de baja). */
    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(rolUsuario -> !rolUsuario.getBaja()
                && rolUsuario.getRol() != null && nombreRol.equalsIgnoreCase(rolUsuario.getRol().getNombre()));
    }

    /** Verifica si el usuario tiene el rol de administrador vigente. */
    /** Obtiene los roles vigentes del usuario (los que no fueron dados de baja), ordenados por nombre. */
    public java.util.List<Rol> getRolesVigentes() {
        return roles.stream()
                .filter(rolUsuario -> !rolUsuario.getBaja() && rolUsuario.getRol() != null)
                .map(RolUsuario::getRol)
                .sorted(java.util.Comparator.comparing(Rol::getNombre))
                .toList();
    }

    /** Obtiene el rol con el que el usuario trabaja: su rol por defecto si lo tiene vigente o, si no, el primero de sus roles vigentes. */
    public Rol getRolActivo() {
        java.util.List<Rol> vigentes = getRolesVigentes();
        if (rolPorDefecto != null && vigentes.stream().anyMatch(rol -> rol.getIdRol() == rolPorDefecto.getIdRol())) {
            return rolPorDefecto;
        }
        return vigentes.isEmpty() ? null : vigentes.get(0);
    }

    /** Indica si el rol con el que el usuario trabaja es el indicado. */
    public boolean esRolActivo(String nombreRol) {
        Rol activo = getRolActivo();
        return activo != null && nombreRol.equalsIgnoreCase(activo.getNombre());
    }

    public boolean esAdministradorActivo() {
        return esRolActivo("Administrador");
    }

    public boolean esDocenteActivo() {
        return esRolActivo("Docente");
    }

    public boolean esAlumnoActivo() {
        return esRolActivo("Alumno");
    }

    public boolean esAdmin() {
        return tieneRol("Administrador");
    }

    /** Verifica si el usuario tiene el rol de docente vigente. */
    public boolean esDocente() {
        return tieneRol("Docente");
    }

    /** Verifica si el usuario tiene el rol de alumno vigente. */
    public boolean esAlumno() {
        return tieneRol("Alumno");
    }

    // Implementación de métodos de la interfaz UserDetails de Spring Security

    /** Obtiene los roles vigentes del usuario como autoridades para la gestión de acceso. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .filter(rolUsuario -> !rolUsuario.getBaja())
                .map(rolUsuario -> new SimpleGrantedAuthority("ROLE_" + rolUsuario.getRol().getNombre()))
                .toList();
    }

    /** Obtiene la contraseña cifrada del usuario. */
    @Override
    public String getPassword() {
        return contrasena;
    }

    /** Obtiene el nombre de usuario (correo electrónico). */
    @Override
    public String getUsername() {
        return correo;
    }

    /** Verifica si la cuenta del usuario ha expirado. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Verifica si la cuenta del usuario está bloqueada. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Verifica si las credenciales del usuario han expirado. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Verifica si la cuenta del usuario está habilitada. */
    @Override
    public boolean isEnabled() {
        return !baja && emailValidado;
    }
}
