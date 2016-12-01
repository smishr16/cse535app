package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.FavouriteLocations;

/**
 * Created by Sooraj on 11/6/2016.
 */
public class MyLocations extends AppCompatActivity {

    ArrayList<FavouriteLocations> locationList = new ArrayList<>();
    ArrayList<String> locationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_locations);

        // Add Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Clear previous data
        locationList.clear();

        // Display locations from database
        displayLocations();

        // Click event for add location floating button
        FloatingActionButton addLoc = (FloatingActionButton) findViewById(R.id.add_my_location);
        addLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyLocations.this, AddLocation.class);
                startActivityForResult(intent, 1);
            }
        });

        // Click event for list view row
        ListView listView = (ListView) findViewById(R.id.listView_my_locations);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView locationDesc = (TextView) view.findViewById(R.id.location_name);
                String locDesc = locationDesc.getText().toString();
                Intent intent = new Intent(MyLocations.this, NewTaskActivity.class);
                intent.putExtra("LocationName", locDesc);
                intent.putExtra("Latitude", locationList.get(position).getLatitude());
                intent.putExtra("Longitude", locationList.get(position).getLongitude());
                intent.putExtra("Mode", R.string.add_task_from_fav);
                startActivity(intent);
            }
        });
    }

    /* On navigate back from add location screen */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        displayLocations();
    }

    /* Get locations saved in db table */
    private void getLocations() {
        locationList = DBManager.getFavLocations();
        locationNames = new ArrayList<>();
        for(FavouriteLocations locs : locationList) {
            locationNames.add(locs.getDescription() + " - Latitude : " + locs.getLatitude() + " Longitude : " + locs.getLongitude());
        }
    }

    /* Display locations in list view */
    private void displayLocations() {
        getLocations();
        ListView listView = (ListView) findViewById(R.id.listView_my_locations);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.my_location_item, R.id.location_name, locationNames);
        listView.setAdapter(adapter);
    }

    /* Click handler for location delete button */
    public void onLocDeleteClick(final View v) {
        LinearLayout ll = (LinearLayout) v.getParent();
        ListView lv = (ListView) ll.getParent();
        int position = lv.getPositionForView(ll);
        FavouriteLocations locToDelete = locationList.get(position);
        DBManager.deleteFromFavLocation(locToDelete);
        locationNames.clear();
        displayLocations();
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
    public void onResume() {
        super.onResume();

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
