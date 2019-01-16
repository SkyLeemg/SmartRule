package com.vitec.task.smartrule.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerUnitEngineer;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;

import java.util.List;

public class GroupUnitAdapter extends BaseAdapter {
    private List<RulerUnitEngineer> unitEngineerList;
    private Context context;
    private boolean isShowDel = false;//是否显示删除按钮

    public GroupUnitAdapter(Context context, List<RulerUnitEngineer> unitEngineers) {
        this.context = context;
        this.unitEngineerList = unitEngineers;
    }

    @Override
    public int getCount() {
        return unitEngineerList.size();
    }

    @Override
    public Object getItem(int i) {
        return unitEngineerList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_listview_unit_engineer_name, null);
            holder = new ViewHolder();
            holder.tvUnitName = view.findViewById(R.id.tv_item_unit_name);
            holder.line = view.findViewById(R.id.line);
            holder.imgDel = view.findViewById(R.id.img_del);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

//        设置检查位置的名称
        holder.tvUnitName.setText(unitEngineerList.get(i).getLocation());

//        设置删除按钮是否可见
        if (isShowDel) {
            holder.imgDel.setVisibility(View.VISIBLE);
        } else {
            holder.imgDel.setVisibility(View.GONE);
        }

        holder.imgDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RulerUnitEngineer engineer = unitEngineerList.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("是否要删除:" + engineer.getLocation() + "?");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = new Bundle();
                        bundle.putString(NetConstant.group_unit_list, String.valueOf(engineer.getServer_id()));
                        Intent delUnitIntent = new Intent(context, ProjectManageRequestIntentService.class);
                        delUnitIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_del_unit_engineer);
                        delUnitIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                        context.startService(delUnitIntent);
                    }
                });
                builder.show();
            }
        });

        return view;

    }

    public void setUnitEngineerList(List<RulerUnitEngineer> unitEngineerList) {
        this.unitEngineerList = unitEngineerList;
    }

    public boolean isShowDel() {
        return isShowDel;
    }

    public void setShowDel(boolean showDel) {
        isShowDel = showDel;
    }

    class ViewHolder {
        TextView tvUnitName;
        View line;
        ImageView imgDel;
    }
}

