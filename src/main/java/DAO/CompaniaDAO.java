/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;


import config.Conexion;
import Modelos.Compania;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompaniaDAO {
    public List<Compania> listar() {
        List<Compania> lista = new ArrayList<>();
        String sql = "SELECT * FROM companias";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Compania(rs.getInt("id_compania"), rs.getString("compania")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}