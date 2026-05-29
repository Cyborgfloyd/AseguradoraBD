package DAO; 

import Modelos.Cliente;
import java.time.LocalDate;


public class ClienteHistorico extends Cliente {
    
    private LocalDate fechaBaja;

    // Constructor vacío
    public ClienteHistorico() {
        super();
    }

    // Getter y Setter para el campo específico
    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}