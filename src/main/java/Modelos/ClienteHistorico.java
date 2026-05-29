/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelos;

import java.util.Date;

public class ClienteHistorico {
    // Igual a Cliente + fechaBaja
    private Date fechaBaja;
    // Getters, Setters...

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public ClienteHistorico(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}