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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private int clickIndex = -1;//是否被点击了，被点击的项目显示出编辑按钮
    private int showEditIndex = -1;//是否点击了编辑i按钮，被点击编辑按钮的项目显示出修改页面---输入框和确定修改按钮

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
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_listview_unit_engineer_name, null);
            holder = new ViewHolder();
            holder.tvUnitName = view.findViewById(R.id.tv_item_unit_name);
            holder.line = view.findViewById(R.id.line);
            holder.imgDel = view.findViewById(R.id.img_del);
            holder.tvEdit = view.findViewById(R.id.tv_edit);
            holder.tvChangeUnit = view.findViewById(R.id.tv_change_unit);
            holder.etUnitName = view.findViewById(R.id.et_input_unit_name);
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

        //是否显示编辑按钮，被点击的项目才显示编辑按钮
        if (clickIndex==i) {
            holder.tvEdit.setVisibility(View.VISIBLE);
            holder.imgDel.setVisibility(View.GONE);
        }else {
            holder.tvEdit.setVisibility(View.GONE);
        }

        //是否显示编辑页面
        if (showEditIndex==i) {
            holder.etUnitName.setVisibility(View.VISIBLE);
            holder.tvChangeUnit.setVisibility(View.VISIBLE);
            holder.tvUnitName.setVisibility(View.INVISIBLE);
            holder.imgDel.setVisibility(View.GONE);
            holder.etUnitName.setText(unitEngineerList.get(i).getLocation());
        }else {
            holder.etUnitName.setVisibility(View.GONE);
            holder.tvChangeUnit.setVisibility(View.GONE);
            holder.tvUnitName.setVisibility(View.VISIBLE);
        }

        if (i == unitEngineerList.size() - 1) {
            holder.line.setVisibility(View.INVISIBLE);
        }

        //删除按钮的点击事件
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

        //编辑按钮的点击事件
        holder.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickIndex = -1;
                showEditIndex = i;
                notifyDataSetChanged();
            }
        });

        //修改单位工程名按钮的点击事件
        holder.tvChangeUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.etUnitName.getText().toString().trim().equals(unitEngineerList.get(i).getLocation())) {
                    Toast.makeText(context,"单位工程名未改变",Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(DataBaseParams.unit_engineer_location, holder.etUnitName.getText().toString().trim());
                bundle.putInt(DataBaseParams.project_server_id, unitEngineerList.get(i).getProject_server_id());
                bundle.putInt(DataBaseParams.measure_id, unitEngineerList.get(i).getServer_id());
                Intent intent = new Intent(context, ProjectManageRequestIntentService.class);
                intent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_update_unit_engineer);
                context.startService(intent);
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

    public List<RulerUnitEngineer> getUnitEngineerList() {
        return unitEngineerList;
    }


    public int getClickIndex() {
        return clickIndex;
    }

    public void setClickIndex(int clickIndex) {
        this.clickIndex = clickIndex;
    }

    public int getShowEditIndex() {
        return showEditIndex;
    }

    public void setShowEditIndex(int showEditIndex) {
        this.showEditIndex = showEditIndex;
    }

    class ViewHolder {
        TextView tvUnitName;
        View line;
        ImageView imgDel;
        TextView tvEdit;
        TextView tvChangeUnit;
        EditText etUnitName;
    }
}

