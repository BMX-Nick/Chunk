package edu.missouri.chunk;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;

/**
 * Sends data to a URI using HTTP POST.
 *
 * @author Andrew Smith
 */
@SuppressWarnings("deprecation")
class TransmitData extends AsyncTask<byte[], Void, Boolean> {

    // *************************** Format Strings *********************************************************
    private static final String POST_ERROR_MSG                = "POST to %s returned code %s ";
    private static final String CLIENT_PROTOCOL_EXCEPTION_MSG = "Client protocol writing data to %s";
    private static final String IO_ERROR_MSG                  = "I/O error writing data to %s";
    private static final String TAG                           = "TransmitData";

    // ************************** Log Messages ************************************************************
    private static final String LOG_THREAD_ID_MSG = "TransmitJSONData thread id: %s";

    private final URI      uri;
    private final String   uriString;

    /**
     * @param uri  The uri to POST the data to.
     */
    public TransmitData(URI uri) {
        this.uri       = uri;
        this.uriString = uri.toASCIIString();
    }

    @Override
    protected Boolean doInBackground(byte[]... data) {
        boolean status200 = true;

        Log.d(TAG, String.format(LOG_THREAD_ID_MSG, Thread.currentThread().getId()));

        HttpPost request = new HttpPost(uri);

        try {
            request.setEntity(new ByteArrayEntity(data[0]));
            HttpResponse response = new DefaultHttpClient().execute(request);

            int statusCode = response.getStatusLine().getStatusCode();

            switch (statusCode) {
                case HttpStatus.SC_BAD_REQUEST:
                    String results = "BAD REQUEST ENCOUNTERED";
                    status200 = false;
                    Log.e(TAG, results);
                    Log.e(TAG, String.format(POST_ERROR_MSG, uriString, statusCode));
                    break;
                default:
                    break;
            }
        }catch (ClientProtocolException e) {
            Log.e(TAG, String.format(CLIENT_PROTOCOL_EXCEPTION_MSG, uriString));
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e(TAG, String.format(IO_ERROR_MSG, uriString));
            e.printStackTrace();
            return false;
        }

        return status200;
    }
}
