package edu.missouri.chunk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Jay Kelner on 7/2/15.
 */
public class SyncService extends IntentService {

    public static final String TAG = "SyncService";
    public static final String URL = "http://dslsrv8.cs.missouri.edu/~jmkwdf/CrtNIMH/example.php";

    private URI uri;

    public SyncService(){
        super("SyncService");

        try {
            uri = new URI(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started in SyncService");

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
        new TransmitData(uri).execute(new byte[]{});
    }
}
