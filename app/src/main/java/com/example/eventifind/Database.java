package com.example.eventifind;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public static void createEvent(String name, String description, Date data , LatLng geoPoint){
        String key = getDatabaseReference().child("events").push().getKey();
        Event event = new Event(name,description,data,geoPoint);
        getDatabaseReference().child("events").child(key).setValue(event);
    }

}

class Event implements Serializable {
    private String name;
    private String description;
    private Date date;
    private LatLng point;

    public Event() {
    }
    // treubie lasat public
    public Event(String name, String description, Date data, LatLng point) {
        this.name = name;
        this.description = description;
        this.date = data;
        this.point = point;
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

    public LatLng getPoint() {
        return point;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }
}
