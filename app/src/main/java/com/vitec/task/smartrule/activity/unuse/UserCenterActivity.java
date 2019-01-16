package com.vitec.task.smartrule.activity.unuse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuyenmonkey.mkloader.MKLoader;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.BaseActivity;
import com.vitec.task.smartrule.activity.ChangePasswordActivity;
import com.vitec.task.smartrule.activity.CheckUpdateActivity;
import com.vitec.task.smartrule.activity.ContactOurActivity;
import com.vitec.task.smartrule.activity.LoginActivity;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.WxResultMessage;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.utils.ScreenSizeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserCenterActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvUserName;
    private TextView tvUserNameEditor;
    private TextView tvJob;
    private TextView tvJobEditor;
    private TextView tvMobile;
    private TextView tvMobileEditor;
    private TextView tvWx;
    private TextView tvWxEditor;
    private ListView listView;

    private User user;
    UserDbHelper userDbHelper;
    private List<User> userList;
    private String job;

    private WeChatHelper weChatHelper;
    private int countDown = 60;

    private List<ItemBean> itemList;//item选项的集合
    private UserListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    private void initData() {


        /**
         * 初始化上面的用户信息
         */
        User tUser = OperateDbUtil.getUser(this);
        userDbHelper = new UserDbHelper(this);
        userList = new ArrayList<>();
        user = new User();

        String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID() ;
        userList = userDbHelper.queryUserDataFromSqlite(where);

        if (userList.size() > 0) {
            user = userList.get(0);
        } else {
            user = tUser;
        }
        LogUtils.show("UserCenterActivity---查看最后user获取的数据:"+user);
//        初始化用户名数据
        if ( user.getUserID() > 0) {
            tvUserNameEditor.setVisibility(View.INVISIBLE);
            tvUserName.setText(user.getUserName());
        }
//        else if (!tUser.getWid().equalsIgnoreCase("null") && !tUser.getWid().equalsIgnoreCase("0")) {
//            tvUserNameEditor.setVisibility(View.VISIBLE);
//            tvUserName.setText("--");
//        }
//        初始化岗位数据
//        try {
////            JSONArray jsonArray = new JSONArray(user.getUserJob());
////            if (jsonArray.length() > 0) {
////                job = jsonArray.getString(0);
////                tvJob.setText(job);
////                tvJobEditor.setVisibility(View.INVISIBLE);
////            } else {
////                tvJob.setText("--");
////                tvJobEditor.setVisibility(View.VISIBLE);
////            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        初始化手机号码
        if (user.getMobile()!=null && !user.getMobile().equalsIgnoreCase("0") && user.getMobile().length() > 5) {
            tvMobile.setText(user.getMobile());
            tvMobileEditor.setVisibility(View.INVISIBLE);
        } else {
            tvMobile.setText("--");
            tvMobileEditor.setVisibility(View.VISIBLE);
        }
//        tvMobileEditor.setVisibility(View.VISIBLE);

//        初始化微信号
        if (user.getWxData() == null || user.getWxData().equalsIgnoreCase("null") || user.getWxData().equalsIgnoreCase("")) {
            tvWx.setText("--");
            tvWxEditor.setVisibility(View.VISIBLE);
        } else {
            try {
                JSONObject object = new JSONObject(user.getWxData());
                String nickName = object.optString("nickname");
                tvWx.setText(nickName);
                tvWxEditor.setVisibility(View.INVISIBLE);
            } catch (JSONException e) {
                tvWx.setText("--");
                tvWxEditor.setVisibility(View.VISIBLE);
                e.printStackTrace();

            }
        }


        /**
         * 初始化listview
         */
        itemList = new ArrayList<>();
        itemList.add(new ItemBean(R.mipmap.icon_user_edit,"编辑用户信息"));
        itemList.add(new ItemBean(R.mipmap.icon_user_change_pwd,"更改密码"));
        itemList.add(new ItemBean(R.mipmap.icon_user_unselect, "退出当前账号"));
        itemList.add(new ItemBean(R.mipmap.icon_user_update,"检查更新"));
        itemList.add(new ItemBean(R.mipmap.icon_intro_unselected,"使用说明"));
        itemList.add(new ItemBean(R.mipmap.icon_intro_unselected,"联系我们"));
        listAdapter = new UserListAdapter();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0://编辑用户信息

                        break;
                    case 1://更改密码
                        Intent cbIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                        startActivity(cbIntent);
                        break;

                    case 2://切换账号
                        Intent cgIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(cgIntent);
                        UserCenterActivity.this.finish();
                        break;

                    case 3://检查更新
                        Intent updateIntent = new Intent(getApplicationContext(), CheckUpdateActivity.class);
                        startActivity(updateIntent);
                        break;

                    case 4://使用说明

                        break;

                    case 5:
                        Intent contactIntent = new Intent(getApplicationContext(), ContactOurActivity.class);
                        startActivity(contactIntent);
                        break;
                }
            }
        });
    }

    private void initView() {
        initWidget();
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserNameEditor = findViewById(R.id.tv_user_name_edit);
        tvJob = findViewById(R.id.tv_user_job);
        tvJobEditor = findViewById(R.id.tv_user_job_edit);
        tvMobile = findViewById(R.id.tv_user_mobile);
        tvMobileEditor = findViewById(R.id.tv_user_mobile_edit);
        tvWx = findViewById(R.id.tv_user_wx);
        tvWxEditor = findViewById(R.id.tv_user_wx_edit);
        listView = findViewById(R.id.lv_user_list);

        tvTitle.setText("个人中心");
        imgIcon.setImageResource(R.mipmap.icon_back);
        imgIcon.setVisibility(View.VISIBLE);
        imgIcon.setOnClickListener(this);

        tvWxEditor.setOnClickListener(this);
        tvMobileEditor.setOnClickListener(this);
        tvUserNameEditor.setOnClickListener(this);
        tvJobEditor.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDbHelper.close();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            返回按钮
            case R.id.img_icon_toolbar:
                UserCenterActivity.this.finish();
                break;
//                绑定微信用户
            case R.id.tv_user_wx_edit:
                weChatHelper = new WeChatHelper(this);
                weChatHelper.regToWx();
                weChatHelper.sendLoginRequest();
                break;

//                绑定手机号码
            case R.id.tv_user_mobile_edit:
                /**
                 * 要弹出一个对话框，对话框里：上面一个手机号码的输入框，下面手机验证码输入框和获取验证码的按钮
                 * 所以需要一个垂直的布局的LL作为根layout，包括一个手机号码的输入框
                 * 再由一个横向的LL，包含住手机验证码和获取验证码
                 */
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("绑定手机号码");
//                设置垂直的根布局
                LinearLayout rootLl = new LinearLayout(this);
                rootLl.setGravity(Gravity.CENTER);
                rootLl.setOrientation(LinearLayout.VERTICAL);

//                创建手机号码输入框
                final EditText etMobile = new EditText(this);
                int width = ScreenSizeUtil.getScreenWidth(this)-(ScreenSizeUtil.getScreenWidth(this)/7);
                etMobile.setWidth(width);
                etMobile.setHint("请输入手机号码");
                LinearLayout.LayoutParams moblieLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                moblieLp.setMargins(0,50,0,25);
                etMobile.setLayoutParams(moblieLp);

//                将手机号码输入框添加到rootll中
                rootLl.addView(etMobile);
//                创建一个横向的LL
                LinearLayout hoLl = new LinearLayout(this);
                hoLl.setOrientation(LinearLayout.HORIZONTAL);
                hoLl.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams hoPl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                hoPl.setMargins(0, 25, 0, 75);
                hoLl.setLayoutParams(hoPl);
                int perWidth = (width) / 10;
//                创建一个验证码的输入框
                final EditText etCode = new EditText(this);
                etCode.setHint("请输入验证码");
                etCode.setWidth(perWidth * 6);
                hoLl.addView(etCode);

//                创建一个获取验证码的按钮
                final Button btnGetCode = new Button(this);
                btnGetCode.setText("获取验证码");
                btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
                btnGetCode.setWidth(perWidth*4);
                btnGetCode.setTextColor(Color.WHITE);
                hoLl.addView(btnGetCode);

//                将横向的LL添加进竖向的LL
                rootLl.addView(hoLl);

                //                初始化一个加载对话框
                final MKLoader mkLoader = new MKLoader(this);
//                mkLoader.setVisibility(View.INVISIBLE);
                final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.alignWithParent = true;
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                mkLoader.setLayoutParams(params);
                mkLoader.setVisibility(View.INVISIBLE);
                mkLoader.setBackgroundColor(Color.GRAY);
                RelativeLayout rl = new RelativeLayout(this);
                rl.addView(rootLl);
                rl.addView(mkLoader);

                //                将布局添加进对话框
                builder.setView(rl);
                /**
                 * 获取手机验证码
                 */
                btnGetCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final  String phone = etMobile.getText().toString().trim();
                        mkLoader.setVisibility(View.VISIBLE);
                        if (phone.length() != 11) {
                            mkLoader.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.mobile_param,phone);
                                    List<OkHttpUtils.Param> paramList = new ArrayList<>();
                                    paramList.add(param);
                                    StringBuffer url = new StringBuffer();
                                    url.append(NetConstant.baseUrl);
                                    url.append(NetConstant.getMobileCodeUrl);
                                    OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
                                        @Override
                                        public void onSuccess(String response) {
                                            try {
                                                /**
                                                 *
                                                 {"status":"success","code":200,"msg":"验证码下发成功"}
                                                 */
                                                JSONObject jsonObject = new JSONObject(response);
                                                int code = jsonObject.optInt("code");
                                                LogUtils.show("onSuccess: 获取验证码成功："+response);
                                                final String msg = jsonObject.optString("msg");
                                                countDown = 60;
                                                if (code == 200) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mkLoader.setVisibility(View.GONE);
                                                            btnGetCode.setClickable(false);
                                                            btnGetCode.setBackgroundColor(Color.GRAY);
                                                            final Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (countDown > 0) {
                                                                        btnGetCode.setText("重新获取(" + countDown + ")");
                                                                        handler.postDelayed(this, 1000);
                                                                    } else {
                                                                        btnGetCode.setText("重新获取");
                                                                        btnGetCode.setClickable(true);
                                                                        btnGetCode.setBackgroundResource(R.drawable.btn_nomal);
                                                                    }
                                                                    countDown--;
                                                                }
                                                            }, 1000);
                                                        }
                                                    });
                                                } else {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mkLoader.setVisibility(View.GONE);
                                                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mkLoader.setVisibility(View.GONE);
                                                        Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mkLoader.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    },paramList);
                                }
                            }).start();

                        }
                    }
                });
