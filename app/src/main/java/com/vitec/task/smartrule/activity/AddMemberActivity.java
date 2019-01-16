package com.vitec.task.smartrule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.TeamMemberAdapter;
import com.vitec.task.smartrule.bean.OnceMeasureData;
import com.vitec.task.smartrule.bean.ProjectUser;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.ProjectGroupAnyMsgEvent;
import com.vitec.task.smartrule.bean.event.QueryProjectGroupMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AddMemberActivity extends BaseActivity implements View.OnClickListener{

//    private TextView tvCancel;
//    private TextView tvSave;
    private EditText etPhone;
    private Button btnSaveAndAdd;
    private ListView lvMember;
    private TextView tvTip;
    private TeamMemberAdapter memberAdapter;
    private List<ProjectUser> userList;
    private RulerCheckProject project;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        intView();
        initData();
        EventBus.getDefault().register(this);
    }




    private void initData() {
        userList = new ArrayList<>();
        memberAdapter = new TeamMemberAdapter(this, userList);
        lvMember.setAdapter(memberAdapter);
        project = new RulerCheckProject();
        project = (RulerCheckProject) getIntent().getSerializableExtra(DataBaseParams.measure_project_id);
    }

    private void intView() {
        initWidget();
        setTvTitle("添加团队成员");
//        initWidget(tvCancel, R.id.tv_cancel);
        etPhone = findViewById(R.id.et_input_phone);
        btnSaveAndAdd = findViewById(R.id.btn_search);
        lvMember = findViewById(R.id.lv_member);
        tvTip = findViewById(R.id.tv_third_tip);
//        initWidget(etPhone, R.id.et_input_phone);
//        initWidget(btnSaveAndAdd, R.id.btn_search);
//        initWidget(lvMember, R.id.lv_member);
//        initWidget(tvTip, R.id.tv_third_tip);

        tvTip.setVisibility(View.GONE);
        lvMember.setVisibility(View.GONE);
        btnSaveAndAdd.setClickable(false);
        etPhone.addTextChangedListener(textWatcher);

        btnSaveAndAdd.setOnClickListener(this);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etPhone.length() == 11) {
                btnSaveAndAdd.setBackgroundResource(R.drawable.selector_login_btn_click);
                btnSaveAndAdd.setClickable(true);
            } else {
                btnSaveAndAdd.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
                btnSaveAndAdd.setClickable(false);
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LogUtils.show("添加页面已经销毁----");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 查找并添加按钮
             */
            case R.id.btn_search:
//                ProjectUser user = new ProjectUser();
//                user.setUserName("张三丰");
//                user.setMobile(etPhone.getText().toString());
//                userList.add(user);

                Intent queryIntent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                Bundle bundle = new Bundle();
                bundle.putString(DataBaseParams.user_mobile, etPhone.getText().toString().trim());
                bundle.putInt(DataBaseParams.measure_project_id, project.getServer_id());
                queryIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_add_member);
                queryIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                startService(queryIntent);

                break;
        }
    }



    /**
     * 添加成员的回调的回调
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void projectGroupEventCallBack(ProjectGroupAnyMsgEvent event) {
        if (event.getRequst_flag() == 2) {
            if (event.isSuccess()) {
                userList.add((ProjectUser) event.getObject());
                tvTip.setVisibility(View.VISIBLE);
                lvMember.setVisibility(View.VISIBLE);
                memberAdapter.setMemberList(userList);
                memberAdapter.notifyDataSetChanged();
            }
        }

    }

}
