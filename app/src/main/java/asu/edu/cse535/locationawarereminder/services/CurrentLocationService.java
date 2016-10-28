package asu.edu.cse535.locationawarereminder.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

/**
 * Created by Sooraj on 10/24/2016.
 */
public class CurrentLocationService extends Service {

    private final IBinder curLocBinder = new LocalBinder();
    private static boolean DEBUG = false;
    private Handler curLocHandler = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        getCurrentLocation();
        return curLocBinder;
    }

    public class LocalBinder extends Binder
    {
        public CurrentLocationService getInstance()
        {
            return CurrentLocationService.this;
        }
    }

    public void setHandler(Handler handler)
    {
        curLocHandler = handler;
    }

    private void sendMessageToMainActivity(Location location) {
        if (curLocHandler != null) {
            Message msg = curLocHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putDouble("latitude", location.getLatitude());
            b.putDouble("longitude", location.getLongitude());
            msg.setData(b);
            curLocHandler.sendMessage(msg);
        }
    }

    private void getCurrentLocation(){
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Get last Known Location
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        sendMessageToMainActivity(lastKnownLocation);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                sendMessageToMainActivity(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            for(int i =0; i<5; i++)
                System.out.println("*********NO PERMISSION**********");
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
    }
}
