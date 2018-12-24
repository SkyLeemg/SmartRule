package com.vitec.task.smartrule.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.ImageTestActivity2;
import com.vitec.task.smartrule.bean.IconViewBean;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommonEditPicView extends RelativeLayout implements View.OnClickListener {

    private ZoomMoveFrameLayout zoomMoveFrameLayout;
    private RelativeLayout rlAddVertical;//添加垂直度图标
    private RelativeLayout rlAddLevel;//添加水平度图标
    private RelativeLayout rlMoreMenu;//更多菜单
    private RelativeLayout rlSavePic;//保存按钮
    private ImageView mImageView;//图纸
    private Bitmap imgBitmap;//图纸的bitmap图像

    private float srcScaleX;//原始缩放比例
    private float srcScaleY;
    private PointF srcCenterPoint;//原始图纸的中心点坐标
    private float imgScale;//图像bitmap与ZoomMoveFrameLayout的比例
    private int zoomWidth;
    private int zoomHeight;

    private List<IconImageView> verticalIconList;//垂直度图标集合
    private List<IconImageView> levelIconList;//水平度图标集合

    private Context context;


    public CommonEditPicView(Context context) {
        super(context);
        this.context = context;
        initView(context);
    }

    public CommonEditPicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(context);
    }

    public CommonEditPicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_common_edit_measure_pic,this);
        zoomMoveFrameLayout = findViewById(R.id.zoom_move_frame_layout);
        rlAddLevel = findViewById(R.id.rl_add_level);
        rlAddVertical = findViewById(R.id.rl_add_vertical);
        rlMoreMenu = findViewById(R.id.rl_menu_more);
        rlSavePic = findViewById(R.id.rl_save_pic);

        rlSavePic.setOnClickListener(this);
        rlAddVertical.setOnClickListener(this);
        rlMoreMenu.setOnClickListener(this);
        rlAddLevel.setOnClickListener(this);

        levelIconList = new ArrayList<>();
        verticalIconList = new ArrayList<>();
    }


    /**
     * 设置图纸的图片
     */
    public void setmImageView(Context context) {
        zoomWidth = zoomMoveFrameLayout.getWidth();
        zoomHeight = zoomMoveFrameLayout.getHeight();
        if (mImageView == null) {
            try {
                mImageView = new ImageView(context);
                InputStream inputStream = context.getAssets().open("paper.png");
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                this.imgBitmap = Bitmap.createBitmap(imgBitmap);
                LogUtils.show("查看FrameLayout的长宽：" +zoomWidth+ "," + zoomHeight);

                LogUtils.show("查看bitmap的图片大小：" + imgBitmap.getWidth() + "," + imgBitmap.getHeight());
//            按比例设置图片的长宽
                float imgWidth, imgHeight;
                if ((imgBitmap.getWidth() / imgBitmap.getHeight()) >= (zoomWidth / zoomHeight)) {
//                imgHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
                    float bitmapWidth = imgBitmap.getWidth();
                    float frameWidth = zoomMoveFrameLayout.getWidth();
                    imgScale = bitmapWidth / frameWidth;
                    imgHeight = imgBitmap.getHeight() / imgScale;
                    imgWidth = zoomMoveFrameLayout.getWidth();
                } else {
//                imgWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
                    float bitmapHeight = imgBitmap.getHeight();
                    float frameHeight = zoomMoveFrameLayout.getHeight();
                    imgScale = bitmapHeight / frameHeight;
                    imgWidth = imgBitmap.getWidth() / imgScale;
                    imgHeight = zoomMoveFrameLayout.getHeight();
                    LogUtils.show("查看比例值：" + imgScale);
                }
                mImageView.setImageBitmap(imgBitmap);
                LogUtils.show("查看计算后的图片长宽：" + imgWidth + "," + imgHeight);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) imgWidth, (int) imgHeight);
//            lp.gravity = Gravity.CENTER;
                mImageView.setLayoutParams(lp);
//            将图片添加到frameLayout中
                zoomMoveFrameLayout.addView(mImageView);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//        初始化原始的缩放倍率，之后复位用
                    srcScaleX = mImageView.getScaleX();
                    srcScaleY = mImageView.getScaleY();
                    LogUtils.show("查看初始化后，大图的缩放值：" + srcScaleX + "," + srcScaleY);
//        初始化原始大图的中心点坐标
                    srcCenterPoint = new PointF();
                    srcCenterPoint.x = mImageView.getX() + (mImageView.getWidth() / 2);
                    srcCenterPoint.y = mImageView.getY() + (mImageView.getHeight() / 2);
                    if (srcCenterPoint.x != zoomWidth / 2 || srcCenterPoint.y != zoomHeight/ 2) {
                        float deltax = srcCenterPoint.x - zoomWidth / 2;
                        float deltaY = srcCenterPoint.y - zoomHeight / 2;
                        RectF rectF = new RectF();
                        rectF.left = mImageView.getLeft() - deltax;
                        rectF.top = mImageView.getTop() - deltaY;
                        rectF.right = mImageView.getRight() - deltax;
                        rectF.bottom = mImageView.getBottom() - deltaY;
                        mImageView.setX(rectF.left);
                        mImageView.setY(rectF.top);
                        srcCenterPoint.x = mImageView.getX() + (mImageView.getWidth() / 2);
                        srcCenterPoint.y = mImageView.getY() + (mImageView.getHeight() / 2);
//                    myImageView.layout(rectF.left, rectF.top, rectF.right, rectF.bottom);
                    }
                    LogUtils.show("查看初始化后，大图的中心点坐标：" + srcCenterPoint.x + "," + srcCenterPoint.y);
                    LogUtils.show("查看原始的长宽：" + mImageView.getWidth() + "," + mImageView.getHeight());
                    LogUtils.show("查看大图的右上角坐标：" + mImageView.getLeft() + "," + mImageView.getTop() + ",XY坐标：" + mImageView.getX() + "," + mImageView.getY());
                }
            }, 100);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 添加水平度图标
             */
            case R.id.rl_add_level:
                IconImageView imageView = new IconImageView(context);
                String text = levelIconList.size() + 1+"";
                imageView.setText(text);
                imageView.setImageResource(R.mipmap.icon_measure_lever);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(100, 100));
                imageView.setLayoutParams(lp);
                zoomMoveFrameLayout.addView(imageView);
                levelIconList.add(imageView);
                imageView.setX(srcCenterPoint.x);
                imageView.setY(srcScaleY);
                break;

            /**
             * 添加垂直度图标
             */
            case R.id.rl_add_vertical:

                IconImageView leverImg = new IconImageView(context);
                String levertext = levelIconList.size() + 1+"";
                leverImg.setText(levertext);
                leverImg.setImageResource(R.mipmap.icon_measure_vertical);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(100, 100));
                leverImg.setLayoutParams(layoutParams);
                zoomMoveFrameLayout.addView(leverImg);
                levelIconList.add(leverImg);
                leverImg.setX(srcCenterPoint.x);
                leverImg.setY(srcCenterPoint.y);
                break;

            /**
             * 保存图片
             */
            case R.id.rl_save_pic:
                if (mImageView != null) {
                    /*******把图片缩为原来的大小********/
                    LogUtils.show("查看当前缩放值：" + mImageView.getScaleX() + "," + mImageView.getScaleY());
                    float currentScale = mImageView.getScaleX();
                    float deltaScale = srcScaleX / currentScale;
                    zoomMoveFrameLayout.scale(deltaScale, srcCenterPoint.x, srcCenterPoint.y);

                    /*********把图片挪回中间*********/
                    PointF currentPoint = new PointF();
                    currentPoint.x = mImageView.getX() + (mImageView.getWidth() / 2);
                    currentPoint.y = mImageView.getY() + (mImageView.getHeight() / 2);
                    LogUtils.show("当前中心点位置：" + currentPoint.x + "," + currentPoint.y);
                    PointF deltaPoint = new PointF();
//            计算偏移值
                    deltaPoint.x = currentPoint.x - srcCenterPoint.x;
                    deltaPoint.y = currentPoint.y - srcCenterPoint.y;
//            开始平移
                    zoomMoveFrameLayout.transLate(deltaPoint.x * -1, deltaPoint.y * -1);
//            合并图像
                    Bitmap newBitmap = saveBitmap();
                    String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
                    String imgFileName = DateFormatUtil.formatDate(new Date(), "yyyyMMddHHmmSS")+".jpg";

                    File imgFile = new File(path, imgFileName);
                    if (!imgFile.getParentFile().exists()) {
                        imgFile.getParentFile().mkdir();
                    }
                    if (imgFile.exists()) {
                        imgFile.delete();
                    }
                    try {
                        imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(imgFile);
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.flush();
                        fos.close();
                        Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
                        LogUtils.show("查看保存的目录："+imgFile.getPath());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mImageView.setImageBitmap(newBitmap);
                    for (int i=0;i<levelIconList.size();i++) {
                        zoomMoveFrameLayout.removeView(levelIconList.get(i));
                    }
                    for (int i=0;i<verticalIconList.size();i++) {
                        zoomMoveFrameLayout.removeView(verticalIconList.get(i));
                    }
                    levelIconList.clear();
                    verticalIconList.clear();

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
                break;

            /**
             * 其他菜单栏
             */
            case R.id.rl_menu_more:

                break;
        }

    }

    /**
     * 合成图像
     * @return
     */
    private Bitmap saveBitmap() {
        if (levelIconList.size() > 0 || verticalIconList.size() > 0) {

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
            if (levelIconList.size() > 0) {
                cv = cavasIcon(cv, levelIconList);
            }
            if (verticalIconList.size() > 0) {
                cv = cavasIcon(cv, verticalIconList);
            }
            //                    计算图片比例
//            float sizeScale = (float) srcWidth / (float)myImageView.getWidth();
            LogUtils.show("查看图片和画布比例：" + srcWidth + ",宽度：" + mImageView.getWidth() + ",比例：");

            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            imgBitmap = newBitmap;
            return newBitmap;
        }
        return null;
    }

    /**
     * 将小图标与图纸合并
     * @param canvas
     * @param iconImageViews
     * @return
     */
    private Canvas cavasIcon(Canvas canvas, List<IconImageView> iconImageViews) {

        if (iconImageViews.size() > 0) {
            for (int i = 0; i < iconImageViews.size(); i++) {
                IconImageView iconImageView = iconImageViews.get(i);
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
                float left = (iconImageView.getX() - mImageView.getX()) * imgScale;
                float top = (iconImageView.getY() - mImageView.getY()) * imgScale;
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

                canvas.drawBitmap(newIconBitmap,left,top,null);
                iconImageView.setDrawingCacheEnabled(false);
                iconBitmap.recycle();
                newIconBitmap.recycle();
            }
        }
        return canvas;
    }

    public ImageView getmImageView() {
        return mImageView;
    }


    public RelativeLayout getRlAddVertical() {
        return rlAddVertical;
    }

    public void setRlAddVertical(RelativeLayout rlAddVertical) {
        this.rlAddVertical = rlAddVertical;
    }

    public RelativeLayout getRlAddLevel() {
        return rlAddLevel;
    }

    public void setRlAddLevel(RelativeLayout rlAddLevel) {
        this.rlAddLevel = rlAddLevel;
    }

    public RelativeLayout getRlMoreMenu() {
        return rlMoreMenu;
    }

    public void setRlMoreMenu(RelativeLayout rlMoreMenu) {
        this.rlMoreMenu = rlMoreMenu;
    }

    public RelativeLayout getRlSavePic() {
        return rlSavePic;
    }

    public void setRlSavePic(RelativeLayout rlSavePic) {
        this.rlSavePic = rlSavePic;
    }



}
