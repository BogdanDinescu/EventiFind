package com.example.eventifind;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Date;

public class Database {
    private static DatabaseReference mDatabase = null;
    private Database(){};
    private static DatabaseReference getDatabaseReference(){
        if(mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();
        return mDatabase;
    }

    public static void createEvent(String name, String description, Date data , GeoPoint geoPoint){
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data,geoPoint);
        getDatabaseReference().child(key).setValue(event);
    }

    // !!! DELETE LATER !!!
    // constructor temporar fara locatie pana cand implementez locationPicker
    public static void createEvent(String name, String description, Date data){
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data);
        getDatabaseReference().child("events").child(key).setValue(event).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Database Error",e.toString());
            }
        });
    }
}

class Event implements Serializable {
    private String name;
    private String description;
    private Date date;
    private GeoPoint geoPoint;

    public Event() {
    }

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

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Event(String name, String description, Date data, GeoPoint geoPoint) {
        this.name = name;
        this.description = description;
        this.date = data;
        this.geoPoint = geoPoint;
    }

    // !!! DELETE LATER !!!
    // constructor temporar fara locatie pana cand implementez locationPicker
    public Event(String name, String description, Date data) {
        this.name = name;
        this.description = description;
        this.date = data;
    }
}
