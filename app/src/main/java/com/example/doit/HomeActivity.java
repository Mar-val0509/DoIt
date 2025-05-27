package com.example.doit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private TextView txtDiasEntreno, txtDiasDescanso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtDiasEntreno = findViewById(R.id.txtDiasEntreno);
        txtDiasDescanso = findViewById(R.id.txtDiasDescanso);

        // Por ahora contadores de ejemplo
        int diasEjercicio = 5;
        int diasDescanso = 2;
        txtDiasEntreno.setText("Días de ejercicio: " + diasEjercicio);
        txtDiasDescanso.setText("Días de descanso: " + diasDescanso);

        // Botones grandes
        LinearLayout btnPersonalizado = findViewById(R.id.btnPersonalizado);
        LinearLayout btnExpress = findViewById(R.id.btnExpress);

        btnPersonalizado.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrenamientoPersonalizadoActivity.class);
            startActivity(intent);
        });

        btnExpress.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrenamientoExpressActivity.class);
            startActivity(intent);
        });

        // Navegación inferior
        findViewById(R.id.navInicio).setOnClickListener(v -> {
            // Ya estás en esta pantalla
        });

        findViewById(R.id.navPesas).setOnClickListener(v -> {
            // Ir a rutina
            startActivity(new Intent(this, PesasActivity.class));
        });

        findViewById(R.id.navRunning).setOnClickListener(v -> {
            // Ir a ejercicios
            startActivity(new Intent(this, RunningActivity.class));
        });

        findViewById(R.id.navPerfil).setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
        });

        findViewById(R.id.navSalir).setOnClickListener(v -> {
            // Logout y volver a login
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
