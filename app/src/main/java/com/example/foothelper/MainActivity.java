package com.example.foothelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

public class MainActivity extends AppCompatActivity {
    private ImageButton button1,button2,button3,button4;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findView();

        button1.setOnClickListener(v -> {
            Intent intent1 = new Intent();
            intent1.setClass(MainActivity.this, Foot_pressure_detection.class);
            startActivity(intent1);
        });

        button2.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, Set_timed_reminders.class);
            startActivity(intent);
        });

        button3.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, View_historical_reports.class);
            startActivity(intent);
        });

    }

    private void findView() {
        button1 = findViewById(R.id.imageButton_foot_detect);
        button2 = findViewById(R.id.imageButton_set_timed_reminders);
        button3 = findViewById(R.id.imageButton_view_historical);
    }
}
