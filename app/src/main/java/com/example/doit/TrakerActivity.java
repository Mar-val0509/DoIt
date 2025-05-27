package com.example.doit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;


public class TrakerActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    List<Location> locationList = new ArrayList<>();
    double totalDistance = 0.0; // en metros
    long startTime = 0;
    final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    Handler chronoHandler = new Handler();
    long chronoStart = 0L;
    Runnable chronoRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traker);

        // Obtener UID del usuario actual
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Crear instancia del helper
        DoItDBHelper dbHelper = new DoItDBHelper(this);

        // Consultar el sexo desde la base de datos
        String sexo = dbHelper.obtenerSexoUsuario(uid);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button startButton = findViewById(R.id.btnStart);
        Button stopButton = findViewById(R.id.btnStop);
        TextView distanceText = findViewById(R.id.txtDistance);
        TextView currentSpeedText = findViewById(R.id.txtSpeed); // velocidad actual
        TextView avgSpeedText = findViewById(R.id.txtAvgSpeed);  // velocidad media
        TextView paceText = findViewById(R.id.txtPace);          // ritmo promedio (min/km)



        startButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                startTime = System.currentTimeMillis();
                startLocationUpdates(distanceText, currentSpeedText, avgSpeedText, paceText);
            }
            chronoStart = SystemClock.elapsedRealtime();
            startChrono();


        } );

        stopButton.setOnClickListener(v -> {
            stopLocationUpdates();
            chronoHandler.removeCallbacks(chronoRunnable);

            chronoHandler.removeCallbacks(chronoRunnable);

            // Calcular los datos
            long elapsedMillis = SystemClock.elapsedRealtime() - chronoStart;
            int hours = (int) (elapsedMillis / (1000 * 60 * 60));
            int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
            int seconds = (int) (elapsedMillis / 1000) % 60;
            String duracionStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

            double distanciaKm = totalDistance / 1000.0;
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            double velocidadMedia = elapsedTime > 0 ? (distanciaKm / (elapsedTime / 3600.0)) : 0;
            double ritmoPromedio = distanciaKm > 0 ? (elapsedTime / 60.0) / distanciaKm : 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String fecha = sdf.format(new Date());

            String uid2 = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Guardar en base de datos
            DoItDBHelper dbHelper2 = new DoItDBHelper(this);
            dbHelper2.guardarEntrenamiento(uid2, fecha, duracionStr, distanciaKm, velocidadMedia, ritmoPromedio);

            Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();

        });
    }

    private void startLocationUpdates(TextView distanceText, TextView currentSpeedText, TextView avgSpeedText, TextView paceText) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // 1000 ms = 1 segundo
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location newLocation = locationResult.getLastLocation();
                if (!locationList.isEmpty()) {
                    Location lastLocation = locationList.get(locationList.size() - 1);
                    totalDistance += lastLocation.distanceTo(newLocation);
                }
                locationList.add(newLocation);

                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // segundos
                double distanceKm = totalDistance / 1000.0;

                double currentSpeedKmH = (newLocation.hasSpeed()) ? newLocation.getSpeed() * 3.6 : 0.0; // m/s * 3.6
                double avgSpeedKmH = elapsedTime > 0 ? (distanceKm / (elapsedTime / 3600.0)) : 0;       // km/h

                double pace = distanceKm > 0 ? (elapsedTime / 60.0) / distanceKm : 0; // min/km

                distanceText.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", distanceKm));
                currentSpeedText.setText(String.format(Locale.getDefault(), "Velocidad actual: %.2f km/h", currentSpeedKmH));
                avgSpeedText.setText(String.format(Locale.getDefault(), "Velocidad media: %.2f km/h", avgSpeedKmH));
                paceText.setText(String.format(Locale.getDefault(), "Ritmo promedio: %.2f min/km", pace));
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTime = System.currentTimeMillis();
        }
    }

    private void startChrono() {
        TextView chronoText = findViewById(R.id.txtChrono);

        chronoRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronoStart;
                int hours = (int) (elapsedMillis / (1000 * 60 * 60));
                int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
                int seconds = (int) (elapsedMillis / 1000) % 60;

                chronoText.setText(String.format(Locale.getDefault(), "Tiempo: %02d:%02d:%02d", hours, minutes, seconds));
                chronoHandler.postDelayed(this, 1000); // actualizar cada segundo
            }
        };

        chronoHandler.post(chronoRunnable);
    }

}
