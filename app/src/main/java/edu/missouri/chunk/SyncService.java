package edu.missouri.chunk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.net.URI;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * @author Jay Kelner
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";
    private static final int PACKET_COUNT = 10;

    private URI uri;

    public SyncService(){
        super("SyncService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "SyncService creating.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "SyncService destroying.");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started in SyncService");

        Bundle extras = intent.getExtras();

        uri  = (URI) extras.get("uri");

        long interval = extras.getLong("interval");

        // Create a connectivity manager to monitor our connection status.
        Context             context       = getApplicationContext();
        ConnectivityManager cm            = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo         activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(MainActivity.counter < PACKET_COUNT) {

            // If the phone isConnected to the internet, perform the sync.
            if (isConnected) {
                performSync();
            } else {
                Log.e(TAG, "sync failed in onHandleIntent: no connectivity was detected.");
            }

            if(MainActivity.counter != PACKET_COUNT) {
                Log.d(TAG, "Schedule the next service");
                AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent mIntent = new Intent(getApplicationContext(), SyncService.class);
                mIntent.putExtra("uri", uri);
                mIntent.putExtra("interval", interval);
                Log.d("DataTransmitter", String.format("About to begin syncing in %d milliseconds, hopefully.", interval));

                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                final long timeOfNextSync = Calendar.getInstance().getTimeInMillis() + interval;
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, timeOfNextSync, pendingIntent);
                Log.d(TAG, String.format("Counter is %d", MainActivity.counter));
            }else {
                Log.d(TAG, String.format("Counter reached maximum: %d, stopping", PACKET_COUNT));
                MainActivity.performingSync = false;
            }
        }
    }

    /**
     * Performs the synchronization with the server using TransmitJSONData
     */
    private void performSync() {
        try {
            boolean result = new TransmitData(uri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DataTransmitter.bytes).get();
            if(result)
                MainActivity.counter++;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
