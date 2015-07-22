package edu.missouri.chunk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

/**
 * Sends data of a specified size to a URI at a regular interval.
 *
 * @author Andrew Smith
 */
class DataTransmitter {
    private final Context context;
    private Date start;
    public static byte[] bytes;

    public Date getStart() {
        return start;
    }

    private URI uri;
    private int size;
    private int freq;

    /**
     * Constructor
     */
    public DataTransmitter(Context context){
        this.context = context;
    }

    /**
     *  Starts sending data.
     */
   public void start() {

       if(uri == null) {
           throw new IllegalStateException("URI cannot be null");
       } else if(size == 0) {
           throw new IllegalStateException("Size cannot be null");
       } else if(freq == 0) {
           throw new IllegalStateException("Frequency cannot be null");
       }

       enableAlarm();
   }

    private Context getApplicationContext() {
        return context;
    }

    private void enableAlarm() {
        long millisecondsTilFirstTrigger = 0;
        long intervalToNextAlarm         =  freq * 1000;

        Log.d("DataTransmitter", "Preparing to initiate Alarm Manager. Should start syncing in "
                + Long.toString(millisecondsTilFirstTrigger)
                + " milliseconds, and once every "
                + Long.toString(intervalToNextAlarm)
                + " milliseconds thereafter.");


        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(getApplicationContext(), SyncService.class);
        mIntent.putExtra("uri", uri);
        mIntent.putExtra("interval", intervalToNextAlarm);

        InputStream is = null;

        try {

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

            bytes = toByteArray(is);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d("DataTransmitter", String.format("About to begin syncing in %d milliseconds, hopefully.", millisecondsTilFirstTrigger));
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final long timeOfNextSync = Calendar.getInstance().getTimeInMillis() + millisecondsTilFirstTrigger;

        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, timeOfNextSync, pendingIntent);
        start = new Date();
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

    /**
     *
     * @param  uri                   The URI to send data to
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     *
     * @param  size                  The size of the data to send to the URI at each interval
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     *
     * @param  freq                  The frequency at which to send data
     */
    public void setFreq(int freq) {

        this.freq = freq;
    }
}
