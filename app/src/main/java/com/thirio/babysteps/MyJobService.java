package com.thirio.babysteps;

/**
 * Created by abhinav on 10/2/18.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                codeYouWantToRun(params);
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public void codeYouWantToRun(final JobParameters parameters) {
        try {

            Log.d(TAG, "completeJob: " + "jobStarted");
            //This task takes 2 seconds to complete.
            long x = readData();
            Thread.sleep(2000);

            Log.d(TAG, "completeJob: " + "jobFinished " + x);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //Tell the framework that the job has completed and doesnot needs to be reschedule
            jobFinished(parameters, true);
        }
    }

    private long readData() {
        final long[] total = new long[1];
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                total[0] = dataSet.isEmpty()
                                        ? 0
                                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
//                                Log.i(TAG, "Total steps: " + total);

                            }

                        })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                readData();
//                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
        return total[0];
    }

}