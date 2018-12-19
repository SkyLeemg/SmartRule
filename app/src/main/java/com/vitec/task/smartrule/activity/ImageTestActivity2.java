package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.IconViewBean;
import com.vitec.task.smartrule.bean.ViewLayout;
import com.vitec.task.smartrule.interfaces.ViewTouchCallBack;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.view.IconImageView;
import com.vitec.task.smartrule.view.MyImageView;
import com.vitec.task.smartrule.view.large_img.LargeImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageTestActivity2 extends Activity implements ViewTouchCallBack{

    private FrameLayout frameLayout;
    private ToggleButton toggleButton;
    private Bitmap bitmap;
    private MyImageView myImageView;

    private List<IconViewBean> iconImgList;
    private float total = 1f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_new_measure);

        initView();

    }

    private void initView() {
        toggleButton = findViewById(R.id.toggleButton);
        frameLayout = findViewById(R.id.frame_layout);
        myImageView = findViewById(R.id.my_img_view);
        iconImgList = new ArrayList<>();
        initViewSetting();

    }

    private void initViewSetting() {
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("f.jpg");
            bitmap = BitmapFactory.decodeStream(inputStream);
            myImageView.setImageBitmap(bitmap);
            myImageView.setTouchCallBack(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    /**
     * 添加图标按钮
     * @param view
     */
//    ViewLayout layout ;
    public void addClick(View view) {
        InputStream inputStream = null;
//        try {
            if (iconImgList.size() > 0) {
                for (int i=0;i<iconImgList.size();i++) {
                    IconImageView img = iconImgList.get(i).getImageView();
                    int mx = (int) img.getX();
                    int my = (int) img.getY();
                    ViewLayout layout = new ViewLayout(mx-img.getWidth()/2,my-img.getHeight()/2,mx+img.getWidth()/2,my+img.getHeight()/2);
                    iconImgList.get(i).setLayout(layout);
                    LogUtils.show("按添加按钮前的坐标："+layout.getLeft()+","+layout.getTop()+","+layout.getRight()+","+layout.getBottom());
                }
            }
//            inputStream = getAssets().open("log.jpg");
//            Bitmap logoBitmap = BitmapFactory.decodeStream(inputStream);
            final IconImageView imageView = new IconImageView(ImageTestActivity2.this);
//            imageView.setImageBitmap(logoBitmap);
            imageView.setImageResource(R.mipmap.icon_measure_lever);
            imageView.setMaxWidth(150);
            imageView.setMaxHeight(150);
            imageView.layout(imageView.getLeft(),imageView.getTop(),imageView.getRight(),imageView.getBottom());
            frameLayout.addView(imageView);
            iconImgList.add(new IconViewBean(imageView,1));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (iconImgList.size() > 1) {
                        for (int i=0;i<iconImgList.size()-1;i++) {
                            ViewLayout layout = iconImgList.get(i).getLayout();
                            iconImgList.get(i).getImageView().layout(layout.getLeft(), layout.getTop(), layout.getRight(), layout.getBottom());
                        }
                    }

                }
            }, 200);


    }


    /**
     * 保存图片，将图标和图片合成
     * @param view
     */
    public void saveClick(View view) {

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
            Bitmap newBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(newBitmap);
//            先画背景图
            cv.drawBitmap(bitmap, 0, 0, null);
            for (int i=0;i<iconImgList.size();i++) {
                iconImgList.get(i).getImageView().setDrawingCacheEnabled(true);
                LogUtils.show("查看计算前的的坐标点left："+iconImgList.get(i).getImageView().getLeft()+",top："+iconImgList.get(i).getImageView().getTop());

                int left;
                int top;
//                left = (int) (iconImgList.get(i).getImageView().getLeft() *rate *largeImageView.getScale());
//                top = (int) (iconImgList.get(i).getImageView().getTop() *rate*largeImageView.getScale());
//                LogUtils.show("查看计算后的的坐标点left："+left+",top："+top);

//                cv.drawBitmap(iconImgList.get(i).getImageView().getDrawingCache(),left,top,null);
                iconImgList.get(i).getImageView().setDrawingCacheEnabled(false);
            }
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            return newBitmap;

        }
        return null;
    }



    /**
     * 平移
     * @param dx
     * @param dy
     */
    private void translateView(float dx, float dy) {
//        LogUtils.show("translateView-----移动的距离："+dx+","+dy);
        myImageView.setX(myImageView.getX() + (dx));
        myImageView.setY(myImageView.getY() + (dy));
        for (int i = 0; i < iconImgList.size(); i++) {

           iconImgList.get(i).getImageView().setX(iconImgList.get(i).getImageView().getX() + (dx));
           iconImgList.get(i).getImageView().setY(iconImgList.get(i).getImageView().getY() + (dy));

            LogUtils.show("translateView-----平移最终坐标："+(iconImgList.get(i).getImageView().getX()+dx)+","+(iconImgList.get(i).getImageView().getY()+dy));
        }
    }


    /**
     * 平移
     * @param dx
     * @param dy
     */
    private void translateView(float dx, float dy,int flag) {
//        LogUtils.show("translateView-----移动的距离："+dx+","+dy);
        for (int i = 0; i < iconImgList.size(); i++) {

            if (flag==0){
                iconImgList.get(i).getImageView().setX(iconImgList.get(i).getImageView().getX() + (dx));
                iconImgList.get(i).getImageView().setY(iconImgList.get(i).getImageView().getY() + (dy));
            }else if(flag==1){
                iconImgList.get(i).getImageView().setX(iconImgList.get(i).getImageView().getX() - (dx));
                iconImgList.get(i).getImageView().setY(iconImgList.get(i).getImageView().getY() - (dy));
            }

            LogUtils.show("translateView-----平移最终坐标："+(iconImgList.get(i).getImageView().getX()+dx)+","+(iconImgList.get(i).getImageView().getY()+dy));
        }
    }

    private void scaleView(float preScale,float scale, float px, float py) {
        total *= scale;
        scaleView(myImageView,scale,px,py);
    }

    private void scaleView(View view,float scale, float px, float py) {
        view.setScaleX(total);
        view.setScaleY(total);
        //求本view中心点在屏幕中的坐标
        float centerX = view.getX() + view.getWidth() / 2;
        float centerY = view.getY() + view.getHeight() / 2;
        /**向缩放中心靠拢，例如缩放为原来的80%，那么缩放中心x到view中心x的距离则为0.8*(缩放中心x - view中心x),
         * 那么view的x距离屏幕左边框的距离则为   view中心x + (1 - 0.8) * (缩放x - view中心x)  ****/
        float leftBoardAfterScale = centerX + (px - centerX) * (1 - scale);
        float topBoardAfterScale = centerY + (py - centerY) * (1 - scale);
        view.setX(leftBoardAfterScale - view.getWidth() / 2);
        view.setY(topBoardAfterScale - view.getHeight() / 2);
    }


    @Override
    public void onScroll(float distanceX, float distanceY) {
        translateView(distanceX, distanceY);
    }

    @Override
    public void onScroll(float distanceX, float distanceY, int flag) {
        translateView(distanceX,distanceY,flag);
    }

    @Override
    public void onScale(float preScale,float scale, float px, float py) {
        scaleView(preScale,scale,px,py);
    }

}
