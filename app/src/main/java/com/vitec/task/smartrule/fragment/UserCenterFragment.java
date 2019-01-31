package com.vitec.task.smartrule.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitec.task.smartrule.R;
import com.vitec.task.smartrule.activity.ChangePasswordActivity;
import com.vitec.task.smartrule.activity.CheckUpdateActivity;
import com.vitec.task.smartrule.activity.ContactOurActivity;
import com.vitec.task.smartrule.activity.CreateMeasureTeamActivity;
import com.vitec.task.smartrule.activity.MeasureTeamManagerActivity;
import com.vitec.task.smartrule.activity.UseInstructionActivity;
import com.vitec.task.smartrule.activity.UserDatumActivity;
import com.vitec.task.smartrule.bean.RulerCheckProject;
import com.vitec.task.smartrule.bean.User;
import com.vitec.task.smartrule.bean.UserWx;
import com.vitec.task.smartrule.bean.event.DownFileMsgEvent;
import com.vitec.task.smartrule.bean.event.QueryProjectGroupMsgEvent;
import com.vitec.task.smartrule.db.DataBaseParams;
import com.vitec.task.smartrule.db.OperateDbUtil;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.net.FileOkHttpUtils;
import com.vitec.task.smartrule.net.NetConstant;
import com.vitec.task.smartrule.service.intentservice.ProjectManageRequestIntentService;
import com.vitec.task.smartrule.utils.DateFormatUtil;
import com.vitec.task.smartrule.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Date;
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
//    private RelativeLayout rlHelper;//使用说明
    private RelativeLayout rlMeasureTeam;//测量组

    private User user;
    UserDbHelper userDbHelper;
    private List<User> userList;
    private boolean isCurrentPage = false;//是否还停留在当前页面
    private boolean isFirst = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine,null);
        initView();
        iniData();
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.show("UserCenterFragment-----onResume:设置为true;查看当前是否隐藏”"+this.isHidden());
        if (!isFirst && !this.isHidden()) {
            isCurrentPage = true;
            isFirst = false;
        } else {
            isFirst = false;
        }

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.show("UserCenterFragment-----onDestroy");
//        EventBus.getDefault().unregister(this);
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
//        rlHelper = view.findViewById(R.id.rl_helper);
        rlMeasureTeam = view.findViewById(R.id.rl_messege_team);

        rlEditUserData.setOnClickListener(this);
        rlCheckUpdate.setOnClickListener(this);
        rlChangePsw.setOnClickListener(this);
        rlContactUs.setOnClickListener(this);
//        rlHelper.setOnClickListener(this);
        rlMeasureTeam.setOnClickListener(this);

    }

    private void iniData() {
        /**
         * 初始化上面的用户信息
         */
        User tUser = OperateDbUtil.getUser(getContext());
        final String user_flag = tUser.getUser_type();
        userDbHelper = new UserDbHelper(getContext());
        userList = new ArrayList<>();
        user = new User();

        String where = " where " + DataBaseParams.user_user_id + " = " + tUser.getUserID() ;
        userList = userDbHelper.queryUserDataFromSqlite(where);

        if (userList.size() > 0) {
            user = userList.get(0);
        } else {
            user = tUser;
        }
        user.setUser_type(user_flag);
        LogUtils.show("查看最终的USER信息："+user.toString());
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
            tvPhone.setText("--");
        }



//        初始化微信号
        if (user_flag.equals("1") ) {
            tvWxName.setText("未绑定");
        } else {
            String wxWhere = " where " + DataBaseParams.user_user_id + " = " + user.getUserID();
            List<UserWx> userWxes = userDbHelper.queryUserWxDataFromSqlite(wxWhere);
            if (userWxes.size() > 0) {
                tvWxName.setText(userWxes.get(0).getNickName());
            }
        }

        LogUtils.show("查看本地地址：" + user.getLocalImgUrl());
//        初始化头像
        if (user.getLocalImgUrl() !=  null && !user.getLocalImgUrl().equals("null")&&user.getLocalImgUrl().length() > 5 ){
            Bitmap bitmap = BitmapFactory.decodeFile(user.getLocalImgUrl());
            circleImageView.setImageBitmap(bitmap);
            LogUtils.show("个人中心----头像不为空");
            EventBus.getDefault().unregister(this);
        } else if (user.getImgUrl() != null && !user.getImgUrl().equals("null")&& user.getImgUrl().length() > 8) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LogUtils.show("个人中心----头像为空");
