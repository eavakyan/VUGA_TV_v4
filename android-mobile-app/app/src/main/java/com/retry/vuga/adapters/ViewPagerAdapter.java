package com.retry.vuga.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.retry.vuga.fragments.DiscoverFragment;
import com.retry.vuga.fragments.HomeFragment;
import com.retry.vuga.fragments.LiveTvFragment;
import com.retry.vuga.fragments.WatchListFragment;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

public class ViewPagerAdapter extends FragmentStateAdapter {
    SessionManager sessionManager;
    Context context;

    public ViewPagerAdapter(@NonNull @NotNull FragmentManager fragmentManager, @NonNull @NotNull Lifecycle lifecycle, Context c) {
        super(fragmentManager, lifecycle);
        context = c;
        sessionManager = new SessionManager(context);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        android.util.Log.d("ViewPagerAdapter", "createFragment called for position: " + position);
        switch (position) {
            case 0:
                android.util.Log.d("ViewPagerAdapter", "Returning HomeFragment for position 0");
                return new HomeFragment();
            case 1:
                android.util.Log.d("ViewPagerAdapter", "Returning DiscoverFragment for position 1");
                return new DiscoverFragment();
            case 2:
                // Always return LiveTvFragment for position 2 for now
                android.util.Log.d("ViewPagerAdapter", "Returning LiveTvFragment for position 2 (always enabled)");
                return new LiveTvFragment();
            case 3:
                android.util.Log.d("ViewPagerAdapter", "Returning WatchListFragment for position 3");
                return new WatchListFragment();

        }
        android.util.Log.d("ViewPagerAdapter", "Default: Returning HomeFragment");
        return new HomeFragment();
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
