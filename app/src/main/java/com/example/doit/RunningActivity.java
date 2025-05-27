package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity {

    private TextView txtDistanciaSemana, txtVelocidadMedia, txtRitmoPromedio, txtKmMasRapido, txtDistanciaMedia;
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        txtDistanciaSemana = findViewById(R.id.txtDistanciaSemana);
        txtVelocidadMedia = findViewById(R.id.txtVelocidadMedia);
        txtRitmoPromedio = findViewById(R.id.txtRitmoPromedio);
        txtKmMasRapido = findViewById(R.id.txtKmMasRapido);
        txtDistanciaMedia = findViewById(R.id.txtDistanciaMedia);

        Button btnIniciar = findViewById(R.id.btnIniciarTracker);
        btnIniciar.setOnClickListener(v -> startActivity(new Intent(this, TrakerActivity.class)));

        setupBottomNav();
        mostrarResumenEstadisticas();
    }

    private void mostrarResumenEstadisticas() {
        double totalDistancia = 0;
        double totalVelocidad = 0;
        double totalRitmo = 0;
        double kmMasRapido = Double.MAX_VALUE;
        int totalEntrenos = 0;

        String inicioSemana = obtenerFechaInicioSemana();

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT distancia, velocidad, ritmo FROM entrenamiento WHERE uid = ? AND fecha >= ?",
                new String[]{uid, inicioSemana});

        while (cursor.moveToNext()) {
            double distancia = cursor.getDouble(0);
            double velocidad = cursor.getDouble(1);
            double ritmo = cursor.getDouble(2);

            totalDistancia += distancia;
            totalVelocidad += velocidad;
            totalRitmo += ritmo;
            totalEntrenos++;

            if (distancia >= 1 && ritmo < kmMasRapido) {
                kmMasRapido = ritmo;
            }
        }

        cursor.close();

        double velocidadMedia = totalEntrenos > 0 ? totalVelocidad / totalEntrenos : 0;
        double ritmoPromedio = totalEntrenos > 0 ? totalRitmo / totalEntrenos : 0;
        double distanciaMedia = totalEntrenos > 0 ? totalDistancia / totalEntrenos : 0;

        txtDistanciaSemana.setText(String.format(Locale.getDefault(), "Distancia total esta semana: %.2f km", totalDistancia));
        txtVelocidadMedia.setText(String.format(Locale.getDefault(), "Velocidad media: %.2f km/h", velocidadMedia));
        txtRitmoPromedio.setText(String.format(Locale.getDefault(), "Ritmo promedio: %.2f min/km", ritmoPromedio));
        txtDistanciaMedia.setText(String.format(Locale.getDefault(), "Distancia media: %.2f km", distanciaMedia));
        txtKmMasRapido.setText(kmMasRapido < Double.MAX_VALUE ?
                String.format(Locale.getDefault(), "Km más rápido: %.2f min/km", kmMasRapido) :
                "Km más rápido: ---");
    }

    private String obtenerFechaInicioSemana() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date inicioSemana = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(inicioSemana);
    }

    private void setupBottomNav() {
        findViewById(R.id.navInicio).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        findViewById(R.id.navPesas).setOnClickListener(v -> {
            startActivity(new Intent(this, PesasActivity.class));
            finish();
        });

        findViewById(R.id.navRunning).setOnClickListener(v -> {
            // Ya estás aquí
        });

        findViewById(R.id.navPerfil).setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
            finish();
        });

        findViewById(R.id.navSalir).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
