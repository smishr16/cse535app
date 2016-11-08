package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.FavouriteLocations;

/**
 * Created by Sooraj on 11/6/2016.
 */
public class AddLocation extends AppCompatActivity {

    static final int PLACE_PICKER_REQUEST = 1;
    static boolean DEBUG = false;
    FavouriteLocations location = new FavouriteLocations();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location);

        // Add Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText locationName = (EditText) findViewById(R.id.editText_locName);

        Button pickLocation = (Button) findViewById(R.id.button_pick_loc);
        pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch Location Picker
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(AddLocation.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        Button addButton = (Button) findViewById(R.id.button_save_loc);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(doValidations()){
                    location.setDescription(locationName.getText().toString());
                    DBManager.insertIntoFavLocations(location);
                    finish();
                }
            }

            private boolean doValidations(){
                if(locationName.getText().toString().isEmpty()){
                    Toast.makeText(AddLocation.this, "Provide Location Name", Toast.LENGTH_LONG).show();
                    return false;
                }
                if(location.getLatitude() == 0.0 && location.getLongitude() == 0.0){
                    Toast.makeText(AddLocation.this, "Pick a location", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                LatLng lat_long =  place.getLatLng();
                if(DEBUG)
                    Toast.makeText(AddLocation.this, lat_long.latitude + " " + lat_long.longitude, Toast.LENGTH_SHORT).show();
                location.setLatitude(lat_long.latitude);
                location.setLongitude(lat_long.longitude);
                TextView locationText = (TextView)findViewById(R.id.textView_locDesc);
                String locText = place.getName() + " " + place.getAddress();
                locationText.setText(locText);
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
