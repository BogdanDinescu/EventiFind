package com.example.eventifind;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AccountFragment extends Fragment {
    private Button signOutBtn;
    private Button joinedEvBtn;
    private Button myEvBtn;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signOutBtn = view.findViewById(R.id.log_out_button);
        joinedEvBtn = view.findViewById(R.id.joined_events);
        myEvBtn = view.findViewById(R.id.my_events);
        recyclerView = view.findViewById(R.id.list_event);
        activity = (MainActivity) getActivity();

        // cand este apasat sign out
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        // cand e apasat joined events
        joinedEvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addJoinedEventsToList();
            }
        });

        // cand e apasat my events
        myEvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHostedEventsToList();
            }
        });

        TextView name = view.findViewById(R.id.account_name);
        name.setText(activity.getUserName());
        ImageView picture = view.findViewById(R.id.account_picture);
        Glide.with(this).load(activity.getUserPhoto()).apply(RequestOptions.circleCropTransform()).into(picture);
    }

    private void addJoinedEventsToList() {
        adapter = new RecyclerAdapter(activity.getDatabase().joinedEvents, activity.getDatabase().eventMap);
        recyclerView.setAdapter(adapter);
    }

    private void addHostedEventsToList(){
        adapter = new RecyclerAdapter(activity.getDatabase().hostedEvents);
        recyclerView.setAdapter(adapter);
    }

    public void setAdminView() {
        this.getView().findViewById(R.id.my_events).setEnabled(true);
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

    private void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("You really want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LoginActivity.signOut();
                gotoLogin();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void gotoLogin() {
        Intent intent = new Intent(getActivity(),LoginActivity.class);
        startActivity(intent);
    }
}
