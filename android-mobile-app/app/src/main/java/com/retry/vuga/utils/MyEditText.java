package com.retry.vuga.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class MyEditText extends androidx.appcompat.widget.AppCompatEditText {

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, icon, null);
    }
}
