package asu.edu.cse535.locationawarereminder.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import asu.edu.cse535.locationawarereminder.R;

/**
 * Created by Sooraj on 10/31/2016.
 */
public class LocationListenerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Inside onBind method");
        return null;
    }

    @Override
    public void onCreate() {
        System.out.println("Inside create method");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Inside onstartcommand method");
        int taskId = intent.getExtras().getInt("task_id");
        String desc = intent.getExtras().getString("task_desc");
        double lat = intent.getExtras().getDouble("lat");
        double lng = intent.getExtras().getDouble("lng");

        createProximityAlert(taskId, desc, lat, lng);

        return START_REDELIVER_INTENT;
    }

    private void createProximityAlert(int task_id, String desc, double lat, double lng) {
        LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        Intent intent = new Intent("lar.proximityalert");           //Custom Action
        intent.putExtra("taskId", task_id);
        intent.putExtra("taskDesc", desc);
        PendingIntent pi = PendingIntent.getBroadcast(LocationListenerService.this, task_id, intent, 0);

        float radius = 100;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locMgr.addProximityAlert(lat, lng, radius, -1, pi);
        IntentFilter filter = new IntentFilter("lar.proximityalert");
        registerReceiver(new ProximityIntentReceiver(), filter);
        /*PendingIntent piRemove = PendingIntent.getBroadcast(LocationListenerService.this, task_id, intent, 0);
        locMgr.removeProximityAlert(piRemove);*/
    }

    public class ProximityIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String key = LocationManager.KEY_PROXIMITY_ENTERING;
            int task_id = intent.getExtras().getInt("taskId");
            String desc = intent.getExtras().getString("taskDesc");
            Boolean entering = intent.getBooleanExtra(key, false);
            if (entering) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent notificationIntent = PendingIntent.getActivity(context, 0, intent, 0);
                Notification.Builder builder = new Notification.Builder(LocationListenerService.this);

                builder.setAutoCancel(true);
                builder.setContentTitle("LAR");
                builder.setContentText("You have open reminders for this location");
                builder.setSmallIcon(R.drawable.common_ic_googleplayservices);
                builder.setContentIntent(notificationIntent);
                builder.setOngoing(false);
                builder.setSubText("Description : " + desc);   //API level 16
                notificationManager.notify(task_id, builder.build());

                //PendingIntent pi = PendingIntent.getBroadcast(LocationListenerService.this, task_id, intent, 0);
                //LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(LocationListenerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationListenerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //locMgr.removeProximityAlert(removePi);
                context.unregisterReceiver(this);
            }
        }
    }
}
