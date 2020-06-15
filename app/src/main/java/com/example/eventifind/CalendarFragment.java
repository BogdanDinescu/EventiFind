package com.example.eventifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private MainActivity activity;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private TextView message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        message = view.findViewById(R.id.message);
        recyclerView = view.findViewById(R.id.list_event);
        activity = (MainActivity) getActivity();


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                showEventsForSelectedDate(year,month,dayOfMonth);
            }
        });

    }

    private void showEventsForSelectedDate(int year, int month, int dayOfMonth) {
        boolean isAtLeastOneEvent = false;
        month++;

        HashMap<String, Event> aux = new HashMap<>();

        for(Map.Entry<String,Event> e : activity.getDatabase().eventMap.entrySet()) {

            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateClickedString = dayOfMonth + "/" + month + "/" + year;

            Date dateClicked = null;
            try {
                dateClicked = formatter.parse(dateClickedString);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            Date dateClickedWithNoTime = null;

            try {
                dateClickedWithNoTime = formatter.parse(formatter.format(dateClicked));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            Date dateInDBWithNoTime = e.getValue().getDate();

            if(dateInDBWithNoTime.equals(dateClicked) && activity.getDatabase().joinedEvents.contains(e.getKey())) {
                aux.put(e.getKey(), e.getValue());
                isAtLeastOneEvent = true;
            }
        }

        if(!isAtLeastOneEvent) {
            message.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(null);
        } else {
            message.setVisibility(View.GONE);
            adapter = new RecyclerAdapter(aux);
            recyclerView.setAdapter(adapter);
        }

    }

    // cand date e string de forma dd/MM/yyyy
    public void focusOnDate(String date) {
        String parts[] = date.split("/");

        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        long milliTime = calendar.getTimeInMillis();

        calendarView.setDate(milliTime);
    }

    public void buttonSetText(String key,boolean join) {
        if(adapter != null) {
            // daca exista in lista
            if(adapter.getIndex(key) >= 0) {
                Button joinButton = recyclerView.getLayoutManager().findViewByPosition(adapter.getIndex(key)).findViewById(R.id.join);
                if(join) {
                    joinButton.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                    joinButton.setText(activity.getResources().getString(R.string.Join));
                } else {
                    joinButton.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                    joinButton.setText(activity.getResources().getString(R.string.Unjoin));
                }
            }
        }
    }

    // cand date e obiect de tip Java.util.date
    public void focusOnDate(Date date) {
        calendarView.setDate(date.getTime());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        showEventsForSelectedDate(year,month,day);
    }

}
