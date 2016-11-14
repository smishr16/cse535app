package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Task;

/**
 * Created by Sitanshu on 11/11/16.
 */

public class History extends AppCompatActivity {
    ArrayList<String> historyList = new ArrayList<>();
    ArrayList<Task> taskList = new ArrayList<>();

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

        // Click event for list view row
        ListView listView = (ListView) findViewById(R.id.listView_history);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(History.this, NewTaskActivity.class);
                intent.putExtra("Task Description", taskList.get(position).getDesc()); // string
                intent.putExtra("Lat", taskList.get(position).getLat()); //double
                intent.putExtra("Long", taskList.get(position).getLng()); // double
                intent.putExtra("Date", taskList.get(position).getTaskDate()); //DateTime
                intent.putExtra("Mot", taskList.get(position).getMot()); // string
                intent.putExtra("Mode", "Completed Reminder");
                startActivity(intent);
            }
        });
    }

    public void displayHistory() {
        final ListView lv = (ListView) findViewById(R.id.listView_history);
        historyList= DBManager.getAllPreviousTasks();
        if(historyList.size()>0){
            // Create a List from String Array elements
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, R.layout.simplerow, historyList);
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
