package com.example.doit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AgregarEjercicioActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private ImageView imgPreview;
    private EditText edtNombre, edtDescripcion;
    private Uri imagenUri = null;
    private DoItDBHelper dbHelper;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_ejercicio);

        imgPreview = findViewById(R.id.imgPreview);
        edtNombre = findViewById(R.id.edtNombreEjercicio);
        edtDescripcion = findViewById(R.id.edtDescripcionEjercicio);
        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnGuardarEjercicio = findViewById(R.id.btnGuardarEjercicio);
        Button btnCancelar = findViewById(R.id.btnCancelarAgregar);

        dbHelper = new DoItDBHelper(this);

        btnSeleccionarImagen.setOnClickListener(v -> mostrarOpcionesImagen());

        btnGuardarEjercicio.setOnClickListener(v -> guardarEjercicio());

        btnCancelar.setOnClickListener(v -> mostrarDialogoConfirmacion());

    }

    private void mostrarOpcionesImagen() {
        String[] opciones = {"Galería", "Cámara"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecciona una imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else if (which == 1) abrirCamara();
                })
                .show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = crearArchivoImagen();
                } catch (IOException ex) {
                    Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_SHORT).show();
                }

                if (photoFile != null) {
                    imagenUri = FileProvider.getUriForFile(this,
                            "com.example.doit.provider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }


    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = "IMG_" + timeStamp + "_";
        File almacenamientoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(nombreArchivo, ".jpg", almacenamientoDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imagenUri = data.getData();
                imgPreview.setImageURI(imagenUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && imagenUri != null) {
                imgPreview.setImageURI(imagenUri);
            }
        }
    }

    private void guardarEjercicio() {
        String nombre = edtNombre.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || imagenUri == null) {
            Toast.makeText(this, "Completa todos los campos e incluye una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.insertarEjercicio(nombre, descripcion, imagenUri.toString());

        Toast.makeText(this, "Ejercicio agregado", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarDialogoConfirmacion() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Cancelar?")
                .setMessage("Se perderán los datos introducidos. ¿Deseas cancelar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    finish();  // cierra la actividad y vuelve atrás
                })
                .setNegativeButton("No", null)
                .show();
    }

}
