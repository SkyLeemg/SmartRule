
package com.vitec.task.smartrule.utils;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class JsonUtils {
//	public static JSONObject initSSLWithHttpClinet(String path)
//			throws ClientProtocolException, IOException {
//		HTTPSTrustManager.allowAllSSL();
//		JSONObject jsonObject = null;
//		int timeOut = 30 * 1000;
//		HttpParams param = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(param, timeOut);
//		HttpConnectionParams.setSoTimeout(param, timeOut);
//		HttpConnectionParams.setTcpNoDelay(param, true);
//
//		SchemeRegistry registry = new SchemeRegistry();
//		registry.register(new Scheme("http", PlainSocketFactory .getSocketFactory(), 80));
//		registry.register(new Scheme("https", TrustAllSSLSocketFactory .getDefault(), 443));
//		ClientConnectionManager manager = new ThreadSafeClientConnManager( param, registry);
//		DefaultHttpClient client = new DefaultHttpClient(manager, param);
//
//		HttpGet request = new HttpGet(path);
//		// HttpGet request = new HttpGet("https://www.alipay.com/");
//		HttpResponse response = client.execute(request);
//		HttpEntity entity = response.getEntity();
//		BufferedReader reader = new BufferedReader(new InputStreamReader( entity.getContent()));
//		StringBuilder result = new StringBuilder();
//		String line = "";
//		while ((line = reader.readLine()) != null) {
//			result.append(line);
//			try {
//				jsonObject = new JSONObject(line);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		return jsonObject;
//	}
}
