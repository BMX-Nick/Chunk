package edu.missouri.chunk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
    public static boolean performingSync;

    private DataTransmitter dataTransmitter;

    private EditText    urlEditText;
    private Spinner     chunksSpinner;
    private Spinner     interValSpinner;
    private Button      startButton;
    public static ProgressBar progressBar;
    private TextView    startTextView;
    private TextView    endTextView;

    public static int counter;

    private static final String[] CHUNKS = new String[]{
            "100",
            "1024",
            "10240"
    };

    private static final String[] INTERVALS = new String[]{
            "60",
            "300",
            "600"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        urlEditText     = (EditText)    findViewById(R.id.urlEdit);
        chunksSpinner   = (Spinner)     findViewById(R.id.chunkSizeSpinner);
        interValSpinner = (Spinner)     findViewById(R.id.intervalSpinner);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar);
        startButton     = (Button)      findViewById(R.id.startButton);

        counter = 0;

        progressBar.setVisibility(View.INVISIBLE);

        startTextView = (TextView) findViewById(R.id.startTextView);
        endTextView = (TextView) findViewById(R.id.endTextView);


        ArrayAdapter<String> chunk    = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CHUNKS);
        final ArrayAdapter<String> interval = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, INTERVALS);

        chunksSpinner.setAdapter(chunk);
        interValSpinner.setAdapter(interval);


        // On click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final boolean areFieldsSet =
                           chunksSpinner.getSelectedItem().toString().length()   > 0
                        && interValSpinner.getSelectedItem().toString().length() > 0
                        && urlEditText.getText().toString().length()             > 0;

                // If all the fields are filled out when start is pressed,
                if (areFieldsSet) {

                    // Collect the user input
                    int chunkSizeKB     = Integer.parseInt(chunksSpinner.getSelectedItem().toString());
                    int intervalSeconds = Integer.parseInt(interValSpinner.getSelectedItem().toString());
                    URI uri;

                    try {
                        uri = new URI(urlEditText.getText().toString());

                        performingSync = true;

                        // Initialize and start the data transmitter.
                        Log.d(TAG, "Initializing and starting data transmitter");
                        dataTransmitter.setFreq(intervalSeconds);
                        dataTransmitter.setSize(chunkSizeKB);
                        dataTransmitter.setUri(uri);
                        dataTransmitter.start();

                        urlEditText.setEnabled(false);
                        chunksSpinner.setEnabled(false);
                        interValSpinner.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                        startButton.setEnabled(false);

                        startButton.setText(getString(R.string.running));
                        final String startTime = DATE_TIME_FORMAT.format(dataTransmitter.getStart());

                        startTextView.setText(String.format("Start: %s", startTime));
                        endTextView.setText("End:");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Error").setMessage("Fields must not be empty.").setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                AsyncTask<Void, Void, Boolean> waitTask = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {

                        while(MainActivity.performingSync) {
                            try {
                                Thread.sleep(500, 0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);

                        progressBar.setVisibility(View.INVISIBLE);
                        endTextView.setText(String.format("End: %s", DATE_TIME_FORMAT.format(new Date())));
                        Context ctx = getApplicationContext();
                        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                        MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.sound_file_1);
                        mediaPlayer.start();

                        urlEditText.setEnabled(true);
                        chunksSpinner.setEnabled(true);
                        interValSpinner.setEnabled(true);

                        startButton.setEnabled(true);
                        startButton.setText(R.string.start);

                        MainActivity.counter = 0;
                    }
                };

                waitTask.execute();

                Log.d(TAG, "After waitTask.execute");
            }
        });
        dataTransmitter = new DataTransmitter(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
