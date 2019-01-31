package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.MeasureProjectListAdapter;
import com.vitec.task.smartrule.bean.OptionMeasure;
import com.vitec.task.smartrule.bean.RulerCheckOptions;
import com.vitec.task.smartrule.bean.event.DelMeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureDataMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.event.StopMeasureMsgEvent;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.interfaces.IClickable;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.service.intentservice.ReplenishDataToServerIntentService;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;
import com.vitec.task.smartrule.utils.ServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 等待测量的界面
 */
public class WaitingMeasureActivity extends BaseActivity implements View.OnClickListener,IClickable{

//    private TextView tvTotal;
//    private TextView tvLastPage,tvNextPage, tvCurrentPage;
    private MKLoader mkLoader;//加载进度条
    private int total=0,//总数
            currentPage=1, //当前页码
            pageSize = 20;//当前页总数

    private ListView lvWaitingMeasureList;
    private TextView tvTip;
    private List<RulerCheck> rulerCheckList;//从数据库获取的所有等待测量的rulercheck集合
    private MeasureProjectListAdapter measureProjectListAdapter;
//    private Button btnFinish;//测量完成按钮
    private int current_id;

    private int stopTotalNum = 0;//记录点击停止/结束/完成测量按钮时总的rulerCheck数目
    private int currentSuccessStopNum = 0;//因为停止测量服务器响应是一条条响应反馈的，这里记录当前响应的是第几条，直达最后一条我们才做结束成功的提示

    private int chooseIndex = -1;//用户点击需要跳转到测量页面的item的序号
    private int chooseEditIndex = -1;//用户点击需要跳转到编辑页面的item的序号
    private RulerCheck delRulerCheck;//用户点击删除的Rulercheck

    private final int request_flag_edit_msg = 13;
    private Set<Integer> checkIDSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating_measure);
        EventBus.getDefault().register(this);
        initView();
    }

    private void initData() {
        rulerCheckList = new ArrayList<>();
        checkIDSet = new HashSet<>();
        /**
         * 补上传服务启动
         */
        Intent replenishIntent = new Intent(getApplicationContext(), ReplenishDataToServerIntentService.class);
        startService(replenishIntent);



        measureProjectListAdapter = new MeasureProjectListAdapter(WaitingMeasureActivity.this, rulerCheckList,current_id);
        lvWaitingMeasureList.setAdapter(measureProjectListAdapter);
        measureProjectListAdapter.setiClickable(this);
//        updatePageData();
//        getUnFinishServerCheckData();
        /**
         * 先获取本地数据再获取服务器数据
         */
        getUnFinishLocalCheck();
        lvWaitingMeasureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (measureProjectListAdapter.getClickIndex() == i) {
                    measureProjectListAdapter.setClickIndex(-1);
                } else {
                    measureProjectListAdapter.setClickIndex(i);
                }
                measureProjectListAdapter.notifyDataSetChanged();

                /***进入测量部分**/
//                mkLoader.setVisibility(View.VISIBLE);
//                Intent serviceIntent = new Intent(WaitingMeasureActivity.this, PerformMeasureNetIntentService.class);
//                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
//                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, rulerCheckList.get(i));
//                startService(serviceIntent);
//                chooseIndex = i;


//                Intent intent = new Intent(WaitingMeasureActivity.this, MeasureManagerAcitivty.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("projectMsg", rulerCheckList.get(i));
//                startActivity(intent);
            }
        });

        /*****请求查询测量组*****/
        User user = OperateDbUtil.getUser(getApplicationContext());
        Intent queryIntent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(DataBaseParams.user_user_id, user.getUserID());
        bundle.putInt(NetConstant.page_size, 50);
        bundle.putInt(NetConstant.current_Page, 1);
        queryIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_project_list);
        queryIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
        startService(queryIntent);
    }

    /**
     * 从服务器获取未完成的ruler_check数据
     */
    private void getUnFinishServerCheckData() {
        mkLoader.setVisibility(View.VISIBLE);
        Intent serviceIntent = new Intent(WaitingMeasureActivity.this, PerformMeasureNetIntentService.class);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_QUERY_MEASURE_RECORD);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, 0);
        serviceIntent.putExtra(NetConstant.current_Page, currentPage);
        serviceIntent.putExtra(NetConstant.page_size, 20);
        startService(serviceIntent);
    }

    /**
     * 从本地获取数据：
     * 获取ruler_check表格中，所有is_finish==0和user_id=当前登录用户的数据
     */
    private void getUnFinishLocalCheck() {
        mkLoader.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = OperateDbUtil.getUser(getApplicationContext());
                String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() + " and " + DataBaseParams.measure_is_finish + "=0 ORDER BY "+ DataBaseParams.measure_create_time+" DESC;" ;
                BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
//        rulerCheckList.clear();
                rulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
//        LogUtils.show("查看本地搜索到的测量记录："+rulerCheckList.size()+",内容："+rulerCheckList);
                for (int i = 0; i < rulerCheckList.size(); i++) {
                    checkIDSet.add(rulerCheckList.get(i).getId());
                }
                dataDbHelper.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rulerCheckList.size() == 0) {
                            tvTip.setVisibility(View.VISIBLE);
                        }
                        measureProjectListAdapter.setRulerCheckList(rulerCheckList);
                        total = rulerCheckList.size();
