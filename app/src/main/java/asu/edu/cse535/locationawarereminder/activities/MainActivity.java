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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.services.CurrentLocationService;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private EditText time;
    private TextView finalResult;
    public static ArrayList<String> t_list;
    Context c;

    SQLiteDatabase db;
    static double currLatitude;
    static double currLongitude;

    String task_type;
    int new_task_added = 0;

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
                startActivityForResult(intent,1);
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

        load_tasks_from_db();

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

    public void load_tasks_from_db() {
        String new_task;
        final ListView lv = (ListView) findViewById(R.id.listview);
        //Button btn1 = new Button(MainActivity.this);
        RelativeLayout rl = (RelativeLayout) findViewById (R.id.rel_layout_id);

        if(new_task_added==1){
            new_task = DBManager.getTaskname();
            Log.v("new_task ",new_task);
            t_list.add(new_task);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this,R.layout.simplerow, t_list);
            lv.setAdapter(arrayAdapter);
            return;

        }
        t_list= DBManager.get_all_tasks();
        if(t_list.size()>0){
            Log.v("task_count", Integer.toString(t_list.size()));
            // Create a List from String Array elements
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this,R.layout.simplerow, t_list);
            lv.setAdapter(arrayAdapter);
        }
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
                Toast.makeText(MainActivity.this, "Waiting for location. Make sure your location services are enabled and try again.", Toast.LENGTH_SHORT).show();
            else{
                Intent intent = new Intent(MainActivity.this, Nearby.class);
                intent.putExtra("currLat", currLatitude);
                intent.putExtra("currLong", currLongitude);
                startActivity(intent);
            }
        }
        else if(id == R.id.action_my_locations) {
            Intent intent = new Intent(MainActivity.this, MyLocations.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("back to mainactivity","back to mainactivity");
        Log.v("back to mainactivity","back to mainactivity");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String task_type=data.getStringExtra("task_type");
                if(task_type.equals("new_task")){
                    //Toast.makeText(MainActivity.this, "task added", Toast.LENGTH_SHORT).show();
                    new_task_added=1;
                    load_tasks_from_db();
                    new_task_added=1;
                    //AsyncTaskRunner runner = new AsyncTaskRunner( c );
                    //runner.execute();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListView lv = (ListView) findViewById(R.id.listview);
        ArrayList<String> task_list = DBManager.get_all_tasks();
        if(task_list.size()>0) {
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, task_list);
            lv.setAdapter(arrayAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, CurrentLocationService.class));
    }
}
