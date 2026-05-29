package Modelos;

public class Vehiculo {
    private int idVehiculo;
    private int idCliente;
    private String modelo;
    private String placas;
    private String nombreCliente; // solo para display en la tabla

    public Vehiculo() {}

    public Vehiculo(int idVehiculo, int idCliente, String modelo, String placas) {
        this.idVehiculo = idVehiculo;
        this.idCliente = idCliente;
        this.modelo = modelo;
        this.placas = placas;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlacas() { return placas; }
    public void setPlacas(String placas) { this.placas = placas; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}