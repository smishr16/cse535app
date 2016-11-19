package asu.edu.cse535.locationawarereminder.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

/**
 * Created by Sooraj on 10/21/2016.
 */
public class NewTaskActivity extends AppCompatActivity {

    static boolean DEBUG = false;
    static final int PLACE_PICKER_REQUEST = 1;
    int mode;
    final Task globalTask = new Task();
    static Button buttonSave, buttonGetDirections, buttonMarkDone, buttonPickLocation, buttonAddReminder, buttonPickDate, buttonPickTime;
    static RadioGroup radioGroupMot;
    static RadioButton radioWalk, radioCycle, radioDrive;
    static EditText reminderDesc;
    DialogFragment dialogFragmentDate, dialogFragmentTime;
    static int hourOfDaySelected, minuteSelected;
    String mode_of_trnspt;
    String locText;
    DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);

        dbManager = new DBManager(NewTaskActivity.this);

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

        // Display up button
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Mode
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getInt("Mode");
        }

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

        // Add onclick for Add Reminder button
        buttonAddReminder.setOnClickListener(new View.OnClickListener(){

            private int year, month, dateField;
            @Override
            public void onClick(View v) {
                //Add task to database
                boolean checked = checkMandatory();
                if(checked){
                    TextView date = (TextView)findViewById(R.id.textView_date);
                    TextView time = (TextView)findViewById(R.id.textView_time);

                    globalTask.setDesc(reminderDesc.getText().toString());
                    Calendar c = Calendar.getInstance();
                    if(!date.getText().toString().isEmpty() && !time.getText().toString().isEmpty()){
                        formatDateInput(date.getText().toString());
                        month -= 1;
                        c.set(year, month, dateField, hourOfDaySelected, minuteSelected);
                        globalTask.setTaskDate(c.getTime());
                    }
                    if(radioWalk.isChecked())
                        globalTask.setMot("Walking");
                    else if(radioCycle.isChecked())
                        globalTask.setMot("Cycling");
                    else if(radioDrive.isChecked())
                        globalTask.setMot("Driving");
                    globalTask.setStatus("Created");
                    globalTask.setCreatedDate(c.getTime());

                    TextView locationText = (TextView) findViewById(R.id.textView_location);
                    globalTask.setLocDesc(locationText.getText().toString());

                    dbManager.insertIntoTask(globalTask);

                    int taskId = dbManager.getLastInserted();
                    startLocationListener(taskId, globalTask.getDesc(), globalTask.getLat(), globalTask.getLng());

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
                String loc_uri= "google.navigation:q=";
                String url = null;
                if(mode_of_trnspt != null && !mode_of_trnspt.equalsIgnoreCase("null") && !mode_of_trnspt.isEmpty()){
                    if(mode_of_trnspt.equals("Walking"))
                        url = loc_uri + locText + "&mode=w";
                    if(mode_of_trnspt.equals("Cycling"))
                        url = loc_uri + locText + "&mode=b";
                    if(mode_of_trnspt.equals("Driving"))
                        url = loc_uri + locText + "&mode=d";
                }
                else {
                    url = loc_uri + locText + "&mode=d";
                }
                Uri gmmIntentUri = Uri.parse(url);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // Add click event for Mark as Done button
        buttonMarkDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int task_id = getIntent().getExtras().getInt("task_id");
                dbManager.updateTaskStatus(task_id, "Completed");
                finish();
            }
        });

        // Add onClick for Pick Date button
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

    /* Check mandatory fields */
    private boolean checkMandatory(){
        //Check all mandatory values here
        if(reminderDesc.getText().toString().isEmpty()){
            Toast.makeText(NewTaskActivity.this, "Provide a Reminder Description", Toast.LENGTH_LONG).show();
            return false;
        }

        if(globalTask.getLat() == 0.0 && globalTask.getLng() == 0.0){
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
                globalTask.setLat(lat_long.latitude);
                globalTask.setLng(lat_long.longitude);
                TextView locationText = (TextView)findViewById(R.id.textView_location);
                String locText = place.getName() + " " + place.getAddress();
                locationText.setText(locText);
            }
        }
    }

    private void hideShowControls(Context context) {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            mode = extras.getInt("Mode");

        if (mode == R.string.new_task) {
            hideControlsForNewTask(context);
            setTitle(R.string.new_task);
        }
        else if (mode == R.string.add_task_from_fav) {
            hideControlsForFavLocTask();
            setTitle(R.string.new_task);
            globalTask.setLat(getIntent().getDoubleExtra("Latitude", 0.0));
            globalTask.setLng(getIntent().getDoubleExtra("Longitude", 0.0));
        }
        else if (mode == R.string.edit_task) {
            String formattedDate;

            int task_id = getIntent().getExtras().getInt("task_id");
            final Task t = dbManager.getTaskByTaskId(task_id);

            TextView locationText = (TextView) findViewById(R.id.textView_location);
            locationText.setText(t.getLocDesc());

            locText = t.getLat() + "," + t.getLng();

            if (t.getLng() != 0.0 && t.getLat() != 0.0) {
                this.globalTask.setLat(t.getLat());
                this.globalTask.setLng(t.getLng());
            }

            if (t.getDesc() != null)
                reminderDesc.setText(t.getDesc());

            TextView date = (TextView) findViewById(R.id.textView_date);
            TextView time = (TextView) findViewById(R.id.textView_time);
            /* Convert date to display format */
            if (t.getTaskDate() != null) {
                String displayFormat = "MM/dd/yyyy HH:mm:ss";
                formattedDate = new SimpleDateFormat(displayFormat).format(t.getTaskDate());
                String datePart = formattedDate.split(" ")[0];
                date.setText(datePart);
                String timepart = formattedDate.split(" ")[1];
                int hour = Integer.parseInt(timepart.split(":")[0]);
                String amPm = getAmPm(hour);
                if (hour > 12)
                    hour -= 12;
                else if (hour == 0)
                    hour = 12;
                timepart = (hour < 10 ? "0" + hour : hour) + ":" + timepart.split(":")[1] + " " + amPm;
                time.setText(timepart);
            }

            if (t.getMot().equals("Walking"))
                radioWalk.setChecked(true);
            else if (t.getMot().equals("Cycling"))
                radioCycle.setChecked(true);
            else if (t.getMot().equals("Driving"))
                radioDrive.setChecked(true);
            mode_of_trnspt = t.getMot();

            hideControlsForEditTask();
            setTitle(R.string.edit_task);

            buttonSave.setOnClickListener(new View.OnClickListener() {
                private int year, month, dateField;

                @Override
                public void onClick(View v) {
                    try {
                        boolean checked = checkMandatory();
                        if (checked) {
                            TextView date = (TextView) findViewById(R.id.textView_date);
                            TextView time = (TextView) findViewById(R.id.textView_time);

                            t.setDesc(reminderDesc.getText().toString());
                            Calendar c = Calendar.getInstance();
                            if (!date.getText().toString().isEmpty() && !time.getText().toString().isEmpty()) {
                                formatDateInput(date.getText().toString());
                                month -= 1;
                                c.set(year, month, dateField, hourOfDaySelected, minuteSelected);
                                t.setTaskDate(c.getTime());
                            }
                            if (radioWalk.isChecked())
                                t.setMot("Walking");
                            else if (radioCycle.isChecked())
                                t.setMot("Cycling");
                            else if (radioDrive.isChecked())
                                t.setMot("Driving");
                            mode_of_trnspt = t.getMot();
                            dbManager.updateTask(t);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(NewTaskActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                private void formatDateInput(String dateVal) {
                    month = Integer.parseInt(dateVal.split("/")[0]);
                    dateField = Integer.parseInt(dateVal.split("/")[1]);
                    year = Integer.parseInt(dateVal.split("/")[2]);
                }
            });
        }
        else if (mode == R.string.open_from_notif) {
            String formattedDate;

            int task_id = getIntent().getExtras().getInt("task_id");
            Task t = dbManager.getTaskByTaskId(task_id);

            if (t.getDesc() != null)
                reminderDesc.setText(t.getDesc());

            TextView date = (TextView) findViewById(R.id.textView_date);
            TextView time = (TextView) findViewById(R.id.textView_time);
            /* Convert date to display format */
            if (t.getTaskDate() != null) {
                String displayFormat = "MM/dd/yyyy HH:mm:ss";
                formattedDate = new SimpleDateFormat(displayFormat).format(t.getTaskDate());
                String datePart = formattedDate.split(" ")[0];
                date.setText(datePart);
                String timepart = formattedDate.split(" ")[1];
                int hour = Integer.parseInt(timepart.split(":")[0]);
                String amPm = getAmPm(hour);
                if (hour > 12)
                    hour -= 12;
                else if (hour == 0)
                    hour = 12;
                timepart = (hour < 10 ? "0" + hour : hour) + ":" + timepart.split(":")[1] + " " + amPm;
                time.setText(timepart);
            }

            TextView location = (TextView) findViewById(R.id.textView_location);
            location.setText(t.getLocDesc());

            if (t.getMot().equals("Walking"))
                radioWalk.setChecked(true);
            else if (t.getMot().equals("Cycling"))
                radioCycle.setChecked(true);
            else if (t.getMot().equals("Driving"))
                radioDrive.setChecked(true);
            mode_of_trnspt = t.getMot();

            hideControlsForViewTask();
            setTitle(R.string.open_from_notif);
        }
        else if (mode == R.string.completed_task) {
            String formattedDate;

            String taskDesc = getIntent().getExtras().getString("Task Description");

            if (!taskDesc.equals(""))
                reminderDesc.setText(taskDesc);

            TextView date = (TextView) findViewById(R.id.textView_date);
            TextView time = (TextView) findViewById(R.id.textView_time);

            /* Convert date to display format */
            Date convDate = (Date)getIntent().getSerializableExtra("Date");
            if (convDate != null) {
                String displayFormat = "MM/dd/yyyy HH:mm:ss";
                formattedDate = new SimpleDateFormat(displayFormat).format(convDate);
                String datePart = formattedDate.split(" ")[0];
                date.setText(datePart);
                String timepart = formattedDate.split(" ")[1];
                int hour = Integer.parseInt(timepart.split(":")[0]);
                String amPm = getAmPm(hour);
                if (hour > 12)
                    hour -= 12;
                else if (hour == 0)
                    hour = 12;
                timepart = (hour < 10 ? "0" + hour : hour) + ":" + timepart.split(":")[1] + " " + amPm;
                time.setText(timepart);
            }

            TextView location = (TextView) findViewById(R.id.textView_location);
            location.setText(getIntent().getExtras().getString("Location"));

            String mot = getIntent().getExtras().getString("Mot");
            if (mot.equals("Walking"))
                radioWalk.setChecked(true);
            else if (mot.equals("Cycling"))
                radioCycle.setChecked(true);
            else if (mot.equals("Driving"))
                radioDrive.setChecked(true);

            hideControlsForCompletedTask();
            setTitle(R.string.completed_task);
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

    private void hideControlsForCompletedTask() {
        buttonMarkDone.setVisibility(View.GONE);
        buttonGetDirections.setVisibility(View.GONE);
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

    private void hideControlsForEditTask() {
        buttonPickLocation.setEnabled(false);
        buttonAddReminder.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
