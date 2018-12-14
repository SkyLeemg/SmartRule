package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MeasureManagerAcitivty;
import com.vitec.task.smartrule.bean.ChooseMeasureMsg;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.RulerOptions;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.interfaces.IChooseGetter;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  选择测量工程的页面的Adapter，当用户点击新增项目的时候，Adapter数据增多
 */
public class ChooseMeasureProjectAdapter extends BaseAdapter {

    private static final String TAG = "ChooseMeasureProjectAdapter";
    private Context context;
    private int count;
//    private List<EngineerBean> engineerBeanList;
    private List<String> spinnerList;
    private String chooseEngineerName;//选中的工程名称
    private int chooseEngineerIndex = 0;//spinner下拉框中选中的工程编号
    private List<ChooseMeasureMsg> chooseMeasureMsgList;
    private List<RulerEngineer> engineerList;
    private List<RulerOptions> optionsList;
    private IChooseGetter getter;

    //从数据库获取的iot_ruler_check表格的所有数据，用于tvProjectName和tvCheckFloor的数据源
    private List<RulerCheck> checkList;
    private List<String> projectNameList;//tvProjectName控件的数据源
    private List<String> checkFloorList;//tvCheckFloor控件的数据源
    private BleDataDbHelper dataDbHelper;

    private ArrayAdapter projectNameAdapter;//项目名的adapter
    private ArrayAdapter checkFloorAdapter;//检查位置的adapter


//    private List<String> engineers;

    public ChooseMeasureProjectAdapter(Context context,IChooseGetter getter) {
        this.context = context;
        this.getter = getter;
        initData();

    }

    public void initData() {
        chooseMeasureMsgList = getter.getChooseMeasureMsgList();
        engineerList = getter.getEngineerList();
        optionsList = getter.getOptionsList();
        initSpinnerData();
    }

    private void initSpinnerData() {
        /**
         *获取集合中所有的工程名字，用于选择工程的spinner控件
         */
        spinnerList = new ArrayList<>();
        for (RulerEngineer engineer : engineerList) {
            String enginName = engineer.getEngineerName();
            if (enginName != null) {
                spinnerList.add(enginName);
            }
        }
        Log.e("2aaa", "initSpinnerData: 查看spinnerList:"+spinnerList.toString()+"，engineerList："+ engineerList.toString());

        /**
         * 从sqlite数据库中获取所有的项目名和测量位置，这些信息在iot_ruler_check表格中
         */
        checkList = new ArrayList<>();
        checkFloorList = new ArrayList<>();
        projectNameList = new ArrayList<>();
        dataDbHelper = new BleDataDbHelper(context);
        checkList = dataDbHelper.queryRulerCheckTableDataFromSqlite("");
//        初始化数据源
        for (int i=0;i<checkList.size();i++) {
            checkFloorList.add(checkList.get(i).getCheckFloor());
            projectNameList.add(checkList.get(i).getProjectName());
        }
        projectNameAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, projectNameList);
        checkFloorAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, checkFloorList);

    }


    @Override
    public int getCount() {
        return chooseMeasureMsgList.size();
    }

    @Override
    public Object getItem(int i) {
        return chooseMeasureMsgList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_single_project_choose, null);
            holder = new ViewHolder();
            holder.btnEnterMeasure = view.findViewById(R.id.btn_enter_measure);
            holder.tvCheckPerson = view.findViewById(R.id.tv_check_person);
            holder.tvCheckTime = view.findViewById(R.id.tv_check_time);
            holder.autoTvCheckPosition = view.findViewById(R.id.tv_check_position);
            holder.autoTvProjectName = view.findViewById(R.id.tv_project_type);
            holder.spinnerCheckProjectType = view.findViewById(R.id.spinner_project_type);
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (spinnerList.size() > 0) {
            final ArrayAdapter listArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, spinnerList);
            holder.spinnerCheckProjectType.setAdapter(listArrayAdapter);
        }
        Log.e("aaa", "getView: Adapter中收到的集合对象："+ chooseMeasureMsgList.size()+",内容"+chooseMeasureMsgList.get(i).toString());
