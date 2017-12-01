package de.gummu.clockjava;

import android.app.AlarmManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

    public static final String KEY_PREF_HOST_NAME = "hostname";
    public static final String KEY_PREF_HOST_PORT = "hostport";

    @Override
    public boolean onStartJob(final JobParameters params) {

        final ClockService cs = this;

        new Thread(new Runnable() {
            public void run() {
                AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo info = alarmMgr.getNextAlarmClock();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(cs);
                String hostname = sharedPref.getString(KEY_PREF_HOST_NAME, "\"192.168.0.4\"");
                Log.d("Sender", "Host is " + hostname);
                String port = sharedPref.getString(KEY_PREF_HOST_PORT, "17055");
                int hostport = Integer.parseInt(port);
                Log.d("Sender", "Port is " + port);

                try {
                    Log.d("Thread", "Sending to socket");
                    Socket socket = new Socket(hostname, hostport);
//                    Socket socket = new Socket("130.75.16.136", hostport);
                    OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                    Date d = new Date(info.getTriggerTime());
                    final DateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    iso8601DateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

                    osw.write(iso8601DateFormatter.format(d));
                    osw.close();
                    socket.close();
                    Log.d("Thread", "Finished Sending");
                } catch (IOException e) {
                    Log.w("TCP Exception", "Message: " + e.getMessage());
                    Log.d("TCP StackTrace" , Log.getStackTraceString(e));
                };
                jobFinished( params, false );
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
