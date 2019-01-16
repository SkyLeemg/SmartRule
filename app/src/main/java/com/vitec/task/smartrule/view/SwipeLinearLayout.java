package com.vitec.task.smartrule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SwipeLinearLayout extends LinearLayout {

    private float preX;
    private float preY;

    public SwipeLinearLayout(Context context) {
        super(context);
    }

    public SwipeLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = ev.getRawX();
                preY = ev.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                float deltX = ev.getRawX() - preX;
                float deltY = ev.getRawY() - preY;
                float finalDelta = Math.abs(deltX) >= Math.abs(deltY) ? Math.abs(deltX) : Math.abs(deltY);
                if (finalDelta < 50) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
