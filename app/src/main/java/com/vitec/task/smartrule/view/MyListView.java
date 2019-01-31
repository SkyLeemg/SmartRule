package com.vitec.task.smartrule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.Scroller;

public class MyListView extends ListView {

    private int lastX;
    private int lastY;
    private Scroller mScroller;

    public MyListView(Context context) {
        super(context);
        mScroller = new Scroller(getContext());
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(getContext());
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(getContext());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                int deltaX = x - lastX;
                int deltaY = y - lastY;
//                if (mScroller.isFinished()) {
//                    getParent().requestDisallowInterceptTouchEvent(false);
//                }
                //向下滑动
                if (y > lastY) {
                    if (canScrollList(-1)) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                } else {
                    //向上滑动
                    if (canScrollList(1)) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                }

                break;

        }
        lastX= (int) ev.getX();
        lastY = (int) ev.getY();
        return super.dispatchTouchEvent(ev);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        getParent().requestDisallowInterceptTouchEvent(true);
//        return super.onInterceptTouchEvent(ev);
//    }
}
