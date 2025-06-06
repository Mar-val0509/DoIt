package com.example.doit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ResumenRutinaActivity extends AppCompatActivity {

    private LinearLayout layoutResumen;
    private EditText edtNombreRutina, edtDescripcionRutina;
    private List<Integer> ejerciciosSeleccionados;
    private DoItDBHelper dbHelper;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_rutina);

        layoutResumen = findViewById(R.id.layoutResumenEjercicios);
        edtNombreRutina = findViewById(R.id.edtNombreRutina);
        edtDescripcionRutina = findViewById(R.id.edtDescripcionRutina);
        Button btnGuardar = findViewById(R.id.btnGuardarRutina);
        Button btnCancelar = findViewById(R.id.btnCancelarRutina);
        btnCancelar.setOnClickListener(v -> mostrarDialogoConfirmacion());

        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ejerciciosSeleccionados = getIntent().getIntegerArrayListExtra("ejerciciosSeleccionados");
        if (ejerciciosSeleccionados == null || ejerciciosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No se seleccionaron ejercicios", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mostrarEjerciciosSeleccionados(ejerciciosSeleccionados);
        btnGuardar.setOnClickListener(v -> guardarRutina());
    }

    private void guardarRutina() {
        String nombreRutina = edtNombreRutina.getText().toString().trim();
        String descripcion = edtDescripcionRutina.getText().toString().trim();

        if (nombreRutina.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa nombre y descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        long idEntrenamiento = dbHelper.insertarRutinaPersonalizada(uid, nombreRutina, descripcion, "pesas");
        int idSerie = dbHelper.insertarSerieParaEntrenamiento((int) idEntrenamiento);

        for (int idEjercicio : ejerciciosSeleccionados) {
            dbHelper.insertarEjercicioEnSerie(idSerie, idEjercicio);
        }

        Toast.makeText(this, "Rutina guardada con éxito", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarEjerciciosSeleccionados(List<Integer> idsEjercicios) {
        LinearLayout layoutResumen = findViewById(R.id.layoutResumenEjercicios);
        layoutResumen.removeAllViews();

        List<String> nombres = dbHelper.obtenerNombresEjerciciosPorIds(idsEjercicios);
        List<String> imagenes = dbHelper.obtenerImagenesPorIds(idsEjercicios);

        for (int i = 0; i < idsEjercicios.size(); i++) {
            View card = getLayoutInflater().inflate(R.layout.item_ejercicio_rutina, null);

            TextView txtNombre = card.findViewById(R.id.txtNombreEjercicio);
            ImageView img = card.findViewById(R.id.imgEjercicio);
            Button btnAgregar = card.findViewById(R.id.btnAgregarSerie);

            txtNombre.setText(nombres.get(i));
            btnAgregar.setVisibility(View.GONE);

            String uri = imagenes.get(i);
            cargarImagen(img, uri);

            layoutResumen.addView(card);
        }
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

    private void mostrarDialogoConfirmacion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Cancelar rutina?")
                .setMessage("Perderás todos los datos que has introducido. ¿Deseas continuar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Intent intent = new Intent(this, PesasActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
