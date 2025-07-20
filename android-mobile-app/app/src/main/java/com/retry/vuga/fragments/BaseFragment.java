package com.retry.vuga.fragments;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.retry.vuga.utils.SessionManager;

import java.util.Locale;


public class BaseFragment extends Fragment {


    SessionManager sessionManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireActivity());
        setLocal();
    }

    private void setLocal() {
        SessionManager sessionManager = new SessionManager(requireActivity());
        Locale locale = new Locale(sessionManager.getLanguage());
        Locale.setDefault(locale);

        Resources resources = requireActivity().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }


}