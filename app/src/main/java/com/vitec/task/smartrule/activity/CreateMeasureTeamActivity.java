package com.vitec.task.smartrule.activity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.MoblieRequestResutEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.List;

public class CreateMeasureTeamActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvCreateTeam;
    private EditText etProjectName;
    private MKLoader mkLoader;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_measure_team);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        initWidget();
        setTvTitle("测量组");
        etProjectName = findViewById(R.id.et_project_name);
        tvCreateTeam = findViewById(R.id.tv_create_team);
        mkLoader = findViewById(R.id.mkloader);
        tvCreateTeam.setOnClickListener(this);
        user = OperateDbUtil.getUser(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void createProjectResultCallBack(MoblieRequestResutEvent event) {
        if (event.isSuccess()) {
            Bundle bundle = new Bundle();
            String where = " where " + DataBaseParams.check_project_name + "= \"" + etProjectName.getText().toString().trim() + "\""
                    + " and " + DataBaseParams.user_user_id + " = " + user.getUserID() + "  ORDER BY " + DataBaseParams.measure_create_time + " DESC;";
            List<RulerCheckProject> projectList = OperateDbUtil.queryProjectDataFromSqlite(getApplicationContext(), where);
            if (projectList.size() > 0) {
                bundle.putSerializable(DataBaseParams.check_project_name, (Serializable) projectList);
            }
            Intent intent = new Intent(this, MeasureTeamManagerActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            mkLoader.setVisibility(View.GONE);
        } else {
            mkLoader.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), event.getMsg(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_create_team:
//
                mkLoader.setVisibility(View.VISIBLE);

                Intent intent = new Intent(getApplicationContext(), ProjectManageRequestIntentService.class);
                Bundle bundle = new Bundle();
                bundle.putString(DataBaseParams.check_project_name, etProjectName.getText().toString().trim());
                bundle.putString(DataBaseParams.user_user_id, String.valueOf(user.getUserID()));
                intent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_create_project);
                intent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                startService(intent);

                break;
        }
    }
}
