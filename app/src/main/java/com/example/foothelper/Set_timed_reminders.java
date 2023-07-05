package com.example.foothelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.TimeZone;

public class Set_timed_reminders extends AppCompatActivity {
    private ImageButton button;
    private TimePicker timePicker;
    private CheckBox checkBox;
    private long currentSystemTime;
    private Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_timed_reminders);

        findView();

        boolean getState = getSharedPreferences("record", MODE_PRIVATE)
                .getBoolean("狀態紀錄", false);
        int HOUR = getSharedPreferences("record", MODE_PRIVATE)
                .getInt("HOUR", 23);
        int MINUTE = getSharedPreferences("record", MODE_PRIVATE)
                .getInt("MINUTE", 59);

        checkBox.setChecked(getState);
        timePicker.setHour(HOUR);
        timePicker.setMinute(MINUTE);


        timePicker.setIs24HourView(true);
        button.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                btnNotify(v);
                checkBox.setChecked(false);
            }
        });

        checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences record = getSharedPreferences("record", MODE_PRIVATE);
                //判斷CheckBox是否有勾選
                if(isChecked)
                {
                    //CheckBox狀態 : 已勾選 關閉提醒
                    stopRemind();
                    record.edit()
                            .putBoolean("狀態紀錄", true)
                            .apply();
                }
                else
                {
                    //CheckBox狀態 : 未勾選 開啟提醒
                    openRemind();
                    record.edit()
                            .putBoolean("狀態紀錄", false)
                            .apply();
                }
            }
        });
    }

    private void findView() {
        button = findViewById(R.id.imageButton_Setting_Finish);
        timePicker = findViewById(R.id.timepicker);
        checkBox = findViewById(R.id.checkBox);
    }

    private void currentTime() {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        currentSystemTime=System.currentTimeMillis();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setTime(Calendar calendar) {
        SharedPreferences record = getSharedPreferences("record", MODE_PRIVATE);
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        record.edit()
                .putInt("HOUR",timePicker.getHour())
                .putInt("MINUTE",timePicker.getMinute())
                .apply();

        long settime = calendar.getTimeInMillis();

        if (currentSystemTime > settime) {
            calendar.add(Calendar.MONTH, 1);
            settime = calendar.getTimeInMillis();
        }
    }

    private void setAlarm() {
        Intent intent = new Intent(Set_timed_reminders.this, Timed_reminders_Ringing.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Set_timed_reminders.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (1000*60*60*24), pendingIntent);
    }

    private void stopRemind(){
        Intent intent = new Intent(Set_timed_reminders.this, Timed_reminders_Ringing.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Set_timed_reminders.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //取消警报
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "關閉提醒", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openRemind() {
        currentTime();
        setTime(calendar);
        setAlarm();
        Toast.makeText(this,"開啟提醒",Toast.LENGTH_LONG).show();
    }

    private void showtime() {

        String text =(calendar.get(Calendar.MONTH)+1)+"月"
                +calendar.get(Calendar.DAY_OF_MONTH)+"日\n"
                +calendar.get(Calendar.HOUR_OF_DAY)+":"
                +calendar.get(Calendar.MINUTE);

        Toast.makeText(this,"設定成功\n" + "設定時間為\n"+text,Toast.LENGTH_LONG).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnNotify(View view) {
        currentTime();
        setTime(calendar);
        setAlarm();
        showtime();
        finish();
    }
}
