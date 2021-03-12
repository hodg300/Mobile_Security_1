package com.example.mysecurityapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private Button main_BTN_login;
    private EditText name_editText;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        init();

        main_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCondition();
            }
        });
    }

    private void init() {
        name_editText = findViewById(R.id.name_editText);
        main_BTN_login = findViewById(R.id.main_BTN_login);
    }


    private void checkCondition() {
        String input = name_editText.getText().toString();

        if (("" + memoryStorage()).equals(input)) {
            openNewActivity();
        } else if (batteryLevel() == 23) {
            openNewActivity();
        } else if (("" + currentBrightness()).equals(input)) {
            openNewActivity();
        } else if (("" + totalContacts()).equals(input)) {
            openNewActivity();
        } else if (checkNumber(input)) {
            openNewActivity();
        }

        getLocation();
    }



    // Checks the count of free RAM memory available on the phone
    private long memoryStorage() {
        ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memory);
        System.out.println(memory.availMem / 1048576L);

        return memory.availMem / 1048576L;
    }

    // Checks the percentage of battery in the phone
    private int batteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }

    // Checks the current brightness in the phone ->  1.0 - 255.0
    private float currentBrightness() {
        float curBrightnessValue = 0;
        try {
            curBrightnessValue = Settings.System.getInt(
                    getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            System.out.println(curBrightnessValue);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return curBrightnessValue;
    }

    // Get the amount of contacts in the phone
    private int totalContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        System.out.println(cursor.getCount());
        return cursor.getCount();
    }

    // Checks if the phone number appears in the contacts
    private boolean checkNumber(String phoneNumber) {
        
        if(phoneNumber.isEmpty()) {
            return false;
        }

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = getApplicationContext().getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);

        return cur.moveToFirst();
    }

    private void openNewActivity() {
        Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
        startActivity(intent);
    }

    //Check the location of the phone
    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MainActivity.this);

    }



    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                         ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS
            }, 100);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        System.out.println(latitude + "  " + longitude );

        if(latitude == 32.0845428 && longitude == 34.8957159) {
            openNewActivity();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        Log.i("TAG", "onPause, done");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
    }
}