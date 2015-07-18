package edu.missouri.chunk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private DataTransmitter dataTransmitter;

    private EditText urlEditText;
    private Spinner  chunksSpinner;
    private Spinner  interValSpinner;
    private Button   startButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] chunks = {
                "100",
                "1024",
                "10240"
        };

        String[] intervals = {
                "60",
                "300",
                "600"
        };

        urlEditText     = (EditText) findViewById(R.id.urlEdit);
        chunksSpinner   = (Spinner)  findViewById(R.id.chunkSizeSpinner);
        interValSpinner = (Spinner)  findViewById(R.id.intervalSpinner);

        startButton = (Button) findViewById(R.id.startButton);

        ArrayAdapter<String> chunk    = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, chunks);
        ArrayAdapter<String> interval = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, intervals);

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

                    if (dataTransmitter.isRunning()) {
                        dataTransmitter.stop();
                        startButton.setText(R.string.start);
                    } else {
                        try {
                            uri = new URI(urlEditText.getText().toString());

                            // Initialize and start the data transmitter.
                            Log.w(TAG, "Initializing and starting data transmitter");
                            dataTransmitter.setFreq(intervalSeconds);
                            dataTransmitter.setSize(chunkSizeKB);
                            dataTransmitter.setUri(uri);


                            dataTransmitter.start();
                            startButton.setText(R.string.stop);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
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
            }
        });

        // BUG: Change my values!
        try {
            dataTransmitter = new DataTransmitter(getApplicationContext(), new URI("http://badurl.com"), 0, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
