package de.gummu.clockjava;

import android.app.AlarmManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ClockService extends JobService {

    private static String TAG = ClockService.class.getCanonicalName();
    private static final String DEFAULT_BROKER = "tcp://192.168.0.2:1883";
    private static final String DEFAULT_CLIENT_ID = "AndroidClock";
    private static final String DEFAULT_TOPIC = "/openhab/handy/test";
    public static final String MQTT_BROKER_URL = "mqttbroker";
    public static final String MQTT_CLIENT_ID = "mqttclient";
    public static final String MQTT_TOPIC = "mqtttopic";
    private MqttAndroidClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private IMqttToken connectionToken;


    @Override
    public boolean onStartJob(final JobParameters params) {

        final ClockService context = this;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String broker   = sharedPref.getString(MQTT_BROKER_URL, DEFAULT_BROKER);
        //TODO Split this one into three settings. Protocol, Hostname and Port
        Log.d(TAG, "Broker: " + broker);
        String clientid = sharedPref.getString(MQTT_CLIENT_ID, DEFAULT_CLIENT_ID);
        Log.d(TAG, "ClientID: " + clientid);

        mqttClient = new MqttAndroidClient(context, broker, clientid , new MemoryPersistence());
        connectionOptions = new MqttConnectOptions();
        mqttClient.registerResources(context);
        new Thread(new Runnable() {
            public void run() {
                AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo info = alarmMgr.getNextAlarmClock();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String topic = sharedPref.getString(MQTT_TOPIC, DEFAULT_TOPIC);
                Log.d(TAG, "Topic: " + topic);
                if(! mqttClient.isConnected() ) {
                    try {
                        //TODO This throws a leaked ServiceConnection Exception.
                        connect();
                    }catch (Exception e) {
                        Log.w(TAG, e);
                        return;
                    }
                }

                if(info!=null) {
                    Date d = new Date(info.getTriggerTime());
                    final DateFormat iso8601DateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                    iso8601DateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                    doSend(topic, iso8601DateFormatter.format(d));
                } else {
                    doSend(topic, "UNDEFINED");
                }
                Log.d(TAG, "Finished Sending");

                jobFinished( params, false );
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Stopping Job");
        return false;
    }


    private void connect() throws MqttException {
        if( mqttClient.isConnected()) return;
        if(connectionToken != null) {
            if(connectionToken.isComplete()) {
                connectionToken = null;
                return;
            }
        } else {
            connectionToken = mqttClient.connect(connectionOptions);
        }
        if(connectionToken != null)
            connectionToken.waitForCompletion(5000);
    }


    private void doSend(String topic, String msg) {
        Log.i(TAG, "Sending message " + msg + " to topic " + topic);
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos( 1 );
        try {
            mqttClient.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        try {
            disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        mqttClient.unregisterResources();
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}
