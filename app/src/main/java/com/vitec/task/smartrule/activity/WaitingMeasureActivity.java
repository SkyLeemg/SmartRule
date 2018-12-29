package com.vitec.task.smartrule.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.vitec.task.smartrule.bean.event.MeasureDataMsgEvent;
import com.vitec.task.smartrule.bean.event.MeasureRecordMsgEvent;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.event.StopMeasureMsgEvent;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService;
import com.vitec.task.smartrule.service.intentservice.PerformMeasureNetIntentService;
import com.vitec.task.smartrule.utils.HeightUtils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;
import com.vitec.task.smartrule.utils.ServiceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 等待测量的界面
 */
public class WaitingMeasureActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvTotal;
    private TextView tvLastPage,tvNextPage, tvCurrentPage;
    private MKLoader mkLoader;//加载进度条
    private int total=0,//总数
            currentPage=1, //当前页码
            pageSize = 20;//当前页总数

    private ListView lvWaitingMeasureList;
    private List<RulerCheck> rulerCheckList;//从数据库获取的所有等待测量的rulercheck集合
    private MeasureProjectListAdapter measureProjectListAdapter;
    private Button btnFinish;//测量完成按钮
    private int current_id;

    private int stopTotalNum = 0;//记录点击停止/结束/完成测量按钮时总的rulerCheck数目
    private int currentSuccessStopNum = 0;//因为停止测量服务器响应是一条条响应反馈的，这里记录当前响应的是第几条，直达最后一条我们才做结束成功的提示

    private int chooseIndex = 0;//用户点击需要跳转的item的序号

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating_measure);
        EventBus.getDefault().register(this);
        initView();

        initData();
    }

    private void initData() {
        rulerCheckList = new ArrayList<>();
        /**
         * 应当先从服务器获取，服务器获取失败再从本地获取
         */


        measureProjectListAdapter = new MeasureProjectListAdapter(WaitingMeasureActivity.this, rulerCheckList,current_id);
        lvWaitingMeasureList.setAdapter(measureProjectListAdapter);
        updatePageData();
        getUnFinishServerCheckData();
//        getUnFinishLocalCheck();
        lvWaitingMeasureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LogUtils.show("lvWaitingMeasureList----点击了Item");
                if (measureProjectListAdapter.getLastClickIndx() >= 0 && measureProjectListAdapter.getLastClickIndx() != i) {

                    measureProjectListAdapter.getClickListenerList().get(measureProjectListAdapter.getLastClickIndx()).onClickable();
                }
                measureProjectListAdapter.getClickListenerList().get(i).onClickable();
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
        serviceIntent.putExtra(NetConstant.page_size, 8);
        startService(serviceIntent);
    }

    /**
     * 从本地获取数据：
     * 获取ruler_check表格中，所有is_finish==0和user_id=当前登录用户的数据
     */
    private void getUnFinishLocalCheck() {
        mkLoader.setVisibility(View.VISIBLE);
        User user = OperateDbUtil.getUser(getApplicationContext());
        String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() + " and " + DataBaseParams.measure_is_finish + "=0 ORDER BY "+ DataBaseParams.measure_id+" DESC;" ;
        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
        rulerCheckList.clear();
        rulerCheckList = dataDbHelper.queryRulerCheckTableDataFromSqlite(where);
        LogUtils.show("查看本地搜索到的测量记录："+rulerCheckList.size()+",内容："+rulerCheckList);
        dataDbHelper.close();
        total = rulerCheckList.size();
        updatePageData();
        updateAdapterData();
        mkLoader.setVisibility(View.GONE);
    }

    private void updateAdapterData() {
//            getUnFinishLocalCheck();
        //        判断是否有项目正在测量
        boolean isAlive = ServiceUtils.isServiceRunning(getApplicationContext(), "com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService");
        current_id = 0;
        if (isAlive) {
            current_id = HandleBleMeasureDataReceiverService.check_id;
        }
        measureProjectListAdapter.setRulerCheckList(rulerCheckList);
        measureProjectListAdapter.setCurrent_id(current_id);
        measureProjectListAdapter.notifyDataSetChanged();
        int height = HeightUtils.setListViewHeighBaseOnChildren(lvWaitingMeasureList);
        height = height + (50 * ScreenSizeUtil.getScreenWidth(getApplicationContext()) / 320);
        ViewGroup.LayoutParams params = lvWaitingMeasureList.getLayoutParams();
//        params.height = totalHeight + (gridView.getMeasuredHeight() * (listAdapter.getCount() - 1));
        params.height = height;
        lvWaitingMeasureList.setLayoutParams(params);

    }

    /**
     * 接受请求服务器记录表响应的数据
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void serverMeasureRecordCallBack(MeasureRecordMsgEvent event) {
        List<RulerCheck> checkList = event.getCheckList();
        LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录总数："+event.getTotal()+"，当前页数："+event.getCurrentPage()+"，每页条数："+event.getPageSize());
        LogUtils.show("serverMeasureRecordCallBack---查看从服务器收到的测量记录："+checkList.size()+"，内容："+checkList);
//        如果checklist大小大于0，则说明请求成功，为0则说明网络请求失败，或者是真的为0
        if (checkList.size() > 0) {
            currentPage = event.getCurrentPage();
            pageSize = event.getPageSize();
            total = event.getTotal();
            rulerCheckList.clear();
            rulerCheckList.addAll(checkList);
            updateAdapterData();
            updatePageData();
            mkLoader.setVisibility(View.GONE);
        } else {
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
     * 获取测量数据接口，接受服务器响应数据处理完毕的信号
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMeasureDataCallBack(MeasureDataMsgEvent event) {
        mkLoader.setVisibility(View.GONE);
        Intent intent = new Intent(WaitingMeasureActivity.this, MeasureManagerAcitivty.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("projectMsg", rulerCheckList.get(chooseIndex));
        startActivity(intent);
    }

    private void updatePageData() {

        if (currentPage == 1) {
            tvLastPage.setEnabled(false);
        } else {
            tvLastPage.setEnabled(true);
        }

        int totalPage = total / pageSize;
        if (total % pageSize > 0) {
            totalPage += 1;
        }
        if (currentPage == totalPage) {
            tvNextPage.setEnabled(false);
        } else {
            tvNextPage.setEnabled(true);
        }
        tvCurrentPage.setText(""+currentPage);
        tvTotal.setText("总数："+total);
    }

    private void initView() {

        initWidget();
        setTvTitle("等待测量");
//        imgIcon.setImageResource(R.mipmap.icon_back);
//        imgIcon.setVisibility(View.VISIBLE);
//        imgIcon.setOnClickListener(this);
        imgMenu.setOnClickListener(this);

        tvLastPage = findViewById(R.id.tv_last_page);
        tvNextPage = findViewById(R.id.tv_next_page);
        tvTotal = findViewById(R.id.tv_total);
        tvCurrentPage = findViewById(R.id.tv_current_page);
        mkLoader = findViewById(R.id.mkloader);
        mkLoader.setVisibility(View.GONE);


        lvWaitingMeasureList = findViewById(R.id.lv_waiting_measure_list);
        btnFinish = findViewById(R.id.btn_finish);

        btnFinish.setOnClickListener(this);
        tvNextPage.setOnClickListener(this);
        tvLastPage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_menu_toolbar:

                onBackPressed();
                break;

            case R.id.btn_finish:
                AlertDialog.Builder builder = new AlertDialog.Builder(WaitingMeasureActivity.this);
                builder.setTitle("是否完成测量？");
                builder.setMessage("完成测量后，所有(重点)等待测量中的项目都不能再继续测量，是否确定完成测量？");
                builder.setPositiveButton("完成测量", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mkLoader.setVisibility(View.VISIBLE);
                        BleDataDbHelper dataDbHelper = new BleDataDbHelper(getApplicationContext());
                        for (int j=0;j<rulerCheckList.size();j++) {

                            /**
                             * 更新到本地
                             */
                            ContentValues values = new ContentValues();
                            values.put(DataBaseParams.measure_is_finish, 1);
                            String where = " id = ?";
                            String[] whereValues = new String[]{String.valueOf(rulerCheckList.get(j).getId())};
                            int result = dataDbHelper.updateDataToSqlite(DataBaseParams.measure_table_name, values, where, whereValues);
                            LogUtils.show("完成测量，更新数据是否成功："+rulerCheckList.get(j).getProjectName()+",更新状态："+result);
