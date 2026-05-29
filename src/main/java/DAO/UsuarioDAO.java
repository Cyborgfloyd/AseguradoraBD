/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;


import config.Conexion;
import Modelos.UsuarioSistema;
import java.sql.*;

public class UsuarioDAO {

    /**
     * Valida si el usuario existe y si la contraseña es correcta.
     * Retorna el objeto UsuarioSistema si el login es exitoso, null si falla.
     */
    public UsuarioSistema validarLogin(String username, String password) {
    // Primero solo buscamos al usuario por su nombre
    String sql = "SELECT * FROM usuarios_sistema WHERE username = ?";
    
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setString(1, username);
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // Aquí obtienes el hash de la base de datos
                String hashGuardado = rs.getString("password_hash");
                
                // Aquí compararías el password ingresado con el hash (usando BCrypt, por ejemplo)
                if (password.equals(hashGuardado)) { // Solo para texto plano, cámbialo pronto
                    UsuarioSistema user = new UsuarioSistema();
                    user.setIdUsuario(rs.getInt("id_usuario"));
                    user.setUsername(rs.getString("username"));
                    user.setRol(rs.getString("rol"));
                    return user;
                }
            }
        }
    } catch (SQLException e) {
        System.err.println("Error en login: " + e.getMessage());
    }
    return null;
}
    
}