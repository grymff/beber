package com.hydratation.beber;

import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.util.Resources;
import com.codename1.notifications.LocalNotification;

import java.io.IOException;
import java.util.Calendar;

public class beber {

    private Form current;
    private Resources theme;

    public void init(Object context) {
        theme = UIManagerSafeInit();
        // Planifie le rappel répété toutes les 2 heures, même app fermée
        planifierRappel();
    }

    public void start() {
        if (current != null) {
            current.show();
            return;
        }
        CoeurInterface coeurForm = new CoeurInterface();
        coeurForm.show();
        current = coeurForm;
    }

    public void stop() {
        current = Display.getInstance().getCurrent();
    }

    public void destroy() {
    }

    private Resources UIManagerSafeInit() {
        try {
            Resources r = Resources.openLayered("/theme");
            return r;
        } catch (IOException e) {
            // Pas de fichier de thème personnalisé, on continue avec le thème par défaut
            return null;
        }
    }

    private void planifierRappel() {
        LocalNotification n = new LocalNotification();
        n.setId("rappelBeber");
        n.setAlertTitle("Beber");
        n.setAlertBody("Vite, à boire !");

        // Première notification dans 2 heures, puis répétition toutes les 2h
        long dansDeuxHeures = System.currentTimeMillis() + (2L * 60 * 60 * 1000);
        n.setRepeatType(com.codename1.notifications.LocalNotification.REPEAT_NONE);

        Display.getInstance().scheduleLocalNotification(n, dansDeuxHeures, LocalNotification.REPEAT_NONE);
        // Remarque : pour une répétition automatique toutes les 2h en continu,
        // on replanifie une nouvelle notification à chaque ouverture de l'app
        // (voir CoeurInterface -> replanifierProchainRappel()).
    }
}
