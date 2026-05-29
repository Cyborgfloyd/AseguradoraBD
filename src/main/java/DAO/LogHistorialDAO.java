package DAO;

import config.Conexion;
import Modelos.LogHistorial;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO exclusivo para el historial de auditoría del sistema.
 * Responsabilidad única: registrar y consultar acciones en la tabla 'historial'.
 * NO debe contener lógica de otras entidades (clientes, vehículos, etc.).
 */
public class LogHistorialDAO {

    // =========================================================================
    // INSERTAR — Registra una acción en el historial de auditoría
    // =========================================================================
    /**
     * Inserta un registro de auditoría en la tabla 'historial'.
     * Se llama desde los demás DAOs después de cada operación importante.
     *
     * Ejemplo de uso desde ClienteDAO:
     *   LogHistorial log = new LogHistorial();
     *   log.setUsuario(usuarioActual);
     *   log.setAccion("ELIMINAR");
     *   log.setTabla("CLIENTES");
     *   log.setDetalles("Eliminación física cliente ID " + id);
     *   log.setFecha(new Timestamp(System.currentTimeMillis()));
     *   new LogHistorialDAO().registrarAccion(log);
     */
    public void registrarAccion(LogHistorial log) {
        String sql = "INSERT INTO historial (usuario, accion, tabla, detalles, fecha) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, log.getUsuario());
            ps.setString(2, log.getAccion());
            ps.setString(3, log.getTabla());
            ps.setString(4, log.getDetalles());
            ps.setTimestamp(5, log.getFecha());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al registrar en historial: " + e.getMessage());
        }
    }

    // =========================================================================
    // LISTAR — Obtiene todos los registros del historial ordenados por más reciente
    // =========================================================================
    /**
     * Retorna todos los logs de auditoría ordenados del más reciente al más antiguo.
     * Útil para el panel de administración / vista de historial.
     */
    public List<LogHistorial> obtenerTodosLosLogs() {
        List<LogHistorial> lista = new ArrayList<>();
        String sql = "SELECT id_log, usuario, accion, tabla, fecha, detalles "
                   + "FROM historial "
                   + "ORDER BY id_log DESC";

        // CORRECCIÓN: era "Config.Conexion" (C mayúscula) — Java distingue mayúsculas,
        // el paquete se llama "config" (minúscula), igual que en todos los demás DAOs.
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LogHistorial log = new LogHistorial(
                    rs.getInt("id_log"),
                    rs.getString("usuario"),
                    rs.getString("accion"),
                    rs.getString("tabla"),
                    rs.getTimestamp("fecha"),
                    rs.getString("detalles")
                );
                lista.add(log);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return lista;
    }

    // =========================================================================
    // LISTAR FILTRADO — Busca logs por nombre de tabla o tipo de acción
    // =========================================================================
    /**
     * Filtra el historial por tabla afectada (ej: "CLIENTES", "POLIZAS").
     * Útil para el panel de auditoría cuando el admin quiere ver solo
     * los cambios de una entidad específica.
     */
    public List<LogHistorial> obtenerLogsPorTabla(String tabla) {
        List<LogHistorial> lista = new ArrayList<>();
        // PAGOS puede estar guardado como 'PAGOS' o 'pagos_parcialidades' (datos históricos)
        boolean esPagos = "PAGOS".equalsIgnoreCase(tabla);
        String sql = "SELECT id_log, usuario, accion, tabla, fecha, detalles "
                   + "FROM historial "
                   + (esPagos
                       ? "WHERE UPPER(tabla) LIKE '%PAGO%' "
                       : "WHERE UPPER(tabla) = ? ")
                   + "ORDER BY id_log DESC";

        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (!esPagos) ps.setString(1, tabla.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LogHistorial log = new LogHistorial(
                        rs.getInt("id_log"),
                        rs.getString("usuario"),
                        rs.getString("accion"),
                        rs.getString("tabla"),
                        rs.getTimestamp("fecha"),
                        rs.getString("detalles")
                    );
                    lista.add(log);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al filtrar historial por tabla: " + e.getMessage());
        }
        return lista;
    }

    // =========================================================================
    // MÉTODO UTILITARIO ESTÁTICO — Facilita registrar desde cualquier DAO
    // =========================================================================
    /**
     * Método de conveniencia estático para no tener que instanciar LogHistorialDAO
     * cada vez que quieras registrar una acción.
     *
     * Uso desde cualquier DAO:
     *   LogHistorialDAO.registrar("javier", "INSERT", "CLIENTES", "Alta cliente ID 5");
     */
    public static void registrar(String usuario, String accion, String tabla, String detalles) {
        LogHistorial log = new LogHistorial();
        log.setUsuario(usuario);
        log.setAccion(accion);
        log.setTabla(tabla);
        log.setDetalles(detalles);
        log.setFecha(new Timestamp(System.currentTimeMillis()));
        new LogHistorialDAO().registrarAccion(log);
    }
}