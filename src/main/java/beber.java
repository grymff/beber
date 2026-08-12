import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;

public class beber {
    public static void main(String[] args) {
        // INTERVALLE RÉEL : 2 heures (7 200 000 millisecondes)
        long period = 7200000; 

        SwingUtilities.invokeLater(() -> {
            // Création de l'interface du cœur
            CoeurInterface fenetreCoeur = new CoeurInterface();
            
            // ACTION IMMÉDIATE : Affiche le cœur directement au démarrage du PC
            fenetreCoeur.configurerDemarrage();
            fenetreCoeur.setVisible(true);

            Timer timer = new Timer();
            
            // Planification : attend 1 minute (60000 ms) au lancement initial pour vous 
            // laisser le temps de taper le code, puis se répète toutes les 2 heures (period).
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        // Si le cœur était encore visible du lancement, on le masque
                        if (fenetreCoeur.isAuDemarrage()) {
                            fenetreCoeur.terminerDemarrage();
                        }
                        
                        fenetreCoeur.viderJauge(); 
                        fenetreCoeur.setVisible(true); // Fait surgir le cœur toutes les 2h
                    });
                }
            }, 60000, period); // 60000 ms = 1 minute d'attente au tout premier démarrage
        });
    }
}
