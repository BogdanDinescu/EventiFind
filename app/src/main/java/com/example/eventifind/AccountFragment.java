package com.example.eventifind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class AccountFragment extends Fragment {
    private Button signOutBtn;
    private static ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signOutBtn = view.findViewById(R.id.log_out_button);
        listView = view.findViewById(R.id.list_event);

        // cand este apasat sign out
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.signOut();
                gotoLogin();
            }
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        TextView name = view.findViewById(R.id.account_name);
        name.setText(account.getDisplayName());
        ImageView picture = view.findViewById(R.id.account_picture);
        Glide.with(this).load(account.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(picture);
        addEventsToList();
    }

    @Override
    public void onResume() {
        super.onResume();
        addEventsToList();
    }

    private void addEventsToList() {
        AdapterList adapter = new AdapterList(Database.joinedEvents);
        listView.setAdapter(adapter);
    }

    private void gotoLogin() {
        Intent intent = new Intent(getActivity(),LoginActivity.class);
        startActivity(intent);
    }
}
