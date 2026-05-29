package vistas;

import DAO.UsuarioDAO;
import Modelos.UsuarioSistema;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class PanelLogin extends JPanel {

    private final MainFrame mainFrame;

    private JTextField     txtUsuario;
    private JPasswordField txtPassword;
    private JLabel         lblError;
    private JButton        btnIngresar;

    private static final Color BG_PROFUNDO  = new Color(15,  23,  42);
    private static final Color BG_CARD      = new Color(22,  33,  55);
    private static final Color BG_INPUT     = new Color(15,  23,  42);
    private static final Color BORDE        = new Color(51,  65,  85);
    private static final Color BORDE_FOCUS  = new Color(99,  202, 183);
    private static final Color TEXTO_BLANCO = new Color(248, 250, 252);
    private static final Color TEXTO_GRIS   = new Color(148, 163, 184);
    private static final Color TEAL         = new Color(99,  202, 183);
    private static final Color TEAL_DARK    = new Color(45,  140, 120);
    private static final Color AZUL         = new Color(37,  99,  235);
    private static final Color AZUL_HOVER   = new Color(29,  78,  216);
    private static final Color ROJO         = new Color(239, 68,  68);

    public PanelLogin(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(BG_PROFUNDO);
        add(crearCard());
    }

    private JPanel crearCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(42, 48, 42, 48)));
        card.setPreferredSize(new Dimension(400, 480));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        // Logo
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrap.setOpaque(false);
        logoWrap.add(crearLogo(70));

        JLabel lblTitulo = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblTitulo.setForeground(TEXTO_BLANCO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblSub = new JLabel("Inicia sesion para continuar", SwingConstants.CENTER);
        lblSub.setForeground(TEXTO_GRIS);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblU = etiqueta("Usuario");
        txtUsuario   = crearCampo("Tu nombre de usuario");

        JLabel lblP = etiqueta("Contrasena");
        txtPassword  = new JPasswordField();
        estilizar(txtPassword);
        agregarFocusBorde(txtPassword);

        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setForeground(ROJO);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnIngresar = crearBoton("Ingresar");

        JLabel lblVersion = new JLabel("Sistema Administrativo v1.0", SwingConstants.CENTER);
        lblVersion.setForeground(new Color(71, 85, 105));
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        ActionListener accion = e -> intentarLogin();
        txtUsuario.addActionListener(accion);
        txtPassword.addActionListener(accion);
        btnIngresar.addActionListener(accion);

        g.gridy = 0; g.insets = new Insets(0,  0, 18, 0); card.add(logoWrap,    g);
        g.gridy = 1; g.insets = new Insets(0,  0, 4,  0); card.add(lblTitulo,   g);
        g.gridy = 2; g.insets = new Insets(0,  0, 28, 0); card.add(lblSub,      g);
        g.gridy = 3; g.insets = new Insets(0,  0, 6,  0); card.add(lblU,        g);
        g.gridy = 4; g.insets = new Insets(0,  0, 18, 0); card.add(txtUsuario,  g);
        g.gridy = 5; g.insets = new Insets(0,  0, 6,  0); card.add(lblP,        g);
        g.gridy = 6; g.insets = new Insets(0,  0, 8,  0); card.add(txtPassword, g);
        g.gridy = 7; g.insets = new Insets(0,  0, 4,  0); card.add(lblError,    g);
        g.gridy = 8; g.insets = new Insets(8,  0, 20, 0); card.add(btnIngresar, g);
        g.gridy = 9; g.insets = new Insets(0,  0, 0,  0); card.add(lblVersion,  g);

        return card;
    }

    // =========================================================================
    //  LOGO
    // =========================================================================
    private JPanel crearLogo(int size) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics gr) {
                Graphics2D g2 = (Graphics2D) gr.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), pad = 4;

                // Sombra
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillOval(pad + 2, pad + 2, w - pad * 2, h - pad * 2);

                // Circulo fondo teal
                GradientPaint gp = new GradientPaint(0, 0, TEAL, w, h, TEAL_DARK);
                g2.setPaint(gp);
                g2.fillOval(pad, pad, w - pad * 2, h - pad * 2);

                // Escudo blanco
                int cx = w / 2, cy = h / 2;
                int sw = (int)(w * 0.38), sh = (int)(h * 0.45);
                Path2D shield = new Path2D.Double();
                shield.moveTo(cx - sw, cy - sh * 0.85);
                shield.curveTo(cx - sw, cy - sh * 1.05, cx + sw, cy - sh * 1.05, cx + sw, cy - sh * 0.85);
                shield.lineTo(cx + sw, cy);
                shield.curveTo(cx + sw, cy + sh * 0.6, cx, cy + sh, cx, cy + sh);
                shield.curveTo(cx, cy + sh, cx - sw, cy + sh * 0.6, cx - sw, cy);
                shield.closePath();
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fill(shield);

                // Letra M
                g2.setColor(TEAL_DARK);
                int fs = (int)(size * 0.28);
                g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "M";
                g2.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(size, size));
        p.setMinimumSize(new Dimension(size, size));
        p.setMaximumSize(new Dimension(size, size));
        return p;
    }

    // =========================================================================
    //  LOGIN
    // =========================================================================
    private void intentarLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Completa todos los campos.");
            return;
        }
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        new SwingWorker<UsuarioSistema, Void>() {
            @Override protected UsuarioSistema doInBackground() {
                return new UsuarioDAO().validarLogin(usuario, password);
            }
            @Override protected void done() {
                try {
                    UsuarioSistema user = get();
                    if (user != null) {
                        lblError.setText(" ");
                        limpiar();
                        mainFrame.iniciarSesion(user);
                    } else {
                        mostrarError("Usuario o contrasena incorrectos.");
                        txtPassword.requestFocus();
                    }
                } catch (Exception ex) {
                    mostrarError("Error de conexion. Revisa que MySQL este activo.");
                } finally {
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("Ingresar");
                }
            }
        }.execute();
    }

    public void limpiar() {
        txtUsuario.setText("");
        txtPassword.setText("");
        lblError.setText(" ");
        txtUsuario.requestFocus();
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================
    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_GRIS);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lbl;
    }

    private JTextField crearCampo(String placeholder) {
        JTextField txt = new JTextField();
        estilizar(txt);
        txt.putClientProperty("JTextField.placeholderText", placeholder);
        agregarFocusBorde(txt);
        return txt;
    }

    private void estilizar(JTextComponent c) {
        c.setBackground(BG_INPUT);
        c.setForeground(TEXTO_BLANCO);
        c.setCaretColor(TEAL);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
    }

    private void agregarFocusBorde(JTextComponent c) {
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE_FOCUS, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
        });
    }

    private JButton crearBoton(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AZUL_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AZUL); }
        });
        return btn;
    }

    private void mostrarError(String msg) {
        lblError.setText("  " + msg);
    }
}
