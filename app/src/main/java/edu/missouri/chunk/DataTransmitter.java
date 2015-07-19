package edu.missouri.chunk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Sends data of a specified size to a URI at a regular interval.
 *
 * @author Andrew Smith
 */
class DataTransmitter {
    private final Context context;

    /**
     * Represents the states of the transmitter
     */
    private enum State {
        STARTED,
        STOPPED
    }

    private State state = State.STOPPED;
    private URI uri;
    private int size;
    private int freq;

    /**
     * Constructor
     */
    public DataTransmitter(Context context){
        this.context = context;
        this.state = State.STOPPED;
    }

    /**
     *  Starts sending data.
     *
     * @throws IllegalStateException Thrown if the transmitter is already running or the uri is null
     */
   public void start() {
       if(isRunning()) {
           throw new IllegalStateException("DataTransmitter is already started");
       }

       if(uri == null) {
           throw new IllegalStateException("URI cannot be null");
       } else if(size == 0) {
           throw new IllegalStateException("Size cannot be null");
       } else if(freq == 0) {
           throw new IllegalStateException("Frequency cannot be null");
       }

       state = State.STARTED;

       enableAlarm();
   }

    private Context getApplicationContext() {
        return context;
    }
// ************* TODO: Implement

    private void enableAlarm() {
        long millisecondsTilFirstTrigger = 0;
        long intervalToNextAlarm         =  getFreq() * 1000;

        Log.d("DataTransmitter", "Preparing to initiate Alarm Manager. Should start syncing in "
                + Long.toString(millisecondsTilFirstTrigger)
                + " milliseconds, and once every "
                + Long.toString(intervalToNextAlarm)
                + " milliseconds thereafter.");


        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(getApplicationContext(), SyncService.class);
        mIntent.putExtra("uri", uri);

        InputStream is;

        Resources resources = context.getResources();

        switch (size) {
            case 1024:
                is = resources.openRawResource(R.raw.onemb);
                break;
            case 10240:
                is = resources.openRawResource(R.raw.tenmb);
                break;
            case 100:
            default:
                is = resources.openRawResource(R.raw.hundredkb);
                break;
        }

        mIntent.putExtra("bytes", toByteArray(is));

        Log.d("DataTransmitter", String.format("About to begin syncing in %d milliseconds, hopefully.", intervalToNextAlarm));
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                millisecondsTilFirstTrigger,
                intervalToNextAlarm, pendingIntent);
    }

    /**
     * Converts an InputStream to a byte array
     * @param is the InputStream to be converted
     * @return the resulting byte array
     */
    private byte[] toByteArray(InputStream is) {
        byte[] bytes = null;
        DataInputStream dataIs;

        try {
            bytes = new byte[is.available()];
            dataIs = new DataInputStream(is);
            dataIs.readFully(bytes);
        } catch (IOException ioe) {
            Log.e("DataTransmitter", String.format("InputStream was already closed in toByteArray %s", ioe.toString()));
        }

        return bytes;
    }


    private void disableAlarm() {
        throw new UnsupportedOperationException("Not implemented");
    }
// ************

    /**
     * Stops sending data.
     *
     * @throws IllegalStateException Thrown if the transmitter is already stopped
     */
    @SuppressWarnings("unused")
    public void stop() {
        if(!isRunning()) {
            throw new IllegalStateException("DataTransmitter is already stopped");
        }

        state = State.STOPPED;

        disableAlarm();
    }

    /**
     *
     * @return The URI that the data is being sent to
     */
    @SuppressWarnings("unused")
    public URI getUri() {
        return uri;
    }

    /**
     *
     * @param  uri                   The URI to send data to
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setUri(URI uri) {
       verifyNotRunning();

        this.uri = uri;
    }

    /**
     *
     * @return The size of the data that is sent to the URI at each interval
     */
    @SuppressWarnings("unused")
    public int getSize() {
        return size;
    }

    /**
     *
     * @param  size                  The size of the data to send to the URI at each interval
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setSize(int size) {
        verifyNotRunning();

        this.size = size;
    }

    /**
     *
     * @return The frequency at which data will be sent
     */
    @SuppressWarnings("WeakerAccess")
    public int getFreq() {
        return freq;
    }

    /**
     *
     * @param  freq                  The frequency at which to send data
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setFreq(int freq) {
        verifyNotRunning();

        this.freq = freq;
    }

    /**
     *
     * @return Returns true if the transmitter is running
     */
    public boolean isRunning() {
        return state == State.STARTED;
    }

    /**
     * Throws an IllegalStateException if the transmitter is already running.
     */
    private void verifyNotRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Cannot mutate DataTransmitter while it is running");
        }
    }
}
