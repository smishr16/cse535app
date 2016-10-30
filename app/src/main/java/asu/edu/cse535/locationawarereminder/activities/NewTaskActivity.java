package asu.edu.cse535.locationawarereminder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.DialogFragment; Using DialogFragment below for Date/Time Picker
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException; //will be used if support for APK 24+ supported
import java.util.Date; //will be used if support for APK 24+ supported
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.icu.text.SimpleDateFormat; //will be used if support for APK 24+ supported
import android.widget.DatePicker;
import android.widget.TimePicker;

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

    static boolean DEBUG = false;
    static final int PLACE_PICKER_REQUEST = 1;
    static int mode;
    static Button buttonSave, buttonGetDirections, buttonMarkDone, buttonPickLocation, buttonAddReminder, buttonPickDate, buttonPickTime;
    static RadioGroup radioGroupMot;
    static RadioButton radioWalk, radioCycle, radioDrive;
    DialogFragment dialogFragmentDate, dialogFragmentTime;
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
            mode = extras.getInt("Mode");

        buttonSave = (Button)findViewById(R.id.button_save);
        buttonGetDirections = (Button)findViewById(R.id.button_get_directions);
        buttonMarkDone = (Button)findViewById(R.id.button_mark_done);
        buttonAddReminder = (Button)findViewById(R.id.button_add_reminder);
        buttonPickLocation = (Button)findViewById(R.id.button_picklocation);
        buttonAddReminder = (Button)findViewById(R.id.button_add_reminder);
        buttonPickDate = (Button)findViewById(R.id.button_pick_date);
        buttonPickTime = (Button)findViewById(R.id.button_pick_time);

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

        //Add onClick for Pick Date button
        buttonPickDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                dialogFragmentDate = new DatePickerDialogTheme();

                dialogFragmentDate.show(getFragmentManager(), "Pick Date");
            }
        });

        //Add onClick for Pick Time button

        buttonPickTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                dialogFragmentTime = new TimePickerDialogTheme();

                dialogFragmentTime.show(getFragmentManager(),"Pick Time");
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
        if(mode == R.string.new_task)
            hideControlsForNewTask(context);
    }

    private static void hideControlsForNewTask(Context context){
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

    // Allows user to choose a date
    public static class DatePickerDialogTheme extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            DatePickerDialog datePickerDialog;
            Calendar calendar =Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_WEEK);


            datePickerDialog  = new DatePickerDialog(getActivity(),16974545,this,year,month,day);

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day){

            TextView displayDate = (TextView)getActivity().findViewById(R.id.textView_date);

            displayDate.setText((month+1) + "/" + day + "/" + year);

        }
    }

    // Allows user to pick a time
    public static class TimePickerDialogTheme extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog;
            timePickerDialog= new TimePickerDialog(getActivity(),16974545,this,hour,minute,false);

            return timePickerDialog;
        }


        public void onTimeSet(TimePicker view, int hourOfDay, int minute){

            TextView displayTime = (TextView)getActivity().findViewById(R.id.textView_time);
            String amPm = getAmPm(hourOfDay);
            String time = getTime(hourOfDay, minute);

            // APK 24+ final String
            //final String time = hourOfDay + ":" + minute;

            displayTime.setText(time + " " + amPm);

            /* Only works with APK 24+
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                final Date dt = sdf.parse(time);
                displayTime.setText(sdf.format(sdf.parse(time)) + " " + amPm);
            } catch (final ParseException e) {
                e.printStackTrace();
            }
            */
        }

        // Using 24-hour time calculate 12-hour time AM/PM
        public String getAmPm (int hour){
            String amPm = "";

            if (hour >= 12) {
                amPm = "PM";
            }

            else {
                amPm = "AM";
            }
            return amPm;
        }

        // Using 24-hour time convert to 12-hour time
        public String getTime (int hour, int minutes){
            String time = "";
            String hours = "";

            if (hour > 12){
                if (hour >=22){
                    hour = hour - 12;
                    hours = hour + "";
                }
                else{
                    hour = hour - 12;
                    hours = "0" + hour;
                }

            }
            else if (hour == 0){
                hours = 12 + "";
            }
            else{
                hours = "0" + hour;
            }

            if (minutes < 10){
                time = hours + ":" + "0" + minutes;
            }
            else {
                time = hours + ":" + minutes;
            }

            return time;
        }

    }
}
