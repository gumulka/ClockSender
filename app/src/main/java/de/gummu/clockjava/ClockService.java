package de.gummu.clockjava;

import android.app.AlarmManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {

        new Thread(new Runnable() {
            public void run() {
                AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo info = alarmMgr.getNextAlarmClock();
                try {
                    Log.d("Thread", "Sending to socket");
                    Socket socket = new Socket("192.168.0.2", 17055);
//                    Socket socket = new Socket("130.75.16.136", 17055);
                    OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                    Date d = new Date(info.getTriggerTime());
                    final DateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.ENGLISH);
                    iso8601DateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

                    osw.write(iso8601DateFormatter.format(d));
                    osw.close();
                    socket.close();
                    Log.d("Thread", "Finished Sending");
                } catch (IOException e) {
                    Log.w("TCP Exception", "Message: " + e.getMessage());
                    Log.d("TCP StackTrace" , Log.getStackTraceString(e));
                };
                jobFinished( params, true );
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
