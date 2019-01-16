package com.vitec.task.smartrule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class SwipeListView extends ListView {

    public SwipeListView(Context context) {
        super(context);
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override

    public boolean dispatchTouchEvent(MotionEvent event) {

        boolean result = super.dispatchTouchEvent(event);

        return result;

    }



    @Override

    public boolean onInterceptTouchEvent(MotionEvent event) {

        boolean result = super.onInterceptTouchEvent(event);

        return result;

    }



    @Override

    public boolean onTouchEvent(MotionEvent event) {

        boolean result = super.onTouchEvent(event);

        return result;

    }
}
