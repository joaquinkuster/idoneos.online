package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad base de autenticación. Alumno, Docente y Administrador son subtipos
 * implementados con @OneToOne para garantizar integridad referencial por tabla.
 * Se conserva el campo rol (enum) para compatibilidad con Spring Security sin
 * necesidad de joins adicionales en cada request.
 */
@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "dni", nullable = true, length = 8)
    private String dni;

    @Column(name = "telefono", nullable = true, length = 20)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasena", nullable = true, length = 255)
    private String contrasena;

    @Column(name = "google_id", nullable = true, length = 255)
    private String googleId;

    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    @Column(name = "email_validado", nullable = false)
    private Boolean emailValidado = false;

    @Column(name = "token_recuperacion", nullable = true, length = 255)
    private String tokenRecuperacion;

    /**
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime para alinearse al modelo conceptual.
     */
    @Column(name = "expiracion_token", nullable = true)
    private LocalDateTime expiracionToken;

    /**
     * DDL: timestamp — cambiado de LocalDate a LocalDateTime para alinearse al modelo conceptual.
     */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    // Rol duplicado intencionalmente para que Spring Security no necesite
    // hacer joins a las tablas de subtipos en cada request autenticado.
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolUsuario rol = RolUsuario.Alumno;

    // ─── Relaciones con subtipos (lazy para no cargar por defecto) ─────────────
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Alumno alumno;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Docente docente;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Administrador administrador;

    // ─── Relaciones de negocio ─────────────────────────────────────────────────
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Set<UsuarioRol> usuarioRoles = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Sesion> sesiones;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public Boolean getEmailValidado() { return emailValidado; }
    public void setEmailValidado(Boolean emailValidado) { this.emailValidado = emailValidado; }

    public String getTokenRecuperacion() { return tokenRecuperacion; }
    public void setTokenRecuperacion(String tokenRecuperacion) { this.tokenRecuperacion = tokenRecuperacion; }

    public LocalDateTime getExpiracionToken() { return expiracionToken; }
    public void setExpiracionToken(LocalDateTime expiracionToken) { this.expiracionToken = expiracionToken; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }

    public Set<UsuarioRol> getUsuarioRoles() { return usuarioRoles; }
    public void setUsuarioRoles(Set<UsuarioRol> usuarioRoles) { this.usuarioRoles = usuarioRoles; }

    public List<Sesion> getSesiones() { return sesiones; }
    public void setSesiones(List<Sesion> sesiones) { this.sesiones = sesiones; }


    // ─── Constructores ─────────────────────────────────────────────────────────

    public Usuario(String nombre, String apellido, String correo, String contrasena, RolUsuario rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.emailValidado = true; // simplificado para el PMV
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    public boolean esInactivo() { return baja; }

    public boolean esDocente() { return rol == RolUsuario.Docente; }

    public boolean esAlumno() { return rol == RolUsuario.Alumno; }

    public boolean esAdmin() { return rol == RolUsuario.Administrador; }

    public boolean tieneClonIA() {
        return docente != null && docente.getFechaConsentimientoClon() != null;
    }

    public String getNombreCompleto() { return nombre + " " + apellido; }

    @Override
    public String toString() { return getNombreCompleto(); }

    // ─── Spring Security: UserDetails ─────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() { return contrasena; }

    @Override
    public String getUsername() { return correo; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return !baja && emailValidado; }
}
