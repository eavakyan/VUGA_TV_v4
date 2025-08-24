package com.retry.vuga.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.retry.vuga.activities.LiveTvActivity;

/**
 * LiveTvFragment - Redirects to LiveTvActivity
 * This fragment acts as a placeholder in the ViewPager and immediately
 * launches the LiveTvActivity when selected.
 */
public class LiveTvFragment extends Fragment {
    
    private boolean hasLaunched = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Return empty view since we're redirecting
        return new View(getContext());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Only launch LiveTvActivity once per fragment lifecycle
        if (!hasLaunched) {
            hasLaunched = true;
            
            // Launch LiveTvActivity
            Intent intent = new Intent(getContext(), LiveTvActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            
            // Use overridePendingTransition for smooth animation
            if (getActivity() != null) {
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }
        
        // Always return to Home tab when this fragment resumes
        if (getActivity() != null && getActivity().findViewById(com.retry.vuga.R.id.viewPager) != null) {
            androidx.viewpager2.widget.ViewPager2 viewPager = getActivity().findViewById(com.retry.vuga.R.id.viewPager);
            viewPager.postDelayed(() -> viewPager.setCurrentItem(0, false), 100); // Small delay to ensure smooth transition
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Reset the flag when fragment is paused
        hasLaunched = false;
    }
}