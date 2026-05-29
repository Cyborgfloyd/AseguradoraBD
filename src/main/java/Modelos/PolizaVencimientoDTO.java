package Modelos;

public class PolizaVencimientoDTO {

    private String noPoliza;
    private String nombreCliente;
    private int numParcialidad;
    private String fechaVencimiento;

    public PolizaVencimientoDTO() {
    }

    public String getNoPoliza() {
        return noPoliza;
    }

    public void setNoPoliza(String noPoliza) {
        this.noPoliza = noPoliza;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getNumParcialidad() {
        return numParcialidad;
    }

    public void setNumParcialidad(int numParcialidad) {
        this.numParcialidad = numParcialidad;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public String toString() {
        return "Vencimiento -> Póliza: " + noPoliza + " | Cliente: " + nombreCliente + " | Parcialidad: " + numParcialidad + " | Vence: " + fechaVencimiento;
    }
}
