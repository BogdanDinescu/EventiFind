package com.example.eventifind;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// clasa aceasta este responsabila de adaptarea unui map de obiecte Event in ListView
public class AdapterList extends BaseAdapter {
    private final ArrayList<Event> eventList;
    private MainActivity activity;
    private String userId;

    public AdapterList(HashMap<String,Event> map, MainActivity activity, String userId){
        eventList = new ArrayList<Event>();
        eventList.addAll(map.values());
        this.activity = activity;
        this.userId = userId;
    }

    public AdapterList(ArrayList<String> list, MainActivity activity, String userId){
        eventList = new ArrayList<Event>();
        for(Map.Entry<String,Event> e : Database.eventMap.entrySet()){
            if(list.contains(e.getKey())){
                eventList.add(e.getValue());
            }
        }
        this.activity = activity;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Event getItem(int position) {
        return (Event) eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View listItem;
        if(convertView == null){
            listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card,parent,false);
        } else {
            listItem = convertView;
        }

        final Event item = getItem(position);
        // Nume
        ((TextView)listItem.findViewById(R.id.title)).setText(item.getName());
        // Descriere
        ((TextView)listItem.findViewById(R.id.description)).setText(item.getDescription());
        // Data
        TextView dateText = listItem.findViewById(R.id.date);
        dateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(item.getDate() ) );
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarFragment.focusOnDate(item.getDate());
                activity.getTabsManager().setCurrentTab(2);
            }
        });
        // Locatie
        TextView locationText = listItem.findViewById(R.id.location);
        locationText.setText("View on map");
        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapFragment.setCentralPoint(item.getLatitude(),item.getLongitude());
                activity.getTabsManager().setCurrentTab(1);
            }
        });

        // Join Button
        Button joinButton = listItem.findViewById(R.id.join);
        
        return listItem;
    }
}
