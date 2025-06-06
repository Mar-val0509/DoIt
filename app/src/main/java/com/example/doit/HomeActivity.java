package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HomeActivity extends AppCompatActivity {

    private TextView txtRutinas, txtDiasDescanso;
    private DoItDBHelper dbHelper;
    private String uid;
    int rutinasCompletadas = 5;
    int diasDescanso = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        txtRutinas = findViewById(R.id.txtDiasEntreno);
        txtDiasDescanso = findViewById(R.id.txtDiasDescanso);

        setupBottomNav();
        actualizarDiasEjercicioYDescanso();

        LinearLayout btnPersonalizado = findViewById(R.id.btnPersonalizado);
        LinearLayout btnExpress = findViewById(R.id.btnExpress);

        btnPersonalizado.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearRutinaActivity.class);
            startActivity(intent);
        });

        btnExpress.setOnClickListener(v -> {
            Cursor cursor = dbHelper.obtenerEntrenamientoAleatorio(uid);
            if (cursor != null && cursor.moveToFirst()) {
                int idEntrenamiento = cursor.getInt(cursor.getColumnIndexOrThrow("id_entrenamiento"));
                cursor.close();

                Intent intent = new Intent(this, RutinaActivity.class);
                intent.putExtra("id_entrenamiento", idEntrenamiento);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No hay entrenamientos disponibles", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void actualizarDiasEjercicioYDescanso() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rutinasCompletadas = dbHelper.contarDiasEjercicio(uid);
        String fechaInicio = dbHelper.obtenerFechaMasAntigua(uid);

        diasDescanso = 0;
        if (fechaInicio != null) {
            try {
                LocalDate inicio = LocalDate.parse(fechaInicio);
                LocalDate hoy = LocalDate.now();
                long diasTotales = ChronoUnit.DAYS.between(inicio, hoy) + 1;
                diasDescanso = (int) diasTotales - rutinasCompletadas;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        txtRutinas.setText("Rutinas completadas: " + rutinasCompletadas);
        txtDiasDescanso.setText("Días de descanso: " + diasDescanso);
    }

    private void setupBottomNav() {
        findViewById(R.id.navPesas).setOnClickListener(v ->
                startActivity(new Intent(this, PesasActivity.class)));

        findViewById(R.id.navRunning).setOnClickListener(v ->
                startActivity(new Intent(this, RunningActivity.class)));

        findViewById(R.id.navPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        findViewById(R.id.navSalir).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
