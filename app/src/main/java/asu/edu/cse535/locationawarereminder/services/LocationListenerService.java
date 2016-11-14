package asu.edu.cse535.locationawarereminder.services;

import android.Manifest;
import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.ArrayList;

import asu.edu.cse535.locationawarereminder.R;
import asu.edu.cse535.locationawarereminder.activities.NewTaskActivity;
import asu.edu.cse535.locationawarereminder.database.Constants;
import asu.edu.cse535.locationawarereminder.database.DBManager;
import asu.edu.cse535.locationawarereminder.database.Properties;
import asu.edu.cse535.locationawarereminder.database.Task;
import android.util.Log;



/**
 * Created by Sooraj on 10/31/2016.
 */
public class LocationListenerService extends Service {

    final String TAG = "LAR";

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
            String phoneNumber;
            String emailId;
            int task_id = intent.getExtras().getInt("taskId");
            String desc = intent.getExtras().getString("taskDesc");
            Boolean entering = intent.getBooleanExtra(key, false);
            if (entering) {
                //Check if task status is still "Created"
                Task t = DBManager.getTaskByTaskId(task_id);
                if(!t.getStatus().equals("Completed") && !t.getStatus().equals("Removed")){
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent openTaskIntent = new Intent(LocationListenerService.this, NewTaskActivity.class);
                    openTaskIntent.putExtra("taskid", task_id);
                    openTaskIntent.putExtra("Mode", R.string.open_from_notif);
                    openTaskIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent notificationIntent = PendingIntent.getActivity(context, task_id, openTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(LocationListenerService.this);

                    builder.setAutoCancel(true);
                    builder.setContentTitle("LAR");
                    builder.setContentText("You have open reminders for this location");
                    builder.setSmallIcon(R.drawable.common_ic_googleplayservices);
                    builder.setContentIntent(notificationIntent);
                    builder.setOngoing(false);
                    builder.setSubText("Description : " + desc);   //API level 16
                    notificationManager.notify(task_id, builder.build());

                    String message = "You have open reminders for this location" + "/n" +
                            "Description: " + desc + "/n";

                    //Check if phone number is there and if so send SMS
                    phoneNumber = getPhoneNumber();
                    if (!phoneNumber.equals("")) {
                        sendSMS(phoneNumber, message);
                    }

                    //Check if email is there and if so send Email
                    emailId = getEmail();
                    if (!emailId.equals("")) {
                        sendEmail(emailId, message);
                    }

                    //PendingIntent pi = PendingIntent.getBroadcast(LocationListenerService.this, task_id, intent, 0);
                    //LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(LocationListenerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationListenerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                //locMgr.removeProximityAlert(removePi);
                context.unregisterReceiver(this);
            }
        }
    }

    private String getPhoneNumber(){
        String phoneNumber = "";
        String[] propertyArray = {Constants.propertyEmail, Constants.propertyPhone};
        ArrayList<Properties> resultList = DBManager.getProperties(propertyArray);
        for (Properties row : resultList) {
            if (row.getName().equals(Constants.propertyPhone))
                phoneNumber = row.getValue();
        }
        return phoneNumber;
    }

    private String getEmail(){
        String email = "";
        String[] propertyArray = {Constants.propertyEmail, Constants.propertyPhone};
        ArrayList<Properties> resultList = DBManager.getProperties(propertyArray);
        for (Properties row : resultList) {
            if (row.getName().equals(Constants.propertyEmail))
                email = row.getValue();
        }
        return email;
    }


    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        BroadcastReceiver sendBroadcastReceiver;
        BroadcastReceiver deliveryBroadcastReceiver;
        PendingIntent deliveredPI;
        PendingIntent sentPI;

        sentPI= PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);


        deliveredPI= PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED),0);
        //---SMS has been sent---
        sendBroadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.v(TAG, "SMS sent");
                        //Toast.makeText(getBaseContext(), "SMS Sent",
                                //Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.v(TAG, "Generic failure");
                        //Toast.makeText(getBaseContext(), "Generic Failure",
                                //Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.v(TAG, "No Service");
                        //Toast.makeText(getBaseContext(), "No Service",
                                //Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.v(TAG, "Null PDU");
                        //Toast.makeText(getBaseContext(), "Null PDU",
                                //Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.v(TAG, "Radio Off");
                        //Toast.makeText(getBaseContext(), "Radio Off",
                                //Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
        //---when the SMS has been delivered---
        deliveryBroadcastReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.v(TAG, "SMS Delivered");
                        //Toast.makeText(getBaseContext(), "SMS delivered",
                                //Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.v(TAG, "SMS Not Delivered");
                        //Toast.makeText(getBaseContext(), "SMS Not Delivered",
                                //Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        unregisterReceiver(sendBroadcastReceiver);
        unregisterReceiver(deliveryBroadcastReceiver);
    }

    public void sendEmail(String recipient, String textMessage) {
        String subject = "Alert from LAR App";
        /*
        java.util.Properties properties = new java.util.Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("notificationcse535app@gmail.com", "cse535app");
            }
        });
        */
        RetreiveFeedTask task = new RetreiveFeedTask();
        task.execute(subject, recipient, textMessage);
    }
    class RetreiveFeedTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String subject = params[0];
            String recipient = params[1];
            String textMessage = params[2];
            java.util.Properties properties = new java.util.Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "465");
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("notificationcse535app@gmail.com", "cse535app");
                }
            });

            try{
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("notificationcse535app@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setContent(textMessage, "text/html; charset=utf-8");
                Transport.send(message);
                Log.v(TAG, "Email Delivered");
            } catch(MessagingException e) {
                e.printStackTrace();
                Log.v(TAG, "Email Not Delivered");
            } catch(Exception e) {
                e.printStackTrace();
                Log.v(TAG, "Email Not Delivered");
            }
            return null;
        }
    }
}
