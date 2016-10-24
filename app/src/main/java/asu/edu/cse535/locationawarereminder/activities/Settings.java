package asu.edu.cse535.locationawarereminder.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import asu.edu.cse535.locationawarereminder.R;

/**
 * Created by Sooraj on 10/23/2016.
 */
public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button saveSettings = (Button) findViewById(R.id.button_save_settings);
        saveSettings.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //TO:DO Save settings to Database

                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
