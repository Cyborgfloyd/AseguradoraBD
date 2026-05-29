package vistas;

import Controladores.VehiculoController;
import DAO.ClienteDAO;
import Modelos.Cliente;
import Modelos.Vehiculo;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelVehiculos extends JPanel {

    private final MainFrame            mainFrame;
    private final VehiculoController   controller = new VehiculoController();
    private final ClienteDAO           clienteDAO = new ClienteDAO();

    // ── Tabla ──────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    // ── Barra de búsqueda ──────────────────────────────────────────────────
    private JTextField txtBuscar;

    // ── Contador ───────────────────────────────────────────────────────────
    private JLabel lblContador;

    private static final String[] COLUMNAS = {
        "ID", "Modelo", "Placas", "Propietario"
    };

    // ── Paleta (idéntica a PanelClientes) ─────────────────────────────────
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
    private static final Color VERDE         = new Color(21,  128, 61);
    private static final Color AZUL          = new Color(37,  99,  235);
    private static final Color AMBAR         = new Color(180, 83,  9);
    private static final Color ROJO          = new Color(239, 68,  68);

    public PanelVehiculos(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PRINCIPAL);
        add(construirHeader(),   BorderLayout.NORTH);
        add(construirTabla(),    BorderLayout.CENTER);
        add(construirAcciones(), BorderLayout.SOUTH);
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

        JLabel lblTitulo = new JLabel("Vehículos");
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblSub = new JLabel("Gestión de vehículos registrados en el sistema");
        lblSub.setForeground(TEXTO_GRIS);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        izq.add(lblTitulo);
        izq.add(lblSub);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);

        txtBuscar = new JTextField(22);
        txtBuscar.setBackground(BG_INPUT);
        txtBuscar.setForeground(TEXTO_NORMAL);
        txtBuscar.setCaretColor(TEAL);
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por modelo, placas o propietario…");

        JButton btnBuscar    = crearBotonUtil("Buscar");
        JButton btnRefrescar = crearBotonUtil("Recargar");

        btnBuscar.addActionListener(e    -> filtrarTabla(txtBuscar.getText()));
        txtBuscar.addActionListener(e    -> filtrarTabla(txtBuscar.getText()));
        btnRefrescar.addActionListener(e -> { txtBuscar.setText(""); cargarDatos(); });

        der.add(txtBuscar);
        der.add(btnBuscar);
        der.add(btnRefrescar);

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
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
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
        tabla.setRowHeight(38);
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

        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(0).setMinWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220); // Modelo
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Placas
        tabla.getColumnModel().getColumn(3).setPreferredWidth(280); // Propietario

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) {
                    setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO);
                } else {
                    setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR);
                    setForeground(TEXTO_NORMAL);
                }
                return this;
            }
        });

        // Columna ID: centrada y en teal
        tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(sel ? TEXTO_BLANCO : TEAL);
                setBackground(sel ? BG_SELECCION : (row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR));
                setBorder(new EmptyBorder(0, 0, 0, 0));
                return this;
            }
        });
    }

    // =========================================================================
    //  ACCIONES
    // =========================================================================
    private JPanel construirAcciones() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(BG_HEADER);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(2, 16, 2, 16)
        ));

        JButton btnAgregar    = crearBotonAccion("Agregar",    VERDE);
        JButton btnEditar     = crearBotonAccion("Editar",     AZUL);
        JButton btnDesactivar = crearBotonAccion("Desactivar", AMBAR);

        btnAgregar.addActionListener(e    -> abrirDialogo(null));
        btnEditar.addActionListener(e     -> accionEditar());
        btnDesactivar.addActionListener(e -> accionDesactivar());

        lblContador = new JLabel("0 registros");
        lblContador.setForeground(TEXTO_GRIS);
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        modeloTabla.addTableModelListener(e ->
            lblContador.setText(modeloTabla.getRowCount() + " registro(s)"));

        barra.add(btnAgregar);
        barra.add(btnEditar);
        barra.add(btnDesactivar);
        barra.add(Box.createHorizontalStrut(20));
        barra.add(lblContador);
        return barra;
    }

    // =========================================================================
    //  CARGA Y FILTRADO
    // =========================================================================
    public void cargarDatos() {
        new SwingWorker<List<Vehiculo>, Void>() {
            @Override
            protected List<Vehiculo> doInBackground() { return controller.obtenerTodos(); }
            @Override
            protected void done() {
                try {
                    modeloTabla.setRowCount(0);
                    for (Vehiculo v : get()) {
                        modeloTabla.addRow(new Object[]{
                            v.getIdVehiculo(),
                            v.getModelo(),
                            v.getPlacas(),
                            v.getNombreCliente() != null ? v.getNombreCliente() : ""
                        });
                    }
                } catch (Exception ex) {
                    mostrarError("Error al cargar vehículos: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) { cargarDatos(); return; }
        String filtro = texto.trim().toLowerCase();
        modeloTabla.setRowCount(0);
        for (Vehiculo v : controller.obtenerTodos()) {
            boolean coincide =
                String.valueOf(v.getIdVehiculo()).contains(filtro) ||
                v.getModelo().toLowerCase().contains(filtro)       ||
                v.getPlacas().toLowerCase().contains(filtro)       ||
                (v.getNombreCliente() != null && v.getNombreCliente().toLowerCase().contains(filtro));
            if (coincide) {
                modeloTabla.addRow(new Object[]{
                    v.getIdVehiculo(), v.getModelo(), v.getPlacas(),
                    v.getNombreCliente() != null ? v.getNombreCliente() : ""
                });
            }
        }
    }

    // =========================================================================
    //  ACCIONES DE BOTONES
    // =========================================================================
    private void accionEditar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona un vehículo de la tabla primero."); return; }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(filaModelo, 0);
        Vehiculo v = controller.buscarPorId(id);
        if (v != null) abrirDialogo(v);
    }

    private void accionDesactivar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona un vehículo de la tabla primero."); return; }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int    id     = (int) modeloTabla.getValueAt(filaModelo, 0);
        String modelo = (String) modeloTabla.getValueAt(filaModelo, 1);
        String placas = (String) modeloTabla.getValueAt(filaModelo, 2);

        int resp = JOptionPane.showConfirmDialog(this,
            "<html>¿Desactivar el vehículo <b>" + modelo + " (" + placas + ")</b>?<br>"
            + "<small>El vehículo quedará inactivo pero NO se borrará de la BD.</small></html>",
            "Confirmar desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String error = controller.desactivar(id, mainFrame.getUsernameActual());
            if (error == null) cargarDatos();
            else mostrarError(error);
        }
    }

    // =========================================================================
    //  DIÁLOGO AGREGAR / EDITAR
    // =========================================================================
    private void abrirDialogo(Vehiculo vehiculoExistente) {
        boolean esNuevo = (vehiculoExistente == null);
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            esNuevo ? "Agregar vehículo" : "Editar vehículo", true);
        dlg.setSize(460, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(28, 36, 28, 36));
        dlg.setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(5, 0, 5, 0);
        gbc.weightx = 1;

        // ComboBox de clientes
        List<Cliente> clientes = clienteDAO.listarTodos();
        JComboBox<String> cbCliente = new JComboBox<>();
        cbCliente.setBackground(BG_PRINCIPAL);
        cbCliente.setForeground(TEXTO_BLANCO);
        cbCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (Cliente c : clientes) {
            cbCliente.addItem(c.getIdCliente() + " — " + c.getNombre()
                + " " + (c.getApellidoP() != null ? c.getApellidoP() : ""));
        }
        // Pre-seleccionar cliente si es edición
        if (!esNuevo) {
            for (int i = 0; i < clientes.size(); i++) {
                if (clientes.get(i).getIdCliente() == vehiculoExistente.getIdCliente()) {
                    cbCliente.setSelectedIndex(i);
                    break;
                }
            }
        }

        JTextField fModelo = campoForm(esNuevo ? "" : vehiculoExistente.getModelo());
        JTextField fPlacas = campoForm(esNuevo ? "" : vehiculoExistente.getPlacas());

        int row = 0;
        agregarFila(panel, gbc, row++, "Propietario *", cbCliente);
        agregarFila(panel, gbc, row++, "Modelo *",      fModelo);
        agregarFila(panel, gbc, row++, "Placas *",      fPlacas);

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblErr, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancelar = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnGuardar  = crearBotonAccion(esNuevo ? "Agregar" : "Guardar", VERDE);
        btnCancelar.addActionListener(e -> dlg.dispose());

        btnGuardar.addActionListener(e -> {
            Vehiculo v = new Vehiculo();
            if (!esNuevo) v.setIdVehiculo(vehiculoExistente.getIdVehiculo());

            // Extraer id_cliente del item seleccionado
            String itemSeleccionado = (String) cbCliente.getSelectedItem();
            if (itemSeleccionado == null) { lblErr.setText("Selecciona un cliente."); return; }
            v.setIdCliente(Integer.parseInt(itemSeleccionado.split(" — ")[0].trim()));
            v.setModelo(fModelo.getText().trim().toUpperCase());
            v.setPlacas(fPlacas.getText().trim().toUpperCase());

            String resultado = esNuevo
                ? controller.agregar(v, mainFrame.getUsernameActual())
                : controller.actualizar(v, mainFrame.getUsernameActual());

            if (resultado == null) { dlg.dispose(); cargarDatos(); }
            else lblErr.setText("Error " + resultado);
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dlg.setVisible(true);
    }

    // =========================================================================
    //  HELPERS UI
    // =========================================================================
    private void agregarFila(JPanel panel, GridBagConstraints gbc,
                              int row, String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.38;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        panel.add(campo, gbc);
    }

    private JTextField campoForm(String valor) {
        JTextField txt = new JTextField(valor);
        txt.setBackground(BG_PRINCIPAL);
        txt.setForeground(TEXTO_BLANCO);
        txt.setCaretColor(TEAL);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        return txt;
    }

    private JButton crearBotonAccion(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JButton crearBotonUtil(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(new Color(45, 55, 72));
        btn.setForeground(TEXTO_NORMAL);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    private void avisar(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
