/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vistas;

import com.formdev.flatlaf.FlatDarkLaf;
import config.Conexion;
import javax.swing.*;

/**
 * Punto de entrada principal del sistema.
 * Configura FlatLaf y verifica la conexión antes de lanzar la ventana.
 *
 * Para que FlatLaf funcione debes tener el JAR en tu proyecto NetBeans:
 *   Clic derecho en el proyecto → Properties → Libraries → Add JAR
 *   Descarga: https://github.com/JFormDesigner/FlatLaf/releases
 *   (busca flatlaf-X.X.jar)
 */
public class App {

    public static void main(String[] args) {

        // ── 1. Configurar FlatLaf ANTES de cualquier componente Swing ─────────
        try {
            FlatDarkLaf.setup();

            // Ajustes globales de estilo
            UIManager.put("Button.arc",           10);
            UIManager.put("Component.arc",        8);
            UIManager.put("TextComponent.arc",    6);
            UIManager.put("ScrollBar.thumbArc",   999);
            UIManager.put("ScrollBar.width",      8);
            UIManager.put("Table.showHorizontalLines", false);
            UIManager.put("Table.showVerticalLines",   false);

        } catch (Exception e) {
            // Si FlatLaf no está disponible, usa el Look&Feel del sistema
            System.err.println("FlatLaf no encontrado — usando L&F por defecto.");
            System.err.println("Descarga el JAR en: https://github.com/JFormDesigner/FlatLaf/releases");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // ── 2. Verificar conexión a la BD ─────────────────────────────────────
        if (!Conexion.probarConexion()) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar a la base de datos.\n\n"
                + "Verifica que:\n"
                + "  • MySQL esté corriendo\n"
                + "  • El archivo config/Conexion.java tenga los datos correctos\n"
                + "  • La base de datos 'segurosmurillo' exista",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── 3. Lanzar la ventana en el hilo de eventos de Swing ───────────────
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}