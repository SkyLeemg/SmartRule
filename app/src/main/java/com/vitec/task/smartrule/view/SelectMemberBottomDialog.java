package com.vitec.task.smartrule.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectMemberBottomDialog extends Dialog implements View.OnClickListener{

    private List<ProjectUser> memberList;
    private RulerCheckProject project;
    private TextView tvSubmit;
    private TextView tvCancel;
    private TextView tvTip;
    private ListView lvContent;
    private MemberAdapter memberAdapter;
    private int clickIndex = -1;//设置被点击的item，都没用被点击则置为-1，有则等于该item的序号

    public SelectMemberBottomDialog(@NonNull Context context, int themeResId, List<ProjectUser> memList, RulerCheckProject project) {
        super(context, themeResId);
//        this.memberList = memberList;
        this.project = project;
        memberList = new ArrayList<>();
        for (int i=0;i<memList.size();i++) {
            if (memList.get(i).getUser_id() != project.getUser().getUserID()) {
                this.memberList.add(memList.get(i));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bottom_slector_member);
        //获取当前Activity所在的窗体
        Window dialogWindow = getWindow();
        if(dialogWindow == null)
        { return; }
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //设置Dialog距离底部的距离
        // 将属性设置给窗体
        dialogWindow.setAttributes(lp);

        tvCancel = findViewById(R.id.tv_cancel);
        tvSubmit = findViewById(R.id.tv_submit);
        tvTip = findViewById(R.id.tv_tip);
        lvContent = findViewById(R.id.lv_dialog_selector_list);
        if (memberList.size() == 0) {
            tvTip.setVisibility(View.VISIBLE);
            tvSubmit.setVisibility(View.GONE);
        } else {
            tvTip.setVisibility(View.GONE);
            tvSubmit.setVisibility(View.VISIBLE);
        }
        memberAdapter = new MemberAdapter();
        lvContent.setAdapter(memberAdapter);

        tvSubmit.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        /**
         * 单选监听
         */
        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtils.show("点击了item");
                clickIndex = i;
                memberAdapter.notifyDataSetChanged();
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 确定按钮
             */
            case R.id.tv_submit:
                if (clickIndex < 0) {
                    Toast.makeText(getContext(), "您还没有选择新群主", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (clickIndex < memberList.size()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("转让确认");
                    String msg = "确定要将群主转让给" + memberList.get(clickIndex).getUserName() + "吗？";
                    builder.setMessage(msg);
                    builder.setPositiveButton("确定", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(DataBaseParams.project_server_id, project.getServer_id());
                            bundle.putInt(DataBaseParams.user_user_id, memberList.get(clickIndex).getUser_id());
                            bundle.putInt(NetConstant.group_owner_id, project.getUser().getUserID());
                            Intent intent = new Intent(getContext(), ProjectManageRequestIntentService.class);
                            intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_trasfer_master);
                            intent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                            getContext().startService(intent);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "请重新选择群主", Toast.LENGTH_SHORT).show();
                }
                break;

            /**
             * 取消
             */
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }


    class MemberAdapter extends BaseAdapter {

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
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_list_view_member_select_dialog, null);
                holder = new ViewHolder();
                holder.tvMemberName = view.findViewById(R.id.tv_menber_name);
                holder.radioButton = view.findViewById(R.id.radio_btn);
                holder.line = view.findViewById(R.id.line);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvMemberName.setText(memberList.get(i).getUserName());
            if (i == (memberList.size() - 1)) {
                holder.line.setVisibility(View.GONE);
            } else {
                holder.line.setVisibility(View.VISIBLE);
            }
            holder.radioButton.setClickable(false);
            if (clickIndex == i) {
                holder.radioButton.setChecked(true);
            } else {
                holder.radioButton.setChecked(false);
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickIndex = i;
                    memberAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }


    class ViewHolder {
        TextView tvMemberName;
        RadioButton radioButton;
        View line;

    }

}
