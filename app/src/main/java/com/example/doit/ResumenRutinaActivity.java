package com.example.doit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
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

        mostrarResumenEjercicios();

        btnGuardar.setOnClickListener(v -> guardarRutina());
    }

    private void mostrarResumenEjercicios() {
        String resumen = dbHelper.obtenerResumenEjerciciosPorIds(ejerciciosSeleccionados);
        for (String nombre : resumen.split(", ")) {
            TextView textView = new TextView(this);
            textView.setText("- " + nombre);
            textView.setTextSize(16);
            textView.setPadding(8, 8, 8, 8);
            layoutResumen.addView(textView);
        }
    }

    private void guardarRutina() {
        String nombreRutina = edtNombreRutina.getText().toString().trim();
        String descripcion = edtDescripcionRutina.getText().toString().trim();

        if (nombreRutina.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa nombre y descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        long idEntrenamiento = dbHelper.insertarRutinaPersonalizada(uid, nombreRutina, descripcion);
        int idSerie = dbHelper.insertarSerieParaEntrenamiento((int) idEntrenamiento);

        for (int idEjercicio : ejerciciosSeleccionados) {
            dbHelper.insertarEjercicioEnSerie(idSerie, idEjercicio);
        }

        Toast.makeText(this, "Rutina guardada con éxito", Toast.LENGTH_SHORT).show();
        finish();
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
