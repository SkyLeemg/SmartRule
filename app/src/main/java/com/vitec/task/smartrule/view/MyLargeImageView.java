package com.vitec.task.smartrule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.vitec.task.smartrule.interfaces.ViewTouchCallBack;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.view.large_img.LargeImageView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyLargeImageView extends LargeImageView {

    private  GestureDetector gestureDetector;
    private  ScaleGestureDetector scaleGestureDetector;
    private ViewTouchCallBack touchCallBack;
    private Field scrollerFiled;
    private Class largeClass;
    private Class scrollerClass;
    private Object scrollerObject;

    public MyLargeImageView(Context context) {
        super(context);
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(context, simpleOnGestureListener);
            initClass();
        }
    }

    public MyLargeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(context, simpleOnGestureListener);
            initClass();
        }
    }

    public MyLargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(context, simpleOnGestureListener);
            initClass();
        }
//        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
    }

    private void initClass() {
        try {
            largeClass = Class.forName("com.shizhefei.view.largeimage.LargeImageView");
            scrollerFiled = largeClass.getDeclaredField("mScroller");
            LogUtils.show("获取到mScroller啦："+ scrollerFiled.getName());
            scrollerClass = scrollerFiled.getClass();

            LogUtils.show("获取到scrollerClass啦："+ scrollerClass.getName());
            Constructor constructor = scrollerClass.getDeclaredConstructor();
            LogUtils.show("获取到Constructor啦："+ constructor.getName());
            scrollerObject = constructor.newInstance();
            LogUtils.show("获取到scrollerObject....newInstance啦：");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            LogUtils.show("获取失败了----"+e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
            LogUtils.show("获取实例错误："+e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            LogUtils.show("获取实例错误："+e.getMessage());
        }
//        catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
    }


    @Override
    public void computeScroll() {
        int oldY = getScrollY();
        int oldX = getScrollX();
        try {
            Method methodX = scrollerClass.getMethod("getCurrX");
            int x = (int) methodX.invoke(scrollerObject);
            LogUtils.show("获取到了X的值啊啊啊-----："+x);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        super.computeScroll();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (getScale() > 1) {
            gestureDetector.onTouchEvent(event);
        }
        if (event.getPointerCount() == 2) {
//            touchCallBack.onScale(getScale(),event.getX(0),event.getY(0));
        }

        return result;
    }

    public ViewTouchCallBack getTouchCallBack() {
        return touchCallBack;
    }

    public void setTouchCallBack(ViewTouchCallBack touchCallBack) {
        this.touchCallBack = touchCallBack;
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

    private  GestureDetector.SimpleOnGestureListener simpleOnGestureListener=new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            overScrollByCompat((int) distanceX, (int) distanceY, getScrollX(), getScrollY(), getScrollRangeX(), getScrollRangeY(), 0, 0, false);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    private boolean overScrollByCompat(int deltaX, int deltaY,
                                       int scrollX, int scrollY,
                                       int scrollRangeX, int scrollRangeY,
                                       int maxOverScrollX, int maxOverScrollY,
                                       boolean isTouchEvent) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();

        float distanceX=deltaX;
        float distanceY=deltaY;
        int newScrollX = scrollX;

        newScrollX += deltaX;

        int newScrollY = scrollY;

        newScrollY += deltaY;

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
            LogUtils.show("overScrollByCompat----边界----到达右边");
            distanceX = 0;
//            distanceX = oldScrollX-right;
        } else if (newScrollX < left) {
            newScrollX = left;
            LogUtils.show("overScrollByCompat----边界----到达左部");
            clampedX = true;
            distanceX = 0;
//            distanceX = left-oldScrollX;

        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            distanceY = 0;
//            distanceY = bottom-oldScrollY;
            clampedY = true;
            LogUtils.show("overScrollByCompat----边界----到达底部");
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
            distanceY = 0;
//            distanceY = top-oldScrollY;
            LogUtils.show("overScrollByCompat----边界----到达顶部");
        }

        if (newScrollX < 0) {
            newScrollX = 0;
        }
        if (newScrollY < 0) {
            newScrollY = 0;
        }

//        LogUtils.show("onScroll---查看滑动距离：" + distanceX + "," + distanceY);
        if (touchCallBack != null && (distanceX!=0 || distanceY!=0)) {

            touchCallBack.onScroll(distanceX, distanceY);
            LogUtils.show("符合条件的----onScroll---查看滑动距离：" + distanceX + "," + distanceY);
        }
//        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return getScrollX() - oldScrollX == deltaX || getScrollY() - oldScrollY == deltaY;
    }

    private int getScrollRangeX() {
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        return (getContentWidth() - contentWidth);
    }

    private int getScrollRangeY() {
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        return getContentHeight() - contentHeight;
    }

}
