package com.example.eventifind;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// clasa aceasta este responsabila de adaptarea unui map de obiecte Event in RecyclerView
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private final HashMap<String,Event> map;
    private final ArrayList<String> keyList;
    private MainActivity activity;
    private final String userId;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView title;
        TextView owner;
        TextView date;
        TextView location;
        TextView description;
        Button join;
        Button delete;
        ImageButton expand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card);
            title = (TextView)itemView.findViewById(R.id.title);
            owner = (TextView)itemView.findViewById(R.id.owner);
            date = (TextView)itemView.findViewById(R.id.date);
            location = (TextView)itemView.findViewById(R.id.location);
            description = (TextView)itemView.findViewById(R.id.description);
            join = (Button)itemView.findViewById(R.id.join);
            delete = (Button)itemView.findViewById(R.id.delete);
            expand = (ImageButton)itemView.findViewById(R.id.expand);
        }
    }

    public RecyclerAdapter(HashMap<String, Event> map, String userId){
        this.map = map;
        this.keyList = new ArrayList<String>(map.keySet());
        this.userId = userId;
    }

    // constructor folosit in acoount tab cand ai o submultime de chei de evenimente
    public RecyclerAdapter(ArrayList<String> list, String userId, HashMap<String,Event> map){
        this.map = new HashMap<>();
        for(Map.Entry<String,Event> e : map.entrySet()){
            if(list.contains(e.getKey())){
                this.map.put(e.getKey(),e.getValue());
            }
        }
        this.keyList = new ArrayList<>(list);
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        activity = (MainActivity) parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Event item = getItem(position);
        // caz particular evenimentul a fost sters anterior, el inca figureaza ca joined.
        if (item == null) {
            holder.title.setText(activity.getResources().getString(R.string.Event_deleted));
            holder.join.setVisibility(View.GONE);
            return;
        }
        // Nume
        holder.title.setText(item.getName());
        // Owner
        holder.owner.setText(activity.getResources().getString(R.string.Hosted_by,item.getOwnerName()));
        // Descriere
        holder.description.setText(item.getDescription());
        // Data
        holder.date.setText(new SimpleDateFormat("dd/MM/yyyy").format(item.getDate() ) );
        holder.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTabsManager().setCurrentTab(2);
                activity.getTabsManager().getCalendarFragment().focusOnDate(item.getDate());
            }
        });
        // Locatie
        holder.location.setText(activity.getTabsManager().getMapFragment().getCityLocation(new LatLng(item.getLatitude(), item.getLongitude())));
        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTabsManager().setCurrentTab(1);
                activity.getTabsManager().getMapFragment().centreOnPoint(item.getLatitude(),item.getLongitude());
            }
        });

        // Join/unjoin Button
        if(activity.getDatabase().joinedEvents.contains(getKey(position))) {
            holder.join.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            holder.join.setText(activity.getResources().getString(R.string.Unjoin));
        } else {
            holder.join.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            holder.join.setText(activity.getResources().getString(R.string.Join));
        }
        holder.join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.join.getText() == activity.getResources().getString(R.string.Join)) {
                    holder.join.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                    holder.join.setText(activity.getResources().getString(R.string.Unjoin));
                } else {
                    holder.join.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                    holder.join.setText(activity.getResources().getString(R.string.Join));
                }
                activity.getDatabase().joinEvent(userId,getKey(position));
            }
        });

        // Delete event
        if(activity.getDatabase().hostedEvents.containsKey(getKey(position)))
            holder.delete.setVisibility(View.VISIBLE);
        else
            holder.delete.setVisibility(View.GONE);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getDatabase().deleteEvent(getKey(position));
            }
        });

        // Expand button
        holder.expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.description.getVisibility() == View.GONE) {
                    holder.description.setVisibility(View.VISIBLE);
                } else {
                    holder.description.setVisibility(View.GONE);
                }

                v.animate().rotationBy(180).setDuration(500).start();
            }
        });

    }

    private Event getItem(int position) {
        return map.get(keyList.get(position));
    }

    private String getKey(int position){
        return keyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e("eee",getItem(position).getName());
        Button myButton = new Button(activity);
        myButton.setText("EEEE");
        myButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout linearLayout = view.findViewById(R.id.container);
        linearLayout.addView(myButton);
    }


}