//        updatePageData();
                        updateAdapterData();
                         getUnFinishServerCheckData();
                        mkLoader.setVisibility(View.GONE);
                    }
                });

            }
        }).start();

    }

    private void updateAdapterData() {
//            getUnFinishLocalCheck();
        //        判断是否有项目正在测量
        boolean isAlive = ServiceUtils.isServiceRunning(getApplicationContext(), "com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService");
        current_id = 0;
        if (isAlive) {
            current_id = HandleBleMeasureDataReceiverService.check_id;
            List<RulerCheck> sortList = new ArrayList<>();
//            将正在测量的挪到最前面
            if (rulerCheckList.size() > 0 && rulerCheckList.get(0).getId() != current_id) {
                for (int i=0;i<rulerCheckList.size();i++) {
                    if (rulerCheckList.get(i).getId() == current_id) {
                        sortList.add(rulerCheckList.get(i));
                        rulerCheckList.remove(i);
                        break;
                    }
                }
                sortList.addAll(rulerCheckList);
                rulerCheckList = sortList;
            }
        }
        measureProjectListAdapter.setRulerCheckList(rulerCheckList);
        measureProjectListAdapter.setCurrent_id(current_id);
        measureProjectListAdapter.notifyDataSetChanged();
        updateListViewHeight();
        if (rulerCheckList.size() == 0) {
            tvTip.setVisibility(View.VISIBLE);
        }else tvTip.setVisibility(View.GONE);

        mkLoader.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        updateAdapterData();
    }



    /**
     * TODO 接受请求服务器记录表响应的数据
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void serverMeasureRecordCallBack(MeasureRecordMsgEvent event) {
        List<RulerCheck> checkList = event.getCheckList();
//        LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录总数："+event.getTotal()+"，当前页数："+event.getCurrentPage()+"，每页条数："+event.getPageSize());
        LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录："+checkList.size()+"，内容："+checkList);
//        如果checklist大小大于0，则说明请求成功，为0则说明网络请求失败，或者是真的为0
        if (checkList.size() > 0) {
            currentPage = event.getCurrentPage();
            pageSize = event.getPageSize();
            total = event.getTotal();
//            rulerCheckList.clear();
            for (RulerCheck check : checkList) {
                if (checkIDSet.add(check.getId())) {
                    rulerCheckList.add(check);
                }
            }
//            rulerCheckList.addAll(checkList);
            if (currentPage * pageSize < total) {
                currentPage++;
                getUnFinishServerCheckData();
            } else {
                mkLoader.setVisibility(View.GONE);
                if (rulerCheckList.size() == 0) {
                    tvTip.setVisibility(View.VISIBLE);
                } else {
                    tvTip.setVisibility(View.GONE);
                }
//                getUnFinishLocalCheck();
                updateAdapterData();

            }

//            updatePageData();

        } else {
            mkLoader.setVisibility(View.GONE);
            getUnFinishLocalCheck();
        }

    }

    /**
     * 接受服务器 停止测量接口的响应信号
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void stopMeasureCallBack(StopMeasureMsgEvent event) {
        currentSuccessStopNum++;
        if (currentSuccessStopNum == stopTotalNum) {
            //                        提示用户测量完成后项目移植的地方
            AlertDialog.Builder finishBuilder = new AlertDialog.Builder(WaitingMeasureActivity.this);
            finishBuilder.setTitle("测量完成");
            finishBuilder.setMessage("所有等待测量的项目都已完成测量，可以【测量记录】中查看哦~");
            finishBuilder.setNegativeButton("好的", null);
            finishBuilder.show();
            mkLoader.setVisibility(View.GONE);
            if (event.getFlag() == 1) {
                getUnFinishServerCheckData();
            } else {
                getUnFinishLocalCheck();
            }

        }
    }

    /**
     * TODO  获取测量数据接口，接受服务器响应数据处理完毕的信号
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMeasureDataCallBack(MeasureDataMsgEvent event) {
        if (chooseIndex >= 0) {

            Intent intent = new Intent(WaitingMeasureActivity.this, MeasureManagerAcitivty.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("projectMsg", rulerCheckList.get(chooseIndex));
            OptionMeasure optionMeasure = new OptionMeasure();
            intent.putExtra("floor_height", optionMeasure);
            startActivity(intent);
            chooseIndex = -1;
        } else if (chooseEditIndex >= 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("rulerCheck", rulerCheckList.get(chooseEditIndex));
            Intent intent = new Intent(this, EditMeasureMsgActivity.class);
            intent.putExtra("rulerCheck", bundle);
            startActivityForResult(intent, request_flag_edit_msg);
        }

        mkLoader.setVisibility(View.GONE);
    }


    /**
     * TODO 接收返回值
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.show("=====收到一个编辑页面返回的通知。。。。查看返回码：" + resultCode + ",,,,查看请求码：" + requestCode);
        if (resultCode == 11) {
            switch (requestCode) {
                case request_flag_edit_msg:
                    int flag = data.getIntExtra("flag", 0);
                    if (flag == 1) {
                        LogUtils.show("等待测量页面------开始更新数据");
                        measureProjectListAdapter.setClickIndex(-1);
                        getUnFinishLocalCheck();
                    }

                    chooseEditIndex = -1;
                    break;
            }

        }
    }

    private void initView() {

        initWidget();
        setTvTitle("等待测量");
        tvTip = findViewById(R.id.tv_tip);
        tvTip.setVisibility(View.GONE);
//        imgIcon.setImageResource(R.mipmap.icon_back);
//        imgIcon.setVisibility(View.VISIBLE);
//        imgIcon.setOnClickListener(this);
        imgMenu.setOnClickListener(this);

//        tvLastPage = findViewById(R.id.tv_last_page);
//        tvNextPage = findViewById(R.id.tv_next_page);
//        tvTotal = findViewById(R.id.tv_total);
//        tvCurrentPage = findViewById(R.id.tv_current_page);
        mkLoader = findViewById(R.id.mkloader);
        mkLoader.setVisibility(View.GONE);


        lvWaitingMeasureList = findViewById(R.id.lv_waiting_measure_list);
//        btnFinish = findViewById(R.id.btn_finish);
//
//        btnFinish.setOnClickListener(this);
//        tvNextPage.setOnClickListener(this);
//        tvLastPage.setOnClickListener(this);
        initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_menu_toolbar:

                onBackPressed();
                break;

            /**
             * 上一页
             */
            case R.id.tv_last_page:
                if (currentPage > 1) {
                    currentPage--;
                    getUnFinishServerCheckData();
                } else {
                    Toast.makeText(getApplicationContext(),"已是第一页",Toast.LENGTH_SHORT).show();
                }
                break;

            /**
             * 下一页
             */
            case R.id.tv_next_page:
                int totalPage = total / pageSize;
                if (total % pageSize > 0) {
                    totalPage += 1;
                }
                if (currentPage < totalPage) {
                    currentPage++;
                    getUnFinishServerCheckData();
                } else {
                    Toast.makeText(getApplicationContext(),"已是最后一页",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 点击了开始测量
     * @param position
     */
    @Override
    public void onFirstClickable(int position) {
        mkLoader.setVisibility(View.VISIBLE);
        /**
         * 启动下载数据的后台服务
         */
        Intent serviceIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, rulerCheckList.get(position));
        startService(serviceIntent);
        chooseIndex = position;
    }

    /**
     * 点击了编辑信息
     * @param position
     */
    @Override
    public void onSencondClickable(int position) {
        mkLoader.setVisibility(View.VISIBLE);
        chooseEditIndex = position;
        Intent serviceIntent = new Intent(getApplicationContext(), PerformMeasureNetIntentService.class);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_GET_MEASURE_DATA);
        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, rulerCheckList.get(position));
        startService(serviceIntent);

    }

    /********************TODO 删除项目相关操作********************/
    /**
     * 点击了删除项目
     * @param position
     */
    @Override
    public void onThirdClickable(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(WaitingMeasureActivity.this);
        builder.setTitle("提示");
        builder.setMessage("是否确定删除" + rulerCheckList.get(position).getProject().getProjectName() + "?");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mkLoader.setVisibility(View.VISIBLE);
                StringBuffer check_ids = new StringBuffer();
                check_ids.append(rulerCheckList.get(position).getServerId());
                delRulerCheck = rulerCheckList.get(position);
                Intent serviceIntent = new Intent(WaitingMeasureActivity.this, PerformMeasureNetIntentService.class);
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_DEL_RECORD);
                //        传完成测量的标志，。
                serviceIntent.putExtra(PerformMeasureNetIntentService.GET_DATA_KEY, check_ids.toString());
                startService(serviceIntent);
            }
        });
        builder.setNegativeButton("取消删除", null);
        builder.show();
    }


    /**
     * TODO delMeasureRecordCallBack 请求删除记录表的响应回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void delMeasureRecordCallBack(DelMeasureRecordMsgEvent event) {
        mkLoader.setVisibility(View.GONE);
        LogUtils.show("删除记录回调了：" + event.isSuccess());
        if (event.isSuccess()) {
//            delLocalData();

        } else {
            /**
             * 如果服务器删除失败，则将本地数据库的upload_flag改成4
             */
            BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
            if (delRulerCheck != null) {

                ContentValues values = new ContentValues();
                values.put(DataBaseParams.upload_flag, 4);
                values.put(DataBaseParams.measure_is_finish, 4);
                String where = " id =?";
                int result = bleDataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, values, where, new String[]{String.valueOf(delRulerCheck.getId())});
                if (result > 0) {
                    delOptionsAndData(delRulerCheck);
                    LogUtils.show("状态数据修改为4成功啦");
                }
            }

        }
        if (HandleBleMeasureDataReceiverService.check_id == delRulerCheck.getId()) {
            HandleBleMeasureDataReceiverService.stopHandleService(getApplicationContext());
        }
        rulerCheckList.remove(delRulerCheck);
        measureProjectListAdapter.notifyDataSetChanged();
        measureProjectListAdapter.setClickIndex(-1);
        updateListViewHeight();
        delRulerCheck = null;
        LogUtils.show("本地数据库修改成功");

    }


    /**
     * 删除rulercheck数据后，要将rulercheck对应的ruler_check_options和ruler_check_options_data表格中的数据也删除了
     * @param rulerCheck
     */
    private void delOptionsAndData(RulerCheck rulerCheck) {
        String where = " where " + DataBaseParams.measure_option_check_id + " = " + rulerCheck.getId();
        List<RulerCheckOptions> checkOptionsList = OperateDbUtil.queryCheckOptionFromSqlite(getApplicationContext(), rulerCheck, where);
        BleDataDbHelper bleDataDbHelper = new BleDataDbHelper(getApplicationContext());
        if (checkOptionsList.size() > 0) {
            for (RulerCheckOptions checkOptions : checkOptionsList) {
                String delWhere = DataBaseParams.options_data_check_options_id + " = ?";
                boolean delResult = bleDataDbHelper.delData(DataBaseParams.options_data_table_name, delWhere, new String[]{String.valueOf(checkOptions.getId())});
//                if (delResult) {
                LogUtils.show( "管控要点ID为："+checkOptions.getId()+"的测量数据删除结果："+delResult);
//                }
            }
        }
        String optionWhere = DataBaseParams.measure_option_check_id + " = ?";
        boolean optionsResult = bleDataDbHelper.delData(DataBaseParams.measure_option_table_name, optionWhere, new String[]{String.valueOf(rulerCheck.getId())});
        LogUtils.show("check_id为："+rulerCheck.getId()+"的所有管控要点数据删除结果："+optionsResult);

    }

    /**
     * 计算Listview的高度
     * 由于有一个点击弹出的菜单，所以要预留多一点位置在底部
     */
    private void updateListViewHeight() {
        int height = HeightUtils.setListViewHeighBaseOnChildren(lvWaitingMeasureList);
        height = height + (50 * ScreenSizeUtil.getScreenWidth(getApplicationContext()) / 320);
        ViewGroup.LayoutParams params = lvWaitingMeasureList.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = height;
        lvWaitingMeasureList.setLayoutParams(params);
    }


    private void updatePageData() {

//        if (currentPage == 1) {
//            tvLastPage.setEnabled(false);
//        } else {
//            tvLastPage.setEnabled(true);
//        }

//        int totalPage = total / pageSize;
//        if (total % pageSize > 0) {
//            totalPage += 1;
//        }
//        if (currentPage == totalPage) {
//            tvNextPage.setEnabled(false);
//        } else {
//            tvNextPage.setEnabled(true);
//        }
//        tvCurrentPage.setText(""+currentPage);
//        tvTotal.setText("总数："+total);
    }


}
