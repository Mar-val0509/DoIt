package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

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
        btnAgregarEjercicio.setOnClickListener(v ->
                startActivity(new Intent(this, AgregarEjercicioActivity.class))
        );

        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> mostrarDialogoCancelar());
    }

    private void cargarEjerciciosDisponibles() {
        Cursor cursor = dbHelper.obtenerTodosLosEjercicios();

        int idCol = cursor.getColumnIndex("id_ejercicio");
        int nombreCol = cursor.getColumnIndex("nombre");
        int descripcionCol = cursor.getColumnIndex("descripcion");
        int uriCol = cursor.getColumnIndex("imagen_uri");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idCol);
            String nombre = cursor.getString(nombreCol);
            String descripcion = cursor.getString(descripcionCol);
            String imagenUri = cursor.getString(uriCol);

            View card = crearCardEjercicio(id, nombre, descripcion, imagenUri);
            layoutEjercicios.addView(card);
        }

        cursor.close();
    }

    private View crearCardEjercicio(int id, String nombre, String descripcion, String imagenUri) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(24, 24, 24, 24);
        container.setBackgroundResource(R.drawable.card_background);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.placeholder);

        if (imagenUri != null && !imagenUri.isEmpty()) {
            if (imagenUri.startsWith("http")) {
                Glide.with(this).load(imagenUri).placeholder(R.drawable.placeholder).into(imageView);
            } else if (imagenUri.startsWith("content://") || imagenUri.startsWith("file://")) {
                imageView.setImageURI(Uri.parse(imagenUri));
            } else {
                int resId = getResources().getIdentifier(imagenUri, "drawable", getPackageName());
                if (resId != 0) {
                    imageView.setImageResource(resId);
                } else {
                    imageView.setImageResource(R.drawable.placeholder);
                }
            }
        }

        TextView titleView = new TextView(this);
        titleView.setText(nombre);
        titleView.setTextSize(18);
        titleView.setPadding(24, 0, 0, 0);

        TextView descView = new TextView(this);
        descView.setText(descripcion);
        descView.setTextSize(14);
        descView.setPadding(24, 8, 0, 0);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.addView(titleView);
        textLayout.addView(descView);

        container.addView(imageView);
        container.addView(textLayout);

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


    @Override
    protected void onResume() {
        super.onResume();
        layoutEjercicios.removeAllViews();
        cargarEjerciciosDisponibles();
    }

}
