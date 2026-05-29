package Controladores;

import DAO.LogHistorialDAO;
import DAO.VehiculoDAO;
import Modelos.Vehiculo;
import java.util.List;

public class VehiculoController {

    private final VehiculoDAO dao = new VehiculoDAO();

    public List<Vehiculo> obtenerTodos() {
        return dao.listarTodos();
    }

    public Vehiculo buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

    public String agregar(Vehiculo v, String usuarioActual) {
        String error = validar(v);
        if (error != null) return error;
        try {
            dao.insertar(v);
            LogHistorialDAO.registrar(usuarioActual, "INSERT", "VEHICULOS",
                "Alta vehículo: " + v.getModelo() + " | Placas: " + v.getPlacas()
                + " | Cliente ID: " + v.getIdCliente());
            return null;
        } catch (Exception e) {
            System.err.println("Error en VehiculoController.agregar: " + e.getMessage());
            return "No se pudo guardar el vehículo. Revisa la consola.";
        }
    }

    public String actualizar(Vehiculo v, String usuarioActual) {
        String error = validar(v);
        if (error != null) return error;
        try {
            dao.actualizar(v);
            LogHistorialDAO.registrar(usuarioActual, "UPDATE", "VEHICULOS",
                "Actualización vehículo ID " + v.getIdVehiculo()
                + ": " + v.getModelo() + " | Placas: " + v.getPlacas());
            return null;
        } catch (Exception e) {
            System.err.println("Error en VehiculoController.actualizar: " + e.getMessage());
            return "No se pudo actualizar el vehículo. Revisa la consola.";
        }
    }

    public String desactivar(int idVehiculo, String usuarioActual) {
        try {
            dao.eliminar(idVehiculo);
            LogHistorialDAO.registrar(usuarioActual, "DESACTIVAR", "VEHICULOS",
                "Baja lógica vehículo ID " + idVehiculo);
            return null;
        } catch (Exception e) {
            System.err.println("Error en VehiculoController.desactivar: " + e.getMessage());
            return "No se pudo desactivar el vehículo. Revisa la consola.";
        }
    }

    public List<Vehiculo> listarPorCliente(int idCliente) {
        return dao.listarPorCliente(idCliente);
    }

    private String validar(Vehiculo v) {
        if (v.getIdCliente() <= 0)
            return "Debes seleccionar un cliente.";
        if (v.getModelo() == null || v.getModelo().trim().isEmpty())
            return "El modelo es obligatorio.";
        if (v.getPlacas() == null || v.getPlacas().trim().isEmpty())
            return "Las placas son obligatorias.";
        if (!v.getPlacas().matches("[A-Za-z0-9\\-]{4,10}"))
            return "Las placas deben tener entre 4 y 10 caracteres alfanuméricos.";
        return null;
    }
}
