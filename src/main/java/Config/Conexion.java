package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de conexión a la base de datos MySQL.
 * Centraliza los parámetros de conexión para todo el proyecto.
 *
 * USO: siempre dentro de try-with-resources para que la conexión
 * se cierre automáticamente y no haya fugas de conexión.
 *
 *   try (Connection con = Conexion.getConnection()) {
 *       // usar con ...
 *   }
 */
public class Conexion {

  
    private static final String HOST      = "localhost";
    private static final String PUERTO    = "3306";
    private static final String BASE_DATOS = "segurosmurillo";
    private static final String USUARIO   = "root";
    private static final String PASSWORD  = "72852";          

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DATOS
            + "?useSSL=false"
            + "&serverTimezone=America/Mexico_City"
            + "&allowPublicKeyRetrieval=true"
            + "&useUnicode=true"
            + "&characterEncoding=UTF-8";

   
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

   
    public static boolean probarConexion() {
        try (Connection con = getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println(" Conexión a '" + BASE_DATOS + "' exitosa.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Error al conectar con la BD: " + e.getMessage());
            System.err.println("  Verifica: host=" + HOST + " | puerto=" + PUERTO + " | bd=" + BASE_DATOS + " | usuario=" + USUARIO);
        }
        return false;
    }
}