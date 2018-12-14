package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.shizhefei.view.largeimage.BlockImageLoader;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.UpdateImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;
import com.vitec.task.smartrule.view.IconImageView;
import com.vitec.task.smartrule.view.MyLargeImageView;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class ImageTestActivity extends Activity {

    private MyLargeImageView largeImageView;
    private RelativeLayout relativeLayout;
    private ToggleButton toggleButton;
    private Bitmap bitmap;

    private List<IconImageView> iconImgList;

    private float preX;//上一次的X坐标
    private float preY;//上一次的Y坐标
    private float preScale;//上一次的缩放比
    private float prevDistance;
    private float totalScale=1f;
    private float rate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_new_measure);

        initView();

    }

    private void initView() {
        largeImageView = findViewById(R.id.imageView);
        toggleButton = findViewById(R.id.toggleButton);
        relativeLayout = findViewById(R.id.relative);
        iconImgList = new ArrayList<>();
        initViewSetting();
    }

    private void initViewSetting() {
        toggleButton.setOnCheckedChangeListener(onCheckedChangeListener);
        largeImageView.setOnDoubleClickListener(onDoubleClickListener);

        try {
            InputStream inputStream = getAssets().open("f.jpg");
            bitmap = BitmapFactory.decodeStream(inputStream);
            largeImageView.setImage(bitmap);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    largeImageView.setScale(1f);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.show("查看scale：" + largeImageView.getScale()+", FitScale:"+largeImageView.getFitScale());

        largeImageView.setOnTouchListener(listener);

        rate = largeImageView.getImageWidth() / ScreenSizeUtil.getScreenWidth(getApplicationContext());

//        BlockImageLoader.OnImageLoadListener onImageLoadListener=largeImageView.getOnImageLoadListener();
//       largeImageView.

    }





    float preOffsetX;
    float preOffsetY;
    View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            boolean result = onTouchEvent(motionEvent);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtils.show("查看刚按下去的偏移坐标：" + motionEvent.getRawX() + "," + motionEvent.getX());
                    preX = motionEvent.getX();
                    preY = motionEvent.getY();
                    preOffsetX = largeImageView.getScrollX();
                    preOffsetY = largeImageView.getScrollY();
                    if (motionEvent.getPointerCount() > 1) {
                        double pointdxPow2 = Math.pow(motionEvent.getX(1) - motionEvent.getX(0), 2);
                        double pointdyPow2 = Math.pow(motionEvent.getY(1) - motionEvent.getY(0), 2);
                        prevDistance = (float) Math.sqrt(pointdxPow2 + pointdyPow2);

                    }
                    break;

                case MotionEvent.ACTION_MOVE:

                    float dx = motionEvent.getX() - preX;
                    float dy = motionEvent.getY() - preY;

                    preX = motionEvent.getX();
                    preY = motionEvent.getY();
                    LogUtils.show("手指的坐标："+preX+","+preY+"，手指计算出来的偏移量：" + dx + "," + dy);
                    float dOffsetX = -1*(largeImageView.getScrollX() - preOffsetX) * rate;
                    float dOffsetY = -1*(largeImageView.getScrollY() - preOffsetY) * rate;
                    LogUtils.show("父控件的偏移坐标："+largeImageView.getScrollX()+","+largeImageView.getScrollY()+",父控件的偏移量："+dOffsetX+","+dOffsetY);
                    preOffsetX = largeImageView.getScrollX();
                    preOffsetY = largeImageView.getScrollY();
                    if (motionEvent.getPointerCount() == 1 && largeImageView.getScale() > 1) {
                        translateView(dx, dy);
                    }
//
                    if (motionEvent.getPointerCount() > 1 && largeImageView.getScale() != preScale) {
//                        translateView(dx, dy);
                        double pointdxPow2 = Math.pow(motionEvent.getX(1) - motionEvent.getX(0), 2);
                        double pointdyPow2 = Math.pow(motionEvent.getY(1) - motionEvent.getY(0), 2);
                        float nowDistance = (float) Math.sqrt(pointdxPow2 + pointdyPow2);
                        if (prevDistance != Float.MIN_VALUE) {
                            float scale = nowDistance / prevDistance;
                            totalScale *= scale;
//                            scaleView(scale, motionEvent.getX(0), motionEvent.getY(0));
                        }
                        prevDistance = nowDistance;

                    }

                    preScale = largeImageView.getScale();
                    break;
                case MotionEvent.ACTION_UP:
                    prevDistance = Float.MIN_VALUE;
//                    totalScale = 1f;
                    LogUtils.show("查看抬起后的缩放比：" + largeImageView.getScale());
                    LogUtils.show("查看抬起的偏移坐标：" + motionEvent.getRawX() + "," + motionEvent.getRawY());
                    break;
            }
            return result;
        }
    };

    /**
     * 平移
     * @param dx
     * @param dy
     */
    private void translateView(float dx, float dy) {
        for (IconImageView view : iconImgList) {
            view.setX(view.getX() + (dx));
            view.setY(view.getY() + (dy));
        }
    }

    private void scaleView(float scale, float px, float py) {
        for (IconImageView view : iconImgList) {
            view.setScaleX(largeImageView.getScaleX());
            view.setScaleY(largeImageView.getScaleY());
            float centerX = view.getX() + view.getWidth() / 2;
            float centerY = view.getY() + view.getHeight() / 2;
            float leftBoardAfterScale = centerX + (px - centerX) * (1 - scale);
            float topBoardAfterScale = centerY + (py - centerY) * (1 - scale);
            view.setX(leftBoardAfterScale-view.getWidth()/2);
            view.setY(topBoardAfterScale-view.getHeight()/2);

        }
    }


    /**
     * 添加图标按钮
     * @param view
     */
    Layout layout ;
    public void addClick(View view) {
        InputStream inputStream = null;
        try {
            if (iconImgList.size() > 0) {
                IconImageView img = iconImgList.get(0);
                int mx = (int) img.getX();
                int my = (int) img.getY();
                layout = new Layout(mx-img.getWidth()/2,my-img.getHeight()/2,mx+img.getWidth()/2,my+img.getHeight()/2);
                LogUtils.show("按添加按钮前的坐标："+layout.getLeft()+","+layout.getTop()+","+layout.getRight()+","+layout.getBottom());
//                layout = new Layout(img.getTop(), img.getLeft(), img.getRight(), img.getBottom());

            }
            inputStream = getAssets().open("log.jpg");
            Bitmap logoBitmap = BitmapFactory.decodeStream(inputStream);
            final IconImageView imageView = new IconImageView(ImageTestActivity.this);
            imageView.setImageBitmap(logoBitmap);
            imageView.layout(imageView.getLeft(),imageView.getTop(),imageView.getRight(),imageView.getBottom());
            relativeLayout.addView(imageView);
            iconImgList.add(imageView);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (layout != null) {
                        LogUtils.show("按添加按钮后需要设置的坐标："+layout.getLeft()+","+layout.getTop()+","+layout.getRight()+","+layout.getBottom());

                        iconImgList.get(0).layout(layout.getLeft(), layout.getTop(), layout.getRight(), layout.getBottom());
                        LogUtils.show("设置后的坐标："+iconImgList.get(0).getLeft()+","+iconImgList.get(0).getTop()+","+iconImgList.get(0).getRight()+","+iconImgList.get(0).getBottom());
                    }
                }
            }, 200);




