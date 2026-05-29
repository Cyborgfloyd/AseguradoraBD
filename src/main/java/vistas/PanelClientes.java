package vistas;

import Controladores.ClienteController;
import Modelos.Cliente;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class PanelClientes extends JPanel {

    private final MainFrame          mainFrame;
    private final ClienteController  controller = new ClienteController();

    // ── Tabla ──────────────────────────────────────────────────────────────
    private JTable             tabla;
    private DefaultTableModel  modeloTabla;

    // ── Barra de búsqueda ──────────────────────────────────────────────────
    private JTextField txtBuscar;

    // ── Contador de registros ──────────────────────────────────────────────
    private JLabel lblContador;

    // ── Columnas de la tabla ───────────────────────────────────────────────
    private static final String[] COLUMNAS = {
        "ID", "Nombre", "Apellido P.", "Apellido M.", "Teléfono 1", "Teléfono 2", "Correo"
    };

    // ── Paleta de colores ──────────────────────────────────────────────────
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

    
    public PanelClientes(MainFrame mainFrame) {
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

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        // Lado izquierdo: título y subtítulo
        JPanel izq = new JPanel(new GridLayout(2, 1, 0, 2));
        izq.setOpaque(false);

        JLabel lblTitulo = new JLabel("Clientes");
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblSub = new JLabel("Gestión de clientes activos del sistema");
        lblSub.setForeground(TEXTO_GRIS);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        izq.add(lblTitulo);
        izq.add(lblSub);

        // Lado derecho: buscador + botones de utilidad
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
        // FlatLaf soporta esta propiedad para placeholder nativo
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o ID…");

        JButton btnBuscar    = crearBotonUtil("Buscar");
        JButton btnRefrescar = crearBotonUtil("Recargar");

        btnBuscar.addActionListener(e -> filtrarTabla(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> filtrarTabla(txtBuscar.getText()));
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarDatos();
        });

        der.add(txtBuscar);
        der.add(btnBuscar);
        der.add(btnRefrescar);

        header.add(izq,  BorderLayout.WEST);
        header.add(der, BorderLayout.EAST);
        return header;
    }

    // ── Tabla ───────────────────────────────────────────────────────────────
    private JPanel construirTabla() {
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(BG_PRINCIPAL);
        cuerpo.setBorder(new EmptyBorder(16, 26, 8, 26));

        // Modelo de tabla — celdas NO editables directamente
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Integer.class : String.class;
            }
        };

        tabla = new JTable(modeloTabla);
        aplicarEstiloTabla();

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scroll.getViewport().setBackground(BG_FILA_PAR);
        scroll.setBackground(BG_HEADER);

        // Barra de desplazamiento discreta
        scroll.getVerticalScrollBar().setBackground(BG_HEADER);
        scroll.getHorizontalScrollBar().setBackground(BG_HEADER);

        cuerpo.add(scroll, BorderLayout.CENTER);
        return cuerpo;
    }

    private void aplicarEstiloTabla() {
        // ── Tabla general ──────────────────────────────────────────────────
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
        tabla.setAutoCreateRowSorter(true); // permite ordenar al clic en columna

        // ── Header de columnas ─────────────────────────────────────────────
        JTableHeader th = tabla.getTableHeader();
        th.setBackground(BG_PRINCIPAL);
        th.setForeground(TEXTO_GRIS);
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 40));
        th.setResizingAllowed(true);
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) th.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBorder(new EmptyBorder(0, 12, 0, 0));

        // ── Anchos de columna ──────────────────────────────────────────────
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(0).setMinWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140); // Nombre
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130); // Ape P
        tabla.getColumnModel().getColumn(3).setPreferredWidth(130); // Ape M
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Tel 1
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120); // Tel 2
        tabla.getColumnModel().getColumn(6).setPreferredWidth(180); // Correo

        // ── Renderer de filas alternadas ───────────────────────────────────
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focused, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (selected) {
                    setBackground(BG_SELECCION);
                    setForeground(TEXTO_BLANCO);
                } else {
                    setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR);
                    setForeground(TEXTO_NORMAL);
                }
                return this;
            }
        });

        // Renderer para la columna ID (centrado)
        tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focused, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(selected ? TEXTO_BLANCO : TEAL);
                setBackground(selected ? BG_SELECCION : (row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR));
                setBorder(new EmptyBorder(0, 0, 0, 0));
                return this;
            }
        });
    }

    // ── Barra de acciones ───────────────────────────────────────────────────
    private JPanel construirAcciones() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(BG_HEADER);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(2, 16, 2, 16)
        ));

        JButton btnAgregar    = crearBotonAccion("  Agregar",    VERDE);
        JButton btnEditar     = crearBotonAccion("  Editar",     AZUL);
        JButton btnDesactivar = crearBotonAccion("  Desactivar", AMBAR);

        btnAgregar.addActionListener(e    -> abrirDialogo(null));
        btnEditar.addActionListener(e     -> accionEditar());
        btnDesactivar.addActionListener(e -> accionDesactivar());

        lblContador = new JLabel("0 registros");
        lblContador.setForeground(TEXTO_GRIS);
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Actualizar contador cada vez que cambie el modelo
        modeloTabla.addTableModelListener(e ->
            lblContador.setText(modeloTabla.getRowCount() + " registro(s)"));

        barra.add(btnAgregar);
        barra.add(btnEditar);
        barra.add(btnDesactivar);
        barra.add(Box.createHorizontalStrut(20));
        barra.add(lblContador);

        return barra;
    }

   
    public void cargarDatos() {
        // Ejecutar en hilo aparte para no bloquear la UI en tablas grandes
        new SwingWorker<List<Cliente>, Void>() {
            @Override
            protected List<Cliente> doInBackground() {
                return controller.obtenerActivos();
            }
            @Override
            protected void done() {
                try {
                    modeloTabla.setRowCount(0);
                    for (Cliente c : get()) {
                        modeloTabla.addRow(new Object[]{
                            c.getIdCliente(),
                            c.getNombre(),
                            c.getApellidoP(),
                            c.getApellidoM()    != null ? c.getApellidoM()    : "",
                            c.getTelefono1(),
                            c.getTelefono2()    != null ? c.getTelefono2()    : "",
                            c.getCorreo()       != null ? c.getCorreo()       : ""
                        });
                    }
                } catch (Exception ex) {
                    mostrarError("Error al cargar clientes: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarDatos();
            return;
        }
        String filtro = texto.trim().toLowerCase();
        modeloTabla.setRowCount(0);

        for (Cliente c : controller.obtenerActivos()) {
            boolean coincide =
                String.valueOf(c.getIdCliente()).contains(filtro) ||
                c.getNombre().toLowerCase().contains(filtro)      ||
                c.getApellidoP().toLowerCase().contains(filtro)   ||
                (c.getApellidoM() != null && c.getApellidoM().toLowerCase().contains(filtro));

            if (coincide) {
                modeloTabla.addRow(new Object[]{
                    c.getIdCliente(),
                    c.getNombre(),
                    c.getApellidoP(),
                    c.getApellidoM()    != null ? c.getApellidoM()    : "",
                    c.getTelefono1(),
                    c.getTelefono2()    != null ? c.getTelefono2()    : "",
                    c.getCorreo()       != null ? c.getCorreo()       : ""
                });
            }
        }
    }

    // =========================================================================
    //  ACCIONES DE LA BARRA
    // =========================================================================
    private void accionEditar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            avisar("Selecciona un cliente de la tabla primero.");
            return;
        }
        // Convertir fila de vista a fila de modelo (necesario cuando hay sorting)
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int id = (int) modeloTabla.getValueAt(filaModelo, 0);
        Cliente c = controller.buscarPorId(id);
        if (c != null) abrirDialogo(c);
    }

    private void accionDesactivar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            avisar("Selecciona un cliente de la tabla primero.");
            return;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        int    id     = (int)    modeloTabla.getValueAt(filaModelo, 0);
        String nombre = modeloTabla.getValueAt(filaModelo, 1)
                      + " " + modeloTabla.getValueAt(filaModelo, 2);

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "<html>¿Desactivar al cliente <b>" + nombre + "</b>?<br>"
            + "<small>El cliente quedará inactivo pero NO se borrará de la BD.</small></html>",
            "Confirmar desactivación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            String error = controller.desactivar(id, mainFrame.getUsernameActual());
            if (error == null) {
                cargarDatos();
            } else {
                mostrarError(error);
            }
        }
    }

    // =========================================================================
    //  DIÁLOGO AGREGAR / EDITAR
    // =========================================================================
    private void abrirDialogo(Cliente clienteExistente) {
        boolean esNuevo = (clienteExistente == null);
        String titulo   = esNuevo ? "Agregar cliente" : "Editar cliente";

        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), titulo, true);
        dlg.setSize(480, 430);
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

        // Crear campos con valores actuales si es edición
        JTextField fNombre = campoForm(esNuevo ? "" : clienteExistente.getNombre());
        JTextField fApP    = campoForm(esNuevo ? "" : clienteExistente.getApellidoP());
        JTextField fApM    = campoForm(esNuevo ? "" : nvl(clienteExistente.getApellidoM()));
        JTextField fTel1   = campoForm(esNuevo ? "" : clienteExistente.getTelefono1());
        JTextField fTel2   = campoForm(esNuevo ? "" : nvl(clienteExistente.getTelefono2()));
        JTextField fCorreo = campoForm(esNuevo ? "" : nvl(clienteExistente.getCorreo()));

        int row = 0;
        agregarFila(panel, gbc, row++, "Nombre *",           fNombre);
        agregarFila(panel, gbc, row++, "Apellido paterno *", fApP);
        agregarFila(panel, gbc, row++, "Apellido materno",   fApM);
        agregarFila(panel, gbc, row++, "Teléfono 1 *",       fTel1);
        agregarFila(panel, gbc, row++, "Teléfono 2",         fTel2);
        agregarFila(panel, gbc, row++, "Correo",             fCorreo);

        // Label de error
        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblErr, gbc);

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnCancelar = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnGuardar  = crearBotonAccion(esNuevo ? "Agregar" : "Guardar", VERDE);

        btnCancelar.addActionListener(e -> dlg.dispose());

        btnGuardar.addActionListener(e -> {
            // Construir objeto Cliente con los datos del formulario
            Cliente c = new Cliente();
            if (!esNuevo) c.setIdCliente(clienteExistente.getIdCliente());
            c.setNombre(   fNombre.getText().trim().toUpperCase());
            c.setApellidoP(fApP.getText().trim().toUpperCase());
            c.setApellidoM(fApM.getText().trim().toUpperCase());
            c.setTelefono1(fTel1.getText().trim());
            c.setTelefono2(fTel2.getText().trim());
            c.setCorreo(   fCorreo.getText().trim().toLowerCase());

            // Delegar validación y guardado al controller
            String resultado = esNuevo
                ? controller.agregar(c, mainFrame.getUsernameActual())
                : controller.actualizar(c, mainFrame.getUsernameActual());

            if (resultado == null) {
                // null = éxito
                dlg.dispose();
                cargarDatos();
            } else {
                // Hay error — mostrarlo en el label del formulario
                lblErr.setText("Error  " + resultado);
            }
        });

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dlg.setVisible(true);
    }

    // =========================================================================
    //  HELPERS DE UI
    // =========================================================================
    private void agregarFila(JPanel panel, GridBagConstraints gbc,
                              int row, String label, JTextField campo) {
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
        JOptionPane.showMessageDialog(
            this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Null-safe: retorna "" si el valor es null. */
    private String nvl(String valor) {
        return valor != null ? valor : "";
    }
}