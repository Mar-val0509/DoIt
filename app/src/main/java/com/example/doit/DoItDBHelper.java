package com.example.doit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DoItDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "doit.db";
    private static final int DB_VERSION = 2;

    public DoItDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS usuario (" +
                "uid TEXT PRIMARY KEY, " +
                "nombre TEXT, edad INTEGER, peso REAL, altura REAL, sexo TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS entrenamiento (" +
                "id_entrenamiento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid TEXT, " +
                "nombre TEXT, " +
                "fecha TEXT, duracion TEXT, " +
                "distancia REAL, velocidad REAL, ritmo REAL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS ejercicio (" +
                "id_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "imagen_uri TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS serie (" +
                "id_serie INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_entrenamiento INTEGER, " +
                "FOREIGN KEY(id_entrenamiento) REFERENCES entrenamiento(id_entrenamiento))");

        db.execSQL("CREATE TABLE IF NOT EXISTS serie_ejercicio (" +
                "id_serie INTEGER, " +
                "id_ejercicio INTEGER, " +
                "FOREIGN KEY(id_serie) REFERENCES serie(id_serie), " +
                "FOREIGN KEY(id_ejercicio) REFERENCES ejercicio(id_ejercicio))");

        db.execSQL("CREATE TABLE IF NOT EXISTS frase_motivacional (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "texto TEXT NOT NULL)");

        db.execSQL("INSERT INTO frase_motivacional (texto) VALUES " +
                "('Hoy es un buen día para superarte')," +
                "('No pares cuando estés cansado, para cuando termines')," +
                "('Cada día cuenta')," +
                "('Cree en ti y hazlo posible')," +
                "('Tu cuerpo puede más de lo que crees')," +
                "('Hazlo con pasión o no lo hagas')," +
                "('El único entrenamiento malo es el que no se hace')");

        db.execSQL("CREATE TABLE IF NOT EXISTS repeticion (" +
                "id_repeticion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_serie INTEGER, " +
                "repeticiones INTEGER, " +
                "peso_usado REAL, " +
                "distancia_recorrida REAL, " +
                "FOREIGN KEY(id_serie) REFERENCES serie(id_serie))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS serie_ejercicio");
        db.execSQL("DROP TABLE IF EXISTS serie");
        db.execSQL("DROP TABLE IF EXISTS ejercicio");
        db.execSQL("DROP TABLE IF EXISTS entrenamiento");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        onCreate(db);
    }

    public void insertarUsuario(String uid, String nombre, int edad, double peso, double altura, String sexo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("nombre", nombre);
        values.put("edad", edad);
        values.put("peso", peso);
        values.put("altura", altura);
        values.put("sexo", sexo);
        db.insert("usuario", null, values);
        db.close();
    }

    public boolean existeUsuario(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuario WHERE uid = ?", new String[]{uid});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();
        return existe;
    }

    public String obtenerSexoUsuario(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT sexo FROM usuario WHERE uid = ?", new String[]{uid});
        String sexo = "Otro";
        if (cursor.moveToFirst()) {
            sexo = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return sexo;
    }

    public void guardarEntrenamiento(String uid, String fecha, String duracion,
                                     double distanciaKm, double velocidadMedia, double ritmoPromedio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("fecha", fecha);
        values.put("duracion", duracion);
        values.put("distancia", distanciaKm);
        values.put("velocidad", velocidadMedia);
        values.put("ritmo", ritmoPromedio);
        db.insert("entrenamiento", null, values);
        db.close();
    }

    public long insertarEjercicio(String nombre, String descripcion, String imagenUri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("imagen_uri", imagenUri);
        return db.insert("ejercicio", null, values);
    }


    public Cursor obtenerTodosLosEjercicios() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT id_ejercicio, nombre FROM ejercicio", null);
    }


    public long insertarSerie(int idEntrenamiento) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_entrenamiento", idEntrenamiento);
        return db.insert("serie", null, values);
    }

    public void insertarSerieEjercicio(long idSerie, long idEjercicio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("id_ejercicio", idEjercicio);
        db.insert("serie_ejercicio", null, values);
    }

    public void insertarRepeticion(long idSerie, int repeticiones, double pesoUsado, Double distanciaRecorrida) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("repeticiones", repeticiones);
        values.put("peso_usado", pesoUsado);
        if (distanciaRecorrida != null) {
            values.put("distancia_recorrida", distanciaRecorrida);
        }
        db.insert("repeticion", null, values);
    }

    public Cursor obtenerRutinasPorUsuario(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT id_entrenamiento, nombre FROM entrenamiento WHERE uid = ?", new String[]{uid});
    }

    public List<String> obtenerNombresEjerciciosDeRutina(int idEntrenamiento) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> ejercicios = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT e.nombre FROM ejercicio e " +
                        "JOIN serie_ejercicio se ON se.id_ejercicio = e.id_ejercicio " +
                        "JOIN serie s ON s.id_serie = se.id_serie " +
                        "WHERE s.id_entrenamiento = ?",
                new String[]{String.valueOf(idEntrenamiento)}
        );
        while (cursor.moveToNext()) {
            ejercicios.add(cursor.getString(0));
        }
        cursor.close();
        return ejercicios;
    }

    public String obtenerResumenEjerciciosPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return "";
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i > 0 ? ",?" : "?");
            args[i] = String.valueOf(ids.get(i));
        }
        Cursor cursor = db.rawQuery("SELECT nombre FROM ejercicio WHERE id_ejercicio IN (" + placeholders + ")", args);
        List<String> nombres = new ArrayList<>();
        while (cursor.moveToNext()) {
            nombres.add(cursor.getString(0));
        }
        cursor.close();
        return String.join(", ", nombres);
    }

    public int obtenerUltimoIdEntrenamiento() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id_entrenamiento) FROM entrenamiento", null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public int insertarSerieParaEntrenamiento(int idEntrenamiento) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_entrenamiento", idEntrenamiento);
        long id = db.insert("serie", null, values);
        return (int) id;
    }

    public void insertarEjercicioEnSerie(int idSerie, int idEjercicio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("id_ejercicio", idEjercicio);
        db.insert("serie_ejercicio", null, values);
    }

    public long insertarRutinaPersonalizada(String uid, String nombre, String descripcion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("nombre", nombre);
        values.put("fecha", ""); // puedes modificar si usas fecha
        values.put("duracion", descripcion); // reutilizando campo "duracion" para la descripción breve
        values.put("distancia", 0);
        values.put("velocidad", 0);
        values.put("ritmo", 0);
        return db.insert("entrenamiento", null, values);
    }


    public String obtenerFraseAleatoria() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT texto FROM frase_motivacional ORDER BY RANDOM() LIMIT 1", null);
        String frase = "¡Hazlo por ti!";
        if (cursor.moveToFirst()) {
            frase = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return frase;
    }

    public Cursor obtenerEntrenamientosPorUsuario(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM entrenamiento WHERE uid = ? ORDER BY fecha DESC", new String[]{uid});
    }

    public static class Estadisticas {
        public double distanciaMedia;
        public double ritmoMedio;
        public double kmMasRapido;
    }

    public Estadisticas obtenerEstadisticasSemana(String uid) {
        Estadisticas stats = new Estadisticas();
        SQLiteDatabase db = getReadableDatabase();

        // Fechas
        long hoy = System.currentTimeMillis();
        long hace7dias = hoy - (7L * 24 * 60 * 60 * 1000);

        Cursor cursor = db.rawQuery(
                "SELECT AVG(distancia), AVG(ritmo), MIN(ritmo) FROM entrenamiento " +
                        "WHERE uid = ? AND fecha BETWEEN ? AND ?",
                new String[]{uid, String.valueOf(hace7dias), String.valueOf(hoy)}
        );

        if (cursor.moveToFirst()) {
            stats.distanciaMedia = cursor.getDouble(0);
            stats.ritmoMedio = cursor.getDouble(1);
            stats.kmMasRapido = cursor.getDouble(2);
        }

        cursor.close();
        return stats;
    }


}