//        如果是之前就测量过的项目则不可更改
        if (chooseMeasureMsgList.get(i).getRulerCheck() != null && chooseMeasureMsgList.get(i).getRulerCheck().getId() > 0) {
            holder.autoTvCheckPosition.setText(chooseMeasureMsgList.get(i).getRulerCheck().getCheckFloor());
            holder.autoTvProjectName.setText(chooseMeasureMsgList.get(i).getRulerCheck().getProjectName());
            holder.autoTvProjectName.setClickable(false);
            holder.autoTvCheckPosition.setClickable(false);
        } else {
            holder.autoTvCheckPosition.setText("");
            holder.autoTvProjectName.setText("");
        }

        holder.tvCheckPerson.setText(chooseMeasureMsgList.get(i).getUser().getUserName());
        holder.tvCheckTime.setText(chooseMeasureMsgList.get(i).getCreateDate());

        holder.autoTvCheckPosition.setAdapter(checkFloorAdapter);
        holder.autoTvProjectName.setAdapter(projectNameAdapter);

        holder.spinnerCheckProjectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < spinnerList.size()) {
                    chooseEngineerName = spinnerList.get(i);
                    chooseEngineerIndex = i;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        holder.btnEnterMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 在进入测量时主要包括以下操作：
                 * 1.判断ChooseMeasureMsg中是否已经有rulercheck数据内容
                 *      有则说明该项时之前测量过的，直接引用之前的rulercheck
                 *      无则说明是需要新添加的
                 *  新添加的项目还需要做以下几点：
                 *   1.新添加的需要更新数据，做备份，测量页面返回时会用到。
                 *   2.把数据更新到iot_ruler_check表格中
                 *   3.把数据上传到服务器
                 *   4.全部操作完成后跳转页面，同时传一个rulerCheck对象给下一个页面
                 */
                RulerCheck rulerCheck = new RulerCheck();

//                判断ChooseMeasureMsg中是否已经有rulercheck数据内容
                if (chooseMeasureMsgList.get(i).getRulerCheck() != null && chooseMeasureMsgList.get(i).getRulerCheck().getId() > 0) {
                    Log.e("aaa", "onClick: 之前引用之前的" );
//                    有则说明该项时之前测量过的，直接引用之前的rulercheck
                    rulerCheck = chooseMeasureMsgList.get(i).getRulerCheck();
                    rulerCheck.setProjectName(holder.autoTvProjectName.getText().toString());
                    rulerCheck.setCheckFloor(holder.autoTvCheckPosition.getText().toString());
//
                } else {
//                    无则说明是需要新添加的,把数据添加到对象中
                    Log.e("aaa", "onClick: 新添加的,项目名："+ holder.autoTvProjectName.getText().toString().trim()+
                            ",楼层："+holder.autoTvCheckPosition.getText().toString().trim());
                    rulerCheck.setProjectName(holder.autoTvProjectName.getText().toString().trim());
                    rulerCheck.setCheckFloor(holder.autoTvCheckPosition.getText().toString().trim());
                    rulerCheck.setUser(chooseMeasureMsgList.get(i).getUser());
                    rulerCheck.setCreateDate(String.valueOf(DateFormatUtil.getDate()));
                    rulerCheck.setCreateTime(DateFormatUtil.transForMilliSecond(new Date()));
                    LogUtils.show("保存下来的createtime时间戳："+DateFormatUtil.transForMilliSecond(new Date()));
                    rulerCheck.setUpload_flag(0);
                    if (chooseEngineerIndex < engineerList.size()) {
                        if (engineerList.get(chooseEngineerIndex).equals(chooseEngineerName)) {
                            rulerCheck.setEngineer(engineerList.get(chooseEngineerIndex));
                        } else {
                            for (RulerEngineer engineer1 : engineerList) {
                                if (engineer1.getEngineerName().equals(chooseEngineerName)) {
                                    rulerCheck.setEngineer(engineer1);
                                }
                            }
                        }
                    }
//                   把数据更新到iot_ruler_check表格中,返回表格的表头ID
                    Log.e("aaa", "onClick: 查看adapter这里收到的rulerCheck:"+rulerCheck.toString() );
                    int checkid = OperateDbUtil.addMeasureDataToSqlite(context, rulerCheck);
                    rulerCheck.setId(checkid);
                    //新添加的需要更新数据，做备份，测量页面返回时会用到。同时将此item的数据更新，返回到此界面时需要记录数据
                    chooseMeasureMsgList.get(i).setCheckFloor(holder.autoTvCheckPosition.getText().toString().trim());
                    chooseMeasureMsgList.get(i).setProjectName(holder.autoTvProjectName.getText().toString().trim());
                    chooseMeasureMsgList.get(i).setRulerCheck(rulerCheck);
                    getter.updateChooseMeasureMsgList(i, chooseMeasureMsgList.get(i));

                }

                Intent startIntent = new Intent(context, MeasureManagerAcitivty.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.putExtra("projectMsg", rulerCheck);
                Log.e("chakabiaozhi", "onClick: 查看准备发给另外一个界面的数据信息："+rulerCheck.toString() );
                context.startActivity(startIntent);
            }
        });


        return view;
    }

    public List<ChooseMeasureMsg> getChooseMeasureMsgList() {
        return chooseMeasureMsgList;
    }

    public void setChooseMeasureMsgList(List<ChooseMeasureMsg> chooseMeasureMsgList) {
        this.chooseMeasureMsgList = chooseMeasureMsgList;
    }

    class ViewHolder{
        Button btnEnterMeasure;
        TextView tvCheckTime;
        TextView tvCheckPerson;
        AutoCompleteTextView autoTvCheckPosition;
        AutoCompleteTextView autoTvProjectName;
        Spinner spinnerCheckProjectType;

    }
}
