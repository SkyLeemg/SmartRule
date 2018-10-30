package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.adapter.ChooseMeasureProjectAdapter;
import com.vitec.task.smartrule.bean.MeasureBean;
import com.vitec.task.smartrule.utils.HeightUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 待解决问题：
 * 1.解决listview和scrollview滑动冲突的问题
 *
 */
public class ChooseMeasureMsgActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG ="ChooseMeasureMsgActivity";
    private ListView lvChoose;
    private ImageView imgAddProject;
    private ChooseMeasureProjectAdapter projectAdapter;
    private List<MeasureBean> projects;
    private int projectCount = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_measure_msg2);

        initView();
        initData();


    }

    private void initData() {
        projects = new ArrayList<>();
        projects.add(new MeasureBean());
        projectAdapter = new ChooseMeasureProjectAdapter(this, projects);
        lvChoose.setAdapter(projectAdapter);
        HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
    }

    private void initView() {
        lvChoose = findViewById(R.id.lv_choose_msg);


        imgAddProject = findViewById(R.id.img_add_project);
        imgAddProject.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_add_project:
                projects.add(new MeasureBean());
                projectAdapter.setProjects(projects);
                projectAdapter.notifyDataSetChanged();
                HeightUtils.setListViewHeighBaseOnChildren(lvChoose);
                Log.e("aaa", "onClick: 点击了添加按钮" );
                break;
        }
    }
}
