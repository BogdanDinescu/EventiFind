package com.mds.eventifind;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class AccountFragment extends Fragment {
    private Button signOutBtn;
    private Button joinedEvBtn;
    private Button myEvBtn;
    private ImageButton popUpBtn;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private SharedPreferences sharedPref;
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
        popUpBtn = view.findViewById(R.id.more_button);
        activity = (MainActivity) getActivity();
        if (activity.getDatabase().admin) setAdminView();

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
                int joinedEventsCount = activity.getDatabase().joinedEvents.size();
                if(joinedEventsCount == 0) {
                    String text = "You haven't joined any event yet";
                    Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_LONG).show();
                }
            }
        });

        // cand e apasat my events
        myEvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHostedEventsToList();
            }
        });

        // cand este apasat butonul more (3 dots)
        popUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpMenuOpen();
            }
        });

        sharedPref = activity.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        TextView name = view.findViewById(R.id.account_name);
        name.setText(activity.getUserName());
        ImageView picture = view.findViewById(R.id.account_picture);
        Glide.with(this).load(activity.getUser().getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(picture);
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
        if(this.getView() != null) {
            this.getView().findViewById(R.id.my_events).setEnabled(true);
        }
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

    private void popUpMenuOpen() {
        PopupMenu popup = new PopupMenu(getContext(), popUpBtn);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());

        boolean darkMode = sharedPref.getBoolean("dark",false);
        popup.getMenu().getItem(0).setChecked(darkMode);
        popup.getMenu().getItem(1).setVisible(activity.getDatabase().admin);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dark_toggle:
                        activity.setDarkMode(!item.isChecked());
                        return true;
                    case R.id.add_admin:
                        addAdminDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private void addAdminDialog() {
        DialogFragment newFragment = new AddAdminDialog(activity);
        newFragment.show(getFragmentManager(), "addAdmin");
    }

    private void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("You really want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LoginActivity.signOut();
                activity.goToLogin();
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

}
