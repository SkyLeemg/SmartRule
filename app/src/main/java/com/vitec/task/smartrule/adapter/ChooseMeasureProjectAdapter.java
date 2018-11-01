package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.vitec.task.smartrule.bean.EngineerBean;
import com.vitec.task.smartrule.bean.OptionBean;
import com.vitec.task.smartrule.utils.OperateDbUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *  选择测量工程的页面的Adapter，当用户点击新增项目的时候，Adapter数据增多
 */
public class ChooseMeasureProjectAdapter extends BaseAdapter {

    private static final String TAG = "ChooseMeasureProjectAdapter";
    private Context context;
    private int count;
    private List<EngineerBean> engineerBeanList;
    private List<String> spinnerList;
    private String chooseEngineer;

//    private List<String> engineers;

    public ChooseMeasureProjectAdapter(Context context,List<EngineerBean> engineerBeanList) {
        this.context = context;
        this.engineerBeanList = engineerBeanList;
        initSpinnerData();
    }

    private void initSpinnerData() {
        /**
         *获取集合中所有的工程名字，用于选择工程的spinner控件
         */
        spinnerList = new ArrayList<>();
        for (EngineerBean engineerBean : engineerBeanList) {
            String enginName = engineerBean.getProjectEngineer();
            if (enginName != null) {
                spinnerList.add(enginName);
            }

        }
    }


    @Override
    public int getCount() {
        return engineerBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return engineerBeanList.get(i);
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
//        holder.spinnerCheckProjectType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                chooseEngineer = spinnerList.get(i);
//            }
//        });
        holder.btnEnterMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseEngineer = spinnerList.get(0);
                EngineerBean engineer = engineerBeanList.get(i);
                engineer.setProjectEngineer(chooseEngineer);
                engineer.setProjectName(holder.autoTvProjectName.getText().toString());
                engineer.setCheckPositon(holder.autoTvCheckPosition.getText().toString());
                engineer.setPersonId(2);
                engineer.setCheckPerson("张三");
                engineer.setCheckTime(holder.tvCheckTime.getText().toString());
//                添加一个测量工程，相当于创建一个表格的表头
                int checkid = OperateDbUtil.addMeasureDataToSqlite(context, engineer);
                for (int i = 0; i < engineer.getMeasureBeanList().size(); i++) {
                    engineer.getMeasureBeanList().get(i).setCheckId(checkid);
                    engineer.getMeasureBeanList().get(i).setResourceID(R.mipmap.icon_data_selected);
                }
                Intent startIntent = new Intent(context, MeasureManagerAcitivty.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.putExtra("projectMsg", engineer);
                Log.e("", "onClick: 准备发给另外一个界面的数据信息："+engineerBeanList.get(i).toString() );
                context.startActivity(startIntent);
            }
        });


        return view;
    }

    public List<EngineerBean> getEngineerBeanList() {
        return engineerBeanList;
    }

    public void setEngineerBeanList(List<EngineerBean> engineerBeanList) {
        this.engineerBeanList = engineerBeanList;
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
