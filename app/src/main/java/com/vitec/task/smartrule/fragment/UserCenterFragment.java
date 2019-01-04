package com.vitec.task.smartrule.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.UserDatumActivity;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserCenterFragment extends Fragment implements View.OnClickListener{

    private View view;
    private CircleImageView circleImageView;//圆形头像
    private TextView tvUserName;
    private TextView tvPhone;
    private TextView tvWxName;
    private RelativeLayout rlEditUserData;//编辑个人资料按钮
    private RelativeLayout rlChangePsw;//修改密码
    private RelativeLayout rlCheckUpdate;//检查更新
    private RelativeLayout rlContactUs;//联系我们
    private RelativeLayout rlHelper;//使用说明

    private User user;
    UserDbHelper userDbHelper;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine,null);
        initView();
        iniData();
        return view;

    }

    private void initView() {
        circleImageView = view.findViewById(R.id.circle_img_view);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvWxName = view.findViewById(R.id.tv_user_wx);
        rlEditUserData = view.findViewById(R.id.rl_edit_user_data);
        rlChangePsw = view.findViewById(R.id.rl_change_psw);
        rlCheckUpdate = view.findViewById(R.id.rl_check_update);
        rlContactUs = view.findViewById(R.id.rl_contact_us);
        rlHelper = view.findViewById(R.id.rl_helper);

        rlEditUserData.setOnClickListener(this);
        rlCheckUpdate.setOnClickListener(this);
        rlChangePsw.setOnClickListener(this);
        rlContactUs.setOnClickListener(this);
        rlHelper.setOnClickListener(this);

    }

    private void iniData() {
        /**
         * 初始化上面的用户信息
         */
        User tUser = OperateDbUtil.getUser(getContext());
        userDbHelper = new UserDbHelper(getContext());
        userList = new ArrayList<>();
        user = new User();

        String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID() + " or " + DataBaseParams.user_wid + " = " + tUser.getWid();
        userList = userDbHelper.queryUserDataFromSqlite(where);

        if (userList.size() > 0) {
            user = userList.get(0);
        } else {
            user = tUser;
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
            StringBuffer sb = new StringBuffer();
            sb.append(phone.substring(0, 3));
            for (int i=0;i<(phone.length()-5);i++) {
                sb.append("*");
            }
            sb.append(phone.substring((phone.length() - 2), phone.length()));
            tvPhone.setText(sb.toString());
        } else {
            tvPhone.setText("--");
        }



//        初始化微信号
        if (user.getWxData() == null || user.getWxData().equalsIgnoreCase("null") || user.getWxData().equalsIgnoreCase("")) {
            tvWxName.setText("未绑定");
        } else {
            try {
                JSONObject object = new JSONObject(user.getWxData());
                String nickName = object.optString("nickname");
                tvWxName.setText(nickName);
            } catch (JSONException e) {
                tvWxName.setText("--");
                e.printStackTrace();

            }
        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 修改个人资料
             */
            case R.id.rl_edit_user_data:
                startActivity(new Intent(getActivity(), UserDatumActivity.class));

                break;

            /**
             * 修改密码
             */
            case R.id.rl_change_psw:

                break;

            /**
             * 检查更新
             */
            case R.id.rl_check_update:
                break;

            /**
             * 联系我们
             */
            case R.id.rl_contact_us:
                break;

            /**
             * 使用说明
             */
            case R.id.rl_helper:

                break;
        }
    }
}
