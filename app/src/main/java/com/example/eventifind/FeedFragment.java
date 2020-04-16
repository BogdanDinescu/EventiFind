package com.example.eventifind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;


public class FeedFragment extends Fragment {

    private static ListView listView;
    private GoogleSignInAccount account;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        account = GoogleSignIn.getLastSignedInAccount(getActivity());
        listView = view.findViewById(R.id.feed_list);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFeed();
    }

    public void loadFeed(){
        super.onResume();
        AdapterList adapter = new AdapterList(activity.getDatabase().eventMap, activity, account.getId());
        listView.setAdapter(adapter);
    }

}
