package asu.edu.cse535.locationawarereminder.activities;
import java.util.ArrayList;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.location.Address;
import android.location.Geocoder;
import java.util.Locale;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.Constants;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;
import asu.edu.cse535.locationawarereminder.services.LocationListenerService;

//import android.support.v4.app.DialogFragment; Using DialogFragment below for Date/Time Picker


/**
 * Created by Sooraj on 10/21/2016.
 */
public class NewTaskActivity extends AppCompatActivity {

    Task task = new Task();
    Context con;
    String task_name;
    int new_or_edit;
    static boolean DEBUG = false;
    static final int PLACE_PICKER_REQUEST = 1;
    int mode;
    static Button buttonSave, buttonGetDirections, buttonMarkDone, buttonPickLocation, buttonAddReminder, buttonPickDate, buttonPickTime;
    static RadioGroup radioGroupMot;
    static RadioButton radioWalk, radioCycle, radioDrive;
    static EditText reminderDesc;
    DialogFragment dialogFragmentDate, dialogFragmentTime;
    static Task t;
    static int hourOfDaySelected, minuteSelected;
    Geocoder geocoder;
    List<Address> addresses;
    int selected_location =0;
    String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);
        con = getApplication().getBaseContext();

        t = new Task();

        // Display up button
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getInt("Mode");

            //Log.v("mode is", Integer.toString(mode));
        }

        if (mode == R.string.edit_task) {
            Log.v("inside edit task","inside edit task");
            task_name = extras.getString("task_name");
            Constants.DATATYPE_STRING = task_name;
            DBManager.fetch_data(task_name);
            Log.v("getdesc", DBManager.task_details.getDesc());
            buttonSave = (Button) findViewById(R.id.button_save);
            buttonGetDirections = (Button) findViewById(R.id.button_get_directions);
            buttonMarkDone = (Button) findViewById(R.id.button_mark_done);
            buttonPickLocation = (Button) findViewById(R.id.button_picklocation);
            buttonAddReminder = (Button) findViewById(R.id.button_add_reminder);
            buttonPickDate = (Button) findViewById(R.id.button_pick_date);
            buttonPickTime = (Button) findViewById(R.id.button_pick_time);

            radioGroupMot = (RadioGroup) findViewById(R.id.radioGroupMot);
            radioWalk = (RadioButton) findViewById(R.id.radiobutton_mot_walking);
            radioCycle = (RadioButton) findViewById(R.id.radiobutton_mot_cycling);
            radioDrive = (RadioButton) findViewById(R.id.radiobutton_mot_driving);

            reminderDesc = (EditText) findViewById(R.id.editText_desc);
            TextView date = (TextView) findViewById(R.id.textView_date);
            TextView time = (TextView) findViewById(R.id.textView_time);
            if (DBManager.task_details.getTaskDate() != null) {
                String displayFormat = "MM/dd/yyyy HH:mm:ss";
                formattedDate = new SimpleDateFormat(displayFormat).format(DBManager.task_details.getTaskDate());
                String datePart = formattedDate.split(" ")[0];
                date.setText(datePart);
                String timepart = formattedDate.split(" ")[1];
                int hour = Integer.parseInt(timepart.split(":")[0]);
                String amPm = getAmPm(hour);
                if (hour > 12)
                    hour -= 12;
                timepart = (hour < 10 ? "0" + hour : hour) + ":" + timepart.split(":")[1] + " " + amPm;
                time.setText(timepart);


                EditText task_text = (EditText) findViewById(R.id.editText_desc);
                task_text.setText(DBManager.task_details.getDesc());

                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(DBManager.task_details.getLat(), DBManager.task_details.getLng(), 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                Log.v("address", address);
                Log.v("city", city);


                radioGroupMot = (RadioGroup) findViewById(R.id.radioGroupMot);
                radioWalk = (RadioButton) findViewById(R.id.radiobutton_mot_walking);
                radioCycle = (RadioButton) findViewById(R.id.radiobutton_mot_cycling);
                radioDrive = (RadioButton) findViewById(R.id.radiobutton_mot_driving);

                TextView locationText = (TextView) findViewById(R.id.textView_location);
                String locText = address + "," + city + "," + state;
                locationText.setText(locText);
                String Mot = DBManager.task_details.getMot();
                Log.v("Mot", Mot);
                if (Mot.contains("Walker")) {
                    radioWalk.setChecked(true);
                }
                if (Mot.contains("Cycling")) {
                    radioCycle.setChecked(true);
                }

                if (Mot.contains("Driving")) {
                    radioDrive.setChecked(true);
                }
                buttonSave = (Button) findViewById(R.id.button_save);
                buttonSave.setOnClickListener(new View.OnClickListener() {
                    private int year, month, dateField;

                    @Override
                    public void onClick(View v) {

                        //Launch Location Picker
                        try {
                            Log.v("task_name_Deleting", DBManager.task_details.getDesc());
                            DBManager.DeleteTask(DBManager.task_details.getDesc());
                            boolean checked = checkMandatory();
                            if (checked) {
                                Log.v("inside checked", "inside checked");
                                TextView date = (TextView) findViewById(R.id.textView_date);
                                TextView time = (TextView) findViewById(R.id.textView_time);

                                t.setDesc(reminderDesc.getText().toString());
                                Log.v("edited_task_name", reminderDesc.getText().toString());
                                Calendar c = Calendar.getInstance();
                                if (!date.getText().toString().isEmpty() && !time.getText().toString().isEmpty()) {
                                    formatDateInput(date.getText().toString());
                                    month -= 1;
                                    c.set(year, month, dateField, hourOfDaySelected, minuteSelected);
                                    t.setTaskDate(c.getTime());
                                }
                                if (selected_location == 0) {
                                    t.setLat(DBManager.task_details.getLat());
                                    t.setLng(DBManager.task_details.getLng());
                                }
                                if (radioWalk.isSelected())
                                    t.setMot("Walking");
                                else if (radioCycle.isSelected())
                                    t.setMot("Cycling");
                                else if (radioDrive.isSelected())
                                    t.setMot("Driving");
                                else
                                    t.setMot(DBManager.task_details.getMot());
                                t.setStatus("Created");
                                t.setCreatedDate(c.getTime());

                                DBManager.insertIntoTask(t);
                                Intent intent = new Intent();
                                intent.putExtra("task_type", "edit_task");
                                Log.v("sending to main", "sending to main");
                                setResult(android.app.Activity.RESULT_OK, intent);
                                Log.v("before finish", "before finish");
                                finish();

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    private void formatDateInput(String dateVal) {
                        month = Integer.parseInt(dateVal.split("/")[0]);
                        dateField = Integer.parseInt(dateVal.split("/")[1]);
                        year = Integer.parseInt(dateVal.split("/")[2]);
                    }
                });


//            Log.v("desc name",task.getDesc());


            }
        }

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
                    if(radioWalk.isChecked())
                        t.setMot("Walking");
                    else if(radioCycle.isChecked())
                        t.setMot("Cycling");
                    else if(radioDrive.isChecked())
                        t.setMot("Driving");
                    t.setStatus("Created");
                    t.setCreatedDate(c.getTime());
                    DBManager.insertIntoTask(t);

                    int taskId = DBManager.getLastInserted();
                    startLocationListener(taskId, t.getDesc(), t.getLat(), t.getLng());

                    Intent intent = new Intent();
                    intent.putExtra("task_type","new_task");
                    Log.v("sending to main","sending to main");
                    setResult(android.app.Activity.RESULT_OK,intent);
                    Log.v("before finish","before finish");

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

        buttonGetDirections.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskActivity.this, MapsActivity.class);
                //intent.putExtra("Mode", R.string.edit_task);
                //intent.putExtra("task_name", ((TextView) view).getText());
                startActivity(intent);
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

    /* Check andatory fields */
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
        // Read extras from Intent
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            mode = extras.getInt("Mode");

        if(mode == R.string.new_task){
            hideControlsForNewTask(context);
            setTitle(R.string.new_task);
        }
        else if(mode == R.string.add_task_from_fav) {
            hideControlsForFavLocTask();
            setTitle(R.string.new_task);
            t.setLat(getIntent().getDoubleExtra("Latitude", 0.0));
            t.setLng(getIntent().getDoubleExtra("Longitude", 0.0));
        }
        else if(mode == R.string.open_from_notif) {
            String formattedDate;

            int task_id = getIntent().getExtras().getInt("taskid");
            Task t = DBManager.getTaskByTaskId(task_id);

            if(t.getDesc() != null)
                reminderDesc.setText(t.getDesc());

            TextView date = (TextView) findViewById(R.id.textView_date);
            TextView time = (TextView) findViewById(R.id.textView_time);
            /* Convert date to display format */
            if(t.getTaskDate() != null) {
                String displayFormat = "MM/dd/yyyy HH:mm:ss";
                formattedDate = new SimpleDateFormat(displayFormat).format(t.getTaskDate());
                String datePart = formattedDate.split(" ")[0];
                date.setText(datePart);
                String timepart = formattedDate.split(" ")[1];
                int hour = Integer.parseInt(timepart.split(":")[0]);
                String amPm = getAmPm(hour);
                if(hour > 12)
                    hour -= 12;
                timepart = (hour<10 ? "0"+hour : hour) + ":" + timepart.split(":")[1] + " " + amPm;
                time.setText(timepart);
            }

            TextView location = (TextView) findViewById(R.id.textView_location);
            if(t.getLng() != 0.0 && t.getLat() != 0.0)
                location.setText("Latitude : " + t.getLat() + " Longitude : " + t.getLng());

            if(t.getMot().equals("Walking"))
                radioWalk.setChecked(true);
            else if(t.getMot().equals("Cycling"))
                radioCycle.setChecked(true);
            else if(t.getMot().equals("Driving"))
                radioDrive.setChecked(true);

            hideControlsForViewTask();
            setTitle(R.string.open_from_notif);
        }
    }

    private void hideControlsForViewTask() {
        buttonMarkDone.setVisibility(View.VISIBLE);
        buttonGetDirections.setVisibility(View.VISIBLE);
        buttonSave.setVisibility(View.GONE);
        buttonAddReminder.setVisibility(View.GONE);
        reminderDesc.setEnabled(false);
        buttonPickDate.setEnabled(false);
        buttonPickTime.setEnabled(false);
        buttonPickLocation.setEnabled(false);
        radioGroupMot.setEnabled(false);
        radioDrive.setEnabled(false);
        radioCycle.setEnabled(false);
        radioWalk.setEnabled(false);
    }

    private static void hideControlsForNewTask(Context context){
        buttonMarkDone.setVisibility(View.GONE);
        buttonGetDirections.setVisibility(View.GONE);
        buttonSave.setVisibility(View.GONE);
        buttonAddReminder.setVisibility(View.VISIBLE);
        if(DEBUG)
            Toast.makeText(context, "Few buttons have been hidden", Toast.LENGTH_SHORT).show();
    }

    private void hideControlsForFavLocTask() {
        buttonMarkDone.setVisibility(View.GONE);
        buttonGetDirections.setVisibility(View.GONE);
        buttonSave.setVisibility(View.GONE);
        buttonAddReminder.setVisibility(View.VISIBLE);
        buttonPickLocation.setEnabled(false);
        TextView textView_location = (TextView) findViewById(R.id.textView_location);
        textView_location.setText(getIntent().getStringExtra("LocationName"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);*/
        finish();
        return true;
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
    }

    // Using 24-hour time calculate 12-hour time AM/PM
    public static String getAmPm (int hour){
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
