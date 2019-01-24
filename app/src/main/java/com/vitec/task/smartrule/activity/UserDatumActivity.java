package com.vitec.task.smartrule.activity;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.decoding.Intents;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.UserWx;
import com.vitec.task.smartrule.bean.event.DownFileMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.helper.WeChatHelper;
import com.vitec.task.smartrule.service.intentservice.UploadPicIntentService;
import com.vitec.task.smartrule.utils.Base64Utils;
import com.vitec.task.smartrule.utils.LogUtils;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.utils.SharePreferenceUtils;
import com.vitec.task.smartrule.view.BottomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 个人资料页面，包括：
 * 1.头像
 * 2.姓名
 * 3.手机号码
 * 4.微信
 * 5.切换账号
 */
public class UserDatumActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout rlImgHead;
    private RelativeLayout rlUserName;
    private RelativeLayout rlPhone;
    private RelativeLayout rlUserWx;
    private TextView tvUserName;
    private TextView tvPhone;
    private TextView tvUserWx;
    private TextView tvExit;//切换账号
    private CircleImageView headCircleImg;
    private User user;
    private boolean hasUpdate = false;//用来判断个人信息是否有更新

    private final int request_flag_head_img = 11;
    private final int request_flag_user_name = 12;
    private final int request_flag_user_phone = 13;
    private final int request_flag_user_wx= 41;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvitiy_user_msg);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initData() {
//        获取用户信息
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            User tUser = OperateDbUtil.getUser(getApplicationContext());
            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
            List<User> userList = new ArrayList<>();
            user = new User();
            String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID() ;
            userList = userDbHelper.queryUserDataFromSqlite(where);

            if (userList.size() > 0) {
                user = userList.get(0);
            } else {
                user = tUser;
            }
            userDbHelper.close();
        }

//        初始化用户名数据
        if ( user.getUserID() > 0) {
            tvUserName.setText(user.getUserName());
        } else {
            tvUserName.setText("未绑定");
        }

//        初始化手机号码
        if (user.getMobile()!=null && !user.getMobile().equalsIgnoreCase("0") && user.getMobile().length() > 6) {
            String phone = user.getMobile();
            LogUtils.show("初始化手机号码模块，打印手机号码："+phone);
            StringBuffer sb = new StringBuffer();
            sb.append(phone.substring(0, 3));
            for (int i=0;i<(phone.length()-5);i++) {
                sb.append("*");
            }
            sb.append(phone.substring((phone.length() - 2), phone.length()));
            tvPhone.setText(sb.toString());
        } else {
            tvPhone.setText("未绑定");
            tvPhone.setTextColor(Color.rgb(53,129,251));
        }



///        初始化微信号
        if (user.getUser_type().equals("1") ) {
            tvUserWx.setText("未绑定");
        } else {
            String wxWhere = " where " + DataBaseParams.user_user_id + " = " + user.getUserID();
            UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
            List<UserWx> userWxes = userDbHelper.queryUserWxDataFromSqlite(wxWhere);
            if (userWxes.size() > 0) {
                tvUserWx.setText(userWxes.get(0).getNickName());
            }
            userDbHelper.close();
        }


