package asu.edu.cse535.locationawarereminder.activities;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;
import asu.edu.cse535.locationawarereminder.services.CurrentLocationService;

public class MainActivity extends AppCompatActivity {

    UserCustomAdapter arrayAdapter;
    ArrayList<Task> task_list = new ArrayList<>();
    ArrayList<String> task_desc_list = new ArrayList<>();

    SQLiteDatabase db;
    DBManager dbManager;
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
        dbManager = new DBManager(MainActivity.this);
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

        // Load tasks list
        load_tasks_from_db();

        // Call Current Location Service
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

        // Attach event for list view item click
        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String t_name = (String) parent.getItemAtPosition(position);
                int task_id = task_list.get(position).getTaskId();
                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                intent.putExtra("Mode", R.string.edit_task);
                intent.putExtra("task_name", t_name);
                intent.putExtra("task_id", task_id);
                startActivity(intent);
            }
        });
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
        else if (id == R.id.action_history){
           Intent intent = new Intent(MainActivity.this, History.class);
           startActivity(intent);
       }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        load_tasks_from_db();
    }

    @Override
    public void onBackPressed() {
        arrayAdapter.notifyDataSetChanged();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, CurrentLocationService.class));
    }

    /* Method which refreshes the screen with latest tasks */
    public void load_tasks_from_db() {
        task_list.clear();
        task_desc_list.clear();

        task_list = dbManager.get_all_tasks();

        for(Task task : task_list){
            String taskDesc = task.getDesc();
            task_desc_list.add(taskDesc);
        }

        arrayAdapter = new UserCustomAdapter(this, R.layout.simplerow, task_desc_list, task_list);
        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(arrayAdapter);
    }

    /* Custom adapter used for list view */
    public class UserCustomAdapter extends ArrayAdapter<String>  {
        private final Activity context;
        private int layoutResourceId;
        private ArrayList<String> task_desc_list = new ArrayList<>();
        private ArrayList<Task> task_list = new ArrayList<>();

        public UserCustomAdapter(Activity context, int layoutResourceId, ArrayList<String> task_desc_list, ArrayList<Task> task_list) {
            super(context, layoutResourceId, task_desc_list);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.task_desc_list = task_desc_list;
            this.task_list = task_list;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(layoutResourceId, null, true);
            TextView taskDesc = (TextView) rowView.findViewById(R.id.rowTextView);
            taskDesc.setText(task_desc_list.get(position));

            Button button_mark_done = (Button) rowView.findViewById(R.id.button_mark_done_main);
            Button button_delete = (Button) rowView.findViewById(R.id.button_delete_task_main);

            final int task_id = task_list.get(position).getTaskId();

            button_mark_done.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dbManager.updateTaskStatus(task_id, "Completed");
                    load_tasks_from_db();
                }
            });
            button_delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dbManager.updateTaskStatus(task_id, "Removed");
                    load_tasks_from_db();
                }
            });

            return rowView;
        }
    }
}
