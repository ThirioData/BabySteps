package com.thirio.babysteps;
/**
 * Created by abhinav on 10/2/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thirio.babysteps.common.logger.Log;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "ALARM!! ALARM!!", Toast.LENGTH_SHORT).show();
        Log.d("ALARM","yo");
        //Stop sound service to play sound for alarm
        context.startService(new Intent(context, ServiceSync.class));

        //This will send a notification message and show notification in notification tray
//        ComponentName comp = new ComponentName(context.getPackageName(),
//                AlarmNotificationService.class.getName());
//        startWakefulService(context, (intent.setComponent(comp)));

    }


}