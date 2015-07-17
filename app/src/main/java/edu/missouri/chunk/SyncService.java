package edu.missouri.chunk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Jay Kelner on 7/2/15.
 */
public class SyncService extends IntentService {

    public static final String TAG = "SyncService";

    private URI uri;

    byte[] bytes;

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

        int size = extras.getInt("size");

        final int totalBytes = size * 1024;

        bytes = new byte[totalBytes];

        for(int i = 0; i < totalBytes; i++) {
            bytes[i] = (byte) (i % Byte.MAX_VALUE);
        }

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
