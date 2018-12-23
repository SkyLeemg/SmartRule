package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.IconViewBean;
import com.vitec.task.smartrule.bean.ViewLayout;
import com.vitec.task.smartrule.interfaces.ViewTouchCallBack;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;
import com.vitec.task.smartrule.view.BottomDialog;
import com.vitec.task.smartrule.view.IconImageView;
import com.vitec.task.smartrule.view.MyImageView;
import com.vitec.task.smartrule.view.ZoomMoveFrameLayout;
import com.vitec.task.smartrule.view.large_img.LargeImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageTestActivity2 extends Activity {

    private ZoomMoveFrameLayout frameLayout;
//    private ToggleButton toggleButton;
//    private Bitmap bitmap;
    private ImageView myImageView;
//    private IconImageView iconImageView;

    private List<IconViewBean> iconImgList;
    private float total = 1f;
    private float srcScaleX;
    private float srcScaleY;
    private PointF srcCenterPoint;
    private Bitmap imgBitmap;
    private float imgScale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_new_measuretest);
        initView();

    }

    private void initView() {
//        toggleButton = findViewById(R.id.toggleButton);
        frameLayout = findViewById(R.id.frame_layout);
        iconImgList = new ArrayList<>();

    }


    /**
     * 添加图标按钮
     *
     * @param view
     */
    public void addClick(View view) {
        final IconImageView imageView = new IconImageView(ImageTestActivity2.this);
        String text = iconImgList.size() + 1+"";
        imageView.setText(text);
        imageView.setImageResource(R.mipmap.icon_measure_lever);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(100, 100));
        imageView.setLayoutParams(lp);
        frameLayout.addView(imageView);
        iconImgList.add(new IconViewBean(imageView, 1));

    }


    /**
     * 保存图片，将图标和图片合成
     *
     * @param view
     */
    public void saveClick(View view) {
        if (myImageView != null) {
            /*******把图片缩为原来的大小********/
            LogUtils.show("查看当前缩放值：" + myImageView.getScaleX() + "," + myImageView.getScaleY());
            float currentScale = myImageView.getScaleX();
            float deltaScale = srcScaleX / currentScale;
            frameLayout.scale(deltaScale, srcCenterPoint.x, srcCenterPoint.y);

            /*********把图片挪回中间*********/
            PointF currentPoint = new PointF();
            currentPoint.x = myImageView.getX() + (myImageView.getWidth() / 2);
            currentPoint.y = myImageView.getY() + (myImageView.getHeight() / 2);
            LogUtils.show("当前中心点位置：" + currentPoint.x + "," + currentPoint.y);
            PointF deltaPoint = new PointF();
//            计算偏移值
            deltaPoint.x = currentPoint.x - srcCenterPoint.x;
            deltaPoint.y = currentPoint.y - srcCenterPoint.y;
//            开始平移
            frameLayout.transLate(deltaPoint.x * -1, deltaPoint.y * -1);
//            合并图像
            Bitmap newBitmap = saveBitmap();
            String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
            String imgFileName = DateFormatUtil.formatDate(new Date(), "yyyyMMddHHmmSS")+".jpg";

            File imgFile = new File(path, imgFileName);
            if (imgFile.exists()) {
                imgFile.delete();
            }
            try {
                FileOutputStream fos = new FileOutputStream(imgFile);
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
                Toast.makeText(getApplicationContext(),"保存成功",Toast.LENGTH_SHORT).show();
                LogUtils.show("查看保存的目录："+imgFile.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            myImageView.setImageBitmap(newBitmap);
            for (int i=0;i<iconImgList.size();i++) {
                frameLayout.removeView(iconImgList.get(i).getImageView());
            }
            iconImgList.clear();

//
//            LogUtils.show("查看保存时的长宽：" + myImageView.getWidth() + "," + myImageView.getHeight());
//            LogUtils.show("查看大图的右上角坐标：" + myImageView.getLeft() + "," + myImageView.getTop() + "，XY坐标：" + myImageView.getX() + "," + myImageView.getY());
//            if (iconImgList.size() > 0) {
//                for (IconViewBean bean : iconImgList) {
//                    LogUtils.show("查看小图标的右上角坐标：" + bean.getImageView().getX() + "," + bean.getImageView().getY());
//
//                }
//            }
////            LogUtils.show("查看屏幕的长款："+ ScreenSizeUtil.getScreenWidth(getApplicationContext()));
//            LogUtils.show("查看图片的偏距："+myImageView.getPaddingLeft()+"，"+myImageView.getPaddingRight());
        }
    }

    /**
     * 合成图像
     * @return
     */
    private Bitmap saveBitmap() {
        if (iconImgList.size() > 0) {
//            获取大图的bitmap
//            myImageView.setDrawingCacheEnabled(true);
//            Bitmap bitmap = myImageView.getDrawingCache();
//            获取大图的图片大小
            int srcWidth = imgBitmap.getWidth();
            int srcHeight = imgBitmap.getHeight();

            LogUtils.show("查看图片宽度：" + srcWidth + ",图片高度：" + srcHeight);
//            根据大图的图片大小创建一个bitmap容器
            Bitmap newBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(newBitmap);
//            先画背景图
            cv.drawBitmap(imgBitmap, 0, 0, null);
//            bitmap用完了之后再回收 不然会报错
//            myImageView.setDrawingCacheEnabled(false);
//            循环画小图标
            //                    计算图片比例
//            float sizeScale = (float) srcWidth / (float)myImageView.getWidth();
            LogUtils.show("查看图片和画布比例："+srcWidth+",宽度："+myImageView.getWidth()+",比例：");

            if (iconImgList.size() > 0) {
                for (int i = 0; i < iconImgList.size(); i++) {
                    IconImageView iconImageView = iconImgList.get(i).getImageView();
                    iconImageView.setDrawingCacheEnabled(true);
                    /**
                     * 1.获取图标的bitmap
                     * 2.把图标按照getScale的参数进行放大缩小
                     * 3.再按照画布比例进行放大缩小
                     */
                    Bitmap iconBitmap = iconImageView.getDrawingCache();
                    LogUtils.show("查看iconBitmap的大小：" + iconBitmap.getWidth() + "," + iconBitmap.getHeight()+",缩放倍率："+iconImageView.getScaleX()+","+iconImageView.getScaleY());
                    LogUtils.show("查看计算前的的坐标点left：" + iconImageView.getX() + ",top：" + iconImageView.getY());
                    float iconWidth = (iconImageView.getWidth() * iconImageView.getScaleX() * imgScale);
                    float iconHeight = (iconImageView.getHeight() * iconImageView.getScaleY() * imgScale);
                    int iw = (int) iconWidth;
                    int ih = (int) iconHeight;
                    Bitmap newIconBitmap = Bitmap.createScaledBitmap(iconBitmap, iw, ih, true);
                    LogUtils.show("查看计算后的图标大小：" + iconWidth + "," + iconHeight);
                    float left = (iconImageView.getX() - myImageView.getX()) * imgScale;
                    float top = (iconImageView.getY() - myImageView.getY()) * imgScale;
                    /**
                     * 1.如果原始宽度不是整型则会画布大小会变小，影响位置，要将位置挪到中间
                     * 2.如果图标有缩小的话，那么图标的坐标还要加上原始宽度与缩放后的宽度除二
                     *   因为我们获取到的左上角的坐标是原始宽度的坐标
                     * **/
                    left += ((iconImageView.getWidth() * imgScale - iw) / 2);
                    top += ((iconImageView.getHeight() * imgScale - ih) / 2);
                    if (iconWidth > iw) {
                        float deltaW = iconWidth - iw;
                        left += (deltaW / 2);
                    }
                    if (iconHeight > ih) {
                        float deltaH = iconHeight - ih;
                        top += (deltaH /2);
                    }
//                LogUtils.show("查看计算后的的坐标点left："+left+",top："+top);

                    cv.drawBitmap(newIconBitmap,left,top,null);
                    iconImageView.setDrawingCacheEnabled(false);
                    iconBitmap = null;
                    newIconBitmap = null;
                }
            }
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            imgBitmap = newBitmap;
            return newBitmap;

        }
        return null;
    }



    public void delIconClick(View view) {
        if (iconImgList.size() > 0) {
            int index = iconImgList.size() - 1;
            frameLayout.removeView(iconImgList.get(index).getImageView());
            iconImgList.remove(index);
            Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * 添加大图
     * @param view
     */
    public void uploadImgClick(View view) {
        if (myImageView == null) {
            try {
                myImageView = new ImageView(ImageTestActivity2.this);
                InputStream inputStream = getAssets().open("paper.png");
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                this.imgBitmap = Bitmap.createBitmap(imgBitmap);
                LogUtils.show("查看FrameLayout的长宽：" + frameLayout.getWidth() + "," + frameLayout.getHeight());

                LogUtils.show("查看bitmap的图片大小：" + imgBitmap.getWidth() + "," + imgBitmap.getHeight());
//            按比例设置图片的长宽
                float imgWidth, imgHeight;
                if (imgBitmap.getWidth() / imgBitmap.getHeight() >= frameLayout.getWidth() / frameLayout.getHeight()) {
//                imgHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
                    float bitmapWidth = imgBitmap.getWidth();
                    float frameWidth = frameLayout.getWidth();
                    imgScale = bitmapWidth / frameWidth;
                    imgHeight = imgBitmap.getHeight() / imgScale;
                    imgWidth = frameLayout.getWidth();
                } else {
//                imgWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
                    float bitmapHeight = imgBitmap.getHeight();
                    float frameHeight = frameLayout.getHeight();
                    imgScale = bitmapHeight / frameHeight;
                    imgWidth = imgBitmap.getWidth() / imgScale;
                    imgHeight = frameLayout.getHeight();
                    LogUtils.show("查看比例值：" + imgScale);
                }
                myImageView.setImageBitmap(imgBitmap);
                LogUtils.show("查看计算后的图片长宽：" + imgWidth + "," + imgHeight);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) imgWidth, (int) imgHeight);
//            lp.gravity = Gravity.CENTER;
                myImageView.setLayoutParams(lp);
//            将图片添加到frameLayout中
                frameLayout.addView(myImageView);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//        初始化原始的缩放倍率，之后复位用
                    srcScaleX = myImageView.getScaleX();
                    srcScaleY = myImageView.getScaleY();
                    LogUtils.show("查看初始化后，大图的缩放值：" + srcScaleX + "," + srcScaleY);
//        初始化原始大图的中心点坐标
                    srcCenterPoint = new PointF();
                    srcCenterPoint.x = myImageView.getX() + (myImageView.getWidth() / 2);
                    srcCenterPoint.y = myImageView.getY() + (myImageView.getHeight() / 2);
                    if (srcCenterPoint.x != frameLayout.getWidth() / 2 || srcCenterPoint.y != frameLayout.getHeight() / 2) {
                        float deltax = srcCenterPoint.x - frameLayout.getWidth() / 2;
                        float deltaY = srcCenterPoint.y - frameLayout.getHeight() / 2;
                        RectF rectF = new RectF();
                        rectF.left = myImageView.getLeft() - deltax;
                        rectF.top = myImageView.getTop() - deltaY;
                        rectF.right = myImageView.getRight() - deltax;
                        rectF.bottom = myImageView.getBottom() - deltaY;
                        myImageView.setX(rectF.left);
                        myImageView.setY(rectF.top);
                        srcCenterPoint.x = myImageView.getX() + (myImageView.getWidth() / 2);
                        srcCenterPoint.y = myImageView.getY() + (myImageView.getHeight() / 2);
//                    myImageView.layout(rectF.left, rectF.top, rectF.right, rectF.bottom);
                    }
                    LogUtils.show("查看初始化后，大图的中心点坐标：" + srcCenterPoint.x + "," + srcCenterPoint.y);
                    LogUtils.show("查看原始的长宽：" + myImageView.getWidth() + "," + myImageView.getHeight());
                    LogUtils.show("查看大图的右上角坐标：" + myImageView.getLeft() + "," + myImageView.getTop() + ",XY坐标：" + myImageView.getX() + "," + myImageView.getY());
                }
            }, 100);
        }


    }

    public void uploadPicTest(View view) {
        BottomDialog bottomDialog = new BottomDialog(ImageTestActivity2.this, R.style.BottomDialog);
        bottomDialog.show();

    }
}