//                            更新完成后，更新集合中的状态，接下来向服务器发起更新的时候会用到状态标志
                            if (result > 0) {
                                rulerCheckList.get(j).setStatus(1);
                            }
//                            requestStopMeasure(rulerCheckList.get(j).getServerId());
                        }
                        dataDbHelper.close();
                        /**
                         * 更新到服务器
                         */
                        LogUtils.show("点击结束测量按钮-----查看开启服务前的总数："+rulerCheckList.size());
                        currentSuccessStopNum = 0;
                        stopTotalNum = rulerCheckList.size();

                        Intent serviceIntent = new Intent(WaitingMeasureActivity.this, PerformMeasureNetIntentService.class);
                        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FLAG_KEY, PerformMeasureNetIntentService.FLAG_FINISH_MEASURE);
                        serviceIntent.putExtra(PerformMeasureNetIntentService.GET_FINISH_MEASURE_KEY, (Serializable) rulerCheckList);
                        startService(serviceIntent);


//                        如果处理数据的服务还在运行，则停止服务
                        boolean isAlive = ServiceUtils.isServiceRunning(getApplicationContext(), "com.vitec.task.smartrule.service.HandleBleMeasureDataReceiverService");
                        if (isAlive) {
                            HandleBleMeasureDataReceiverService.stopHandleService(getApplicationContext());
                        }
//                        更新列表
//                        getUnFinishServerCheckData();
//                        updateAdapterData();


                    }
                });
                builder.setNegativeButton("继续测量", null);
                builder.show();

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
}
