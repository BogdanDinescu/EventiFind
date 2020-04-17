package com.example.eventifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.net.ConnectException;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {
    private TabsManager tabsManager;
    private Database database;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar_cyclic);
        /*toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_1);*/

        // initializari
        database = new Database(this);
        tabsManager = new TabsManager(this, getSupportFragmentManager());

        // verifica permisiunile
        // daca nu sunt permise se cere permisiunea
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
        }else{
            tabsManager.CreateTabs();
            try {
                // obtine lista cu event-urile din imprejur si tot aceasta functie apeleaza addMarkers
                Location location = tabsManager.getMapFragment().getCurrentLocation(this);
                if (location == null) {
                    throw new NullPointerException();
                }
                database.queryClosestEvents(location,10);
            } catch (ConnectException e) {
                EnableDialog();
            } catch (NullPointerException e) {
                finish();
            }
            // obtine evenimentele la care participa userul cu id-ul respectiv
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            database.getJoinedEvents(account.getId());
        }
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
        tabsManager.CreateTabs();
    }

    public TabsManager getTabsManager() {
        return tabsManager;
    }

    public Database getDatabase() {
        return database;
    }

    private void EnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Internet service and Location");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
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
