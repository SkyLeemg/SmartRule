package com.vitec.task.smartrule.aliyun;

import android.print.PrinterId;
import android.util.Log;

import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttRrpcRegisterRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectUnscribeListener;
import com.aliyun.alink.linksdk.tools.AError;

import java.util.List;

public class TopicHelper {

    private MqttPublishRequest request;
    private MqttSubscribeRequest subscribeRequest;
    public static final String PUBLISH_TOPIC= "/a1UNvLhFILp/testS/update";
    public static final String SUBSCRIBE_TOPIC = "/a1UNvLhFILp/testS/get";

    public static final String TAG = "TopicHelper";


    public TopicHelper() {
        request = new MqttPublishRequest();
        subscribeRequest = new MqttSubscribeRequest();
    }

    public void publishRequest(String msg) {
        request.isRPC = false;
        request.topic = PUBLISH_TOPIC;
        request.payloadObj = msg;
        LinkKit.getInstance().publish(request, new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
//                发布成功
                showLog("发布成功了");
                showLog("查看响应的数据："+aResponse.data);
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
//                发布失败
                showLog("发布失败了");
            }
        });
    }

    private void showLog(String msg) {
        Log.e(TAG, "发布订阅类 -- showLog: "+msg);
    }

    public String getData() {
        return "that is android msg";
    }

    public void listenerSubscribeRequest() {
        subscribeRequest.topic = SUBSCRIBE_TOPIC;
        subscribeRequest.isSubscribe = true;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
            @Override
            public void onSuccess() {
//                订阅成功
                showLog("订阅成功啦");
            }

            @Override
            public void onFailure(AError aError) {
//                订阅失败
                showLog("订阅失败啦");
            }
        });

    }


    public void cancleSubscribeRequest() {
        LinkKit.getInstance().unsubscribe(subscribeRequest, new IConnectUnscribeListener() {
            @Override
            public void onSuccess() {
//                取消订阅成功
                showLog("取消订阅成功~");
            }

            @Override
            public void onFailure(AError aError) {
//                取消订阅失败
                showLog("取消订阅失败~");
            }
        });
    }


    public void way2() {
//        final MqttRrpcRegisterRequest registerRequest = new MqttRrpcRegisterRequest();
//        registerRequest.topic = SUBSCRIBE_TOPIC;
//        registerRequest.replyTopic = SUBSCRIBE_TOPIC;
////        registerRequest.replyTopic = rrpcReplyTopic;
//
//// 先订阅回复的 replyTopic
//// 云端发布消息到 replyTopic
//// 收到下行数据 回复云端 具体可参考 Demo 同步服务调用
//        LinkKit.getInstance().subscribeRRPC(registerRequest, new IConnectRrpcListener() {
//            @Override
//            public void onSubscribeSuccess(ARequest aRequest) {
//                // 订阅成功
//            }
//
//            @Override
//            public void onSubscribeFailed(ARequest aRequest, AError aError) {
//                // 订阅失败
//            }
//
//            @Override
//            public void onReceived(ARequest aRequest, IConnectRrpcHandle iConnectRrpcHandle) {
//                // 收到云端下行
//                AResponse response = new AResponse();
////                response.data = responseData;
//                iConnectRrpcHandle.onRrpcResponse(registerRequest.topic, response);
//            }
//
//            @Override
//            public void onResponseSuccess(ARequest aRequest) {
//                // RRPC 响应成功
//            }
//
//            @Override
//            public void onResponseFailed(ARequest aRequest, AError aError) {
//                // RRPC 响应失败
//            }
//        });
    }


    IConnectRrpcListener mIConnectRrpcListener = new IConnectRrpcListener() {
        @Override
        public void onSubscribeSuccess(ARequest aRequest) {
            showLog("订阅成功2");
        }

        @Override
        public void onSubscribeFailed(ARequest aRequest, AError aError) {
            showLog("订阅失败");
        }

        @Override
        public void onReceived(ARequest aRequest, IConnectRrpcHandle iConnectRrpcHandle) {
            AResponse response = new AResponse();
            response.data = "这是我响应的数据";

//            iConnectRrpcHandle.onRrpcResponse();
        }

        @Override
        public void onResponseSuccess(ARequest aRequest) {
            showLog("响应成功");
        }

        @Override
        public void onResponseFailed(ARequest aRequest, AError aError) {
            showLog("响应失败");
        }
    };

}
