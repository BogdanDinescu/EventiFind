package com.example.eventifind;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fonfon.geohash.GeoHash;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    private static DatabaseReference mDatabase = null;
    public static List<Event> eventsList;
    private Database(){};
    private static DatabaseReference getDatabaseReference(){
        if(mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            eventsList = new ArrayList<Event>();
        }
        return mDatabase;
    }

    public static void createEvent(String name, String description, Date data , double latitude, double longitude){
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data,latitude,longitude);
        getDatabaseReference().child("events").child(key).setValue(event).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException("Eroare");
            }
        });
    }

    public static void queryClosestEvents(Location location, Integer numberOfEvents){
        // obtine hash-ul pentru locatia data
        final GeoHash hash = GeoHash.fromLocation(location,4);
        // obtine regiunile din imprejur
        GeoHash[] hashes = hash.getAdjacentRect();
        // hash-urile in lista de string-uri
        final List<String> stringsHashes = new ArrayList<String>();
        for (GeoHash h:hashes) {
            stringsHashes.add(h.toString());
        }

        ValueEventListener eventListener = new ValueEventListener() {
            // cand a fost citit cu succes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot eventSnapshot : dataSnapshot.getChildren()){
                    // daca inregistrarea e in zonele date de hash-uri
                    String h = eventSnapshot.child("geoHash").getValue(String.class);
                    if (stringsHashes.contains(h)){
                        eventsList.add(eventSnapshot.getValue(Event.class));
                        MapFragment.addMarkers();
                    }
                }
            }
            // daca citirea a esuat
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("loadPost:onCancelled", databaseError.toString());
            }
        };
        // Querry
        getDatabaseReference().child("events").limitToFirst(numberOfEvents).addValueEventListener(eventListener);
    }
}

class Event implements Serializable {
    private String name;
    private String description;
    private Date date;
    private double latitude;
    private double longitude;
    private String geoHash;

    // firebase are nevoie de un constructor fara parametrii cand iau obiecte din BD
    public Event() {
    }

    // treubie lasat public
    public Event(String name, String description, Date data, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.date = data;
        this.latitude = latitude;
        this.longitude = longitude;
        GeoHash hash = GeoHash.fromLocation(LatLngToLocation(latitude,longitude),4);
        this.geoHash = hash.toString();
    }

    // set-erele si get-erele sunt necesare pentru firebase sa aiba acces la obiect.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    private Location LatLngToLocation(double latitude, double longitude){
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}