package com.example.foothelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

public class Set_warning_value extends AppCompatActivity {
    private int returnValue;
    private ImageButton button;
    private SeekBar seekBar;
    private TextView textView;

    @SuppressLint("SetTextI18n")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_warning_value);

        findView();

        int warning_value = getSharedPreferences("record", MODE_PRIVATE)
                .getInt("warning_value", 10);

        seekBar.setMax(20);
        seekBar.setProgress(warning_value);
        textView.setText(Integer.toString(warning_value));


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                SharedPreferences record = getSharedPreferences("record", MODE_PRIVATE);

                returnValue = seekBar.getProgress();

                record.edit()
                        .putInt("warning_value", returnValue)
                        .apply();

                //建立包裹，放入回傳值。
                Bundle argument = new Bundle();
                argument.putInt("returnValueName", returnValue);

                //取出上一個Activity傳過來的 Intent 物件。
                Intent intent = getIntent();
                //放入要回傳的包裹。
                intent.putExtras(argument);

                //設定回傳狀態。
                setResult(Activity.RESULT_OK, intent);
                Toast.makeText(getApplicationContext(),
                        "設定成功",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void findView() {
        button = findViewById(R.id.imageButton_sendWarning);
        seekBar = findViewById(R.id.volumeSeekBar);
        textView = findViewById(R.id.volume);
    }
}
