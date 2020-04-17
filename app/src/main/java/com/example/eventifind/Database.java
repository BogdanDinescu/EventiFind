package com.example.eventifind;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fonfon.geohash.GeoHash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class Database {
    private DatabaseReference mDatabase = null;
    public HashMap<String,Event> eventMap;
    public ArrayList<String> joinedEvents;
    private MainActivity activity;

    public Database(MainActivity context){
        this.activity = context;
    };

    private DatabaseReference getDatabaseReference(){
        if(mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            eventMap = new HashMap<String, Event>();
            joinedEvents = new ArrayList<String>();
        }
        return mDatabase;
    }

    public void createEvent(String name, String description, Date data , double latitude, double longitude) {
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data,latitude,longitude);
        getDatabaseReference().child("events").child(key).setValue(event).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException("Eroare");
            }
        });
    }

    public void queryClosestEvents(Location location, Integer numberOfEvents){
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
                        eventMap.put(eventSnapshot.getKey(),eventSnapshot.getValue(Event.class));
                    }
                }
                activity.getTabsManager().getMapFragment().addMarkers();
                activity.getTabsManager().getMapFragment().colorMarkers();
                activity.getTabsManager().getFeedFragment().loadFeed();
                activity.hideProgressBar();
            }
            // daca citirea a esuat
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Cancelled", databaseError.toString());
            }
        };
        // Querry
        getDatabaseReference().child("events").limitToFirst(numberOfEvents).addValueEventListener(eventListener);
    }

    public void getJoinedEvents(String id){
        getDatabaseReference().child("user-data").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        HashMap<String, String> joinedEventsMap = (HashMap<String, String>) dataSnapshot.getValue();
                        joinedEvents.clear();
                        joinedEvents.addAll(joinedEventsMap.values());
                    }
                    activity.getTabsManager().getMapFragment().colorMarkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
    }

    // Il adauga la joined (return true) daca nu e deja, altfel il sterge (return false)
    public void joinEvent(final String userId, final String eventId){
        getDatabaseReference().child("user-data").child(userId).orderByValue().equalTo(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    String key = dataSnapshot.getRef().push().getKey();
                    dataSnapshot.getRef().child(key).setValue(eventId);
                }else {
                    for(DataSnapshot d:dataSnapshot.getChildren()){
                        if (d.getValue().equals(eventId))
                            d.getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
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