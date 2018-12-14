package com.vitec.task.smartrule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.vitec.task.smartrule.utils.LogUtils;

@SuppressLint("AppCompatCustomView")
public class IconImageView extends ImageView {

    private float preX;
    private float preY;
    public IconImageView(Context context) {
        super(context);
    }

    public IconImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    preX = event.getRawX();
                    preY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - preX;
                    float dy = event.getRawY() - preY;
                    setX(getX() + dx);
                    setY(getY() + dy);
                    preY = event.getRawY();
                    preX = event.getRawX();
//                layout(mx-getWidth()/2,my-getHeight()/2,mx+getWidth()/2,my+getHeight()/2);
                    break;
                case MotionEvent.ACTION_UP:
//                mx = (int) event.getRawX();
//                my = (int) event.getRawY();
//                LogUtils.show("查看最后移动的坐标get：" + getLeft()+ "," + getTop()  + "," + getRight() + "," + getBottom());
//                LogUtils.show("查看最后移动的坐标：" +(mx-getWidth()/2) + "," + (my-getHeight()/2) + "," + (mx+getWidth()/2) + "," + (my+getHeight()/2));


                    break;
            }
            return true;
        } else return false;


    }
}
