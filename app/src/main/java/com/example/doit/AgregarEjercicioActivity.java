package com.example.doit;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.doit.DoItDBHelper;
import com.example.doit.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AgregarEjercicioActivity extends AppCompatActivity {

    private EditText edtNombre, edtDescripcion;
    private Button btnSeleccionarImagen, btnGuardar;
    private ImageView imageView;
    private Uri imagenUri;
    private DoItDBHelper dbHelper;

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMARA = 102;
    private static final int REQUEST_PERMISOS = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_ejercicio);

        edtNombre = findViewById(R.id.edtNombreEjercicio);
        edtDescripcion = findViewById(R.id.edtDescripcionEjercicio);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnGuardar = findViewById(R.id.btnGuardarEjercicio);
        imageView = findViewById(R.id.imgPreview);
        dbHelper = new DoItDBHelper(this);

        btnSeleccionarImagen.setOnClickListener(view -> {
            if (tienePermisos()) {
                mostrarDialogoImagen();
            } else {
                solicitarPermisos();
            }
        });
        btnGuardar.setOnClickListener(view -> guardarEjercicio());
    }

    private void guardarEjercicio() {
        String nombre = edtNombre.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || imagenUri == null) {
            Toast.makeText(this, "Completa todos los campos e incluye una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        subirImagenAFirebase(imagenUri, url -> {
            dbHelper.insertarEjercicio(nombre, descripcion, url);
            Toast.makeText(this, "Ejercicio agregado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void subirImagenAFirebase(Uri imagenUri, OnImageUploadListener listener) {
        String nombreArchivo = "ejercicios_fotos/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(nombreArchivo);
        ref.putFile(imagenUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> listener.onSuccess(uri.toString()))
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
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
                uriSeleccionada = imagenUri; // uriImagenActual
            }

            if (uriSeleccionada != null) {
                imagenUri = uriSeleccionada; // guarda temporalmente
                Glide.with(this).load(imagenUri).into(imageView);
            }
        }
    }
    private boolean tienePermisos() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
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