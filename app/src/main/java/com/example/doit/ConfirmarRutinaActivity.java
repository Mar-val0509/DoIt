package com.example.doit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ConfirmarRutinaActivity extends AppCompatActivity {

    private EditText inputNombreRutina, inputDescripcion;
    private TextView resumenEjercicios;
    private Button btnGuardar;
    private DoItDBHelper dbHelper;
    private String uid;
    private ArrayList<Integer> idsSeleccionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_rutina);

        inputNombreRutina = findViewById(R.id.inputNombreRutina);
        inputDescripcion = findViewById(R.id.inputDescripcionRutina);
        resumenEjercicios = findViewById(R.id.txtResumenEjercicios);
        btnGuardar = findViewById(R.id.btnGuardarRutina);
        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        idsSeleccionados = (ArrayList<Integer>) getIntent().getSerializableExtra("idsSeleccionados");

        // Mostrar resumen
        String resumen = dbHelper.obtenerResumenEjerciciosPorIds(idsSeleccionados);
        resumenEjercicios.setText(resumen);

        btnGuardar.setOnClickListener(v -> guardarRutina());
    }

    private void guardarRutina() {
        String nombre = inputNombreRutina.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar rutina (como entrenamiento con nombre y sin métricas)
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("fecha", "Rutina personalizada");
        values.put("duracion", "-");
        values.put("distancia", 0);
        values.put("velocidad", 0);
        values.put("ritmo", 0);
        values.put("nombre", nombre); // Asegúrate de que la columna exista
        db.insert("entrenamiento", null, values);

        // Obtener id del último entrenamiento insertado
        int idEntrenamiento = dbHelper.obtenerUltimoIdEntrenamiento();

        // Crear serie para este entrenamiento
        int idSerie = dbHelper.insertarSerieParaEntrenamiento(idEntrenamiento);

        // Asociar ejercicios a la serie
        for (int idEj : idsSeleccionados) {
            dbHelper.insertarEjercicioEnSerie(idSerie, idEj);
        }

        Toast.makeText(this, "Rutina guardada", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PesasActivity.class));
        finish();
    }
}
