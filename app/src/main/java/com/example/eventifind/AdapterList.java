package com.example.eventifind;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// clasa aceasta este responsabila de adaptarea unui map de obiecte Event in ListView
public class AdapterList extends BaseAdapter {
    private final ArrayList<Event> eventList;

    public AdapterList(HashMap<String,Event> map){
        eventList = new ArrayList<Event>();
        eventList.addAll(map.values());
    }

    public AdapterList(ArrayList<String> list){
        eventList = new ArrayList<Event>();
        for(Map.Entry<String,Event> e : Database.eventMap.entrySet()){
            if(list.contains(e.getKey())){
                eventList.add(e.getValue());
            }
        }
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
        Event item = getItem(position);
        ((TextView)listItem.findViewById(R.id.title)).setText(item.getName());
        ((TextView)listItem.findViewById(R.id.description)).setText(item.getDescription());
        ((TextView)listItem.findViewById(R.id.date)).setText(new SimpleDateFormat("dd/MM/yyyy").format(item.getDate() ) );

        return listItem;
    }
}
