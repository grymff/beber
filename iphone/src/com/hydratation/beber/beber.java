package com.hydratation.beber;

import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Dialog;
import com.codename1.ui.util.UITimer;

public class beber {

    private Form current;
    private CoeurInterface fenetreCoeur;

    public void init(Object context) {
        // Paramétrage initial du thème mobile Codename One
    }
    
    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        
        fenetreCoeur = new CoeurInterface();
        fenetreCoeur.show();

        // Horloge d'hydratation : se déclenche toutes les 2 heures réelles (7200000 ms)
        UITimer.timer(7200000, true, () -> {
            Display.getInstance().callSerially(() -> {
                fenetreCoeur.viderJauge();
                fenetreCoeur.show(); // Fait réapparaître le cœur au premier plan du téléphone
            });
        });
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }
    
    public void destroy() {
    }
}
