package edu.missouri.chunk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import chunk.missouri.edu.chunk.R;


public class MainActivity extends Activity {

    private EditText urlEditText;
    private Spinner  chunksSpinner;
    private Spinner  interValSpinner;

    private Button startButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] chunks = {
                "1K",
                "100K",
                "1MB",
                "10MB"
        };

        String[] intervals = {
                "30 Sec",
                "60 Sec"
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

                // TODO
                // if (chunksSpinner has a value and intervalSpinner has a value) {
                //      Use PendingIntent and AlarmManager here just like in the NIMH app.
                // }
            }
        });
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
