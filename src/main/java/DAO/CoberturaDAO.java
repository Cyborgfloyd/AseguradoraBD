/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;


import Modelos.Cobertura;
import config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoberturaDAO {
    
    public List<Cobertura> listarTodas() {
        List<Cobertura> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipos_cobertura"; // Ajusta el nombre de la tabla según tu SQL
        
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Cobertura(
                    rs.getInt("id_cobertura"), 
                    rs.getString("nombre_cobertura")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar coberturas: " + e.getMessage());
        }
        return lista;
    }
}