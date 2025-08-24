package com.retry.vuga.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeTouchListeners implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public OnSwipeTouchListeners(Context context) {
        this.gestureDetector = new GestureDetector(context, new GestureListeners());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event);
        // Always return true to consume the event and allow gesture detection
        return true;
    }

    public void onDoubleTouch() {

    }

    public void onSingleTouch() {

    }

    public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {

    }
    
    public void onSwipeDown() {
        
    }
    
    public void onSwipeUp() {
        
    }
    
    public void onSwipeLeft() {
        
    }
    
    public void onSwipeRight() {
        
    }

    public final class GestureListeners extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (e1 != null && e2 != null) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    // Vertical swipe
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            Log.d("OnSwipeTouchListeners", "Swipe down detected: diffY=" + diffY + ", velocityY=" + velocityY);
                            onSwipeDown();
                        } else {
                            Log.d("OnSwipeTouchListeners", "Swipe up detected: diffY=" + diffY + ", velocityY=" + velocityY);
                            onSwipeUp();
                        }
                        return true;
                    }
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            onDoubleTouch();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            onSingleTouch();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            onScrollTouch(e1, e2, distanceX, distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
}
