package com.hydratation.beber;

import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Button;
import com.codename1.ui.Label;
import com.codename1.ui.Dialog;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.GeneralPath;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.notifications.LocalNotification;

public class CoeurInterface extends Form {

    private int scoreHydratation = 0;
    private final Label texteLabel;
    private final HeartComponent coeurComponent;
    private final int vitesseTriche = 25;
    private boolean auDemarrage = false;

    // Code triche par zones : 0=Haut, 1=Bas, 2=Gauche, 3=Droite
    private final int[] CODE_ZONES = {0, 1, 2, 3, 1, 3, 2, 2};
    private int indexCode = 0;
    private long tempsDernierClic = 0;
    private long tempsDernierClicCentre = 0;

    public CoeurInterface() {
        super("", new BorderLayout());
        getContentPane().getStyle().setBgColor(0x000000);
        getContentPane().getStyle().setBgTransparency(255);

        coeurComponent = new HeartComponent();

        texteLabel = new Label("VITE, À BOIRE !");
        texteLabel.getAllStyles().setFgColor(0xFFFFFF);
        texteLabel.getAllStyles().setBgTransparency(0);
        texteLabel.getAllStyles().setFont(Font.getDefaultFont().derive(18, Font.STYLE_BOLD));

        Button boutonBoire = new Button("J'AI BU !");
        styleBouton(boutonBoire, 0xFFFFFF, 0x333333);
        boutonBoire.addActionListener(e -> {
            auDemarrage = false;
            scoreHydratation = 0;
            mettreAJour();
            Display.getInstance().minimizeApplication();
        });

        Button boutonPlusTard = new Button("Plus tard");
        styleBouton(boutonPlusTard, 0x000000, 0xCCCCCC);
        boutonPlusTard.getAllStyles().setBgTransparency(0);
        boutonPlusTard.getAllStyles().setBorder(null);
        boutonPlusTard.addActionListener(e -> {
            auDemarrage = false;
            Display.getInstance().minimizeApplication();
        });

        Container bas = new Container(BoxLayout.y());
        bas.getAllStyles().setBgTransparency(0);
        bas.add(texteLabel);
        bas.add(boutonBoire);
        bas.add(boutonPlusTard);

        addComponent(BorderLayout.CENTER, coeurComponent);
        addComponent(BorderLayout.SOUTH, bas);

        demarrerAnimationYeux();
    }

    private void styleBouton(Button b, int fgColor, int bgColor) {
        Style s = b.getAllStyles();
        s.setFgColor(fgColor);
        s.setBgColor(bgColor);
        s.setBgTransparency(255);
        s.setFont(Font.getDefaultFont().derive(14, Font.STYLE_PLAIN));
    }

    private void demarrerAnimationYeux() {
        com.codename1.ui.util.UITimer animationTimer = new com.codename1.ui.util.UITimer(() -> {
            coeurComponent.avancerAnimation();
        });
        animationTimer.schedule(120, true, this);
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
        coeurComponent.setScore(scoreHydratation);
        coeurComponent.repaint();
    }

    private void activerCheatMenu() {
        com.codename1.ui.Command bleu = new com.codename1.ui.Command("Forcer le Coeur Bleu (100%)");
        com.codename1.ui.Command rouge = new com.codename1.ui.Command("Forcer le Coeur Rouge (0%)");
        com.codename1.ui.Command annuler = new com.codename1.ui.Command("Annuler");

        com.codename1.ui.Command choix = Dialog.show(
                "Beber - Cheat Menu",
                "Code Secret Réussi ! Que voulez-vous faire ?\n(créé par GRYMFF, août 2026, pour Jennifer)",
                bleu, rouge, annuler);

        if (choix == bleu) {
            this.auDemarrage = false;
            this.scoreHydratation = 100;
            mettreAJour();
        } else if (choix == rouge) {
            this.auDemarrage = false;
            this.scoreHydratation = 0;
            mettreAJour();
        }
    }

    private void afficherPleinEcranJennifer() {
        Form pleinEcran = new Form("", new com.codename1.ui.layouts.LayeredLayout());
        pleinEcran.getContentPane().getStyle().setBgColor(0x000000);
        pleinEcran.getContentPane().getStyle().setBgTransparency(255);

        Label texteCoucou = new Label("coucou jennifer");
        texteCoucou.getAllStyles().setFgColor(0x8A2BE2);
        texteCoucou.getAllStyles().setFont(Font.getDefaultFont().derive(48, Font.STYLE_BOLD));
        texteCoucou.setAlignment(Component.CENTER);

        pleinEcran.add(texteCoucou);
        pleinEcran.addPointerPressedListener(e -> {
            CoeurInterface.this.show();
        });
        pleinEcran.show();
    }

