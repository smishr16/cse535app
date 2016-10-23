package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;

/**
 * Created by Sooraj on 10/21/2016.
 */
public class NewTaskActivity extends AppCompatActivity {

    static final int PLACE_PICKER_REQUEST = 1;
    static double latitude;
    static double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // Add onclick for Pick Location button
        Button pickLocation = (Button)findViewById(R.id.button_picklocation);
        pickLocation.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Launch Location Picker
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(NewTaskActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //Add onclick for Add Reminder button
        Button addRem = (Button)findViewById(R.id.button_add);
        addRem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Add task to database
                String desc = "test";
                Calendar c = Calendar.getInstance();
                Date taskDate = c.getTime();
                String mot = "test";
                String status = "Created";
                Task t = new Task(desc, taskDate, mot, latitude, longitude, taskDate);
                DBManager.addTaskToDB(t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                LatLng lat_long =  place.getLatLng();
                latitude = lat_long.latitude;
                longitude = lat_long.longitude;
                //String toastMsg = String.format("Place: %s", place.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                TextView locationText = (TextView)findViewById(R.id.textView_location);
                locationText.setText(place.getName());
            }
        }
    }
}
