
package DAO;

import Modelos.Cliente;
import config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements GenericDAO<Cliente> {

    @Override
    public void insertar(Cliente c) {
        String sql = "INSERT INTO clientes (nombre, apellido_p, apellido_m, telefono_1, telefono_2, correo) VALUES (?,?,?,?,?,?)";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellidoP());
            ps.setString(3, c.getApellidoM());
            ps.setString(4, c.getTelefono1());
            ps.setString(5, c.getTelefono2());
            ps.setString(6, c.getCorreo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
// 1. Listar todos los clientes activos
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE estado = 'Activo'";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt("id_cliente"));
                c.setNombre(rs.getString("nombre"));
                c.setApellidoP(rs.getString("apellido_p"));
                c.setApellidoM(rs.getString("apellido_m"));
                c.setTelefono1(rs.getString("telefono_1"));
                c.setTelefono2(rs.getString("telefono_2"));
                c.setCorreo(rs.getString("correo"));
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

// 2. Actualizar datos de un cliente
    public void actualizar(Cliente c) {
        String sql = "UPDATE clientes SET nombre=?, apellido_p=?, apellido_m=?, telefono_1=?, telefono_2=?, correo=? WHERE id_cliente=?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellidoP());
            ps.setString(3, c.getApellidoM());
            ps.setString(4, c.getTelefono1());
            ps.setString(5, c.getTelefono2());
            ps.setString(6, c.getCorreo());
            ps.setInt(7, c.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. BUSCAR POR ID
    @Override
    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        Cliente c = null;

        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new Cliente(
                            rs.getInt("id_cliente"),
                            rs.getString("nombre"),
                            rs.getString("apellido_p"),
                            rs.getString("apellido_m"),
                            rs.getString("telefono_1"),
                            rs.getString("telefono_2"),
                            rs.getString("correo")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
        }
        return c;
    }

    @Override
    public List<Cliente> listar() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    //  eliminar ahora cumple con el contrato realizando un DELETE físico
    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar físicamente: " + e.getMessage());
        }
    }

    //  Método Desactivar
    public void desactivar(int id) {
        String sql = "UPDATE clientes SET estado = 'Inactivo' WHERE id_cliente = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // BUG 4 SOLUCIONADO: Método rescatado de LogHistorialDAO y centralizado aquí bajo una transacción segura
    public boolean eliminarClienteConHistorial(int idCliente, String usuario) {
        String sqlBajaHistorico = "{CALL mover_cliente_a_historico(?, ?)}";
        // Nota: Si usas SQL directo en vez de procedimiento, reemplázalo con tu START TRANSACTION e INSERT SELECT

        Connection con = null;
        try {
            con = Conexion.getConnection();
            con.setAutoCommit(false); // Iniciamos transacción controlada desde Java

            try (PreparedStatement ps = con.prepareStatement(sqlBajaHistorico)) {
                ps.setInt(1, idCliente);
                ps.setString(2, usuario);
                ps.executeUpdate();
            }

            con.commit(); // Si todo sale bien, guardamos cambios
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
