package com.example.doit;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrearRutinaActivity extends AppCompatActivity {

    private LinearLayout layoutEjercicios;
    private Set<Integer> ejerciciosSeleccionados = new HashSet<>();
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_rutina);

        layoutEjercicios = findViewById(R.id.layoutEjercicios);
        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        cargarEjerciciosDisponibles();

        Button btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> irAResumenRutina());

        ImageView btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio);
        btnAgregarEjercicio.setOnClickListener(v -> {
            startActivity(new Intent(this, AgregarEjercicioActivity.class));
        });

        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> mostrarDialogoCancelar());
    }

    private void cargarEjerciciosDisponibles() {
        Cursor cursor = dbHelper.obtenerTodosLosEjercicios();

        int idCol = cursor.getColumnIndex("id_ejercicio");
        int nombreCol = cursor.getColumnIndex("nombre");
        int uriCol = cursor.getColumnIndex("imagen_uri");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idCol);
            String nombre = cursor.getString(nombreCol);
            String imagenUri = cursor.getString(uriCol);

            View card = crearCardEjercicio(id, nombre, imagenUri);
            layoutEjercicios.addView(card);
        }

        cursor.close();
    }


    private View crearCardEjercicio(int id, String nombre, String imagenUri) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(24, 24, 24, 24);
        container.setBackgroundResource(R.drawable.card_background);

        // Imagen
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.placeholder); // por si falla

        if (imagenUri != null && !imagenUri.isEmpty()) {
            try {
                imageView.setImageURI(Uri.parse(imagenUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Texto
        TextView textView = new TextView(this);
        textView.setText(nombre);
        textView.setTextSize(18);
        textView.setPadding(24, 0, 0, 0);

        container.addView(imageView);
        container.addView(textView);

        // Cambio visual al seleccionar
        container.setOnClickListener(v -> {
            if (ejerciciosSeleccionados.contains(id)) {
                ejerciciosSeleccionados.remove(id);
                container.setAlpha(1f);
            } else {
                ejerciciosSeleccionados.add(id);
                container.setAlpha(0.5f);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        container.setLayoutParams(params);

        return container;
    }

    private void irAResumenRutina() {
        if (ejerciciosSeleccionados.isEmpty()) return;

        Intent intent = new Intent(this, ResumenRutinaActivity.class);
        intent.putIntegerArrayListExtra("ejerciciosSeleccionados", new ArrayList<>(ejerciciosSeleccionados));
        startActivity(intent);
    }

    private void mostrarDialogoCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar rutina")
                .setMessage("¿Estás seguro de que quieres cancelar? Perderás todos los datos introducidos.")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    Intent intent = new Intent(CrearRutinaActivity.this, PesasActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
