package de.gummu.clockjava;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mJobScheduler;
    private Button mScheduleJobButton;
    private TextView blub;

    private void showInfo(String info) {
//        blub.setText(info);
        Toast.makeText( getApplicationContext(),
                info, Toast.LENGTH_SHORT )
                .show();
        Log.i("showInfo",info);
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        mJobScheduler = (JobScheduler) getSystemService( Context.JOB_SCHEDULER_SERVICE );
        mScheduleJobButton = (Button) findViewById( R.id.button );
        blub = (TextView) findViewById( R.id.textView );

        mScheduleJobButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName serviceComponent = new ComponentName(v.getContext(), ClockService.class);
                JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
  //              builder.setMinimumLatency(30 * 100); // wait at least
                builder.setOverrideDeadline(60 * 100); // maximum delay

                JobInfo ji = builder.build();
                showInfo("Sending Information");

                mJobScheduler.schedule(ji); // */
            }
        });
    }
}
