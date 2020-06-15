package com.example.eventifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private MainActivity activity;
    private RecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.feed_list);
        activity = (MainActivity) getActivity();
        if (activity.getDatabase().queriedAlready)
            loadFeed();
    }

    public void loadFeed(){
        super.onResume();
        adapter = new RecyclerAdapter(activity.getDatabase().eventMap);
        recyclerView.setAdapter(adapter);
    }

    public void buttonSetText(String key,boolean join) {
        if(adapter != null) {
            // daca exista in lista
            int index = adapter.getIndex(key);
            if(index >= 0) {
                Button joinButton = recyclerView.getLayoutManager().findViewByPosition(index).findViewById(R.id.join);
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

    public void notifyEventCardRemoved(String key) {
        if (adapter != null) {
            int index = adapter.getIndex(key);
            if (index >= 0) {
                adapter.notifyItemRemoved(index);
            }
        }
    }


}
