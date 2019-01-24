package com.vitec.task.smartrule.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.event.UpdateUserMessageResutCallBack;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.ChangeUserMessageIntentService;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UpdateUserNameActivity extends BaseActivity implements OnClickListener{

    private EditText etUserName;
    private Button btnSubmit;
    private User user;
    private boolean hasUpdate = false;//用来判断个人信息是否有更新

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);
        initView();
        EventBus.getDefault().register(this);
        initData();
    }

    private void initData() {
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            User tUser = OperateDbUtil.getUser(getApplicationContext());
            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
            List<User> userList = new ArrayList<>();
            user = new User();
            String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID();
            userList = userDbHelper.queryUserDataFromSqlite(where);
            if (userList.size() > 0) {
                user = userList.get(0);
            } else {
                user = tUser;
            }
        }
    }

    private void initView() {
        initWidget();
        setTvTitle("修改姓名");
        etUserName = findViewById(R.id.et_name);
        btnSubmit = findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(this);
        btnSubmit.setClickable(false);
        etUserName.addTextChangedListener(userNameTextWather);

    }

    private TextWatcher userNameTextWather = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etUserName.getText().toString().trim().length() > 1) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.selector_login_btn_click);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.shape_btn_blue_unclick);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (hasUpdate) {
            intent.putExtra("flag", 1);
        } else {
            intent.putExtra("flag", 0);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    /**
     * 修改手机号码接口返回的信息
     * @param message
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatemobileRequestCallBack(UpdateUserMessageResutCallBack message) {
        if (message.isSuccess()) {
//            更新到本地数据库
            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
            final ContentValues values = new ContentValues();
            values.put(DataBaseParams.user_user_name,etUserName.getText().toString());
            String where = DataBaseParams.user_user_id + "=?";
            userDbHelper.updateUserData(values,where, new String[]{String.valueOf(user.getUserID())});
            Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
            hasUpdate = true;
            onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), message.getMsg(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_submit:
                if (TextUtils.isEmpty(etUserName.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(),"用户名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.register_name, etUserName.getText().toString().trim());
                paramList.add(param);
                Intent intent = new Intent(UpdateUserNameActivity.this, ChangeUserMessageIntentService.class);
                intent.putExtra(ChangeUserMessageIntentService.PARAM_LIST_KEY, (Serializable) paramList);
                startService(intent);
                break;
        }
    }
}
