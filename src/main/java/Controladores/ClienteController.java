package Controladores;

import DAO.ClienteDAO;
import DAO.LogHistorialDAO;
import Modelos.Cliente;
import java.util.List;

public class ClienteController {

    private final ClienteDAO dao = new ClienteDAO();

    public List<Cliente> obtenerActivos() {
        return dao.listarTodos();
    }

    public Cliente buscarPorId(int id) {
        return dao.buscarPorId(id);
    }

   
    public String agregar(Cliente c, String usuarioActual) {
        String error = validar(c);
        if (error != null) return error;

        try {
            dao.insertar(c);
            LogHistorialDAO.registrar(
                usuarioActual,
                "INSERT",
                "CLIENTES",
                "Alta cliente: " + c.getNombre() + " " + c.getApellidoP()
                + " | Tel: " + c.getTelefono1()
            );
            return null; 

        } catch (Exception e) {
            System.err.println("Error en ClienteController.agregar: " + e.getMessage());
            return "No se pudo guardar el cliente. Revisa la consola.";
        }
    }

 
    public String actualizar(Cliente c, String usuarioActual) {
        String error = validar(c);
        if (error != null) return error;

        try {
            dao.actualizar(c);
            LogHistorialDAO.registrar(usuarioActual,"UPDATE","CLIENTES",
                    "Actualización cliente ID " + c.getIdCliente()
                + ": " + c.getNombre() + " " + c.getApellidoP()
            );
            return null;

        } catch (Exception e) {
            System.err.println("Error en ClienteController.actualizar: " + e.getMessage());
            return "No se pudo actualizar el cliente. Revisa la consola.";
        }
    }

   
    public String desactivar(int idCliente, String usuarioActual) {
        try {
            dao.desactivar(idCliente);
            LogHistorialDAO.registrar(
                usuarioActual,
                "DESACTIVAR",
                "CLIENTES",
                "Baja lógica cliente ID " + idCliente
            );
            return null;

        } catch (Exception e) {
            System.err.println("Error en ClienteController.desactivar: " + e.getMessage());
            return "No se pudo desactivar el cliente. Revisa la consola.";
        }
    }

   
  
    public String validar(Cliente c) {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty())
            return "El nombre es obligatorio.";

        if (c.getApellidoP() == null || c.getApellidoP().trim().isEmpty())
            return "El apellido paterno es obligatorio.";

        if (c.getTelefono1() == null || c.getTelefono1().trim().isEmpty())
            return "El teléfono 1 es obligatorio.";

        if (!c.getTelefono1().matches("\\d{10,15}"))
            return "El teléfono 1 debe tener entre 10 y 15 dígitos (solo números).";

        if (c.getTelefono2() != null && !c.getTelefono2().isEmpty()
                && !c.getTelefono2().matches("\\d{10,15}"))
            return "El teléfono 2 debe tener entre 10 y 15 dígitos (solo números).";

        if (c.getCorreo() != null && !c.getCorreo().isEmpty()
                && !c.getCorreo().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            return "El correo no tiene un formato válido (ejemplo: juan@correo.com).";

        return null; 
    }
}