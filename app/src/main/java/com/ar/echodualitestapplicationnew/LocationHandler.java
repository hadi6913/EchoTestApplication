package com.ar.echodualitestapplicationnew;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LocationHandler extends Service {
    NotificationCompat.Builder mBuilder;
    Callbacks activity;
    MainActivity mainActivity;
    private final IBinder mBinder = new LocalBinder();
    private LocationListener locationListener = null;
    private LocationManager locationManager = null;


    @Override
    public void onCreate() {
        init();
    }

    private boolean temp = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private void initListener() {
        try{
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    if (activity != null){
                        activity.successSignal();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    //nothing
                }

                @Override
                public void onProviderEnabled(String s) {
                    if (activity != null){
                        activity.enabledProvider();
                    }
                }

                @Override
                public void onProviderDisabled(String s) {
                    if (activity != null){
                        activity.disabledProvider();
                    }
                }
            };
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void registerClient(MainActivity mainActivity) {
        this.activity = (Callbacks) mainActivity;
        this.mainActivity = mainActivity;
    }


    public class LocalBinder extends Binder {
        public LocationHandler getServiceInstance() {
            return LocationHandler.this;
        }
    }

    public interface Callbacks {
        void successSignal();
        void disabledProvider();
        void enabledProvider();
    }

    public void init() {
        try {
            initListener();
            locationManager = (LocationManager) getSystemService(getApplication().LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (locationListener != null)
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


