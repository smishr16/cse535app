package asu.edu.cse535.locationawarereminder.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.services.CurrentLocationService;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    static double currLatitude;
    static double currLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Attach event for Plus Button click
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                intent.putExtra("Mode", R.string.new_task);
                startActivity(intent);
            }
        });

        // Create database
        DBManager dbManager = new DBManager(MainActivity.this);
        dbManager.initializeDB(this.getApplicationContext().getPackageName());
        db = dbManager.getAppDataBase();

        // Call Current Location Service
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                currLatitude = msg.getData().getDouble("latitude");
                currLongitude = msg.getData().getDouble("longitude");
            }
        };

        Intent serviceIntent;
        serviceIntent = new Intent(MainActivity.this.getBaseContext(), CurrentLocationService.class);
        startService(serviceIntent);
        ServiceConnection serve = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                CurrentLocationService curLocService = ((CurrentLocationService.LocalBinder) service).getInstance();
                curLocService.setHandler(handler);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(serviceIntent,serve, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        }
        else if(id == R.id.action_nearby_places) {
            if(currLatitude == 0.0 && currLongitude == 0.0)
                Toast.makeText(MainActivity.this, "Waiting for location. Try again.", Toast.LENGTH_SHORT).show();
            else{
                Intent intent = new Intent(MainActivity.this, Nearby.class);
                intent.putExtra("currLat", currLatitude);
                intent.putExtra("currLong", currLongitude);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
