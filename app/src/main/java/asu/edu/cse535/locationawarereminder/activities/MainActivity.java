package asu.edu.cse535.locationawarereminder.activities;
import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
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
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.services.CurrentLocationService;

public class MainActivity extends AppCompatActivity {
    ListView userList;
    //UserCustomAdapter userAdapter;
    //ArrayList<User> userArray = new ArrayList<User>();
   // private Button button;
   // private EditText time;
    //private TextView finalResult;
    // int task_status_changed=0;
    ServiceConnection serve;
    ListView lv;
    RelativeLayout rl;

    UserCustomAdapter arrayAdapter;
   // Context c;
    int task_status_changed = 0;


    int edit_task_added = 0;
    EditText reminderDesc;

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

    public  void load_tasks_from_db() {
        String new_task;
        lv = (ListView) findViewById(R.id.listview);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // String t_name= ((TextView)findViewById(position)).getText().toString();
                String t_name=  (String) parent.getItemAtPosition(position);
                //String t_name = c.getText().toString();


                //Log.v("t_name", ((TextView) view.getRootView().findViewById(R.id.rowTextView)).getText().toString());

                Toast.makeText(MainActivity.this, t_name, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                intent.putExtra("Mode", R.string.edit_task);
                intent.putExtra("task_name", t_name);
                startActivityForResult(intent, 2);

            }
        });
        //Button btn1 = new Button(MainActivity.this);
        rl = (RelativeLayout) findViewById(R.id.rel_layout_id);

        if (new_task_added == 1) {
            new_task = DBManager.getTaskname();
            t_list.add(new_task);
            final UserCustomAdapter  arrayAdapter = new UserCustomAdapter(this, R.layout.simplerow, t_list);
            lv.setAdapter(arrayAdapter);
            return;

        }



        if (edit_task_added == 1) {
            reminderDesc = (EditText) findViewById(R.id.editText_desc);
            Log.v("DBManager.getTaskname()",DBManager.getTaskname());
            t_list.add(DBManager.getTaskname());
            Log.v("task_details.getDesc()",DBManager.task_details.getDesc());
            t_list.remove(DBManager.task_details.getDesc());
            Log.v("task_cnt_edit",Integer.toString(DBManager.getTaskCount()));
            final UserCustomAdapter  arrayAdapter = new UserCustomAdapter(this, R.layout.simplerow, t_list);
            lv.setAdapter(arrayAdapter);
            return;
        }
        if(task_status_changed == 1)
        {
            Log.v("inside restart","inside restart");
            task_status_changed =0;
            //   this.recreate();

        }
        //arrayAdapter.notifyDataSetChanged();

        //rl.addView(btn);
        //ll.addView(btn1);


        //t_list.clear();
        //arrayAdapter.notifyDataSetChanged();
        //this.recreate();
        t_list = DBManager.get_all_tasks();
        Log.v("task_cnt",Integer.toString(DBManager.getTaskCount()));
        for ( int i=0;i< t_list.size();i++){
            Log.v("task is", t_list.get(i));
        }
        if (t_list.size() > 0) {
            Log.v("task_count_final", Integer.toString(t_list.size()));


            // Create a List from String Array elements
            final List<String> fruits_list = new ArrayList<String>(t_list);


            arrayAdapter = new UserCustomAdapter(this, R.layout.simplerow, t_list);
            //rl.addView(btn);
            //ll.addView(btn1)
            //arrayAdapter.notifyDataSetChanged();
            lv.setAdapter(arrayAdapter);

        }
    }


    public class UserCustomAdapter extends ArrayAdapter<String>  {
        String t_name;
        Context context;
        int layoutResourceId;
        ArrayList<String> data = new ArrayList<String>();

        public UserCustomAdapter(Context context, int layoutResourceId,
                                 ArrayList<String> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View row = convertView;
            asu.edu.cse535.locationawarereminder.activities.UserCustomAdapter.UserHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new asu.edu.cse535.locationawarereminder.activities.UserCustomAdapter.UserHolder();
                holder.textName = (TextView) row.findViewById(R.id.rowTextView);
                //holder.textAddress = (TextView) row.findViewById(R.id.textView2);
                //holder.textLocation = (TextView) row.findViewById(R.id.textView3);
                holder.Mark_done = (Button) row.findViewById(R.id.button1);
                holder.btnDelete = (Button) row.findViewById(R.id.button2);
                row.setTag(holder);
            } else {
                holder = (asu.edu.cse535.locationawarereminder.activities.UserCustomAdapter.UserHolder) row.getTag();
            }
            String user = data.get(position);
            holder.textName.setText(user);
            t_name = holder.textName.getText().toString();
            //holder.textAddress.setText(user.getAddress());
            //holder.textLocation.setText(user.getLocation());
            holder.Mark_done.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub,
                    Toast.makeText(context, "Mark_done button Clicked",
                            Toast.LENGTH_LONG).show();
                    DBManager.Update_task(t_name);
                    task_status_changed = 1;
                    //MainActivity main = new MainActivity();
                    //load_tasks_from_db();


                }
            });
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Log.i("Delete Button Clicked", "**********");
                    Toast.makeText(context, "Delete button Clicked",
                            Toast.LENGTH_LONG).show();
                    Log.v("t_name_Del",t_name);
                    DBManager.DeleteTask(t_name);
                    task_status_changed = 1;

                    //MainActivity main = new MainActivity();
                    //load_tasks_from_db();


                }
            });
            return row;

        }

        class UserHolder {
            TextView textName;
            Button Mark_done;
            Button btnDelete;
        }
    }

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

        else if (id == R.id.action_history){
                       Intent intent = new Intent(MainActivity.this, History.class);
                       startActivity(intent);
                   }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("back to mainactivity", "back to mainactivity");
        Log.v("back to mainactivity", "back to mainactivity");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == 2) {
            Log.v("request code", Integer.toString(requestCode));
            if (resultCode == RESULT_OK) {
                String task_type = data.getStringExtra("task_type");
                Log.v("task_typ1", task_type);
                if (task_type.equals("new_task")) {
                    Toast.makeText(MainActivity.this, "task added", Toast.LENGTH_SHORT).show();
                    new_task_added = 1;
                    Log.v("loading tasks from db", "load");
                    load_tasks_from_db();
                    //AsyncTaskRunner runner = new AsyncTaskRunner( c );
                    //runner.execute();


                }
                if (task_type.equals("edit_task")) {
                    Log.v("task_typ", task_type);
                    Toast.makeText(MainActivity.this, "task added", Toast.LENGTH_SHORT).show();
                    edit_task_added = 1;
                    Log.v("loading tasks from db", "load");
                    load_tasks_from_db();
//                    //AsyncTaskRunner runner = new AsyncTaskRunner( c );
//                    //runner.execute();
//
//
//
//                }

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
             UserCustomAdapter arrayAdapter = new UserCustomAdapter(this, R.layout.simplerow, task_list);
            lv.setAdapter(arrayAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        t_list.clear();
        arrayAdapter.notifyDataSetChanged();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, CurrentLocationService.class));
    }
}
