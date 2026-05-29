package DAO;

import config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertasDAO {

    public List<String[]> consultarRenovaciones() {
        return ejecutar("{CALL consultar_renovaciones()}",
            "cliente", "telefono_1", "correo", "no_poliza", "compania", "fecha_expiracion", "dias_restantes");
    }

    public List<String[]> consultarVencimientos() {
        return ejecutar("{CALL consultar_vencimientos()}",
            "no_poliza", "nombre_cliente", "num_parcialidad", "fecha_vencimiento");
    }

    public List<String[]> consultarAlertasUrgentes() {
        return ejecutar("{CALL consultar_alertas_urgentes()}",
            "cliente", "telefono_1", "correo", "no_poliza", "num_parcialidad", "fecha_vencimiento", "dias_restantes");
    }

    public List<String[]> consultarPagosSubsecuentes() {
        return ejecutar("{CALL consultar_pagos_subsecuentes()}",
            "cliente", "telefono_1", "correo", "no_poliza", "num_parcialidad", "fecha_vencimiento", "dias_restantes");
    }

    private List<String[]> ejecutar(String sql, String... columnas) {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             CallableStatement cs = con.prepareCall(sql);
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                String[] fila = new String[columnas.length];
                for (int i = 0; i < columnas.length; i++) {
                    Object val = rs.getObject(columnas[i]);
                    fila[i] = val != null ? val.toString() : "";
                }
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error AlertasDAO [" + sql + "]: " + e.getMessage());
        }
        return lista;
    }
}
