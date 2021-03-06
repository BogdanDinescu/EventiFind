package com.mds.eventifind;

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
    private static Database instance = null;
    public boolean queriedAlready;
    private DatabaseReference databaseRef;
    public HashMap<String,Event> eventMap;
    public ArrayList<String> joinedEvents;
    public HashMap<String,Event> hostedEvents;
    public boolean admin;
    private MainActivity activity;

    private Database(MainActivity context) {
        this.activity = context;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.eventMap = new HashMap<String, Event>();
        this.joinedEvents = new ArrayList<String>();
        this.hostedEvents = new HashMap<String, Event>();
        this.admin = false;
        this.queriedAlready = false;
    }

    public static Database getDatabase(MainActivity context) {
        if (instance == null) {
            instance = new Database(context);
        }
        return instance;
    }

    private DatabaseReference getDatabaseReference() {
        return instance.databaseRef;
    }

    public void createEvent(String name, String description, Date data , double latitude, double longitude, String ownerId, String ownerName) {
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data,latitude,longitude,ownerId,ownerName);
        getDatabaseReference().child("events").child(key).setValue(event).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                throw new RuntimeException("Eroare");
            }
        });
    }

    public void checkAdminGetHosted(final String userId){
        getDatabaseReference().child("users").child(userId).child("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                admin = (boolean) dataSnapshot.getValue();
                if(admin) {
                    activity.getTabsManager().getAccountFragment().setAdminView();
                    getHostedEvents(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
    }

    public void queryClosestEvents(Location location, Integer numberOfEvents) {
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
                activity.getTabsManager().getFeedFragment().loadFeed();
                activity.getTabsManager().getMapFragment().colorMarkers();
                queriedAlready = true;
                activity.hideProgressBar();
                activity.showNotifications();
            }
            // daca citirea a esuat
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Cancelled", databaseError.toString());
            }
        };
        // Query
        getDatabaseReference().child("events").limitToFirst(numberOfEvents).addValueEventListener(eventListener);
    }

    public void getJoinedEvents(String id){
        getDatabaseReference().child("users").child(id).child("joined").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        HashMap<String, String> joinedEventsMap = (HashMap<String, String>) dataSnapshot.getValue();
                        joinedEvents.clear();
                        joinedEvents.addAll(joinedEventsMap.values());
                    }
                    activity.getTabsManager().getMapFragment().colorMarkers();
                    activity.showNotifications();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
    }

    // Il adauga la joined daca nu e deja, altfel il sterge
    public void joinEvent(final String userId, final String eventId){
        getDatabaseReference().child("users").child(userId).child("joined").orderByValue().equalTo(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    String key = dataSnapshot.getRef().push().getKey();
                    dataSnapshot.getRef().child(key).setValue(eventId);
                    activity.getTabsManager().getFeedFragment().buttonSetText(eventId,false);
                    activity.getTabsManager().getAccountFragment().buttonSetText(eventId,false);
                    activity.getTabsManager().getCalendarFragment().buttonSetText(eventId,false);
                }else {
                    for(DataSnapshot d:dataSnapshot.getChildren()){
                        if (d.getValue().equals(eventId)) {
                            d.getRef().removeValue();
                            activity.getTabsManager().getFeedFragment().buttonSetText(eventId,true);
                            activity.getTabsManager().getAccountFragment().buttonSetText(eventId,true);
                            activity.getTabsManager().getCalendarFragment().buttonSetText(eventId,true);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
    }

    public void getHostedEvents(String userId){
        getDatabaseReference().child("events").orderByChild("ownerId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hostedEvents.clear();
                for(DataSnapshot eventSnapshot : dataSnapshot.getChildren()){
                        hostedEvents.put(eventSnapshot.getKey(),eventSnapshot.getValue(Event.class));
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Canceled",databaseError.toString());
            }
        });
    }

    public void deleteEvent(final String eventId){
        getDatabaseReference().child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
                eventMap.remove(eventId);
                joinedEvents.remove(eventId);
                hostedEvents.remove(eventId);
                activity.getTabsManager().getMapFragment().deleteMarker(eventId);
                activity.getTabsManager().getFeedFragment().notifyEventCardRemoved(eventId);
                activity.getTabsManager().getAccountFragment().notifyEventCardRemoved(eventId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("onCancelled", databaseError.toString());
            }
        });
    }

    public void addAdminByEmail(String email) {
        if (this.admin) {
            getDatabaseReference().child("users").orderByChild("email").equalTo(email).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("result", dataSnapshot.toString());
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot user : dataSnapshot.getChildren()) {
                            user.getRef().child("admin").setValue(true);
                        }
                        activity.showToast(activity.getResources().getString(R.string.User_admined));
                    } else {
                        activity.showToast(activity.getResources().getString(R.string.User_not_exist));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("onCancelled", databaseError.toString());
                }
            });
        }
    }
}

class Event implements Serializable {
    private String name;
    private String ownerId;
    private String ownerName;
    private String description;
    private Date date;
    private double latitude;
    private double longitude;
    private String geoHash;

    // firebase are nevoie de un constructor fara parametrii cand iau obiecte din BD
    public Event() {
    }

    // treubie lasat public
    public Event(String name, String description, Date data, double latitude, double longitude, String ownerId, String ownerName) {
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    private Location LatLngToLocation(double latitude, double longitude){
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}