package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity {

    private TextView txtDistanciaSemana, txtVelocidadMedia, txtRitmoPromedio, txtKmMasRapido, txtDistanciaMedia;
    private LinearLayout layoutUltimos;
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        setupBottomNav();

        txtDistanciaSemana = findViewById(R.id.txtDistanciaSemana);
        txtVelocidadMedia = findViewById(R.id.txtVelocidadMedia);
        txtRitmoPromedio = findViewById(R.id.txtRitmoPromedio);
        txtKmMasRapido = findViewById(R.id.txtKmMasRapido);
        txtDistanciaMedia = findViewById(R.id.txtDistanciaMedia);
        layoutUltimos = findViewById(R.id.layoutUltimosEntrenamientos);
        Button btnIniciar = findViewById(R.id.btnIniciarTracker);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbHelper = new DoItDBHelper(this);

        cargarEstadisticas();
        cargarUltimosEntrenamientos();

        btnIniciar.setOnClickListener(v -> {
            startActivity(new Intent(this, TrakerActivity.class));
        });
    }

    private void cargarEstadisticas() {
        double totalKm = 0, totalVel = 0, totalRitmo = 0;
        int count = 0;
        double minRitmo = Double.MAX_VALUE;

        // Obtenemos la fecha hace 7 días
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String fechaLimite = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT fecha, distancia, velocidad, ritmo FROM entrenamiento WHERE uid = ?", new String[]{uid});
        while (c.moveToNext()) {
            String fecha = c.getString(0).split(" ")[0];
            if (fecha.compareTo(fechaLimite) >= 0) {
                double dist = c.getDouble(1);
                double vel = c.getDouble(2);
                double ritmo = c.getDouble(3);

                totalKm += dist;
                totalVel += vel;
                totalRitmo += ritmo;
                if (ritmo < minRitmo) minRitmo = ritmo;
                count++;
            }
        }
        c.close();

        double avgVel = count > 0 ? totalVel / count : 0;
        double avgRitmo = count > 0 ? totalRitmo / count : 0;
        double avgDist = count > 0 ? totalKm / count : 0;

        txtDistanciaSemana.setText(String.format(Locale.getDefault(), "Distancia total esta semana: %.2f km", totalKm));
        txtVelocidadMedia.setText(String.format(Locale.getDefault(), "Velocidad media: %.2f km/h", avgVel));
        txtRitmoPromedio.setText(String.format(Locale.getDefault(), "Ritmo promedio: %.2f min/km", avgRitmo));
        txtKmMasRapido.setText(minRitmo < Double.MAX_VALUE ?
                String.format(Locale.getDefault(), "Km más rápido: %.2f min/km", minRitmo)
                : "Km más rápido: ---");
        txtDistanciaMedia.setText(String.format(Locale.getDefault(), "Distancia media: %.2f km", avgDist));
    }

    private void cargarUltimosEntrenamientos() {
        layoutUltimos.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT fecha, distancia, ritmo FROM entrenamiento WHERE uid = ? AND tipo = 'running' ORDER BY fecha DESC LIMIT 5",
                new String[]{uid}
        );
        while (c.moveToNext()) {
            String fecha = c.getString(0);
            double distancia = c.getDouble(1);
            double ritmo = c.getDouble(2);

            TextView txt = new TextView(this);
            txt.setText(String.format(" %s - %.2f km | %.2f min/km", fecha, distancia, ritmo));
            txt.setTextSize(14);
            txt.setTextColor(getColor(android.R.color.black));
            txt.setPadding(8, 8, 8, 8);
            layoutUltimos.addView(txt);
        }
        c.close();
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
