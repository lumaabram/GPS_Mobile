package com.example.provapdm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GNSSA extends AppCompatActivity {
    private LocationManager locationManager; // Gerenciador de localização
    private LocationProvider locationProvider; // Provedor de localização
    private static final int REQUEST_LOCATION = 1; // Código de requisição de permissão
    private int latitudeFormato = Location.FORMAT_SECONDS; // Formato para latitude
    private int longitudeFormato = Location.FORMAT_SECONDS; // Formato para longitude
    private TextView textViewLocalizacao; // TextView para exibir a localização

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esfera_celeste_layout); // Define o layout da atividade
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // Inicializa o gerenciador de localização
        textViewLocalizacao = findViewById(R.id.textviewLocation_id); // Encontra a TextView de localização

        // Configura o clique na TextView para mostrar o diálogo de escolha de formato
        textViewLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoDeEscolha(); // Chama o método para mostrar o diálogo
            }
        });

        // Obtém permissão para o provedor de localização
        obtemLocationProvider_Permission();
    }

    public void mostrarDialogoDeEscolha() {
        // Opções de formato de coordenadas
        String[] formatos = {"Graus [+/-DDD.DDDDD]",
                "Graus-Minutos [+/-DDD:MM.MMMMM]",
                "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]"};

        // Cria um diálogo para escolher o formato de coordenadas
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Formato para as coordenadas:")
                .setSingleChoiceItems(formatos, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int escolha) {
                        // Define o formato de latitude e longitude com base na escolha
                        switch (escolha) {
                            case 0:
                                latitudeFormato = Location.FORMAT_DEGREES;
                                longitudeFormato = Location.FORMAT_DEGREES;
                                break;
                            case 1:
                                latitudeFormato = Location.FORMAT_MINUTES;
                                longitudeFormato = Location.FORMAT_MINUTES;
                                break;
                            case 2:
                                latitudeFormato = Location.FORMAT_SECONDS;
                                longitudeFormato = Location.FORMAT_SECONDS;
                                break;
                        }
                        dialog.dismiss(); // Fecha o diálogo

                        // Verifica permissões antes de obter a última localização
                        if (ActivityCompat.checkSelfPermission(GNSSA.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(GNSSA.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        Location ultimaLocalizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // Obtém a última localização conhecida
                        mostraLocation(ultimaLocalizacao); // Exibe a localização
                    }
                });
        builder.create().show(); // Mostra o diálogo
    }

    public void obtemLocationProvider_Permission() {
        // Verifica se a permissão de localização foi concedida
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER); // Obtém o provedor GPS
            startLocationAndGNSSUpdates(); // Inicia atualizações de localização e GNSS
        } else {
            // Solicita permissão para acessar a localização
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                obtemLocationProvider_Permission(); // Re-tenta obter permissão se concedida
            } else {
                Toast.makeText(this, "Sem permissão para acessar o sistema de posicionamento",
                        Toast.LENGTH_SHORT).show();
                finish(); // Finaliza a atividade se a permissão não for concedida
            }
        }
    }

    public void startLocationAndGNSSUpdates() {
        // Verifica permissão antes de solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Solicita atualizações de localização
        locationManager.requestLocationUpdates(locationProvider.getName(), 1000, 0.1f, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                mostraLocation(location); // Atualiza a localização exibida
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras); // Método padrão
            }
        });
        // Registra um callback para status de satélite GNSS
        locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                EsferaCelesteView esferaCelesteView = findViewById(R.id.esferacelesteview_id); // Encontra a vista da esfera celeste
                esferaCelesteView.setGnssStatus(status); // Define o status do GNSS na vista
                mostraGNSSGrafico(status); // Atualiza o gráfico GNSS
            }
        });
    }

    public void mostraGNSSGrafico(GnssStatus status) {
        ArrayList<String> sateliteIds = new ArrayList<>(); // IDs dos satélites
        ArrayList<Float> dadosQualidadeSinal = new ArrayList<>(); // Dados de qualidade do sinal
        int contagemSatelites = status.getSatelliteCount(); // Contagem de satélites
        for (int i = 0; i < contagemSatelites; i++) {
            int svid = status.getSvid(i); // Obtém o ID do satélite
            float qualidadeSinal = status.getCn0DbHz(i); // Obtém a qualidade do sinal do satélite
            sateliteIds.add(String.valueOf(svid)); // Adiciona o ID à lista
            dadosQualidadeSinal.add(qualidadeSinal); // Adiciona a qualidade do sinal à lista
        }
        // Atualiza o gráfico de qualidade de sinal
        QualidadeSateliteView qualidadeSateliteView = findViewById(R.id.QualidadeSateliteView);
        qualidadeSateliteView.setSignalQualityData(sateliteIds, dadosQualidadeSinal);
    }


    public void mostraLocation(Location localizacao) {
        String dados = "Posicionamento:\n"; // Inicia a string de dados
        if (localizacao != null) {
            // Converte a latitude e longitude no formato selecionado
            String latitudeSatelite = Location.convert(Math.abs(localizacao.getLatitude()), latitudeFormato);
            String longitudeSatelite = Location.convert(Math.abs(localizacao.getLongitude()), longitudeFormato);

            // Verifica se a latitude é negativa (hemisfério sul) e adiciona 'S'
            if (localizacao.getLatitude() < 0) {
                latitudeSatelite += " S";
            }

            // Verifica se a longitude é negativa (hemisfério oeste) e adiciona 'W'
            if (localizacao.getLongitude() < 0) {
                longitudeSatelite += " W";
            }

            // Concatena as informações da localização
            dados += "Latitude: " + latitudeSatelite + "\n"
                    + "Longitude: " + longitudeSatelite + "\n"
                    + "Velocidade (m/s): " + localizacao.getSpeed();


            Log.d("RumoView", "Direção definida: " + localizacao.getBearing()); // Loga a direção
            RumoView rumoView = findViewById(R.id.rumoView_id); // Encontra a vista de rumo
            rumoView.setDirecao(localizacao.getBearing()); // Atualiza a direção na vista de rumo
        } else {
            dados += "Localização Não disponível"; // Mensagem se a localização não estiver disponível
        }
        textViewLocalizacao.setText(dados); // Atualiza a TextView com os dados
        textViewLocalizacao.setTextColor(Color.parseColor("#800080")); // Define a cor da TextView para roxo
    }

}