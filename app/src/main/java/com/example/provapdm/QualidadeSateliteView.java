package com.example.provapdm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class QualidadeSateliteView extends View {
    private Paint paint;
    private ArrayList<String> nomesSatelites;
    private ArrayList<Float> dadosQualidadeSatelites;

    public QualidadeSateliteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QualidadeSateliteView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        nomesSatelites = new ArrayList<>();
        dadosQualidadeSatelites = new ArrayList<>();
    }

    public void setSignalQualityData(ArrayList<String> nomes, ArrayList<Float> qualidade) {
        // Limitar a quantidade de satélites a 15
        int maxSatelites = Math.min(nomes.size(), 15);
        this.nomesSatelites = new ArrayList<>(nomes.subList(0, maxSatelites));
        this.dadosQualidadeSatelites = new ArrayList<>(qualidade.subList(0, maxSatelites));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();


        float barraWidth = width / (nomesSatelites.size() * 1.2f);
        float maxBarraHeight = height - 100;
        float comecaX = (width - (barraWidth * nomesSatelites.size())) / 2;

        // Fundo preto
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, width, height, paint);

        // Bordas
        paint.setColor(Color.parseColor("#6A0DAD")); // Roxo
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5); // Largura da borda
        canvas.drawRect(0, 0, width, height, paint);

        // Título
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Qualidade do Sinal GNSS", width / 2 - paint.measureText("Qualidade do Sinal GNSS") / 2, 50, paint);

        // Desenhar as barras
        for (int i = 0; i < nomesSatelites.size(); i++) {
            float qualidadeSinal = dadosQualidadeSatelites.get(i);
            int cor = getCorQualidade(qualidadeSinal);

            paint.setShader(new LinearGradient(
                    comecaX + i * barraWidth, maxBarraHeight,
                    comecaX + (i * barraWidth) + barraWidth, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    cor, Color.DKGRAY, Shader.TileMode.CLAMP));

            // Desenhar a barra
            canvas.drawRoundRect(
                    comecaX + i * barraWidth, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    comecaX + (i * barraWidth) + barraWidth, maxBarraHeight,
                    20, 20, paint);

            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            // Bordas da barra
            canvas.drawRoundRect(
                    comecaX + i * barraWidth, maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight,
                    comecaX + (i * barraWidth) + barraWidth, maxBarraHeight,
                    20, 20, paint);

            // Desenhar nome do satélite
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            String svidTexto = nomesSatelites.get(i);
            canvas.drawText(svidTexto,
                    comecaX + (i * barraWidth) + barraWidth / 2 - paint.measureText(svidTexto) / 2,
                    maxBarraHeight + 30, paint);

            // Desenhar qualidade do sinal
            String textoQualidadeSinal = String.valueOf((int) qualidadeSinal);
            canvas.drawText(textoQualidadeSinal,
                    comecaX + (i * barraWidth) + barraWidth / 2 - paint.measureText(textoQualidadeSinal) / 2,
                    maxBarraHeight - (qualidadeSinal / 60) * maxBarraHeight - 10, paint);
        }
    }


    private int getCorQualidade(float qualidade) {
        if (qualidade < 20) {
            return Color.RED; // Baixa qualidade
        } else if (qualidade < 40) {
            return Color.parseColor("#FF69B4"); // Rosa
        } else {
            return Color.parseColor("#8A2BE2"); // Azul Violeta
        }
    }
}