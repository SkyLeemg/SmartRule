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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.BaseActivity;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import java.util.List;

public class TeamMemberAdapter extends BaseAdapter{

    private List<ProjectUser> memberList;
    private Context context;
    private int[] bgs = {R.drawable.shape_oval_blue_team, R.drawable.shape_oval_green_team, R.drawable.shape_oval_pink_team, R.drawable.shape_oval_yellow_team};
    private boolean isShowDel = false;//是否显示删除按钮
    private RulerCheckProject project;

    public TeamMemberAdapter(Context context, List<ProjectUser> memberList) {
        this.memberList = memberList;
        this.context = context;
    }

    public TeamMemberAdapter(Context context, List<ProjectUser> memberList, RulerCheckProject project) {
        this.memberList = memberList;
        this.context = context;
        this.project = project;
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int i) {
        return memberList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_listview_team_menber, null);
            holder = new ViewHolder();
            holder.rlBg = view.findViewById(R.id.rl_oval);
            holder.tvMemberName = view.findViewById(R.id.tv_menber_name);
            holder.tvOvalName = view.findViewById(R.id.tv_oval_name);
            holder.line = view.findViewById(R.id.line);
            holder.imgDel = view.findViewById(R.id.img_del);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        LogUtils.show("查看第"+i+"个数据源:"+memberList.get(i).toString());
        final String memberName = memberList.get(i).getUserName();
//        设置名字数据
        holder.tvMemberName.setText(memberName);
//        设置圆形框里的名字显示
        if (memberName.length() > 2) {
            holder.tvOvalName.setText(memberName.substring(memberName.length()-2));
        } else {
            holder.tvOvalName.setText(memberName);
        }
//        设置圆形框的背景颜色
        int index = Math.abs(memberName.hashCode() % 4);
        holder.rlBg.setBackgroundResource(bgs[index]);
        if (i == (memberList.size() - 1)) {
            holder.line.setVisibility(View.GONE);
        }
//        设置删除按钮是否可见
        if (isShowDel) {
            holder.imgDel.setVisibility(View.VISIBLE);
        } else {
            holder.imgDel.setVisibility(View.GONE);
        }

        if (i == (memberList.size() - 1)) {
            holder.line.setVisibility(View.GONE);
        }

        holder.imgDel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final ProjectUser member = memberList.get(i);
                final User user = OperateDbUtil.getUser(context);
                String tip = "";
                if (user.getUserID()==member.getUser_id()) {
                    tip = "是否要要退出测量组:" + project.getProjectName() + "?";
                } else {
                    tip = "是否要删除成员:" + memberName + "?";

                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage(tip);
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle bundle = new Bundle();
                        bundle.putString(NetConstant.group_group_list, String.valueOf(member.getServer_id()));
                        Intent delUnitIntent = new Intent(context, ProjectManageRequestIntentService.class);
                        delUnitIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_del_member);
                        delUnitIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                        context.startService(delUnitIntent);
                    }
                });
                builder.show();
            }
        });

        return view;
    }

    public RulerCheckProject getProject() {
        return project;
    }

    public void setProject(RulerCheckProject project) {
        this.project = project;
    }

    public List<ProjectUser> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<ProjectUser> memberList) {
        this.memberList = memberList;
    }

    public boolean isShowDel() {
        return isShowDel;
    }

    public void setShowDel(boolean showDel) {
        isShowDel = showDel;
    }

    class ViewHolder {
        RelativeLayout rlBg;
        TextView tvOvalName;
        TextView tvMemberName;
        ImageView imgDel;
        View line;
    }


    /**
     * AlertDialog.Builder builder = new AlertDialog.Builder(context);
     builder.setTitle("提示");
     builder.setMessage("是否要要退出测量组:" + project.getProjectName() + "?");
     builder.setNegativeButton("取消", null);
     builder.setPositiveButton("确定退出", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
    Bundle bundle = new Bundle();
    bundle.putString(NetConstant.group_project_list, String.valueOf(project.getServer_id()));
    Intent delUnitIntent = new Intent(context, ProjectManageRequestIntentService.class);
    delUnitIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_del_project);
    delUnitIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
    context.startService(delUnitIntent);
    }
    });
     builder.show();
     */

}
