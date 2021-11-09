package com.female.safetyapp.femalesafetyapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.female.safetyapp.femalesafetyapplication.App.CHANNEL_ID;

public class MainActivity extends AppCompatActivity
        implements MainFragment.OnFragmentInteractionListener, NewContactFragment.OnFragmentInteractionListener {

    int PERMISSION_ID = 44;

    static String MESSAGE;

    MainFragment mainFragment;
    NewContactFragment newContactFragment;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    MyBroadcastReceiver myBroadcastReceiver;

    TextView textViewGPSRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewGPSRequired = findViewById(R.id.textView_label_gps_required);

        Intent safetyIntent = new Intent(this, FemaleSafetyAppService.class);
        startService(safetyIntent);

        mainFragment = new MainFragment(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.mainactivity_frag_cont, mainFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();

        myBroadcastReceiver = new MyBroadcastReceiver(this);
        //registerReceiver(myBroadcastReceiver, new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));
        registerReceiver(myBroadcastReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        if (!checkPermissions()) requestPermissions();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.textView_label_gps_required).setVisibility(View.VISIBLE);
                }
            });
        }

        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onShake(int count) {
                if (checkPermissions()) {
                    MESSAGE = "This is a test!!\nI'm in trouble, respond ASAP!\nLocation: " + gpsToGeoLocation(getGPSCoord());
                } else requestPermissions();
                SmsManager smsManager = SmsManager.getDefault();
                final Intent callIntent = new Intent(Intent.ACTION_CALL);
                ExecutorService callExecutorService = Executors.newSingleThreadExecutor();
                for (Contact contact : MainFragment.contactsArray) {
                    smsManager.sendTextMessage(contact.getContactNumber(), null, "" + MESSAGE, null, null);
                    callIntent.setData(Uri.parse("tel:" + contact.getContactNumber()));
                    callExecutorService.submit(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(callIntent);
                            try {
                                Thread.sleep(17000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }));
                }
                callExecutorService.shutdown();
            }
        });

    }


    @Override
    public void onBackPressed() {
        
        if (mainFragment.contactsList.getCheckedItemCount() > 0) {
            for (int i = 0; i < mainFragment.contactsList.getCheckedItemCount(); i++) {
                mainFragment.contactsList.setItemChecked(i, false);
            }
            mainFragment.contactsList.clearChoices();
        } else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myBroadcastReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    protected void onStop() {
        //if (myBroadcastReceiver!=null) unregisterReceiver(myBroadcastReceiver);
        super.onStop();
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.PROCESS_OUTGOING_CALLS,
                },
                PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0) {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[5] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[6] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[7] == PackageManager.PERMISSION_GRANTED) {
                    MESSAGE = "This is a test!!\nI'm in trouble, respond ASAP!\nLocation: " + gpsToGeoLocation(getGPSCoord());
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private double[] getGPSCoord() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location location = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            location = lm.getLastKnownLocation(providers.get(i));
            if (location != null) break;
        }

        double[] gps = new double[2];
        if (location != null) {
            gps[0] = location.getLatitude();
            gps[1] = location.getLongitude();
        }
        return gps;
    }

    private String gpsToGeoLocation(double[] gps) {
        String address = "";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(gps[0], gps[1], 1);
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        } catch (IOException | IndexOutOfBoundsException e) {
            Toast.makeText(this, "Please Turn Geo Location on", Toast.LENGTH_SHORT).show();
        }
        return address;
    }

    @Override
    public void onMainFragInteraction(long viewId) {
        if (viewId == R.id.button_add_contact || viewId == R.id.empty_list_add_button) {
            newContactFragment = new NewContactFragment(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.mainactivity_frag_cont, newContactFragment);
            transaction.addToBackStack("newContactFragment");
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.commit();
        }
        if (viewId == R.id.button_delete_contact) {
            mainFragment.deleteContact();
        }
    }

    @Override
    public void onNewContactFragInteraction(long viewId) {

        if (viewId == R.id.button_save) {
            boolean saved = newContactFragment.saveContact();
            if (!saved) return;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack("newContactFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction()
                    .remove(newContactFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit();
        } else if (viewId == R.id.button_add_from_phonebook) {

            /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT, ContactsContract.Contacts.CONTENT_URI);
            //intent.setDataAndType(ContactsContract.Contacts.CONTENT_URI, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            startActivityForResult(intent, 1);*/
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                            phones.moveToFirst();
                            String contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String contactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                            newContactFragment.setNameField(contactName);
                            newContactFragment.setNumberField(contactNumber);
                        }
                    }
                }
        }
    }

    public static class MyBroadcastReceiver extends BroadcastReceiver {
        Context c;

        public MyBroadcastReceiver() {}

        public MyBroadcastReceiver(Context context) {
            this.c = context;
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        endCall(context);
                    }
                }).start();
            }
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                String notificationTitle;
                int notificationColor;
                LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (c instanceof MainActivity) ((MainActivity)c).findViewById(R.id.textView_label_gps_required).setVisibility(View.GONE);
                    notificationTitle = "Safety App Running";
                    notificationColor = 0xff00574B;
                } else {
                    if (c instanceof MainActivity) ((MainActivity)c).findViewById(R.id.textView_label_gps_required).setVisibility(View.VISIBLE);
                    notificationTitle = "GPS Turned Off";
                    notificationColor = 0xffFF5722;
                }
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                Intent mainActivityIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(mainActivityIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification safetyServiceNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(notificationTitle)
                        .setContentText("Female Safety Application")
                        .setSmallIcon(R.drawable.helping_hand_240px)
                        .setContentIntent(pendingIntent)
                        .setColor(notificationColor)
                        .build();
                notificationManager.notify(App.NOTIFICATION_ID, safetyServiceNotification);
            }
        }

        private void endCall(Context context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                Object telephonyService = m.invoke(tm);

                c = Class.forName(telephonyService.getClass().getName());
                m = c.getDeclaredMethod("endCall");
                m.setAccessible(true);
                m.invoke(telephonyService);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

class Contact {
    public String contactName;
    public String contactNumber;

    public Contact() {
        // default constructor for Datasnapshot.getValue(Contact.class)
    }

    Contact(String contactName, String contactNumber) {
        this.contactName = contactName;
        this.contactNumber = contactNumber;
    }

    String getContactName() {
        return contactName;
    }

    String getContactNumber() {
        return contactNumber;
    }
}
