package com.vitec.task.smartrule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;

public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener{

    private EditText etOldPwd;
    private EditText etNewPwd;
    private EditText etNewRepeatPwd;
    private Button btnSubmit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initView();
    }

    private void initView() {
        initWidget();
//        imgIcon.setImageResource(R.mipmap.icon_back);
//        imgIcon.setVisibility(View.VISIBLE);
//        imgIcon.setOnClickListener(this);

        etNewPwd = findViewById(R.id.et_new_pwd);
        etNewRepeatPwd = findViewById(R.id.et_new_repeat_pwd);
        etOldPwd = findViewById(R.id.et_old_pwd);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_icon_toolbar:
                ChangePasswordActivity.this.finish();
                break;

            case R.id.btn_submit:
                final String newPwd = etNewPwd.getText().toString();
                String newRepeatPwd = etNewRepeatPwd.getText().toString();
                if (etOldPwd.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(),"请输入旧密码",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPwd.length() == 0) {
                    Toast.makeText(getApplicationContext(),"请输入新密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newRepeatPwd.length() == 0) {
                    Toast.makeText(getApplicationContext(),"请输入确认密码",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPwd.equals(newRepeatPwd)) {
                    Toast.makeText(getApplicationContext(),"密码不一致",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPwd.contains(" ")) {
                    Toast.makeText(getApplicationContext(),"密码不能包含空格",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPwd.length() < 6) {
                    Toast.makeText(getApplicationContext(),"密码长度不能小于6",Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        User user = OperateDbUtil.getUser(ChangePasswordActivity.this);
                        String token = user.getToken();
                        String oldPwd = etOldPwd.getText().toString();
                        OkHttpUtils.Param oldPwdParam = new OkHttpUtils.Param(NetConstant.change_pwd_password, oldPwd);
                        OkHttpUtils.Param newPwdParam = new OkHttpUtils.Param(NetConstant.change_pwd_new_password, newPwd);
                        OkHttpUtils.Param tokenParam = new OkHttpUtils.Param(NetConstant.change_pwd_token, token);
                        List<OkHttpUtils.Param> paramList = new ArrayList<>();
                        paramList.add(oldPwdParam);
                        paramList.add(tokenParam);
                        paramList.add(newPwdParam);
                        LogUtils.show("打印更改密码请求数据："+paramList);
                        String url = NetConstant.baseUrl + NetConstant.change_pwd_url;
                        OkHttpUtils.post(url, new OkHttpUtils.ResultCallback<String>() {
                            @Override
                            public void onSuccess(String response)  {
                                /**
                                 * {"status":"error","code":404,"msg":{"status":400,"statusInfo":"未登录！"}}
                                 * d5b6665dc42e57d038dd23e41a7f8d19
                                 * d5b6665dc42e57d038dd23e41a7f8d19
                                 */
                                LogUtils.show("更改密码返回的信息："+response);
                                try {
                                    JSONObject object = new JSONObject(response);
                                    int code = object.optInt("code");
                                    if (code == 200) {
                                        Toast.makeText(getApplicationContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                                        etNewPwd.setText("");
                                        etNewRepeatPwd.setText("");
                                        etOldPwd.setText("");

                                    } else {
//                                        JSONObject msgJson = new JSONObject(object.optString("msg"));
                                        String msg = object.optString("msg");
                                        if (msg.contains("未登录")) {
                                            Toast.makeText(getApplicationContext(), "登录已过期,请重新登录再重试", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                        }
//                                        String info = msgJson.optString("statusInfo");

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(),"修改失败",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                            }
                        },paramList);
                    }
                }).start();

                break;
        }
    }
}
