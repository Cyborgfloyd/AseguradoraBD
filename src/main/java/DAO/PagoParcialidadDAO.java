/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import config.Conexion;
import Modelos.PagoParcialidad;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoParcialidadDAO {

    public void registrarPago(PagoParcialidad p) {
        String sql = "INSERT INTO pagos_parcialidades (no_poliza, num_parcialidad, total_parcialidades, fecha_vencimiento, estatus) VALUES (?,?,?,?,?)";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNoPoliza());
            ps.setInt(2, p.getNumParcialidad());
            ps.setInt(3, p.getTotalParcialidades());
            ps.setDate(4, new java.sql.Date(p.getFechaVencimiento().getTime()));
            ps.setString(5, p.getEstatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PagoParcialidad> listarTodos() {
        List<PagoParcialidad> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos_parcialidades ORDER BY no_poliza, num_parcialidad ASC";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<PagoParcialidad> listarPorPoliza(String noPoliza) {
        List<PagoParcialidad> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos_parcialidades WHERE no_poliza = ? ORDER BY num_parcialidad ASC";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noPoliza);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private PagoParcialidad mapear(ResultSet rs) throws SQLException {
        PagoParcialidad p = new PagoParcialidad();
        p.setIdPago(rs.getInt("id_pago"));
        p.setNoPoliza(rs.getString("no_poliza"));
        p.setNumParcialidad(rs.getInt("num_parcialidad"));
        p.setTotalParcialidades(rs.getInt("total_parcialidades"));
        p.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
        p.setEstatus(rs.getString("estatus"));
        return p;
    }

    // 3. ACTUALIZAR ESTATUS (Para marcar un pago como "Pagado" cuando el cliente deposite)
    public void actualizarEstatus(int idPago, String nuevoEstatus) {
        String sql = "UPDATE pagos_parcialidades SET estatus = ? WHERE id_pago = ?";
        try (Connection con = Conexion.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstatus);
            ps.setInt(2, idPago);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
