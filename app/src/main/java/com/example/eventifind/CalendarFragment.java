package com.example.eventifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.google.firebase.database.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private MainActivity activity;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
/*
        setDate1 = view.findViewById(R.id.button2);
        setDate2 = view.findViewById(R.id.button3);


        setDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = "1/12/2020";
                focusOnDate(date);
            }
        });

        setDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    focusOnDate(format.parse("01/07/2020"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
 */
        calendarView = view.findViewById(R.id.calendarView);
        final LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayoutVertical);
        activity = (MainActivity) getActivity();

//        ArrayList<ElementCalendar> elementCalendar = new ArrayList<>();
//        ArrayList<String> joinedEvents = activity.getDatabase().joinedEvents;

        listView = view.findViewById(R.id.list_view1);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            TextView textView = activity.findViewById(R.id.textView);
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                boolean isAtLeastOneEvent = false;
                month++;

                HashMap<String, Event> aux = new HashMap<>();

                for(Map.Entry<String,Event> e : activity.getDatabase().eventMap.entrySet()) {

                    aux.put(e.getKey(), e.getValue());

                    String dateClickedString = dayOfMonth + "/" + month + "/" + year;
                    Date dateClicked = null;
                    try {
                         dateClicked = new SimpleDateFormat("dd/MM/yyyy").parse(dateClickedString);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                    Date dateClickedWithNoTime = null;

                    try {
                        dateClickedWithNoTime = formatter.parse(formatter.format(dateClicked));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }

                    Date dateInDBWithNoTime = null;

                    try {
                         dateInDBWithNoTime = formatter.parse(formatter.format(e.getValue().getDate()));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }

                    if(dateInDBWithNoTime.toString().equals(dateClickedWithNoTime.toString()) && activity.getDatabase().joinedEvents.contains(e.getKey())) {
                        textView.setText("");
                        listView.setAdapter(null);
                        AdapterList adapter = new AdapterList(aux, activity, activity.getUserId());
                        listView.setAdapter(adapter);

                        isAtLeastOneEvent = true;
                    }
                }

                if(isAtLeastOneEvent == false) {
                    listView.setAdapter(null);
                    textView.setText("Niciun eveniment in aceasta zi!");
                }

            }
        });

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

    // cand date e obiect de tip Java.util.date
    public void focusOnDate(Date date) {
        calendarView.setDate(date.getTime());
    }

}
