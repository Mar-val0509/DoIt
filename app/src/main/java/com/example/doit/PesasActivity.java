package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PesasActivity extends AppCompatActivity {

    private LinearLayout layoutRutinas;
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesas);

        setupBottomNav();

        layoutRutinas = findViewById(R.id.layoutRutinas);
        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Evento de "Agregar rutina"
        LinearLayout cardAgregarRutina = findViewById(R.id.cardAgregarRutina);
        cardAgregarRutina.setOnClickListener(v -> {
            startActivity(new Intent(this, CrearRutinaActivity.class));
        });

        cargarRutinasDesdeDB();
    }

    private void cargarRutinasDesdeDB() {
        List<View> tarjetas = new ArrayList<>();
        Cursor cursor = dbHelper.obtenerRutinasPorUsuario(uid);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            String ejercicios = String.join(", ", dbHelper.obtenerNombresEjerciciosDeRutina(id));
            tarjetas.add(crearTarjeta(nombre, ejercicios));
        }

        cursor.close();

        // Insertar tarjetas de 2 en 2
        for (int i = 0; i < tarjetas.size(); i += 2) {
            LinearLayout fila = new LinearLayout(this);
            fila.setOrientation(LinearLayout.HORIZONTAL);
            fila.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            fila.setPadding(0, 0, 0, 16);

            fila.addView(tarjetas.get(i));

            if (i + 1 < tarjetas.size()) {
                fila.addView(tarjetas.get(i + 1));
            } else {
                View espacio = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 0, 1);
                espacio.setLayoutParams(lp);
                fila.addView(espacio);
            }

            layoutRutinas.addView(fila);
        }
    }

    private View crearTarjeta(String nombre, String descripcion) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(16, 16, 16, 16);
        tarjeta.setGravity(Gravity.START);
        tarjeta.setBackgroundResource(R.drawable.card_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 160);
        params.weight = 1;
        params.setMargins(8, 0, 8, 0);
        tarjeta.setLayoutParams(params);

        TextView txtNombre = new TextView(this);
        txtNombre.setText(nombre);
        txtNombre.setTextSize(16);
        txtNombre.setTextColor(getResources().getColor(android.R.color.black));
        txtNombre.setPadding(0, 0, 0, 8);
        txtNombre.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtDescripcion = new TextView(this);
        txtDescripcion.setText(descripcion);
        txtDescripcion.setTextSize(14);
        txtDescripcion.setTextColor(getResources().getColor(android.R.color.darker_gray));

        tarjeta.addView(txtNombre);
        tarjeta.addView(txtDescripcion);

        return tarjeta;
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
            startActivity(new Intent(this, RunningActivity.class));
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
