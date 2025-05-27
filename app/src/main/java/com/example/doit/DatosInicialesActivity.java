package com.example.doit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DatosInicialesActivity extends AppCompatActivity {

    private EditText inputNombre, inputEdad, inputPeso, inputAltura;
    private RadioGroup groupSexo;
    private Button btnGuardar;
    private DoItDBHelper dbHelper;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_iniciales);

        inputNombre = findViewById(R.id.inputNombre);
        inputEdad = findViewById(R.id.inputEdad);
        inputPeso = findViewById(R.id.inputPeso);
        inputAltura = findViewById(R.id.inputAltura);
        groupSexo = findViewById(R.id.groupSexo); // nuevo
        btnGuardar = findViewById(R.id.btnGuardarDatos);

        dbHelper = new DoItDBHelper(this);
        user = FirebaseAuth.getInstance().getCurrentUser();

        btnGuardar.setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString();
            int edad = Integer.parseInt(inputEdad.getText().toString());
            double peso = Double.parseDouble(inputPeso.getText().toString());
            double altura = Double.parseDouble(inputAltura.getText().toString());

            int selectedId = groupSexo.getCheckedRadioButtonId();
            String sexo;

            if (selectedId == R.id.radioHombre) sexo = "Hombre";
            else if (selectedId == R.id.radioMujer) sexo = "Mujer";
            else if (selectedId == R.id.radioOtro) sexo = "Otro";
            else {
                Toast.makeText(this, "Selecciona tu sexo", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.insertarUsuario(user.getUid(), nombre, edad, peso, altura, sexo);

            FirebaseAuth.getInstance().signOut(); // cerrar sesión automática tras guardar datos
            startActivity(new Intent(DatosInicialesActivity.this, LoginActivity.class));
            finish();
        });
    }
}
