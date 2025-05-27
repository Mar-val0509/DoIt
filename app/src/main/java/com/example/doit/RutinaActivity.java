package com.example.doit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.*;

public class RutinaActivity extends AppCompatActivity {

    private LinearLayout layoutEjercicios;
    private Button btnGuardarTodo;
    private DoItDBHelper dbHelper;
    private int id_rutina;
    private Map<Integer, List<View>> seriesPorEjercicio = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutina);

        layoutEjercicios = findViewById(R.id.layoutEjercicios);
        btnGuardarTodo = findViewById(R.id.btnGuardarTodo);
        dbHelper = new DoItDBHelper(this);

        id_rutina = getIntent().getIntExtra("id_entrenamiento", -1);
        if (id_rutina == -1) {
            Toast.makeText(this, "Rutina no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        agregarCabecera();
        cargarEjerciciosDeRutina();

        btnGuardarTodo.setOnClickListener(v -> guardarSeries());
    }

    private void agregarCabecera() {
        Cursor cursor = dbHelper.obtenerRutinaPorId(id_rutina);
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));

            TextView txtCabecera = new TextView(this);
            txtCabecera.setText(nombre);  // Solo el nombre, sin "Rutina:"
            txtCabecera.setTextSize(22);
            txtCabecera.setTextColor(getResources().getColor(R.color.black));
            txtCabecera.setPadding(0, dpToPx(16), 0, dpToPx(12));
            txtCabecera.setGravity(Gravity.CENTER);
            txtCabecera.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            layoutEjercicios.addView(txtCabecera);
        }
        cursor.close();
    }

    private void cargarEjerciciosDeRutina() {
        Cursor cursor = dbHelper.obtenerEjerciciosDeRutina(id_rutina);
        if (cursor == null || !cursor.moveToFirst()) return;

        do {
            int idEjercicio = cursor.getInt(cursor.getColumnIndexOrThrow("id_ejercicio"));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String imagenUri = cursor.getString(cursor.getColumnIndexOrThrow("imagen_uri"));

            View card = getLayoutInflater().inflate(R.layout.item_ejercicio_rutina, null);
            TextView txtNombre = card.findViewById(R.id.txtNombreEjercicio);
            ImageView img = card.findViewById(R.id.imgEjercicio);
            LinearLayout layoutSeries = card.findViewById(R.id.layoutSeries);
            Button btnAgregar = card.findViewById(R.id.btnAgregarSerie);

            txtNombre.setText(nombre);
            cargarImagen(img, imagenUri);

            List<View> listaSeries = new ArrayList<>();
            seriesPorEjercicio.put(idEjercicio, listaSeries);

            btnAgregar.setOnClickListener(v -> {
                View serieView = getLayoutInflater().inflate(R.layout.item_serie, null);
                layoutSeries.addView(serieView);
                listaSeries.add(serieView);
            });

            layoutEjercicios.addView(card);
        } while (cursor.moveToNext());

        cursor.close();
    }

    private void cargarImagen(ImageView img, String uri) {
        if (uri == null || uri.isEmpty()) {
            img.setImageResource(R.drawable.placeholder);
        } else if (uri.startsWith("http")) {
            Glide.with(this).load(uri).placeholder(R.drawable.placeholder).into(img);
        } else if (uri.startsWith("content://") || uri.startsWith("file://")) {
            img.setImageURI(Uri.parse(uri));
        } else {
            int resId = getResources().getIdentifier(uri, "drawable", getPackageName());
            img.setImageResource(resId != 0 ? resId : R.drawable.placeholder);
        }
    }

    private void guardarSeries() {
        // 1. Obtener nombre y descripción desde la base de datos mediante cursor
        Cursor cursor = dbHelper.obtenerRutinaPorId(id_rutina);
        if (cursor == null || !cursor.moveToFirst()) {
            Toast.makeText(this, "Error al recuperar datos de la rutina", Toast.LENGTH_SHORT).show();
            return;
        }

        cursor.close();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 2. Insertar registro en entrenamiento_realizado
        long idEntrenamientoRealizado = dbHelper.insertarEntrenamientoRealizado(id_rutina, uid, fechaActual);

        // 3. Guardar series asociadas a los ejercicios
        for (Map.Entry<Integer, List<View>> entry : seriesPorEjercicio.entrySet()) {
            int idEjercicio = entry.getKey();
            List<View> series = entry.getValue();

            for (View view : series) {
                EditText repsView = view.findViewById(R.id.inputRepeticiones);
                EditText pesoView = view.findViewById(R.id.inputPeso);

                String repsStr = repsView.getText().toString().trim();
                String pesoStr = pesoView.getText().toString().trim();

                if (!repsStr.isEmpty() && !pesoStr.isEmpty()) {
                    int repeticiones = Integer.parseInt(repsStr);
                    float peso = Float.parseFloat(pesoStr);

                    long idSerie = dbHelper.insertarSerie((int) idEntrenamientoRealizado);
                    dbHelper.insertarSerieEjercicio(idSerie, idEjercicio);
                    dbHelper.insertarRepeticion(idSerie, repeticiones, peso, 0f);
                }
            }
        }

        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
        finish();
    }


    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

}
