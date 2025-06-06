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
    private static final int DB_VERSION = 11;

    public DoItDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS usuario (" +
                "uid TEXT PRIMARY KEY, " +
                "nombre TEXT, " +
                "edad INTEGER, " +
                "peso REAL, " +
                "altura REAL, " +
                "sexo TEXT, " +
                "foto_perfil TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS entrenamiento (" +
                "id_entrenamiento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid TEXT, " +
                "nombre TEXT, " +
                "descripcion TEXT, " +
                "tipo TEXT," +
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

        db.execSQL("CREATE TABLE entrenamiento_realizado (" +
                "id_entrenamiento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_rutina INTEGER," +
                "fecha TEXT," +
                "uid_usuario TEXT, " +
                "FOREIGN KEY (id_rutina) REFERENCES rutina(id_entrenamiento))");



        db.execSQL("INSERT INTO ejercicio (nombre, descripcion, imagen_uri) VALUES\n" +
                "('Press de banca', 'Ejercicio de fuerza para pectorales realizado en banco plano.', 'press_banca')," +
                "('Sentadillas', 'Ejercicio compuesto que trabaja piernas y glúteos.', 'sentadillas')," +
                "('Peso muerto', 'Ejercicio de levantamiento que involucra la cadena posterior.', 'peso_muerto')," +
                "('Press militar', 'Ejercicio de hombros que se realiza con barra o mancuernas.', 'press_militar')," +
                "('Remo con barra', 'Ejercicio para la espalda media y alta.', 'remo_barra')," +
                "('Fondos en paralelas', 'Ideal para trabajar tríceps y pecho.', 'fondos_paralelas')," +
                "('Elevaciones laterales', 'Aislamiento de deltoides laterales.', 'elevaciones_laterales')," +
                "('Curl de bíceps', 'Ejercicio básico para fortalecer los bíceps.', 'curl_biceps')," +
                "('Extensión de tríceps', 'Ejercicio para el desarrollo de los tríceps.', 'extension_triceps')," +
                "('Zancadas', 'Ejercicio unilateral para piernas y glúteos.', 'zancadas')," +
                "('Jalón al pecho', 'Ejercicio para dorsales con polea.', 'jalon_pecho')," +
                "('Remo con mancuerna', 'Remo unilateral para espalda.', 'remo_mancuerna')," +
                "('Curl martillo', 'Trabaja bíceps y braquiorradial.', 'curl_martillo')," +
                "('Abdominales crunch', 'Ejercicio tradicional para el abdomen.', 'crunch_abdominal')," +
                "('Plancha abdominal', 'Ejercicio isométrico para el core.', 'plancha_abdominal'),\n" +
                "('Press de piernas', 'Trabajo de cuádriceps y glúteos en máquina.', 'press_piernas')," +
                "('Pantorrillas de pie', 'Elevaciones para fortalecer gemelos.', 'pantorrillas_pie')," +
                "('Hip thrust', 'Enfocado en el glúteo mayor.', 'hip_thrust')," +
                "('Face pulls', 'Trabajo de deltoides posteriores y trapecios con polea.', 'face_pulls')," +
                "('Burpees', 'Ejercicio completo para cardio y fuerza.', 'burpees')"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS serie_ejercicio");
        db.execSQL("DROP TABLE IF EXISTS serie");
        db.execSQL("DROP TABLE IF EXISTS ejercicio");
        db.execSQL("DROP TABLE IF EXISTS entrenamiento");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        db.execSQL("DROP TABLE IF EXISTS entrenamiento_realizado");
        onCreate(db);
    }

    public Cursor obtenerDatosUsuario(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT nombre, edad, peso, altura, sexo, foto_perfil FROM usuario WHERE uid = ?", new String[]{uid});
    }

    public void insertarUsuarioVacio(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        db.insert("usuario", null, values);
        db.close();
    }


    public void actualizarUsuario(String uid, String nombre, int edad, double peso, double altura,String sexo, String foto_perfil) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("edad", edad);
        values.put("peso", peso);
        values.put("altura", altura);
        values.put("sexo", sexo);
        values.put("foto_perfil", foto_perfil);
        db.update("usuario", values, "uid = ?", new String[]{uid});
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

    public void guardarEntrenamiento(String uid, String fecha, String duracion, String tipo, double distanciaKm, double velocidadMedia, double ritmoPromedio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("fecha", fecha);
        values.put("duracion", duracion);
        values.put("distancia", distanciaKm);
        values.put("velocidad", velocidadMedia);
        values.put("tipo", tipo);
        values.put("ritmo", ritmoPromedio);
        db.insert("entrenamiento", null, values);
        db.close();
    }

    public Cursor obtenerRutinaPorId(int idEntrenamiento) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM entrenamiento WHERE id_entrenamiento = ?",
                new String[]{String.valueOf(idEntrenamiento)});
    }

    public long insertarSerie(int idEntrenamiento) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_entrenamiento", idEntrenamiento);
        return db.insert("serie", null, values);
    }

    public void insertarRepeticion(long idSerie, int repeticiones, float peso, float distancia) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("repeticiones", repeticiones);
        values.put("peso_usado", peso);
        values.put("distancia_recorrida", distancia);
        db.insert("repeticion", null, values);
    }

    public void insertarSerieEjercicio(long idSerie, long idEjercicio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("id_ejercicio", idEjercicio);
        db.insert("serie_ejercicio", null, values);
    }

    public void insertarEjercicio(String nombre, String descripcion, String imagenUri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("imagen_uri", imagenUri);
        db.insert("ejercicio", null, values);
    }

    public Cursor obtenerTodosLosEjercicios() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM ejercicio", null);
    }

    public Cursor obtenerEjerciciosDeRutina(int idEntrenamiento) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT e.id_ejercicio, e.nombre, e.descripcion, e.imagen_uri " +
                        "FROM ejercicio e " +
                        "JOIN serie_ejercicio se ON e.id_ejercicio = se.id_ejercicio " +
                        "JOIN serie s ON s.id_serie = se.id_serie " +
                        "WHERE s.id_entrenamiento = ?",
                new String[]{String.valueOf(idEntrenamiento)}
        );
    }

    public Cursor obtenerRutinasPorUsuarioDisplay(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DISTINCT e.id_entrenamiento, e.nombre, e.descripcion " +
                        "FROM entrenamiento e " +
                        "WHERE e.tipo= 'pesas' AND e.uid = ? ",
                new String[]{uid}
        );
    }

    public Cursor obtenerEntrenamientoAleatorio(String uidUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id_entrenamiento FROM entrenamiento WHERE tipo = 'pesas' AND uid = ? ORDER BY RANDOM() LIMIT 1";
        return db.rawQuery(query, new String[]{uidUsuario});
    }

    public Cursor obtenerEntrenamientosRealizados(String uidUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT er.id_entrenamiento, e.nombre, e.descripcion " +
                "FROM entrenamiento_realizado er " +
                "JOIN entrenamiento e ON er.id_rutina = e.id_entrenamiento " +
                "WHERE er.uid_usuario = ? " +
                "ORDER BY er.fecha DESC";
        return db.rawQuery(query, new String[]{uidUsuario});
    }

    public int contarDiasEjercicio(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT fecha) FROM entrenamiento_realizado WHERE uid_usuario = ?";
        Cursor cursor = db.rawQuery(query, new String[]{uid});
        int dias = 0;
        if (cursor.moveToFirst()) dias = cursor.getInt(0);
        cursor.close();
        return dias;
    }

    public String obtenerFechaMasAntigua(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MIN(fecha) FROM entrenamiento_realizado WHERE uid_usuario = ?";
        Cursor cursor = db.rawQuery(query, new String[]{uid});
        String fecha = null;
        if (cursor.moveToFirst()) fecha = cursor.getString(0);
        cursor.close();
        return fecha;
    }

    public List<Integer> obtenerIdsSeriesPorEntrenamiento(int idEntrenamiento) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> ids = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT id_serie FROM serie WHERE id_entrenamiento = ?",
                new String[]{String.valueOf(idEntrenamiento)}
        );

        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow("id_serie")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ids;
    }

    public Cursor obtenerEjercicioPorSerie(int idSerie) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT e.nombre FROM ejercicio e " +
                        "JOIN serie_ejercicio se ON e.id_ejercicio = se.id_ejercicio " +
                        "WHERE se.id_serie = ? LIMIT 1",
                new String[]{String.valueOf(idSerie)}
        );
    }

    public Cursor obtenerRepeticionesPorSerie(int idSerie) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT repeticiones, peso_usado FROM repeticion WHERE id_serie = ?",
                new String[]{String.valueOf(idSerie)}
        );
    }

    public List<String> obtenerNombresEjerciciosPorIds(List<Integer> ids) {
        List<String> nombres = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return nombres;

        SQLiteDatabase db = getReadableDatabase();
        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i > 0 ? ",?" : "?");
            args[i] = String.valueOf(ids.get(i));
        }

        String query = "SELECT nombre FROM ejercicio WHERE id_ejercicio IN (" + placeholders + ")";
        Cursor cursor = db.rawQuery(query, args);

        while (cursor.moveToNext()) {
            nombres.add(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        }
        cursor.close();
        return nombres;
    }

    public List<String> obtenerImagenesPorIds(List<Integer> ids) {
        List<String> imagenes = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return imagenes;

        SQLiteDatabase db = getReadableDatabase();
        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            placeholders.append(i > 0 ? ",?" : "?");
            args[i] = String.valueOf(ids.get(i));
        }

        Cursor cursor = db.rawQuery(
                "SELECT imagen_uri FROM ejercicio WHERE id_ejercicio IN (" + placeholders + ")", args
        );

        while (cursor.moveToNext()) {
            imagenes.add(cursor.getString(0));
        }

        cursor.close();
        return imagenes;
    }

    public int insertarSerieParaEntrenamiento(int idEntrenamiento) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_entrenamiento", idEntrenamiento);
        long id = db.insert("serie", null, values);
        return (int) id;
    }

    public long insertarEntrenamientoRealizado(int id_rutina, String uidUsuario, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_rutina", id_rutina);
        values.put("uid_usuario", uidUsuario);
        values.put("fecha", fecha);
        return db.insert("entrenamiento_realizado", null, values);
    }

    public void insertarEjercicioEnSerie(int idSerie, int idEjercicio) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_serie", idSerie);
        values.put("id_ejercicio", idEjercicio);
        db.insert("serie_ejercicio", null, values);
    }

    public long insertarRutinaPersonalizada(String uid, String nombre, String descripcion, String tipo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("nombre", nombre);
        values.put("fecha", "");
        values.put("descripcion", descripcion);
        values.put("tipo", tipo);
        values.put("duracion", " ");
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

}
