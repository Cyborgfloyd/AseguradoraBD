package Controladores;

import DAO.LogHistorialDAO;
import DAO.PolizaDAO;
import Modelos.Poliza;
import java.util.List;

public class PolizaController {

    private final PolizaDAO dao = new PolizaDAO();

    public List<Poliza> obtenerTodas() {
        return dao.listarPolizas();
    }

    public Poliza buscarPorNoPoliza(String noPoliza) {
        return dao.buscarPorNoPoliza(noPoliza);
    }

    public String agregar(Poliza p, String usuarioActual) {
        String error = validar(p);
        if (error != null) return error;
        try {
            dao.registrarPoliza(p);
            LogHistorialDAO.registrar(usuarioActual, "INSERT", "POLIZAS",
                "Alta póliza: " + p.getNoPoliza() + " | Cliente ID: " + p.getIdCliente()
                + " | Compañía ID: " + p.getIdCompania());
            return null;
        } catch (Exception e) {
            System.err.println("Error en PolizaController.agregar: " + e.getMessage());
            return "No se pudo registrar la póliza. Verifica que el número de póliza no esté duplicado.";
        }
    }

    public String actualizar(Poliza p, String usuarioActual) {
        if (p.getIdCompania() <= 0) return "Selecciona una compañía.";
        if (p.getIdCobertura() <= 0) return "Selecciona una cobertura.";
        try {
            dao.actualizar(p);
            LogHistorialDAO.registrar(usuarioActual, "UPDATE", "POLIZAS",
                "Actualización póliza: " + p.getNoPoliza());
            return null;
        } catch (Exception e) {
            System.err.println("Error en PolizaController.actualizar: " + e.getMessage());
            return "No se pudo actualizar la póliza. Revisa la consola.";
        }
    }

    public String cancelar(String noPoliza, String usuarioActual) {
        try {
            dao.cancelarPolizaSegura(noPoliza);
            LogHistorialDAO.registrar(usuarioActual, "CANCELAR", "POLIZAS",
                "Cancelacion poliza: " + noPoliza);
            return null;
        } catch (Exception e) {
            System.err.println("Error en PolizaController.cancelar: " + e.getMessage());
            return "No se pudo cancelar la poliza. Revisa la consola.";
        }
    }

    public String renovar(String noPolizaVieja, String noPolizaNueva, String nuevaFecha, String usuarioActual) {
        if (noPolizaNueva == null || noPolizaNueva.trim().isEmpty())
            return "El nuevo numero de poliza es obligatorio.";
        if (!noPolizaNueva.trim().matches("[A-Za-z0-9\\-]{3,20}"))
            return "El numero de poliza debe tener 3-20 caracteres alfanumericos.";
        if (nuevaFecha == null || !nuevaFecha.matches("\\d{4}-\\d{2}-\\d{2}"))
            return "Formato de fecha incorrecto. Usa AAAA-MM-DD.";
        try {
            dao.renovar(noPolizaVieja, noPolizaNueva.trim().toUpperCase(), nuevaFecha);
            LogHistorialDAO.registrar(usuarioActual, "RENOVAR", "POLIZAS",
                "Renovacion: " + noPolizaVieja + " -> " + noPolizaNueva + " | Nueva fecha: " + nuevaFecha);
            return null;
        } catch (Exception e) {
            System.err.println("Error en PolizaController.renovar: " + e.getMessage());
            return "No se pudo renovar. Verifica que el nuevo numero de poliza no este duplicado.";
        }
    }

    public String eliminarFisico(String noPoliza, String rolUsuario, String usuarioActual) {
        if (!"Administrador".equals(rolUsuario))
            return "Solo el Administrador puede eliminar polizas de la base de datos.";
        try {
            dao.eliminarFisico(noPoliza);
            LogHistorialDAO.registrar(usuarioActual, "DELETE_FISICO", "POLIZAS",
                "Eliminacion fisica poliza: " + noPoliza);
            return null;
        } catch (Exception e) {
            System.err.println("Error en PolizaController.eliminarFisico: " + e.getMessage());
            return "No se pudo eliminar. La poliza puede tener pagos asociados.";
        }
    }

    private String validar(Poliza p) {
        if (p.getNoPoliza() == null || p.getNoPoliza().trim().isEmpty())
            return "El número de póliza es obligatorio.";
        if (!p.getNoPoliza().matches("[A-Za-z0-9\\-]{3,20}"))
            return "El número de póliza debe tener 3–20 caracteres alfanuméricos.";
        if (p.getIdCliente() <= 0)  return "Selecciona un cliente.";
        if (p.getIdVehiculo() <= 0) return "Selecciona un vehículo.";
        if (p.getIdCompania() <= 0) return "Selecciona una compañía.";
        if (p.getIdCobertura() <= 0) return "Selecciona una cobertura.";
        if (p.getFechaInicio() == null || p.getFechaInicio().trim().isEmpty())
            return "La fecha de inicio es obligatoria (formato: AAAA-MM-DD).";
        if (!p.getFechaInicio().matches("\\d{4}-\\d{2}-\\d{2}"))
            return "Formato de fecha incorrecto. Usa AAAA-MM-DD.";
        return null;
    }
}
