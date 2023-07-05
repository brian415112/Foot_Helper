package com.example.foothelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class View_historical_reports extends AppCompatActivity {
    Spinner spinner;
    ImageButton imageButton;
    ArrayList<MeasureValue> measureValues = new ArrayList<>();
    String[] measure_timestamp;
    int position;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_historical_reports);

        spinner = findViewById(R.id.spinner);
        imageButton = findViewById(R.id.search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("/users/Brian/Measure")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        measureValues.add(document.toObject(MeasureValue.class));
                    }

                    Collections.reverse(measureValues);
                    Log.e("test2",String.valueOf(measureValues.size()));

                    measure_timestamp = new String[measureValues.size()];

                    Log.e("test",String.valueOf(measureValues.size()));
                    for(int i = 0; i < measureValues.size(); i++){
                        Log.e("test",String.valueOf(i));
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
                        Log.e("Test", sdf.format(measureValues.get(i).getTimestamp().toDate()));
                        measure_timestamp[i] = sdf.format(measureValues.get(i).getTimestamp().toDate());
                        Log.e("Test", measure_timestamp[i]);
                    }

                    ArrayAdapter<String> measure_timestamp_List = new ArrayAdapter<>(View_historical_reports.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            measure_timestamp);
                    spinner.setAdapter(measure_timestamp_List);
                });


//        db.collection("/users/Brian/Measure")
//                .orderBy("timestamp")
//                .addSnapshotListener((value, e) -> {
//                    if (e != null) {
//                        Log.e("Measure document  Error", "Listen failed.", e);
//                        return;
//                    }
//
//                    assert value != null;
//                    for (QueryDocumentSnapshot doc : value) {
//                        measureValues.add(doc.toObject(MeasureValue.class));
//                    }
//
//                    Collections.reverse(measureValues);
//                    Log.e("test2",String.valueOf(measureValues.size()));
//                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
                position = p;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        imageButton.setOnClickListener(v -> {
            MeasureValue tempMeasureValue = measureValues.get(position);

            StringBuilder csvText = new StringBuilder();
            for (int i = 0; i < tempMeasureValue.getForefoot_pressure().size(); i++) {
                csvText.append(tempMeasureValue.getForefoot_pressure().get(i)).append(",");
            }

            csvText.append("\n");

            for (int i = 0; i < tempMeasureValue.getMidfoot_pressure().size(); i++) {
                csvText.append(tempMeasureValue.getMidfoot_pressure().get(i)).append(",");
            }

            csvText.append("\n");

            for (int i = 0; i < tempMeasureValue.getHindfoot_pressure().size(); i++) {
                csvText.append(tempMeasureValue.getHindfoot_pressure().get(i)).append(",");
            }

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss");
            String temp_measure_timestamp = sdf.format(tempMeasureValue.getTimestamp().toDate());

            runOnUiThread(() -> {
                try {
                    FileOutputStream out = openFileOutput(temp_measure_timestamp , Context.MODE_PRIVATE);
                    out.write((csvText.toString().getBytes()));
                    out.close();
                    File fileLocation = new File(Environment.
                            getExternalStorageDirectory().getAbsolutePath()+"/"+"Download"+"/"+ temp_measure_timestamp+".csv");
                    FileOutputStream fos = new FileOutputStream(fileLocation);
                    fos.write(csvText.toString().getBytes());
                    Uri path = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", fileLocation);
                    Intent fileIntent = new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/txt");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, temp_measure_timestamp);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "輸出檔案"));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("TAG", "makeCSV: "+e.toString());
                }
            });



//            db.collection("/users/Brian/Measure")
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
//                                Log.d("TAG", document.getId() + " => " + document.getData());
//
//                                new Thread(()->{
//                                    MeasureValue measureValue = document.toObject(MeasureValue.class);
//                                    StringBuilder csvText = new StringBuilder();
//                                    for (int i = 0; i < measureValue.getForefoot_pressure().size(); i++) {
//                                        csvText.append(measureValue.getForefoot_pressure().get(i)).append(",");
//                                    }
//
//                                    csvText.append("\n");
//
//                                    for (int i = 0; i < measureValue.getMidfoot_pressure().size(); i++) {
//                                        csvText.append(measureValue.getMidfoot_pressure().get(i)).append(",");
//                                    }
//
//                                    csvText.append("\n");
//
//                                    for (int i = 0; i < measureValue.getHindfoot_pressure().size(); i++) {
//                                        csvText.append(measureValue.getHindfoot_pressure().get(i)).append(",");
//                                    }
//
//                                    runOnUiThread(() -> {
//                                        try {
//                                            FileOutputStream out = openFileOutput(document.getId(), Context.MODE_PRIVATE);
//                                            out.write((csvText.toString().getBytes()));
//                                            out.close();
//                                            File fileLocation = new File(Environment.
//                                                    getExternalStorageDirectory().getAbsolutePath(), document.getId());
//                                            FileOutputStream fos = new FileOutputStream(fileLocation);
//                                            fos.write(csvText.toString().getBytes());
//                                            Uri path = Uri.fromFile(fileLocation);
//                                            Intent fileIntent = new Intent(Intent.ACTION_SEND);
//                                            fileIntent.setType("text/csv");
//                                            fileIntent.putExtra(Intent.EXTRA_SUBJECT, document.getId());
//                                            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
//                                            startActivity(Intent.createChooser(fileIntent, "輸出檔案"));
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                            Log.w("TAG", "makeCSV: "+e.toString());
//                                        }
//                                    });
//                                }).start();
//                            }
//                        } else {
//                            Log.d("TAG", "Error getting documents: ", task.getException());
//                        }
//                    });
        });
    }
}