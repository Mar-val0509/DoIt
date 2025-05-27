package com.example.doit;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class DatosInicialesActivity extends AppCompatActivity {

    private EditText edtNombre, edtEdad, edtPeso, edtAltura;
    private RadioGroup radioGroupSexo;
    private RadioButton radioHombre, radioMujer, radioOtro;
    private ImageView imgPerfil;
    private Button btnGuardar;

    private Uri imagenUri;
    private String fotoPerfilUrl;
    private DoItDBHelper dbHelper;
    private String uid;

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMARA = 102;
    private static final int REQUEST_PERMISOS = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_iniciales);

        edtNombre = findViewById(R.id.inputNombre);
        edtEdad = findViewById(R.id.inputEdad);
        edtPeso = findViewById(R.id.inputPeso);
        edtAltura = findViewById(R.id.inputAltura);
        radioGroupSexo = findViewById(R.id.groupSexo);
        radioHombre = findViewById(R.id.radioHombre);
        radioMujer = findViewById(R.id.radioMujer);
        radioOtro = findViewById(R.id.radioOtro);
        imgPerfil = findViewById(R.id.imgAvatar);
        btnGuardar = findViewById(R.id.btnGuardarDatos);

        radioHombre.setButtonTintList(ContextCompat.getColorStateList(this, R.color.rojo));
        radioMujer.setButtonTintList(ContextCompat.getColorStateList(this, R.color.rojo));
        radioOtro.setButtonTintList(ContextCompat.getColorStateList(this, R.color.rojo));


        dbHelper = new DoItDBHelper(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!dbHelper.existeUsuario(uid)) {
            dbHelper.insertarUsuarioVacio(uid);
        }

        cargarDatosUsuario();

        imgPerfil.setOnClickListener(v -> {
            if (tienePermisos()) {
                mostrarDialogoImagen();
            } else {
                solicitarPermisos();
            }
        });
        btnGuardar.setOnClickListener(v -> guardarDatosPerfil());
    }

    private void mostrarDialogoImagen() {
        String[] opciones = {"Seleccionar de galería", "Tomar foto"};
        new AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagen();
                    } else {
                        tomarFoto();
                    }
                }).show();
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fotoFile = crearArchivoTemporal();
        if (fotoFile != null) {
            imagenUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", fotoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri);
            startActivityForResult(intent, REQUEST_CAMARA);
        }
    }

    private File crearArchivoTemporal() {
        try {
            File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("avatar_", ".jpg", directorio);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri uriSeleccionada = null;

            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                uriSeleccionada = data.getData();
            } else if (requestCode == REQUEST_CAMARA) {
                uriSeleccionada = imagenUri; // o uriImagenActual, si lo nombras así
            }

            if (uriSeleccionada != null) {
                imagenUri = uriSeleccionada; // guarda temporalmente
                Glide.with(this).load(imagenUri).into(imgPerfil);
            }
        }
    }

    private void subirImagenAFirebase(Uri imagenUri, OnImageUploadListener listener) {
        String nombreArchivo = "fotos_perfil/" + uid + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(nombreArchivo);
        ref.putFile(imagenUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    listener.onSuccess(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseStorage", "Upload error", e);
                });
    }

    private void guardarDatosPerfil() {
        String nombre = edtNombre.getText().toString().trim();
        String edadStr = edtEdad.getText().toString().trim();
        String pesoStr = edtPeso.getText().toString().trim();
        String alturaStr = edtAltura.getText().toString().trim();
        String sexo;

        int selectedId = radioGroupSexo.getCheckedRadioButtonId();
        if (selectedId == R.id.radioHombre) sexo = "Hombre";
        else if (selectedId == R.id.radioMujer) sexo = "Mujer";
        else sexo = "Otro";

        if (nombre.isEmpty() || edadStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);
        double peso = Double.parseDouble(pesoStr);
        double altura = Double.parseDouble(alturaStr);

        if (imagenUri != null) {
            subirImagenAFirebase(imagenUri, url -> {
                fotoPerfilUrl = url;
                dbHelper.actualizarUsuario(uid, nombre, edad, peso, altura, sexo, fotoPerfilUrl);
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        } else {
            dbHelper.actualizarUsuario(uid, nombre, edad, peso, altura, sexo, fotoPerfilUrl);
            Toast.makeText(this, "Datos guardados sin imagen nueva", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void cargarDatosUsuario() {
        Cursor cursor = dbHelper.obtenerDatosUsuario(uid);
        if (cursor != null && cursor.moveToFirst()) {
            edtNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            edtEdad.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("edad"))));
            edtPeso.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("peso"))));
            edtAltura.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("altura"))));

            String sexo = cursor.getString(cursor.getColumnIndexOrThrow("sexo"));
            if ("Hombre".equals(sexo)) radioHombre.setChecked(true);
            else if ("Mujer".equals(sexo)) radioMujer.setChecked(true);
            else radioOtro.setChecked(true);

            cursor.close();
        }
    }

    private boolean tienePermisos() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarPermisos() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_PERMISOS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISOS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarDialogoImagen();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private interface OnImageUploadListener {
        void onSuccess(String url);
    }
}
