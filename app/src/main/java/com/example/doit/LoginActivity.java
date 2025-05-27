package com.example.doit;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn, goToRegisterBtn;
    private FirebaseAuth mAuth;
    private DoItDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView txtFrase = findViewById(R.id.txtFraseMotivacional);
        DoItDBHelper dbHelper = new DoItDBHelper(this);
        String frase = dbHelper.obtenerFraseAleatoria();
        txtFrase.setText(frase);

        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        loginBtn = findViewById(R.id.btnLogin);
        goToRegisterBtn = findViewById(R.id.btnGoRegister);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);

        btnTogglePassword.setOnClickListener(v -> {
            if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Mostrar contraseña
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            } else {
                // Ocultar contraseña
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            }
            // Mover el cursor al final
            passwordInput.setSelection(passwordInput.getText().length());
        });


        mAuth = FirebaseAuth.getInstance();

        goToRegisterBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance(); // Asegúrate de tener esto también
        dbHelper = new DoItDBHelper(this);  // <---- ¡AÑADE ESTA LÍNEA!

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            if (dbHelper.existeUsuario(uid)) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        }
    }

}
