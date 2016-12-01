package asu.edu.cse535.locationawarereminder.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.Constants;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Properties;

/**
 * Created by Sooraj on 10/23/2016.
 */
public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Populate current values
        populateValues();

        final EditText editText_email = (EditText) findViewById(R.id.editText_email);
        final EditText editText_phone = (EditText) findViewById(R.id.editText_phone);
        final ArrayList<Properties> propertyList = new ArrayList<>();

        // Display up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button saveSettings = (Button) findViewById(R.id.button_save_settings);
        saveSettings.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(validateFields()) {
                    //TO:DO Save settings to Database
                    propertyList.add(new Properties(Constants.propertyEmail, editText_email.getText().toString()));
                    propertyList.add(new Properties(Constants.propertyPhone, editText_phone.getText().toString()));

                    DBManager.setProperties(propertyList);
                    finish();
                }
            }

            private boolean validateFields(){
                boolean isValidated = true;
                if(!TextUtils.isEmpty(editText_email.getText().toString()))
                    if(!Patterns.EMAIL_ADDRESS.matcher(editText_email.getText().toString()).matches()) {
                        Toast.makeText(Settings.this, "Provide valid Email", Toast.LENGTH_LONG).show();
                        isValidated = false;
                    }
                if(!TextUtils.isEmpty(editText_phone.getText().toString()))
                    if(!Patterns.PHONE.matcher(editText_phone.getText().toString()).matches()) {
                        Toast.makeText(Settings.this, "Provide valid Phone", Toast.LENGTH_LONG).show();
                        isValidated = false;
                    }
                return isValidated;
            }
        });
    }

    private void populateValues() {

        final EditText editText_email = (EditText) findViewById(R.id.editText_email);
        final EditText editText_phone = (EditText) findViewById(R.id.editText_phone);

        String[] propertyArray = {Constants.propertyEmail, Constants.propertyPhone};
        ArrayList<Properties> resultList = DBManager.getProperties(propertyArray);
        for(Properties row : resultList){
            if(row.getName().equals(Constants.propertyEmail))
                editText_email.setText(row.getValue());
            else if(row.getName().equals(Constants.propertyPhone))
                editText_phone.setText(row.getValue());
        }
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
