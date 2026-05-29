package vistas;

import DAO.AlertasDAO;
import DAO.LogHistorialDAO;
import Modelos.LogHistorial;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelHistorial extends JPanel {

    private final MainFrame       mainFrame;
    private final LogHistorialDAO logDAO     = new LogHistorialDAO();
    private final AlertasDAO      alertasDAO = new AlertasDAO();

    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtBuscar;
    private JComboBox<String> cbFiltro;
    private JLabel            lblContador;

    private static final String[] COLUMNAS = { "ID", "Usuario", "Accion", "Tabla", "Fecha", "Detalles" };

    private static final Color BG_PRINCIPAL  = new Color(15,  23,  42);
    private static final Color BG_HEADER     = new Color(22,  28,  40);
    private static final Color BG_FILA_PAR   = new Color(22,  28,  40);
    private static final Color BG_FILA_IMPAR = new Color(30,  41,  59);
    private static final Color BG_SELECCION  = new Color(37,  99,  235, 90);
    private static final Color BG_INPUT      = new Color(30,  41,  59);
    private static final Color BORDE         = new Color(51,  65,  85);
    private static final Color TEXTO_BLANCO  = new Color(248, 250, 252);
    private static final Color TEXTO_NORMAL  = new Color(203, 213, 225);
    private static final Color TEXTO_GRIS    = new Color(100, 116, 139);
    private static final Color TEAL          = new Color(99,  202, 183);

    private static final Color COLOR_INSERT   = new Color(21,  128, 61);
    private static final Color COLOR_UPDATE   = new Color(37,  99,  235);
    private static final Color COLOR_DELETE   = new Color(185, 28,  28);
    private static final Color COLOR_CANCELAR = new Color(180, 83,  9);
    private static final Color COLOR_RENOVAR  = new Color(109, 40,  217);

    public PanelHistorial(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PRINCIPAL);
        add(construirHeader(), BorderLayout.NORTH);
        add(construirTabla(),  BorderLayout.CENTER);
        add(construirBarra(),  BorderLayout.SOUTH);
    }

    // =========================================================================
    //  HEADER
    // =========================================================================
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel izq = new JPanel(new GridLayout(2, 1, 0, 2));
        izq.setOpaque(false);
        JLabel lblTitulo = new JLabel("Historial de Actividad");
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSub = new JLabel("Registro completo de cambios, altas, bajas y cancelaciones del sistema");
        lblSub.setForeground(TEXTO_GRIS);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        izq.add(lblTitulo);
        izq.add(lblSub);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);

        txtBuscar = new JTextField(16);
        txtBuscar.setBackground(BG_INPUT);
        txtBuscar.setForeground(TEXTO_NORMAL);
        txtBuscar.setCaretColor(TEAL);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(7, 10, 7, 10)));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por usuario o detalle...");

        cbFiltro = new JComboBox<>(new String[]{ "Todos", "CLIENTES", "VEHICULOS", "POLIZAS", "PAGOS" });
        cbFiltro.setBackground(BG_INPUT);
        cbFiltro.setForeground(TEXTO_NORMAL);
        cbFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbFiltro.setPreferredSize(new Dimension(120, 34));

        JButton btnBuscar    = crearBotonUtil("Buscar");
        JButton btnRefrescar = crearBotonUtil("Refrescar");
        JButton btnAlertas   = crearBotonUtil("Ver Alertas");

        txtBuscar.addActionListener(e -> aplicarFiltros());
        btnBuscar.addActionListener(e -> aplicarFiltros());
        cbFiltro.addActionListener(e  -> aplicarFiltros());
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText(""); cbFiltro.setSelectedIndex(0); cargarDatos();
        });
        btnAlertas.addActionListener(e -> abrirDialogoAlertas());

        der.add(txtBuscar); der.add(cbFiltro); der.add(btnBuscar); der.add(btnRefrescar); der.add(btnAlertas);
        header.add(izq, BorderLayout.WEST);
        header.add(der, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    //  TABLA
    // =========================================================================
    private JPanel construirTabla() {
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(BG_PRINCIPAL);
        cuerpo.setBorder(new EmptyBorder(16, 26, 8, 26));

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        tabla = new JTable(modeloTabla);
        aplicarEstiloTabla();

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scroll.getViewport().setBackground(BG_FILA_PAR);
        scroll.setBackground(BG_HEADER);
        scroll.getVerticalScrollBar().setBackground(BG_HEADER);

        cuerpo.add(scroll, BorderLayout.CENTER);
        return cuerpo;
    }

    private void aplicarEstiloTabla() {
        tabla.setBackground(BG_FILA_PAR);
        tabla.setForeground(TEXTO_NORMAL);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(36);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(BG_SELECCION);
        tabla.setSelectionForeground(TEXTO_BLANCO);
        tabla.setFocusable(false);
        tabla.setAutoCreateRowSorter(true);

        JTableHeader th = tabla.getTableHeader();
        th.setBackground(BG_PRINCIPAL);
        th.setForeground(TEXTO_GRIS);
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer hr = (DefaultTableCellRenderer) th.getDefaultRenderer();
        hr.setHorizontalAlignment(SwingConstants.LEFT);
        hr.setBorder(new EmptyBorder(0, 12, 0, 0));

        tabla.getColumnModel().getColumn(0).setMaxWidth(55);
        tabla.getColumnModel().getColumn(0).setMinWidth(45);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(400);

        DefaultTableCellRenderer renderGeneral = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) { setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO); }
                else     { setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR); setForeground(TEXTO_NORMAL); }
                return this;
            }
        };

        tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(sel ? TEXTO_BLANCO : TEAL);
                setBackground(sel ? BG_SELECCION : (row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR));
                setBorder(new EmptyBorder(0, 0, 0, 0));
                return this;
            }
        });

        for (int c : new int[]{1, 3, 4, 5}) tabla.getColumnModel().getColumn(c).setCellRenderer(renderGeneral);

        tabla.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                String accion = val != null ? val.toString() : "";
                if (!sel) {
                    Color bg = switch (accion.toUpperCase()) {
                        case "INSERT"                         -> COLOR_INSERT;
                        case "UPDATE"                         -> COLOR_UPDATE;
                        case "DELETE", "DELETE_FISICO", "DESACTIVAR" -> COLOR_DELETE;
                        case "CANCELAR"                       -> COLOR_CANCELAR;
                        case "RENOVAR"                        -> COLOR_RENOVAR;
                        default                               -> new Color(51, 65, 85);
                    };
                    setBackground(bg);
                    setForeground(Color.WHITE);
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                } else { setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO); }
                return this;
            }
        });
    }

    private JPanel construirBarra() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(BG_HEADER);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(2, 16, 2, 16)));
        lblContador = new JLabel("0 registros");
        lblContador.setForeground(TEXTO_GRIS);
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeloTabla.addTableModelListener(e ->
            lblContador.setText(modeloTabla.getRowCount() + " registro(s)"));
        barra.add(lblContador);
        return barra;
    }

    // =========================================================================
    //  CARGA Y FILTRADO
    // =========================================================================
    public void cargarDatos() {
        new SwingWorker<List<LogHistorial>, Void>() {
            @Override protected List<LogHistorial> doInBackground() { return logDAO.obtenerTodosLosLogs(); }
            @Override protected void done() {
                try { llenarTabla(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String tablaFiltro = (String) cbFiltro.getSelectedItem();
        boolean filtrarTabla = !"Todos".equals(tablaFiltro);
        new SwingWorker<List<LogHistorial>, Void>() {
            @Override protected List<LogHistorial> doInBackground() {
                return filtrarTabla ? logDAO.obtenerLogsPorTabla(tablaFiltro) : logDAO.obtenerTodosLosLogs();
            }
            @Override protected void done() {
                try {
                    List<LogHistorial> lista = get();
                    if (!texto.isEmpty()) {
                        lista.removeIf(l ->
                            !l.getUsuario().toLowerCase().contains(texto) &&
                            !(l.getDetalles() != null && l.getDetalles().toLowerCase().contains(texto)));
                    }
                    llenarTabla(lista);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void llenarTabla(List<LogHistorial> lista) {
        modeloTabla.setRowCount(0);
        for (LogHistorial l : lista) {
            modeloTabla.addRow(new Object[]{
                l.getIdLog(), l.getUsuario(), l.getAccion(), l.getTabla(),
                l.getFecha() != null ? l.getFecha().toString() : "",
                l.getDetalles() != null ? l.getDetalles() : ""
            });
        }
    }

    // =========================================================================
    //  DIALOGO ALERTAS
    // =========================================================================
    private void abrirDialogoAlertas() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Alertas y Vencimientos", false);
        dlg.setSize(820, 560);
        dlg.setLocationRelativeTo(this);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_PRINCIPAL);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBorder(new EmptyBorder(8, 12, 12, 12));

        tabs.addTab("Urgentes (0-3 dias)",
            construirTabAlerta(alertasDAO.consultarAlertasUrgentes(),
                new String[]{"Cliente","Telefono","Correo","No. Poliza","Parcialidad","Vence","Dias"},
                new Color(185, 28, 28)));
        tabs.addTab("Proximos (4-10 dias)",
            construirTabAlerta(alertasDAO.consultarPagosSubsecuentes(),
                new String[]{"Cliente","Telefono","Correo","No. Poliza","Parcialidad","Vence","Dias"},
                new Color(180, 83, 9)));
        tabs.addTab("Vencimientos poliza",
            construirTabAlerta(alertasDAO.consultarVencimientos(),
                new String[]{"No. Poliza","Cliente","Parcialidad","Fecha Vencimiento"},
                new Color(37, 99, 235)));
        tabs.addTab("Renovaciones (15 dias o menos)",
            construirTabAlerta(alertasDAO.consultarRenovaciones(),
                new String[]{"Cliente","Telefono","Correo","No. Poliza","Compania","Expira","Dias"},
                new Color(21, 128, 61)));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(51, 65, 85));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnCerrar.addActionListener(e -> dlg.dispose());

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        sur.setBackground(BG_HEADER);
        sur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));
        sur.add(btnCerrar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRINCIPAL);
        panel.add(tabs, BorderLayout.CENTER);
        panel.add(sur,  BorderLayout.SOUTH);
        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private JPanel construirTabAlerta(List<String[]> datos, String[] columnas, Color acento) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(BG_PRINCIPAL);
        panel.setBorder(new EmptyBorder(8, 6, 6, 6));

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] fila : datos) modelo.addRow(fila);

        JTable t = new JTable(modelo);
        t.setBackground(BG_FILA_PAR); t.setForeground(TEXTO_NORMAL);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13)); t.setRowHeight(34);
        t.setShowGrid(false); t.setFocusable(false); t.setAutoCreateRowSorter(true);
        t.setSelectionBackground(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 80));
        t.setSelectionForeground(TEXTO_BLANCO);
        t.getTableHeader().setBackground(BG_HEADER); t.getTableHeader().setForeground(acento);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));

        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scroll.getViewport().setBackground(BG_FILA_PAR);

        JLabel lbl = new JLabel(datos.size() + " registro(s)");
        lbl.setForeground(TEXTO_GRIS);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setBorder(new EmptyBorder(4, 2, 0, 0));

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lbl,    BorderLayout.SOUTH);
        return panel;
    }

    private JButton crearBotonUtil(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(45, 55, 72));
        btn.setForeground(TEXTO_NORMAL);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }
}
