package com.vitec.task.smartrule.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.vitec.task.smartrule.bean.WxResultMessage;
import com.vitec.task.smartrule.db.UserDbHelper;
import com.vitec.task.smartrule.utils.OkHttpUtils;
import com.vitec.task.smartrule.wxapi.bean.ResultInfo;
import com.vitec.task.smartrule.wxapi.bean.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * WXEntryActivity 是微信固定的Activiy、 不要改名字、并且放到你对应的项目报名下面、
 * 例如： ....(package报名).wxapi.WXEntryActivity
 * 不然无法回调、切记...
 * Wx  回调接口 IWXAPIEventHandler
 * <p/>
 * 关于WXEntryActivity layout。 我们没给页面、而是把Activity  主题 android:theme="@android:style/Theme.Translucent" 透明、
 * <p/>
 * User: MoMo - Nen
 * Date: 2015-10-24
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private final String TAG = this.getClass().getSimpleName();
    //    public static final String APP_ID = "wx6855a20462153a04";
    public static final String APP_ID = "wx4543e42598b7bf6d";
    //    public static final String APP_SECRET = "da262c74de1c6a752a4d861f9cd74a30";
    public static final String APP_SECRET = "8d54430389db6050f6ca7af80046115f";

    private IWXAPI mApi;

    private ResultInfo resultInfo;
    private UserInfo userInfo;
    private UserDbHelper userDbHelper;



    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApi = WXAPIFactory.createWXAPI(this, APP_ID, false);
        mApi.handleIntent(this.getIntent(), this);
        userDbHelper = new UserDbHelper(getApplicationContext());
    }

    //微信发送的请求将回调到onReq方法
    @Override
    public void onReq(BaseReq baseReq) {
    }


    //发送到微信请求的响应结果
    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //发送成功
                showLog("发送成功");
                SendAuth.Resp sendResp = (SendAuth.Resp) resp;
                if (sendResp != null) {
                    String code = sendResp.code;
                    getAccess_token(code);
                    showLog("正在获取用户信息");
//                    startActivity(new Intent(getApplicationContext(), UnuseMainActivity.class));
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                showLog("发送被取消");
                //发送取消
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                showLog("发送被拒绝");
                //发送被拒绝
                break;
            default:
                showLog("发送返回:" + resp.errCode + ",errStr:" + resp.openId + "," + resp.transaction);
                //发送返回
                break;
        }
        finish();

    }

    private void showLog(String mgs) {
        Log.e(TAG, "查看发送状态。showLog: " + mgs);
    }


    /**
     * 获取openid accessToken值用于后期操作
     *
     * @param code 请求码
     */
    private void getAccess_token(final String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                        + APP_ID
                        + "&secret="
                        + APP_SECRET
                        + "&code="
                        + code
                        + "&grant_type=authorization_code";
                try {
                    /**

                     {
                     "access_token":"ACCESS_TOKEN",  //access_token接口调用凭证
                     "expires_in":7200,expires_in access_token接口调用凭证超时时间，单位（秒）
                     "refresh_token":"REFRESH_TOKEN",refresh_token 用户刷新access_token
                     "openid":"OPENID",openid 授权用户唯一标识
                     "scope":"SCOPE",scope 用户授权的作用域，使用逗号（,）分隔
                     "unionid":"o6_bmasdasdsad6_2sgVt7hMZOPfL"unionid 当且仅当该移动应用已获得该用户的userinfo授权时，才会出现该字段
                     }
                     错误返回样例：
                     {"errcode":40029,"errmsg":"invalid code"}
                     */
                    OkHttpUtils.ResultCallback<String> resultCallback = new OkHttpUtils.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String response) {
                            String access = null;
                            String openId = null;
                            try {
                                /**
                                 * 1.获取微信返回的数据信息
                                 * 2.根据unionid查找数据库有没有相同的
                                 *      有则自动跳转 登陆成功
                                 *      无则跳转到注册页面 让用户填写注册信息
                                 */
                                Log.e(TAG, "onSuccess: 查看收到的返回信息："+response );
                                JSONObject jsonObject = new JSONObject(response);
                                access = jsonObject.getString("access_token");
                                openId = jsonObject.getString("openid");
                                String unionId = jsonObject.optString("unionid");
                                resultInfo = new ResultInfo(access, openId);
                                resultInfo.setUnionId(unionId);
                                resultInfo.setRefreshToken(jsonObject.optString("refresh_token"));

                                getUserMesg(access,openId);

                                Log.e(TAG, "onSuccess: 查看收到的token:" +resultInfo.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getApplicationContext(), "获取token失败", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onFailure: 获取Token失败" );
                            finish();
                        }
                    };
//                    JSONObject jsonObject = JsonUtils.initSSLWithHttpClinet(path);// 请求https连接并得到json结果
//                    if (null != jsonObject) {
//                        String openid = jsonObject.getString("openid").toString().trim();
//                        String access_token = jsonObject.getString("access_token").toString().trim();
//                        showLog("查看openid:"+openid+",查看accesstoken:"+access_token);
//                        getUserMesg(access_token, openid);
//                    }
                    OkHttpUtils.get(path, resultCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }



    /**
     * 获取微信的个人信息
     *
     * @param access_token
     * @param openid
     */
    private void getUserMesg(final String access_token, final String openid) {
        String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;

        OkHttpUtils.ResultCallback<String> resultCallback = new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, "onSuccess: 收到的微信的个人信息长度："+response.length()+",内容："+response );

                String nickName = null;
                String sex;
                String city;
                String province;
                String country;
                String headImgUrl;
                String openid;
                String unionid;
                /**
                 * 将所有数据转发给服务器，写到data里面
                 */
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    openid = jsonObject.getString("openid");
                    nickName = jsonObject.getString("nickname");
                    sex = jsonObject.getString("sex");
                    city = jsonObject.getString("city");
                    province = jsonObject.getString("province");
                    country = jsonObject.getString("country");
                    headImgUrl = jsonObject.getString("headimgurl");
//                    oKP_j1Dj8KQQRNVCsEJykp4P8Eog
                    unionid = jsonObject.getString("unionid");
                    userInfo = new UserInfo(nickName, sex, city, province, country, headImgUrl, openid, unionid);
                    WxResultMessage message = new WxResultMessage();
//                    message.setFlag(1);
                    message.setUionId(unionid);
                    message.setData(response);
                    EventBus.getDefault().post(message);
//                    requestLoginFromServer(unionid,response);

                    Log.e(TAG, "onSuccess: 查看收到的个人信息，openid:" + userInfo.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "获取信息失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
//        try {
//            JSONObject jsonObject = JsonUtils.initSSLWithHttpClinet(path);// 请求https连接并得到json结果
//            if (null != jsonObject) {
//                String nickname = jsonObject.getString("nickname");
//                int sex = Integer.parseInt(jsonObject.get("sex").toString());
//                String headimgurl = jsonObject.getString("headimgurl");
//
//                Log.e(TAG, "getUserMesg 拿到了用户Wx基本信息.. nickname:" + nickname);
//                showLog("查看用户的信息:"+nickname+","+sex+","+headimgurl);
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        OkHttpUtils.get(path, resultCallback);
        return;
    }


}
