package com.vitec.task.smartrule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.shizhefei.view.largeimage.LargeImageView;
import com.vitec.task.smartrule.utils.LogUtils;

public class MyLargeImageView extends LargeImageView {

    public MyLargeImageView(Context context) {
        super(context);
    }

    public MyLargeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0) {
            return;
        }
        int drawOffsetX = 0;
        int drawOffsetY = 0;
        int contentWidth = getContentWidth();
        int contentHeight = getContentHeight();
        if (viewWidth > contentWidth) {
            drawOffsetX = (viewWidth - contentWidth) / 2;
        }
        if (viewHeight > contentHeight) {
            drawOffsetY = (viewHeight - contentHeight) / 2;
        }
//        LogUtils.show("查看获取到的绘画偏移量："+drawOffsetX+","+drawOffsetY);

    }

    private int getContentWidth() {
        if (hasLoad()) {
            return (int) (getMeasuredWidth() * super.getScale());
        }
        return 0;
    }

    private int getContentHeight() {
        if (hasLoad()) {
            if (getImageWidth() == 0) {
                return 0;
            }
            return (int) (1.0f * getMeasuredWidth() * getImageHeight() / getImageWidth() * super.getScale());
        }
        return 0;
    }
}
