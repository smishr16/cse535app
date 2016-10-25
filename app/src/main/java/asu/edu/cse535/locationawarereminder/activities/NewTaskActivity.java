package asu.edu.cse535.locationawarereminder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;

import android.widget.RadioGroup.OnCheckedChangeListener;


/**
 * Created by Sooraj on 10/21/2016.
 */
public class NewTaskActivity extends AppCompatActivity {

    static boolean DEBUG = true;
    static final int PLACE_PICKER_REQUEST = 1;
    static String type;
    static Button buttonSave, buttonGetDirections, buttonMarkDone, buttonPickLocation, buttonAddReminder;
    static RadioGroup radioGroupMot;
    static RadioButton radioWalk, radioCycle, radioDrive;
    static Task t = new Task();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Read extras from Intent
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            type = extras.getString("Type");

        buttonSave = (Button)findViewById(R.id.button_save);
        buttonGetDirections = (Button)findViewById(R.id.button_get_directions);
        buttonMarkDone = (Button)findViewById(R.id.button_mark_done);
        buttonAddReminder = (Button)findViewById(R.id.button_add_reminder);
        buttonPickLocation = (Button)findViewById(R.id.button_picklocation);
        buttonAddReminder = (Button)findViewById(R.id.button_add_reminder);

        radioGroupMot = (RadioGroup)findViewById(R.id.radioGroupMot);
        radioWalk = (RadioButton)findViewById(R.id.radiobutton_mot_walking);
        radioCycle = (RadioButton)findViewById(R.id.radiobutton_mot_cycling);
        radioDrive = (RadioButton)findViewById(R.id.radiobutton_mot_driving);



        // Method to hide or show controls according to context
        hideShowControls(getApplicationContext());

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
        Button addRem = (Button)findViewById(R.id.button_add_reminder);
        addRem.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Add task to database
                //TO:DO Replace dummy values and date values with actual field values
                boolean checked = checkMandatory();
                if(checked){
                    t.setDesc("test");
                    Calendar c = Calendar.getInstance();
                    t.setTaskDate(c.getTime());
                    t.setMot("test");
                    t.setStatus("Created");
                    t.setCreatedDate(c.getTime());
                    DBManager.addTaskToDB(t);
                    finish();
                }
            }
        });
    }

    private boolean checkMandatory(){
        //Check all mandatory values here
        //TO:DO Do Mandatory field validations
        if(t.getLat() == 0.0 && t.getLng() == 0.0){
            Toast.makeText(NewTaskActivity.this, "Pick a location", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                LatLng lat_long =  place.getLatLng();
                if(DEBUG)
                    Toast.makeText(NewTaskActivity.this, lat_long.latitude + " " + lat_long.longitude, Toast.LENGTH_SHORT).show();
                t.setLat(lat_long.latitude);
                t.setLng(lat_long.longitude);
                TextView locationText = (TextView)findViewById(R.id.textView_location);
                locationText.setText(place.getName());
            }
        }
    }

    private void hideShowControls(Context context){
        //TO:DO Implement for other types of context
        if(type.equalsIgnoreCase("New"))
            hideControlsForNew(context);
    }

    private static void hideControlsForNew(Context context){
        buttonMarkDone.setVisibility(View.GONE);
        buttonGetDirections.setVisibility(View.GONE);
        buttonSave.setVisibility(View.GONE);
        if(DEBUG)
            Toast.makeText(context, "Few buttons have been hidden", Toast.LENGTH_SHORT).show();
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
