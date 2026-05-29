package DAO;

import config.Conexion;
import Modelos.Vehiculo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT v.id_vehiculo, v.id_cliente, v.modelo, v.placas, " +
                     "CONCAT(c.nombre, ' ', IFNULL(c.apellido_p,''), ' ', IFNULL(c.apellido_m,'')) AS nombre_cliente " +
                     "FROM vehiculos v JOIN clientes c ON v.id_cliente = c.id_cliente " +
                     "WHERE v.estado = 'Activo' ORDER BY v.id_vehiculo";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vehiculo v = new Vehiculo(rs.getInt("id_vehiculo"), rs.getInt("id_cliente"),
                        rs.getString("modelo"), rs.getString("placas"));
                v.setNombreCliente(rs.getString("nombre_cliente").trim());
                lista.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Vehiculo> listarPorCliente(int idCliente) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE id_cliente = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Vehiculo(rs.getInt("id_vehiculo"), rs.getInt("id_cliente"),
                            rs.getString("modelo"), rs.getString("placas")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    // Dentro de VehiculoDAO.java

    public void actualizar(Vehiculo v) {
        String sql = "UPDATE vehiculos SET modelo=?, placas=? WHERE id_vehiculo=?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getModelo());
            ps.setString(2, v.getPlacas());
            ps.setInt(3, v.getIdVehiculo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para agregar un vehículo nuevo
    public void insertar(Vehiculo v) {
        String sql = "INSERT INTO vehiculos (id_cliente, modelo, placas) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, v.getIdCliente());
            ps.setString(2, v.getModelo());
            ps.setString(3, v.getPlacas());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para buscar un vehículo específico (muy útil para editar)
    public Vehiculo buscarPorId(int idVehiculo) {
        Vehiculo v = null;
        String sql = "SELECT * FROM vehiculos WHERE id_vehiculo = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Vehiculo(rs.getInt("id_vehiculo"),
                            rs.getInt("id_cliente"),
                            rs.getString("modelo"),
                            rs.getString("placas"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }
    // Método analítico solicitado para contar unidades por propietario

    public int contarVehiculosPorCliente(int idCliente) {
        String sql = "SELECT COUNT(*) FROM vehiculos WHERE id_cliente = ?";
        try (Connection con = config.Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void eliminar(int id) {
    // Corregido: Cambiado DELETE por UPDATE para aplicar baja lógica
    String sql = "UPDATE vehiculos SET estado = 'Inactivo' WHERE id_vehiculo = ?";
    
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setInt(1, id);
        int filasAfectadas = ps.executeUpdate();
        
        if (filasAfectadas > 0) {
            System.out.println("Vehículo desactivado (baja lógica) con éxito.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException("Error al intentar dar de baja lógica al vehículo: " + e.getMessage());
    }
}
}
