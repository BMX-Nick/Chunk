package edu.missouri.chunk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.net.URI;

/**
 * @author Jay Kelner
 */
public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    private URI uri;

    private byte[] bytes;

    public SyncService(){
        super("SyncService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started in SyncService");

        Bundle extras = intent.getExtras();

        uri  = (URI) extras.get("uri");

        bytes = (byte[]) extras.get("bytes");

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
    }

    /**
     * Performs the synchronization with the server using TransmitJSONData
     */
    private void performSync() {

        new TransmitData(uri).execute(bytes);
    }
}
