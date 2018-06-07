package com.thirio.babysteps;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhinav on 10/2/18.
 */

public class ServiceSync extends Service {
    private MediaPlayer mediaPlayer;
    int val=0;
    SharedPreferences myPrefs;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        Toast.makeText(this, "Sync in progress", Toast.LENGTH_SHORT).show();
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                Log.d("READ DATA","SUCC"+dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt());
                                val = dataSet.isEmpty()
                                        ? dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt():0;
                                RequestQueue queue;
                                queue = Volley.newRequestQueue(getApplicationContext());
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("mobile",myPrefs.getString("mobile","1234567890"));
                                params.put("steps",dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()+"");
                                params.put("timestamp", System.currentTimeMillis()+"");
                                JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST,
                                        "URL_JSON", new JSONObject(params), new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {



                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {


                                    }
                                });
                                // Adding request to request queue
                                queue.add(strReq);





                            }

                        })

                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d("READ DATA","FAIL");

//                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
        //Start media player
//        mediaPlayer = MediaPlayer.create(this, );
//        mediaPlayer.start();
//        mediaPlayer.setLooping(true);//set looping true to run it infinitely
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //On destory stop and release the media player
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//            mediaPlayer.stop();
//            mediaPlayer.reset();
//            mediaPlayer.release();
        }
    }

}