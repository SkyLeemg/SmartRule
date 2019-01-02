package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.DisplayMeasureFileAdapter;
import com.vitec.task.smartrule.adapter.MeasureProjectListAdapter;
import com.vitec.task.smartrule.helper.ExportMeaureDataHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ShareFileToQQ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeasureFileActivity extends BaseActivity implements View.OnClickListener{

    private EditText tvKeyWord;
    private Button btnSearch;
    private Button btnShareFile;
    private Button btnDelFile;
    private TextView tvhasChoose;
    private ListView lvDisplayFile;
    private RelativeLayout rlSelectable;
    private MKLoader mkLoader;

    //顶部“选择”图标的标志状态，0-图标显示为选择，1-图标显示为取消
    private int chooseBtnStatus = 0;
    private DisplayMeasureFileAdapter measureFileAdapter;
    private List<File> allFileList;//目录下所有的excel文件集合
    private List<File> displayFileList;//当前界面显示的excel文件集合，是经过搜索框筛选后的
    private List<File> chooseFileList;//复选框选中的excel文件集合


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_file);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initView();
        initData();
    }

    private void initData() {
        allFileList = new ArrayList<>();
        chooseFileList = new ArrayList<>();
        displayFileList = new ArrayList<>();
        File dir = new File(ExportMeaureDataHelper.path);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();
        if (files.length > 0) {
            for (int i=0;i<files.length;i++) {
                LogUtils.show("打印查看文件路径："+files[i].getPath());
                LogUtils.show("打印查看当前文件："+i+",文件名："+files[i].getName());
                if (files[i].getName().endsWith(".xls")) {
                    allFileList.add(files[i]);
                    displayFileList.add(files[i]);
                }
            }
        }
        measureFileAdapter = new DisplayMeasureFileAdapter(MeasureFileActivity.this, displayFileList);
        lvDisplayFile.setAdapter(measureFileAdapter);
        HeightUtils.setListViewHeighBaseOnChildren(lvDisplayFile);
        mkLoader.setVisibility(View.GONE);

        /**
         * 设置Listview中复选框的选中事件的监听
         */
        measureFileAdapter.setChecked(new MeasureProjectListAdapter.OnChecked() {
            @Override
            public void onCheckedChanged(int position, boolean isChecked) {
                if (isChecked) {
                    chooseFileList.add(allFileList.get(position));

                } else {
                    chooseFileList.remove(allFileList.get(position));
                }
                tvhasChoose.setText("已选："+chooseFileList.size());
            }
        });

    }

    private void initView() {
        initWidget();
        setTitle("测量文件");
        setImgSource(R.mipmap.icon_back,R.mipmap.choose);
        imgIcon.setVisibility(View.VISIBLE);
        imgMenu.setVisibility(View.VISIBLE);
        imgMenu.setOnClickListener(this);
        imgIcon.setOnClickListener(this);
        tvKeyWord = findViewById(R.id.et_keyword);
        btnSearch = findViewById(R.id.btn_search);
        lvDisplayFile = findViewById(R.id.lv_display_file);
        rlSelectable = findViewById(R.id.rl_selectable);
        btnShareFile = findViewById(R.id.btn_share_file);
        btnDelFile = findViewById(R.id.btn_del_file);
        tvhasChoose = findViewById(R.id.tv_has_choose);
        mkLoader = findViewById(R.id.mkloader);

        rlSelectable.setVisibility(View.GONE);
        btnShareFile.setOnClickListener(this);
        btnDelFile.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 退出按钮
             */
            case R.id.img_menu_toolbar:
                MeasureFileActivity.this.finish();
                break;
            /**
             * TODO 选择按钮
             */
            case R.id.img_icon_toolbar:
                if (chooseBtnStatus == 0) {
                    imgIcon.setImageResource(R.mipmap.cancel);
                    chooseBtnStatus = 1;
                    rlSelectable.setVisibility(View.VISIBLE);
                    measureFileAdapter.setShowCheckBox(true);
                    measureFileAdapter.notifyDataSetChanged();

                } else if (chooseBtnStatus == 1) {
                    imgIcon.setImageResource(R.mipmap.choose);
                    chooseBtnStatus = 0;
                    rlSelectable.setVisibility(View.GONE);
                    chooseFileList.clear();
                    measureFileAdapter.setShowCheckBox(false);
                    measureFileAdapter.setAllChecked(false);
                    measureFileAdapter.notifyDataSetChanged();
                }
                break;

            /**
             * TODO 删除按钮
             */
            case R.id.btn_del_file:
                AlertDialog.Builder builder = new AlertDialog.Builder(MeasureFileActivity.this);
                builder.setTitle("是否确定删除以下文件？");
                StringBuffer delFileNames = new StringBuffer();
                for (File file : chooseFileList) {
                    delFileNames.append(file.getName());
                    delFileNames.append("\n");
                }
                builder.setMessage(delFileNames.toString());
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean result = false;
                        for (File file : chooseFileList) {
                            result = file.delete();
                        }
                        if (result) {
                            allFileList.removeAll(chooseFileList);
                            displayFileList.removeAll(chooseFileList);
                            measureFileAdapter.setFileList(displayFileList);
                            measureFileAdapter.notifyDataSetChanged();
                            chooseFileList.clear();
                            tvhasChoose.setText("已选："+chooseFileList.size());
                            AlertDialog.Builder tip = new AlertDialog.Builder(MeasureFileActivity.this);
                            tip.setMessage("删除成功");
                            tip.setNegativeButton("知道了", null);
                            tip.show();
                        } else {
                            AlertDialog.Builder tip = new AlertDialog.Builder(MeasureFileActivity.this);
                            tip.setMessage("删除失败");
                            tip.setNegativeButton("知道了", null);
                            tip.show();
                        }
                    }
                });

                builder.setNegativeButton("取消", null);
                builder.show();
                break;

            /**
             * TODO 分享按钮
             */
            case R.id.btn_share_file:
                ShareFileToQQ.sendFile(MeasureFileActivity.this, chooseFileList);
                AlertDialog.Builder shareBuilder = new AlertDialog.Builder(MeasureFileActivity.this);
                String[] items = {"分享给微信好友", "分享给QQ好友"};
                shareBuilder.setTitle("请选择分享目标");
                shareBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            /**
                             * 分享给微信好友
                             */
                            case 0:
                                WeChatHelper helper = new WeChatHelper(MeasureFileActivity.this);
                                helper.regToWx();
                                helper.shareFileToWx(chooseFileList);
                                break;
                            /**
                             * 分享给QQ好友
                             */
                            case 1:
                                ShareFileToQQ.sendFile(MeasureFileActivity.this,chooseFileList);
//                                ShareFileToQQ.sendToQQ(MeasureFileActivity.this,chooseFileList.get(0).getPath());
                                break;
                        }
                    }
                });
//                shareBuilder.show();
                break;
        }
    }
}
