package com.vitec.task.smartrule.view;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.vitec.task.smartrule.utils.LogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ZoomMoveFrameLayout extends FrameLayout {

    private PointF currentCenter = new PointF();
    private PointF preCurrentCenter = null;
    private float prevDistance = Float.MIN_VALUE;
    public float totalScale = 1f;
    private List<View> childViewList = new LinkedList<>();
    /*******触摸点点距队列*******/
    private Queue<Float> touchDistanceQueue = new LinkedBlockingQueue<>();
    private float minScale = 1f;
    private float maxScale = 4f;
    private float fixScale = 1f;
    private ScaleGestureDetector scaleGestureDetector;

    public ZoomMoveFrameLayout(@NonNull Context context) {
        super(context);
        initData(context);
    }

    public ZoomMoveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData(context);

    }

    public ZoomMoveFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context);
    }

    private void initData(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                preCurrentCenter.x = event.getX();
//                preCurrentCenter.y = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
//                LogUtils.show("ZoomMoveFrameLayout----ACTION_MOVE");
//                奇序号点平均值
//                PointF oddAveragePoint = new PointF();
////                偶序号点平均值
//                PointF evenAveragePoint = new PointF();
//                for (int i=0;i<event.getPointerCount();i++) {
//                    currentCenter.x += event.getX(i);
//                    currentCenter.y += event.getY(i);
//                }
//                currentCenter.x /= event.getPointerCount();
//                currentCenter.y /= event.getPointerCount();

                if (event.getPointerCount() > 1) {
//                    int oddCount = 0;
//                    int eventCount = 0;
//                    for (int i = 0; i < event.getPointerCount(); i++) {
//                        if ((i + 1) % 2 == 0) {
//                            evenAveragePoint.x += event.getX(i);
//                            evenAveragePoint.y += event.getY(i);
//                            eventCount++;
//                        } else {
//                            oddAveragePoint.x += event.getX(i);
//                            oddAveragePoint.y += event.getY(i);
//                            oddCount++;
//                        }
//                    }
//                    evenAveragePoint.x /= eventCount;
//                    evenAveragePoint.y /= eventCount;
//                    oddAveragePoint.x /= oddCount;
//                    oddAveragePoint.y /= oddCount;
//
//                    Path path = new Path();
//                    for (int i = 0; i < event.getPointerCount(); i++) {
//                        for (int j = 0; j < event.getPointerCount(); j++) {
//                            path.moveTo(event.getX(i), event.getY(i));
//                            path.lineTo(event.getX(j), event.getY(j));
//                        }
//                    }
//
//                    RectF rectF = new RectF();
//                    path.computeBounds(rectF, false);
//                    float distance = (rectF.width() + rectF.height()) * 2;
//                    touchDistanceQueue.add(distance);
////                    平滑处理
//                    int size = 4;
//                    if (touchDistanceQueue.size() >= size) {
//                        float dTotalAverage = 0f;
//                        for (Float d : touchDistanceQueue) {
//                            dTotalAverage += d;
//                            dTotalAverage += 200;
//                        }
//                        dTotalAverage /= touchDistanceQueue.size();
////                        判断是多点漫游还是多点缩放
//                        float distanceX = currentCenter.x - preCurrentCenter.x;
//                        float distanceY = currentCenter.y - preCurrentCenter.y;
//
//                        if (Math.abs(distanceX) > 2 || Math.abs(distanceY) > 2) {
////                            平移
//                            transLate(distanceX, distanceY);
//                        } else {
//                            if (prevDistance != Float.MIN_VALUE) {
////                                缩放
//                                float scale = dTotalAverage / prevDistance;
//                                totalScale *= dTotalAverage / prevDistance;
//                                scale(scale, currentCenter.x, currentCenter.y);
//                            }
//                            prevDistance = dTotalAverage;
//                        }
//                        while (touchDistanceQueue.size() > size) {
//                            touchDistanceQueue.poll();
//                        }
//
//                    }

                } else {
                    currentCenter.x = event.getX();
                    currentCenter.y = event.getY();
                    if (preCurrentCenter != null) {
                        float distanceX = currentCenter.x - preCurrentCenter.x;
                        float distanceY = currentCenter.y - preCurrentCenter.y;
                        transLate(distanceX, distanceY);
                    }
                }
                preCurrentCenter = currentCenter;
                currentCenter = new PointF();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                touchDistanceQueue.clear();
                preCurrentCenter = null;
                prevDistance = Float.MIN_VALUE;
                break;
        }
        return true;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        childViewList.add(child);
        LogUtils.show("addView-----添加了一个view");
    }

    public void addChildView(View child) {
        childViewList.add(child);
        LogUtils.show("addView-----添加了一个view");
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        boolean result = childViewList.remove(view);
        LogUtils.show("removeView----删除了一个view："+result);
    }

    /**View移动
     * @param dx x轴偏移多少
     * @param dy y轴偏移多少 **/
    public void transLate(float dx, float dy) {
        for (View view : childViewList) {
            view.setX(view.getX() + (dx));
            view.setY(view.getY() + (dy));
        }
    }


    /**
     * 用View中心对缩放中心进行缩放的办法
     * @param scale 缩放为上一次的百分之几
     * @param px,py 缩放中心，图标们向哪个坐标收缩聚拢
     **/
    public void scale(float scale, float px, float py) {
//        LogUtils.show("scale---查看缩放间隔："+scale+",坐标："+px+","+py);
        for (View view : childViewList) {
            //以本View中心点为缩放中心缩放
            view.setScaleX(view.getScaleX() * scale);
            view.setScaleY(view.getScaleY() * scale);
            //求本view中心点在屏幕中的坐标
            float centerX = view.getX() + view.getWidth() / 2;
            float centerY = view.getY() + view.getHeight() / 2;
            /**向缩放中心靠拢，例如缩放为原来的80%，那么缩放中心x到view中心x的距离则为0.8*(缩放中心x - view中心x),
             * 那么view的x距离屏幕左边框的距离则为   view中心x + (1 - 0.8) * (缩放x - view中心x)  ****/
            float leftBoardAfterScale = centerX + (px - centerX) * (1 - scale);
            float topBoardAfterScale = centerY + (py - centerY) * (1 - scale);
            view.setX(leftBoardAfterScale - view.getWidth() / 2);
            view.setY(topBoardAfterScale - view.getHeight() / 2);
//            viewFind(view, this.scale);
        }
    }


    private ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener=new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
//            LogUtils.show("onScaleGestureListener---打印查看缩放间隔：" + scaleGestureDetector.getScaleFactor());
            LogUtils.show("onScaleGestureListener---打印查看缩放坐标：" + scaleGestureDetector.getFocusX() + "," + scaleGestureDetector.getFocusY());
            scale(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
//            LogUtils.show("onScaleEnd---打印查看抬起时的缩放间隔：" + scaleGestureDetector.getScaleFactor());
//            LogUtils.show("onScaleEnd---打印查看抬起时的缩放坐标：" + scaleGestureDetector.getFocusX() + "," + scaleGestureDetector.getFocusY());
//            scale(scaleGestureDetector.getScaleFactor(), scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
        }
    };

}
