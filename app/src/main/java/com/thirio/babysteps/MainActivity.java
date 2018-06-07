package com.thirio.babysteps;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.thirio.babysteps.common.logger.Log;
import com.thirio.babysteps.common.logger.LogView;
import com.thirio.babysteps.common.logger.LogWrapper;
import com.thirio.babysteps.common.logger.MessageOnlyLogFilter;

import java.util.Calendar;

import static android.view.View.GONE;

/**
 * This sample demonstrates combining the Recording API and History API of the Google Fit platform
 * to record steps, and display the daily current step count. It also demonstrates how to
 * authenticate a user with Google Play Services.
 */
public class MainActivity extends AppCompatActivity {
    private PendingIntent pendingIntent;
    CardView steps_button;
    Button button;
    TextView stepsText,steps;
    ProgressBar progressBar;
    //Alarm Request Code
    private static final int ALARM_REQUEST_CODE = 133;
    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();
        button=findViewById(R.id.card_view_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readData();
            }
        });
        progressBar=findViewById(R.id.progressBar);
        stepsText=findViewById(R.id.stepsText);
        steps=findViewById(R.id.steps);
        stepsText.setText("Sigin to proceed");
        steps_button=findViewById(R.id.card_view_steps);
        steps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FitnessOptions fitnessOptions =
                        FitnessOptions.builder()
                                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .build();
                if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions)) {
                    GoogleSignIn.requestPermissions(
                            MainActivity.this,
                            REQUEST_OAUTH_REQUEST_CODE,
                            GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                            fitnessOptions);

                } else {
                    subscribe();

                }
            }
        });
        steps.setVisibility(GONE);
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getApplicationContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                    fitnessOptions);

        } else {
            subscribe();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
                triggerAlarmManager(8);
                readData();
            }
        }
    }

    /** Records step data by requesting a subscription to background step data. */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                    triggerAlarmManager(8);
                                    readData();
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void readData() {
        progressBar.setVisibility(View.VISIBLE);
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
                                stepsText.setText("Steps Today: ");
                                steps.setVisibility(View.VISIBLE);
                                steps.setText(total+"");
                                steps_button.setOnClickListener(null);

                                progressBar.setVisibility(GONE);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);

                                progressBar.setVisibility(GONE);
                            }
                        });
        triggerAlarmManager(8);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_update_data) {
//            Toast.makeText(getApplicationContext(),"menu option clicked",Toast.LENGTH_SHORT).show();
            readData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Initializes a custom log class that outputs both to in-app targets and logcat. */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
        LogView logView = (LogView) findViewById(R.id.sample_logview);

        // Fixing this lint error adds logic without benefit.
        // noinspection AndroidLintDeprecation
        logView.setTextAppearance(R.style.Log);

        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
    }
    //Trigger alarm manager with entered time interval
    public void triggerAlarmManager(int alarmTriggerTime) {
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add alarmTriggerTime seconds to the calendar object
        cal.add(Calendar.SECOND, alarmTriggerTime);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
//        manager.setRe(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);//set alarm manager with entered timer by converting into milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 30);
        manager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY,pendingIntent);
//        Toast.makeText(this, "Alarm Set for " + alarmTriggerTime + " seconds.", Toast.LENGTH_SHORT).show();
    }
}