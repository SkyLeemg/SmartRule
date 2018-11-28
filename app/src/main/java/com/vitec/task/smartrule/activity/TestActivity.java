package com.vitec.task.smartrule.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.MeasureTable;
import com.vitec.task.smartrule.bean.MeasureTableRow;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.interfaces.IAddExcelResultCallBack;
import com.vitec.task.smartrule.utils.ExportMeaureData;
import com.vitec.task.smartrule.utils.ShareFileToQQ;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends BaseActivity implements View.OnClickListener{


    private Button btnTestExport;
    private Button btnOpenDir;
    private Button btnSendFileToWxF;
    private Button btnSendFiletoQQ;
    private Button btnUpdateFirm;
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_test);
        requestLocationPermissions();
        initView();

//        WXFileObject
    }


    private void initView() {
        btnTestExport = findViewById(R.id.btn_test_export);
        btnOpenDir = findViewById(R.id.btn_open_dir);
        btnSendFileToWxF = findViewById(R.id.btn_send_file);
        btnSendFiletoQQ = findViewById(R.id.btn_send_file_to_qq);
        btnUpdateFirm = findViewById(R.id.btn_update_firm);

        btnOpenDir.setOnClickListener(this);
        btnTestExport.setOnClickListener(this);
        btnSendFileToWxF.setOnClickListener(this);
        btnSendFiletoQQ.setOnClickListener(this);
        btnUpdateFirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_test_export:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MeasureTable table = new MeasureTable();
                        table.setCheckDate("2018-09-21");
                        table.setCheckFloor("A栋3层");
                        table.setCheckPerson("张三");
                        table.setProjectName("马来建筑工程项目");
                        table.setEngineerName("混凝土建筑工程");
                        List<MeasureTableRow> rowList = new ArrayList<>();
                        for (int i=0;i<3;i++) {
                            MeasureTableRow row = new MeasureTableRow();
                            /**获取管控要点的Json字符串
                             *
                             * 获取回来的信息样本：
                             * {"status":"success",
                             * "code":200,
                             * "data":[
                             * {"id":1,"name":"立面垂直度","standard":"≤8mm\/≤10mm(≤6m取8)",
                             * "methods":"2米靠尺测量,每个柱选取相邻2面测量;当所选墙面长度小于3m时,两端测量2尺;当所选墙面长度大于 3m时,两端测量2尺、中间水平测量1尺,每个测量值作为1个计算点",
                             * "engin_id":1},
                             * {"id":2,"name":"表面平整度","standard":"≤8mm",
                             * "methods":"2米靠尺测量,每个墙柱选取一面作为测量点,砼柱平整度测量对角线;当所选墙面长度小于3m时,两端45°测量2尺;当所选墙面长度大于3m时,两端45°测量2尺、中间水平测量1尺;相邻构件跨洞口必测,每个测量值作为1个计算点",
                             * "engin_id":1}],"msg":"查询成功"}
                             */
                            row.setCheckMethod("2米靠尺测量,每个柱选取相邻2面测量;当所选墙面长度小于3m时,两端测量2尺;当所选墙面长度大于 3m时,两端测量2尺、中间水平测量1尺,每个测量值作为1个计算点");
                            row.setId(i+1);
                            row.setOptionName("立面垂直度");
                            row.setQualifiedNum(60);
                            row.setRealMeasureNum(80);
                            row.setQualifiedRate("80%");
                            row.setStandard("≤8mm\\/≤10mm(≤6m取8)");
                            List<String> datalist = new ArrayList<>();
                            for (int j=0;j<57;j++) {
                                datalist.add(String.valueOf(j));
                            }
                            row.setDatalist(datalist);
                            rowList.add(row);
                        }
                        table.setRowList(rowList);
                        final ExportMeaureData exportMeaureData = new ExportMeaureData(getApplicationContext(), "测试表格12.xls");
                        exportMeaureData.addExcelData(table, new IAddExcelResultCallBack() {
                            @Override
                            public void onSuccess(final String title) {
                                Log.e("aaa", "onSuccess: 第一个表格保存完成" );
                            }

                            @Override
                            public void onFail(final String msg) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"导出失败",Toast.LENGTH_LONG).show();
                                        Log.e("aaa", "run: 查看失败原因："+ msg);
                                    }
                                });
                            }
                        });


                        MeasureTable measureTable = new MeasureTable();
                        measureTable.setCheckDate("2018-09-21");
                        measureTable.setCheckFloor("A栋3层");
                        measureTable.setCheckPerson("张三");
                        measureTable.setProjectName("马aaaa程项目");
                        measureTable.setEngineerName("新的建筑工程");
                        List<MeasureTableRow> rowList1 = new ArrayList<>();
                        for (int i=0;i<2;i++) {
                            MeasureTableRow row = new MeasureTableRow();
                            /**获取管控要点的Json字符串
                             *
                             * 获取回来的信息样本：
                             * {"status":"success",
                             * "code":200,
                             * "data":[
                             * {"id":1,"name":"立面垂直度","standard":"≤8mm\/≤10mm(≤6m取8)",
                             * "methods":"2米靠尺测量,每个柱选取相邻2面测量;当所选墙面长度小于3m时,两端测量2尺;当所选墙面长度大于 3m时,两端测量2尺、中间水平测量1尺,每个测量值作为1个计算点",
                             * "engin_id":1},
                             * {"id":2,"name":"表面平整度","standard":"≤8mm",
                             * "methods":"2米靠尺测量,每个墙柱选取一面作为测量点,砼柱平整度测量对角线;当所选墙面长度小于3m时,两端45°测量2尺;当所选墙面长度大于3m时,两端45°测量2尺、中间水平测量1尺;相邻构件跨洞口必测,每个测量值作为1个计算点",
                             * "engin_id":1}],"msg":"查询成功"}
                             */
                            row.setCheckMethod("2米靠尺测量,每个墙柱选取一面作为测量点,砼柱平整度测量对角线;当所选墙面长度小于3m时,两端45°测量2尺;当所选墙面长度大于3m时,两端45°测量2尺、中间水平测量1尺;相邻构件跨洞口必测,每个测量值作为1个计算点");
                            row.setId(i+1);
                            row.setOptionName("表面平整度");
                            row.setQualifiedNum(30);
                            row.setRealMeasureNum(20);
                            row.setQualifiedRate("10%");
                            row.setStandard("≤8mm\\/≤10mm(≤6m取8)");
                            List<String> datalist = new ArrayList<>();
                            for (int j=0;j<21;j++) {
                                datalist.add(String.valueOf(j));
                            }
                            row.setDatalist(datalist);
                            rowList1.add(row);
                        }
                        measureTable.setRowList(rowList1);

                        exportMeaureData.addExcelData(measureTable, new IAddExcelResultCallBack() {
                            @Override
                            public void onSuccess(final String title) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"导出成功，文件路径为："+title,Toast.LENGTH_LONG).show();
                                        Log.e("aaa", "run: 第二个表格导出完成" );
                                        Log.e("aaaa", "run: 查看导出的路径："+title );
                                        exportMeaureData.write();
//                                        一定要关闭，不然表格内容会为空
                                        exportMeaureData.close();
                                    }
                                });
                            }

                            @Override
                            public void onFail(String msg) {

                            }
                        });
                    }
                }).start();



                break;

            case R.id.btn_open_dir:
                File file = new File(ExportMeaureData.path);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivity(intent);
                break;

            case R.id.btn_send_file:
                WeChatHelper helper = new WeChatHelper(this);
                helper.regToWx();
                helper.shareFileToWx(ExportMeaureData.path+"/测试表格12.xls");
                break;

            case R.id.btn_send_file_to_qq:
                File apkFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/测试表格12.xls");
                ShareFileToQQ.sendToQQ(this,apkFile.getPath());
                break;

            case R.id.btn_update_firm:
                Intent openIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openIntent.setType("*/*");
                openIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(openIntent,1);
                break;
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == 1) {
//            Uri uri = data.getData();
//            if (uri == null) {
//                return;
//            }
//            if ("file".equalsIgnoreCase(uri.getScheme())) {
//                path = uri.getPath();
//            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//                path=getPath
//            }
//        }
//
//    }
}