//                点击确定按钮
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String mobile = etMobile.getText().toString().trim();
                        final String code = etCode.getText().toString().trim();
                        if (mobile.length() != 11) {
                            Toast.makeText(getApplicationContext(), "手机号码长度不对", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (code.length() < 4) {
                            Toast.makeText(getApplicationContext(), "验证码长度不对", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mkLoader.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpUtils.Param mobileParam = new OkHttpUtils.Param(NetConstant.mobile_param, mobile);
                                OkHttpUtils.Param codeParam = new OkHttpUtils.Param(NetConstant.register_code, code);
                                OkHttpUtils.Param tokeParam = new OkHttpUtils.Param(NetConstant.change_pwd_token, user.getToken());
                                List<OkHttpUtils.Param> paramList = new ArrayList<>();
                                paramList.add(mobileParam);
                                paramList.add(codeParam);
                                paramList.add(tokeParam);
                                String url = NetConstant.baseUrl + NetConstant.bind_account_url;
                                LogUtils.show("查看绑定账号前返回的信息："+paramList);
                                OkHttpUtils.post(url,new OkHttpUtils.ResultCallback<String>() {
                                    @Override
                                    public void onSuccess(String response) {
                                        LogUtils.show("查看绑定返回的信息："+response);
                                        try {
                                            JSONObject object = new JSONObject(response);
                                            int code = object.optInt("code");
                                            final String msg = object.optString("msg");
                                            if (code == 200) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), "账号绑定成功", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"网络请求失败",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                },paramList);
                            }
                        }).start();

                    }
                });
                builder.setNegativeButton("取消", null);
                builder.setCancelable(false);
                builder.show();



                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void wxLoginCallBack(WxResultMessage message) {
//        if (message.getFlag() == 1) {
        String unionId = message.getUionId();
        final String data = message.getData();
        OkHttpUtils.Param param = new OkHttpUtils.Param(NetConstant.login_data, data);
        OkHttpUtils.Param tokenParam = new OkHttpUtils.Param(NetConstant.change_pwd_token, user.getToken());
        List<OkHttpUtils.Param> paramList = new ArrayList<>();
        paramList.add(param);
        paramList.add(tokenParam);
        StringBuffer url = new StringBuffer();
        url.append(NetConstant.baseUrl);
        url.append(NetConstant.bind_account_url);
        LogUtils.show("查看请求绑定微信的信息："+paramList.toString());
        OkHttpUtils.post(url.toString(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    /**{"status":"success","code":200,"msg":"绑定账号成功"}*/
                    JSONObject object = new JSONObject(response);
                    final int code = object.optInt("code");
                    final String msg = object.optString("msg");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 200) {
                                Toast.makeText(getApplicationContext(), "绑定成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),"绑定失败："+msg,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LogUtils.show("查看绑定微信返回的信息："+response);
            }

            @Override
            public void onFailure(Exception e) {
//                Log.e(TAG, "onFailure: 网络请求失败："+e.getMessage() );
            }
        },paramList);
//            }
    }

    class UserListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int i) {
            return itemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            LayoutInflater inflater = LayoutInflater.from(UserCenterActivity.this);
            if (view == null) {
                view = inflater.inflate(R.layout.item_list_for_user_center, null);
                holder = new ViewHolder();
                holder.imageView = view.findViewById(R.id.img_item_user);
                holder.textView = view.findViewById(R.id.tv_item_user);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.imageView.setImageResource(itemList.get(i).getImgResouce());
            holder.textView.setText(itemList.get(i).getItemName());
            return view;
        }
    }

    class ViewHolder {
        ImageView imageView;
        TextView textView;
    }



    class ItemBean {
        int imgResouce;
        String itemName;

        public ItemBean(int imgResouce, String itemName) {
            this.imgResouce = imgResouce;
            this.itemName = itemName;
        }

        public int getImgResouce() {
            return imgResouce;
        }

        public void setImgResouce(int imgResouce) {
            this.imgResouce = imgResouce;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public String toString() {
            return "ItemBean{" +
                    "imgResouce=" + imgResouce +
                    ", itemName='" + itemName + '\'' +
                    '}';
        }
    }


}
