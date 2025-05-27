package com.example.doit;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, confirmInput;
    private Button registerBtn, goToLoginBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        confirmInput = findViewById(R.id.inputConfirmPassword);
        registerBtn = findViewById(R.id.btnRegister);
        goToLoginBtn = findViewById(R.id.btnGoLogin);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        EditText inputPassword = findViewById(R.id.inputPassword);
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            boolean isVisible = false;
            @Override
            public void onClick(View v) {
                if (isVisible) {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
                } else {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.ic_visibility);
                }
                inputPassword.setSelection(inputPassword.getText().length());
                isVisible = !isVisible;
            }
        });

        ImageButton btnTogglePassword2 = findViewById(R.id.btnTogglePassword2);
        EditText inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnTogglePassword2.setOnClickListener(new View.OnClickListener() {
            boolean isVisible2 = false;
            @Override
            public void onClick(View v) {
                if (isVisible2) {
                    inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword2.setImageResource(R.drawable.ic_visibility_off);
                } else {
                    inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword2.setImageResource(R.drawable.ic_visibility);
                }
                inputConfirmPassword.setSelection(inputConfirmPassword.getText().length());
                isVisible2 = !isVisible2;
            }
        });


        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirm = confirmInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DatosInicialesActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        goToLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
