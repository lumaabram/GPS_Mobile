package com.example.provapdm;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus;
    private Paint paint;
    private int r;
    private int height, width;
    private Random random;
    private ArrayList<estrela> estrelas;

    private boolean filtroGPS = true;
    private boolean filtroGalileo = true;
    private boolean filtroGlonass = true;
    private boolean filtroUsado = true;

    // Variáveis para contagem de satélites
    private int satelitesDisponiveis = 0;
    private int satelitesEmUso = 0;

    private class estrela {
        float x, y, velocidade;

        estrela(float x, float y, float speed) {
            this.x = x;
            this.y = y;
            this.velocidade = speed;
        }

        void updatePosition() {
            y += velocidade;
            if (y > r) {
                y = -r;
                x = random.nextFloat() * 2 * r - r;
                velocidade = 0.5f + random.nextFloat();
            }
        }
    }

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        random = new Random();
        estrelas = new ArrayList<>();

        for (int i = 0; i < 300; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float radius = random.nextFloat() * r;
            float x = radius * (float) Math.cos(angle);
            float y = radius * (float) Math.sin(angle);
            float speed = 0.5f + random.nextFloat();
            estrelas.add(new estrela(x, y, speed));
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialog(context);
            }
        });
    }

    private boolean deveDesenharSatelite(int sateliteIndex) {
        int tipoConstelacao = newStatus.getConstellationType(sateliteIndex);
        boolean usandoFix = newStatus.usedInFix(sateliteIndex);
        boolean ChecandoConstelacao = (tipoConstelacao == GnssStatus.CONSTELLATION_GPS && filtroGPS) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GALILEO && filtroGalileo) ||
                (tipoConstelacao == GnssStatus.CONSTELLATION_GLONASS && filtroGlonass);
        boolean checandoFix = filtroUsado ? usandoFix : true;
        return ChecandoConstelacao && checandoFix;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (width < height)
            r = (int) (width / 2 * 0.9);
        else
            r = (int) (height / 2 * 0.9);

        // Reinicializa contagens
        satelitesDisponiveis = 0;
        satelitesEmUso = 0;

        desenharEstrelas(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
        int raio = r;
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        raio = (int) (raio * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        raio = (int) (raio * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computarXc(0), computarYc(0), raio, paint);
        canvas.drawLine(computarXc(0), computarYc(-r), computarXc(0), computarYc(r), paint);
        canvas.drawLine(computarXc(-r), computarYc(0), computarXc(r), computarYc(0), paint);
        paint.setStyle(Paint.Style.FILL);

        if (newStatus != null) {
            for (int i = 0; i < newStatus.getSatelliteCount(); i++) {
                float az = newStatus.getAzimuthDegrees(i);
                float el = newStatus.getElevationDegrees(i);
                float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
                float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));

                if (deveDesenharSatelite(i)) {
                    desenhaSatelite(canvas, computarXc(x), computarYc(y), newStatus.getConstellationType(i));
                    desenhaSateliteInfo(canvas, x, y, i);
                    satelitesDisponiveis++; // Contar satélites disponíveis
                    if (newStatus.usedInFix(i)) {
                        satelitesEmUso++; // Contar satélites em uso
                    }
                }
            }
        }

        // Desenhar informações de satélites disponíveis e em uso
        desenharInformacoesSatelites(canvas);

        postInvalidateDelayed(30);
    }

    private void desenharInformacoesSatelites(Canvas canvas) {
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        canvas.drawText("Satélites Disponíveis: " + satelitesDisponiveis, 20, 50, paint);
        canvas.drawText("Satélites em Uso: " + satelitesEmUso, 20, 100, paint);
    }

    private void desenharEstrelas(Canvas canvas) {
        paint.setColor(Color.WHITE);
        for (EsferaCelesteView.estrela estrela : estrelas) {
            if (Math.sqrt(estrela.x * estrela.x + estrela.y * estrela.y) <= r) {
                canvas.drawCircle(computarXc(estrela.x), computarYc(estrela.y), 2, paint);
            } else {
                float angulo = random.nextFloat() * 2 * (float) Math.PI;
                float raio = random.nextFloat() * r;
                estrela.x = raio * (float) Math.cos(angulo);
                estrela.y = raio * (float) Math.sin(angulo);
            }
            estrela.updatePosition();
        }
    }

    private void desenhaSatelite(Canvas canvas, float cx, float cy, int tipoConstelacao) {
        switch (tipoConstelacao) {
            case GnssStatus.CONSTELLATION_GPS:
                paint.setColor(Color.YELLOW);
                desenharOctaedro(canvas, cx, cy);
                break;

            case GnssStatus.CONSTELLATION_GALILEO:
                paint.setColor(Color.BLUE);
                canvas.drawCircle(cx, cy, 15, paint);
                break;

            case GnssStatus.CONSTELLATION_GLONASS:
                paint.setColor(Color.RED);
                float[] caminhoX = {cx, cx - 10, cx, cx + 10};
                float[] caminhoY = {cy - 10, cy, cy + 10, cy};
                canvas.drawPath(criarCaminho(caminhoX, caminhoY), paint);
                break;

            default:
                paint.setColor(Color.GRAY);
                canvas.drawRect(cx - 5, cy - 15, cx + 5, cy + 5, paint);
                break;
        }
    }

    private void desenharOctaedro(Canvas canvas, float cx, float cy) {
        float size = 15;
        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx - size, cy);
        path.lineTo(cx + size, cy);
        path.close();
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, size / 3, paint);
    }

    private Path criarCaminho(float[] xPoints, float[] yPoints) {
        Path path = new Path();
        path.moveTo(xPoints[0], yPoints[0]);
        path.lineTo(xPoints[1], yPoints[1]);
        path.lineTo(xPoints[2], yPoints[2]);
        path.lineTo(xPoints[3], yPoints[3]);
        path.close();
        return path;
    }

    private float computarXc(float x) {
        return width / 2 + x;
    }

    private float computarYc(float y) {
        return height / 2 - y;
    }

    public void setGnssStatus(GnssStatus status) {
        this.newStatus = status;
        invalidate();
    }

    private void mostrarDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Filtros de Satélites");
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        CheckBox cbGPS = new CheckBox(context);
        cbGPS.setText("GPS");
        cbGPS.setChecked(filtroGPS);
        layout.addView(cbGPS);
        CheckBox cbGalileo = new CheckBox(context);
        cbGalileo.setText("Galileo");
        cbGalileo.setChecked(filtroGalileo);
        layout.addView(cbGalileo);
        CheckBox cbGlonass = new CheckBox(context);
        cbGlonass.setText("GLONASS");
        cbGlonass.setChecked(filtroGlonass);
        layout.addView(cbGlonass);
        CheckBox cbUsado = new CheckBox(context);
        cbUsado.setText("Usado em Fix");
        cbUsado.setChecked(filtroUsado);
        layout.addView(cbUsado);
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filtroGPS = cbGPS.isChecked();
                filtroGalileo = cbGalileo.isChecked();
                filtroGlonass = cbGlonass.isChecked();
                filtroUsado = cbUsado.isChecked();
                invalidate(); // Atualiza a tela após as mudanças
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void desenhaSateliteInfo(Canvas canvas, float x, float y, int index) {
        paint.setColor(Color.BLACK); // Cor do texto
        paint.setTextSize(20);

        // Obter o SVID
        int svid = newStatus.getSvid(index);

        // Determinar a constelação
        String constelacao;
        switch (newStatus.getConstellationType(index)) {
            case GnssStatus.CONSTELLATION_GPS:
                constelacao = "GPS";
                break;
            case GnssStatus.CONSTELLATION_GALILEO:
                constelacao = "Galileo";
                break;
            case GnssStatus.CONSTELLATION_GLONASS:
                constelacao = "GLONASS";
                break;
            default:
                constelacao = "Desconhecida";
        }

        // Verificar se está sendo usado em Fix
        String usadoEmFix = newStatus.usedInFix(index) ? "Usado em Fix" : "Não Usado";

        // Desenhar as informações
        String info = "SVID: " + svid + " | " + constelacao + " | " + usadoEmFix;
        canvas.drawText(info, computarXc(x) + 10, computarYc(y) - 10, paint);
    }

}

