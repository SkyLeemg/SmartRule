package com.vitec.task.smartrule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.vitec.task.smartrule.interfaces.ViewTouchCallBack;

@SuppressLint("AppCompatCustomView")
public class MyImageView extends ImageView {


    private ViewTouchCallBack touchCallBack;
    private float preDistance;
    private float preX;
    private float preY;
    private float totalScale = 1;


    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public ViewTouchCallBack getTouchCallBack() {
        return touchCallBack;
    }

    public void setTouchCallBack(ViewTouchCallBack touchCallBack) {
        this.touchCallBack = touchCallBack;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touchCallBack != null) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    preX = event.getX();
                    preY = event.getY();
                    if(event.getPointerCount() > 1){
                        double pointdxPow2 = Math.pow(event.getX(1) - event.getX(0), 2);
                        double pointdyPow2 = Math.pow(event.getY(1) - event.getY(0), 2);
                        preDistance = (float) Math.sqrt(pointdxPow2 + pointdyPow2);

                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - preX;
                    float dy = event.getY() - preY;
                    preX = event.getX();
                    preY = event.getY();
                    touchCallBack.onScroll(dx,dy);
                    if(event.getPointerCount() > 1){
                        double pointdxPow2 = Math.pow(event.getX(1) - event.getX(0), 2);
                        double pointdyPow2 = Math.pow(event.getY(1) - event.getY(0), 2);
                        float nowDistance = (float) Math.sqrt(pointdxPow2 + pointdyPow2);
                        if(preDistance != Float.MIN_VALUE){
                            float scale = nowDistance / preDistance;
                            float preScale = totalScale;
                            totalScale *= scale;
                            touchCallBack.onScale(preScale,scale, event.getX(0), event.getY(0));
                        }
                        preDistance = nowDistance;
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    preDistance = Float.MIN_VALUE;
                    break;

            }
        }

        return true;

    }
}
