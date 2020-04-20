package com.example.eventifind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// clasa aceasta este responsabila de adaptarea unui map de obiecte Event in ListView
public class AdapterList extends BaseAdapter {
    private final HashMap<String,Event> map;
    private final ArrayList<String> keyList;
    private final MainActivity activity;
    private final String userId;

    public AdapterList(HashMap<String,Event> map, MainActivity activity, String userId){
        this.map = map;
        this.keyList = new ArrayList<String>(map.keySet());
        this.activity = activity;
        this.userId = userId;
    }

    public AdapterList(ArrayList<String> list, MainActivity activity, String userId){
        this.map = new HashMap<>();
        for(Map.Entry<String,Event> e : activity.getDatabase().eventMap.entrySet()){
            if(list.contains(e.getKey())){
                map.put(e.getKey(),e.getValue());
            }
        }
        this.keyList = new ArrayList<>(list);
        this.activity = activity;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return keyList.size();
    }

    @Override
    public Event getItem(int position) {
        return map.get(keyList.get(position));
    }

    public String getKey(int position){
        return keyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View listItem;
        if(convertView == null){
            listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card,parent,false);
        } else {
            listItem = convertView;
        }

        final Event item = getItem(position);
        // Nume
        ((TextView)listItem.findViewById(R.id.title)).setText(item.getName());
        // Owner
        ((TextView)listItem.findViewById(R.id.owner)).setText(activity.getResources().getString(R.string.Hosted_by,item.getOwnerName()));
        // Descriere
        ((TextView)listItem.findViewById(R.id.description)).setText(item.getDescription());
        // Data
        TextView dateText = listItem.findViewById(R.id.date);
        dateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(item.getDate() ) );
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTabsManager().setCurrentTab(2);
                activity.getTabsManager().getCalendarFragment().focusOnDate(item.getDate());
            }
        });
        // Locatie
        TextView locationText = listItem.findViewById(R.id.location);
        locationText.setText("View on map");
        locationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTabsManager().setCurrentTab(1);
                activity.getTabsManager().getMapFragment().centreOnPoint(item.getLatitude(),item.getLongitude());
            }
        });

        // Join/unjoin Button
        final Button joinButton = listItem.findViewById(R.id.join);
        if(activity.getDatabase().joinedEvents.contains(getKey(position))) {
            joinButton.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
            joinButton.setText(activity.getResources().getString(R.string.Unjoin));
        } else {
            joinButton.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
            joinButton.setText(activity.getResources().getString(R.string.Join));
        }
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(joinButton.getText() == activity.getResources().getString(R.string.Join)) {
                    joinButton.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                    joinButton.setText(activity.getResources().getString(R.string.Unjoin));
                } else {
                    joinButton.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                    joinButton.setText(activity.getResources().getString(R.string.Join));
                }
                activity.getDatabase().joinEvent(userId,getKey(position));
            }
        });

        // Delete event
        final Button deleteButton = listItem.findViewById(R.id.delete);
        if(activity.getDatabase().hostedEvents.containsValue(getKey(position)))
            deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getDatabase().deleteEvent(getKey(position));
            }
        });

        return listItem;
    }
}
