package com.example.doit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    EditText editNombre, editEdad, editPeso, editAltura;
    TextView txtTotalEntrenos, txtEjercicioFavorito;
    ImageView imgAvatar;
    Button btnGuardar;

    DoItDBHelper dbHelper;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Vistas
        editNombre = findViewById(R.id.editNombre);
        editEdad = findViewById(R.id.editEdad);
        editPeso = findViewById(R.id.editPeso);
        editAltura = findViewById(R.id.editAltura);
        txtTotalEntrenos = findViewById(R.id.txtTotalEntrenos);
        txtEjercicioFavorito = findViewById(R.id.txtEjercicioFavorito);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnGuardar = findViewById(R.id.btnGuardar);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbHelper = new DoItDBHelper(this);

        cargarDatosUsuario();
        cargarEstadisticas();
        setupBottomNav(); // Añadido

        btnGuardar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString();
            int edad = Integer.parseInt(editEdad.getText().toString());
            double peso = Double.parseDouble(editPeso.getText().toString());
            double altura = Double.parseDouble(editAltura.getText().toString());

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("edad", edad);
            values.put("peso", peso);
            values.put("altura", altura);

            db.update("usuario", values, "uid = ?", new String[]{uid});
            db.close();

            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatosUsuario() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, edad, peso, altura FROM usuario WHERE uid = ?", new String[]{uid});

        if (cursor.moveToFirst()) {
            editNombre.setText(cursor.getString(0));
            editEdad.setText(String.valueOf(cursor.getInt(1)));
            editPeso.setText(String.valueOf(cursor.getDouble(2)));
            editAltura.setText(String.valueOf(cursor.getDouble(3)));
        }

        cursor.close();
        db.close();
    }

    private void cargarEstadisticas() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Total entrenamientos
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM entrenamiento WHERE uid = ?", new String[]{uid});
        if (c1.moveToFirst()) {
            txtTotalEntrenos.setText("Entrenamientos hechos: " + c1.getInt(0));
        }
        c1.close();

        // Ejercicio favorito
        Cursor c2 = db.rawQuery("SELECT e.nombre, COUNT(*) as veces FROM serie_ejercicio se " +
                "JOIN ejercicio e ON se.id_ejercicio = e.id_ejercicio " +
                "JOIN serie s ON s.id_serie = se.id_serie " +
                "JOIN entrenamiento en ON s.id_entrenamiento = en.id_entrenamiento " +
                "WHERE en.uid = ? GROUP BY e.nombre ORDER BY veces DESC LIMIT 1", new String[]{uid});

        if (c2.moveToFirst()) {
            txtEjercicioFavorito.setText("Ejercicio favorito: " + c2.getString(0));
        } else {
            txtEjercicioFavorito.setText("Ejercicio favorito: ---");
        }

        c2.close();
        db.close();
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
            // Ya estás en esta pantalla
        });

        findViewById(R.id.navSalir).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
