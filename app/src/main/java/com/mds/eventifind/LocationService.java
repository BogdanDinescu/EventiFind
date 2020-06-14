package com.mds.eventifind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;


import static android.content.Context.LOCATION_SERVICE;

public class LocationService implements LocationListener {

    private Location currentLocation = null;
    private MainActivity activity;
    private LocationManager service;

    public LocationService(MainActivity activity) {
        this.activity = activity;
        this.service = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
    }

    public Location getCurrentLocation() {

        if (currentLocation == null) {

            Criteria criteria = new Criteria();
            String provider = service.getBestProvider(criteria, true);

            // verifica permisiunile
            // daca nu sunt permise se cere permisiunea
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
            // daca sunt permise
            } else {
                // se cere ultima locatie cunoscuta
                currentLocation = service.getLastKnownLocation(provider);
                // daca nu exista se cere
                if (currentLocation == null)
                    service.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }
        return currentLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        activity.startWithLocation(location);
        service.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