//                    String where = DataBaseParams.user_user_id + "=?";
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + DateFormatUtil.transForMilliSecond(new Date()) + ".jpg";
                    FileOkHttpUtils.downloadFile(user.getImgUrl(),path,user.getUserID(),getActivity());
                }
            }).start();
        }

    }

    /**
     * 接收头像更新的信息
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void headImgUpdateCallBack(DownFileMsgEvent event) {
        LogUtils.show("个人中心----收到头像加载完毕回调");
        if (event.isSuccess() && event.getPath().length() > 5 && event.getType()==1) {
            user.setLocalImgUrl(event.getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(event.getPath());
            circleImageView.setImageBitmap(bitmap);
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            LogUtils.show("UserCenterFragment=--=====被隐藏了");
            isCurrentPage = false;
        } else {
            isCurrentPage = true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 修改个人资料
             */
            case R.id.rl_edit_user_data:
                Intent userIntent = new Intent(getActivity(), UserDatumActivity.class);
                userIntent.putExtra("user", user);
                startActivityForResult(userIntent,0);
                isCurrentPage = false;
                break;

            /**
             * 修改密码
             */
            case R.id.rl_change_psw:
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
                isCurrentPage = false;
                break;

            /**
             * 检查更新
             */
            case R.id.rl_check_update:
                startActivity(new Intent(getActivity(), CheckUpdateActivity.class));
                isCurrentPage = false;
                break;

            /**
             * 联系我们
             */
            case R.id.rl_contact_us:
                startActivity(new Intent(getActivity(), ContactOurActivity.class));
                isCurrentPage = false;

                break;

            /**
             * 使用说明
             */
//            case R.id.rl_helper:
//                startActivity(new Intent(getActivity(), UseInstructionActivity.class));
//                isCurrentPage = false;
//                break;

            /**
             * 测量组
             */
            case R.id.rl_messege_team:
                if (!EventBus.getDefault().isRegistered(this)) {
                    EventBus.getDefault().register(this);
                }
                /*****请求查询测量组*****/
                Intent queryIntent = new Intent(getActivity(), ProjectManageRequestIntentService.class);
                Bundle bundle = new Bundle();
                bundle.putInt(DataBaseParams.user_user_id, user.getUserID());
                bundle.putInt(NetConstant.page_size, 50);
                bundle.putInt(NetConstant.current_Page, 1);
                queryIntent.putExtra(ProjectManageRequestIntentService.REQUEST_FLAG, ProjectManageRequestIntentService.flag_group_get_project_list);
                queryIntent.putExtra(ProjectManageRequestIntentService.key_get_value, bundle);
                getActivity().startService(queryIntent);
//                queryProjectGroupData();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void projectGroupEventCallBack(QueryProjectGroupMsgEvent event) {
        LogUtils.show("projectGroupEventCallBack---接收到项目更新的返回");
        if (isCurrentPage) {
//            if (event.isSuccess()) {
                int size = queryProjectGroupData();
                if (size == 0) {
                    startActivity(new Intent(getActivity(),CreateMeasureTeamActivity.class));
                }
//            }

        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 查找数据库中的项目组
     */
    private int queryProjectGroupData() {
//        String where = " where " + DataBaseParams.user_user_id + '=' + user.getUserID()+" ORDER BY id DESC";
        List<RulerCheckProject> projectList = OperateDbUtil.queryAllProjectOrderMember(getActivity(), user.getUserID());
        if (projectList.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(DataBaseParams.check_project_name, (Serializable) projectList);
            Intent intent = new Intent(getActivity(), MeasureTeamManagerActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            isCurrentPage = false;
        }
        return projectList.size();
    }


    /**
     * 接收修改个人资料界面返回的值；
     * 判断个人信息是否有更新
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            int flag = data.getIntExtra("flag", 0);
            if (flag == 1) {
                iniData();
            }
        }
    }
}
