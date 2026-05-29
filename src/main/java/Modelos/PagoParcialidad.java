/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelos;
import java.util.Date;

public class PagoParcialidad {
    private int idPago;
    private String noPoliza;
    private int numParcialidad;
    private int totalParcialidades;
    private Date fechaVencimiento;
    private String estatus; // "Pendiente", "Pagado", "Vencido"

    public PagoParcialidad() {}
    // Getters, Setters y Constructores...

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public String getNoPoliza() {
        return noPoliza;
    }

    public void setNoPoliza(String noPoliza) {
        this.noPoliza = noPoliza;
    }

    public int getNumParcialidad() {
        return numParcialidad;
    }

    public void setNumParcialidad(int numParcialidad) {
        this.numParcialidad = numParcialidad;
    }

    public int getTotalParcialidades() {
        return totalParcialidades;
    }

    public void setTotalParcialidades(int totalParcialidades) {
        this.totalParcialidades = totalParcialidades;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public PagoParcialidad(int idPago, String noPoliza, int numParcialidad, int totalParcialidades, Date fechaVencimiento, String estatus) {
        this.idPago = idPago;
        this.noPoliza = noPoliza;
        this.numParcialidad = numParcialidad;
        this.totalParcialidades = totalParcialidades;
        this.fechaVencimiento = fechaVencimiento;
        this.estatus = estatus;
    }
}