//        初始化头像
        if (user.getLocalImgUrl() !=  null && user.getLocalImgUrl().length() > 4) {
            Bitmap bitmap = BitmapFactory.decodeFile(user.getLocalImgUrl());
            headCircleImg.setImageBitmap(bitmap);
        }

    }

    private void initView() {
        initWidget();
        setTvTitle("个人资料");

        rlImgHead = findViewById(R.id.rl_img_head);
        rlPhone = findViewById(R.id.rl_phone);
        rlUserName = findViewById(R.id.rl_user_name);
        rlUserWx = findViewById(R.id.rl_wx);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserWx = findViewById(R.id.tv_user_wx);
        tvPhone = findViewById(R.id.tv_phone);
        headCircleImg = findViewById(R.id.cir_img_head);
        tvExit = findViewById(R.id.tv_exit);

        rlUserName.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        rlUserWx.setOnClickListener(this);
        rlImgHead.setOnClickListener(this);
        tvExit.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
//        Bundle bundle = new Bundle();
        if (hasUpdate) {
//            bundle.putInt("flag", 1);
            intent.putExtra("flag", 1);
        } else {
//            bundle.putInt("flag", 0);
            intent.putExtra("flag", 0);
        }
        setResult(RESULT_OK, intent);
        //下面这行代码一定要放在下面，不然resultcode会返回0
        super.onBackPressed();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 退出登录
             */
            case R.id.tv_exit:
                //清除用户信息
                Map<String, String> map = new HashMap<>();
                map.put(SharePreferenceUtils.user_id, "0");
                map.put(SharePreferenceUtils.user_mobile, "");
                map.put(SharePreferenceUtils.user_real_name, "");
                map.put(SharePreferenceUtils.user_token, "");
                map.put(SharePreferenceUtils.user_type, String.valueOf(""));
                map.put(SharePreferenceUtils.user_pwd, "");
                SharePreferenceUtils.savaData(this,map,SharePreferenceUtils.user_table);

                //回到登录页面，并且清除上面的栈顶
//                Intent loginIntent = new Intent(this, LoginActivity.class);
//                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(loginIntent);

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("closeType", 1);
                startActivity(intent);
                this.finish();
                break;
            /**
             * 上传头像
             */
            case R.id.rl_img_head:
                BottomDialog bottomDialog = new BottomDialog(UserDatumActivity.this, R.style.BottomDialog);
                bottomDialog.setActivity(this);
                bottomDialog.show();
                break;

            /**
             * 修改姓名
             */
            case R.id.rl_user_name:
                Intent userIntent = new Intent(UserDatumActivity.this, UpdateUserNameActivity.class);
                userIntent.putExtra("user", user);
                startActivityForResult(userIntent, request_flag_user_name);

                break;

            /**
             * 更换手机号码
             */
            case R.id.rl_phone:
                Intent phoneIntent = new Intent(this, ChangeMobileActivity.class);
                phoneIntent.putExtra("user", user);
                startActivityForResult(phoneIntent,request_flag_user_phone);

                break;

            /**
             * 绑定微信用户
             */
            case R.id.rl_wx:
                if (user.getUser_type().equals("1")) {
                    WeChatHelper weChatHelper = new WeChatHelper(this);
                    weChatHelper.regToWx();
                    weChatHelper.sendLoginRequest();
                }

                break;
        }
    }
    /**
     * 接收头像更新的信息
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void headImgUpdateCallBack(DownFileMsgEvent event) {
        if (event.isSuccess() && event.getPath().length() > 5) {
            user.setLocalImgUrl(event.getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(event.getPath());
            headCircleImg.setImageBitmap(bitmap);
        }
    }

    /**
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                /**上传头像的回调**/
                case PictureConfig.CHOOSE_REQUEST:
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    if (selectList.size() > 0) {
                        for (final LocalMedia media : selectList) {
                            LogUtils.show("onActivityResult---打印查看返回的原图片路径：" + media.getPath() + ",长宽：" + media.getWidth() + "," + media.getHeight());
                            LogUtils.show("onActivityResult---打印查看返回裁剪后的图片路径：" + media.getCutPath());
                            LogUtils.show("onActivityResult---打印查看压缩后的图片路径：" + media.getCompressPath());
                            Bitmap bitmap = BitmapFactory.decodeFile(media.getCompressPath());
                            headCircleImg.setImageBitmap(bitmap);
                            Intent uploadIntent = new Intent(UserDatumActivity.this, UploadPicIntentService.class);
                            uploadIntent.putExtra(UploadPicIntentService.UPLOAD_FLAG, UploadPicIntentService.FLAG_UPLOAD_HEAD_IMG);
                            uploadIntent.putExtra(UploadPicIntentService.VALUE_IMG_PATH, media.getCompressPath());
                            startService(uploadIntent);
                        }
                        hasUpdate = true;
                    }
                    break;

                    /**修改用户名的回调**/
                case request_flag_user_name:
                    int flag = data.getIntExtra("flag", 0);
                    if (flag == 1) {
                        String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() ;
                        UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
                        List<User> userList = userDbHelper.queryUserDataFromSqlite(where);
                        if (userList.size() > 0) {
                            user = userList.get(0);
                        }
                        tvUserName.setText(user.getUserName());
                        hasUpdate = true;
                        userDbHelper.close();
                    }
                    break;

                    /*****修改手机号码的回调****/
                case request_flag_user_phone:
                    int flag_phone = data.getIntExtra("flag", 0);
                    if (flag_phone == 1) {
                        String where = " where " + DataBaseParams.user_user_id + " = " + user.getUserID() ;
                        UserDbHelper userDbHelper = new UserDbHelper(getApplicationContext());
                        List<User> userList = userDbHelper.queryUserDataFromSqlite(where);
                        if (userList.size() > 0) {
                            user = userList.get(0);
                        }
                        //        初始化手机号码
                        if (user.getMobile()!=null && !user.getMobile().equalsIgnoreCase("0") && user.getMobile().length() > 6) {
                            String phone = user.getMobile();
                            LogUtils.show("初始化手机号码模块，打印手机号码："+phone);
                            StringBuffer sb = new StringBuffer();
                            sb.append(phone.substring(0, 3));
                            for (int i=0;i<(phone.length()-5);i++) {
                                sb.append("*");
                            }
                            sb.append(phone.substring((phone.length() - 2), phone.length()));
                            tvPhone.setText(sb.toString());
                        } else {
                            tvPhone.setText("未绑定");
                            tvPhone.setTextColor(Color.rgb(53,129,251));
                        }
                        hasUpdate = true;
                        userDbHelper.close();
                    }
                    break;
            }
        }
    }
}
