package com.hydratation.beber;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class MonCoeurView extends View {
    private int scoreHydratation = 0;
    private boolean auDemarrage = true;

    // Animation des yeux suspects
    private int decalagePupilleX = 0;
    private int directionAnimation = 1;
    private Handler animationHandler = new Handler();

    // Code triche par zones : 0 = Haut, 1 = Bas, 2 = Gauche, 3 = Droite
    private final int[] CODE_ZONES = {0, 1, 2, 3, 1, 3, 2, 2};
    private int indexCode = 0;
    private long tempsDernierClic = 0;
    private long tempsDernierClicMilieu = 0;

    private MainActivity activity;

    public MonCoeurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        demarrerAnimationYeux();
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Calcul dynamique de la couleur inversée (0% = Rouge, 100% = Bleu)
        float ratio = scoreHydratation / 100f;
        int rouge = (int) ((1 - ratio) * 250);
        int bleu = (int) (ratio * 250);
        int vert = (int) (ratio * 100);
        paint.setColor(Color.rgb(rouge, vert, bleu));

        // Dessin mathématique de la forme du cœur
        Path path = new Path();
        path.moveTo(w / 2f, h / 4f + 40);
        path.cubicTo(w / 4f, h / 10f, 0, h / 3f, w / 2f, h - 80);
        path.cubicTo(w, h / 3f, 3 * w / 4f, h / 10f, w / 2f, h / 4f + 40);
        path.close();
        canvas.drawPath(path, paint);

        // --- DESSIN DES YEUX PLISSÉS ET ANIMÉS ---
        int oeilG_X = w / 2 - 130;
        int oeilD_X = w / 2 + 30;
        int oeilY = h / 4 - 20;

        // Globles oculaires blancs
        paint.setColor(Color.WHITE);
        canvas.drawOval(new RectF(oeilG_X, oeilY, oeilG_X + 100, oeilY + 130), paint);
        canvas.drawOval(new RectF(oeilD_X, oeilY, oeilD_X + 100, oeilY + 130), paint);

        // Pupilles mobiles noires
        paint.setColor(Color.rgb(30, 30, 30));
        canvas.drawOval(new RectF(oeilG_X + 24 + decalagePupilleX * 2, oeilY + 44, oeilG_X + 76 + decalagePupilleX * 2, oeilY + 96), paint);
        canvas.drawOval(new RectF(oeilD_X + 24 + decalagePupilleX * 2, oeilY + 44, oeilD_X + 76 + decalagePupilleX * 2, oeilY + 96), paint);

        // Reflets de lumière blancs
        paint.setColor(Color.WHITE);
        canvas.drawOval(new RectF(oeilG_X + 36 + decalagePupilleX * 2, oeilY + 52, oeilG_X + 52 + decalagePupilleX * 2, oeilY + 68), paint);
        canvas.drawOval(new RectF(oeilD_X + 36 + decalagePupilleX * 2, oeilY + 52, oeilD_X + 52 + decalagePupilleX * 2, oeilY + 68), paint);

        // Sourcils noirs inclinés fâchés (effectuent l'effet de plissement)
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(12);
        canvas.drawLine(oeilG_X - 10, oeilY + 24, oeilG_X + 110, oeilY + 44, paint);
        canvas.drawLine(oeilD_X - 10, oeilY + 44, oeilD_X + 110, oeilY + 24, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int w = getWidth();
            int h = getHeight();

            // 1. Double tap au milieu pour Coucou Jennifer
            if (x >= (w/2f - 120) && x <= (w/2f + 120) && y >= (h/2f - 120) && y <= (h/2f + 120)) {
                long tempsActuel = System.currentTimeMillis();
                if (tempsActuel - tempsDernierClicMilieu < 500) {
                    if (activity != null) activity.ouvrirSurpriseJennifer();
                    return true;
                }
                tempsDernierClicMilieu = tempsActuel;
            }

            // 2. Détection tactile par zone géométrique (Code Triche)
            long tempsActuel = System.currentTimeMillis();
            if (tempsActuel - tempsDernierClic > 10000) {
                indexCode = 0;
            }
            tempsDernierClic = tempsActuel;

            int zoneDetectee = -1;
            if (y < x && y < (h - x)) zoneDetectee = 0;      // HAUT
            else if (y > x && y > (h - x)) zoneDetectee = 1; // BAS
            else if (x < y && x < (w - y)) zoneDetectee = 2; // GAUCHE
            else if (x > y && x > (w - y)) zoneDetectee = 3; // DROITE

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
        return true;
    }

    private void demarrerAnimationYeux() {
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                decalagePupilleX += directionAnimation;
                if (decalagePupilleX >= 8) directionAnimation = -1;
                else if (decalagePupilleX <= -8) directionAnimation = 1;
                invalidate(); // Force l'écran Android à se rafraîchir
                animationHandler.postDelayed(this, 120); // Balayage lent à 120ms
            }
        }, 120);
    }

    public void viderJauge() {
        this.auDemarrage = false;
        this.scoreHydratation = Math.min(this.scoreHydratation + vitesseTriche, 100);
        invalidate();
        if (activity != null) activity.actualiserTexteInterface(scoreHydratation);
    }

    public void forcerJauge(int valeur) {
        this.auDemarrage = false;
        this.scoreHydratation = valeur;
        invalidate();
        if (activity != null) activity.actualiserTexteInterface(scoreHydratation);
    }

    private void activerCheatMenu() {
        if (activity == null) return;
        activity.ouvrirCheatMenuAndroid(this);
    }
}
