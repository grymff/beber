import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Timer;
import java.util.TimerTask;

public class CoeurInterface extends JFrame {
    private int scoreHydratation = 0; 
    private final JLabel texteLabel;
    private final JPanel coeurPanel;
    private int vitesseTriche = 25;
    private boolean auDemarrage = false;

    // --- VARIABLES DE L'ANIMATION DES YEUX ---
    private int decalagePupilleX = 0; 
    private int directionAnimation = 1; 
    private Timer animationTimer;

    // Code triche par zones : 0 = Haut, 1 = Bas, 2 = Gauche, 3 = Droite
    private final int[] CODE_ZONES = {0, 1, 2, 3, 1, 3, 2, 2}; 
    private int indexCode = 0; 
    private long tempsDernierClic = 0;

    public CoeurInterface() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); 
        setSize(400, 400);
        setLocationRelativeTo(null); 
        setAlwaysOnTop(true); 
        setLayout(new BorderLayout());

        coeurPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                float ratio = scoreHydratation / 100f;
                int rouge = (int) ((1 - ratio) * 250);
                int bleu = (int) (ratio * 250);
                int vert = (int) (ratio * 100); 
                g2d.setColor(new Color(rouge, vert, bleu));

                // Dessin de la forme géométrique du cœur
                GeneralPath path = new GeneralPath();
                path.moveTo(w / 2f, h / 4f + 20);
                path.curveTo(w / 4f, h / 10f, 0, h / 3f, w / 2f, h - 40);
                path.curveTo(w, h / 3f, 3 * w / 4f, h / 10f, w / 2f, h / 4f + 20);
                path.closePath();
                g2d.fill(path);

                // --- DESSIN DES YEUX PLISSÉS ET ANIMÉS ---
                int oeilG_X = w / 2 - 65;
                int oeilD_X = w / 2 + 15;
                int oeilY = h / 4 - 20;

                // Fond blanc des globes oculaires
                g2d.setColor(Color.WHITE);
                g2d.fillOval(oeilG_X, oeilY, 50, 65);
                g2d.fillOval(oeilD_X, oeilY, 50, 65);

                // Pupilles noires mobiles (gauche/droite)
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillOval(oeilG_X + 12 + decalagePupilleX, oeilY + 22, 26, 26);
                g2d.fillOval(oeilD_X + 12 + decalagePupilleX, oeilY + 22, 26, 26);

                // Reflets blancs brillants
                g2d.setColor(Color.WHITE);
                g2d.fillOval(oeilG_X + 18 + decalagePupilleX, oeilY + 26, 8, 8);
                g2d.fillOval(oeilD_X + 18 + decalagePupilleX, oeilY + 26, 8, 8);

                // --- LES SOURCILS ÉPAIS EFFECTUENT LE PLISSEMENT ---
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(5)); 

                // Sourcil/Paupière gauche
                g2d.drawLine(oeilG_X - 5, oeilY + 12, oeilG_X + 55, oeilY + 22);

                // Sourcil/Paupière droite
                g2d.drawLine(oeilD_X - 5, oeilY + 22, oeilD_X + 55, oeilY + 12);
            }
        };
        coeurPanel.setOpaque(false);
        coeurPanel.setLayout(new GridBagLayout());
        // --- CAPTEUR ABSOLU DE CLICS (CODE TRICHE ET PLEIN ÉCRAN JENNIFER) ---
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent) event;
                    if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getComponent() != null && SwingUtilities.isDescendingFrom(e.getComponent(), CoeurInterface.this)) {
                        
                        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), CoeurInterface.this);
                        int x = p.x;
                        int y = p.y;

                        // 1. DÉTECTION DU DOUBLE-CLIC GAUCHE AU MILIEU (Zone centrale 120x120)
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                            if (x >= 140 && x <= 260 && y >= 140 && y <= 260) {
                                afficherPleinEcranJennifer();
                                return; 
                            }
                        }

                        // 2. LOGIQUE DU CODE DE TRICHE GÉOMÉTRIQUE
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                            long tempsActuel = System.currentTimeMillis();
                            if (tempsActuel - tempsDernierClic > 10000) {
                                indexCode = 0;
                            }
                            tempsDernierClic = tempsActuel;

                            int zoneDetectee = -1;
                            if (y < x && y < (400 - x)) zoneDetectee = 0;      
                            else if (y > x && y > (400 - x)) zoneDetectee = 1; 
                            else if (x < y && x < (400 - y)) zoneDetectee = 2; 
                            else if (x > y && x > (400 - y)) zoneDetectee = 3; 

                            if (zoneDetectee == CODE_ZONES[indexCode]) {
                                indexCode++;
                                if (indexCode == CODE_ZONES.length) {
                                    indexCode = 0;
                                    activerCheatMenu();
                                }
                            } else {
                                indexCode = 0; 
                            }
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        // --- ENSEMBLE DES BOUTONS ET TEXTES ---
        texteLabel = new JLabel("VITE, À BOIRE !", SwingConstants.CENTER);
        texteLabel.setFont(new Font("Arial", Font.BOLD, 18));
        texteLabel.setForeground(Color.WHITE);

        JButton boutonBoire = new JButton("J'AI BU !");
        boutonBoire.setFont(new Font("Arial", Font.BOLD, 14));
        boutonBoire.setFocusPainted(false);
        boutonBoire.setBackground(Color.WHITE);
        boutonBoire.setForeground(Color.DARK_GRAY);
        boutonBoire.addActionListener(e -> {
            auDemarrage = false;
            scoreHydratation = 0; 
            mettreAJour();
            setVisible(false); 
        });

        JButton boutonPlusTard = new JButton("Plus tard");
        boutonPlusTard.setFont(new Font("Arial", Font.PLAIN, 12));
        boutonPlusTard.setFocusPainted(false);
        boutonPlusTard.setContentAreaFilled(false);
        boutonPlusTard.setForeground(Color.LIGHT_GRAY);
        boutonPlusTard.setBorder(BorderFactory.createEmptyBorder());
        boutonPlusTard.addActionListener(e -> {
            auDemarrage = false;
            setVisible(false);
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridy = 1; coeurPanel.add(texteLabel, gbc);
        gbc.gridy = 2; coeurPanel.add(boutonBoire, gbc);
        gbc.gridy = 3; coeurPanel.add(boutonPlusTard, gbc);

        add(coeurPanel, BorderLayout.CENTER);

        demarrerAnimationYeux();
    }

    private void demarrerAnimationYeux() {
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                decalagePupilleX += directionAnimation;
                if (decalagePupilleX >= 8) directionAnimation = -1;
                else if (decalagePupilleX <= -8) directionAnimation = 1;
                coeurPanel.repaint();
            }
        }, 0, 120); 
    }

    // --- SURPRISE : FENÊTRE PLEIN ÉCRAN POUR JENNIFER ---
    private void afficherPleinEcranJennifer() {
        JWindow pleinEcran = new JWindow();
        pleinEcran.setBackground(Color.BLACK);
        pleinEcran.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        pleinEcran.setLocation(0, 0);
        pleinEcran.setAlwaysOnTop(true);

        JLabel texteCoucou = new JLabel("coucou jennifer", SwingConstants.CENTER);
        texteCoucou.setFont(new Font("Arial", Font.BOLD, 72)); 
        texteCoucou.setForeground(new Color(138, 43, 226)); 
        pleinEcran.add(texteCoucou);

        pleinEcran.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pleinEcran.dispose();
            }
        });

        pleinEcran.setVisible(true);
    }

    public void configurerDemarrage() {
        this.auDemarrage = true;
        this.scoreHydratation = 0; 
        this.texteLabel.setText("Je vous surveille...");
    }

    public boolean isAuDemarrage() {
        return this.auDemarrage;
    }

    public void terminerDemarrage() {
        this.auDemarrage = false;
        setVisible(false);
    }

    private void activerCheatMenu() {
        String[] options = {
            "Forcer le Coeur Bleu (100%)", 
            "Forcer le Coeur Rouge (0%)", 
            "Fermer definitivement Beber", 
            "Annuler"
        };
        
        String messageHtml = "<html><table width='350'>"
            + "<tr>"
            + "  <td valign='top'><b>Code Secret Reussi !</b><br>Que voulez-vous faire ?</td>"
            + "  <td align='right' valign='top' style='font-family:Arial; font-size:10px; color:#8A2BE2;'>"
            + "    cree par ; GRYMFF<br>en aout 2026 pour jennifer"
            + "  </td>"
            + "</tr>"
            + "</table></html>";
        
        int choix = JOptionPane.showOptionDialog(
            this,
            messageHtml, 
            "Beber - Cheat Menu",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options
        );

        if (choix == 0) {
            this.auDemarrage = false;
            this.scoreHydratation = 100; 
            mettreAJour();
        } else if (choix == 1) {
            this.auDemarrage = false;
            this.scoreHydratation = 0; 
            mettreAJour();
        } else if (choix == 2) {
            System.exit(0);
        }
    }

    public void viderJauge() {
        this.scoreHydratation = Math.min(this.scoreHydratation + vitesseTriche, 100); 
        mettreAJour();
    }

    private void mettreAJour() {
        if (auDemarrage) {
            texteLabel.setText("Je vous surveille...");
        } else if (scoreHydratation == 100) {
            texteLabel.setText("Plein d'eau !");
        } else if (scoreHydratation == 0) {
            texteLabel.setText("VITE, À BOIRE !");
        } else {
            texteLabel.setText("Niveau de Bleu : " + scoreHydratation + "%");
        }
        coeurPanel.repaint();
    }
}
