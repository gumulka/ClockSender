package de.gummu.clockjava;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pflug on 15.11.17.
 */

public class GumReceiver extends BroadcastReceiver {

    private static JobScheduler jobScheduler = null;
    private static JobInfo periodic = null;
    private static ComponentName serviceComponent = null;

    private void startStuff() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootReceiver","Started Receiver");

        if(serviceComponent==null) {
            serviceComponent = new ComponentName(context, ClockService.class);
        }
        if(jobScheduler==null) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        }
        if(intent.getAction().equalsIgnoreCase("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(30 * 1000); // wait at least
            builder.setOverrideDeadline( 4 * 60 * 60 * 1000); // maximum delay of 4 hours
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            // builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or no

            jobScheduler.schedule(builder.build());
        }
        if(periodic==null) {
            JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
            builder.setPeriodic(3 * 60 * 60 * 1000);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or no

            periodic = builder.build();
            Log.i("periodic", ""+periodic.getIntervalMillis());

            jobScheduler.schedule(periodic);
        }


    }

}
