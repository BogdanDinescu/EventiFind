package com.example.eventifind;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CalendarFragment extends Fragment {

    private Button  setDate1, setDate2;
    private CalendarView calendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDate1 = view.findViewById(R.id.button2);
        setDate2 = view.findViewById(R.id.button3);
        calendarView = view.findViewById(R.id.calendarView);

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

    // cand date de obiect de tip Java.util.date
    public void focusOnDate(Date date) {
        calendarView.setDate(date.getTime());
    }

}
