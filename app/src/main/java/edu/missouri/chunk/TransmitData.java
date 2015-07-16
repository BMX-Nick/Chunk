package edu.missouri.chunk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends data to a URI using HTTP POST.
 *
 * @author Andrew Smith
 */
public class TransmitData extends AsyncTask<byte[], Void, Boolean> {

    // *************************** Format Strings *********************************************************
    private static final String POST_ERROR_MSG                = "POST to %s returned code %s ";
    private static final String UNSUPPORTED_ENCODING_MSG      = "Unable to encode parameter %s";
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
        String  message   = new String(data[0]);
        boolean status200 = false;

        Log.d(TAG, String.format(LOG_THREAD_ID_MSG, Thread.currentThread().getId()));

        HttpPost request = new HttpPost(uri);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data", message));

        try {
            request.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response       = new DefaultHttpClient().execute(request);
            HttpEntity   entity         = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();

            switch (statusCode) {
                case HttpStatus.SC_BAD_REQUEST:
                    String results = "BAD REQUEST ENCOUNTERED";
                    status200 = true;
                    Log.d(TAG, results);
                    break;
                default:
                    Log.w(TAG, String.format(POST_ERROR_MSG, uriString, statusCode));
                    return false;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, String.format(UNSUPPORTED_ENCODING_MSG, params.get(0)));
            e.printStackTrace();
            return false;
        } catch (ClientProtocolException e) {
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
