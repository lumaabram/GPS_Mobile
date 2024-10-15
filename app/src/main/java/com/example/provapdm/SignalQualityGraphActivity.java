package com.example.provapdm;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;

public class SignalQualityGraphActivity extends Activity {

    private QualidadeSateliteView qualidadeSateliteView;
    private RumoView rumoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_quality_graph); // Referenciando o layout criado

        // Inicialização das views
        qualidadeSateliteView = findViewById(R.id.QualidadeSateliteView);
        rumoView = findViewById(R.id.rumoView_id);

        // Dados de exemplo
        ArrayList<String> nomesSatelites = new ArrayList<>();
        ArrayList<Float> qualidadeSinal = new ArrayList<>();

        // Adicionando dados de exemplo
        nomesSatelites.add("SVID1");
        nomesSatelites.add("SVID2");
        nomesSatelites.add("SVID3");
        qualidadeSinal.add(10f); // SNR para SVID1
        qualidadeSinal.add(30f); // SNR para SVID2
        qualidadeSinal.add(50f); // SNR para SVID3

        // Atualizando a view de qualidade dos satélites
        qualidadeSateliteView.setSignalQualityData(nomesSatelites, qualidadeSinal);

        // Definindo a direção (exemplo)
        rumoView.setDirecao(45); // Defina a direção desejada
    }
}