    /**
     * Composant custom qui dessine le cœur, les yeux animés, et gère les zones tactiles
     * (code triche par quadrants + double-tap central pour l'easter egg).
     */
    private class HeartComponent extends Component {

        private int score = 0;
        private int decalagePupilleX = 0;
        private int directionAnimation = 1;

        HeartComponent() {
            setUIID("Container");
        }

        void setScore(int s) {
            this.score = s;
        }

        void avancerAnimation() {
            decalagePupilleX += directionAnimation;
            if (decalagePupilleX >= 8) directionAnimation = -1;
            else if (decalagePupilleX <= -8) directionAnimation = 1;
            repaint();
        }

        @Override
        protected Dimension calcPreferredSize() {
            return new Dimension(400, 400);
        }

        @Override
        public void paint(Graphics g) {
            int x0 = getX();
            int y0 = getY();
            int w = getWidth();
            int h = getHeight();

            float ratio = score / 100f;
            int rouge = (int) ((1 - ratio) * 250);
            int bleu = (int) (ratio * 250);
            int vert = (int) (ratio * 100);
            g.setColor((rouge << 16) | (vert << 8) | bleu);

            GeneralPath path = new GeneralPath();
            path.moveTo(x0 + w / 2f, y0 + h / 4f + 20);
            path.curveTo(x0 + w / 4f, y0 + h / 10f, x0, y0 + h / 3f, x0 + w / 2f, y0 + h - 40);
            path.curveTo(x0 + w, y0 + h / 3f, x0 + 3 * w / 4f, y0 + h / 10f, x0 + w / 2f, y0 + h / 4f + 20);
            path.closePath();
            g.fillShape(path);

            int oeilG_X = x0 + w / 2 - 65;
            int oeilD_X = x0 + w / 2 + 15;
            int oeilY = y0 + h / 4 - 20;

            g.setColor(0xFFFFFF);
            g.fillArc(oeilG_X, oeilY, 50, 65, 0, 360);
            g.fillArc(oeilD_X, oeilY, 50, 65, 0, 360);

            g.setColor(0x1E1E1E);
            g.fillArc(oeilG_X + 12 + decalagePupilleX, oeilY + 22, 26, 26, 0, 360);
            g.fillArc(oeilD_X + 12 + decalagePupilleX, oeilY + 22, 26, 26, 0, 360);

            g.setColor(0xFFFFFF);
            g.fillArc(oeilG_X + 18 + decalagePupilleX, oeilY + 26, 8, 8, 0, 360);
            g.fillArc(oeilD_X + 18 + decalagePupilleX, oeilY + 26, 8, 8, 0, 360);

            g.setColor(0x000000);
            g.drawLine(oeilG_X - 5, oeilY + 12, oeilG_X + 55, oeilY + 22);
            g.drawLine(oeilD_X - 5, oeilY + 22, oeilD_X + 55, oeilY + 12);
        }

        @Override
        public void pointerPressed(int x, int y) {
            super.pointerPressed(x, y);
            int relX = x - getX();
            int relY = y - getY();
            int w = getWidth();
            int h = getHeight();

            // Double-tap zone centrale -> easter egg
            if (relX >= w / 2 - 60 && relX <= w / 2 + 60 && relY >= h / 2 - 60 && relY <= h / 2 + 60) {
                long maintenant = System.currentTimeMillis();
                if (maintenant - tempsDernierClicCentre < 400) {
                    afficherPleinEcranJennifer();
                    tempsDernierClicCentre = 0;
                    return;
                }
                tempsDernierClicCentre = maintenant;
            }

            // Code triche par quadrant
            long tempsActuel = System.currentTimeMillis();
            if (tempsActuel - tempsDernierClic > 10000) {
                indexCode = 0;
            }
            tempsDernierClic = tempsActuel;

            int zoneDetectee = -1;
            if (relY < relX && relY < (h - relX)) zoneDetectee = 0;
            else if (relY > relX && relY > (h - relX)) zoneDetectee = 1;
            else if (relX < relY && relX < (w - relY)) zoneDetectee = 2;
            else if (relX > relY && relX > (w - relY)) zoneDetectee = 3;

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
