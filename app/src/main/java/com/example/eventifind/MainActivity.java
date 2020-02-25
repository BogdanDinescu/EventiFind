package com.example.eventifind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private FeedFragment feedFragment;
    private MapFragment mapFragment;
    private CalendarFragment calendarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // creeaza obiectele fragment
        feedFragment = new FeedFragment();
        mapFragment = new MapFragment();
        calendarFragment = new CalendarFragment();

        tabLayout.setupWithViewPager(viewPager);

        viewPagerAdapter viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager(),0);
        // adaug fragmentele si numele lor
        viewPagerAdapter.addFragment(feedFragment,getResources().getString(R.string.Feed));
        viewPagerAdapter.addFragment(mapFragment,getResources().getString(R.string.Map));
        viewPagerAdapter.addFragment(calendarFragment,getResources().getString(R.string.Calendar));
        // le randez
        viewPager.setAdapter(viewPagerAdapter);
        // pun iconitele
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_feed);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_pin);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_calendar);
    }

    // clasa care tine evidenta fragmentelor
    private class viewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitle = new ArrayList<>();

         public viewPagerAdapter(@NonNull FragmentManager fm,int behavior) {
            super(fm,behavior);
        }

        public void addFragment(Fragment fragment,String title){
             fragments.add(fragment);
             fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public CharSequence getPageTitle(int position){
             return fragmentTitle.get(position);
        }
    }
}
