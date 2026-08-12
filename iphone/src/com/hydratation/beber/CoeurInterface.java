package com.hydratation.beber;

import com.codename1.ui.*;
import com.codename1.ui.geom.GeneralPath;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.UITimer;

public class CoeurInterface extends Form {
    private int scoreHydratation = 0;
    private boolean auDemarrage = true;
    private int vitesseTriche = 25;

    // Animation des yeux
    private int decalagePupilleX = 0;
    private int directionAnimation = 1;

    // Code de triche : 0=Haut, 1=Bas, 2=Gauche, 3=Droite
    private final int[] CODE_ZONES = {0, 1, 2, 3, 1, 3, 2, 2};
    private int indexCode = 0;
    private long tempsDernierClic = 0;

    private Label texteLabel;
    private Container coeurDessinContainer;

    public CoeurInterface() {
        // Mode plein écran sombre sans barre de titre
        super(new LayeredLayout());
        getToolbar().hideToolbar();
        getContentPane().getUnselectedStyle().setBackgroundColor(0x000000);

        // 1. COMPOSANT DE DESSIN DU COEUR ET DES YEUX
        coeurDessinContainer = new Container() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setAntiAliased(true);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 40;
                int xOffset = (w - size) / 2;
                int yOffset = (h - size) / 4;

                // Calcul de la couleur inversée
                float ratio = scoreHydratation / 100f;
                int rouge = (int) ((1 - ratio) * 250);
                int bleu = (int) (ratio * 250);
                int vert = (int) (ratio * 100);
                int couleurHex = (rouge << 16) | (vert << 8) | bleu;
                g.setColor(couleurHex);

                // Dessin géométrique du cœur
                GeneralPath path = new GeneralPath();
                float centerX = w / 2f;
                float startY = yOffset + size / 4f;
                path.moveTo(centerX, startY);
                path.curveTo(w / 4f, yOffset, xOffset, yOffset + size / 3f, centerX, yOffset + size - 20);
                path.curveTo(w - xOffset, yOffset + size / 3f, 3 * w / 4f, yOffset, centerX, startY);
                g.fillShape(path);

                // --- DESSIN DES YEUX PLISSÉS ET ANIMÉS ---
                int oeilG_X = w / 2 - 60;
                int oeilD_X = w / 2 + 15;
                int oeilY = yOffset + size / 4;

                // Fond blanc des yeux
                g.setColor(0xffffff);
                g.fillArc(oeilG_X, oeilY, 45, 55, 0, 360);
                g.fillArc(oeilD_X, oeilY, 45, 55, 0, 360);

                // Pupilles noires mobiles
                g.setColor(0x1e1e1e);
                g.fillArc(oeilG_X + 10 + decalagePupilleX, oeilY + 20, 24, 24, 0, 360);
                g.fillArc(oeilD_X + 10 + decalagePupilleX, oeilY + 20, 24, 24, 0, 360);

                // Reflets brillants
                g.setColor(0xffffff);
                g.fillArc(oeilG_X + 15 + decalagePupilleX, oeilY + 24, 6, 6, 0, 360);
                g.fillArc(oeilD_X + 15 + decalagePupilleX, oeilY + 24, 6, 6, 0, 360);

                // Sourcils noirs inclinés qui masquent le haut pour l'effet plissé
                g.setColor(0x000000);
                g.drawLine(oeilG_X - 5, oeilY + 12, oeilG_X + 50, oeilY + 22);
                g.drawLine(oeilD_X - 5, oeilY + 22, oeilD_X + 50, oeilY + 12);
            }
        };

        // --- ENREGISTREUR DE CLICS TACTILES (CODE TRICHE ET PLEIN ÉCRAN) ---
        coeurDessinContainer.addPointerPressedListener(e -> {
            int x = e.getX();
            int y = e.getY();
            int w = coeurDessinContainer.getWidth();
            int h = coeurDessinContainer.getHeight();

            // Zone centrale 120x120 pour Jennifer
            if (x >= (w/2 - 60) && x <= (w/2 + 60) && y >= (h/2 - 60) && y <= (h/2 + 60)) {
                long tempsActuel = System.currentTimeMillis();
                if (tempsActuel - tempsDernierClic < 500) { // Détection du double-tap tactile
                    afficherPleinEcranJennifer();
                    return;
                }
            }

            long tempsActuel = System.currentTimeMillis();
            if (tempsActuel - tempsDernierClic > 10000) {
                indexCode = 0;
            }
            tempsDernierClic = tempsActuel;

            // Découpage en 4 triangles géométriques sur l'écran du téléphone
            int zoneDetectee = -1;
            float relX = (float)x / w * 400;
            float relY = (float)y / h * 400;

            if (relY < relX && relY < (400 - relX)) zoneDetectee = 0;
            else if (relY > relX && relY > (400 - relX)) zoneDetectee = 1;
            else if (relX < relY && relX < (400 - relY)) zoneDetectee = 2;
            else if (relX > relY && relX > (400 - relY)) zoneDetectee = 3;

            if (zoneDetectee == CODE_ZONES[indexCode]) {
                indexCode++;
                if (indexCode == CODE_ZONES.length) {
                    indexCode = 0;
                    activerCheatMenu();
                }
            } else {
                indexCode = 0;
            }
        });

        // 2. INTERFACE TEXTE ET BOUTONS INTERNES
        Container interfaceOverlay = new Container(new BorderLayout());
        Container centreLayout = new Container(new com.codename1.ui.layouts.BoxLayout(com.codename1.ui.layouts.BoxLayout.Y_AXIS));
        centreLayout.getStyle().setMarginTop(220);

        texteLabel = new Label("Je vous surveille...", "Label");
        texteLabel.getStyle().setFgColor(0xffffff);
        texteLabel.getStyle().setAlignment(Component.CENTER);

        Button boutonBoire = new Button("J'AI BU !");
        boutonBoire.addActionListener(e -> {
            auDemarrage = false;
            scoreHydratation = 0;
            mettreAJour();
        });

        Button boutonPlusTard = new Button("Plus tard");
        boutonPlusTard.addActionListener(e -> {
            auDemarrage = false;
            mettreAJour();
        });

        centreLayout.add(texteLabel).add(boutonBoire).add(boutonPlusTard);
        interfaceOverlay.add(BorderLayout.CENTER, centreLayout);

        // Assemblage des couches graphiques mobiles
        this.add(coeurDessinContainer);
        this.add(interfaceOverlay);

        // Lancement de l'animation lente des yeux (120ms)
        UITimer.timer(120, true, this, () -> {
            decalagePupilleX += directionAnimation;
            if (decalagePupilleX >= 6) directionAnimation = -1;
            else if (decalagePupilleX <= -6) directionAnimation = 1;
            coeurDessinContainer.repaint();
        });
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
        coeurDessinContainer.repaint();
    }

    private void afficherPleinEcranJennifer() {
        Form pleinEcran = new Form(new BorderLayout());
        pleinEcran.getToolbar().hideToolbar();
        pleinEcran.getContentPane().getUnselectedStyle().setBackgroundColor(0x000000);

        Label texteCoucou = new Label("coucou jennifer", "Label");
        texteCoucou.getStyle().setFont(Font.createTrueTypeFont("Arial", "Arial").derive(48, Font.STYLE_BOLD));
        texteCoucou.getStyle().setFgColor(0x8A2BE2); // Violet de la dédicace
        texteCoucou.getStyle().setAlignment(Component.CENTER);
        pleinEcran.add(BorderLayout.CENTER, texteCoucou);

        pleinEcran.addPointerPressedListener(evt -> this.showBack());
        pleinEcran.show();
    }

    private void activerCheatMenu() {
        // Boîte de dialogue mobile stylisée avec la dédicace en violet intégrée
        Command bleuCmd = new Command("Forcer le Coeur Bleu (100%)");
        Command rougeCmd = new Command("Forcer le Coeur Rouge (0%)");
        Command annulerCmd = new Command("Annuler");

        String texteMenu = "Code Secret Reussi !\nQue voulez-vous faire ?\n\ncree par ; GRYMFF\nen aout 2026 pour jennifer";

        Command choix = Dialog.show("Beber - Cheat Menu", texteMenu, new Command[]{bleuCmd, rougeCmd, annulerCmd});

        if (choix == bleuCmd) {
            this.auDemarrage = false;
            this.scoreHydratation = 100;
            mettreAJour();
        } else if (choix == rougeCmd) {
            this.auDemarrage = false;
            this.scoreHydratation = 0;
            mettreAJour();
        }
    }
}
