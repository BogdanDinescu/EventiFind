package com.example.eventifind;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventDialog extends DialogFragment {
    private EditText textName;
    private EditText textDescription;
    public static TextView textDate;
    private TextView textLocation;
    private double latitude;
    private double longitude;
    private String address;
    private Button cancel;
    private Button ok;

    EventDialog(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    EventDialog(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_event,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textName =  view.findViewById(R.id.event_name);
        textDescription = view.findViewById(R.id.description);
        textDate = view.findViewById(R.id.date);
        textLocation = view.findViewById(R.id.location);
        cancel = view.findViewById(R.id.cancel);
        ok = view.findViewById(R.id.ok);

        textLocation.setText(address);

        // Cancel
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // OK
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = textName.getText().toString();
                String description = textDescription.getText().toString();
                Date date = null;
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy",Locale.US).parse(textDate.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // daca nu s-au introdus date atunci afiseaza un mesaj de eroare
                if(name.isEmpty() || description.isEmpty() || date == null){
                    showToast(getActivity().getResources().getString(R.string.Fields_empty));
                    // altfel pune evenimentul in baza de date si afieaza un mesaj
                }else {
                    try {
                        ((MainActivity)getActivity() ).getDatabase().createEvent(name,description,date,latitude,longitude);
                        showToast(getActivity().getResources().getString(R.string.Event_created));
                    }catch (Exception e){
                        showToast(e.getMessage());
                    }
                    EventDialog.this.getDialog().cancel();
                }
            }
        });
        // se deschide dialogul pentru selectarea datei
        textDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        // se deschide dialogul pentru selectarea locatiei
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    // clasa care se ocupa de selctarea datei
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            return new DatePickerDialog(getActivity(),this,year,month,day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // aparent luna se indexeaza de la 0

            textDate.setText(day + "/" + (month+1) + "/" + year);
        }
    }

    // functia care afiseaza un mesaj
    private void showToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
