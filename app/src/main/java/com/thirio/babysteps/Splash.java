package com.thirio.babysteps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    SharedPreferences myPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        myPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean reg=myPrefs.getBoolean("registered",false);
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (reg == true) {
                    startActivity(new Intent(Splash.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(Splash.this, PhoneAuthActivity.class));
                    finish();
                }
            }
        }.start();
    }
}
