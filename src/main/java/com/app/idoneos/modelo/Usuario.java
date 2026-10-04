package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuario del sistema. Concentra los datos de identidad y de acceso, y sus roles se definen mediante {@link RolUsuario}. Implementa {@link UserDetails} para la autenticación con Spring Security.
 */
@Entity
@Table(name = "Usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private int idUsuario;

    /**
     * Relación con Rol. Es opcional.
     */
    @ManyToOne
    @JoinColumn(name = "idRolPorDefecto", nullable = true)
    private Rol rolPorDefecto;

    /**
     * Nombre del usuario.
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /**
     * Apellido del usuario.
     */
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    /**
     * Documento Nacional de Identidad. Debe ser único.
     */
    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    /**
     * Correo electrónico. Se usa como nombre de usuario para iniciar sesión. Debe ser único.
     */
    @Column(name = "correo", nullable = false, length = 150, unique = true)
    private String correo;

    /**
     * Contraseña cifrada del usuario.
     */
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    /**
     * Ruta de la imagen de perfil.
     */
    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    /**
     * Teléfono de contacto.
     */
    @Column(name = "telefono", nullable = true, length = 20)
    private String telefono;

    /**
     * Token temporal de un solo uso para verificar el correo. Si el correo no fue validado (emailValidado = false), verifica el correo al registrarse. Si ya fue validado (emailValidado = true), autoriza el restablecimiento de la contraseña. Debe ser único. Es opcional.
     */
    @Column(name = "tokenVerificacion", nullable = true, length = 255, unique = true)
    private String tokenVerificacion;

    /**
     * Fecha y hora de vencimiento del token de verificación. Es opcional.
     */
    @Column(name = "vencimientoTokenVerificacion", nullable = true)
    private LocalDateTime vencimientoTokenVerificacion;

    /**
     * Identificador de la cuenta de Google vinculada, si existe.
     */
    @Column(name = "googleId", nullable = true, length = 255, unique = true)
    private String googleId;

    /**
     * Indica si el correo electrónico fue validado.
     */
    @Column(name = "emailValidado", nullable = false)
    private Boolean emailValidado = false;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Perfil de docente del usuario, si lo tiene.
     */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Docente docente;

    /**
     * Conjunto de sesiones asociados.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Sesion> sesiones = new HashSet<>();

    /**
     * Conjunto de auditorias asociados.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<Auditoria> auditorias = new HashSet<>();

    /**
     * Perfil de alumno del usuario, si lo tiene.
     */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Alumno alumno;

    /**
     * Perfil de administrador del usuario, si lo tiene.
     */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Administrador administrador;

    /**
     * Conjunto de roles asociados.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RolUsuario> roles = new HashSet<>();

    /**
     * Constructor para crear el usuario con los datos básicos.
     *
     * @param nombre nombre del usuario.
     * @param apellido apellido del usuario.
     * @param dni documento Nacional de Identidad. Debe ser único.
     * @param correo correo electrónico. Se usa como nombre de usuario para iniciar sesión. Debe ser único.
     * @param contrasena contraseña cifrada del usuario.
     */
    public Usuario(String nombre, String apellido, String dni, String correo, String contrasena) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena del usuario.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return El nombre y el apellido del usuario.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Verifica si el usuario tiene un rol vigente (no dado de baja).
     *
     * @param nombreRol El nombre del rol (Administrador, Docente o Alumno).
     * @return {@code true} si el usuario tiene el rol vigente.
     */
    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(rolUsuario -> !rolUsuario.getBaja()
                && rolUsuario.getRol() != null && nombreRol.equalsIgnoreCase(rolUsuario.getRol().getNombre()));
    }

    /**
     * Verifica si el usuario tiene el rol de administrador vigente.
     *
     * @return {@code true} si es administrador.
     */
    /**
     * Obtiene los roles vigentes del usuario (los que no fueron dados de baja), ordenados por nombre.
     *
     * @return La lista de roles vigentes.
     */
    public java.util.List<Rol> getRolesVigentes() {
        return roles.stream()
                .filter(rolUsuario -> !rolUsuario.getBaja() && rolUsuario.getRol() != null)
                .map(RolUsuario::getRol)
                .sorted(java.util.Comparator.comparing(Rol::getNombre))
                .toList();
    }

    /**
     * Obtiene el rol con el que el usuario trabaja: su rol por defecto si lo tiene vigente o, si no,
     * el primero de sus roles vigentes.
     *
     * @return El rol activo, o {@code null} si el usuario no tiene roles vigentes.
     */
    public Rol getRolActivo() {
        java.util.List<Rol> vigentes = getRolesVigentes();
        if (rolPorDefecto != null && vigentes.stream().anyMatch(rol -> rol.getIdRol() == rolPorDefecto.getIdRol())) {
            return rolPorDefecto;
        }
        return vigentes.isEmpty() ? null : vigentes.get(0);
    }

    /**
     * Indica si el rol con el que el usuario trabaja es el indicado.
     *
     * @param nombreRol El nombre del rol.
     * @return {@code true} si el rol activo coincide.
     */
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

    /**
     * Verifica si el usuario tiene el rol de docente vigente.
     *
     * @return {@code true} si es docente.
     */
    public boolean esDocente() {
        return tieneRol("Docente");
    }

    /**
     * Verifica si el usuario tiene el rol de alumno vigente.
     *
     * @return {@code true} si es alumno.
     */
    public boolean esAlumno() {
        return tieneRol("Alumno");
    }

    // Implementación de métodos de la interfaz UserDetails de Spring Security

    /**
     * Obtiene los roles vigentes del usuario como autoridades para la gestión de acceso.
     * Solo se consideran las asignaciones de rol que no fueron dadas de baja.
     *
     * @return La colección de autoridades del usuario.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .filter(rolUsuario -> !rolUsuario.getBaja())
                .map(rolUsuario -> new SimpleGrantedAuthority("ROLE_" + rolUsuario.getRol().getNombre()))
                .toList();
    }

    /**
     * Obtiene la contraseña cifrada del usuario.
     *
     * @return La contraseña del usuario.
     */
    @Override
    public String getPassword() {
        return contrasena;
    }

    /**
     * Obtiene el nombre de usuario (correo electrónico).
     *
     * @return El correo electrónico del usuario.
     */
    @Override
    public String getUsername() {
        return correo;
    }

    /**
     * Verifica si la cuenta del usuario ha expirado.
     *
     * @return {@code true} ya que las cuentas no tienen expiración.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Verifica si la cuenta del usuario está bloqueada.
     *
     * @return {@code true} ya que las cuentas no se bloquean.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Verifica si las credenciales del usuario han expirado.
     *
     * @return {@code true} ya que las credenciales no tienen expiración.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Verifica si la cuenta del usuario está habilitada: no debe estar dada de baja y su correo debe estar validado.
     *
     * @return {@code true} si la cuenta está habilitada.
     */
    @Override
    public boolean isEnabled() {
        return !baja && emailValidado;
    }
}
