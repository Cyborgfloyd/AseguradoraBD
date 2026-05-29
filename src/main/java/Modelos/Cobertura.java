/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelos;


public class Cobertura {
    private int idCobertura;
    private String nombreCobertura;

    public Cobertura() {}
    public Cobertura(int idCobertura, String nombreCobertura) {
        this.idCobertura = idCobertura;
        this.nombreCobertura = nombreCobertura;
    }
    // Getters y Setters...

    public int getIdCobertura() {
        return idCobertura;
    }

    public void setIdCobertura(int idCobertura) {
        this.idCobertura = idCobertura;
    }

    public String getNombreCobertura() {
        return nombreCobertura;
    }

    public void setNombreCobertura(String nombreCobertura) {
        this.nombreCobertura = nombreCobertura;
    }
}