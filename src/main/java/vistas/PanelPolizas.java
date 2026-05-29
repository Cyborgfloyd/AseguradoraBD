package vistas;

import Controladores.PolizaController;
import DAO.ClienteDAO;
import DAO.CoberturaDAO;
import DAO.CompaniaDAO;
import DAO.PagoParcialidadDAO;
import DAO.VehiculoDAO;
import Modelos.*;
import Modelos.PagoParcialidad;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelPolizas extends JPanel {

    private final MainFrame          mainFrame;
    private final PolizaController   controller   = new PolizaController();
    private final ClienteDAO         clienteDAO   = new ClienteDAO();
    private final VehiculoDAO        vehiculoDAO  = new VehiculoDAO();
    private final CompaniaDAO        companiaDAO  = new CompaniaDAO();
    private final CoberturaDAO       coberturaDAO = new CoberturaDAO();
    private final PagoParcialidadDAO pagoDAO      = new PagoParcialidadDAO();

    // ── Tabla ──────────────────────────────────────────────────────────────
    private JTable            tabla;
    private DefaultTableModel modeloTabla;

    // ── Búsqueda y contador ────────────────────────────────────────────────
    private JTextField txtBuscar;
    private JLabel     lblContador;

    private static final String[] COLUMNAS = {
        "No. Póliza", "Cliente", "Vehículo", "Compañía", "Cobertura", "Fecha Inicio", "CFDI", "Estatus"
    };

    // ── Paleta ────────────────────────────────────────────────────────────
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
    private static final Color ROJO          = new Color(185, 28,  28);
    private static final Color ROJO_TEXTO    = new Color(239, 68,  68);
    private static final Color MORADO        = new Color(109, 40,  217);
    private static final Color VERDE_OSCURO  = new Color(6,   78,  59);

    // Colores para el badge de estatus
    private static final Color COLOR_ACTIVA    = new Color(21,  128, 61);
    private static final Color COLOR_VENCIDA   = new Color(180, 83,  9);
    private static final Color COLOR_CANCELADA = new Color(127, 29,  29);

    public PanelPolizas(MainFrame mainFrame) {
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
        JLabel lblTitulo = new JLabel("Pólizas");
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSub = new JLabel("Emisión, edición y cancelación de pólizas de seguro");
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
            new EmptyBorder(7, 10, 7, 10)));
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por no. poliza, cliente o compania...");

        JButton btnBuscar    = crearBotonUtil("Buscar");
        JButton btnRefrescar = crearBotonUtil("Refrescar");

        btnBuscar.addActionListener(e    -> filtrarTabla(txtBuscar.getText()));
        txtBuscar.addActionListener(e    -> filtrarTabla(txtBuscar.getText()));
        btnRefrescar.addActionListener(e -> { txtBuscar.setText(""); cargarDatos(); });

        der.add(txtBuscar); der.add(btnBuscar); der.add(btnRefrescar);
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
                return c == 6 ? Boolean.class : String.class;
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

        // Anchos de columna
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110); // No. Póliza
        tabla.getColumnModel().getColumn(1).setPreferredWidth(190); // Cliente
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150); // Vehículo
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100); // Compañía
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Cobertura
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100); // Fecha
        tabla.getColumnModel().getColumn(6).setMaxWidth(55);         // CFDI
        tabla.getColumnModel().getColumn(7).setPreferredWidth(95);  // Estatus

        // Renderer general de filas alternadas
        DefaultTableCellRenderer renderGeneral = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) { setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO); }
                else     { setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR); setForeground(TEXTO_NORMAL); }
                return this;
            }
        };
        for (int i = 0; i < COLUMNAS.length; i++) {
            if (i != 6 && i != 7) tabla.getColumnModel().getColumn(i).setCellRenderer(renderGeneral);
        }

        // Renderer de CFDI (checkbox)
        tabla.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JCheckBox cb = new JCheckBox();
                cb.setSelected(Boolean.TRUE.equals(val));
                cb.setHorizontalAlignment(SwingConstants.CENTER);
                cb.setBackground(sel ? BG_SELECCION : (row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR));
                cb.setOpaque(true);
                return cb;
            }
        });

        // Renderer de Estatus (badge de color)
        tabla.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                String estatus = val != null ? val.toString() : "";
                if (!sel) {
                    switch (estatus) {
                        case "Activa"    -> { setBackground(COLOR_ACTIVA);    setForeground(Color.WHITE); }
                        case "Vencida"   -> { setBackground(COLOR_VENCIDA);   setForeground(Color.WHITE); }
                        case "Cancelada" -> { setBackground(COLOR_CANCELADA); setForeground(Color.WHITE); }
                        default          -> { setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR); setForeground(TEXTO_NORMAL); }
                    }
                } else {
                    setBackground(BG_SELECCION);
                    setForeground(TEXTO_BLANCO);
                }
                return this;
            }
        });
    }

    // =========================================================================
    //  ACCIONES (barra inferior)
    // =========================================================================
    private JPanel construirAcciones() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(BG_HEADER);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(2, 16, 2, 16)));

        JButton btnAgregar     = crearBotonAccion("Nueva poliza",       VERDE);
        JButton btnEditar      = crearBotonAccion("Editar",              AZUL);
        JButton btnCancelar    = crearBotonAccion("Cancelar poliza",     ROJO);
        JButton btnPagos       = crearBotonAccion("Ver Pagos",           MORADO);
        JButton btnParcialidad = crearBotonAccion("Agregar Parcialidad", new Color(30, 90, 120));
        JButton btnRenovar     = crearBotonAccion("Renovar",             VERDE_OSCURO);
        JButton btnEliminar    = crearBotonAccion("Eliminar de BD",      new Color(100, 0, 0));
        btnEliminar.setVisible(false);

        btnAgregar.addActionListener(e     -> abrirDialogo(null));
        btnEditar.addActionListener(e      -> accionEditar());
        btnCancelar.addActionListener(e    -> accionCancelar());
        btnPagos.addActionListener(e       -> accionVerPagos());
        btnParcialidad.addActionListener(e -> accionAgregarParcialidad());
        btnRenovar.addActionListener(e     -> accionRenovar());
        btnEliminar.addActionListener(e    -> accionEliminarFisico());

        lblContador = new JLabel("0 registros");
        lblContador.setForeground(TEXTO_GRIS);
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeloTabla.addTableModelListener(e ->
            lblContador.setText(modeloTabla.getRowCount() + " registro(s)"));

        if ("Administrador".equals(mainFrame.getUsuarioActual() != null
                ? mainFrame.getUsuarioActual().getRol() : "")) {
            btnEliminar.setVisible(true);
        }

        barra.add(btnAgregar); barra.add(btnEditar); barra.add(btnCancelar);
        barra.add(btnPagos); barra.add(btnParcialidad); barra.add(btnRenovar);
        barra.add(btnEliminar);
        barra.add(Box.createHorizontalStrut(20));
        barra.add(lblContador);
        return barra;
    }

    // =========================================================================
    //  CARGA Y FILTRADO
    // =========================================================================
    public void cargarDatos() {
        new SwingWorker<List<Poliza>, Void>() {
            @Override protected List<Poliza> doInBackground() { return controller.obtenerTodas(); }
            @Override protected void done() {
                try {
                    modeloTabla.setRowCount(0);
                    for (Poliza p : get()) {
                        modeloTabla.addRow(new Object[]{
                            p.getNoPoliza(),
                            nvl(p.getNombreCliente()),
                            nvl(p.getModeloVehiculo()) + " — " + nvl(p.getPlacasVehiculo()),
                            nvl(p.getNombreCompania()),
                            nvl(p.getNombreCobertura()),
                            nvl(p.getFechaInicio()),
                            p.isCfdi(),
                            nvl(p.getEstatus())
                        });
                    }
                } catch (Exception ex) {
                    mostrarError("Error al cargar pólizas: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void filtrarTabla(String texto) {
        if (texto == null || texto.trim().isEmpty()) { cargarDatos(); return; }
        String f = texto.trim().toLowerCase();
        modeloTabla.setRowCount(0);
        for (Poliza p : controller.obtenerTodas()) {
            boolean coincide =
                p.getNoPoliza().toLowerCase().contains(f) ||
                (p.getNombreCliente() != null && p.getNombreCliente().toLowerCase().contains(f)) ||
                (p.getNombreCompania() != null && p.getNombreCompania().toLowerCase().contains(f)) ||
                (p.getPlacasVehiculo() != null && p.getPlacasVehiculo().toLowerCase().contains(f));
            if (coincide) modeloTabla.addRow(new Object[]{
                p.getNoPoliza(), nvl(p.getNombreCliente()),
                nvl(p.getModeloVehiculo()) + " — " + nvl(p.getPlacasVehiculo()),
                nvl(p.getNombreCompania()), nvl(p.getNombreCobertura()),
                nvl(p.getFechaInicio()), p.isCfdi(), nvl(p.getEstatus())
            });
        }
    }

    // =========================================================================
    //  ACCIONES DE BOTONES
    // =========================================================================
    private void accionEditar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una póliza de la tabla primero."); return; }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        String noPoliza = (String) modeloTabla.getValueAt(filaModelo, 0);
        Poliza p = controller.buscarPorNoPoliza(noPoliza);
        if (p != null) abrirDialogo(p);
    }

    private void accionCancelar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una póliza de la tabla primero."); return; }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        String noPoliza = (String) modeloTabla.getValueAt(filaModelo, 0);
        String estatus  = (String) modeloTabla.getValueAt(filaModelo, 7);

        if ("Cancelada".equals(estatus)) {
            avisar("Esta póliza ya está cancelada."); return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
            "<html>¿Cancelar la póliza <b>" + noPoliza + "</b>?<br>"
            + "<small>Esta acción cambiará su estatus a <b>Cancelada</b> y no se puede deshacer.</small></html>",
            "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            String error = controller.cancelar(noPoliza, mainFrame.getUsernameActual());
            if (error == null) cargarDatos();
            else mostrarError(error);
        }
    }

    // =========================================================================
    //  DIÁLOGO AGREGAR / EDITAR
    // =========================================================================
    private void abrirDialogo(Poliza polizaExistente) {
        boolean esNuevo = (polizaExistente == null);

        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            esNuevo ? "Nueva póliza" : "Editar póliza — " + polizaExistente.getNoPoliza(), true);
        dlg.setSize(500, esNuevo ? 480 : 360);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(28, 36, 28, 36));
        dlg.setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1;

        // ── Cargar catálogos ──────────────────────────────────────────────
        List<Cliente>   clientes  = clienteDAO.listarTodos();
        List<Compania>  companias = companiaDAO.listar();
        List<Cobertura> coberturas= coberturaDAO.listarTodas();

        // Compañía
        JComboBox<String> cbCompania = new JComboBox<>();
        estilizarCombo(cbCompania);
        for (Compania c : companias)
            cbCompania.addItem(c.getIdCompania() + " — " + c.getCompania());

        // Cobertura
        JComboBox<String> cbCobertura = new JComboBox<>();
        estilizarCombo(cbCobertura);
        for (Cobertura c : coberturas)
            cbCobertura.addItem(c.getIdCobertura() + " — " + c.getNombreCobertura());

        // CFDI
        JCheckBox chkCfdi = new JCheckBox("Requiere CFDI");
        chkCfdi.setBackground(new Color(30, 41, 59));
        chkCfdi.setForeground(TEXTO_NORMAL);
        chkCfdi.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        int row = 0;
        JTextField fNoPoliza = null;
        JComboBox<String> cbCliente  = null;
        JComboBox<String> cbVehiculo = null;
        JTextField fFecha = null;

        if (esNuevo) {
            // No. Póliza
            fNoPoliza = campoForm("");
            agregarFila(panel, gbc, row++, "No. Póliza *", fNoPoliza);

            // Cliente
            cbCliente = new JComboBox<>();
            estilizarCombo(cbCliente);
            for (Cliente c : clientes)
                cbCliente.addItem(c.getIdCliente() + " — " + c.getNombre()
                    + " " + nvl(c.getApellidoP()));
            agregarFila(panel, gbc, row++, "Cliente *", cbCliente);

            // Vehículo (se filtra al cambiar cliente)
            cbVehiculo = new JComboBox<>();
            estilizarCombo(cbVehiculo);
            agregarFila(panel, gbc, row++, "Vehículo *", cbVehiculo);

            // Llenar vehículos del primer cliente y actualizar al cambiar
            final JComboBox<String> cbVeh = cbVehiculo;
            final JComboBox<String> cbCliRef = cbCliente;
            final List<Cliente> listaClientes = clientes;
            cargarVehiculos(cbVeh, listaClientes.isEmpty() ? 0 : listaClientes.get(0).getIdCliente());
            cbCliRef.addActionListener(e -> {
                int idx = cbCliRef.getSelectedIndex();
                if (idx >= 0) cargarVehiculos(cbVeh, listaClientes.get(idx).getIdCliente());
            });

            // Fecha inicio
            fFecha = campoForm("");
            fFecha.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
            agregarFila(panel, gbc, row++, "Fecha inicio *", fFecha);
        } else {
            // En edición solo se muestran campos no clave como info
            JLabel infoCliente = etiquetaInfo(nvl(polizaExistente.getNombreCliente()));
            JLabel infoVehiculo = etiquetaInfo(nvl(polizaExistente.getModeloVehiculo())
                + " — " + nvl(polizaExistente.getPlacasVehiculo()));
            agregarFila(panel, gbc, row++, "Cliente", infoCliente);
            agregarFila(panel, gbc, row++, "Vehículo", infoVehiculo);
        }

        agregarFila(panel, gbc, row++, "Compañía *",  cbCompania);
        agregarFila(panel, gbc, row++, "Cobertura *", cbCobertura);
        agregarFila(panel, gbc, row++, "CFDI",        chkCfdi);

        // Pre-seleccionar valores en edición
        if (!esNuevo) {
            seleccionarCombo(cbCompania,  String.valueOf(polizaExistente.getIdCompania()));
            seleccionarCombo(cbCobertura, String.valueOf(polizaExistente.getIdCobertura()));
            chkCfdi.setSelected(polizaExistente.isCfdi());
        }

        // Label de error
        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO_TEXTO);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblErr, gbc);

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancelarDlg = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnGuardar     = crearBotonAccion(esNuevo ? "Registrar" : "Guardar", VERDE);
        btnCancelarDlg.addActionListener(e -> dlg.dispose());

        // Guardar referencias finales para uso en lambda
        final JTextField fNoPolizaFinal = fNoPoliza;
        final JComboBox<String> cbClienteFinal  = cbCliente;
        final JComboBox<String> cbVehiculoFinal = cbVehiculo;
        final JTextField fFechaFinal = fFecha;

        btnGuardar.addActionListener(e -> {
            Poliza p = new Poliza();

            if (esNuevo) {
                p.setNoPoliza(fNoPolizaFinal.getText().trim().toUpperCase());

                String itemCli = (String) cbClienteFinal.getSelectedItem();
                String itemVeh = (String) cbVehiculoFinal.getSelectedItem();
                if (itemCli == null) { lblErr.setText("⚠  Selecciona un cliente."); return; }
                if (itemVeh == null) { lblErr.setText("⚠  Selecciona un vehículo."); return; }
                p.setIdCliente(Integer.parseInt(itemCli.split(" — ")[0].trim()));
                p.setIdVehiculo(Integer.parseInt(itemVeh.split(" — ")[0].trim()));
                p.setFechaInicio(fFechaFinal.getText().trim());
            } else {
                p.setNoPoliza(polizaExistente.getNoPoliza());
                p.setIdCliente(polizaExistente.getIdCliente());
                p.setIdVehiculo(polizaExistente.getIdVehiculo());
                p.setFechaInicio(polizaExistente.getFechaInicio());
            }

            String itemComp = (String) cbCompania.getSelectedItem();
            String itemCob  = (String) cbCobertura.getSelectedItem();
            if (itemComp == null) { lblErr.setText("⚠  Selecciona una compañía."); return; }
            if (itemCob  == null) { lblErr.setText("⚠  Selecciona una cobertura."); return; }
            p.setIdCompania( Integer.parseInt(itemComp.split(" — ")[0].trim()));
            p.setIdCobertura(Integer.parseInt(itemCob.split(" — ")[0].trim()));
            p.setCfdi(chkCfdi.isSelected());
            p.setEstatus("Activa");

            String resultado = esNuevo
                ? controller.agregar(p, mainFrame.getUsernameActual())
                : controller.actualizar(p, mainFrame.getUsernameActual());

            if (resultado == null) { dlg.dispose(); cargarDatos(); }
            else lblErr.setText("⚠  " + resultado);
        });

        btnPanel.add(btnCancelarDlg);
        btnPanel.add(btnGuardar);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        dlg.setVisible(true);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================
    private void cargarVehiculos(JComboBox<String> cb, int idCliente) {
        cb.removeAllItems();
        for (Vehiculo v : vehiculoDAO.listarPorCliente(idCliente)) {
            cb.addItem(v.getIdVehiculo() + " — " + v.getModelo() + " (" + v.getPlacas() + ")");
        }
    }

    private void seleccionarCombo(JComboBox<String> cb, String idPrefix) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).startsWith(idPrefix + " — ")) {
                cb.setSelectedIndex(i); break;
            }
        }
    }

    private void estilizarCombo(JComboBox<String> cb) {
        cb.setBackground(BG_PRINCIPAL);
        cb.setForeground(TEXTO_BLANCO);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JLabel etiquetaInfo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEAL);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

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
            new EmptyBorder(7, 10, 7, 10)));
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

    // =========================================================================
    //  VER PAGOS
    // =========================================================================
    private void accionVerPagos() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una poliza de la tabla primero."); return; }
        String noPoliza = (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        abrirDialogoPagos(noPoliza);
    }

    private void abrirDialogoPagos(String noPoliza) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Pagos - Poliza " + noPoliza, true);
        dlg.setSize(620, 420);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_PRINCIPAL);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] cols = { "ID Pago", "Parcialidad", "Fecha Vencimiento", "Estatus" };
        DefaultTableModel modeloPagos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tablaPagos = new JTable(modeloPagos);
        tablaPagos.setBackground(BG_FILA_PAR); tablaPagos.setForeground(TEXTO_NORMAL);
        tablaPagos.setFont(new Font("Segoe UI", Font.PLAIN, 13)); tablaPagos.setRowHeight(34);
        tablaPagos.setShowGrid(false); tablaPagos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPagos.setSelectionBackground(BG_SELECCION); tablaPagos.setSelectionForeground(TEXTO_BLANCO);
        tablaPagos.getTableHeader().setBackground(BG_HEADER);
        tablaPagos.getTableHeader().setForeground(TEXTO_GRIS);
        tablaPagos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        tablaPagos.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) switch (val != null ? val.toString() : "") {
                    case "Pagado"    -> { setBackground(COLOR_ACTIVA);    setForeground(Color.WHITE); }
                    case "Pendiente" -> { setBackground(COLOR_VENCIDA);   setForeground(Color.WHITE); }
                    case "Vencido"   -> { setBackground(COLOR_CANCELADA); setForeground(Color.WHITE); }
                    default          -> { setBackground(BG_FILA_PAR);     setForeground(TEXTO_NORMAL); }
                } else { setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO); }
                return this;
            }
        });

        List<PagoParcialidad> pagos = pagoDAO.listarPorPoliza(noPoliza);
        for (PagoParcialidad p : pagos)
            modeloPagos.addRow(new Object[]{ p.getIdPago(), p.getNumParcialidad(), p.getFechaVencimiento(), p.getEstatus() });

        JScrollPane scroll = new JScrollPane(tablaPagos);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scroll.getViewport().setBackground(BG_FILA_PAR);

        JButton btnPagado = crearBotonAccion("Marcar como Pagado", VERDE);
        JButton btnVencido = crearBotonAccion("Marcar como Vencido", ROJO);
        JButton btnCerrar = crearBotonAccion("Cerrar", new Color(51, 65, 85));

        btnPagado.addActionListener(e -> {
            int f = tablaPagos.getSelectedRow();
            if (f == -1) return;
            int idPago = (int) modeloPagos.getValueAt(f, 0);
            pagoDAO.actualizarEstatus(idPago, "Pagado");
            DAO.LogHistorialDAO.registrar(mainFrame.getUsernameActual(), "UPDATE", "PAGOS",
                "Pago ID " + idPago + " marcado Pagado | Poliza: " + noPoliza);
            modeloPagos.setValueAt("Pagado", f, 3);
        });
        btnVencido.addActionListener(e -> {
            int f = tablaPagos.getSelectedRow();
            if (f == -1) return;
            int idPago = (int) modeloPagos.getValueAt(f, 0);
            pagoDAO.actualizarEstatus(idPago, "Vencido");
            DAO.LogHistorialDAO.registrar(mainFrame.getUsernameActual(), "UPDATE", "PAGOS",
                "Pago ID " + idPago + " marcado Vencido | Poliza: " + noPoliza);
            modeloPagos.setValueAt("Vencido", f, 3);
        });
        btnCerrar.addActionListener(e -> dlg.dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setBackground(BG_PRINCIPAL);
        botones.add(btnPagado); botones.add(btnVencido);
        botones.add(Box.createHorizontalStrut(16)); botones.add(btnCerrar);

        JLabel info = new JLabel(pagos.size() + " pago(s) para la poliza " + noPoliza);
        info.setForeground(TEXTO_GRIS); info.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.setBackground(BG_PRINCIPAL); sur.setBorder(new EmptyBorder(8, 0, 0, 0));
        sur.add(botones, BorderLayout.CENTER); sur.add(info, BorderLayout.SOUTH);

        panel.add(scroll, BorderLayout.CENTER); panel.add(sur, BorderLayout.SOUTH);
        dlg.setContentPane(panel); dlg.setVisible(true);
    }

    // =========================================================================
    //  AGREGAR PARCIALIDAD
    // =========================================================================
    private void accionAgregarParcialidad() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una poliza de la tabla primero."); return; }
        int filaM = tabla.convertRowIndexToModel(fila);
        String noPoliza = (String) modeloTabla.getValueAt(filaM, 0);
        String estatus  = (String) modeloTabla.getValueAt(filaM, 7);
        if ("Cancelada".equals(estatus)) { avisar("No se pueden agregar pagos a una poliza cancelada."); return; }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Agregar Parcialidad - Poliza " + noPoliza, true);
        dlg.setSize(440, 310); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(28, 36, 28, 36));
        dlg.setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 0, 5, 0); gbc.weightx = 1;

        List<PagoParcialidad> existentes = pagoDAO.listarPorPoliza(noPoliza);
        int siguienteNum = existentes.size() + 1;

        JTextField fNum   = campoForm(String.valueOf(siguienteNum));
        JTextField fTotal = campoForm(existentes.isEmpty() ? "" : String.valueOf(existentes.get(0).getTotalParcialidades()));
        JTextField fFecha = campoForm("");
        fFecha.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{ "Pendiente", "Pagado", "Vencido" });
        cbEstatus.setBackground(BG_PRINCIPAL); cbEstatus.setForeground(TEXTO_BLANCO);
        cbEstatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        int row = 0;
        agregarFila(panel, gbc, row++, "Poliza",              etiquetaInfo(noPoliza));
        agregarFila(panel, gbc, row++, "No. Parcialidad",     fNum);
        agregarFila(panel, gbc, row++, "Total parcialidades", fTotal);
        agregarFila(panel, gbc, row++, "Fecha vencimiento *", fFecha);
        agregarFila(panel, gbc, row++, "Estatus",             cbEstatus);

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO_TEXTO); lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(lblErr, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnOk     = crearBotonAccion("Registrar", VERDE);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnOk.addActionListener(e -> {
            String fecha  = fFecha.getText().trim();
            String numStr = fNum.getText().trim();
            String totStr = fTotal.getText().trim();
            if (fecha.isEmpty() || !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                lblErr.setText("[!] Fecha invalida. Usa AAAA-MM-DD."); return;
            }
            if (numStr.isEmpty() || totStr.isEmpty()) {
                lblErr.setText("[!] Completa los campos de parcialidades."); return;
            }
            try {
                PagoParcialidad p = new PagoParcialidad();
                p.setNoPoliza(noPoliza);
                p.setNumParcialidad(Integer.parseInt(numStr));
                p.setTotalParcialidades(Integer.parseInt(totStr));
                p.setFechaVencimiento(java.sql.Date.valueOf(fecha));
                p.setEstatus((String) cbEstatus.getSelectedItem());
                pagoDAO.registrarPago(p);
                DAO.LogHistorialDAO.registrar(mainFrame.getUsernameActual(), "INSERT", "PAGOS",
                    "Parcialidad " + numStr + "/" + totStr + " | Poliza: " + noPoliza + " | Vence: " + fecha);
                dlg.dispose();
            } catch (NumberFormatException ex) {
                lblErr.setText("[!] Los numeros de parcialidad deben ser enteros.");
            } catch (Exception ex) {
                lblErr.setText("[!] Error: " + ex.getMessage());
            }
        });
        btnPanel.add(btnCancel); btnPanel.add(btnOk);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(btnPanel, gbc);
        dlg.setVisible(true);
    }

    // =========================================================================
    //  RENOVAR
    // =========================================================================
    private void accionRenovar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una poliza de la tabla primero."); return; }
        int filaM = tabla.convertRowIndexToModel(fila);
        String noPoliza = (String) modeloTabla.getValueAt(filaM, 0);
        String estatus  = (String) modeloTabla.getValueAt(filaM, 7);
        if ("Cancelada".equals(estatus)) { avisar("No se puede renovar una poliza cancelada."); return; }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Renovar poliza - " + noPoliza, true);
        dlg.setSize(420, 240); dlg.setLocationRelativeTo(this); dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(new EmptyBorder(28, 36, 28, 36));
        dlg.setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 0, 6, 0); gbc.weightx = 1;

        JTextField fNuevoNum  = campoForm("");
        fNuevoNum.putClientProperty("JTextField.placeholderText", "Ej: POL-2026-001");
        JTextField fNuevaFecha = campoForm("");
        fNuevaFecha.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");

        int row = 0;
        agregarFila(panel, gbc, row++, "Poliza original",    etiquetaInfo(noPoliza));
        agregarFila(panel, gbc, row++, "Nuevo No. Poliza *", fNuevoNum);
        agregarFila(panel, gbc, row++, "Nueva fecha inicio *", fNuevaFecha);

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO_TEXTO); lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(lblErr, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnOk     = crearBotonAccion("Renovar", VERDE_OSCURO);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnOk.addActionListener(e -> {
            String resultado = controller.renovar(noPoliza,
                fNuevoNum.getText().trim(), fNuevaFecha.getText().trim(),
                mainFrame.getUsernameActual());
            if (resultado == null) { dlg.dispose(); cargarDatos(); }
            else lblErr.setText("[!] " + resultado);
        });
        btnPanel.add(btnCancel); btnPanel.add(btnOk);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(btnPanel, gbc);
        dlg.setVisible(true);
    }

    // =========================================================================
    //  ELIMINAR FISICO (solo Administrador)
    // =========================================================================
    private void accionEliminarFisico() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona una poliza de la tabla primero."); return; }
        String noPoliza = (String) modeloTabla.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        String rol = mainFrame.getUsuarioActual() != null ? mainFrame.getUsuarioActual().getRol() : "";

        int conf = JOptionPane.showConfirmDialog(this,
            "<html><b>ELIMINACION PERMANENTE</b><br><br>" +
            "Deseas eliminar fisicamente la poliza <b>" + noPoliza + "</b>?<br>" +
            "<small>Se borrara la poliza y TODOS sus pagos. Esta accion NO se puede deshacer.</small></html>",
            "Confirmar eliminacion permanente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        String confirmacion = JOptionPane.showInputDialog(this,
            "Escribe el numero de poliza para confirmar: " + noPoliza,
            "Confirmacion final", JOptionPane.WARNING_MESSAGE);
        if (!noPoliza.equals(confirmacion)) { avisar("El numero ingresado no coincide. Operacion cancelada."); return; }

        String error = controller.eliminarFisico(noPoliza, rol, mainFrame.getUsernameActual());
        if (error == null) cargarDatos();
        else mostrarError(error);
    }

    private void avisar(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
