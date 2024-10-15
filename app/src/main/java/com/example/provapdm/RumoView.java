package com.example.provapdm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RumoView extends View {
    private Paint paint;
    private Paint textPaint;
    private Paint setaPaint;
    private float direcao = 0; // Direção do satélite em graus

    public RumoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RumoView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        setaPaint = new Paint();
        setaPaint.setColor(Color.CYAN);
        setaPaint.setStyle(Paint.Style.FILL);
        setaPaint.setAntiAlias(true);
    }

    public void setDirecao(float direcao) {
        this.direcao = direcao;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float centroX = width / 2;
        float centroY = height / 2;
        float raio = Math.min(centroX, centroY) - 20;

        // Desenhar os pontos cardeais
        textPaint.setTextSize(30);
        canvas.drawText("N", centroX, centroY - raio + 50, textPaint);
        canvas.drawText("S", centroX, centroY + raio - 20, textPaint);
        canvas.drawText("E", centroX + raio - 20, centroY + 5, textPaint);
        canvas.drawText("W", centroX - raio + 20, centroY + 5, textPaint);

        // Desenhar o círculo
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(centroX, centroY, raio, paint);

        // Desenhar a agulha
        desenhaAgulha(canvas, centroX, centroY, raio);
    }

    private void desenhaAgulha(Canvas canvas, float centroX, float centroY, float raio) {
        float comprimentoSeta = raio - 40;
        float baseRaioSeta = 30;
        float fimX = (float) (centroX + Math.cos(Math.toRadians(direcao)) * comprimentoSeta);
        float fimY = (float) (centroY + Math.sin(Math.toRadians(direcao)) * comprimentoSeta);

        // Desenhar linha da agulha
        canvas.drawLine(centroX, centroY, fimX, fimY, setaPaint);

        // Desenhar base da agulha
        setaPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centroX, centroY, baseRaioSeta, setaPaint);

        // Desenhar ponta da agulha
        Path pontaSeta = new Path();
        pontaSeta.moveTo(fimX, fimY);
        pontaSeta.lineTo((float) (centroX + Math.cos(Math.toRadians(direcao - 150)) * 50),
                (float) (centroY + Math.sin(Math.toRadians(direcao - 150)) * 50));
        pontaSeta.lineTo((float) (centroX + Math.cos(Math.toRadians(direcao + 150)) * 50),
                (float) (centroY + Math.sin(Math.toRadians(direcao + 150)) * 50));
        pontaSeta.close();
        canvas.drawPath(pontaSeta, setaPaint);
    }
}