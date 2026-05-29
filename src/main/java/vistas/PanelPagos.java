package vistas;

import DAO.LogHistorialDAO;
import DAO.PagoParcialidadDAO;
import DAO.PolizaDAO;
import Modelos.PagoParcialidad;
import Modelos.Poliza;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelPagos extends JPanel {

    private final MainFrame          mainFrame;
    private final PagoParcialidadDAO pagoDAO   = new PagoParcialidadDAO();
    private final PolizaDAO          polizaDAO = new PolizaDAO();

    private JTable            tabla;
    private DefaultTableModel modeloTabla;
    private JTextField        txtBuscarPoliza;
    private JComboBox<String> cbPolizas;
    private JLabel            lblContador;
    private JLabel            lblResumen;

    private List<Poliza> todasLasPolizas;
    private boolean      cargandoCombo = false;

    private static final String[] COLUMNAS = {
        "ID Pago", "No. Póliza", "Parcialidad", "Total Parc.", "Fecha Vencimiento", "Estatus"
    };

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
    private static final Color ROJO          = new Color(185, 28,  28);
    private static final Color AMBAR         = new Color(180, 83,  9);
    private static final Color ROJO_TEXTO    = new Color(239, 68,  68);

    private static final Color COLOR_PAGADO    = new Color(21,  128, 61);
    private static final Color COLOR_PENDIENTE = new Color(180, 83,  9);
    private static final Color COLOR_VENCIDO   = new Color(127, 29,  29);

    public PanelPagos(MainFrame mainFrame) {
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
        JLabel lblTitulo = new JLabel("Pagos y Parcialidades");
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSub = new JLabel("Registro y seguimiento de pagos por póliza");
        lblSub.setForeground(TEXTO_GRIS);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        izq.add(lblTitulo);
        izq.add(lblSub);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);

        txtBuscarPoliza = new JTextField(18);
        txtBuscarPoliza.setBackground(BG_INPUT);
        txtBuscarPoliza.setForeground(TEXTO_NORMAL);
        txtBuscarPoliza.setCaretColor(TEAL);
        txtBuscarPoliza.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtBuscarPoliza.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(7, 10, 7, 10)));
        txtBuscarPoliza.putClientProperty("JTextField.placeholderText", "No. de póliza...");

        cbPolizas = new JComboBox<>();
        cbPolizas.setBackground(BG_INPUT);
        cbPolizas.setForeground(TEXTO_NORMAL);
        cbPolizas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbPolizas.setPreferredSize(new Dimension(200, 34));

        JButton btnFiltrar    = crearBotonUtil("Ver pagos");
        JButton btnTodos      = crearBotonUtil("Todos");
        JButton btnRefrescar  = crearBotonUtil("Refrescar");

        txtBuscarPoliza.addActionListener(e -> cargarPorPolizaTexto());
        btnFiltrar.addActionListener(e    -> cargarPorCombo());
        cbPolizas.addActionListener(e     -> { if (!cargandoCombo) cargarPorCombo(); });
        btnTodos.addActionListener(e      -> cargarTodos());
        btnRefrescar.addActionListener(e  -> { txtBuscarPoliza.setText(""); cargarDatos(); });

        JLabel lblO = new JLabel(" o ");
        lblO.setForeground(TEXTO_GRIS);
        lblO.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        der.add(txtBuscarPoliza);
        der.add(lblO);
        der.add(cbPolizas);
        der.add(btnFiltrar);
        der.add(btnTodos);
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
            @Override public Class<?> getColumnClass(int c) { return c == 0 || c == 2 || c == 3 ? Integer.class : String.class; }
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

        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(0).setMinWidth(55);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);

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

        // ID centrado en TEAL
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

        for (int c : new int[]{1, 2, 3, 4}) tabla.getColumnModel().getColumn(c).setCellRenderer(renderGeneral);

        // Estatus con badge de color
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                String estatus = val != null ? val.toString() : "";
                if (!sel) switch (estatus) {
                    case "Pagado"    -> { setBackground(COLOR_PAGADO);    setForeground(Color.WHITE); }
                    case "Pendiente" -> { setBackground(COLOR_PENDIENTE); setForeground(Color.WHITE); }
                    case "Vencido"   -> { setBackground(COLOR_VENCIDO);   setForeground(Color.WHITE); }
                    default          -> { setBackground(row % 2 == 0 ? BG_FILA_PAR : BG_FILA_IMPAR); setForeground(TEXTO_NORMAL); }
                } else { setBackground(BG_SELECCION); setForeground(TEXTO_BLANCO); }
                return this;
            }
        });
    }

    // =========================================================================
    //  BARRA DE ACCIONES
    // =========================================================================
    private JPanel construirAcciones() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(BG_HEADER);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
            new EmptyBorder(6, 16, 6, 16)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnPanel.setOpaque(false);

        JButton btnNuevo   = crearBotonAccion("Nueva parcialidad", VERDE);
        JButton btnPagado  = crearBotonAccion("Marcar Pagado",     new Color(22, 101, 52));
        JButton btnVencido = crearBotonAccion("Marcar Vencido",    ROJO);

        btnNuevo.addActionListener(e   -> abrirDialogoNuevaParcialidad());
        btnPagado.addActionListener(e  -> cambiarEstatus("Pagado"));
        btnVencido.addActionListener(e -> cambiarEstatus("Vencido"));

        lblContador = new JLabel("0 registros");
        lblContador.setForeground(TEXTO_GRIS);
        lblContador.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modeloTabla.addTableModelListener(e -> actualizarResumen());

        lblResumen = new JLabel("");
        lblResumen.setForeground(TEXTO_GRIS);
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnPanel.add(btnNuevo);
        btnPanel.add(Box.createHorizontalStrut(8));
        btnPanel.add(btnPagado);
        btnPanel.add(btnVencido);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(lblResumen);
        infoPanel.add(lblContador);

        barra.add(btnPanel,  BorderLayout.WEST);
        barra.add(infoPanel, BorderLayout.EAST);
        return barra;
    }

    // =========================================================================
    //  CARGA DE DATOS
    // =========================================================================
    public void cargarDatos() {
        new SwingWorker<List<Poliza>, Void>() {
            @Override protected List<Poliza> doInBackground() { return polizaDAO.listarPolizas(); }
            @Override protected void done() {
                try {
                    todasLasPolizas = get();
                    cargandoCombo = true;
                    cbPolizas.removeAllItems();
                    for (Poliza p : todasLasPolizas)
                        cbPolizas.addItem(p.getNoPoliza()
                            + (p.getNombreCliente() != null ? " — " + p.getNombreCliente() : ""));
                    cargandoCombo = false;
                    cargarTodos();
                } catch (Exception ex) {
                    cargandoCombo = false;
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void cargarTodos() {
        new SwingWorker<List<PagoParcialidad>, Void>() {
            @Override protected List<PagoParcialidad> doInBackground() { return pagoDAO.listarTodos(); }
            @Override protected void done() {
                try { llenarTabla(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void cargarPorPolizaTexto() {
        String poliza = txtBuscarPoliza.getText().trim().toUpperCase();
        if (poliza.isEmpty()) { cargarTodos(); return; }
        new SwingWorker<List<PagoParcialidad>, Void>() {
            @Override protected List<PagoParcialidad> doInBackground() { return pagoDAO.listarPorPoliza(poliza); }
            @Override protected void done() {
                try { llenarTabla(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void cargarPorCombo() {
        int idx = cbPolizas.getSelectedIndex();
        if (idx < 0 || todasLasPolizas == null || todasLasPolizas.isEmpty()) return;
        String noPoliza = todasLasPolizas.get(idx).getNoPoliza();
        new SwingWorker<List<PagoParcialidad>, Void>() {
            @Override protected List<PagoParcialidad> doInBackground() { return pagoDAO.listarPorPoliza(noPoliza); }
            @Override protected void done() {
                try { llenarTabla(get()); } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void llenarTabla(List<PagoParcialidad> lista) {
        modeloTabla.setRowCount(0);
        for (PagoParcialidad p : lista) {
            modeloTabla.addRow(new Object[]{
                p.getIdPago(),
                p.getNoPoliza(),
                p.getNumParcialidad(),
                p.getTotalParcialidades(),
                p.getFechaVencimiento() != null ? p.getFechaVencimiento().toString() : "",
                p.getEstatus()
            });
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        int total = modeloTabla.getRowCount();
        int pagados = 0, pendientes = 0, vencidos = 0;
        for (int i = 0; i < total; i++) {
            String est = (String) modeloTabla.getValueAt(i, 5);
            switch (est != null ? est : "") {
                case "Pagado"    -> pagados++;
                case "Pendiente" -> pendientes++;
                case "Vencido"   -> vencidos++;
            }
        }
        lblContador.setText(total + " registro(s)");
        lblResumen.setText(
            "Pagados: " + pagados + "  |  " +
            "Pendientes: " + pendientes + "  |  " +
            "Vencidos: " + vencidos + "    ");
    }

    // =========================================================================
    //  CAMBIAR ESTATUS
    // =========================================================================
    private void cambiarEstatus(String nuevoEstatus) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) { avisar("Selecciona un pago de la tabla primero."); return; }
        int filaM   = tabla.convertRowIndexToModel(fila);
        int idPago  = (int) modeloTabla.getValueAt(filaM, 0);
        String noPoliza  = (String) modeloTabla.getValueAt(filaM, 1);
        String estatusActual = (String) modeloTabla.getValueAt(filaM, 5);
        int parcialidad = (int) modeloTabla.getValueAt(filaM, 2);
        int total       = (int) modeloTabla.getValueAt(filaM, 3);

        if (nuevoEstatus.equals(estatusActual)) {
            avisar("El pago ya está en estatus «" + nuevoEstatus + "»."); return;
        }

        pagoDAO.actualizarEstatus(idPago, nuevoEstatus);
        LogHistorialDAO.registrar(
            mainFrame.getUsernameActual(), "UPDATE", "PAGOS",
            "Pago ID " + idPago + " | Póliza: " + noPoliza
            + " | Parcialidad " + parcialidad + "/" + total
            + " | [" + estatusActual + "] → [" + nuevoEstatus + "]"
        );
        modeloTabla.setValueAt(nuevoEstatus, filaM, 5);
        actualizarResumen();
    }

    // =========================================================================
    //  NUEVA PARCIALIDAD
    // =========================================================================
    private void abrirDialogoNuevaParcialidad() {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Nueva Parcialidad", true);
        dlg.setSize(460, 340);
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

        JTextField fNum   = campoForm("");
        JTextField fTotal = campoForm("");
        JTextField fFecha = campoForm("");
        fFecha.putClientProperty("JTextField.placeholderText", "AAAA-MM-DD");
        JComboBox<String> cbEstatus = new JComboBox<>(new String[]{"Pendiente", "Pagado", "Vencido"});
        cbEstatus.setBackground(new Color(15, 23, 42));
        cbEstatus.setForeground(TEXTO_BLANCO);
        cbEstatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Combo de pólizas — se llena primero sin listener para evitar disparos prematuros
        JComboBox<String> cbPol = new JComboBox<>();
        cbPol.setBackground(new Color(15, 23, 42));
        cbPol.setForeground(TEXTO_BLANCO);
        cbPol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (todasLasPolizas != null) {
            for (Poliza p : todasLasPolizas)
                cbPol.addItem(p.getNoPoliza()
                    + (p.getNombreCliente() != null ? " — " + p.getNombreCliente() : ""));
        }
        // Pre-seleccionar la póliza activa en el header
        int idxPresel = cbPolizas.getSelectedIndex();
        if (idxPresel >= 0 && cbPol.getItemCount() > idxPresel)
            cbPol.setSelectedIndex(idxPresel);

        // Pre-llenar número de parcialidad una sola vez
        actualizarNumParcialidad(cbPol, fNum, fTotal);

        // Ahora sí agregamos el listener para cambios del usuario
        cbPol.addActionListener(e -> actualizarNumParcialidad(cbPol, fNum, fTotal));

        int row = 0;
        agregarFila(panel, gbc, row++, "Póliza *",              cbPol);
        agregarFila(panel, gbc, row++, "No. Parcialidad *",     fNum);
        agregarFila(panel, gbc, row++, "Total parcialidades *", fTotal);
        agregarFila(panel, gbc, row++, "Fecha vencimiento *",   fFecha);
        agregarFila(panel, gbc, row++, "Estatus",               cbEstatus);

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(ROJO_TEXTO);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(lblErr, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancel = crearBotonAccion("Cancelar", new Color(51, 65, 85));
        JButton btnOk     = crearBotonAccion("Registrar", VERDE);
        btnCancel.addActionListener(e -> dlg.dispose());

        btnOk.addActionListener(e -> {
            String fecha  = fFecha.getText().trim();
            String numStr = fNum.getText().trim();
            String totStr = fTotal.getText().trim();

            if (cbPol.getSelectedIndex() < 0) { lblErr.setText("[!] Selecciona una póliza."); return; }
            if (fecha.isEmpty() || !fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                lblErr.setText("[!] Fecha inválida. Usa AAAA-MM-DD."); return;
            }
            if (numStr.isEmpty() || totStr.isEmpty()) {
                lblErr.setText("[!] Completa los campos de parcialidad."); return;
            }
            try {
                int idxPol = cbPol.getSelectedIndex();
                String noPoliza = todasLasPolizas.get(idxPol).getNoPoliza();
                int numParc  = Integer.parseInt(numStr);
                int totParc  = Integer.parseInt(totStr);

                PagoParcialidad p = new PagoParcialidad();
                p.setNoPoliza(noPoliza);
                p.setNumParcialidad(numParc);
                p.setTotalParcialidades(totParc);
                p.setFechaVencimiento(java.sql.Date.valueOf(fecha));
                p.setEstatus((String) cbEstatus.getSelectedItem());
                pagoDAO.registrarPago(p);

                LogHistorialDAO.registrar(
                    mainFrame.getUsernameActual(), "INSERT", "PAGOS",
                    "Nueva parcialidad " + numParc + "/" + totParc
                    + " | Póliza: " + noPoliza + " | Vence: " + fecha
                    + " | Estatus: " + p.getEstatus()
                );
                dlg.dispose();
                cargarTodos();
            } catch (NumberFormatException ex) {
                lblErr.setText("[!] Los números de parcialidad deben ser enteros.");
            } catch (Exception ex) {
                lblErr.setText("[!] Error: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnOk);
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);
        dlg.setVisible(true);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================
    private void agregarFila(JPanel panel, GridBagConstraints gbc,
                              int row, String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.35;
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(campo, gbc);
    }

    private JTextField campoForm(String valor) {
        JTextField txt = new JTextField(valor);
        txt.setBackground(new Color(15, 23, 42));
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

    private void actualizarNumParcialidad(JComboBox<String> cbPol, JTextField fNum, JTextField fTotal) {
        int idx = cbPol.getSelectedIndex();
        if (idx >= 0 && todasLasPolizas != null && idx < todasLasPolizas.size()) {
            String noP = todasLasPolizas.get(idx).getNoPoliza();
            List<PagoParcialidad> existentes = pagoDAO.listarPorPoliza(noP);
            fNum.setText(String.valueOf(existentes.size() + 1));
            if (!existentes.isEmpty())
                fTotal.setText(String.valueOf(existentes.get(0).getTotalParcialidades()));
        }
    }

    private void avisar(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }
}
