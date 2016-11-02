package asu.edu.cse535.locationawarereminder.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;
import asu.edu.cse535.locationawarereminder.services.LocationListenerService;

//import android.support.v4.app.DialogFragment; Using DialogFragment below for Date/Time Picker


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
    static EditText reminderDesc;
    DialogFragment dialogFragmentDate, dialogFragmentTime;
    static Task t;
    static int hourOfDaySelected, minuteSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);

        t = new Task();

        // Display up button
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Read extras from Intent
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            mode = extras.getInt("Mode");

        buttonSave = (Button)findViewById(R.id.button_save);
        buttonGetDirections = (Button)findViewById(R.id.button_get_directions);
        buttonMarkDone = (Button)findViewById(R.id.button_mark_done);
        buttonPickLocation = (Button)findViewById(R.id.button_picklocation);
        buttonAddReminder = (Button)findViewById(R.id.button_add_reminder);
        buttonPickDate = (Button)findViewById(R.id.button_pick_date);
        buttonPickTime = (Button)findViewById(R.id.button_pick_time);

        radioGroupMot = (RadioGroup)findViewById(R.id.radioGroupMot);
        radioWalk = (RadioButton)findViewById(R.id.radiobutton_mot_walking);
        radioCycle = (RadioButton)findViewById(R.id.radiobutton_mot_cycling);
        radioDrive = (RadioButton)findViewById(R.id.radiobutton_mot_driving);

        reminderDesc = (EditText)findViewById(R.id.editText_desc);

        // Method to hide or show controls according to context
        hideShowControls(getApplicationContext());

        // Add onclick for Pick Location button
        buttonPickLocation.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Launch Location Picker
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(NewTaskActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //Add onclick for Add Reminder button
        buttonAddReminder.setOnClickListener(new View.OnClickListener(){

            private int year, month, dateField;
            @Override
            public void onClick(View v) {
                //Add task to database
                boolean checked = checkMandatory();
                if(checked){
                    TextView date = (TextView)findViewById(R.id.textView_date);
                    TextView time = (TextView)findViewById(R.id.textView_time);

                    t.setDesc(reminderDesc.getText().toString());
                    Calendar c = Calendar.getInstance();
                    if(!date.getText().toString().isEmpty() && !time.getText().toString().isEmpty()){
                        formatDateInput(date.getText().toString());
                        month -= 1;
                        c.set(year, month, dateField, hourOfDaySelected, minuteSelected);
                        t.setTaskDate(c.getTime());
                    }
                    if(radioWalk.isSelected())
                        t.setMot("Walking");
                    else if(radioCycle.isSelected())
                        t.setMot("Cycling");
                    else if(radioDrive.isSelected())
                        t.setMot("Driving");
                    t.setStatus("Created");
                    t.setCreatedDate(c.getTime());
                    DBManager.insertIntoTask(t);
                    int taskId = DBManager.getLastInserted();
                    startLocationListener(taskId, t.getDesc(), t.getLat(), t.getLng());
                    finish();
                }
            }

            // Method to start location listener service
            private void startLocationListener(final int taskId, final String taskDesc, final double lat, final double lng) {

                Thread t = new Thread(){
                    public void run(){
                        Intent serviceIntent = new Intent(NewTaskActivity.this.getBaseContext(), LocationListenerService.class);
                        serviceIntent.putExtra("task_id", taskId);
                        serviceIntent.putExtra("task_desc", taskDesc);
                        serviceIntent.putExtra("lat", lat);
                        serviceIntent.putExtra("lng", lng);
                        startService(serviceIntent);
                    }
                };
                t.start();
            }

            private void formatDateInput(String dateVal) {
                month = Integer.parseInt(dateVal.split("/")[0]);
                dateField = Integer.parseInt(dateVal.split("/")[1]);
                year = Integer.parseInt(dateVal.split("/")[2]);
            }
        });

        //Add onClick for Pick Date button
        buttonPickDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialogFragmentDate = new DatePickerDialogTheme();
                dialogFragmentDate.show(getFragmentManager(), "Pick Date");
            }
        });

        //Add onClick for Pick Time button

        buttonPickTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogFragmentTime = new TimePickerDialogTheme();
                dialogFragmentTime.show(getFragmentManager(),"Pick Time");
            }
        });
    }

    private boolean checkMandatory(){
        //Check all mandatory values here
        if(reminderDesc.getText().toString().isEmpty()){
            Toast.makeText(NewTaskActivity.this, "Provide a Reminder Description", Toast.LENGTH_LONG).show();
            return false;
        }

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
                String locText = place.getName() + " " + place.getAddress();
                locationText.setText(locText);
            }
        }
    }

    private void hideShowControls(Context context){
        //TO:DO Implement for other types of context
        if(mode == R.string.new_task){
            hideControlsForNewTask(context);
            setTitle(R.string.new_task);
        }
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
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);


            datePickerDialog  = new DatePickerDialog(getActivity(),android.R.style.Theme_DeviceDefault_Dialog,this,year,month,day);

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day){

            TextView displayDate = (TextView)getActivity().findViewById(R.id.textView_date);
            String dateText = (month+1) + "/" + day + "/" + year;
            displayDate.setText(dateText);

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
            timePickerDialog= new TimePickerDialog(getActivity(),android.R.style.Theme_DeviceDefault_Dialog,this,hour,minute,false);

            return timePickerDialog;
        }


        public void onTimeSet(TimePicker view, int hourOfDay, int minute){

            TextView displayTime = (TextView)getActivity().findViewById(R.id.textView_time);
            hourOfDaySelected = hourOfDay;
            minuteSelected = minute;
            String amPm = getAmPm(hourOfDay);

            final String time = hourOfDay + ":" + minute;
            String timeText = time + " " + amPm;
            displayTime.setText(timeText);

            try {
                final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                final Date dt = sdf.parse(time);
                displayTime.setText(sdf.format(dt) + " " + amPm);
            } catch (final ParseException e) {
                e.printStackTrace();
            }

        }

        // Using 24-hour time calculate 12-hour time AM/PM
        public String getAmPm (int hour){
            String amPm;

            if (hour >= 12) {
                amPm = "PM";
            }
            else {
                amPm = "AM";
            }
            return amPm;
        }
    }
}
