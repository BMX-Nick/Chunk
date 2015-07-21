package edu.missouri.chunk;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.URI;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * @author Jay Kelner
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    private URI uri;

    private byte[] bytes;

    private long interval;

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

        bytes = (byte[]) extras.get("bytes");

        interval =  extras.getLong("interval");

        // Create a connectivity manager to monitor our connection status.
        Context             context       = getApplicationContext();
        ConnectivityManager cm            = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo         activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // If the phone isConnected to the internet, perform the sync.
        if (isConnected) {
            performSync();
        } else {
            Log.e(TAG, "sync failed in onHandleIntent: no connectivity was detected.");
        }

        if(MainActivity.counter <= 10) {
            Log.d(TAG, "Schedule the next service");
            AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent mIntent = new Intent(getApplicationContext(), SyncService.class);
            mIntent.putExtra("uri", uri);
            mIntent.putExtra("bytes", bytes);
            mIntent.putExtra("interval", interval);
            Log.d("DataTransmitter", String.format("About to begin syncing in %d milliseconds, hopefully.", interval));

            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, interval, pendingIntent);
            Log.d(TAG, String.format("Counter is %d", MainActivity.counter));
        } else {
            Log.d(TAG, "Counter reached maximum, stopping");
        }
    }

    /**
     * Performs the synchronization with the server using TransmitJSONData
     */
    private void performSync() {


        try {
            boolean result = new TransmitData(uri).execute(bytes).get();
            if(result)
                MainActivity.counter++;


            Toast.makeText(getApplicationContext(), String.valueOf(MainActivity.counter), Toast.LENGTH_SHORT).show();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
