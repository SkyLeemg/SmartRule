package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.DisplayMeasureFileAdapter;
import com.vitec.task.smartrule.adapter.MeasureProjectListAdapter;
import com.vitec.task.smartrule.helper.ExportMeaureDataHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.interfaces.IClickable;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ShareFileToQQ;
import com.vitec.task.smartrule.view.ShareFileBottomDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeasureFileActivity extends BaseActivity implements View.OnClickListener,IClickable{

    private EditText tvKeyWord;
//    private Button btnShareFile;
    private TextView tvDelFile;
    private TextView tvhasChoose;
    private ListView lvDisplayFile;
    private RelativeLayout rlSelectable;
    private MKLoader mkLoader;
    private TextView tvChoose;

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
        File[] files =  dir.listFiles();
        int i = files.length-1;
        if (files != null && files.length > 0) {
            for (; i > 0; i--) {
                LogUtils.show("打印查看文件路径：" + files[i].getPath());
                LogUtils.show("打印查看当前文件：" + i + ",文件名：" + files[i].getName());
                if (files[i].getName().endsWith(".xls")) {
                    allFileList.add(files[i]);
                    displayFileList.add(files[i]);
                }
            }
        }
        measureFileAdapter = new DisplayMeasureFileAdapter(MeasureFileActivity.this, displayFileList,this);
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
                if (chooseFileList.size() > 0) {
                    rlSelectable.setVisibility(View.VISIBLE);
                } else {
                    rlSelectable.setVisibility(View.GONE);
                }
            }
        });

        tvKeyWord.addTextChangedListener(textWatcher);



    }

    private void initView() {
        initWidget();
        setTitle("测量文件");
        setImgSource(R.mipmap.icon_back,R.mipmap.choose);
//        imgIcon.setVisibility(View.VISIBLE);
        imgMenu.setVisibility(View.VISIBLE);
        imgMenu.setOnClickListener(this);
//        imgIcon.setOnClickListener(this);
        tvKeyWord = findViewById(R.id.et_keyword);
//        btnSearch = findViewById(R.id.btn_search);
        lvDisplayFile = findViewById(R.id.lv_display_file);
        rlSelectable = findViewById(R.id.rl_selectable);
//        btnShareFile = findViewById(R.id.btn_share_file);
        tvDelFile = findViewById(R.id.tv_del_file);
        tvhasChoose = findViewById(R.id.tv_has_choose);
        mkLoader = findViewById(R.id.mkloader);
        tvChoose = findViewById(R.id.tv_choose);

        rlSelectable.setVisibility(View.GONE);
//        btnShareFile.setOnClickListener(this);
        tvDelFile.setOnClickListener(this);
        tvChoose.setOnClickListener(this);

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
            case R.id.tv_choose:
                if (chooseBtnStatus == 0) {
                    tvChoose.setText("取消");
                    chooseBtnStatus = 1;
//                    rlSelectable.setVisibility(View.VISIBLE);
                    measureFileAdapter.setShowCheckBox(true);
                    measureFileAdapter.notifyDataSetChanged();

                } else if (chooseBtnStatus == 1) {
                    tvChoose.setText("选择");
                    chooseBtnStatus = 0;
//                    rlSelectable.setVisibility(View.GONE);
                    chooseFileList.clear();
                    measureFileAdapter.setShowCheckBox(false);
                    measureFileAdapter.setAllChecked(false);
                    measureFileAdapter.notifyDataSetChanged();
                }
                break;

            /**
             * TODO 删除按钮
             */
            case R.id.tv_del_file:
                if (chooseFileList.size() == 0) {
                    Toast.makeText(getApplicationContext(),"未选择文件",Toast.LENGTH_SHORT).show();
                    return;
                }
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
                            rlSelectable.setVisibility(View.GONE);
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


        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            LogUtils.show("beforeTextChanged---之前：" + tvKeyWord.getText());
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            LogUtils.show("onTextChanged-----改变时：" + tvKeyWord.getText());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            LogUtils.show("afterTextChanged-----改变后：" + tvKeyWord.getText());
            displayFileList.clear();
            if (!tvKeyWord.getText().toString().trim().equals("")) {
                for (int i = 0; i < allFileList.size(); i++) {
                    if (allFileList.get(i).getName().contains(tvKeyWord.getText().toString().trim())) {
                        displayFileList.add(allFileList.get(i));
                    }
                }
            } else {
                displayFileList.addAll(allFileList);
            }

            measureFileAdapter.setFileList(displayFileList);
            measureFileAdapter.notifyDataSetChanged();

        }
    };


    /**
     * 接受分享按钮响应
     * @param position
     */
    @Override
    public void onFirstClickable(int position) {
        ShareFileBottomDialog shareFileBottomDialog = new ShareFileBottomDialog(MeasureFileActivity.this, R.style.BottomDialog, displayFileList.get(position));
        shareFileBottomDialog.show();

    }


    /**
     * 接收删除按钮响应
     * @param position
     */
    @Override
    public void onSencondClickable(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasureFileActivity.this);
        builder.setTitle("是否删除以下文件?");
        StringBuffer delFileNames = new StringBuffer();
        delFileNames.append(displayFileList.get(position).getName());
        builder.setMessage(delFileNames.toString());
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean result = false;
                result = displayFileList.get(position).delete();
                if (result) {
                    allFileList.remove(displayFileList.get(position));
                    displayFileList.remove(position);
                    measureFileAdapter.setFileList(displayFileList);
                    measureFileAdapter.notifyDataSetChanged();
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
    }

    /**
     * 点击事件
     * @param position
     */
    @Override
    public void onThirdClickable(int position) {
        LogUtils.show("点击了打开文件");
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            File openFile = displayFileList.get(position);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//大于等于android 7.0的时候
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
                Uri uri = FileProvider.getUriForFile(
                        MeasureFileActivity.this,
                        "com.vitec.task.smartrule.fileprovider",
                        openFile);
                intent.setDataAndType(uri, "application/vnd.ms-excel");

            } else {
                intent.setDataAndType(Uri.fromFile(openFile), "application/vnd.ms-excel");
            }
            startActivity(intent);
            Intent.createChooser(intent, "选择对应的软件打开文件");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),"文件无法打开，请下载相关软件",Toast.LENGTH_SHORT).show();
        }

    }
}
