package com.vitec.task.smartrule.view;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.UserDatumActivity;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.fragment.MeasureFragment;
import com.vitec.task.smartrule.interfaces.IEditPicControler;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.UploadPicIntentService;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 图纸编辑模块
 * 1.可以添加图标
 * 2.保存图纸
 * 3.删除图标
 */
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
    private int verticalIndex = 0;//垂直度图标序号
    private int leverIndex = 0;//水平度图标序号
    private List<RulerCheckOptions> optionsList;

    private MeasureFragment fragment;
    private IEditPicControler editPicControler;
    private List<Integer> addLink;//图标添加的顺序记录，删除图标用,添加的1代表垂直度，2代表水平度

    private Context context;


    public CommonEditPicView(Context context,IEditPicControler editPicControler) {
        super(context);
        this.context = context;
        this.editPicControler = editPicControler;
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
        addLink = new ArrayList<>();
        optionsList = new ArrayList<>();
        optionsList = editPicControler.getCheckOptions();
        //初始化序号
        for (int i=0;i<optionsList.size();i++) {
            if (optionsList.get(i).getRulerOptions().getType() == 1) {
                verticalIndex = optionsList.get(i).getImgNumber();
                LogUtils.show("查看初始化垂直度的值：" + verticalIndex);
            } else if (optionsList.get(i).getRulerOptions().getType() == 2) {
                leverIndex = optionsList.get(i).getImgNumber();
                LogUtils.show("查看初始化平整度的值：" + verticalIndex);
            }
        }
    }

    /**
     * 设置fragment对象，用于调用fragment里面的方法
     * @param fragment
     */
    public void setFragment(MeasureFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * 设置图纸的图片
     */
    public void setmImageView(Context context,String path) {
        zoomWidth = zoomMoveFrameLayout.getWidth();
        zoomHeight = zoomMoveFrameLayout.getHeight();
        if (mImageView == null) {
//            try {
                mImageView = new ImageView(context);

//                InputStream inputStream = context.getAssets().open("paper.png");
//                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);

                Bitmap imgBitmap = BitmapFactory.decodeFile(path);
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
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

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
//                    zoomMoveFrameLayout.setInitX();
                }
            }, 100);

           saveBitmapToFile(imgBitmap);

        }

    }

    private void updateOptionData(RulerCheckOptions options) {
        ContentValues values = new ContentValues();
        values.put(DataBaseParams.measure_option_img_number, options.getImgNumber());
        String where = " id=?";
       int res= OperateDbUtil.updateOptionsDataToSqlite(context, DataBaseParams.measure_option_table_name, where, values, new String[]{String.valueOf(options.getId())});
        LogUtils.show("图片序号保存----查看序号:" + options.getImgNumber() + ",查看是否保存成功：" + res);

    }

    private void delUpdateOptionData() {
        for (int i=0;i<optionsList.size();i++) {
            optionsList.get(i).setImgNumber(0);
            ContentValues values = new ContentValues();
            values.put(DataBaseParams.measure_option_img_number, 0);
            values.put(DataBaseParams.measure_option_img_path, "");
            String where = " id=?";
            int res= OperateDbUtil.updateOptionsDataToSqlite(context, DataBaseParams.measure_option_table_name, where, values, new String[]{String.valueOf(optionsList.get(i).getId())});
            LogUtils.show("删除图纸----查看序号:" + optionsList.get(i).getImgNumber() + ",查看是否删除成功：" + res);
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
                leverIndex++;
                String text = leverIndex+"";
                imageView.setText(text);
                imageView.setImageResource(R.mipmap.icon_measure_lever);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(100, 100));
                imageView.setLayoutParams(lp);
                zoomMoveFrameLayout.addView(imageView);
                levelIconList.add(imageView);
                imageView.setX(srcCenterPoint.x);
                imageView.setY(srcCenterPoint.y);
                addLink.add(2);
                //
                for (int i=0;i<optionsList.size();i++) {
                    if (optionsList.get(i).getRulerOptions().getType() == 2) {
                        optionsList.get(i).setImgNumber(leverIndex);
                        updateOptionData(optionsList.get(i));
                    }
                }


                break;

            /**
             * 添加垂直度图标
             */
            case R.id.rl_add_vertical:

                IconImageView verImg = new IconImageView(context);
                verticalIndex++;
                String verrtext =verticalIndex+"";
                verImg.setText(verrtext);
                verImg.setImageResource(R.mipmap.icon_measure_vertical);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(100, 100));
                verImg.setLayoutParams(layoutParams);
                zoomMoveFrameLayout.addView(verImg);
                verticalIconList.add(verImg);
                verImg.setX(srcCenterPoint.x);
                verImg.setY(srcCenterPoint.y);
                addLink.add(1);
                for (int i=0;i<optionsList.size();i++) {
                    if (optionsList.get(i).getRulerOptions().getType() == 1) {
                        optionsList.get(i).setImgNumber(verticalIndex);
                        updateOptionData(optionsList.get(i));
                    }
                }

                break;

            /**
             *TODO FR保存图片
             */
            case R.id.rl_save_pic:
                if (mImageView != null && (verticalIconList.size() > 0 || levelIconList.size() > 0)) {
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
                    String filePath = saveBitmapToFile(newBitmap);
//                    上传文件到服务器
                    uploadPicToServer(filePath);
                    mImageView.setImageBitmap(newBitmap);
                    for (int i = 0; i < levelIconList.size(); i++) {
                        zoomMoveFrameLayout.removeView(levelIconList.get(i));
                    }
                    for (int i = 0; i < verticalIconList.size(); i++) {
                        zoomMoveFrameLayout.removeView(verticalIconList.get(i));
                    }
                    levelIconList.clear();
                    verticalIconList.clear();
                    addLink.clear();
                }
                break;

            /**
             * 其他菜单栏
             */
            case R.id.rl_menu_more:
                final PopupMenu menu = new PopupMenu(context, view);
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.menu_measure_more, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            /**
                             * 删除图标
                             */
                            case R.id.item_del_icon:
                                if (addLink.size() > 0) {
                                    int index = addLink.size() - 1;
                                    /**
                                     * 如果为1则删除垂直度的最后一个图标
                                     */
                                    if (addLink.get(index) == 1) {
                                        zoomMoveFrameLayout.removeView(verticalIconList.get(verticalIconList.size() - 1));
                                        verticalIconList.remove(verticalIconList.size() - 1);
                                        verticalIndex--;
                                        addLink.remove(index);
                                    }
                                    /****如果为2则删除水平度的最后一个图标*****/
                                    else if ((addLink.get(index) == 2)) {
                                        zoomMoveFrameLayout.removeView(levelIconList.get(levelIconList.size() - 1));
                                        levelIconList.remove(levelIconList.size() - 1);
                                        leverIndex--;
                                        addLink.remove(index);
                                    }
                                }

                                break;

                            /**
                             * 删除图纸
                             */
                            case R.id.item_del_pic:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("提示");
                                builder.setMessage("是否确定删除图纸？图标也会同步删除。");
                                builder.setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        zoomMoveFrameLayout.removeAllViews();
                                        verticalIconList.clear();
                                        levelIconList.clear();
                                        mImageView = null;
                                        imgBitmap.recycle();
                                        leverIndex = 0;
                                        verticalIndex = 0;

                                        if (editPicControler != null) {
                                            editPicControler.setTvAddmPicVisibale(1);
                                        }

                                        Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                                    }
                                });

                                builder.setNegativeButton("取消", null);
                                builder.show();
                                break;

                        }
                        return false;
                    }
                });
                menu.show();
                break;
        }

    }

    /**
     * TODO 上传图纸到服务器
     */
    private void uploadPicToServer(String path) {
        List<RulerCheckOptions> optionsList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer imgNum = new StringBuffer();

        if (editPicControler != null) {
            optionsList = editPicControler.getCheckOptions();

            for (int i = 0; i < optionsList.size(); i++) {
                stringBuffer.append(optionsList.get(i).getServerId());
                imgNum.append(optionsList.get(0).getImgNumber());
                if (i < (optionsList.size() - 1)) {
                    stringBuffer.append(",");
                    imgNum.append(",");
                }
            }
            LogUtils.show("打印查看保存图纸之前的管控要点："+stringBuffer.toString());
            if (!stringBuffer.toString().equals("")) {
                Intent uploadIntent = new Intent(context, UploadPicIntentService.class);
                uploadIntent.putExtra(UploadPicIntentService.UPLOAD_FLAG, UploadPicIntentService.FLAG_UPLOAD_OPTION_IMG);
                uploadIntent.putExtra(UploadPicIntentService.VALUE_IMG_PATH, path);
//                uploadIntent.putExtra(UploadPicIntentService.VALUE_OPTION_LIST, stringBuffer.toString());
                Bundle bundle = new Bundle();
                bundle.putString(NetConstant.upload_option_pic_check_options_list, stringBuffer.toString());
                bundle.putString(NetConstant.upload_option_pic_number_list, imgNum.toString());
                uploadIntent.putExtra(UploadPicIntentService.VALUE_OPTION_BUNDLE, bundle);
                context.startService(uploadIntent);
            }
        }


    }


    /**
     * TODO 将Bitmap图片保存到本地，并更新到数据库
     * @param bitmap
     */
    private String saveBitmapToFile(Bitmap bitmap) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        String imgFileName;
        List<RulerCheckOptions> optionsList = new ArrayList<>();
        if (editPicControler != null && editPicControler.getCheckOptions()!=null) {
            optionsList = editPicControler.getCheckOptions();
            imgFileName = optionsList.get(0).getCreateTime() + ".png";
        } else {
            imgFileName = DateFormatUtil.formatDate(new Date(), "yyyyMMddHHmmSS") + ".png";
        }
//                    String imgFileName = DateFormatUtil.formatDate(new Date(), "yyyyMMddHHmmSS")+".jpg";
//                    String imgFileName = optionsList.get(0).getCreateTime()+".jpg";

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            //将图片地址更新到数据库
            ContentValues values = new ContentValues();
            values.put(DataBaseParams.measure_option_img_path, imgFile.getPath());
            values.put(DataBaseParams.measure_option_img_time,DateFormatUtil.transForMilliSecond(new Date()));
            values.put(DataBaseParams.measure_option_img_upload_flag, 0);
            for (RulerCheckOptions options : optionsList) {
                OperateDbUtil.updateOptionsDataToSqlite(getContext(), DataBaseParams.measure_option_table_name, values, new String[]{String.valueOf(options.getId())});
            }
            fos.flush();
            fos.close();
            Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
            LogUtils.show("查看保存的目录："+imgFile.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgFile.getPath();
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
