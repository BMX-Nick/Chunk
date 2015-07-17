package edu.missouri.chunk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.URI;

/**
 * Sends data of a specified size to a URI at a regular interval.
 *
 * @author Andrew Smith
 */
public class DataTransmitter {
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
     * @param uri  the URI of the server to which garbage data will be sent
     * @param size the size of the data in KB
     * @param freq the update frequency in seconds
     */
    public DataTransmitter(Context context, URI uri, int size, int freq){
        this.context = context;
        
        this.uri  = uri;
        this.size = size;
        this.freq = freq;

        this.state = State.STOPPED;
    }

    /**
     *  Starts sending data.
     *
     * @throws IllegalStateException Thrown if the transmitter is already running or the uri is null
     */
   public void start() throws IllegalStateException {
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
        long millisecondsTilFirstTrigger = getFreq() * 1000;
        long intervalToNextAlarm         = millisecondsTilFirstTrigger;

        Log.w("DataTransmitter", "Preparing to initiate Alarm Manager. Should start syncing in "
                + Long.toString(millisecondsTilFirstTrigger)
                + " milliseconds, and once every "
                + Long.toString(intervalToNextAlarm)
                + " milliseconds thereafter.");

        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(getApplicationContext(), SyncService.class);

        mIntent.putExtra("uri",       getUri());
        mIntent.putExtra("frequency", getFreq());
        mIntent.putExtra("size",      getSize());

        Log.w("DataTransmitter", "About to begin syncing in 30 seconds, hopefully.");

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                millisecondsTilFirstTrigger,
                intervalToNextAlarm, PendingIntent.getService(getApplicationContext(), 30, mIntent, PendingIntent.FLAG_UPDATE_CURRENT));

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
    public void stop() throws IllegalStateException {
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
    public URI getUri() {
        return uri;
    }

    /**
     *
     * @param  uri                   The URI to send data to
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setUri(URI uri) throws IllegalStateException {
       verifyNotRunning();

        this.uri = uri;
    }

    /**
     *
     * @return The size of the data that is sent to the URI at each interval
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param  size                  The size of the data to send to the URI at each interval
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setSize(int size) throws IllegalStateException {
        verifyNotRunning();

        this.size = size;
    }

    /**
     *
     * @return The frequency at which data will be sent
     */
    public int getFreq() {
        return freq;
    }

    /**
     *
     * @param  freq                  The frequency at which to send data
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setFreq(int freq) throws IllegalStateException {
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
