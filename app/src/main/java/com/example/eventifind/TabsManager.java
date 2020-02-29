package com.example.eventifind;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

// Aceasta clasa creeaza dinamic meniul cu taburi
class TabsManager {
    private FragmentManager fragmentManager;
    private Activity activity;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private FeedFragment feedFragment;
    private MapFragment mapFragment;
    private CalendarFragment calendarFragment;

    TabsManager(Activity activity, FragmentManager fm) {
        this.activity = activity;
        this.fragmentManager = fm;
    }

    void CreateTabs() {

        viewPager = this.activity.findViewById(R.id.view_pager);
        tabLayout = this.activity.findViewById(R.id.tab_layout);

        // creeaza obiectele fragment
        feedFragment = new FeedFragment();
        mapFragment = new MapFragment();
        calendarFragment = new CalendarFragment();

        tabLayout.setupWithViewPager(viewPager);

        viewPagerAdapter viewPagerAdapter = new viewPagerAdapter();
        // adaug fragmentele si numele lor
        viewPagerAdapter.addFragment(feedFragment, this.activity.getResources().getString(R.string.Feed));
        viewPagerAdapter.addFragment(mapFragment, this.activity.getResources().getString(R.string.Map));
        viewPagerAdapter.addFragment(calendarFragment, this.activity.getResources().getString(R.string.Calendar));
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

        viewPagerAdapter() {
            super(fragmentManager,0);
        }

        void addFragment(Fragment fragment, String title){
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