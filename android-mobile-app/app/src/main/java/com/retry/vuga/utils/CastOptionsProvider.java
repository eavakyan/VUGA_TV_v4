package com.retry.vuga.utils;

import android.content.Context;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.retry.vuga.R;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    
    @Override
    public CastOptions getCastOptions(Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId("CC1AD845")  // Default media receiver app ID
                .build();
    }
    
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}