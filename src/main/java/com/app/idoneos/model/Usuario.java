package com.app.idoneos.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entidad base de autenticación e identidad de usuarios de la plataforma.
 * Subtipos: Alumno, Docente, Administrador.
 * Mapea directamente a la tabla "Usuario" en base_datos.sql.
 */
@Entity
@Table(name = "Usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    /** Nombre del usuario. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Apellido del usuario. */
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    /** Documento Nacional de Identidad. */
    @Column(name = "dni", nullable = false, length = 8)
    private String dni = "00000000";

    /** Correo electrónico (identificador de login). */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash de la contraseña de acceso. */
    @Column(name = "contrasena", length = 255)
    private String contrasena;

    /** Ruta de la imagen de perfil del usuario. */
    @Column(name = "imagen", length = 150)
    private String imagen;

    /** Número de teléfono de contacto. */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Token temporal para restablecimiento de contraseña. */
    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    /** Expiración del token de recuperación. */
    @Column(name = "expiracion_token")
    private LocalDateTime expiracionToken;

    /** Identificador único devuelto por Google OAuth. */
    @Column(name = "google_id", length = 255)
    private String googleId;

    /** Estado de verificación del correo electrónico. */
    @Column(name = "email_validado", nullable = false)
    private boolean emailValidado = false;

    /** Fecha y hora de creación de la cuenta. */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /** Marca de baja lógica del usuario. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Rol del usuario (FK a tabla Rol, persiste en BD). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    /** Subtipo Alumno (si aplica). */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Alumno alumno;

    /** Subtipo Docente (si aplica). */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Docente docente;

    /** Subtipo Administrador (si aplica). */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Administrador administrador;

    /** Sesiones activas e inactivas del usuario. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Sesion> sesiones = new ArrayList<>();

    /** Registros de auditoría generados por acciones del usuario. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Auditoria> auditorias = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String correo, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.emailValidado = true;
        this.fechaRegistro = LocalDateTime.now();
    }

    /** Alias de compatibilidad: retorna idUsuario. */
    public int getId() {
        return idUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setId(int id) {
        this.idUsuario = id;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTokenRecuperacion() {
        return tokenRecuperacion;
    }

    public void setTokenRecuperacion(String tokenRecuperacion) {
        this.tokenRecuperacion = tokenRecuperacion;
    }

    public LocalDateTime getExpiracionToken() {
        return expiracionToken;
    }

    public void setExpiracionToken(LocalDateTime expiracionToken) {
        this.expiracionToken = expiracionToken;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public boolean isEmailValidado() {
        return emailValidado;
    }

    public boolean getEmailValidado() {
        return emailValidado;
    }

    public void setEmailValidado(boolean emailValidado) {
        this.emailValidado = emailValidado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Retorna el RolUsuario equivalente al rol persistido, para compatibilidad
     * con lógica de Spring Security y servicios existentes.
     */
    public RolUsuario getRolUsuario() {
        if (rol != null && rol.getNombre() != null) {
            try {
                return RolUsuario.valueOf(rol.getNombre());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (administrador != null) return RolUsuario.Administrador;
        if (docente != null) return RolUsuario.Docente;
        if (alumno != null) return RolUsuario.Alumno;
        return RolUsuario.Alumno;
    }

    public boolean esInactivo() {
        return baja;
    }

    public boolean esDocente() {
        return docente != null || (rol != null && "Docente".equals(rol.getNombre()));
    }

    public boolean esAlumno() {
        return alumno != null || (rol != null && "Alumno".equals(rol.getNombre()));
    }

    public boolean esAdmin() {
        return administrador != null || (rol != null && "Administrador".equals(rol.getNombre()));
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = (rol != null && rol.getNombre() != null) ? rol.getNombre() : "Alumno";
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !baja && emailValidado;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public List<Sesion> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<Sesion> sesiones) {
        this.sesiones = sesiones;
    }

    public List<Auditoria> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<Auditoria> auditorias) {
        this.auditorias = auditorias;
    }
}
