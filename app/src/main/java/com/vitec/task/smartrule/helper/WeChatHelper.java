package com.vitec.task.smartrule.helper;

import android.content.Context;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.vitec.task.smartrule.wxapi.WeiXinUtil;

public class WeChatHelper {
//    App_id为应用官网申请到的合法appid
    private static final String APP_ID = "wx4543e42598b7bf6d";
    private static final String APP_SECRET = "8d54430389db6050f6ca7af80046115f";
//    IWXAPID是第三方APP和微信通信的openapid接口
    private IWXAPI iwxapi;//

    private Context mContext;

    public WeChatHelper(Context mContext) {
        this.mContext = mContext;
    }


    public void regToWx() {
//        通过WXAPIFactory工厂获取IWXAPI
        iwxapi = WXAPIFactory.createWXAPI(mContext, APP_ID, true);
//        将应用的appid注册到微信
        iwxapi.registerApp(APP_ID);
    }


    public void sendLoginRequest() {
        if (!iwxapi.isWXAppInstalled()) {
            Toast.makeText(mContext, "未安装微信", Toast.LENGTH_SHORT).show();
        } else {
            SendAuth.Req req = new SendAuth.Req();
//            用户授权作用域
            req.scope = "snsapi_userinfo";
            /* state：
            用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击），
            建议第三方带上该参数，可设置为简单的随机数加session进行校验
             */
            req.state = "wechat_sdk_demo_test_smart_rule";
            iwxapi.sendReq(req);
        }
    }



    public  void shareFileToWx(String filePath) {
        WXFileObject fileObject = new WXFileObject();
        fileObject.fileData = WeiXinUtil.inputStreamToByte(filePath);
        fileObject.filePath = filePath;

//        使用媒体消息分享
        WXMediaMessage mediaMessage = new WXMediaMessage(fileObject);
        mediaMessage.title = "测试一下文件分享功能.xls";
//        发送请求
        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        创建唯一标识
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = mediaMessage;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        iwxapi.sendReq(req);

    }
}


