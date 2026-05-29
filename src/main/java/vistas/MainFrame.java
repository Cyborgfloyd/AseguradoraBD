package vistas;

import Modelos.UsuarioSistema;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana principal del sistema — único JFrame de la aplicación.
 *
 * Estructura visual:
 * ┌──────────────────────────────────────────────────┐
 * │  cardRaiz (CardLayout)                           │
 * │  ┌─────────────────────────────────────────┐    │
 * │  │  LOGIN: PanelLogin (centrado)            │    │
 * │  └─────────────────────────────────────────┘    │
 * │  ┌──────────┬──────────────────────────────┐    │
 * │  │ Sidebar  │  cardContenido (CardLayout)   │    │
 * │  │  (200px) │  ┌────────────────────────┐  │    │
 * │  │ [Clientes│  │ PanelClientes          │  │    │
 * │  │ [Vehícu  │  │ PanelVehiculos         │  │    │
 * │  │ [Pólizas │  │ PanelPolizas           │  │    │
 * │  │ [Histori │  │ PanelHistorial         │  │    │
 * │  └──────────┴──────────────────────────────┘    │
 * └──────────────────────────────────────────────────┘
 */
public class MainFrame extends JFrame {

    // ── CardLayout raíz (LOGIN ↔ APP) ─────────────────────────────────────
    private JPanel      panelRaiz;
    private CardLayout  cardRaiz;

    // ── Área de contenido de la app ───────────────────────────────────────
    private JPanel      panelApp;
    private JPanel      panelContenido;
    private CardLayout  cardContenido;

    // ── Vistas ────────────────────────────────────────────────────────────
    private PanelLogin     panelLogin;
    private PanelClientes  panelClientes;
    private PanelVehiculos panelVehiculos;
    private PanelPolizas   panelPolizas;
    private PanelPagos     panelPagos;
    private PanelHistorial panelHistorial;

    // ── Botones del sidebar (guardamos referencia para resaltar el activo) ─
    private JButton btnClientes;
    private JButton btnVehiculos;
    private JButton btnPolizas;
    private JButton btnPagos;
    private JButton btnHistorial;

    // ── Info del usuario en el footer del sidebar ─────────────────────────
    private JLabel lblUsuarioFooter;

    // ── Sesión activa ─────────────────────────────────────────────────────
    private UsuarioSistema usuarioActual;

    // ── Constantes de navegación ──────────────────────────────────────────
    public static final String CARD_LOGIN     = "LOGIN";
    public static final String CARD_APP       = "APP";
    public static final String CARD_CLIENTES  = "CLIENTES";
    public static final String CARD_VEHICULOS = "VEHICULOS";
    public static final String CARD_POLIZAS   = "POLIZAS";
    public static final String CARD_PAGOS     = "PAGOS";
    public static final String CARD_HISTORIAL = "HISTORIAL";

    // ── Paleta de colores ─────────────────────────────────────────────────
    private static final Color SIDEBAR_BG       = new Color(22,  28,  40);
    private static final Color SIDEBAR_BTN      = new Color(28,  35,  49);
    private static final Color SIDEBAR_HOVER    = new Color(38,  47,  65);
    private static final Color SIDEBAR_ACTIVO   = new Color(37,  99,  235);
    private static final Color SIDEBAR_HEADER   = new Color(15,  23,  42);
    private static final Color SIDEBAR_FOOTER   = new Color(15,  23,  42);
    private static final Color TEXTO_NORMAL     = new Color(203, 213, 225);
    private static final Color TEXTO_APAGADO    = new Color(100, 116, 139);
    private static final Color TEXTO_ACTIVO     = Color.WHITE;
    private static final Color TEAL             = new Color(99,  202, 183);
    private static final Color ROJO_DARK        = new Color(185, 28,  28);
    private static final Color APP_BG           = new Color(15,  23,  42);

    // =========================================================================
    //  CONSTRUCTOR
    // =========================================================================
    public MainFrame() {
        super("Seguros Murillo — Sistema Administrativo");
        construirUI();
        configurarVentana();
    }

    // =========================================================================
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // =========================================================================
    private void construirUI() {
        cardRaiz  = new CardLayout();
        panelRaiz = new JPanel(cardRaiz);

        // ── Panel Login ───────────────────────────────────────────────────
        panelLogin = new PanelLogin(this);
        panelRaiz.add(panelLogin, CARD_LOGIN);

        // ── Panel App ─────────────────────────────────────────────────────
        panelApp = new JPanel(new BorderLayout());
        panelApp.add(construirSidebar(), BorderLayout.WEST);

        cardContenido  = new CardLayout();
        panelContenido = new JPanel(cardContenido);

        // Paneles reales
        panelClientes = new PanelClientes(this);
        panelContenido.add(panelClientes, CARD_CLIENTES);

        // Placeholders para las próximas iteraciones
        panelVehiculos = new PanelVehiculos(this);
        panelContenido.add(panelVehiculos, CARD_VEHICULOS);
        panelPolizas = new PanelPolizas(this);
        panelContenido.add(panelPolizas, CARD_POLIZAS);
        panelPagos = new PanelPagos(this);
        panelContenido.add(panelPagos, CARD_PAGOS);
        panelHistorial = new PanelHistorial(this);
        panelContenido.add(panelHistorial, CARD_HISTORIAL);

        panelApp.add(panelContenido, BorderLayout.CENTER);
        panelRaiz.add(panelApp, CARD_APP);

        setContentPane(panelRaiz);

        // Mostrar login al arrancar
        cardRaiz.show(panelRaiz, CARD_LOGIN);
    }

    // =========================================================================
    //  SIDEBAR
    // =========================================================================
    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));

        // ── Header: logo / nombre ──────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SIDEBAR_HEADER);
        header.setBorder(new EmptyBorder(22, 18, 22, 18));
        header.setMaximumSize(new Dimension(210, 80));

        JLabel lblLogo = new JLabel("Seguros Murillo");
        lblLogo.setForeground(TEAL);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.add(lblLogo, BorderLayout.CENTER);

        sidebar.add(header);
        sidebar.add(Box.createRigidArea(new Dimension(0, 14)));

        // ── Módulos ────────────────────────────────────────────────────────
        sidebar.add(crearSeccionLabel("MÓDULOS"));

        btnClientes  = crearNavBtn("Clientes",  CARD_CLIENTES);
        btnVehiculos = crearNavBtn("Vehiculos", CARD_VEHICULOS);
        btnPolizas   = crearNavBtn("Polizas",   CARD_POLIZAS);
        btnPagos     = crearNavBtn("Pagos",     CARD_PAGOS);

        sidebar.add(btnClientes);
        sidebar.add(btnVehiculos);
        sidebar.add(btnPolizas);
        sidebar.add(btnPagos);

        sidebar.add(Box.createRigidArea(new Dimension(0, 14)));

        // ── Sistema ────────────────────────────────────────────────────────
        sidebar.add(crearSeccionLabel("SISTEMA"));

        btnHistorial = crearNavBtn("Historial", CARD_HISTORIAL);
        sidebar.add(btnHistorial);

        // Empuja el footer al fondo
        sidebar.add(Box.createVerticalGlue());

        // ── Footer: usuario y cerrar sesión ───────────────────────────────
        sidebar.add(construirFooterSidebar());

        return sidebar;
    }

    private JLabel crearSeccionLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_APAGADO);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setBorder(new EmptyBorder(2, 20, 2, 0));
        lbl.setMaximumSize(new Dimension(210, 20));
        return lbl;
    }

    private JButton crearNavBtn(String texto, String card) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setMinimumSize(new Dimension(210, 42));
        btn.setPreferredSize(new Dimension(210, 42));
        btn.setBackground(SIDEBAR_BTN);
        btn.setForeground(TEXTO_NORMAL);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVO))
                    btn.setBackground(SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVO))
                    btn.setBackground(SIDEBAR_BTN);
            }
        });

        btn.addActionListener(e -> mostrarPanel(card));
        return btn;
    }

    private JPanel construirFooterSidebar() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(SIDEBAR_FOOTER);
        footer.setBorder(new EmptyBorder(14, 18, 18, 18));
        footer.setMaximumSize(new Dimension(210, 90));

        // Separador superior
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(51, 65, 85));
        sep.setMaximumSize(new Dimension(210, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblUsuarioFooter = new JLabel("Sin sesión");
        lblUsuarioFooter.setForeground(TEXTO_APAGADO);
        lblUsuarioFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuarioFooter.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCerrar = new JButton("Cerrar sesion");
        btnCerrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCerrar.setBackground(ROJO_DARK);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setMaximumSize(new Dimension(174, 32));
        btnCerrar.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnCerrar.addActionListener(e -> cerrarSesion());

        footer.add(sep);
        footer.add(Box.createRigidArea(new Dimension(0, 12)));
        footer.add(lblUsuarioFooter);
        footer.add(Box.createRigidArea(new Dimension(0, 10)));
        footer.add(btnCerrar);

        return footer;
    }

    // =========================================================================
    //  NAVEGACIÓN
    // =========================================================================

    /**
     * Cambia el panel visible en el área de contenido.
     * Llamado desde botones del sidebar y opcionalmente desde los propios paneles.
     */
    public void mostrarPanel(String card) {
        cardContenido.show(panelContenido, card);
        resaltarBotonActivo(card);

        // Refresca datos cuando se abre un panel con tabla
        if (CARD_CLIENTES.equals(card))  panelClientes.cargarDatos();
        if (CARD_VEHICULOS.equals(card)) panelVehiculos.cargarDatos();
        if (CARD_POLIZAS.equals(card))   panelPolizas.cargarDatos();
        if (CARD_PAGOS.equals(card))     panelPagos.cargarDatos();
        if (CARD_HISTORIAL.equals(card)) panelHistorial.cargarDatos();
    }

    private void resaltarBotonActivo(String card) {
        // Resetea todos
        for (JButton b : new JButton[]{btnClientes, btnVehiculos, btnPolizas, btnPagos, btnHistorial}) {
            b.setBackground(SIDEBAR_BTN);
            b.setForeground(TEXTO_NORMAL);
        }
        // Resalta el activo
        JButton activo = switch (card) {
            case CARD_CLIENTES  -> btnClientes;
            case CARD_VEHICULOS -> btnVehiculos;
            case CARD_POLIZAS   -> btnPolizas;
            case CARD_PAGOS     -> btnPagos;
            case CARD_HISTORIAL -> btnHistorial;
            default             -> null;
        };
        if (activo != null) {
            activo.setBackground(SIDEBAR_ACTIVO);
            activo.setForeground(TEXTO_ACTIVO);
        }
    }

    // =========================================================================
    //  GESTIÓN DE SESIÓN
    // =========================================================================

    /**
     * Llamado por PanelLogin al autenticar exitosamente.
     * Transiciona del estado LOGIN al estado APP y navega al panel por defecto.
     */
    public void iniciarSesion(UsuarioSistema usuario) {
        this.usuarioActual = usuario;
        lblUsuarioFooter.setText(usuario.getUsername() + "  -  " + usuario.getRol());

        // El historial solo lo ven Administrador y Auditor
        boolean esAdmin = "Administrador".equals(usuario.getRol());
        btnHistorial.setVisible(esAdmin);
        // Pagos lo ven todos los roles
        btnPagos.setVisible(true);

        cardRaiz.show(panelRaiz, CARD_APP);
        mostrarPanel(CARD_CLIENTES);  // panel por defecto al entrar
    }

    public void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Cerrar la sesión de «" + (usuarioActual != null ? usuarioActual.getUsername() : "") + "»?",
            "Confirmar cierre de sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (respuesta == JOptionPane.YES_OPTION) {
            usuarioActual = null;
            lblUsuarioFooter.setText("Sin sesión");
            btnHistorial.setVisible(true);
            cardRaiz.show(panelRaiz, CARD_LOGIN);
            panelLogin.limpiar();
        }
    }

    // =========================================================================
    //  PLACEHOLDER — se reemplaza al agregar el panel real
    // =========================================================================
    private JPanel crearPlaceholder(String emoji, String titulo, String descripcion) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(APP_BG);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel ico  = new JLabel(emoji, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl  = new JLabel(titulo, SwingConstants.CENTER);
        lbl.setForeground(new Color(148, 163, 184));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub  = new JLabel(descripcion, SwingConstants.CENTER);
        sub.setForeground(TEXTO_APAGADO);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel badge = new JLabel("Próximamente en la siguiente iteración");
        badge.setForeground(new Color(37, 99, 235));
        badge.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenido.add(ico);
        contenido.add(Box.createRigidArea(new Dimension(0, 12)));
        contenido.add(lbl);
        contenido.add(Box.createRigidArea(new Dimension(0, 6)));
        contenido.add(sub);
        contenido.add(Box.createRigidArea(new Dimension(0, 16)));
        contenido.add(badge);

        panel.add(contenido);
        return panel;
    }

    // =========================================================================
    //  CONFIGURACIÓN DE VENTANA
    // =========================================================================
    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 650));
        setSize(1280, 760);
        setLocationRelativeTo(null);  // centrar en pantalla al abrir
    }

    // =========================================================================
    //  GETTERS DE SESIÓN (usados por los paneles)
    // =========================================================================
    public UsuarioSistema getUsuarioActual() {
        return usuarioActual;
    }

    public String getUsernameActual() {
        return usuarioActual != null ? usuarioActual.getUsername() : "sistema";
    }
}