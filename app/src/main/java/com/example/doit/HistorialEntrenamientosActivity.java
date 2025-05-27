package com.example.doit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialEntrenamientosActivity extends AppCompatActivity {

    private LinearLayout layoutEntrenamientos;
    private DoItDBHelper dbHelper;
    private String uid;
    Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_entrenamientos);

        btnVolver = findViewById(R.id.btnVolver);
        layoutEntrenamientos = findViewById(R.id.layoutEntrenamientos);
        dbHelper = new DoItDBHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(this, PesasActivity.class);
            startActivity(intent);
            finish();
        });

        cargarHistorialEntrenamientos();
    }


    private void cargarHistorialEntrenamientos() {
        Cursor cursorEntrenamientos = dbHelper.obtenerEntrenamientosRealizados(uid);
        if (cursorEntrenamientos == null || !cursorEntrenamientos.moveToFirst()) return;

        do {
            int idEntrenamientoRealizado = cursorEntrenamientos.getInt(
                    cursorEntrenamientos.getColumnIndexOrThrow("id_entrenamiento"));
            String nombreRutina = cursorEntrenamientos.getString(cursorEntrenamientos.getColumnIndexOrThrow("nombre"));

            List<Integer> seriesIds = dbHelper.obtenerIdsSeriesPorEntrenamiento(idEntrenamientoRealizado);
            Map<String, Integer> repsPorEjercicio = new HashMap<>();
            Map<String, Float> pesoMaxPorEjercicio = new HashMap<>();

            int totalReps = 0;
            float sumaPesos = 0;
            int totalSeries = 0;

            for (int serieId : seriesIds) {
                Cursor ejercicioCursor = dbHelper.obtenerEjercicioPorSerie(serieId);
                String nombreEjercicio = ejercicioCursor.moveToFirst() ?
                        ejercicioCursor.getString(ejercicioCursor.getColumnIndexOrThrow("nombre")) : "";
                ejercicioCursor.close();

                Cursor repCursor = dbHelper.obtenerRepeticionesPorSerie(serieId);
                while (repCursor.moveToNext()) {
                    int reps = repCursor.getInt(repCursor.getColumnIndexOrThrow("repeticiones"));
                    float peso = repCursor.getFloat(repCursor.getColumnIndexOrThrow("peso_usado"));

                    totalReps += reps;
                    sumaPesos += peso;
                    totalSeries++;

                    repsPorEjercicio.put(nombreEjercicio,
                            repsPorEjercicio.getOrDefault(nombreEjercicio, 0) + reps);
                    pesoMaxPorEjercicio.put(nombreEjercicio,
                            Math.max(pesoMaxPorEjercicio.getOrDefault(nombreEjercicio, 0f), peso));
                }
                repCursor.close();
            }

            float pesoMedio = totalSeries > 0 ? sumaPesos / totalSeries : 0;
            float mediaRepsPorEjercicio = repsPorEjercicio.size() > 0 ?
                    (float) totalReps / repsPorEjercicio.size() : 0;

            String ejercicioMasSeries = "-";
            int maxReps = 0;
            for (Map.Entry<String, Integer> entry : repsPorEjercicio.entrySet()) {
                if (entry.getValue() > maxReps) {
                    maxReps = entry.getValue();
                    ejercicioMasSeries = entry.getKey();
                }
            }

            String ejercicioMasPeso = "-";
            float maxPeso = 0;
            for (Map.Entry<String, Float> entry : pesoMaxPorEjercicio.entrySet()) {
                if (entry.getValue() > maxPeso) {
                    maxPeso = entry.getValue();
                    ejercicioMasPeso = entry.getKey();
                }
            }

            View item = getLayoutInflater().inflate(R.layout.item_historial_entrenamiento, null);

            ((TextView) item.findViewById(R.id.txtNombreRutina)).setText(nombreRutina);
            ((TextView) item.findViewById(R.id.txtPesoMedio)).setText("Peso medio: " + String.format("%.1f", pesoMedio) + " kg");
            ((TextView) item.findViewById(R.id.txtRepsTotales)).setText("Repeticiones totales: " + totalReps);
            ((TextView) item.findViewById(R.id.txtMediaReps)).setText("Media de repeticiones por ejercicio: " + String.format("%.1f", mediaRepsPorEjercicio));
            ((TextView) item.findViewById(R.id.txtEjercicioMasSeries)).setText("Ejercicio con más series: " + ejercicioMasSeries.toLowerCase() + " (" + maxReps + " reps)");
            ((TextView) item.findViewById(R.id.txtEjercicioMasPeso)).setText("Ejercicio con más peso: " + ejercicioMasPeso.toLowerCase() + " (" + maxPeso + " kg)");

            layoutEntrenamientos.addView(item);


        } while (cursorEntrenamientos.moveToNext());

        cursorEntrenamientos.close();
    }


}
