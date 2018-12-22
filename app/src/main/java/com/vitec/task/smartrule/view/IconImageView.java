package com.vitec.task.smartrule.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.vitec.task.smartrule.utils.LogUtils;

@SuppressLint("AppCompatCustomView")
public class IconImageView extends ImageView {

    private float preX;
    private float preY;
    private String text;
    private Paint mPaint;


    public IconImageView(Context context) {
        super(context);
        initPaint();
    }

    public IconImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        LogUtils.show("调用了onDraw-------方法："+text);
        mPaint.getTextBounds(text, 0, text.length(), rect);
        int x = getWidth()/2 - rect.centerX();
        int y = getHeight()/2 - rect.centerY();
        LogUtils.show("onDraw-----查看x,y:"+x+","+y);
        canvas.drawText(this.text, x, y, mPaint);

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        LogUtils.show("setText-----设置文字内容");
//        invalidate();
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

    private void initPaint() {
        this.mPaint = new Paint();
        this.mPaint.setTextSize(40);
        this.mPaint.setColor(Color.WHITE);
        this.mPaint.setStyle(Paint.Style.STROKE);

    }
}
