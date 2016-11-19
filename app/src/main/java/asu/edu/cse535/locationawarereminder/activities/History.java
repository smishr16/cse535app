package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;

/**
 * Created by Sitanshu on 11/11/16.
 */

public class History extends AppCompatActivity {
    ArrayList<String> taskNameList = new ArrayList<>();
    ArrayList<String> historyList = new ArrayList<>();
    ArrayList<Task> taskList = new ArrayList<>();
    ArrayList<String> addressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // Add Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Clear previous data
        historyList.clear();

        // Display locations from database
        displayHistory();

        // Initialize taskList
        taskList = DBManager.getCompletedTasks();

        // Click event for list view row
        ListView listView = (ListView) findViewById(R.id.listView_history);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView historyDesc = (TextView) view.findViewById(R.id.history_name);
                Intent intent = new Intent(History.this, NewTaskActivity.class);
                intent.putExtra("Task Description", taskList.get(position).getDesc());
                intent.putExtra("Lat", taskList.get(position).getLat());
                intent.putExtra("Long", taskList.get(position).getLng());
                intent.putExtra("Location", taskList.get(position).getLocDesc());
                intent.putExtra("Date", taskList.get(position).getTaskDate());
                intent.putExtra("Mot", taskList.get(position).getMot());
                intent.putExtra("Mode", R.string.completed_task);
                startActivity(intent);
            }
        });
    }

    public void displayHistory() {
        final ListView lv = (ListView) findViewById(R.id.listView_history);
        taskNameList = DBManager.getCompletedTaskNames();
        addressList = DBManager.getCompletedTaskAddress();
        if (taskNameList.size()>0 && addressList.size()>0 && taskNameList.size()==addressList.size()){
            for (int i=0; i<taskNameList.size(); i++){
                String temp = "Task: ";
                String address = "Address: ";
                temp = temp + taskNameList.get(i) + System.getProperty("line.separator");
                address = address + addressList.get(i);
                temp = temp + address;
                historyList.add(i, temp);
            }
        }

        if(historyList.size()>0){
            // Create a List from String Array elements
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<> (this, R.layout.history_item, R.id.history_name, historyList);
            lv.setAdapter(arrayAdapter);
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
