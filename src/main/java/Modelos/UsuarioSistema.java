
package Modelos;


public class UsuarioSistema {
    private int idUsuario;
    private String username;
    private String passwordHash;
    private String rol; // "Administrador" o "Operador"

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public UsuarioSistema(int idUsuario, String username, String passwordHash, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public UsuarioSistema() {}
    // Getters, Setters y Constructores...
}