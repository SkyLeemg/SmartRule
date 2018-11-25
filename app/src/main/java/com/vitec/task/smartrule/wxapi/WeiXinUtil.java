package com.vitec.task.smartrule.wxapi;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WeiXinUtil {

    public static void shareFileToWx(String filePath) {
        WXFileObject fileObject = new WXFileObject();
        fileObject.fileData = inputStreamToByte(filePath);
        fileObject.filePath = filePath;

//        使用媒体消息分享
        WXMediaMessage mediaMessage = new WXMediaMessage(fileObject);
        mediaMessage.title = "测试一下文件分享功能";
//        发送请求
        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        创建唯一标识
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = mediaMessage;
        req.scene = SendMessageToWX.Req.WXSceneSession;


    }

    public static byte[] inputStreamToByte(String path) {
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ch;
            while ((ch = fileInputStream.read()) != -1) {
                byteArrayOutputStream.write(ch);
            }
            byte data[] = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            fileInputStream.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
