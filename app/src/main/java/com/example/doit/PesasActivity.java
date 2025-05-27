package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

public class PesasActivity extends AppCompatActivity {

    private LinearLayout layoutRutinas;
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesas);

        layoutRutinas = findViewById(R.id.layoutRutinas);
        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupHeader();
        cargarRutinas();
        setupBottomNav();

        Button btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(PesasActivity.this, HistorialEntrenamientosActivity.class);
            startActivity(intent);
        });

    }

    private void setupHeader() {
        LinearLayout headerRow = (LinearLayout) layoutRutinas.getChildAt(0);
        LinearLayout cardAgregarRutina = headerRow.findViewById(R.id.cardAgregarRutina);

        cardAgregarRutina.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearRutinaActivity.class);
            startActivity(intent);
        });
    }

    private void cargarRutinas() {
        Cursor cursor = dbHelper.obtenerRutinasPorUsuarioDisplay(uid);
        if (cursor == null || !cursor.moveToFirst()) return;

        // Borra todas las vistas excepto el primer hijo (la tarjeta "Añadir rutina")
        if (layoutRutinas.getChildCount() > 1) {
            layoutRutinas.removeViews(1, layoutRutinas.getChildCount() - 1);
        }

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setWeightSum(2);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        int contador = 0;

        do {
            int idRutina = cursor.getInt(cursor.getColumnIndexOrThrow("id_entrenamiento"));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"));

            LinearLayout tarjeta = crearTarjetaRutina(idRutina, nombre, descripcion);
            fila.addView(tarjeta);
            contador++;

            if (contador % 2 == 0) {
                layoutRutinas.addView(fila);
                fila = new LinearLayout(this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setWeightSum(2);
                fila.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
            }

        } while (cursor.moveToNext());

        // Añadir última fila si quedó incompleta
        if (fila.getChildCount() > 0) {
            layoutRutinas.addView(fila);
        }

        cursor.close();
    }


    private LinearLayout crearTarjetaRutina(int idRutina, String nombre, String descripcion) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        tarjeta.setBackground(ContextCompat.getDrawable(this, R.drawable.card_background));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(160), 1);
        params.setMargins(dpToPx(8), 0, dpToPx(8), dpToPx(16));
        tarjeta.setLayoutParams(params);
        tarjeta.setGravity(Gravity.START | Gravity.TOP);

        TextView titulo = new TextView(this);
        titulo.setText(nombre);
        titulo.setTextSize(16);
        titulo.setTextColor(getResources().getColor(R.color.black));
        titulo.setGravity(Gravity.START);
        titulo.setMaxLines(1);

        TextView desc = new TextView(this);
        desc.setText(descripcion);
        desc.setTextSize(12);
        desc.setTextColor(getResources().getColor(R.color.gris));
        desc.setGravity(Gravity.START);
        desc.setMaxLines(2);

        tarjeta.addView(titulo);
        tarjeta.addView(desc);

        tarjeta.setOnClickListener(v -> {
            Intent intent = new Intent(this, RutinaActivity.class);
            intent.putExtra("id_entrenamiento", idRutina);
            startActivity(intent);
        });

        return tarjeta;
    }



    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupBottomNav() {
        findViewById(R.id.navInicio).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

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

    @Override
    protected void onResume() {
        super.onResume();

        // Borra solo las rutinas dinámicas, deja el botón
        if (layoutRutinas.getChildCount() > 1) {
            layoutRutinas.removeViews(1, layoutRutinas.getChildCount() - 1);
        }

        cargarRutinas();
    }

}
