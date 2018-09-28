package de.gummu.clockjava;

import android.app.AlarmManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockService extends JobService {

    public static final String MQTT_BROKER_URL = "mqttbroker";
    public static final String MQTT_CLIENT_ID = "mqttclient";
    public static final String MQTT_TOPIC = "mqtttopic";


    private MqttAndroidClient client;


    @Override
    public boolean onStartJob(final JobParameters params) {
        final ClockService cs = this;
        final MqqClient pahoMqttClient = new MqqClient();

        new Thread(new Runnable() {
            public void run() {
                AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo info = alarmMgr.getNextAlarmClock();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(cs);
                String broker   = sharedPref.getString(MQTT_BROKER_URL, "\"192.168.0.4\"");
                Log.d("Sender", "Broker: " + broker);
                String clientid = sharedPref.getString(MQTT_CLIENT_ID, "\"AndroidClock\"");
                Log.d("Sender", "ClientID: " + clientid);
                client = pahoMqttClient.getMqttClient(getApplicationContext(), broker, clientid);
                String topic = sharedPref.getString(MQTT_TOPIC, "\"/openhab/phone/test\"");
                Log.d("Sender", "Topic: " + topic);

                    if(info!=null) {
                        Date d = new Date(info.getTriggerTime());
                        final DateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                        iso8601DateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                        pahoMqttClient.publishMessage(client, iso8601DateFormatter.format(d), 1, topic);
                    } else {
                        pahoMqttClient.publishMessage(client, "UNDEFINED", 1, topic);
                    }
                    Log.d("Thread", "Finished Sending");

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
