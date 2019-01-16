package com.vitec.task.smartrule.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.RulerCheck;
import com.vitec.task.smartrule.bean.RulerEngineer;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.BleDataDbHelper;
import com.vitec.task.smartrule.db.UserDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 查询测量数据页面
 *
 */
public class SearchMeasureDataActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "SearchMeasure";
    private AutoCompleteTextView tvProjectName;
    private AutoCompleteTextView tvCheckFloor;
    private Spinner spinnerEngineer;
    private Spinner spinnerPerson;
    private EditText etStartDate;
    private EditText etEndDate;
    private Button btnSearch;

    private BleDataDbHelper dataDbHelper;
    private UserDbHelper userDbHelper;

    private List<RulerEngineer> engineerList;//从数据库获取的所有工程对象集合
    private List<String> engineerNameList;//spinner工程的Adapter需要用到的数据集合

    private List<User> userList;//从数据库获取的所有用户对象的集合
    private List<String> userNameList;//用户spinner的adapter需要用到的数据集合
    //从数据库获取的iot_ruler_check表格的所有数据，用于tvProjectName和tvCheckFloor的数据源
    private List<RulerCheck> checkList;
    private List<String> projectNameList;//tvProjectName控件的数据源
    private List<String> checkFloorList;//tvCheckFloor控件的数据源

    private ArrayAdapter engineerAdapter;//工程spinner的Adapter
    private ArrayAdapter userAdapter;//用户spinner的Adapter
    private ArrayAdapter projectNameAdapter;//项目名的adapter
    private ArrayAdapter checkFloorAdapter;//检查位置的adapter
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private int mEndDay;
    private int mEndMonth;
    private int mEndYear;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_search_measure_data);
        initWidget();
        setTvTitle("查询测量数据");
        initView();

        initData();
    }

    /**
     * 初始化项目模板中的数据
     */
    private void initData() {
        /**
         * 从Sqlite数据库中获取所有的工程名，并初始化engineerAdapter
         *
         */
        dataDbHelper = new BleDataDbHelper(getApplicationContext());
        engineerList = new ArrayList<>();
        engineerNameList = new ArrayList<>();
        engineerList = dataDbHelper.queryEnginDataFromSqlite("");
        for (RulerEngineer engineer:engineerList) {
            engineerNameList.add(engineer.getEngineerName());
        }

        engineerAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, engineerNameList);
        spinnerEngineer.setAdapter(engineerAdapter);

        /**
         * 从Sqlite数据库中获取所有用户名，并初始化userAdapter
         */
        userDbHelper = new UserDbHelper(getApplicationContext());
        userNameList = new ArrayList<>();
        userList = new ArrayList<>();
        userList = userDbHelper.queryUserDataFromSqlite("");
        if (userList.size() > 0) {
            for (User user : userList) {
                Log.e(TAG, "initData: 查询到的用户数据："+user );
                userNameList.add(user.getUserName());
            }
        }

        userAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, userNameList);
        spinnerPerson.setAdapter(userAdapter);

        /**
         * 从sqlite数据库中获取所有的项目名和测量位置，这些信息在iot_ruler_check表格中
         */
        checkList = new ArrayList<>();
        checkFloorList = new ArrayList<>();
        projectNameList = new ArrayList<>();
        checkList = dataDbHelper.queryRulerCheckTableDataFromSqlite("");
//        初始化数据源
        for (int i=0;i<checkList.size();i++) {
            checkFloorList.add(checkList.get(i).getCheckFloor());
            projectNameList.add(checkList.get(i).getProject().getProjectName());
        }
//        初始化这两个控件的需要的adapter
        Log.e(TAG, "initData: 查看项目名："+projectNameList.toString() );
        projectNameAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, projectNameList);
        checkFloorAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, checkFloorList);
        tvCheckFloor.setAdapter(checkFloorAdapter);
        tvProjectName.setAdapter(projectNameAdapter);

        /**
         * 初始化日期数据
         */
        Calendar calendar = Calendar.getInstance();
        mStartYear = calendar.get(Calendar.YEAR);
        mStartMonth = calendar.get(Calendar.MONTH);
        mStartDay = calendar.get(Calendar.DAY_OF_MONTH);
        etStartDate.setText(mStartYear+"."+(mStartMonth+1)+"."+mStartDay);

        mEndYear = calendar.get(Calendar.YEAR);
        mEndMonth = calendar.get(Calendar.MONTH);
        mEndDay = calendar.get(Calendar.DAY_OF_MONTH);
        etEndDate.setText(mEndYear+"."+(mEndMonth+1)+"."+mEndDay);

    }

    private void initView() {
        imgIcon.setImageResource(R.mipmap.icon_back);
        imgIcon.setVisibility(View.VISIBLE);

        tvProjectName = findViewById(R.id.tv_project_type);
        tvCheckFloor = findViewById(R.id.tv_check_position);
        spinnerEngineer = findViewById(R.id.spinner_project_type);
        spinnerPerson = findViewById(R.id.spinner_check_person);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        btnSearch = findViewById(R.id.btn_search);

        etStartDate.setCursorVisible(false);
        etStartDate.setFocusable(false);
        etStartDate.setFocusableInTouchMode(false);

        etEndDate.setCursorVisible(false);
        etEndDate.setFocusableInTouchMode(false);
        etEndDate.setFocusable(false);

        imgIcon.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        etEndDate.setOnClickListener(this);
        etStartDate.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
           case  R.id.img_icon_toolbar://返回按钮
            SearchMeasureDataActivity.this.finish();
            break;

            case R.id.btn_search://查询按钮

                break;

            case R.id.et_start_date://点击弹出日期控件

                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        mStartDay = day;
                        mStartMonth = month;
                        mStartYear = year;
                        String data = year + "." + (month + 1) + "." + day;
                        if (mStartMonth < 10 && mStartDay < 10) {
                            data = year + ".0" + (month + 1) + ".0" + day;
                        }else if (mStartMonth < 10) {
                            data = year + ".0" + (month + 1) + "." + day;
                        }
                        else if (mStartDay < 10) {
                            data = year + "." + (month + 1) + ".0" + day;
                        }
                        etStartDate.setText(data);
                    }
                }, mStartYear, mStartMonth, mStartDay).show();

                break;

            case R.id.et_end_date://点击弹出日期控件
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        mEndDay = day;
                        mEndMonth = month;
                        mEndYear = year;
                        String data = year + "." + (month + 1) + "." + day;
                        if (month < 10 && day < 10) {
                            data = year + ".0" + (month + 1) + ".0" + day;
                        }else if (month < 10) {
                            data = year + ".0" + (month + 1) + "." + day;
                        }
                        else if (day < 10) {
                            data = year + "." + (month + 1) + ".0" + day;
                        }
                        etEndDate.setText(data);
                    }
                }, mEndYear, mEndMonth, mEndDay).show();

                break;
        }
    }
}
