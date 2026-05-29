/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelos;

public class Poliza {

    private String noPoliza;
    private int idCliente;
    private int idVehiculo;
    private int idCompania;
    private int idCobertura;
    private String fechaInicio;
    private boolean cfdi;
    private String estatus;

    // Campos de solo display (para la tabla)
    private String nombreCliente;
    private String modeloVehiculo;
    private String placasVehiculo;
    private String nombreCompania;
    private String nombreCobertura;

    // Constructor vacío
    public Poliza() {
    }

    // Constructor completo
    public Poliza(String noPoliza, int idCliente, int idVehiculo, int idCompania, int idCobertura, String fechaInicio, boolean cfdi, String estatus) {
        this.noPoliza = noPoliza;
        this.idCliente = idCliente;
        this.idVehiculo = idVehiculo;
        this.idCompania = idCompania;
        this.idCobertura = idCobertura;
        this.fechaInicio = fechaInicio;
        this.cfdi = cfdi;
        this.estatus = estatus;
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================
    public String getNoPoliza() {
        return noPoliza;
    }

    public void setNoPoliza(String noPoliza) {
        this.noPoliza = noPoliza;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public int getIdCompania() {
        return idCompania;
    }

    public void setIdCompania(int idCompania) {
        this.idCompania = idCompania;
    }

    public int getIdCobertura() {
        return idCobertura;
    }

    public void setIdCobertura(int idCobertura) {
        this.idCobertura = idCobertura;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public boolean isCfdi() {
        return cfdi;
    }

    public void setCfdi(boolean cfdi) {
        this.cfdi = cfdi;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getNombreCliente()  { return nombreCliente; }
    public void setNombreCliente(String v)  { this.nombreCliente = v; }
    public String getModeloVehiculo() { return modeloVehiculo; }
    public void setModeloVehiculo(String v) { this.modeloVehiculo = v; }
    public String getPlacasVehiculo() { return placasVehiculo; }
    public void setPlacasVehiculo(String v) { this.placasVehiculo = v; }
    public String getNombreCompania() { return nombreCompania; }
    public void setNombreCompania(String v) { this.nombreCompania = v; }
    public String getNombreCobertura(){ return nombreCobertura; }
    public void setNombreCobertura(String v){ this.nombreCobertura = v; }
}