//            Bitmap cpBitmap = Bitmap.createBitmap(bitmap);
//            Bitmap newBitmap = createBitmap(cpBitmap, logoBitmap);
//            largeImageView.setImage(newBitmap);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    largeImageView.setScale(1f);
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    /**
     * 保存图片，将图标和图片合成
     * @param view
     */
    public void saveClick(View view) {
        LogUtils.show("查看scale：" + largeImageView.getScale()+", FitScale:"+largeImageView.getFitScale());
        bitmap = saveBitmap();
        largeImageView.setImage(bitmap);
        for (int i=0;i<iconImgList.size();i++) {
            relativeLayout.removeView(iconImgList.get(i));
        }
        iconImgList.clear();
        largeImageView.setScale(largeImageView.getScale());
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
    }

    private Bitmap createBitmap(Bitmap src, Bitmap watermark) {
        String tag = "createBitmap";
        if (src == null) {
            return null;
        }

        int w = src.getWidth();
        int h = src.getHeight();
        int ww = watermark.getWidth();
        int wh = watermark.getHeight();
        // create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        // draw src into
        cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
        // draw watermark into
        cv.drawBitmap(watermark, w - ww + 5, h - wh + 5, null);// 在src的右下角画入水印
        // save all clip
        cv.save(Canvas.ALL_SAVE_FLAG);// 保存
        // store
        cv.restore();// 存储
        return newb;
    }

    private Bitmap saveBitmap() {
        if (iconImgList.size() > 0) {
            int srcWidth = bitmap.getWidth();
            int srcHeight = bitmap.getHeight();


//            float rate = srcWidth / ScreenSizeUtil.getScreenWidth(getApplicationContext());
            float rate = 1.77778f;
//            LogUtils.show("查看计算出的比例值：" + rate);
            LogUtils.show("查看图片宽度：" + srcWidth + ",图片高度：" + srcHeight);
//            LogUtils.show("查看屏幕宽度："+ ScreenSizeUtil.getScreenWidth(getApplicationContext())+",屏幕高度："+ScreenSizeUtil.getScreenHeight(getApplicationContext()));
            LogUtils.show("查看控件的宽度："+largeImageView.getWidth()+",控件的高度："+largeImageView.getHeight()+",控件的其他四个点坐标："+largeImageView.getLeft()+"," +
                    ""+largeImageView.getTop()+","+largeImageView.getRight()+","+largeImageView.getBottom());
            int dif = (int) (largeImageView.getHeight() * rate-srcHeight) / 2;
            Bitmap newBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(newBitmap);
//            先画背景图
            cv.drawBitmap(bitmap, 0, 0, null);
            for (int i=0;i<iconImgList.size();i++) {
                iconImgList.get(i).setDrawingCacheEnabled(true);
                LogUtils.show("查看计算前的的坐标点left："+iconImgList.get(i).getLeft()+",top："+iconImgList.get(i).getTop());

                int left;
                int top;
                left = (int) (iconImgList.get(i).getLeft() *rate *largeImageView.getScale());
                top = (int) (iconImgList.get(i).getTop() *rate*largeImageView.getScale());
                LogUtils.show("查看计算后的的坐标点left："+left+",top："+top);

                cv.drawBitmap(iconImgList.get(i).getDrawingCache(),left,top,null);
                iconImgList.get(i).setDrawingCacheEnabled(false);
            }
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            return newBitmap;

        }
        return null;
    }





    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            largeImageView.setEnabled(!b);
        }
    };


    private LargeImageView.CriticalScaleValueHook criticalScaleValueHook=new LargeImageView.CriticalScaleValueHook() {
        @Override
        public float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale) {
            return 1;
        }

        @Override
        public float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale) {
            return 4;
        }
    };

    private LargeImageView.OnDoubleClickListener onDoubleClickListener=new LargeImageView.OnDoubleClickListener() {
        @Override
        public boolean onDoubleClick(LargeImageView view, MotionEvent event) {
            float fitScale = view.getFitScale();
            float maxScale = view.getMaxScale();
            float minScale = view.getMinScale();
            String message="双击事件 minScale:" + minScale + " maxScale:" + maxScale + " fitScale:" + fitScale;
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            //返回true 拦截双击缩放的事件
            return false;
        }
    };



    class Layout {
        int top;
        int left;
        int bottom;
        int right;

        public Layout(int left,int top,   int right, int bottom) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }
    }
}
