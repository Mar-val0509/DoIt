package com.example.doit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.*;

public class TrakerActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final List<Location> locationList = new ArrayList<>();
    private long startTime = 0;
    private long pauseOffset = 0;
    private boolean isTracking = false;
    private boolean isPaused = false;
    private double totalDistance = 0;

    private Handler chronoHandler = new Handler();
    private Runnable chronoRunnable;

    private TextView distanceText, currentSpeedText, avgSpeedText, paceText, chronoText, txtCountdown;
    private Button startButton, stopButton, pauseButton, backButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traker);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001); // Código arbitrario de petición
        }


        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        distanceText = findViewById(R.id.txtDistance);
        currentSpeedText = findViewById(R.id.txtSpeed);
        avgSpeedText = findViewById(R.id.txtAvgSpeed);
        paceText = findViewById(R.id.txtPace);
        chronoText = findViewById(R.id.txtChrono);

        startButton = findViewById(R.id.btnStart);
        stopButton = findViewById(R.id.btnStop);
        pauseButton = findViewById(R.id.btnPause);
        backButton = findViewById(R.id.btnBack);
        txtCountdown = findViewById(R.id.txtCountdown);


        startButton.setOnClickListener(v -> {
            startButton.setEnabled(false);  // Evitar múltiples clics
            txtCountdown.setVisibility(View.VISIBLE);
            new CountDownTimer(4000, 1000) {
                int count = 3;

                @Override
                public void onTick(long millisUntilFinished) {
                    if (count > 0) {
                        txtCountdown.setText(String.valueOf(count));
                        count--;
                    } else {
                        txtCountdown.setText("¡YA!");
                    }
                }

                @Override
                public void onFinish() {
                    txtCountdown.setVisibility(View.GONE);
                    startTracking();  // Aquí empieza la carrera
                }
            }. start();
        });
        stopButton.setOnClickListener(v -> stopTracking());
        pauseButton.setOnClickListener(v -> togglePause());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void startTracking() {
        if (isTracking) return;

        isTracking = true;
        isPaused = false;
        startTime = SystemClock.elapsedRealtime() - pauseOffset;
        locationList.clear();
        totalDistance = 0;

        startChrono();
        startLocationUpdates();
    }

    private void stopTracking() {
        if (!isTracking) return;

        isTracking = false;
        isPaused = false;
        pauseOffset = 0;
        stopLocationUpdates();
        chronoHandler.removeCallbacks(chronoRunnable);

        long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
        int hours = (int) (elapsedMillis / (1000 * 60 * 60));
        int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
        int seconds = (int) (elapsedMillis / 1000) % 60;
        String duracionStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        double distanciaKm = totalDistance / 1000.0;
        long elapsedTime = elapsedMillis / 1000;
        double velocidadMedia = elapsedTime > 0 ? (distanciaKm / (elapsedTime / 3600.0)) : 0;
        double ritmoPromedio = distanciaKm > 0 ? (elapsedTime / 60.0) / distanciaKm : 0;

        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        dbHelper.guardarEntrenamiento(uid, fecha, duracionStr,"running", distanciaKm, velocidadMedia, ritmoPromedio);
        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
    }

    private void togglePause() {
        if (!isTracking) return;

        if (isPaused) {
            startTime = SystemClock.elapsedRealtime() - pauseOffset;
            startChrono();
            startLocationUpdates();
        } else {
            pauseOffset = SystemClock.elapsedRealtime() - startTime;
            chronoHandler.removeCallbacks(chronoRunnable);
            stopLocationUpdates();
        }

        isPaused = !isPaused;
    }

    private void startChrono() {
        chronoRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                int hours = (int) (elapsedMillis / (1000 * 60 * 60));
                int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
                int seconds = (int) (elapsedMillis / 1000) % 60;

                chronoText.setText(String.format(Locale.getDefault(), "Tiempo: %02d:%02d:%02d", hours, minutes, seconds));
                chronoHandler.postDelayed(this, 1000);
            }
        };

        chronoHandler.post(chronoRunnable);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location newLocation = result.getLastLocation();

                if (!locationList.isEmpty()) {
                    Location lastLocation = locationList.get(locationList.size() - 1);
                    totalDistance += lastLocation.distanceTo(newLocation);
                }

                locationList.add(newLocation);

                long elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000;
                double distanciaKm = totalDistance / 1000.0;
                double velocidadActual = newLocation.hasSpeed() ? newLocation.getSpeed() * 3.6 : 0.0;
                double velocidadMedia = elapsedTime > 0 ? (distanciaKm / (elapsedTime / 3600.0)) : 0;
                double ritmo = distanciaKm > 0 ? (elapsedTime / 60.0) / distanciaKm : 0;

                distanceText.setText(String.format(Locale.getDefault(), "Distancia: %.2f km", distanciaKm));
                currentSpeedText.setText(String.format(Locale.getDefault(), "Velocidad actual: %.2f km/h", velocidadActual));
                avgSpeedText.setText(String.format(Locale.getDefault(), "Velocidad media: %.2f km/h", velocidadMedia));
                paceText.setText(String.format(Locale.getDefault(), "Ritmo promedio: %.2f min/km", ritmo));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                Toast.makeText(this, "Permiso de ubicación requerido para registrar la carrera", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isTracking || !isPaused) {
            new AlertDialog.Builder(this)
                    .setTitle("¿Salir?")
                    .setMessage("La carrera no está guardada. ¿Seguro que deseas salir y perder los datos?")
                    .setPositiveButton("Salir", (dialog, which) -> {
                        finish();
                        startActivity(new Intent(this, RunningActivity.class));
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
