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
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new DiscoverFragment();
            case 2:
                if (sessionManager.getAppSettings().getSettings().getLiveTvEnable() == 1) {

                    return new LiveTvFragment();
                } else {
                    return new HomeFragment();
                }
            case 3:
                return new WatchListFragment();

        }
        return new HomeFragment();
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
