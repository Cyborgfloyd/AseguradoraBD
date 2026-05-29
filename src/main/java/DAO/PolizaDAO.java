
package DAO;

import config.Conexion;
import Modelos.Poliza;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolizaDAO {

    // 1. Insertar Póliza (Tu método original, mantenido)
    public void registrarPoliza(Poliza p) {
        String sql = "INSERT INTO polizas (no_poliza, id_cliente, id_vehiculo, id_compania, id_cobertura, fecha_inicio, cfdi, estatus) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNoPoliza());
            ps.setInt(2, p.getIdCliente());
            ps.setInt(3, p.getIdVehiculo());
            ps.setInt(4, p.getIdCompania());
            ps.setInt(5, p.getIdCobertura());
            ps.setString(6, p.getFechaInicio());
            ps.setBoolean(7, p.isCfdi());
            ps.setString(8, p.getEstatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cancela póliza via SP (también cancela pagos pendientes)
    public void cancelarPolizaSegura(String noPoliza) {
        String sql = "{CALL cancelar_poliza_segura(?)}";
        try (Connection con = Conexion.getConnection();
                CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, noPoliza);
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar poliza: " + e.getMessage(), e);
        }
    }

    public void renovar(String noPolizaVieja, String noPolizaNueva, String nuevaFecha) {
        String sql = "{CALL renovar_poliza(?, ?, ?)}";
        try (Connection con = Conexion.getConnection();
                CallableStatement cs = con.prepareCall(sql)) {
            cs.setString(1, noPolizaVieja);
            cs.setString(2, noPolizaNueva);
            cs.setDate(3, java.sql.Date.valueOf(nuevaFecha));
            cs.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error al renovar poliza: " + e.getMessage(), e);
        }
    }

    public void eliminarFisico(String noPoliza) {
        String sql = "DELETE FROM polizas WHERE no_poliza = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noPoliza);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar poliza: " + e.getMessage(), e);
        }
    }

    // 4. Método para consultar renovaciones (Uso de CallableStatement)
    public void consultarRenovaciones() {
        String sql = "{CALL consultar_renovaciones()}";
        try (Connection con = Conexion.getConnection();
                CallableStatement cs = con.prepareCall(sql);
                ResultSet rs = cs.executeQuery()) {
            // Aquí procesas el ResultSet para llenar un modelo de tabla
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelarPoliza(String noPoliza) {
        String sql = "UPDATE polizas SET estatus = 'Cancelada' WHERE no_poliza = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noPoliza);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// MODIFICAR (Solo datos generales)
    public void actualizar(Poliza p) {
        String sql = "UPDATE polizas SET id_compania=?, id_cobertura=?, cfdi=? WHERE no_poliza=?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdCompania());
            ps.setInt(2, p.getIdCobertura());
            ps.setBoolean(3, p.isCfdi());
            ps.setString(4, p.getNoPoliza());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Asegúrate de usar la importación correcta al inicio del archivo:
// import com.mycompany.segurosmurillo.Conexion;
    public List<Poliza> listarPolizas() {
        List<Poliza> lista = new ArrayList<>();
        String sql =
            "SELECT p.no_poliza, p.id_cliente, p.id_vehiculo, p.id_compania, p.id_cobertura, " +
            "       p.fecha_inicio, p.cfdi, p.estatus, " +
            "       CONCAT(c.nombre,' ',IFNULL(c.apellido_p,''),' ',IFNULL(c.apellido_m,'')) AS nombre_cliente, " +
            "       v.modelo, v.placas, co.compania, tc.nombre_cobertura " +
            "FROM polizas p " +
            "JOIN clientes c       ON p.id_cliente  = c.id_cliente " +
            "JOIN vehiculos v      ON p.id_vehiculo = v.id_vehiculo " +
            "JOIN companias co     ON p.id_compania = co.id_compania " +
            "JOIN tipos_cobertura tc ON p.id_cobertura = tc.id_cobertura " +
            "WHERE c.estado = 'Activo' " +
            "ORDER BY p.no_poliza";

        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Poliza p = new Poliza();
                p.setNoPoliza(rs.getString("no_poliza"));
                p.setIdCliente(rs.getInt("id_cliente"));
                p.setIdVehiculo(rs.getInt("id_vehiculo"));
                p.setIdCompania(rs.getInt("id_compania"));
                p.setIdCobertura(rs.getInt("id_cobertura"));
                p.setFechaInicio(rs.getDate("fecha_inicio") != null
                    ? rs.getDate("fecha_inicio").toLocalDate().toString() : null);
                p.setCfdi(rs.getBoolean("cfdi"));
                p.setEstatus(rs.getString("estatus"));
                p.setNombreCliente(rs.getString("nombre_cliente").trim());
                p.setModeloVehiculo(rs.getString("modelo"));
                p.setPlacasVehiculo(rs.getString("placas"));
                p.setNombreCompania(rs.getString("compania"));
                p.setNombreCobertura(rs.getString("nombre_cobertura"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Poliza buscarPorNoPoliza(String noPoliza) {
        String sql =
            "SELECT p.*, " +
            "CONCAT(c.nombre,' ',IFNULL(c.apellido_p,''),' ',IFNULL(c.apellido_m,'')) AS nombre_cliente, " +
            "v.modelo, v.placas, co.compania, tc.nombre_cobertura " +
            "FROM polizas p " +
            "JOIN clientes c       ON p.id_cliente  = c.id_cliente " +
            "JOIN vehiculos v      ON p.id_vehiculo = v.id_vehiculo " +
            "JOIN companias co     ON p.id_compania = co.id_compania " +
            "JOIN tipos_cobertura tc ON p.id_cobertura = tc.id_cobertura " +
            "WHERE p.no_poliza = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noPoliza);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Poliza p = new Poliza();
                    p.setNoPoliza(rs.getString("no_poliza"));
                    p.setIdCliente(rs.getInt("id_cliente"));
                    p.setIdVehiculo(rs.getInt("id_vehiculo"));
                    p.setIdCompania(rs.getInt("id_compania"));
                    p.setIdCobertura(rs.getInt("id_cobertura"));
                    p.setFechaInicio(rs.getDate("fecha_inicio") != null
                        ? rs.getDate("fecha_inicio").toLocalDate().toString() : null);
                    p.setCfdi(rs.getBoolean("cfdi"));
                    p.setEstatus(rs.getString("estatus"));
                    p.setNombreCliente(rs.getString("nombre_cliente").trim());
                    p.setModeloVehiculo(rs.getString("modelo"));
                    p.setPlacasVehiculo(rs.getString("placas"));
                    p.setNombreCompania(rs.getString("compania"));
                    p.setNombreCobertura(rs.getString("nombre_cobertura"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
