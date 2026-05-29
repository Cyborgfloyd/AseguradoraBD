package Modelos;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellidoP;
    private String apellidoM;
    private String telefono1;
    private String telefono2;
    private String correo;

    // Constructor vacío
    public Cliente() {
    }

    // Constructor completo (Para cuando listamos desde la BD)
    public Cliente(int idCliente, String nombre, String apellidoP, String apellidoM, String telefono1, String telefono2, String correo) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellidoP = apellidoP;
        this.apellidoM = apellidoM;
        this.telefono1 = telefono1;
        this.telefono2 = telefono2;
        this.correo = correo;
    }

    // Getters y Setters
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoP() { return apellidoP; }
    public void setApellidoP(String apellidoP) { this.apellidoP = apellidoP; }

    public String getApellidoM() { return apellidoM; }
    public void setApellidoM(String apellidoM) { this.apellidoM = apellidoM; }

    public String getTelefono1() { return telefono1; }
    public void setTelefono1(String telefono1) { this.telefono1 = telefono1; }

    public String getTelefono2() { return telefono2; }
    public void setTelefono2(String telefono2) { this.telefono2 = telefono2; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() {
        return "Cliente{" + "id=" + idCliente + ", nombre=" + nombre + " " + apellidoP + " " + apellidoM + ", tel=" + telefono1 + ", correo=" + correo + '}';
    }

   
}