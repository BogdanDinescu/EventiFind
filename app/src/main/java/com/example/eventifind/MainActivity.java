package com.example.eventifind;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {
    private TabsManager tabsManager;
    private Database database;
    private LocationService locationService;
    private ProgressBar progressBar;
    private TextView errorText;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar_cyclic);
        errorText = findViewById(R.id.error);
        /*toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_1);*/

        if(!isNetworkAvailable(this)) {
            setErrorText(getResources().getString(R.string.Internet_unavailable));
            return;
        }

        if(!isLocationAvailable(this)) {
            setErrorText(getResources().getString(R.string.Location_unavailable));
        }

        // initializari
        database = new Database(this);
        locationService = new LocationService(this);
        tabsManager = new TabsManager(this, getSupportFragmentManager());
        user = FirebaseAuth.getInstance().getCurrentUser();


        Location location = locationService.getCurrentLocation();
        if (location != null)
            startWithLocation(location);
    }

    public void startWithLocation(Location location) {
        database.queryClosestEvents(location,10);
        database.getJoinedEvents(user.getUid());
        database.checkAdminGetHosted(user.getUid());
        tabsManager.CreateTabs();
    }

    // cand au fost acceptate sau respinse permisiunile se apeleaza functia asta
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // daca una a fost respinse se inchide aplicatia (solutie temporara neeleganta)
        for(int i:grantResults){
            if(i == PERMISSION_DENIED){
                finishAndRemoveTask();
                return;
            }
        }
        this.recreate();
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public TabsManager getTabsManager() {
        return tabsManager;
    }

    public Database getDatabase() {
        return database;
    }

    public String getUserId(){
        return user.getUid();
    }

    public String getUserName(){
        return user.getDisplayName();
    }

    public Uri getUserPhoto(){
        return user.getPhotoUrl();
    }

    public void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean isLocationAvailable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    private void setErrorText(String text) {
        errorText.setText(text);
        errorText.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    public void reload(View view){
        this.finish();
    }

    // go home on back pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
