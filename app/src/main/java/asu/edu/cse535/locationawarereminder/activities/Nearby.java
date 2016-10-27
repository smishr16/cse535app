package asu.edu.cse535.locationawarereminder.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import asu.edu.cse535.locationawarereminder.R;

/**
 * Created by Sooraj on 10/26/2016.
 */
public class Nearby extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;
    private static ArrayList<String> placesList = new ArrayList<>();
    private static ArrayList<String> placeIconList = new ArrayList<>();
    static double currLatitude;
    static double currLongitude;
    private static boolean DEBUG = false;
    HashMap<String, String> locationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_places);

        currLatitude = getIntent().getExtras().getDouble("currLat");
        currLongitude = getIntent().getExtras().getDouble("currLong");
        if(DEBUG)
            Toast.makeText(this, currLatitude + " " + currLongitude, Toast.LENGTH_SHORT).show();

        // Add Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Nearby Place list and calls displayList()
        placesList.clear();
        placeIconList.clear();
        new ConnectToService().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        // Handle onclick of place
        ListView listView = (ListView) findViewById(R.id.listView_nearby_places);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = ((TextView) view.findViewById(R.id.item_name)).getText().toString();
                if(DEBUG)
                    Toast.makeText(Nearby.this, selected, Toast.LENGTH_SHORT).show();
                String latlng = locationMap.get(selected);
                float lat = Float.parseFloat(latlng.split(",")[0]);
                float lng = Float.parseFloat(latlng.split(",")[1]);
                //String uri = String.format(Locale.ENGLISH,"geo:%d,%d?z=%d&q=%f,%f (%s)", 0, 0, 10, lat, lng, Uri.encode(selected));
                String uri = String.format(Locale.ENGLISH,"geo:%f,%f?z=%d", lat, lng, 18);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
    }

    // Display place list
    private void displayList() {

        CustomList adapter = new  CustomList(this, placesList, placeIconList);
        ListView listView = (ListView) findViewById(R.id.listView_nearby_places);
        listView.setAdapter(adapter);

        /*ListView listView = (ListView) findViewById(R.id.listView_nearby_places);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.nearby_place_item, R.id.item_name, placesList);
        listView.setAdapter(adapter);*/
    }

    // AsyncTask to get nearby palces
    private class ConnectToService extends AsyncTask<String, Long, Void> {

        String nextToken = "";

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected Void doInBackground(String... params) {
            try{
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?";
                urlString += "location=" + currLatitude + "," + currLongitude;
                urlString += "&radius=5000";
                urlString += "&key=AIzaSyC54ZbYtQCj5KYdYo7hdawgFDCQXZyoErI";
                if(nextToken != "")
                    urlString += "&pagetoken=" + nextToken;
                nextToken = "";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser myparser = xmlFactoryObject.newPullParser();
                myparser.setInput(in, null);
                int event = myparser.getEventType();
                boolean isLocationTag = false;
                String placeName = ""; String lat = ""; String lng = "";
                while (event != XmlPullParser.END_DOCUMENT) {
                    String name = myparser.getName();
                    switch (event) {
                        case XmlPullParser.START_TAG:
                            if (name.equals("name")){
                                placeName = myparser.nextText();
                                placesList.add(placeName);
                                isLocationTag = true;
                            }
                            if(name.equals("icon"))
                                placeIconList.add(myparser.nextText());
                            if(name.equals("next_page_token"))
                                nextToken = myparser.nextText();
                            if(name.equals("error_message"))
                                Toast.makeText(Nearby.this, myparser.nextText(), Toast.LENGTH_LONG).show();
                            if(name.equals("lat") && isLocationTag)
                                lat = myparser.nextText();
                            if(name.equals("lng") && isLocationTag){
                                lng = myparser.nextText();
                                locationMap.put(placeName, lat + "," + lng);
                                isLocationTag = false;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }
                    event = myparser.next();
                }
                br.close();
                in.close();
                urlConnection.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... value){}

        @Override
        protected void onPostExecute(final Void unused){
            displayList();
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
