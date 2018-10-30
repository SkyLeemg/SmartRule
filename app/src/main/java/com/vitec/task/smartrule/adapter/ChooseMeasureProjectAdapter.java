package com.vitec.task.smartrule.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.MeasureManagerAcitivty;
import com.vitec.task.smartrule.bean.MeasureBean;

import java.util.List;

public class ChooseMeasureProjectAdapter extends BaseAdapter {

    private Context context;
    private int count;
    private List<MeasureBean> projects;

    public ChooseMeasureProjectAdapter(Context context,List<MeasureBean> projects) {
        this.context = context;
        this.projects = projects;
    }

    @Override
    public int getCount() {
        return projects.size();
    }

    @Override
    public Object getItem(int i) {
        return projects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
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

        holder.btnEnterMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(context, MeasureManagerAcitivty.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
            }
        });
        return view;
    }

    public List<MeasureBean> getProjects() {
        return projects;
    }

    public void setProjects(List<MeasureBean> projects) {
        this.projects = projects;
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
