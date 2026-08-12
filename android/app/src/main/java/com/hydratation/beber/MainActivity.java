package com.hydratation.beber;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private MonCoeurView coeurView;
    private TextView texteLabel;
    private View layoutPrincipal;
    private View layoutJennifer;
    private Timer timerPrincipal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode plein écran immersif sans barre Android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Liaison avec l'interface XML d'Android Studio
        coeurView = findViewById(R.id.coeurView);
        texteLabel = findViewById(R.id.texteLabel);
        layoutPrincipal = findViewById(R.id.layoutPrincipal);
        layoutJennifer = findViewById(R.id.layoutJennifer);

        coeurView.setActivity(this);
        texteLabel.setText("Je vous surveille...");

        Button boutonBoire = findViewById(R.id.boutonBoire);
        boutonBoire.setOnClickListener(v -> {
            coeurView.forcerJauge(0); // Réinitialise en Rouge vif
            layoutPrincipal.setVisibility(View.GONE); // Se masque en silence
        });

        Button boutonPlusTard = findViewById(R.id.boutonPlusTard);
        boutonPlusTard.setOnClickListener(v -> layoutPrincipal.setVisibility(View.GONE));

        // Sortie de l'écran "Coucou Jennifer" lors d'un clic n'importe où
        layoutJennifer.setOnClickListener(v -> {
            layoutJennifer.setVisibility(View.GONE);
            layoutPrincipal.setVisibility(View.VISIBLE);
        });

        // --- TIMER PRINCIPAL D'HYDRATATION (2 HEURES RÉELLES) ---
        timerPrincipal = new Timer();
        timerPrincipal.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    coeurView.viderJauge();
                    layoutPrincipal.setVisibility(View.VISIBLE); // Fait jaillir le cœur sur le téléphone
                });
            }
        }, 60000, 7200000); // Attend 1 minute au démarrage, puis se répète toutes les 2 heures
    }

    public void actualiserTexteInterface(int score) {
        if (score == 100) texteLabel.setText("Plein d'eau !");
        else if (score == 0) texteLabel.setText("VITE, À BOIRE !");
        else texteLabel.setText("Niveau de Bleu : " + score + "%");
    }

    public void ouvrirSurpriseJennifer() {
        layoutPrincipal.setVisibility(View.GONE);
        layoutJennifer.setVisibility(View.VISIBLE); // Affiche le plein écran coucou jennifer
    }

    public void ouvrirCheatMenuAndroid(MonCoeurView view) {
        String messageHtml = "<b>Code Secret Reussi !</b><br>Que voulez-vous faire ?<br><br>"
                + "<font color='#8A2BE2'>cree par ; GRYMFF<br>en aout 2026 pour jennifer</font>";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Beber - Cheat Menu")
                .setMessage(Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("Forcer le Coeur Bleu (100%)", (d, w) -> view.forcerJauge(100))
                .setNegativeButton("Forcer le Coeur Rouge (0%)", (d, w) -> view.forcerJauge(0))
                .setNeutralButton("Fermer Beber", (d, w) -> System.exit(0))
                .create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy() {
            if (timerPrincipal != null) timerPrincipal.cancel();
        }
    }
}
