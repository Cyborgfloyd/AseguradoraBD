/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelos;

import java.sql.Timestamp;

public class LogHistorial {
    private int idLog;
    private String usuario;
    private String accion;
    private String tabla;
    private Timestamp fecha;

    public LogHistorial(int idLog, String usuario, String accion, String tabla, Timestamp fecha, String detalles) {
        this.idLog = idLog;
        this.usuario = usuario;
        this.accion = accion;
        this.tabla = tabla;
        this.fecha = fecha;
        this.detalles = detalles;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTabla() {
        return tabla;
    }

    public void setTabla(String tabla) {
        this.tabla = tabla;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getDetalles() {
        return detalles;
    }

    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
    private String detalles;

    public LogHistorial() {}
    // Getters, Setters y Constructores...
}